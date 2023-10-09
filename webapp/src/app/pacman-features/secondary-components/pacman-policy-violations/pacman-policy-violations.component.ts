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

 import {
  Component,
  OnInit,
  OnDestroy,
  Input,
  Output,
  EventEmitter,
} from "@angular/core";
import { Subscription } from "rxjs";
import { ActivatedRoute, Router } from "@angular/router";
import { AssetGroupObservableService } from "../../../core/services/asset-group-observable.service";
import { AutorefreshService } from "../../services/autorefresh.service";
import { environment } from "./../../../../environments/environment";
import { CommonResponseService } from "../../../shared/services/common-response.service";
import { LoggerService } from "../../../shared/services/logger.service";
import { ErrorHandlingService } from "../../../shared/services/error-handling.service";
import { WorkflowService } from "../../../core/services/workflow.service";
import { RefactorFieldsService } from "./../../../shared/services/refactor-fields.service";

@Component({
  selector: "app-pacman-policy-violations",
  templateUrl: "./pacman-policy-violations.component.html",
  styleUrls: ["./pacman-policy-violations.component.css"],
  providers: [CommonResponseService, AutorefreshService],
})
export class PacmanPolicyViolationsComponent implements OnInit, OnDestroy {
  public somedata: any;
  public outerArr: any;
  public allColumns: any;
  selectedAssetGroup: string;
  public apiData: any;
  public applicationValue: any;
  public errorMessage: any;
  public dataComing = true;
  public showLoader = true;
  public tableHeaderData: any;
  private subscriptionToAssetGroup: Subscription;
  private dataSubscription: Subscription;
  public seekdata = false;
  durationParams: any;
  autoRefresh: boolean;
  totalRows = 0;
  bucketNumber = 0;
  paginatorSize = 1000;
  currentBucket: any = [];
  firstPaginator = 1;
  dataTableData: any = [];
  tableDataLoaded = false;
  lastPaginator: number;
  currentPointer = 0;
  errorValue = 0;
  searchTxt = "";
  showGenericMessage = false;
  firstTimeLoad = true;
  headerColName = "Status";
  direction = "asc";
  columnWidths = {"Policy":1.5,"Severity":0.5,"Category":0.5,"Status": 0.5};
  centeredColumns = {
    Severity: true,
    Category: true,
};
  @Input() breadcrumbPresent;
  private urlToRedirect: string;
  @Input() pageLevel: number;
  @Input() resourceId = "";
  @Input() resourceType = "ec2";
  @Output() errorOccured = new EventEmitter<any>();
  @Output() policyCount = new EventEmitter<any>();
  policyTableDataLoaded: boolean = false;
  tableErrorMessage: string;
  whiteListColumns: string[];
  columnNamesMap = {"lastScan": "Status","policyName": "Policy"};
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
    }
}
  associatedPolicies = [];

  columnsSortFunctionMap = {
    Status: (a, b, isAsc) => {
      const order = ["fail", "exempt", "exempted", "pass"];
      const severityOrder = ["critical", "high", "medium", "low"];
      
      const AStatus = a["Status"].valueText.toLowerCase();
      const BStatus = b["Status"].valueText.toLowerCase();
      const ASeverity = a["Severity"].valueText.toLowerCase();
      const BSeverity = b["Severity"].valueText.toLowerCase();

      if(order.indexOf(AStatus)==order.indexOf(BStatus)){
        return (severityOrder.indexOf(ASeverity) < severityOrder.indexOf(BSeverity) ? -1 : 1) * (isAsc ? 1 : -1);
      }

      return (order.indexOf(AStatus) < order.indexOf(BStatus) ? -1 : 1) * (isAsc ? 1 : -1);
    },
  }

  constructor(
    private commonResponseService: CommonResponseService,
    private assetGroupObservableService: AssetGroupObservableService,
    private autorefreshService: AutorefreshService,
    private logger: LoggerService,
    private errorHandling: ErrorHandlingService,
    private router: Router,
    private activatedRoute: ActivatedRoute,
    private workflowService: WorkflowService,
    private refactorFieldsService: RefactorFieldsService
  ) {
    this.durationParams = this.autorefreshService.getDuration();
    this.durationParams = parseInt(this.durationParams, 10);
    this.autoRefresh = this.autorefreshService.autoRefresh;
    this.subscriptionToAssetGroup = this.assetGroupObservableService
      .getAssetGroup()
      .subscribe((assetGroupName) => {
        this.selectedAssetGroup = assetGroupName;
        this.updateComponent();
      });
  }

  ngOnInit() {
    this.whiteListColumns = Object.keys(this.columnWidths);
    this.urlToRedirect = this.router.routerState.snapshot.url;
    this.updateComponent();
  }

  handleHeaderColNameSelection(event){
    this.headerColName = event.headerColName;
    this.direction = event.direction;
  }

  getUpdatedUrl(){
    let updatedQueryParams = {};
    updatedQueryParams = {
      headerColName: this.headerColName,
      direction : this.direction,
      bucketNumber : this.bucketNumber
    }

    this.router.navigate([], {
      relativeTo: this.activatedRoute,
      queryParams: updatedQueryParams,
      queryParamsHandling: 'merge',
    });
  }

  updateComponent() {
    if (this.resourceId) {
      /* All functions variables which are required to be set for component to be reloaded should go here */
      this.outerArr = [];
      this.searchTxt = "";
      this.currentBucket = [];
      // this.bucketNumber = 0;
      this.dataTableData = [];
      this.tableDataLoaded = false;
      this.firstPaginator = 1;
      // this.currentPointer = 0;
      this.showLoader = true;
      this.dataComing = false;
      this.seekdata = false;
      this.errorValue = 0;
      this.showGenericMessage = false;
      this.getData();
    }
  }

  goToDetails(row) {
    const rowSelected = row.rowSelected;
    const status = rowSelected["Status"].valueText.toLowerCase();
    try {
      
      let updatedQueryParams = {...this.activatedRoute.snapshot.queryParams};
      updatedQueryParams["headerColName"] = undefined;
      updatedQueryParams["direction"] = undefined;
      updatedQueryParams["bucketNumber"] = undefined;
      updatedQueryParams["searchValue"] = undefined;
      // if (row.col.toLowerCase() === "policy") {
      //   this.router.navigate(
      //     [
      //       "/pl/compliance/policy-knowledgebase-details",
      //       rowSelected["policy"].text,
      //       "false",
      //     ],
      //     { relativeTo: this.activatedRoute, queryParams:updatedQueryParams, queryParamsHandling: "merge" }
      //   );
      //   this.workflowService.addRouterSnapshotToLevel(
      //     this.router.routerState.snapshot.root, 0, this.breadcrumbPresent
      //   );
      // } else 
      if (status == "fail") {
        this.workflowService.navigateTo({
          urlArray: [
          "../../../../",
          "compliance",
          "issue-listing",
          "issue-details",
          rowSelected["Violation ID"].text,
        ], queryParams: updatedQueryParams,relativeTo: this.activatedRoute,currPagetitle: this.breadcrumbPresent,nextPageTitle: "Violation Details"});
      }
    } catch (error) {
      this.errorMessage = this.errorHandling.handleJavascriptError(error);
      this.logger.log("error", error);
    }
  }

  getData() {
    /* All functions to get data should go here */
    this.getAllPatchingDetails();
  }

  getAllPatchingDetails() {
    if (this.dataSubscription) {
      this.dataSubscription.unsubscribe();
    }
    const payload = {
      ag: this.selectedAssetGroup,
      filter: {},
      from: this.bucketNumber * this.paginatorSize,
      searchtext: this.searchTxt,
      size: this.paginatorSize,
    };

    const queryParam = {
      from: this.bucketNumber * this.paginatorSize,
      searchtext: this.searchTxt,
      size: this.paginatorSize,
    };

    this.errorValue = 0;
    const pacmanPolicyViolationsUrl = environment.pacmanPolicyViolations.url;
    const newUrl = this.replaceUrl(pacmanPolicyViolationsUrl);
    const pacmanPolicyViolationsMethod =
      environment.pacmanPolicyViolations.method;

    this.dataSubscription = this.commonResponseService
      .getData(newUrl, pacmanPolicyViolationsMethod, payload, queryParam)
      .subscribe(
        (response) => {
          this.showGenericMessage = false;
          try {
            this.errorValue = 1;
            this.showLoader = false;
            this.seekdata = false;
            this.dataComing = true;
            this.dataTableData = response.response;
            this.dataComing = true;

            if (response.response.length === 0 && this.firstTimeLoad) {
              this.errorMessage = "noPolicyFound";
            }
            this.firstTimeLoad = false;
            this.totalRows = response.total;

            this.firstPaginator = this.bucketNumber * this.paginatorSize + 1;
            this.lastPaginator =
              this.bucketNumber * this.paginatorSize + this.paginatorSize;

            this.currentPointer = this.bucketNumber;

            if (this.lastPaginator > this.totalRows) {
              this.lastPaginator = this.totalRows;
            }

            if (response.response.length > 0) {
              const updatedResponse = this.massageData(response.response);
              this.currentBucket[this.bucketNumber] = updatedResponse;
              this.associatedPolicies = this.processData(updatedResponse);
              this.policyCount.emit(this.associatedPolicies.length);
              this.policyTableDataLoaded = true;
            }
          } catch (e) {
            this.errorValue = 0;
            this.errorMessage = this.errorHandling.handleJavascriptError(e);
            this.getErrorValues();
            this.errorOccured.emit();
          }
        },
        (error) => {
          this.showGenericMessage = true;
          this.errorMessage = error;
          this.getErrorValues();
          this.errorOccured.emit();
        }
      );
  }

  replaceUrl(url) {
    let replacedUrl = url.replace("{resourceId}", this.resourceId.toString());
    replacedUrl = replacedUrl.replace(
      "{assetGroup}",
      this.selectedAssetGroup.toString()
    );
    replacedUrl = replacedUrl.replace(
      "{resourceType}",
      this.resourceType.toString()
    );
    return replacedUrl;
  }

  getErrorValues(): void {
    this.errorValue = -1;
    this.showLoader = false;
    this.dataComing = false;
    this.seekdata = true;
  }
  massageData(data) {
    /*
     * added by Trinanjan 14/02/2017
     * the funciton replaces keys of the table header data to a readable format
     */
    const refactoredService = this.refactorFieldsService;
    const columnNamesMap = this.columnNamesMap;
    const newData = [];
    const formattedFilters = data.map(function (row) {
      const KeysTobeChanged = Object.keys(row);
      let newObj = {};
      KeysTobeChanged.forEach((element) => {
        let elementnew;
        if(columnNamesMap[element]) {
          elementnew = columnNamesMap[element];
          newObj = Object.assign(newObj, { [elementnew]: row[element] });
        }else{
        elementnew =
        refactoredService.getDisplayNameForAKey(
          element.toLocaleLowerCase()
        ) || element;
        newObj = Object.assign(newObj, { [elementnew]: row[element] });
      }
      });
      newData.push(newObj);
    });
    return newData;
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
            // chipVariant: "", // this value exists if isChip is true,
            // menuItems: [], // add this if isMenuBtn
          }
          if(col.toLowerCase()=="policy name"){
            cellObj = {
              ...cellObj,
              isLink: true
            };
          } else if(col.toLowerCase()=="status"){
              if(cellData.toLowerCase() == "fail"){
                cellObj = {
                  ...cellObj,
                  isLink: true
                };
              }
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

  prevPg() {
    this.currentPointer--;
    // this.processData(this.currentBucket[this.currentPointer]);

    this.firstPaginator = this.currentPointer * this.paginatorSize + 1;
    this.lastPaginator =
      this.currentPointer * this.paginatorSize + this.paginatorSize;
    this.bucketNumber--;
    this.getData();
    this.getUpdatedUrl();
  }

  nextPg() {
    if (this.currentPointer < this.bucketNumber) {
      this.currentPointer++;
      this.processData(this.currentBucket[this.currentPointer]);
      this.firstPaginator = this.currentPointer * this.paginatorSize + 1;
      this.lastPaginator =
        this.currentPointer * this.paginatorSize + this.paginatorSize;
      if (this.lastPaginator > this.totalRows) {
        this.lastPaginator = this.totalRows;
      }
    } else {
      this.bucketNumber++;
      this.getData();
    }
    this.getUpdatedUrl();
  }
  searchCalled(search) {
    this.searchTxt = search;
  }

  callNewSearch() {
    this.bucketNumber = 0;
    this.currentBucket = [];
    this.outerArr = [];
    this.currentBucket = [];
    this.firstPaginator = 1;
    this.currentPointer = 0;
    this.showLoader = true;
    this.getData();
  }
  ngOnDestroy() {
    try {
      this.subscriptionToAssetGroup.unsubscribe();
      this.dataSubscription.unsubscribe();
    } catch (error) {
      this.errorMessage = this.errorHandling.handleJavascriptError(error);
      this.getErrorValues();
    }
  }
}
