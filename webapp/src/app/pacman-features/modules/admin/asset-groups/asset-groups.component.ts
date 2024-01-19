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

import { Component, OnInit, OnDestroy, ViewChild, TemplateRef, AfterViewInit } from "@angular/core";
import { environment } from "./../../../../../environments/environment";
import { ActivatedRoute, Router } from "@angular/router";
import { Subject, Subscription } from "rxjs";
import { UtilsService } from "../../../../shared/services/utils.service";
import { LoggerService } from "../../../../shared/services/logger.service";
import { ErrorHandlingService } from "../../../../shared/services/error-handling.service";
import { WorkflowService } from "../../../../core/services/workflow.service";
import { RouterUtilityService } from "../../../../shared/services/router-utility.service";
import { AdminService } from "../../../services/all-admin.service";
import { MatDialog } from "@angular/material/dialog";
import { TableStateService } from "src/app/core/services/table-state.service";
import find from "lodash/find";
import { TourService } from "src/app/core/services/tour.service";
import { ComponentKeys } from "src/app/shared/constants/component-keys";
import { FilterManagementService } from "src/app/shared/services/filter-management.service";
import { map } from "lodash";
import { IColumnNamesMap, IColumnWidthsMap } from "src/app/shared/table/interfaces/table-props.interface";

@Component({
  selector: "app-asset-groups",
  templateUrl: "./asset-groups.component.html",
  styleUrls: ["./asset-groups.component.css"],
  providers: [LoggerService, ErrorHandlingService, AdminService],
})
export class AssetGroupsComponent implements OnInit, AfterViewInit, OnDestroy {
  pageTitle: String = "Asset Groups";
  saveStateKey: String = ComponentKeys.AdminAssetGroupList;
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
  columnWidths: IColumnWidthsMap = {'Name': 1.2, "Type": 1, "Number of assets": 1, "Created By": 2, "Created Date": 1, "Updated Date": 1};
  columnNamesMap: IColumnNamesMap = {
    'createdDate': 'Created Date',
    'updatedDate': 'Updated Date'
  };
  columnsAndFiltersToExcludeFromCasing = ["Name", "Type"];
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
  urlToRedirect: any = "";
  private pageLevel = 0;
  public backButtonRequired;
  mandatory: any;
  private routeSubscription: Subscription;
  private previousUrlSubscription: Subscription;
  assetGroupList: any[];
  @ViewChild("actionRef") actionRef: TemplateRef<any>;
  errorMessage: string;
  selectedRowIndex: any;
  filtersDataSubscription: Subscription;
  fieldName: any;
  fieldType: any;
  selectedOrder: any;
  sortOrder: any;
  dateCategoryList: string[] = [];


  constructor(
    private activatedRoute: ActivatedRoute,
    private router: Router,
    private utils: UtilsService,
    private logger: LoggerService,
    private errorHandling: ErrorHandlingService,
    private filterManagementService: FilterManagementService,
    private workflowService: WorkflowService,
    private routerUtilityService: RouterUtilityService,
    private adminService: AdminService,
    public dialog: MatDialog,
    private tableStateService: TableStateService,
    private tourService: TourService
  ) {}
  ngAfterViewInit(): void {
    this.tourService.setComponentReady();
  }

  ngOnInit() {
    this.urlToRedirect = this.router.routerState.snapshot.url;
    this.breadcrumbPresent = "Asset Groups";
    this.backButtonRequired = this.workflowService.checkIfFlowExistsCurrently(
      this.pageLevel
    );
    this.getPreservedState();
    this.getFilters();

    window.onbeforeunload = () => this.storeState();
  }

  getFilterPayloadForDataAPI(){
    const filterToBePassed = {...this.filterText};
    Object.keys(filterToBePassed).forEach(filterKey => {
      filterToBePassed[filterKey] = filterToBePassed[filterKey].split(",");

      if(this.columnNamesMap[filterKey]?.toLowerCase()=="number of assets"){
        filterToBePassed[filterKey] = filterToBePassed[filterKey].map(filterVal => {
          const [min, max] = filterVal.split("-");
          return {min: parseFloat(min), max: parseFloat(max)};
        })
      }
    })

    return filterToBePassed;
  }

  getPreservedState(){
    const state = this.tableStateService.getState(this.saveStateKey) ?? {};
    if(state){
      this.headerColName = state.headerColName ?? 'Name';
      this.direction = state.direction ?? 'asc';
      this.bucketNumber = state.bucketNumber ?? 0;
      this.totalRows = state.totalRows ?? 0;
      this.searchTxt = state?.searchTxt ?? '';
      
      this.whiteListColumns = state?.whiteListColumns ?? Object.keys(this.columnWidths);
      this.tableScrollTop = state?.tableScrollTop;
      this.selectedRowIndex = state?.selectedRowIndex;

      this.applyPreservedFilters(state);
    }
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

  clearState(){
    this.isStatePreserved = false;
  }

  storeState(){
    const state = {
      totalRows: this.totalRows,
      data: this.tableData,
      headerColName: this.headerColName,
      direction: this.direction,
      whiteListColumns: this.whiteListColumns,
      searchTxt: this.searchTxt,
      tableScrollTop: this.tableScrollTop,
      filters: this.filters,
      selectedRowIndex: this.selectedRowIndex
    }
    this.tableStateService.setState(this.saveStateKey, state);
  }

  handleHeaderColNameSelection(event){
    this.headerColName = event.headerColName;
    this.direction = event.direction;
    this.updateComponent();
  }

  handleWhitelistColumnsChange(event){
    this.whiteListColumns = event;
  }

  deleteFilters(event?) {
    let shouldUpdateComponent = false;
    [this.filters, shouldUpdateComponent] = this.filterManagementService.deleteFilters(event, this.filters);      
    if(shouldUpdateComponent){
      this.getUpdatedUrl();
      this.updateComponent();
    }
  }

  getAssetGroupsDetails(isNextPageCalled?) {
    const url = environment.assetGroups.url;
    const method = environment.assetGroups.method;

    const sortFilters = {
      fieldName: this.fieldName,
      fieldType: this.fieldType,
      order: this.selectedOrder,
      sortOrder: this.sortOrder
    }

    const payload = {
      page: this.pageNumber,
      size: this.paginatorSize,
      sortFilter: sortFilters,
      filter:this.getFilterPayloadForDataAPI(),
    };

    const queryParams = {}

    if(!isNextPageCalled){
      this.tableDataLoaded = false;
    }
    this.tableErrorMessage = "";

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
            let updatedResponse = this.utils.massageTableData(data.content, this.columnNamesMap);
            this.assetGroupList = updatedResponse;
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
    this.updateSortFieldName();
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

  updateSortFieldName(){
    try{
      this.selectedOrder = this.direction;
      this.fieldType = "string";
      if(this.headerColName?.toLowerCase()=='created date'){
        this.fieldName = 'createdDate'
      }
      else if(this.headerColName?.toLowerCase()=='updated date'){
        this.fieldName = 'modifiedDate';
      }
      else{
        let apiColName:any = Object.keys(this.columnNamesMap).find(col => this.columnNamesMap[col]==this.headerColName);
        if(!apiColName){
          apiColName =  find(this.filterTypeOptions, {
            optionName: this.headerColName,
          })["optionValue"];
        }
        this.fieldName = apiColName;
      }
    }catch(e){
      this.logger.log('sortError', e);
      this.headerColName = '';
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

  processData(data) {
    try {
      return this.utils.processTableData(data, this.tableImageDataMap, (row, col, cellObj) => {
        if(col.toLowerCase()=="name"){
          cellObj = {
            ...cellObj,
            isLink: true
          };
        }else if(col.toLowerCase()=="number of assets"){
          cellObj = {
            ...cellObj,
            isNumber: true
          };
        }else if(col.toLowerCase()=="updated date" || col.toLowerCase()=="created date"){
          cellObj = {
            ...cellObj,
            isDate: true
          };
        }
        return cellObj;
      });
      
    } catch (error) {
      this.errorMessage = this.errorHandling.handleJavascriptError(error);
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

  handleRowClick(event) {
    const rowSelected = event.rowSelected;
    const groupId = rowSelected["Group Id"].valueText;
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

  /**
  * This function get calls the keyword service before initializing
  * the filter array ,so that filter keynames are changed
  */

  getFilters() {
    try {
      this.filtersDataSubscription = this.filterManagementService
      .getFilters(19)
        .subscribe(async(filterOptions) => {
          this.filterTypeOptions = filterOptions;
          this.filterTypeLabels = map(filterOptions, "optionName");
          
          this.filterTypeLabels.sort();
          [this.columnNamesMap, this.columnWidths] = this.utils.getColumnNamesMapAndColumnWidthsMap(this.filterTypeLabels, this.filterTypeOptions, this.columnWidths, this.columnNamesMap, []);
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
    } catch (error) {
      this.errorMessage = this.errorHandling.handleJavascriptError(error);
      this.logger.log("error", error);
    }
  }

  getUpdateFilterTagsCallback(){
    const labelsToExcludeSort = ['number of assets'];
    const updateFilterTags = (filterTagsData, value) => {      
      if(value.toLowerCase() === "number of assets"){
        const numOfIntervals = 5;
        const min = filterTagsData.optionRange.min;
        const max = filterTagsData.optionRange.max;
        const intervals = this.utils.generateIntervals(min, max, numOfIntervals);
        filterTagsData = [];
        intervals.forEach(interval => {
          const lb = Math.round(interval.lowerBound);
          const up = Math.round(interval.upperBound);
          filterTagsData.push({ id: `${lb}-${up}`, name: `${lb}-${up}` });
        });
      }
      return filterTagsData;
    }
    return [updateFilterTags, labelsToExcludeSort];
  }

  async changeFilterTags(event) {
    let filterValues = event.filterValue;
    if(!filterValues){
      return;
    }
    console.log(event);
    
    this.currentFilterType =  find(this.filterTypeOptions, {
      optionName: event.filterKeyDisplayValue,
    });

    this.filters = this.filterManagementService.changeFilterTags(this.filters, this.filterTagOptions, this.currentFilterType, event);
    this.getUpdatedUrl();
    this.updateComponent();
  }

  ngOnDestroy () {
    this.storeState();
    try {
      if(this.filtersDataSubscription){
        this.filtersDataSubscription.unsubscribe();
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
