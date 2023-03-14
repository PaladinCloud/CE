/*
 *Copyright 2018 T Mobile, Inc. or its affiliates. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the 'License'); You may not use
 * this file except in compliance with the License. A copy of the License is located at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * or in the 'license' file accompanying this file. This file is distributed on
 * an 'AS IS' BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, express or
 * implied. See the License for the specific language governing permissions and
 * limitations under the License.
 */

import { Component, OnInit, OnDestroy, ViewChild } from '@angular/core';
import { environment } from './../../../../../environments/environment';

import { ActivatedRoute, Router } from '@angular/router';
import { Subscription } from 'rxjs';
import { UtilsService } from '../../../../shared/services/utils.service';
import { LoggerService } from '../../../../shared/services/logger.service';
import { ErrorHandlingService } from '../../../../shared/services/error-handling.service';


import { RefactorFieldsService } from './../../../../shared/services/refactor-fields.service';
import { WorkflowService } from '../../../../core/services/workflow.service';
import { RouterUtilityService } from '../../../../shared/services/router-utility.service';
import { AdminService } from '../../../services/all-admin.service';
import { NgForm } from '@angular/forms';
import { UploadFileService } from '../../../services/upload-file-service';
import { MatDialog } from '@angular/material/dialog';
import { DialogBoxComponent } from 'src/app/shared/components/molecules/dialog-box/dialog-box.component';
import { NotificationObservableService } from 'src/app/shared/services/notification-observable.service';
import { DATA_MAPPING } from 'src/app/shared/constants/data-mapping';

@Component({
  selector: 'app-admin-create-edit-policy',
  templateUrl: './create-edit-policy.component.html',
  styleUrls: ['./create-edit-policy.component.css'],
  providers: [
    LoggerService,
    ErrorHandlingService,
    UploadFileService,
    AdminService
  ]
})
export class CreateEditPolicyComponent implements OnInit, OnDestroy {

  selectedAssetType = "";
  resolutionUrl = "";
  isDisabled = true;
  currentStepperIndex = 0;
  currentStepperName = "";
  description = "";
  readonly = true;
  policyLoader = false;
  pageTitle = 'Edit Policy';
  FormHeader = "Policy Details";
  allPolicies = [];
  breadcrumbArray = ['policys'];
  breadcrumbLinks = ['policys'];
  breadcrumbPresent;
  outerArr = [];
  dataLoaded = false;
  errorMessage;
  showingArr = ['policyName', 'policyId', 'policyDesc'];
  allColumns = [];
  submitBtn = "Update";
  totalRows = 0;
  currentBucket = [];
  bucketNumber = 0;
  firstPaginator = 1;
  lastPaginator;
  currentPointer = 0;
  seekdata = false;
  showLoader = true;
  allMonthDays = [];
  allEnvironments = [];
  allpolicyParamKeys = [];
  allEnvParamKeys = [];
  hasEditableParams = 0;
  allPolicyParams = Object();
  paramsList = [];
  status = false;
  hideContent = false;
  ispolicyCreationFailed = false;
  ispolicyCreationSuccess = false;
  policyPolicyLoader = false;
  policyPolicyLoaderFailure = false;
  policyDisplayName = '';

  paginatorSize = 25;
  isLastPage;
  isFirstPage;
  totalPages: number;
  pageNumber = 0;

  searchTxt = '';
  dataTableData = [];
  initVals = [];
  tableDataLoaded = false;
  filters = [];
  searchCriteria;
  filterText = {};
  errorValue = 0;
  showGenericMessage = false;
  dataTableDesc = '';
  urlID = '';

  FullQueryParams;
  queryParamsWithoutFilter;
  urlToRedirect = '';
  mandatory;
  activePolicy = [];
  parametersInput = { policyKey: '', policyValue: '', envKey: '', envValue: '' };
  alexaKeywords = [];
  assetGroupNames = [];
  datasourceDetails = [];
  targetTypesNames = [];
  policyCategories = ["cost","security","operations","tagging"];
  policySeverities = ["critical", "high", "medium", "low"];
  policyJarFile;
  currentFileUpload: File;
  selectedFiles: FileList;

  selectedFrequency = '';
  policyJarFileName = '';
  selectedPolicyId = '';
  contentHidden = true;
  selectedpolicyName = '';
  selectedTargetType = '';
  isAutofixEnabled = false;
  emailId : string;
  isRequired = true;

  public labels;
  private previousUrl = '';
  private pageLevel = 0;
  public backButtonRequired;
  private routeSubscription: Subscription;
  private previousUrlSubscription: Subscription;
  selectedCategory: any;
  selectedSeverity: any;
  selectedMonthId: number;
  selectedDay: any;
  selectedAssetGroup: any;
  selectedMonths: any;
  selectedDays: any;
  selectedWeekName: any;
  alexaKeyword: any;
  dataSourceName: any;
  policyFrequency: any;
  selectedPolicyType: any;
  policyName = "";
  policyId: any;
  policyDetails: any;
  resolution = "";
  policyUrl = "";
  stepperData = [
    {
      id: 0,
      name: "Policy Details"
    },
    {
      id: 1,
      name: "Policy Parameters"
    },
    {
      id: 2,
      name: "Autofix"
    }
  ]
  isSilentNotificationEnabled = false;                                                                                                                                                                                                                                                        
  isAutofixAvailable = false;
  updatedAccounts: string[];
  attributeList : string[] = ["A","V"];
  selectedAttributes: string[];
  updatedAttributes: string[];
  selectedAccounts: string[];
  accountList: string[] = [];
  fixMailSubject: string;
  postFixMessage: string;
  violationMessage: string;
  warningMessage: any;
  warningMailSubject: any;
  index: string;
  waitingTime: any;
  maxEmailNotification: any;
  elapsedTime: any;
  fixType: any;
  showAutofix = false;


  constructor(
    private activatedRoute: ActivatedRoute,
    private router: Router,
    private utils: UtilsService,
    private logger: LoggerService,
    private errorHandling: ErrorHandlingService,
    private uploadService: UploadFileService,
    private notificationObservableService: NotificationObservableService,
    private workflowService: WorkflowService,
    private routerUtilityService: RouterUtilityService,
    private adminService: AdminService,
    public dialog: MatDialog
  ) {

    this.routerParam();
    this.updateComponent();
  }

  ngOnInit() {
    this.urlToRedirect = this.router.routerState.snapshot.url;
    const breadcrumbInfo = this.workflowService.getDetailsFromStorage()["level0"];

    if (breadcrumbInfo) {
      this.breadcrumbArray = breadcrumbInfo.map(item => item.title);
      this.breadcrumbLinks = breadcrumbInfo.map(item => item.url);
    }
    this.backButtonRequired = this.workflowService.checkIfFlowExistsCurrently(
      this.pageLevel
    );
  }

  selectedType(event: any) {
    this.selectedPolicyType = event;
    if (event) {
      this.isDisabled = false;
      this.readonly = false;
    }
  }

  pageCounter(clickedButton: string) {
    if (clickedButton == 'back') {
      this.currentStepperIndex--;
    } else
      this.currentStepperIndex++;

    this.selectedStepperIndex(this.currentStepperIndex);
  }

  getDatasourceDetails() {
    this.policyPolicyLoader = true;
    this.contentHidden = true;
    this.policyPolicyLoaderFailure = false;
    const url = environment.datasourceDetails.url;
    const method = environment.datasourceDetails.method;
    this.adminService.executeHttpAction(url, method, {}, {}).subscribe(reponse => {
      const fullDatasourceNames = [];
      for (let index = 0; index < reponse[0].length; index++) {
        fullDatasourceNames.push(reponse[0][index].dataSourceName);
      }
      this.datasourceDetails = fullDatasourceNames;
    },
      error => {
        this.policyPolicyLoader = false;
        this.contentHidden = true;
        this.policyPolicyLoaderFailure = true;
        this.datasourceDetails = [];
        this.errorMessage = 'apiResponseError';
        this.showLoader = false;
      });
  }

  getpolicyCategoryDetails() {
    this.policyPolicyLoader = true;
    this.contentHidden = true;
    this.policyPolicyLoaderFailure = false;
    const url = environment.policyCategory.url;
    const method = environment.policyCategory.method;
    this.adminService.executeHttpAction(url, method, {}, {}).subscribe(reponse => {
      const categories = [];
      for (let index = 0; index < reponse[0].length; index++) {
        const categoryDetail = reponse[0][index];
        if (categoryDetail.policyCategory.toLowerCase() == "costoptimization") {
          categoryDetail.policyCategory = "cost";
        }
        else if (categoryDetail.policyCategory.toLowerCase() == "governance") {
          categoryDetail.policyCategory = "operations";
        }
        categories.push(categoryDetail.policyCategory);
      }
      this.policyCategories = categories;
      this.showLoader = false;
      this.contentHidden = false;
      this.policyPolicyLoaderFailure = false;
      this.policyPolicyLoader = false;
    },
      error => {
        this.policyPolicyLoader = false;
        this.contentHidden = true;
        this.policyPolicyLoaderFailure = true;
        this.policyCategories = [];
        this.errorMessage = 'apiResponseError';
        this.showLoader = false;
      });
  }

  onSubmit() {
    this.hideContent = true;
    this.policyLoader = true;
    this.buildCreatepolicyModel();
  }

  private buildCreatepolicyModel() {
    const PolicyModel = Object();
    PolicyModel.policyType = this.selectedPolicyType;
    PolicyModel.policyDisplayName = this.policyDisplayName;
    PolicyModel.policyId = this.policyId;
    PolicyModel.policyName = this.policyName;
  
    PolicyModel.targetType = this.selectedAssetType;
    PolicyModel.severity = this.selectedSeverity;
    PolicyModel.status = this.status?"ENABLED": "DISABLED";
    PolicyModel.category = this.selectedCategory;
    PolicyModel.policyDesc = this.description;
    PolicyModel.resolution = this.resolution;
    PolicyModel.resolutionUrl = this.resolutionUrl;
    PolicyModel.assetGroup = this.selectedAssetGroup;
    PolicyModel.policyExecutable = this.policyJarFileName;
    PolicyModel.policyRestUrl = this.policyUrl;
    PolicyModel.policyParams = this.buildpolicyParams();
    PolicyModel.policyFrequency = "0 0 ? * MON *";
    PolicyModel.isAutofixEnabled = false;

  this.createOrUpdatepolicy(PolicyModel);
  
  }

  createOrUpdatepolicy(PolicyModel: any) {
    const url =  environment.updatePolicy.url;
    const method = environment.updatePolicy.method;
    // if(this.status){
    //   this.enableDisableRuleOrJob("Enable");
    // }
    // else
    // this.enableDisableRuleOrJob("Disable");
    this.uploadService.pushFileToStorage(url, method, this.currentFileUpload, PolicyModel).subscribe(event => {
      this.policyLoader = false;
      this.ispolicyCreationSuccess = true;
      this.notificationObservableService.postMessage("Policy " + this.policyDisplayName +  "updated successfully!!", 500, "variant1", "green-info-circle");
      this.workflowService.addRouterSnapshotToLevel(this.router.routerState.snapshot.root);
      this.workflowService.clearAllLevels();
      this.router.navigate(['../'], {
        relativeTo: this.activatedRoute,
        state: {
          dataUpdated: true
          }
      });
    },
      error => {
        this.ispolicyCreationFailed = true;
        this.policyLoader = false;
      });
  }

  private buildpolicyParams() {
    const policyParms = Object();
    policyParms.params = this.paramsList;
    policyParms.environmentVariables = this.allEnvironments;
    return JSON.stringify(policyParms);
  }

  onSelectCategory(selectedCategory) {
    this.selectedCategory = selectedCategory;
  }

  onSelectSeverity(selectedSeverity) {
    this.selectedSeverity = selectedSeverity;
  }

  closeErrorMessage() {
    this.ispolicyCreationFailed = false;
    this.hideContent = false;
  }

  getTargetTypeNamesByDatasourceName(datasourceName) {
    const url = environment.targetTypesByDatasource.url;
    const method = environment.targetTypesByDatasource.method;
    this.adminService.executeHttpAction(url, method, {}, { dataSourceName: datasourceName }).subscribe(reponse => {
      this.showLoader = false;
      this.targetTypesNames = [...reponse[0]];
    },
      error => {
        this.errorMessage = 'apiResponseError';
        this.showLoader = false;
      });
  }

  selectedStepperIndex(event: any) {
    const index = this.stepperData.findIndex(element => element.id == event);
    this.currentStepperIndex = event;
    this.currentStepperName = this.stepperData[index].name;
    this.FormHeader =  this.currentStepperName ;
  }

  onSelectPolicyId(policyId: any) {
    this.selectedPolicyId = policyId;
    this.getPolicyDetails(policyId);
  }
  onSelectTargetType(targetType: any) {
    this.selectedTargetType = targetType;
  }
  onSelectFrequency(frequencyType: any) {
    this.selectedFrequency = frequencyType;
  }

  getData() {
    this.getDatasourceDetails();
  }

  /*
    * This function gets the urlparameter and queryObj
    *based on that different apis are being hit with different queryparams
  */

  routerParam() {
    try {
      // this.filterText saves the queryparam
      const currentQueryParams = this.routerUtilityService.getQueryParametersFromSnapshot(this.router.routerState.snapshot.root);
      if (currentQueryParams) {
        this.FullQueryParams = currentQueryParams;
        this.queryParamsWithoutFilter = JSON.parse(JSON.stringify(this.FullQueryParams));
        this.policyId = this.queryParamsWithoutFilter.policyId;
        this.showAutofix = this.queryParamsWithoutFilter.showAutofix == "true";
        delete this.queryParamsWithoutFilter['filter'];
        this.dataSourceName = this.queryParamsWithoutFilter.ag;
        if (this.policyId) {
          this.breadcrumbPresent = 'Edit Policy';
          this.isDisabled = false;
          this.readonly = false;
          this.hideContent = true;
          // this.getpolicyCategoryDetails();
          this.getPolicyDetails(this.policyId);
        }

        this.FullQueryParams = currentQueryParams;

        this.queryParamsWithoutFilter = JSON.parse(JSON.stringify(this.FullQueryParams));
        delete this.queryParamsWithoutFilter['filter'];

        /**
         * The below code is added to get URLparameter and queryparameter
         * when the page loads ,only then this function runs and hits the api with the
         * filterText obj processed through processFilterObj function
         */
        this.filterText = this.utils.processFilterObj(
          this.FullQueryParams
        );

        this.urlID = this.FullQueryParams.TypeAsset;
        // check for mandatory filters.
        if (this.FullQueryParams.mandatory) {
          this.mandatory = this.FullQueryParams.mandatory;
        }
      }
    } catch (error) {
      this.errorMessage = this.errorHandling.handleJavascriptError(error);
      this.logger.log('error', error);
    }
  }

  /**
   * This function get calls the keyword service before initializing
   * the filter array ,so that filter keynames are changed
   */
  getPolicyDetails(policyId: any) {
    let url = environment.getPolicyDetailsById.url;
    let method = environment.getPolicyDetailsById.method;

    this.adminService.executeHttpAction(url, method, {}, { policyId: policyId }).subscribe(reponse => {
      this.policyDetails = reponse[0];
      this.selectedPolicyId = this.policyDetails.policyId;
      this.status = this.policyDetails.status=="ENABLED";
      this.selectedSeverity = this.policyDetails.severity;
      this.selectedCategory = this.policyDetails.category;
      this.policyId = this.policyDetails.policyId;
      this.policyDisplayName = this.policyDetails.policyDisplayName;
      this.selectedPolicyType = this.policyDetails.policyType;
      this.policyName = this.policyDetails.policyName;
      this.selectedAssetGroup = this.policyDetails.assetGroup;
      this.policyUrl = this.policyDetails.policyRestUrl;
      this.selectedAssetType = this.policyDetails.targetType;
      this.description = this.policyDetails.policyDesc;
      this.resolution = this.policyDetails.resolution;
      this.resolutionUrl = this.policyDetails.resolutionUrl;
      this.isAutofixAvailable = this.policyDetails.autoFixAvailable == "true";
      this.isAutofixEnabled =this.policyDetails.autoFixEnabled == "true";
     

      if(!this.isAutofixAvailable)
           this.removeStepper("Autofix");
      else{
          this.getAccounts();
          this.selectedAccounts = this.policyDetails.allowList.split(",").slice();
          this.fixMailSubject = this.policyDetails.fixMailSubject;
          this.postFixMessage = this.policyDetails.fixMessage;
          this.violationMessage = this.policyDetails.violationMessage;
          this.waitingTime = this.policyDetails.waitingTime;
          this.maxEmailNotification = this.policyDetails.maxEmailNotification;
          this.warningMessage = this.policyDetails.warningMessage;
          this.warningMailSubject = this.policyDetails.warningMailSubject; 
          this.elapsedTime = this.policyDetails.elapsedTime;
          this.fixType = this.policyDetails.fixType;
          this.isSilentNotificationEnabled = this.fixType == "silent";
        }
      this.allPolicyParams = JSON.parse(this.policyDetails.policyParams)["params"];
      this.paramsList = [];
      
      for (let i = this.allPolicyParams.length - 1; i >= 0; i -= 1) {
        if (this.allPolicyParams[i]["isEdit"]) {
          this.hasEditableParams++;
        }
          this.paramsList.push(
            {
              "key": this.allPolicyParams[i]["key"],
              "value": this.allPolicyParams[i]["value"],
              "displayName": this.allPolicyParams[i]["displayName"]?this.allPolicyParams[i]["displayName"]:this.allPolicyParams[i]["key"],
              "isEdit": this.allPolicyParams[i]["isEdit"] ? this.allPolicyParams[i]["isEdit"] : false,
              "isMandatory": this.allPolicyParams[i]["isMandatory"] ? this.allPolicyParams[i]["isMandatory"] : false,
              "description": this.allPolicyParams[i]["description"] 
            }
          )
      }

      
   
      // if (this.selectedPolicyType == "ManagePolicy") {
      //   this.isDisabled = true;
      // }
      this.getTargetTypeNamesByDatasourceName(this.selectedAssetGroup);
      this.hideContent = false;
    },
      error => {
        this.logger.log("Error",error);
      });
  }

  getAccounts(){

    try{
      const url = environment.listTargetTypeAttributeValues.url;
      const method = environment.listTargetTypeAttributeValues.method;

      let index = "",field="",accounts=[];
      if(this.selectedAssetGroup == "aws"){
        index = "/aws_account";
        field =  "accountid.keyword";
      } else if(this.selectedAssetGroup == "azure"){
        index = "/azure_subscription";
        field = "subscription.keyword";
      } else{
        index = "/gcp_vminstance"
        field =  "projectId.keyword";

      }
      const payload = {
        index: index +"/_search?filter_path=aggregations.alldata.buckets.key",
        payload :	'{"size":0,"aggs":{"alldata":{"terms":{"field":"'+field+'","size":10000}}}}'
      }
      this.adminService.executeHttpAction(url,method,payload,{}).subscribe(response=>{
          const aggregations = response[0]?.data?.aggregations;
          const alldata = aggregations.alldata;
          const buckets = alldata.buckets;
          buckets.forEach(element => {
            accounts.push(element.key);
          });
          this.accountList = accounts;
      })
    } catch (error) {
      this.errorMessage = this.errorHandling.handleJavascriptError(error);
      this.logger.log('error', error);
    }
  }

  onAccountsChange(updatedAccounts:any){
    this.updatedAccounts = updatedAccounts;
  }

  removeStepper(stepperName: string){
    this.stepperData= this.stepperData.filter(element=> element.name != stepperName);
  }

  openDialog(PolicyModel: any): void {
    const title = "Update Policy!"
    const yesButtonLabel = "Update";
    const dialogRef = this.dialog.open(DialogBoxComponent, {
      width: '500px',
      data: { title: title,
            yesButtonLabel: yesButtonLabel,
          } 
    });

    dialogRef.afterClosed().subscribe(result => {
      if (result == "yes") {
        this.createOrUpdatepolicy(PolicyModel);
      }
    });
  }

  updateComponent() {
    this.outerArr = [];
    this.searchTxt = '';
    this.currentBucket = [];
    this.bucketNumber = 0;
    this.firstPaginator = 1;
    this.showLoader = true;
    this.currentPointer = 0;
    this.dataTableData = [];
    this.tableDataLoaded = false;
    this.dataLoaded = false;
    this.seekdata = false;
    this.errorValue = 0;
    this.showGenericMessage = false;
    this.getData();
  }

  navigateBack() {
    try {
      this.workflowService.goBackToLastOpenedPageAndUpdateLevel(this.router.routerState.snapshot.root);
    } catch (error) {
      this.logger.log('error', error);
    }
  }

  toggleStatus(event:any){
      this.status = event.checked;
  }

  enableDisableRuleOrJob(action) {
    try {      
      const url = environment.enableDisableRuleOrJob.url;
      const method = environment.enableDisableRuleOrJob.method;
      const params = {};
      params['policyId'] = this.policyId;
      
      params['action'] = action;

      this.adminService.executeHttpAction(url, method, {}, params).subscribe(response => {
          console.log(response,"response");
      }, 
        error => {
        });
    } catch (error) {
      this.logger.log("error", error);
    }
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
      this.logger.log('error', '--- Error while unsubscribing ---');
    }
  }
}
