import { CdkVirtualScrollViewport } from '@angular/cdk/scrolling';
import {
    AfterViewInit,
    Component,
    ElementRef,
    EventEmitter,
    HostListener,
    Input,
    NgZone,
    OnChanges,
    OnDestroy,
    OnInit,
    Output,
    SimpleChanges,
    ViewChild,
} from '@angular/core';
import { FormControl } from '@angular/forms';
import { MatSelect } from '@angular/material/select';
import { Sort, SortDirection } from '@angular/material/sort';
import { Subject } from 'rxjs';
import { debounceTime, filter, map, pairwise, takeUntil, throttleTime } from 'rxjs/operators';
import { WindowExpansionService } from 'src/app/core/services/window-expansion.service';
import { OptionChange } from '../table-filters/table-filters.component';
import { TableDataSource } from './table-data-source';

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
    @Input() idColumn;
    @Input() selectedRowId;
    @Input() columnsAndFiltersToExcludeFromCasing = [];
    @Input() centeredColumns: { [key: string]: boolean } = {};
    @Input() columnsSortFunctionMap;
    @Input() filterFunctionMap;
    @Input() dateCategoryList;
    @Input() set columnWidths(value: { [key: string]: number }) {
        this._columnWidths = value;
        this.updateDenominator();
    }
    get columnWidths() {
        return this._columnWidths;
    }
    @Input() data = [];
    @Input() direction: SortDirection;
    @Input() areAllFiltersEnabled = false;
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
    @Input() hasMoreData: boolean = false;
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
    @Output() filterSearchTextChange = new EventEmitter();

    @ViewChild('select') select: MatSelect;
    @ViewChild('customTable', { static: true }) customTable: ElementRef<HTMLDivElement>;

    mainDataSource: TableDataSource;
    dataSource: TableDataSource;

    displayedColumns: string[];
    searchInColumns = new FormControl();

    totalRecordsAfterFilter = 0;

    denominator = 1;

    isWindowExpanded = true;
    isDataLoading = false;
    selectedFiltersList: string[] = [];
    destroy$ = new Subject<void>();
    @ViewChild(CdkVirtualScrollViewport, { static: true })
    private viewport: CdkVirtualScrollViewport;

    rowHeight = 36;
    offset: number;
    tooltipMessageMap = {}
    private whitelistColumnsChange = new Subject<any>();
    private readonly initialScrollPosition = 2;

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

    constructor(
        private ngZone: NgZone,
        ) {
        this.dataSource = new TableDataSource(this.ngZone);
        this.mainDataSource = new TableDataSource(this.ngZone);
    }

    scrollTableToPos (scrollPos) {
        this.viewport.scrollToOffset(scrollPos);
    }

    ngOnInit(): void {
        this.whitelistColumnsChange
        .pipe(takeUntil(this.destroy$), debounceTime(300))
        .subscribe((selectedColumns) => {
                this.whiteListColumns = Object.keys(this.columnWidths).filter((c) =>
                selectedColumns.includes(c),
            );
            this.whiteListColumnsChanged();
        });
        this.dataSource.attach(this.viewport);

      this.viewport.renderedRangeStream.pipe(takeUntil(this.destroy$)).subscribe(range => {
        this.offset = range.start * -this.rowHeight;
      });


        if (this.onScrollDataLoader) {
            this.onScrollDataLoader.pipe(takeUntil(this.destroy$)).subscribe((data) => {
                this.isDataLoading = false;
                if (data && data.length > 0) {
                    this.data.push(...data);
                    this.mainDataSource.matTableDataSource.data = this.data.slice();
                    this.dataSource.matTableDataSource.data = this.data.slice();
                }
            });
        }

    }

    ngOnChanges(changes: SimpleChanges): void {    
        if (this.customTable) {
            if (!this.tableDataLoaded) {
                this.tableScrollTop = 0;
                this.customTable.nativeElement.scrollTop = 0;
                this.mainDataSource.matTableDataSource.data = [];
                this.dataSource.matTableDataSource.data = [];
            }
        }
        if (changes.columnWidths) {
            if (this.columnWidths) {
                this.displayedColumns = Object.keys(this.columnWidths);
            }
            this.displayedColumns.sort();
        }
        // data property changes only when pageNumber also becomes 0. i.e., when sorting or data loading initially or filtering or landing L1->L0
        // when landing from L1, we will recieve tableScrollTop property with some value which is being used in afterViewInit cycle.
        if (changes.data) {
            this.mainDataSource.matTableDataSource.data = this.data;
            this.dataSource.matTableDataSource.data = this.data;
            // if tableScrollTop value is 0, it means that it is initial loading of data (bucketNumber or pageNumber = 0)
            if (!this.tableScrollTop) {
                // initialCallFlag is used to slice data for virtual scroll for screens where data loaded from API is greater than 100.
                this.dataSource.intialCallFlag = true;
                this.scrollTableToPos(this.initialScrollPosition);
            }
        }
        if(changes.tableScrollTop && this.tableScrollTop){
            this.dataSource.intialCallFlag = false;
        }
        if(changes.totalRows){
            this.totalRecordsAfterFilter = this.totalRows;
        }
        // below lines will cause UI driven (filter or sort) tables to render all the records when new column is added/removed.
        if ((this.doLocalSearch || this.doLocalSort) && this.tableDataLoaded) {
            // below lines should fix the above described issue
            if (!this.tableScrollTop) {
                this.dataSource.intialCallFlag = true;
            }
            this.filterAndSort();
        }

        if(changes.whiteListColumns){
            this.updateDenominator();          
        }
    }

    ngAfterViewInit(): void {
        if (this.tableScrollTop) {
            this.scrollTableToPos(this.tableScrollTop);        
        }

        this.viewport.elementScrolled().pipe(
            map(() => this.viewport.measureScrollOffset("bottom")),
            pairwise(),
            filter(([y1, y2]) => (y2<y1) && (y2<1000)),
            throttleTime(200),
            takeUntil(this.destroy$)
          ).subscribe(() => {
            this.selectedRowIndex = -1;                        
            if((this.data.length < this.totalRows || this.hasMoreData) && !this.isDataLoading && this.tableDataLoaded){
                this.isDataLoading = true;
                this.nextPageCalled.emit(this.offset);
            }
          })
    }

    ngOnDestroy(): void {
        this.destroy$.next();
        this.destroy$.complete();
    }

    displayToolTip(column:string){
        return this.tooltipMessageMap[column] ?? column;
    }

    updateDenominator(){
        this.denominator = this.whiteListColumns.reduce((acc,next) => acc+=this.columnWidths[next], 0); 
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
        const rowsToAddToOffset = 5;
        const heightToAddToOffset = (i>rowsToAddToOffset?i-rowsToAddToOffset:i) * this.rowHeight;
        const event = {
            tableScrollTop: -this.offset + heightToAddToOffset,
            rowSelected: row,
            data: this.data,
            col,
            filters: this.filteredArray,
            searchTxt: this.searchQuery,
            selectedRowIndex: i,
            selectedRowId: row[this.idColumn]?.valueText
        };
        this.ngZone.run(() => {
            this.rowSelectEventEmitter.emit(event);
        })
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
        this.whitelistColumnsChange.next(selectedColumns);
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
        this.dataSource.matTableDataSource.data = this.dataSource.matTableDataSource.data.filter((item) => {
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
                        if(cellValue<min || cellValue>max) return false;
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

        this.totalRecordsAfterFilter = this.dataSource.matTableDataSource.data.length;
        if (this.dataSource.matTableDataSource.data.length == 0) {
            this.tableErrorMessage = 'noDataAvailable';
        }
    }

    customSearch() {
        const searchTxt = this.searchQuery;
        // whenever search or filter is called, we perform search first and then filter and thus we take maindatasource here for search
        this.dataSource.matTableDataSource.data = this.mainDataSource.matTableDataSource.data.filter((item) => {
            const columnsToSearchIN = Object.keys(item);
            for (const i in columnsToSearchIN) {
                const col = columnsToSearchIN[i];
                if (String(item[col].valueText).toLowerCase().match(searchTxt)) {
                    return true;
                }
            }
            return false;
        });
        if (this.dataSource.matTableDataSource.data.length == 0) {
            this.tableErrorMessage = 'noDataAvailable';
        }
        this.totalRecordsAfterFilter = this.dataSource.matTableDataSource.data.length;

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
        if (this.doNotSort || sort.active=='Actions') {
            return;
        }
        this.scrollTableToPos(this.initialScrollPosition); // scroll to top when sort is applied
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

        this.dataSource.matTableDataSource.data = this.dataSource.matTableDataSource.data.sort((a, b) => {
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

    trackColumnByIndex(index: number){
        return index;
    }
}
