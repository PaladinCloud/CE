import { Component, OnInit, OnDestroy } from "@angular/core";
import { environment } from "./../../../../../environments/environment";
import { AssetGroupObservableService } from "../../../../core/services/asset-group-observable.service";
import { ActivatedRoute, Router } from "@angular/router";
import { Subject, Subscription } from "rxjs";
import { IssueListingService } from "../../../services/issue-listing.service";
import { IssueFilterService } from "../../../services/issue-filter.service";
import find from "lodash/find";
import map from "lodash/map";
import { UtilsService } from "../../../../shared/services/utils.service";
import { LoggerService } from "../../../../shared/services/logger.service";
import { ErrorHandlingService } from "../../../../shared/services/error-handling.service";
import { ToastObservableService } from "../../../../post-login-app/common/services/toast-observable.service";
import { DownloadService } from "../../../../shared/services/download.service";
import { RefactorFieldsService } from "./../../../../shared/services/refactor-fields.service";
import { WorkflowService } from "../../../../core/services/workflow.service";
import { DomainTypeObservableService } from "../../../../core/services/domain-type-observable.service";
import { RouterUtilityService } from "../../../../shared/services/router-utility.service";
import { TableStateService } from "src/app/core/services/table-state.service";
import { DATA_MAPPING } from "src/app/shared/constants/data-mapping";
import { AssetTypeMapService } from "src/app/core/services/asset-type-map.service";
import { ComponentKeys } from "src/app/shared/constants/component-keys";
import { FilterManagementService } from "src/app/shared/services/filter-management.service";
import { IColumnNamesMap, IColumnWidthsMap } from "src/app/shared/table/interfaces/table-props.interface";

@Component({
  selector: "app-asset-list",
  templateUrl: "./asset-list.component.html",
  styleUrls: ["./asset-list.component.css"],
  providers: [
    IssueListingService,
    IssueFilterService,
    LoggerService,
    ErrorHandlingService,
  ],
})
export class AssetListComponent implements OnInit, OnDestroy {
  pageTitle = "Asset List";
  saveStateKey: String = ComponentKeys.AssetList;
  assetListData: any;
  selectedAssetGroup: string;
  breadcrumbArray: any = [];
  breadcrumbLinks: any = [];
  errorMessage: any;
  allColumns: any = [];
  totalRows = 0;
  bucketNumber = 0;
  paginatorSize = 100;
  searchTxt = "";
  filterTypeOptions: any = [];
  filterTagOptions: any = {};
  currentFilterType;
  filterTypeLabels = [];
  filterTagLabels = {};
  tableDataLoaded = false;
  filters: any = [];
  searchCriteria: any;
  filterText: any = {};
  dataTableDesc = "";
  urlID = "";
  public labels: any;
  FullQueryParams: any;
  queryParamsWithoutFilter: any;
  private previousUrl: any = "";
  selectedDomain: any = "";
  urlToRedirect: any = "";
  serviceId = 7;
  tableDownloadName = "All Assets";
  private pageLevel = 0;
  public backButtonRequired;
  mandatory: any;
  showDownloadBtn = true;
  showFilterBtn = true;
  tableTitle = "Asset list";
  tableErrorMessage = '';
  headerColName;
  direction;
  tableScrollTop=0;
  selectedRowIndex;
  onScrollDataLoader: Subject<any> = new Subject<any>();
  columnWidths: IColumnWidthsMap = {'Asset ID': 2, 'Asset Name': 1, 'Asset Type': 0.7, 'Account ID':1, 'Account Name': 1, 'Region': 0.5, 'Source': 0.5};
  columnNamesMap: IColumnNamesMap = {
    targettypedisplayname: 'Asset Type',
    _entitytype: 'assetTypeValue',
    _cloudType: 'Source'
  };
  columnsSortFunctionMap = {
    Severity: (a, b, isAsc) => {
      let severeness = {"low":1, "medium":2, "high":3, "critical":4, "default": 5 * (isAsc ? 1 : -1)}

      const ASeverity = a["Severity"].valueText??"default";
      const BSeverity = b["Severity"].valueText??"default";
      return (severeness[ASeverity] < severeness[BSeverity] ? -1 : 1) * (isAsc ? 1 : -1);
    },
  };
  columnsToExcludeFromCasing = [];
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

  assetTypeMap: any;
  selectedOrder: any = 'asc';
  sortOrder: any ;
  fieldName: string = '_resourceid.keyword';
  fieldType: string = 'string';

  isMultiValuedFilterEnabled = true;

  private assetGroupSubscription: Subscription;
  private routeSubscription: Subscription;
  private complianceDropdownSubscription: Subscription;
  private issueListingSubscription: Subscription;
  private issueFilterSubscription: Subscription;
  private previousUrlSubscription: Subscription;
  private subscriptionDomain: Subscription;

  constructor(
    private assetGroupObservableService: AssetGroupObservableService,
    private activatedRoute: ActivatedRoute,
    private issueListingService: IssueListingService,
    private issueFilterService: IssueFilterService,
    private filterManagementService: FilterManagementService,
    private router: Router,
    private utils: UtilsService,
    private logger: LoggerService,
    private errorHandling: ErrorHandlingService,
    private downloadService: DownloadService,
    private refactorFieldsService: RefactorFieldsService,
    private workflowService: WorkflowService,
    private domainObservableService: DomainTypeObservableService,
    private routerUtilityService: RouterUtilityService,
    private tableStateService: TableStateService,
    private assetTypeMapService:AssetTypeMapService
  ) {
    this.assetGroupSubscription = this.assetGroupObservableService
    .getAssetGroup()
    .subscribe(async(assetGroupName) => {
      await this.getPreservedState();
      this.backButtonRequired =
      this.workflowService.checkIfFlowExistsCurrently(this.pageLevel);
      this.selectedAssetGroup = assetGroupName;
      const currentQueryParams =
        this.routerUtilityService.getQueryParametersFromSnapshot(
          this.router.routerState.snapshot.root
        );
      this.urlID = currentQueryParams.TypeAsset;
      this.getFilters();
    });

    this.assetTypeMapService.getAssetMap().subscribe(assetTypeMap=>{
      this.assetTypeMap = assetTypeMap;
    });
    this.subscriptionDomain = this.domainObservableService
    .getDomainType()
    .subscribe((domain) => {
      this.selectedDomain = domain;
    });
  }

  async getPreservedState(){
    const state = this.tableStateService.getState(this.saveStateKey) || {};
    this.headerColName = state.headerColName ?? 'Asset ID';
    this.direction = state.direction ?? 'asc';
    this.bucketNumber = state.bucketNumber ?? 0;
    this.totalRows = state.totalRows ?? 0;
    this.searchTxt = state?.searchTxt ?? '';
    this.selectedRowIndex = state?.selectedRowIndex;

    this.tableDataLoaded = true;
    this.displayedColumns = ['Asset ID', 'Asset Name', 'Asset Type', 'Account ID', 'Account Name', 'Region', 'Source'];
    this.whiteListColumns = state?.whiteListColumns ?? this.displayedColumns;
    this.tableScrollTop = state?.tableScrollTop;

    this.isStatePreserved = false;
    const navDirection = this.workflowService.getNavigationDirection();

    if(navDirection<=0){
      this.filters = state.filters || [];
      if (state.data && state.data.length > 0) {
        this.isStatePreserved = true;
        this.tableData = state.data || [];
      }
      await Promise.resolve().then(() => this.getUpdatedUrl());
    }    
    
  }

  ngOnInit() {
    this.urlToRedirect = this.router.routerState.snapshot.url;
    const breadcrumbInfo = this.workflowService.getDetailsFromStorage()["level0"];

    if(breadcrumbInfo){
      this.breadcrumbArray = breadcrumbInfo.map(item => item.title);
      this.breadcrumbLinks = breadcrumbInfo.map(item => item.url);
    }

    window.onbeforeunload = () => this.storeState();
  }

  handleAddFilterClick(e){}

  handleHeaderColNameSelection(event: any) {
    this.headerColName = event.headerColName;
    this.direction = event.direction;

    this.bucketNumber = 0;

    
    this.updateComponent();
  }

  updateSortFieldName() {
    try {
      this.selectedOrder = this.direction;
      const sortColName = this.headerColName.toLowerCase();
      this.sortOrder = null;
      if (this.selectedAssetGroup?.toLowerCase() == "azure" && sortColName === "asset id") {
        this.fieldName = "assetIdDisplayName.keyword";
        this.fieldType = "string";
      } else if (sortColName === "asset type") {
        this.fieldName = "_entitytype.keyword";
        this.fieldType = "string";
      } else {
        let apiColName: any = Object.keys(this.columnNamesMap).find(col => this.columnNamesMap[col] == this.headerColName);
        if (!apiColName) {
          apiColName = find(this.filterTypeOptions, {
            optionName: this.headerColName,
          })["optionValue"];
        } else {
          apiColName = apiColName + ".keyword";
        }
        this.fieldType = "string";
        this.fieldName = apiColName;
      }
    } catch (e) {
      this.errorHandling.handleJavascriptError(e);
      this.headerColName = '';
    }
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
      filterText: this.filterText,
      selectedRowIndex: this.selectedRowIndex
    }
    this.tableStateService.setState(this.saveStateKey, state);
  }

  clearState(){
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
        this.urlID = this.FullQueryParams.TypeAsset;
        // check for mandatory filters.
        if (this.FullQueryParams.mandatory) {
          this.mandatory = this.FullQueryParams.mandatory;
        }
      }
    } catch (error) {
      this.errorMessage = this.errorHandling.handleJavascriptError(error);
      this.logger.log("error", error);
    }
  }

  deleteFilters(event?) {
    let shouldUpdateComponent = false;
    [this.filters, shouldUpdateComponent] = this.filterManagementService.deleteFilters(event, this.filters);      
    if(shouldUpdateComponent){
        this.getUpdatedUrl();
        this.updateComponent();
    }
  }

  /*
   * this function passes query params to filter component to show filter
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

    } catch (error) {
      this.errorMessage = this.errorHandling.handleJavascriptError(error);
      this.logger.log("error", error);
    }
  }

  async processAndAddFilterItem({formattedFilterItem, filters}){

    const keyDisplayValue = this.utils.getFilterKeyDisplayValue(formattedFilterItem, this.filterTypeOptions);
    const filterKey = formattedFilterItem.filterkey;
    const existingFilterObjIndex = filters.findIndex(filter => filter.keyDisplayValue === keyDisplayValue || filter.keyDisplayValue === filterKey);
    let filterObj;
      
    if(existingFilterObjIndex<0){
      if(!keyDisplayValue){
        const validFilterValues = this.filterText[filterKey]?.split(',').map(value => {
          return {id: value, name:value};
        })
        filterObj = this.filterManagementService.createFilterObj(filterKey, filterKey, validFilterValues);
      } else{
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

  getData(isNextPageCalled=false) {
    try {
      let queryParams;
      let assetListUrl;
      let assetListMethod;

      assetListUrl = environment.assetList.url;
      assetListMethod = environment.assetList.method;
      this.serviceId = 7;
      this.tableDownloadName = "All Assets";
      this.filterText["domain"] = this.selectedDomain;

      const sortFilter = {
        fieldName: this.fieldName,
        fieldType: this.fieldType,
        order: this.selectedOrder,
        sortOrder: this.sortOrder
      }

      if(this.isMultiValuedFilterEnabled){
        const filtersToBePassed = this.getFilterPayloadForDataAPI();
        queryParams = {
          ag: this.selectedAssetGroup,
          reqFilter: filtersToBePassed,
          sortFilter: sortFilter,
          from: this.bucketNumber * this.paginatorSize,
          searchtext: this.searchTxt,
          size: this.paginatorSize,
        };
      }else{
        queryParams = {
          ag: this.selectedAssetGroup,
          filter: this.filterText,
          sortFilter: sortFilter,
          from: this.bucketNumber * this.paginatorSize,
          searchtext: this.searchTxt,
          size: this.paginatorSize,
        };
      }

      this.getDataForAParticularTypeOfAssets(
        queryParams,
        assetListUrl,
        assetListMethod,
        isNextPageCalled
      );
    } catch (error) {
      this.errorMessage = this.errorHandling.handleJavascriptError(error);
      this.logger.log("error", error);
    }
  }

  getFilterPayloadForDataAPI(){
    const filterToBePassed = {...this.filterText};
    Object.keys(filterToBePassed).forEach(filterKey => {
      if(filterKey=="domain") return;
      filterToBePassed[filterKey] = filterToBePassed[filterKey].split(",");
    })

    return filterToBePassed;
  }

  processData(data) {
    try {
      return this.utils.processTableData(data, {}, (row, col, cellObj) => {
        if(col.toLowerCase()=="asset id"){
          const displayValue = row["assetIdDisplayName"];
          cellObj = {
            ...cellObj,
            text: displayValue?displayValue:row[col],
            titleText: displayValue?displayValue:row[col], // text to show on hover
            valueText:  row[col],
            isLink: true
          };            
        }
        
        return cellObj;
      });
      
    } catch (error) {
      this.errorMessage = this.errorHandling.handleJavascriptError(error);
      this.logger.log("error", error);
    }
  }

  getDataForAParticularTypeOfAssets(
    queryParams,
    assetListUrl,
    assetListMethod,
    isNextPageCalled=false
  ) {
    if (this.issueListingSubscription) {
      this.issueListingSubscription.unsubscribe();
    }
    this.tableErrorMessage = '';
    this.issueListingSubscription = this.issueListingService
      .getData(queryParams, assetListUrl, assetListMethod)
      .subscribe(
        (response) => {
          try {
            this.searchCriteria = undefined;
            const data = response[0];
            this.tableDataLoaded = true;
            if (response[0].response.length === 0 && !isNextPageCalled) {
              this.allColumns = [];
              this.totalRows = 0;
              this.tableErrorMessage = 'noDataAvailable'
            }
            this.assetListData = data.response;
            this.totalRows = data.total;

            const updatedResponse = this.massageData(this.assetListData);
            const processedData = this.processData(updatedResponse);
            if(isNextPageCalled){
                this.onScrollDataLoader.next(processedData)
            }else{
              this.tableData = processedData;
            }
          } catch (e) {
            this.tableDataLoaded = true;
            this.tableErrorMessage = this.errorHandling.handleJavascriptError(e);
          }
        },
        (error) => {
          this.tableDataLoaded = true;
          this.tableErrorMessage = "apiResponseError";
        }
      );
  }
  massageData(data){
    const refactoredService = this.refactorFieldsService;
    const columnNamesMap = this.columnNamesMap;    
    const newData = [];
    data.forEach((row) => {
      const KeysTobeChanged = Object.keys(row);
      let newObj = {};
      KeysTobeChanged.forEach((element) => {
        let elementnew;
        const isTag = element.split(".")[0]=="tags";
        if(isTag){
          columnNamesMap[element] = element.split(".")[1];
        }
        if(columnNamesMap[element]) {
          elementnew = columnNamesMap[element];
          newObj = Object.assign(newObj, { [elementnew]: row[element] });
        }
        else {
        elementnew =
          refactoredService.getDisplayNameForAKey(
            element.toLocaleLowerCase()
          ) || element;
          newObj = Object.assign(newObj, { [elementnew]: row[element] });
        }
        // change data value
        newObj[elementnew] = DATA_MAPPING[typeof newObj[elementnew]=="string"?newObj[elementnew].toLowerCase():newObj[elementnew]]?DATA_MAPPING[newObj[elementnew].toLowerCase()]: newObj[elementnew];
      });
      newData.push(newObj);
    });
    
    this.columnNamesMap = columnNamesMap;
    return newData;
  }

  goToDetails(event) {

    const row = event.rowSelected;
    this.tableScrollTop = event.tableScrollTop;
    this.selectedRowIndex = event.selectedRowIndex;
    try {
      this.workflowService.addRouterSnapshotToLevel(
        this.router.routerState.snapshot.root, 0, this.pageTitle
      );
      const resourceType = row?.assetTypeValue.valueText;      
      

      const resourceID = encodeURIComponent(row["Asset ID"].valueText);
      let updatedQueryParams = {...this.activatedRoute.snapshot.queryParams};
      // updatedQueryParams["searchValue"] = undefined;
      this.router.navigate([resourceType, resourceID], {
        relativeTo: this.activatedRoute,
        queryParams: updatedQueryParams,
        queryParamsHandling: "merge",
      });
    } catch (error) {
      this.errorMessage = this.errorHandling.handleJavascriptError(error);
      this.logger.log("error", error);
    }
  }

  prevPg() {
    try {
      this.bucketNumber--;
      this.getData();
      this.getUpdatedUrl();
    } catch (error) {
      this.errorMessage = this.errorHandling.handleJavascriptError(error);
      this.logger.log("error", error);
    }
  }

  nextPg(e) {
    try {
      this.tableScrollTop = e;
      if(this.bucketNumber*this.paginatorSize<this.totalRows){
        this.bucketNumber++;
        
        this.getData(true);
      }
    } catch (error) {
      this.errorMessage = this.errorHandling.handleJavascriptError(error);
      this.logger.log("error", error);
    }
  }

  handleFilterSearchTextChange(event){
    if(event.selectedFilterCategory=="Asset ID") this.changeFilterType(event.selectedFilterCategory, event.searchText);
  }

  handlePopClick(rowText) {
    const fileType = "csv";

    try {
      let queryParams;

      queryParams = {
        fileFormat: "csv",
        serviceId: this.serviceId,
        fileType: fileType,
      };

      const sortFilter = {
        fieldName: this.fieldName,
        fieldType: this.fieldType,
        order: this.selectedOrder,
        sortOrder: this.sortOrder
      }

      // temp code to send download domain filters only for dev page assets landing
      this.filterText["domain"] = this.selectedDomain;

      let filtersToBePassed = {...this.filterText};

      if(this.isMultiValuedFilterEnabled){
        filtersToBePassed = this.getFilterPayloadForDataAPI();
      }
      
      const downloadRequest = {
        ag: this.selectedAssetGroup,
        reqFilter: filtersToBePassed,
        sortFilter: sortFilter,
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
        this.tableDownloadName,
        this.totalRows
      );
    } catch (error) {
      this.logger.log("error", error);
    }
  }

  callNewSearch(searchVal){
    this.searchTxt = searchVal;
    // this.state.searchValue = searchVal;
    
    this.updateComponent();
    // this.getUpdatedUrl();
  }

  trimStringsInArrayOfObjs(arrayOfObj){
    arrayOfObj.forEach(element => {
      let keys = Object.keys(element);
      keys.forEach(key => {
        element[key] = element[key].trim();
      })
    });
  }

  /**
   * This function get calls the keyword service before initializing
   * the filter array ,so that filter keynames are changed
   */

  getFilters() {
      try {
        let filterId = 8;
        if (
          this.urlID &&
          (this.urlID.toLowerCase() === "pull-request-trend" ||
            this.urlID.toLowerCase() === "pull-request-age" ||
            this.urlID.toLowerCase() === "branching-strategy")
        ) {
          filterId = 9;
        }
        this.issueFilterSubscription = this.issueFilterService
          .getFilters(
            { filterId: filterId, domain: this.selectedDomain },
            environment.issueFilter.url,
            environment.issueFilter.method
          )
          .subscribe(async(response) => {            
            response[0].response.forEach(item => {
              if(item.optionValue.includes("tags.")){
                this.columnsToExcludeFromCasing.push(item.optionName);
              }
              item.optionValue = item.optionValue.includes("resourceType") ? "_entitytype.keyword" : item.optionValue;
            });
            
            this.filterTypeOptions = response[0].response;
            this.trimStringsInArrayOfObjs(this.filterTypeOptions);
            let filterTypeLabels = this.filterTypeOptions.map(labelObj => labelObj.optionName)

            filterTypeLabels.sort();

            this.filterTypeLabels = filterTypeLabels;
            [this.columnNamesMap, this.columnWidths] = this.utils.getColumnNamesMapAndColumnWidthsMap(this.filterTypeLabels, this.filterTypeOptions, this.columnWidths, this.columnNamesMap, ['Exempted', 'Tagged', 'Policy ID', 'Compliant']);
            this.routerParam();
            await this.getFilterArray();
            await Promise.resolve().then(() => this.getUpdatedUrl());
            this.updateComponent();
          });
      } catch (error) {
        this.errorMessage = this.errorHandling.handleJavascriptError(error);
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
      const [updateFilterTags, labelsToExcludeSort] = this.getUpdateFilterTagsCallback();
      const agAndDomain = {
        ag: this.selectedAssetGroup,
        domain: this.selectedDomain
      }

      const [filterTagOptions, filterTagLabels] = await this.filterManagementService.changeFilterType({currentFilterType, filterText, filtersToBePassed, type:'asset', currentQueryParams, agAndDomain, searchText, updateFilterTags, labelsToExcludeSort});
      this.filterTagOptions[value] = filterTagOptions;
      this.filterTagLabels[value] = filterTagLabels;

      this.filterTagLabels = {...this.filterTagLabels};          

  
    } catch (error) {
      this.errorMessage = this.errorHandling.handleJavascriptError(error);
      this.logger.log("error", error);
    }
  }

  getUpdateFilterTagsCallback(){
    const labelsToExcludeSort = [];
    const updateFilterTags = (filterTagsData, value) => {      
      if (value.toLowerCase() === "asset type") {
        this.assetTypeMapService.getAssetMap().subscribe(assetTypeMap=>{
          filterTagsData.forEach(filterOption => {
              filterOption["name"] = assetTypeMap.get(filterOption["name"]?.toLowerCase()) || filterOption["name"]
          });
        });
      }
      return filterTagsData;
    }
    return [updateFilterTags, labelsToExcludeSort];
  }
  changeFilterTags(event) {
    let filterValues = event.filterValue;
    if(!filterValues){
      return;
    }
    this.currentFilterType =  find(this.filterTypeOptions, {
      optionName: event.filterKeyDisplayValue,
    });
    this.filters = this.filterManagementService.changeFilterTags(this.filters, this.filterTagOptions, this.currentFilterType, event);
    this.getUpdatedUrl();
    this.updateComponent();
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
  navigateToCreate() {
    this.router.navigateByUrl("../assets/asset-list/create-account");
  }
  ngOnDestroy() {
    try {
      this.storeState();
      if (this.assetGroupSubscription) {
        this.assetGroupSubscription.unsubscribe();
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
      if (this.previousUrlSubscription) {
        this.previousUrlSubscription.unsubscribe();
      }
      if (this.subscriptionDomain) {
        this.subscriptionDomain.unsubscribe();
      }
      if (this.issueFilterSubscription) {
        this.issueFilterSubscription.unsubscribe();
      }
    } catch (error) {
      this.logger.log("error", "--- Error while unsubscribing ---");
    }
  }
}
