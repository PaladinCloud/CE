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
    @Input() options: string[] = [];
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

    @Output() clear = new EventEmitter<string>();
    @Output() update = new EventEmitter<FilterChipUpdateEvent>();

    readonly optionsMenuOffsetY = 7;
    readonly maxOptionChars = 30;

    isOptionsMenuOpen = false;

    optionFilterQuery = '';

    private _appliedFilters: { name: string; value: boolean }[] = [];
    private _appliedFiltersDict: { [key: string]: boolean } = {};

    constructor() {}

    ngOnInit(): void {}

    toggleOptionsMenu() {
        this.isOptionsMenuOpen = !this.isOptionsMenuOpen;
    }

    updateFilterOption(filterName: string, filterValue: boolean) {
        this.update.next({
            category: this.category,
            filterName,
            filterValue,
        });
    }

    overlayKeyDown(event: KeyboardEvent) {
        if (event.key === 'Escape') {
            this.isOptionsMenuOpen = false;
        }
    }

    filterOptionsByQuery() {
        return this.options.filter((f) =>
            f.toLowerCase().includes(this.optionFilterQuery.toLowerCase()),
        );
    }
}
