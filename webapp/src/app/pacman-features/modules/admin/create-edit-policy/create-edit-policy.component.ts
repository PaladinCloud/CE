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

  policyType = ["Federated", "Serverless", "Manage Rule"];
  selectedAssetType = "";
  resolutionUrl = "";
  isDisabled = true;
  currentStepperIndex = 0;
  @ViewChild("policyForm") policyForm: NgForm;
  description = "";

  policyLoader = false;
  pageTitle = 'Create Policy';
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
  paramsList = [[null,null]];
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
  policyFrequencyModeValue: any;
  selectedPolicyType: any;
  policyName = "";
  policyId: any;
  isPolicyIdValid: number;
  policyDetails: any;


  constructor(
    private activatedRoute: ActivatedRoute,
    private router: Router,
    private utils: UtilsService,
    private logger: LoggerService,
    private errorHandling: ErrorHandlingService,
    private uploadService: UploadFileService,
    private refactorFieldsService: RefactorFieldsService,
    private workflowService: WorkflowService,
    private routerUtilityService: RouterUtilityService,
    private adminService: AdminService
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
    this.breadcrumbPresent = 'Create Policy';
    this.backButtonRequired = this.workflowService.checkIfFlowExistsCurrently(
      this.pageLevel
    );
  }

  selectedType(event: any) {
    this.selectedPolicyType = event;
    if (event) {
      this.isDisabled = false;
    }
  }

  createParameters(para: NgForm) {
    this.buildCreatepolicyModel(para.value);
  }

  pageCounter(clickedButton: string) {
    if (clickedButton == 'back') {
      this.currentStepperIndex--;
      return;
    }
    this.currentStepperIndex++;
  }
  dataMarshalling(dataToMarshall) {
    const fullPolicies = [];
    for (let index = 0; index < dataToMarshall.length; index++) {
      const policyItem = {};
      policyItem['createdDate'] = dataToMarshall[index][0];
      policyItem['modifiedDate'] = dataToMarshall[index][1];
      policyItem['resolution'] = dataToMarshall[index][2];
      policyItem['policyDesc'] = dataToMarshall[index][3];
      policyItem['policyId'] = dataToMarshall[index][4];
      policyItem['policyUrl'] = dataToMarshall[index][5];
      policyItem['policyVersion'] = dataToMarshall[index][6];
      policyItem['policyName'] = dataToMarshall[index][7];
      policyItem['numberOfpolicys'] = dataToMarshall[index][8];
      fullPolicies.push(policyItem);
    }
    return fullPolicies;
  }

  nextPage() {
    try {
      if (!this.isLastPage) {
        this.pageNumber++;
        this.showLoader = true;
        // this.getPolicyDetails();
      }
    } catch (error) {
      this.errorMessage = this.errorHandling.handleJavascriptError(error);
      this.logger.log('error', error);
    }
  }

  prevPage() {
    try {
      if (!this.isFirstPage) {
        this.pageNumber--;
        this.showLoader = true;
        // this.getPolicyDetails();
      }

    } catch (error) {
      this.errorMessage = this.errorHandling.handleJavascriptError(error);
      this.logger.log('error', error);
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
      this.getAllAssetGroupNames();
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
        // if (categoryDetail.policyCategory.toLowerCase() == "costoptimization") {
        //   categoryDetail.policyCategory = "cost";
        // }
        // else if (categoryDetail.policyCategory.toLowerCase() == "governance") {
        //   categoryDetail.policyCategory = "operations";
        // }
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

  getAllAssetGroupNames() {
    this.policyPolicyLoader = true;
    this.contentHidden = true;
    this.policyPolicyLoaderFailure = false;
    const url = environment.assetGroupNames.url;
    const method = environment.assetGroupNames.method;
    this.adminService.executeHttpAction(url, method, {}, {}).subscribe(reponse => {
      this.assetGroupNames = reponse[0];
      this.getAllpolicyIds();
    },
      error => {
        this.policyPolicyLoader = false;
        this.contentHidden = true;
        this.policyPolicyLoaderFailure = true;
        this.assetGroupNames = [];
        this.errorMessage = 'apiResponseError';
        this.showLoader = false;
      });
  }

  createOrUpdatepolicy(form: NgForm) {
    this.hideContent = true;
    this.policyLoader = true;
    console.log(form.value);
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
      this.getpolicyCategoryDetails();
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

  onSelectAssetGroup(selectedAssetGroup) {
    this.selectedAssetGroup = selectedAssetGroup;
  }

  private buildCreatepolicyModel(policyForm: any) {
    const PolicyModel = Object();
    PolicyModel.policyType = this.selectedPolicyType;
    PolicyModel.policyName = policyForm.policyName;
    PolicyModel.targetType = this.selectedAssetType;
    PolicyModel.severity = this.selectedSeverity;
    PolicyModel.category = this.selectedCategory;
    PolicyModel.description = policyForm.description;
    PolicyModel.policyRestUrl = this.getpolicyRestUrl(policyForm);
    PolicyModel.assetGroup = this.selectedAssetGroup;
    PolicyModel.policyId = policyForm.policyName + '_' + this.selectedAssetGroup + '_' + this.selectedTargetType;
    PolicyModel.policyId = PolicyModel.policyId.replace(/\s/g, '-');
    PolicyModel.policyExecutable = this.policyJarFileName;
    PolicyModel.policyParams = this.buildpolicyParams();
    PolicyModel.isFileChanged = true;

    const url = environment.createPolicy.url;
    const method = environment.createPolicy.method;
    if (policyForm.policyType === 'Classic') {
      this.currentFileUpload = this.selectedFiles.item(0);
    } else {
      this.currentFileUpload = new File([''], '');
    }
    console.log(PolicyModel, "PolicyModel");
    this.uploadService.pushFileToStorage(url, method, this.currentFileUpload, PolicyModel).subscribe(event => {
      this.policyLoader = false;
      this.ispolicyCreationSuccess = true;
    },
      error => {
        this.ispolicyCreationFailed = true;
        this.policyLoader = false;
      });
  }

  private buildpolicyParams() {
    const policyParms = Object();
    policyParms.params = this.allPolicyParams;
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
  }

  removePolicyParameters(index: number): void {
    if (this.paramsList[index][0] != "policyKey" && this,this.paramsList.length>1)
      this.paramsList.splice(index, 1);
    console.log(this.paramsList, "list");
  }

  addPolicyParameters() {
    this.paramsList.push([null, null]);
  }

  removeJarFileName() {
    this.policyJarFileName = '';
    this.policyJarFile = '';
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

  public onSelectDatasource(datasourceName: any): void {
    this.dataSourceName = datasourceName;
    this.getTargetTypeNamesByDatasourceName(datasourceName);
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
    this.ispolicyIdAvailable(this.selectedPolicyId + '_' + this.selectedpolicyName + '_' + this.selectedTargetType);
  }
  onSelectTargetType(targetType: any) {
    this.selectedTargetType = targetType;
    this.ispolicyIdAvailable(this.selectedPolicyId + '_' + this.selectedpolicyName + '_' + this.selectedTargetType);
  }
  onSelectFrequency(frequencyType: any) {
    this.selectedFrequency = frequencyType;
  }

  getData() {
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
        console.log(this.policyId, "getPolicyDetails");
        delete this.queryParamsWithoutFilter['filter'];
        this.selectedAssetGroup = this.queryParamsWithoutFilter.ag;
        if (this.policyId) {
          this.pageTitle = 'Edit Policy';
          this.breadcrumbPresent = 'Edit Policy';
          this.isCreate = false;
          this.hideContent = true;
          this.getTargetTypeNamesByDatasourceName(this.selectedAssetGroup);
          this.getpolicyCategoryDetails();
          this.isPolicyIdValid = 1;
          this.getPolicyDetails(this.policyId);
        } else {
          this.pageTitle = 'Create New Policy';
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
      this.policyId = this.policyDetails.policyId;
      this.selectedPolicyType = this.policyDetails.policyType;
      this.policyName = this.policyDetails.policyName;
      this.selectedAssetType = this.policyDetails.targetType;
      this.selectedSeverity = this.policyDetails.severity;
      this.selectedCategory = this.policyDetails.category;
      this.description = this.policyDetails.policyDesc;
      this.resolutionUrl = this.policyDetails.resolutionUrl;
      this.allPolicyParams = JSON.parse(this.policyDetails.policyParams)["params"];
      let currentParam = [];
      this.paramsList = [];
      for (let i = this.allPolicyParams.length - 1; i >= 0; i -= 1) {
        if (this.allPolicyParams[i]["key"] == 'severity') {
          this.selectedSeverity = this.allPolicyParams[i]["value"];
          this.allPolicyParams.splice(i, 1);
        } else if (this.allPolicyParams[i]["key"] == 'policyCategory') {
          this.selectedCategory = this.allPolicyParams[i]["value"];
          this.allPolicyParams.splice(i, 1);
        } else {
          currentParam.push(this.allPolicyParams[i]["key"]);
          currentParam.push(this.allPolicyParams[i]["value"]);
          this.paramsList.push(currentParam);
        }
        currentParam = [];
      }
      this.hideContent = false;
    },
      error => {
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

  massageData(data) {
    const refactoredService = this.refactorFieldsService;
    const newData = [];
    const formattedFilters = data.map(function (datam) {
      const keysTobeChanged = Object.keys(datam);
      let newObj = {};
      keysTobeChanged.forEach(element => {
        const elementnew =
          refactoredService.getDisplayNameForAKey(
            element
          ) || element;
        newObj = Object.assign(newObj, { [elementnew]: datam[element] });
      });
      newObj['Actions'] = '';
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
          if (getCols[col].toLowerCase() === 'actions') {
            cellObj = {
              link: true,
              properties: {
                'text-shadow': '0.33px 0',
                'color': '#0047bb'
              },
              colName: getCols[col],
              hasPreImg: false,
              valText: 'Edit',
              imgLink: '',
              text: 'Edit',
              statusProp: {
                'color': '#0047bb'
              }
            };
          } else {
            cellObj = {
              link: '',
              properties: {
                color: ''
              },
              colName: getCols[col],
              hasPreImg: false,
              imgLink: '',
              text: getData[row][getCols[col]],
              valText: getData[row][getCols[col]]
            };
          }
          innerArr[getCols[col]] = cellObj;
          totalVariablesObj[getCols[col]] = '';
        }
        this.outerArr.push(innerArr);
      }
      if (this.outerArr.length > getData.length) {
        const halfLength = this.outerArr.length / 2;
        this.outerArr = this.outerArr.splice(halfLength);
      }
      this.allColumns = Object.keys(totalVariablesObj);
      this.allColumns = ['Policy Id', 'Policy Name', 'Policy Description', 'Policy Version', 'No of policys', 'Actions'];
    } catch (error) {
      this.errorMessage = this.errorHandling.handleJavascriptError(error);
      this.logger.log('error', error);
    }
  }

  goToCreatePolicy() {
    try {
      this.workflowService.addRouterSnapshotToLevel(this.router.routerState.snapshot.root, 0, this.pageTitle);
      this.router.navigate(['../create-edit-policy'], {
        relativeTo: this.activatedRoute,
        queryParamsHandling: 'merge',
        queryParams: {
        }
      });
    } catch (error) {
      this.errorMessage = this.errorHandling.handleJavascriptError(error);
      this.logger.log('error', error);
    }
  }

  goToDetails(row) {
    if (row.col === 'Actions') {
      try {
        this.workflowService.addRouterSnapshotToLevel(this.router.routerState.snapshot.root, 0, this.pageTitle);
        this.router.navigate(['../create-edit-policy'], {
          relativeTo: this.activatedRoute,
          queryParamsHandling: 'merge',
          queryParams: {
            policyId: row.row['Policy Id'].text
          }
        });
      } catch (error) {
        this.errorMessage = this.errorHandling.handleJavascriptError(error);
        this.logger.log('error', error);
      }
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
