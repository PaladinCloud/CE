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
import { DataCacheService } from 'src/app/core/services/data-cache.service';
import { DomainMappingService } from 'src/app/core/services/domain-mapping.service';
import { DomainTypeObservableService } from 'src/app/core/services/domain-type-observable.service';
import { ThemeObservableService } from 'src/app/core/services/theme-observable.service';
import { WorkflowService } from 'src/app/core/services/workflow.service';
import { LoggerService } from 'src/app/shared/services/logger.service';
import { RefactorFieldsService } from 'src/app/shared/services/refactor-fields.service';
import { RouterUtilityService } from 'src/app/shared/services/router-utility.service';
import { UtilsService } from 'src/app/shared/services/utils.service';

import { DomainGroupComponent } from './domain-group.component';

describe('DomainGroupComponent', () => {
  let component: DomainGroupComponent;
  let fixture: ComponentFixture<DomainGroupComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      imports: [RouterTestingModule],
      declarations: [DomainGroupComponent],
      providers: [
        DataCacheService,
        DomainMappingService,
        DomainTypeObservableService,
        LoggerService,
        RefactorFieldsService,
        RouterUtilityService,
        ThemeObservableService,
        WorkflowService,
        UtilsService,
      ],
    }).compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(DomainGroupComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
