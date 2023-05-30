import {
    AfterViewInit,
    Component,
    ElementRef,
    EventEmitter,
    HostListener,
    Input,
    OnChanges,
    OnInit,
    Output,
    SimpleChanges,
    ViewChild,
    ViewChildren,
} from '@angular/core';
import { FormControl } from '@angular/forms';
import { MatOption } from '@angular/material/core';
import { MatSelect } from '@angular/material/select';
import { MatTableDataSource } from '@angular/material/table';
import { find, findIndex } from 'lodash';
import { Subject } from 'rxjs';
import { WindowExpansionService } from 'src/app/core/services/window-expansion.service';
import { OptionChange } from '../table-filters/table-filters.component';

export interface FilterItem {
    filterValue?: string | undefined;
    key?: string;
    keyDisplayValue: string;
    value?: string | undefined;
    compareKey?: string;
    filterkey?: string;
}

@Component({
    selector: 'app-table',
    templateUrl: './table.component.html',
    styleUrls: ['./table.component.css'],
})
export class TableComponent implements OnInit, AfterViewInit, OnChanges {
    @Input() centeredColumns: { [key: string]: boolean } = {};
    @Input() columnsSortFunctionMap;
    @Input() columnWidths: { [key: string]: number };
    @Input() data = [];
    @Input() direction: 'desc' | 'asc';
    @Input() doLocalFilter = false;
    @Input() doLocalSearch = false;
    @Input() doLocalSort = true;
    @Input() doNotSort = false;
    @Input() filterTagLabels: { [key: string]: string[] };
    @Input() filterTypeLabels = [];
    @Input() gapBetweenFilterAndTable;
    @Input() headerColName;
    @Input() imageDataMap = {};
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
    @Input() whiteListColumns = [];
    @Input() filteredArray: FilterItem[] = [];
    @Input() filterTypeOptions: {
        optionName: string;
        optionURL: string;
        optionValue: string;
    }[];

    @Output() actionSelected = new EventEmitter();
    @Output() deleteFilters = new EventEmitter<{
        index?: number;
        clearAll?: boolean;
    }>();
    @Output() downloadClicked = new EventEmitter<any>();
    @Output() headerColNameSelected = new EventEmitter<any>();
    @Output() nextPageCalled = new EventEmitter<any>();
    @Output() rowSelectEventEmitter = new EventEmitter<any>();
    @Output() searchCalledEventEmitter = new EventEmitter<string>();
    @Output() searchEventEmitter = new EventEmitter<string>();
    @Output() searchInColumnsChanged = new EventEmitter<any>();
    @Output() selectedFilter = new EventEmitter<{
        index: number;
        filterValue: string;
        filterKeyDisplayValue: string;
    }>();
    @Output() selectedFilterType = new EventEmitter<any>();
    @Output() whitelistColumnsChanged = new EventEmitter<any>();

    @ViewChild('select') select: MatSelect;
    @ViewChild('tableContainer') tableContainer: ElementRef;
    @ViewChildren('customTable') customTable: any;

    mainDataSource;
    dataSource;

    displayedColumns: string[];
    searchInColumns = new FormControl();

    allSelected = true;
    screenWidth: number;
    denominator: number;
    screenWidthFactor: number;
    isWindowExpanded = true;
    isDataLoading = false;
    selectedFiltersList: string[] = [];

    constructor(private windowExpansionService: WindowExpansionService) {
        this.windowExpansionService.getExpansionStatus().subscribe(() => {
            this.waitAndResizeTable();
        });
    }

    ngOnInit(): void {
        if (this.onScrollDataLoader) {
            this.onScrollDataLoader.subscribe((data) => {
                this.isDataLoading = false;
                if (data && data.length > 0) {
                    this.data.push(...data);
                    this.mainDataSource = new MatTableDataSource(this.data);
                    this.dataSource = new MatTableDataSource(this.data);
                }
            });
        }
    }

    ngOnChanges(changes: SimpleChanges): void {
        if (this.customTable) {
            if (changes.tableScrollTop && changes.tableScrollTop.currentValue != undefined) {
                this.customTable.first.nativeElement.scrollTop = this.tableScrollTop;
            }
            if (!this.tableDataLoaded) {
                this.tableScrollTop = 0;
                // this.data = []; in a race condition, this may empty data after data is loaded
                this.customTable.first.nativeElement.scrollTop = 0;
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

                this.waitAndResizeTable();
            }

            this.selectedFiltersList = this.filteredArray.map((item) => item.keyDisplayValue);
        }
        if ((this.doLocalSearch || this.doLocalSort) && this.tableDataLoaded) {
            this.filterAndSort();
        }
    }

    ngAfterViewInit(): void {
        this.customTable.first.nativeElement.scrollTop = this.tableScrollTop;
        this.waitAndResizeTable();
    }

    filterAndSort() {
        if (this.doLocalSearch) {
            this.customSearch();
        }
        if (this.headerColName && this.direction && this.doLocalSort) {
            this.customSort(this.headerColName, this.direction);
        }
    }

    waitAndResizeTable() {
        setTimeout(() => {
            this.screenWidth = parseInt(
                window
                    .getComputedStyle(this.tableContainer.nativeElement, null)
                    .getPropertyValue('width'),
                10,
            );
            this.getWidthFactor();
        }, 1000);
    }

    openColumnSelectorModal() {
        this.select.open();
    }

    getWidthFactor() {
        this.denominator = 0;
        for (const i in this.whiteListColumns) {
            const col = this.whiteListColumns[i];
            this.denominator += this.columnWidths[col];
        }
        this.getScreenWidthFactor();
    }

    getScreenWidthFactor() {
        this.screenWidthFactor = (this.screenWidth - 30) / this.denominator;
    }

    @HostListener('window:resize', ['$event'])
    onWindowResize() {
        this.waitAndResizeTable();
    }

    handleSearchInColumnsChange() {
        this.searchInColumnsChanged.emit(this.searchInColumns.value);
    }

    whiteListColumnsChanged() {
        this.whitelistColumnsChanged.emit(this.whiteListColumns);
    }

    handleClick(row, col, i) {
        if (row[col].isMenuBtn) {
            return;
        }
        const event = {
            tableScrollTop: this.customTable.first.nativeElement.scrollTop,
            rowSelected: row,
            data: this.data,
            col: col,
            filters: this.filteredArray,
            searchTxt: this.searchQuery,
            selectedRowIndex: i,
        };
        this.rowSelectEventEmitter.emit(event);
    }

    handleAction(element, action, i: number) {
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
        this.getWidthFactor();
        this.waitAndResizeTable();
        this.whiteListColumnsChanged();
    }

    handleFilterDropdownOpen() {
        this.selectedFiltersList = [];
        this.filteredArray.forEach((filter) => {
            this.selectedFiltersList.push(filter.keyDisplayValue);
        });
    }

    selectFilterCategory(category: string) {
        const filteredArrayKeys = this.filteredArray.map((item) => item.keyDisplayValue);

        if (!filteredArrayKeys.includes(category)) {
            // add to filteredArray
            this.selectedFiltersList.push(category);
            this.filteredArray.push({
                keyDisplayValue: category,
                filterValue: undefined,
            });
            this.onSelectFilterType(category, this.filteredArray.length - 1);
        }
    }

    removeOnlyFilterValue(index) {
        this.filteredArray[index].value = undefined;
        this.filteredArray[index].filterValue = undefined;
        let event = {
            index,
            removeOnlyFilterValue: true,
        };
        if (this.doLocalFilter) {
            this.filterAndSort();
        }
        this.deleteFilters.emit(event);
    }

    selectFilterCategoryOption(event: OptionChange) {
        let index = this.filteredArray.findIndex((i) => i.key === event.category);

        if (event.value == false) {
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
                const key = find(this.filterTypeOptions, {
                    optionName: event.category,
                }).optionValue;

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
            this.filteredArray[index].filterValue = this.filteredArray[index].value = event.option;
        }

        if (this.doLocalFilter) {
            this.filterAndSort();
        }

        this.selectedFilter.emit({
            index,
            filterKeyDisplayValue: event.category,
            filterValue: event.option,
        });
    }

    onSelectFilter(e: string | undefined, i: number) {
        if (!e) {
            this.selectedFiltersList.splice(i, 1);
            this.removeFilter(i);
            return;
        }

        const filterIndex = findIndex(this.filteredArray, (el, j) => {
            return el['keyDisplayValue'] === this.filteredArray[i].keyDisplayValue && i != j;
        });
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

    onSelectFilterType(e, i) {
        this.filteredArray[i].key = e;
        this.filteredArray[i].keyDisplayValue = e;
        this.filteredArray[i].value = undefined;

        if (!this.doLocalFilter) {
            const key = find(this.filterTypeOptions, {
                optionName: e,
            })['optionValue'];
            this.filteredArray[i].compareKey = key.toLowerCase().trim();
            this.filteredArray[i].filterkey = key.trim();
            this.filteredArray[i].filterValue = undefined;
        }

        this.selectedFilterType.emit(e);
    }

    removeFilter(i: number) {
        this.selectedFiltersList.splice(i, 1);

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
        const index = this.selectedFiltersList.indexOf(filterCategory);
        this.selectedFiltersList.splice(index, 1);
        if (this.doLocalFilter) {
            this.filteredArray.splice(index, 1);
            this.filterAndSort();
        }
        this.deleteFilters.emit({
            index,
        });
    }

    removeAllFilters() {
        this.selectedFiltersList = [];
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
                    } else if (!(String(cellValue).toLowerCase() == filterValue.toLowerCase())) {
                        return false;
                    }
                }
            }
            return true;
        });

        this.totalRows = this.dataSource.data.length;
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
        this.totalRows = this.dataSource.data.length;

        if (this.doLocalFilter) {
            this.customFilter();
        }
    }

    handleSearch(event) {
        const searchTxt = event.target.value.toLowerCase();
        this.searchQuery = searchTxt;

        if (event.keyCode === 13 || searchTxt == '') {
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

    announceSortChange(sort: any) {
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

    customSort(columnName, direction) {
        if (!columnName || direction === '' || this.doNotSort) {
            return;
        }
        const isAsc = this.direction == 'asc';

        this.dataSource.data = this.dataSource.data.sort((a, b) => {
            if (this.columnsSortFunctionMap && this.columnsSortFunctionMap[this.headerColName]) {
                return this.columnsSortFunctionMap[this.headerColName](a, b, isAsc);
            }

            const elementA = a[this.headerColName];
            const elementB = b[this.headerColName];

            if (!isNaN(parseFloat(elementA.valueText)) || !isNaN(parseFloat(elementB.valueText))) {
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
                    ? elementA.valueText.toLowerCase()
                    : isAsc
                    ? 'zzzzzz'
                    : '000000';
            const elementBValue =
                elementB && elementB.valueText
                    ? elementB.valueText.toLowerCase()
                    : isAsc
                    ? 'zzzzzz'
                    : '000000';

            return (elementAValue < elementBValue ? -1 : 1) * (isAsc ? 1 : -1);
        });
    }

    onScroll(event: any) {
        // visible height + pixel scrolled >= total height
        if (event.target.offsetHeight + event.target.scrollTop >= event.target.scrollHeight - 10) {
            if (this.data.length < this.totalRows && !this.isDataLoading && this.data.length > 0) {
                this.tableScrollTop = event.target.scrollTop;
                this.nextPageCalled.emit(this.tableScrollTop);
                this.isDataLoading = true;
            }
        }
    }

    download() {
        const event = {
            searchTxt: this.searchQuery,
            filters: this.filteredArray,
        };
        this.downloadClicked.emit(event);
    }
}
