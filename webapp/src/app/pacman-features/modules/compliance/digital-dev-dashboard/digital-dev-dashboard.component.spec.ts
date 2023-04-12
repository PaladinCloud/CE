/*
 *Copyright 2018 T Mobile, Inc. or its affiliates. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); You may not use
 * this file except in compliance with the License. A copy of the License is located at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * or in the "license" file accompanying this file. This file is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, express or
 * implied. See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
import { DevPullRequestApplicationsComponent } from 'src/app/pacman-features/secondary-components/dev-pull-request-applications/dev-pull-request-applications.component';
import { DevStaleBranchApplicationsComponent } from 'src/app/pacman-features/secondary-components/dev-stale-branch-applications/dev-stale-branch-applications.component';
import { DevStandardPullRequestAgeComponent } from 'src/app/pacman-features/secondary-components/dev-standard-pull-request-age/dev-standard-pull-request-age.component';
import { DevStandardStaleBranchAgeComponent } from 'src/app/pacman-features/secondary-components/dev-standard-stale-branch-age/dev-standard-stale-branch-age.component';
import { DevStandardTotalStaleBranchesComponent } from 'src/app/pacman-features/secondary-components/dev-standard-total-stale-branches/dev-standard-total-stale-branches.component';
import { DigitalApplicationDistributionComponent } from 'src/app/pacman-features/secondary-components/digital-application-distribution/digital-application-distribution.component';
import { DigitalDevStrategyDistributionComponent } from 'src/app/pacman-features/secondary-components/digital-dev-strategy-distribution/digital-dev-strategy-distribution.component';
import { PullRequestLineMetricsComponent } from 'src/app/pacman-features/secondary-components/pull-request-line-metrics/pull-request-line-metrics.component';
import { BreadcrumbComponent } from 'src/app/shared/breadcrumb/breadcrumb.component';
import { TextComponent } from 'src/app/shared/components/atoms/text/text.component';
import { FilteredSelectorComponent } from 'src/app/shared/filtered-selector/filtered-selector.component';
import { GenericPageFilterComponent } from 'src/app/shared/generic-page-filter/generic-page-filter.component';
import { SearchableDropdownComponent } from 'src/app/shared/searchable-dropdown/searchable-dropdown.component';
import { ErrorHandlingService } from 'src/app/shared/services/error-handling.service';
import { FilterManagementService } from 'src/app/shared/services/filter-management.service';
import { HttpService } from 'src/app/shared/services/http-response.service';
import { LoggerService } from 'src/app/shared/services/logger.service';
import { RefactorFieldsService } from 'src/app/shared/services/refactor-fields.service';
import { RouterUtilityService } from 'src/app/shared/services/router-utility.service';
import { UtilsService } from 'src/app/shared/services/utils.service';
import { TitleBurgerHeadComponent } from 'src/app/shared/title-burger-head/title-burger-head.component';
import { WidgetSectionStarterComponent } from 'src/app/shared/widget-section-starter/widget-section-starter.component';

import { DigitalDevDashboardComponent } from './digital-dev-dashboard.component';

describe('DigitalDevDashboardComponent', () => {
  let component: DigitalDevDashboardComponent;
  let fixture: ComponentFixture<DigitalDevDashboardComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      imports: [
        AgGridModule,
        HttpClientTestingModule,
        FormsModule,
        RouterTestingModule,
      ],
      declarations: [
        DigitalDevDashboardComponent,
        BreadcrumbComponent,
        DevPullRequestApplicationsComponent,
        DevStandardPullRequestAgeComponent,
        DevStandardStaleBranchAgeComponent,
        DevStandardTotalStaleBranchesComponent,
        DevStaleBranchApplicationsComponent,
        DigitalApplicationDistributionComponent,
        DigitalDevStrategyDistributionComponent,
        FilteredSelectorComponent,
        GenericPageFilterComponent,
        NgxSelectDropdownComponent,
        PullRequestLineMetricsComponent,
        SearchableDropdownComponent,
        TextComponent,
        TitleBurgerHeadComponent,
        WidgetSectionStarterComponent,
      ],
      providers: [
        AssetGroupObservableService,
        DataCacheService,
        DomainTypeObservableService,
        FilterManagementService,
        ErrorHandlingService,
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
    fixture = TestBed.createComponent(DigitalDevDashboardComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
