import { Component, EventEmitter, Input, OnInit, Output } from '@angular/core';
import { MatCheckboxChange } from '@angular/material/checkbox';

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

        this._filterValues = values;
    }
    get filterValues() {
        return this._filterValues;
    }
    @Output() clearFilter = new EventEmitter<string>();
    @Output() updateFilter = new EventEmitter<FilterChipUpdateEvent>();

    isFilterMenuShown = false;

    appliedFilters: string[] = [];

    private _filterValues: { [key: string]: boolean } = {};

    constructor() {}

    ngOnInit(): void {}

    toggleFilterMenu() {
        this.isFilterMenuShown = !this.isFilterMenuShown;
    }

    updateFilterCategory(filterName: string, event: MatCheckboxChange) {
        this.updateFilter.next({
            category: this.category,
            filterName,
            filterValue: event.checked,
        });
    }
}
