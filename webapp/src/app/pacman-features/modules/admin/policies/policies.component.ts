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

 import { Component, OnInit, OnDestroy, TemplateRef, ViewChild } from "@angular/core";
 import { environment } from "./../../../../../environments/environment";
 
 import { ActivatedRoute, Router } from "@angular/router";
 import { Subject, Subscription } from "rxjs";
 import { UtilsService } from "../../../../shared/services/utils.service";
 import { LoggerService } from "../../../../shared/services/logger.service";
 import { ErrorHandlingService } from "../../../../shared/services/error-handling.service";
 import { WorkflowService } from "../../../../core/services/workflow.service";
 import { RouterUtilityService } from "../../../../shared/services/router-utility.service";
 import { AdminService } from "../../../services/all-admin.service";
 import { TableStateService } from "src/app/core/services/table-state.service";
 import { MatDialog } from "@angular/material/dialog";
 import { DialogBoxComponent } from "src/app/shared/components/molecules/dialog-box/dialog-box.component";
 import { NotificationObservableService } from "src/app/shared/services/notification-observable.service";
 import { ComponentKeys } from "src/app/shared/constants/component-keys";
 import { find, map } from "lodash";
 import { FilterManagementService } from "src/app/shared/services/filter-management.service";
 import { CategoryOrderMap, SeverityOrderMap } from "src/app/shared/constants/order-mapping";
 import { AssetTypeMapService } from "src/app/core/services/asset-type-map.service";
import { IColumnNamesMap, IColumnWidthsMap, IFilterOption } from "src/app/shared/table/interfaces/table-props.interface";
 
 @Component({
   selector: "app-admin-policies",
   templateUrl: "./policies.component.html",
   styleUrls: ["./policies.component.css"],
   providers: [LoggerService, ErrorHandlingService, AdminService],
 })
 export class PoliciesComponent implements OnInit, OnDestroy {
   pageTitle: String = "Policies";
   saveStateKey: String = ComponentKeys.AdminPolicyList;
   allPolicies: any = [];
 
   filterTypeLabels = [];
   filterTagLabels = {};
 
   outerArr: any = [];
   dataLoaded: boolean = false;
   errorMessage: any;
   showingArr: any = ["policyName", "policyId", "policyDesc"];
   allColumns: any = [];
   totalRows: number = 0;
   currentBucket: any = [];
   bucketNumber: number = 0;
   firstPaginator: number = 1;
   lastPaginator: number;
   currentPointer: number = 0;
   seekdata: boolean = false;
   showLoader: boolean = true;
 
   headerColName: string;
   direction: string;
   columnNamesMap: IColumnNamesMap = {"policyDisplayName": "Policy","targetDisplayName": "Asset Type",  "severity": "Severity", "category":"Category", "status": "Status", "assetGroup":"Source"};
   columnWidths: IColumnWidthsMap = {"Policy": 2.5, "Asset Type": 0.7, "Severity": 0.5, "Category": 0.5, "Status": 0.5, "Source":0.5}
   centeredColumns = {
     Severity: true,
     Category: true,
     Source: true,
   };
   whiteListColumns;
   isStatePreserved = false;
   tableScrollTop = 0;
   @ViewChild("enableOrDisablePolicyRef") enableOrDisablePolicyRef: TemplateRef<any>;
   onScrollDataLoader: Subject<any> = new Subject<any>();
   filterFunctionMap = {
     'Autofix status': (item, filterKey, filterValue) => {
       if(filterValue=="available"){
         return item[filterKey].valueText.toLowerCase()=="enabled" || item[filterKey].valueText.toLowerCase()=="available";  
       }else if(filterValue=="enabled"){
         return item[filterKey].valueText.toLowerCase()=="enabled";
       }else{
         return item[filterKey].valueText.toLowerCase()!="enabled" && item[filterKey].valueText.toLowerCase()!="available";
       }
     }
   }
   columnsSortFunctionMap = {
     Severity: (a, b, isAsc) => {
       let severeness = {"low":1, "medium":2, "high":3, "critical":4, "default": 5 * (isAsc ? 1 : -1)}
 
       const ASeverity = a["Severity"].valueText??"default";
       const BSeverity = b["Severity"].valueText??"default";
       return (severeness[ASeverity] < severeness[BSeverity] ? -1 : 1) * (isAsc ? 1 : -1);
     },
     Category: (a, b, isAsc) => {
       let priority = {"security":4, "operations":3, "cost":2, "tagging":1, "default": 5 * (isAsc ? 1 : -1)}
 
       const ACategory = a["Category"].valueText??"default";
       const BCategory = b["Category"].valueText??"default";
       return (priority[ACategory] < priority[BCategory] ? -1 : 1) * (isAsc ? 1 : -1);
     },
   }
   tableImageDataMap = {
       security:{
           image: "category-security",
           imageOnly: true
       },
       operations:{
           image: "category-operations",
           imageOnly: true
       },
       cost:{
           image: "category-cost",
           imageOnly: true
       },
       tagging:{
           image: "category-tagging",
           imageOnly: true
       },
       low: {
           image: "violations-low-icon",
           imageOnly: true
       },
       medium: {
           image: "violations-medium-icon",
           imageOnly: true
       },
       high: {
           image: "violations-high-icon",
           imageOnly: true
       },
       critical: {
           image: "violations-critical-icon",
           imageOnly: true
       },
       aws:{
         image: "aws-color",
         imageOnly: true
       },
       azure:{
         image: "azure-color",
         imageOnly: true
       },
       gcp:{
         image: "gcp-color",
         imageOnly: true
       },
       "red hat":{
         image: "redhat-color",
         imageOnly: true
       }
   }
 
   paginatorSize: number = 100;
   isLastPage: boolean;
   isFirstPage: boolean;
   totalPages: number;
   pageNumber: number = 0;
 
   searchTxt: string = "";
   selectedRowId: string;
   tableData: any = [];
   tableDataLoaded: boolean = false;
   filters: any = [];
   searchCriteria: any;
   filterText: any = {};
   errorValue: number = 0;
   showGenericMessage: boolean = false;
   dataTableDesc: String = "";
   urlID: string = "";
   public labels: any;
   FullQueryParams: any;
   queryParamsWithoutFilter: any;
   urlToRedirect: any = "";
   private pageLevel = 0;
   public backButtonRequired;
   mandatory: any;
   private routeSubscription: Subscription;
   private previousUrlSubscription: Subscription;
   selectedRowTitle: any;
   action: any;
   filtersDataSubscription: Subscription;
   filterTypeOptions: IFilterOption[] = [];
   filterTagOptions: any = {};
   currentFilterType: IFilterOption;
   fieldType: string;
   fieldName: string;
   selectedOrder: string;
   sortOrder: string[];
   dataSubscription: Subscription;
 
   constructor(
     private activatedRoute: ActivatedRoute,
     private router: Router,
     private utils: UtilsService,
     private filterManagementService: FilterManagementService,
     private logger: LoggerService,
     private errorHandling: ErrorHandlingService,
     private workflowService: WorkflowService,
     private routerUtilityService: RouterUtilityService,
     private adminService: AdminService,
     private tableStateService: TableStateService,
     private notificationObservableService: NotificationObservableService,
     private assetTypeMapService: AssetTypeMapService,
     public dialog: MatDialog,
   ) { }
 
   ngOnInit() {
     this.notificationObservableService.getMessage();
     this.urlToRedirect = this.router.routerState.snapshot.url;
     this.backButtonRequired = this.workflowService.checkIfFlowExistsCurrently(
       this.pageLevel
     );
     this.getPreservedState();
     this.backButtonRequired =
       this.workflowService.checkIfFlowExistsCurrently(this.pageLevel);
     this.getFilters();
   }
 
   getFilters() {
     try {
       this.filtersDataSubscription = this.filterManagementService
       .getFilters(17)
         .subscribe(async(filterOptions) => {
           this.filterTypeOptions = filterOptions;
           this.filterTypeLabels = map(filterOptions, "optionName");
           
           this.filterTypeLabels.sort();
           this.routerParam();
           await this.getFilterArray();
           await Promise.resolve().then(() => this.getUpdatedUrl());
           this.updateComponent();
         });
     } catch (error) {
       this.errorMessage = this.errorHandling.handleJavascriptError(error);
       this.logger.log("error", error);
     }
   }
 
   async changeFilterType(value, searchText='') {  
     try {
       const currentQueryParams =
         this.routerUtilityService.getQueryParametersFromSnapshot(
           this.router.routerState.snapshot.root
         );
       this.currentFilterType = find(this.filterTypeOptions, { optionName: value });
       const filtersToBePassed = this.getFilterPayloadForDataAPI();
       const filterText = this.filterText;
       const currentFilterType = this.currentFilterType;
 
       const [updateFilterTags, labelsToExcludeSort] = this.getUpdateFilterTagsCallback();
       const [filterTagOptions, filterTagLabels] = await this.filterManagementService.changeFilterType({currentFilterType, filterText, filtersToBePassed, type:undefined, currentQueryParams, agAndDomain:{}, searchText, updateFilterTags, labelsToExcludeSort});
       this.filterTagOptions[value] = filterTagOptions;
       this.filterTagLabels[value] = filterTagLabels;
       this.storeState();
   
     } catch (error) {
       this.errorMessage = this.errorHandling.handleJavascriptError(error);
       this.logger.log("error", error);
     }
   }
 
   getUpdateFilterTagsCallback(){
     const labelsToExcludeSort = [];
     const updateFilterTags = (filterTagsData, value) => {      
       if (value.toLowerCase() === "asset type") {
         this.assetTypeMapService.getAssetMap().subscribe(assetTypeMap=>{
           filterTagsData.forEach(filterOption => {
               filterOption["name"] = assetTypeMap.get(filterOption["name"]?.toLowerCase()) || filterOption["name"]
           });
         });
       }
       return filterTagsData;
     }
     return [updateFilterTags, labelsToExcludeSort];
   }
 
   /*
    * this functin passes query params to filter component to show filter
    */
   async getFilterArray() {
     try {
       const filterText = this.filterText;
       const filterTypeOptions = this.filterTypeOptions;
       let filters = this.filters;
       
       const formattedFilters = this.filterManagementService.getFormattedFilters(filterText, filterTypeOptions);
 
       for (let i = 0; i < formattedFilters.length; i++) {
         filters = await this.processAndAddFilterItem({ formattedFilterItem: formattedFilters[i] , filters});
         this.filters = filters;
       }
       this.storeState();
     } catch (error) {
       this.errorMessage = this.errorHandling.handleJavascriptError(error);
       this.logger.log("error", error);
     }
   }
 
   async processAndAddFilterItem({formattedFilterItem, filters}){
 
     const keyDisplayValue = this.utils.getFilterKeyDisplayValue(formattedFilterItem, this.filterTypeOptions);
     const filterKey = formattedFilterItem.filterkey;
       
     const existingFilterObjIndex = filters.findIndex(filter => filter.keyDisplayValue === keyDisplayValue);
     if(existingFilterObjIndex<0){
       // we make API call by calling changeFilterType mathod to fetch filter options and their display names for a filterKey
       await this.changeFilterType(keyDisplayValue);
       const validFilterValues = this.filterManagementService.getValidFilterValues(keyDisplayValue, filterKey, this.filterText, this.filterTagOptions, this.filterTagLabels);
       const filterObj = this.filterManagementService.createFilterObj(keyDisplayValue, filterKey, validFilterValues);
 
       filters.push(filterObj);
     }
     filters = [...filters];
     return filters;
   }

   getUpdatedUrl() {
     let updatedQueryParams = {};
       this.filterText = this.utils.arrayToObject(
       this.filters,
       "filterkey",
       "value"
     ); // <-- TO update the queryparam which is passed in the filter of the api
     this.filterText = this.utils.makeFilterObj(this.filterText);
 
     /**
      * To change the url
      * with the deleted filter value along with the other existing paramter(ex-->tv:true)
      */
 
     updatedQueryParams = {
       filter: this.filterText.filter,
     }
 
 
     /**
      * Finally after changing URL Link
      * api is again called with the updated filter
      */
     this.filterText = this.utils.processFilterObj(this.filterText);
 
     this.router.navigate([], {
       relativeTo: this.activatedRoute,
       queryParams: updatedQueryParams,
       queryParamsHandling: 'merge',
   });
   }
 
   getFilterPayloadForDataAPI(){
     const filterToBePassed = {...this.filterText};
     Object.keys(filterToBePassed).forEach(filterKey => {
       filterToBePassed[filterKey] = filterToBePassed[filterKey].split(",");
     })
 
     return filterToBePassed;
   }
 
   getPreservedState(){
     const stateUpdated =  history.state.dataUpdated;
     const state = this.tableStateService.getState(this.saveStateKey) || {};
     if(stateUpdated){
       state.data = [];
     }
     this.headerColName = state.headerColName || 'Severity';
     this.direction = state.direction || 'desc';
     this.bucketNumber = state.bucketNumber || 0;
     this.totalRows = state.totalRows || 0;
     this.searchTxt = state?.searchTxt || '';
 
     this.whiteListColumns = state?.whiteListColumns || Object.keys(this.columnWidths);
     this.tableScrollTop = state?.tableScrollTop;
     this.selectedRowId = state?.selectedRowId;

     this.applyPreservedFilters(state);
   }

   applyPreservedFilters (state) {
     this.isStatePreserved = false;

     const updateInfo = this.filterManagementService.applyPreservedFilters(state);
     if (updateInfo.shouldUpdateFilters) {
       this.filters = state.filters || [];
       this.filterText = updateInfo.filterText;
     }
     if (updateInfo.shouldUpdateData) {
       this.isStatePreserved = true;
       this.tableData = state.data || [];
       this.tableDataLoaded = true;
     }
   }
 
   handleHeaderColNameSelection(event){
     this.headerColName = event.headerColName;
     this.direction = event.direction;
     this.storeState();
     this.updateComponent();
   }
 
   handleWhitelistColumnsChange(event){
     this.whiteListColumns = event;
     this.storeState();
   }
 
   deleteFilters(event?) {
     let shouldUpdateComponent = false;
     [this.filters, shouldUpdateComponent] = this.filterManagementService.deleteFilters(event, this.filters);      
     if(shouldUpdateComponent){
       this.getUpdatedUrl();
       this.updateComponent();
     }
     this.storeState();
   }
 
   handleFilterSelection(){    
     this.storeState();
   }
 
   handleFilterTypeSelection(){    
     this.storeState();
   }
 
   nextPage(e) {
     try {
         this.pageNumber++;
         this.showLoader = true;
         this.storeState();
         this.getPolicyDetails(true);
     } catch (error) {
       this.errorMessage = this.errorHandling.handleJavascriptError(error);
       this.logger.log("error", error);
     }
   }
 
   getPolicyDetails(isNextPageCalled?) {
     const url = environment.policyDetails.url;
     const method = environment.policyDetails.method;
 
     const sortFilters = {
       fieldName: this.fieldName,
       fieldType: this.fieldType,
       order: this.selectedOrder,
       sortOrder: this.sortOrder
     }
 
     const payload = {
       sortFilter: sortFilters,
       from: this.pageNumber * this.paginatorSize,
       filter:this.getFilterPayloadForDataAPI(),
       size: this.paginatorSize,
     };
 
     this.errorMessage = '';
 
     try{
       this.dataSubscription = this.adminService.executeHttpAction(url, method, payload, {}).subscribe(
       (reponse) => {        
         this.showLoader = false;
         if (reponse[0].data.response) {
           this.allPolicies = reponse[0].data.response;
           this.errorValue = 1;
           this.searchCriteria = undefined;
           this.tableDataLoaded = true;
           let updatedResponse = this.utils.massageTableData(reponse[0].data.response, this.columnNamesMap);
           let processedData = this.processData(updatedResponse);
           if(isNextPageCalled){
             this.onScrollDataLoader.next(processedData)
           }else{
             this.tableData = processedData;
             if(this.tableData?.length==0){
               this.errorMessage = "noDataAvailable";
             }
           }
           this.totalRows = reponse[0].data.total;
           this.dataLoaded = true;
         }
       },
       (error) => {
         this.showGenericMessage = true;
         this.errorValue = -1;
         this.outerArr = [];
         this.dataLoaded = true;
         this.seekdata = true;
         this.errorMessage = "apiResponseError";
         this.showLoader = false;
       }
     );
     }catch(e){
       this.logger.log("error: ", e);
     }
   }
 
   async changeFilterTags(event) {
     let filterValues = event.filterValue;
     if(!filterValues){
       return;
     }
     this.currentFilterType =  find(this.filterTypeOptions, {
       optionName: event.filterKeyDisplayValue,
     });
 
     this.filters = this.filterManagementService.changeFilterTags(this.filters, this.filterTagOptions, this.currentFilterType, event);
     this.getUpdatedUrl();
     this.storeState();
     this.updateComponent();
   }
 
   /*
    * This function gets the urlparameter and queryObj
    *based on that different apis are being hit with different queryparams
    */
   routerParam() {
     try {
       // this.filterText saves the queryparam
       let currentQueryParams =
         this.routerUtilityService.getQueryParametersFromSnapshot(
           this.router.routerState.snapshot.root
         );
       if (currentQueryParams) {
         this.FullQueryParams = currentQueryParams;
 
         this.queryParamsWithoutFilter = JSON.parse(
           JSON.stringify(this.FullQueryParams)
         );
         delete this.queryParamsWithoutFilter["filter"];
 
         /**
          * The below code is added to get URLparameter and queryparameter
          * when the page loads ,only then this function runs and hits the api with the
          * filterText obj processed through processFilterObj function
          */
         this.filterText = this.utils.processFilterObj(this.FullQueryParams);
 
         this.urlID = this.FullQueryParams.TypeAsset;
         //check for mandatory filters.
         if (this.FullQueryParams.mandatory) {
           this.mandatory = this.FullQueryParams.mandatory;
         }
       }
     } catch (error) {
       this.errorMessage = this.errorHandling.handleJavascriptError(error);
       this.logger.log("error", error);
     }
   }
 
   /**
    * This function get calls the keyword service before initializing
    * the filter array ,so that filter keynames are changed
    */
 
   updateComponent() {
     this.updateSortFieldName();
     if(this.isStatePreserved){
       this.tableDataLoaded = true;
       this.clearState();
     }else{
       this.tableDataLoaded = false;
       this.pageNumber = 0;
       this.tableData = [];
       this.getPolicyDetails();
     }
   }
 
   updateSortFieldName(){
     const sortColName = this.headerColName.toLowerCase();
     this.selectedOrder = this.direction;
     this.sortOrder = null;
     this.fieldType = 'string';
     if(sortColName==='policy'){
       this.fieldName = 'policyDisplayName';
     }else if(sortColName==='asset type'){
       this.fieldName = 'targetDisplayName';
     }else if(sortColName=='severity' || sortColName=='category'){
       this.fieldName = sortColName;
       const mapOfOrderMaps = {'severity': SeverityOrderMap, 'category': CategoryOrderMap}
       this.sortOrder = Object.keys(mapOfOrderMaps[sortColName]).sort((a,b) => SeverityOrderMap[a]-SeverityOrderMap[b]);
     } else{
       try{
         let apiColName =  find(this.filterTypeOptions, {
           optionName: this.headerColName,
         })["optionValue"];
         this.fieldName = apiColName;
       }catch(e){
         this.headerColName = '';
         this.logger.log("error", e);
       }
     }
   }
 
   storeState(data?){    
     const state = {
       totalRows: this.totalRows,
       data: data,
       headerColName: this.headerColName,
       direction: this.direction,
       whiteListColumns: this.whiteListColumns,
       bucketNumber: this.bucketNumber,
       searchTxt: this.searchTxt,
       tableScrollTop: this.tableScrollTop,
       filters: this.filters,
       selectedRowId: this.selectedRowId
     }
     this.tableStateService.setState(this.saveStateKey, state);
   }
 
   clearState(){
     // this.tableStateService.clearState(this.saveStateKey);
     this.isStatePreserved = false;
   }
 
   navigateBack() {
     try {
       this.workflowService.goBackToLastOpenedPageAndUpdateLevel(
         this.router.routerState.snapshot.root
       );
     } catch (error) {
       this.logger.log("error", error);
     }
   }
 
   processData(data) {
     try {
       return this.utils.processTableData(data, this.tableImageDataMap, (row, col, cellObj) => {
         if(col.toLowerCase()=="policy"){
           const autoFixAvailable = row.autoFixAvailable;
           const autoFixEnabled = row.autoFixEnabled;
           let imgSrc = 'noImg';
           let imageTitleText = "";
           
           if (autoFixAvailable=="true") {
             imgSrc = autoFixEnabled=="true" ? 'autofix' : 'no-autofix';
             imageTitleText = autoFixEnabled=="true" ? 'Autofix Enabled': 'Autofix Available'
           }
             cellObj = {
             ...cellObj,
             imgSrc: imgSrc,
             isLink: true,
             imageTitleText: imageTitleText
           };
         }
         else if(col.toLowerCase() == "status"){
           let chipBackgroundColor,chipTextColor;
           if(row["Status"].toLowerCase() === "enabled"){
             chipBackgroundColor = "#E6F5EC";
             chipTextColor = "#00923f";
           }else{
             chipBackgroundColor = "#F2F3F5";
             chipTextColor = "#73777D";
           }
           cellObj = {
             ...cellObj,
             chipList: [row[col]],
             text: row[col],
             isChip: true,
             chipBackgroundColor: chipBackgroundColor,
             chipTextColor: chipTextColor
           };
         }
         return cellObj;
       });
       
     } catch (error) {
       this.errorMessage = this.errorHandling.handleJavascriptError(error);
       this.logger.log("error", error);
     }
   }
 
   enableDisableRuleOrJob(policyId,action) {
       const url = environment.enableDisableRuleOrJob.url;
       const method = environment.enableDisableRuleOrJob.method;
       const params = {};
       params['policyId'] = policyId;
       params['action'] = action;
 
       this.adminService.executeHttpAction(url, method, {}, params).subscribe(response => {
         const snackbarText = 'Policy "' +  policyId + '" ' + action + 'd successfully';
         this.openSnackBar(snackbarText, "check-circle");
         this.getPolicyDetails();
       },
         error => {
             this.logger.log("error", error);
         });
   }
 
   openDialog(event): void {
     const action = event.action;
     const element = event.rowSelected;
     const policyId = event.rowSelected["Policy ID"].text;
     const autofix = element.autoFixEnabled.text == "true"? "disable" : "enable";
     this.selectedRowTitle =  element["Policy"].text ;
     this.action = action;
     const dialogRef = this.dialog.open(DialogBoxComponent, {
       width: '500px',
       data: {
         title: null,
          yesButtonLabel: action,
           noButtonLabel: "Cancel" ,
           template: this.enableOrDisablePolicyRef
         },
       });
 
     dialogRef.afterClosed().subscribe(result => {
       if(result=="yes"){
         if(action == "Enable Policy" || action == "Disable Policy")
         {
           this.enableDisableRuleOrJob(policyId,action.split(" ")[0].toLowerCase());
         }
         else
         this.enabelOrDisableAutofix(policyId,autofix);
       }
     });
   }
 
   openSnackBar(message, iconSrc) {
     this.notificationObservableService.postMessage(message, 3*1000, "success", iconSrc);
     }
 
   goToCreatePolicy() {
     try {
       this.workflowService.addRouterSnapshotToLevel(
         this.router.routerState.snapshot.root, 0, this.pageTitle
       );
       this.router.navigate(["create-edit-policy"], {
         relativeTo: this.activatedRoute,
         queryParamsHandling: "merge",
         queryParams: {},
       });
     } catch (error) {
       this.errorMessage = this.errorHandling.handleJavascriptError(error);
       this.logger.log("error", error);
     }
   }
 
 
   handleRowClick(event){
     this.goToDetails(event,true);
   }
 
   goToDetails(event,isRowclicked=false) {
     const action = event?.action?.toLowerCase();
     if(action == "enable policy"
     || action == "disable policy"
     || action == "enable autofix"
     || action == "disable autofix"
      ){
       this.openDialog(event);
       return;
     }
     
     const row = event.rowSelected;
     const data = event.data;
     const policyId = row["Policy ID"].valueText;
     this.tableScrollTop = event.tableScrollTop;
     this.selectedRowId = event.selectedRowId;
     this.storeState(data);
     try {
       this.workflowService.addRouterSnapshotToLevel(
         this.router.routerState.snapshot.root, 0, this.pageTitle
       );
       if(isRowclicked){
         this.router.navigate(["create-edit-policy"], {
           relativeTo: this.activatedRoute,
           queryParamsHandling: "merge",
           queryParams: {
             policyId: policyId,
           },
         });
       }
     if (action && action === "edit") {
         this.router.navigate(["create-edit-policy"], {
           relativeTo: this.activatedRoute,
           queryParamsHandling: "merge",
           queryParams: {
             policyId: policyId
           },
         });
     } else if (action && (action === "run policy")){
          this.invokePolicy(policyId);
      }
     } catch (error) {
         this.errorMessage = this.errorHandling.handleJavascriptError(error);
         this.logger.log("error", error);
       }
   }
 
   enabelOrDisableAutofix(policyId:string,autoFix:string){
     try{
       const url = environment.enableDisableAutofix.url;
       const method = environment.enableDisableAutofix.method;
       const queryParams = {
         policyId: policyId,
         autofixStatus: autoFix == "enable"
       }
       this.adminService.executeHttpAction(url,method,{},queryParams).subscribe(response=>{
         if(response && response[0].message=="success"){
           const snackbarText = 'Autofix for policy "' +  policyId + '" ' + autoFix + 'd successfully';
           this.openSnackBar(snackbarText,"check-circle");
           this.getPolicyDetails();
         }
       })
     }catch(error){
        this.errorHandling.handleJavascriptError(error);
     }
   }
 
   invokePolicy(policyId:string){
     var url = environment.invokePolicy.url;
     var method = environment.invokePolicy.method;
     this.adminService.executeHttpAction(url, method, [{}], {policyId:policyId}).subscribe(response => {
      const invocationId = response[0].data;
       if(invocationId)
       this.openSnackBar("Invocation Id " + invocationId + " invoked successfully!!","check-circle");
     },
     error => {
       this.errorHandling.handleJavascriptError(error);
     })
   }
 
   callNewSearch(search) {
     this.searchTxt = search;
     this.isStatePreserved = false;
     this.updateComponent();
   }
 
   ngOnDestroy() {
     try {
       if(this.dataSubscription){
         this.dataSubscription.unsubscribe();
       }
       if (this.routeSubscription) {
         this.routeSubscription.unsubscribe();
       }
       if (this.previousUrlSubscription) {
         this.previousUrlSubscription.unsubscribe();
       }
     } catch (error) {
       this.logger.log("error", "--- Error while unsubscribing ---");
     }
   }
 }