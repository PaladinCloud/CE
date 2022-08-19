import { ComponentFixture, TestBed } from '@angular/core/testing';
import { MatTableModule } from '@angular/material/table';

import { TableComponent } from './table.component';

describe('Table', () => {
  let component: TableComponent;
  let fixture: ComponentFixture<TableComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports:[ MatTableModule ],
      declarations: [ TableComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(TableComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();

    component.dataSource.data = [
      {col1:"row1 col1", col2:"row1 col2"},
      {col1:"row2 col1", col2:"row2 col2"},
    ];

    component.displayedColumns = ["col1", "col2"];

    component.columnNamesMap = {col1: "col1", col2: "col2"};

    component.columnWidths = {col1:2, col2:1};

    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should have 2 rows', () => {
    let tableRows = fixture.nativeElement.querySelectorAll('tr');
    expect(tableRows.length-1).toBe(2);
  });

  it("should have 1st col name as col1", () => {
    let colName = fixture.nativeElement.querySelectorAll('tr > th')[0];
    expect(colName.textContent).toContain("col1");
  });

  it("should have 1st cell as row1 col1", () => {
    let firstCell = fixture.nativeElement.querySelectorAll('tr > td')[0];
    expect(firstCell.textContent).toContain("row1 col1");
  });
});
