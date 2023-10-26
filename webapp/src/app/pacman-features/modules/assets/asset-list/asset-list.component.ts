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
  columnWidths = {'Asset ID': 2, 'Asset Name': 1, 'Asset Type': 0.7, 'Account ID':1, 'Account Name': 1, 'Region': 0.5, 'Source': 0.5};
  columnNamesMap = {
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
    .subscribe((assetGroupName) => {
      this.getPreservedState();
      this.backButtonRequired =
      this.workflowService.checkIfFlowExistsCurrently(this.pageLevel);
      this.selectedAssetGroup = assetGroupName;
      // this.updateComponent();
      const currentQueryParams =
        this.routerUtilityService.getQueryParametersFromSnapshot(
          this.router.routerState.snapshot.root
        );
      this.urlID = currentQueryParams.TypeAsset;
      if(this.urlID?.toLowerCase().includes("scanned")){
        this.isMultiValuedFilterEnabled = false;
        this.showFilterBtn = false;
      }
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
            if(apiColName && this.columnNamesMap[apiColName.replace(".keyword","")]==undefined) {
              this.columnNamesMap[apiColName.replace(".keyword", "")] = label;
            }
          }  
        })
        this.columnNamesMap = {...this.columnNamesMap};
        this.columnWidths = {...this.columnWidths}

      });
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

  getPreservedState(){
    const state = this.tableStateService.getState(this.saveStateKey) || {};
    this.headerColName = state.headerColName ?? 'Asset ID';
    this.direction = state.direction ?? 'asc';
    this.bucketNumber = state.bucketNumber ?? 0;
    this.totalRows = state.totalRows ?? 0;
    this.searchTxt = state?.searchTxt ?? '';
    this.selectedRowIndex = state?.selectedRowIndex;

    this.tableData = state?.data ?? [];
    this.tableDataLoaded = true;
    this.displayedColumns = ['Asset ID', 'Asset Name', 'Asset Type', 'Account ID', 'Account Name', 'Region', 'Source'];
    this.whiteListColumns = state?.whiteListColumns ?? this.displayedColumns;
    this.tableScrollTop = state?.tableScrollTop;

    if(state.data){
      this.isStatePreserved = true;
    }else{
      this.isStatePreserved = false;
    }
    const isTempFilter = this.activatedRoute.snapshot.queryParamMap.get("tempFilters");
    if((!isTempFilter || isTempFilter=="false") && (state.filters || state.filterText)){
      this.filters = state.filters || [];
      Promise.resolve().then(() => this.getUpdatedUrl());
    }else{
      this.isStatePreserved = false;
    }

    if(this.isStatePreserved){
      this.tableData = state.data || [];
    }
  }

  ngOnInit() {
    this.urlToRedirect = this.router.routerState.snapshot.url;
    const breadcrumbInfo = this.workflowService.getDetailsFromStorage()["level0"];

    if(breadcrumbInfo){
      this.breadcrumbArray = breadcrumbInfo.map(item => item.title);
      this.breadcrumbLinks = breadcrumbInfo.map(item => item.url);
    }
  }

  handleAddFilterClick(e){}

  handleHeaderColNameSelection(event: any) {
    this.headerColName = event.headerColName;
    this.direction = event.direction;

    this.bucketNumber = 0;

    this.storeState();
    this.updateComponent();
  }

  updateSortFieldName() {
    this.selectedOrder = this.direction;
    const sortColName = this.headerColName.toLowerCase();
    this.sortOrder = null;
    if(this.selectedAssetGroup?.toLowerCase()=="azure" && sortColName==="asset id"){
      this.fieldName = "assetIdDisplayName.keyword";
      this.fieldType = "string";
    } else if (sortColName === "asset type") {
      this.fieldName = "_entitytype.keyword";
      this.fieldType = "string";
    } else{
      let apiColName:any = Object.keys(this.columnNamesMap).find(col => this.columnNamesMap[col]==this.headerColName);
      if(!apiColName){
        apiColName =  find(this.filterTypeOptions, {
          optionName: this.headerColName,
        })["optionValue"];
      }else{
        apiColName = apiColName+".keyword";
      }
      this.fieldType = "string";
      this.fieldName = apiColName;
    }
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
      filterText: this.filterText,
      selectedRowIndex: this.selectedRowIndex
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
    try {
      if (!event) {
        this.filters = [];
        this.storeState();
      } else if(event.removeOnlyFilterValue){
        this.getUpdatedUrl();
        this.updateComponent();
        this.storeState();
      } else if(event.index && !this.filters[event.index].filterValue){
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
    } catch (error) { }
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
        const keyDisplayValue = find(this.filterTypeOptions, {
          optionValue: filterObjKeys[i],
        })["optionName"];
        obj = {
          keyDisplayValue,
          filterkey: filterObjKeys[i],
        };
        dataArray.push(obj);
      }

      const state = this.tableStateService.getState(this.saveStateKey) ?? {};
      const filters = state?.filters;

      if(filters){
        const dataArrayFilterKeys = dataArray.map(obj => obj.keyDisplayValue);
        filters.forEach(filter => {
          if(!dataArrayFilterKeys.includes(filter.keyDisplayValue)){
            dataArray.push({
              filterkey: filter.filterkey,
              keyDisplayValue: filter.key
            });
          }
        });
      }

      const formattedFilters = dataArray;
      for (let i = 0; i < formattedFilters.length; i++) {

        let keyDisplayValue = formattedFilters[i].keyDisplayValue;
        if(!keyDisplayValue){
          keyDisplayValue = find(this.filterTypeOptions, {
            optionValue: formattedFilters[i].filterKey,
          })["optionName"];
        }

        this.changeFilterType(keyDisplayValue).then(() => {
          const filterKey = dataArray[i].filterkey;
          
          const filterValueObj = this.filterText[filterKey]?.split(',').map(val => {
            const valObj:any = find(this.filterTagOptions[keyDisplayValue], {
              id: val,
            });
            return valObj?.name;
          });
                    
          if(!this.filters.find(filter => filter.keyDisplayValue==keyDisplayValue)){
            if(filterValueObj){
              const eachObj = {
                keyDisplayValue: keyDisplayValue,
                filterValue: filterValueObj??undefined,
                key: keyDisplayValue, // <-- displayKey-- Resource Type
                value: this.filterText[filterKey], // <<-- value to be shown in the filter UI-- S2
                filterkey: filterKey?.trim(), // <<-- filter key that to be passed -- "resourceType "
                compareKey: filterKey?.toLowerCase().trim(), // <<-- key to compare whether a key is already present -- "resourcetype"
              };
              this.filters.push(eachObj);
            }
            this.filters = [...this.filters];
            this.storeState();
          }
        })
      }
    } catch (error) {
      this.errorMessage = this.errorHandling.handleJavascriptError(error);
      this.logger.log("error", error);
    }
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

      if (this.urlID) {
        if (this.urlID.toLowerCase() === "exempted") {
          this.dataTableDesc = "Note: This page shows all the exempt assets";
          // the url and method for exempted << -- defines url and method
          assetListUrl = environment.assetListExempted.url;
          assetListMethod = environment.assetListExempted.method;
          this.serviceId = 15;
          this.tableDownloadName = "Exempt Assets";
        } else if (this.urlID.toLowerCase() === "taggable") {
          this.dataTableDesc = "Note: This page shows all the taggable assets";
          // the url and method for tagging << -- defines url and method
          assetListUrl = environment.assetListTaggable.url;
          assetListMethod = environment.assetListTaggable.method;
          this.serviceId = 10;
          this.tableDownloadName = "Taggable Assets";
        } else if (this.urlID.toLowerCase() === "patchable") {
          this.dataTableDesc = "Note: This page shows all the patchable assets";
          // patchable  asset list api
          // the url and method for patching << -- defines url and method
          assetListUrl = environment.assetListPatchable.url;
          assetListMethod = environment.assetListPatchable.method;
          this.serviceId = 8;
          this.tableDownloadName = "Patchable Assets";
          this.filterText["resourceType"] = "ec2";
        } else if (this.urlID.toLowerCase() === "scanned") {
          this.dataTableDesc = "Note: This page shows all the scanned assets";
          // patchable  asset list api
          // the url and method for patching << -- defines url and method
          assetListUrl = environment.assetListScanned.url;
          assetListMethod = environment.assetListScanned.method;
          this.isMultiValuedFilterEnabled = false;
          this.serviceId = 9;
          this.tableDownloadName = "Scanned Assets";
        } else if (this.urlID.toLowerCase() === "vulnerable") {
          this.dataTableDesc =
            "Note: This page shows all the vulnerable assets";
          // vulnerable  asset list api
          // the url and method for patching << -- defines url and method
          assetListUrl = environment.assetListVulnerable.url;
          assetListMethod = environment.assetListVulnerable.method;
          this.serviceId = 11;
          this.tableDownloadName = "Vulnerable Assets";
        } else if (this.urlID.toLowerCase() === "pull-request-trend") {
          if (this.filterText.prstate) {
            this.dataTableDesc =
              "Note: This page shows the " +
              this.filterText.prstate.toString().toLowerCase() +
              " pull request trend";
          } else {
            this.dataTableDesc = "Note: This page shows the pull request trend";
          }
          // vulnerable  asset list api
          // the url and method for patching << -- defines url and method
          assetListUrl = environment.PullReqLineTrend.url;
          assetListMethod = environment.PullReqLineTrend.method;
          this.serviceId = 12;
          this.tableDownloadName = "Pull Request Trend";
        } else if (this.urlID.toLowerCase() === "pull-request-age") {
          if (this.filterText.daysRange) {
            this.dataTableDesc =
              "Note: This page shows the pull request age (" +
              this.filterText.daysRange.toString().toLowerCase() +
              " days)";
          } else {
            this.dataTableDesc = "Note: This page shows the pull request age";
          }
          assetListUrl = environment.PullReqAge.url;
          assetListMethod = environment.PullReqAge.method;
          this.serviceId = 13;
          this.tableDownloadName = "Pull Request Age";
        } else if (this.urlID.toLowerCase() === "branching-strategy") {
          if (this.filterText.strategyType) {
            this.dataTableDesc =
              "Note: This page shows the" +
              " '" +
              this.filterText.strategyType.toString().toLowerCase() +
              "' " +
              this.filterText.branchingStrategyType.toString().toLowerCase() +
              " " +
              "distribution by branching strategies";
          } else {
            this.dataTableDesc =
              "Note: This page shows the" +
              " " +
              this.filterText.branchingStrategyType.toString().toLowerCase() +
              " " +
              "distribution by branching strategies";
          }
          assetListUrl = environment.devDistribution.url;
          assetListMethod = environment.devDistribution.method;
          this.serviceId = 14;
          this.tableDownloadName =
            this.filterText.branchingStrategyType.toString() +
            " " +
            "Distribution";
        } else {
          assetListUrl = environment.assetList.url;
          assetListMethod = environment.assetList.method;
          this.serviceId = 7;
          this.tableDownloadName = "All Assets";
          this.filterText["domain"] = this.selectedDomain;
        }
      } else {
        assetListUrl = environment.assetList.url;
        assetListMethod = environment.assetList.method;
        this.filterText["domain"] = this.selectedDomain;
      }

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
      var innerArr = {};
      var totalVariablesObj = {};
      var cellObj = {};
      let processedData = [];
      var getData = data;
      
      let cellData;
      for (var row = 0; row < getData.length; row++) {
        const keynames = Object.keys(getData[row]);
        innerArr = {};
        keynames.forEach(col => {
          cellData = getData[row][col];
          cellObj = {
            text: this.tableImageDataMap[typeof cellData == "string"?cellData.toLowerCase(): cellData]?.imageOnly?"":cellData, // text to be shown in table cell
            titleText: cellData, // text to show on hover
            valueText: cellData,
            hasPostImage: false,
            imgSrc: this.tableImageDataMap[typeof cellData == "string"?cellData.toLowerCase(): cellData]?.image,  // if imageSrc is not empty and text is also not empty then this image comes before text otherwise if imageSrc is not empty and text is empty then only this image is rendered,
            postImgSrc: "",
            isChip: "",
            isMenuBtn: false,
            properties: "",
            isLink: false
          }
          if(col.toLowerCase()=="asset id"){
            const displayValue = getData[row]["assetIdDisplayName"];
            cellObj = {
              ...cellObj,
              text: displayValue?displayValue:cellData,
              titleText:  cellData, // text to show on hover
              valueText:  cellData,
              isLink: true
            };            
          } 
          innerArr[col] = cellObj;
          totalVariablesObj[col] = "";
        });
        processedData.push(innerArr);
      }
      return processedData;
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
    this.storeState(event.data);
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
        this.storeState();
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

      if (
        this.urlID &&
        (this.urlID.toLowerCase() === "taggable" ||
          this.urlID.toLowerCase() === "patchable" ||
          this.urlID.toLowerCase() === "scanned" ||
          this.urlID.toLowerCase() === "vulnerable")
      ) {
        // this.filterText['domain'] = this.selectedDomain;
      } else {
        this.filterText["domain"] = this.selectedDomain;
      }

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
    this.storeState();
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
    return new Promise((resolve) => {
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
          .subscribe((response) => {            
            response[0].response.forEach(item => {
              if(item.optionValue.includes("tags.")){
                this.columnsToExcludeFromCasing.push(item.optionName);
              }
              item.optionValue = item.optionValue.includes("resourceType") ? "_entitytype.keyword" : item.optionValue;
            });
            
            this.filterTypeOptions = response[0].response;
            resolve(true);
            this.trimStringsInArrayOfObjs(this.filterTypeOptions);
            this.filterTypeLabels = map(this.filterTypeOptions, "optionName");

            this.filterTypeLabels.sort();

            this.routerParam();
            // this.deleteFilters();
            this.getFilterArray();
            this.updateComponent();
          });
      } catch (error) {
        this.errorMessage = this.errorHandling.handleJavascriptError(error);
        this.logger.log("error", error);
        resolve(false);
      }
    })
  }

  changeFilterType(value, searchText=''){
    return new Promise((resolve) => {
    try {
      this.currentFilterType = find(this.filterTypeOptions, {
        optionName: value,
      });
      const urlObj = this.utils.getParamsFromUrlSnippet(this.currentFilterType.optionURL);

      const excludedKeys = [
        this.currentFilterType.optionValue,
        "domain",
        "include_exempt",
        urlObj.params["attribute"],
        this.currentFilterType["optionValue"]?.replace(".keyword", "")
      ];
      
      const excludedKeysInUrl = Object.keys(this.filterText).filter(key => urlObj.url.includes(key));

      if(urlObj.url.includes("attribute") || value=="Exempted" || value=="Tagged"){
      let filtersToBePassed = this.getFilterPayloadForDataAPI();
      filtersToBePassed = Object.keys(filtersToBePassed).reduce((result, key) => {
        const normalizedKey = key.replace(".keyword", "");
        if ((!excludedKeys.includes(normalizedKey) && !excludedKeysInUrl.includes(normalizedKey))) {
          result[normalizedKey] = filtersToBePassed[key];
        }
        return result;
      }, {});
      
      const payload = {
        type: "asset",
        attributeName: this.currentFilterType["optionValue"]?.replace(".keyword", ""),
        ag: this.selectedAssetGroup,
        domain: this.selectedDomain,
        filter: filtersToBePassed,
        searchText
      }
        this.issueFilterSubscription = this.issueFilterService
        .getFilters(
          {},
          environment.base +
          this.utils.getParamsFromUrlSnippet(this.currentFilterType.optionURL)
            .url,
          "POST",
          payload
        )
        .subscribe((response) => {
          const filterTagsData = response[0].data.response;          
          if(value.toLowerCase()=="asset type"){
            this.assetTypeMapService.getAssetMap().subscribe(assetTypeMap=>{
              filterTagsData.forEach(filterOption => {
                filterOption["name"] = assetTypeMap.get(filterOption["name"]?.toLowerCase()) || filterOption["name"]
              });
            });
          }
          this.filterTagOptions[value] = filterTagsData;
          this.filterTagLabels = {
              ...this.filterTagLabels,
              ...{
                  [value]: map(filterTagsData, 'name').sort((a, b) =>
                      a.localeCompare(b),
                  ),
              },
          };
          resolve(this.filterTagOptions[value]);
          this.storeState();
        });
      }else{
        const queryParams = {
          ...urlObj.params,
          ag: this.selectedAssetGroup,
          domain: this.selectedDomain,
        }
        this.issueFilterSubscription = this.issueFilterService
      .getFilters(
        queryParams,
        environment.base +
        urlObj.url,
        "GET"
      )
      .subscribe((response) => {
        if(value.toLowerCase()=="asset type"){
          this.assetTypeMapService.getAssetMap().subscribe(assetTypeMap=>{
            response[0].response.map(filterOption => filterOption["name"] = assetTypeMap.get(filterOption["name"]));
          });
        }
        this.filterTagOptions[value] = response[0].response;
        this.filterTagLabels = {
            ...this.filterTagLabels,
            ...{
                [value]: map(response[0].response, 'name').sort((a, b) =>
                    a.localeCompare(b),
                ),
            },
        };
        resolve(this.filterTagOptions[value]);
        this.storeState();
      });
      }
      } catch (error) {
      this.errorMessage = this.errorHandling.handleJavascriptError(error);
      this.logger.log("error", error);
    }
    });
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
          const v = find(this.filterTagOptions[event.filterKeyDisplayValue], { name: value });
          return v?v["id"]:value;
        });
        this.utils.addOrReplaceElement(
          this.filters,
          {
            keyDisplayValue: event.filterKeyDisplayValue,
            filterValue: filterValues,
            key: this.currentFilterType.optionName,
            value: this.isMultiValuedFilterEnabled?filterTags:filterValues,
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
