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
  @Output() rowSelectEventEmitter = new EventEmitter<any>();
  @Output() headerColNameSelected = new EventEmitter<any>();

  mainDataSource;
  dataSource;
  
  displayedColumns;
  whiteListColumns = [];
  searchInColumns = new FormControl();

  @ViewChild('select') select: MatSelect;
  @ViewChild('allColumnsSelected') private allColumnsSelected: MatOption;

  allSelected=true;
  
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
  }

  customFilter(event){ 
    let searchTxt = event.target.value.toLowerCase();
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

  constructor(private readonly changeDetectorRef: ChangeDetectorRef) { }

  ngOnInit(): void {
    this.mainDataSource = new MatTableDataSource(this.data);
    this.dataSource = new MatTableDataSource(this.data);
    if(this.columnWidths){
      this.displayedColumns = Object.keys(this.columnWidths);
    }
    this.whiteListColumns = this.displayedColumns;
    this.allSelected=true;
    // this.announceSortChange({active:this.headerColName, direction: this.direction})
  }

  ngAfterViewInit(): void {  
    if(this.allSelected && this.select){
      this.select.options?.forEach((item: MatOption) => {
        item.select();
      })
    }
    this.changeDetectorRef.detectChanges();
  }

  announceSortChange(sort) {
    if (!sort.active || sort.direction === '') {
      this.dataSource.data = this.mainDataSource.data.slice();
      return;
    }

    this.dataSource.data = this.dataSource.data.sort((a, b) => {
      this.headerColName = sort.active;
      this.direction = sort.direction;
      const isAsc = this.direction=='asc';
      if(this.columnsSortFunctionMap[this.headerColName]){
        return this.columnsSortFunctionMap[this.headerColName](a, b, isAsc);
      }
      return (a[this.headerColName]<b[this.headerColName]? -1: 1)*(isAsc ? 1 : -1);
    });
    this.headerColNameSelected.emit({headerColName:this.headerColName, direction:this.direction});
  }
}
