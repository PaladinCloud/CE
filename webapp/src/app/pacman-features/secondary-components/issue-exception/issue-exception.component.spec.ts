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
import { ReactiveFormsModule } from '@angular/forms';
import { DataCacheService } from 'src/app/core/services/data-cache.service';
import { ToastObservableService } from 'src/app/post-login-app/common/services/toast-observable.service';
import { ErrorHandlingService } from 'src/app/shared/services/error-handling.service';
import { HttpService } from 'src/app/shared/services/http-response.service';
import { LoggerService } from 'src/app/shared/services/logger.service';
import { RefactorFieldsService } from 'src/app/shared/services/refactor-fields.service';
import { UtilsService } from 'src/app/shared/services/utils.service';

import { IssueExceptionComponent } from './issue-exception.component';

describe('IssueExceptionComponent', () => {
  let component: IssueExceptionComponent;
  let fixture: ComponentFixture<IssueExceptionComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule, ReactiveFormsModule],
      declarations: [IssueExceptionComponent],
      providers: [
        DataCacheService,
        ErrorHandlingService,
        HttpService,
        LoggerService,
        RefactorFieldsService,
        ToastObservableService,
        UtilsService,
      ],
    }).compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(IssueExceptionComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
