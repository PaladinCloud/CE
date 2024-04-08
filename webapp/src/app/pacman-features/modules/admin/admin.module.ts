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
import { PoliciesComponent } from './policies/policies.component';
import { AdminRoutingModule } from './admin-routing.module';
import { CreateEditPolicyComponent } from './create-edit-policy/create-edit-policy.component';
import { CreateJobExecutionManagerComponent } from './job-execution-manager/create-job-execution-manager/create-job-execution-manager.component';
import { JobExecutionManagerComponent } from './job-execution-manager/job-execution-manager.component';
import { CreateAssetGroupsComponent } from './asset-groups/create-asset-groups/create-asset-groups.component';
import { AssetGroupsComponent } from './asset-groups/asset-groups.component';
import { StickyExceptionsComponent } from './sticky-exceptions/sticky-exceptions.component';
import { DomainsComponent } from './asset-groups/domains/domains.component';
import { TargetTypesComponent } from './asset-groups/target-types/target-types.component';
import { CreateUpdateDomainComponent } from './asset-groups/domains/create-update-domain/create-update-domain.component';
import { CreateUpdateTargetTypesComponent } from './asset-groups/target-types/create-update-target-types/create-update-target-types.component';
import { CreateStickyExceptionsComponent } from './sticky-exceptions/create-sticky-exceptions/create-sticky-exceptions.component';
import { DeleteStickyExceptionsComponent } from './sticky-exceptions/delete-sticky-exceptions/delete-sticky-exceptions.component';
import { UpdateJobExecutionManagerComponent } from './job-execution-manager/update-job-execution-manager/update-job-execution-manager.component';
import { PacmanLoaderComponent } from './commons/pacman-loader/pacman-loader.component';
import { AccountManagementComponent } from './account-management/account-management.component';
import { AccountManagementDetailsComponent } from './account-management-details/account-management-details.component';
import { SystemManagementComponent } from './system-management/system-management.component';
import { ConfigManagementComponent } from './config-management/config-management.component';
import { MatDatepickerModule } from '@angular/material/datepicker';
import { MatNativeDateModule } from '@angular/material/core';
import { MatInputModule } from '@angular/material/input';
import { UserManagementComponent } from './user-management/user-management.component';
import { AdminService } from '../../services/all-admin.service';
import { MatSlideToggleModule } from '@angular/material/slide-toggle';
import { AddAccountComponent } from './account-management/add-account/add-account.component';
import { AssetTilesService } from 'src/app/core/services/asset-tiles.service';

@NgModule({
    imports: [
        CommonModule,
        SharedModule,
        MatSlideToggleModule,
        AdminRoutingModule,
        MatInputModule,
        MatDatepickerModule,
        MatNativeDateModule,
    ],
    declarations: [
        PoliciesComponent,
        CreateEditPolicyComponent,
        JobExecutionManagerComponent,
        CreateJobExecutionManagerComponent,
        CreateAssetGroupsComponent,
        AssetGroupsComponent,
        StickyExceptionsComponent,
        CreateStickyExceptionsComponent,
        DomainsComponent,
        CreateUpdateDomainComponent,
        TargetTypesComponent,
        CreateUpdateTargetTypesComponent,
        DeleteStickyExceptionsComponent,
        UpdateJobExecutionManagerComponent,
        PacmanLoaderComponent,
        AccountManagementComponent,
        AccountManagementDetailsComponent,
        SystemManagementComponent,
        ConfigManagementComponent,
        UserManagementComponent,
        AddAccountComponent,
    ],
    providers: [AdminService, AssetTilesService],
})
export class AdminModule {}
