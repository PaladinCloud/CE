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

import { Component, OnInit, OnDestroy, HostListener } from "@angular/core";
import { environment } from "./../../../../../environments/environment";
import { AssetGroupObservableService } from "../../../../core/services/asset-group-observable.service";
import { ActivatedRoute, Router } from "@angular/router";
import { Subject, Subscription } from "rxjs";
import { IssueFilterService } from "../../../services/issue-filter.service";
import { CommonResponseService } from "../../../../shared/services/common-response.service";
import * as _ from "lodash";
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

@Component({
  selector: "app-issue-listing",
  templateUrl: "./issue-listing.component.html",
  styleUrls: ["./issue-listing.component.css"],
  providers: [IssueFilterService, LoggerService, ErrorHandlingService],
})
export class IssueListingComponent implements OnInit, OnDestroy {
  pageTitle = "Violations";
  issueListingdata: any = [];
  selectedAssetGroup: string;
  selectedDomain: string;
  breadcrumbArray: any = [];
  breadcrumbLinks: any = [];
  breadcrumbPresent: any;
  outerArr: any = [];
  dataLoaded = false;
  errorMessage: any;
  showingArr: any = ["severity", "owner", "executionId"];
  allColumns: any = [];
  totalRows = 0;
  currentBucket: any = [];
  bucketNumber = 0;
  firstPaginator = 1;
  lastPaginator: number;
  currentPointer = 0;
  seekdata = false;
  showLoader = true;
  paginatorSize = 25;
  searchTxt = "";
  popRows: any = ["Download Data"];
  filterTypeOptions: any = [];
  filterTagOptions: any = {};
  currentFilterType;
  filterTypeLabels = [];
  cbArr = [];
  cbModel = [];
  cbObj = {};
  filterTagLabels = {};
  filters: any = [];
  searchCriteria: any;
  filterText: any;
  errorValue = 0;
  showGenericMessage = false;
  public labels: any;
  FullQueryParams: any;
  queryParamsWithoutFilter: any;
  private previousUrl: any = "";
  dataTableData: any = [];
  tableDataLoaded = false;
  issueList: any = [];
  exceptionAction: any;
  showExceptionalModal = false;
  adminAccess = false; // check for admin access
  showDownloadBtn = true;
  showFilterBtn = true;
  private assetGroupSubscription: Subscription;
  private domainSubscription: Subscription;
  private routeSubscription: Subscription;
  private complianceDropdownSubscription: Subscription;
  private issueListingSubscription: Subscription;
  private issueFilterSubscription: Subscription;
  public pageLevel = 0;
  public backButtonRequired;
  public agAndDomain = {};
  public doNotDisplaySearch=true;
  filterErrorMessage = '';
  tableTitle = "Violations";
  tableErrorMessage = '';
  headerColName;
  direction;
  tableScrollTop=0;
  onScrollDataLoader: Subject<any> = new Subject<any>();
  columnWidths = {'Title': 2, 'Issue ID': 1, 'Resource ID': 1, 'Severity': 0.5, 'Category':0.5};
  columnNamesMap = {"PolicyName": "Title"};
  columnsSortFunctionMap = {
    severity: (a, b, isAsc) => {
      let severeness = {"low":1, "medium":2, "high":3, "critical":4}
      return (severeness[a.severity] < severeness[b.severity] ? -1 : 1) * (isAsc ? 1 : -1);
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
    private tableStateService: TableStateService
  ) {


    this.assetGroupSubscription = this.assetGroupObservableService
      .getAssetGroup()
      .subscribe((assetGroupName) => {        
        this.backButtonRequired =
          this.workflowService.checkIfFlowExistsCurrently(this.pageLevel);
        this.selectedAssetGroup = assetGroupName;
        if(this.selectedDomain){
          this.updateComponent();
        }
      });

    this.domainSubscription = this.domainObservableService
      .getDomainType()
      .subscribe((domain) => {
        this.selectedDomain = domain;
        if(this.selectedAssetGroup){
          this.updateComponent();
        }
      });
      this.getFilters();
  }

  ngOnInit() {
    const state = this.tableStateService.getState("issueListing") || {};
    this.searchTxt = this.activatedRoute.snapshot.queryParams.searchValue || '';
    if(state){      
      this.headerColName = state.headerColName || '';
      this.direction = state.direction || '';
      this.bucketNumber = state.bucketNumber || 0;
      this.totalRows = state.totalRows || 0;
      
      this.tableData = state?.data || [];
      this.displayedColumns = Object.keys(this.columnWidths);
      this.whiteListColumns = state?.whiteListColumns || this.displayedColumns;
      this.tableScrollTop = state?.tableScrollTop;
      
      if(this.tableData && this.tableData.length>0){
        this.isStatePreserved = true;
      }else{
        this.isStatePreserved = false;
      }
    }

    const breadcrumbInfo = this.workflowService.getDetailsFromStorage()["level0"];    
    
    if(breadcrumbInfo){
      this.breadcrumbArray = breadcrumbInfo.map(item => item.title);
      this.breadcrumbLinks = breadcrumbInfo.map(item => item.url);
    }

    this.breadcrumbPresent = "Violations";
    // check for admin access
    this.adminAccess = this.permissions.checkAdminPermission();
  }

  handlePaginatorSizeSelection(event) {
    this.paginatorSize = event;
    this.getData();
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
    this.tableStateService.setState("issueListing", state);    
  }

  clearState(){
    this.tableStateService.clearState("issueListing");
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
   * this functin passes query params to filter component to show filter
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

      const filterValues = dataArray;
      const refactoredService = this.refactorFieldsService;
      const formattedFilters = dataArray
      // .map(function (data) {
      //   data.name =
      //     refactoredService.getDisplayNameForAKey(data.name) || data.name;
      //   return data;
      // });
      for (let i = 0; i < formattedFilters.length; i++) {
        let keyValue = _.find(this.filterTypeOptions, {
          optionValue: formattedFilters[i].name,
        })["optionName"];
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

  getFilters() {
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
          this.filterTypeLabels = _.map(response[0].response, "optionName");
          this.filterTypeOptions = response[0].response;
          
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
    }
    if(isApiError) this.filterErrorMessage = 'apiResponseError';
  }

  changeFilterType(value) {
    var subject = new Subject<any>();
    this.filterErrorMessage = '';
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
          this.filterTagLabels[value].sort((a,b)=>a.localeCompare(b));
          if(this.filterTagLabels[value].length==0) this.filterErrorMessage = 'noDataAvailable';
          subject.next(this.filterTagOptions[value]);
        });
      }
    } catch (error) {
      this.filterErrorMessage = 'apiResponseError';
      this.errorMessage = this.errorHandling.handleJavascriptError(error);
      this.logger.log("error", error);
    }
    return subject.asObservable();
  }

  changeFilterTags(event) {    
    let value = event.filterValue;
    this.currentFilterType =  _.find(this.filterTypeOptions, {
        optionName: event.filterKeyDisplayValue,
      });      
    
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

  checkBoxClicked(event) {
    this.cbArr = [];
    if (!this.cbObj[event.index + this.firstPaginator - 1]) {
      this.cbObj[event.index + this.firstPaginator - 1] = event.data;
    } else {
      delete this.cbObj[event.index + this.firstPaginator - 1];
    }
    for (let i = 0; i < Object.keys(this.cbObj).length; i++) {
      this.cbArr[i] = this.cbObj[Object.keys(this.cbObj)[i]];
    }
  }

  updateComponent() {
    this.cbArr = [];
    this.cbObj = {};
    // this.outerArr = [];
    this.tableDataLoaded = false;
    // this.searchTxt = "";
    this.currentBucket = [];
    this.cbModel = [];
    this.dataTableData = [];
    this.issueListingdata = [];
    // this.firstPaginator = 1;
    this.showLoader = true;
    this.dataLoaded = false;
    this.seekdata = false;
    this.errorValue = 0;
    this.showGenericMessage = false;
    // this.clearState();
    if(this.isStatePreserved){      
      this.clearState();
    }else{
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
    if(!this.selectedAssetGroup || !this.selectedDomain){
      return;
    }
    try {
      if (this.issueListingSubscription) {
        this.issueListingSubscription.unsubscribe();
      }
      const filterToBePassed = this.filterText;
      if(filterToBePassed){
        filterToBePassed.domain = this.selectedDomain;
        if (!filterToBePassed.include_exempt) {
          filterToBePassed.include_exempt = "yes";
        }
      } 
      
      const payload = {
        ag: this.selectedAssetGroup,
        filter: filterToBePassed,
        from: this.bucketNumber * this.paginatorSize,
        searchtext: this.searchTxt,
        size: this.paginatorSize,
      };
      const issueListingUrl = environment.issueListing.url;
      const issueListingMethod = environment.issueListing.method;
      this.errorValue = 0;
      this.issueListingSubscription = this.commonResponseService
        .getData(issueListingUrl, issueListingMethod, payload, {})
        .subscribe(
          (response) => {
            this.showGenericMessage = false;
            try {
              this.errorValue = 1;
              this.searchCriteria = undefined;
              this.tableDataLoaded = true;
              const data = response.data;
              this.dataTableData = data.response;              
              this.showLoader = false;
              this.dataLoaded = true;
              if (response.data.response.length === 0) {
                this.tableErrorMessage = 'noDataAvailable';
                this.errorValue = -1;
                this.outerArr = [];
                this.allColumns = [];
                this.totalRows = 0;
              }
              if (data.response.length > 0) {
                this.tableErrorMessage = '';
                this.issueListingdata = data.response;
                this.seekdata = false;
                this.totalRows = data.total;
                this.firstPaginator =
                  this.bucketNumber * this.paginatorSize + 1;
                this.lastPaginator =
                  this.bucketNumber * this.paginatorSize + this.paginatorSize;
                this.currentPointer = this.bucketNumber;
                if (this.lastPaginator > this.totalRows) {
                  this.lastPaginator = this.totalRows;
                }
                const updatedResponse = this.massageData(this.issueListingdata);
                this.currentBucket[this.bucketNumber] = updatedResponse;
                if(isNextPageCalled){
                  this.onScrollDataLoader.next(updatedResponse)
                }else{
                  this.tableData = updatedResponse;
                }

              }
            } catch (e) {
              this.tableErrorMessage = 'apiResponseError';
              this.errorValue = 0;
              this.errorValue = -1;
              this.outerArr = [];
              this.dataLoaded = true;
              this.seekdata = true;
              this.errorMessage = this.errorHandling.handleJavascriptError(e);
            }
          },
          (error) => {
            this.showGenericMessage = true;
            this.errorValue = -1;
            this.outerArr = [];
            this.dataLoaded = true;
            this.seekdata = true;
            this.errorMessage = "apiResponseError";
          }
        );
    } catch (error) {
      this.showLoader = false;
      this.errorMessage = this.errorHandling.handleJavascriptError(error);
      this.logger.log("error", error);
    }
  }
  showExceptionModal(exceptionAction) {
    this.exceptionAction = exceptionAction;
    this.showExceptionalModal = true;
  }

  closeExceptionalModal($event) {
    this.showExceptionalModal = false;
  }

  refreshDataTable($event) {
    this.updateComponent();
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

  processData(data) {
    try {
      let innerArr = {};
      const totalVariablesObj = {};
      let cellObj = {};
      this.outerArr = [];
      const getData = data;
      let getCols;
      if (getData.length) {
        getCols = Object.keys(getData[0]);
      } else {
        this.seekdata = true;
      }

      for (let row = 0; row < getData.length; row++) {
        innerArr = {};
        for (let col = 0; col < getCols.length; col++) {
          if (
            getCols[col].toLowerCase() === "resource id" ||
            getCols[col].toLowerCase() === "resourceid"
          ) {
            cellObj = {
              link: "true",
              properties: {
                "text-shadow": "0.1px 0",
                "text-transform": "lowercase",
              },
              colName: getCols[col],
              hasPreImg: false,
              imgLink: "",
              text: getData[row][getCols[col]],
              valText: getData[row][getCols[col]],
            };
          } else if (
            getCols[col].toLowerCase() === "rule name" ||
            getCols[col].toLowerCase() === "issue id"
          ) {
            cellObj = {
              link: "true",
              properties: {
                "text-transform": "lowercase",
                "text-shadow": "0.1px 0",
              },
              colName: getCols[col],
              hasPreImg: false,
              imgLink: "",
              valText: getData[row][getCols[col]],
              text: getData[row][getCols[col]],
            };
          } else if (getCols[col].toLowerCase() === "severity") {
            if (getData[row][getCols[col]] === "low") {
              cellObj = {
                link: "",
                properties: {
                  color: "",
                  "text-transform": "capitalize",
                },
                colName: getCols[col],
                hasPreImg: true,
                imgLink: "",
                text: getData[row][getCols[col]],
                valText: 1,
                statusProp: {
                  "background-color": "#ffe00d",
                },
              };
            } else if (getData[row][getCols[col]] === "medium") {
              cellObj = {
                link: "",
                properties: {
                  color: "",
                  "text-transform": "capitalize",
                },
                colName: getCols[col],
                hasPreImg: true,
                imgLink: "",
                valText: 2,
                text: getData[row][getCols[col]],
                statusProp: {
                  "background-color": "#ffb00d",
                },
              };
            } else if (getData[row][getCols[col]] === "high") {
              cellObj = {
                link: "",
                properties: {
                  color: "",
                  "text-transform": "capitalize",
                },
                colName: getCols[col],
                hasPreImg: true,
                valText: 3,
                imgLink: "",
                text: getData[row][getCols[col]],
                statusProp: {
                  "background-color": "#0047bb",
                },
              };
            } else {
              cellObj = {
                link: "",
                properties: {
                  color: "",
                  "text-transform": "capitalize",
                },
                colName: getCols[col],
                hasPreImg: true,
                imgLink: "",
                valText: 4,
                text: getData[row][getCols[col]],
                statusProp: {
                  "background-color": "#e60127",
                },
              };
            }
          } else if (
            getCols[col].toLowerCase() === "created on" ||
            getCols[col].toLowerCase() === "modified on"
          ) {
            cellObj = {
              link: "",
              properties: {
                color: "",
              },
              colName: getCols[col],
              hasPreImg: false,
              imgLink: "",
              text: this.calculateDate(getData[row][getCols[col]]),
              valText: new Date(getData[row][getCols[col]]).getTime(),
            };
          } else {
            cellObj = {
              link: "",
              properties: {
                color: "",
              },
              colName: getCols[col],
              hasPreImg: false,
              imgLink: "",
              text: getData[row][getCols[col]],
              valText: getData[row][getCols[col]],
            };
          }
          innerArr[getCols[col]] = cellObj;
          totalVariablesObj[getCols[col]] = "";
        }
        this.outerArr.push(innerArr);
      }
      if (this.outerArr.length > getData.length) {
        const halfLength = this.outerArr.length / 2;
        this.outerArr = this.outerArr.splice(halfLength);
      }
      this.allColumns = Object.keys(totalVariablesObj);
    } catch (error) {
      this.errorMessage = this.errorHandling.handleJavascriptError(error);
      this.logger.log("error", error);
    }
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
      // searchTxt: this.searchTxt,
      tableScrollTop: event.tableScrollTop
      // filterText: this.filterText
    }
    this.storeState(state);
    try {
      this.workflowService.addRouterSnapshotToLevel(
        this.router.routerState.snapshot.root, 0, this.breadcrumbPresent
      );
      this.router
          .navigate(["issue-details", row["Issue ID"]], {
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

  calculateDate(_JSDate) {
    if (!_JSDate) {
      return "No Data";
    }
    const date = new Date(_JSDate);
    const year = date.getFullYear().toString();
    const month = date.getMonth() + 1;
    let monthString;
    if (month < 10) {
      monthString = "0" + month.toString();
    } else {
      monthString = month.toString();
    }
    const day = date.getDate();
    let dayString;
    if (day < 10) {
      dayString = "0" + day.toString();
    } else {
      dayString = day.toString();
    }
    return monthString + "-" + dayString + "-" + year;
  }

  searchCalled(search) {
    this.searchTxt = search;
    // this.getUpdatedUrl();
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


  nextPg() {
    try {
        this.bucketNumber++;
        this.getData(true);
    } catch (error) {
      this.errorMessage = this.errorHandling.handleJavascriptError(error);
      this.logger.log("error", error);
    }
  }

  callNewSearch(searchVal){    
    this.searchTxt = searchVal;
    // this.state.searchValue = searchVal;
    this.isStatePreserved = false;
    this.updateComponent();  
    this.getUpdatedUrl();
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
