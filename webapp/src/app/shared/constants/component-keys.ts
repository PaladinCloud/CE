export enum NonAdminComponentKeys {
    Dashboard = "dashboard",
    ViolationList = "violationList",
    AssetList = "assetList",
    NotificationList = "notificationList",
    ComplianceCategoryAssetList = "complianceCategoryAssetList",
    ComplianceCategoryPolicyList = "complianceCategoryPolicyList",
}

export enum AdminComponentKeys {
    UserPolicyList = "userPolicyList",
    AdminPolicyList = "adminPolicyList",
    UserManagementList = "userManagementList",
    AdminAssetGroupList = "adminAssetGroupList",
    AccountManagementList = "accountManagementList",
    ActivityLogs = "activityLogs"
}

export const ComponentKeys = {...NonAdminComponentKeys, ...AdminComponentKeys};