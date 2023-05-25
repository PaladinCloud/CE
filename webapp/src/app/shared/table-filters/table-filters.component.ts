import { Component, EventEmitter, Input, OnInit, Output } from '@angular/core';
import { MatCheckboxChange } from '@angular/material/checkbox';
import { merge } from 'lodash';
import { FilterChipUpdateEvent } from './table-filter-item/table-filter-item.component';

interface AppliedFilter {
    [name: string]: {
        [child: string]: boolean;
    };
}

export interface FilterOptionChange {
    category: string;
    option: string;
    value: boolean;
}

@Component({
    selector: 'app-table-filters',
    templateUrl: './table-filters.component.html',
    styleUrls: ['./table-filters.component.css'],
})
export class TableFiltersComponent implements OnInit {
    @Input() set categories(values) {
        this._categories = values;
        this.appliedFiltersDict = values.reduce(
            (acc, next) => ({
                ...acc,
                [next]: {},
            }),
            {} as AppliedFilter,
        );
    }
    get categories() {
        return this._categories;
    }

    @Input() categoryOptions: { [key: string]: string[] } = {};

    @Output() filterCategorySelected = new EventEmitter<string>();
    @Output() filterCategoryRemoved = new EventEmitter<string>();
    @Output() filterOptionSelected = new EventEmitter<FilterOptionChange>();

    appliedFiltersDict: AppliedFilter = {};
    appliedFilters: string[] = [];

    selectedFilterCategory: string = null;

    isFilterMenuOpen = false;
    isFilterSubCategoryOpen = false;

    categoryFilterQuery = '';
    categoryChildFilterQuery = '';

    private _categories: string[] = [];

    constructor() {}

    ngOnInit(): void {}

    openMenu() {
        this.isFilterMenuOpen = !this.isFilterMenuOpen;
        this.isFilterSubCategoryOpen = false;
    }

    openFilterCategory(filterCategory: string) {
        this.isFilterSubCategoryOpen = true;
        this.selectedFilterCategory = filterCategory;
        this.filterCategorySelected.emit(filterCategory);
    }

    applyFilter(filterChild: string, event: MatCheckboxChange) {
        const filterCatName = this.selectedFilterCategory;

        // TODO: REMOVE WHEN API WILL SUPPORT MULTI FILTER VALUE SELECTION
        const uncheckedOptionsDict = Object.entries(this.appliedFiltersDict[filterCatName]).reduce(
            (prev, [name]) => {
                if (name !== filterChild) {
                    prev[name] = false;
                }
                return prev;
            },
            {},
        );

        this.appliedFiltersDict = merge({}, this.appliedFiltersDict, {
            [filterCatName]: merge({}, uncheckedOptionsDict, {
                [filterChild]: event.checked,
            }),
        });

        if (this.appliedFilters.includes(filterCatName)) {
            if (Object.values(this.appliedFiltersDict[filterCatName]).every((i) => !i)) {
                this.appliedFilters = this.appliedFilters.filter((f) => f !== filterCatName);
                this.filterCategoryRemoved.emit(filterCatName);
                return;
            }
        } else {
            this.appliedFilters = this.appliedFilters.concat(filterCatName);
        }
        this.filterOptionSelected.emit({
            category: filterCatName,
            option: filterChild,
            value: event.checked,
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

        this.filterOptionSelected.emit({
            category: event.category,
            option: event.filterName,
            value: event.filterValue,
        });
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

        this.filterCategoryRemoved.emit(filterCategory);
    }

    overlayKeyDown(event: KeyboardEvent) {
        if (event.key === 'Escape') {
            this.isFilterMenuOpen = false;
        }
    }

    filterCategoryByQuery() {
        return this.categories.filter((c) =>
            c.toLowerCase().includes(this.categoryFilterQuery.toLowerCase()),
        );
    }

    filterSelectedCategoryChildrenByQuery() {
        return (
            this.categoryOptions[this.selectedFilterCategory]?.filter((c) =>
                c?.toLowerCase().includes(this.categoryChildFilterQuery.toLowerCase()),
            ) || []
        );
    }
}
