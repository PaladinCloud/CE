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
import { Subscription } from "rxjs";
import * as _ from "lodash";
import { AssetGroupObservableService } from "../../../../core/services/asset-group-observable.service";
import { environment } from "./../../../../../environments/environment";
import { Router, ActivatedRoute } from "@angular/router";
import { AutorefreshService } from "../../../services/autorefresh.service";
import { LoggerService } from "../../../../shared/services/logger.service";
import { ErrorHandlingService } from "../../../../shared/services/error-handling.service";
import { WorkflowService } from "../../../../core/services/workflow.service";
import { CommonResponseService } from "../../../../shared/services/common-response.service";
import { UtilsService } from "src/app/shared/services/utils.service";
import { MatSnackBar } from "@angular/material/snack-bar";
import { MatDialog } from "@angular/material/dialog";
import { DialogBoxComponent } from "src/app/shared/components/molecules/dialog-box/dialog-box.component";
import { SnackbarComponent } from "src/app/shared/components/molecules/snackbar/snackbar.component";
import { AdminService } from "src/app/pacman-features/services/all-admin.service";
import { PermissionGuardService } from "src/app/core/services/permission-guard.service";

@Component({
  selector: "app-policy-knowledgebase-details",
  templateUrl: "./policy-knowledgebase-details.component.html",
  styleUrls: ["./policy-knowledgebase-details.component.css"],
  providers: [
    LoggerService,
    ErrorHandlingService,
    CommonResponseService,
    AutorefreshService,
    AdminService
  ],
})
export class PolicyKnowledgebaseDetailsComponent implements OnInit, OnDestroy {
  pageTitle = "Policy Details";
  breadcrumbArray: any = ["Policy Knowledgebase"];
  breadcrumbLinks: any = ["policy-knowledgebase"];
  breadcrumbPresent: any;
  selectedAssetGroup: string;
  actionItems = ["Disable", "Edit"]; // TODO: add "Remove"
  subscriptionToAssetGroup: Subscription;
  public autoFix = false;
  public policyID: any = "";
  public setpolicyIdObtained = false;
  public dataComing = true;
  public showLoader = true;
  public durationParams: any;
  public autoRefresh: boolean;
  public seekdata = false;
  public dataSubscriber: Subscription;
  public errorMessage: any;
  public policyDesc: {};
  displayName: any = "";
  policyParamList: Array<Array<String>>;
  totalRows = 3;
  tableScrollTop = true;
  tableTitle = "Policy Parameters";
  searchTxt = "";
  columnWidths = { 'key': 2, 'value': 1 };
  whiteListColumns;
  tableErrorMessage = '';
  tableDataLoaded = false;
  haveAdminPageAccess = false;
  policyParams = [
    {
      "key": "policykey",
      "value": "check-for-cloud-storage"
    },
    {
      "key": "Metric Name",
      "value": "cloud trial event"
    }
  ]
  policyDescription: any = "";
  resolution: any = [];
  private routeSubscription: Subscription;
  urlToRedirect: any = "";
  private previousUrl: any = "";
  private pageLevel = 0;
  public backButtonRequired;
  public resolutionUrl: string;
  allpolicyParamKeys: any[];
  allEnvParamKeys: any[];
  allEnvironments: any[];
  policyDetails: any;
  allpolicyParams: any[];
  isAutofixEnabled: boolean;
  policyDisplayName: any;
  selectedSeverity: any;
  selectedCategory: any;
  policyType: any;
  assetGroup;
  policyJarFileName: any;
  policyRestUrl: any;
  status: any;
  assetType: string;
  createdDate: any;
  modifiedDate: any;

  constructor(
    private assetGroupObservableService: AssetGroupObservableService,
    private router: Router,
    private activatedRoute: ActivatedRoute,
    private commonResponseService: CommonResponseService,
    private autorefreshService: AutorefreshService,
    private logger: LoggerService,
    private errorHandling: ErrorHandlingService,
    private workflowService: WorkflowService,
    private utils: UtilsService,
    private snackBar: MatSnackBar,
    public dialog: MatDialog,
    private adminService: AdminService,
    private permissions: PermissionGuardService,
  ) {
    this.subscriptionToAssetGroup = this.assetGroupObservableService
      .getAssetGroup()
      .subscribe((assetGroupName) => {
        this.backButtonRequired =
          this.workflowService.checkIfFlowExistsCurrently(this.pageLevel);
        this.selectedAssetGroup = assetGroupName;
        this.updateComponent();
      });
  }
  ngOnInit() {
    try {
      this.haveAdminPageAccess = this.permissions.checkAdminPermission();
      this.durationParams = this.autorefreshService.getDuration();
      this.durationParams = parseInt(this.durationParams, 10);
      this.autoRefresh = this.autorefreshService.autoRefresh;
      const breadcrumbInfo = this.workflowService.getDetailsFromStorage()["level0"];
      this.whiteListColumns = Object.keys(this.columnWidths);

      if (breadcrumbInfo) {
        this.breadcrumbArray = breadcrumbInfo.map(item => item.title);
        this.breadcrumbLinks = breadcrumbInfo.map(item => item.url);
      }
      this.breadcrumbPresent = "Policy Details ";
    } catch (error) {
      this.logger.log("error", error);
    }
  }

  /* Function to repaint component */
  updateComponent() {
    /* All functions variables which are required to be set for component to be reloaded should go here */

    this.seekdata = false;
    this.dataComing = false;
    this.showLoader = true;
    this.getData();
  }

  /* Function to get Data */
  getData() {
    /* All functions to get data should go here */
    this.getpolicyId();
    this.getProgressData();
  }

  /**
   * this funticn gets the policyid from the url
   */
  getpolicyId() {
    /*  TODO:Trinanjan Wrong way of doing it */
    this.routeSubscription = this.activatedRoute.params.subscribe((params) => {
      this.policyID = params["policyID"];
      this.autoFix = params["autoFix"] === "true";
    });
    if (this.policyID !== undefined) {
      this.setpolicyIdObtained = true;
    }
  }

  getProgressData() {
    if (this.policyID !== undefined) {
      if (this.dataSubscriber) {
        this.dataSubscriber.unsubscribe();
      }
      const queryParams = {
        policyId: this.policyID,
      };
      const getPolicyByIdUrl = environment.getPolicyById.url;
      const getPolicyByIdMethod = environment.getPolicyById.method;
      try {
        this.dataSubscriber = this.commonResponseService
          .getData(
            getPolicyByIdUrl,
            getPolicyByIdMethod,
            {},
            queryParams
          )
          .subscribe(
            (response) => {
              try {
                this.showLoader = false;
                this.seekdata = false;
                this.dataComing = true;

                this.processData(response);
              } catch (e) {
                this.errorMessage = this.errorHandling.handleJavascriptError(e);
                this.getErrorValues();
              }
            },
            (error) => {
              this.errorMessage = error;
              this.getErrorValues();
            }
          );
      } catch (error) {
        this.errorMessage = this.errorHandling.handleJavascriptError(error);
        this.getErrorValues();
      }
    }
  }

  getErrorValues(): void {
    this.showLoader = false;
    this.dataComing = false;
    this.seekdata = true;
  }

  processForTableData(data) {
    try {
      var innerArr = {};
      var totalVariablesObj = {};
      var cellObj = {};
      let processedData = [];
      var getData = data;
      const keynames = Object.keys(getData[0]);

      for (var row = 0; row < getData.length; row++) {
        innerArr = {};
        keynames.forEach(col => {
          cellObj = {
            text: getData[row][col], // text to be shown in table cell
            titleText: getData[row][col], // text to show on hover
            valueText: getData[row][col],
            hasPostImage: false,
            isChip: "",
            isMenuBtn: false,
            properties: "",
            link: ""
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

  processData(data) {
    this.displayName = this.uppercasefirst(data.policyDisplayName);
    this.policyDescription = data.policyDescription;
    this.resolutionUrl = data.resolutionUrl;
    this.resolution = data.resolution;
    if (this.resolutionUrl == null || this.resolutionUrl == "") {
      this.resolutionUrl = "https://github.com/PaladinCloud/CE/wiki/Policy";
    }

    this.allpolicyParamKeys = [];
    this.allEnvParamKeys = [];
    this.policyDetails = data;
    let policyParams = Object();
    this.policyDetails.dataSource = 'N/A';
    this.allEnvironments = [];
    this.allpolicyParams = [];
    this.isAutofixEnabled = false;
    this.policyDisplayName = this.uppercasefirst(this.policyDetails.policyDisplayName);
    policyParams = JSON.parse(this.policyDetails.policyParams);
    this.createdDate = this.utils.calculateDateAndTime(data.createdDate, true);
    this.modifiedDate = this.utils.calculateDateAndTime(data.modifiedDate, true);

    if (policyParams.hasOwnProperty('pac_ds')) {
      this.policyDetails.dataSource = this.uppercasefirst(policyParams.pac_ds);
    }

    if (policyParams.hasOwnProperty('environmentVariables')) {
      this.allEnvironments = policyParams.environmentVariables;
      this.allEnvParamKeys = _.map(policyParams.environmentVariables, 'key');
    }
    if (policyParams.hasOwnProperty('params')) {
      if (policyParams.params instanceof Array) {
        for (let i = policyParams.params.length - 1; i >= 0; i -= 1) {
          if (policyParams.params[i].key == 'severity') {
            this.selectedSeverity = this.uppercasefirst(policyParams.params[i].value);
            policyParams.params.splice(i, 1);
          } else if (policyParams.params[i].key == 'policyCategory') {
            this.selectedCategory = this.uppercasefirst(policyParams.params[i].value);
            this.selectedCategory = policyParams.params[i].value == "Governance" ? "Operations" : this.selectedCategory;
            policyParams.params.splice(i, 1);
          } else if (policyParams.params[i].key == 'policyType') {
            this.policyType = this.uppercasefirst(policyParams.params[i].value);
          }
        }
        this.policyDescription = this.policyDetails.policyDesc;
        this.allpolicyParams = policyParams.params;
        this.allpolicyParamKeys = _.map(policyParams.params, 'key');

        let i = 0;
        this.policyParams = this.processForTableData(this.policyParams);
        this.tableDataLoaded = true;
        this.totalRows = this.policyParams.length;

      }
    }
    if (policyParams.hasOwnProperty('autofix')) {
      this.isAutofixEnabled = policyParams.autofix;
    }
    this.status = this.uppercasefirst(data.status);
    this.assetType = this.uppercasefirst(this.policyDetails.targetType);

    this.policyType = this.policyDetails.policyType;
    if (this.policyDetails.assetGroup !== '') {
      this.assetGroup = this.uppercasefirst(this.policyDetails.assetGroup);
    }
    if (this.policyType === 'Classic') {
      this.policyJarFileName = this.policyDetails.policyExecutable;
    } else if (this.policyType === 'Serverless') {
      this.policyRestUrl = this.policyDetails.policyRestUrl;
    }
  }

  callNewSearch(e: any) {
    this.searchTxt = e;
  }

  /**
   * This function returns the first char as upper case
   */
  uppercasefirst(value: any) {
    if (value === null) {
      return "Not assigned";
    }
    value = value.toLocaleLowerCase();
    return value.charAt(0).toUpperCase() + value.slice(1);
  }
  openDialog(event): void {
    const action = event;
    let title, message;
    if (action == "Enable") {
      title = "Enable Policy";
    } else if (action == "Disable") {
      title = "Disable Policy";
    }
    message = 'Are you really sure you want to disable "' + this.policyDisplayName + '".';
    const dialogRef = this.dialog.open(DialogBoxComponent, {
      width: '500px',
      data: { title: title, message: message, yesButtonLabel: action, noButtonLabel: "Cancel" },
    });

    dialogRef.afterClosed().subscribe(result => {
      if (result == "yes") {
        this.enableDisableRuleOrJob(event);
      }
    });
  }

  enableDisableRuleOrJob(event) {
    if (!this.haveAdminPageAccess) {
      return;
    }
    try {
      const url = environment.enableDisableRuleOrJob.url;
      const method = environment.enableDisableRuleOrJob.method;
      const params = {};
      params['policyId'] = this.policyID;

      params['action'] = event;

      this.adminService.executeHttpAction(url, method, {}, params).subscribe(response => {
        // change status 
        this.status = event + 'd';
        // change actions list

        let dropDownItems = ["Edit"];
        if (this.status.toLowerCase() === "enabled") {
          dropDownItems.push("Disable");
        } else {
          // dropDownItems.push("Invoke");
          dropDownItems.push("Enable");
        }
        this.actionItems = dropDownItems;

        const snackbarText = 'Policy "' + this.policyDisplayName + '" ' + this.status.toLowerCase() + ' successfully';
        this.openSnackBar(snackbarText, "check-circle");

      },
        error => {
          this.logger.log("error", error);
        });
    } catch (error) {
      // this.errorMessage = this.errorHandling.handleJavascriptError(error);
      this.logger.log("error", error);
    }
  }

  openSnackBar(message, iconSrc) {
    this.snackBar.openFromComponent(SnackbarComponent, {
      horizontalPosition: "right",
      verticalPosition: "top",
      data: {
        message: message,
        iconSrc: iconSrc,
        variant: "variant1"
      },
      duration: 500 * 1000,
    });
  }


  goToDetails(event) {
    const action = event?.toLowerCase();
    // if(!this.haveAdminPageAccess){
    //   return;
    // }
    if (action == "enable" || action == "disable") {
      this.openDialog(event);
      return;
    }

    try {
      this.workflowService.addRouterSnapshotToLevel(
        this.router.routerState.snapshot.root, 0, this.pageTitle
      );
      if (action && action === "edit") {
        this.router.navigate(["/pl/admin/policies/create-edit-policy"], {
          relativeTo: this.activatedRoute,
          queryParamsHandling: "merge",
          queryParams: {
            policyId: this.policyID,
          },
        });

      }
    } catch (error) {
      this.errorMessage = this.errorHandling.handleJavascriptError(error);
      this.logger.log("error", error);
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
  /*
   * unsubscribing component
   */
  ngOnDestroy() {
    try {
      if (this.subscriptionToAssetGroup) {
        this.subscriptionToAssetGroup.unsubscribe();
      }
      if (this.dataSubscriber) {
        this.dataSubscriber.unsubscribe();
      }
      if (this.routeSubscription) {
        this.routeSubscription.unsubscribe();
      }
    } catch (error) {
      this.logger.log("info", "--- Error while unsubscribing ---");
    }
  }
}
