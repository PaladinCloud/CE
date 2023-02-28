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
  selectedAssetGroup: string;
  selectedDomain: string;
  breadcrumbArray: any = [];
  breadcrumbLinks: any = [];
  breadcrumbPresent: any;
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
  columnWidths = {'Title': 2, 'Violation ID': 1, 'Resource ID': 1, 'Severity': 0.5, 'Category':0.5};
  columnNamesMap = {"PolicyName": "Title"};
  fieldName: string = "policyId.keyword";
  fieldType: string = "number";
  selectedOrder: string = "asc";
  sortOrder: string[];
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
        this.tableScrollTop = 0;
        this.searchTxt = "";
        this.backButtonRequired =
          this.workflowService.checkIfFlowExistsCurrently(this.pageLevel);
        this.selectedAssetGroup = assetGroupName;
        this.getFilters();
      });

    this.domainSubscription = this.domainObservableService
      .getDomainType()
      .subscribe((domain) => {
        this.selectedDomain = domain;
      });
  }

  ngOnInit() {
    const state = this.tableStateService.getState("issueListing") || {};
    if(state){      
      this.headerColName = state.headerColName || '';
      this.direction = state.direction || '';
      this.bucketNumber = state.bucketNumber || 0;
      this.totalRows = state.totalRows || 0;
      this.searchTxt = state?.searchTxt || '';
      
      this.tableDataLoaded = true;
      
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

  handleHeaderColNameSelection(event: any) {
    this.headerColName = event.headerColName;
    const sortColName = this.headerColName?.toLowerCase();
    this.direction = event.direction;
    this.selectedOrder = this.direction;
    this.bucketNumber = 0;
    this.sortOrder = null;
    if (sortColName === "severity") {
      this.fieldName = "severity.keyword";
      this.fieldType = "number";
      this.sortOrder = ["low", "medium", "high", "critical"]
    } else if (sortColName === "issue id") {
      this.fieldName = "_uid";
      this.fieldType = "string";
    } else if (sortColName === "resource id") {
      this.fieldName = "_resourceid.keyword";
      this.fieldType = "string";
    } else if (sortColName === "category") {
      this.fieldName = "policyCategory.keyword";
      this.fieldType = "number";
      this.sortOrder = ["tagging", "costOptimization", "governance", "security"]
    } else if (sortColName === "title") {
      this.fieldType = "number";
      this.fieldName = "policyId.keyword";
    }
    this.updateComponent();
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
      for (let i = 0; i < formattedFilters.length; i++) {
        
        let keyValue = _.find(this.filterTypeOptions, {
          optionValue: formattedFilters[i].name,
        })["optionName"];
        this.changeFilterType(keyValue).then(() => {
            let filterValue = _.find(this.filterTagOptions[keyValue], {
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
          this.filters.push(eachObj);
          this.filters = [...this.filters];
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
    return new Promise((resolve) => {
      this.filterErrorMessage = '';
    try {
      this.currentFilterType = _.find(this.filterTypeOptions, {
        optionName: value,
      });
      const urlObj = this.utils.getParamsFromUrlSnippet(this.currentFilterType.optionURL);
      const queryParams = {
            ...urlObj.params,
            ag: this.selectedAssetGroup,
            domain: this.selectedDomain,
          }
      
      if(!this.filterTagOptions[value] || !this.filterTagLabels[value]){
        this.issueFilterSubscription = this.issueFilterService
        .getFilters(
          queryParams,
          environment.base +
          urlObj.url,
          "GET"
        )
        .subscribe((response) => {
          this.filterTagOptions[value] = response[0].response;
          this.filterTagLabels[value] = _.map(response[0].response, "name");
          this.filterTagLabels[value].sort((a,b)=>a.localeCompare(b));
          if(this.filterTagLabels[value].length==0) this.filterErrorMessage = 'noDataAvailable';
          resolve(this.filterTagOptions[value]);
        });
      }
    } catch (error) {
      this.filterErrorMessage = 'apiResponseError';
      this.errorMessage = this.errorHandling.handleJavascriptError(error);
      this.logger.log("error", error);
    }
    }); 
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

  processData(data) {
    try {
      var innerArr = {};
      var totalVariablesObj = {};
      var cellObj = {};
      let processedData = [];
      var getData = data;      
      const keynames = Object.keys(getData[0]);

      let cellData;
      for (var row = 0; row < getData.length; row++) {
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
          if(col.toLowerCase()=="title"){
            cellObj = {
              ...cellObj,
              isLink: true
            };
          }
          innerArr[col] = cellObj;
          totalVariablesObj[col] = "";
        });
        processedData.push(innerArr);
      }
      if (processedData.length > getData.length) {
        var halfLength = processedData.length / 2;
        processedData = processedData.splice(halfLength);
      }
      return processedData;
    } catch (error) {
      this.errorMessage = this.errorHandling.handleJavascriptError(error);
      this.logger.log("error", error);
    }
  }

  getData(isNextPageCalled=false) {
    try {
      if (this.issueListingSubscription) {
        this.issueListingSubscription.unsubscribe();
      }
      const filterToBePassed = this.filterText;
      if(filterToBePassed){
        filterToBePassed.domain = this.selectedDomain;
        if (!filterToBePassed["issueStatus.keyword"] && filterToBePassed.include_exempt=="yes") {
          filterToBePassed.include_exempt = "yes";
        }
      } 

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
          (response) => {
            try {
              if(!isNextPageCalled){
                this.tableData = [];
              }
              this.tableDataLoaded = true;
              const data = response.data;
              if (response.data.response.length === 0) {
                this.tableErrorMessage = 'noDataAvailable';
                this.totalRows = 0;
              }
              if (data.response.length > 0) {
                this.tableErrorMessage = '';
                this.totalRows = data.total;

                const updatedResponse = this.massageData(data.response);
                const processData = this.processData(updatedResponse);
                if(isNextPageCalled){
                  this.onScrollDataLoader.next(processData)
                }else{
                  this.tableData = processData;
                }

              }
            } catch (e) {
              this.tableErrorMessage = 'apiResponseError';
              this.tableData = [];
              this.tableErrorMessage = this.errorHandling.handleJavascriptError(e);
            }
          },
          (error) => {
            this.tableDataLoaded = true;
            this.tableErrorMessage = "apiResponseError";
            this.logger.log("error", error);
          }
        );
    } catch (error) {
      this.tableErrorMessage = this.errorHandling.handleJavascriptError(error);
      this.logger.log("error", error);
    }
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
        newObj[elementnew] = DATA_MAPPING[typeof newObj[elementnew]=="string"?newObj[elementnew].toLowerCase():newObj[elementnew]]?DATA_MAPPING[newObj[elementnew].toLowerCase()]: newObj[elementnew];
      });
      newData.push(newObj);
    });
    return newData;
  }

  goToDetails(event) {
    // store in this function    
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
      this.router
          .navigate(["issue-details", row["Issue ID"].valueText], {
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
