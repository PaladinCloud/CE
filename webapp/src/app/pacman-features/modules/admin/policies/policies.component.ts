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

import { ActivatedRoute, Router } from "@angular/router";
import { Subject, Subscription } from "rxjs";
import { UtilsService } from "../../../../shared/services/utils.service";
import { LoggerService } from "../../../../shared/services/logger.service";
import { ErrorHandlingService } from "../../../../shared/services/error-handling.service";
import { RefactorFieldsService } from "./../../../../shared/services/refactor-fields.service";
import { WorkflowService } from "../../../../core/services/workflow.service";
import { RouterUtilityService } from "../../../../shared/services/router-utility.service";
import { AdminService } from "../../../services/all-admin.service";
import { TableStateService } from "src/app/core/services/table-state.service";
import { DATA_MAPPING } from "src/app/shared/constants/data-mapping";

@Component({
  selector: "app-admin-policies",
  templateUrl: "./policies.component.html",
  styleUrls: ["./policies.component.css"],
  providers: [LoggerService, ErrorHandlingService, AdminService],
})
export class PoliciesComponent implements OnInit, OnDestroy {
  pageTitle: String = "Policies";
  allPolicies: any = [];

  outerArr: any = [];
  dataLoaded: boolean = false;
  errorMessage: any;
  showingArr: any = ["policyName", "policyId", "policyDesc"];
  allColumns: any = [];
  totalRows: number = 0;
  currentBucket: any = [];
  bucketNumber: number = 0;
  firstPaginator: number = 1;
  lastPaginator: number;
  currentPointer: number = 0;
  seekdata: boolean = false;
  showLoader: boolean = true;

  headerColName;
  direction;
  columnNamesMap = {"policyDisplayName": "Title","targetType": "Asset",  "severity": "Severity", "category":"Category", "status": "Status"};
  columnWidths = {"Title": 2.4, "Asset": 1, "Severity": 0.5, "Category": 0.5, "Status": 0.8, "Actions": 0.8}
  whiteListColumns;
  isStatePreserved = false;
  tableScrollTop = 0;
  onScrollDataLoader: Subject<any> = new Subject<any>();
  columnsSortFunctionMap = {
    Severity: (a, b, isAsc) => {
      let severeness = {"low":1, "medium":2, "high":3, "critical":4}
      return (severeness[a["Severity"]] < severeness[b["Severity"]] ? -1 : 1) * (isAsc ? 1 : -1);
    }
  }
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
      cost:{
          image: "category-cost",
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

  paginatorSize: number = 25;
  isLastPage: boolean;
  isFirstPage: boolean;
  totalPages: number;
  pageNumber: number = 0;

  searchTxt: String = "";
  tableData: any = [];
  tableDataLoaded: boolean = false;
  filters: any = [];
  searchCriteria: any;
  filterText: any = {};
  errorValue: number = 0;
  showGenericMessage: boolean = false;
  dataTableDesc: String = "";
  urlID: String = "";
  public labels: any;
  FullQueryParams: any;
  queryParamsWithoutFilter: any;
  private previousUrl: any = "";
  urlToRedirect: any = "";
  private pageLevel = 0;
  public backButtonRequired;
  mandatory: any;
  private routeSubscription: Subscription;
  private getKeywords: Subscription;
  private previousUrlSubscription: Subscription;
  private downloadSubscription: Subscription;

  constructor(
    private activatedRoute: ActivatedRoute,
    private router: Router,
    private utils: UtilsService,
    private logger: LoggerService,
    private errorHandling: ErrorHandlingService,
    private refactorFieldsService: RefactorFieldsService,
    private workflowService: WorkflowService,
    private routerUtilityService: RouterUtilityService,
    private adminService: AdminService,
    private tableStateService: TableStateService
  ) {
    this.routerParam();
    this.updateComponent();
  }

  ngOnInit() {
    this.urlToRedirect = this.router.routerState.snapshot.url;
    this.backButtonRequired = this.workflowService.checkIfFlowExistsCurrently(
      this.pageLevel
    );
    const state = this.tableStateService.getState("issueListing") || {};
    if(state){      
      this.headerColName = state.headerColName || '';
      this.direction = state.direction || '';
      this.bucketNumber = state.bucketNumber || 0;
      this.totalRows = state.totalRows || 0;
      this.searchTxt = state?.searchTxt || '';
      
      this.tableDataLoaded = true;
      
      this.tableData = state?.data || [];
      this.whiteListColumns = state?.whiteListColumns || Object.keys(this.columnWidths);
      this.tableScrollTop = state?.tableScrollTop;
      
      if(this.tableData && this.tableData.length>0){
        this.isStatePreserved = true;
      }else{
        this.isStatePreserved = false;
      }
    }

    const breadcrumbInfo = this.workflowService.getDetailsFromStorage()["level0"];
  }

  handleHeaderColNameSelection(event){
    this.headerColName = event.headerColName;
    this.direction = event.direction;
  }

  dataMarshalling(dataToMarshall) {
    let fullPolicies = [];
    for (var index = 0; index < dataToMarshall.length; index++) {
      let policyItem = {};
      policyItem["createdDate"] = dataToMarshall[index][0];
      policyItem["modifiedDate"] = dataToMarshall[index][1];
      policyItem["resolution"] = dataToMarshall[index][2];
      policyItem["policyDesc"] = dataToMarshall[index][3];
      policyItem["policyId"] = dataToMarshall[index][4];
      policyItem["policyUrl"] = dataToMarshall[index][5];
      policyItem["policyVersion"] = dataToMarshall[index][6];
      policyItem["policyName"] = dataToMarshall[index][7];
      policyItem["numberOfRules"] = dataToMarshall[index][8];
      fullPolicies.push(policyItem);
    }
    return fullPolicies;
  }

  nextPage(e) {
    try {
        this.pageNumber++;
        this.showLoader = true;
        this.getPolicyDetails(true);
    } catch (error) {
      this.errorMessage = this.errorHandling.handleJavascriptError(error);
      this.logger.log("error", error);
    }
  }

  prevPage() {
    try {
      if (!this.isFirstPage) {
        this.pageNumber--;
        this.showLoader = true;
        this.getPolicyDetails();
      }
    } catch (error) {
      this.errorMessage = this.errorHandling.handleJavascriptError(error);
      this.logger.log("error", error);
    }
  }

  massageData(data){
    const refactoredService = this.refactorFieldsService;
    const columnNamesMap = this.columnNamesMap;
    const newData = [];
    data.map(function (row) {
      const KeysTobeChanged = Object.keys(row); 
      console.log(KeysTobeChanged);
           
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
      newObj["Actions"] = "";
      newData.push(newObj);
    });
    console.log("new data: ", newData);
    
    return newData;
  }

  getPolicyDetails(isNextPageCalled?) {
    var url = environment.policyDetails.url;
    var method = environment.policyDetails.method;

    var queryParams = {
      page: this.pageNumber,
      size: this.paginatorSize,
    };

    if (this.searchTxt !== undefined && this.searchTxt !== "") {
      queryParams["searchTerm"] = this.searchTxt;
    }

    this.adminService.executeHttpAction(url, method, {}, queryParams).subscribe(
      (reponse) => {
        this.showLoader = false;
        if (reponse[0].content !== undefined) {
          // reponse[0].content = this.dataMarshalling(reponse[0].content);
          this.allPolicies = reponse[0].content;
          this.errorValue = 1;
          this.searchCriteria = undefined;
          var data = reponse[0];
          this.tableDataLoaded = true;
          let updatedResponse = this.massageData(reponse[0].content);
          if(isNextPageCalled){
            this.onScrollDataLoader.next(updatedResponse)
          }else{
            this.tableData = updatedResponse;
          }
          this.dataLoaded = true;
          if (reponse[0].content.length == 0) {
            this.errorValue = -1;
            this.outerArr = [];
            this.allColumns = [];
          }

          if (data.content.length > 0) {
            this.isLastPage = data.last;
            this.isFirstPage = data.first;
            this.totalPages = data.totalPages;
            this.pageNumber = data.number;

            this.seekdata = false;

            this.totalRows = data.totalElements;

            this.firstPaginator = data.number * this.paginatorSize + 1;
            this.lastPaginator =
              data.number * this.paginatorSize + this.paginatorSize;

            this.currentPointer = data.number;

            if (this.lastPaginator > this.totalRows) {
              this.lastPaginator = this.totalRows;
            }
            // let updatedResponse = this.massageData(data.content);
            // this.processData(updatedResponse);
          }
        }
      },
      (error) => {
        this.showGenericMessage = true;
        this.errorValue = -1;
        this.outerArr = [];
        this.dataLoaded = true;
        this.seekdata = true;
        this.errorMessage = "apiResponseError";
        this.showLoader = false;
      }
    );
  }

  /*
   * This function gets the urlparameter and queryObj
   *based on that different apis are being hit with different queryparams
   */
  routerParam() {
    try {
      // this.filterText saves the queryparam
      let currentQueryParams =
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
        //check for mandatory filters.
        if (this.FullQueryParams.mandatory) {
          this.mandatory = this.FullQueryParams.mandatory;
        }
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
    if(this.isStatePreserved){     
      this.tableDataLoaded = true;
      this.clearState();
    }else{
      this.tableDataLoaded = false;
      this.bucketNumber = 0;
      // this.tableData = [];
      this.getPolicyDetails();
    }
  }

  storeState(state){
    this.tableStateService.setState("adminPolicies", state);    
  }

  clearState(){
    this.tableStateService.clearState("adminPolicies");
    this.isStatePreserved = false;
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

  // massageData(data) {
  //   let refactoredService = this.refactorFieldsService;
  //   let newData = [];
  //   let formattedFilters = data.map(function (data) {
  //     let keysTobeChanged = Object.keys(data);
  //     let newObj = {};
  //     keysTobeChanged.forEach((element) => {
  //       var elementnew =
  //         refactoredService.getDisplayNameForAKey(element) || element;
  //       newObj = Object.assign(newObj, { [elementnew]: data[element] });
  //     });
  //     newObj["Actions"] = "";
  //     newData.push(newObj);
  //   });
  //   return newData;
  // }

  processData(data) {
    try {
      var innerArr = {};
      var totalVariablesObj = {};
      var cellObj = {};
      var blue = "#336cc9";
      var green = "#26ba9d";
      var red = "#f2425f";
      var orange = "#ffb00d";
      var yellow = "yellow";
      this.outerArr = [];
      var getData = data;

      if (getData.length) {
        var getCols = Object.keys(getData[0]);
      } else {
        this.seekdata = true;
      }

      for (var row = 0; row < getData.length; row++) {
        innerArr = {};
        for (var col = 0; col < getCols.length; col++) {
          if (getCols[col].toLowerCase() == "actions") {
            cellObj = {
              link: true,
              properties: {
                "text-shadow": "0.33px 0",
                color: "#0047bb",
              },
              colName: getCols[col],
              hasPreImg: false,
              valText: "Edit",
              imgLink: "",
              text: "Edit",
              statusProp: {
                color: "#0047bb",
              },
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
        var halfLength = this.outerArr.length / 2;
        this.outerArr = this.outerArr.splice(halfLength);
      }
      this.allColumns = Object.keys(totalVariablesObj);
      this.allColumns = [
        "Policy Id",
        "Policy Name",
        "Policy Description",
        "Policy Version",
        "No of Rules",
        "Actions",
      ];
    } catch (error) {
      this.errorMessage = this.errorHandling.handleJavascriptError(error);
      this.logger.log("error", error);
    }
  }

  goToCreatePolicy() {
    try {
      this.workflowService.addRouterSnapshotToLevel(
        this.router.routerState.snapshot.root, 0, this.pageTitle
      );
      this.router.navigate(["create-edit-policy"], {
        relativeTo: this.activatedRoute,
        queryParamsHandling: "merge",
        queryParams: {},
      });
    } catch (error) {
      this.errorMessage = this.errorHandling.handleJavascriptError(error);
      this.logger.log("error", error);
    }
  }

  goToDetails(event) {
    console.log("Event called!!: ", event);
    
    // store in this function    
    // const row = event.rowSelected;
    // const data = event.data;
    // const state = {
    //   totalRows: this.totalRows,
    //   data: data,
    //   headerColName: this.headerColName,
    //   direction: this.direction,
    //   whiteListColumns: this.whiteListColumns,
    //   bucketNumber: this.bucketNumber,
    //   searchTxt: this.searchTxt,
    //   tableScrollTop: event.tableScrollTop
    //   // filterText: this.filterText
    // }
    // this.storeState(state);
    if (event.cell === "Edit") {
      try {
        this.workflowService.addRouterSnapshotToLevel(
          this.router.routerState.snapshot.root, 0, this.pageTitle
        );
        this.router.navigate(["create-edit-policy"], {
          relativeTo: this.activatedRoute,
          queryParamsHandling: "merge",
          queryParams: {
            policyId: event.rowSelected["Policy ID"],
          },
        });
      } catch (error) {
        this.errorMessage = this.errorHandling.handleJavascriptError(error);
        this.logger.log("error", error);
      }
    }
  }

  // searchCalled(search) {
  //   this.searchTxt = search;
  // }

  callNewSearch(search) {
    this.searchTxt = search;
    this.isStatePreserved = false;
    this.updateComponent();  
  }

  ngOnDestroy() {
    try {
      if (this.routeSubscription) {
        this.routeSubscription.unsubscribe();
      }
      if (this.previousUrlSubscription) {
        this.previousUrlSubscription.unsubscribe();
      }
    } catch (error) {
      this.logger.log("error", "--- Error while unsubscribing ---");
    }
  }
}
