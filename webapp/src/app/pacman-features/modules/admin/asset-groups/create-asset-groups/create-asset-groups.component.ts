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

 import { Component, OnInit, OnDestroy, ViewChild, TemplateRef } from '@angular/core';
 import { trigger, state, style, transition, animate } from "@angular/animations";
 import { environment } from './../../../../../../environments/environment';
 
 import { ActivatedRoute, Router } from '@angular/router';
 import { of, Subject, Subscription } from 'rxjs';
 import find from 'lodash/find';
 import cloneDeep from 'lodash/cloneDeep';
 import { UtilsService } from '../../../../../shared/services/utils.service';
 import { LoggerService } from '../../../../../shared/services/logger.service';
 import { ErrorHandlingService } from '../../../../../shared/services/error-handling.service';
 
 
 import { WorkflowService } from '../../../../../core/services/workflow.service';
 import { RouterUtilityService } from '../../../../../shared/services/router-utility.service';
 import { AdminService } from '../../../../services/all-admin.service';
 import { UploadFileService } from '../../../../services/upload-file-service';
 import { NotificationObservableService } from 'src/app/shared/services/notification-observable.service';
 import { DataCacheService } from 'src/app/core/services/data-cache.service';
 import { DATA_MAPPING } from 'src/app/shared/constants/data-mapping';
 import { AssetTypeMapService } from 'src/app/core/services/asset-type-map.service';
 import { AssetTilesService } from 'src/app/core/services/asset-tiles.service';
 import { DialogBoxComponent } from 'src/app/shared/components/molecules/dialog-box/dialog-box.component';
 import { MatDialog } from '@angular/material/dialog';
 import { AbstractControl, FormBuilder, FormGroup, Validators } from '@angular/forms';
import { ONLY_DIGITS } from 'src/app/shared/constants/regex-constants';
import { catchError, map, takeUntil } from 'rxjs/operators';
 
 interface ICondition{
     keyList: string[];
     valueList: string[];
     isDisabled: boolean;
     selectedValue: string;
     selectedKey: string;
 };
 
 type ICriteria = ICondition[];
 
 @Component({
   selector: 'app-admin-create-asset-groups',
   templateUrl: './create-asset-groups.component.html',
   styleUrls: ['./create-asset-groups.component.css'],
   animations: [
     trigger('slideInOut', [
       state('in', style({
         transform: 'translate3d(0, 0, 0)'
       })),
       state('out', style({
         transform: 'translate3d(100%, 0, 0)'
       })),
       transition('in => out', animate('400ms ease-in-out')),
       transition('out => in', animate('400ms ease-in-out'))
     ]),
     trigger('fadeInOut', [
       state('open', style({ 'z-index': 2, opacity: 1 })),
       state('closed', style({ 'z-index': -1, opacity: 0 })),
       transition('open <=> closed', animate('500ms')),
     ])
   ],
   providers: [
     LoggerService,
     ErrorHandlingService,
     UploadFileService,
     AdminService
   ]
 })
 export class CreateAssetGroupsComponent implements OnInit, OnDestroy {
 
   backButtonRequired: boolean = false;
   pageTitle = 'Create Asset Group';
   breadcrumbArray = ['Asset Groups'];
   breadcrumbLinks = ['asset-groups'];
   breadcrumbPresent;
   highlightedText;
   progressText;
   outerArr = [];
   filters = [];
   selectedCriteriaKeyList = [];
   isGroupNameValid = -1;
   targetTypeValue: string = "";
   assetForm = {
     dataSourceName: 'aws',
     groupName: '',
     displayName: '',
     type: '',
     createdBy: '',
     description: '',
     visible: true,
     targetTypes: []
   };
   isCreate = false;
   highlightName = '';
   groupName = '';
   assetLoaderTitle = '';
   assetLoader = false;
   assetLoaderFailure = false;
   attributeName = '';
   attributeValue = '';
   targetTypeSelectedValue = '';
   disableOptions: boolean = true;
   selectedAttributes = [];
   AttributeKeyViewMap = {"CloudType":"Source", "TargetType" : "Asset Type" ,"Region":"Region", "Id": "Account Id", "region":"Region","accountid":"Account Id"};
   AttributeKeyMap = {"Source":"CloudType", "Asset Type" : "TargetType" ,"Region":"region", "Account Id": "accountid"};
   allOptionalRuleParams = [];
   isAssetGroupFailed = false;
   isAssetGroupSuccess = false;
   ruleContentLoader = true;
   assetGroupLoader = false;
   invocationId = '';
   paginatorSize = 25;
   isLastPage;
   assetGroupNames = [];
   isFirstPage;
   totalPages;
   pageNumber = 0;
   showLoader = true;
   showWidget = true;
   remainingTargetTypes;
   remainingTargetTypesFullDetails;
   targetTypeAttributeValues = [];
   errorMessage;
   searchTerm = '';
   submitBtn = "Confirm and Create";
   configurationsBeforeEdit = {};
   private assetGroupForm: FormGroup;
   public assetGroupFormErrors ={
     assetGroupDisplayName: '',
     assetGroupDesc: '',
     selectedAccountType: '',
   }
 
   hideContent = false;
   pageContent = [
     { title: 'Enter Group Details', hide: false, isChanged: false },
     { title: 'Select Domains', hide: true, isChanged: false },
     { title: 'Select Targets', hide: true, isChanged: false },
     { title: 'Configure Attributes', hide: true, isChanged: false }
   ];
 
   availChoosedItems = {};
   availChoosedSelectedItems = {};
   availChoosedItemsCount = 0;
 
   selectChoosedItems = {};
   selectChoosedSelectedItems = {};
   selectChoosedItemsCount = 0;
 
   availableItems = [];
   selectedItems = [];
 
   availableItemsBackUp = [];
   selectedItemsBackUp = [];
 
   availableItemsCopy = [];
   selectedItemsCopy = [];
 
   searchSelectedDomainTerms = '';
   searchAvailableDomainTerms = '';
 
 
   // Target Details //
   availTdChoosedItems = {};
   availTdChoosedSelectedItems = {};
   availTdChoosedItemsCount = 0;
   state = 'closed';
   menuState = 'out';
   selectedIndex = -1;
   selectedAttributeDetails = [];
   selectedAttributeIndex = '';
 
   selectTdChoosedItems = {};
   selectTdChoosedSelectedItems = {};
   selectTdChoosedItemsCount = 0;
 
   availableTdItems = [];
   selectedTdItems = [];
   selectedTdItemsCopyForPrevNext = [];
 
   availableTdItemsBackUp = [];
   selectedTdItemsBackUp = [];
 
   availableTdItemsCopy = [];
   selectedTdItemsCopy = [];
 
   searchSelectedTargetTerms = '';
   searchAvailableTargetTerms = '';
 
   stepIndex = 0;
   stepTitle = this.pageContent[this.stepIndex].title;
   allAttributeDetails = [];
   allAttributeDetailsCopy = [];
   allAttributeDetailsCopyForPrevNext = [];
   allSelectedAttributeDetailsCopy = [];
 
   filterText = {};
   errorValue = 0;
   urlID = '';
   groupId = '';
   successTitleStart = '';
   successTitleEnd = '';
 
   failedTitleStart = '';
   isAttributeAlreadyAdded = -1;
   failedTitleEnd = '';
 
   FullQueryParams;
   queryParamsWithoutFilter;
   urlToRedirect = '';
   mandatory;
   private pageLevel = 0;
 
 
   currentStepperIndex = 0;
   cloudsData = [];
   criterias =  [];
   criteriaKeys = [];
   criteriasKeyValues = {};
   currentStepperName: string ="Asset Group Details";
   assetGroupDisplayName = "";
   assetGroupDesc = "";
   createdBy = "";
   selectedAccountType = "";
   typeList = ["Admin"];
   destroy$ = new Subject<void>();
   currentTemplateRef : TemplateRef<any>;
   @ViewChild('assetGroupRef') assetGroupRef: TemplateRef<any>;
   @ViewChild('configurationRef') configurationRef: TemplateRef<any>;
   @ViewChild('reviewRef') reviewRef: TemplateRef<any>;
   @ViewChild("actionRef") actionRef: TemplateRef<any>;
 
   stepperData = [
     {
       id: 0,
       name: "Asset Group Details"
     },
     {
       id: 1,
       name: "Configuration"
     },
     {
       id: 2,
       name: "Review"
     }
   ]
 
   public labels;
   private routeSubscription: Subscription;
   private previousUrlSubscription: Subscription;
   selectedCriteriaValues = [];
   selectedValue = "";
   selectedKey = "";
   assetTypeMap: any;
   assetGroupName: any;
   buttonClicked: boolean = false;
   selectedAssetGroup: string = "";
   currentDomain: string = "";
   criteriaDetails: any;
 
   constructor(
     private router: Router,
     private utils: UtilsService,
     private logger: LoggerService,
     private errorHandling: ErrorHandlingService,
     private workflowService: WorkflowService,
     private routerUtilityService: RouterUtilityService,
     private adminService: AdminService,
     private notificationObservableService: NotificationObservableService,
     private activatedRoute: ActivatedRoute,
     private dataCacheService: DataCacheService,
     private assetTypeMapService: AssetTypeMapService,
     private assetTilesService: AssetTilesService,
     public dialog: MatDialog,
     private form: FormBuilder
   ) {
     this.routerParam();
     this.updateComponent();
   }
 
   ngOnInit() {
     setTimeout(()=>{
       this.urlToRedirect = this.router.routerState.snapshot.url;
       const breadcrumbInfo = this.workflowService.getDetailsFromStorage()["level0"];    
     
       this.assetTypeMapService.getAssetMap().subscribe(data=>{
         this.assetTypeMap = data;
       });
       if(breadcrumbInfo){
         this.breadcrumbArray = breadcrumbInfo.map(item => item.title);
         this.breadcrumbLinks = breadcrumbInfo.map(item => item.url);
       }
       this.backButtonRequired = this.workflowService.checkIfFlowExistsCurrently(
         this.pageLevel
       );
       this.createdBy = this.dataCacheService.getUserDetailsValue().getEmail();
       this.configurationsBeforeEdit = {};
     },0)
   }
 
   public buildForm() {
     this.assetGroupForm = this.form.group({
       assetGroupDisplayName: ['', [Validators.required,this.isGroupNameAvailable.bind(this)]],
       assetGroupDesc: ['', [Validators.required,Validators.minLength(6)]],
       selectedAccountType : ['',[Validators.required,this.isValidType.bind(this)]]
     });
   }
 
   isNextButtonDisabled() {
     let isDisabled: boolean = true;
     if (this.currentStepperIndex == 0) {
        isDisabled = this.assetGroupForm.invalid;
     } else {
       this.criterias.forEach(criteria => {
         if (criteria[0].selectedValue) {
           isDisabled = false;
         }
       })
     }
     return isDisabled;
   }
 
   isSubmitButtonDisabled() {
     return this.buttonClicked
       || this.selectedAccountType.toLowerCase() == "system"
       || this.selectedAccountType.toLowerCase() == "user";
   }
 
   isEditable() {
     return this.groupId &&
       (this.selectedAccountType.toLowerCase() == "system"
       || this.selectedAccountType.toLowerCase() == "user");
   }
 
   getDetails(criteriaDetails){
     const criteriaMap = criteriaDetails.reduce((acc, criteria) => {
       if (!acc[criteria.criteriaName]) {
         acc[criteria.criteriaName] = [];
       }
       acc[criteria.criteriaName].push(criteria);
       return acc;
     }, {});
     this.createCriteriaList(criteriaMap);
     this.configurationsBeforeEdit = { 
     "type": this.selectedAccountType,
     "groupName": this.assetGroupDisplayName,
     "description": this.assetGroupDesc,
     "createdBy": this.createdBy,
     "configuration": criteriaMap
   };
 }
 
   createCriteriaList(criteriaMap){
     let conditionList = [];
     let k = 0;
     for(const criteria in criteriaMap){
       conditionList = criteriaMap[criteria];
       for(let j=0;j<conditionList.length;j++){
           const conditionKey = this.AttributeKeyViewMap[conditionList[j]["attributeName"]] ? this.AttributeKeyViewMap[conditionList[j]["attributeName"]] : conditionList[j]["attributeName"];
           this.addEmptyCondition(k,conditionKey,conditionList[j]["attributeValue"]);
       }
       k++;
     }
   }
 
   getAttributesData(criteriaDetails:any=null){
     const url = environment.getCloudTypeObject.url;
     const method = environment.getCloudTypeObject.method;
 
     this.adminService.executeHttpAction(url,method,{},{}).subscribe(response=>{
       const cloudsData = response[0];
       this.processData(cloudsData);
       if(criteriaDetails){
           this.getDetails(criteriaDetails);
       }else{
         this.criterias.push([
           {
             valueList: [],
             keyList: this.criteriaKeys,
             isDisabled: false,
             selectedValue: "",
             selectedKey: "",
           }
           ]);
       }
     })
   }
 
   processData(cloudsData:any){
       for(let i=0;i<cloudsData.length;i++){
           cloudsData[i] = Object.entries(cloudsData[i]).reduce((acc, [oldKey, value]) => {
             const newKey = this.AttributeKeyViewMap[oldKey]?this.AttributeKeyViewMap[oldKey]:oldKey;
             acc[newKey] = value;
             return acc;
           }, {});
       }
       this.cloudsData = cloudsData;
       this.criteriaKeys = Object.keys(this.cloudsData[0]); 
   }
 
   async addEmptyCondition(criteriaIdx,selectedKey="",selectedValue=""){
     let selectedKeyList = [];
  
     if(this.criterias.length>criteriaIdx){
       this.criterias[criteriaIdx].forEach(condition=>{
         selectedKeyList.push(condition.selectedKey);
      })
     }
 
 
     this.selectedCriteriaKeyList[criteriaIdx] = selectedKeyList;
     const newKeyList = this.criteriaKeys.filter(key=>{
           return !selectedKeyList.includes(key);
     })
     let valueList = [].concat(...this.cloudsData.map(cloud => cloud[selectedKey]));
     valueList = await Promise.all(valueList.map((value) => this.getDisplayName(selectedKey, value)));
     selectedValue = await this.getDisplayName(selectedKey, selectedValue);
 
     const condition: ICondition = {
       keyList: newKeyList,
       valueList: valueList,
       isDisabled: false,
       selectedValue: selectedValue,
       selectedKey: selectedKey
     }
 
     if(this.criterias.length<criteriaIdx+1){
       this.criterias.push([condition]);
     }
     else{
     this.criterias[criteriaIdx].push(condition);
      }
   }
 
   openModal(){
     const dialogRef = this.dialog.open(DialogBoxComponent,
     {
       width: '600px',
       data: {
         title: null,
         yesButtonLabel: "Delete",
         noButtonLabel: "Cancel",
         template: this.actionRef
       }
     });
     dialogRef.afterClosed().subscribe(result => {
       try {
         if (result == "yes") {
               this.deleteAssetGroup();
         }
       } catch (error) {
         this.errorMessage = this.errorHandling.handleJavascriptError(error);
         this.logger.log('error', error);
       }
     });
   }
 
   deleteAssetGroup() {
     let url = environment.deleteAssetGroups.url; 
     let method = environment.deleteAssetGroups.method; 
     const queryParams = {
       "ag": this.selectedAssetGroup == this.assetGroupName? "aws" : this.selectedAssetGroup,
       "domain": this.currentDomain
     };
     this.adminService.executeHttpAction(url, method, {groupId: this.groupId}, {}).subscribe(response => {
       if(response){
         const data = response[0]?.data;
         this.updateComponent();
         this.notificationObservableService.postMessage(data,3000,"","check-circle");
         if(response[0].message==="success"){
           this.assetTilesService.getAssetGroupList().subscribe(response=>{
             console.log(" Delete Asset Group Successfully ");
           })
           let criteriasBeforeUpdate = {};
           this.criteriaDetails?.forEach(crit => {
               if(crit.criteriaName in criteriasBeforeUpdate){
                 criteriasBeforeUpdate[crit.criteriaName][crit.attributeName]=crit.attributeValue;
               }
               else{
                 criteriasBeforeUpdate[crit.criteriaName]={};
                 criteriasBeforeUpdate[crit.criteriaName][crit.attributeName]=crit.attributeValue;
               }
             });
             let agDetails = {groupName : this.assetGroupName, description : this.assetGroupDesc, type : this.selectedAccountType, configuration : Object.values(criteriasBeforeUpdate)};
             delete agDetails['criteriaDetails'];
         }
         this.router.navigate(['../'], {
           relativeTo: this.activatedRoute,
           queryParams: queryParams
          });
       }
     },
     error => {
       this.notificationObservableService.postMessage("Error in deleting asset group",3000,"error","Error");
       this.logger.log("Error in Js",error);
     })
   }
 
   getAssetGroupDisplayName (ag: string): Promise<string> {
     return this.assetTilesService.getAssetGroupDisplayName(ag)
       .pipe(
         takeUntil(this.destroy$),
         map((agDisplayName: string) => agDisplayName ?? ag),
         catchError((error) => {
           console.error('Error occurred: ', error);
           return of(ag);
         })
       )
       .toPromise();
   }

   async getDisplayName (selectedKey: string, selectedValue: string): Promise<string> {
     selectedKey = selectedKey.toLowerCase();

     if (selectedKey === 'source') {
       return this.getAssetGroupDisplayName(selectedValue);
     } else if (selectedKey === 'asset type') {
       if(this.assetTypeMap.get(selectedValue))
         return this.assetTypeMap.get(selectedValue);
     }
     return selectedValue;
   }
 
   getName(selectedKey:string,selectedValue:string){
     selectedKey = selectedKey.toLowerCase();
     if(selectedKey == "source"){
       const keys = Object.keys(DATA_MAPPING);
       return keys.find(key => DATA_MAPPING[key]===selectedValue);
     } else if(selectedKey == "asset type"){
       for (const [key, value] of this.assetTypeMap) {
         if(selectedValue == value){
           return key;
         }
       }
     }
     return selectedValue;
   }
 
   async onKeySelect(criteriaIdx,conditionIdx,selectedKey:string){
     this.selectedCriteriaValues = [];
     let selectedValues = [];
     selectedValues = [].concat(...this.cloudsData.map(cloud => cloud[selectedKey]));
     if(this.selectedCriteriaKeyList[criteriaIdx])
     {
       this.selectedCriteriaKeyList[criteriaIdx].splice(conditionIdx,1);
       this.selectedCriteriaKeyList[criteriaIdx].push(selectedKey);
     }else{
       this.selectedCriteriaKeyList.push([selectedKey]);
     }
     this.criterias[criteriaIdx][conditionIdx].selectedKey = selectedKey;
     this.criterias[criteriaIdx][conditionIdx].selectedValue = "";
     selectedValues = await Promise.all(selectedValues.map((value) => this.getDisplayName(selectedKey, value)));
     this.criterias[criteriaIdx][conditionIdx].valueList = selectedValues;
     this.criterias = [...this.criterias];
   }
 
   onValueSelect(condition:ICondition,selectedValue:string){
     condition.selectedValue = selectedValue;
     this.criterias = [...this.criterias];
   }
 
   deleteCondition(criteriaIdx,conditionIdx){
     if(this.selectedCriteriaKeyList[criteriaIdx])
       this.selectedCriteriaKeyList[criteriaIdx].splice(conditionIdx,1);
     this.criterias[criteriaIdx].splice(conditionIdx,1);
     if(this.criterias[criteriaIdx].length==0){
       this.deleteCriteria(criteriaIdx);
     }
   }
 
   deleteCriteria(criteriaIdx){
     this.criterias.splice(criteriaIdx,1);
   }
 
   addEmptyCriteria(){
     const condition: ICondition = {
       keyList: this.criteriaKeys,
       valueList: [],
       isDisabled: false,
       selectedValue: '',
       selectedKey: ''
     }
     this.criterias.push([condition]);
   }
 
   isNumber(val){
     return !isNaN(val);
   }
 
 
   closeAttributeConfigure() {
     this.state = 'closed';
     this.menuState = 'out';
     this.selectedIndex = -1;
   }
 
   openAttributeConfigure(attributeDetail, index) {
     let dataSourceName = "/aws_";
     if (!attributeDetail.includeAll) {
       this.attributeValue = '';
       this.attributeName = '';
       this.state = 'open';
       this.menuState = 'in';
       this.selectedIndex = index;
       this.selectedAttributeDetails = attributeDetail.allAttributesName;
       this.selectedAttributes = attributeDetail.attributes;
       if(this.isCreate){
       this.selectedAttributeIndex = attributeDetail["index"] + '/_search?filter_path=aggregations.alldata.buckets.key';
       }
       else if(!this.isCreate && attributeDetail.dataSourceName){
         dataSourceName =  "/"+attributeDetail.dataSourceName+"_";
         this.selectedAttributeIndex = dataSourceName + attributeDetail.targetName + '/_search?filter_path=aggregations.alldata.buckets.key';
       }
       else
       this.selectedAttributeIndex = dataSourceName + attributeDetail.targetName + '/_search?filter_path=aggregations.alldata.buckets.key';
     }
   }
 
   includeAllAttributes(attributeDetail, index) {
     this.allAttributeDetails[index].includeAll = !this.allAttributeDetails[index].includeAll;
   }
 
   isGroupNameAvailable(control: AbstractControl) {
     if (this.groupId) return null;
     const alexaKeyword = control.value.toLowerCase().replace(/\s+/g, '-');
     const isKeywordExits = this.assetGroupNames.findIndex(item => alexaKeyword === item.toLowerCase());
     if (isKeywordExits === -1) {
       return null;
     } 
     return { groupNameAlreadyExists: true };
   }
 
   isValidType(control: AbstractControl) {
     if (this.groupId) return null;
     const type = control.value.toLowerCase();
     const typeList = ["system", "user"];
     if (typeList.includes(type)) {
       return { invalidType : true };
     }
     return null;
   }
 
 
   getAllAssetGroupNames() {
     this.hideContent = true;
     this.assetGroupLoader = true;
     this.progressText = 'Loading details';
     this.isAssetGroupFailed = false;
     this.isAssetGroupSuccess = false;
     const url = environment.assetGroupNames.url;
     const method = environment.assetGroupNames.method;
     this.adminService.executeHttpAction(url, method, {}, {}).subscribe(response => {
       this.hideContent = false;
       this.assetGroupLoader = false;
       this.showLoader = false;
       this.assetGroupNames = response[0];
     },
       error => {
         this.assetGroupNames = [];
         this.errorMessage = 'apiResponseError';
         this.showLoader = false;
       });
   }
 
   public selectAttributes(value: any): void {
     this.attributeValue = value;
     this.checkAttributeAlreadyTaken(value);
   }
 
   checkAttributeAlreadyTaken(attributeValue) {
     const attributeSearchedResult = find(this.allAttributeDetails[this.selectedIndex].attributes, { name: this.attributeName, value: attributeValue });
     if (attributeSearchedResult === undefined) {
       this.isAttributeAlreadyAdded = -1;
     } else {
       this.isAttributeAlreadyAdded = 0;
     }
   }
 
   getTargetTypeAttributeValues(attributeName:any) {
     const attrNameObj: any = {};
     attrNameObj.size = 0;
     attrNameObj.aggs = {};
     attrNameObj.aggs.alldata = {};
     attrNameObj.aggs.alldata.terms = {};
     attrNameObj.aggs.alldata.terms.field = attributeName + '.keyword';
     attrNameObj.aggs.alldata.terms.size = 10000;
     this.isAttributeAlreadyAdded = -1;
     this.attributeName = attributeName;
     this.targetTypeAttributeValues = [];
     const url = environment.listTargetTypeAttributeValues.url;
     const method = environment.listTargetTypeAttributeValues.method;
     let queryParams = { index: this.selectedAttributeIndex, payload: JSON.stringify(attrNameObj) };
     this.adminService.executeHttpAction(url, method, queryParams, {}).subscribe(attributeValues => {
       if (attributeValues.length > 0) {
 
         if (attributeValues[0].hasOwnProperty('data')) {
           if (attributeValues[0].data.hasOwnProperty('aggregations')) {
             if (attributeValues[0].data.aggregations.alldata.hasOwnProperty('buckets')) {
               const allAttributeValues = attributeValues[0].data.aggregations.alldata.buckets;
               allAttributeValues.forEach((attrValue) => {
                 const allCurrentAttributeValues = {};
                 allCurrentAttributeValues['text'] = attrValue.key;
                 allCurrentAttributeValues['id'] = attrValue.key;
                 this.targetTypeAttributeValues.push(allCurrentAttributeValues);
               });
               this.targetTypeAttributeValues = [...this.targetTypeAttributeValues];
             }
           }
         }
 
       }
     },
       error => {
         this.targetTypeAttributeValues = [];
       });
   }
 
   getSelectedOption(option: any) {
     this.targetTypeValue = option;
   }
 
   addTagetType(targetTypeValue) {
     const targetTypeName = targetTypeValue;
     const targetTypeDetails1 = find(this.remainingTargetTypesFullDetails, { targetName: targetTypeName });
     const targetTypeDetails2 = find(this.remainingTargetTypes, { id: targetTypeName });
     this.allAttributeDetails.push(targetTypeDetails1);
     const itemIndex2 = this.remainingTargetTypes.indexOf(targetTypeDetails2);
     this.remainingTargetTypes.splice(itemIndex2, 1);
     this.targetTypeValue = "";
   }
 
   addAttributes(attributeName, attributeValue) {
     this.allAttributeDetails[this.selectedIndex].attributes.push({ name: attributeName, value: attributeValue });
     this.attributeValue = '';
     this.attributeName = '';
   }
 
   deleteAttributes(attributeName, itemIndex) {
     this.allAttributeDetails[this.selectedIndex].attributes.splice(itemIndex, 1);
   }
 
   nextPage() {
     try {
       if (!this.isLastPage) {
         this.pageNumber++;
         this.showLoader = true;
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
       }
 
     } catch (error) {
       this.errorMessage = this.errorHandling.handleJavascriptError(error);
       this.logger.log('error', error);
     }
   }
 
   nextStep() {
     if (!this.isCreate) {
       this.goToNextStep();
     } else {
       if (this.stepIndex + 1 === 1) {
         if (!this.pageContent[this.stepIndex].isChanged) {
           this.assetLoaderFailure = false;
           this.assetLoader = true;
           this.assetLoaderTitle = 'Domain';
           this.pageContent[0].hide = true;
           const url = environment.domains.url;
           const method = environment.domains.method;
           this.adminService.executeHttpAction(url, method, {}, {}).subscribe(reponse => {
             this.assetLoader = false;
             this.showLoader = false;
             if (reponse !== undefined) {
               this.availableItems = reponse[0];
               this.selectedItems = [];
               this.availableItemsBackUp = cloneDeep(this.availableItems);
               this.selectedItemsBackUp = cloneDeep(this.selectedItems);
               this.availableItemsCopy = cloneDeep(this.availableItems);
               this.selectedItemsCopy = cloneDeep(this.selectedItems);
               this.searchAvailableDomains();
               this.searchSelectedDomains();
               this.goToNextStep();
             }
           },
             error => {
               this.assetLoader = false;
               this.assetLoaderFailure = true;
               this.errorValue = -1;
               this.outerArr = [];
               this.errorMessage = 'apiResponseError';
               this.showLoader = false;
             });
         } else {
           this.searchAvailableDomains();
           this.searchSelectedDomains();
           this.goToNextStep();
         }
       } else if (this.stepIndex + 1 === 2) {
         if (!this.pageContent[this.stepIndex].isChanged) {
           this.assetLoaderTitle = 'Target';
           this.assetLoaderFailure = false;
           this.assetLoader = true;
           this.pageContent[1].hide = true;
           const url = environment.targetTypesByDomains.url;
           const method = environment.targetTypesByDomains.method;
           const domainList = this.selectedItems.map(domain => domain.domainName);
           this.adminService.executeHttpAction(url, method, domainList, {}).subscribe(reponse => {
             this.assetLoader = false;
             this.showLoader = false;
             if (reponse !== undefined) {
               this.availableTdItems = reponse[0].data;
               this.selectedTdItems = [];
               if (this.selectedTdItemsCopyForPrevNext.length > 0) {
                 this.selectedTdItemsCopyForPrevNext.forEach(tdItem => {
                   const availableTdSearchedResult = find(this.availableTdItems, { targetName: tdItem.targetName });
                   const itemIndex = this.availableTdItems.indexOf(availableTdSearchedResult);
                   if (itemIndex !== -1) {
                     this.availableTdItems.splice(itemIndex, 1);
                     this.selectedTdItems.push(availableTdSearchedResult);
                   }
                 });
               }
               this.availableTdItemsBackUp = cloneDeep(this.availableTdItems);
               this.selectedTdItemsBackUp = cloneDeep(this.selectedTdItems);
               this.availableTdItemsCopy = cloneDeep(this.availableTdItems);
               this.selectedTdItemsCopy = cloneDeep(this.selectedTdItems);
               this.searchAvailableTargets();
               this.searchSelectedTargets();
               this.goToNextStep();
             }
           },
             error => {
               this.assetLoader = false;
               this.assetLoaderFailure = true;
               this.errorValue = -1;
               this.outerArr = [];
               this.errorMessage = 'apiResponseError';
               this.showLoader = false;
             });
         } else {
           this.searchAvailableDomains();
           this.searchSelectedDomains();
           this.goToNextStep();
         }
       } else if (this.stepIndex + 1 === 3) {
         if (!this.pageContent[this.stepIndex].isChanged) {
           this.assetLoaderTitle = 'Target Attributes';
           this.assetLoaderFailure = false;
           this.assetLoader = true;
           this.pageContent[2].hide = true;
           const url = environment.targetTypesAttributes.url;
           const method = environment.targetTypesAttributes.method;
           this.adminService.executeHttpAction(url, method, this.selectedTdItems, {}).subscribe(reponse => {
             this.assetLoader = false;
             this.showLoader = false;
             if (reponse !== undefined) {
               this.allAttributeDetails = reponse[0].data;
               if (this.allAttributeDetailsCopyForPrevNext.length > 0) {
                 this.allAttributeDetailsCopyForPrevNext.forEach(attrElement => {
                   const attributeSearchedResult = find(this.allAttributeDetails, { targetName: attrElement.targetName });
                   const itemIndex = this.allAttributeDetails.indexOf(attributeSearchedResult);
                   if (itemIndex !== -1) {
                     this.allAttributeDetails[itemIndex] = attrElement;
                   }
                 });
               }
               this.allSelectedAttributeDetailsCopy = cloneDeep(this.allAttributeDetails);
               this.goToNextStep();
             }
           },
             error => {
               this.assetLoader = false;
               this.assetLoaderFailure = true;
               this.errorValue = -1;
               this.outerArr = [];
               this.errorMessage = 'apiResponseError';
               this.showLoader = false;
             });
         } else {
           this.goToNextStep();
         }
       } else {
         this.goToNextStep();
       }
     }
   }
 
   goToNextStep() {
     this.pageContent[this.stepIndex].hide = true;
     this.pageContent[this.stepIndex].isChanged = true;
     if (this.isCreate) {
       this.stepIndex++;
     } else {
       this.stepIndex += 3;
     }
     this.stepTitle = this.pageContent[this.stepIndex].title;
     this.pageContent[this.stepIndex].hide = false;
   }
 
   prevStep() {
     this.assetLoaderFailure = false;
     this.assetLoader = false;
     this.pageContent[this.stepIndex].hide = true;
     if (this.isCreate) {
       this.stepIndex--;
     } else {
       this.stepIndex -= 3;
     }
     this.stepTitle = this.pageContent[this.stepIndex].title;
     this.pageContent[this.stepIndex].hide = false;
     if (this.stepIndex + 1 === 3) {
       this.allAttributeDetailsCopyForPrevNext = cloneDeep(this.allAttributeDetails);
     }
     if (this.stepIndex + 1 === 2) {
       this.selectedTdItemsCopyForPrevNext = cloneDeep(this.selectedTdItems);
     }
   }
 
   closeAssetErrorMessage() {
     this.assetLoaderFailure = false;
     this.assetLoader = false;
     this.pageContent[this.stepIndex].hide = false;
   }
 
   submit() {
     this.buttonClicked = true;
     const url = this.submitBtn =="Confirm and Create"?
     environment.createAssetGroups.url:environment.updateAssetGroups.url;
     const method = environment.createAssetGroups.method;
   
     let criteriaList: object[] = [];
     this.criterias.forEach(criteria=>{
       let obj = {};
       criteria.forEach(condition=>{
         const conditionKey = this.AttributeKeyMap[condition.selectedKey]? this.AttributeKeyMap[condition.selectedKey] : condition.selectedKey;
          obj[conditionKey] = this.getName(condition.selectedKey,condition.selectedValue);
       })
       criteriaList.push(obj);
     })
     const payload = {
       "type": this.selectedAccountType,
       "groupName": this.assetGroupDisplayName,
       "description": this.assetGroupDesc,
       "createdBy": this.createdBy,
       "configuration": criteriaList
     }
     if(this.submitBtn.toLowerCase() == "confirm and update"){
       payload["groupId"] = this.groupId;
     }
     const queryParams = {
       "ag": this.selectedAssetGroup,
       "domain": this.currentDomain
     };
     this.adminService.executeHttpAction(url, method, payload, {}).subscribe(response => {
       if(response && response[0]){
         this.assetTilesService.getAssetGroupList().subscribe(response=>{
           console.log(" Updated Asset Group List ");
         })
          const data = response[0].data;
         if(response[0]['message']==="success"){
           if(this.submitBtn =="Confirm and Create"){
            //  this.activityLogService.saveActivityLog('Asset Group',this.assetGroupName,'create','NA',JSON.stringify(payload));
           }
         else{
           let criteriasBeforeUpdate = {};
           for (let obj in this.configurationsBeforeEdit['configuration']) {
             this.configurationsBeforeEdit['configuration'][obj]?.forEach(crit => {
               if(crit.criteriaName in criteriasBeforeUpdate){
                 criteriasBeforeUpdate[crit.criteriaName][crit.attributeName]=crit.attributeValue;
               }
               else{
                 criteriasBeforeUpdate[crit.criteriaName]={};
                 criteriasBeforeUpdate[crit.criteriaName][crit.attributeName]=crit.attributeValue;
               }
             });
           }
           this.configurationsBeforeEdit['configuration']=Object.values(criteriasBeforeUpdate);
         }
         this.notificationObservableService.postMessage(data,3000,"","check-circle");
         this.router.navigate(['../'], {
          relativeTo: this.activatedRoute,
          queryParams: queryParams
         });
         }
       }
     },  
      error => {
         this.assetGroupLoader = false;
         this.isAssetGroupFailed = true;
         if(this.submitBtn =="Confirm and Create"){
           this.failedTitleStart = 'Failed Creating Asset Group !!!';
         }
         else{
           this.failedTitleStart = 'Failed Updating Asset Group !!!';
         }
         this.failedTitleEnd = '!!!';
         this.notificationObservableService.postMessage(this.failedTitleStart,3000,"error","Error");
         this.router.navigate(['../'], {
           relativeTo: this.activatedRoute,
           queryParamsHandling: 'merge',
           queryParams: {}
         });
       });
   }
 
   isStepDisabled(stepIndex) {
     if (stepIndex === 0) {
       if (this.assetForm.groupName.trim().length>=3 && this.assetForm.displayName.trim().length>=3 &&
         this.assetForm.type.trim().length>=5 && this.assetForm.description.trim().length>=15 && this.assetForm.createdBy.trim().length>=5 && this.isGroupNameValid === 1 && !(ONLY_DIGITS.test(this.assetForm.createdBy.trim()))) {
         return false;
       }
     } else if (stepIndex === 1) {
       return (this.selectedItems.length === 0);
     } else if (stepIndex === 2) {
       return (this.selectedTdItems.length === 0);
     }
     return true;
   }
 
   closeErrorMessage() {
     this.showWidget = true;
     this.isAssetGroupFailed = false;
     this.hideContent = false;
   }
 
   MatchAttribute(atrributeDetail:any) {
     const term = this.searchTerm;
     return atrributeDetail.targetName.indexOf(term) >= 0;
   }
 
   onClickAvailableItem(index, availableItem, key) {
     if (this.availChoosedItems.hasOwnProperty(index)) {
       this.availChoosedItems[index] = !this.availChoosedItems[index];
       if (this.availChoosedItems[index]) {
         this.availChoosedSelectedItems[key] = availableItem;
       } else {
         delete this.availChoosedSelectedItems[key];
       }
 
     } else {
       this.availChoosedItems[index] = true;
       this.availChoosedSelectedItems[key] = availableItem;
     }
     this.availChoosedItemsCount = Object.keys(this.availChoosedSelectedItems).length;
   }
 
   onClickSelectedItem(index, selectedItem, key) {
     if (this.selectChoosedItems.hasOwnProperty(index)) {
       this.selectChoosedItems[index] = !this.selectChoosedItems[index];
       if (this.selectChoosedItems[index]) {
         this.selectChoosedSelectedItems[key] = selectedItem;
       } else {
         delete this.selectChoosedSelectedItems[key];
       }
     } else {
       this.selectChoosedItems[index] = true;
       this.selectChoosedSelectedItems[key] = selectedItem;
     }
     this.selectChoosedItemsCount = Object.keys(this.selectChoosedSelectedItems).length;
   }
 
   moveAllItemsToLeft() {
     this.pageContent[this.stepIndex].isChanged = false;
     this.pageContent[2].isChanged = false;
     if (this.searchSelectedDomainTerms.length === 0) {
       this.availableItems = cloneDeep(this.availableItemsBackUp);
       this.availableItemsCopy = cloneDeep(this.availableItemsBackUp);
       this.selectedItems = [];
       this.selectedItemsCopy = [];
       this.selectChoosedItems = {};
       this.selectChoosedSelectedItems = {};
       this.selectChoosedItemsCount = 0;
       this.searchAvailableDomains();
       this.searchSelectedDomains();
     } else {
       this.selectChoosedSelectedItems = {};
       this.selectedItems.forEach((element) => {
 
         this.selectChoosedSelectedItems[element.domainName] = element;
       });
       this.moveItemToLeft();
     }
   }
 
   moveAllItemsToRight() {
     this.pageContent[this.stepIndex].isChanged = false;
     this.pageContent[2].isChanged = false;
     if (this.searchAvailableDomainTerms.length === 0) {
       this.selectedItems = cloneDeep(this.availableItemsBackUp);
       this.selectedItemsCopy = cloneDeep(this.availableItemsBackUp);
       this.availableItemsCopy = [];
       this.availableItems = [];
       this.availChoosedItems = {};
       this.availChoosedSelectedItems = {};
       this.availChoosedItemsCount = 0;
       this.searchAvailableDomains();
       this.searchSelectedDomains();
     } else {
       this.availChoosedSelectedItems = {};
       this.availableItems.forEach((element) => {
         this.availChoosedSelectedItems[element.domainName] = element;
       });
       this.moveItemToRight();
     }
   }
 
   moveItemToRight() {
     this.pageContent[this.stepIndex].isChanged = false;
     this.pageContent[2].isChanged = false;
     const selectedItemsCopy = this.selectedItemsCopy;
     const availableItemsCopy = this.availableItemsCopy;
     for (const choosedSelectedKey in this.availChoosedSelectedItems) {
       if (this.availChoosedSelectedItems.hasOwnProperty(choosedSelectedKey)) {
         selectedItemsCopy.push(this.availChoosedSelectedItems[choosedSelectedKey]);
         const filterIndex = availableItemsCopy.indexOf(this.availChoosedSelectedItems[choosedSelectedKey]);
         availableItemsCopy.splice(filterIndex, 1);
       }
     }
 
     this.availableItems = availableItemsCopy;
     if (this.searchAvailableDomainTerms.length !== 0) {
       this.searchAvailableDomains();
     }
 
     this.selectedItems = selectedItemsCopy;
     if (this.searchSelectedDomainTerms.length !== 0) {
       this.searchSelectedDomains();
     }
 
     this.availChoosedItems = {};
     this.availChoosedSelectedItems = {};
     this.availChoosedItemsCount = 0;
   }
 
   moveItemToLeft() {
     this.pageContent[this.stepIndex].isChanged = false;
     this.pageContent[2].isChanged = false;
     const selectedItemsCopy = this.selectedItemsCopy;
     const availableItemsCopy = this.availableItemsCopy;
     for (const choosedSelectedKey in this.selectChoosedSelectedItems) {
       if (this.selectChoosedSelectedItems.hasOwnProperty(choosedSelectedKey)) {
         availableItemsCopy.push(this.selectChoosedSelectedItems[choosedSelectedKey]);
         const filterIndex = selectedItemsCopy.indexOf(this.selectChoosedSelectedItems[choosedSelectedKey]);
         selectedItemsCopy.splice(filterIndex, 1);
       }
     }
 
     this.availableItems = availableItemsCopy;
     if (this.searchAvailableDomainTerms.length !== 0) {
       this.searchAvailableDomains();
     }
 
     this.selectedItems = selectedItemsCopy;
     if (this.searchSelectedDomainTerms.length !== 0) {
       this.searchSelectedDomains();
     }
 
     this.selectChoosedItems = {};
     this.selectChoosedSelectedItems = {};
     this.selectChoosedItemsCount = 0;
   }
 
 
   searchAvailableDomains() {
     const term = this.searchAvailableDomainTerms;
     this.availableItems = this.availableItemsCopy.filter(function (tag) {
       return tag.domainName.toLowerCase().indexOf(term.toLowerCase()) >= 0;
     });
   }
 
   searchSelectedDomains() {
     const term = this.searchSelectedDomainTerms;
     this.selectedItems = this.selectedItemsCopy.filter(function (tag) {
       return tag.domainName.toLowerCase().indexOf(term.toLowerCase()) >= 0;
     });
   }
   /*
   *
         TARGET DETAILS
   *
   */
   onClickAvailableTdItem(index, availableItem, key) {
     if (this.availTdChoosedItems.hasOwnProperty(index)) {
       this.availTdChoosedItems[index] = !this.availTdChoosedItems[index];
       if (this.availTdChoosedItems[index]) {
         this.availTdChoosedSelectedItems[key] = availableItem;
       } else {
         delete this.availTdChoosedSelectedItems[key];
       }
 
     } else {
       this.availTdChoosedItems[index] = true;
       this.availTdChoosedSelectedItems[key] = availableItem;
     }
     this.availTdChoosedItemsCount = Object.keys(this.availTdChoosedSelectedItems).length;
   }
 
   onClickSelectedTdItem(index, selectedItem, key) {
     if (this.selectTdChoosedItems.hasOwnProperty(index)) {
       this.selectTdChoosedItems[index] = !this.selectTdChoosedItems[index];
       if (this.selectTdChoosedItems[index]) {
         this.selectTdChoosedSelectedItems[key] = selectedItem;
       } else {
         delete this.selectTdChoosedSelectedItems[key];
       }
     } else {
       this.selectTdChoosedItems[index] = true;
       this.selectTdChoosedSelectedItems[key] = selectedItem;
     }
     this.selectTdChoosedItemsCount = Object.keys(this.selectTdChoosedSelectedItems).length;
   }
 
   moveTdAllItemsToLeft() {
     this.pageContent[this.stepIndex].isChanged = false;
     if (this.searchSelectedTargetTerms.length === 0) {
       this.availableTdItems = cloneDeep(this.availableTdItemsBackUp);
       this.availableTdItemsCopy = cloneDeep(this.availableTdItemsBackUp);
       this.selectedTdItems = [];
       this.selectedTdItemsCopy = [];
       this.selectTdChoosedItems = {};
       this.selectTdChoosedSelectedItems = {};
       this.selectTdChoosedItemsCount = 0;
       this.searchAvailableTargets();
       this.searchSelectedTargets();
     } else {
       this.selectTdChoosedSelectedItems = {};
       this.selectedTdItems.forEach((element) => {
 
         this.selectTdChoosedSelectedItems[element.targetName] = element;
       });
       this.moveTdItemToLeft();
     }
   }
 
   moveTdAllItemsToRight() {
     this.pageContent[this.stepIndex].isChanged = false;
     if (this.searchAvailableTargetTerms.length === 0) {
       this.selectedTdItems = cloneDeep(this.availableTdItemsBackUp);
       this.selectedTdItemsCopy = cloneDeep(this.availableTdItemsBackUp);
       this.availableTdItemsCopy = [];
       this.availableTdItems = [];
       this.availTdChoosedItems = {};
       this.availTdChoosedSelectedItems = {};
       this.availTdChoosedItemsCount = 0;
       this.searchAvailableTargets();
       this.searchSelectedTargets();
     } else {
       this.availTdChoosedSelectedItems = {};
       this.availableTdItems.forEach((element) => {
         this.availTdChoosedSelectedItems[element.targetName] = element;
       });
       this.moveTdItemToRight();
     }
   }
 
   moveTdItemToRight() {
     this.pageContent[this.stepIndex].isChanged = false;
     const selectedTdItemsCopy = this.selectedTdItemsCopy;
     const availableTdItemsCopy = this.availableTdItemsCopy;
     for (const choosedTdSelectedKey in this.availTdChoosedSelectedItems) {
       if (this.availTdChoosedSelectedItems.hasOwnProperty(choosedTdSelectedKey)) {
         selectedTdItemsCopy.push(this.availTdChoosedSelectedItems[choosedTdSelectedKey]);
         const filterIndex = availableTdItemsCopy.indexOf(this.availTdChoosedSelectedItems[choosedTdSelectedKey]);
         availableTdItemsCopy.splice(filterIndex, 1);
       }
     }
 
     this.availableTdItems = availableTdItemsCopy;
     if (this.searchAvailableTargetTerms.length !== 0) {
       this.searchAvailableTargets();
     }
 
     this.selectedTdItems = selectedTdItemsCopy;
     if (this.searchSelectedTargetTerms.length !== 0) {
       this.searchSelectedTargets();
     }
 
     this.availTdChoosedItems = {};
     this.availTdChoosedSelectedItems = {};
     this.availTdChoosedItemsCount = 0;
   }
 
   moveTdItemToLeft() {
     this.pageContent[this.stepIndex].isChanged = false;
     const selectedTdItemsCopy = this.selectedTdItemsCopy;
     const availableTdItemsCopy = this.availableTdItemsCopy;
     for (const choosedTdSelectedKey in this.selectTdChoosedSelectedItems) {
       if (this.selectTdChoosedSelectedItems.hasOwnProperty(choosedTdSelectedKey)) {
         availableTdItemsCopy.push(this.selectTdChoosedSelectedItems[choosedTdSelectedKey]);
         const filterIndex = selectedTdItemsCopy.indexOf(this.selectTdChoosedSelectedItems[choosedTdSelectedKey]);
         selectedTdItemsCopy.splice(filterIndex, 1);
       }
     }
 
     this.availableTdItems = availableTdItemsCopy;
     if (this.searchAvailableTargetTerms.length !== 0) {
       this.searchAvailableTargets();
     }
 
     this.selectedTdItems = selectedTdItemsCopy;
     if (this.searchSelectedTargetTerms.length !== 0) {
       this.searchSelectedTargets();
     }
 
     this.selectTdChoosedItems = {};
     this.selectTdChoosedSelectedItems = {};
     this.selectTdChoosedItemsCount = 0;
   }
 
 
   searchAvailableTargets() {
     const term = this.searchAvailableTargetTerms;
     this.availableTdItems = this.availableTdItemsCopy.filter(function (tag) {
       return tag.targetName.toLowerCase().indexOf(term.toLowerCase()) >= 0;
     });
   }
 
   searchSelectedTargets() {
     const term = this.searchSelectedTargetTerms;
     this.selectedTdItems = this.selectedTdItemsCopy.filter(function (tag) {
       return tag.targetName.toLowerCase().indexOf(term.toLowerCase()) >= 0;
     });
   }
 
 
   getAssetGroupDetails() {
     const url = environment.assetGroupDetailsById.url;
     const method = environment.assetGroupDetailsById.method;
     this.adminService.executeHttpAction(url, method, {}, { assetGroupId: this.groupId, dataSource: '' }).subscribe(assetGroupReponse => {
     const assetGroupDetails = assetGroupReponse[0];
     this.assetGroupName = assetGroupDetails.groupName;
     this.assetGroupDisplayName = assetGroupDetails.displayName;
     this.assetGroupDesc = assetGroupDetails.description;
     this.createdBy = assetGroupDetails.createdBy;
     this.selectedAccountType = assetGroupDetails.type;
     this.criteriaDetails = assetGroupDetails.criteriaDetails;
     this.getAttributesData(this.criteriaDetails);
     },
       error => {
         this.assetGroupLoader = false;
         this.isAssetGroupFailed = true;
         this.failedTitleStart = 'Failed in loading Asset Group';
         this.failedTitleEnd = '!!!';
       });
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
         this.groupId = this.FullQueryParams.groupId;
         this.groupName = this.FullQueryParams.groupName;
         this.selectedAssetGroup = this.FullQueryParams.ag;
         this.currentDomain = this.FullQueryParams.domain;
         this.queryParamsWithoutFilter = JSON.parse(JSON.stringify(this.FullQueryParams));
         delete this.queryParamsWithoutFilter['filter'];
         this.getAllAssetGroupNames();
         this.buildForm();
         if (this.groupId) {
           this.pageTitle = 'Edit Asset Group';
           this.breadcrumbPresent = 'Edit Asset Group';
           this.submitBtn = "Confirm and Update";
           this.isCreate = false;
           this.highlightName = this.groupName;
           this.highlightedText = this.groupName;
           this.getAssetGroupDetails();
           this.stepIndex = 0;
           this.pageContent[0].hide = true;
           this.pageContent[1].hide = true;
           this.pageContent[2].hide = true;
 
           this.pageContent[this.stepIndex].hide = false;
           this.stepTitle = 'Update Group Details - ' + this.groupName;
         } else {
           this.pageTitle = 'Create Asset Group';
           this.getAttributesData();
           this.breadcrumbPresent = 'Create Asset Group';
           this.isCreate = true;
         }
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
 
   updateComponent() {
     this.outerArr = [];
     this.showLoader = true;
     this.errorValue = 0;
   }
 
   navigateBack() {
     try {
       this.workflowService.goBackToLastOpenedPageAndUpdateLevel(this.router.routerState.snapshot.root);
     } catch (error) {
       this.logger.log('error', error);
     }
   }
 
   ngOnDestroy () {
     this.destroy$.next();
     this.destroy$.complete()
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
   
   pageCounter(clickedButton: string) {
     if (clickedButton == 'back') {
       this.currentStepperIndex--;
     } else
       this.currentStepperIndex++;
     if(this.currentStepperIndex==2){
       this.filterConfigureData();
     }
     this.selectedStepperIndex(this.currentStepperIndex);
   }
 
   selectedStepperIndex(event: any) {
     const index = this.stepperData.findIndex(element => element.id == event);
     this.currentStepperIndex = event;
     this.currentStepperName = this.stepperData[index].name;
   }
 
   getCurrentTemplate(){
     if(this.currentStepperName == "Asset Group Details"){
       this.currentTemplateRef = this.assetGroupRef;
     } else if(this.currentStepperName == "Configuration"){
       this.currentTemplateRef = this.configurationRef;
     } else{
       this.currentTemplateRef = this.reviewRef;
     }
     return this.currentTemplateRef;
   }
   
   filterConfigureData(){
     let deleteIndexes = [];
     this.criterias.forEach((criteria,index)=>{
       if(!criteria[0].selectedKey){
         deleteIndexes.push(index);
       } else{
         const size = criteria.length-1;
         if(!criteria[size].selectedKey || !criteria[size].selectedValue){
           this.deleteCondition(index,size);
         }
       }
     })
 
     deleteIndexes.forEach(id=>{
       this.deleteCriteria(id);
     })
   }
 
   onSelectType(event:any){
     this.selectedAccountType = event;
   }
 }
 
