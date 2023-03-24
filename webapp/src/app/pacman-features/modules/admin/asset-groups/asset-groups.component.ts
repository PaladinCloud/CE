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

import { Component, OnInit, OnDestroy, ChangeDetectorRef } from "@angular/core";
import { environment } from "./../../../../../environments/environment";
import { ActivatedRoute, Router } from "@angular/router";
import { Subject, Subscription } from "rxjs";
import * as _ from "lodash";
import { UtilsService } from "../../../../shared/services/utils.service";
import { LoggerService } from "../../../../shared/services/logger.service";
import { ErrorHandlingService } from "../../../../shared/services/error-handling.service";
import { RefactorFieldsService } from "./../../../../shared/services/refactor-fields.service";
import { WorkflowService } from "../../../../core/services/workflow.service";
import { RouterUtilityService } from "../../../../shared/services/router-utility.service";
import { AdminService } from "../../../services/all-admin.service";
import { DATA_MAPPING } from "src/app/shared/constants/data-mapping";

@Component({
  selector: "app-asset-groups",
  templateUrl: "./asset-groups.component.html",
  styleUrls: ["./asset-groups.component.css"],
  providers: [LoggerService, ErrorHandlingService, AdminService],
})
export class AssetGroupsComponent implements OnInit {
  pageTitle: String = "Asset Groups";
  allAssetGroups: any = [];

  breadcrumbArray: any = ["Admin"];
  breadcrumbLinks: any = ["policies"];
  breadcrumbPresent: any;

  tableTitle = "All Asset Groups";
  onScrollDataLoader: Subject<any> = new Subject<any>();
  headerColName: string;
  direction: string;
  bucketNumber: number = 0;
  totalRows: number = 0;
  tableDataLoaded: boolean = false;
  tableData: any = [];
  displayedColumns: string[] = [];
  whiteListColumns: any = [];
  tableScrollTop: any;
  columnWidths = {'Name': 1, "Type": 1, "Created By": 1, "Number of assets": 1.5, "Actions": 0.5};
  columnNamesMap = {"groupName": "Name", "type": "Type", "createdBy": "Created By", "assetCount": "Number of assets"};
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
    private adminService: AdminService
  ) {
    this.getFilters();
  }

  ngOnInit() {
    this.displayedColumns = Object.keys(this.columnWidths);
    this.whiteListColumns = this.displayedColumns;
    this.urlToRedirect = this.router.routerState.snapshot.url;
    this.breadcrumbPresent = "Asset Groups";
    this.backButtonRequired = this.workflowService.checkIfFlowExistsCurrently(
      this.pageLevel
    );
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

    this.tableData = [];
    this.tableDataLoaded = false;
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
            const processData = this.processData(updatedResponse);
            if(isNextPageCalled){
              this.onScrollDataLoader.next(processData)
            }else{
              this.tableData = processData;
            }
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
    this.searchTxt = "";
    this.bucketNumber = 0;
    this.tableDataLoaded = false;
    this.errorValue = 0;
    this.showGenericMessage = false;
    this.getAssetGroupsDetails();
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
          if(col.toLowerCase()=="name"){
            cellObj = {
              ...cellObj,
              isLink: true
            };
          } else if (col.toLowerCase() == "actions") {
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

  onSelectAction(event) {
    const action = event.action.toLowerCase();
    const rowSelected = event.rowSelected;
    if (action === "delete") {
      try {
        this.workflowService.addRouterSnapshotToLevel(
          this.router.routerState.snapshot.root, 0, this.pageTitle
        );
        this.router.navigate(["delete-asset-groups"], {
          relativeTo: this.activatedRoute,
          queryParamsHandling: "merge",
          queryParams: {
            groupId: rowSelected["Group Id"].valueText,
            groupName: rowSelected["Name"].valueText,
          },
        });
      } catch (error) {
        this.tableErrorMessage = this.errorHandling.handleJavascriptError(error);
        this.logger.log("error", error);
      }
    } else if (action === "edit") {
      try {
        this.workflowService.addRouterSnapshotToLevel(
          this.router.routerState.snapshot.root, 0, this.pageTitle
        );
        this.router.navigate(["create-asset-groups"], {
          relativeTo: this.activatedRoute,
          queryParamsHandling: "merge",
          queryParams: {
            groupId: rowSelected["Group Id"].valueText,
            groupName: rowSelected["Name"].valueText,
          },
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
  deleteFilters(event?) {
    try {
      if (!event) {
        this.filters = [];
      } else {
        if (event.clearAll) {
          this.filters = [];
        } else {
          this.filters.splice(event.index, 1);
        }
        this.getUpdatedUrl();
        this.updateComponent();
      }
    } catch (error) { }
    /* TODO: Aditya: Why are we not calling any updateCompliance function in observable to update the filters */
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
        
        let keyValue = _.find(this.filterTypeOptions, {
          optionValue: formattedFilters[i].name,
        })["optionName"];
        
        this.changeFilterType(keyValue).then(() => {
            let filterValue = _.find(this.filterTagOptions[keyValue], {
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
      this.currentFilterType = _.find(this.filterTypeOptions, {
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
    this.currentFilterType =  _.find(this.filterTypeOptions, {
        optionName: event.filterKeyDisplayValue,
      });          
    try {
      if (this.currentFilterType) {
        const filterTag = _.find(this.filterTagOptions[event.filterKeyDisplayValue], { name: value });        
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
