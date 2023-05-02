export interface MenuItem {
    id: string;
    name: string;
    image?: string;
    route?: string;
    children?: MenuItemChild[];
    overlay?: boolean;
}
export interface MenuItemChild {
    id: string;
    name: string;
    image?: string;
    parent: string;
    route: string;
    notDisplayIfAzure?: boolean;
    permissions?: string[];
}

export const MENU_NODES: MenuItem[] = [
    {
        id: 'Dashboard',
        name: 'Dashboard',
        image: 'dashboard-icon',
        route: '/pl/compliance/compliance-dashboard',
    },
    {
        id: 'Violations',
        name: 'Violations',
        image: 'violations-icon',
        route: '/pl/compliance/issue-listing',
    },
    {
        id: 'Assets',
        name: 'Assets',
        image: 'assets-icon',
        children: [
            {
                id: 'Summary',
                name: 'Summary',
                parent: 'Assets',
                route: '/pl/assets/asset-dashboard',
            },
            {
                id: 'Distribution',
                name: 'Distribution',
                parent: 'Assets',
                route: '/pl/assets/asset-distribution',
            },
            {
                id: 'List',
                name: 'List',
                parent: 'Assets',
                route: '/pl/assets/asset-list',
            },
        ],
    },
    {
        id: 'Policy',
        name: 'Policy',
        image: 'policy-icon',
        route: '/pl/compliance/policy-knowledgebase',
    },
    {
        id: 'Tagging',
        name: 'Tagging',
        image: 'tagging-icon',
        route: '/pl/compliance/tagging-compliance',
    },
    {
        id: 'Fix Central',
        name: 'Fix Central',
        image: 'fix-central-icon',
        children: [
            {
                id: 'Health Notifications',
                name: 'Notifications',
                parent: 'Fix Central',
                route: '/pl/notifications/notifications-list',
                notDisplayIfAzure: true,
            },
            {
                id: 'Recommendations',
                name: 'Recommendations',
                parent: 'Fix Central',
                route: '/pl/compliance/recommendations',
            },
            // { id: 11, name: "Fixes", parent: "Fix Central" },
        ],
    },
    {
        id: 'Statistics',
        name: 'Statistics',
        image: 'statistics-icon',
        route: 'stats-overlay',
        overlay: true,
    },
    {
        id: 'Admin',
        name: 'Admin',
        image: 'admin-icon',
        children: [
            {
                id: 'admin-policy',
                name: 'Policy',
                parent: 'Admin',
                route: '/pl/admin/policies',
                permissions: ['policy-management'],
            },
            {
                id: 'Job Execution Manager',
                name: 'Job Execution Manager',
                parent: 'Admin',
                route: '/pl/admin/job-execution-manager',
                permissions: ['job-execution-management'],
            },
            {
                id: 'Domains',
                name: 'Domains',
                parent: 'Admin',
                route: '/pl/admin/domains',
                permissions: ['domain-management'],
            },
            {
                id: 'Asset Types',
                name: 'Asset Types',
                parent: 'Admin',
                route: '/pl/admin/target-types',
                permissions: ['target-type-management'],
            },
            {
                id: 'Asset Groups',
                name: 'Asset Groups',
                parent: 'Admin',
                route: '/pl/admin/asset-groups',
                permissions: ['asset-group-management'],
            },
            {
                id: 'Sticky Exceptions',
                name: 'Sticky Exceptions',
                parent: 'Admin',
                route: '/pl/admin/sticky-exceptions',
                permissions: ['exemption-management'],
            },
            {
                id: 'Roles',
                name: 'Roles',
                parent: 'Admin',
                route: '/pl/admin/roles',
                permissions: ['user-management'],
            },
            {
                id: 'User Management',
                name: 'User Management',
                parent: 'Admin',
                route: '/pl/admin/user-management',
                permissions: ['user-management'],
            },
            {
                id: 'Account Management',
                name: 'Account Management',
                parent: 'Admin',
                route: '/pl/admin/account-management',
                permissions: ['account-management'],
            },
            {
                id: 'Configuration Management',
                name: 'Configuration Management',
                parent: 'Admin',
                route: '/pl/admin/config-management',
                permissions: ['configuration-management'],
            },
            {
                id: 'System Management',
                name: 'System Management',
                parent: 'Admin',
                route: '/pl/admin/system-management',
                permissions: ['system-management'],
            },
        ],
    },
];
