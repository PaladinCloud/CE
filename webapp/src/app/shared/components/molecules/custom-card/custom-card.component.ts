import {
    Component,
    EventEmitter,
    Input,
    OnChanges,
    OnInit,
    Output,
    SimpleChanges,
    ViewChild,
} from '@angular/core';
import { DateRange } from '@angular/material/datepicker';
import { MatMenuTrigger } from '@angular/material/menu';
import { Subscription } from 'rxjs';
import { AgDomainObservableService } from 'src/app/core/services/ag-domain-observable.service';
import { ALL_TIME } from 'src/app/shared/constants/global';

interface AssetTypesFilters {
    list: string[];
    listData: {
        [key: string]: boolean;
    };
    category: string;
    shortFilters: string[];
    numberOfAllowedFilters: number;
}

interface AssetTypeFilterChangeData {
    category: string;
    filterName: string;
    filterValue: boolean;
}

interface AppliedFilter {
    [categoryName: string]: boolean;
}

@Component({
    selector: 'app-custom-card',
    templateUrl: './custom-card.component.html',
    styleUrls: ['./custom-card.component.css'],
})
export class CustomCardComponent implements OnInit, OnChanges {
    @Output() graphIntervalSelected = new EventEmitter();

    @Input() tabs;
    @Input() tabSelected = 0;
    @Input() selectedItem = ALL_TIME;
    @Input() fromDate: Date;
    @Input() toDate: Date = new Date();
    @Input() minDate: Date = new Date();
    selectedRange?: DateRange<Date>;
    @Input() showDateDropdown = false;
    @Input() card = {
        header: 'Default title',
    };
    @Input() filters: AssetTypesFilters;
    isCustomSelected: boolean = false;
    @ViewChild('menuTrigger') matMenuTrigger: MatMenuTrigger;
    @Output() switchTabs = new EventEmitter<any>();
    @Output() filterChange = new EventEmitter<AssetTypeFilterChangeData>();
    @Output() onChipDropdownClose = new EventEmitter();

    private agDomainSubscription: Subscription;
    private appliedFiltersDict: AppliedFilter | undefined;

    public showFilters: boolean = false;
    public isFilterMenuOpen: boolean = false;

    constructor(private agDomainObservableService: AgDomainObservableService) {
        this.agDomainSubscription = this.agDomainObservableService
            .getAgDomain()
            .subscribe(async ([assetGroupName, domain]) => {
                this.selectedRange = null;
            });
    }

    ngOnInit(): void {}

    ngOnChanges(changes: SimpleChanges) {
        if (changes?.filters?.currentValue?.listData)
            this.appliedFiltersDict = { ...changes?.filters?.currentValue?.listData };
        if (changes?.fromDate?.currentValue) this.fromDate = changes?.fromDate?.currentValue;
        if (changes?.toDate?.currentValue) this.toDate = changes?.toDate?.currentValue;
        if (changes?.minDate?.currentValue) this.minDate = changes?.minDate?.currentValue;
        if (changes?.selectedItem?.currentValue)
            this.selectedItem = changes?.selectedItem?.currentValue;
        this.updateShowFilters();
    }

    switchTabView($event) {
        this.switchTabs.emit($event);
    }

    handleGraphIntervalSelection = (e) => {
        this.toDate = new Date();
        this.selectedItem = e;
        e = e.toLowerCase();
        if (e == 'all time' || e == 'custom') {
            if (e == 'custom') {
                this.isCustomSelected = true;
                this.matMenuTrigger.openMenu();
                return;
            }
            this.dateIntervalSelected(this.minDate, this.toDate);
            return;
        }
        let date = new Date();
        this.isCustomSelected = false;
        this.selectedRange = null;
        switch (e) {
            case '1 week':
                date.setDate(date.getDate() - 7);
                break;
            case '1 month':
                date.setMonth(date.getMonth() - 1);
                break;
            case '6 months':
                date.setMonth(date.getMonth() - 6);
                break;
            case '12 months':
                date.setFullYear(date.getFullYear() - 1);
                break;
        }
        this.dateIntervalSelected(date, this.toDate);
    };

    dateIntervalSelected(from: Date, to: Date) {
        if (this.isCustomSelected) {
            this.selectedRange = new DateRange<Date>(from, to);
        }
        let event = {
            from: from,
            to: to,
            graphInterval: this.selectedItem,
        };
        this.graphIntervalSelected.emit(event);
        this.matMenuTrigger.closeMenu();
    }

    onDropdownClose() {
        if (this.selectedItem == '-1') this.selectedItem = 'Custom';
    }

    chipDropdownClose() {
        this.isFilterMenuOpen = false;
        this.onChipDropdownClose.emit();
    }

    onCustomSelection() {
        if (this.selectedItem == 'Custom') this.selectedItem = '-1';
    }

    onFilterChipClick() {
        this.isFilterMenuOpen = true;
    }

    filtersUpdate(e: AssetTypeFilterChangeData) {
        this.filterChange.emit(e);
    }

    private updateShowFilters() {
        this.showFilters = !!(this.filters?.list?.length !== 0 && this.appliedFiltersDict);
    }

    ngOnDestroy() {
        if (this.agDomainSubscription) this.agDomainSubscription.unsubscribe();
    }
}
