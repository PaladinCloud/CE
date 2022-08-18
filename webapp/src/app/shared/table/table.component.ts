import { AfterViewInit, Component, EventEmitter, Input, OnInit, Output, ViewChild } from '@angular/core';
import { MatSort, Sort } from '@angular/material/sort';
import { MatTableDataSource } from '@angular/material/table';

@Component({
  selector: 'app-table',
  templateUrl: './table.component.html',
  styleUrls: ['./table.component.css']
})
export class TableComponent implements OnInit, AfterViewInit {

  @Input() dataSource: MatTableDataSource<any>;
  @Input() columnWidths;
  @Input() displayedColumns;
  @Input() columnNamesMap;
  @Input() columnsSortFunctionMap;
  @Output() rowSelectEventEmitter = new EventEmitter<any>();
  dataSourceCopy : MatTableDataSource<any>;

  constructor() {}

  @ViewChild(MatSort) sort: MatSort;

  ngAfterViewInit() {
  }
  
  announceSortChange(sort: Sort) {
    if (!sort.active || sort.direction === '') {
      this.dataSource.data = this.dataSourceCopy.data.slice();
      return;
    }

    this.dataSource.data = this.dataSource.data.sort((a, b) => {
      const colToSort = sort.active;
      const isAsc = sort.direction=='asc';
      if(this.columnsSortFunctionMap[colToSort]){
        return this.columnsSortFunctionMap[colToSort](a, b, isAsc);
      }
      return (a[colToSort]<b[colToSort]? -1: 1)*(isAsc ? 1 : -1);
    });
  }

  goToDetails(row){
    this.rowSelectEventEmitter.emit(row);
  }

  ngOnInit(): void {
    this.dataSourceCopy = new MatTableDataSource(this.dataSource.data.slice());    
  }
}