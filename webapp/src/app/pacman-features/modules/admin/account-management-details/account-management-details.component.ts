import { Component, OnInit, OnDestroy, TemplateRef, ViewChild } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { Subscription } from 'rxjs';
import { environment } from './../../../../../environments/environment';
import { WorkflowService } from '../../../../core/services/workflow.service';
import { FormGroup, FormControl, Validators, NgForm } from '@angular/forms';
import { LoggerService } from '../../../../shared/services/logger.service';
import { DataCacheService } from '../../../../core/services/data-cache.service';
import { FilterManagementService } from '../../../../shared/services/filter-management.service';
import { ErrorHandlingService } from '../../../../shared/services/error-handling.service';
import { AssetGroupObservableService } from '../../../../core/services/asset-group-observable.service';
import { DomainTypeObservableService } from '../../../../core/services/domain-type-observable.service';
import { UtilsService } from '../../../../shared/services/utils.service';
import { RouterUtilityService } from '../../../../shared/services/router-utility.service';
import { RefactorFieldsService } from '../../../../shared/services/refactor-fields.service';
import { CommonResponseService } from '../../../../shared/services/common-response.service';
import { WindowRefService } from '../../../services/window.service';
import { FormService } from '../../../../shared/services/form.service';
import { NotificationObservableService } from 'src/app/shared/services/notification-observable.service';
import { MatDialog } from '@angular/material/dialog';
import { DialogBoxComponent } from 'src/app/shared/components/molecules/dialog-box/dialog-box.component';
@Component({
  selector: 'app-account-management-details',
  templateUrl: './account-management-details.component.html',
  styleUrls: ['./account-management-details.component.css']
})

export class AccountManagementDetailsComponent implements OnInit, OnDestroy {
  isCreateFlow = true;
  isupdated;
  isSuccess = false;
  pageTitle: String = 'Create Account';
  fieldArray = [];
  tableSubscription: Subscription;
  breadcrumbDetails = {
    breadcrumbArray: ['Admin', 'Plugins'],
    breadcrumbLinks: ['policies', 'account-management'],
    breadcrumbPresent: 'Details',
  };
  backButtonRequired: boolean;
  url;
  pageLevel = 0;
  errorMessage: string;
  errorValue = 0;
  accountValue;
  agAndDomain = {};

  isFilterRquiredOnPage = false;
  appliedFilters = {
    queryParamsWithoutFilter: {}, /* Stores the query parameter ibject without filter */
    pageLevelAppliedFilters: {} /* Stores the query parameter ibject without filter */
  };
  filterArray = []; /* Stores the page applied filter array */

  routeSubscription: Subscription;
  urlToRedirect: string;
  breadcrumbArray: any;
  breadcrumbLinks: any;
  breadcrumbPresent = 'Plugin Details';
  accountId: any;
  accountName: any;
  accountDetailsList = [];
  violations: any;
  assets: any;
  status: any;
  createdBy: any;
  platform: any;
  chipList = [];
  @ViewChild("deleteAccountRef") deleteAccountRef: TemplateRef<any>;
  createdTime: any;
  platformDisplayName: any = "";


  constructor(private router: Router,
    private activatedRoute: ActivatedRoute,
    private workflowService: WorkflowService,
    private logger: LoggerService,
    private commonResponseService: CommonResponseService,
    private errorHandling: ErrorHandlingService,
    private assetGroupObservableService: AssetGroupObservableService,
    private routerUtilityService: RouterUtilityService,
    public dialog: MatDialog,
    private notificationObservableService: NotificationObservableService,
    ) {
    this.routerParam();
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

  replaceUrl(url, action=''){
    if(this.platform=="redhat"){
      return url.replace("{pluginSelected}", this.platform)
    }else{
      return url.replace("{pluginSelected}/","").replace(action, '');
    }
  }

  deleteAccount(){
    // accountId:string,provider:string
    const url = this.replaceUrl(environment.deleteAccount.url, 'delete');
    const method = environment.deleteAccount.method;
    const queryParams = {
      accountId : this.accountId,
    }
    let nofificationMessage = "";

    this.commonResponseService.getData(url,method,{},queryParams).subscribe(responseData=>{
      try{
        const response = responseData.data;
        const status =response.validationStatus || response.status;        
        if(status.toLowerCase() == "success"){
          nofificationMessage = "Account "+ this.accountId +" has been deleted successfully";
          this.notificationObservableService.postMessage(nofificationMessage,3000,"","check-circle");
          this.router.navigate(['../'], {
            relativeTo: this.activatedRoute,
            queryParamsHandling: "merge",
            state: {
              dataUpdated: true
            }
          });
        }
      }
      catch(error) {
      this.logger.log('error', 'JS Error - ' + error);
      }
    })
  }

  routerParam() {
    try {
      // this.filterText saves the queryparam
      const currentQueryParams = this.routerUtilityService.getQueryParametersFromSnapshot(this.router.routerState.snapshot.root);
      if (currentQueryParams) {
        const FullQueryParams = currentQueryParams;
        const queryParamsWithoutFilter = JSON.parse(JSON.stringify(FullQueryParams));
        this.accountId = queryParamsWithoutFilter.accountId;
        this.getData();
      }
    } catch (error) {
      this.errorMessage = this.errorHandling.handleJavascriptError(error);
      this.logger.log('error', error);
    }
  }

  getData(){
    const url = environment.getAccounts.url;
    const method = environment.getAccounts.method;
    const filterToBePassed = {
        "accountId": [this.accountId]
      }
    const payload = {
      filter: filterToBePassed,
      page: 0,                                                                      
      size: 1,
    };
    this.commonResponseService
      .getData(url, method, payload , {})
      .subscribe(
        responseData => {
          const data = responseData.data.response[0];
          this.processData(data);          
          this.accountName = data.accountName;
        }
      );
  }

  processData(data){
      this.accountId = data.accountId;
      this.accountName = data.accountName;
      this.assets = data.assets;
      this.violations = data.violations;
      this.status = data.accountStatus;
      this.createdBy = data.createdBy.split(".")[0];
      this.platform = data.source;
      // this.createdTime = this.utilityService.calculateDateAndTime(data.createdTime); ;
      this.createdTime = data.createdTime;
      if(this.status.toLowerCase()=="configured")
        this.chipList.push("Online");
      else
        this.chipList.push("Offline");
  }

  getDisplayName(){
    const providerMap = {
      "aws": "AWS",
      "gcp": "GCP",
      "azure": "Azure",
      "qualys": "Qualys",
      "aqua": "Aqua"
    }
    this.platformDisplayName = this.platform?.toLowerCase();
  }

  openModal() {
    const dialogRef = this.dialog.open(DialogBoxComponent, {
      width: '500px',
      data: {
        title: null,
         yesButtonLabel: "Delete",
          noButtonLabel: "Cancel" ,
          template: this.deleteAccountRef,
        },
      });

    dialogRef.afterClosed().subscribe(result => {
      if(result=="yes"){
        this.deleteAccount();
      }
    });
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
    } catch (error) {
      this.logger.log('error', 'JS Error - ' + error);
    }
  }

}

