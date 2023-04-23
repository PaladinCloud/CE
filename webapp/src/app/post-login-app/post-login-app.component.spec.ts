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
import { MatSidenavModule } from '@angular/material/sidenav';
import { MatSnackBarModule } from '@angular/material/snack-bar';
import { NoopAnimationsModule } from '@angular/platform-browser/animations';
import { RouterTestingModule } from '@angular/router/testing';
import { AdalService } from '../core/services/adal.service';
import { AssetGroupObservableService } from '../core/services/asset-group-observable.service';
import { AuthSessionStorageService } from '../core/services/auth-session-storage.service';
import { AuthService } from '../core/services/auth.service';
import { DataCacheService } from '../core/services/data-cache.service';
import { DomainTypeObservableService } from '../core/services/domain-type-observable.service';
import { OnPremAuthenticationService } from '../core/services/onprem-authentication.service';
import { PermissionGuardService } from '../core/services/permission-guard.service';
import { ThemeObservableService } from '../core/services/theme-observable.service';
import { WindowExpansionService } from '../core/services/window-expansion.service';
import { WorkflowService } from '../core/services/workflow.service';
import { TokenResolverService } from '../resolver/token-resolver.service';
import { CommonResponseService } from '../shared/services/common-response.service';
import { DownloadService } from '../shared/services/download.service';
import { ErrorHandlingService } from '../shared/services/error-handling.service';
import { HttpService } from '../shared/services/http-response.service';
import { LoggerService } from '../shared/services/logger.service';
import { MainRoutingAnimationEventService } from '../shared/services/main-routing-animation-event.service';
import { RefactorFieldsService } from '../shared/services/refactor-fields.service';
import { RouterUtilityService } from '../shared/services/router-utility.service';
import { UtilsService } from '../shared/services/utils.service';
import { ToastNotificationComponent } from '../shared/toast-notification/toast-notification.component';
import { ContextualMenuComponent } from './common/contextual-menu/contextual-menu.component';
import { HeaderComponent } from './common/header/header.component';
import { ToastObservableService } from './common/services/toast-observable.service';
import { ToastObservableService as SharedToasObservableService } from '../shared/services/toast-observable.service';

import { PostLoginAppComponent } from './post-login-app.component';
import { AssetSwitcherComponent } from './common/asset-switcher/asset-switcher.component';
import { RecentlyViewedObservableService } from '../core/services/recently-viewed-observable.service';
import { DefaultAssetGroupComponent } from './default-asset-group/default-asset-group.component';
import { AwsResourceTypeSelectionService } from '../pacman-features/services/aws-resource-type-selection.service';
import { AssetTilesService } from '../core/services/asset-tiles.service';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';

describe('PostLoginAppComponent', () => {
  let component: PostLoginAppComponent;
  let fixture: ComponentFixture<PostLoginAppComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      imports: [
        HttpClientTestingModule,
        MatFormFieldModule,
        MatInputModule,
        MatSnackBarModule,
        MatSidenavModule,
        NoopAnimationsModule,
        RouterTestingModule,
      ],
      declarations: [
        AssetSwitcherComponent,
        ContextualMenuComponent,
        DefaultAssetGroupComponent,
        HeaderComponent,
        PostLoginAppComponent,
        ToastNotificationComponent,
      ],
      providers: [
        AdalService,
        AssetGroupObservableService,
        AssetTilesService,
        AuthService,
        AuthSessionStorageService,
        AwsResourceTypeSelectionService,
        CommonResponseService,
        DataCacheService,
        DownloadService,
        DomainTypeObservableService,
        ErrorHandlingService,
        HttpService,
        LoggerService,
        MainRoutingAnimationEventService,
        OnPremAuthenticationService,
        PermissionGuardService,
        RecentlyViewedObservableService,
        RefactorFieldsService,
        RouterUtilityService,
        SharedToasObservableService,
        ToastObservableService,
        TokenResolverService,
        ThemeObservableService,
        UtilsService,
        WindowExpansionService,
        WorkflowService,
      ],
    }).compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(PostLoginAppComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
