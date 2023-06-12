import { Component, EventEmitter, Input, OnInit, Output } from '@angular/core';
import { MatCheckboxChange } from '@angular/material/checkbox';
import merge from 'lodash/merge';
import { FilterItem } from '../table/table.component';
import { FilterChipUpdateEvent } from './table-filter-chip/table-filter-chip.component';

interface AppliedFilter {
    [categoryName: string]: {
        [optionName: string]: boolean;
    };
}

export interface OptionChange {
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
    @Input() set appliedFilters(filters: FilterItem[] | undefined) {
        this._appliedFilters = filters || [];
        const optionDict = this._appliedFilters.reduce((prev, next) => {
            if (!next.filterValue || !next.key) {
                return prev;
            }
            prev = merge({}, prev, {
                [next.key]: {
                    [next.filterValue]: true,
                },
            });
            return prev;
        }, {});

        // TODO: REMOVE IF STATEMENT WHEN API WILL SUPPORT MULTI FILTER VALUE SELECTION
        if (Object.keys(this.appliedFiltersDict).length) {
            this.appliedFiltersDict = Object.keys(this.appliedFiltersDict).reduce((acc, next) => {
                acc[next] = optionDict[next] ? optionDict[next] : this.appliedFiltersDict[next];
                return acc;
            }, {});
            return;
        }
        this.appliedFiltersDict = merge({}, this.appliedFiltersDict, optionDict);
    }

    get appliedFilters() {
        return this._appliedFilters;
    }

    @Input() set categories(values: string[] | undefined) {
        this._categories = values || [];
        this.appliedFiltersDict = merge(
            {},
            this.appliedFiltersDict,
            this._categories.reduce(
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

    @Output() categoryChange = new EventEmitter<string>();
    @Output() categoryClear = new EventEmitter<string>();
    @Output() optionChange = new EventEmitter<OptionChange>();

    readonly filterMenuOffsetY = 7;
    readonly maxOptionChars = 30;

    appliedFiltersDict: AppliedFilter = {};

    selectedCategory: string | null = null;

    isCategoryMenuOpen = false;
    isCategoryOptionsMenuOpen = false;

    categoryFilterQuery = '';
    categoryOptionFilterQuery = '';

    private _appliedFilters: FilterItem[] = [];
    private _categories: string[] = [];

    constructor() {}

    ngOnInit(): void {}

    openMenu() {
        this.isCategoryMenuOpen = !this.isCategoryMenuOpen;
        this.isCategoryOptionsMenuOpen = false;
    }

    openFilterCategory(filterCategory: string) {
        this.isCategoryOptionsMenuOpen = true;
        this.selectedCategory = filterCategory;
        this.categoryChange.emit(filterCategory);
    }

    applyFilter(filterOption: string, event: MatCheckboxChange) {
        if (!this.selectedCategory) {
            return;
        }
        this.updateFilter({
            category: this.selectedCategory,
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

        this.optionChange.emit({
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

        this.categoryClear.emit(filterCategory);
    }

    overlayKeyDown(event: KeyboardEvent) {
        if (event.key === 'Escape') {
            this.isCategoryMenuOpen = false;
        }
    }

    filterCategoriesByQuery() {
        return (
            this.categories?.filter((c) =>
                c.toLowerCase().includes(this.categoryFilterQuery.toLowerCase()),
            ) || []
        );
    }

    filterSelectedCategoryOptionsByQuery() {
        if (!this.selectedCategory) {
            return [];
        }
        return (
            this.categoryOptions[this.selectedCategory]?.filter((c) =>
                c?.toLowerCase().includes(this.categoryOptionFilterQuery.toLowerCase()),
            ) || []
        );
    }

    trackByAppliedFilter(index: number, item: FilterItem) {
        return item.key || index;
    }
}
