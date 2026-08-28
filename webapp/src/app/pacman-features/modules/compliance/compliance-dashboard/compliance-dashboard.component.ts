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

import { CdkDragDrop, moveItemInArray } from '@angular/cdk/drag-drop';
import { DecimalPipe } from '@angular/common';
import { Component, ElementRef, OnDestroy, OnInit, ViewChild } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import find from 'lodash/find';
import map from 'lodash/map';
import { Subject, Subscription } from 'rxjs';
import { AssetTypeMapService } from 'src/app/core/services/asset-type-map.service';
import { TableStateService } from 'src/app/core/services/table-state.service';
import { WindowExpansionService } from 'src/app/core/services/window-expansion.service';
import { WorkflowService } from 'src/app/core/services/workflow.service';
import { IssueFilterService } from 'src/app/pacman-features/services/issue-filter.service';
import { MultilineChartService } from 'src/app/pacman-features/services/multilinechart.service';
import { OverallComplianceService } from 'src/app/pacman-features/services/overall-compliance.service';
import { DATA_MAPPING } from 'src/app/shared/constants/data-mapping';
import { ComponentKeys } from 'src/app/shared/constants/component-keys';
import { CommonResponseService } from 'src/app/shared/services/common-response.service';
import { DownloadService } from 'src/app/shared/services/download.service';
import { ErrorHandlingService } from 'src/app/shared/services/error-handling.service';
import { LoggerService } from 'src/app/shared/services/logger.service';
import { RefactorFieldsService } from 'src/app/shared/services/refactor-fields.service';
import { RouterUtilityService } from 'src/app/shared/services/router-utility.service';
import { UtilsService } from 'src/app/shared/services/utils.service';
import { environment } from 'src/environments/environment';
import {
    DasbhoardCollapsedDict,
    DashboardArrangementItems,
    DashboardArrangementService,
    DashboardContainerIndex,
} from '../services/dashboard-arrangement.service';
import { AgDomainObservableService } from 'src/app/core/services/ag-domain-observable.service';

import { FilterManagementService } from 'src/app/shared/services/filter-management.service';
import {
    IFilterObj,
    IFilterOption,
    IFilterTagLabelsMap,
    IFilterTagOptionsMap,
    IFilterTypeLabel,
} from 'src/app/shared/table/interfaces/table-props.interface';
import { takeUntil } from 'rxjs/operators';
import { COMPLIANCE_LABEL, VIOLATIONS_LABEL } from 'src/app/shared/constants/global';

import { CategoryOrderMap, SeverityOrderMap } from 'src/app/shared/constants/order-mapping';

@Component({
    selector: 'app-compliance-dashboard',
    templateUrl: './compliance-dashboard.component.html',
    styleUrls: ['./compliance-dashboard.component.css'],
    animations: [],
    providers: [
        CommonResponseService,
        DashboardArrangementService,
        DecimalPipe,
        ErrorHandlingService,
        IssueFilterService,
        LoggerService,
        MultilineChartService,
        OverallComplianceService,
    ],
})
export class ComplianceDashboardComponent implements OnInit, OnDestroy {
    @ViewChild('widget') widgetContainer: ElementRef;

    pageTitle = 'Overview';
    saveStateKey: String = ComponentKeys.Dashboard;
    filterArr: any = [];
    filterText;
    queryParamsWithoutFilter;
    selectedAssetGroup: string;
    showingArr: any;
    ruleCatFilter;
    noMinHeight = false;
    paginatorSize = 100;
    totalRows = 0;
    bucketNumber = 0;
    searchTxt = '';
    complianceTableData: any = [];
    currentFilterType;
    filterTypeLabels: IFilterTypeLabel[] = [];
    filterTagLabels: IFilterTagLabelsMap = {};
    filterTypeOptions: IFilterOption[] = [];
    filters: IFilterObj[] = [];
    filterTagOptions: IFilterTagOptionsMap = {};
    selectedDomain: string = '';
    searchPassed = '';
    tableDataLoaded = false;
    showSearchBar = false;
    showAddRemoveCol = false;
    private complianceTableSubscription: Subscription;
    public pageLevel = 0;
    dataSubscriber: any;
    policyData: {
        color: string[];
        data: any[];
        legend: string[];
        legendTextcolor: string;
        totalCount: number;
        link: boolean;
        styling: { cursor: string };
    };
    policyDataError = '';
    showdata: boolean;
    error: boolean;
    loaded: boolean;
    fetchedViolations = false;
    widgetWidth2: number;
    breakpoint1: number;
    breakpoint2: number;
    breakpoint3: number;
    breakpoint4: number;
    tableTitle = 'Policy Compliance Overview';
    tableErrorMessage = '';
    errorMessage = '';
    headerColName: string;
    direction: string;
    complianceData = [];
    complianceDataError = '';
    breadcrumbArray = [];
    breadcrumbLinks = [];
    breadcrumbPresent = 'Dashboard';
    columnNamesMap = {
        name: 'Policy',
        failed: 'Violations',
        provider: 'Source',
        severity: 'Severity',
        policyCategory: 'Category',
        compliance_percent: 'Compliance',
    };
    columnWidths = {
        Policy: 3,
        Violations: 1,
        Source: 1,
        'Asset Type': 1,
        Severity: 1,
        Category: 1,
        Compliance: 1,
    };
    centeredColumns = {
        Policy: false,
        Violations: true,
        Source: true,
        Severity: true,
        Category: true,
        Compliance: true,
    };

    tableImageDataMap = {
        security: {
            image: 'category-security',
            imageOnly: true,
        },
        operations: {
            image: 'category-operations',
            imageOnly: true,
        },
        cost: {
            image: 'category-cost',
            imageOnly: true,
        },
        tagging: {
            image: 'category-tagging',
            imageOnly: true,
        },
        low: {
            image: 'violations-low-icon',
            imageOnly: true,
        },
        medium: {
            image: 'violations-medium-icon',
            imageOnly: true,
        },
        high: {
            image: 'violations-high-icon',
            imageOnly: true,
        },
        critical: {
            image: 'violations-critical-icon',
            imageOnly: true,
        },
    };
    whiteListColumns: string[];
    displayedColumns: string[];
    selectedRowId: string;

    totalAssetsCountData = [];
    totalAssetsCountDataError = '';
    isStatePreserved = false;
    showDownloadBtn = true;
    tableScrollTop = 0;

    dashboardContainers: DashboardArrangementItems;
    dashcobardCollapsedContainers: DasbhoardCollapsedDict;

    readonly dashcobardCollapsedContainersTitles: { [key: number]: string } = {
        [DashboardContainerIndex.VIOLATION_SEVERITY]: 'Violations by Severity',
        [DashboardContainerIndex.CATEGORY_COMPLIANCE]:
            'Category Compliance & Violations by Severity',
        [DashboardContainerIndex.ASSET_GRAPH]: 'Asset Graph',
        [DashboardContainerIndex.POLICY_OVERVIEW]: 'Policy Compliance Overview',
    };
    agDomainSubscription: Subscription;
    selectedOrder: any;
    sortOrder: any;
    fieldName: string;
    fieldType: string;
    onScrollDataLoader = new Subject();
    destroy$ = new Subject<void>();

    constructor(
        private activatedRoute: ActivatedRoute,
        private agDomainObservableService: AgDomainObservableService,
        private commonResponseService: CommonResponseService,
        private dashboardArrangementService: DashboardArrangementService,
        private downloadService: DownloadService,
        private errorHandling: ErrorHandlingService,
        private issueFilterService: IssueFilterService,
        private logger: LoggerService,
        private multilineChartService: MultilineChartService,
        private numbersPipe: DecimalPipe,
        private overallComplianceService: OverallComplianceService,
        private refactorFieldsService: RefactorFieldsService,
        private router: Router,
        private routerUtilityService: RouterUtilityService,
        private tableStateService: TableStateService,
        private utils: UtilsService,
        private windowExpansionService: WindowExpansionService,
        private workflowService: WorkflowService,
        private assetTypeMapService: AssetTypeMapService,
        private filterManagementService: FilterManagementService,
    ) {}

    getPreservedState() {
        const state = this.tableStateService.getState(this.saveStateKey) ?? {};

        this.headerColName = state.headerColName ?? 'Severity';
        this.direction = state.direction ?? 'desc';
        this.displayedColumns = [
            'Policy',
            'Violations',
            'Source',
            'Severity',
            'Category',
            'Compliance',
        ];
        // this.bucketNumber = state.bucketNumber ?? 0;
        this.whiteListColumns = state?.whiteListColumns ?? this.displayedColumns;
        this.searchTxt = state?.searchTxt ?? '';
        this.tableScrollTop = state?.tableScrollTop;
        this.totalRows = state.totalRows ?? 0;
        this.selectedRowId = state?.selectedRowId;

        this.applyPreservedFilters(state);
    }

    applyPreservedFilters(state) {
        this.isStatePreserved = false;
        const updateInfo = this.filterManagementService.applyPreservedFilters(state);
        if (updateInfo.shouldUpdateFilters) {
            this.filters = state.filters || [];
            this.filterText = updateInfo.filterText;
        }
        if (updateInfo.shouldUpdateData) {
            this.isStatePreserved = true;
            this.complianceTableData = state.data || [];
            this.tableDataLoaded = true;
        }
    }

    ngOnInit() {
        this.agDomainObservableService
            .getAgDomain()
            .pipe(takeUntil(this.destroy$))
            .subscribe(([assetGroupName, domain]) => {
                this.getPreservedState();
                this.selectedAssetGroup = assetGroupName;
                this.selectedDomain = domain;
                this.updateComponent();
            });

        window.onbeforeunload = () => this.storeState();

        const breadcrumbInfo = this.workflowService.getDetailsFromStorage()['level0'];

        if (breadcrumbInfo) {
            this.breadcrumbArray = breadcrumbInfo.map((item) => item.title);
            this.breadcrumbLinks = breadcrumbInfo.map((item) => item.url);
        }
        this.breakpoint1 = window.innerWidth <= 800 ? 2 : 4;
        this.breakpoint2 = window.innerWidth <= 800 ? 1 : 2;
        this.breakpoint3 = window.innerWidth <= 400 ? 1 : 1;
        this.breakpoint4 = window.innerWidth <= 400 ? 1 : 1;
        this.dashboardContainers = this.dashboardArrangementService.getArrangement();
        this.dashcobardCollapsedContainers = this.dashboardArrangementService.getCollapsed();
    }

    openOverAllComplianceTrendModal = () => {
        this.router.navigate(['/pl', { outlets: { modal: ['overall-compliance-trend'] } }], {
            queryParamsHandling: 'merge',
        });
    };

    openOverAllPolicyViolationsTrendModal = () => {
        this.router.navigate(['/pl', { outlets: { modal: ['policy-violations-trend'] } }], {
            queryParamsHandling: 'merge',
        });
    };

    navigateToAssetDistribution = () => {
        this.workflowService.addRouterSnapshotToLevel(
            this.router.routerState.snapshot.root,
            0,
            this.breadcrumbPresent,
        );
        this.router.navigate(['/pl/assets/asset-distribution/'], {
            queryParamsHandling: 'merge',
        });
    };

    violationCards = [
        {
            id: 1,
            name: 'critical',
            totalViolations: 0,
            subInfo: { Policies: { value: 0 }, Assets: { value: 0 } },
        },
        {
            id: 2,
            name: 'high',
            totalViolations: 0,
            subInfo: { Policies: { value: 0 }, Assets: { value: 0 } },
        },
        {
            id: 3,
            name: 'medium',
            totalViolations: 0,
            subInfo: { Policies: { value: 0 }, Assets: { value: 0 } },
        },
        {
            id: 4,
            name: 'low',
            totalViolations: 0,
            subInfo: { Policies: { value: 0 }, Assets: { value: 0 } },
        },
    ];

    cards = [
        {
            id: 1,
            header: 'Category Compliance',
            footer: 'View Trends',
            cardButtonAction: this.openOverAllComplianceTrendModal,
        },
        {
            id: 2,
            header: 'Violations by Severity',
            footer: 'View Trends',
            cardButtonAction: this.openOverAllPolicyViolationsTrendModal,
        },
        {
            id: 3,
            header: 'Asset Graph',
            footer: 'View Asset Distribution',
            cardButtonAction: this.navigateToAssetDistribution,
        },
    ];

    handleHeaderColNameSelection(event) {
        this.headerColName = event.headerColName;
        this.direction = event.direction;
        this.storeState();
        this.updatePoliciesTable();
    }

    handleFilterTypeSelection() {
        this.storeState();
    }

    handleFilterSelection() {
        this.storeState();
    }

    clearState() {
        // this.tableStateService.clearState(this.saveStateKey);
        this.isStatePreserved = false;
    }

    storeState() {
        const state = {
            totalRows: this.totalRows,
            data: this.complianceTableData,
            headerColName: this.headerColName,
            direction: this.direction,
            whiteListColumns: this.whiteListColumns,
            bucketNumber: this.bucketNumber,
            searchTxt: this.searchTxt,
            tableScrollTop: this.tableScrollTop,
            filters: this.filters,
            selectedRowId: this.selectedRowId,
        };
        this.tableStateService.setState(this.saveStateKey, state);
    }

    getDistributionBySeverity() {
        const distributionBySeverityUrl = environment.distributionBySeverity.url;
        const distributionBySeverityMethod = environment.distributionBySeverity.method;
        const queryParams = {
            ag: this.selectedAssetGroup,
            domain: this.selectedDomain,
        };

        try {
            this.commonResponseService
                .getData(distributionBySeverityUrl, distributionBySeverityMethod, {}, queryParams)
                .subscribe((response) => {
                    const data = response.distribution.distributionBySeverity;
                    this.processDonutChartData(data);
                    for (let i = 0; i < this.violationCards.length; i++) {
                        const violationName = this.violationCards[i].name;
                        if (data[violationName]) {
                            this.violationCards[i].totalViolations =
                                data[violationName].totalViolations;
                            this.violationCards[i].subInfo = {
                                Policies: { value: data[violationName].policyCount },
                                Assets: { value: data[violationName].assetCount },
                            };
                        } else {
                            this.violationCards[i].totalViolations = 0;
                            this.violationCards[i].subInfo = {
                                Policies: { value: 0 },
                                Assets: { value: 0 },
                            };
                        }
                    }
                });
        } catch (error) {}
    }

    processDonutChartData(distributionBySeverity) {
        try {
            const dataValue = [];
            let totalCount = 0;
            for (const key of this.utils.getDescendingOrder(SeverityOrderMap)) {
                const count = distributionBySeverity[key].totalViolations;
                dataValue.push(count);
                totalCount += +count;
            }
            this.fetchedViolations = true;
            this.policyDataError = '';
            if (dataValue.length > 0 && totalCount > 0) {
                this.policyData = {
                    color: ['#D14938', '#F58F6F', '#F5B66F', '#506EA7'],
                    data: dataValue,
                    legend: this.utils.getDescendingOrder(SeverityOrderMap),
                    legendTextcolor: '#000',
                    totalCount: totalCount,
                    link: true,
                    styling: {
                        cursor: 'pointer',
                    },
                };
            } else {
                this.policyDataError = 'noDataAvailable';
            }
            this.loaded = true;
            this.showdata = true;
            this.error = false;
        } catch (error) {
            this.tableErrorMessage = this.errorHandling.handleJavascriptError(error);
            this.getErrorValues();
        }
    }

    getErrorValues(): void {
        this.loaded = true;
        this.error = true;
    }

    handleWhitelistColumnsChange(event) {
        this.whiteListColumns = event;
        this.storeState();
    }

    /*
     * This function gets the urlparameter and queryObj
     *based on that different apis are being hit with different queryparams
     */
    routerParam() {
        try {
            // this.filterText saves the queryparam
            const currentQueryParams = this.routerUtilityService.getQueryParametersFromSnapshot(
                this.router.routerState.snapshot.root,
            );
            if (currentQueryParams) {
                this.queryParamsWithoutFilter = JSON.parse(JSON.stringify(currentQueryParams));
                delete this.queryParamsWithoutFilter['filter'];
                /**
                 * The below code is added to get URLparameter and queryparameter
                 * when the page loads ,only then this function runs and hits the api with the
                 * filterText obj processed through processFilterObj function
                 */
                this.filterText = this.utils.processFilterObj(currentQueryParams);
            }
        } catch (error) {
            this.errorMessage = this.errorHandling.handleJavascriptError(error);
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
        let shouldUpdateComponent = false;
        [this.filters, shouldUpdateComponent] = this.filterManagementService.deleteFilters(
            event,
            this.filters,
        );
        if (shouldUpdateComponent) {
            this.getUpdatedUrl();
            this.updatePoliciesTable();
        }
    }
    /*
     * this functin passes query params to filter component to show filter
     */
    async getFilterArray() {
        try {
            const filterText = this.filterText;
            const filterTypeOptions = this.filterTypeOptions;
            let filters = this.filters;

            const formattedFilters = this.filterManagementService.getFormattedFilters(
                filterText,
                filterTypeOptions,
            );

            for (let i = 0; i < formattedFilters.length; i++) {
                filters = await this.processAndAddFilterItem({
                    formattedFilterItem: formattedFilters[i],
                    filters,
                });
                this.filters = filters;
            }
        } catch (error) {
            this.errorMessage = this.errorHandling.handleJavascriptError(error);
            this.logger.log('error', error);
        }
    }

    async processAndAddFilterItem({ formattedFilterItem, filters }) {
        const keyDisplayValue = this.utils.getFilterKeyDisplayValue(
            formattedFilterItem,
            this.filterTypeOptions,
        );
        const filterKey = formattedFilterItem.filterkey;
        const existingFilterObjIndex = filters.findIndex(
            (filter) =>
                filter.keyDisplayValue === keyDisplayValue || filter.keyDisplayValue === filterKey,
        );
        let filterObj;

        if (existingFilterObjIndex < 0) {
            if (!keyDisplayValue) {
                const validFilterValues = this.filterText[filterKey]?.split(',').map((value) => {
                    return { id: value, name: value };
                });
                filterObj = this.filterManagementService.createFilterObj(
                    filterKey,
                    filterKey,
                    validFilterValues,
                );
            } else {
                // we make API call by calling changeFilterType mathod to fetch filter options and their display names for a filterKey
                await this.changeFilterType(keyDisplayValue);
                const validFilterValues = this.filterManagementService.getValidFilterValues(
                    keyDisplayValue,
                    filterKey,
                    this.filterText,
                    this.filterTagOptions,
                    this.filterTagLabels,
                );
                filterObj = this.filterManagementService.createFilterObj(
                    keyDisplayValue,
                    filterKey,
                    validFilterValues,
                );
            }
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
        return new Promise((resolve) => {
            try {
                this.issueFilterService
                    .getFilters(
                        { filterId: 13, domain: this.selectedDomain },
                        environment.issueFilter.url,
                        environment.issueFilter.method,
                    )
                    .pipe(takeUntil(this.destroy$))
                    .subscribe(
                        async (response) => {
                            this.filterTypeLabels = map(response[0].response, 'optionName');
                            resolve(true);
                            this.filterTypeOptions = response[0].response;

                            this.filterTypeLabels.sort();
                            this.routerParam();
                            // this.deleteFilters();
                            await this.getFilterArray();
                            await Promise.resolve().then(() => this.getUpdatedUrl());
                        },
                        (error) => {
                            this.errorHandling.handleAPIError(error);
                        },
                    );
            } catch (error) {
                this.errorHandling.handleJavascriptError(error);
                resolve(false);
            }
        });
    }

    async changeFilterType(value, searchText = '') {
        try {
            const currentQueryParams = this.routerUtilityService.getQueryParametersFromSnapshot(
                this.router.routerState.snapshot.root,
            );
            this.currentFilterType = find(this.filterTypeOptions, { optionName: value });
            const filtersToBePassed = this.getFilterPayloadForDataAPI();
            const filterText = this.filterText;
            const currentFilterType = this.currentFilterType;
            const [updateFilterTags, labelsToExcludeSort] = this.getUpdateFilterTagsCallback();
            const agAndDomain = {
                ag: this.selectedAssetGroup,
                domain: this.selectedDomain,
            };

            const [filterTagOptions, filterTagLabels] =
                await this.filterManagementService.changeFilterType({
                    currentFilterType,
                    filterText,
                    filtersToBePassed,
                    type: undefined,
                    currentQueryParams,
                    agAndDomain,
                    searchText,
                    updateFilterTags,
                    labelsToExcludeSort,
                });
            this.filterTagOptions[value] = filterTagOptions;
            this.filterTagLabels[value] = filterTagLabels;

            this.filterTagLabels = { ...this.filterTagLabels };
        } catch (error) {
            this.errorMessage = this.errorHandling.handleJavascriptError(error);
            this.logger.log('error', error);
        }
    }

    getUpdateFilterTagsCallback() {
        const labelsToExcludeSort = ['compliance', 'violations'];
        const updateFilterTags = (filterTagsData, value) => {
            if (value.toLowerCase() == 'asset type') {
                this.assetTypeMapService.getAssetMap().subscribe((assetTypeMap) => {
                    const tagsData = filterTagsData;
                    filterTagsData = [];
                    tagsData.forEach((filterOption) => {
                        const obj = {
                            id: filterOption,
                            name: assetTypeMap.get(filterOption?.toLowerCase()) || filterOption,
                        };
                        filterTagsData.push(obj);
                    });
                });
            } else if (value === VIOLATIONS_LABEL || value === COMPLIANCE_LABEL) {
                filterTagsData = this.filterManagementService.getRangeFilterOptions(
                    filterTagsData,
                    value === COMPLIANCE_LABEL,
                );
            } else {
                const tagsData = filterTagsData;
                filterTagsData = [];
                tagsData.forEach((filterOption) => {
                    const obj = {
                        id: filterOption,
                        name: filterOption,
                    };
                    filterTagsData.push(obj);
                });
            }
            return filterTagsData;
        };
        return [updateFilterTags, labelsToExcludeSort];
    }

    async changeFilterTags(event) {
        let filterValues = event.filterValue;
        if (!filterValues) {
            return;
        }
        this.currentFilterType = find(this.filterTypeOptions, {
            optionName: event.filterKeyDisplayValue,
        });
        this.filters = this.filterManagementService.changeFilterTags(
            this.filters,
            this.filterTagOptions,
            this.currentFilterType,
            event,
        );
        this.getUpdatedUrl();
        this.updatePoliciesTable();
    }

    updateSortFieldName() {
        const sortColName = this.headerColName.toLowerCase();
        this.sortOrder = null;
        this.fieldType = 'string';
        try {
            this.fieldName = this.getSortFieldName(this.headerColName);

            if (sortColName == 'severity' || sortColName == 'category') {
                const mapOfOrderMaps = { severity: SeverityOrderMap, category: CategoryOrderMap };
                this.sortOrder = this.utils.getAscendingOrder(mapOfOrderMaps[sortColName]);
            }
        } catch (e) {
            this.logger.log('error', e);
            this.headerColName = 'Severity';
            this.direction = 'desc';
            this.sortOrder = this.utils.getAscendingOrder(SeverityOrderMap);
            this.fieldName = this.getSortFieldName(this.headerColName);
        }
    }

    getSortFieldName(colName) {
        let apiColName: any = Object.keys(this.columnNamesMap).find(
            (col) => this.columnNamesMap[col] == colName,
        );
        if (!apiColName) {
            apiColName = find(this.filterTypeOptions, {
                optionName: colName,
            })['optionValue'];
        }
        return apiColName;
    }

    updateComponent() {
        if (this.complianceTableSubscription) {
            this.complianceTableSubscription.unsubscribe();
        }
        // below condition ensures that on initial landing, updatecomponent executes only once
        if (!this.selectedAssetGroup || !this.selectedDomain) {
            return;
        }
        this.ruleCatFilter = undefined;
        this.noMinHeight = false;
        this.complianceData = [];
        this.complianceDataError = '';
        this.policyDataError = '';
        this.updatePoliciesTable();
        this.getDistributionBySeverity();
        this.getComplianceData();
    }

    updatePoliciesTable() {
        this.updateSortFieldName();
        if (this.isStatePreserved) {
            this.clearState();
            this.tableDataLoaded = true;
            this.getFilters();
        } else {
            this.tableScrollTop = 0;
            this.searchTxt = '';
            this.tableErrorMessage = '';
            this.errorMessage = '';
            this.tableDataLoaded = false;
            this.tableScrollTop = 0;
            this.bucketNumber = 0;
            this.complianceTableData = [];
            this.getFilters().then(() => {
                this.getData();
            });
        }
    }

    processData(data) {
        try {
            let innerArr = {};
            const totalVariablesObj = {};
            let cellObj = {};
            let processedData = [];
            const getData = data;

            let cellData;
            for (let row = 0; row < getData.length; row++) {
                const keynames = Object.keys(getData[row]);
                innerArr = {};
                keynames.forEach((col) => {
                    const isPolicyCol = col.toLowerCase() === 'policy';
                    cellData = getData[row][col];
                    cellObj = {
                        text: this.tableImageDataMap[
                            typeof cellData == 'string' ? cellData.toLowerCase() : cellData
                        ]?.imageOnly
                            ? ''
                            : cellData, // text to be shown in table cell
                        titleText: cellData == 'NR' ? 'No Resources' : cellData, // text to show on hover
                        valueText: cellData,
                        hasPostImage: false,
                        imgSrc: this.tableImageDataMap[
                            typeof cellData == 'string' ? cellData.toLowerCase() : cellData
                        ]?.image, // if imageSrc is not empty and text is also not empty then this image comes before text otherwise if imageSrc is not empty and text is empty then only this image is rendered,
                        postImgSrc: '',
                        isChip: '',
                        isMenuBtn: false,
                        properties: '',
                        isLink: isPolicyCol,
                    };
                    if (col.toLowerCase() === 'violations') {
                        cellObj = {
                            ...cellObj,
                            text: this.numbersPipe.transform(cellData),
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
            this.errorMessage = this.errorHandling.handleJavascriptError(error);
            this.logger.log('error', error);
        }
    }

    private getComplianceData() {
        if (!this.selectedAssetGroup || !this.selectedDomain) {
            return;
        }
        const queryParams = {
            ag: this.selectedAssetGroup,
            domain: this.selectedDomain,
        };

        const overallComplianceUrl = environment.overallCompliance.url;
        const overallComplianceMethod = environment.overallCompliance.method;
        this.overallComplianceService
            .getOverallCompliance(queryParams, overallComplianceUrl, overallComplianceMethod)
            .subscribe((response) => {
                try {
                    if (response[0].error) {
                        throw response[0];
                    }
                    this.complianceDataError = '';
                    this.complianceData = [
                        { class: '', title: 'Security', val: 'NR' },
                        { class: '', title: 'Cost', val: 'NR' },
                        { class: '', title: 'Operations', val: 'NR' },
                        { class: '', title: 'Tagging', val: 'NR' },
                    ];
                    response[0].data.forEach((element) => {
                        const category = element[1]['title'].toLowerCase();
                        let index;
                        switch (category) {
                            case 'security':
                                index = 0;
                                break;
                            case 'cost':
                                index = 1;
                                break;
                            case 'operations':
                                index = 2;
                                break;
                            case 'tagging':
                                index = 3;
                                break;
                        }
                        this.complianceData[index].val = element[1]['val'];

                        if (element[1]['val'] <= 40) {
                            this.complianceData[index].class = 'red';
                        } else if (element[1]['val'] <= 75) {
                            this.complianceData[index].class = 'or';
                        } else {
                            this.complianceData[index].class = 'gr';
                        }
                    });
                    if (this.complianceData.length == 0) {
                        this.complianceDataError = 'noDataAvailable';
                    }
                } catch (error) {
                    this.complianceDataError = 'apiResponseError';
                    this.logger.log('error', error);
                }
            });
    }

    getFilterPayloadForDataAPI() {
        const filterToBePassed = { ...this.filterText };
        Object.keys(filterToBePassed).forEach((filterKey) => {
            if (filterKey == 'domain') return;
            filterToBePassed[filterKey] = filterToBePassed[filterKey].split(',');
            if (filterKey == 'failed' || filterKey == 'compliance_percent') {
                filterToBePassed[filterKey] = filterToBePassed[filterKey].map((filterVal) => {
                    const [min, max] = filterVal.split('-');
                    if (!min || min == 'NR') {
                        return { min: -1, max: -1 };
                    }
                    return { min, max };
                });
            }
        });

        return filterToBePassed;
    }

    getData(isNextPageCalled = false) {
        if (!this.selectedAssetGroup || !this.selectedDomain) {
            return;
        }
        const filterToBePassed = this.getFilterPayloadForDataAPI();

        const filters = { domain: this.selectedDomain };

        const sortFilter = {
            fieldName: this.fieldName,
            fieldType: this.fieldType,
            order: this.direction,
            sortOrder: this.sortOrder,
        };

        const payload = {
            ag: this.selectedAssetGroup,
            filter: filters,
            reqFilter: filterToBePassed,
            from: this.bucketNumber * this.paginatorSize,
            size: this.paginatorSize,
            sortFilter,
        };

        this.tableErrorMessage = '';
        const complianceTableUrl = environment.complianceTable.url;
        const complianceTableMethod = environment.complianceTable.method;
        this.complianceTableSubscription = this.commonResponseService
            .getData(complianceTableUrl, complianceTableMethod, payload, {})
            .subscribe(
                (response) => {
                    try {
                        const updatedResponse = this.massageData(response.data.response);
                        const processedData = this.processData(updatedResponse);
                        if (isNextPageCalled) {
                            this.onScrollDataLoader.next(processedData);
                        } else {
                            this.complianceTableData = processedData;
                            this.tableDataLoaded = true;
                            if (this.complianceTableData.length === 0) {
                                this.totalRows = 0;
                                this.tableErrorMessage = 'noDataAvailable';
                            }
                            if (response.data.hasOwnProperty('total')) {
                                this.totalRows = response.data.total;
                            } else {
                                this.totalRows = this.complianceTableData.length;
                            }
                        }
                    } catch (e) {
                        this.setError(
                            this.errorHandling.handleJavascriptError(e),
                            isNextPageCalled,
                        );
                    }
                },
                (error) => {
                    this.setError(this.errorHandling.handleAPIError(error), isNextPageCalled);
                },
            );
    }

    setError(errorType, isNextPageCalled?) {
        if (!isNextPageCalled) {
            this.tableErrorMessage = errorType;
        }
        this.tableDataLoaded = true;
    }

    nextPg(e) {
        try {
            this.tableScrollTop = e;
            this.bucketNumber++;

            this.getData(true);
        } catch (error) {
            this.errorHandling.handleJavascriptError(error);
        }
    }

    getRouteQueryParameters(): any {
        this.activatedRoute.queryParams.pipe(takeUntil(this.destroy$)).subscribe((params) => {
            if (this.selectedAssetGroup && this.selectedDomain) {
                this.updateComponent();
            }
        });
    }

    massageData(data) {
        const refactoredService = this.refactorFieldsService;
        const columnNamesMap = this.columnNamesMap;
        const newData = [];
        data.map(function (row) {
            const KeysTobeChanged = Object.keys(row);
            let newObj = {};
            KeysTobeChanged.forEach((element) => {
                let elementnew;
                if (columnNamesMap[element]) {
                    elementnew = columnNamesMap[element];
                    newObj = Object.assign(newObj, { [elementnew]: row[element] });
                } else {
                    elementnew =
                        refactoredService.getDisplayNameForAKey(element.toLocaleLowerCase()) ||
                        element;
                    newObj = Object.assign(newObj, { [elementnew]: row[element] });
                }
                // change data value
                newObj[elementnew] = DATA_MAPPING[
                    typeof newObj[elementnew] == 'string'
                        ? newObj[elementnew].toLowerCase()
                        : newObj[elementnew]
                ]
                    ? DATA_MAPPING[newObj[elementnew].toLowerCase()]
                    : newObj[elementnew];
            });
            newObj['Compliance'] = newObj['assetsScanned'] == 0 ? 'NR' : newObj['Compliance'] + '%';
            newData.push(newObj);
        });
        return newData;
    }

    goToDetails(event) {
        const selectedRow = event.rowSelected;
        const data = event.data;
        this.tableScrollTop = event.tableScrollTop;
        this.selectedRowId = event.selectedRowId;
        this.filters = event.filters;
        try {
            this.workflowService.addRouterSnapshotToLevel(
                this.router.routerState.snapshot.root,
                0,
                this.breadcrumbPresent,
            );
            const updatedQueryParams = { ...this.activatedRoute.snapshot.queryParams };
            updatedQueryParams['searchValue'] = undefined;
            this.router.navigate(['../policy-details', selectedRow['Policy ID'].valueText], {
                relativeTo: this.activatedRoute,
                queryParams: updatedQueryParams,
                queryParamsHandling: 'merge',
            });
        } catch (error) {
            this.errorMessage = this.errorHandling.handleJavascriptError(error);
            this.logger.log('error', error);
        }
    }

    callNewSearch(searchVal) {
        this.searchTxt = searchVal;
        this.isStatePreserved = false;
        this.tableDataLoaded = false;
        this.getData();
    }

    calculateDate(_JSDate) {
        if (!_JSDate) {
            return 'No Data';
        }
        const date = new Date(_JSDate);
        const year = date.getFullYear().toString();
        const month = date.getMonth() + 1;
        let monthString;
        if (month < 10) {
            monthString = '0' + month.toString();
        } else {
            monthString = month.toString();
        }
        const day = date.getDate();
        let dayString;
        if (day < 10) {
            dayString = '0' + day.toString();
        } else {
            dayString = day.toString();
        }
        return monthString + '-' + dayString + '-' + year;
    }

    handlePopClick(event) {
        const fileType = 'csv';

        try {
            const queryParams = {
                fileFormat: fileType,
                serviceId: 2,
                fileType,
            };

            const filterToBePassed = this.getFilterPayloadForDataAPI();

            const downloadRequest = {
                ag: this.selectedAssetGroup,
                filter: {
                    domain: this.selectedDomain,
                },
                reqFilter: filterToBePassed,
                from: 0,
                searchtext: event.searchTxt,
                size: this.totalRows,
            };

            const downloadUrl = environment.download.url;
            const downloadMethod = environment.download.method;

            this.downloadService.requestForDownload(
                queryParams,
                downloadUrl,
                downloadMethod,
                downloadRequest,
                'Policy Compliance Overview',
                this.totalRows,
            );
        } catch (error) {
            this.logger.log('error', error);
        }
    }

    dropDashboardItem({
        container,
        previousIndex,
        currentIndex,
    }: CdkDragDrop<DashboardArrangementItems>) {
        moveItemInArray(container.data, previousIndex, currentIndex);
        this.dashboardArrangementService.saveArrangement(this.dashboardContainers);
        this.tableScrollTop = 0;
        this.complianceTableData = [...this.complianceTableData];
    }

    toggleContainer(index: number) {
        if (index === DashboardContainerIndex.ASSET_GRAPH && this.isCollapsedContainer(index)) {
            this.windowExpansionService.status.next(true);
        }

        this.dashcobardCollapsedContainers = {
            ...this.dashcobardCollapsedContainers,
            ...{ [index]: !this.isCollapsedContainer(index) },
        };
        this.dashboardArrangementService.saveCollapsed(this.dashcobardCollapsedContainers);
    }

    isCollapsedContainer(index: number) {
        return this.dashcobardCollapsedContainers[index];
    }

    collapsedContainerTitle(index: number) {
        return this.dashcobardCollapsedContainersTitles[index];
    }

    ngOnDestroy() {
        try {
            this.storeState();
            this.destroy$.next();
            this.destroy$.complete();

            if (this.complianceTableSubscription) {
                this.complianceTableSubscription.unsubscribe();
            }
        } catch (error) {
            this.logger.log('error', error);
        }
    }

    onresize(event): void {
        this.breakpoint1 = event.target.innerWidth <= 1000 ? 2 : 4;
        this.breakpoint2 = event.target.innerWidth <= 800 ? 1 : 2;
        this.breakpoint3 = event.target.innerWidth <= 400 ? 1 : 1;
        this.breakpoint4 = event.target.innerWidth <= 400 ? 1 : 1;
    }
}
