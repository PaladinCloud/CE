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
import { WorkflowService } from 'src/app/core/services/workflow.service';
import { ErrorHandlingService } from 'src/app/shared/services/error-handling.service';
import { LoggerService } from 'src/app/shared/services/logger.service';
import { RefactorFieldsService } from 'src/app/shared/services/refactor-fields.service';
import { RouterUtilityService } from 'src/app/shared/services/router-utility.service';
import { UtilsService } from 'src/app/shared/services/utils.service';

import { StateTableComponent } from './state-table.component';

describe('StateTableComponent', () => {
  let component: StateTableComponent;
  let fixture: ComponentFixture<StateTableComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      imports: [RouterTestingModule],
      declarations: [ StateTableComponent ],
      providers: [
        DataCacheService,
        ErrorHandlingService,
        LoggerService,
        RefactorFieldsService,
        RouterUtilityService,
        UtilsService,
        WorkflowService,
      ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(StateTableComponent);
    component = fixture.componentInstance;
    component.data = {
      response: []
    }
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
