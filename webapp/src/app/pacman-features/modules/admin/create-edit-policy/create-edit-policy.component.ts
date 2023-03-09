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

  policyTypeList = ["Federated", "Serverless"];
  selectedAssetType = "";
  resolutionUrl = "";
  isDisabled = true;
  currentStepperIndex = 0;
  description = "";
  readonly = true;
  policyLoader = false;
  pageTitle = 'Create Policy';
  FormHeader = "Policy Overview";
  ispolicyIdValid = -1;
  isCreate;
  allPolicies = [];
  breadcrumbArray = ['policys'];
  breadcrumbLinks = ['policys'];
  breadcrumbPresent;
  outerArr = [];
  dataLoaded = false;
  errorMessage;
  showingArr = ['policyName', 'policyId', 'policyDesc'];
  allColumns = [];
  submitBtn = "Create";
  totalRows = 0;
  currentBucket = [];
  bucketNumber = 0;
  firstPaginator = 1;
  lastPaginator;
  currentPointer = 0;
  seekdata = false;
  showLoader = true;
  allMonthDays = [];
  policyIds = [];
  allEnvironments = [];
  allpolicyParamKeys = [];
  allEnvParamKeys = [];
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
  policyCategories = [];
  policySeverities = ["critical", "high", "medium", "low"];
  allPolicyIds = [];
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
  isRequired = true;

  public labels;
  private previousUrl = '';
  private pageLevel = 0;
  public backButtonRequired;
  private routeSubscription: Subscription;
  private getKeywords: Subscription;
  private previousUrlSubscription: Subscription;
  private downloadSubscription: Subscription;
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
  isPolicyIdValid: number;
  policyDetails: any;
  resolution = "";
  isFileChanged: boolean = false;
  policyUrl = "";
  isManagePolicy: boolean = false;


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

    if (this.currentStepperIndex == 0) {
      this.FormHeader = "Policy Details";
    } else {
      this.FormHeader = "Policy Parameters";
    }
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

  onSubmit(form: NgForm) {
    this.hideContent = true;
    this.policyLoader = true;
    console.log(form.value, "Form");
    this.buildCreatepolicyModel(form.value);
  }

  ispolicyIdAvailable(policyIdKeyword) {
    if (policyIdKeyword.trim().length === 0) {
      this.ispolicyIdValid = -1;
    } else {
      const isKeywordExits = this.policyIds.findIndex(item => policyIdKeyword.trim().toLowerCase() === item.trim().toLowerCase());
      if (isKeywordExits === -1) {
        this.ispolicyIdValid = 1;
      } else {
        this.ispolicyIdValid = 0;
      }
    }
  }

  getAllpolicyIds() {
    this.policyPolicyLoader = true;
    this.contentHidden = true;
    this.policyPolicyLoaderFailure = false;
    const url = environment.allPolicyIds.url;
    const method = environment.allPolicyIds.method;
    this.adminService.executeHttpAction(url, method, {}, {}).subscribe(reponse => {
      this.policyIds = reponse[0];
    },
      error => {
        this.contentHidden = true;
        this.policyPolicyLoader = false;
        this.policyPolicyLoaderFailure = true;
        this.policyIds = [];
        this.errorMessage = 'apiResponseError';
        this.showLoader = false;
      });
  }

  private buildCreatepolicyModel(policyForm: any) {
    const PolicyModel = Object();
    PolicyModel.policyType = this.selectedPolicyType;
    if (this.isPolicyIdValid && policyForm.policyDisplayName == this.policyDisplayName) {
      PolicyModel.policyDisplayName = this.policyDisplayName;
      PolicyModel.policyId = this.policyId;
      PolicyModel.policyName = this.policyName;
    }
    else {
      PolicyModel.policyDisplayName = policyForm.policyDisplayName;
      PolicyModel.policyName = policyForm.policyDisplayName + '_' + this.selectedAssetGroup + '_' + this.selectedAssetType;
      PolicyModel.policyName = PolicyModel.policyName.replace(/\s/g, '-');
      PolicyModel.policyId = PolicyModel.policyName;
    }
    
    PolicyModel.targetType = this.selectedAssetType;
    PolicyModel.severity = this.selectedSeverity;
    PolicyModel.status = this.status?"ENABLED": "DISABLED";
    PolicyModel.category = this.selectedCategory;
    PolicyModel.policyDesc = policyForm.description;
    PolicyModel.resolution = policyForm.resolution;
    PolicyModel.resolutionUrl = policyForm.resolutionUrl;
    PolicyModel.assetGroup = this.selectedAssetGroup;
    PolicyModel.policyExecutable = this.policyJarFileName;
    PolicyModel.policyRestUrl = this.policyUrl;
    PolicyModel.policyParams = this.buildpolicyParams();
    PolicyModel.isFileChanged = this.isFileChanged;
    PolicyModel.policyFrequency = "0 0 ? * MON *";
    PolicyModel.isAutofixEnabled = false;

    if (this.isFileChanged && this.selectedPolicyType === 'Federated') {
      this.currentFileUpload = this.selectedFiles.item(0);
    } else {
      this.currentFileUpload = new File([''], '');
    }
    console.log(PolicyModel, "PolicyModel");
    if (this.selectedPolicyType == "Federated") {
      const isFormValid = this.isValid(PolicyModel);
      this.openDialog(PolicyModel, isFormValid);
    }
    else {
      this.createOrUpdatepolicy(PolicyModel);
    }
  }

  isValid(PolicyModel: any) {
    return true;
  }

  createOrUpdatepolicy(PolicyModel: any) {
    const url = this.isPolicyIdValid ? environment.updatePolicy.url : environment.createPolicy.url;
    const method = environment.createPolicy.method;
    if(this.status){
      this.enableDisableRuleOrJob("Enable");
    }
    else
    this.enableDisableRuleOrJob("Disable");
    this.uploadService.pushFileToStorage(url, method, this.currentFileUpload, PolicyModel).subscribe(event => {
      this.policyLoader = false;
      this.ispolicyCreationSuccess = true;
      this.notificationObservableService.postMessage("Policy " + this.policyDisplayName + (this.isCreate ? " created" : " updated") + " successfully!!", 500, "variant1", "green-info-circle");
    },
      error => {
        this.ispolicyCreationFailed = true;
        this.policyLoader = false;
      });

    this.workflowService.addRouterSnapshotToLevel(this.router.routerState.snapshot.root);
    this.workflowService.clearAllLevels();
    this.router.navigate(['../'], {
      relativeTo: this.activatedRoute,
      queryParams: {
      }
    });
  }


  private buildpolicyParams() {
    const policyParms = Object();
    for (let i = 0; i < this.paramsList.length; i++) {
      if (!this.paramsList[i].key || !this.paramsList[i].value) {
        this.paramsList.splice(i, 1);
      }
    }
    policyParms.params = this.paramsList;
    policyParms.environmentVariables = this.allEnvironments;
    return JSON.stringify(policyParms);
  }

  private getpolicyRestUrl(policyForm) {
    const policyType = policyForm.policyType;
    if (policyType === 'Serverless') {
      return policyForm.resolutionUrl;
    } else {
      return '';
    }
  }

  onSelectCategory(selectedCategory) {
    this.selectedCategory = selectedCategory;
  }

  onSelectSeverity(selectedSeverity) {
    this.selectedSeverity = selectedSeverity;
  }


  onSelectAssetType(selectedAssetType) {
    this.selectedAssetType = selectedAssetType;
  }

  closeErrorMessage() {
    this.ispolicyCreationFailed = false;
    this.hideContent = false;
  }

  onJarFileChange(event) {
    this.selectedFiles = event.target.files;
    this.policyJarFileName = this.selectedFiles[0].name;
    const extension = this.policyJarFileName.substring(this.policyJarFileName.lastIndexOf('.') + 1);
    if (extension !== 'jar') {
      this.removeJarFileName();
    }
    this.isFileChanged = true;
  }

  removePolicyParameters(index: number): void {
    if (this.paramsList[index].key != "policyKey")
      this.paramsList.splice(index, 1);
  }

  addPolicyParameters() {
    this.paramsList.push({
      "key": "",
      "value": "",
      "isEdit": true,
      "isMandatory": false
    });
  }

  removeJarFileName() {
    this.policyJarFileName = '';
    this.policyJarFile = '';
    this.isFileChanged = true;
  }

  openJarFileBrowser(event) {
    const element: HTMLElement = document.getElementById('selectJarFile') as HTMLElement;
    element.click();
  }

  getTargetTypeNamesByDatasourceName(datasourceName) {
    const url = environment.targetTypesByDatasource.url;
    const method = environment.targetTypesByDatasource.method;
    this.adminService.executeHttpAction(url, method, {}, { dataSourceName: datasourceName }).subscribe(reponse => {
      this.showLoader = false;
      this.targetTypesNames = reponse[0];
    },
      error => {
        this.allPolicyIds = [];
        this.errorMessage = 'apiResponseError';
        this.showLoader = false;
      });
  }

  getAllPolicyIds() {
    this.policyPolicyLoader = true;
    this.contentHidden = true;
    this.policyPolicyLoaderFailure = false;
    const url = environment.allPolicyIds.url;
    const method = environment.allPolicyIds.method;
    this.adminService.executeHttpAction(url, method, {}, {}).subscribe(reponse => {
      this.allPolicyIds = reponse[0];
    },
      error => {
        this.contentHidden = true;
        this.policyPolicyLoader = false;
        this.policyPolicyLoaderFailure = true;
        this.allPolicyIds = [];
        this.errorMessage = 'apiResponseError';
        this.showLoader = false;
      });
  }

  public onSelectAssetGroup(selectedAssetGroup: any): void {
    this.selectedAssetGroup = selectedAssetGroup;
    this.getTargetTypeNamesByDatasourceName(selectedAssetGroup);
  }

  addpolicyParameters(parametersInput: any, isEncrypted: any) {
    if (parametersInput.policyKey !== '' && parametersInput.policyValue !== '') {
      this.allPolicyParams.push({ key: parametersInput.policyKey.trim(), value: parametersInput.policyValue.trim(), encrypt: isEncrypted.checked });
      this.allpolicyParamKeys.push(parametersInput.policyKey.trim());
      parametersInput.policyKey = '';
      parametersInput.policyValue = '';
      isEncrypted.checked = false;
    }
  }

  selectedStepperIndex(event: any) {
    this.currentStepperIndex = event;
  }

  onSelectPolicyId(policyId: any) {
    this.selectedPolicyId = policyId;
    this.getPolicyDetails(policyId);
    this.ispolicyIdAvailable(this.selectedPolicyId + '_' + this.selectedpolicyName + '_' + this.selectedAssetType);
  }
  onSelectTargetType(targetType: any) {
    this.selectedTargetType = targetType;
    this.ispolicyIdAvailable(this.selectedPolicyId + '_' + this.selectedpolicyName + '_' + this.selectedAssetType);
  }
  onSelectFrequency(frequencyType: any) {
    this.selectedFrequency = frequencyType;
  }

  getData() {
    this.getpolicyCategoryDetails();
    this.getDatasourceDetails();
    this.getAllPolicyIds();
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
        delete this.queryParamsWithoutFilter['filter'];
        this.dataSourceName = this.queryParamsWithoutFilter.ag;
        if (this.policyId) {
          this.policyTypeList.push("ManagePolicy");
          this.submitBtn = "Update";
          this.pageTitle = 'Edit Policy';
          this.breadcrumbPresent = 'Edit Policy';
          this.isDisabled = false;
          this.readonly = false;
          this.isCreate = false;
          this.hideContent = true;
          this.getpolicyCategoryDetails();
          this.isPolicyIdValid = 1;
          this.getPolicyDetails(this.policyId);
        } else {
          this.pageTitle = 'Create Policy';
          this.breadcrumbPresent = 'Create Policy';
          this.isCreate = true;
          this.getAllPolicyIds();
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
    let url = environment.getPolicyById.url;
    let method = environment.getPolicyById.method;

    this.adminService.executeHttpAction(url, method, {}, { policyId: policyId }).subscribe(reponse => {
      this.policyDetails = reponse[0];
      this.selectedPolicyId = this.policyDetails.policyId;
      this.status = this.policyDetails.status=="ENABLED";
      this.selectedSeverity = this.policyDetails.severity;
      this.selectedCategory = this.policyDetails.category;
      this.policyId = this.policyDetails.policyId;
      if (!this.isCreate) {
        this.policyDisplayName = this.policyDetails.policyDisplayName;
        this.selectedPolicyType = this.policyDetails.policyType;
        if (this.selectedPolicyType == "ManagePolicy") {
          this.isManagePolicy = true;
        }
      }
      this.policyName = this.policyDetails.policyName;
      this.selectedAssetGroup = this.policyDetails.assetGroup;
      this.policyJarFileName = this.policyDetails.policyExecutable;
      this.policyUrl = this.policyDetails.policyRestUrl;
      this.selectedAssetType = this.policyDetails.targetType;
      this.description = this.policyDetails.policyDesc;
      this.resolution = this.policyDetails.resolution;
      this.resolutionUrl = this.policyDetails.resolutionUrl;
      this.allPolicyParams = JSON.parse(this.policyDetails.policyParams)["params"];
      this.paramsList = [];
      for (let i = this.allPolicyParams.length - 1; i >= 0; i -= 1) {
        if (this.allPolicyParams[i]["key"] == 'severity') {
          this.selectedSeverity = this.allPolicyParams[i]["value"];
          this.allPolicyParams.splice(i, 1);
        } else if (this.allPolicyParams[i]["key"] == 'policyCategory') {
          if (this.allPolicyParams[i]["value"].toLowerCase() == "costoptimization") {
            this.allPolicyParams[i]["value"] = "cost";
          }
          else if (this.allPolicyParams[i]["value"].toLowerCase() == "governance") {
            this.allPolicyParams[i]["value"] = "operations";
          }
          this.selectedCategory = this.allPolicyParams[i]["value"];
          this.allPolicyParams.splice(i, 1);
        } else {
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
      }
      if (this.paramsList.length == 0) {
        this.paramsList.push({
          "key": "",
          "value": "",
          "isEdit": false,
          "isMandatory": false
        });
      }
      // if (this.selectedPolicyType == "ManagePolicy") {
      //   this.isDisabled = true;
      // }
      this.getTargetTypeNamesByDatasourceName(this.selectedAssetGroup);
      this.hideContent = false;
    },
      error => {
      });
  }

  openDialog(PolicyModel: any, valid: boolean): void {
    const title = valid ? "Policy Validated Successfully!" : "Policy Validation Failed!"
    const message = valid ? "Policy " + this.policyDisplayName + " has been validated. Confirm to create policy or discard run" : "Please fill the missing policy details";
    const yesButtonLabel = this.isCreate ? "Create" : "Update";
    const noButtonLabel = valid ? "Discard" : "Close";
    const dialogRef = this.dialog.open(DialogBoxComponent, {
      width: '500px',
      data: valid ? { title: title, message: message, yesButtonLabel: yesButtonLabel, noButtonLabel: noButtonLabel } : { title: title, message: message, noButtonLabel: noButtonLabel },
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
