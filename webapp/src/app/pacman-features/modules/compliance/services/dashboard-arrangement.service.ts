import { Injectable } from '@angular/core';

export type DashboardArrangementItems = [number, number, number, number];
export type DasbhoardCollapsedDict = { [key: number]: boolean };

@Injectable()
export class DashboardArrangementService {
    private readonly DASHBOARD_ARRANGEMENT_LS_KEY = 'dashboard-arrangement';
    private readonly DEFAULT_DASHBOARD_ARRANGEMENT: DashboardArrangementItems = [0, 1, 2, 3];
    private readonly DASHBOARD_COLLAPSED_LS_KEY = 'dashboard-collapsed';
    private readonly DEFAULT_DASHBOARD_COLLAPSED = {
        0: false,
        1: false,
        2: false,
        3: false,
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
