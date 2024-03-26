import { PermissionGuardService } from '../../../core/services/permission-guard.service';
import { PoliciesComponent } from './policies/policies.component';
import { CreateEditPolicyComponent } from './create-edit-policy/create-edit-policy.component';
import { CreateJobExecutionManagerComponent } from './job-execution-manager/create-job-execution-manager/create-job-execution-manager.component';
import { JobExecutionManagerComponent } from './job-execution-manager/job-execution-manager.component';
import { AuthGuardService } from '../../../shared/services/auth-guard.service';
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
import { AccountManagementComponent } from './account-management/account-management.component';
import { AccountManagementDetailsComponent } from './account-management-details/account-management-details.component';
import { SystemManagementComponent } from './system-management/system-management.component';
import { ConfigManagementComponent } from './config-management/config-management.component';
import { UserManagementComponent } from 'src/app/pacman-features/modules/admin/user-management/user-management.component';
import { AddAccountComponent } from 'src/app/pacman-features/modules/admin/account-management/add-account/add-account.component';

export const ADMIN_ROUTES = [
    {
        path: 'policies',
        component: PoliciesComponent,
        data: {
            title: 'Policies',
            roles: ['ROLE_ADMIN'],
            capabilities: ['policy-management'],
        },
        canActivate: [AuthGuardService, PermissionGuardService],
    },
    {
        path: 'policies/create-edit-policy',
        component: CreateEditPolicyComponent,
        data: {
            title: 'Create Edit Policy',
            roles: ['ROLE_ADMIN'],
            capabilities: ['policy-management'],
        },
        canActivate: [AuthGuardService, PermissionGuardService],
    },
    {
        path: 'account-management/add-account',
        component: AddAccountComponent,
        data: {
            title: 'Add Plugin',
            roles: ['ROLE_ADMIN'],
            capabilities: ['account-management'],
        },
        canActivate: [AuthGuardService, PermissionGuardService],
    },
    {
        path: 'job-execution-manager',
        component: JobExecutionManagerComponent,
        data: {
            title: 'Job Execution Manager',
            roles: ['ROLE_ADMIN'],
            capabilities: ['job-execution-management'],
        },
        canActivate: [AuthGuardService, PermissionGuardService],
    },
    {
        path: 'job-execution-manager/create-job-execution-manager',
        component: CreateJobExecutionManagerComponent,
        data: {
            title: 'Create Job Execution Manager',
            roles: ['ROLE_ADMIN'],
            capabilities: ['job-execution-management'],
        },
        canActivate: [AuthGuardService, PermissionGuardService],
    },
    {
        path: 'asset-groups/create-asset-groups',
        component: CreateAssetGroupsComponent,
        data: {
            title: 'Create Asset Groups',
            roles: ['ROLE_ADMIN'],
            capabilities: ['asset-group-management'],
        },
        canActivate: [AuthGuardService, PermissionGuardService],
    },
    {
        path: 'asset-groups',
        component: AssetGroupsComponent,
        data: {
            title: 'Asset Groups',
            roles: ['ROLE_ADMIN'],
            capabilities: ['asset-group-management'],
        },
        canActivate: [AuthGuardService, PermissionGuardService],
    },
    {
        path: 'sticky-exceptions',
        component: StickyExceptionsComponent,
        data: {
            title: 'Exemptions',
            roles: ['ROLE_ADMIN'],
            capabilities: ['exemption-management'],
        },
        canActivate: [AuthGuardService, PermissionGuardService],
    },
    {
        path: 'sticky-exceptions/create-sticky-exceptions',
        component: CreateStickyExceptionsComponent,
        data: {
            title: 'Create Exemptions',
            roles: ['ROLE_ADMIN'],
            capabilities: ['exemption-management'],
        },
        canActivate: [AuthGuardService, PermissionGuardService],
    },
    {
        path: 'sticky-exceptions/delete-sticky-exceptions',
        component: DeleteStickyExceptionsComponent,
        data: {
            title: 'Delete Exemptions',
            roles: ['ROLE_ADMIN'],
            capabilities: ['exemption-management'],
        },
        canActivate: [AuthGuardService, PermissionGuardService],
    },
    {
        path: 'domains',
        component: DomainsComponent,
        data: {
            title: 'Domains',
            roles: ['ROLE_ADMIN'],
            capabilities: ['domain-management'],
        },
        canActivate: [AuthGuardService, PermissionGuardService],
    },
    {
        path: 'domains/create-update-domain',
        component: CreateUpdateDomainComponent,
        data: {
            title: 'Create Update Domain',
            roles: ['ROLE_ADMIN'],
            capabilities: ['domain-management'],
        },
        canActivate: [AuthGuardService, PermissionGuardService],
    },
    {
        path: 'target-types',
        component: TargetTypesComponent,
        data: {
            title: 'Target Types',
            roles: ['ROLE_ADMIN'],
            capabilities: ['target-type-management'],
        },
        canActivate: [AuthGuardService, PermissionGuardService],
    },
    {
        path: 'target-types/create-update-target-type',
        component: CreateUpdateTargetTypesComponent,
        data: {
            title: 'Create Update Target Type',
            roles: ['ROLE_ADMIN'],
            capabilities: ['target-type-management'],
        },
        canActivate: [AuthGuardService, PermissionGuardService],
    },
    {
        path: 'job-execution-manager/update-job-execution-manager',
        component: UpdateJobExecutionManagerComponent,
        data: {
            title: 'Update Job Execution Manager',
            roles: ['ROLE_ADMIN'],
            capabilities: ['job-execution-management'],
        },
        canActivate: [AuthGuardService, PermissionGuardService],
    },
    {
        path: 'account-management',
        component: AccountManagementComponent,
        data: {
            title: 'Plugin Manager',
            roles: ['ROLE_ADMIN'],
            pageLevel: 0,
            capabilities: ['account-management'],
        },
        canActivate: [AuthGuardService, PermissionGuardService],
    },
    {
        path: 'account-management/account-management-details',
        component: AccountManagementDetailsComponent,
        data: {
            title: 'Plugin Manager Details',
            roles: ['ROLE_ADMIN'],
            capabilities: ['account-management'],
        },
        canActivate: [AuthGuardService, PermissionGuardService],
    },
    {
        path: 'user-management',
        component: UserManagementComponent,
        data: {
            title: 'Users',
            roles: ['ROLE_ADMIN'],
            pageLevel: 0,
            capabilities: ['user-management'],
        },
        canActivate: [AuthGuardService, PermissionGuardService],
    },
    {
        path: 'config-management',
        component: ConfigManagementComponent,
        data: {
            title: 'Configuration Management',
            roles: ['ROLE_ADMIN'],
            pageLevel: 0,
            capabilities: ['configuration-management'],
        },
        canActivate: [AuthGuardService, PermissionGuardService],
    },
    {
        path: 'system-management',
        component: SystemManagementComponent,
        data: {
            title: 'System Management',
            roles: ['ROLE_ADMIN'],
            capabilities: ['system-management'],
        },
        canActivate: [AuthGuardService, PermissionGuardService],
    },
];
