import { Component, Input, OnInit } from '@angular/core';
import { MatCheckboxChange } from '@angular/material/checkbox';

export interface TableFilter {
    name: string;
    children: string[];
}

interface AppliedFilter {
    [name: string]: {
        [child: string]: boolean;
    };
}

@Component({
    selector: 'app-table-filters',
    templateUrl: './table-filters.component.html',
    styleUrls: ['./table-filters.component.css'],
})
export class TableFiltersComponent implements OnInit {
    @Input() set filters(values: TableFilter[]) {
        this.appliedFilters = values.reduce(
            (acc, next) => ({
                ...acc,
                [next.name]: next.children.reduce((prev, child) => {
                    prev[child] = false;
                    return prev;
                }, {}),
            }),
            {} as AppliedFilter,
        );

        this._filters = values;
    }

    get filters() {
        return this._filters;
    }

    appliedFilters: AppliedFilter = {};

    selectedFilterCategory: TableFilter = null;

    isFilterMenuOpen = false;
    isFilterSubCategoryOpen = false;

    private _filters: TableFilter[] = [];

    constructor() {}

    ngOnInit(): void {}

    openMenu() {
        this.isFilterMenuOpen = !this.isFilterMenuOpen;
        this.isFilterSubCategoryOpen = false;
    }

    openFilterCategory(filterCategory: TableFilter) {
        this.isFilterSubCategoryOpen = true;
        this.selectedFilterCategory = filterCategory;
    }

    applyFilter(filterChild: string, event: MatCheckboxChange) {
        this.appliedFilters[this.selectedFilterCategory.name][filterChild] = event.checked;
    }
}
