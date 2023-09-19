import { PermissionGuardService } from "../../../core/services/permission-guard.service";
import { PoliciesComponent } from "./policies/policies.component";
import { RulesComponent } from "./rules/rules.component";
import { CreateEditPolicyComponent } from "./create-edit-policy/create-edit-policy.component";
import { CreateJobExecutionManagerComponent } from "./job-execution-manager/create-job-execution-manager/create-job-execution-manager.component";
import { JobExecutionManagerComponent } from "./job-execution-manager/job-execution-manager.component";
import { AuthGuardService } from "../../../shared/services/auth-guard.service";
import { InvokeRuleComponent } from "./invoke-rule/invoke-rule.component";
import { CreateAssetGroupsComponent } from "./asset-groups/create-asset-groups/create-asset-groups.component";
import { EnableDisableRuleComponent } from "./enable-disable-rule/enable-disable-rule.component";
import { AssetGroupsComponent } from "./asset-groups/asset-groups.component";
import { StickyExceptionsComponent } from "./sticky-exceptions/sticky-exceptions.component";
import { DomainsComponent } from "./asset-groups/domains/domains.component";
import { TargetTypesComponent } from "./asset-groups/target-types/target-types.component";
import { CreateUpdateDomainComponent } from "./asset-groups/domains/create-update-domain/create-update-domain.component";
import { CreateUpdateTargetTypesComponent } from "./asset-groups/target-types/create-update-target-types/create-update-target-types.component";
import { CreateStickyExceptionsComponent } from "./sticky-exceptions/create-sticky-exceptions/create-sticky-exceptions.component";
import { RolesComponent } from "./roles/roles.component";
import { RolesAllocationComponent } from "./roles-allocation/roles-allocation.component";
import { CreateUpdateRolesComponent } from "./roles/create-update-roles/create-update-roles.component";
import { DeleteStickyExceptionsComponent } from "./sticky-exceptions/delete-sticky-exceptions/delete-sticky-exceptions.component";
import { DeleteAssetGroupsComponent } from "./asset-groups/delete-asset-groups/delete-asset-groups.component";
import { UpdateJobExecutionManagerComponent } from "./job-execution-manager/update-job-execution-manager/update-job-execution-manager.component";
import { ConfigUsersComponent } from "./roles/config-users/config-users.component";
import { AccountManagementComponent } from "./account-management/account-management.component";
import { AccountManagementDetailsComponent } from "./account-management-details/account-management-details.component";
import { SystemManagementComponent } from "./system-management/system-management.component";
import { ConfigManagementComponent } from "./config-management/config-management.component";
import { UserManagementComponent } from "src/app/pacman-features/modules/admin/user-management/user-management.component";
import { AddAccountComponent } from "src/app/pacman-features/modules/admin/account-management/add-account/add-account.component";

 export const ADMIN_ROUTES = [
   {
     path: "policies",
     component: PoliciesComponent,
     data: {
       title: "Policies",
       roles: ["ROLE_ADMIN"],
       capabilities: ["policy-management"]
     },
     canActivate: [AuthGuardService, PermissionGuardService]
   },
   {
     path: "rules",
     component: RulesComponent,
     data: {
       title: "Rules",
       roles: ["ROLE_ADMIN"],
       capabilities: ["rule-severity-management", "rule-admin", "rules-security", "rules-technical-admin"],
     },
     canActivate: [AuthGuardService, PermissionGuardService]
   },
   {
     path: "rules/invoke-rule",
     component: InvokeRuleComponent,
     data: {
       title: "Invoke Rule",
       roles: ["ROLE_ADMIN"],
       capabilities: ["rule-admin"],
     },
     canActivate: [AuthGuardService, PermissionGuardService]
   },
   {
     path: "policies/create-edit-policy",
     component: CreateEditPolicyComponent,
     data: {
       title: "Create Edit Policy",
       roles: ["ROLE_ADMIN"],
       capabilities: ["policy-management"]
     },
     canActivate: [AuthGuardService, PermissionGuardService]
   },
   {
     path: "account-management/add-account",
     component: AddAccountComponent,
     data: {
       title: "Add Plugin",
       roles: ["ROLE_ADMIN"],
       capabilities: ["account-management"]
     },
     canActivate: [AuthGuardService, PermissionGuardService]
   },
   {
     path: "job-execution-manager",
     component: JobExecutionManagerComponent,
     data: {
       title: "Job Execution Manager",
       roles: ["ROLE_ADMIN"],
       capabilities: ["job-execution-management"]
     },
     canActivate: [AuthGuardService, PermissionGuardService]
   },
   {
     path: "job-execution-manager/create-job-execution-manager",
     component: CreateJobExecutionManagerComponent,
     data: {
       title: "Create Job Execution Manager",
       roles: ["ROLE_ADMIN"],
       capabilities: ["job-execution-management"]
     },
     canActivate: [AuthGuardService, PermissionGuardService]
   },
   {
     path: "asset-groups/create-asset-groups",
     component: CreateAssetGroupsComponent,
     data: {
       title: "Create Asset Groups",
       roles: ["ROLE_ADMIN"],
       capabilities: ["asset-group-management"]
     },
     canActivate: [AuthGuardService, PermissionGuardService]
   },
   {
     path: "asset-groups",
     component: AssetGroupsComponent,
     data: {
       title: "Asset Groups",
       roles: ["ROLE_ADMIN"],
       capabilities: ["asset-group-management"]
     },
     canActivate: [AuthGuardService, PermissionGuardService],
   },
   {
     path: "asset-groups/delete-asset-groups",
     component: DeleteAssetGroupsComponent,
     data: {
       title: "Delete Asset Groups",
       roles: ["ROLE_ADMIN"],
       capabilities: ["asset-group-management"]
     },
     canActivate: [AuthGuardService, PermissionGuardService],
   },
   {
     path: "roles",
     component: RolesComponent,
     data: {
       title: "Roles",
       roles: ["ROLE_ADMIN"],
       capabilities: ["user-management"],
     },
     canActivate: [AuthGuardService, PermissionGuardService]
   },
   {
     path: "roles/create-update-roles",
     component: CreateUpdateRolesComponent,
     data: {
       title: "Create Update Roles",
       roles: ["ROLE_ADMIN"],
       capabilities: ["user-management"],
     },
     canActivate: [AuthGuardService, PermissionGuardService]
   },
   {
     path: "roles/roles-allocation",
     component: RolesAllocationComponent,
     data: {
       title: "User Roles Allocation",
       roles: ["ROLE_ADMIN"],
       capabilities: ["user-management"],
     },
     canActivate: [AuthGuardService, PermissionGuardService]
   },
   {
     path: "sticky-exceptions",
     component: StickyExceptionsComponent,
     data: {
       title: "Exemptions",
       roles: ["ROLE_ADMIN"],
       capabilities: ["exemption-management"],
     },
     canActivate: [AuthGuardService, PermissionGuardService]
   },
   {
     path: "sticky-exceptions/create-sticky-exceptions",
     component: CreateStickyExceptionsComponent,
     data: {
       title: "Create Exemptions",
       roles: ["ROLE_ADMIN"],
       capabilities: ["exemption-management"],
     },
     canActivate: [AuthGuardService, PermissionGuardService]
   },
   {
     path: "sticky-exceptions/delete-sticky-exceptions",
     component: DeleteStickyExceptionsComponent,
     data: {
       title: "Delete Exemptions",
       roles: ["ROLE_ADMIN"],
       capabilities: ["exemption-management"],
     },
     canActivate: [AuthGuardService, PermissionGuardService]
   },
   {
     path: "rules/enable-disable-rule",
     component: EnableDisableRuleComponent,
     data: {
       title: "Enable Disable Rule",
       roles: ["ROLE_ADMIN"],
       capabilities: ["rules-security", "rules-technical-admin"],
     },
     canActivate: [AuthGuardService, PermissionGuardService]
   },
   {
     path: "domains",
     component: DomainsComponent,
     data: {
       title: "Domains",
       roles: ["ROLE_ADMIN"],
       capabilities: ["domain-management"]
     },
     canActivate: [AuthGuardService, PermissionGuardService]
   },
   {
     path: "domains/create-update-domain",
     component: CreateUpdateDomainComponent,
     data: {
       title: "Create Update Domain",
       roles: ["ROLE_ADMIN"],
       capabilities: ["domain-management"]
     },
     canActivate: [AuthGuardService, PermissionGuardService]
   },
   {
     path: "target-types",
     component: TargetTypesComponent,
     data: {
       title: "Target Types",
       roles: ["ROLE_ADMIN"],
       capabilities: ["target-type-management"]
     },
     canActivate: [AuthGuardService, PermissionGuardService]
   },
   {
     path: "target-types/create-update-target-type",
     component: CreateUpdateTargetTypesComponent,
     data: {
       title: "Create Update Target Type",
       roles: ["ROLE_ADMIN"],
       capabilities: ["target-type-management"]
     },
     canActivate: [AuthGuardService, PermissionGuardService]
   },
   {
     path: "job-execution-manager/update-job-execution-manager",
     component: UpdateJobExecutionManagerComponent,
     data: {
       title: "Update Job Execution Manager",
       roles: ["ROLE_ADMIN"],
       capabilities: ["job-execution-management"]
     },
     canActivate: [AuthGuardService, PermissionGuardService]
   },
   {
     path: "roles/config-users",
     component: ConfigUsersComponent,
     data: {
       title: "Config Users",
       roles: ["ROLE_ADMIN"],
       capabilities: ["user-management"]
     },
     canActivate: [AuthGuardService, PermissionGuardService]
   },
   {
         path: 'account-management',
         component: AccountManagementComponent,
         data: {
             title: 'Plugin Manager',
             roles: ['ROLE_ADMIN'],
             pageLevel: 0,
             capabilities: ["account-management"]
           },
           canActivate: [AuthGuardService, PermissionGuardService]
     },
     {
         path: 'account-management/account-management-details',
         component: AccountManagementDetailsComponent,
         data: {
             title: 'Plugin Manager Details',
             roles: ['ROLE_ADMIN'],
             capabilities: ["account-management"]
         },
         canActivate: [AuthGuardService, PermissionGuardService]
     },
   /*  {
         path: 'account-management-create',
         component: AccountManagementDetailsComponent,
         data: {
             title: 'Plugin Manager Details',
             roles: ['ROLE_ADMIN']
         }
     },
     {
         path: 'plugin-management',
         component: PluginManagementComponent,
         data: {
             title: 'Plugin Management',
             roles: ['ROLE_ADMIN']
         }
     },
     {
         path: 'plugin-management-details/:pluginId',
         component: PluginManagementDetailsComponent,
         data: {
             title: 'Plugin Management Details',
             roles: ['ROLE_ADMIN']
         }
     }*/
   {
     path: "user-management",
     component: UserManagementComponent,
     data: {
       title: "Users",
       roles: ["ROLE_ADMIN"],
       pageLevel: 0,
       capabilities: ["user-management"]
     },
     canActivate: [AuthGuardService, PermissionGuardService]
   },
 {
   path: "config-management",
     component: ConfigManagementComponent,
     data: {
     title: "Configuration Management",
       roles: ["ROLE_ADMIN"],
       pageLevel: 0,
       capabilities: ["configuration-management"]
     },
     canActivate: [AuthGuardService, PermissionGuardService]
   },
   {
     path: "system-management",
     component: SystemManagementComponent,
     data: {
       title: "System Management",
       roles: ["ROLE_ADMIN"],
       capabilities: ["system-management"]
     },
     canActivate: [AuthGuardService, PermissionGuardService]
   },
 ];