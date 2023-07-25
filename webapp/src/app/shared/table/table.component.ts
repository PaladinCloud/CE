import {
    AfterViewInit,
    Component,
    ElementRef,
    EventEmitter,
    Input,
    OnChanges,
    OnDestroy,
    OnInit,
    Output,
    SimpleChanges,
    ViewChild,
} from '@angular/core';
import { FormControl } from '@angular/forms';
import { MatOption } from '@angular/material/core';
import { MatSelect } from '@angular/material/select';
import { Sort, SortDirection } from '@angular/material/sort';
import { MatTableDataSource } from '@angular/material/table';
import { Subject } from 'rxjs';
import { skip, takeUntil } from 'rxjs/operators';
import { AssetGroupObservableService } from 'src/app/core/services/asset-group-observable.service';
import { AppliedFilterTags, OptionChange } from '../table-filters/table-filters.component';

export interface FilterItem {
    filterValue?: string[] | string | undefined;
    key?: string;
    keyDisplayValue: string;
    value?: string[] | string | undefined;
    compareKey?: string;
    filterkey?: string;
}

@Component({
    selector: 'app-table',
    templateUrl: './table.component.html',
    styleUrls: ['./table.component.css'],
})
export class TableComponent implements OnInit, AfterViewInit, OnChanges, OnDestroy {
    @Input() centeredColumns: { [key: string]: boolean } = {};
    @Input() columnsSortFunctionMap;
    @Input() filterFunctionMap;
    @Input() set columnWidths(value: { [key: string]: number }) {
        this._columnWidths = value;
        this.denominator = Object.keys(value).reduce((acc, next) => acc + value[next], 0);
    }
    get columnWidths() {
        return this._columnWidths;
    }
    @Input() data = [];
    @Input() direction: SortDirection;
    @Input() enableMultiValuedFilter = false;
    @Input() doLocalFilter = false;
    @Input() doLocalSearch = false;
    @Input() doLocalSort = true;
    @Input() doNotSort = false;
    @Input() filterTagLabels: { [key: string]: string[] };
    @Input() filterTypeLabels = [];
    @Input() headerColName: string;
    @Input() onScrollDataLoader: Subject<any>;
    @Input() rowClickable = true;
    @Input() rowSize: 'SM' | 'MD' | 'LG' = 'SM';
    @Input() searchQuery = '';
    @Input() selectedRowIndex: number;
    @Input() showAddRemoveCol: boolean;
    @Input() showCopyButton = true;
    @Input() showDownloadBtn: boolean;
    @Input() showFilterBtn: boolean;
    @Input() showMoreMenu = true;
    @Input() showSearchBar: boolean;
    @Input() tableDataLoaded: boolean;
    @Input() tableErrorMessage = '';
    @Input() tableScrollTop: number;
    @Input() tableTitle: string;
    @Input() totalRows = 0;
    @Input() whiteListColumns: string[] = [];
    @Input() set filteredArray(filterItems: FilterItem[]){
        filterItems?.forEach(filterItem => {
            if(typeof filterItem.value == "string" && typeof filterItem.filterValue == "string"){
                filterItem.value = filterItem.value?.split(",");
                filterItem.filterValue = filterItem.filterValue?.split(",");
            }
        })
        this._filteredArray = filterItems;
    }
    get filteredArray() {
        return this._filteredArray;
    }
    @Input() filterTypeOptions: {
        optionName: string;
        optionURL: string;
        optionValue: string;
    }[];

    private _filteredArray: FilterItem[];
    @Output() actionSelected = new EventEmitter();
    @Output() deleteFilters = new EventEmitter<{
        index?: number;
        removeOnlyFilterValue?: boolean;
        clearAll?: boolean;
    }>();
    @Output() downloadClicked = new EventEmitter<{
        searchTxt: string;
        filters: FilterItem[];
    }>();
    @Output() headerColNameSelected = new EventEmitter<{
        headerColName: string;
        direction: SortDirection;
    }>();
    @Output() nextPageCalled = new EventEmitter<number>();
    @Output() rowSelectEventEmitter = new EventEmitter<any>();
    @Output() searchCalledEventEmitter = new EventEmitter<string>();
    @Output() searchEventEmitter = new EventEmitter<string>();
    @Output() searchInColumnsChanged = new EventEmitter<string>();
    @Output() selectedFilter = new EventEmitter<{
        index: number;
        filterValue: string[] | string;
        filterKeyDisplayValue: string;
    }>();
    @Output() selectedFilterType = new EventEmitter<string>();
    @Output() whitelistColumnsChanged = new EventEmitter<string[]>();

    @ViewChild('select') select: MatSelect;
    @ViewChild('customTable') customTable: ElementRef<HTMLDivElement>;

    mainDataSource: MatTableDataSource<unknown>;
    dataSource: MatTableDataSource<unknown>;

    displayedColumns: string[];
    searchInColumns = new FormControl();

    allSelected = true;
    totalRecordsAfterFilter = 0;

    denominator = 1;

    isWindowExpanded = true;
    isDataLoading = false;
    selectedFiltersList: string[] = [];
    destroy$ = new Subject<void>();

    private _columnWidths: { [key: string]: number } = {};
    private sortByColumn = {
        severity: (a, b, isAsc) => {
            const severeness = { low: 1, medium: 2, high: 3, critical: 4, default: 5 * (isAsc ? 1 : -1) };
      
            const ASeverity = a["Severity"].valueText?.toLowerCase()??"default";
            const BSeverity = b["Severity"].valueText?.toLowerCase()??"default";
            
            return (severeness[ASeverity] < severeness[BSeverity] ? -1 : 1) * (isAsc ? 1 : -1);
          },
          category: (a, b, isAsc) => {
            const priority = {"security":4, "operations":3, "cost":2, "tagging":1, "default": 5 * (isAsc ? 1 : -1)}
      
            const ACategory = a["Category"].valueText??"default";
            const BCategory = b["Category"].valueText??"default";
            if(priority[ACategory] == priority[BCategory]){
              return a['Violations']<b['Violations'] ? -1: 1
            }
            return (priority[ACategory] < priority[BCategory] ? -1 : 1) * (isAsc ? 1 : -1);
          },
    }

    constructor(private assetGroupChangeService: AssetGroupObservableService) {}

    ngOnInit(): void {
        if (this.onScrollDataLoader) {
            this.onScrollDataLoader.pipe(takeUntil(this.destroy$)).subscribe((data) => {
                this.isDataLoading = false;
                if (data && data.length > 0) {
                    this.data.push(...data);
                    this.mainDataSource = new MatTableDataSource(this.data);
                    this.dataSource = new MatTableDataSource(this.data);
                }
            });
        }

        this.assetGroupChangeService
            .getAssetGroup()
            .pipe(skip(1), takeUntil(this.destroy$))
            .subscribe(() => {
                this.removeAllFilters();
            });
    }

    ngOnChanges(changes: SimpleChanges): void {
        if (this.customTable) {
            if (changes.tableScrollTop && changes.tableScrollTop.currentValue != undefined) {
                this.customTable.nativeElement.scrollTop = this.tableScrollTop;
            }
            if (!this.tableDataLoaded) {
                this.tableScrollTop = 0;
                this.customTable.nativeElement.scrollTop = 0;
                this.mainDataSource = new MatTableDataSource([]);
                this.dataSource = new MatTableDataSource([]);
            }
        }
        if (changes.columnWidths) {
            if (this.columnWidths) {
                this.displayedColumns = Object.keys(this.columnWidths);
            }

            this.allSelected = this.displayedColumns.length === this.whiteListColumns.length;

            this.displayedColumns.sort();

            if (this.select) {
                this.select.options.forEach((item: MatOption) => {
                    if (this.allSelected || this.whiteListColumns.includes(item.value)) {
                        item.select();
                    } else {
                        item.deselect();
                    }
                });
            }
        }
        if (changes.data || changes.filteredArray) {
            if (changes.data) {
                this.mainDataSource = new MatTableDataSource(this.data);
                this.dataSource = new MatTableDataSource(this.data);
            }
            this.totalRecordsAfterFilter = this.totalRows;
        }
        if ((this.doLocalSearch || this.doLocalSort) && this.tableDataLoaded) {
            this.filterAndSort();
        }
    }

    ngAfterViewInit(): void {
        this.customTable.nativeElement.scrollTop = this.tableScrollTop;
    }

    ngOnDestroy(): void {
        this.destroy$.next();
        this.destroy$.complete();
    }

    filterAndSort() {
        if (this.doLocalSearch) {
            this.customSearch();
        }
        if (this.headerColName && this.direction && this.doLocalSort) {
            this.customSort(this.headerColName, this.direction);
        }
    }

    openColumnSelectorModal() {
        this.select.open();
    }

    handleSearchInColumnsChange() {
        this.searchInColumnsChanged.emit(this.searchInColumns.value);
    }

    whiteListColumnsChanged() {
        this.whitelistColumnsChanged.emit(this.whiteListColumns);
    }

    handleClick(row, col: string, i: number) {
        if (row[col].isMenuBtn || row[col].isCheckbox) {
            return;
        }
        const event = {
            tableScrollTop: this.customTable.nativeElement.scrollTop,
            rowSelected: row,
            data: this.data,
            col,
            filters: this.filteredArray,
            searchTxt: this.searchQuery,
            selectedRowIndex: i,
        };
        this.rowSelectEventEmitter.emit(event);
    }

    handleAction(element, action: string, i: number) {
        const event = {
            action: action,
            rowSelected: element,
            selectedRowIndex: i,
        };
        this.actionSelected.emit(event);
    }

    handleColumnSelection(selectedColumns: string[]) {
        this.whiteListColumns = Object.keys(this.columnWidths).filter((c) =>
            selectedColumns.includes(c),
        );
        this.whiteListColumnsChanged();
    }


    selectFilterCategory(category: string) {
        if (this.filteredArray.some((i) => i.keyDisplayValue === category)) {
            this.handleFilterTagsDropdownOpen(category);
            return;
        }
        this.filteredArray.push({
            keyDisplayValue: category,
            filterValue: undefined,
        });
        const filteredArrayKeys = this.filteredArray.map((item) => item.keyDisplayValue);
        const index = filteredArrayKeys.indexOf(category)>=0?filteredArrayKeys.indexOf(category):this.filteredArray.length-1;
        this.onSelectFilterType(category, index);
    }

    removeOnlyFilterValue(index: number) {
        this.filteredArray[index].value = undefined;
        this.filteredArray[index].filterValue = undefined;
        if (this.doLocalFilter) {
            this.filterAndSort();
        }
        this.deleteFilters.emit({
            index,
            removeOnlyFilterValue: true,
        });
    }

    selectFilterCategoryOption(event: OptionChange) {
        let index = this.filteredArray.findIndex((i) => i.key === event.category);

        const appliedFilterTags = event.appliedFilterTags;

        if(appliedFilterTags.length==0){
            this.removeOnlyFilterValue(index);
            return;
        }

        if (index === -1) {
            let filterItem = {
                key: event.category,
                keyDisplayValue: event.category,
                value: undefined,
            };

            if (!this.doLocalFilter) {
                const key = this.filterTypeOptions.find(
                    (f) => f.optionName === event.category,
                ).optionValue;

                filterItem = {
                    ...filterItem,
                    ...{
                        compareKey: key.toLowerCase().trim(),
                        filterkey: key.trim(),
                        filterValue: undefined,
                    },
                };
            }

            this.filteredArray = [...this.filteredArray, filterItem];
            index = this.filteredArray.length - 1;
        } else {
            this.filteredArray[index].filterValue = this.filteredArray[index].value = appliedFilterTags;
        }

        if (this.doLocalFilter) {
            this.filterAndSort();
        }

        this.selectedFilter.emit({
            index,
            filterKeyDisplayValue: event.category,
            filterValue: this.enableMultiValuedFilter?event.appliedFilterTags:event.appliedFilterTags[0],
        });
    }

    onSelectFilter(e: string[] | undefined, i: number) {
        if (!e) {
            this.removeFilter(i);
            return;
        }

        const filterIndex = this.filteredArray.findIndex(
            (el, j) => el.keyDisplayValue === this.filteredArray[i].keyDisplayValue && i !== j,
        );

        let currIdx = i;

        if (filterIndex >= 0 && filterIndex != i) {
            if (filterIndex > i) {
                this.filteredArray.splice(filterIndex, 1);
                currIdx = i;
            } else {
                this.filteredArray.splice(i, 1);
                currIdx = filterIndex;
            }
        }

        this.filteredArray[currIdx].filterValue = e;
        this.filteredArray[currIdx].value = e;

        if (this.doLocalFilter) {
            this.filterAndSort();
        }
        const event = {
            index: currIdx,
            filterKeyDisplayValue: this.filteredArray[currIdx].keyDisplayValue,
            filterValue: this.filteredArray[currIdx].filterValue,
        };
        this.selectedFilter.emit(event);
    }

    handleFilterTagsDropdownOpen(e){
        this.selectedFilterType.emit(e);
    }

    onSelectFilterType(e: string, i: number) {
        this.filteredArray[i].key = e;
        this.filteredArray[i].keyDisplayValue = e;
        this.filteredArray[i].value = undefined;

        if (!this.doLocalFilter) {
            const key = this.filterTypeOptions.find((f) => f.optionName === e).optionValue;
            this.filteredArray[i].compareKey = key.toLowerCase().trim();
            this.filteredArray[i].filterkey = key.trim();
            this.filteredArray[i].filterValue = undefined;
        }

        this.selectedFilterType.emit(e);
    }

    removeFilter(i: number) {
        if (this.doLocalFilter) {
            this.filteredArray.splice(i, 1);
            this.filterAndSort();
        }
        const event = {
            index: i,
        };
        this.deleteFilters.emit(event);
    }

    removeFilterCategory(filterCategory: string) {
        const index = this.filteredArray.map(filter => filter.keyDisplayValue).indexOf(filterCategory);
        if (this.doLocalFilter) {
            this.filteredArray.splice(index, 1);
            this.filterAndSort();
        }
        this.deleteFilters.emit({
            index,
        });
    }

    removeAllFilters() {
        if (this.doLocalFilter) {
            this.filteredArray = [];
            this.filterAndSort();
        }
        const event = {
            clearAll: true,
        };
        this.deleteFilters.emit(event);
    }

    customFilter() {
        this.tableErrorMessage = '';
        this.dataSource.data = this.dataSource.data.filter((item) => {
            for (let i = 0; i < this.filteredArray.length; i++) {
                const filterObj = this.filteredArray[i];

                const filterKey = filterObj.keyDisplayValue;
                const filterValue = String(filterObj.filterValue);

                if (filterValue == 'undefined') {
                    continue;
                }

                if (filterKey && filterValue) {
                    const cellValue = item[filterKey].valueText;
                    if (
                        filterValue == '0%-25%' ||
                        filterValue == '26%-50%' ||
                        filterValue == '51%-75%' ||
                        filterValue == '76%-100%'
                    ) {
                        const cv = cellValue.substring(0, cellValue.length - 1);
                        const cv_f = parseFloat(cv);
                        if (isNaN(cv_f)) return false;
                        if (filterValue == '0%-25%' && !(cv_f >= 0 && cv_f <= 25)) return false;
                        if (filterValue == '26%-50%' && !(cv_f >= 26 && cv_f <= 50)) return false;
                        if (filterValue == '51%-75%' && !(cv_f >= 51 && cv_f <= 75)) return false;
                        if (filterValue == '76%-100%' && !(cv_f >= 76 && cv_f <= 100)) return false;
                    } else if(filterValue.includes('-')){
                        const [min, max] = filterValue.split('-');
                        return (cellValue >=min && cellValue<=max);
                    } else if(this.filterFunctionMap && this.filterFunctionMap[filterKey]){
                        if(!this.filterFunctionMap[filterKey](item, filterKey, filterValue)){
                            return false;
                        }
                    } else if (!(String(cellValue).toLowerCase() == filterValue.toLowerCase())) {
                        return false;
                    }
                }
            }
            return true;
        });

        this.totalRecordsAfterFilter = this.dataSource.data.length;
        if (this.dataSource.data.length == 0) {
            this.tableErrorMessage = 'noDataAvailable';
        }
    }

    customSearch() {
        const searchTxt = this.searchQuery;
        // whenever search or filter is called, we perform search first and then filter and thus we take maindatasource here for search
        this.dataSource.data = this.mainDataSource.data.filter((item) => {
            const columnsToSearchIN = Object.keys(item);
            for (const i in columnsToSearchIN) {
                const col = columnsToSearchIN[i];
                if (String(item[col].valueText).toLowerCase().match(searchTxt)) {
                    return true;
                }
            }
            return false;
        });
        if (this.dataSource.data.length == 0) {
            this.tableErrorMessage = 'noDataAvailable';
        }
        this.totalRecordsAfterFilter = this.dataSource.data.length;

        if (this.doLocalFilter) {
            this.customFilter();
        }
    }

    handleSearch(event: KeyboardEvent) {
        const searchTxt = (event.target as HTMLInputElement).value.toLowerCase();
        this.searchQuery = searchTxt;

        if (event.key === 'Enter' || searchTxt == '') {
            this.tableErrorMessage = '';
            if (this.doLocalSearch) {
                this.filterAndSort();
            }
            this.searchCalledEventEmitter.emit(searchTxt);
        }
    }

    clearSearchTextAndFilters() {
        this.searchQuery = '';
        if (this.tableErrorMessage === 'noDataAvailable') {
            this.tableErrorMessage = '';
        }
        this.removeAllFilters();
        if (!this.doLocalSearch) {
            this.searchCalledEventEmitter.emit(this.searchQuery);
        }
    }

    announceSortChange(sort: Sort) {
        if (this.doNotSort) {
            return;
        }
        this.headerColName = sort.active;
        this.direction = sort.direction;
        this.headerColNameSelected.emit({
            headerColName: this.headerColName,
            direction: this.direction,
        });
    }

    customSort(columnName: string, direction: SortDirection | '') {
        if (!columnName || direction === '' || this.doNotSort) {
            return;
        }
        const isAsc = this.direction === 'asc';

        this.dataSource.data = this.dataSource.data.sort((a, b) => {
            if (this.columnsSortFunctionMap && this.columnsSortFunctionMap[this.headerColName]) {
                return this.columnsSortFunctionMap[this.headerColName](a, b, isAsc);
            }else if(this.sortByColumn[this.headerColName.toLowerCase()]){
                return this.sortByColumn[this.headerColName.toLowerCase()](a, b, isAsc);
            }

            const elementA = a[this.headerColName];
            const elementB = b[this.headerColName];

            if (!isNaN(parseFloat(elementA?.valueText)) && !isNaN(parseFloat(elementB?.valueText))) {
                if (
                    typeof elementA.valueText == 'number' ||
                    typeof elementB.valueText == 'number'
                ) {
                    return (elementA.valueText - elementB.valueText) * (isAsc ? 1 : -1);
                }

                return (
                    (parseFloat(elementA.valueText) - parseFloat(elementB.valueText)) *
                    (isAsc ? 1 : -1)
                );
            }

            const elementAValue =
                elementA && elementA.valueText
                    ? elementA.valueText.toString().toLowerCase().trim()
                    : isAsc
                    ? 'zzzzzzzzzzzzzzzz'
                    : '0000000000000000';
            const elementBValue =
                elementB && elementB.valueText
                    ? elementB.valueText.toString().toLowerCase().trim()
                    : isAsc
                    ? 'zzzzzzzzzzzzzzzz'
                    : '0000000000000000';

            return (elementAValue < elementBValue ? -1 : 1) * (isAsc ? 1 : -1);
        });
    }

    onScroll(event: Event) {
        // visible height + pixel scrolled >= total height
        const target = event.target as HTMLDivElement;
        if (target.offsetHeight + target.scrollTop >= target.scrollHeight - 10) {
            if (this.data.length < this.totalRows && !this.isDataLoading && this.data.length > 0) {
                this.tableScrollTop = target.scrollTop;
                this.nextPageCalled.emit(this.tableScrollTop);
                this.isDataLoading = true;
            }
        }
    }

    handleCheckboxChange(row, col, e){
        row[col].valueText=e.checked?'checked':'unchecked';
        const event = {
            rowSelected: row,
            col
        };
        this.rowSelectEventEmitter.emit(event);
    }

    download() {
        this.downloadClicked.emit({
            searchTxt: this.searchQuery,
            filters: this.filteredArray,
        });
    }
}
