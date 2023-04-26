import { Injectable } from '@angular/core';

export type DashboardArrangementItems = [number, number, number, number];
export type DasbhoardCollapsedDict = { [key in DashboardContainerIndex]: boolean };

export enum DashboardContainerIndex {
    VIOLATION_SEVERITY = 0,
    CATEGORY_COMPLIANCE = 1,
    ASSET_GRAPH = 2,
    POLICY_OVERVIEW = 3,
}

@Injectable()
export class DashboardArrangementService {
    private readonly DASHBOARD_ARRANGEMENT_LS_KEY = 'dashboard-arrangement';
    private readonly DEFAULT_DASHBOARD_ARRANGEMENT: DashboardArrangementItems = [
        DashboardContainerIndex.VIOLATION_SEVERITY,
        DashboardContainerIndex.CATEGORY_COMPLIANCE,
        DashboardContainerIndex.ASSET_GRAPH,
        DashboardContainerIndex.POLICY_OVERVIEW,
    ];
    private readonly DASHBOARD_COLLAPSED_LS_KEY = 'dashboard-collapsed';
    private readonly DEFAULT_DASHBOARD_COLLAPSED = {
        [DashboardContainerIndex.VIOLATION_SEVERITY]: false,
        [DashboardContainerIndex.CATEGORY_COMPLIANCE]: false,
        [DashboardContainerIndex.ASSET_GRAPH]: false,
        [DashboardContainerIndex.POLICY_OVERVIEW]: false,
    };

    constructor() {}

    getArrangement() {
        const item = localStorage.getItem(this.DASHBOARD_ARRANGEMENT_LS_KEY);

        if (item) {
            return JSON.parse(item) as DashboardArrangementItems;
        }
        return this.DEFAULT_DASHBOARD_ARRANGEMENT;
    }

    saveArrangement(list: DashboardArrangementItems) {
        localStorage.setItem(this.DASHBOARD_ARRANGEMENT_LS_KEY, JSON.stringify(list));
    }

    getCollapsed() {
        const item = localStorage.getItem(this.DASHBOARD_COLLAPSED_LS_KEY);

        if (item) {
            return JSON.parse(item) as DasbhoardCollapsedDict;
        }
        return this.DEFAULT_DASHBOARD_COLLAPSED;
    }

    saveCollapsed(dict: DasbhoardCollapsedDict) {
        localStorage.setItem(this.DASHBOARD_COLLAPSED_LS_KEY, JSON.stringify(dict));
    }
}
