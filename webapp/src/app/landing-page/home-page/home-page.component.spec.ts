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
import { AdalService } from 'src/app/core/services/adal.service';
import { AssetGroupObservableService } from 'src/app/core/services/asset-group-observable.service';
import { AuthSessionStorageService } from 'src/app/core/services/auth-session-storage.service';
import { AuthService } from 'src/app/core/services/auth.service';
import { DataCacheService } from 'src/app/core/services/data-cache.service';
import { OnPremAuthenticationService } from 'src/app/core/services/onprem-authentication.service';
import { TokenResolverService } from 'src/app/resolver/token-resolver.service';
import { ButtonComponent } from 'src/app/shared/button/button.component';
import { CommonResponseService } from 'src/app/shared/services/common-response.service';
import { ErrorHandlingService } from 'src/app/shared/services/error-handling.service';
import { HttpService } from 'src/app/shared/services/http-response.service';
import { LoggerService } from 'src/app/shared/services/logger.service';
import { RefactorFieldsService } from 'src/app/shared/services/refactor-fields.service';
import { UtilsService } from 'src/app/shared/services/utils.service';
import { FeatureComponent } from '../common/feature/feature.component';
import { LandingPageHeaderComponent } from '../common/landing-page-header/landing-page-header.component';

import { HomePageComponent } from './home-page.component';

describe('HomePageComponent', () => {
    let component: HomePageComponent;
    let fixture: ComponentFixture<HomePageComponent>;

    beforeEach(async(() => {
        TestBed.configureTestingModule({
            imports: [HttpClientTestingModule, RouterTestingModule],
            declarations: [
                HomePageComponent,
                LandingPageHeaderComponent,
                FeatureComponent,
                ButtonComponent,
            ],
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
                OnPremAuthenticationService,
                RefactorFieldsService,
                TokenResolverService,
                UtilsService,
            ],
        }).compileComponents();
    }));

    beforeEach(() => {
        fixture = TestBed.createComponent(HomePageComponent);
        component = fixture.componentInstance;
        fixture.detectChanges();
    });

    it('should create', () => {
        expect(component).toBeTruthy();
    });
});
