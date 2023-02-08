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
import { MatCardModule } from '@angular/material/card';
import { MatMenuModule } from '@angular/material/menu';
import { MatSelectModule } from '@angular/material/select';
import { NoopAnimationsModule } from '@angular/platform-browser/animations';
import { RouterTestingModule } from '@angular/router/testing';
import { AssetGroupObservableService } from 'src/app/core/services/asset-group-observable.service';
import { AssetTilesService } from 'src/app/core/services/asset-tiles.service';
import { DataCacheService } from 'src/app/core/services/data-cache.service';
import { DomainTypeObservableService } from 'src/app/core/services/domain-type-observable.service';
import { WorkflowService } from 'src/app/core/services/workflow.service';
import { AwsResourceTypeSelectionService } from 'src/app/pacman-features/services/aws-resource-type-selection.service';
import { TextComponent } from 'src/app/shared/components/atoms/text/text.component';
import { CustomCardComponent } from 'src/app/shared/components/molecules/custom-card/custom-card.component';
import { OverviewTileComponent } from 'src/app/shared/components/molecules/overview-tile/overview-tile.component';
import { TextWithIconComponent } from 'src/app/shared/components/molecules/text-with-icon/text-with-icon.component';
import { DateSelection } from 'src/app/shared/date-selection/date-selection.component';
import { DropdownComponent } from 'src/app/shared/dropdown/dropdown.component';
import { CommonResponseService } from 'src/app/shared/services/common-response.service';
import { ErrorHandlingService } from 'src/app/shared/services/error-handling.service';
import { HttpService } from 'src/app/shared/services/http-response.service';
import { LoggerService } from 'src/app/shared/services/logger.service';
import { RefactorFieldsService } from 'src/app/shared/services/refactor-fields.service';
import { RouterUtilityService } from 'src/app/shared/services/router-utility.service';
import { UtilsService } from 'src/app/shared/services/utils.service';

import { AssetDashboardComponent } from './asset-dashboard.component';

describe('AssetDashboardComponent', () => {
  let component: AssetDashboardComponent;
  let fixture: ComponentFixture<AssetDashboardComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      imports: [
        FormsModule,
        HttpClientTestingModule,
        MatCardModule,
        MatMenuModule,
        MatSelectModule,
        NoopAnimationsModule,
        RouterTestingModule,
      ],
      declarations: [
        AssetDashboardComponent,
        DateSelection,
        DropdownComponent,
        TextComponent,
        TextWithIconComponent,
        CustomCardComponent,
        OverviewTileComponent,
      ],
      providers: [
        AssetGroupObservableService,
        AssetTilesService,
        AwsResourceTypeSelectionService,
        CommonResponseService,
        DataCacheService,
        DomainTypeObservableService,
        ErrorHandlingService,
        HttpService,
        LoggerService,
        RefactorFieldsService,
        RouterUtilityService,
        UtilsService,
        WorkflowService,
      ],
    }).compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(AssetDashboardComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
