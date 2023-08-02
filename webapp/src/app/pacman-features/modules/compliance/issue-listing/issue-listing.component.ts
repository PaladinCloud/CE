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

@Component({
  selector: "app-issue-listing",
  templateUrl: "./issue-listing.component.html",
  styleUrls: ["./issue-listing.component.css"],
  providers: [IssueFilterService, LoggerService, ErrorHandlingService],
})
export class IssueListingComponent implements OnInit, OnDestroy {
  pageTitle = "Violations";
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
  selectedRowIndex;
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
  columnWidths = {'Policy': 2, 'Violation ID': 1, 'Asset ID': 1, 'Asset Type': 0.5, 'Account Name': 0.7, 'Region': 0.7, 'Severity': 0.5, 'Category':0.5, 'Status': 0.5};
  centeredColumns = {
    Policy: false,
    'Violation ID': false,
    'Asset ID': false,
    Severity: true,
    Category: true,
  };
  columnNamesMap = {"PolicyName": "Policy","IssueId":"Violation ID", "Asset Type":"resourcetype", "AccountName": "Account Name"};
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
  columnsToExcludeFromCasing = [];

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
    private assetTypeMapService: AssetTypeMapService
  ) {

    this.assetGroupSubscription = this.assetGroupObservableService
    .getAssetGroup()
    .subscribe((assetGroupName) => {
        this.getPreservedState();
        this.backButtonRequired =
          this.workflowService.checkIfFlowExistsCurrently(this.pageLevel);
        this.selectedAssetGroup = assetGroupName;
        this.getFilters().then(() => {
          this.filterTypeLabels.forEach(label => {
            if(label=="Exempted" || label=="Tagged"){
              return;
            }
            if(!Object.keys(this.columnWidths).includes(label)){
              this.columnWidths[label] = 0.7;
            }
            if(!Object.values(this.columnNamesMap).includes(label)){
              const apiColName =  find(this.filterTypeOptions, {
                optionName: label,
              })["optionValue"];
              if(apiColName) this.columnNamesMap[apiColName.replace(".keyword", "")] = label;
            }
          })
          this.columnNamesMap = {...this.columnNamesMap};
          this.columnWidths = {...this.columnWidths};

        });
      });

    this.domainSubscription = this.domainObservableService
      .getDomainType()
      .subscribe((domain) => {
        this.selectedDomain = domain;
      });
  }

  getPreservedState(){
    const state = this.tableStateService.getState(this.pageTitle) ?? {};
    if(state){
      this.headerColName = state.headerColName ?? 'Severity';
      this.direction = state.direction ?? 'desc';
      this.bucketNumber = state.bucketNumber ?? 0;
      this.totalRows = state.totalRows ?? 0;
      this.searchTxt = state?.searchTxt ?? '';

      this.tableDataLoaded = true;

      this.tableData = state?.data ?? [];
      this.displayedColumns = ['Policy', 'Asset ID', 'Severity', 'Category'];
      this.whiteListColumns = state?.whiteListColumns ?? this.displayedColumns;
      this.tableScrollTop = state?.tableScrollTop;
      this.selectedRowIndex = state?.selectedRowIndex;
      // this.filterText = state.filterText??"";

      if(this.tableData && this.tableData.length>0){
        this.isStatePreserved = true;
      }else{
        this.isStatePreserved = false;
      }

      const isStateFiltersArray = Array.isArray(state.filters);
      const statusFilterExists = isStateFiltersArray ? state.filters.some(item => item.keyDisplayValue === "Status") : false;

      if (!statusFilterExists) {
        state.filters = isStateFiltersArray ? state.filters : [];
        state.filters.push({
          "keyDisplayValue": "Status",
          "filterValue": ["Open", "Exempt"],
          "key": "Status",
          "value": ["open", "exempt"],
          "filterkey": "issueStatus.keyword",
          "compareKey": "issuestatus.keyword"
        });
      }

      const isTempFilter = this.activatedRoute.snapshot.queryParamMap.get("tempFilters");

      if (!isTempFilter && state.filters) {
        this.filters = state.filters;
        Promise.resolve().then(() => this.getUpdatedUrl());
      }

    }
  }

  ngOnInit() {
    const breadcrumbInfo = this.workflowService.getDetailsFromStorage()["level0"];

    if(breadcrumbInfo){
      this.breadcrumbArray = breadcrumbInfo.map(item => item.policy);
      this.breadcrumbLinks = breadcrumbInfo.map(item => item.url);
    }

    // check for admin access
    this.adminAccess = this.permissions.checkAdminPermission();
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
    }else if (sortColName === "asset type") {
      this.fieldType = "number";
      this.fieldName = "resourcetType.keyword";
    }else{
      let apiColName:any = Object.keys(this.columnNamesMap).find(col => col==this.headerColName);
      if(!apiColName){
        apiColName =  find(this.filterTypeOptions, {
          optionName: this.headerColName,
        })["optionValue"];
      }
      this.fieldType = "string";
      this.fieldName = apiColName;
    }
  }

  handleHeaderColNameSelection(event: any) {
    this.headerColName = event.headerColName;
    this.direction = event.direction;

    this.bucketNumber = 0;

    this.storeState();
    this.updateComponent();
  }

  handleWhitelistColumnsChange(event){
    this.whiteListColumns = event;
    this.storeState();
  }

  storeState(data?){
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
        selectedRowIndex: this.selectedRowIndex,
        // filterText: this.filterText
      }
    this.tableStateService.setState(this.pageTitle, state);
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
  
      this.storeState();
    } catch (error) { }
  }
  
  /*
   * this functin passes query params to filter component to show filter
   */
  async getFilterArray() {
    try {
      const dataArray = Object.keys(this.filterText).map(filterKey => {
        const keyDisplayValue = this.filterTypeOptions.find(option => option.optionValue === filterKey)?.optionName;
        return {
          keyDisplayValue,
          filterkey: filterKey,
        };
      });
      const state = this.tableStateService.getState(this.pageTitle) ?? {};
      const filters = state?.filters;

      if (filters) {
        const dataArrayFilterKeys = new Set(dataArray.map(obj => obj.keyDisplayValue));
        filters.forEach(filter => {
          if (!dataArrayFilterKeys.has(filter.keyDisplayValue)) {
            dataArray.push({
              filterkey: filter.filterkey,
              keyDisplayValue: filter.key,
            });
          }
        });
      }
      const formattedFilters = dataArray;
      for (let i = 0; i < formattedFilters.length; i++) {
        await this.processFilterItem(formattedFilters[i]);
      }
    } catch (error) {
      this.errorMessage = this.errorHandling.handleJavascriptError(error);
      this.logger.log("error", error);
    }
  }

  async processFilterItem(formattedFilterItem){

    let keyDisplayValue = formattedFilterItem.keyDisplayValue;
    if(!keyDisplayValue){
      keyDisplayValue = find(this.filterTypeOptions, {
        optionValue: formattedFilterItem.filterkey,
      })["optionName"];
    }

    await this.changeFilterType(keyDisplayValue);
    const filterKey = formattedFilterItem.filterkey;
    const filterValues = this.filterText[filterKey]?.split(',') || [];
    const filterTagOptionsForKey = this.filterTagOptions[keyDisplayValue];
    const filterTagLabelsForKey = this.filterTagLabels[keyDisplayValue];

    const validFilterValues = filterValues
      .map(val => filterTagOptionsForKey?.find(obj => obj.id === val))
      .filter(valObj => valObj && filterTagLabelsForKey?.includes(valObj.name));

    if (validFilterValues.length > 0) {
      const existingFilterObj = this.filters.find(filter => filter.keyDisplayValue === keyDisplayValue);

      if (!existingFilterObj) {
        const eachObj = {
          keyDisplayValue: keyDisplayValue,
          filterValue: validFilterValues.map(valObj => valObj.name),
          key: keyDisplayValue,
          value: validFilterValues.map(valObj => valObj.id),
          filterkey: filterKey?.trim(),
          compareKey: filterKey?.toLowerCase().trim(),
        };
        this.filters.push(eachObj);
      } else {
        existingFilterObj.value = validFilterValues.map(valObj => valObj.id);
        existingFilterObj.filterValue = validFilterValues.map(valObj => valObj.name);
      }

      this.filters = [...this.filters];
      this.storeState();
    }
  }

  /**
   * This function get calls the keyword service before initializing
   * the filter array ,so that filter keynames are changed
   */

  getFilters() {
    return new Promise((resolve) => {
      this.filterErrorMessage = '';
    let isApiError = true;
    try {
      this.issueFilterSubscription = this.issueFilterService
        .getFilters(
          { filterId: 1, domain: this.selectedDomain },
          environment.issueFilter.url,
          environment.issueFilter.method
        )
        .subscribe((response) => {
          this.filterTypeLabels = map(response[0].response, "optionName");
          resolve(true);
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
          isApiError = false;
          this.routerParam();
          // this.deleteFilters();
          this.getFilterArray();
          this.updateComponent();
        });
    } catch (error) {
      this.filterErrorMessage = 'apiResponseError';
      this.errorMessage = this.errorHandling.handleJavascriptError(error);
      this.logger.log("error", error);
      resolve(false);
    }
    if(isApiError) this.filterErrorMessage = 'apiResponseError';
    });
  }

  async applyFilterTagsData(filterTagsData, value) {
    if (value.toLowerCase() === "asset type") {
      this.assetTypeMapService.getAssetMap().subscribe(assetTypeMap=>{
        filterTagsData.forEach(filterOption => {
          filterOption["name"] = assetTypeMap.get(filterOption["name"]?.toLowerCase()) || filterOption["name"]
        });
      });
    }
  
    this.filterTagOptions[value] = filterTagsData;
    this.filterTagLabels[value] = map(filterTagsData, 'name').sort((a, b) => a.localeCompare(b));
  
    if (this.filterTagLabels[value].length === 0) {
      this.filterErrorMessage = 'noDataAvailable';
    }
  
    this.storeState();
    return this.filterTagOptions[value];
  }
  
  async getFilterTagsData(payload) {
    return this.issueFilterService.getFilters({}, environment.base + this.utils.getParamsFromUrlSnippet(this.currentFilterType.optionURL).url, "POST", payload)
      .toPromise()
      .then(response => response[0].data.response);
  }
  
  async changeFilterType(value) {
    this.filterErrorMessage = '';
  
    try {
      this.currentFilterType = find(this.filterTypeOptions, { optionName: value });
      const urlObj = this.utils.getParamsFromUrlSnippet(this.currentFilterType.optionURL);
  
      const excludedKeys = [
        this.currentFilterType.optionValue,
        "domain",
        "include_exempt",
        urlObj.params["attribute"],
      ];
  
      const excludedKeysInUrl = Object.keys(this.filterText).filter(key => urlObj.url.includes(key));
  
      const filtersToBePassed = Object.keys(this.filterText).reduce((result, key) => {
        const normalizedKey = key.replace(".keyword", "");
        if (!excludedKeys.includes(normalizedKey) && !excludedKeysInUrl.includes(normalizedKey)) {
          result[normalizedKey] = this.filterText[key].split(",");
        }
        return result;
      }, {});
  
      const payload = {
        type: "issue",
        attributeName: this.currentFilterType["optionValue"]?.replace(".keyword", ""),
        ag: this.selectedAssetGroup,
        domain: this.selectedDomain,
        filter: filtersToBePassed,
      };
  
      const filterTagsData = await this.getFilterTagsData(payload);
      await this.applyFilterTagsData(filterTagsData, value);
  
    } catch (error) {
      this.filterErrorMessage = 'apiResponseError';
      this.errorMessage = this.errorHandling.handleJavascriptError(error);
      this.logger.log("error", error);
    }
  }
  

  changeFilterTags(event) {
    let filterValues = event.filterValue;
    if(!filterValues){
      return;
    }
    this.currentFilterType =  find(this.filterTypeOptions, {
        optionName: event.filterKeyDisplayValue,
      });

    try {
      if (this.currentFilterType) {
        const filterTags = filterValues.map(value => {
          const v = find(this.filterTagOptions[event.filterKeyDisplayValue], { name: value })["id"];
          return v;
        });
        this.utils.addOrReplaceElement(
          this.filters,
          {
            keyDisplayValue: event.filterKeyDisplayValue,
            filterValue: filterValues,
            key: this.currentFilterType.optionName,
            value: filterTags,
            filterkey: this.currentFilterType.optionValue.trim(),
            compareKey: this.currentFilterType.optionValue.toLowerCase().trim(),
          },
          (el) => {
            return (
              el.compareKey ===
              this.currentFilterType.optionValue.toLowerCase().trim()
            );
          }
        );
      }
      this.storeState();
      this.getUpdatedUrl();
      this.utils.clickClearDropdown();
      this.updateComponent();
    } catch (error) {
      this.errorMessage = this.errorHandling.handleJavascriptError(error);
      this.logger.log("error", error);
    }
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

  getData(isNextPageCalled=false) {
    try {
      if (this.issueListingSubscription) {
        this.issueListingSubscription.unsubscribe();
      }
      const filterToBePassed = { ...this.filterText };
      filterToBePassed.domain = this.selectedDomain;

      Object.keys(filterToBePassed).forEach(filterKey => {
        if (filterKey !== "domain") {
          filterToBePassed[filterKey] = filterToBePassed[filterKey]?.split(",") || [];
        }
      });

      const sortFilters = {
        fieldName: this.fieldName,
        fieldType: this.fieldType,
        order: this.selectedOrder,
        sortOrder: this.sortOrder
      }

      const payload = {
        ag: this.selectedAssetGroup,
        filter: filterToBePassed,
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

  refreshDataTable($event) {
    this.updateComponent();
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
    this.selectedRowIndex = event.selectedRowIndex;
    this.storeState(event.data);

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

      const filterToBePassed = this.filterText;
      filterToBePassed.domain = this.selectedDomain;

      const downloadRequest = {
        ag: this.selectedAssetGroup,
        filter: filterToBePassed,
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
        this.storeState();
        this.getData(true);
    } catch (error) {
      this.errorMessage = this.errorHandling.handleJavascriptError(error);
      this.logger.log("error", error);
    }
  }

  callNewSearch(searchVal){
    this.searchTxt = searchVal;
    // this.searchValue = searchVal;
    this.storeState();
    this.isStatePreserved = false;
    this.updateComponent();
    // this.getUpdatedUrl();
  }


  ngOnDestroy() {
    try {
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
