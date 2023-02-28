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
import { AssetGroupObservableService } from 'src/app/core/services/asset-group-observable.service';
import { DataCacheService } from 'src/app/core/services/data-cache.service';
import { WorkflowService } from 'src/app/core/services/workflow.service';
import { TaggingAcrossTargetTypeComponent } from 'src/app/pacman-features/secondary-components/tagging-across-target-type/tagging-across-target-type.component';
import { TaggingAssetsTrendComponent } from 'src/app/pacman-features/secondary-components/tagging-assets-trend/tagging-assets-trend.component';
import { TaggingComplianceTrendComponent } from 'src/app/pacman-features/secondary-components/tagging-compliance-trend/tagging-compliance-trend.component';
import { TaggingInstancesTableComponent } from 'src/app/pacman-features/secondary-components/tagging-instances-table/tagging-instances-table.component';
import { TaggingSummaryComponent } from 'src/app/pacman-features/secondary-components/tagging-summary/tagging-summary.component';
import { TotalTagComplianceComponent } from 'src/app/pacman-features/secondary-components/total-tag-compliance/total-tag-compliance.component';
import { SelectComplianceDropdown } from 'src/app/pacman-features/services/select-compliance-dropdown.service';
import { ToastObservableService } from 'src/app/post-login-app/common/services/toast-observable.service';
import { DataTableComponent } from 'src/app/shared/data-table/data-table.component';
import { DownloadService } from 'src/app/shared/services/download.service';
import { ErrorHandlingService } from 'src/app/shared/services/error-handling.service';
import { HttpService } from 'src/app/shared/services/http-response.service';
import { LoggerService } from 'src/app/shared/services/logger.service';
import { RefactorFieldsService } from 'src/app/shared/services/refactor-fields.service';
import { RouterUtilityService } from 'src/app/shared/services/router-utility.service';
import { UtilsService } from 'src/app/shared/services/utils.service';
import { TitleBurgerHeadComponent } from 'src/app/shared/title-burger-head/title-burger-head.component';

import { TaggingComplianceComponent } from './tagging-compliance.component';

describe('TaggingComplianceComponent', () => {
  let component: TaggingComplianceComponent;
  let fixture: ComponentFixture<TaggingComplianceComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule, FormsModule, RouterTestingModule],
      declarations: [
        TaggingComplianceComponent,
        DataTableComponent,
        TaggingAcrossTargetTypeComponent,
        TaggingAssetsTrendComponent,
        TaggingComplianceTrendComponent,
        TaggingInstancesTableComponent,
        TaggingSummaryComponent,
        TitleBurgerHeadComponent,
        TotalTagComplianceComponent,
      ],
      providers: [
        AssetGroupObservableService,
        DataCacheService,
        DownloadService,
        ErrorHandlingService,
        HttpService,
        LoggerService,
        RefactorFieldsService,
        RouterUtilityService,
        SelectComplianceDropdown,
        ToastObservableService,
        UtilsService,
        WorkflowService,
      ],
    }).compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(TaggingComplianceComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
