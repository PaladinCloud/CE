import { Component, Input, OnInit } from '@angular/core';
import { MatCheckboxChange } from '@angular/material/checkbox';
import { merge } from 'lodash';
import { FilterChipUpdateEvent } from './table-filter-item/table-filter-item.component';

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
        this.appliedFiltersDict = values.reduce(
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

    appliedFiltersDict: AppliedFilter = {};
    appliedFilters: string[] = [];

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
        const filterCatName = this.selectedFilterCategory.name;

        this.appliedFiltersDict = merge({}, this.appliedFiltersDict, {
            [filterCatName]: {
                [filterChild]: event.checked,
            },
        });

        if (this.appliedFilters.includes(filterCatName)) {
            if (Object.values(this.appliedFiltersDict[filterCatName]).every((i) => !i)) {
                this.appliedFilters = this.appliedFilters.filter((f) => f !== filterCatName);
            }
        } else {
            this.appliedFilters = this.appliedFilters.concat(filterCatName);
        }
    }

    clearFilter(filterCategory: string) {
        this.appliedFilters = this.appliedFilters.filter((f) => f !== filterCategory);
        const resettedFilter = Object.keys(this.appliedFiltersDict[filterCategory]).reduce(
            (acc, next) => {
                acc[next] = false;
                return acc;
            },
            {},
        );

        this.appliedFiltersDict = merge({}, this.appliedFiltersDict, {
            [filterCategory]: resettedFilter,
        });
    }

    updateFilter(event: FilterChipUpdateEvent) {
        const filterCategory = event.category;
        const updatedFilter = merge({}, this.appliedFiltersDict[filterCategory], {
            [event.filterName]: event.filterValue,
        });

        this.appliedFiltersDict = merge({}, this.appliedFiltersDict, {
            [filterCategory]: updatedFilter,
        });
    }

    overlayKeyDown(event: KeyboardEvent) {
        if (event.key === 'Escape') {
            this.isFilterMenuOpen = false;
        }
    }
}
