import { Component, EventEmitter, Input, OnInit, Output } from '@angular/core';

export interface FilterChipUpdateEvent {
    category: string;
    filterName: string;
    filterValue: boolean;
}

@Component({
    selector: 'app-table-filter-item',
    templateUrl: './table-filter-item.component.html',
    styleUrls: ['./table-filter-item.component.css'],
})
export class TableFilterItemComponent implements OnInit {
    @Input() category: string;
    @Input() set filterValues(values: { [key: string]: boolean }) {
        this.appliedFilters = Object.entries(values)
            .filter(([, v]) => v)
            .map(([n]) => n);

        this._filterItems = Object.entries(values).map(([name, value]) => ({
            name,
            value,
        }));
    }

    @Output() clearFilter = new EventEmitter<string>();
    @Output() updateFilter = new EventEmitter<FilterChipUpdateEvent>();

    isFilterMenuShown = false;

    appliedFilters: string[] = [];
    filterOptionQuery = '';

    get filterItems() {
        return this._filterItems;
    }
    private _filterItems: { name: string; value: boolean }[] = [];

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
        return this.filterItems.filter((f) =>
            f.name.toLowerCase().includes(this.filterOptionQuery.toLowerCase()),
        );
    }
}
