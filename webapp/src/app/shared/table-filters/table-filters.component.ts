import { Component, EventEmitter, Input, OnInit, Output } from '@angular/core';
import { MatCheckboxChange } from '@angular/material/checkbox';
import { merge } from 'lodash';
import { FilterChipUpdateEvent } from './table-filter-item/table-filter-item.component';

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
    // @Input() categories: string[] = [];
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
