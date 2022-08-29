import { AfterViewInit, ChangeDetectorRef, Component, EventEmitter, Input, OnChanges, OnInit, Output, SimpleChanges, ViewChild } from '@angular/core';
import { FormControl } from '@angular/forms';
import { MatOption } from '@angular/material/core';
import { MatSelect } from '@angular/material/select';
import { Sort } from '@angular/material/sort';
import {MatTableDataSource} from '@angular/material/table';

@Component({
  selector: 'app-table',
  templateUrl: './table.component.html',
  styleUrls: ['./table.component.css']
})
export class TableComponent implements OnInit,AfterViewInit {

  @Input() data;
  @Input() columnWidths;
  @Input() columnNamesMap;
  @Input() columnsSortFunctionMap;
  @Input() headerColName;
  @Input() direction;
  @Input() searchQuery = "";
  @Input() showSearchBar;
  @Input() showAddRemoveCol;
  @Input() showTitle;
  @Output() rowSelectEventEmitter = new EventEmitter<any>();
  @Output() headerColNameSelected = new EventEmitter<any>();
  @Output() searchCalledEventEmitter = new EventEmitter<string>();
  @Output() whitelistColumnsChanged = new EventEmitter<any>();
  @Output() searchInColumnsChanged = new EventEmitter<any>();

  mainDataSource;
  dataSource;
  
  displayedColumns;
  @Input() whiteListColumns = [];
  searchInColumns = new FormControl();

  @ViewChild('select') select: MatSelect;
  @ViewChild('allColumnsSelected') private allColumnsSelected: MatOption;

  allSelected=true;

  constructor(private readonly changeDetectorRef: ChangeDetectorRef) { }

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
    if(this.searchQuery!='') this.customFilter(this.searchQuery);
    if(this.headerColName) this.customSort(this.headerColName, this.direction);
  }

  ngAfterViewInit(): void {  
    this.select.options.forEach((item: MatOption) => {
      if((item.value == "selectAll" && this.allSelected) || this.whiteListColumns.includes(item.value)){
        item.select();
      }
    });
    this.changeDetectorRef.detectChanges();
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
    this.whitelistColumnsChanged.emit(this.whiteListColumns);
  }

  customFilter(searchTxt){    
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
  }

  handleSearch(event){ 
    let searchTxt = event.target.value.toLowerCase();
    
    if (event.keyCode === 13 || searchTxt=='') {
      this.customFilter(searchTxt);
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
}
