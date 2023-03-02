import { HttpClientTestingModule } from '@angular/common/http/testing';
import { async, ComponentFixture, TestBed } from '@angular/core/testing';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { RouterTestingModule } from '@angular/router/testing';
import { AssetGroupObservableService } from 'src/app/core/services/asset-group-observable.service';
import { DataCacheService } from 'src/app/core/services/data-cache.service';
import { DomainTypeObservableService } from 'src/app/core/services/domain-type-observable.service';
import { WorkflowService } from 'src/app/core/services/workflow.service';
import { ButtonComponent } from 'src/app/shared/button/button.component';
import { FormsComponent } from 'src/app/shared/forms/forms.component';
import { CommonResponseService } from 'src/app/shared/services/common-response.service';
import { ErrorHandlingService } from 'src/app/shared/services/error-handling.service';
import { FilterManagementService } from 'src/app/shared/services/filter-management.service';
import { FormService } from 'src/app/shared/services/form.service';
import { HttpService } from 'src/app/shared/services/http-response.service';
import { LoggerService } from 'src/app/shared/services/logger.service';
import { RefactorFieldsService } from 'src/app/shared/services/refactor-fields.service';
import { RouterUtilityService } from 'src/app/shared/services/router-utility.service';
import { UtilsService } from 'src/app/shared/services/utils.service';

import { AccountManagementDetailsComponent } from './account-management-details.component';

describe('AccountManagementDetailsComponent', () => {
  let component: AccountManagementDetailsComponent;
  let fixture: ComponentFixture<AccountManagementDetailsComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule, ReactiveFormsModule, FormsModule, RouterTestingModule],
      declarations: [
        AccountManagementDetailsComponent,
        ButtonComponent,
        FormsComponent,
      ],
      providers: [
        AssetGroupObservableService,
        CommonResponseService,
        DataCacheService,
        DomainTypeObservableService,
        ErrorHandlingService,
        FilterManagementService,
        FormService,
        HttpService,
        LoggerService,
        RefactorFieldsService,
        RouterUtilityService,
        WorkflowService,
        UtilsService,
      ],
    }).compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(AccountManagementDetailsComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
