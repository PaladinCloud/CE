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

import {Component, OnDestroy, OnInit} from '@angular/core';
import {environment} from '../../../../../environments/environment';
import {ActivatedRoute, Router} from '@angular/router';
import {Subject, Subscription} from 'rxjs';
import {UtilsService} from '../../../../shared/services/utils.service';
import {LoggerService} from '../../../../shared/services/logger.service';
import {CommonResponseService} from '../../../../shared/services/common-response.service';
import {DownloadService} from '../../../../shared/services/download.service';
import {WorkflowService} from '../../../../core/services/workflow.service';
import {RouterUtilityService} from '../../../../shared/services/router-utility.service';
import {ErrorHandlingService} from 'src/app/shared/services/error-handling.service';
import {TableStateService} from 'src/app/core/services/table-state.service';
import find from 'lodash/find';
import map from 'lodash/map';
import { IssueFilterService } from 'src/app/pacman-features/services/issue-filter.service';
import { ComponentKeys } from 'src/app/shared/constants/component-keys';
import { DatePipe } from '@angular/common';
import { FilterManagementService } from 'src/app/shared/services/filter-management.service';
import { AgDomainObservableService } from 'src/app/core/services/ag-domain-observable.service';
import { IColumnNamesMap, IColumnWidthsMap } from 'src/app/shared/table/interfaces/table-props.interface';

@Component({
    selector: 'app-cloud-notifications',
    templateUrl: './cloud-notifications.component.html',
    styleUrls: ['./cloud-notifications.component.css'],
    providers: [
        LoggerService,
        IssueFilterService
    ]
})

export class CloudNotificationsComponent implements OnInit, OnDestroy {
    agDomainSubscription: Subscription;
    dataSubscription: Subscription;
    summarySubscription: Subscription;
    filterSubscription: Subscription;

    pageTitle = "Notifications";
    saveStateKey: String = ComponentKeys.NotificationList;
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
        'eventtypecategory': ''
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
    bucketNumber: number = 0;
    totalRows: number = 0;
    tableDataLoaded: boolean = false;
    tableData: any = [];
    displayedColumns: string[] = [];
    whiteListColumns: any = [];
    selectedRowIndex;
    tableScrollTop: any;
    isTableStatePreserved = false;

    columnNamesMap: IColumnNamesMap = {
        'eventName': 'Event'
    };

    columnWidths: IColumnWidthsMap = {
        'Event': 2,
    };

    centeredColumns = {
        Event: false,
        Type: true,
        Source: true,
        "Created Date": true,
    };

    FullQueryParams: any;
    queryParamsWithoutFilter: any;
    selectedOrder: string;
    sortOrder: any;
    fieldType: string;
    fieldName: any;
    dateCategoryList: string[] = ['Created Date'];

    constructor(
        private agDomainObservableService: AgDomainObservableService,
        private filterService: IssueFilterService,
        private router: Router,
        private errorHandler: ErrorHandlingService,
        private utils: UtilsService,
        private logger: LoggerService,
        private workflowService: WorkflowService,
        private commonResponseService: CommonResponseService,
        private downloadService: DownloadService,
        private routerUtilityService: RouterUtilityService,
        private activatedRoute: ActivatedRoute,
        private tableStateService: TableStateService,
        private filterManagementService: FilterManagementService
    ) {
        this.currentPageLevel = this.routerUtilityService.getpageLevel(this.router.routerState.snapshot.root);
        this.backButtonRequired = this.workflowService.checkIfFlowExistsCurrently(
            this.pageLevel
        );
    }

    getPreservedState(){
        const state = this.tableStateService.getState(this.saveStateKey) || {};
        this.headerColName = state.headerColName || '';
        this.direction = state.direction || '';
        this.bucketNumber = state.bucketNumber || 0;
        this.totalRows = state.totalRows || 0;
        this.searchTxt = state?.searchTxt || '';
        this.selectedRowIndex = state?.selectedRowIndex;

        this.tableDataLoaded = true;

        this.displayedColumns = ['Event', 'Type', 'Source', 'Created Date'];
        this.whiteListColumns = state?.whiteListColumns || this.displayedColumns;
        this.tableScrollTop = state?.tableScrollTop;

        this.isTableStatePreserved = false;
        const navDirection = this.workflowService.getNavigationDirection();

        if(navDirection<=0){
            this.filters = state.filters || [];
            if (state.data && state.data.length > 0) {
                this.isTableStatePreserved = true;
                this.tableData = state.data;
            }
            Promise.resolve().then(() => this.getUpdatedUrl());
        }
    }

    ngOnInit() {
        this.agDomainSubscription = this.agDomainObservableService
            .getAgDomain()
            .subscribe(([ag, domain]) => {
                  this.getPreservedState();
                  if(this.selectedAssetGroup){
                    this.tableScrollTop = 0;
                  }
                  this.selectedAssetGroup = ag;
                  this.selectedDomain = domain;
                  this.getFilters();
            });
    }

    /*
     * This function gets the urlparameter and queryObj
     *based on that different apis are being hit with different queryparams
     */
    routerParam() {
        try {
            const currentQueryParams =
                this.routerUtilityService.getQueryParametersFromSnapshot(
                    this.router.routerState.snapshot.root
                );
            if (currentQueryParams) {
                this.FullQueryParams = currentQueryParams;
                this.queryParamsWithoutFilter = JSON.parse(
                    JSON.stringify(this.FullQueryParams)
                );
                delete this.queryParamsWithoutFilter["filter"];
                this.filterText = this.utils.processFilterObj(this.FullQueryParams);
            }
        } catch (error) {
            this.errorMessage = this.errorHandler.handleJavascriptError(error);
            this.logger.log("error", error);
        }
    }

    getUpdatedUrl() {
        let updatedQueryParams = {};
        this.filterText = this.utils.arrayToObject(
            this.filters,
            "filterkey",
            "value"
        ); // <-- TO update the queryparam which is passed in the filter of the api
        this.filterText = this.utils.makeFilterObj(this.filterText);

        /**
         * To change the url
         * with the deleted filter value along with the other existing paramter(ex-->tv:true)
         */
        updatedQueryParams = {
            filter: this.filterText.filter,
        }

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
        let shouldUpdateComponent = false;
        [this.filters, shouldUpdateComponent] = this.filterManagementService.deleteFilters(event, this.filters);      
        if(shouldUpdateComponent){
            this.getUpdatedUrl();
            this.updateComponent();
        }
        this.storeState();
    }

    /*
       * this functin passes query params to filter component to show filter
       */
    async getFilterArray() {
        try {
          const filterText = this.filterText;
          const filterTypeOptions = this.filterTypeOptions;
          let filters = this.filters;
          
          const formattedFilters = this.filterManagementService.getFormattedFilters(filterText, filterTypeOptions);
    
          for (let i = 0; i < formattedFilters.length; i++) {
            filters = await this.processAndAddFilterItem({ formattedFilterItem: formattedFilters[i] , filters});
            this.filters = filters;
          }
          this.storeState();
        } catch (error) {
          this.errorMessage = this.errorHandler.handleJavascriptError(error);
          this.logger.log("error", error);
        }
      }
    
      async processAndAddFilterItem({formattedFilterItem, filters}){
    
        const keyDisplayValue = this.utils.getFilterKeyDisplayValue(formattedFilterItem, this.filterTypeOptions);
        const filterKey = formattedFilterItem.filterkey;
          
        const existingFilterObjIndex = filters.findIndex(filter => filter.keyDisplayValue === keyDisplayValue);
        if(existingFilterObjIndex<0){
          // we make API call by calling changeFilterType mathod to fetch filter options and their display names for a filterKey
          await this.changeFilterType(keyDisplayValue);
          const validFilterValues = this.filterManagementService.getValidFilterValues(keyDisplayValue, filterKey, this.filterText, this.filterTagOptions, this.filterTagLabels);
          const filterObj = this.filterManagementService.createFilterObj(keyDisplayValue, filterKey, validFilterValues);
    
          filters.push(filterObj);
        }
        filters = [...filters];
        return filters;
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
                    this.filterTypeLabels = map(response[0].response, 'optionName');
                    this.filterTypeOptions = response[0].response;
                    this.filterTypeLabels.sort();

                    [this.columnNamesMap, this.columnWidths] = this.utils.getColumnNamesMapAndColumnWidthsMap(this.filterTypeLabels, this.filterTypeOptions, this.columnWidths, this.columnNamesMap, []);
                    this.routerParam();
                    this.getFilterArray();
                    this.updateComponent();
                });
        } catch (error) {
            this.errorMessage = this.errorHandler.handleJavascriptError(error);
            this.logger.log("error", error);
        }
    }

    async changeFilterType(value, searchText='') {
        try {
          const currentQueryParams =
            this.routerUtilityService.getQueryParametersFromSnapshot(
              this.router.routerState.snapshot.root
            );
          this.currentFilterType = find(this.filterTypeOptions, { optionName: value });
          const filtersToBePassed = this.getFilterPayloadForDataAPI();
          const filterText = this.filterText;
          const currentFilterType = this.currentFilterType;
          const labelsToExcludeSort = ['created date'];
    
          const [filterTagOptions, filterTagLabels] = await this.filterManagementService.changeFilterType({currentFilterType, filterText, filtersToBePassed, type:undefined, currentQueryParams, agAndDomain:{}, searchText, updateFilterTags: undefined, labelsToExcludeSort});
          this.filterTagOptions[value] = filterTagOptions;
          this.filterTagLabels[value] = filterTagLabels;

          this.filterTagLabels = {...this.filterTagLabels};          
          this.storeState();
      
        } catch (error) {
          this.errorMessage = this.errorHandler.handleJavascriptError(error);
          this.logger.log("error", error);
        }
    }


    getFormattedDate(dateString: string, isEndDate: boolean = false): string {
        const localDate = new Date(dateString);
    
        if (isEndDate) {
            localDate.setHours(23, 59, 59);
        } else {
            localDate.setMinutes(localDate.getMinutes() + localDate.getTimezoneOffset());
        }
        localDate.setMinutes(localDate.getMinutes() + localDate.getTimezoneOffset());
    
        const datePipe = new DatePipe('en-US');
    
        const formattedDate = datePipe.transform(localDate, 'yyyy-MM-dd HH:mm:ss');
    
        return formattedDate+"+0000";
    }

    async changeFilterTags(event) {
        let filterValues = event.filterValue;
        if(!filterValues){
          return;
        }
        this.currentFilterType =  find(this.filterTypeOptions, {
          optionName: event.filterKeyDisplayValue,
        });
        if(event.filterKeyDisplayValue.toLowerCase() !== "created date"){
            this.filters = this.filterManagementService.changeFilterTags(this.filters, this.filterTagOptions, this.currentFilterType, event);
        }
        this.storeState();
        this.getUpdatedUrl();
        this.updateComponent();
    }

    updateComponent() {
        this.updateSortFieldName();
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

    updateSortFieldName(){
        try{
            if(!this.headerColName || !this.direction) return;        
            this.selectedOrder = this.direction;        
            const apiColName =  find(this.filterTypeOptions, {
                optionName: this.headerColName,
            })["optionValue"];
            this.fieldType = "string";
            this.fieldName = apiColName+'.keyword';
        }catch(e){
            this.logger.log('Sort error', e);
            this.headerColName = '';
        }
    }

    handleHeaderColNameSelection(event) {
        this.headerColName = event.headerColName;
        this.direction = event.direction;
        this.updateComponent();
        this.storeState();
    }

    handleWhitelistColumnsChange(event) {
        this.whiteListColumns = event;
        this.storeState();
    }

    storeState(data?) {
        const isTempFilter = this.activatedRoute.snapshot.queryParamMap.get("tempFilters");
        if(isTempFilter) return;
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
        this.tableStateService.setState(this.saveStateKey, state);
    }

    clearState() {
        // this.tableStateService.clearState(this.pageTitle);
        this.isTableStatePreserved = false;
    }

    navigateBack() {
        try {
            this.workflowService.goBackToLastOpenedPageAndUpdateLevel(this.router.routerState.snapshot.root);
        } catch (error) {
            this.logger.log('error', error);
        }
    }

    getData(isNextPageCalled?) {
        if (this.dataSubscription) {
            this.dataSubscription.unsubscribe();
        }

        const sortFilter = {
            fieldName: this.fieldName,
            fieldType: this.fieldType,
            order: this.selectedOrder,
        }

        const filtersToBePassed = this.getFilterPayloadForDataAPI();

        const payload = {
            'ag': this.selectedAssetGroup,
            'filter': filtersToBePassed,
            'from': (this.bucketNumber) * this.paginatorSize,
            'searchtext': this.searchTxt,
            'size': this.paginatorSize,
            sortFilter
        };

        const queryParam = {
            global: this.tabSelected === 'general'
        };

        const TableUrl = environment.cloudNotifications.url;
        const TableMethod = environment.cloudNotifications.method;
        this.dataSubscription = this.commonResponseService.getData(TableUrl, TableMethod, payload, queryParam).subscribe(
            response => {
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

                        let updatedResponse = this.utils.massageTableData(response.data.response, this.columnNamesMap);
                        const processData = this.processData(updatedResponse);
                        if (isNextPageCalled) {
                            this.onScrollDataLoader.next(processData)
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
            error => {
                this.errorValue = -1;
                this.logger.log('error', error);
                this.tableErrorMessage = 'apiResponseError';
            });
    }

    processData(data) {
        try {
          return this.utils.processTableData(data, {}, (row, col, cellObj) => {
            if (col.toLowerCase() == "event") {
                cellObj = {
                    ...cellObj,
                    isLink: true
                };
            }else if(col.toLowerCase() == "created date"){
                
                cellObj = {
                    ...cellObj,
                    isDate: true
                }
            }
            
            return cellObj;
          });
          
        } catch (error) {
          this.errorMessage = this.errorHandler.handleJavascriptError(error);
          this.logger.log("error", error);
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
            this.workflowService.addRouterSnapshotToLevel(this.router.routerState.snapshot.root, 0, this.pageTitle);
            this.router.navigate(
                ['pl/notifications/notification-details'],
                {queryParams: {'eventId': eventId}, queryParamsHandling: 'merge'}
            ).then(response => {
                this.logger.log('info', 'Successfully navigated to details page: ' + response);
            }).catch(error => {
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
            this.logger.log("error", error);
        }
    }

    getFilterPayloadForDataAPI(){
        const filterToBePassed = {...this.filterText};
        Object.keys(filterToBePassed).forEach(filterKey => {
            if(filterKey=='_loaddate'){

                const [fromDate, toDate] = filterToBePassed[filterKey].split(" - ");
                const dateRangeString = `${this.getFormattedDate(fromDate)} - ${this.getFormattedDate(toDate, true)}`;
                filterToBePassed[filterKey] = dateRangeString;
            }
            filterToBePassed[filterKey] = filterToBePassed[filterKey].split(",");
        })

        return filterToBePassed;
    }

    handlePopClick(rowText) {
        const fileType = 'csv';
        try {
            let queryParams;
            queryParams = {
                'fileFormat': 'csv',
                'serviceId': this.tabSelected === 'general' ? 18 : 17,
                'fileType': fileType
            };

            const sortFilter = {
                fieldName: this.fieldName,
                fieldType: this.fieldType,
                order: this.selectedOrder,
            }

            const filtersToBePassed = this.getFilterPayloadForDataAPI();

            const downloadRequest = {
                'ag': this.selectedAssetGroup,
                'filter': filtersToBePassed,
                'from': 0,
                'searchtext': this.searchTxt,
                'size': this.totalRows,
                sortFilter
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
                this.totalRows);
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
        if (this.agDomainSubscription) {
            this.agDomainSubscription.unsubscribe();
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
    }
}
