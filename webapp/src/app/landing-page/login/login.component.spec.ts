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

import { NO_ERRORS_SCHEMA } from '@angular/core';
import { async, ComponentFixture, TestBed } from '@angular/core/testing';
import { RouterTestingModule } from '@angular/router/testing';
import { NoopAnimationsModule } from '@angular/platform-browser/animations';

import { LoginComponent } from './login.component';
import { FormInputComponent } from '../../shared/form-input/form-input.component';
import { ButtonComponent } from '../../shared/button/button.component';
import { OnPremAuthenticationService } from '../../core/services/onprem-authentication.service';
import { UtilsService } from '../../shared/services/utils.service';
import { AuthService } from 'src/app/core/services/auth.service';
import { AdalService } from 'src/app/core/services/adal.service';
import { DataCacheService } from 'src/app/core/services/data-cache.service';
import { AuthSessionStorageService } from 'src/app/core/services/auth-session-storage.service';
import { LoggerService } from 'src/app/shared/services/logger.service';
import { HttpClientTestingModule } from '@angular/common/http/testing';
import { HttpService } from 'src/app/shared/services/http-response.service';
import { ErrorHandlingService } from 'src/app/shared/services/error-handling.service';
import { TokenResolverService } from 'src/app/resolver/token-resolver.service';
import { AssetGroupObservableService } from 'src/app/core/services/asset-group-observable.service';
import { CommonResponseService } from 'src/app/shared/services/common-response.service';

describe('LoginComponent', () => {
  let component: LoginComponent;
  let fixture: ComponentFixture<LoginComponent>;

  class MockOnPremAuthenticationService {
    login() {}
    takeActionAfterLogin() {}
    formatUsernameWithoutDomain() {}
  }
  class MockUtilsService {
    isObject() {}
  }

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      imports: [RouterTestingModule, NoopAnimationsModule, HttpClientTestingModule],
      declarations: [LoginComponent, FormInputComponent, ButtonComponent],
      providers: [
        AdalService,
        AssetGroupObservableService,
        AuthService,
        AuthSessionStorageService,
        CommonResponseService,
        DataCacheService,
        ErrorHandlingService,
        HttpService,
        LoggerService,
        TokenResolverService,
        { provide: OnPremAuthenticationService, useClass: MockOnPremAuthenticationService },
        { provide: UtilsService, useClass: MockUtilsService }
      ],
      schemas: [NO_ERRORS_SCHEMA]
    }).compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(LoginComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  // Disabled
  // todo: need to replace window.location call with DI injectable Window
  xit('should create', () => {
    expect(component).toBeTruthy();
  });
});
