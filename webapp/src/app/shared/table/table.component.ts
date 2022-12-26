import { AfterViewInit, ChangeDetectorRef, Component, ElementRef, EventEmitter, HostListener, Input, OnChanges, OnInit, Output, SimpleChanges, ViewChild, ViewChildren } from '@angular/core';
import { FormControl } from '@angular/forms';
import { MatOption } from '@angular/material/core';
import { MatSelect } from '@angular/material/select';
import { Sort } from '@angular/material/sort';
import {MatTable, MatTableDataSource} from '@angular/material/table';
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
  @Input() headerColName;
  @Input() direction;
  @Input() searchQuery = "";
  @Input() showSearchBar;
  @Input() showAddRemoveCol;
  @Input() showDownloadBtn;
  @Input() showFilterBtn;
  @Input() tableTitle;
  @Input() imageDataMap = {};
  @Input() filterTypeLabels = [];
  @Input() filterTagLabels= {};
  @Input() tableErrorMessage = '';
  @Input() onScrollDataLoader: Subject<any>;
  @Input() totalRows = 0;
  @Input() tableScrollTop;
  @Input() doLocalSearch = false; // should remove this once we get tiles data from backend.
  @Input() doLocalSort = true;
  @Input() tableDataLoaded;
  @Output() rowSelectEventEmitter = new EventEmitter<any>();
  @Output() headerColNameSelected = new EventEmitter<any>();
  @Output() searchCalledEventEmitter = new EventEmitter<string>();
  @Output() whitelistColumnsChanged = new EventEmitter<any>();
  @Output() searchInColumnsChanged = new EventEmitter<any>();
  @Output() nextPageCalled = new EventEmitter<any>();
  @Output() downloadClicked = new EventEmitter<any>();
  @Output() selectedFilterType = new EventEmitter<any>();
  @Output() selectedFilter = new EventEmitter<any>();
  @Output() deleteFilters = new EventEmitter<any>();

  mainDataSource;
  dataSource;
  
  @Input() displayedColumns;
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
  

  constructor(private readonly changeDetectorRef: ChangeDetectorRef,
    private windowExpansionService: WindowExpansionService) { 
      this.windowExpansionService.getExpansionStatus().subscribe((res) => {
        this.waitAndResizeTable();
      });
    }

  ngOnInit(): void {
    this.mainDataSource = new MatTableDataSource(this.data);
    this.dataSource = new MatTableDataSource(this.data);
    if(this.columnWidths){
      this.displayedColumns = Object.keys(this.columnWidths);
    }
    if(this.displayedColumns.length == this.whiteListColumns.length){
      this.allSelected=true;
    }else{
      this.allSelected=false;
    }
    if(this.searchQuery && this.doLocalSearch){
      this.customSearch(this.searchQuery);
    }
    if(this.onScrollDataLoader){
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
    if(this.customTable && changes.tableScrollTop && changes.tableScrollTop.currentValue!=undefined){      
      this.customTable.first.nativeElement.scrollTop = this.tableScrollTop;
    }
    if(!this.tableDataLoaded && this.customTable){
       this.tableScrollTop = 0;
      this.customTable.first.nativeElement.scrollTop = 0;
      this.data = [];
      this.dataSource = new MatTableDataSource(this.data);
      this.mainDataSource = new MatTableDataSource(this.data);
    }
    if(!this.doLocalSearch || (changes.data && changes.data.currentValue && changes.data.currentValue.length>0)){
      this.mainDataSource = new MatTableDataSource(this.data);
      this.dataSource = new MatTableDataSource(this.data);
      // handles when pagesize is small and screen height is large
      // if(window.innerHeight>1800 && this.data.length>0){
      //   this.nextPageCalled.emit();
      //   this.isDataLoading = true;
      // }
    }
    this.filteredArray.forEach((item, i) => {
      if(item.filterValue.length==0){
        this.filteredArray.splice(i, 1);
      }
    })    
    this.chips = this.filteredArray.map(obj => {return {...obj}}); // cloning filteredArray
    this.chips.splice(2);
    this.totalChips = this.filteredArray.length;
    this.addFilter();
  }

  ngAfterViewInit(): void { 
    this.customTable.first.nativeElement.scrollTop = this.tableScrollTop;
    this.waitAndResizeTable();

    if(this.select){
      this.select.options.forEach((item: MatOption) => {
        if((item.value == "selectAll" && this.allSelected) || this.whiteListColumns.includes(item.value)){
          item.select();
        }
      });
      this.changeDetectorRef.detectChanges();
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
  
  toggleAllSelection() {    
    this.whiteListColumns = [];
    this.allSelected = !this.allSelected;
    if (this.allSelected) {
      this.select.options.forEach((item: MatOption) => {
        if(item.value!="selectAll" && item.value!="disabled"){
          this.whiteListColumns.push(item.value);
        }
      });
      this.select.options.forEach((item: MatOption) => item.select());
    } else {
      this.whiteListColumns = [];
      this.select.options.forEach((item: MatOption) => item.deselect());
    }
    this.whitelistColumnsChanged.emit(this.whiteListColumns);
    this.getWidthFactor();
  }

  goToDetails(row){
    let event = {
      tableScrollTop : this.customTable.first.nativeElement.scrollTop,
      rowSelected: row,
      data: this.data
    }
    this.rowSelectEventEmitter.emit(event);
  }

   optionClick() {
    this.whiteListColumns = [];
    let newStatus = true;
    this.select.options.forEach((item: MatOption) => {
      if(item.value!="selectAll" && item.value!="disabled"){
        if (!item.selected) {
          newStatus = false;
        }else{
          this.whiteListColumns.push(item.value);
        }
      }
    });
    this.allSelected = newStatus;
    if(this.allSelected){
      this.allColumnsSelected.select();
    }else{
      this.allColumnsSelected.deselect();
    }
    this.getWidthFactor();
    this.whitelistColumnsChanged.emit(this.whiteListColumns);
  }

  onSelectFilter(e, i){
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

    let event = {
      index: currIdx,
      filterKeyDisplayValue: this.filteredArray[currIdx].keyDisplayValue,
      filterValue: this.filteredArray[currIdx].filterValue
    }

      this.selectedFilter.emit(event);
  }

  onSelectFilterType(e, i){      
    let key = _.find(this.filterTypeOptions, {
        optionName: e,
      })["optionValue"];
    this.filteredArray[i].compareKey = key.toLowerCase().trim();
    this.filteredArray[i].filterkey = key.trim();
    this.filteredArray[i].key = e;
    this.filteredArray[i].keyDisplayValue = e; 
    this.filteredArray[i].value = undefined;
    this.filteredArray[i].filterValue = "";
    this.selectedFilterType.emit(e);
  }

  addFilter(){
      let obj = {
        keyDisplayValue: "",
        filterValue: ""
      };
      this.filteredArray.push(obj);
      setTimeout(() => this.scrollFilterModalToBottom(true), 1);
  }

  removeFilter(i){
    if(this.filteredArray[i].value==undefined){
      this.filteredArray.splice(i, 1);
      return;
    }
    let event = {
      index: i,
    }
    this.deleteFilters.emit(event);
  }

  removeAllFilters(){
    let event = {
      clearAll: true,
    }
    this.deleteFilters.emit(event);
  }

  customFilter(){

    this.tableErrorMessage = '';
    this.dataSource.data = this.mainDataSource.data.filter((item) => {
      for(const i in this.filteredArray){
        const filterObj = this.filteredArray[i];
        
        const col = filterObj.filter;
        const searchTxt = filterObj.filterText;
        
        if(!String(item[col]).toLowerCase().match(searchTxt.toLowerCase())){
          return false;
        }
      }
      return true;
    })

    if(this.dataSource.data.length==0){
      this.tableErrorMessage = 'noSearchFound';
    }
  }

  customSearch(searchTxt){    
    let columnsToSearchIN = this.searchInColumns.value;
    if(columnsToSearchIN==null || (columnsToSearchIN as any[]).length==0){
      columnsToSearchIN = this.whiteListColumns;
    }   
    this.dataSource.data = this.mainDataSource.data.filter((item) => {
      for(const i in columnsToSearchIN) {
        const col = columnsToSearchIN[i];
        if(String(item[col]).toLowerCase().match(searchTxt)){
          return true;
        }
      }
      return false;
    })

    if(this.dataSource.data.length==0){
      this.tableErrorMessage = 'noSearchFound';
    }
  }

  handleSearch(event){ 
    let searchTxt = event.target.value.toLowerCase();
    
    if (event.keyCode === 13 || searchTxt=='') {
      // this.customTable.first.nativeElement.scrollTop = 0;
      this.tableErrorMessage = '';
      if(this.doLocalSearch){
        this.customSearch(searchTxt);
      }
      this.searchCalledEventEmitter.emit(searchTxt);
    }
  }

  clearSearchText(){
    this.searchQuery = "";
    this.searchCalledEventEmitter.emit(this.searchQuery);
  }

  announceSortChange(sort:any) {
    this.headerColName = sort.active;
    this.direction = sort.direction;
    if(this.doLocalSort){
      this.customSort(this.headerColName, this.direction);
    }
    this.headerColNameSelected.emit({headerColName:this.headerColName, direction:this.direction});
  }

    customSort(columnName, direction){    
    if (!columnName || direction === '') {
      // this.dataSource.data = this.mainDataSource.data.slice();
      return;
    }
    const isAsc = this.direction=='asc';

    this.dataSource.data = this.dataSource.data.sort((a, b) => {
      if(this.columnsSortFunctionMap[this.headerColName]){
        return this.columnsSortFunctionMap[this.headerColName](a, b, isAsc);
      }
      return (a[this.headerColName]<b[this.headerColName]? -1: 1)*(isAsc ? 1 : -1);
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
    this.downloadClicked.emit();
  }
}
