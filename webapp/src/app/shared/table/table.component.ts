import { AfterContentChecked, AfterViewChecked, AfterViewInit, ChangeDetectorRef, Component, DoCheck, EventEmitter, HostListener, Input, OnChanges, OnInit, Output, SimpleChanges, ViewChild } from '@angular/core';
import { FormControl } from '@angular/forms';
import { MatOption } from '@angular/material/core';
import { MatSelect } from '@angular/material/select';
import { Sort } from '@angular/material/sort';
import {MatTableDataSource} from '@angular/material/table';
import * as _ from 'lodash';
import { WindowExpansionService } from 'src/app/core/services/window-expansion.service';

@Component({
  selector: 'app-table',
  templateUrl: './table.component.html',
  styleUrls: ['./table.component.css']
})
export class TableComponent implements OnInit,AfterViewInit {

  @Input() data;
  @Input() columnWidths;
  @Input() columnsSortFunctionMap;
  @Input() headerColName;
  @Input() direction;
  @Input() searchQuery = "";
  @Input() showSearchBar;
  @Input() showAddRemoveCol;
  @Input() tableTitle;
  @Input() imageDataMap = {};
  @Input() filterTypeLabels = [];
  @Input() filterTagLabels= {};
  tableErrorMessage = '';
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
  @Output() testFilteredArray = new EventEmitter<any>();
  
  mainDataSource;
  dataSource;
  
  @Input() displayedColumns;
  @Input() whiteListColumns = [];
  searchInColumns = new FormControl();

  @ViewChild('select') select: MatSelect;
  @ViewChild('allColumnsSelected') private allColumnsSelected: MatOption;

  allSelected=true;
  screenWidth;
  denominator;
  screenWidthFactor;
  removeFromScreenWidth = 320;
  isWindowExpanded = true;

  showFilterModal = false;
  @Input() filteredArray;
  @Input() filterTypeOptions;
  

  constructor(private readonly changeDetectorRef: ChangeDetectorRef,
    private windowExpansionService: WindowExpansionService) { 
      this.windowExpansionService.getExpansionStatus().subscribe((res) => {
        this.isWindowExpanded = res;
        this.getScreenWidthFactor();
      });
    }

  ngOnInit(): void {
    console.log("OnInit: ", this.filteredArray);
    
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
    // if(this.searchQuery && this.showSearchBar) this.customSearch(this.searchQuery);
    if(this.headerColName) this.customSort(this.headerColName, this.direction);

    this.screenWidth = window.innerWidth;
    this.getWidthFactor();
    // this.addFilter();
    // this.nextPageCalled.emit();
  }

  ngAfterViewInit(): void {  
    if(this.select){
      this.select.options.forEach((item: MatOption) => {
        if((item.value == "selectAll" && this.allSelected) || this.whiteListColumns.includes(item.value)){
          item.select();
        }
      });
      this.changeDetectorRef.detectChanges();
    }
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
    if(this.isWindowExpanded){
      this.screenWidthFactor = (this.screenWidth - this.removeFromScreenWidth) / this.denominator;
    }else{
      this.screenWidthFactor = (this.screenWidth - 140) / this.denominator;
    }
  }

  @HostListener('window:resize', ['$event'])
  onWindowResize() {
    this.screenWidth = window.innerWidth;
    this.getScreenWidthFactor();
  }

  handleSearchInColumnsChange(){
    this.searchInColumnsChanged.emit(this.searchInColumns.value);
  }

  testFilterArr(){
    console.log("table testFilteredArray", this.filteredArray);
    
    this.testFilteredArray.emit();
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
    this.rowSelectEventEmitter.emit(row);
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

  onSelectFilterType(e, i){  
    let key = _.find(this.filterTypeOptions, {
        optionName: e,
      })["optionValue"];
    this.filteredArray[i].compareKey = key;
    this.filteredArray[i].filterkey = key;
    this.filteredArray[i].key = e;
    this.filteredArray[i].keyDisplayValue = e;  
    this.selectedFilterType.emit(e);
  }

  onSelectFilter(e, i){
    let filterIndex = _.findIndex(this.filteredArray, (el) => {
      return (
        el["keyDisplayValue"] ===
        this.filteredArray[i].keyDisplayValue
      );
    });
    let currIdx = i;
    
    if(filterIndex>=0 && filterIndex!=i){
      if(filterIndex>i){
        //remove filterIndex
        this.filteredArray.splice(filterIndex, 1);
        currIdx = i;
      }else{
        //remove i
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

    // this.filteredArray.splice(i, 1);
    // if (e.keyCode === 13) { 
      this.selectedFilter.emit(event);
    // }    
  }

  addFilter(){
    let obj = {
      key: "",
      value: "",
      filterkey: "",
      compareKey: "",
      keyDisplayValue: "",
      filterValue: ""
    };
    this.filteredArray.push(obj);
  }

  removeFilter(i){
    // this.filterArr.splice(i, 1);
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
      this.tableErrorMessage = ''
      // this.customSearch(searchTxt);
      // this.customSort(this.headerColName, this.direction);
      this.searchCalledEventEmitter.emit(searchTxt);
    }
  }

  announceSortChange(sort) {
    this.customSort(sort.active, sort.direction);
    this.headerColNameSelected.emit({headerColName:this.headerColName, direction:this.direction});
  }

  customSort(columnName, direction){    
    if (!columnName || direction === '') {
      this.dataSource.data = this.mainDataSource.data.slice();
      return;
    }

    this.headerColName = columnName;
    this.direction = direction;
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
    if (event.target.offsetHeight + event.target.scrollTop >= event.target.scrollHeight) {
      this.nextPageCalled.emit();
      this.mainDataSource = new MatTableDataSource(this.data);
      this.dataSource = new MatTableDataSource(this.data);
      // if(this.searchQuery && this.showSearchBar) this.customSearch(this.searchQuery);
      if(this.headerColName) this.customSort(this.headerColName, this.direction);

      setTimeout(() => {}, 1000);
    }
  }

  download(){
    this.downloadClicked.emit();
  }

  changeFilterType(filterType) {
    this.selectedFilterType.emit(filterType);
  }

  changeFilterTags(filterName) {
    this.selectedFilter.emit(filterName);
    // this.trigger.closeMenu();
  }
}
