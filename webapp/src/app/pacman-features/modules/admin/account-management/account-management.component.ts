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
import { RefactorFieldsService } from '../../../../shared/services/refactor-fields.service';
import { NotificationObservableService } from 'src/app/shared/services/notification-observable.service';
import { DATA_MAPPING } from 'src/app/shared/constants/data-mapping';
import find from 'lodash/find';
import { TableStateService } from 'src/app/core/services/table-state.service';
import { IssueFilterService } from 'src/app/pacman-features/services/issue-filter.service';
import map from 'lodash/map';
import { TourService } from 'src/app/core/services/tour.service';

@Component({
  selector: 'app-account-management',
  templateUrl: './account-management.component.html',
  styleUrls: ['./account-management.component.css'],
  providers: [IssueFilterService]
})
export class AccountManagementComponent implements OnInit, AfterViewInit, OnDestroy {

  pageTitle: String = 'Plugins';
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
  columnNamesMap = {"accountName": "Account Name","accountId":"Account ID", "assets": "Assets",  "violations": "Violations", "accountStatus": "Status","platform":"Platform","createdBy": "Created By"};
  columnWidths = {"Account Name": 1.5, "Account ID": 1.5, "Assets": 0.5, "Violations": 0.5, "Status": 0.5, "Created By": 1};
  whiteListColumns;
  tableScrollTop = 0;
  centeredColumns = {
    Assets: true,
    Violations: true,
    Platform: true,
};
  dataTableDesc: String = "";
  tableImageDataMap = {
    aws:{
        image: "aws-color",
        imageOnly: true
    },
    azure:{
      image: "azure-color",
      imageOnly: true
   }, gcp:{
    image: "gcp-color",
    imageOnly: true
   },
    aqua:{
      image: "aqua-color",
      imageOnly: true
    },
    qualys: {
      image: "qualys-color",
      imageOnly: true
    },
    redhat: {
      image: "redhat-color",
      imageOnly: true
    },
    tenable: {
      image: "tenable-color",
      imageOnly: true
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

  constructor(private router: Router,
    private activatedRoute: ActivatedRoute,
    private workflowService: WorkflowService,
    private commonResponseService: CommonResponseService,
    private logger: LoggerService,
    private errorHandling: ErrorHandlingService,
    private utils: UtilsService,
    private routerUtilityService: RouterUtilityService,
    private refactorFieldsService: RefactorFieldsService,
    private tableStateService: TableStateService,
    private issueFilterService: IssueFilterService,
    private tourService: TourService,
    private notificationObservableService: NotificationObservableService) {
      this.getPreservedState();
      this.getFilters();
  }
  ngAfterViewInit(): void {
    this.tourService.setComponentReady();
  }

  ngOnInit() {
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
    const state = this.tableStateService.getState("account-management") ?? {};
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

    const isTempFilter = this.activatedRoute.snapshot.queryParamMap.get("tempFilters");
      if(!isTempFilter){
        this.filters = state.filters || [];
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
    this.tableStateService.setState("account-management", state);
  }

  clearState(){
    this.isStatePreserved = false;
  }

  updateSortFieldName(){
    this.sortOrder = this.direction;
    let apiColName:any = Object.keys(this.columnNamesMap).find(col => col==this.headerColName);
    if(!apiColName){
      apiColName =  find(this.filterTypeOptions, {
        optionName: this.headerColName,
      })["optionValue"];
    }
    this.fieldName = apiColName;
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

      let filtersToBePassed = {};      
      Object.keys(this.filterText).map(key => {
        filtersToBePassed[key] = [this.filterText[key]]
      })

      const payload = {
        filter: filtersToBePassed,
        // sortFilter: sortFilters, // uncomment for server side sort
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
                data = this.massageData(data);
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
      // newObj["Actions"] = "";
      newData.push(newObj);
    });
    return newData;
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
      var innerArr = {};
      var totalVariablesObj = {};
      var cellObj = {};
      let processedData = [];
      var getData = data;      
      
      let cellData;
      for (var row = 0; row < getData.length; row++) {
        const keynames = Object.keys(getData[row]);
        innerArr = {};
        keynames.forEach(col => {
          cellData = getData[row][col];
          cellObj = {
            text: this.tableImageDataMap[typeof cellData == "string"?cellData.toLowerCase(): cellData]?.imageOnly?"":cellData, // text to be shown in table cell
            titleText: cellData, // text to show on hover
            valueText: cellData?.toLowerCase(),
            hasPostImage: false,
            imgSrc: this.tableImageDataMap[typeof cellData == "string"?cellData.toLowerCase(): cellData]?.image,  // if imageSrc is not empty and text is also not empty then this image comes before text otherwise if imageSrc is not empty and text is empty then only this image is rendered,
            postImgSrc: "",
            isChip: "",
            isMenuBtn: false,
            properties: "",
            isLink: false
            // chipVariant: "", // this value exists if isChip is true,
            // menuItems: [], // add this if isMenuBtn
          }
          if(col.toLowerCase()=="account name"){
            cellObj = {
              ...cellObj,
              imgSrc: this.tableImageDataMap[getData[row]["Platform"]?.toLowerCase()]?this.tableImageDataMap[getData[row]["Platform"].toLowerCase()].image:"noImg",
              isLink: true
            };
          }
          else if (col.toLowerCase() == "actions") {
            let dropdownItems: Array<String> = ["Delete"];
            if(getData[row]["Account ID"]==this.baseAccountId)
                 dropdownItems = [];
            cellObj = {
              ...cellObj,
              isMenuBtn: true,
              menuItems: dropdownItems,
            };
          } 
          else if(col.toLowerCase() == "status"){
            let chipBackgroundColor,chipTextColor;
            if(getData[row]["Status"].toLowerCase() === "configured"){
              chipBackgroundColor = "#E6F5EC";
              chipTextColor = "#00923f";
            }else{
              chipBackgroundColor = "#F2F3F5";
              chipTextColor = "#73777D";
            }
            cellObj = {
              ...cellObj,
              chipList: getData[row][col].toLowerCase() === "configured"?["Online"]:["Offline"],
              text: getData[row][col].toLowerCase(),
              isChip: true,
              chipBackgroundColor: chipBackgroundColor,
              chipTextColor: chipTextColor
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
    // this.updateComponent(); // uncomment for server side sort
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
    const provider = rowSelected["Platform"].valueText;
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
    try {
      if (!event) {
        this.filters = [];
        this.storeState();
      }else if(event.removeOnlyFilterValue){
        this.getUpdatedUrl();
        this.updateComponent();
        this.storeState();
      }
      else if(event.index && !this.filters[event.index].filterValue){
        this.filters.splice(event.index, 1);
        this.storeState();
      }
      else {
        if (event.clearAll) {
          this.filters = [];
        } else {
          this.filters.splice(event.index, 1);
        }
        this.storeState();
        this.getUpdatedUrl();
        this.updateComponent();
      }
    } catch (error) { 
      this.logger.log('jsError', 'Error deleting filters')
    }
    /* TODO: Aditya: Why are we not calling any updateCompliance function in observable to update the filters */
  }
  /*
   * this functin passes query params to filter component to show filter
   */
  getFilterArray() {
    return new Promise((resolve) => {
      const filterObjKeys = Object.keys(this.filterText);      
      const dataArray = [];
      for (let i = 0; i < filterObjKeys.length; i++) {
        let obj = {};
        const keyDisplayValue = find(this.filterTypeOptions, {
          optionValue: filterObjKeys[i],
        })["optionName"];
        obj = {
          keyDisplayValue,
          filterkey: filterObjKeys[i],
        };
        dataArray.push(obj);
      }      
      
      const state = this.tableStateService.getState("account-management") ?? {};
      const filters = state?.filters;
      
      if(filters){
        const dataArrayFilterKeys = dataArray.map(obj => obj.keyDisplayValue);
        filters.forEach(filter => {
          if(!dataArrayFilterKeys.includes(filter.keyDisplayValue)){
            dataArray.push({
              filterkey: filter.filterkey,
              keyDisplayValue: filter.key
            });
          }
        });
      }

      const formattedFilters = dataArray;  
      if(formattedFilters.length==0){
        resolve(true);
      }    
      for (let i = 0; i < formattedFilters.length; i++) {

        let keyDisplayValue = formattedFilters[i].keyDisplayValue;
        if(!keyDisplayValue){
          keyDisplayValue = find(this.filterTypeOptions, {
            optionValue: formattedFilters[i].filterKey,
          })["optionName"];
        }

        this.changeFilterType(keyDisplayValue).then(() => {
          let filterValueObj = find(this.filterTagOptions[keyDisplayValue], {
            id: this.filterText[formattedFilters[i].filterkey],
          });

          let filterKey = dataArray[i].filterkey;
          
          if(!this.filters.find(filter => filter.keyDisplayValue==keyDisplayValue)){
            const eachObj = {
              keyDisplayValue: keyDisplayValue,
              filterValue: filterValueObj?filterValueObj["name"]:undefined,
              key: keyDisplayValue, // <-- displayKey-- Resource Type
              value: this.filterText[filterKey], // <<-- value to be shown in the filter UI-- S2
              filterkey: filterKey?.trim(), // <<-- filter key that to be passed -- "resourceType "
              compareKey: filterKey?.toLowerCase().trim(), // <<-- key to compare whether a key is already present -- "resourcetype"
            };
            this.filters.push(eachObj);
          }
          if(i==formattedFilters.length-1){
            this.filters = [...this.filters];
            this.storeState();
            resolve(true);
          }           
        })
      }
    })
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
        .subscribe((response) => {
          this.filterTypeLabels = map(response[0].response, "optionName");
          this.filterTypeOptions = response[0].response;
          this.filterTypeLabels.sort();
          if(this.filterTypeLabels.length==0){
            this.filterErrorMessage = 'noDataAvailable';
          }
          this.routerParam();
          // this.deleteFilters();
          this.getFilterArray().then(() => {
            this.updateComponent();
          }).catch(e => {
            this.logger.log("jsError: ", e);
            this.updateComponent();
          });
        });
    } catch (error) {
      this.filterErrorMessage = 'jsError';
      this.errorMessage = this.errorHandling.handleJavascriptError(error);
      this.logger.log("error", error);
    }
  }

  changeFilterType(value) {
    return new Promise((resolve) => {
      this.filterErrorMessage = '';
    try {
      this.currentFilterType = find(this.filterTypeOptions, {
        optionName: value,
      });
      this.storeState();
      if(!this.filterTagOptions[value] || !this.filterTagLabels[value]){
        const urlObj = this.utils.getParamsFromUrlSnippet(this.currentFilterType.optionURL);
      const queryParams = {
            ...urlObj.params,
          }
        this.filterSubscription = this.issueFilterService
        .getFilters(
          queryParams,
          environment.base +
          this.utils.getParamsFromUrlSnippet(this.currentFilterType.optionURL)
            .url,
          "GET"
        )
        .subscribe((response) => {
          let filtersData;
          if(value=="Status"){
            filtersData = response[0].map(item => { return {id: item, name: item=="configured"?"online":item} });
          }else{
            filtersData = response[0].map(item => { return {id: item, name: item} });
          }
          this.filterTagOptions[value] = filtersData;
          this.filterTagLabels = {
              ...this.filterTagLabels,
              ...{
                  [value]: map(filtersData, 'name').sort((a, b) =>
                      a.localeCompare(b),
                  ),
              },
          };
          if(this.filterTagLabels[value].length==0) this.filterErrorMessage = 'noDataAvailable';
          resolve(this.filterTagOptions[value]);
          this.storeState();
        });
      }
    } catch (error) {
      this.filterErrorMessage = 'jsError';
      this.errorMessage = this.errorHandling.handleJavascriptError(error);
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
      this.storeState();
      this.getUpdatedUrl();
      this.utils.clickClearDropdown();
      this.updateComponent();
    } catch (error) {
      this.errorMessage = this.errorHandling.handleJavascriptError(error);
      this.logger.log("error", error);
    }
  }

  ngOnDestroy() {
    try {

    } catch (error) {
      this.logger.log('error', 'JS Error - ' + error);
    }
  }

}

