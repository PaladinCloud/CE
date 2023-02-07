import { async, ComponentFixture, TestBed } from '@angular/core/testing';
import { FormsModule } from '@angular/forms';
import { RouterTestingModule } from '@angular/router/testing';
import { AgGridModule } from 'ag-grid-angular';
import { AssetGroupObservableService } from 'src/app/core/services/asset-group-observable.service';
import { DataCacheService } from 'src/app/core/services/data-cache.service';
import { WorkflowService } from 'src/app/core/services/workflow.service';
import { LoggerService } from '../services/logger.service';
import { RefactorFieldsService } from '../services/refactor-fields.service';
import { RouterUtilityService } from '../services/router-utility.service';
import { UtilsService } from '../services/utils.service';
import { TitleBurgerHeadComponent } from '../title-burger-head/title-burger-head.component';

import { AgGridTableComponent } from './ag-grid-table.component';

describe('AgGridTableComponent', () => {
  let component: AgGridTableComponent;
  let fixture: ComponentFixture<AgGridTableComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      imports: [
        RouterTestingModule,
        AgGridModule.withComponents([AgGridTableComponent]),
        FormsModule,
      ],
      declarations: [AgGridTableComponent, TitleBurgerHeadComponent],
      providers: [
        AssetGroupObservableService,
        DataCacheService,
        LoggerService,
        RefactorFieldsService,
        RouterUtilityService,
        UtilsService,
        WorkflowService,
      ],
    }).compileComponents();
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
