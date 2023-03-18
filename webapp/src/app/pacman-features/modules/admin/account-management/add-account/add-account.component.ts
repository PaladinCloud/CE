import { AfterViewInit, Component, ElementRef, OnInit, TemplateRef, ViewChild } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { WorkflowService } from 'src/app/core/services/workflow.service';
import { Clipboard } from '@angular/cdk/clipboard';
import { CommonResponseService } from 'src/app/shared/services/common-response.service';
import { environment } from 'src/environments/environment';
import { NotificationObservableService } from 'src/app/shared/services/notification-observable.service';
import { NgForm } from '@angular/forms';


@Component({
  selector: 'app-add-account',
  templateUrl: './add-account.component.html',
  styleUrls: ['./add-account.component.css']
})
export class AddAccountComponent implements OnInit,AfterViewInit {

  private pageLevel = 0;
  pageTitle: string = "Add Account";
  currentStepperIndex = 0;
  backButtonRequired;
  breadcrumbPresent: string;
  urlToRedirect: string;
  breadcrumbArray: any;
  breadcrumbLinks: any;
  selectedAccount: string = "Azure";
  accountId : string;
  accountName: string;
  projectId: string;
  tenantId: string;
  tenantName : string;
  tenantSecretData: string;
  projectName: string;
  subscriptionName: string;
  clientId: string;
  isValidating = false;
  isValidated = false;
  isValid = false;
  serviceAccount = "";
  gcpProjectId = "";
  location = "";
  gcpProjectNumber = "";
  downloadUrl = "https://account-configure.s3.amazonaws.com/aws-account-configure.template";
  workloadIdentityPoolId;validateSubscription: any;
  roleName: any;
  baseAccountSubscription: any;
  providerId = "";
  firstGcpCommand = "";
  secondGcpCommand = "";
  thirdGcpCommand = "";
  errorList = [];
  @ViewChild("selectAccountRef") selectAccountRef: TemplateRef<any>;
  @ViewChild("configureAccountRef") configureAccountRef: TemplateRef<any>;
  @ViewChild("addDetailsRef") addDetailsRef: TemplateRef<any>;
  @ViewChild("reviewRef") reviewRef: TemplateRef<any>;
  currentTemplateRef : TemplateRef<ElementRef>;
  @ViewChild('accountForm', {static: false}) accountForm: NgForm;
  stepperData = [
    {
      id: 0,
      name: "Select Account"
    },
    {
      id: 1,
      name: "Add Details"
    },
    {
      id: 2,
      name: "Configure Screen"
    },
    {
      id: 3,
      name: "Review"
    }
  ]

  accountsList = [{
    name: "AWS",
    img: "aws",
    FullName: "Amazon Web Services"
    },
    {
    name: "Azure",
    img: "azure",
    FullName: "Microsoft Azure"
    },
    {
    name:"GCP",
    img: "gcp",
    FullName: "Google Cloud Services"
    },
  ]

  configureSteps = [];
  roleArn: string;
  
  constructor(private workflowService: WorkflowService,
    private activatedRoute: ActivatedRoute,
    private router: Router,
    private clipboard: Clipboard,
    private commonResponseService: CommonResponseService,
    private notificationObservableService: NotificationObservableService
    ) { }

  ngAfterViewInit(): void {
    this.displayTemplate();
  }

  ngOnInit(): void {
    this.urlToRedirect = this.router.routerState.snapshot.url;
    const breadcrumbInfo = this.workflowService.getDetailsFromStorage()["level0"];

    if (breadcrumbInfo) {
      this.breadcrumbArray = breadcrumbInfo.map(item => item.title);
      this.breadcrumbLinks = breadcrumbInfo.map(item => item.url);
    }
    this.backButtonRequired = this.workflowService.checkIfFlowExistsCurrently(
      this.pageLevel
    );

    this.getRoleName();
    this.breadcrumbPresent = 'Add Account';
  }

  displayTemplate(){
    switch (this.currentStepperIndex) {
      case 1:
        if(this.selectedAccount.toLowerCase()=="azure")
        this.currentTemplateRef = this.configureAccountRef;
        else
        this.currentTemplateRef = this.addDetailsRef;
        break;
      case 2:
        if(this.selectedAccount.toLowerCase()!="azure")
            this.currentTemplateRef = this.configureAccountRef;
        else
        this.currentTemplateRef = this.addDetailsRef;
        break;
      case 3:
          this.currentTemplateRef = this.reviewRef;
          break;
      default:
        this.currentTemplateRef = this.selectAccountRef;
        break;
    }
  }
  
  selectedStepperIndex(index:any){
    this.currentStepperIndex = index;
  }

  pageCounter(clickedButton: string){
    if(this.currentStepperIndex == 1 && this.selectedAccount.toLowerCase() == "gcp"){
        this.createCommand();
    }
    if (clickedButton == 'back') {
      this.currentStepperIndex--;
    } else
      this.currentStepperIndex++;

    this.displayTemplate();
  }

  selectAccount(account:any){
    this.selectedAccount = account.name;
    this.configureSteps = [];
    if(this.selectedAccount.toLowerCase() == "aws"){
      this.configureSteps = [
        "Download the cloudformation template file",
        "Go to AWS console and navigate to cloudformation",
        "Click on stacks and create stacks > with new resources",
        'Under specify template choose "Upload a template file" and upload the downloaded template file',
        "Provide name to the stack and click on next",
        "Click on submit. This will create the necessary resources and permissions"
      ]
    } else if(this.selectedAccount.toLowerCase() == "azure"){
      this.configureSteps = [
        "Go to Azure active directory",
        "Navigate to App registration and register new application",
        "Once registered click on Client credentials and create a new client secret",
        "Get the client secret value, clientId and tenantId"
      ]
    } else if(this.selectedAccount.toLowerCase() == "gcp"){
      this.configureSteps = ["Go to gshell cloud and run following commands"];
    }
    this.currentStepperIndex++;
    this.displayTemplate();
  }

  getRoleName(){
    const url = environment.getBaseAccount.url;
    const method = environment.getBaseAccount.method;
    this.baseAccountSubscription = this.commonResponseService.getData(url,method,{},{}).subscribe(response=>{
      try{
        this.roleName = response.roleName;
      }
      catch(error){
        console.log(error,"error JS");
      }
    })
  }

  validateAccount(){
    const provider = this.selectedAccount.toLowerCase();
    let payload = {};
    switch (provider) {
      case "aws":
        payload =  {
          "accountId": this.accountId,
          "roleName": this.roleName,
          "platform": provider
        }
        break;
      case "gcp":
        payload = {
          "projectId": this.projectId,
          "projectName": this.projectName,
          "platform": provider,
          "projectNumber": this.gcpProjectNumber,
          "location": this.location,
          "getWorkloadIdentityPoolId": this.workloadIdentityPoolId,
          "workloadIdentityProviderId": this.providerId,
          "serviceAccountEmail": this.serviceAccount
        }
      break;
      case "azure":
        payload = {
          "tenantId": this.tenantId,
          "platform": provider,
          "tenantSecretData": this.tenantSecretData
        }
        break;
    }
    this.isValidating = true;
    const url = environment.validateAccount.url;
    const method = environment.validateAccount.method;
    this.validateSubscription = this.commonResponseService.getData(url,method,payload,{})
    .subscribe(response=>{
      try{
        const data = response.data;
        this.isValid = data.validationStatus.toLowerCase() != "failure";
        if(!this.isValid){
          this.errorList = data.errorDetails.split(",");
        }
        this.isValidating = false;
        this.isValidated = true;
      }
      catch(error){
        console.log(error,"error js");
      }
    })
  }

goToReview(){
  this.isValidated = false;
  this.currentStepperIndex++;
  this.roleArn = "arn:aws::iam:"+this.accountId+":role/"+this.roleName;
  this.displayTemplate();
}

goToConfigure(){
  this.isValidated = false;
}

onSubmit(){
   const provider = this.selectedAccount.toLowerCase();
   let accountid = "";
   let payload = {};
    switch (provider) {
      case "aws":
        payload = {
          "accountId": this.accountId,
          "accountName": this.accountName,
          "platform": provider
          }
        accountid = this.accountId;
        break;
      case "gcp":
        payload = {
          "projectId": this.projectId,
          "projectName": this.projectName,
          "platform": provider,
          "projectNumber": this.gcpProjectNumber,
          "location": this.location,
          "getWorkloadIdentityPoolId": this.workloadIdentityPoolId,
          "workloadIdentityProviderId": this.providerId,
          "serviceAccountEmail": this.serviceAccount
          }
        accountid = this.projectId;
      break;
      case "azure":
      payload = {
        "tenantId": this.tenantId,
        "tenantName": this.tenantName,
        "platform": provider,
        "tenantSecretData": this.tenantSecretData
        }
      accountid = this.tenantId;
      break;
    }
  
   const url = environment.createAccount.url;
   const method = environment.createAccount.method;
   let notificationMessage = "";
   this.commonResponseService.getData(url,method,payload,{})
   .subscribe(response=>{
     try{
      notificationMessage =  provider.toUpperCase() + " Account "+ accountid +" has been created successfully";
      this.notificationObservableService.postMessage(notificationMessage,3000,"","check-circle");
      this.redirectTo();
       }
      catch(error){
        console.log(error,"error js");
      }
   })

}

redirectTo(){
  this.router.navigate(['../'], {
    relativeTo: this.activatedRoute,
    queryParams: {}
  });
}

copyToClipboard(text: string) {
  this.clipboard.copy(text);
}

  createCommand(){
   this.firstGcpCommand = "cloud iam workload-identity-pools create "+ this.workloadIdentityPoolId +" --location="+ this.location +" --description='AWS id pool 2' --display-name='AWS workload id federation'";
   this.secondGcpCommand = "gcloud iam workload-identity-pools providers create-aws "+ this.providerId +" --workload-identity-pool="+ this.workloadIdentityPoolId +" --account-id="+ this.projectId+"--location="+this.location;
   this.thirdGcpCommand = "gcloud iam service-accounts add-iam-policy-binding "+ this.serviceAccount+" --role roles/iam.workloadIdentityUser --member" + "principalSet://iam.googleapis.com/projects/"+ this.gcpProjectNumber+"/locations/" + this.location +"/workloadIdentityPools/"+ this.workloadIdentityPoolId+"/*"
  }
}
