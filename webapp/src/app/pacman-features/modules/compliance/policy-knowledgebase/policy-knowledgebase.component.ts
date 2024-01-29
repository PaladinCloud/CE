import { AfterViewInit, Component, OnDestroy, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import find from 'lodash/find';
import map from 'lodash/map';
import { Subject, Subscription } from 'rxjs';
import { AssetTypeMapService } from 'src/app/core/services/asset-type-map.service';
import { TableStateService } from 'src/app/core/services/table-state.service';
import { TourService } from 'src/app/core/services/tour.service';
import { WorkflowService } from 'src/app/core/services/workflow.service';
import { IssueFilterService } from 'src/app/pacman-features/services/issue-filter.service';
import { ComponentKeys } from 'src/app/shared/constants/component-keys';
import { CommonResponseService } from 'src/app/shared/services/common-response.service';
import { DownloadService } from 'src/app/shared/services/download.service';
import { ErrorHandlingService } from 'src/app/shared/services/error-handling.service';
import { LoggerService } from 'src/app/shared/services/logger.service';
import { RefactorFieldsService } from 'src/app/shared/services/refactor-fields.service';
import { RouterUtilityService } from 'src/app/shared/services/router-utility.service';
import { UtilsService } from 'src/app/shared/services/utils.service';
import { environment } from 'src/environments/environment';
import { AgDomainObservableService } from 'src/app/core/services/ag-domain-observable.service';
import { FilterManagementService } from 'src/app/shared/services/filter-management.service';
import { IFilterObj, IFilterOption, IFilterTagLabelsMap, IFilterTagOptionsMap, IFilterTypeLabel } from 'src/app/shared/table/interfaces/table-props.interface';
import { CategoryOrderMap, SeverityOrderMap } from 'src/app/shared/constants/order-mapping';

enum PolicyCategory {
  ALL_POLICIES = 'all policies',
  COST = 'cost',
  OPERATIONS = 'operations',
  SECURITY = 'security',
  TAGGING = 'tagging',
}

@Component({
  selector: 'app-policy-knowledgebase',
  templateUrl: './policy-knowledgebase.component.html',
  styleUrls: ['./policy-knowledgebase.component.css'],
  providers: [CommonResponseService, LoggerService, ErrorHandlingService]
})
export class PolicyKnowledgebaseComponent implements OnInit, AfterViewInit, OnDestroy {
  pageTitle = 'Policies';
  saveStateKey: String = ComponentKeys.UserPolicyList;
  selectedAssetGroup: string;
  selectedDomain: string;
  subscriptionToAssetGroup: Subscription;
  domainSubscription: Subscription;
  complianceTableSubscription: Subscription;
  issueFilterSubscription: Subscription;
  tableDataLoaded = false;
  searchTxt = '';
  breadcrumbPresent;
  policyCategoryDic: { [key in PolicyCategory]: number } = {
    [PolicyCategory.ALL_POLICIES]: 0,
    [PolicyCategory.SECURITY]: 0,
    [PolicyCategory.OPERATIONS]: 0,
    [PolicyCategory.COST]: 0,
    [PolicyCategory.TAGGING]: 0,
  };
  policyCategories = [
    PolicyCategory.ALL_POLICIES,
    PolicyCategory.SECURITY,
    PolicyCategory.OPERATIONS,
    PolicyCategory.COST,
    PolicyCategory.TAGGING,
  ];
  errorMessage: any = '';
  currentPageLevel = 0;
  headerColName: string;
  direction: string;
  showSearchBar = true;
  showAddRemoveCol = true;
  filterText;
  queryParamsWithoutFilter;
  filters: IFilterObj[] = [];
  filterTypeLabels: IFilterTypeLabel[] = [];
  filterTagLabels: IFilterTagLabelsMap = {};
  filterTypeOptions: IFilterOption[] = [];
  filterTagOptions: IFilterTagOptionsMap = {};

  selectedOrder: string;
  sortOrder: string[] | null;
  fieldName: string;
  fieldType: string;
  onScrollDataLoader = new Subject();
  destroy$ = new Subject<void>();
  
  centeredColumns = {
    Policy: false,
    Source: true,
    Severity: true,
    Category: true,
    'Asset Type': false,
  };
  columnWidths = { Policy: 2.5, Source: 0.5, Severity: 0.75, Category: 0.75, 'Asset Type': 1};
  columnNamesMap = { name: 'Policy', provider: 'Source'};
  tableImageDataMap = {
    [PolicyCategory.ALL_POLICIES]: {
      image: 'policy-icon',
      imageOnly: true
    },
    security: {
      image: "category-security",
      imageOnly: true
    },
    operations: {
      image: "category-operations",
      imageOnly: true
    },
    cost: {
      image: "category-cost",
      imageOnly: true
    },
    tagging: {
      image: "category-tagging",
      imageOnly: true
    },
    low: {
      image: "violations-low-icon",
      imageOnly: true
    },
    medium: {
      image: "violations-medium-icon",
      imageOnly: true
    },
    high: {
      image: "violations-high-icon",
      imageOnly: true
    },
    critical: {
      image: "violations-critical-icon",
      imageOnly: true
    },
  }
  state: any = {};
  whiteListColumns: string[];
  selectedRowId: string;
  displayedColumns: string[];
  tableScrollTop = 0;
  tableData = [];
  isStatePreserved = false;
  doLocalSearch = true; // should be removed once tiles data is available from backend
  totalRows = 0;
  assetTypeMap: any;
  agDomainSubscription: Subscription;
  bucketNumber: number = 0;
  paginatorSize: number = 100;

  constructor(private agDomainObservableService: AgDomainObservableService,
    private router: Router,
    private activatedRoute: ActivatedRoute,
    private commonResponseService: CommonResponseService,
    private logger: LoggerService,
    private errorHandling: ErrorHandlingService,
    private workflowService: WorkflowService,
    private routerUtilityService: RouterUtilityService,
    private tableStateService: TableStateService,
    private downloadService: DownloadService,
    private assetTypeMapService: AssetTypeMapService,
    private tourService: TourService,
    private utils: UtilsService,
    private issueFilterService: IssueFilterService,
    private filterManagementService: FilterManagementService
  ) {}

  ngOnInit (): void {
    this.getAssetTypeMap();
    this.onAgDomainChange();
    this.breadcrumbPresent = "Policies";
    this.currentPageLevel = this.routerUtilityService.getpageLevel(this.router.routerState.snapshot.root);
    window.onbeforeunload = () => this.storeState();
  }
  
  getAssetTypeMap () {
    this.assetTypeMapService.getAssetMap().subscribe(assetTypeMap => {
      this.assetTypeMap = assetTypeMap;
    });
  }

  onAgDomainChange () {
    this.agDomainSubscription = this.agDomainObservableService.getAgDomain().subscribe(([ag, domain]) => {
      this.selectedAssetGroup = ag;
      this.selectedDomain = domain;
      this.getPreservedState();
      this.getFilters();
    })
  }

  getPreservedState(){
      const state = this.tableStateService.getState(this.saveStateKey) || {};
      
      this.searchTxt = this.activatedRoute.snapshot.queryParams.searchValue || '';
      this.displayedColumns = Object.keys(this.columnWidths);

      this.headerColName = state?.headerColName || 'Severity';
      this.direction = state?.direction || 'desc';
      this.displayedColumns = Object.keys(this.columnWidths);
      this.whiteListColumns = state?.whiteListColumns || this.displayedColumns;
      this.searchTxt = state?.searchTxt || '';
      this.tableDataLoaded = true;
      this.tableScrollTop = state?.tableScrollTop;
      this.totalRows = this.tableData.length;
      this.selectedRowId = state?.selectedRowId;
      if(state?.policyCategoryDic)
      this.policyCategoryDic = state.policyCategoryDic;

    this.isStatePreserved = false;

    this.applyPreservedFilters(state);
  }

  applyPreservedFilters (state) {
    this.isStatePreserved = false;
    const updateInfo = this.filterManagementService.applyPreservedFilters(state);
    if (updateInfo.shouldUpdateFilters) {
      this.filters = state.filters || [];
      this.filterText = updateInfo.filterText;
    }
    if (updateInfo.shouldUpdateData) {
      this.isStatePreserved = true;
      this.tableData = state.data || [];
      this.tableDataLoaded = true;
    }
  }

  getRouteQueryParameters(): any {
    this.activatedRoute.queryParams.subscribe(
      (params) => {
        if (this.selectedAssetGroup && this.selectedDomain) {
          this.updateComponent();
        }
      }
    );
  }

  handleHeaderColNameSelection(event) {
    this.headerColName = event.headerColName;
    this.direction = event.direction;
    this.updateComponent();
  }

  handleWhitelistColumnsChange(event) {
    this.whiteListColumns = event;
  }

  handlePopClick(event) {
    const fileType = "csv";

    try {
      let queryParams;

      queryParams = {
        fileFormat: "csv",
        serviceId: 2,
        fileType: fileType,
      };

      const filtersToBePassed = this.getFilterPayloadForDataAPI();

      const downloadRequest = {
        ag: this.selectedAssetGroup,
        filter: {
          domain: this.selectedDomain,
        },
        reqFilter: filtersToBePassed,
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
        "Policy",
        this.totalRows
      );
    } catch (error) {
      this.logger.log("error", error);
    }
  }

  clearState() {
    // this.tableStateService.clearState("policyKnowledgebase");
    this.isStatePreserved = false;
  }

  storeState(){
    const state = {
      totalRows: this.totalRows,
      data: this.tableData,
      headerColName: this.headerColName,
      direction: this.direction,
      whiteListColumns: this.whiteListColumns,
      searchTxt: this.searchTxt,
      tableScrollTop: this.tableScrollTop,
      filters: this.filters,
      selectedRowId: this.selectedRowId,
      policyCategoryDic: this.policyCategoryDic
    }
    this.tableStateService.setState(this.saveStateKey, state);
  }

  /*
   * This function gets the urlparameter and queryObj
   *based on that different apis are being hit with different queryparams
   */
  routerParam() {
    try {
      // this.filterText saves the queryparam
      const currentQueryParams =
        this.routerUtilityService.getQueryParametersFromSnapshot(
          this.router.routerState.snapshot.root
        );
      if (currentQueryParams) {
        this.queryParamsWithoutFilter = JSON.parse(
          JSON.stringify(currentQueryParams)
        );
        delete this.queryParamsWithoutFilter["filter"];
        /**
         * The below code is added to get URLparameter and queryparameter
         * when the page loads ,only then this function runs and hits the api with the
         * filterText obj processed through processFilterObj function
         */
        this.filterText = this.utils.processFilterObj(currentQueryParams);
      }
    } catch (error) {
      this.errorMessage = this.errorHandling.handleJavascriptError(error);
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

  deleteFilters (event?) {
    const [filters, shouldUpdateComponent] = this.filterManagementService.deleteFilters(event, this.filters);
    this.filters = filters;
    if (shouldUpdateComponent) {
      this.getUpdatedUrl();
      this.updateComponent();
    }
  }
  
  /*
   * this functin passes query params to filter component to show filter
   */
  async getFilterArray () {
    try {
      const filterText = this.filterText;
      const filterTypeOptions = this.filterTypeOptions;
      let filters = this.filters;

      const formattedFilters = this.filterManagementService.getFormattedFilters(filterText, filterTypeOptions);

      for (let i = 0; i < formattedFilters.length; i++) {
        filters = await this.processAndAddFilterItem({ formattedFilterItem: formattedFilters[i], filters });
        this.filters = filters;
      }
    } catch (error) {
      this.errorMessage = this.errorHandling.handleJavascriptError(error);
      this.logger.log("error", error);
    }
  }

  async processAndAddFilterItem ({ formattedFilterItem, filters }) {

    const keyDisplayValue = this.utils.getFilterKeyDisplayValue(formattedFilterItem, this.filterTypeOptions);
    const filterKey = formattedFilterItem.filterkey;

    const existingFilterObjIndex = filters.findIndex(filter => filter.keyDisplayValue === keyDisplayValue);
    if (existingFilterObjIndex < 0) {
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
    return new Promise((resolve) => {
    try {
      this.issueFilterSubscription = this.issueFilterService
        .getFilters(
          { filterId: 16, domain: this.selectedDomain },
          environment.issueFilter.url,
          environment.issueFilter.method
        )
        .subscribe(async(response) => {
          this.filterTypeLabels = map(response[0].response, "optionName");
          resolve(true);
          this.filterTypeOptions = response[0].response;

          this.filterTypeLabels.sort();
          this.routerParam();
          await this.getFilterArray();
          await Promise.resolve().then(() => this.getUpdatedUrl());
          this.updateComponent();
        });
    } catch (error) {
      this.errorMessage = this.errorHandling.handleJavascriptError(error);
      this.logger.log("error", error);
      resolve(false);
    }
    });
  }

  async changeFilterType (value, searchText = '') {
    try {
      const currentQueryParams =
        this.routerUtilityService.getQueryParametersFromSnapshot(
          this.router.routerState.snapshot.root
        );
      const currentFilterType = find(this.filterTypeOptions, { optionName: value });
      const filtersToBePassed = this.getFilterPayloadForDataAPI();
      const filterText = this.filterText;
      const [updateFilterTags, labelsToExcludeSort] = this.getUpdateFilterTagsCallback();
      const agAndDomain = {
        ag: this.selectedAssetGroup,
        domain: this.selectedDomain
      }

      const [filterTagOptions, filterTagLabels] = await this.filterManagementService.changeFilterType({ currentFilterType, filterText, filtersToBePassed, type: undefined, currentQueryParams, agAndDomain, searchText, updateFilterTags, labelsToExcludeSort });
      this.filterTagOptions[value] = filterTagOptions;
      this.filterTagLabels[value] = filterTagLabels;

      this.filterTagLabels = { ...this.filterTagLabels };

    } catch (error) {
      this.errorMessage = this.errorHandling.handleJavascriptError(error);
      this.logger.log("error", error);
    }
  }

  getUpdateFilterTagsCallback () {
    const labelsToExcludeSort = [];
    const updateFilterTags = (filterTagsData, value) => {
      
      if (value.toLowerCase() == "asset type") {
        this.assetTypeMapService.getAssetMap().subscribe(assetTypeMap => {
          const tagsData = filterTagsData;
          filterTagsData = [];
          tagsData.forEach(filterOption => {
            const obj = {
              id: filterOption,
              name: assetTypeMap.get(filterOption?.toLowerCase()) || filterOption
            };
            filterTagsData.push(obj);
          });
        });
      } else {
        const tagsData = filterTagsData;
        filterTagsData = [];
        tagsData.forEach(filterOption => {
          const obj = {
            id: filterOption,
            name: filterOption
          };
          filterTagsData.push(obj);
        });
      }
      return filterTagsData;
    }
    return [updateFilterTags, labelsToExcludeSort];
  }

  async changeFilterTags (event) {
    let filterValues = event.filterValue;
    if (!filterValues) {
      return;
    }
    const currentFilterType = find(this.filterTypeOptions, {
      optionName: event.filterKeyDisplayValue,
    });
    this.filters = this.filterManagementService.changeFilterTags(this.filters, this.filterTagOptions, currentFilterType, event);
    this.getUpdatedUrl();
    this.updateComponent();
  }

  callNewSearch(searchVal) {
    if (!this.doLocalSearch) {
      this.searchTxt = searchVal;
      this.updateComponent();
    } else {
      this.searchTxt = searchVal;
    }
  }

  ngAfterViewInit() {

  }

  updateComponent () {
    this.updateSortFieldName();
    if(this.isStatePreserved){
      this.tableDataLoaded = true;
      this.clearState();
      this.tourService.setComponentReady();
    } else {
      this.bucketNumber = 0;
      this.tableScrollTop = 0;
      this.tableDataLoaded = false;
      this.getData();
    }
  }

  processData (data) {
    try {
      return this.utils.processTableData(data, this.tableImageDataMap, (row, col, cellObj) => {
        const cellData = row[col];
        if (col.toLowerCase() === 'policy') {
          const autoFixAvailable = typeof row.autoFixAvailable == "string" ? row.autoFixAvailable == "true" : row.autoFixAvailable;
          const autoFixEnabled = typeof row.autoFixEnabled == "string" ? row.autoFixEnabled == "true" : row.autoFixEnabled;
          let imgSrc = 'noImg';
          let imageTitleText = "";
          if (autoFixAvailable) {
            imgSrc = autoFixEnabled ? 'autofix' : 'no-autofix';
            imageTitleText = autoFixEnabled ? 'Autofix Enabled' : 'Autofix Available'
          }
          cellObj = {
            ...cellObj,
            isLink: true,
            imgSrc: imgSrc,
            imageTitleText: imageTitleText
          };
        } else if (col.toLowerCase() === 'asset type') {
          const currentAssetType = this.assetTypeMap.get(cellData);
          cellObj = {
            ...cellObj,
            text: currentAssetType ? currentAssetType : cellData,
            titleText: currentAssetType ? currentAssetType : cellData, // text to show on hover
            valueText: currentAssetType ? currentAssetType : cellData
          };
        }
        return cellObj;
      });

    } catch (error) {
      this.errorMessage = this.errorHandling.handleJavascriptError(error);
      this.logger.log("error", error);
    }
  }

  getTilesData() {
    const newPolicyDic: { [key in PolicyCategory]: number } = {
      [PolicyCategory.ALL_POLICIES]: 0,
      [PolicyCategory.COST]: 0,
      [PolicyCategory.OPERATIONS]: 0,
      [PolicyCategory.SECURITY]: 0,
      [PolicyCategory.TAGGING]: 0,
    };

    const payload = {
      ag: this.selectedAssetGroup,
      filter: { domain: this.selectedDomain },
      reqFilter: {},
      "includeDisabled": false
    };

    const complianceTableUrl = environment.complianceTable.url;
    const complianceTableMethod = environment.complianceTable.method;
    this.complianceTableSubscription = this.commonResponseService
      .getData(complianceTableUrl, complianceTableMethod, payload, {})
      .subscribe(
        (response) => {
          try {
            const getData = response.data.response;
            for (let i = 0; i < getData.length; i++) {
              newPolicyDic[PolicyCategory.ALL_POLICIES]++;
              newPolicyDic[getData[i]["policyCategory"].toLowerCase()]++
            }
            this.policyCategoryDic = newPolicyDic;
          } catch (e) {
            this.logger.log("jsError", e);
          }
        }, error => {
          this.logger.log("apiError", error);
        })
  }

  getFilterPayloadForDataAPI(){
    const filterToBePassed = {...this.filterText};

    Object.keys(filterToBePassed).forEach(filterKey => {
      if (filterKey == "domain") return;
      filterToBePassed[filterKey] = filterToBePassed[filterKey].split(",");
    })

    return filterToBePassed;
  }

  updateSortFieldName () {
    const sortColName = this.headerColName.toLowerCase();
    this.selectedOrder = this.direction;
    this.sortOrder = null;
    this.fieldType = "string";
    try {
        let apiColName: any = Object.keys(this.columnNamesMap).find(col => this.columnNamesMap[col] == this.headerColName);
        if (!apiColName) {
          apiColName = find(this.filterTypeOptions, {
            optionName: this.headerColName,
          })["optionValue"];
        }
        this.fieldName = apiColName;

      if (sortColName == 'severity' || sortColName == 'category') {
        const mapOfOrderMaps = { 'severity': SeverityOrderMap, 'category': CategoryOrderMap }
        this.sortOrder = this.utils.getAscendingOrder(mapOfOrderMaps[sortColName]);
      }
    } catch (e) {
      this.logger.log("error", e);
      this.headerColName = '';
    }
  }

  getData (isNextPageCalled = false) {
    if(!this.selectedAssetGroup || !this.selectedDomain){
      return;
    }
    this.errorMessage = '';
    if (this.complianceTableSubscription) {
      this.complianceTableSubscription.unsubscribe();
    }

    const filters = { domain: this.selectedDomain };

    const sortFilter = {
      fieldName: this.fieldName,
      fieldType: this.fieldType,
      order: this.selectedOrder,
      sortOrder: this.sortOrder
    }

    const payload = {
      ag: this.selectedAssetGroup,
      filter: filters,
      reqFilter: this.getFilterPayloadForDataAPI(),
      from: this.bucketNumber * this.paginatorSize,
      size: this.paginatorSize,
      sortFilter,
      "includeDisabled" : false
    };

    const complianceTableUrl = environment.complianceTable.url;
    const complianceTableMethod = environment.complianceTable.method;
    this.complianceTableSubscription = this.commonResponseService
    .getData(complianceTableUrl, complianceTableMethod, payload, {})
    .subscribe(
      (response) => {
        try {
          const updatedResponse = this.utils.massageTableData(response.data.response, this.columnNamesMap);
          const processedData = this.processData(updatedResponse);
          if (isNextPageCalled) {
            this.onScrollDataLoader.next(processedData);
          } else {
            this.tableData = processedData;
            this.getTilesData();
            this.tableDataLoaded = true;
            if (this.tableData.length === 0) {
              this.totalRows = 0;
              this.errorMessage = 'noDataAvailable';
            }
            if (response.data.hasOwnProperty("total")) {
              this.totalRows = response.data.total;
            } else {
              this.totalRows = this.tableData.length;
            }
          }
        } catch (e) {
          this.setError(this.errorHandling.handleJavascriptError(e), isNextPageCalled);
        }
        this.tourService.setComponentReady();
      },
      (error) => {
        this.setError(this.errorHandling.handleAPIError(error), isNextPageCalled);
        this.tourService.setComponentReady();
      }
    );
  }

  setError (errorType, isNextPageCalled?) {
    if (!isNextPageCalled) {
      this.errorMessage = errorType;
    }
    this.tableDataLoaded = true;
  }

  /*
    * this function is used to fetch the rule id and to navigate to the next page
    */

  goToDetails(event) {
    // store in this function
    const tileData = event.rowSelected;
    const data = event.data;
    this.selectedRowId = event.selectedRowId;
    this.tableScrollTop = event.tableScrollTop;
    let autofixEnabled = false;
    if ( tileData.autoFixEnabled) {
      autofixEnabled = true;
    }
    const policyId = tileData["Policy ID"].valueText;
    try {
      this.workflowService.addRouterSnapshotToLevel(this.router.routerState.snapshot.root, 0, this.breadcrumbPresent);
      const updatedQueryParams = { ...this.activatedRoute.snapshot.queryParams };
      updatedQueryParams["searchValue"] = undefined;
      this.router.navigate(
        ['pl', 'compliance', 'policy-knowledgebase-details', policyId, autofixEnabled],
        {
          queryParams: updatedQueryParams,
          queryParamsHandling: 'merge'
        });
    } catch (error) {
      this.errorMessage = this.errorHandling.handleJavascriptError(error);
      this.logger.log('error', error);
    }
  }

  async applyFilterByCategory(policyCategory: PolicyCategory) {
    if (this.policyCategoryDic[policyCategory] == 0) return;
    const key = 'Category';
    this.filters = [];
    await Promise.resolve().then(() => this.getUpdatedUrl());
    if (policyCategory !== PolicyCategory.ALL_POLICIES) {
      await this.changeFilterType(key)
      this.changeFilterTags({
        filterKeyDisplayValue: key,
        filterValue: [policyCategory],
      })
    } else {
      await this.changeFilterType(key)
      this.changeFilterTags({
        filterKeyDisplayValue: key,
        filterValue: this.filterTagOptions[key].map(item => item.id),
      })
    }
  }

  nextPg (e) {
    try {
      this.tableScrollTop = e;
      this.bucketNumber++;

      this.getData(true);
    } catch (error) {
      this.errorHandling.handleJavascriptError(error);
    }
  }

  ngOnDestroy() {
    try {
      this.storeState();
      if(this.agDomainSubscription){
        this.agDomainSubscription.unsubscribe();
      }
      if (this.complianceTableSubscription) {
        this.complianceTableSubscription.unsubscribe();
      }
      if (this.subscriptionToAssetGroup) {
        this.subscriptionToAssetGroup.unsubscribe();
      }
      if (this.domainSubscription) {
        this.domainSubscription.unsubscribe();
      }
      if(this.agDomainSubscription){
         this.agDomainSubscription.unsubscribe();
      }
    } catch (error) {
      this.logger.log('error', '--- Error while unsubscribing ---');
    }
  }
}
