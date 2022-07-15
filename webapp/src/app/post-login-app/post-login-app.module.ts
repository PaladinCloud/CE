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

import { NgModule, Optional, SkipSelf } from "@angular/core";
import { CommonModule } from "@angular/common";

/* Feature modules */
import { SharedModule } from "../shared/shared.module";
import { PostLoginAppRoutingModule } from "./post-login-app-routing.module";
import { ComplianceModule } from "./../pacman-features/modules/compliance/compliance.module";
import { AssetsModule } from "./../pacman-features/modules/assets/assets.module";
import { NavIconComponent } from "./common/nav-icon/nav-icon.component";
import { PostLoginAppComponent } from "./post-login-app.component";
import { DefaultAssetGroupComponent } from "./default-asset-group/default-asset-group.component";
import { FirstTimeUserJourneyComponent } from "./first-time-user-journey/first-time-user-journey.component";
import { AssetGroupsComponent } from "./asset-groups/asset-groups.component";
import { AssetGroupTabsComponent } from "./common/asset-group-tabs/asset-group-tabs.component";
import { AssetGroupDetailsComponent } from "./common/asset-group-details/asset-group-details.component";
import { AssetGroupSearchComponent } from "./common/asset-group-search/asset-group-search.component";
import { AssetGroupObservableService } from "../core/services/asset-group-observable.service";
import { AwsResourceTypeSelectionService } from "./../pacman-features/services/aws-resource-type-selection.service";
import { StateManagementService } from "./common/services/state-management.service";
import { ToastObservableService } from "./common/services/toast-observable.service";
import { HelpObservableService } from "./common/services/help-observable.service";
import { ChangeDefaultAssetGroupComponent } from "./change-default-asset-group/change-default-asset-group.component";
import { ToolsModule } from "../pacman-features/modules/tools/tools.module";
import { KnowYourDashboardComponent } from "./know-your-dashboard/know-your-dashboard.component";
import { OmnisearchModule } from "./../pacman-features/modules/omnisearch/omnisearch.module";
import { DownloadService } from "../shared/services/download.service";
import { DomainGroupComponent } from "./domain-group/domain-group.component";
import { DomainDropdownComponent } from "./common/domain-dropdown/domain-dropdown.component";
import { AdminModule } from "../pacman-features/modules/admin/admin.module";
import { VulnerabilityReportComponent } from "./vulnerability-report/vulnerability-report.component";
import { DomainOverlayComponent } from "./domain-overlay/domain-overlay.component";
import { VulnReportTrendComponent } from "./common/vuln-report-trend/vuln-report-trend.component";
import { VulnReportDistributionComponent } from "./common/vuln-report-distribution/vuln-report-distribution.component";
import { VulnReportTablesComponent } from "./common/vuln-report-tables/vuln-report-tables.component";
import { VulnReportStatsComponent } from "./common/vuln-report-stats/vuln-report-stats.component";
import { VulnTrendGraphComponent } from "./common/vuln-trend-graph/vuln-trend-graph.component";
import { VulnReportWorkflowComponent } from "./common/vuln-report-workflow/vuln-report-workflow.component";
import { CopyElementService } from "../shared/services/copy-element.service";
import { TreeModule } from "@circlon/angular-tree-component";
import { ContextualMenuComponent } from "./common/contextual-menu/contextual-menu.component";
import { AssetSwitcherComponent } from "./common/asset-switcher/asset-switcher.component";
import { HeaderComponent } from "./common/header/header.component";
import { HttpClientModule } from "@angular/common/http";
import { MatIconModule } from "@angular/material/icon";
 import {MatInputModule} from '@angular/material/input'; 

@NgModule({
  imports: [
    MatIconModule,
    MatInputModule,
    HttpClientModule,
    CommonModule,
    PostLoginAppRoutingModule,
    SharedModule,
    ComplianceModule,
    AssetsModule,
    ToolsModule,
    OmnisearchModule,
    AdminModule,
    TreeModule,
  ],
  declarations: [
    ContextualMenuComponent,
    HeaderComponent,
    AssetSwitcherComponent,
    NavIconComponent,
    PostLoginAppComponent,
    DefaultAssetGroupComponent,
    FirstTimeUserJourneyComponent,
    AssetGroupsComponent,
    AssetGroupTabsComponent,
    AssetGroupDetailsComponent,
    AssetGroupSearchComponent,
    ChangeDefaultAssetGroupComponent,
    KnowYourDashboardComponent,
    DomainGroupComponent,
    DomainDropdownComponent,
    VulnerabilityReportComponent,
    DomainOverlayComponent,
    VulnReportTrendComponent,
    VulnReportDistributionComponent,
    VulnReportTablesComponent,
    VulnReportStatsComponent,
    VulnTrendGraphComponent,
    VulnReportWorkflowComponent,
  ],

  providers: [
    AssetGroupObservableService,
    AwsResourceTypeSelectionService,
    StateManagementService,
    ToastObservableService,
    CopyElementService,
    HelpObservableService,
    DownloadService,
  ],
})
export class PostLoginAppModule {
  constructor(@Optional() @SkipSelf() parentModule: PostLoginAppModule) {
    if (parentModule) {
      throw new Error(
        "PostLoginAppModule is already loaded. Import it in the AppModule only"
      );
    }
  }
}
