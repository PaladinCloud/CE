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

import { CdkTreeModule } from '@angular/cdk/tree';
import { CommonModule } from '@angular/common';
import { HttpClientModule } from '@angular/common/http';
import { NgModule, Optional, SkipSelf } from '@angular/core';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatSidenavModule } from '@angular/material/sidenav';
import { AssetGroupObservableService } from '../core/services/asset-group-observable.service';
import { FetchResourcesService } from '../pacman-features/services/fetch-resources.service';
import { TokenResolverService } from '../resolver/token-resolver.service';
import { CopyElementService } from '../shared/services/copy-element.service';
import { DownloadService } from '../shared/services/download.service';
import { SharedModule } from '../shared/shared.module';
import { ComplianceModule } from './../pacman-features/modules/compliance/compliance.module';
import { AwsResourceTypeSelectionService } from './../pacman-features/services/aws-resource-type-selection.service';
import { AssetGroupsComponent } from './asset-groups/asset-groups.component';
import { ChangeDefaultAssetGroupComponent } from './change-default-asset-group/change-default-asset-group.component';
import { AssetGroupDetailsComponent } from './common/asset-group-details/asset-group-details.component';
import { AssetGroupSearchComponent } from './common/asset-group-search/asset-group-search.component';
import { AssetSwitcherComponent } from './common/asset-switcher/asset-switcher.component';
import { ContextualMenuComponent } from './common/contextual-menu/contextual-menu.component';
import { DomainDropdownComponent } from './common/domain-dropdown/domain-dropdown.component';
import { HeaderComponent } from './common/header/header.component';
import { NavIconComponent } from './common/nav-icon/nav-icon.component';
import { HelpObservableService } from './common/services/help-observable.service';
import { StateManagementService } from './common/services/state-management.service';
import { ToastObservableService } from './common/services/toast-observable.service';
import { DefaultAssetGroupComponent } from './default-asset-group/default-asset-group.component';
import { DomainGroupComponent } from './domain-group/domain-group.component';
import { DomainOverlayComponent } from './domain-overlay/domain-overlay.component';
import { PostLoginAppRoutingModule } from './post-login-app-routing.module';
import { PostLoginAppComponent } from './post-login-app.component';
import { MatMenuModule } from '@angular/material/menu';
import { MatBadgeModule } from '@angular/material/badge';
import { AssetTilesService } from '../core/services/asset-tiles.service';

@NgModule({
    imports: [
        MatBadgeModule,
        MatIconModule,
        MatInputModule,
        MatSidenavModule,
        HttpClientModule,
        CommonModule,
        PostLoginAppRoutingModule,
        SharedModule,
        ComplianceModule,
        MatMenuModule,
        CdkTreeModule,
    ],
    declarations: [
        ContextualMenuComponent,
        HeaderComponent,
        AssetSwitcherComponent,
        NavIconComponent,
        PostLoginAppComponent,
        DefaultAssetGroupComponent,
        AssetGroupsComponent,
        AssetGroupDetailsComponent,
        AssetGroupSearchComponent,
        ChangeDefaultAssetGroupComponent,
        DomainGroupComponent,
        DomainDropdownComponent,
        DomainOverlayComponent,
    ],

    providers: [
        AssetGroupObservableService,
        AwsResourceTypeSelectionService,
        StateManagementService,
        ToastObservableService,
        CopyElementService,
        HelpObservableService,
        DownloadService,
        FetchResourcesService,
        TokenResolverService,
        AssetTilesService,
    ],
})
export class PostLoginAppModule {
    constructor(@Optional() @SkipSelf() parentModule: PostLoginAppModule) {
        if (parentModule) {
            throw new Error(
                'PostLoginAppModule is already loaded. Import it in the AppModule only',
            );
        }
    }
}
