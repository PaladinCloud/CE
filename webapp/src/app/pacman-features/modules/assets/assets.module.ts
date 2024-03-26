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
import { CommonModule } from '@angular/common';
import { SharedModule } from '../../../shared/shared.module';
import { AssetsRoutingModule } from './assets-routing.module';
import { AssetDashboardComponent } from './asset-dashboard/asset-dashboard.component';
import { MultilineChartComponent } from './../../secondary-components/multiline-chart/multiline-chart.component';
import { AssetDetailsComponent } from './asset-details/asset-details.component';
import { PacmanPolicyViolationsComponent } from './../../secondary-components/pacman-policy-violations/pacman-policy-violations.component';
import { AssetListComponent } from './asset-list/asset-list.component';
import { AttributeComponent } from './../../secondary-components/attribute/attribute.component';
import { AgGridModule } from 'ag-grid-angular';
import { MatSelectModule } from '@angular/material/select';
import { MatCardModule } from '@angular/material/card';
import { NgApexchartsModule } from 'ng-apexcharts';
import { AssetDistributionComponent } from './asset-distribution/asset-distribution.component';
import { FetchResourcesService } from '../../services/fetch-resources.service';
import { MatMenuModule } from '@angular/material/menu';

@NgModule({
    imports: [
        MatCardModule,
        MatMenuModule,
        MatSelectModule,
        CommonModule,
        AssetsRoutingModule,
        NgApexchartsModule,
        SharedModule,
        AgGridModule,
    ],
    declarations: [
        AssetDashboardComponent,
        MultilineChartComponent,
        AssetDistributionComponent,
        AssetDetailsComponent,
        PacmanPolicyViolationsComponent,
        AssetListComponent,
        AttributeComponent,
    ],
    providers: [FetchResourcesService],
})
export class AssetsModule {}
