import { Component, OnInit, OnDestroy, HostListener } from "@angular/core";
import { environment } from "./../../../../../environments/environment";
import { AssetGroupObservableService } from "../../../../core/services/asset-group-observable.service";
import { ActivatedRoute, Router } from "@angular/router";
import { Subject, Subscription } from "rxjs";
import { IssueFilterService } from "../../../services/issue-filter.service";
import { CommonResponseService } from "../../../../shared/services/common-response.service";
import find from 'lodash/find';
import map from 'lodash/map';
import { UtilsService } from "../../../../shared/services/utils.service";
import { LoggerService } from "../../../../shared/services/logger.service";
import { ErrorHandlingService } from "../../../../shared/services/error-handling.service";
import { DownloadService } from "../../../../shared/services/download.service";
import { RefactorFieldsService } from "./../../../../shared/services/refactor-fields.service";
import { WorkflowService } from "../../../../core/services/workflow.service";
import { DomainTypeObservableService } from "../../../../core/services/domain-type-observable.service";
import { RouterUtilityService } from "../../../../shared/services/router-utility.service";
import { PermissionGuardService } from "../../../../core/services/permission-guard.service";
import { DATA_MAPPING } from "src/app/shared/constants/data-mapping";
import { TableStateService } from "src/app/core/services/table-state.service";
import { AssetTypeMapService } from "src/app/core/services/asset-type-map.service";
import { ComponentKeys } from "src/app/shared/constants/component-keys";
import { FilterManagementService } from "src/app/shared/services/filter-management.service";

@Component({
  selector: "app-issue-listing",
  templateUrl: "./issue-listing.component.html",
  styleUrls: ["./issue-listing.component.css"],
  providers: [IssueFilterService, LoggerService, ErrorHandlingService],
})
export class IssueListingComponent implements OnInit, OnDestroy {
  pageTitle = "Violations";
  saveStateKey: String = ComponentKeys.ViolationList;
  selectedAssetGroup: string;
  selectedDomain: string;
  breadcrumbArray: any = [];
  breadcrumbLinks: any = [];
  totalRows = 0;
  bucketNumber = 0;
  paginatorSize = 100;
  searchTxt = "";
  filterTypeOptions: any = [];
  filterTagOptions: any = {};
  currentFilterType;
  filterTypeLabels = [];
  filterTagLabels = {};
  filters: any = [];
  filterText: any;
  FullQueryParams: any;
  queryParamsWithoutFilter: any;
  tableDataLoaded = false;
  adminAccess = false; // check for admin access
  showDownloadBtn = true;
  showFilterBtn = true;
  selectedRowId: string;
  private assetGroupSubscription: Subscription;
  private domainSubscription: Subscription;
  private routeSubscription: Subscription;
  private complianceDropdownSubscription: Subscription;
  private issueListingSubscription: Subscription;
  private issueFilterSubscription: Subscription;
  public pageLevel = 0;
  public backButtonRequired;
  public doNotDisplaySearch=true;
  filterErrorMessage = '';
  tableTitle = "Violations";
  tableErrorMessage = '';
  errorMessage = '';
  headerColName: string;
  direction;
  tableScrollTop=0;
  onScrollDataLoader: Subject<any> = new Subject<any>();
  columnWidths = { 'Policy': 2, 'Violation ID': 1, 'Asset ID': 1, 'Asset Type': 0.5, 'Source': 0.4, 'Account Name': 0.7, 'Region': 0.7, 'Severity': 0.5, 'Category':0.5, 'Status': 0.5};
  centeredColumns = {
    Policy: false,
    'Violation ID': false,
    'Asset ID': false,
    Severity: true,
    Category: true,
  };
  columnNamesMap = {"PolicyName": "Policy","IssueId":"Violation ID", "Asset Type":"resourcetype", "AccountName": "Account Name", "source": "Source"};
  fieldName: string = "severity.keyword";
  fieldType: string = "number";
  selectedOrder: string = "desc";
  sortOrder: string[] = ["low", "medium", "high", "critical"];
  tableImageDataMap = {
      security:{
          image: "category-security",
          imageOnly: true
      },
      operations:{
          image: "category-operations",
          imageOnly: true
      },
      cost:{
          image: "category-cost",
          imageOnly: true
      },
      tagging:{
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
  whiteListColumns;
  displayedColumns;
  tableData = [];
  isStatePreserved = false;
  columnsToExcludeFromCasing = ["Account Name"];
  filterOrder: any;

  constructor(
    private assetGroupObservableService: AssetGroupObservableService,
    private domainObservableService: DomainTypeObservableService,
    private activatedRoute: ActivatedRoute,
    private issueFilterService: IssueFilterService,
    private router: Router,
    private utils: UtilsService,
    private logger: LoggerService,
    private commonResponseService: CommonResponseService,
    private errorHandling: ErrorHandlingService,
    private refactorFieldsService: RefactorFieldsService,
    private downloadService: DownloadService,
    private workflowService: WorkflowService,
    private routerUtilityService: RouterUtilityService,
    private permissions: PermissionGuardService,
    private tableStateService: TableStateService,
    private assetTypeMapService: AssetTypeMapService,
    private filterManagementService: FilterManagementService
  ) { }

  getUpdatedColumnWidthsAndNamesMap(){
    const excludedColumnNames = new Set(["Exempted", "Tagged"]);
    this.filterTypeLabels.forEach(label => {
        if (!excludedColumnNames.has(label)) {
            if (!Object.keys(this.columnWidths).includes(label)) {
                this.columnWidths[label] = 0.7;
            }

            const columnName = Object.entries(this.columnNamesMap)
                .find(([_, value]) => value === label)?.[0];

            if (!columnName) {
                const apiColName = this.filterTypeOptions
                    .find(option => option.optionName === label)?.optionValue;

                if (apiColName) {
                    this.columnNamesMap[apiColName.replace(".keyword", "")] = label;
                }
            }
        }
    });

    this.columnNamesMap = { ...this.columnNamesMap };
    this.columnWidths = { ...this.columnWidths };
  }

  getPreservedState() {
    const state = this.tableStateService.getState(this.saveStateKey) ?? {};
  
    this.headerColName = state.headerColName || 'Severity';
    this.direction = state.direction || 'desc';
    this.bucketNumber = state.bucketNumber || 0;
    this.totalRows = state.totalRows || 0;
    this.searchTxt = state.searchTxt || '';
    this.tableDataLoaded = true;
    this.displayedColumns = ['Policy', 'Asset ID', 'Severity', 'Category'];
    this.whiteListColumns = state.whiteListColumns || this.displayedColumns;
    this.tableScrollTop = state.tableScrollTop;
    this.selectedRowId = state.selectedRowId;
    

    this.applyPreservedFilters(state);
  }
  
  applyPreservedFilters (state) {
    // below code is to apply filters which are in saved state and update the URL (just to keep URL in sync and also getData method is dependent) 
    // overriding of filters is being done in routerParam() method which basically updates filterText
    // based on filterQueryParams and later when getFilterArray is called, they are added to filters array.

    // Note: getData method does not depend on filters array directly
    // but rather it depends on filterText object which is build on URL queryParams.

    this.isStatePreserved = false;

    //  below code should run when filter is not available in queryParams
    const updateInfo = this.filterManagementService.applyPreservedFilters(state);
    if (updateInfo.shouldUpdateFilters) {
      this.filters = state.filters || [];
      this.filterText = updateInfo.filterText;
    }
    if (updateInfo.preApply) {
      this.preApplyStatusFilter(state);
    }
    if (updateInfo.shouldUpdateData) {
      this.isStatePreserved = true;
      this.tableData = state.data || [];
    }
  }

  ngOnInit () {
    this.assetGroupSubscription = this.assetGroupObservableService
    .getAssetGroup()
    .subscribe(async (assetGroupName) => {
      // whenever ag is changed, all filters and data are cleared in change-default-asset-group component.
      this.getPreservedState();
      this.backButtonRequired =
        this.workflowService.checkIfFlowExistsCurrently(this.pageLevel);
      this.selectedAssetGroup = assetGroupName;
      await this.getFilters();
      this.getUpdatedColumnWidthsAndNamesMap();
      this.storeState();
    });

    this.domainSubscription = this.domainObservableService
    .getDomainType()
    .subscribe((domain) => {
      this.selectedDomain = domain;
    });
    
    const breadcrumbInfo = this.workflowService.getDetailsFromStorage()["level0"];

    if(breadcrumbInfo){
      this.breadcrumbArray = breadcrumbInfo.map(item => item.policy);
      this.breadcrumbLinks = breadcrumbInfo.map(item => item.url);
    }

    // check for admin access
    this.adminAccess = this.permissions.checkAdminPermission();

    window.onbeforeunload = () => this.storeState();
  }

  preApplyStatusFilter(state){
    const isStateFiltersArray = Array.isArray(this.filters);
    const statusFilterExists = isStateFiltersArray ? this.filters.some(item => item.keyDisplayValue === "Status") : false;

    if (!statusFilterExists) {
      this.filters = isStateFiltersArray ? this.filters : [];
      this.filters.push({
        "keyDisplayValue": "Status",
        "filterValue": ["open"],
        "key": "Status",
        "value": ["open"],
        "filterkey": "issueStatus.keyword",
        "compareKey": "issuestatus.keyword"
      });
    }
  }

  handlePaginatorSizeSelection(event) {
    this.paginatorSize = event;
    this.getData();
  }

  updateSortFieldName(){
    const sortColName = this.headerColName.toLowerCase();
    this.selectedOrder = this.direction;
    this.sortOrder = null;
    if (sortColName === "severity") {
      this.fieldName = "severity.keyword";
      this.fieldType = "number";
      this.sortOrder = ["low", "medium", "high", "critical"]
    } else if (sortColName === "violation id") {
      this.fieldName = "_id";
      this.fieldType = "string";
    } else if (sortColName === "asset id") {
      this.fieldName = "_resourceid.keyword";
      this.fieldType = "string";
    } else if (sortColName === "category") {
      this.fieldName = "policyCategory.keyword";
      this.fieldType = "number";
      this.sortOrder = ["tagging", "cost", "operations", "security"]
    } else if (sortColName === "policy") {
      this.fieldType = "number";
      this.fieldName = "policyId.keyword";
    } else if (sortColName === "asset type") {
      this.fieldType = "number";
      this.fieldName = "resourcetType.keyword";
    } else{
      try{
        let apiColName =  find(this.filterTypeOptions, {
          optionName: this.headerColName,
        })["optionValue"];
        this.fieldType = "string";
        this.fieldName = apiColName;
      }catch(e){
        this.logger.log("error", e);
      }
    }
  }

  handleHeaderColNameSelection(event: any) {
    this.headerColName = event.headerColName;
    this.direction = event.direction;

    this.bucketNumber = 0;

    this.updateComponent();
  }

  handleWhitelistColumnsChange(event){
    this.whiteListColumns = event;
  }

  storeState(){
    const state = {
        totalRows: this.totalRows,
        data: this.tableData,
        headerColName: this.headerColName,
        direction: this.direction,
        whiteListColumns: this.whiteListColumns,
        bucketNumber: this.bucketNumber,
        searchTxt: this.searchTxt,
        tableScrollTop: this.tableScrollTop,
        filters: this.filters,
        selectedRowId: this.selectedRowId,
        // filterText: this.filterText
      }
    this.tableStateService.setState(this.saveStateKey, state);
  }

  clearState(){
    // this.tableStateService.clearState(this.pageTitle);
    this.isStatePreserved = false;
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
        this.FullQueryParams = currentQueryParams;
        this.queryParamsWithoutFilter = JSON.parse(
          JSON.stringify(this.FullQueryParams)
        );
        delete this.queryParamsWithoutFilter["filter"];
        /**
         * The below code is added to get URLparameter and queryparameter
         * when the page loads ,only then this function runs and hits the api with the
         * filterText obj processed through processFilterObj function
         */
        this.filterText = this.utils.processFilterObj(this.FullQueryParams);
      }
    } catch (error) {
      this.errorMessage = this.errorHandling.handleJavascriptError(error);
      this.logger.log("error", error);
    }
  }

  // TODO: getting order from url might sometimes lead to inconsistency because
  // the filter might be present (say at 2nd position) with 0 selected but since it
  // is 0 selected, it might not be present in the URL.
  // Thus, filter order cannot be derived from url.
  getFiltersAppliedOrderFromURL(filterString){
    try{
      if(filterString){
        // Split the string by '**'
        const splitByDoubleAsterisk = filterString.split('**');

        // Initialize an array to hold the keys
        const keys = [];

        // Iterate through the substrings obtained from the split
        splitByDoubleAsterisk.forEach(substring => {
          // Split each substring by '=' to get key-value pairs
          const keyValuePair = substring.split('=');
          if (keyValuePair.length === 2) {
            // Extract the key from the key-value pair and add it to the keys array
            keys.push(decodeURIComponent(keyValuePair[0]).replace(".keyword", ""));
          }
        });

        this.filterOrder = keys;
      }
    }catch(e){
      this.logger.log("jsError",e);
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
    try {
      if (!event) {
        this.filters = [];
      } else if (event.removeOnlyFilterValue) {
        this.removeFiltersOnRightOfIndex(event.index);
        this.getUpdatedUrl();
        this.updateComponent();
      } else if (event.index !== undefined && !this.filters[event.index].filterValue) {
        this.filters.splice(event.index, 1);
      } else {
        if (!event.clearAll) {
          this.filters.splice(event.index, 1);
        } else {
          this.filters = [];
        }
        this.getUpdatedUrl();
        this.updateComponent();
      }
  
      
    } catch (error) { }
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
    const existingFilterObjIndex = filters.findIndex(filter => filter.keyDisplayValue === keyDisplayValue || filter.keyDisplayValue === filterKey);
    let filterObj;

    if (existingFilterObjIndex < 0) {
      if (!keyDisplayValue) {
        const validFilterValues = this.filterText[filterKey]?.split(',').map(value => {
          return { id: value, name: value };
        })
        filterObj = this.filterManagementService.createFilterObj(filterKey, filterKey, validFilterValues);
      } else {
        // we make API call by calling changeFilterType mathod to fetch filter options and their display names for a filterKey
        await this.changeFilterType(keyDisplayValue);
        const validFilterValues = this.filterManagementService.getValidFilterValues(keyDisplayValue, filterKey, this.filterText, this.filterTagOptions, this.filterTagLabels);
        filterObj = this.filterManagementService.createFilterObj(keyDisplayValue, filterKey, validFilterValues);
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

  async getFilters() {
    try {
      this.issueFilterSubscription = this.issueFilterService
        .getFilters(
          { filterId: 1, domain: this.selectedDomain },
          environment.issueFilter.url,
          environment.issueFilter.method
        )
        .subscribe(async(response) => {
          this.filterTypeLabels = map(response[0].response, "optionName");
          this.filterTypeOptions = response[0].response;
          this.filterTypeOptions.forEach(item => {            
            if(item.optionValue.includes("tags")){
              this.columnsToExcludeFromCasing.push(item.optionName);
            }
          });

          this.filterTypeLabels.sort();
          if(this.filterTypeLabels.length==0){
            this.filterErrorMessage = 'noDataAvailable';
          }
          this.getUpdatedColumnWidthsAndNamesMap();
          this.routerParam();
          // this.deleteFilters();
          await this.getFilterArray();
          await Promise.resolve().then(() => this.getUpdatedUrl());
          this.updateComponent();
        },
        error => {
          this.tableErrorMessage = 'apiResponseError';
          this.logger.log("error", error);
        });
    } catch (error) {
      this.tableErrorMessage = 'jsError';
      this.errorMessage = this.errorHandling.handleJavascriptError(error);
      this.logger.log("error", error);
    }
  }
  
  async changeFilterType (value, searchText = '') {
    try {
      const currentQueryParams =
        this.routerUtilityService.getQueryParametersFromSnapshot(
          this.router.routerState.snapshot.root
        );
      this.currentFilterType = find(this.filterTypeOptions, { optionName: value });
      const filtersToBePassed = this.getFilterPayloadForDataAPI();
      const filterText = this.filterText;
      const currentFilterType = this.currentFilterType;
      const [updateFilterTags, labelsToExcludeSort] = this.getUpdateFilterTagsCallback();
      const agAndDomain = {
        ag: this.selectedAssetGroup,
        domain: this.selectedDomain
      }

      const [filterTagOptions, filterTagLabels] = await this.filterManagementService.changeFilterType({ currentFilterType, filterText, filtersToBePassed, type: 'issue', currentQueryParams, agAndDomain, searchText, updateFilterTags, labelsToExcludeSort });
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
      if (value.toLowerCase() === "asset type") {
        this.assetTypeMapService.getAssetMap().subscribe(assetTypeMap => {
          filterTagsData.forEach(filterOption => {
            filterOption["name"] = assetTypeMap.get(filterOption["name"]?.toLowerCase()) || filterOption["name"]
          });
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
    this.currentFilterType = find(this.filterTypeOptions, {
      optionName: event.filterKeyDisplayValue,
    });
    this.filters = this.filterManagementService.changeFilterTags(this.filters, this.filterTagOptions, this.currentFilterType, event);
    this.getUpdatedUrl();
    this.updateComponent();
  }

  removeFiltersOnRightOfIndex(index: number){
    for(let i=index+1; i<this.filters.length && i>0; i++){
      this.filters[i].filterValue = [];
      this.filters[i].value = [];
    }
    this.filters = [...this.filters];
  }

  updateComponent() {
    this.updateSortFieldName();
    if(this.isStatePreserved){
      this.tableDataLoaded = true;
      this.clearState();
    }else{
      this.tableDataLoaded = false;
      this.bucketNumber = 0;
      this.tableData = [];
      this.getData();
    }
  }

  navigateBack() {
    try {
      this.workflowService.goBackToLastOpenedPageAndUpdateLevel(
        this.router.routerState.snapshot.root
      );
    } catch (error) {
      this.logger.log("error", error);
    }
  }

  processData(data) {
    try {
      const processedData = [];
      
      for (const row of data) {
          const innerArr = {};
          for (const col in row) {
              const cellData = row[col];
              const tableImageData = this.tableImageDataMap[typeof cellData === "string" ? cellData.toLowerCase() : cellData];
              
              const cellObj = {
                  text: tableImageData?.imageOnly ? "" : cellData,
                  titleText: cellData,
                  valueText: cellData,
                  hasPostImage: false,
                  imgSrc: tableImageData?.image || "",
                  postImgSrc: "",
                  isChip: "",
                  isMenuBtn: false,
                  properties: "",
                  isLink: col.toLowerCase() === "policy",
                  imageTitleText: ""
              };

              innerArr[col] = cellObj;
          }
          
          processedData.push(innerArr);
      }
      return processedData;
    } catch (error) {
        this.errorMessage = this.errorHandling.handleJavascriptError(error);
        this.logger.log("error", error);
        return [];
    }
  }

  getFilterPayloadForDataAPI(){
    const filterToBePassed = { ...this.filterText };
      filterToBePassed.domain = this.selectedDomain;

      Object.keys(filterToBePassed).forEach(filterKey => {
        if (filterKey !== "domain") {
          if(filterToBePassed[filterKey]?.length>0){
            filterToBePassed[filterKey] = filterToBePassed[filterKey]?.split(",") || [];
          }else{
            delete filterToBePassed[filterKey];
          }
        }
      });

      return filterToBePassed;
  }

  getData(isNextPageCalled=false) {
    try {
      if (this.issueListingSubscription) {
        this.issueListingSubscription.unsubscribe();
      }
      
      const filtersToBePassed = this.getFilterPayloadForDataAPI();

      const sortFilters = {
        fieldName: this.fieldName,
        fieldType: this.fieldType,
        order: this.selectedOrder,
        sortOrder: this.sortOrder
      }

      const payload = {
        ag: this.selectedAssetGroup,
        filter: filtersToBePassed,
        sortFilter: sortFilters,
        from: this.bucketNumber * this.paginatorSize,
        searchtext: this.searchTxt,
        size: this.paginatorSize,
      };
      const issueListingUrl = environment.issueListing.url;
      const issueListingMethod = environment.issueListing.method;
      this.issueListingSubscription = this.commonResponseService
        .getData(issueListingUrl, issueListingMethod, payload, {})
        .subscribe(
          async(response) => {
            try {
              this.tableErrorMessage = '';
              if (!isNextPageCalled) {
                this.tableData = [];
              }
              
              this.tableDataLoaded = true;
              const data = response.data;
              
              if (data.response.length === 0) {
                this.tableErrorMessage = 'noDataAvailable';
                this.totalRows = 0;
              }
              

              this.totalRows = data.total;
              
              const updatedResponse = await this.massageData(data.response);
              
              const processData = this.processData(updatedResponse);
              
              if (isNextPageCalled) {
                this.onScrollDataLoader.next(processData);
              } else {
                this.tableData = processData;
              }
            } catch (e) {
              this.tableErrorMessage = !isNextPageCalled?'jsError':'';
              this.logger.log("error", e);
            }
          },
          (error) => {
            this.tableDataLoaded = true;
            this.tableErrorMessage = !isNextPageCalled?'apiResponseError':'';
            this.logger.log("error", error);
          }
        );
    } catch (error) {
      this.tableErrorMessage = !isNextPageCalled?'jsError':'';
      this.logger.log("error", error);
    }
  }

  async massageData(data) {
    const refactoredService = this.refactorFieldsService;
    const columnNamesMap = this.columnNamesMap;
    let assetTypeMapData;
    this.assetTypeMapService.getAssetMap().subscribe(assetTypeMap=>{
      assetTypeMapData = assetTypeMap;
    });
    const newData = [];

    data.forEach((row) => {
        const keysToBeChanged = Object.keys(row);

        const newObj = {};

        keysToBeChanged.forEach((element) => {
            let elementNew;

            if (columnNamesMap[element]) {
                elementNew = columnNamesMap[element];
            } else {
                elementNew = refactoredService.getDisplayNameForAKey(element.toLowerCase()) || element;
            }

            let newDataValue = DATA_MAPPING[typeof row[element] === "string" ? row[element].toLowerCase() : row[element]];

            if (newDataValue === undefined) {
                newDataValue = row[element];
            }

            if (elementNew === 'Asset Type') {
              newDataValue = assetTypeMapData.get(newDataValue);
            }
            newObj[elementNew] = newDataValue;
        });

        newData.push(newObj);
    });
    return newData;
  }

  goToDetails(event) {
    const row = event.rowSelected;
    this.tableScrollTop = event.tableScrollTop;
    this.selectedRowId = event.selectedRowId;

    try {
      this.workflowService.addRouterSnapshotToLevel(
        this.router.routerState.snapshot.root, 0, this.pageTitle
      );
      this.router
          .navigate(["issue-details", row["Violation ID"].valueText], {
            relativeTo: this.activatedRoute,
            queryParamsHandling: "merge",
          })
          .then((response) => {
            this.logger.log(
              "info",
              "Successfully navigated to issue details page: " + response
            );
          })
          .catch((error) => {
            this.logger.log("error", "Error in navigation - " + error);
          });
    } catch (error) {
      this.errorMessage = this.errorHandling.handleJavascriptError(error);
      this.logger.log("error", error);
    }
  }

  handlePopClick(e) {
    const fileType = "csv";

    try {
      let queryParams;

      queryParams = {
        fileFormat: "csv",
        serviceId: 1,
        fileType: fileType,
      };

      const sortFilters = {
        fieldName: this.fieldName,
        fieldType: this.fieldType,
        order: this.selectedOrder,
        sortOrder: this.sortOrder
      }

      const filtersToBePassed = this.getFilterPayloadForDataAPI();
      filtersToBePassed.domain = this.selectedDomain;

      const downloadRequest = {
        ag: this.selectedAssetGroup,
        filter: filtersToBePassed,
        sortFilter: sortFilters,
        from: 0,
        searchtext: this.searchTxt,
        size: this.totalRows,
      };

      const downloadUrl = environment.download.url;
      const downloadMethod = environment.download.method;

      this.downloadService.requestForDownload(
        queryParams,
        downloadUrl,
        downloadMethod,
        downloadRequest,
        "Policy Violations",
        this.totalRows
      );
    } catch (error) {
      this.logger.log("error", error);
    }
  }


  nextPg(e) {    
    try {
      this.tableScrollTop = e;
        this.bucketNumber++;
        
        this.getData(true);
    } catch (error) {
      this.errorMessage = this.errorHandling.handleJavascriptError(error);
      this.logger.log("error", error);
    }
  }

  callNewSearch(searchVal){
    this.searchTxt = searchVal;
    // this.searchValue = searchVal;
    
    this.isStatePreserved = false;
    this.updateComponent();
    // this.getUpdatedUrl();
  }

  handleFilterSearchTextChange(event){
    if(event.selectedFilterCategory=="Violation ID") this.changeFilterType(event.selectedFilterCategory, event.searchText);
  }


  ngOnDestroy() {
    try {
      this.storeState();
      if (this.assetGroupSubscription) {
        this.assetGroupSubscription.unsubscribe();
      }
      if (this.domainSubscription) {
        this.domainSubscription.unsubscribe();
      }
      if (this.routeSubscription) {
        this.routeSubscription.unsubscribe();
      }
      if (this.complianceDropdownSubscription) {
        this.complianceDropdownSubscription.unsubscribe();
      }
      if (this.issueListingSubscription) {
        this.issueListingSubscription.unsubscribe();
      }
      if (this.issueFilterSubscription) {
        this.issueFilterSubscription.unsubscribe();
      }
    } catch (error) {
      this.logger.log("error", "--- Error while unsubscribing ---");
    }
  }
}
