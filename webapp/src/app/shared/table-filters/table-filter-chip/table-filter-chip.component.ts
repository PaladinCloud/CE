import { Component, EventEmitter, Input, OnChanges, OnInit, Output, SimpleChanges } from '@angular/core';
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
export class TableFilterChipComponent implements OnInit, OnChanges {
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
    @Output() filterSearchTextChange = new EventEmitter();

    readonly optionsMenuOffsetY = 7;
    readonly maxOptionChars = 30;

    isOptionsMenuOpen = false;

    optionFilterQuery = '';
    filteredOptions = [];

    private _appliedFilters: { name: string; value: boolean }[] = [];
    private _appliedFiltersDict: { [key: string]: boolean } = {};

    constructor(private logger: LoggerService) {}

    ngOnChanges(changes: SimpleChanges): void {
        if(changes.options){
            this.filterOptionsByQuery();
        }
        
    }

    ngOnInit(): void {}

    handleSearchTextChange(searchText){
        const event = {
            searchText,
            selectedFilterCategory: this.category
        };

        this.filterOptionsByQuery();
        this.filterSearchTextChange.emit(event);
    }

    toggleOptionsMenu() {
        this.isDateFilter =this.category.toLowerCase() == "created date" ?  true : false;
        this.isOptionsMenuOpen = !this.isOptionsMenuOpen;
        if(this.isOptionsMenuOpen){
            this.filterOptionsByQuery();
        }else{
            this.optionFilterQuery = '';
        }
    }

    closeMenu(){
        this.isOptionsMenuOpen = false;
        this.optionFilterQuery = '';
    }

    sortCheckedOptionsFirst(){
        const checkedOptions = Object.keys(this.appliedFiltersDict || {}).filter(key => this.appliedFiltersDict[key]);
        const uncheckedOptions = this.options?.filter((f) => !Object.keys(this.appliedFiltersDict).includes(f)) || [];
        this.options = [...checkedOptions, ...uncheckedOptions];
    }

    dateIntervalSelected(from?, to?){
        const toDate = new Date(to).toLocaleDateString('en-CA');
        const fromDate = new Date(from).toLocaleDateString('en-CA');
        this.isOptionsMenuOpen = false;
        this.optionFilterQuery = '';
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
            this.optionFilterQuery = '';
        }
    }

    filterOptionsByQuery() {
        this.sortCheckedOptionsFirst();
        
        try{
            this.filteredOptions = this.options?.filter((f) =>
                f?.toLowerCase()?.includes(this.optionFilterQuery?.toLowerCase()),
            ) || [];

        }catch(e){
            this.logger.log('jsError', e);
        }
    }
}
