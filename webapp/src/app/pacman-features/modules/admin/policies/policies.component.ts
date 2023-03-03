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

import { Component, OnInit, OnDestroy, TemplateRef, ViewChild } from "@angular/core";
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
import { MatDialog } from "@angular/material/dialog";
import { DialogBoxComponent } from "src/app/shared/components/molecules/dialog-box/dialog-box.component";
import { NotificationObservableService } from "src/app/shared/services/notification-observable.service";

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
  @ViewChild("enableOrDisablePolicyRef") enableOrDisablePolicyRef: TemplateRef<any>;
  onScrollDataLoader: Subject<any> = new Subject<any>();
  columnsSortFunctionMap = {
    Severity: (a, b, isAsc) => {
      let severeness = {"low":1, "medium":2, "high":3, "critical":4, "default": 5 * (isAsc ? 1 : -1)}
      
      const ASeverity = a["Severity"].valueText??"default";
      const BSeverity = b["Severity"].valueText??"default";
      return (severeness[ASeverity] < severeness[BSeverity] ? -1 : 1) * (isAsc ? 1 : -1);
    }
  }
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
  urlToRedirect: any = "";
  private pageLevel = 0;
  public backButtonRequired;
  mandatory: any;
  private routeSubscription: Subscription;
  private previousUrlSubscription: Subscription;
  selectedRowTitle: any;
  action: any;

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
    private tableStateService: TableStateService,
    private notificationObservableService: NotificationObservableService,
    public dialog: MatDialog,
  ) { }

  ngOnInit() {
    this.notificationObservableService.getMessage();
    this.urlToRedirect = this.router.routerState.snapshot.url;
    this.backButtonRequired = this.workflowService.checkIfFlowExistsCurrently(
      this.pageLevel
    );
    const state = this.tableStateService.getState("adminPolicies") || {};
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
    this.routerParam();
    this.updateComponent();
  }

  handleHeaderColNameSelection(event){
    this.headerColName = event.headerColName;
    this.direction = event.direction;
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
      newObj["Actions"] = "";
      newData.push(newObj);
    });
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

    this.errorMessage = '';

    try{
      this.adminService.executeHttpAction(url, method, {}, queryParams).subscribe(
      (reponse) => {
        this.showLoader = false;
        if (reponse[0].content && reponse[0].content.length>0) {
          this.allPolicies = reponse[0].content;
          this.errorValue = 1;
          this.searchCriteria = undefined;
          var data = reponse[0];
          this.tableDataLoaded = true;
          let updatedResponse = this.massageData(reponse[0].content);
          let processedData = this.processData(updatedResponse)
          if(isNextPageCalled){
            this.onScrollDataLoader.next(processedData)
          }else{
            this.tableData = processedData;
            if(this.tableData?.length==0){
              this.errorMessage = "noDataAvailable";
            }
          }
          this.totalRows = data.totalElements;
          this.dataLoaded = true;
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
    }catch(e){
      this.logger.log("error: ", e);
    }
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
        const autoFixAvailable = getData[row].autoFixAvailable;
        const autoFixEnabled = getData[row].autoFixEnabled;
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
            // chipVariant: "", // this value exists if isChip is true,
            // menuItems: [], // add this if isMenuBtn
          }
          if(col.toLowerCase()=="title"){
              cellObj = {
              ...cellObj,
              imgSrc: autoFixAvailable == "true" ?autoFixEnabled == "true" ?"autofix":"no-autofix":"noImg",
              isLink: true
            };
          }
          else if (col.toLowerCase() == "actions") {
            let dropDownItems: Array<String> = ["Edit Policy"];
          if (autoFixAvailable === "true"){ 
             dropDownItems.push("Edit Autofix");
             if(autoFixEnabled == "true") {
              dropDownItems.push("Disable Autofix");
             } else {
              dropDownItems.push("Enable Autofix");
            }
          }
            if (getData[row].Status.toLowerCase() === "enabled") {
              dropDownItems.push("Disable Policy");
            } else {
              dropDownItems.push("Enable Policy");
            }
          dropDownItems.push("Run Policy");
            cellObj = {
              ...cellObj,
              isMenuBtn: true,
              menuItems: dropDownItems,
            };
          } else if(col.toLowerCase() == "status"){
            let chipBackgroundColor,chipTextColor;
            if(getData[row]["Status"].toLowerCase() === "enabled"){
              chipBackgroundColor = "#E6F5EC";
              chipTextColor = "#00923f";
            }else{
              chipBackgroundColor = "#F2F3F5";
              chipTextColor = "#73777D";
            }
            cellObj = {
              ...cellObj,
              chipList: [getData[row][col]],
              text: getData[row][col].toLowerCase(),
              isChip: true,
              chipBackgroundColor: chipBackgroundColor,
              chipTextColor: chipTextColor
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

  enableDisableRuleOrJob(policyId,action) {
      const url = environment.enableDisableRuleOrJob.url;
      const method = environment.enableDisableRuleOrJob.method;
      const params = {};
      params['policyId'] = policyId;
      params['action'] = action;

      this.adminService.executeHttpAction(url, method, {}, params).subscribe(response => {
        const snackbarText = 'Policy "' +  policyId + '" ' + action + 'd successfully';
        this.openSnackBar(snackbarText, "check-circle");
        this.getPolicyDetails();
      },
        error => {
            this.logger.log("error", error);
        });
  }

  openDialog(event): void {
    const action = event.action;
    const element = event.rowSelected;
    const policyId = event.rowSelected["Policy ID"].text;
    const autofix = element.autoFixEnabled.text == "true"? "disable" : "enable";

    this.selectedRowTitle =  element["Title"].text ;
    this.action = action;
    const dialogRef = this.dialog.open(DialogBoxComponent, {
      width: '500px',
      data: { 
        title: null,
         yesButtonLabel: action,
          noButtonLabel: "Cancel" ,
          template: this.enableOrDisablePolicyRef
        },
      });

    dialogRef.afterClosed().subscribe(result => {
      if(result=="yes"){
        if(action == "Enable Policy" || action == "Disable Policy")
        {
          this.enableDisableRuleOrJob(policyId,action.split(" ")[0].toLowerCase());
        }
        else
        this.enabelOrDisableAutofix(policyId,autofix);
      }
    });
  }

  openSnackBar(message, iconSrc) {
    this.notificationObservableService.postMessage(message, 3*1000, "success", iconSrc);
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
    const action = event?.action?.toLowerCase();
    if(action == "enable policy" 
    || action == "disable policy"
    || action == "enable autofix"
    || action == "disable autofix"
     ){
      this.openDialog(event);
      return;
    }
    
    // store in this function    
    const row = event.rowSelected;
    const data = event.data;
    const policyId = event.rowSelected["Policy ID"].text;
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
        this.router.routerState.snapshot.root, 0, this.pageTitle
      );
    if (action && (action === "edit policy" || action === "edit autofix")) {
        this.router.navigate(["create-edit-policy"], {
          relativeTo: this.activatedRoute,
          queryParamsHandling: "merge",
          queryParams: {
            policyId: policyId,
            showAutofix: action == "edit autofix"
          },
        });
    } else if (action && (action === "Run Policy")){
         this.invokePolicy(policyId);
    }
    } catch (error) {
        this.errorMessage = this.errorHandling.handleJavascriptError(error);
        this.logger.log("error", error);
      }
  }

  enabelOrDisableAutofix(policyId:string,autoFix:string){
    try{
      const url = environment.enableDisableAutofix.url;
      const method = environment.enableDisableAutofix.method;
      const queryParams = {
        policyId: policyId,
        autofixStatus: autoFix == "enable"
      }
      this.adminService.executeHttpAction(url,method,{},queryParams).subscribe(response=>{
        if(response && response[0].message=="success"){
          const snackbarText = 'Autofix for policy "' +  policyId + '" ' + autoFix + 'd successfully';
          this.openSnackBar(snackbarText,"green-info-circle");
          this.getPolicyDetails();
        }
      })
    }catch(error){
       this.errorHandling.handleJavascriptError(error);
    }
  }

  invokePolicy(policyId:string){
    var url = environment.invokePolicy.url; 
    var method = environment.invokePolicy.method; 
    this.adminService.executeHttpAction(url, method, [{}], {policyId:policyId}).subscribe(response => {
     const invocationId = response[0].data;
      if(invocationId)
      this.openSnackBar("Invocation Id " + invocationId + " invoked successfully!!","green-info-circle");
    },
    error => {
      this.errorHandling.handleJavascriptError(error);
    })
  }

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
