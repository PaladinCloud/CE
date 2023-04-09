import { HttpClientTestingModule } from '@angular/common/http/testing';
import { async, ComponentFixture, TestBed } from '@angular/core/testing';
import { FormsModule } from '@angular/forms';
import { RouterTestingModule } from '@angular/router/testing';
import { AgGridModule } from 'ag-grid-angular';
import { NgxSelectDropdownComponent } from 'ngx-select-dropdown';
import { AssetGroupObservableService } from 'src/app/core/services/asset-group-observable.service';
import { DataCacheService } from 'src/app/core/services/data-cache.service';
import { DomainTypeObservableService } from 'src/app/core/services/domain-type-observable.service';
import { WorkflowService } from 'src/app/core/services/workflow.service';
import { IssueListingService } from 'src/app/pacman-features/services/issue-listing.service';
import { ToastObservableService } from 'src/app/post-login-app/common/services/toast-observable.service';
import { AgGridTableComponent } from 'src/app/shared/ag-grid-table/ag-grid-table.component';
import { BreadcrumbComponent } from 'src/app/shared/breadcrumb/breadcrumb.component';
import { TextComponent } from 'src/app/shared/components/atoms/text/text.component';
import { GenericPageFilterComponent } from 'src/app/shared/generic-page-filter/generic-page-filter.component';
import { SearchableDropdownComponent } from 'src/app/shared/searchable-dropdown/searchable-dropdown.component';
import { CommonResponseService } from 'src/app/shared/services/common-response.service';
import { DownloadService } from 'src/app/shared/services/download.service';
import { ErrorHandlingService } from 'src/app/shared/services/error-handling.service';
import { FilterManagementService } from 'src/app/shared/services/filter-management.service';
import { HttpService } from 'src/app/shared/services/http-response.service';
import { LoggerService } from 'src/app/shared/services/logger.service';
import { RefactorFieldsService } from 'src/app/shared/services/refactor-fields.service';
import { RouterUtilityService } from 'src/app/shared/services/router-utility.service';
import { UtilsService } from 'src/app/shared/services/utils.service';
import { TitleBurgerHeadComponent } from 'src/app/shared/title-burger-head/title-burger-head.component';

import { PolicyViolationsListComponent } from './policy-violations-list.component';

describe('PolicyViolationsListComponent', () => {
  let component: PolicyViolationsListComponent;
  let fixture: ComponentFixture<PolicyViolationsListComponenat>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      imports: [
        HttpClientTestingModule,
        AgGridModule.withComponents([PolicyViolationsListComponent]),
        FormsModule,
        RouterTestingModule,
      ],
      declarations: [
        PolicyViolationsListComponent,
        AgGridTableComponent,
        BreadcrumbComponent,
        GenericPageFilterComponent,
        NgxSelectDropdownComponent,
        SearchableDropdownComponent,
        TextComponent,
        TitleBurgerHeadComponent,
      ],
      providers: [
        AssetGroupObservableService,
        CommonResponseService,
        DataCacheService,
        DomainTypeObservableService,
        DownloadService,
        ErrorHandlingService,
        FilterManagementService,
        HttpService,
        IssueListingService,
        LoggerService,
        RefactorFieldsService,
        RouterUtilityService,
        ToastObservableService,
        UtilsService,
        WorkflowService,
      ],
    }).compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(PolicyViolationsListComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
