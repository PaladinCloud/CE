/*
 *Copyright 2018 T Mobile, Inc. or its affiliates. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); You may not use
 * this file except in compliance with the License. A copy of the License is located at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * or in the "license" file accompanying this file. This file is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, express or
 * implied. See the License for the specific language governing permissions and
 * limitations under the License.
 */

import { DatePipe } from '@angular/common';
import { Component, OnDestroy, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import * as _ from 'lodash';
import { Subject, Subscription } from 'rxjs';
import { AssetGroupObservableService } from 'src/app/core/services/asset-group-observable.service';
import { DomainTypeObservableService } from 'src/app/core/services/domain-type-observable.service';
import { TableStateService } from 'src/app/core/services/table-state.service';
import { WorkflowService } from 'src/app/core/services/workflow.service';
import { IssueFilterService } from 'src/app/pacman-features/services/issue-filter.service';
import { CommonResponseService } from 'src/app/shared/services/common-response.service';
import { DownloadService } from 'src/app/shared/services/download.service';
import { ErrorHandlingService } from 'src/app/shared/services/error-handling.service';
import { LoggerService } from 'src/app/shared/services/logger.service';
import { RouterUtilityService } from 'src/app/shared/services/router-utility.service';
import { UtilsService } from 'src/app/shared/services/utils.service';
import { environment } from 'src/environments/environment';

@Component({
    selector: 'app-cloud-notifications',
    templateUrl: './cloud-notifications.component.html',
    styleUrls: ['./cloud-notifications.component.css'],
    providers: [LoggerService, IssueFilterService, DatePipe],
})
export class CloudNotificationsComponent implements OnInit, OnDestroy {
    assetGroupSubscription: Subscription;
    dataSubscription: Subscription;
    summarySubscription: Subscription;
    filterSubscription: Subscription;
    domainSubscription: Subscription;

    pageTitle = 'Notifications';
    popRows = ['Download Data'];
    tabSelected = 'asset';
    backButtonRequired;
    pageLevel = 0;
    selectedAssetGroup;
    selectedDomain;
    currentPageLevel = 0;

    paginatorSize = 100;
    currentBucket: any = [];
    firstPaginator = 1;
    lastPaginator: number;
    currentPointer = 0;
    prevFilter = {};
    outerArr = [];
    allColumns = [];
    errorValue = 0;
    summaryValue = 0;
    errorMessage = '';
    filter = {
        eventtypecategory: '',
    };

    filterTypeOptions: any = [];
    filterTagOptions: any = {};
    currentFilterType;
    filterTypeLabels = [];
    filterTagLabels = {};
    filters: any = [];
    filterText: any;

    searchTxt = '';
    tableErrorMessage = '';
    onScrollDataLoader: Subject<any> = new Subject<any>();
    headerColName: string;
    direction: string;
    bucketNumber = 0;
    totalRows = 0;
    tableDataLoaded = false;
    tableData: any = [];
    displayedColumns: string[] = [];
    whiteListColumns: any = [];
    selectedRowIndex;
    tableScrollTop: any;
    isTableStatePreserved = false;

    columnNamesMap = {
        eventName: 'Event',
        eventCategoryName: 'Type',
        eventSourceName: 'Source',
        startTime: 'Created',
    };

    columnWidths = {
        Event: 2,
        Type: 1,
        Source: 1,
        Created: 1,
    };

    centeredColumns = {
        Event: false,
        Type: true,
        Source: true,
        Created: true,
    };

    FullQueryParams: any;
    queryParamsWithoutFilter: any;

    constructor(
        private activatedRoute: ActivatedRoute,
        private assetGroupObservableService: AssetGroupObservableService,
        private commonResponseService: CommonResponseService,
        private datePipe: DatePipe,
        private domainObservableService: DomainTypeObservableService,
        private downloadService: DownloadService,
        private errorHandler: ErrorHandlingService,
        private filterService: IssueFilterService,
        private logger: LoggerService,
        private router: Router,
        private routerUtilityService: RouterUtilityService,
        private tableStateService: TableStateService,
        private utils: UtilsService,
        private workflowService: WorkflowService,
    ) {
        this.currentPageLevel = this.routerUtilityService.getpageLevel(
            this.router.routerState.snapshot.root,
        );
        this.backButtonRequired = this.workflowService.checkIfFlowExistsCurrently(this.pageLevel);
    }

    ngOnInit() {
        const state = this.tableStateService.getState(this.pageTitle) || {};
        if (state) {
            this.headerColName = state.headerColName || '';
            this.direction = state.direction || '';
            this.bucketNumber = state.bucketNumber || 0;
            this.totalRows = state.totalRows || 0;
            this.searchTxt = state?.searchTxt || '';
            this.selectedRowIndex = state?.selectedRowIndex;

            this.tableDataLoaded = true;

            this.tableData = state?.data || [];
            this.displayedColumns = Object.keys(this.columnWidths);
            this.whiteListColumns = state?.whiteListColumns || this.displayedColumns;
            this.tableScrollTop = state?.tableScrollTop;

            if (this.tableData && this.tableData.length > 0) {
                this.isTableStatePreserved = true;
            } else {
                this.isTableStatePreserved = false;
            }

            this.assetGroupSubscription = this.assetGroupObservableService
                .getAssetGroup()
                .subscribe((assetGroupName) => {
                    this.selectedAssetGroup = assetGroupName;
                });

            this.domainSubscription = this.domainObservableService
                .getDomainType()
                .subscribe((domain) => {
                    this.selectedDomain = domain;
                });
            this.getFilters();
        }
    }

    /*
     * This function gets the urlparameter and queryObj
     *based on that different apis are being hit with different queryparams
     */
    routerParam() {
        try {
            const currentQueryParams = this.routerUtilityService.getQueryParametersFromSnapshot(
                this.router.routerState.snapshot.root,
            );
            if (currentQueryParams) {
                this.FullQueryParams = currentQueryParams;
                this.queryParamsWithoutFilter = JSON.parse(JSON.stringify(this.FullQueryParams));
                delete this.queryParamsWithoutFilter['filter'];
                this.filterText = this.utils.processFilterObj(this.FullQueryParams);
            }
        } catch (error) {
            this.errorMessage = this.errorHandler.handleJavascriptError(error);
            this.logger.log('error', error);
        }
    }

    getUpdatedUrl() {
        let updatedQueryParams = {};
        this.filterText = this.utils.arrayToObject(this.filters, 'filterkey', 'value'); // <-- TO update the queryparam which is passed in the filter of the api
        this.filterText = this.utils.makeFilterObj(this.filterText);

        /**
         * To change the url
         * with the deleted filter value along with the other existing paramter(ex-->tv:true)
         */
        updatedQueryParams = {
            filter: this.filterText.filter,
        };

        /**
         * Finally after changing URL Link
         * api is again called with the updated filter
         */
        this.filterText = this.utils.processFilterObj(this.filterText);

        this.router.navigate([], {
            relativeTo: this.activatedRoute,
            queryParams: updatedQueryParams,
            queryParamsHandling: 'merge',
        });
    }

    deleteFilters(event?) {
        try {
            if (!event) {
                this.filters = [];
                this.storeState();
            } else if(event.removeOnlyFilterValue) {
                this.getUpdatedUrl();
                this.updateComponent();
                this.storeState();
            } else if (event.index && !this.filters[event.index].filterValue) {
                this.filters.splice(event.index, 1);
                this.storeState();
            } else {
                if (event.clearAll) {
                    this.filters = [];
                } else {
                    this.filters.splice(event.index, 1);
                }
                this.storeState();
                this.getUpdatedUrl();
                this.updateComponent();
            }
        } catch (error) {
            this.errorMessage = this.errorHandler.handleJavascriptError(error);
            this.logger.log('error', error);
        }
        /* TODO: Aditya: Why are we not calling any updateCompliance function in observable to update the filters */
    }

    /*
     * this function passes query params to filter component to show filter
     */
    getFilterArray() {
        try {
            const filterObjKeys = Object.keys(this.filterText);
            const dataArray = [];
            for (let i = 0; i < filterObjKeys.length; i++) {
                let obj = {};
                const keyDisplayValue = _.find(this.filterTypeOptions, {
                    optionValue: filterObjKeys[i],
                })['optionName'];
                obj = {
                    keyDisplayValue,
                    filterkey: filterObjKeys[i],
                };
                dataArray.push(obj);
            }

            const state = this.tableStateService.getState(this.pageTitle) ?? {};
            const filters = state?.filters;

            if (filters) {
                const dataArrayFilterKeys = dataArray.map((obj) => obj.keyDisplayValue);
                filters.forEach((filter) => {
                    if (!dataArrayFilterKeys.includes(filter.keyDisplayValue)) {
                        dataArray.push({
                            filterkey: filter.filterkey,
                            keyDisplayValue: filter.key,
                        });
                    }
                });
            }

            const formattedFilters = dataArray;
            for (let i = 0; i < formattedFilters.length; i++) {
                let keyDisplayValue = formattedFilters[i].keyDisplayValue;
                if (!keyDisplayValue) {
                    keyDisplayValue = _.find(this.filterTypeOptions, {
                        optionValue: formattedFilters[i].filterKey,
                    })['optionName'];
                }

                this.changeFilterType(keyDisplayValue).then(() => {
                    const filterValueObj = _.find(this.filterTagOptions[keyDisplayValue], {
                        id: this.filterText[formattedFilters[i].filterkey],
                    });

                    const filterKey = dataArray[i].filterkey;

                    if (!this.filters.find((filter) => filter.keyDisplayValue == keyDisplayValue)) {
                        const eachObj = {
                            keyDisplayValue: keyDisplayValue,
                            filterValue: filterValueObj ? filterValueObj['name'] : undefined,
                            key: keyDisplayValue, // <-- displayKey-- Resource Type
                            value: this.filterText[filterKey], // <<-- value to be shown in the filter UI-- S2
                            filterkey: filterKey?.trim(), // <<-- filter key that to be passed -- "resourceType "
                            compareKey: filterKey?.toLowerCase().trim(), // <<-- key to compare whether a key is already present -- "resourcetype"
                        };
                        this.filters.push(eachObj);
                        this.filters = [...this.filters];
                        this.storeState();
                    }
                });
            }
        } catch (error) {
            this.logger.log('error', error);
        }
    }

    /**
     * This function get calls the keyword service before initializing
     * the filter array ,so that filter keynames are changed
     */
    getFilters() {
        try {
            this.filterSubscription = this.filterService
                .getFilters(
                    { filterId: 10, domain: this.selectedDomain },
                    environment.issueFilter.url,
                    environment.issueFilter.method,
                )
                .subscribe((response) => {
                    this.filterTypeLabels = _.map(response[0].response, 'optionName');
                    this.filterTypeOptions = response[0].response;
                    this.filterTypeLabels.sort();

                    this.routerParam();
                    // this.deleteFilters();
                    this.getFilterArray();
                    this.updateComponent();
                });
        } catch (error) {
            this.errorMessage = this.errorHandler.handleJavascriptError(error);
            this.logger.log('error', error);
        }
    }

    changeFilterType(value) {
        return new Promise((resolve) => {
            try {
                this.currentFilterType = _.find(this.filterTypeOptions, {
                    optionName: value,
                });
                const urlObj = this.utils.getParamsFromUrlSnippet(this.currentFilterType.optionURL);
                const queryParams = {
                    ...urlObj.params,
                    ag: this.selectedAssetGroup,
                    domain: this.selectedDomain,
                };

                if (!this.filterTagOptions[value] || !this.filterTagLabels[value]) {
                    this.filterSubscription = this.filterService
                        .getFilters(queryParams, environment.base + urlObj.url, 'GET')
                        .subscribe((response) => {
                            this.filterTagOptions[value] = response[0].response;

                            this.filterTagLabels = {
                                ...this.filterTagLabels,
                                ...{
                                    [value]: _.map(response[0].response, 'name').sort((a, b) =>
                                        a.localeCompare(b),
                                    ),
                                },
                            };

                            resolve(this.filterTagOptions[value]);
                            this.storeState();
                        });
                }
            } catch (error) {
                this.errorMessage = this.errorHandler.handleJavascriptError(error);
                this.logger.log('error', error);
            }
        });
    }

    changeFilterTags(event) {
        const value = event.filterValue;
        this.currentFilterType = _.find(this.filterTypeOptions, {
            optionName: event.filterKeyDisplayValue,
        });
        try {
            if (this.currentFilterType) {
                const filterTag = _.find(this.filterTagOptions[event.filterKeyDisplayValue], {
                    name: value,
                });
                this.utils.addOrReplaceElement(
                    this.filters,
                    {
                        keyDisplayValue: event.filterKeyDisplayValue,
                        filterValue: value,
                        key: this.currentFilterType.optionName,
                        value: filterTag['id'],
                        filterkey: this.currentFilterType.optionValue.trim(),
                        compareKey: this.currentFilterType.optionValue.toLowerCase().trim(),
                    },
                    (el) => {
                        return (
                            el.compareKey ===
                            this.currentFilterType.optionValue.toLowerCase().trim()
                        );
                    },
                );
            }
            this.storeState();
            this.getUpdatedUrl();
            this.updateComponent();
        } catch (error) {
            this.errorMessage = this.errorHandler.handleJavascriptError(error);
            this.logger.log('error', error);
        }
    }

    updateComponent() {
        if (this.isTableStatePreserved) {
            this.tableDataLoaded = true;
            this.clearState();
        } else {
            this.tableData = [];
            this.tableDataLoaded = false;
            this.bucketNumber = 0;
            this.getData();
        }
    }

    handleHeaderColNameSelection(event) {
        this.headerColName = event.headerColName;
        this.direction = event.direction;
        this.storeState();
    }

    handleWhitelistColumnsChange(event) {
        this.whiteListColumns = event;
        this.storeState();
    }

    storeState(data?) {
        const state = {
            totalRows: this.totalRows,
            data: data,
            headerColName: this.headerColName,
            direction: this.direction,
            whiteListColumns: this.whiteListColumns,
            bucketNumber: this.bucketNumber,
            searchTxt: this.searchTxt,
            tableScrollTop: this.tableScrollTop,
            filters: this.filters,
            filterText: this.filterText,
            selectedRowIndex: this.selectedRowIndex,
        };
        this.tableStateService.setState(this.pageTitle, state);
    }

    clearState() {
        // this.tableStateService.clearState(this.pageTitle);
        this.isTableStatePreserved = false;
    }

    navigateBack() {
        try {
            this.workflowService.goBackToLastOpenedPageAndUpdateLevel(
                this.router.routerState.snapshot.root,
            );
        } catch (error) {
            this.logger.log('error', error);
        }
    }

    getData(isNextPageCalled?) {
        if (this.dataSubscription) {
            this.dataSubscription.unsubscribe();
        }

        const payload = {
            ag: this.selectedAssetGroup,
            filter: this.filterText,
            from: this.bucketNumber * this.paginatorSize,
            searchtext: this.searchTxt,
            size: this.paginatorSize,
        };

        const queryParam = {
            global: this.tabSelected === 'general',
        };

        const TableUrl = environment.cloudNotifications.url;
        const TableMethod = environment.cloudNotifications.method;
        this.dataSubscription = this.commonResponseService
            .getData(TableUrl, TableMethod, payload, queryParam)
            .subscribe(
                (response) => {
                    if (!isNextPageCalled) {
                        this.tableData = [];
                    }

                    this.tableDataLoaded = true;
                    try {
                        if (response.data.response.length === 0) {
                            this.totalRows = 0;
                            this.tableErrorMessage = 'noDataAvailable';
                        } else {
                            this.tableErrorMessage = '';
                        }

                        this.totalRows = response.data.total;
                        if (response.data.response.length > 0) {
                            const updatedResponse = this.utils.massageTableData(
                                response.data.response,
                                this.columnNamesMap,
                            );
                            const processData = this.processData(updatedResponse);
                            if (isNextPageCalled) {
                                this.onScrollDataLoader.next(processData);
                            } else {
                                this.tableData = processData;
                            }
                        }
                    } catch (e) {
                        this.errorValue = -1;
                        this.logger.log('error', e);
                        this.tableErrorMessage = 'jsError';
                    }
                },
                (error) => {
                    this.errorValue = -1;
                    this.logger.log('error', error);
                    this.tableErrorMessage = 'apiResponseError';
                },
            );
    }

    processData(data) {
        try {
            let innerArr = {};
            const totalVariablesObj = {};
            let cellObj = {};
            let processedData = [];
            const getData = data;
            const keynames = Object.keys(getData[0]);

            let cellData;
            for (let row = 0; row < getData.length; row++) {
                innerArr = {};
                keynames.forEach((col) => {
                    cellData = getData[row][col];
                    cellObj = {
                        text: cellData, // text to be shown in table cell
                        titleText: cellData, // text to show on hover
                        valueText: cellData,
                        hasPostImage: false,
                        imgSrc: '', // if imageSrc is not empty and text is also not empty then this image comes before text otherwise if imageSrc is not empty and text is empty then only this image is rendered,
                        postImgSrc: '',
                        isChip: '',
                        isMenuBtn: false,
                        properties: '',
                        isLink: false,
                    };

                    if (col.toLowerCase() === 'event') {
                        cellObj = {
                            ...cellObj,
                            isLink: true,
                        };
                    } else if (col.toLowerCase() === 'created') {
                        const createdDate = this.datePipe.transform(cellData, 'MMM d, y, h:mm a');
                        cellObj = {
                            ...cellObj,
                            text: createdDate,
                            titleText: createdDate,
                        };
                    }

                    innerArr[col] = cellObj;
                    totalVariablesObj[col] = '';
                });

                processedData.push(innerArr);
            }
            if (processedData.length > getData.length) {
                const halfLength = processedData.length / 2;
                processedData = processedData.splice(halfLength);
            }

            return processedData;
        } catch (error) {
            this.tableErrorMessage = this.errorHandler.handleJavascriptError(error);
            this.logger.log('error', error);
        }
    }

    goToDetails(event) {
        const rowSelected = event.rowSelected;
        const data = event.data;
        this.selectedRowIndex = event.selectedRowIndex;
        this.tableScrollTop = event.tableScrollTop;

        this.storeState(data);
        try {
            const eventId = encodeURIComponent(rowSelected['eventId'].valueText);
            this.workflowService.addRouterSnapshotToLevel(
                this.router.routerState.snapshot.root,
                0,
                this.pageTitle,
            );
            this.router
                .navigate(['pl/notifications/notification-details'], {
                    queryParams: { eventId: eventId },
                    queryParamsHandling: 'merge',
                })
                .then((response) => {
                    this.logger.log('info', 'Successfully navigated to details page: ' + response);
                })
                .catch((error) => {
                    this.logger.log('error', 'Error in navigation - ' + error);
                });
        } catch (error) {
            this.logger.log('error', error);
        }
    }

    nextPg(e) {
        try {
            this.tableScrollTop = e;
            this.bucketNumber++;
            this.storeState();
            this.getData(true);
        } catch (error) {
            this.logger.log('error', error);
        }
    }

    handlePopClick(rowText) {
        const fileType = 'csv';
        try {
            const queryParams = {
                fileFormat: 'csv',
                serviceId: this.tabSelected === 'general' ? 18 : 17,
                fileType: fileType,
            };

            const downloadRequest = {
                ag: this.selectedAssetGroup,
                filter: this.filter,
                from: 0,
                searchtext: this.searchTxt,
                size: this.totalRows,
            };

            const downloadUrl = environment.download.url;
            const downloadMethod = environment.download.method;
            const downloadName = 'Event Logs';

            this.downloadService.requestForDownload(
                queryParams,
                downloadUrl,
                downloadMethod,
                downloadRequest,
                downloadName,
                this.totalRows,
            );
        } catch (error) {
            this.logger.log('error', error);
        }
    }

    callNewSearch(searchVal) {
        this.searchTxt = searchVal;
        this.isTableStatePreserved = false;
        this.storeState();
        this.updateComponent();
    }

    ngOnDestroy() {
        if (this.assetGroupSubscription) {
            this.assetGroupSubscription.unsubscribe();
        }
        if (this.dataSubscription) {
            this.dataSubscription.unsubscribe();
        }
        if (this.summarySubscription) {
            this.summarySubscription.unsubscribe();
        }

        if (this.filterSubscription) {
            this.filterSubscription.unsubscribe();
        }

        if (this.domainSubscription) {
            this.domainSubscription.unsubscribe();
        }
    }
}
