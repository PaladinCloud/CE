import { Component, EventEmitter, Input, OnInit, Output } from '@angular/core';
import { MatCheckboxChange } from '@angular/material/checkbox';
import { merge } from 'lodash';
import { FilterItem } from '../table/table.component';
import { FilterChipUpdateEvent } from './table-filter-chip/table-filter-chip.component';

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
    @Input() set appliedFilters(filters) {
        this._appliedFilters = filters;
        const optionDict = filters.reduce((prev, next) => {
            if (!next.value) {
                return prev;
            }
            prev = merge({}, prev, {
                [next.key]: {
                    [next.value]: true,
                },
            });
            return prev;
        }, {});

        this.appliedFiltersDict = merge({}, this.appliedFiltersDict, optionDict);
    }
    get appliedFilters() {
        return this._appliedFilters;
    }

    @Input() set categories(values) {
        this._categories = values;
        this.appliedFiltersDict = merge(
            {},
            this.appliedFiltersDict,
            values.reduce(
                (acc, next) => ({
                    ...acc,
                    [next]: {},
                }),
                {},
            ),
        );
    }
    get categories() {
        return this._categories;
    }

    @Input() categoryOptions: { [key: string]: string[] } = {};

    @Output() filterCategorySelected = new EventEmitter<string>();
    @Output() filterCategoryRemoved = new EventEmitter<string>();
    @Output() filterOptionSelected = new EventEmitter<FilterOptionChange>();

    readonly filterMenuOffsetY = 7;

    appliedFiltersDict: AppliedFilter = {};

    selectedFilterCategory: string = null;

    isFilterMenuOpen = false;
    isFilterSubCategoryOpen = false;

    categoryFilterQuery = '';
    categoryChildFilterQuery = '';

    private _appliedFilters: FilterItem[] = [];
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

    applyFilter(filterOption: string, event: MatCheckboxChange) {
        this.updateFilter({
            category: this.selectedFilterCategory,
            filterName: filterOption,
            filterValue: event.checked,
        });
    }

    updateFilter(event: FilterChipUpdateEvent) {
        const filterCategory = event.category;
        // TODO: REMOVE WHEN API WILL SUPPORT MULTI FILTER VALUE SELECTION
        const uncheckedOptionsDict = Object.entries(this.appliedFiltersDict[filterCategory]).reduce(
            (prev, [name]) => {
                if (name !== event.filterName) {
                    prev[name] = false;
                }
                return prev;
            },
            {},
        );

        this.appliedFiltersDict = merge({}, this.appliedFiltersDict, {
            [filterCategory]: merge({}, uncheckedOptionsDict, {
                [event.filterName]: event.filterValue,
            }),
        });

        this.filterOptionSelected.emit({
            category: event.category,
            option: event.filterName,
            value: event.filterValue,
        });
    }

    clearFilter(filterCategory: string) {
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

    trackByAppliedFilter(index: number, item: FilterItem) {
        return item.key || index;
    }
}
