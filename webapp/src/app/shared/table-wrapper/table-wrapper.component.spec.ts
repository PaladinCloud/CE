import { DebugElement } from '@angular/core';
import { ComponentFixture, fakeAsync, TestBed, tick } from '@angular/core/testing';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { MatSelectModule } from '@angular/material/select';
import { MatSortModule } from '@angular/material/sort';
import { MatTableModule } from '@angular/material/table';
import { By } from '@angular/platform-browser';
import { BrowserAnimationsModule } from '@angular/platform-browser/animations';
import { TableComponent } from '../table/table.component';

import { TableWrapperComponent } from './table-wrapper.component';

describe('TableWrapperComponent', () => {
  let component: TableWrapperComponent;
  let fixture: ComponentFixture<TableWrapperComponent>;
  let columnSelector;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [MatTableModule, MatSelectModule, BrowserAnimationsModule, MatSortModule, FormsModule, ReactiveFormsModule],
      declarations: [ TableWrapperComponent, TableComponent ]
    })
    .compileComponents();
  });

  beforeEach(async() => {
    fixture = TestBed.createComponent(TableWrapperComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();

    component.data = [
      {col1:"row1 col1", col2:"row1 col2"},
      {col1:"row2 col1", col2:"row2 col2"},
    ];

    component.displayedColumns = ["col1", "col2"];

    component.columnNamesMap = {col1: "col1", col2: "col2"};

    component.columnWidths = {col1:2, col2:1};

    component.whiteListColumns = ["col1", "col2"];

    component.ngOnInit();

    fixture.detectChanges();

    component.ngAfterViewInit();

    fixture.detectChanges();

    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it("should have datasource and maindatasource defined", () => {
    expect(component.dataSource.data).toBeDefined();
    expect(component.mainDataSource.data).toBeDefined();
  });

  it("should have datasource and maindatasource length 2", () => {
    expect(component.dataSource.data.length).toBe(2);
  });

  it('check the length of drop down', async () => {
    const trigger = fixture.debugElement.queryAll(By.css('.mat-select-trigger'))[1].nativeElement;
    trigger.click();
    fixture.detectChanges();
      await fixture.whenStable().then(() => {
          const inquiryOptions = fixture.debugElement.queryAll(By.css('.mat-option-text'));
          expect(inquiryOptions.length).toEqual(3);
      });
  });

  it("should call optionClick() when an option is clicked from add-remove columns list", fakeAsync(() => {
    spyOn(component, 'optionClick');

    let colButton = fixture.debugElement.nativeElement.querySelectorAll(".mat-select-arrow")[1];
    colButton.click();
    fixture.detectChanges();
    const trigger = fixture.debugElement.queryAll(By.css('.mat-select-trigger'))[1].nativeElement;
    trigger.click();
    fixture.detectChanges();
    tick();
    const inquiryOptions = fixture.debugElement.queryAll(By.css('.mat-option-text'));
    let optionSelected = inquiryOptions[1];
    optionSelected.nativeElement.click();
    fixture.detectChanges();
    tick();
    expect(component.optionClick).toHaveBeenCalled();
  }));

  it("testing optionClick", fakeAsync(() => {
    let colButton = fixture.debugElement.nativeElement.querySelectorAll(".mat-select-arrow")[1];
    colButton.click();
    fixture.detectChanges();
    tick();
    const inquiryOptions = fixture.debugElement.queryAll(By.css('.mat-option-text'));
    let optionSelected = inquiryOptions[1];
    optionSelected.nativeElement.click();
    fixture.detectChanges();
    expect(component.whiteListColumns).not.toContain("col1");
  }))

  it("testing selectAll", fakeAsync(() => {
    const prevAllSelectedVal = component.allSelected;
    let colButton = fixture.debugElement.nativeElement.querySelectorAll(".mat-select-arrow")[1];
    colButton.click();
    fixture.detectChanges();
    tick();
    const inquiryOptions = fixture.debugElement.queryAll(By.css('.mat-option-text'));
    let selectAllOption = inquiryOptions[0];
    selectAllOption.nativeElement.click();
    fixture.detectChanges();
    expect(component.whiteListColumns).toEqual([]);
    expect(component.allSelected).toEqual(!prevAllSelectedVal);
  }));

  it("testing search", () => {
    const inputElement = fixture.debugElement.query(By.css('input')).nativeElement;
    inputElement.value = "row1";
    inputElement.dispatchEvent(new Event('keyup'));
    fixture.detectChanges();
    expect(component.dataSource.data.length).toBe(1);
  })

});
