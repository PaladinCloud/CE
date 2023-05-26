import { Component, EventEmitter, Input, OnInit, Output } from '@angular/core';

export interface FilterChipUpdateEvent {
    category: string;
    filterName: string;
    filterValue: boolean;
}

@Component({
    selector: 'app-table-filter-chip',
    templateUrl: './table-filter-chip.component.html',
    styleUrls: ['./table-filter-chip.component.css'],
})
export class TableFilterChipComponent implements OnInit {
    @Input() category: string;
    @Input() filterValues: string[] = [];
    @Input() set appliedFiltersDict(values: { [key: string]: boolean }) {
        this._appliedFilters = Object.entries(values)
            .filter(([, value]) => value)
            .map(([name, value]) => ({
                name,
                value,
            }));
        this._appliedFiltersDict = values;
    }
    get appliedFiltersDict() {
        return this._appliedFiltersDict;
    }

    get appliedFilters() {
        return this._appliedFilters;
    }

    @Output() clearFilter = new EventEmitter<string>();
    @Output() updateFilter = new EventEmitter<FilterChipUpdateEvent>();

    isFilterMenuShown = false;

    filterOptionQuery = '';

    private _appliedFilters: { name: string; value: boolean }[] = [];
    private _appliedFiltersDict: { [key: string]: boolean } = {};

    constructor() {}

    ngOnInit(): void {}

    toggleFilterMenu() {
        this.isFilterMenuShown = !this.isFilterMenuShown;
    }

    updateFilterOption(filterName: string, filterValue: boolean) {
        this.updateFilter.next({
            category: this.category,
            filterName,
            filterValue,
        });
    }

    overlayKeyDown(event: KeyboardEvent) {
        if (event.key === 'Escape') {
            this.isFilterMenuShown = false;
        }
    }

    filterOptionsByQuery() {
        return this.filterValues.filter((f) =>
            f.toLowerCase().includes(this.filterOptionQuery.toLowerCase()),
        );
    }
}
