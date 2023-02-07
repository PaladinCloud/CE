import { async, ComponentFixture, TestBed } from '@angular/core/testing';
import { RefactorFieldsService } from '../services/refactor-fields.service';

import { AgGridTableComponent } from './ag-grid-table.component';

describe('AgGridTableComponent', () => {
  let component: AgGridTableComponent;
  let fixture: ComponentFixture<AgGridTableComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ AgGridTableComponent ],
      providers: [ RefactorFieldsService ],
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(AgGridTableComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
