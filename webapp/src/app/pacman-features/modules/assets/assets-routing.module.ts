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

import { NgModule } from '@angular/core';
import { Routes, RouterModule } from '@angular/router';
import { AuthGuardService } from 'src/app/shared/services/auth-guard.service';
import { AssetDashboardComponent } from './asset-dashboard/asset-dashboard.component';
import { AssetDetailsComponent } from './asset-details/asset-details.component';
import { AssetDistributionComponent } from './asset-distribution/asset-distribution.component';
import { AssetListComponent } from './asset-list/asset-list.component';

export const ASSETS_ROUTES = [
    {
      path: "asset-dashboard",
      component: AssetDashboardComponent,
      data: {
        title: "Asset Dashboard",
      },
      canActivate: [AuthGuardService],
    },
    {
      path: "asset-list/:resourceType/:resourceId",
      component: AssetDetailsComponent,
      data: {
        title: "Asset 360",
      },
      canActivate: [AuthGuardService],
    },
    {
      path: "asset-distribution",
      component: AssetDistributionComponent,
      data: {
        title: "Asset Distribution",
      },
      canActivate: [AuthGuardService],
    },
    {
      path: "asset-list",
      component: AssetListComponent,
      data: {
        title: "Asset List",
      },
      canActivate: [AuthGuardService],
    }
  ];
const routes: Routes = ASSETS_ROUTES;

@NgModule({
    imports: [RouterModule.forChild(routes)],
    exports: [RouterModule]
})

export class AssetsRoutingModule {}
