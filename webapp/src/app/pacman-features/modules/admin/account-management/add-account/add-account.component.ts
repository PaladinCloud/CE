import { AfterViewInit, Component, ElementRef, OnInit, TemplateRef, ViewChild } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { WorkflowService } from 'src/app/core/services/workflow.service';
import { Clipboard } from '@angular/cdk/clipboard';
import { CommonResponseService } from 'src/app/shared/services/common-response.service';
import { environment } from 'src/environments/environment';
import { NotificationObservableService } from 'src/app/shared/services/notification-observable.service';
import { FormBuilder, FormGroup, NgForm, Validators } from '@angular/forms';
import { CONFIGURATIONS } from 'src/config/configurations';
import { DataCacheService } from 'src/app/core/services/data-cache.service';
import { CustomValidators } from 'src/app/shared/custom-validators';
import { DialogBoxComponent } from 'src/app/shared/components/molecules/dialog-box/dialog-box.component';
import { MatDialog } from '@angular/material/dialog';


@Component({
  selector: 'app-add-account',
  templateUrl: './add-account.component.html',
  styleUrls: ['./add-account.component.css']
})
export class AddAccountComponent implements OnInit,AfterViewInit {

  private pageLevel = 0;
  pageTitle: string = "Add Plugin";
  currentStepperIndex = 0;
  backButtonRequired;
  breadcrumbPresent: string;
  urlToRedirect: string;
  breadcrumbArray: any;
  breadcrumbLinks: any;
  selectedAccount: string = "";
  accountId : string;
  accountName: string = "";
  qualysApiUrl: string;
  qualysApiUser: string;
  qualysApiPassword: string;
  projectId: string;
  tenantId: string;
  tenantName : string;
  tenantSecret: string;
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
  CloudformationTemplateUrl = "";
  workloadIdentityPoolId;validateSubscription: any;
  roleName: any;
  baseAccountSubscription: any;
  providerId = "";
  firstGcpCommand = "";
  secondGcpCommand = "";
  thirdGcpCommand = "";
  errorList = [];
  pluginSelected: string = "";
  @ViewChild("selectAccountRef") selectAccountRef: TemplateRef<any>;
  @ViewChild("configureAccountRef") configureAccountRef: TemplateRef<any>;
  @ViewChild("addDetailsRef") addDetailsRef: TemplateRef<any>;
  @ViewChild("reviewRef") reviewRef: TemplateRef<any>;
  currentTemplateRef : TemplateRef<ElementRef>;
  @ViewChild('accountForm', {static: false}) accountForm: NgForm;
  @ViewChild('supportInfoRef') supportInfoRef: TemplateRef<any>;
  @ViewChild("connectRef") connectRef: TemplateRef<any>;


  private currentPluginForm: FormGroup;
  private awsPluginForm: FormGroup;
  private azurePluginForm: FormGroup;
  private gcpPluginForm: FormGroup;
  private qualysPluginForm: FormGroup;
  private aquaPluginForm: FormGroup;

  
  public awsFormErrors = {
    accountId: ''
  }
  public azureFormErrors ={
    applicationId: '',
    directoryId: '',
    secretValue: '',
  }
  public gcpFormErrors ={
    projectId: '',
    serviceAccount: '',
  }
  public aquaFormErrors ={
    aquaApiUser: '',
    aquaApiUrl: '',
    aquaClientDomainUrl: '',
    aquaApiPassword : ''
  }
  public qualysFormErrors ={
    qualysApiUrl: '',
    qualysApiPassword: '',
    qualysApiUser: '',
  }

  stepperData = [];

  commands = [
    {
      command: 'gcloud services enable',
      service: 'cloudresourcemanager.googleapis.com'
    },
    {
      command: 'gcloud services enable',
      service: 'cloudtasks.googleapis.com'
    },
    {
      command: 'gcloud services enable',
      service: 'compute.googleapis.com'
    },
    {
      command: 'gcloud services enable',
      service: 'cloudkms.googleapis.com'
    },
    {
      command: 'gcloud services enable',
      service: 'dataproc.googleapis.com'
    },
    {
      command: 'gcloud services enable',
      service: 'cloudasset.googleapis.com'
    },
  ];

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
    {
      name: "Qualys",
      img: "qualys",
      FullName: "Vulnerability Management"
    },
    {
      name: "Aqua",
      img: "aqua",
      FullName: "Vulnerability Management"
    },
    {
      name: "Red Hat",
      img: "redhat",
      FullName: "Advanced Cluster Security"
    },
    {
      name: "Tenable",
      img: "tenable",
      FullName: "Vulnerability Management"
    }
  ]

  configureSteps = [];
  roleArn: string;
  aquaClientDomainUrl: string = "";
  aquaApiUser: any;
  aquaApiUrl: any;
  aquaApiPassword: any;
  createdBy: any;
  buttonClicked: boolean = false;
  dialogRef: any;
  selectedAccountName: any;
  
  constructor(private workflowService: WorkflowService,
    private activatedRoute: ActivatedRoute,
    private router: Router,
    private clipboard: Clipboard,
    private commonResponseService: CommonResponseService,
    private notificationObservableService: NotificationObservableService,
    private dataCacheService: DataCacheService,
    public dialog: MatDialog,
    public form: FormBuilder,
    ) {
      this.activatedRoute.queryParams.subscribe(params => {
        const selectedAcc = params["selectedAccount"];
        this.selectedAccount = selectedAcc??"";
        this.breadcrumbPresent = selectedAcc??"Add Plugin";
        this.pluginSelected = selectedAcc?.toLowerCase();
        if(this.pluginSelected){
          this.currentStepperIndex = 0;
          const selectedAccount = this.accountsList.find(account=>account.name == selectedAcc);
          this.selectAccount(selectedAccount);
        }
      })
    }

  ngAfterViewInit(): void {
    setTimeout(()=>{
    this.aquaClientDomainUrl = "https://573ecbdbc1.cloud.aquasec.com";
    this.displayTemplate();
    },0)
  }

  ngOnInit(): void {
    this.buildForm();
    this.CloudformationTemplateUrl = CONFIGURATIONS.optional.auth.cognitoConfig.CloudformationTemplateUrl;
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
    if(!this.pluginSelected) this.breadcrumbPresent = 'Add Plugin';
  }

  getImageName(){
    const imageName = this.selectedAccount.replace(/\s/g, '').toLowerCase();
    return imageName;
  }

  displayTemplate(){
    const currentAccount = this.selectedAccount.toLowerCase();
    switch (this.currentStepperIndex) {
      case 0:
        if(currentAccount=="azure" || currentAccount == "aqua" || currentAccount == "qualys" || currentAccount == "gcp")
        this.currentTemplateRef = this.configureAccountRef;
        else
        this.currentTemplateRef = this.addDetailsRef;
        break;
      case 1:
        if(currentAccount=="aws")
            this.currentTemplateRef = this.configureAccountRef;
        else if(currentAccount == "gcp"){
             this.currentTemplateRef = this.connectRef;
        } else
            this.currentTemplateRef = this.addDetailsRef;
        break;
      case 2:
        if(currentAccount=="gcp"){
          this.currentTemplateRef = this.addDetailsRef;
        }else{
          this.currentTemplateRef = this.reviewRef;
        }
        break;
      case 3:
        this.currentTemplateRef = this.reviewRef;
        break;
    }
  }
  
  selectedStepperIndex(index:any){
    this.currentStepperIndex = index;
  }

  pageCounter(clickedButton: string){
    if(this.currentStepperIndex == 0 && this.selectedAccount.toLowerCase() == "gcp"){
        this.createCommand();
    }
    if (clickedButton == 'back') {
      if(this.currentStepperIndex == 0){
        this.selectedAccount = "";
      }
      else
      this.currentStepperIndex--;
    } else{
      this.currentStepperIndex++;
    }
    this.displayTemplate();
  }

    // build the user edit form
    public buildForm() {
      this.awsPluginForm = this.form.group({
        accountId: ['', [Validators.required,
                         CustomValidators.exactDigits]
                   ],
        accountName : ['']
      });

      this.azurePluginForm = this.form.group({
        applicationId: ['', [Validators.required,CustomValidators.alphanumericHyphenValidator]],
        directoryId: ['', [Validators.required,CustomValidators.alphanumericHyphenValidator]],
        secretValue: ['', [Validators.required,CustomValidators.clientSecretVlidator]]
      })

      this.gcpPluginForm = this.form.group({
        projectId: ['', [Validators.required,CustomValidators.alphanumericHyphenValidator,CustomValidators.validateProjectId]],
        serviceAccount: ['', [Validators.required,CustomValidators.validateJson]]
      })

      this.aquaPluginForm = this.form.group({
        aquaApiUser: ['', [Validators.required,CustomValidators.alphanumericValidator]],
        aquaApiUrl: ['', [Validators.required,CustomValidators.urlValidator]],
        aquaClientDomainUrl: [''],
        aquaApiPassword : ['', [Validators.required]]
      })

      this.qualysPluginForm = this.form.group({
        qualysApiUrl: ['', [Validators.required,CustomValidators.urlValidator]],
        qualysApiPassword: ['', [Validators.required]],
        qualysApiUser: ['', [Validators.required,CustomValidators.alphanumericValidator]]
      })
    }

    openSupportInfoDialog(accountName): void {
      this.selectedAccountName = accountName;
      this.dialogRef = this.dialog.open(DialogBoxComponent, {
        width: '500px',
        data: { 
              template: this.supportInfoRef,
            } 
      });
    }

    closeDialog(): void {
      this.dialogRef.close();
    }

  selectAccount(account:any){
    if(account.name.toLowerCase() == "red hat"
        || account.name.toLowerCase() == "tenable"
        || account.name.toLowerCase() == "aqua"
      ){
          this.openSupportInfoDialog(account.name);
          return;
    }
    this.selectedAccount = account.name;
    this.configureSteps = [];

    this.router.navigate([], {
      queryParams: {
        selectedAccount: account.name,
      },
      queryParamsHandling: "merge",
      replaceUrl: false
    });
    this.workflowService.addRouterSnapshotToLevel(
      this.router.routerState.snapshot.root, 0, "Add Plugin",
    );
    switch (this.selectedAccount.toLowerCase()) {
      case "aws":
        this.configureSteps = [
          "Download the cloudformation template file",
          "Go to AWS console and navigate to cloudformation",
          "Click on stacks and create stacks > with new resources",
          'Under specify template choose "Upload a template file" and upload the downloaded template file',
          "Provide name to the stack and click on next",
          "Click on submit. This will create the necessary resources and permissions"
        ];
        this.stepperData = [
          {
            id: 1,
            name: "Add Details"
          },
          {
            id: 2,
            name: "Configure Access"
          },
          {
            id: 3,
            name: "Review"
          }
        ]
        this.currentPluginForm = this.awsPluginForm;
        break;
      case "azure":
        this.configureSteps = [
          "Go to Azure active directory",
          "Navigate to App registration and register new application",
          "Once registered click on Client credentials and create a new client secret",
          "Get the Application ID, Directory ID and Client Secret Value",
          "Configure the registered App to allow access to the Azure Subscriptions, for which data needs to be collected"
        ];
        this.currentPluginForm = this.azurePluginForm;
        this.stepperData = [
          {
            id: 1,
            name: "Configure Access"
          },
          {
            id: 2,
            name: "Add Details"
          },
          {
            id: 3,
            name: "Review"
          }
        ]
        break;
      case "gcp": 
          this.stepperData = [
            {
              id: 1,
              name: "Configure Access"
            },
            {
              id: 2,
              name: "Connect"
            },
            {
              id: 3,
              name: "Add Details"
            },
            {
              id: 4,
              name: "Review"
            }
          ]
        this.currentPluginForm = this.gcpPluginForm;
          this.configureSteps = [
            "Go to GCP IAM, navigate to service account",
            "Grant this service account owner access to the project",
            "Create key for the service account in JSON format",
            "Copy the contents of JSON key file, it will be used in next step"
         ];
          break;
      case "qualys": 
          this.configureSteps = [
          "To scan the resources and generate Qualys vulnerability data, you would need to install Qualys agent. Follow the guide to setup Qualys agent. Click here to get the guide for Qualys configuration",
          "Once done please make sure you have Qualys API Access to retrieve host scanned data"
          ];
          this.currentPluginForm = this.qualysPluginForm;
          this.stepperData = [
            {
              id: 1,
              name: "Configure Access"
            },
            {
              id: 2,
              name: "Add Details"
            },
            {
              id: 3,
              name: "Review"
            }
          ]
          break;
      case "aqua":
        this.stepperData = [
          {
            id: 1,
            name: "Configure Access"
          },
          {
            id: 2,
            name: "Add Details"
          },
          {
            id: 3,
            name: "Review"
          }
        ]
        this.currentPluginForm = this.aquaPluginForm;
        this.configureSteps = [
          "To scan the resources and generate aqua vulnerability data, you would need to have account with aqua before you add the details here. Click here to get the guide for Aqua configuration"
          ];
          break;
    }
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

  hidePassword(password:string){
    let hiddenPassword:string = "";
    for(let i=0;i<password.length;i++){
      hiddenPassword+="*";
    }
    return hiddenPassword;
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
          "platform": provider,
          "secretData": this.serviceAccount
        }
      break;
      case "azure":
      this.tenantSecretData = 'tenant:'+this.tenantId+',clientId:'+this.clientId+',secretId:'+this.tenantSecret;
         payload = {
          "tenantId": this.tenantId,
          "platform": provider,
          "tenantSecretData": this.tenantSecretData
        }
        break;
      case "qualys": payload = {
        "qualysApiUrl": this.qualysApiUrl,
        "qualysApiUser": this.qualysApiUser,
        "qualysApiPassword": this.qualysApiPassword,
        "platform": this.selectedAccount.toLowerCase()
        }
        break;
      case "aqua":
        payload = {
          "aquaClientDomainUrl": this.aquaClientDomainUrl,
          "aquaApiUrl": this.aquaApiUrl,
          "aquaApiUser": this.aquaApiUser,
          "aquaApiPassword": this.aquaApiPassword,
          "platform": this.selectedAccount.toLowerCase()
        }

    }
    this.isValidating = true;
    const url = environment.validateAccount.url;
    const method = environment.validateAccount.method;
    this.validateSubscription = this.commonResponseService.getData(url,method,payload,{})
    .subscribe(response=>{
      try{
        const data = response.data;
        this.isValid = data.validationStatus.toLowerCase() != "failure";
        this.isValidating = false;
        this.isValidated = true;
        if(!this.isValid){
          this.errorList = data.errorDetails?.split(",");
        }
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
  this.buttonClicked = true;
   const provider = this.selectedAccount.toLowerCase();
   let accountid = "";
   let payload = {};
    switch (provider) {
      case "aws":
        payload = {
          "accountId": this.accountId,
          "accountName": this.accountName.length?this.accountName:this.accountId,
          "platform": provider
          }
        accountid = this.accountId;
        break;
      case "gcp":
        payload = {
          "projectId": this.projectId,
          "platform": provider,
          "secretData": this.serviceAccount
          }
        accountid = this.projectId;
      break;
      case "azure":
      this.tenantSecretData = 'tenant:'+this.tenantId+',clientId:'+this.clientId+',secretId:'+this.tenantSecret;
      payload = {
        "tenantId": this.tenantId,
        "platform": provider,
        "tenantSecretData": this.tenantSecretData
        }
      accountid = this.tenantId;
      break;
      case "qualys": payload = {
          "qualysApiUrl": this.qualysApiUrl,
          "qualysApiUser": this.qualysApiUser,
          "qualysApiPassword": this.qualysApiPassword,
          "platform": this.selectedAccount.toLowerCase()
        };
      break;
      case "aqua":
        payload = {
          "aquaClientDomainUrl": this.aquaClientDomainUrl,
          "aquaApiUrl": this.aquaApiUrl,
          "aquaApiUser": this.aquaApiUser,
          "aquaApiPassword": this.aquaApiPassword,
          "platform": this.selectedAccount.toLowerCase()
        }
    }
   const userDetails = this.dataCacheService.getUserDetailsValue();
   let userId = userDetails.getEmail(); 
   payload['createdBy'] = userId;
   const url = environment.createAccount.url;
   const method = environment.createAccount.method;
   let notificationMessage = "";
   this.commonResponseService.getData(url,method,payload,{})
   .subscribe(response=>{
     try{
       const data = response.data;
       if(data){
           if(data.validationStatus.toLowerCase() !== "failure"){
            notificationMessage =  provider.toUpperCase() + " Account "+ accountid +" has been created successfully";
            this.notificationObservableService.postMessage(notificationMessage,3000,"","check-circle");
           } else{
            notificationMessage =  provider.toUpperCase() + " Account "+ accountid +" creation has been failed";
            this.notificationObservableService.postMessage(notificationMessage,3000,"error","Error");
           }
           this.redirectTo();
       }
       }
      catch(error){
        console.log(error,"error js");
      }
   })
}

isSelectedAccount(account:string){
    return this.selectedAccount.toLowerCase() == account;
}

redirectTo(){
  this.workflowService.navigateTo({
    urlArray: ['../'],
    queryParams: {selectedAccount:undefined},
    relativeTo: this.activatedRoute,
    currPagetitle: undefined,
    nextPageTitle: "Plugins",
    state: {
      dataUpdated: true
    }
  });
}

copyToClipboard(commands) {
  let text = ""; 
  commands?.map(command => text+=command.command + " " + command.service+"\n");
  this.clipboard.copy(text);
}

  createCommand(){
   this.firstGcpCommand = "cloud iam workload-identity-pools create "+ this.workloadIdentityPoolId +" --location="+ this.location +" --description='AWS id pool 2' --display-name='AWS workload id federation'";
   this.secondGcpCommand = "gcloud iam workload-identity-pools providers create-aws "+ this.providerId +" --workload-identity-pool="+ this.workloadIdentityPoolId +" --account-id="+ this.projectId+"--location="+this.location;
   this.thirdGcpCommand = "gcloud iam service-accounts add-iam-policy-binding "+ this.serviceAccount+" --role roles/iam.workloadIdentityUser --member" + "principalSet://iam.googleapis.com/projects/"+ this.gcpProjectNumber+"/locations/" + this.location +"/workloadIdentityPools/"+ this.workloadIdentityPoolId+"/*"
  }
}
