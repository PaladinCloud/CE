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
import { GoogleAnalyticsService } from 'ngx-google-analytics';


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
  redHatId: string;
  redHatToken: string;
  redHatAccountName: string;
  redHatOwner: string;
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
  isValid = true;
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
  tenableAccessKey = "";
  tenableSecretKey = "";
  tenableAPIUrl = "https://cloud.tenable.com";
  errorList = [];
  pluginSelected: string = "";
  @ViewChild("selectAccountRef") selectAccountRef: TemplateRef<any>;
  @ViewChild("configureAccountRef") configureAccountRef: TemplateRef<any>;
  @ViewChild("validateAccountRef") validateAccountRef: TemplateRef<any>;
  @ViewChild("addDetailsRef") addDetailsRef: TemplateRef<any>;
  @ViewChild("reviewRef") reviewRef: TemplateRef<any>;
  @ViewChild("tenableRef") tenableRef: TemplateRef<any>;
  currentTemplateRef : TemplateRef<ElementRef>;
  @ViewChild('accountForm', {static: false}) accountForm: NgForm;
  @ViewChild('supportInfoRef') supportInfoRef: TemplateRef<any>;
  @ViewChild("connectRef") connectRef: TemplateRef<any>;
  @ViewChild("videoThumbnailRef") videoThumbnailRef:TemplateRef<any>;

  @ViewChild("redHatStepsVideo") redHatStepsVideo: TemplateRef<any>;
  name = 'Video events';
  videoSource = "https://paladincloud-production-video.s3.amazonaws.com/redhat-setup-video.mp4";
  @ViewChild('videoPlayer') videoplayer: any;
  public startedPlay:boolean = false;
  public show:boolean = false;
  comingSoonPluginList = [];
  manualConfiguredAccountList: String[]=["aqua"];
  showAllSteps = false;
  pluginsWithUpdatedEndPoint = ["gcp", "redhat"];

  private currentPluginForm: FormGroup;
  private awsPluginForm: FormGroup;
  private azurePluginForm: FormGroup;
  private gcpPluginForm: FormGroup;
  private qualysPluginForm: FormGroup;
  private aquaPluginForm: FormGroup;
  private redHatPluginForm: FormGroup;
  private tenablePluginForm: FormGroup;


  
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

  public redHatFormErrors = {
    redHatId: '',
    redHatAccountName: '',
    redHatToken: '',
    redHatOwner: ''
  }
  public tenableFormErrors = {
    tenableAccessKey: '',
    tenableSecretKey: ''
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
    },
    {
      name: "Contrast",
      img: "contrast",
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
  isAdding: boolean = false;
  errorMessage: any;
  selectedAccountImage: any;
  accountsWithoutSteppers = ["tenable"];
  addDetailsStepperIndex: number;
  
  constructor(private workflowService: WorkflowService,
    private activatedRoute: ActivatedRoute,
    private router: Router,
    private clipboard: Clipboard,
    private commonResponseService: CommonResponseService,
    private notificationObservableService: NotificationObservableService,
    private dataCacheService: DataCacheService,
    public dialog: MatDialog,
    private gaService: GoogleAnalyticsService,
    public form: FormBuilder,
    ) {
      this.activatedRoute.queryParams.subscribe(params => {
        const selectedAcc = params["selectedAccount"];
        this.breadcrumbPresent = selectedAcc??"Add Plugin";
        this.pluginSelected = selectedAcc?.toLowerCase();
        this.selectedAccount = selectedAcc??"";
        if(this.pluginSelected){
          this.currentStepperIndex = 0;
          const selectedAccount = this.accountsList.find(account => account.name===selectedAcc);
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
    const featureFlags = CONFIGURATIONS.optional.general.featureFlags;
    
    const featureFlagMap = {
      contrastPluginEnabled: "contrast",
      tenablePluginEnabled: "tenable",
      rapid7PluginEnabled: "rapid7",
    };

    for (const key in featureFlags) {
      if (!featureFlags[key]) {
        const plugin = featureFlagMap[key];
        this.comingSoonPluginList.push(plugin);
      }
    }

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

  toggleSteps() {
    this.showAllSteps = !this.showAllSteps;
  }

  showStepper(){
    if(this.accountsWithoutSteppers.includes(this.selectedAccount.toLowerCase())){
        return false;
    }
    return true;
  }

  pauseVideo(videoplayer)
{
  videoplayer.nativeElement.play();
  // this.startedPlay = true;
  // if(this.startedPlay == true)
  // {
     setTimeout(() => 
     {
      videoplayer.nativeElement.pause();
       if(videoplayer.nativeElement.paused)
      {
        this.show = !this.show;       
      } 
     }, 5000);
  // }
  }

  closebutton(videoplayer){
    this.show = !this.show; 
    videoplayer.nativeElement.play();
  }

  displayTemplate(addDetailsStepperIndex?){
    const currentAccount = this.selectedAccount.toLowerCase();
    if(addDetailsStepperIndex>=0){
      this.currentStepperIndex = this.addDetailsStepperIndex;
    }
    switch (this.currentStepperIndex) {
      case 0:
        if(currentAccount=="azure" || currentAccount == "aqua" || currentAccount == "qualys" || currentAccount == "gcp" || currentAccount == "red hat")
        this.currentTemplateRef = this.configureAccountRef;
        else{
          this.addDetailsStepperIndex = this.currentStepperIndex;
          this.currentTemplateRef = this.addDetailsRef;
        }
        break;
      case 1:
        if(currentAccount=="aws")
            this.currentTemplateRef = this.configureAccountRef;
        else if(currentAccount == "gcp"){
             this.currentTemplateRef = this.connectRef;
        } else{
          this.addDetailsStepperIndex = this.currentStepperIndex;
          this.currentTemplateRef = this.addDetailsRef;
        }
        break;
      case 2:
        if(currentAccount=="gcp"){
          this.addDetailsStepperIndex = this.currentStepperIndex;
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
      this.gaService.event('Button', 'Click', 'Back');
      if(this.currentStepperIndex == 0){
        this.selectedAccount = "";
      }
      else
      this.currentStepperIndex--;
    } else{
      this.gaService.event('Button', 'Click', 'Next');
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

      this.redHatPluginForm = this.form.group({
        redHatId: ['', [Validators.required]],
        redHatToken: ['',Validators.required],
        redHatAccountName: [''],
        redHatOwner: [''],
      })

      this.tenablePluginForm = this.form.group({
        tenableAccessKey: ['', [Validators.required,CustomValidators.alphanumericValidator]],
        tenableSecretKey: ['',[Validators.required,CustomValidators.alphanumericValidator]]
      })
    }

    shouldHideAddButton(): boolean {
      if(this.accountsWithoutSteppers.includes(this.selectedAccount.toLowerCase())){
        return false;
      }
      return (
          (this.pluginSelected === 'gcp' && (this.currentStepperIndex < 2 || this.currentStepperIndex > 2)) ||
          (this.pluginSelected !== 'gcp' && (this.currentStepperIndex < 1 || this.currentStepperIndex > 1))
      );
    }
  
    isAddButtonDisabled(): boolean {
      return this.selectedAccount && this.currentPluginForm?.invalid;
    }
    
    shouldHideNextButton(): boolean {
        return (
            (this.currentStepperIndex > 1 && this.pluginSelected === 'gcp') ||
            (this.currentStepperIndex > 0 && this.pluginSelected !== 'gcp')
        );
    }
    
    isNextButtonDisabled(): boolean {
        return this.selectedAccount && this.selectedAccount.toLowerCase() === 'aws' && this.currentPluginForm?.invalid;
    }
    
    shouldHideBackButton(): boolean {
        return this.currentStepperIndex === 0;
    }
    
    isComingSoonPlugin(pluginName: string): boolean {
        return this.comingSoonPluginList.includes(pluginName.toLowerCase());
    }

    selectedAccountImageSrc(displayImage:string): string {
      // Define a mapping of account names to their corresponding image paths
      const accountImageMappings: Record<string, string> = {
          aws: 'aws-color.svg',
          gcp: 'gcp-color.svg',
          azure: 'azure-color.svg',
          qualys: 'qualys-color.svg',
          aqua: 'aqua-color.svg',
          tenable: 'tenable-color.svg',
          contrast: 'contrast-color.svg',
          rapid7: 'rapid7-color.svg',
          'red hat': 'redhat-color.svg',
      };

      // Use the mapping to generate the image source dynamically
      return '/assets/icons/'+accountImageMappings[displayImage.toLowerCase()];
  }

    // Determine if the details form should be shown based on the selected plugin
    shouldShowDetailsForm(): boolean {
      const pluginSelected = this.getPluginSelected(); // Replace with your logic
      return !!pluginSelected;
    }

    getPluginSelected() {
      return this.pluginSelected;
    }

    // Get the form group for the selected plugin
    getPluginForm(): FormGroup {
          return this.currentPluginForm;
    }

   // Get form errors based on the selected plugin and field name
   getFormErrors(): any {
    const pluginSelected = this.getPluginSelected();
    let selectedFormError = null;
  
    switch (pluginSelected) {
      case 'aws':
        selectedFormError = this.awsFormErrors;
        break;
  
      case 'azure':
        selectedFormError = this.azureFormErrors;
        break;
  
      case 'gcp':
        selectedFormError = this.gcpFormErrors;
        break;
  
      case 'aqua':
        selectedFormError = this.aquaFormErrors;
        break;
  
      case 'qualys':
        selectedFormError = this.qualysFormErrors;
        break;
  
      case 'red hat':
        selectedFormError = this.redHatFormErrors;
        break;
  
      default:
        // Handle the case where no plugin is selected
        return null;
    }
  
    return selectedFormError;
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
      if(this.dialogRef){
        this.dialogRef.close();
      }
    }

  selectAccount(account:any){
    this.gaService.event('Button', 'Click', 'Select Plugin');
    this.isValid = true;
    if(account.name.toLowerCase() == "tenable"
        || account.name.toLowerCase() == "aqua" 
        || account.name.toLowerCase() == "contrast"
      ){
          this.openSupportInfoDialog(account.name);
          return;
    }
    this.selectedAccount = account.name;
    this.selectedAccountImage = account.img;
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
          }
        ]
        this.currentPluginForm = this.aquaPluginForm;
        this.configureSteps = [
          "To scan the resources and generate aqua vulnerability data, you would need to have account with aqua before you add the details here. Click here to get the guide for Aqua configuration"
          ];
          break;
      case "red hat":
        this.stepperData = [
          {
            id: 1,
            name: "Configure Access",
            stepInfo: "Setup your Account"
          },
          {
            id: 2,
            name: "Add Details",
            stepInfo: "Provide Account Details"
          }
        ]
        this.currentPluginForm = this.redHatPluginForm;
        this.configureSteps = [
          "Access the ACS Instance page by selecting 'Advanced Security Cluster' in the side menu. Click on the ACS instance you want to link with Paladin Cloud.",
          "Inside the chosen ACS instance, copy the 'ID' from the 'Instance Details' card.",
          "Switch to the Paladin Cloud application, move to step 2 (Add Details), and paste the copied 'ID' into the 'ID Field'.",
          "Return to the Red Hat ACS Instance page and click the “Open ACS Console” button.",
          "Access Platform Configuration from the side menu, and then choose the 'Integrations' option.",
          "Scroll down to the 'Authentication Tokens' section.",
          "Select 'API Token'. You'll be taken to the 'Integration API Tokens' page, which lists previously generated tokens (if any).",
          "Click the 'Generate Token' button.",
          "Name the token and choose a role that suits your requirements.",
          "Copy the generated token and paste it into the token input field within the Red Hat Plugin in the Paladin Cloud application.",
          "Hit the 'Validate' button to confirm and successfully add the plugin to Paladin Cloud"
        ];
          break;
      case "tenable":
        this.currentPluginForm = this.tenablePluginForm;
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

  showValidation(){
    this.dialogRef = this.dialog.open(DialogBoxComponent, {
      width: '500px',
      data: { 
            template: this.validateAccountRef,
          } 
    });
  }

  

  validateAccount(){
    this.gaService.event('Button', 'Click', 'Add Plugin');
    this.showValidation();
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
          "gcpProjectId": this.projectId,
          "gcpServiceAccountKey": this.serviceAccount
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
        break;
      case "red hat":
        payload = {
          redhatAccountId: this.redHatId,
          redhatAccountName: this.redHatAccountName,
          redhatToken: this.redHatToken,
          redhatOwner: this.redHatOwner
        }
        break;
      case "tenable":
        payload = { 
          "platform":this.selectedAccount.toLowerCase(),
          "tenableAccessKey": this.tenableAccessKey,
          "tenableSecretKey": this.tenableSecretKey,
        }
        break;
    }
    this.isValidating = true;
    const url = this.replaceUrl(environment.validateAccount.url, 'validate');
    const method = environment.validateAccount.method;
    this.validateSubscription = this.commonResponseService.getData(url,method,payload,{})
    .subscribe(response=>{
      try{
        const data = response.data;
        const status = data.validationStatus || data.status;        
        this.isValid = status.toLowerCase() != "failure" ;
        this.isValidated = true;
        if(!this.isValid){
          this.displayTemplate(this.addDetailsStepperIndex);
          this.closeDialog();
          this.errorMessage = data.message;
          this.notificationObservableService.postMessage("Validation has been failed!", 3000, "error", "Error");
          this.errorList = data.errorDetails?.split(",");
        }else {
          this.isValidating = false;
          this.onSubmit();
        }
      }
      catch(error){
        this.closeDialog();
        this.notificationObservableService.postMessage("Something went wrong", 3000, "error", "Error");
        console.log(error,"error js");
      }
    }, error => {
        this.closeDialog();
        this.notificationObservableService.postMessage("Something went wrong", 3000, "error", "Error");
        console.log(error,"api error");
    })
  }

goToReview(){
  this.isValidated = false;
  if(this.dialogRef){
    this.closeDialog();
  }
  this.currentStepperIndex++;
  this.roleArn = "arn:aws::iam:"+this.accountId+":role/"+this.roleName;
  this.displayTemplate();
}

goToConfigure(){
  this.isValidated = false;
  if(this.dialogRef){
    this.closeDialog();
  }
}

onSubmit(){
  this.isAdding = true;
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
          "gcpProjectId": this.projectId,
          "gcpServiceAccountKey": this.serviceAccount
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
        break;
      case "red hat":
        payload = {
          redhatAccountId: this.redHatId,
          redhatAccountName: this.redHatAccountName,
          redhatToken: this.redHatToken,
          redhatOwner: this.redHatOwner
        }
        break;
        case "tenable":
          payload = { 
            "platform":this.selectedAccount.toLowerCase(),
            "tenableAccessKey": this.tenableAccessKey,
            "tenableSecretKey": this.tenableSecretKey,
            "createdBy": this.createdBy
          }
          break;
    }
   const userDetails = this.dataCacheService.getUserDetailsValue();
   let userId = userDetails.getEmail(); 
   if(!this.pluginsWithUpdatedEndPoint.includes(this.getImageName())){
    payload['createdBy'] = userId;
   }
   const url = this.replaceUrl(environment.createAccount.url, 'create');
   const method = environment.createAccount.method;
   let notificationMessage = "";
   this.commonResponseService.getData(url,method,payload,{})
   .subscribe(response=>{
     try{
       const data = response.data;
       if(data){
          if(this.dialogRef){
            this.closeDialog();
          }
           this.isAdding = false;
           const status = data.validationStatus || data.status;           
           this.isValid = status.toLowerCase() != "failure" ;
           if(this.isValid){
            notificationMessage =  provider.toUpperCase() + " Account "+ accountid +" has been created successfully";
            this.notificationObservableService.postMessage(notificationMessage,3000,"","check-circle");
            this.redirectTo();
           } else{
            this.displayTemplate(this.addDetailsStepperIndex);
            this.errorMessage = data.message;
             notificationMessage =  provider.toUpperCase() + " Account "+ accountid +" creation has been failed";
             this.notificationObservableService.postMessage(notificationMessage,3000,"error","Error");
            }
          }
          if(this.dialogRef){
            this.closeDialog();
          }
          this.isAdding = false;
       }
      catch(error){
        console.log(error,"error js");
      }
   })
}

replaceUrl(url, action=''){
  if(this.pluginsWithUpdatedEndPoint.includes(this.getImageName())){
    return url.replace("{pluginSelected}", this.getImageName());
  }else if(action=='validate'){
    return url.replace("{pluginSelected}/","");
  } else {
    return url.replace("/{pluginSelected}/","").replace(action, '');
  }
}

isSelectedAccount(account:string){
    return this.selectedAccount.toLowerCase() == account;
}

redirectTo(){
  if(this.dialogRef){
    this.closeDialog();
  }
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

  openVideoDialog(){
    this.gaService.event('Button', 'Click', 'Video');
    this.dialog.open(DialogBoxComponent, {
      width: '800px',
      data: { 
            customClass: 'video-dialog',
            template: this.redHatStepsVideo,
          } 
    });
  }

}