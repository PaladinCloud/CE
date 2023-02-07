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
import { DataCacheService } from 'src/app/core/services/data-cache.service';
import { HelpObservableService } from 'src/app/post-login-app/common/services/help-observable.service';
import { ErrorHandlingService } from '../services/error-handling.service';
import { HttpService } from '../services/http-response.service';
import { LoggerService } from '../services/logger.service';
import { RefactorFieldsService } from '../services/refactor-fields.service';
import { UtilsService } from '../services/utils.service';
import { HelpTextComponent } from './help-text.component';

describe('HelpTextComponent', () => {
  let component: HelpTextComponent;
  let fixture: ComponentFixture<HelpTextComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule, RouterTestingModule],
      declarations: [HelpTextComponent],
      providers: [
        DataCacheService,
        ErrorHandlingService,
        HelpObservableService,
        HttpService,
        LoggerService,
        RefactorFieldsService,
        UtilsService,
      ],
    }).compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(HelpTextComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
