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

import { Component, OnInit, OnDestroy, ChangeDetectorRef, ViewChild, TemplateRef, AfterViewInit } from "@angular/core";
import { environment } from "./../../../../../environments/environment";
import { ActivatedRoute, Router } from "@angular/router";
import { Subject, Subscription } from "rxjs";
import { UtilsService } from "../../../../shared/services/utils.service";
import { LoggerService } from "../../../../shared/services/logger.service";
import { ErrorHandlingService } from "../../../../shared/services/error-handling.service";
import { RefactorFieldsService } from "./../../../../shared/services/refactor-fields.service";
import { WorkflowService } from "../../../../core/services/workflow.service";
import { RouterUtilityService } from "../../../../shared/services/router-utility.service";
import { AdminService } from "../../../services/all-admin.service";
import { DATA_MAPPING } from "src/app/shared/constants/data-mapping";
import { NotificationObservableService } from "src/app/shared/services/notification-observable.service";
import { DialogBoxComponent } from "src/app/shared/components/molecules/dialog-box/dialog-box.component";
import { MatDialog } from "@angular/material/dialog";
import { AssetTilesService } from "src/app/core/services/asset-tiles.service";
import { TableStateService } from "src/app/core/services/table-state.service";
import find from "lodash/find";
import { TourService } from "src/app/core/services/tour.service";

@Component({
  selector: "app-asset-groups",
  templateUrl: "./asset-groups.component.html",
  styleUrls: ["./asset-groups.component.css"],
  providers: [LoggerService, ErrorHandlingService, AdminService],
})
export class AssetGroupsComponent implements OnInit, AfterViewInit {
  pageTitle: String = "Asset Groups";
  allAssetGroups: any = [];

  breadcrumbArray: any = ["Admin"];
  breadcrumbLinks: any = ["policies"];
  breadcrumbPresent: any;

  tableTitle = "All Asset Groups";
  onScrollDataLoader: Subject<any> = new Subject<any>();
  headerColName: string = 'Name';
  direction: string = 'asc';
  bucketNumber: number = 0;
  totalRows: number = 0;
  tableDataLoaded: boolean = false;
  tableData: any = [];
  displayedColumns: string[] = [];
  whiteListColumns: any = [];
  tableScrollTop: any;
  columnWidths = {'Name': 1.2, "Type": 1, "Created By": 2, "Number of assets": 1, "Actions": 0.5};
  columnNamesMap = {"displayName": "Name", "type": "Type", "createdBy": "Created By", "assetCount": "Number of assets"};
  columnsSortFunctionMap = {
    Severity: (a, b, isAsc) => {
      let severeness = {"low":1, "medium":2, "high":3, "critical":4}
      return (severeness[a["Severity"]] < severeness[b["Severity"]] ? -1 : 1) * (isAsc ? 1 : -1);
    },
  };

  tableImageDataMap = {
      security:{
          image: "category-security",
          imageOnly: true
      },
      governance:{
          image: "category-operations",
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
      costOptimization:{
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
  }

  card = {
      id: 3,
      header: "Total Compliance Trend",
    }

  selectedAssetGroup = "";
  isStatePreserved: boolean;
  selectedDomain: any;
  paginatorSize: number = 25;
  tableErrorMessage: string;

  totalPages: number;
  pageNumber: number = 0;

  searchTxt = "";
  filterTypeOptions: any = [];
  filterTagOptions: any = {};
  currentFilterType;
  filterTypeLabels = [];
  filterTagLabels = {};
  filters: any = [];
  filterText: any;

  searchCriteria: any;
  errorValue: number = 0;
  showGenericMessage: boolean = false;
  dataTableDesc: String = "";
  urlID: String = "";
  public labels: any;
  FullQueryParams: any;
  queryParamsWithoutFilter: any;
  private previousUrl: any = "";
  urlToRedirect: any = "";
  private pageLevel = 0;
  public backButtonRequired;
  mandatory: any;
  private routeSubscription: Subscription;
  private getKeywords: Subscription;
  private previousUrlSubscription: Subscription;
  private downloadSubscription: Subscription;
  assetGroupList: any[];
  @ViewChild("actionRef") actionRef: TemplateRef<any>;
  errorMessage: string;
  selectedRowIndex: any;


  constructor(
    private activatedRoute: ActivatedRoute,
    private router: Router,
    private utils: UtilsService,
    private logger: LoggerService,
    private errorHandling: ErrorHandlingService,
    private ref: ChangeDetectorRef,
    private refactorFieldsService: RefactorFieldsService,
    private workflowService: WorkflowService,
    private routerUtilityService: RouterUtilityService,
    private adminService: AdminService,
    private notificationObservableService: NotificationObservableService,
    public dialog: MatDialog,
    private assetTilesService: AssetTilesService,
    private tableStateService: TableStateService,
    private tourService: TourService
  ) {
    // this.getFilters();
    this.getPreservedState();
    this.updateComponent();
  }
  ngAfterViewInit(): void {
    this.tourService.setComponentReady();
  }

  ngOnInit() {
    this.urlToRedirect = this.router.routerState.snapshot.url;
    this.breadcrumbPresent = "Asset Groups";
    this.backButtonRequired = this.workflowService.checkIfFlowExistsCurrently(
      this.pageLevel
    );
  }

  getPreservedState(){
    const state = this.tableStateService.getState("admin-asset-groups") ?? {};
    if(state){
      this.headerColName = state.headerColName ?? 'Name';
      this.direction = state.direction ?? 'asc';
      this.bucketNumber = state.bucketNumber ?? 0;
      this.totalRows = state.totalRows ?? 0;
      this.searchTxt = state?.searchTxt ?? '';
      
      this.tableDataLoaded = true;

      this.tableData = state?.data ?? [];
      this.whiteListColumns = state?.whiteListColumns ?? Object.keys(this.columnWidths);
      this.tableScrollTop = state?.tableScrollTop;
      this.selectedRowIndex = state?.selectedRowIndex;

      this.filters = state?.filters ?? [];

      if(this.tableData && this.tableData.length>0){        
        this.isStatePreserved = true;
      }else{
        this.isStatePreserved = false;
      }
    }
  }

  clearState(){
    this.isStatePreserved = false;
  }

  storeState(data?){
    const state = {
      totalRows: this.totalRows,
      data: data,
      headerColName: this.headerColName,
      direction: this.direction,
      whiteListColumns: this.whiteListColumns,
      searchTxt: this.searchTxt,
      tableScrollTop: this.tableScrollTop,
      filters: this.filters,
      selectedRowIndex: this.selectedRowIndex
    }
    this.tableStateService.setState("admin-asset-groups", state);
  }

  handleHeaderColNameSelection(event){
    this.headerColName = event.headerColName;
    this.direction = event.direction;
    this.storeState();
  }

  handleWhitelistColumnsChange(event){
    this.whiteListColumns = event;
    this.storeState();
  }

  deleteFilters(event?) {
    try {
      if (!event) {
        this.filters = [];
      } else if (event.clearAll) {
        this.filters = [];
      }
      this.storeState();
    } catch (error) { }
  }

  handleFilterSelection(){    
    this.storeState();
  }

  handleFilterTypeSelection(){    
    this.storeState();
  }

  getFiltersData(data){
    this.filterTypeLabels = [];
    this.filterTagLabels = {};
    this.whiteListColumns.forEach(column => {
      if(column.toLowerCase()=='number of assets' || column.toLowerCase()=='actions'){
        return;
      }
      let filterTags = [];
      this.filterTypeLabels.push(column);
      const set = new Set();
      data.forEach(row => {
        if(row[column])
        set.add(row[column].valueText.toLowerCase());
      });
      filterTags = Array.from(set);
      filterTags.sort();
      
      this.filterTagLabels[column] = filterTags;
    });
    this.filterTypeLabels.sort();
  }

  confirmAction(action:string,selectedRow:any, currentAG:any){
    const groupId = selectedRow["Group Id"].valueText;
    this.selectedAssetGroup = selectedRow["Name"].valueText;
    const dialogRef = this.dialog.open(DialogBoxComponent,
    {
      width: '600px',
      data: {
        title: null,
        yesButtonLabel: action,
        noButtonLabel: "Cancel",
        template: this.actionRef
      }
    });
    dialogRef.afterClosed().subscribe(result => {
      try {
        if (result == "yes") {
              this.deleteAssetGroup(groupId,currentAG);
        }
      } catch (error) {
        this.errorMessage = this.errorHandling.handleJavascriptError(error);
        this.logger.log('error', error);
      }
    });
  }


  getAssetGroupsDetails(isNextPageCalled?) {
    var url = environment.assetGroups.url;
    var method = environment.assetGroups.method;

    const filterToBePassed = this.filterText;

    const queryParams = {
      page: this.pageNumber,
      searchTerm: this.searchTxt,
      size: this.paginatorSize,
    }
    
    const payload = {
      ...filterToBePassed,
    };

    if(!isNextPageCalled){
      this.tableDataLoaded = false;
    }
    this.tableErrorMessage = "";

    if (this.searchTxt !== undefined && this.searchTxt !== "") {
      queryParams["searchTerm"] = this.searchTxt;
    }

    this.adminService.executeHttpAction(url, method, payload, queryParams).subscribe(
      (reponse) => {
        const data = reponse[0].data;
        if (data.content !== undefined) {
          this.tableDataLoaded = true;
          if (data.content.length == 0) {
            this.tableErrorMessage = "noDataAvialable";
            this.totalRows = 0;
          }

          if (data.content.length > 0) {
            this.totalPages = data.totalPages;
            this.pageNumber = data.number;
            this.totalRows = data.totalElements;
            let updatedResponse = this.massageData(data.content);
            this.assetGroupList = updatedResponse;
            const processData = this.processData(updatedResponse);
            if(isNextPageCalled){
              this.onScrollDataLoader.next(processData)
            }else{
              this.tableData = processData;
            }
            this.getFiltersData(this.tableData);
          }
        }
      },
      (error) => {
        this.tableDataLoaded = true;
        this.tableErrorMessage = this.errorHandling.handleJavascriptError(error);
      }
    );
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
      this.tableErrorMessage = this.errorHandling.handleJavascriptError(error);
      this.logger.log("error", error);
    }
  }

  /**
  * This function get calls the keyword service before initializing
  * the filter array ,so that filter keynames are changed
  */

  updateComponent() {
    if(this.isStatePreserved){
      this.clearState();
    }else{
      this.searchTxt = "";
      this.bucketNumber = 0;
      this.tableDataLoaded = false;
      this.errorValue = 0;
      this.showGenericMessage = false;
      this.getAssetGroupsDetails();
    }
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

  massageData(data){
    const refactoredService = this.refactorFieldsService;
    const columnNamesMap = this.columnNamesMap;
    const newData = [];
    data.map(function (row) {
      const KeysTobeChanged = Object.keys(row);      
      let newObj = {};
      KeysTobeChanged.forEach((element) => {
        let elementnew;
        if(columnNamesMap[element]) {
          elementnew = columnNamesMap[element];
          newObj = Object.assign(newObj, { [elementnew]: row[element] });
        }
        else {
        elementnew =
          refactoredService.getDisplayNameForAKey(
            element.toLocaleLowerCase()
          ) || element;
          newObj = Object.assign(newObj, { [elementnew]: row[element] });
        }
        // change data value
        newObj[elementnew] = DATA_MAPPING[typeof newObj[elementnew]=="string"?newObj[elementnew].toLowerCase():newObj[elementnew]]?DATA_MAPPING[newObj[elementnew].toLowerCase()]: newObj[elementnew];
      });
      newObj["Actions"] = "";
      newData.push(newObj);
    });
    return newData;
  }

  processData(data) {
    try {
      var innerArr = {};
      var totalVariablesObj = {};
      var cellObj = {};
      let processedData = [];
      var getData = data;      
      const keynames = Object.keys(getData[0]);

      let cellData;
      for (var row = 0; row < getData.length; row++) {
        innerArr = {};
        keynames.forEach(col => {
          cellData = getData[row][col];
          cellObj = {
            text: this.tableImageDataMap[typeof cellData == "string"?cellData.toLowerCase(): cellData]?.imageOnly?"":cellData, // text to be shown in table cell
            titleText: cellData, // text to show on hover
            valueText: cellData,
            hasPostImage: false,
            imgSrc: this.tableImageDataMap[typeof cellData == "string"?cellData.toLowerCase(): cellData]?.image,  // if imageSrc is not empty and text is also not empty then this image comes before text otherwise if imageSrc is not empty and text is empty then only this image is rendered,
            postImgSrc: "",
            isChip: "",
            isMenuBtn: false,
            properties: "",
            isLink: false
          }
         if(col.toLowerCase()=="number of assets"){
            cellObj = {
              ...cellObj,
              isNumber: true
            };
          }
          else if (col.toLowerCase() == "actions") {
            let dropDownItems: Array<String> = ["Edit", "Delete"];
            cellObj = {
              ...cellObj,
              isMenuBtn: true,
              menuItems: dropDownItems,
            };
          }
          innerArr[col] = cellObj;
          totalVariablesObj[col] = "";
        });
        processedData.push(innerArr);
      }
      if (processedData.length > getData.length) {
        var halfLength = processedData.length / 2;
        processedData = processedData.splice(halfLength);
      }
      return processedData;
    } catch (error) {
      this.tableErrorMessage = this.errorHandling.handleJavascriptError(error);
      this.logger.log("error", error);
    }
  }

  goToCreateAssetGroup() {
    try {
      this.workflowService.addRouterSnapshotToLevel(
        this.router.routerState.snapshot.root, 0, this.pageTitle
      );
      this.router.navigate(["create-asset-groups"], {
        relativeTo: this.activatedRoute,
        queryParamsHandling: "merge",
        queryParams: {},
      });
    } catch (error) {
      this.tableErrorMessage = this.errorHandling.handleJavascriptError(error);
      this.logger.log("error", error);
    }
  }
  deleteAssetGroup(groupId:string, currentAG:any) {
    let url = environment.deleteAssetGroups.url; 
    let method = environment.deleteAssetGroups.method; 
    this.adminService.executeHttpAction(url, method, {groupId: groupId}, {}).subscribe(response => {
      if(response){
        const data = response[0]?.data;
        this.updateComponent();
        this.notificationObservableService.postMessage(data,3000,"","check-circle");
        if(response[0].message==="success"){
          this.assetTilesService.getAssetGroupList().subscribe(response=>{
            console.log(" Updated Asset Group List ");
          });
          let criteriasBeforeUpdate = {};
          currentAG['criteriaDetails']?.forEach(crit => {
              if(crit.criteriaName in criteriasBeforeUpdate){
                criteriasBeforeUpdate[crit.criteriaName][crit.attributeName]=crit.attributeValue;
              }
              else{
                criteriasBeforeUpdate[crit.criteriaName]={};
                criteriasBeforeUpdate[crit.criteriaName][crit.attributeName]=crit.attributeValue;
              }
            });
            let agDetails = {groupName : currentAG['Group Name'], description : currentAG['Description'], type : currentAG['Type'], configuration : Object.values(criteriasBeforeUpdate)};
            delete agDetails['criteriaDetails'];
        }
      }
    },
    error => {
      this.notificationObservableService.postMessage("Error in deleting asset group",3000,"error","Error");
      this.logger.log("Error in Js",error);
    })
  }

  onSelectAction(event) {
    const action = event.action;
    const rowSelected = event.rowSelected;
    const groupId = rowSelected["Group Id"].valueText;
    let currentAG: any;
    this.assetGroupList.forEach(assetGroup=>{
                    if(assetGroup["Group Id"] == groupId){
                      currentAG = assetGroup;
                    }
          })
    if (action === "Delete") {
      this.confirmAction(action,rowSelected,currentAG);
    } else if (action === "Edit") {
      try {
        this.workflowService.addRouterSnapshotToLevel(
          this.router.routerState.snapshot.root, 0, this.pageTitle
        );
        this.router.navigate(["create-asset-groups"],{
          relativeTo: this.activatedRoute,
          queryParams:{
            groupId : groupId
          },
          queryParamsHandling: 'merge',
        });
      } catch (error) {
        this.tableErrorMessage = this.errorHandling.handleJavascriptError(error);
        this.logger.log("error", error);
      }
    }
  }

  callNewSearch(search) {
    this.searchTxt = search;
    this.pageNumber = 0;
    this.bucketNumber = 0;
    this.isStatePreserved = false;
    this.updateComponent();
  }

  nextPg(e) {
    try {
      this.tableScrollTop = e;
        this.bucketNumber++;
        this.pageNumber++;
        this.getAssetGroupsDetails(true);
    } catch (error) {
      // this.errorMessage = this.errorHandling.handleJavascriptError(error);
      this.logger.log("error", error);
    }
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
  /*
  * this functin passes query params to filter component to show filter
  */
  getFilterArray() {
    try {
      // let labelsKey = Object.keys(this.labels);
      const filterObjKeys = Object.keys(this.filterText);
      const dataArray = [];
      for (let i = 0; i < filterObjKeys.length; i++) {
        let obj = {};
        obj = {
          name: filterObjKeys[i],
        };
        dataArray.push(obj);
      }
      const formattedFilters = dataArray;
      for (let i = 0; i < formattedFilters.length; i++) {
        
        let keyValue = find(this.filterTypeOptions, {
          optionValue: formattedFilters[i].name,
        })["optionName"];
        
        this.changeFilterType(keyValue).then(() => {
            let filterValue = find(this.filterTagOptions[keyValue], {
              id: this.filterText[filterObjKeys[i]],
            })["name"];
          const eachObj = {
            keyDisplayValue: keyValue,
            filterValue: filterValue,
            key: keyValue, // <-- displayKey-- Resource Type
            value: this.filterText[filterObjKeys[i]], // <<-- value to be shown in the filter UI-- S2
            filterkey: filterObjKeys[i].trim(), // <<-- filter key that to be passed -- "resourceType "
            compareKey: filterObjKeys[i].toLowerCase().trim(), // <<-- key to compare whether a key is already present -- "resourcetype"
          };
          this.filters.push(eachObj);
          this.filters = [...this.filters];
        })
      }
    } catch (error) {
      this.logger.log("error", error);
    }
  }

  /**
  * This function get calls the keyword service before initializing
  * the filter array ,so that filter keynames are changed
  */

  getFilters() {
    try {
          this.filterTypeLabels.push("Type");
          this.filterTypeOptions.push({
            optionName: 'Type',
            optionValue: 'type'
          })
          this.routerParam();
          // this.deleteFilters();
          this.getFilterArray();
          this.updateComponent();
    } catch (error) {
      this.logger.log("error", error);
    }
  }

  changeFilterType(value) {
    return new Promise((resolve) => {
    try {
      this.currentFilterType = find(this.filterTypeOptions, {
        optionName: value,
      });
      if(!this.filterTagOptions[value] || !this.filterTagLabels[value]){
        if(value.toLowerCase()=="type"){
        this.filterTagLabels[value] = ["admin", "admin"];
        this.filterTagOptions[value] = [
          {
            id: "admin",
            name: "admin"
          }
        ]
        resolve(this.filterTagLabels[value]);
        return;
      }

      }
    } catch (error) {
      this.tableErrorMessage = this.errorHandling.handleJavascriptError(error);
      this.logger.log("error", error);
    }
    }); 
  }

  changeFilterTags(event) {        
    let value = event.filterValue;
    this.currentFilterType =  find(this.filterTypeOptions, {
        optionName: event.filterKeyDisplayValue,
      });          
    try {
      if (this.currentFilterType) {
        const filterTag = find(this.filterTagOptions[event.filterKeyDisplayValue], { name: value });        
        this.utils.addOrReplaceElement(
          this.filters,
          {
            keyDisplayValue: event.filterKeyDisplayValue,
            filterValue: value,
            key: this.currentFilterType.optionName,
            value: filterTag["id"],
            filterkey: this.currentFilterType.optionValue.trim(),
            compareKey: this.currentFilterType.optionValue.toLowerCase().trim(),
          },
          (el) => {
            return (
              el.compareKey ===
              this.currentFilterType.optionValue.toLowerCase().trim()
            );
          }
        );
      }
      this.getUpdatedUrl();
      this.utils.clickClearDropdown();
      this.updateComponent();
    } catch (error) {
      this.tableErrorMessage = this.errorHandling.handleJavascriptError(error);
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
      this.logger.log("error", "--- Error while unsubscribing ---");
    }
  }
}
