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
import { AdminModule } from '../pacman-features/modules/admin/admin.module';
import { NotificationsModule } from '../pacman-features/modules/notifications/notifications.module';
import { ToolsModule } from '../pacman-features/modules/tools/tools.module';
import { FetchResourcesService } from '../pacman-features/services/fetch-resources.service';
import { TokenResolverService } from '../resolver/token-resolver.service';
import { CopyElementService } from '../shared/services/copy-element.service';
import { DownloadService } from '../shared/services/download.service';
import { SharedModule } from '../shared/shared.module';
import { AssetsModule } from './../pacman-features/modules/assets/assets.module';
import { ComplianceModule } from './../pacman-features/modules/compliance/compliance.module';
import { OmnisearchModule } from './../pacman-features/modules/omnisearch/omnisearch.module';
import { AwsResourceTypeSelectionService } from './../pacman-features/services/aws-resource-type-selection.service';
import { AssetGroupsComponent } from './asset-groups/asset-groups.component';
import { ChangeDefaultAssetGroupComponent } from './change-default-asset-group/change-default-asset-group.component';
import { AssetGroupDetailsComponent } from './common/asset-group-details/asset-group-details.component';
import { AssetGroupSearchComponent } from './common/asset-group-search/asset-group-search.component';
import { AssetGroupTabsComponent } from './common/asset-group-tabs/asset-group-tabs.component';
import { AssetSwitcherComponent } from './common/asset-switcher/asset-switcher.component';
import { ContextualMenuComponent } from './common/contextual-menu/contextual-menu.component';
import { DomainDropdownComponent } from './common/domain-dropdown/domain-dropdown.component';
import { HeaderComponent } from './common/header/header.component';
import { NavIconComponent } from './common/nav-icon/nav-icon.component';
import { HelpObservableService } from './common/services/help-observable.service';
import { StateManagementService } from './common/services/state-management.service';
import { ToastObservableService } from './common/services/toast-observable.service';
import { VulnReportDistributionComponent } from './common/vuln-report-distribution/vuln-report-distribution.component';
import { VulnReportStatsComponent } from './common/vuln-report-stats/vuln-report-stats.component';
import { VulnReportTablesComponent } from './common/vuln-report-tables/vuln-report-tables.component';
import { VulnReportTrendComponent } from './common/vuln-report-trend/vuln-report-trend.component';
import { VulnReportWorkflowComponent } from './common/vuln-report-workflow/vuln-report-workflow.component';
import { VulnTrendGraphComponent } from './common/vuln-trend-graph/vuln-trend-graph.component';
import { DefaultAssetGroupComponent } from './default-asset-group/default-asset-group.component';
import { DomainGroupComponent } from './domain-group/domain-group.component';
import { DomainOverlayComponent } from './domain-overlay/domain-overlay.component';
import { FirstTimeUserJourneyComponent } from './first-time-user-journey/first-time-user-journey.component';
import { KnowYourDashboardComponent } from './know-your-dashboard/know-your-dashboard.component';
import { PostLoginAppRoutingModule } from './post-login-app-routing.module';
import { PostLoginAppComponent } from './post-login-app.component';
import { VulnerabilityReportComponent } from './vulnerability-report/vulnerability-report.component';

@NgModule({
    imports: [
        AdminModule,
        AssetsModule,
        CdkTreeModule,
        CommonModule,
        ComplianceModule,
        HttpClientModule,
        MatIconModule,
        MatInputModule,
        MatSidenavModule,
        NotificationsModule,
        OmnisearchModule,
        PostLoginAppRoutingModule,
        SharedModule,
        ToolsModule,
    ],
    declarations: [
        AssetGroupDetailsComponent,
        AssetGroupsComponent,
        AssetGroupSearchComponent,
        AssetGroupTabsComponent,
        AssetSwitcherComponent,
        ChangeDefaultAssetGroupComponent,
        ContextualMenuComponent,
        DefaultAssetGroupComponent,
        DomainDropdownComponent,
        DomainGroupComponent,
        DomainOverlayComponent,
        FirstTimeUserJourneyComponent,
        HeaderComponent,
        KnowYourDashboardComponent,
        NavIconComponent,
        PostLoginAppComponent,
        VulnerabilityReportComponent,
        VulnReportDistributionComponent,
        VulnReportStatsComponent,
        VulnReportTablesComponent,
        VulnReportTrendComponent,
        VulnReportWorkflowComponent,
        VulnTrendGraphComponent,
    ],

    providers: [
        AssetGroupObservableService,
        AwsResourceTypeSelectionService,
        CopyElementService,
        DownloadService,
        FetchResourcesService,
        HelpObservableService,
        StateManagementService,
        ToastObservableService,
        TokenResolverService,
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
