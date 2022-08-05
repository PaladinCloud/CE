import { AfterViewInit, Component, Input, OnChanges, OnInit, SimpleChanges, ViewChild } from '@angular/core';
import { FormControl } from '@angular/forms';
import { MatOption } from '@angular/material/core';
import { MatSelect } from '@angular/material/select';
import {MatTableDataSource} from '@angular/material/table';

@Component({
  selector: 'app-table-wrapper',
  templateUrl: './table-wrapper.component.html',
  styleUrls: ['./table-wrapper.component.css']
})
export class TableWrapperComponent implements OnInit,AfterViewInit {

  @Input() data;
  @Input() columnWidths;
  @Input() columnNamesMap;
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
        if(item.value!="0"){
          this.whiteListColumns.push(item.value);
        }
      });
      this.select.options.forEach((item: MatOption) => item.select());
    } else {
      this.whiteListColumns = [];
      this.select.options.forEach((item: MatOption) => item.deselect());
    }
  }

   optionClick() {
    this.whiteListColumns = [];
    let newStatus = true;
    this.select.options.forEach((item: MatOption) => {
      if(item.value!="0"){
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
    if(this.searchInColumns.value==null || (this.searchInColumns.value as any[]).length==0){
      return;
    }   
    let searchTxt = event.target.value.toLowerCase();
    this.dataSource.data = this.mainDataSource.data.filter((item) => {
      for(const i in this.searchInColumns.value) {
        const col = this.searchInColumns.value[i];
        if(String(item[col]).toLowerCase().match(searchTxt)){                    
          return true;
        }
      }
      return false;
    }) 
  }

  constructor() { }

  ngOnInit(): void {
    this.mainDataSource = new MatTableDataSource(this.data);
    this.dataSource = new MatTableDataSource(this.data);
    this.displayedColumns = Object.keys(this.columnWidths);

    this.whiteListColumns = this.displayedColumns;
    this.allSelected=true;
  }

  ngAfterViewInit(): void {  
    if(this.allSelected){
      this.select.options.forEach((item: MatOption) => {
        item.select();
      })
    }
  }
}
