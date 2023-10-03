
import {
  Component,
  OnInit,
  ViewChild,
  ElementRef,
  Input,
  OnDestroy,
  HostListener
} from '@angular/core';
import { AssetGroupObservableService } from '../../../../core/services/asset-group-observable.service';
import { ActivatedRoute, UrlSegment, Router } from '@angular/router';
import { ICONS } from './../../../../shared/constants/icons-mapping';
import { DataCacheService } from '../../../../core/services/data-cache.service';
import {
  FormControl,
  FormGroup,
  FormBuilder,
  Validators
} from '@angular/forms';
import { AutorefreshService } from '../../../services/autorefresh.service';
import { CommonResponseService } from '../../../../shared/services/common-response.service';
import { IssueAuditService } from '../../../services/issue-audit.service';
import { environment } from './../../../../../environments/environment';
import { Subscription } from 'rxjs';
import { LoggerService } from '../../../../shared/services/logger.service';
import { WorkflowService } from '../../../../core/services/workflow.service';
import { UtilsService } from '../../../../shared/services/utils.service';
import { DomainTypeObservableService } from '../../../../core/services/domain-type-observable.service';
import { PolicyViolationDescComponent } from '../../../secondary-components/policy-violation-desc/policy-violation-desc.component';
import { CONFIGURATIONS } from '../../../../../config/configurations';
import { PermissionGuardService } from '../../../../core/services/permission-guard.service';
import { AssetTypeMapService } from 'src/app/core/services/asset-type-map.service';
import { AutofixDetails } from './policy-autofix/policy-autofix.component';
import { TableStateService } from 'src/app/core/services/table-state.service';
import { MatDialog } from '@angular/material/dialog';
import { DialogBoxComponent } from 'src/app/shared/components/molecules/dialog-box/dialog-box.component';
import { NotificationObservableService } from 'src/app/shared/services/notification-observable.service';
const emailPattern = /^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,4}$/;

@Component({
  selector: 'app-issue-details',
  templateUrl: './issue-details.component.html',
  styleUrls: ['./issue-details.component.css'],
  providers: [
    CommonResponseService,
    AutorefreshService,
    LoggerService,
    IssueAuditService
  ]
})


export class IssueDetailsComponent implements OnInit, OnDestroy {
  /* global variables for email template and add exception*/
  @ViewChild(PolicyViolationDescComponent) policyViolationDescComponent: PolicyViolationDescComponent;

  public queryValue = '';
  public filteredList = [];
  public idDetailsName = [];
  public elementRef;
  public emailArray = [];
  public users;
  public endDateValue: any;
  public grantedDateValue: any;
  searchTxt = '';
  today = new Date();

  @ViewChild('query') vc: ElementRef;

  dataForm: FormGroup;
  user: FormGroup;
  userEmail: FormGroup;

  /*variables for breadcrumb data*/

  breadcrumbArray: any = [];
  breadcrumbLinks: any = [];
  breadcrumbPresent: any;

  /* variables for handling data*/

  autofixDetails: AutofixDetails;
  issueBlocks: any;
  search: any;
  entity: any;
  tagsData: any;
  totalRows = 0;
  bucketNumber = 0;
  currentBucket: any = [];
  firstPaginator = 1;
  lastPaginator: number;
  currentPointer = 0;
  errorValue = 0;
  addRevokeExemptionErrorMessage;
  tableErrorMessage: any;
  resourceDetails: any;
  issueAudit: any;
  outerArr: any = [];
  issueTopblocks: any = [];
  endDate: any;
  allColumns: any = [];
  descname: any;
  accountIdname: any;
  sevname: any;
  regionname: any;
  policyname: any;
  issuename: any;
  categoryname: any;
  modifiedname: any;
  rulename: any;
  paginatorSize = 1000;
  dataTableData: any = [];
  tableDataLoaded = false;
  resourceIdname: any;
  awsAccountname: any;
  createdname: any;
  accountname: any;
  policyNameVal: any;
  targetname: any;
  recommedData: any;
  numberOfButtons: any = [];
  actionData: any = [];
  arrowkeyLocation = 0;
  assetID: any;
  keysValue: any;
  issueKey: any;

  /*Boolean variables for setting property*/

  showNone = true;
  showOpposite = false;
  showOppositeEmail = false;
  showLoader = true;
  seekdata = false;
  showTransaction = false;
  showTransactionEmail = false;
  showLoadcomplete = false;
  showLoadcompleteEmail = false;
  check = false;
  checkEmail = false;
  checkRecommend = false;
  emailObj = {
    'to': {
      'required': true,
      'validFormat': true
    },
    'from': {
      'required': true,
      'validFormat': true
    }
  };
  showTopSection = false;
  showRecommendantions = false;
  exceptionAdded = false;
  showOppositeRecommend = false;
  showLoadcompleteRecommend = false;
  showRecommend = true;
  showJira = true;
  adminAccess = false;
  showRevoke = true;
  showOppositeJira = false;
  showOppositeRevoke = false;
  showLoadcompleteJira = false;
  showLoadcompleteRevoke = false;
  checkJira = false;
  checkRevoke = false;
  userAdmin = false;
  showJiraData = false;
  showJiraButton = false;
  viewJira = false;
  private previousUrl: any = '';
  selectedDomain: any = '';
  selectedAssetGroup: string;
  public GLOBAL_CONFIG;
  fromEmailID: any;
  public policyViolationId;
  title = "Violation Audit Logs";
  direction = "asc";
  tileList = [];
  columnWidths = {'Date': 1, 'Source': 1, 'Status': 1};
  whiteListColumns = [];
  columnsSortFunctionMap = {
    Date: (a, b, isAsc) => {      
      const ADate = a["Date"].valueText??"default";
      const BDate = b["Date"].valueText??"default";
      return (ADate < BDate ? -1 : 1) * (isAsc ? 1 : -1);
    }
  };

  /*Subscription variables*/

  private getRuleDescSubscription: Subscription;
  private getEmailSubscription: Subscription;
  private getResourceDetailsSubscription: Subscription;
  private getEntityDetailsSubscription: Subscription;
  private getIssueAuditSubscription: Subscription;
  private assetGroupSubscription: Subscription;
  private routeSubscription: Subscription;
  private getUserSubscription: Subscription;
  private getExceptionSubscription: Subscription;
  private getRecommendSubscription: Subscription;
  private getActionDataSubscription: Subscription;
  private getJiraSubscription: Subscription;
  private getRevokeSubscription: Subscription;
  private findJiraSubscription: Subscription;
  private subscriptionDomain: Subscription;
  private autofixDetails$: Subscription;

  emailIcon: any = {
    icon: '../assets/icons/email.svg'
  };
  jiraIcon: any = {
    icon: '../assets/icons/jira.svg'
  };
  downIcon: any = {
    icon: '../assets/png/down.png'
  };
  viewMore: any = {
    icon: '../assets/icons/front-arrow.svg'
  };
  public pageLevel = 0;
  public backButtonRequired;
  issueAssetGroup: any;
  assetTypeMap: any;
  violationAuditLogs = [];
  addExemption: string = "Add Exemption";
  revokeExemption: string = "Revoke Exemption";
  exemptionReason: string = "";
  exemptionDetails: any;
  exemptionRaisedBy: string = "";
  violationAge: string = "";
  violationCreatedDate: Date;
  violationReason: string = "";
  policyViolated: string = "";
  resourceType: string = "";
  policyId: string = "";
  sortColName: any;
  violationModifiedDate: any;
  hasCurrentUserRequested: boolean;

  @HostListener('document:click', ['$event']) handleClick(event) {
    try {
      let clickedComponent = event.target;
      let inside = false;
      do {
        if (clickedComponent === this.elementRef.nativeElement) {
          inside = true;
        }
        clickedComponent = clickedComponent.parentNode;
      } while (clickedComponent);
      if (!inside) {
        this.filteredList = [];
      }
    } catch (e) {
      this.logger.log('error', e);
    }
  }
  constructor(
    private activatedRoute: ActivatedRoute,
    private assetGroupObservableService: AssetGroupObservableService,
    private dataStore: DataCacheService,
    private formBuilder: FormBuilder,
    private issueAuditService: IssueAuditService,
    private commonResponseService: CommonResponseService,
    private router: Router,
    private myElement: ElementRef,
    private logger: LoggerService,
    private workflowService: WorkflowService,
    private utilityService: UtilsService,
    private domainObservableService: DomainTypeObservableService,
    private permissions: PermissionGuardService,
    private assetTypeMapService:AssetTypeMapService,
    private tableStateService: TableStateService,
    public dialog: MatDialog,
    private notificationObservableService: NotificationObservableService
  ) {
    try {
      this.elementRef = this.myElement;
      this.GLOBAL_CONFIG = CONFIGURATIONS;
      this.fromEmailID = this.GLOBAL_CONFIG && this.GLOBAL_CONFIG.optional && this.GLOBAL_CONFIG.optional.pacmanIssue && this.GLOBAL_CONFIG.optional.pacmanIssue.emailPacManIssue && this.GLOBAL_CONFIG.optional.pacmanIssue.emailPacManIssue.ISSUE_EMAIL_FROM_ID;
      this.routeSubscription = this.activatedRoute.params.subscribe(params => {
        this.policyViolationId = params['issueId'];
      });
      this.whiteListColumns = Object.keys(this.columnWidths);
      this.assetGroupSubscription = this.assetGroupObservableService
        .getAssetGroup()
        .subscribe(assetGroupName => {
          this.backButtonRequired = this.workflowService.checkIfFlowExistsCurrently(
            this.pageLevel
          );
          this.selectedAssetGroup = assetGroupName;
        });

      this.subscriptionDomain = this.domainObservableService
        .getDomainType()
        .subscribe(domain => {
          this.selectedDomain = domain;
          this.updateComponent();
        });
    } catch (e) {
      this.logger.log('error', e);
    }
  }

  ngOnInit() {
    try {
      this.adminAccess = this.permissions.checkAdminPermission();
      if(!this.adminAccess){
        this.addExemption = "Request Exemption"
        this.revokeExemption = "Revoke Request"
      }

      this.dataForm = this.formBuilder.group({
        date: ''
      });

      this.user = new FormGroup({
        name: new FormControl('', [
          Validators.required,
          Validators.minLength(5)
        ])
      });

      this.userEmail = new FormGroup({
        ename: new FormControl('', [
          Validators.required,
          Validators.minLength(6)
        ]),
        fname: new FormControl('', [Validators.required, Validators.minLength(6)])
      });

      const breadcrumbInfo = this.workflowService.getDetailsFromStorage()["level0"];

    if (breadcrumbInfo) {
      this.breadcrumbArray = breadcrumbInfo.map(item => item.title);
      this.breadcrumbLinks = breadcrumbInfo.map(item => item.url);
    }
      this.breadcrumbPresent = 'Violation Details';

      // this.getData();
    } catch (error) {
      this.logger.log('error', error);
    }
  }

  /* Function to repaint component */

  updateComponent() {
    this.showNone = true;
    this.outerArr = [];
    this.showOpposite = false;
    this.showOppositeEmail = false;
    this.showLoader = true;
    this.seekdata = false;
    this.showTransaction = false;
    this.showTransactionEmail = false;
    this.showLoadcomplete = false;
    this.showLoadcompleteEmail = false;
    this.showOppositeRecommend = false;
    this.showLoadcompleteRecommend = false;
    this.check = false;
    this.checkEmail = false;
    this.dataTableData = [];
    this.tableDataLoaded = false;
    this.checkRecommend = false;
    this.emailObj = {
      'to': {
        'required': true,
        'validFormat': true
      },
      'from': {
        'required': true,
        'validFormat': true
      }
    };
    this.showTopSection = false;
    this.issueBlocks = false;
    this.showRecommendantions = false;
    this.showJira = true;
    this.showRevoke = true;
    this.showOppositeJira = false;
    this.showLoadcompleteJira = false;
    this.showLoadcompleteRevoke = false;
    this.checkJira = false;
    this.checkRevoke = false;
    this.showOppositeRevoke = false;
    this.errorValue = 0;
    this.showJiraData = false;
    this.showJiraButton = false;
    this.viewJira = false;
    this.getData();
  }

  getData() {
    this.getRuleDesc();
    this.getUsers();
    this.findJiraExist();
    this.getAutofixDetails();
  }

  getRuleDesc(): any {
    try {
      this.issueTopblocks = [];

      if (this.policyViolationId) {
        const queryParams = {
          ag: this.selectedAssetGroup,
          issueId: this.policyViolationId
        };

        const ruleDescUrl = environment.ruleDesc.url;
        const ruleDescMethod = environment.ruleDesc.method;

        this.getRuleDescSubscription = this.commonResponseService
          .getData(ruleDescUrl, ruleDescMethod, {}, queryParams)
          .subscribe(
            response => {
              try {
                let statusIcon;
                this.showLoader = false;
                if (!this.utilityService.isObjectEmpty(response)) {
                  this.issueBlocks = response;
                  this.violationModifiedDate = this.issueBlocks?.violationModifiedDate;
                  this.exemptionDetails = response["exemption"];
                  this.hasCurrentUserRequested = this.exemptionDetails.exemptionRaisedBy == this.dataStore.getUserDetailsValue().getEmail();
                  this.exemptionRaisedBy = this.exemptionDetails.exemptionRaisedBy.split('.')[0];
                  this.exemptionRaisedBy = this.exemptionRaisedBy.charAt(0).toUpperCase() + this.exemptionRaisedBy.slice(1);
                  // changing the time using utils func
                  if (this.issueBlocks['violationCreatedDate']) {
                    this.violationCreatedDate = new Date(this.issueBlocks['violationCreatedDate']);
                    const [time, units] = this.daysBetween(this.violationCreatedDate, new Date());
                    this.issueBlocks["Age"] = time + ' ' + units;
                    this.violationAge = time + ' ' + units;
                  }
                  if (this.issueBlocks['assetGroup'] !== undefined) {
                    this.issueAssetGroup = this.issueBlocks['assetGroup'];
                  }
                  this.assetID = this.issueBlocks.resouceViolatedPolicy;
                  if (this.issueBlocks.status !== undefined) {
                    this.exceptionAdded = (this.issueBlocks.status === 'exempted');
                    if (this.exceptionAdded) {
                      this.issueBlocks.status = "exempt";
                        statusIcon = '../assets/icons/Lock-Closed.svg';
                    }
                    else {
                         statusIcon = '../assets/icons/Lock-Open.svg';
                    }
                    const obj = {
                      header: 'Status',
                      footer: this.issueBlocks.status,
                      img: statusIcon
                    };
                    this.issueTopblocks.push(obj);
                  }
                  this.violationReason = this.issueBlocks.violationReason;
                  this.policyViolated = this.issueBlocks.policyViolated;
                  this.policyId = this.issueBlocks.policyId;
                  if (this.issueBlocks.severity !== undefined) {
                    const obj = {
                      header: 'Severity',
                      footer: this.issueBlocks.severity,
                      img: '../assets/icons/violations-'+this.issueBlocks.severity.toLowerCase()+'-icon.svg'
                    };
                    this.issueTopblocks.push(obj);
                  }

                  if (this.issueBlocks.policyCategory !== undefined) {
                    let obj;
                    if (
                      this.issueBlocks.policyCategory === 'governance' ||
                      this.issueBlocks.policyCategory === 'Governance'
                    ) {
                      obj = {
                        header: 'Policy Category',
                        footer: this.issueBlocks.policyCategory,
                        img: '../assets/icons/Governance.svg'
                      };
                    } else {
                      obj = {
                        header: 'Policy Category',
                        footer: this.issueBlocks.policyCategory,
                        img: '../assets/icons/Security.svg'
                      };
                    }
                    this.issueTopblocks.push(obj);
                  }

                  this.assetTypeMapService.getAssetMap().subscribe(assetTypeMap=>{
                    this.assetTypeMap = assetTypeMap;
                  })

                  if (this.issueBlocks.resourceType !== undefined) {
                    this.resourceType = this.issueBlocks.resourceType;
                    const obj = {
                        header: 'Asset Type',
                        footer: this.assetTypeMap.get(this.issueBlocks.resourceType),
                        img: '../assets/icons/'+this.issueBlocks.assetGroup+'-color.svg'
                    };
                    this.issueTopblocks.push(obj);
                  }

                  this.showTopSection = true;
                  this.getEntityDetails(this.issueBlocks);
                  this.getIssueAudit(this.issueBlocks);
                } else {
                  this.showLoader = false;
                  this.seekdata = true;
                  this.issueBlocks = false;
                }
              } catch (e) {
                this.showLoader = false;
                this.seekdata = true;
                this.issueBlocks = false;
              }

              // this.getRecommend();
            },
            error => {
              this.showLoader = false;
              this.seekdata = true;
              this.issueBlocks = false;
            }
          );
      }
    } catch (error) {
      this.logger.log('error', error);
    }
  }

  daysBetween( date1, date2 ) {
    let units;
    let timeDiff;
    //Get 1 day in milliseconds
    const one_day = 1000*60*60*24;
    const one_hour = one_day/24;
    const one_min = one_hour/60;
    const one_sec = one_min/60;

    // Convert both dates to milliseconds
    const date1_ms = date1.getTime();
    const date2_ms = date2.getTime();

    // Calculate the difference in milliseconds
    const difference_ms = date2_ms - date1_ms;

    if(difference_ms<one_min){
      units = 'seconds';
      timeDiff = difference_ms/one_sec;
    }else if(difference_ms<one_hour){
      units = 'minutes';
      timeDiff = difference_ms/one_min;
    }else if(difference_ms<one_day){
      units = 'hours';
      timeDiff = difference_ms/one_hour;
    }else{
      units = 'days';
      timeDiff = difference_ms/one_day;
    }
    // Convert back to days and return
    return [Math.round(timeDiff), units];
  }

  navigateBack() {
    try {
      this.workflowService.goBackToLastOpenedPageAndUpdateLevel(
        this.router.routerState.snapshot.root
      );
    } catch (error) {
      this.logger.log('error', error);
    }
  }

  select(item) {
    try {
      this.queryValue = item;
      this.filteredList = [];
      item = this.retrieveEmailFromSelectedItem(this.queryValue);
      this.emailArray.push(item);
      this.queryValue = '';
      if (this.emailArray.length < 1) {
        this.emailObj.to.required = false;
      } else {
        this.emailObj.to.required = true;
      }
    } catch (e) {
      this.logger.log('error', e);
    }
  }

  navigateTo(destination, id1?, id2?) {
    this.workflowService.addRouterSnapshotToLevel(
      this.router.routerState.snapshot.root, 0, this.breadcrumbPresent
    );
    try {
      if (destination === "asset details") {
        const resourceId = encodeURIComponent(id1);
        const resourceType = encodeURIComponent(id2);
        this.workflowService.navigateTo({
          urlArray: ["../../../../", "assets", "asset-list", resourceType, resourceId],
          queryParams: {},
          relativeTo: this.activatedRoute,
          currPagetitle: this.breadcrumbPresent,
          nextPageTitle: "Asset Details"
        });
        // window.open("/pl/assets/asset-list/"+resourceType+"/"+resourceId);
      } else if (destination === "policy knowledgebase details") {
        const policyId = encodeURIComponent(id1);
        this.router.navigate(["../../../policy-knowledgebase-details", policyId, "false"], {
          relativeTo: this.activatedRoute,
          queryParamsHandling: "merge",
        });
        // window.open("/pl/compliance/policy-knowledgebase-details/"+policyId+"/false", "_blank");
      }
    } catch (e) {
      this.logger.log("error", e);
    }
  }

  // TODO: Remove unused variables
  // getRecommend() {
  //   try {
  //     const Url = environment.recommendDetails.url;
  //     const Method = environment.recommendDetails.method;
  //     let queryparams;
  //     if (this.issueBlocks) {
  //       queryparams = {
  //         targetType: this.issueBlocks.resourceType,
  //         policyId: this.issueBlocks.policyId
  //       };
  //     }

  //     this.getRecommendSubscription = this.commonResponseService
  //       .getData(Url, Method, {}, queryparams)
  //       .subscribe(
  //         response => {
  //           const self = this;
  //           setTimeout(() => {
  //             self.checkRecommend = true;
  //             self.showLoadcompleteRecommend = true;
  //           }, 4500);

  //           this.numberOfButtons = response;
  //           this.recommedData = response[0];
  //           for (let i = 0; i < this.numberOfButtons.length; i++) {
  //             this.actionData.push(this.numberOfButtons[i].actionApiUrl);
  //           }
  //           if (this.recommedData !== undefined) {
  //             this.showRecommendantions = true;
  //           }
  //         },
  //         error => {
  //           const self = this;
  //           setTimeout(() => {
  //             self.checkRecommend = false;
  //             self.showLoadcompleteRecommend = true;
  //           }, 4500);

  //         }
  //       );
  //   } catch (e) {
  //     this.logger.log('error', e);
  //   }
  // }

  showButtons(index) {
    try {
      const Method = 'GET';
      const Url = this.actionData[index];

      this.getActionDataSubscription = this.commonResponseService
        .getData(Url, Method, {}, {})
        .subscribe(response => {
        }, error => {
        });
    } catch (e) {
      this.logger.log('error', e);
    }
  }

  getEntityDetails(data): any {
    try {
      const resourceId = data.resouceViolatedPolicy;
      const queryParams = {
        ag: this.selectedAssetGroup,
        resourceId: resourceId
      };
      const resourceDetailsUrl = environment.resourceDetails.url;
      const resourceDetailsMethod = environment.resourceDetails.method;
      this.getEntityDetailsSubscription = this.commonResponseService
        .getData(resourceDetailsUrl, resourceDetailsMethod, {}, queryParams)
        .subscribe(
          response => {
            if (
              !this.utilityService.checkIfAPIReturnedDataIsEmpty(
                response.response
              )
            ) {
              const enityData = response.response[0];
              this.chunckTags(enityData);
            }
          },
          error => {
          }
        );
    } catch (e) {
      this.logger.log('error', e);
    }
  }

  callNewSearch() {
    try {
      this.bucketNumber = 0;
      this.currentBucket = [];
      this.getIssueAudit();
    } catch (e) {
      this.logger.log('error', e);
    }
  }

  chunckTags(data) {
    try {
      const dataObj = JSON.parse(JSON.stringify(data));
      const dataValue = JSON.parse(JSON.stringify(data));
      const keys = Object.keys(dataValue);
      keys.forEach(element => {
        if (element.indexOf('tags') > -1) {
          delete dataValue[element];
        }
      });
      this.entity = dataValue;
      this.tagsData = dataObj;
    } catch (e) {
      this.logger.log('error', e);
    }
  }

  getIssueAudit(data?: any): any {
    try {
      let issueId;
      this.routeSubscription = this.activatedRoute.params.subscribe(params => {
        issueId = params['issueId'];
      });

      const email = this.dataStore.getUserDetailsValue().getEmail();

      const payload = {
        user: email,
        dataSource: this.issueAssetGroup,
        from: this.bucketNumber * this.paginatorSize,
        issueId: issueId,
        size: this.paginatorSize,
        targetType: this.issueBlocks.resourceType,
        searchtext: this.searchTxt,
        filter: { domain: this.selectedDomain }
      };
      this.errorValue = 0;
      const issueAuditUrl = environment.issueAudit.url;
      const issueAuditMethod = environment.issueAudit.method;
      this.getIssueAuditSubscription = this.issueAuditService
        .getData(payload, issueAuditUrl, issueAuditMethod)
        .subscribe(
          response => {            
            this.errorValue = 1;
            this.tableDataLoaded = true;
            this.issueAudit = response.data.response;
            this.dataTableData = this.issueAudit;
            this.firstPaginator = this.bucketNumber * this.paginatorSize + 1;
            this.lastPaginator =
              this.bucketNumber * this.paginatorSize + this.paginatorSize;
            this.currentPointer = this.bucketNumber;

            data = this.massageData(this.issueAudit);
            this.currentBucket[this.bucketNumber] = data;
            const processedData = this.utilityService.processTableData(this.issueAudit);
            this.processData(processedData);
          },
          error => {
            this.errorValue = -1;
            this.tableErrorMessage = "noDataAvailable";
          }
        );
    } catch (e) {
      this.logger.log('error', e);
    }
  }

  massageData(data) {
    try {
      this.issueBlocks['violationModifiedDate'] = data[0].auditdate;
      this.issueBlocks['status'] = data[0].status.toLowerCase() == "enforced" ? "exempt": data[0].status;
      for (let i = 0; i < data.length; i++) {
        data[i][`Date`] = data[i].auditdate;
        data[i][`Source`] = data[i].createdBy?.split("@")[0];
        data[i][`Status`] = data[i].status;

        delete data[i].auditdate;
        delete data[i].datasource;
        delete data[i].status;
        delete data[i]._id;
        delete data[i].createdBy;
      }

      return data;
    } catch (e) {
      this.logger.log('error', e);
    }
  }

  bodyClicked() {
    this.policyViolationDescComponent.closePopup();
  }

  processData(data) {
    try {
      this.outerArr = data;
      let firsOpenAuditLog = false;
      let processedData = this.outerArr.reverse().filter((obj, index) => {
        this.outerArr[index].Date.isDate = true;
         if(this.outerArr[index].Date.valueText >= this.issueBlocks["violationCreatedDate"]){
          if(!firsOpenAuditLog){  
            firsOpenAuditLog = true;
              return true;
            } 
            else{
              return index !== 0  && this.outerArr[index-1].Status.valueText.toLowerCase() != obj.Status.valueText.toLowerCase();
            }
          }
          return false;
      });

      processedData = processedData.reverse();      
      this.violationAuditLogs = processedData;
      if(this.violationAuditLogs.length>0){
        this.issueBlocks[
          'violationModifiedDate'
        ] = this.violationAuditLogs[0].Date.valText;
        this.tableErrorMessage = '';
      }else{
        this.tableErrorMessage = 'noDataAvailable';
      }
      this.totalRows = this.violationAuditLogs.length;
      if (this.lastPaginator > this.totalRows) {
        this.lastPaginator = this.totalRows;
      }
    } catch (e) {
      this.logger.log('error', e);
    }
  }

  calculateDateEmail(_JSDate) {
    try {
      const date = new Date(_JSDate);
      const year = date.getFullYear().toString();
      const month = date.getMonth() + 1;
      let monthString;
      if (month < 10) {
        monthString = '0' + month.toString();
      } else {
        monthString = month.toString();
      }
      const day = date.getDate();
      let dayString;
      if (day < 10) {
        dayString = '0' + day.toString();
      } else {
        dayString = day.toString();
      }
      return year + '-' + monthString + '-' + dayString;
    } catch (e) {
      this.logger.log('error', e);
    }
  }

  // below fns are related to RHS of page // not to be removed

  removeData(index): any {
    try {
      this.emailArray.splice(index, 1);
      if (this.emailArray.length < 1) {
        this.emailObj.to.required = false;
      } else {
        this.emailObj.to.required = true;
      }
    } catch (e) {
      this.logger.log('error', e);
    }
  }

  showOtherDivJira() {
    try {
      this.showJira = !this.showJira;
      this.showOppositeJira = !this.showOppositeJira;
    } catch (e) {
      this.logger.log('error', e);
    }
  }
  clearViolationsPreservedData(){
    this.tableStateService.clearPreservedData("Violations");
  }

  revokeException() {
    if(!this.adminAccess){
      this.handleRequest("revoke request");
    }else {
      try {
        const Url = environment.revokeIssueException.url;
        const Method = environment.revokeIssueException.method;
        const email = this.dataStore.getUserDetailsValue().getEmail();
        const payload = {
          issueIds: [this.policyViolationId],
          revokedBy: email.split("@")[0]
        };
        const queryParams = {
          ag: this.selectedAssetGroup
        }
        this.getRevokeSubscription = this.commonResponseService
          .getData(Url, Method, payload, queryParams)
          .subscribe(
            response => {
              if(response.message==='success'){
                this.clearViolationsPreservedData();
                setTimeout(() => {
                  this.checkRevoke = false;
                  this.showLoadcompleteRevoke = true;
                }, 100);
                this.updateComponent();
              }
            },
            error => {
              setTimeout(() => {
                this.checkRevoke = true;
                this.showLoadcompleteRevoke = true;
              }, 100);
            }
          );
      } catch (e) {
        this.logger.log('error', e);
      }
    }
  }

  createJira() {
    try {
      let Data = this.dataStore.getUserDetailsValue();
      Data = Data.getEmail();
      const Url = environment.createJira.url;
      const Method = environment.createJira.method;
      const payload = {
        email: Data,
        title: 'JIRA ticket created with issueId ' + this.assetID,
        description: [
          {
            heading: 'Issue Details',
            details: {},
            highlight: 'true'
          },
          {
            heading: 'Entity Details',
            details: {}
          },
          {
            heading: 'Rule Parameters',
            details: {}
          }
        ],
        issue_id: this.assetID
      };

      this.keysValue = Object.keys(this.issueBlocks);
      let count = 0;
      for (let i = 0; i < this.keysValue.length; i++) {
        if (i >= 0 && i < 6) {
          count = 0;
        } else if (i >= 6 && i < 12) {
          count = 1;
        } else {
          count = 2;
        }
        if (this.keysValue[i] !== 'nonDisplayableAttributes') {
          if (this.keysValue[i] === 'description') {
            payload.description[0].details[
              this.keysValue[i]
            ] = this.issueBlocks[this.keysValue[i]];
          } else if (this.keysValue[i] === 'RuleCategory') {
            payload.description[2].details[
              this.keysValue[i]
            ] = this.issueBlocks[this.keysValue[i]];
          } else {
            payload.description[count].details[
              this.keysValue[i]
            ] = this.issueBlocks[this.keysValue[i]];
          }
        }
      }

      this.getJiraSubscription = this.commonResponseService
        .getData(Url, Method, payload, {})
        .subscribe(
          response => {
            this.issueKey = response.data.issueKey;
            if (response.data.exists === true) {
              this.viewJira = true;
            }
            setTimeout(() => {
              return this.hideJira();
            }, 4500);
          },
          error => {
            this.viewJira = false;
            const self = this;
            setTimeout(() => {
              self.checkJira = true;
              self.showLoadcompleteJira = true;
            }, 4500);
          }
        );
    } catch (e) {
      this.logger.log('error', e);
    }
  }

  onSubmit(){
  if(!this.adminAccess){
    this.handleRequest("request");
  }
  else{
    try {
      const date = new Date();
      const endDateValue = this.utilityService.getUTCDate(this.endDate);
      const grantedDateValue = this.utilityService.getUTCDate(date);
      const email = this.dataStore.getUserDetailsValue().getEmail();
      const payload = {
        createdBy: email,
        exceptionEndDate: endDateValue,
        exceptionGrantedDate: grantedDateValue,
        exceptionReason: this.exemptionReason,
        issueIds: [this.policyViolationId],
      };
      const queryParams = {
        ag: this.selectedAssetGroup
      }
      const exceptionUrl = environment.addIssueException.url;
      const exceptionMethod = environment.addIssueException.method;
      this.getExceptionSubscription = this.commonResponseService
        .getData(exceptionUrl, exceptionMethod, payload, queryParams)
        .subscribe(
          response => {
            if(response.message==='success'){
              this.clearViolationsPreservedData();
              this.check = true;
              this.showLoadcomplete = true;
              this.showTopSection = false;
              this.updateComponent();
            }
            else{
              const message = "Adding an exemption has failed!"
              this.notificationObservableService.postMessage(message,3000);
            }
          },
          error => {
            this.logger.log("error test", error);
            this.addRevokeExemptionErrorMessage = error.error.message;
            this.check = false;
            this.showLoadcomplete = true;
          }
        );

      this.user.reset();
    } catch (e) {
      this.logger.log('error', e);
    }
  }
}

  handleRequest(action:string) {
    try {
      const date = new Date();
      const endDateValue = this.utilityService.getUTCDate(this.endDate);
      const grantedDateValue = this.utilityService.getUTCDate(date);
      const email = this.dataStore.getUserDetailsValue().getEmail();
      let payload = {
        createdBy: email,
        assetGroup: this.selectedAssetGroup,
        issueIds: [this.policyViolationId],
        action: "CREATE_EXEMPTION_REQUEST"
      };
      if(action == "grant"){
          payload["createdBy"] = this.exemptionDetails.exemptionRaisedBy;
          payload["approvedBy"] = email;
          payload["exceptionEndDate"] = this.exemptionDetails.exemptionRaisedExpiringOn;
          payload["exceptionReason"] = this.exemptionDetails.reasonToExempt,
          payload.action = "APPROVE_EXEMPTION_REQUEST"
      }
      else if(action == "deny"){
        payload.action = "CANCEL_EXEMPTION_REQUEST";
      }
      else if(action == "request"){
        payload["exceptionEndDate"] = endDateValue;
        payload["exceptionReason"] = this.exemptionReason;
      } else if(action == "revoke request"){
        payload.action = "REVOKE_EXEMPTION_REQUEST";
      } 
      
      const queryParams = {}
      const exceptionUrl = environment.createRevokeExemption.url;
      const exceptionMethod = environment.createRevokeExemption.method;
      this.getExceptionSubscription = this.commonResponseService
        .getData(exceptionUrl, exceptionMethod, payload, queryParams)
        .subscribe(
          response => {
            this.clearViolationsPreservedData();
            this.check = true;
            this.showLoadcomplete = true;
            this.showTopSection = false;
            if(response?.data?.status?.toLowerCase() == "success"){              
              this.updateComponent();
            }else{              
              this.dialog.open(DialogBoxComponent, {
                width: '600px',
                data: {
                  title: `Error - ${Object.values(response?.data?.failureReason).toString()}`,
                }
              })
            }
          },
          error => {
            this.logger.log("error test", error);
            this.addRevokeExemptionErrorMessage = error.error.message;
            this.check = false;
            this.showLoadcomplete = true;
          }
        );

      this.user.reset();
    } catch (e) {
      this.logger.log('error', e);
    }
  }

  keyDown(event: KeyboardEvent) {
    try {
      switch (event.keyCode) {
        case 38: // this is the ascii of arrow up
          this.arrowkeyLocation--;
          break;
        case 40: // this is the ascii of arrow down
          this.arrowkeyLocation++;
          break;
        case 13: // this is the ascii of enter
          if (this.filteredList.length > 0) {
            this.queryValue = this.filteredList[this.arrowkeyLocation];
            this.filteredList = [];
            this.queryValue = this.retrieveEmailFromSelectedItem(this.queryValue);
            this.emailArray.push(this.queryValue);
          } else if (this.queryValue.length > 0) {
              if (this.validateEmailInput(this.queryValue)) {
                this.emailArray.push(this.queryValue);
              }
          }
          this.queryValue = '';
          if (this.emailArray.length < 1) {
            this.emailObj.to.required = false;
          } else {
            this.emailObj.to.required = true;
          }
      }
    } catch (e) {
      this.logger.log('error', e);
    }
  }

  keyEvent(event: KeyboardEvent, item) {
    try {
      switch (event.keyCode) {
        case 13: // this is the ascii of enter
          this.queryValue = item;
          this.filteredList = [];
          item = this.retrieveEmailFromSelectedItem(item);
          this.emailArray.push(item);
          this.queryValue = '';
          if (this.emailArray.length < 1) {
            this.emailObj.to.required = false;
          } else {
            this.emailObj.to.required = true;
          }
      }
    } catch (e) {
      this.logger.log('error', e);
    }
  }

  onSubmitemail() {
    try {
      // reset values
      this.emailObj = {
        'to': {
          'required': true,
          'validFormat': true
        },
        'from': {
          'required': true,
          'validFormat': true
        }
      };
      // to address validation
      if (this.emailArray.length < 1 && this.queryValue.length <= 0) {
        this.emailObj.to.required = false;
        return;
      } else {
        this.emailObj.to.required = true;
        if (this.emailArray.length < 1 && this.queryValue.length > 0) {
          if (this.validateEmailInput(this.queryValue)) {
            this.emailArray.push(this.queryValue);
          } else {
            this.emailObj.to.validFormat = false;
            return;
          }
        }
      }
      // from address validation
      if (this.fromEmailID.length > 0) {
        if (!this.validateEmailInput(this.fromEmailID)) {
          this.emailObj.from.validFormat = false;
          return;
        }
      } else {
        this.emailObj.from.required = false;
        return;
      }

      this.showTransactionEmail = true;

      this.postEmail(this.emailArray);

      this.emailArray = [];
      this.userEmail.reset();
    } catch (e) {
      this.logger.log('error', e);
    }
  }

  postEmail(emailArrayList): any {
    try {
      const locationValue =
        window.location.href + '?ag=' + this.selectedAssetGroup;

      const emailUrl = environment.email.url;
      const emailMethod = environment.email.method;
      const resourceId = encodeURIComponent(encodeURIComponent(this.issueBlocks.resouceViolatedPolicy));
      const resourceType = encodeURIComponent(encodeURIComponent(this.issueBlocks.resourceType));
      const assetGroup = encodeURIComponent(encodeURIComponent(this.selectedAssetGroup));
      const domainName = encodeURIComponent(encodeURIComponent(this.selectedDomain));
      const ruleID = encodeURIComponent(encodeURIComponent(this.issueBlocks.policyId));
      const payload = {
        attachmentUrl: this.GLOBAL_CONFIG.optional.pacmanIssue.emailPacManIssue.ISSUE_MAIL_TEMPLATE_URL + '/html.handlebars',
        from: this.fromEmailID,
        mailTemplateUrl: this.GLOBAL_CONFIG.optional.pacmanIssue.emailPacManIssue.ISSUE_MAIL_TEMPLATE_URL + '/html.handlebars',
        placeholderValues: {
          link: locationValue,
          name: name,
          statusName: 'Status',
          statusFooter: this.issueBlocks.status,
          severityName: 'Severity',
          severityFooter: this.issueBlocks.severity,
          targetTypeName: 'Asset Type',
          targetTypeFooter: this.issueBlocks.resourceType,
          policyCategoryName: 'Policy Category',
          policyCategoryFooter: this.issueBlocks.policyCategory,
          policyViolated: this.issueBlocks.policyViolated,
          policyDescription: this.issueBlocks.policyDescription,
          violationReason: this.issueBlocks.violationReason,
          resourceId: this.issueBlocks.resouceViolatedPolicy,
          resourceUrl: window.location.origin+'/pl/assets/asset-list/'+resourceType+'/'+resourceId+'?ag='+assetGroup+'&domain='+domainName,
          policyUrl: window.location.origin+'/pl/compliance/policy-knowledgebase-details/'+ruleID+'/false?ag='+assetGroup+'&domain='+domainName,
          createdOn: this.issueBlocks.violationCreatedDate,
          lastModifiedDate: this.violationModifiedDate??"",
          templatePath: this.GLOBAL_CONFIG.optional.pacmanIssue.emailPacManIssue.ISSUE_MAIL_TEMPLATE_URL
        },
        subject: 'Issue Details',
        to: emailArrayList
      };
      this.getEmailSubscription = this.commonResponseService
        .getData(emailUrl, emailMethod, payload, {}, {
          responseType: 'text'
        })
        .subscribe(
          response => {
            this.showLoadcompleteEmail = true;
            this.checkEmail = true;
          },
          error => {
            this.showLoadcompleteEmail = true;
            this.checkEmail = false;
          }
        );
    } catch (e) {
      this.logger.log('error', e);
    }
  }

  clearContents(element): any {
      this.showTransaction = true;
  }

  showOtherDivRecommend(): any {
    try {
      this.showRecommend = !this.showRecommend;
      this.showOppositeRecommend = !this.showOppositeRecommend;
    } catch (e) {
      this.logger.log('error', e);
    }
  }

  searchCalled(search) {
    try {
      this.searchTxt = search;
    } catch (e) {
      this.logger.log('error', e);
    }
  }

  getDateData(date: any): any {
    try {
      const todaysDate = new Date();
      this.endDate = date;
      this.endDate.setHours(todaysDate.getHours());
      this.endDate.setMinutes(todaysDate.getMinutes());
    } catch (e) {
      this.logger.log('error', e);
    }
  }

  showOtherDiv(): any {
    try {
      this.showOpposite = !this.showOpposite;
      this.showNone = !this.showNone;
      if (this.showOpposite === false) {
        this.showTransaction = false;
        this.showLoadcomplete = false;
      }
      this.user.reset();
    } catch (e) {
      this.logger.log('error', e);
    }
  }

  showOtherDivRevoke(): any {
    try {
      this.showRevoke = !this.showRevoke;
      this.showOppositeRevoke = !this.showOppositeRevoke;
    } catch (e) {
      this.logger.log('error', e);
    }
  }

  showOtherDivEmail(): any {
    try {
      this.showOppositeEmail = !this.showOppositeEmail;
      this.showNone = !this.showNone;
      if (this.showOppositeEmail === false) {
        this.showTransactionEmail = false;
        this.showLoadcompleteEmail = false;
        this.queryValue = '';
        this.filteredList = [];
        this.fromEmailID = this.GLOBAL_CONFIG && this.GLOBAL_CONFIG.optional && this.GLOBAL_CONFIG.optional.pacmanIssue && this.GLOBAL_CONFIG.optional.pacmanIssue.emailPacManIssue && this.GLOBAL_CONFIG.optional.pacmanIssue.emailPacManIssue.ISSUE_EMAIL_FROM_ID;
        this.emailObj = {
          'to': {
            'required': true,
            'validFormat': true
          },
          'from': {
            'required': true,
            'validFormat': true
          }
        };
      }
    } catch (e) {
      this.logger.log('error', e);
    }
  }

  prevPg() {
    try {
      this.currentPointer--;
      this.processData(this.currentBucket[this.currentPointer]);
      this.firstPaginator = this.currentPointer * this.paginatorSize + 1;
      this.lastPaginator =
        this.currentPointer * this.paginatorSize + this.paginatorSize;
    } catch (error) {
      this.logger.log('error', error);
    }
  }

  nextPg() {
    try {
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
    } catch (e) {
      this.logger.log('error', e);
    }
  }

  showJiraTicket(): any {
    try {
      this.viewJira = true;
      setTimeout(() => {
        return this.hideJira();
      }, 10);
    } catch (e) {
      this.logger.log('error', e);
    }
  }

  findJiraExist(): any {
    try {
      const url = environment.findJira.url;
      const method = environment.findJira.method;
      let issueValue;
      this.routeSubscription = this.activatedRoute.params.subscribe(params => {
        issueValue = params['issueId'];
      });

      const payload = {
        issue_id: issueValue
      };

      this.findJiraSubscription = this.commonResponseService
        .getData(url, method, payload, {})
        .subscribe(
          response => {
            this.showJiraButton = true;
            if (response.data.exists === false) {
              this.showJiraData = true;
            } else {
              this.showJiraData = false;
            }
          },
          error => {
            this.showJiraButton = false;
          }
        );
    } catch (e) {
      this.logger.log('error', e);
    }
  }

  getUsers(): any {
    try {
      const userUrl = environment.users.url;
      const userMethod = environment.users.method;
      const queryparams = {};
      this.getUserSubscription = this.commonResponseService
        .getData(userUrl, userMethod, {}, queryparams)
        .subscribe(
          response => {
            this.users = response.values;
            for (let i = 0; i < this.users.length; i++) {
              const userdetails =
                this.users[i].displayName +
                ' ' +
                '(' +
                this.users[i].userEmail +
                ')';
              this.idDetailsName.push(userdetails);
            }
          },
          error => {
          }
        );
    } catch (e) {
      this.logger.log('error', e);
    }
  }

  filter() {
    try {
      if (this.queryValue !== '') {
        this.filteredList = this.idDetailsName.filter(
          function (el) {
            return el.toLowerCase().indexOf(this.queryValue.toLowerCase()) > -1;
          }.bind(this)
        );
      } else {
        this.filteredList = [];
      }
    } catch (e) {
      this.logger.log('error', e);
    }
  }

  hideJira() {
    try {
      this.checkJira = false;
      this.showLoadcompleteJira = true;
      this.showJiraData = false;
    } catch (e) {
      this.logger.log('error', e);
    }
  }

  // function to check whether input is matching email pattern
  validateEmailInput(inputValue) {
    if (!emailPattern.test(inputValue)) {
      return false;
    }
    return true;
  }

  // function to retrieve email id from selected list user item
  retrieveEmailFromSelectedItem(selectedItem) {
    return selectedItem.split(' (')[1].replace(')', '');
  }

  private getAutofixDetails() {
    const url = environment.issueAutofix.url;
    const method = environment.issueAutofix.method;

    const payload = {
        ag: this.selectedAssetGroup,
        filter: {
            issueId: this.policyViolationId,
        }
    };

    this.autofixDetails$ =
        this.commonResponseService.getData(url, method, payload)
        .subscribe(({ data }) => {
            if (data?.Name) {
                this.autofixDetails = {
                    name: data.Name,
                    endDate: data.endTime,
                    playItemsCount: data.planItems.length,
                    status: data.status,
                };
            }
        })
  }

  handleSortColNameSelection(event){
    this.sortColName = event.headerColName;
    this.direction = event.direction;
  }

  ngOnDestroy() {
    try {
      // pushes the current url to datastore
      if (this.getRuleDescSubscription) {
        this.getRuleDescSubscription.unsubscribe();
      }
      if (this.getEmailSubscription) {
        this.getEmailSubscription.unsubscribe();
      }
      if (this.getResourceDetailsSubscription) {
        this.getResourceDetailsSubscription.unsubscribe();
      }
      if (this.getEntityDetailsSubscription) {
        this.getEntityDetailsSubscription.unsubscribe();
      }
      if (this.getIssueAuditSubscription) {
        this.getIssueAuditSubscription.unsubscribe();
      }
      if (this.assetGroupSubscription) {
        this.assetGroupSubscription.unsubscribe();
      }
      if (this.routeSubscription) {
        this.routeSubscription.unsubscribe();
      }
      if (this.subscriptionDomain) {
        this.subscriptionDomain.unsubscribe();
      }
      if (this.getUserSubscription) {
        this.getUserSubscription.unsubscribe();
      }
      if (this.getExceptionSubscription) {
        this.getExceptionSubscription.unsubscribe();
      }
      if (this.getRecommendSubscription) {
        this.getRecommendSubscription.unsubscribe();
      }
      if (this.getActionDataSubscription) {
        this.getActionDataSubscription.unsubscribe();
      }
      if (this.getJiraSubscription) {
        this.getJiraSubscription.unsubscribe();
      }
      if (this.getRevokeSubscription) {
        this.getRevokeSubscription.unsubscribe();
      }
      if (this.findJiraSubscription) {
        this.findJiraSubscription.unsubscribe();
      }
      if (this.autofixDetails$) {
        this.autofixDetails$.unsubscribe();
      }
    } catch (e) {
      this.logger.log('error', e);
    }
  }
}