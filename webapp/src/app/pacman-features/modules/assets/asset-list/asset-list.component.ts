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

import { Component, OnInit, OnDestroy } from "@angular/core";
import { environment } from "./../../../../../environments/environment";
import { AssetGroupObservableService } from "../../../../core/services/asset-group-observable.service";
import { ActivatedRoute, Router } from "@angular/router";
import { Subject, Subscription } from "rxjs";
import { IssueListingService } from "../../../services/issue-listing.service";
import { IssueFilterService } from "../../../services/issue-filter.service";
import * as _ from "lodash";
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
  assetListData: any;
  selectedAssetGroup: string;
  breadcrumbArray: any = [];
  breadcrumbLinks: any = [];
  breadcrumbPresent: any;
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
  onScrollDataLoader: Subject<any> = new Subject<any>();
  columnWidths = {'Resource ID': 2, 'Asset Type': 1, 'Account ID':1, 'Account Name': 1, 'Region': 1, 'Cloud Type': 1};
  columnNamesMap = {};
  columnsSortFunctionMap = {
    Severity: (a, b, isAsc) => {
      let severeness = {"low":1, "medium":2, "high":3, "critical":4}
      return (severeness[a["Severity"]] < severeness[b["Severity"]] ? -1 : 1) * (isAsc ? 1 : -1);
    },
  };
  tableImageDataMap = {
      security:{
          image: "category-security",
          imageOnly: true
      },
      governance:{
          image: "category-operations",
          imageOnly: true
      },
      operations:{
          image: "category-operations",
          imageOnly: true
      },
      costOptimization:{
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

  private assetGroupSubscription: Subscription;
  private routeSubscription: Subscription;
  private complianceDropdownSubscription: Subscription;
  private issueListingSubscription: Subscription;
  private issueFilterSubscription: Subscription;
  private previousUrlSubscription: Subscription;
  private downloadSubscription: Subscription;
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
    private toastObservableService: ToastObservableService,
    private refactorFieldsService: RefactorFieldsService,
    private workflowService: WorkflowService,
    private domainObservableService: DomainTypeObservableService,
    private routerUtilityService: RouterUtilityService,
    private tableStateService: TableStateService
  ) { 
    this.assetGroupSubscription = this.assetGroupObservableService
    .getAssetGroup()
    .subscribe((assetGroupName) => {
      this.tableScrollTop = 0;
      this.searchTxt = "";
      this.backButtonRequired =
      this.workflowService.checkIfFlowExistsCurrently(this.pageLevel);
      this.selectedAssetGroup = assetGroupName;
      // this.updateComponent();
      this.getFilters();
    });
    
    this.subscriptionDomain = this.domainObservableService
    .getDomainType()
    .subscribe((domain) => {
      this.selectedDomain = domain;
    });
  }

  ngOnInit() { 
    
    const state = this.tableStateService.getState("assetList") || {};
    if(state){      
      this.headerColName = state.headerColName || '';
      this.direction = state.direction || '';
      this.bucketNumber = state.bucketNumber || 0;
      this.totalRows = state.totalRows || 0;
      this.searchTxt = state?.searchTxt || '';
      
      this.tableData = state?.data || [];
      this.tableDataLoaded = true;
      this.displayedColumns = Object.keys(this.columnWidths);
      this.whiteListColumns = state?.whiteListColumns || this.displayedColumns;
      this.tableScrollTop = state?.tableScrollTop;
      
      if(this.tableData && this.tableData.length>0){
        this.isStatePreserved = true;
      }else{
        this.isStatePreserved = false;
      }
    }

    this.urlToRedirect = this.router.routerState.snapshot.url;
    const breadcrumbInfo = this.workflowService.getDetailsFromStorage()["level0"];    
    
    if(breadcrumbInfo){
      this.breadcrumbArray = breadcrumbInfo.map(item => item.title);
      this.breadcrumbLinks = breadcrumbInfo.map(item => item.url);
    }
    this.breadcrumbPresent = "Asset List";
  }

  handleAddFilterClick(e){}

  handleHeaderColNameSelection(event){
    this.headerColName = event.headerColName;
    this.direction = event.direction;
  }

  handleWhitelistColumnsChange(event){
    this.whiteListColumns = event;
  }

  storeState(state){
    this.tableStateService.setState("assetList", state);    
  }

  clearState(){
    this.tableStateService.clearState("assetList");
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
      } else {
        if (event.clearAll) {
          this.filters = [];
        } else {
          this.filters.splice(event.index, 1);
        }
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
      const localFilters = []; // <<-- this filter is used to store data for filter
      // let labelsKey = Object.keys(this.labels);
      const filterObjKeys = Object.keys(this.filterText);
      const dataArray = [];
      for (let i = 0; i < filterObjKeys.length; i++) {
        let obj = {};
        obj = {
          name: filterObjKeys[i],
        };
        dataArray.push(obj);
      }
      const formattedFilters = dataArray;
      let keyValue;
      for (let i = 0; i < formattedFilters.length; i++) {
        for(let j=0; j<this.filterTypeOptions.length; j++){
          if(formattedFilters[i].name.trim().toLowerCase()==this.filterTypeOptions[j].optionValue.trim().toLowerCase()){
            keyValue = this.filterTypeOptions[j].optionName;
            break;
          }
        }
        // let keyValue = _.find(this.filterTypeOptions, {
        //   optionValue: formattedFilters[i].name,
        // })["optionName"];
        
        // this.changeFilterType(keyValue);
        this.changeFilterType(keyValue).subscribe(filterTagOptions => {          
            let filterValue = _.find(filterTagOptions, {
              id: this.filterText[filterObjKeys[i]],
            })["name"];
          const eachObj = {
            keyDisplayValue: keyValue,
            filterValue: filterValue,
            key: keyValue, // <-- displayKey-- Resource Type
            value: this.filterText[filterObjKeys[i]], // <<-- value to be shown in the filter UI-- S2
            filterkey: filterObjKeys[i].trim(), // <<-- filter key that to be passed -- "resourceType "
            compareKey: filterObjKeys[i].toLowerCase().trim(), // <<-- key to compare whether a key is already present -- "resourcetype"
          };
          localFilters.push(eachObj);
        })
      }
      this.filters = localFilters;
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
    if(this.isStatePreserved){  
      this.tableDataLoaded = true;
      this.clearState();
    }else{
      this.tableDataLoaded = false;
      this.bucketNumber = 0;
      // this.tableData = [];
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

      queryParams = {
        ag: this.selectedAssetGroup,
        filter: this.filterText,
        from: this.bucketNumber * this.paginatorSize,
        searchtext: this.searchTxt,
        size: this.paginatorSize,
      };

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
            if (response[0].response.length === 0) {
              this.allColumns = [];
              this.totalRows = 0;
              this.tableErrorMessage = 'noDataAvailable'
            }
            if (data.response.length > 0) {
              this.assetListData = data.response;
              this.totalRows = data.total;
             
              const updatedResponse = this.massageData(this.assetListData);
              if(isNextPageCalled){
                  this.onScrollDataLoader.next(updatedResponse)
                }else{
                  this.tableData = updatedResponse;
                }
              // this.processData(updatedResponse);
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
    data.map(function (row) {
      const KeysTobeChanged = Object.keys(row);      
      let newObj = {};
      KeysTobeChanged.forEach((element) => {
        let elementnew;
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
        newObj[elementnew] = DATA_MAPPING[newObj[elementnew]]?DATA_MAPPING[newObj[elementnew]]: newObj[elementnew];
      });
      newData.push(newObj);
    });
    return newData;
  }

  goToDetails(event) {
    
    const row = event.rowSelected;
    const data = event.data;
    const state = {
      totalRows: this.totalRows,
      data: data,
      headerColName: this.headerColName,
      direction: this.direction,
      whiteListColumns: this.whiteListColumns,
      bucketNumber: this.bucketNumber,
      searchTxt: this.searchTxt,
      tableScrollTop: event.tableScrollTop
      // filterText: this.filterText
    }
    this.storeState(state);
    try {
      this.workflowService.addRouterSnapshotToLevel(
        this.router.routerState.snapshot.root, 0, this.breadcrumbPresent
      );
      let resourceType;
      if (row["Asset Type"]) {
        resourceType = row["Asset Type"];
      }

      if (
        this.urlID &&
        (this.urlID.toLowerCase() === "pull-request-trend" ||
          this.urlID.toLowerCase() === "pull-request-age" ||
          this.urlID.toLowerCase() === "branching-strategy")
      ) {
        resourceType = this.filterText.resourceType;
      }
      const resourceID = encodeURIComponent(row["Resource ID"]);
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
        this.bucketNumber++;
        this.getData(true);
    } catch (error) {
      this.errorMessage = this.errorHandling.handleJavascriptError(error);
      this.logger.log("error", error);
    }
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

      const downloadRequest = {
        ag: this.selectedAssetGroup,
        filter: this.filterText,
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
    this.isStatePreserved = false;
    this.updateComponent();  
    // this.getUpdatedUrl();
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
        .subscribe((response) => {
          this.filterTypeLabels = _.map(response[0].response, "optionName");
          this.filterTypeOptions = response[0].response;

          this.routerParam();
          // this.deleteFilters();
          this.getFilterArray();
          this.updateComponent();
        });
    } catch (error) {
      this.errorMessage = this.errorHandling.handleJavascriptError(error);
      this.logger.log("error", error);
    }
  }

  changeFilterType(value) {
    var subject = new Subject<any>();
    try {
      this.currentFilterType = _.find(this.filterTypeOptions, {
        optionName: value,
      });
      if(!this.filterTagOptions[value] || !this.filterTagLabels[value]){
        this.issueFilterSubscription = this.issueFilterService
        .getFilters(
          {
            ag: this.selectedAssetGroup,
            domain: this.selectedDomain,
          },
          environment.base +
            this.utils.getParamsFromUrlSnippet(this.currentFilterType.optionURL)
              .url,
          "GET"
        )
        .subscribe((response) => {
          this.filterTagOptions[value] = response[0].response;
          this.filterTagLabels[value] = _.map(response[0].response, "name");
          // if(this.filterTagLabels[value].length==0) this.filterErrorMessage = 'noDataAvailable';
          subject.next(this.filterTagOptions[value]);
        });
      }
      
    } catch (error) {
      this.errorMessage = this.errorHandling.handleJavascriptError(error);
      this.logger.log("error", error);
    }
    return subject.asObservable();
  }

  changeFilterTags(event) {
    let value = event.filterValue;
    try {
      if (this.currentFilterType) {
        const filterTag = _.find(this.filterTagOptions[event.filterKeyDisplayValue], { name: value });
        this.utils.addOrReplaceElement(
          this.filters,
          {
            keyDisplayValue: event.filterKeyDisplayValue,
            filterValue: value,
            key: this.currentFilterType.optionName,
            value: filterTag["id"],
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
      searchValue: this.searchTxt
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
