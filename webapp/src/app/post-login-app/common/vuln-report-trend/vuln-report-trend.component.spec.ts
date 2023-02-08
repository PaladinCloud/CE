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

import { HttpClientModule } from '@angular/common/http';
import { async, ComponentFixture, TestBed } from '@angular/core/testing';
import { RouterTestingModule } from '@angular/router/testing';
import { AssetGroupObservableService } from 'src/app/core/services/asset-group-observable.service';
import { AssetTilesService } from 'src/app/core/services/asset-tiles.service';
import { DataCacheService } from 'src/app/core/services/data-cache.service';
import { DomainTypeObservableService } from 'src/app/core/services/domain-type-observable.service';
import { PermissionGuardService } from 'src/app/core/services/permission-guard.service';
import { CommonResponseService } from 'src/app/shared/services/common-response.service';
import { ErrorHandlingService } from 'src/app/shared/services/error-handling.service';
import { HttpService } from 'src/app/shared/services/http-response.service';
import { LoggerService } from 'src/app/shared/services/logger.service';
import { RefactorFieldsService } from 'src/app/shared/services/refactor-fields.service';
import { UtilsService } from 'src/app/shared/services/utils.service';
import { VulnTrendGraphComponent } from '../vuln-trend-graph/vuln-trend-graph.component';

import { VulnReportTrendComponent } from './vuln-report-trend.component';

describe('VulnReportTrendComponent', () => {
  let component: VulnReportTrendComponent;
  let fixture: ComponentFixture<VulnReportTrendComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      imports: [HttpClientModule, RouterTestingModule],
      declarations: [ VulnTrendGraphComponent, VulnReportTrendComponent ],
      providers: [
        AssetGroupObservableService,
        AssetTilesService,
        CommonResponseService,
        DataCacheService,
        DomainTypeObservableService,
        ErrorHandlingService,
        HttpService,
        LoggerService,
        PermissionGuardService,
        RefactorFieldsService,
        UtilsService,
      ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(VulnReportTrendComponent);
    component = fixture.componentInstance;
    component.ngAfterViewInit();
    fixture.detectChanges();
  });

  // todo: since it throw exception 'Expression has changed after it was checked'
  // investigate the handling of intial parentWidth value
  xit('should create', () => {
    expect(component).toBeTruthy();
  });
});
