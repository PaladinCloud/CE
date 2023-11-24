import { Component, OnInit, OnDestroy, AfterViewInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';

import { Subject, Subscription } from 'rxjs';

import { environment } from './../../../../../environments/environment';
import { WorkflowService } from '../../../../core/services/workflow.service';
import { LoggerService } from '../../../../shared/services/logger.service';
import { ErrorHandlingService } from '../../../../shared/services/error-handling.service';
import { UtilsService } from '../../../../shared/services/utils.service';
import { RouterUtilityService } from '../../../../shared/services/router-utility.service';
import { CommonResponseService } from '../../../../shared/services/common-response.service';
import { NotificationObservableService } from 'src/app/shared/services/notification-observable.service';
import find from 'lodash/find';
import { TableStateService } from 'src/app/core/services/table-state.service';
import { IssueFilterService } from 'src/app/pacman-features/services/issue-filter.service';
import map from 'lodash/map';
import { TourService } from 'src/app/core/services/tour.service';
import { ComponentKeys } from 'src/app/shared/constants/component-keys';
import { FilterManagementService } from 'src/app/shared/services/filter-management.service';
import { IColumnNamesMap, IColumnWidthsMap } from 'src/app/shared/table/interfaces/table-props.interface';

@Component({
  selector: 'app-account-management',
  templateUrl: './account-management.component.html',
  styleUrls: ['./account-management.component.css'],
  providers: [IssueFilterService]
})
export class AccountManagementComponent implements OnInit, AfterViewInit, OnDestroy {

  pageTitle: String = 'Plugins';
  saveStateKey: String = ComponentKeys.AccountManagementList;
  breadcrumbDetails = {
    breadcrumbArray: ['Admin'],
    breadcrumbLinks: ['policies'],
    breadcrumbPresent: 'Plugins'
  };
  backButtonRequired: boolean;
  pageLevel = 0;
  errorMessage = '';
  searchPassed = '';
  agAndDomain = {};
  totalRows = 0;
  errorValue = 0;
  currentId;
  currentBucket: any = [];
  outerArr = [];
  bucketNumber = 0;
  showConfBox = false;
  firstPaginator = 1;
  allColumns = [];
  lastPaginator: number;
  currentPointer = 0;

  tableData = [];
  headerColName;
  direction;
  columnNamesMap: IColumnNamesMap = {source: 'Source'};
  columnWidths: IColumnWidthsMap = {"Account Name": 1.5, "Account ID": 1.5, "Assets": 0.5, "Violations": 0.5, "Status": 0.5, "Created By": 1};
  whiteListColumns;
  tableScrollTop = 0;
  centeredColumns = {
    Assets: true,
    Violations: true,
    Source: true,
};
  dataTableDesc: String = "";
  tableImageDataMap = {
    aws:{
        image: "aws-color",
        imageOnly: false
    },
    azure:{
      image: "azure-color",
      imageOnly: false
   }, gcp:{
    image: "gcp-color",
    imageOnly: false
   },
    aqua:{
      image: "aqua-color",
      imageOnly: false
    },
    qualys: {
      image: "qualys-color",
      imageOnly: false
    },
    "red hat": {
      image: "redhat-color",
      imageOnly: false
    },
    tenable: {
      image: "tenable-color",
      imageOnly: false
    }
    }

  filterTypeOptions: any = [];
  filterTagOptions: any = {};
  currentFilterType;
  filterTypeLabels = [];
  filterTagLabels = {};
  filters: any = [];
  filterText : any;
  actionsArr = ['Edit', 'Delete'];
  paginatorSize = 100;
  searchTxt = '';
  tableDataLoaded = false;
  onScrollDataLoader: Subject<any> = new Subject<any>();

  tableSubscription: Subscription;
  assetGroupSubscription: Subscription;
  domainSubscription: Subscription;
  filterSubscription: Subscription;

  isFilterRquiredOnPage = false;
  appliedFilters = {
    queryParamsWithoutFilter: {}, /* Stores the query parameter ibject without filter */
    pageLevelAppliedFilters: {} /* Stores the query parameter ibject without filter */
  };
  filterArray = []; /* Stores the page applied filter array */
  routeSubscription: Subscription;
  baseAccountId: string = "";
  filterErrorMessage: string;
  FullQueryParams: any;
  queryParamsWithoutFilter: any;
  isStatePreserved;
  selectedRowIndex;
  fieldName: any;
  sortOrder: any;
  columnsAndFiltersToExcludeFromCasing = ['Account Name'];

  constructor(private router: Router,
    private activatedRoute: ActivatedRoute,
    private workflowService: WorkflowService,
    private commonResponseService: CommonResponseService,
    private logger: LoggerService,
    private errorHandling: ErrorHandlingService,
    private utils: UtilsService,
    private routerUtilityService: RouterUtilityService,
    private filterManagementService: FilterManagementService,
    private tableStateService: TableStateService,
    private tourService: TourService,
    private issueFilterService: IssueFilterService,
    private notificationObservableService: NotificationObservableService) { }
  ngAfterViewInit(): void {
    this.tourService.setComponentReady();
  }

  ngOnInit() {
    this.getPreservedState();
    this.getFilters();
    this.backButtonRequired = this.workflowService.checkIfFlowExistsCurrently(this.pageLevel);
  }

  createAccount(){
    try {
      this.workflowService.addRouterSnapshotToLevel(
        this.router.routerState.snapshot.root, 0, this.pageTitle
      );
      this.router.navigate(["add-account"], {
        relativeTo: this.activatedRoute,
        queryParamsHandling: "merge",
        queryParams: {},
      });
    } catch (error) {
      this.errorMessage = this.errorHandling.handleJavascriptError(error);
      this.logger.log("error", error);
    }
  }

  reset() {
    /* Reset the page */
    this.filterArray = [];
    this.outerArr = [];
    this.searchTxt = '';
    this.currentBucket = [];
    this.bucketNumber = 0;
    this.firstPaginator = 1;
    this.currentPointer = 0;
    this.allColumns = [];
    this.errorValue = 0;
  }

  getBaseAccount(){
    const url = environment.getBaseAccount.url;
    const method = environment.getBaseAccount.method;

    this.commonResponseService.getData(url,method,{},{})
    .subscribe(response=>{
      try{
        this.baseAccountId = response.accountId;
      }catch(err){
        this.logger.log("error JS",err);
      }
    })
  }

  updateComponent() {
    this.updateSortFieldName();
    if(this.isStatePreserved){
      this.tableDataLoaded = true;
      this.clearState();
    }else{
      this.tableDataLoaded = false;
      this.bucketNumber = 0;
      this.getData();
    }
  }

  getPreservedState(){
    const stateUpdated =  history.state.dataUpdated;
    const state = this.tableStateService.getState(this.saveStateKey) ?? {};
    if(stateUpdated){
      state.data = [];
      state.bucketNumber = 0;
      this.storeState();
    }
    this.headerColName = state.headerColName ?? 'Account Name';
    this.direction = state.direction ?? 'asc';
    this.bucketNumber = state.bucketNumber ?? 0;
    this.totalRows = state.totalRows ?? 0;
    this.searchTxt = state?.searchTxt ?? '';
    
    this.tableData = state?.data || [];
    this.whiteListColumns = state?.whiteListColumns ?? Object.keys(this.columnWidths);
    this.tableScrollTop = state?.tableScrollTop;
    this.selectedRowIndex = state?.selectedRowIndex;
    this.filters = state?.filters || [];

    if(this.tableData && this.tableData.length>0){   
      this.tableDataLoaded = true;
      this.isStatePreserved = true;
    }else{
      this.isStatePreserved = false;
    }

    const navDirection = this.workflowService.getNavigationDirection();

    if(navDirection<=0){
      this.filters = state.filters || [];
      if (state.data && state.data.length > 0) {
        this.isStatePreserved = true;
        this.tableData = state.data;
      }
      Promise.resolve().then(() => this.getUpdatedUrl());
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
        selectedRowIndex: this.selectedRowIndex
        // filterText: this.filterText
      }
    this.tableStateService.setState(this.saveStateKey, state);
  }

  clearState(){
    this.isStatePreserved = false;
  }

  updateSortFieldName(){
    try{
      this.sortOrder = this.direction;
      let apiColName:any = Object.keys(this.columnNamesMap).find(col => col==this.headerColName);
      if(!apiColName){
        apiColName =  find(this.filterTypeOptions, {
          optionName: this.headerColName,
        })["optionValue"];
      }
      this.fieldName = apiColName;
    }catch(e){
      this.logger.log('sortError', e);
      this.headerColName = '';
    }
  }

   /*
   * This function gets the urlparameter and queryObj
   *based on that different apis are being hit with different queryparams
   */
   routerParam() {
    try {
      const currentQueryParams =
        this.routerUtilityService.getQueryParametersFromSnapshot(
          this.router.routerState.snapshot.root
        );
      if (currentQueryParams) {
        this.FullQueryParams = currentQueryParams;
        this.queryParamsWithoutFilter = JSON.parse(
          JSON.stringify(this.FullQueryParams)
        );
        delete this.queryParamsWithoutFilter["filter"];
        this.filterText = this.utils.processFilterObj(this.FullQueryParams);
      }
    } catch (error) {
      this.errorMessage = this.errorHandling.handleJavascriptError(error);
      this.logger.log("error", error);
    }
  }

  navigateBack() {
    try {
      this.workflowService.goBackToLastOpenedPageAndUpdateLevel(this.router.routerState.snapshot.root);
    } catch (error) {
      this.logger.log('error', error);
    }

  }

  getData(isNextPageCalled?) {
    try {
      if (this.tableSubscription) {
        this.tableSubscription.unsubscribe();
      }
      this.errorMessage = "";
      let queryParams;
      const sortFilters = {
        sortElement: this.fieldName,
        sortOrder: this.sortOrder
      }

      const filtersToBePassed = this.getFilterPayloadForDataAPI();

      const payload = {
        filter: filtersToBePassed,
        sortFilter: sortFilters,
        page: this.bucketNumber,
        searchtext: this.searchTxt,
        size: this.paginatorSize,
      };
      this.errorValue = 0;
      const url = environment.getAccounts.url;
      const method = environment.getAccounts.method;
      this.tableSubscription = this.commonResponseService
        .getData(url, method, payload, queryParams)
        .subscribe(
          responseData => {
            this.tableDataLoaded = true;
            try {
              let data = responseData.data.response;
              this.totalRows = responseData.data.total;
              if (data.length === 0) {
                this.errorMessage = 'noDataAvailable';
                this.totalRows = 0;
              }
              if (data.length > 0) {
                data = this.utils.massageTableData(data, this.columnNamesMap);
                const processData = this.processData(data);
                if(isNextPageCalled){
                  this.onScrollDataLoader.next(processData)
                }else{
                  this.tableData = processData;
                }
              }
            } catch (e) {
              this.logger.log("jsError", e);
              this.errorMessage = 'jsError';
            }
          },
          error => {
            this.tableDataLoaded = true;
            this.errorMessage = 'apiResponseError';
            this.logger.log("apiResponseError", "Error fetching data")
          }
        );
    } catch (error) {
      this.tableDataLoaded = true;
      this.logger.log('error', error);
    }
  }

  nextPg(e) {
    try {
      this.tableScrollTop = e;
        this.bucketNumber++;
        this.storeState();
        this.getData(true);
    } catch (error) {
      this.errorMessage = this.errorHandling.handleJavascriptError(error);
      this.logger.log("error", error);
    }
  }

  handleDropdown(event) {
    if (event.type === 'Delete') {
      this.showConfBox = true;
      this.currentId = event.data['id'].text;
    } else {
      // redirect to details page
      this.workflowService.addRouterSnapshotToLevel(this.router.routerState.snapshot.root);
      this.router.navigate(['../account-management-details', event.data['id'].text], {
        relativeTo: this.activatedRoute,
        queryParamsHandling: 'merge',
        queryParams: {
        }
      });
    }
  }

  processData(data) {
    try {
      return this.utils.processTableData(data, this.tableImageDataMap, (row, col, cellObj) => {
        if(col.toLowerCase()=="account name"){
          cellObj = {
            ...cellObj,
            imgSrc: this.tableImageDataMap[row["Source"]?.toLowerCase()]?this.tableImageDataMap[row["Source"].toLowerCase()].image:"noImg",
            isLink: true
          };
        }
        else if (col.toLowerCase() == "actions") {
          let dropdownItems: Array<String> = ["Delete"];
          if(row["Account ID"]==this.baseAccountId)
               dropdownItems = [];
          cellObj = {
            ...cellObj,
            isMenuBtn: true,
            menuItems: dropdownItems,
          };
        } 
        else if(col.toLowerCase() == "status"){
          let chipBackgroundColor,chipTextColor;
          if(row["Status"].toLowerCase() === "configured"){
            chipBackgroundColor = "#E6F5EC";
            chipTextColor = "#00923f";
          }else{
            chipBackgroundColor = "#F2F3F5";
            chipTextColor = "#73777D";
          }
          cellObj = {
            ...cellObj,
            chipList: row[col].toLowerCase() === "configured"?["Online"]:["Offline"],
            text: row[col].toLowerCase(),
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

  goToCreate() {
    this.workflowService.addRouterSnapshotToLevel(this.router.routerState.snapshot.root);
    this.router.navigate(['../account-management-create'], {
      relativeTo: this.activatedRoute,
      queryParamsHandling: 'merge',
      queryParams: {
      }
    });
  }

  searchCalled(search) {
    this.searchTxt = search;
    if (this.searchTxt === '') {
      this.searchPassed = this.searchTxt;
    }
  }

  callNewSearch(e:any) {
    this.searchPassed = this.searchTxt;
  }

  handleHeaderColNameSelection(event: any) {
    this.headerColName = event.headerColName;
    this.direction = event.direction;

    this.bucketNumber = 0;

    this.storeState();
    this.updateComponent();
  }

  handleWhitelistColumnsChange(event){
    this.whiteListColumns = event;
    this.storeState();
  }

  deleteAccount(accountId:string,provider:string){
    const url = environment.deleteAccount.url;
    const method = environment.deleteAccount.method;
    const queryParams = {
      accountId : accountId,
      provider: provider
    }
    let nofificationMessage = "";
    this.commonResponseService.getData(url,method,{},queryParams).subscribe(responseData=>{
      try{
        const response = responseData.data;
        const status =response.validationStatus;
        if(status.toLowerCase() == "success"){
          nofificationMessage = "Account "+ accountId +" has been deleted successfully";
          this.notificationObservableService.postMessage(nofificationMessage,3000,"","check-circle");
          this.updateComponent();
        }
      }
      catch(error) {
      this.logger.log('error', 'JS Error - ' + error);
      }
    })
  }

  goToDetails(event:any){
    const action = event.action;
    const rowSelected = event.rowSelected;
    this.tableScrollTop = event.tableScrollTop;
    this.selectedRowIndex = event.selectedRowIndex;
    this.storeState();
    const accountId = rowSelected["Account ID"].valueText;
    const provider = rowSelected["Source"].valueText;
    if(action.toLowerCase() == "delete"){
      this.deleteAccount(accountId,provider);
     }
  }

  handleRowClick(event){
      const rowSelected = event.rowSelected;
      const data = event.data;
      this.tableScrollTop = event.tableScrollTop;
      this.selectedRowIndex = event.selectedRowIndex;
      this.storeState(data);
      this.workflowService.addRouterSnapshotToLevel(
        this.router.routerState.snapshot.root, 0, this.pageTitle
      );
      let accountId = rowSelected["Account ID"].valueText;
      this.router.navigate(["account-management-details"], {
        relativeTo: this.activatedRoute,
        queryParamsHandling: "merge",
        queryParams: {
          accountId: accountId,
        },
      });
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
    let shouldUpdateComponent = false;
    [this.filters, shouldUpdateComponent] = this.filterManagementService.deleteFilters(event, this.filters);      
    if(shouldUpdateComponent){
      this.getUpdatedUrl();
      this.updateComponent();
    }
    this.storeState();
  }
  /*
   * this functin passes query params to filter component to show filter
   */
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

  /**
   * This function get calls the keyword service before initializing
   * the filter array ,so that filter keynames are changed
   */

  getFilters() {
    this.filterErrorMessage = '';
    try {
      this.filterSubscription = this.issueFilterService
        .getFilters(
          { filterId: 12 },
          environment.issueFilter.url,
          environment.issueFilter.method
        )
        .subscribe(async(response) => {
          this.filterTypeLabels = map(response[0].response, "optionName");
          this.filterTypeOptions = response[0].response;
          this.filterTypeLabels.sort();
          [this.columnNamesMap, this.columnWidths] = this.utils.getColumnNamesMapAndColumnWidthsMap(this.filterTypeLabels, this.filterTypeOptions, this.columnWidths, this.columnNamesMap, []);
          if(this.filterTypeLabels.length==0){
            this.filterErrorMessage = 'noDataAvailable';
          }
          this.routerParam();
          await this.getFilterArray();
          await Promise.resolve().then(() => this.getUpdatedUrl());
          this.updateComponent();
        });
    } catch (error) {
      this.filterErrorMessage = 'jsError';
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
    const labelsToExcludeSort = ['assets', 'violations'];
    const updateFilterTags = (filterTagsData, value) => {      
      if(value.toLowerCase() === "assets" || value.toLowerCase() === "violations"){
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

  getFilterPayloadForDataAPI(){
    const filterToBePassed = {...this.filterText};
    Object.keys(filterToBePassed).forEach(filterKey => {
      filterToBePassed[filterKey] = filterToBePassed[filterKey].split(",");

      if(this.columnNamesMap[filterKey]?.toLowerCase()=="assets" || this.columnNamesMap[filterKey]?.toLowerCase()=="violations"){
        filterToBePassed[filterKey] = filterToBePassed[filterKey].map(filterVal => {
          const [min, max] = filterVal.split("-");
          return {min: parseFloat(min), max: parseFloat(max)};
        })
      }
    })

    return filterToBePassed;
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

  ngOnDestroy() {
    try {

    } catch (error) {
      this.logger.log('error', 'JS Error - ' + error);
    }
  }

}

