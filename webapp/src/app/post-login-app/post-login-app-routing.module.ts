/*
 *Copyright 2018 T Mobile, Inc. or its affiliates. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the 'License'); You may not use
 * this file except in compliance with the License. A copy of the License is located at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * or in the 'license' file accompanying this file. This file is distributed on
 * an 'AS IS' BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, express or
 * implied. See the License for the specific language governing permissions and
 * limitations under the License.
 */

import { NgModule } from '@angular/core';
import { Routes, RouterModule } from '@angular/router';
import { PostLoginAppComponent } from './post-login-app.component';
import { ChangeDefaultAssetGroupComponent } from './change-default-asset-group/change-default-asset-group.component';
import { AuthGuardService } from './../shared/services/auth-guard.service';
import { DomainOverlayComponent } from './domain-overlay/domain-overlay.component';
import { HelpTextComponent } from '../shared/help-text/help-text.component';
import { OmnisearchComponent } from '../pacman-features/modules/omnisearch/omnisearch.component';
import { ComplianceOverviewTrendComponent } from '../pacman-features/secondary-components/compliance-overview-trend/compliance-overview-trend.component';
import { IssuesTrendHistoryComponent } from '../pacman-features/secondary-components/issues-trend-history/issues-trend-history.component';

const routes: Routes = [
    {
        path: 'pl',
        component: PostLoginAppComponent,
        children: [
            {
                path: 'compliance',
                data: { sequence: 1 },
                loadChildren: () =>
                    import('./../pacman-features/modules/compliance/compliance.module').then(
                        (m) => m.ComplianceModule,
                    ),
            },
            {
                path: 'assets',
                data: { sequence: 2 },
                loadChildren: () =>
                    import('./../pacman-features/modules/assets/assets.module').then(
                        (m) => m.AssetsModule,
                    ),
            },
            {
                path: 'statistics',
                data: { sequence: 3 },
                loadChildren: () =>
                    import('./../pacman-features/modules/statistics/statistics.module').then(
                        (m) => m.StatisticsModule,
                    ),
            },
            {
                path: 'tools',
                data: { sequence: 3 },
                loadChildren: () =>
                    import('./../pacman-features/modules/tools/tools.module').then(
                        (m) => m.ToolsModule,
                    ),
            },
            {
                path: 'notifications',
                data: { sequence: 6 },
                loadChildren: () =>
                    import('../pacman-features/modules/notifications/notifications.module').then(
                        (m) => m.NotificationsModule,
                    ),
            },
            {
                path: 'omnisearch',
                component: OmnisearchComponent,
                data: { sequence: 4 },
                loadChildren: () =>
                    import('./../pacman-features/modules/omnisearch/omnisearch.module').then(
                        (m) => m.OmnisearchModule,
                    ),
            },
            {
                path: 'admin',
                data: { sequence: 5 },
                loadChildren: () =>
                    import('./../pacman-features/modules/admin/admin.module').then(
                        (m) => m.AdminModule,
                    ),
            },
            {
                path: 'change-default-asset-group',
                component: ChangeDefaultAssetGroupComponent,
                outlet: 'modal',
            },
            {
                path: 'overall-compliance-trend',
                component: ComplianceOverviewTrendComponent,
                outlet: 'modal',
            },
            {
                path: 'policy-violations-trend',
                component: IssuesTrendHistoryComponent,
                outlet: 'modal',
            },
            {
                path: 'domain-overlay',
                component: DomainOverlayComponent,
                outlet: 'modalBGMenu',
            },
            {
                path: 'help-text',
                component: HelpTextComponent,
                outlet: 'helpTextModal',
            },
        ],
        canActivate: [AuthGuardService],
    },
    {
        path: '**',
        redirectTo: '/home',
    },
];

@NgModule({
    imports: [RouterModule.forChild(routes)],
    exports: [RouterModule],
})
export class PostLoginAppRoutingModule {}
