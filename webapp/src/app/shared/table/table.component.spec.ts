import { DebugElement } from '@angular/core';
import { ComponentFixture, fakeAsync, TestBed, tick } from '@angular/core/testing';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { MatMenuModule } from '@angular/material/menu';
import { MatSelectModule } from '@angular/material/select';
import { MatSortModule } from '@angular/material/sort';
import { MatTableModule } from '@angular/material/table';
import { By } from '@angular/platform-browser';
import { BrowserAnimationsModule } from '@angular/platform-browser/animations';
import { WindowExpansionService } from 'src/app/core/services/window-expansion.service';

import { TableComponent } from './table.component';

describe('TableComponent', () => {
  let component: TableComponent;
  let fixture: ComponentFixture<TableComponent>;
  let columnSelector;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [
        MatTableModule,
        MatSelectModule,
        MatMenuModule,
        BrowserAnimationsModule,
        MatSortModule,
        FormsModule,
        ReactiveFormsModule,
      ],
      declarations: [TableComponent],
      providers: [WindowExpansionService],
    }).compileComponents();
  });

  beforeEach(async() => {
    fixture = TestBed.createComponent(TableComponent);
    component = fixture.componentInstance;

    component.data = [
      {
        col1: {
          valueText: "row1 col1",
        },
        col2: {
          valueText: "row1 col2",
        },
      },
      {
        col1: {
          valueText: "row2 col1",
        },
        col2: {
          valueText: "row2 col2",
        },
      },
    ];

    component.displayedColumns = ["col1", "col2"];

    component.columnWidths = {col1:2, col2:1};

    component.whiteListColumns = ["col1", "col2"];

    component.showAddRemoveCol = true;
    component.showSearchBar = true;
    component.tableTitle = "Title";

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

  // todo: check why mat-select is not available
  xit('check the length of drop down', async () => {
    const trigger = fixture.debugElement.queryAll(By.css('.mat-select-trigger'))[1].nativeElement;    
    trigger.click();
    fixture.detectChanges();
      await fixture.whenStable().then(() => {
          const inquiryOptions = fixture.debugElement.queryAll(By.css('.mat-option-text'));          
          expect(inquiryOptions.length).toEqual(4);
      });
  });

  xit("should call optionClick() when an option is clicked from add-remove columns list", fakeAsync(() => {
    spyOn(component, 'optionClick');

    let colButton = fixture.debugElement.queryAll(By.css('.mat-select-trigger'))[1].nativeElement;
    colButton.click();
    fixture.detectChanges();
    tick();
    const inquiryOptions = fixture.debugElement.queryAll(By.css('.mat-option-text'));
    let optionSelected = inquiryOptions[2];
    optionSelected.nativeElement.click();
    fixture.detectChanges();
    tick();
    expect(component.optionClick).toHaveBeenCalled();
  }));
  

  xit("tests optionClick", fakeAsync(() => {
    let colButton = fixture.debugElement.queryAll(By.css('.mat-select-trigger'))[1].nativeElement;   
    colButton.click();
    fixture.detectChanges();
    tick();
    const inquiryOptions = fixture.debugElement.queryAll(By.css('.mat-option-text'));
    let optionSelected = inquiryOptions[2];
    optionSelected.nativeElement.click();
    fixture.detectChanges();
    expect(component.whiteListColumns).not.toContain("col1");
  }))

  xit("tests selectAll", fakeAsync(() => {
    const prevAllSelectedVal = component.allSelected;
    let colButton = fixture.debugElement.nativeElement.querySelectorAll(".mat-select-arrow")[1];
    colButton.click();
    fixture.detectChanges();
    tick();
    const inquiryOptions = fixture.debugElement.queryAll(By.css('.mat-option-text'));
    let selectAllOption = inquiryOptions[1];
    selectAllOption.nativeElement.click();
    fixture.detectChanges();
    expect(component.whiteListColumns).toEqual([]);
    expect(component.allSelected).toEqual(!prevAllSelectedVal);
  }));

  xit("tests search", () => {
    const inputElement = fixture.debugElement.query(By.css('input')).nativeElement;
    inputElement.value = "row1";
    inputElement.dispatchEvent(new KeyboardEvent('keyup', {'keyCode': 13}));
    fixture.detectChanges();
    expect(component.dataSource.data.length).toBe(1);
  })

  it("tests sort", () => {
    component.headerColName = "col1";
    component.direction = "asc";

    fixture.detectChanges();

    let colButton = fixture.debugElement.query(By.css('.mat-sort-header-arrow')).nativeElement;   
    colButton.click();
    fixture.detectChanges();

    expect(component.direction).toBe("desc");
  })

});
