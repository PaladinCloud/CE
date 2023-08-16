import { Component, EventEmitter, Input, OnInit, Output } from '@angular/core';
import { LoggerService } from '../../services/logger.service';

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
    @Input() isDisabled = false;
    @Input() filtersToExcludeFromCasing = [];
    @Input() category: string;
    @Input() options: string[] = [];
    isDateFilter: boolean;
    @Input() set appliedFiltersDict(values: { [key: string]: boolean }) {
        this._appliedFilters = Object.entries(values || {})
            .filter(([, value]) => value)
            .map(([name, value]) => ({
                name,
                value,
            }));
        this._appliedFiltersDict = values || {};
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

    constructor(private logger: LoggerService) {}

    ngOnInit(): void {}

    toggleOptionsMenu() {
        this.isDateFilter =this.category.toLowerCase() == "created date" ?  true : false;
        this.isOptionsMenuOpen = !this.isOptionsMenuOpen;
    }

    dateIntervalSelected(from?, to?){
        const toDate = new Date(to).toLocaleDateString('en-CA');
        const fromDate = new Date(from).toLocaleDateString('en-CA');
        this.isOptionsMenuOpen = false;
        this.updateFilterOption(fromDate+' - '+toDate,true);
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
        try{
            return this.options?.filter((f) =>
            f?.toLowerCase()?.includes(this.optionFilterQuery?.toLowerCase()),
        );
        }catch(e){
            this.logger.log('jsError', e);
            return [];
        }
    }
}
