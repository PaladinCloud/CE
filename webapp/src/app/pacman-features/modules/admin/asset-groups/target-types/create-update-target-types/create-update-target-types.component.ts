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

import { Component, OnInit, OnDestroy } from '@angular/core';
import { environment } from './../../../../../../../environments/environment';

import { Router } from '@angular/router';
import { Subscription } from 'rxjs';
import { UtilsService } from '../../../../../../shared/services/utils.service';
import { LoggerService } from '../../../../../../shared/services/logger.service';
import { ErrorHandlingService } from '../../../../../../shared/services/error-handling.service';


import { WorkflowService } from '../../../../../../core/services/workflow.service';
import { RouterUtilityService } from '../../../../../../shared/services/router-utility.service';
import { AdminService } from '../../../../../services/all-admin.service';

@Component({
  selector: 'app-admin-create-update-target-types',
  templateUrl: './create-update-target-types.component.html',
  styleUrls: ['./create-update-target-types.component.css'],
  providers: [
    LoggerService,
    ErrorHandlingService,
    AdminService
  ]
})
export class CreateUpdateTargetTypesComponent implements OnInit, OnDestroy {
  pageTitle: String = '';
  breadcrumbArray: any = ['Target Types'];
  breadcrumbLinks: any = ['target-types'];
  breadcrumbPresent: any;
  outerArr: any = [];
  filters: any = [];

  targetTypes: any = {
    domain: '',
    category: '',
    name: '',
    desc: '',
    config: '',
    dataSource:[],
    displayName:''
  };

  isCreate: boolean = false;
  successTitle: String = '';
  failedTitle: string = '';
  successSubTitle: String = '';
  isTargetTypeCreationUpdationFailed: boolean = false;
  isTargetTypeCreationUpdationSuccess: boolean = false;
  loadingContent: string = '';
  targetTypeLoader: boolean = false;

  targetTypeName: string = '';

  paginatorSize: number = 25;
  isLastPage: boolean;
  isFirstPage: boolean;
  totalPages: number;
  pageNumber: number = 0;
  showLoader: boolean = true;
  errorMessage: any;

  hideContent: boolean = false;

  filterText: any = {};
  errorValue: number = 0;

  FullQueryParams: any;
  queryParamsWithoutFilter: any;
  urlToRedirect: any = '';
  mandatory: any;
  datasourceDetails = [];
  
  public labels: any;
  private previousUrl: any = '';
  private pageLevel = 0;
  public backButtonRequired;
  private routeSubscription: Subscription;
  private getKeywords: Subscription;
  private previousUrlSubscription: Subscription;

  constructor(
    private router: Router,
    private utils: UtilsService,
    private logger: LoggerService,
    private errorHandling: ErrorHandlingService,
    private workflowService: WorkflowService,
    private routerUtilityService: RouterUtilityService,
    private adminService: AdminService
  ) {

    this.routerParam();
    this.updateComponent();
  }

  ngOnInit() {
    this.urlToRedirect = this.router.routerState.snapshot.url;
    this.backButtonRequired = this.workflowService.checkIfFlowExistsCurrently(
      this.pageLevel
    );
  }

  nextPage() {
    try {
      if (!this.isLastPage) {
        this.pageNumber++;
        this.showLoader = true;
        //this.getPolicyDetails();
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
        //this.getPolicyDetails();
      }

    } catch (error) {
      this.errorMessage = this.errorHandling.handleJavascriptError(error);
      this.logger.log('error', error);
    }
  }

  selectedTargetTypeName: string = '';
  createTargetType(targetTypes) {
    this.loadingContent = 'creating';
    this.hideContent = true;
    this.targetTypeLoader = true;
    this.isTargetTypeCreationUpdationFailed = false;
    this.isTargetTypeCreationUpdationSuccess = false;
    let url = environment.createTargetType.url;
    let method = environment.createTargetType.method;
    this.selectedTargetTypeName = targetTypes.name;
    let targetTypeDetails = {
      domain: this.targetTypes.domain,
      category: this.targetTypes.category,
      name: targetTypes.name,
      desc: targetTypes.desc,
      config: targetTypes.config,
      displayName: targetTypes.displayName,
     dataSource: targetTypes.dataSource[0].text
    }
    this.adminService.executeHttpAction(url, method, targetTypeDetails, {}).subscribe(reponse => {
      this.successTitle = 'Target type Created';
      this.isTargetTypeCreationUpdationSuccess = true;
      this.targetTypeLoader = false;
      this.targetTypes = {
        domain: '',
        category: '',
        name: '',
        desc: '',
        displayName:'',
        config: '',
        dataSource:[]
      };
    },
      error => {
        this.failedTitle = 'Creation Failed';
        this.targetTypeLoader = false;
        this.isTargetTypeCreationUpdationFailed = true;
      })
  }

  updateTargetType(targetTypes) {
    this.loadingContent = 'updating';
    this.hideContent = true;
    this.targetTypeLoader = true;
    this.isTargetTypeCreationUpdationFailed = false;
    this.isTargetTypeCreationUpdationSuccess = false;
    let url = environment.updateTargetType.url;
    let method = environment.updateTargetType.method;
    this.selectedTargetTypeName = targetTypes.name;
    let targetTypeDetails = {
      domain: this.targetTypes.domain,
      category: this.targetTypes.category,
      name: targetTypes.name,
      desc: targetTypes.desc,
      config: targetTypes.config,
      displayName: targetTypes.displayName,
      dataSource: targetTypes.dataSource[0].text
    }
    this.adminService.executeHttpAction(url, method, targetTypeDetails, {}).subscribe(reponse => {
      this.successTitle = 'Target type Updated';
      this.isTargetTypeCreationUpdationSuccess = true;
      this.targetTypeLoader = false;
      this.targetTypes = {
        domain: '',
        category: '',
        name: '',
        desc: '',
        config: '',
        displayName:'',
        dataSource:[]
      };
    },
      error => {
        this.failedTitle = 'Updation Failed';
        this.targetTypeLoader = false;
        this.isTargetTypeCreationUpdationFailed = true;
      })
  }
  onSelectTargetTypeDomain(domain: any) {
    this.targetTypes.domain = domain;
  }
  onSelectTargetTypeCategory(category: any) {
    this.targetTypes.category = category;
  }
   onSelectDataSource(dataSource: any) {
    this.targetTypes.dataSource = dataSource;
  }
  closeErrorMessage() {
    if (this.failedTitle === 'Loading Failed') {
      this.getDomainAndCategoryDetails();
    } else {
      this.hideContent = false;
    }
    this.isTargetTypeCreationUpdationFailed = false;
    this.isTargetTypeCreationUpdationSuccess = false;
  }

  getData() {
    //this.getAllPolicyIds();
  }

  /*
    * This function gets the urlparameter and queryObj 
    *based on that different apis are being hit with different queryparams
    */
  routerParam() {
    try {
      // this.filterText saves the queryparam
      let currentQueryParams = this.routerUtilityService.getQueryParametersFromSnapshot(this.router.routerState.snapshot.root);
      if (currentQueryParams) {

        this.FullQueryParams = currentQueryParams;
        this.queryParamsWithoutFilter = JSON.parse(JSON.stringify(this.FullQueryParams));
        this.targetTypeName = this.queryParamsWithoutFilter.targetTypeName;
        delete this.queryParamsWithoutFilter['filter'];
        if (this.targetTypeName) {
          this.pageTitle = 'Edit Target Type';
          this.breadcrumbPresent = 'Edit Target Type';
          this.isCreate = false;
          this.getDomainAndCategoryDetails();
        } else {
          this.pageTitle = 'Create New Target Type';
          this.breadcrumbPresent = 'Create Target Type';
          this.isCreate = true;
          this.getDomainAndCategoryDetails();
        }

        /**
         * The below code is added to get URLparameter and queryparameter
         * when the page loads ,only then this function runs and hits the api with the
         * filterText obj processed through processFilterObj function
         */
        this.filterText = this.utils.processFilterObj(
          this.FullQueryParams
        );

        //check for mandatory filters.
        if (this.FullQueryParams.mandatory) {
          this.mandatory = this.FullQueryParams.mandatory;
        }

      }
    } catch (error) {
      this.errorMessage = this.errorHandling.handleJavascriptError(error);
      this.logger.log('error', error);
    }
  }

  highlightName: string = '';
  allDomainDetails: any = [];
  allCategoryDetails: any = [];
  getDomainAndCategoryDetails() {
    this.hideContent = true;
    this.targetTypeLoader = true;
    this.loadingContent = 'loading';
    this.highlightName = 'Domain and Category details'
    this.isTargetTypeCreationUpdationFailed = false;
    this.isTargetTypeCreationUpdationSuccess = false;
    let url = environment.domains.url;
    let method = environment.domains.method;
    this.adminService.executeHttpAction(url, method, {}, {}).subscribe(domainsReponse => {
      this.allDomainDetails = this.deMarshalDomain(domainsReponse[0]);
      this.getDatasourceDetails();
      let targetCategoryUrl = environment.getTargetTypesCategories.url;
      let targetCategoryMethod = environment.getTargetTypesCategories.method;
      this.adminService.executeHttpAction(targetCategoryUrl, targetCategoryMethod, {}, {}).subscribe(categoryReponse => {
        this.allCategoryDetails = categoryReponse[0];
        if (this.isCreate) {
          this.hideContent = false;
          this.targetTypeLoader = false;
        } else {
          this.getTargetTypeDetails(this.targetTypeName);
        }
      },
        error => {
          this.errorValue = -1;
          this.outerArr = [];
          this.errorMessage = 'apiResponseError';
          this.showLoader = false;
          this.failedTitle = 'Loading Failed'
          this.loadingContent = 'Loading';
          this.highlightName = 'Domain and Category'
          this.isTargetTypeCreationUpdationFailed = true;
          this.targetTypeLoader = false;
        })
    },
      error => {
        this.errorValue = -1;
        this.outerArr = [];
        this.errorMessage = 'apiResponseError';
        this.showLoader = false;
        this.failedTitle = 'Loading Failed'
        this.loadingContent = 'Loading';
        this.highlightName = 'Domain and Category'
        this.isTargetTypeCreationUpdationFailed = true;
        this.targetTypeLoader = false;
      })
  }
	getDatasourceDetails() {
		const url = environment.datasourceDetails.url;
		const method = environment.datasourceDetails.method;
		this.adminService.executeHttpAction(url, method, {}, {}).subscribe(reponse => {
			const fullDatasourceNames = [];
			for (let index = 0; index < reponse[0].length; index++) {
				let domainItem = {};
				domainItem['id'] = reponse[0][index].dataSourceName;
				domainItem['text'] = reponse[0][index].dataSourceName;
				fullDatasourceNames.push(domainItem);

			}
			this.datasourceDetails = fullDatasourceNames;
		},
			error => {

				this.datasourceDetails = [];
				this.errorMessage = 'apiResponseError';
				this.showLoader = false;
			});
	}
  allSelectedTargettypeDetails: any;
  getTargetTypeDetails(targetTypeName) {
    this.hideContent = true;
    this.targetTypeLoader = true;
    this.loadingContent = 'loading';
    this.highlightName = 'Target Type details'
    this.isTargetTypeCreationUpdationFailed = false;
    this.isTargetTypeCreationUpdationSuccess = false;
    let url = environment.getTargetTypesByName.url;
    let method = environment.getTargetTypesByName.method;
    this.adminService.executeHttpAction(url, method, {}, { targetTypeName: targetTypeName }).subscribe(reponse => {
      this.allSelectedTargettypeDetails = reponse[0];
      this.hideContent = false;
      this.targetTypeLoader = false;
      this.targetTypes.domain = this.allSelectedTargettypeDetails.domain;
      this.targetTypes.category = this.allSelectedTargettypeDetails.category;
      this.targetTypes.name = this.allSelectedTargettypeDetails.targetName;
      this.targetTypes.desc = this.allSelectedTargettypeDetails.targetDesc;
      this.targetTypes.config = this.allSelectedTargettypeDetails.targetConfig;
      this.targetTypes.displayName = this.allSelectedTargettypeDetails.displayName;
      this.targetTypes.dataSource = [{text: this.allSelectedTargettypeDetails.dataSourceName, id:this.allSelectedTargettypeDetails.dataSourceName}];
    },
      error => {
        this.errorValue = -1;
        this.outerArr = [];
        this.errorMessage = 'apiResponseError';
        this.showLoader = false;
        this.failedTitle = 'Loading Failed'
        this.loadingContent = 'Loading';
        this.highlightName = 'Target Type details'
        this.isTargetTypeCreationUpdationFailed = true;
        this.targetTypeLoader = false;
      })
  }


  deMarshalDomain(domainsData) {
    let fullDomains = [];
    for (var index = 0; index < domainsData.length; index++) {
      let domainItem = {};
      domainItem['id'] = domainsData[index].domainName;
      domainItem['text'] = domainsData[index].domainName;
      fullDomains.push(domainItem);
    }
    return fullDomains;
  }
  
  /**
   * This function get calls the keyword service before initializing
   * the filter array ,so that filter keynames are changed
   */

  updateComponent() {
    this.outerArr = [];
    this.showLoader = true;
    this.errorValue = 0;
    this.getData();
  }

  navigateBack() {
    try {
      this.workflowService.goBackToLastOpenedPageAndUpdateLevel(this.router.routerState.snapshot.root);
    } catch (error) {
      this.logger.log('error', error);
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
