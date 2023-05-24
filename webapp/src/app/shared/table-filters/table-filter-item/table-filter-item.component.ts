import { Component, EventEmitter, Input, OnInit, Output } from '@angular/core';

@Component({
    selector: 'app-table-filter-item',
    templateUrl: './table-filter-item.component.html',
    styleUrls: ['./table-filter-item.component.css'],
})
export class TableFilterItemComponent implements OnInit {
    @Input() name: string;
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

    appliedFilters: string[] = [];

    private _filterValues: { [key: string]: boolean } = {};

    constructor() {}

    ngOnInit(): void {}
}
