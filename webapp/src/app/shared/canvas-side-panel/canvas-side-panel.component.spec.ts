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

import { async, ComponentFixture, TestBed } from '@angular/core/testing';
import { RouterTestingModule } from '@angular/router/testing';
import { AssetGroupObservableService } from 'src/app/core/services/asset-group-observable.service';
import { DataCacheService } from 'src/app/core/services/data-cache.service';
import { DomainMappingService } from 'src/app/core/services/domain-mapping.service';
import { DomainTypeObservableService } from 'src/app/core/services/domain-type-observable.service';
import { RoutingService } from 'src/app/core/services/routing.service';
import { WorkflowService } from 'src/app/core/services/workflow.service';
import { LoggerService } from '../services/logger.service';
import { RefactorFieldsService } from '../services/refactor-fields.service';
import { RouterUtilityService } from '../services/router-utility.service';
import { UtilsService } from '../services/utils.service';

import { CanvasSidePanelComponent } from './canvas-side-panel.component';

describe('CanvasSidePanelComponent', () => {
  let component: CanvasSidePanelComponent;
  let fixture: ComponentFixture<CanvasSidePanelComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      imports: [ RouterTestingModule ],
      declarations: [CanvasSidePanelComponent],
      providers: [
        AssetGroupObservableService,
        DataCacheService,
        DomainMappingService,
        DomainTypeObservableService,
        LoggerService,
        RefactorFieldsService,
        RoutingService,
        RouterUtilityService,
        UtilsService,
        WorkflowService,
      ],
    }).compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(CanvasSidePanelComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
