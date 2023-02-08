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
import { RouterTestingModule } from '@angular/router/testing';
import { TreeModule } from '@circlon/angular-tree-component';
import { AssetGroupObservableService } from 'src/app/core/services/asset-group-observable.service';
import { AssetTilesService } from 'src/app/core/services/asset-tiles.service';
import { DataCacheService } from 'src/app/core/services/data-cache.service';
import { DomainTypeObservableService } from 'src/app/core/services/domain-type-observable.service';
import { PermissionGuardService } from 'src/app/core/services/permission-guard.service';
import { RecentlyViewedObservableService } from 'src/app/core/services/recently-viewed-observable.service';
import { ThemeObservableService } from 'src/app/core/services/theme-observable.service';
import { WorkflowService } from 'src/app/core/services/workflow.service';
import { AwsResourceTypeSelectionService } from 'src/app/pacman-features/services/aws-resource-type-selection.service';
import { CommonResponseService } from 'src/app/shared/services/common-response.service';
import { DownloadService } from 'src/app/shared/services/download.service';
import { ErrorHandlingService } from 'src/app/shared/services/error-handling.service';
import { HttpService } from 'src/app/shared/services/http-response.service';
import { LoggerService } from 'src/app/shared/services/logger.service';
import { RefactorFieldsService } from 'src/app/shared/services/refactor-fields.service';
import { RouterUtilityService } from 'src/app/shared/services/router-utility.service';
import { UtilsService } from 'src/app/shared/services/utils.service';
import { DefaultAssetGroupComponent } from '../../default-asset-group/default-asset-group.component';
import { AssetSwitcherComponent } from '../asset-switcher/asset-switcher.component';
import { ToastObservableService } from '../services/toast-observable.service';

import { ContextualMenuComponent } from './contextual-menu.component';

describe('ContextualMenuComponent', () => {
  let component: ContextualMenuComponent;
  let fixture: ComponentFixture<ContextualMenuComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule, TreeModule, RouterTestingModule],
      declarations: [DefaultAssetGroupComponent, AssetSwitcherComponent, ContextualMenuComponent],
      providers: [
        AssetGroupObservableService,
        AssetTilesService,
        AwsResourceTypeSelectionService,
        CommonResponseService,
        DataCacheService,
        DomainTypeObservableService,
        DownloadService,
        ErrorHandlingService,
        HttpService,
        LoggerService,
        PermissionGuardService,
        RefactorFieldsService,
        RecentlyViewedObservableService,
        RouterUtilityService,
        ThemeObservableService,
        ToastObservableService,
        UtilsService,
        WorkflowService,
      ],
    }).compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(ContextualMenuComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
