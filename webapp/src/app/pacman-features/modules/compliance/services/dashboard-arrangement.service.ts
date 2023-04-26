import { Injectable } from '@angular/core';

export type DashboardArrangementItems = [number, number, number, number];

@Injectable()
export class DashboardArrangementService {
    private readonly DASHBOARD_ARRANGEMENT_LS_KEY = 'dashboard-arrangement';
    private readonly DEFAULT_DASHBOARD_ARRANGEMENT: DashboardArrangementItems = [0, 1, 2, 3];

    constructor() {}

    get() {
        const item = localStorage.getItem(this.DASHBOARD_ARRANGEMENT_LS_KEY);

        if (item) {
            return JSON.parse(item) as DashboardArrangementItems;
        }
        return this.DEFAULT_DASHBOARD_ARRANGEMENT;
    }

    save(list: DashboardArrangementItems) {
        localStorage.setItem(this.DASHBOARD_ARRANGEMENT_LS_KEY, JSON.stringify(list));
    }
}
