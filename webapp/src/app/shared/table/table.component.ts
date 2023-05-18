import { AfterViewInit, ChangeDetectorRef, Component, ElementRef, EventEmitter, HostListener, Input, OnChanges, OnInit, Output, SimpleChanges, ViewChild, ViewChildren } from '@angular/core';
import { FormControl } from '@angular/forms';
import { MatOption } from '@angular/material/core';
import { MatSelect } from '@angular/material/select';
import {MatTableDataSource} from '@angular/material/table';
import * as _ from 'lodash';
import { Subject } from 'rxjs';
import { WindowExpansionService } from 'src/app/core/services/window-expansion.service';

@Component({
  selector: 'app-table',
  templateUrl: './table.component.html',
  styleUrls: ['./table.component.css']
})
export class TableComponent implements OnInit,AfterViewInit, OnChanges {

  @Input() data = [];
  @Input() columnWidths;
  @Input() columnsSortFunctionMap;
  @Input() centeredColumns: { [key: string]: boolean } = {};
  @Input() headerColName;
  @Input() direction;
  @Input() searchQuery = "";
  @Input() showSearchBar;
  @Input() showAddRemoveCol;
  @Input() showDownloadBtn;
  @Input() showFilterBtn;
  @Input() showMoreMenu = true;
  @Input() rowClickable = true;
  @Input() tableTitle;
  @Input() imageDataMap = {};
  @Input() filterTypeLabels = [];
  @Input() filterTagLabels= {};
  @Input() tableErrorMessage = '';
  @Input() onScrollDataLoader: Subject<any>;
  @Input() totalRows = 0;
  @Input() tableScrollTop;
  @Input() doLocalSearch = false;
  @Input() doLocalSort = true;
  @Input() doLocalFilter = false;
  @Input() tableDataLoaded;
  @Input() doNotSort = false;
  @Input() rowSize: 'SM' | 'MD' | 'LG' = 'SM';
  @Input() gapBetweenFilterAndTable;
  @Input() showCopyButton = true;
  @Input() selectedRowIndex;
  @Output() rowSelectEventEmitter = new EventEmitter<any>();
  @Output() actionSelected = new EventEmitter();
  @Output() headerColNameSelected = new EventEmitter<any>();
  @Output() searchCalledEventEmitter = new EventEmitter<string>();
  @Output() searchEventEmitter = new EventEmitter<string>();
  @Output() whitelistColumnsChanged = new EventEmitter<any>();
  @Output() searchInColumnsChanged = new EventEmitter<any>();
  @Output() nextPageCalled = new EventEmitter<any>();
  @Output() downloadClicked = new EventEmitter<any>();
  @Output() selectedFilterType = new EventEmitter<any>();
  @Output() selectedFilter = new EventEmitter<any>();
  @Output() deleteFilters = new EventEmitter<any>();

  mainDataSource;
  dataSource;

  displayedColumns;
  @Input() whiteListColumns = [];
  searchInColumns = new FormControl();

  @ViewChild('select') select: MatSelect;
  @ViewChild('allColumnsSelected') private allColumnsSelected: MatOption;
  @ViewChildren('customTable') customTable: any;
  @ViewChild("tableContainer") tableContainer: ElementRef;
  @ViewChild("filtersContainer") filtersContainer: ElementRef;

  allSelected=true;
  screenWidth;
  denominator;
  screenWidthFactor;
  isWindowExpanded = true;
  isDataLoading = false;


  @Input() filteredArray = [];
  @Input() filterTypeOptions;
  totalChips;
  chips;
  selectedFiltersList = [];


  constructor(private readonly changeDetectorRef: ChangeDetectorRef,
    private windowExpansionService: WindowExpansionService,
    ) {
      this.windowExpansionService.getExpansionStatus().subscribe((res) => {
        this.waitAndResizeTable();
      });
    }

  ngOnInit(): void {
    
    if (this.onScrollDataLoader) {
      this.onScrollDataLoader.subscribe(data => {
      this.isDataLoading = false;
        if(data && data.length>0){
          this.data.push(...data);
          this.mainDataSource = new MatTableDataSource(this.data);
          this.dataSource = new MatTableDataSource(this.data);
        }
    })
    }
  }

  ngOnChanges(changes: SimpleChanges): void {

    if(this.customTable){
      if(changes.tableScrollTop && changes.tableScrollTop.currentValue!=undefined){
        this.customTable.first.nativeElement.scrollTop = this.tableScrollTop;
      }
      if(!this.tableDataLoaded){
        this.tableScrollTop = 0;
        // this.data = []; in a race condition, this may empty data after data is loaded
        this.customTable.first.nativeElement.scrollTop = 0;
        this.mainDataSource = new MatTableDataSource([]);
        this.dataSource = new MatTableDataSource([]);
      }
    }
    if(changes.columnWidths){
      if(this.columnWidths){
        this.displayedColumns = Object.keys(this.columnWidths);  
      }    
      if(this.displayedColumns.length == this.whiteListColumns.length){
        this.allSelected=true;
      }else{
        this.allSelected=false;
      }
      // if(this.displayedColumns[this.displayedColumns.length-1].toLowerCase() == 'actions'){
      //   this.displayedColumns.pop();
      // }
  
      this.displayedColumns.sort();

      if(this.select){
        this.select.options.forEach((item: MatOption) => {
          if(this.allSelected || this.whiteListColumns.includes(item.value)){
            item.select();
          }else{
            item.deselect();
          }
        });
      }
    }
    if((changes.data || changes.filteredArray)){
      if(changes.data){
        this.mainDataSource = new MatTableDataSource(this.data);
        this.dataSource = new MatTableDataSource(this.data);
  
        this.waitAndResizeTable();
      }    
      // this.filteredArray.forEach((item, i) => {
      //   if(item.filterValue.length==0){
      //     this.filteredArray.splice(i, 1);
      //   }
      // })
      this.selectedFiltersList = this.filteredArray.map(item => item.keyDisplayValue);
      if(!this.doLocalFilter){
        this.chips = this.filteredArray.map(obj => {return {...obj}}); // cloning filteredArray
        this.chips.splice(2);
        this.totalChips = this.filteredArray.length;
        this.addFilter();
      }
    }
    if((this.doLocalSearch || this.doLocalSort) && this.tableDataLoaded){
      this.filterAndSort();
    }
  }

  ngAfterViewInit(): void {
    this.customTable.first.nativeElement.scrollTop = this.tableScrollTop;
    this.waitAndResizeTable();
  }

  filterAndSort(){
    if(this.doLocalSearch){
      this.customSearch();
    }
    if(this.headerColName && this.direction && this.doLocalSort){
      this.customSort(this.headerColName, this.direction);
    }
  }

  scrollFilterModalToBottom(forceScroll?){
    if(this.filtersContainer){
      if(this.totalChips > 2 || forceScroll){
        this.filtersContainer.nativeElement.scrollTop = this.filtersContainer.nativeElement.scrollHeight ;
      }
    }
  }

  waitAndResizeTable(){
    setTimeout(() => {
      this.screenWidth = parseInt(window.getComputedStyle(this.tableContainer.nativeElement, null).getPropertyValue('width'), 10);
      this.getWidthFactor();
    }, 1000);
  }

  openColumnSelectorModal(){
    this.select.open();
  }

  getWidthFactor(){
    this.denominator = 0;
    for(let i in this.whiteListColumns){
      let col = this.whiteListColumns[i];
      this.denominator += this.columnWidths[col];
    }
    this.getScreenWidthFactor();
  }

  getScreenWidthFactor(){
      this.screenWidthFactor = (this.screenWidth - 30) / this.denominator;
  }

  @HostListener('window:resize', ['$event'])
  onWindowResize() {
    this.waitAndResizeTable();
  }

  handleSearchInColumnsChange(){
    this.searchInColumnsChanged.emit(this.searchInColumns.value);
  }

  whiteListColumnsChanged(){
    this.whitelistColumnsChanged.emit(this.whiteListColumns);
  }

  handleClick(row, col, i){
    if(row[col].isMenuBtn){
      return;
    }
    let event = {
      tableScrollTop : this.customTable.first.nativeElement.scrollTop,
      rowSelected: row,
      data: this.data,
      col: col,
      filters: this.filteredArray,
      searchTxt: this.searchQuery,
      selectedRowIndex: i
    }
    this.rowSelectEventEmitter.emit(event);
  }

  handleAction(element, action, i){
    let event = {
      action: action,
      rowSelected: element,
      selectedRowIndex: i
    }
    this.actionSelected.emit(event);
  }

  handleColumnSelection(e){
    const cols = [];
    const shouldIncludeActions = this.whiteListColumns.includes("Actions");
    const allCols = Object.keys(this.columnWidths);
    allCols.forEach(col => {
      if(e.includes(col)){
        cols.push(col);
      }
    })
    this.whiteListColumns = cols;
    // if(shouldIncludeActions) this.whiteListColumns.push("Actions");
    this.getWidthFactor();
    this.waitAndResizeTable();
    this.whiteListColumnsChanged();
  }

  handleFilterDropdownOpen(){
    this.selectedFiltersList = [];
    this.filteredArray.forEach(filter => {
      this.selectedFiltersList.push(filter.keyDisplayValue);
    })
  }

  filterOptionClick(e){
    const filteredArrayKeys = this.filteredArray.map(item => item.keyDisplayValue);

    if(!filteredArrayKeys.includes(e)){
      // add to filteredArray
      this.selectedFiltersList.push(e);
      this.filteredArray.push({
        keyDisplayValue: e,
        filterValue: undefined
      })
      this.onSelectFilterType(e, this.filteredArray.length-1);
    }else{
      // remove from filteredArray
      const filterIdx = this.selectedFiltersList.indexOf(e);
      // warning: this may give wierd results if filteredArray keys and selectedFiltersList are not in sync
      this.removeFilter(filterIdx);
    }
  }

  onSelectFilter(e, i){
    if(!e){
      this.selectedFiltersList.splice(i, 1);
      this.removeFilter(i);
      return;
    }
    
    let filterIndex = _.findIndex(this.filteredArray, (el, j) => {
      return (
        el["keyDisplayValue"] ===
        this.filteredArray[i].keyDisplayValue && i!=j
      );
    });
    let currIdx = i;

    if(filterIndex>=0 && filterIndex!=i){
      if(filterIndex>i){
        this.filteredArray.splice(filterIndex, 1);
        currIdx = i;
      }else{
        this.filteredArray.splice(i, 1);
        currIdx = filterIndex;
      }
    }

    this.filteredArray[currIdx].filterValue = e;
    this.filteredArray[currIdx].value = e;

    if(this.doLocalFilter){
      this.filterAndSort();
    }else{
    }
    let event = {
      index: currIdx,
      filterKeyDisplayValue: this.filteredArray[currIdx].keyDisplayValue,
      filterValue: this.filteredArray[currIdx].filterValue
    }
    this.selectedFilter.emit(event);
  }

  onSelectFilterType(e, i){
    if(this.doLocalFilter){
      this.filteredArray[i].key = e;
      this.filteredArray[i].keyDisplayValue = e;
      this.filteredArray[i].value = undefined;
    }else{
    let key = _.find(this.filterTypeOptions, {
        optionName: e,
      })["optionValue"];
    this.filteredArray[i].compareKey = key.toLowerCase().trim();
    this.filteredArray[i].filterkey = key.trim();
    this.filteredArray[i].key = e;
    this.filteredArray[i].keyDisplayValue = e;
    this.filteredArray[i].value = undefined;
    this.filteredArray[i].filterValue = undefined;
  }
  this.selectedFilterType.emit(e);
  }

  addFilter(){
      // let obj = {
      //   keyDisplayValue: "",
      //   filterValue: ""
      // };
      // this.filteredArray.push(obj);
      // setTimeout(() => this.scrollFilterModalToBottom(true), 1);
  }

  removeFilter(i){
    this.selectedFiltersList.splice(i, 1);
    // if(this.filteredArray[i].value==undefined){
    //   this.filteredArray.splice(i, 1);
    //   return;
    // }

    if(this.doLocalFilter){
      this.filteredArray.splice(i, 1);
      this.filterAndSort();
      // return;
    }
    let event = {
      index: i,
    }
    this.deleteFilters.emit(event);
  }

  removeAllFilters(){
    this.selectedFiltersList = [];
    if(this.doLocalFilter){
      this.filteredArray = [];
      this.filterAndSort();
    }
    let event = {
      clearAll: true,
    }
    this.deleteFilters.emit(event);
  }

  customFilter(){
    this.tableErrorMessage = '';
    this.dataSource.data = this.dataSource.data.filter((item) => {
      for(let i=0; i<this.filteredArray.length; i++){
        const filterObj = this.filteredArray[i];

        const filterKey = filterObj.keyDisplayValue;
        const filterValue = String(filterObj.filterValue);

        if(filterValue=="undefined"){
          continue;
        }

        if(filterKey && filterValue){
          const cellValue = item[filterKey].valueText;
          if(filterValue=="0%-25%" || filterValue=="26%-50%" || filterValue=="51%-75%" || filterValue=="76%-100%"){
            const cv = cellValue.substring(0, cellValue.length-1);
            const cv_f = parseFloat(cv);
            if(isNaN(cv_f)) return false;
            if(filterValue=="0%-25%" && !(cv_f>=0 && cv_f<=25)) return false;
            if(filterValue=="26%-50%" && !(cv_f>=26 && cv_f<=50)) return false;
            if(filterValue=="51%-75%" && !(cv_f>=51 && cv_f<=75)) return false;
            if(filterValue=="76%-100%" && !(cv_f>=76 && cv_f<=100)) return false;
          }
          else if(!(String(cellValue).toLowerCase()==filterValue.toLowerCase())){
            return false;
          }
        }else{
          // this.filteredArray.splice(i, 1);
        }
      }
      return true;
    })

    this.chips = this.filteredArray.map(obj => {return {...obj}}); // cloning filteredArray
    this.chips = this.chips.filter(obj => obj.keyDisplayValue && obj.filterValue);
    this.totalChips = this.chips.length;
    this.chips.splice(2);

    this.totalRows = this.dataSource.data.length;
    if(this.dataSource.data.length==0){
      this.tableErrorMessage = 'noDataAvailable';
    }else{
      this.addFilter();
    }
  }

  customSearch(){
    const searchTxt = this.searchQuery;
    // whenever search or filter is called, we perform search first and then filter and thus we take maindatasource here for search
    this.dataSource.data = this.mainDataSource.data.filter((item) => {
      const columnsToSearchIN = Object.keys(item);
      for(const i in columnsToSearchIN) {
        const col = columnsToSearchIN[i];
        if(String(item[col].valueText).toLowerCase().match(searchTxt)){
          return true;
        }
      }
      return false;
    })
    if(this.dataSource.data.length==0){
      this.tableErrorMessage = 'noDataAvailable';
    }
    this.totalRows = this.dataSource.data.length;

    if(this.doLocalFilter){
      this.customFilter();
    }
  }

  handleSearch(event){
    let searchTxt = event.target.value.toLowerCase();
    this.searchQuery = searchTxt;

    if (event.keyCode === 13 || searchTxt=='') {
      // this.customTable.first.nativeElement.scrollTop = 0;
      this.tableErrorMessage = '';
      if(this.doLocalSearch){
        this.filterAndSort();
      }
      this.searchCalledEventEmitter.emit(searchTxt);
    }
  }

  clearSearchTextAndFilters() {
    this.searchQuery = '';
    if (this.tableErrorMessage === 'noDataAvailable') {
      this.tableErrorMessage = '';
    }
    this.removeAllFilters();
    if (!this.doLocalSearch) {
      this.searchCalledEventEmitter.emit(this.searchQuery);
    }
  }

  announceSortChange(sort: any) {
    if(this.doNotSort){
      return;
    }
    this.headerColName = sort.active;
    this.direction = sort.direction;
    this.headerColNameSelected.emit({headerColName:this.headerColName, direction:this.direction});

  }

  customSort(columnName, direction) {
    if (!columnName || direction === '' || this.doNotSort) {
      // this.dataSource.data = this.mainDataSource.data.slice();
      return;
    }
    const isAsc = this.direction == 'asc';
 
    this.dataSource.data = this.dataSource.data.sort((a, b) => {
      if(this.columnsSortFunctionMap && this.columnsSortFunctionMap[this.headerColName]){
        return this.columnsSortFunctionMap[this.headerColName](a, b, isAsc);
      }

      const elementA =a[this.headerColName];
      const elementB =b[this.headerColName]

      if(!isNaN(parseFloat(elementA.valueText)) || !isNaN(parseFloat(elementB.valueText))){
        if(typeof elementA.valueText=="number" || typeof elementB.valueText=="number"){
          return (elementA.valueText-elementB.valueText)*(isAsc ? 1 : -1);
        }

        return (parseFloat(elementA.valueText)-parseFloat(elementB.valueText))*(isAsc ? 1 : -1);
      }
      
      let elementAValue =elementA&&elementA.valueText?elementA.valueText.toLowerCase():isAsc?'zzzzzz':'000000';
      let elementBValue =elementB&&elementB.valueText?elementB.valueText.toLowerCase():isAsc?'zzzzzz':'000000';

      return (elementAValue<elementBValue? -1: 1)*(isAsc ? 1 : -1);
    });
  }

  onScroll(event: any) {
    // visible height + pixel scrolled >= total height
    if (event.target.offsetHeight + event.target.scrollTop >= event.target.scrollHeight - 10) {
      if(this.data.length<this.totalRows && !this.isDataLoading && this.data.length>0) {
        this.tableScrollTop = event.target.scrollTop;
        this.nextPageCalled.emit(this.tableScrollTop);
        this.isDataLoading = true;
      }
    }
  }

  download(){
    const event = {
      searchTxt: this.searchQuery,
      filters: this.filteredArray
    }
    this.downloadClicked.emit(event);
  }
}