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

import { CommonModule } from '@angular/common';
import { HTTP_INTERCEPTORS } from '@angular/common/http';
import { NgModule, Optional, SkipSelf } from '@angular/core';
import { SharedModule } from './../shared/shared.module';
import { GraphQLModule } from './graphql/graphql.module';
import { AdalService } from './services/adal.service';
import { AssetGroupObservableService } from './services/asset-group-observable.service';
import { AssetTilesService } from './services/asset-tiles.service';
import { AuthSessionStorageService } from './services/auth-session-storage.service';
import { AuthService } from './services/auth.service';
import { DataCacheService } from './services/data-cache.service';
import { DomainMappingService } from './services/domain-mapping.service';
import { DomainTypeObservableService } from './services/domain-type-observable.service';
import { OnPremAuthenticationService } from './services/onprem-authentication.service';
import { PermissionGuardService } from './services/permission-guard.service';
import { RecentlyViewedObservableService } from './services/recently-viewed-observable.service';
import { RequestInterceptorService } from './services/request-interceptor.service';
import { RoutingService } from './services/routing.service';
import { TableStateService } from './services/table-state.service';
import { ThemeObservableService } from './services/theme-observable.service';
import { WindowExpansionService } from './services/window-expansion.service';
import { WorkflowService } from './services/workflow.service';

@NgModule({
    imports: [CommonModule, GraphQLModule, SharedModule],
    declarations: [],
    exports: [],
    providers: [
        AdalService,
        AssetGroupObservableService,
        AssetTilesService,
        AuthService,
        AuthSessionStorageService,
        DataCacheService,
        DomainMappingService,
        DomainTypeObservableService,
        OnPremAuthenticationService,
        PermissionGuardService,
        RecentlyViewedObservableService,
        RoutingService,
        TableStateService,
        ThemeObservableService,
        WindowExpansionService,
        WorkflowService,
        {
            provide: HTTP_INTERCEPTORS,
            useClass: RequestInterceptorService,
            multi: true,
        },
    ],
})
export class CoreModule {
    constructor(@Optional() @SkipSelf() parentModule: CoreModule) {
        if (parentModule) {
            throw new Error('CoreModule is already loaded. Import it in the AppModule only');
        }
    }
}
