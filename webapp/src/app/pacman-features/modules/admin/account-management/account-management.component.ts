import { Component, OnInit, OnDestroy } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';

import { Subscription } from 'rxjs';

import { environment } from './../../../../../environments/environment';
import { WorkflowService } from '../../../../core/services/workflow.service';
import { LoggerService } from '../../../../shared/services/logger.service';
import { DataCacheService } from '../../../../core/services/data-cache.service';
import { FilterManagementService } from '../../../../shared/services/filter-management.service';
import { ErrorHandlingService } from '../../../../shared/services/error-handling.service';
import { UtilsService } from '../../../../shared/services/utils.service';
import { RouterUtilityService } from '../../../../shared/services/router-utility.service';
import { CommonResponseService } from '../../../../shared/services/common-response.service';
import { RefactorFieldsService } from '../../../../shared/services/refactor-fields.service';
import { NotificationObservableService } from 'src/app/shared/services/notification-observable.service';
import { DATA_MAPPING } from 'src/app/shared/constants/data-mapping';
import * as _ from 'lodash';

@Component({
  selector: 'app-account-management',
  templateUrl: './account-management.component.html',
  styleUrls: ['./account-management.component.css']
})
export class AccountManagementComponent implements OnInit, OnDestroy {

  pageTitle: String = 'Account Management';
  breadcrumbDetails = {
    breadcrumbArray: ['Admin'],
    breadcrumbLinks: ['policies'],
    breadcrumbPresent: 'Account Management'
  };
  backButtonRequired: boolean;
  pageLevel = 0;
  errorMessage = 'apiResponseError';
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
  columnNamesMap = {"accountName": "Account Name","accountId":"Account ID", "assets": "Assets",  "violations": "Violations", "accountStatus": "Status","platform":"Platform"};
  columnWidths = {"Account Name": 1, "Account ID": 1, "Assets": 1, "Violations": 1, "Status": 1, "Platform": 1,"Actions":0.5};
  whiteListColumns;
  tableScrollTop = 0;
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
  paginatorSize = 25;
  searchTxt = '';

  tableSubscription: Subscription;
  assetGroupSubscription: Subscription;
  domainSubscription: Subscription;

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

  constructor(private router: Router,
    private activatedRoute: ActivatedRoute,
    private workflowService: WorkflowService,
    private commonResponseService: CommonResponseService,
    private logger: LoggerService,
    private dataStore: DataCacheService,
    private filterManagementService: FilterManagementService,
    private errorHandling: ErrorHandlingService,
    private utils: UtilsService,
    private routerUtilityService: RouterUtilityService,
    private refactorFieldsService: RefactorFieldsService,
    private notificationObservableService: NotificationObservableService) {
    this.getFilters();
  }

  ngOnInit() {
    this.backButtonRequired = this.workflowService.checkIfFlowExistsCurrently(this.pageLevel);
    this.whiteListColumns = Object.keys(this.columnWidths);
    this.reset();
    this.init();
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

  init() {
    /* Initialize */
    this.getBaseAccount();
    this.routerParam();
    this.updateComponent();
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
    /* Updates the whole component */
    this.reset();
    this.getData();

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

  getData() {
    try {
      if (this.tableSubscription) {
        this.tableSubscription.unsubscribe();
      }
      let queryParams;
      if(this.filters.length>0){
        queryParams = {
            page: 0,                                                                      
            size: 100,
            filterName: this.filters[0].key.toLowerCase(),
            filterValue: this.filters[0].value
          };
      }
      else{
       queryParams = {
          page: 0,                                                                      
          size: 100,
        };
      }
 
      const payload = {};
      this.errorValue = 0;
      const url = environment.getAccounts.url;
      const method = environment.getAccounts.method;
      this.tableSubscription = this.commonResponseService
        .getData(url, method, payload, queryParams)
        .subscribe(
          responseData => {
            try {
              let data = responseData.response;
              if (data.length === 0) {
                this.errorMessage = 'noDataAvailable';
                this.totalRows = 0;
              }
              if (data.length > 0) {
                data = this.massageData(data);
                this.tableData = this.processData(data);
                this.getFiltersData(this.tableData);
              }
            } catch (e) {
              this.logger.log("jsError", e);
              this.errorMessage = 'jsError';
            }
          },
          error => {
            this.errorMessage = 'apiResponseError';
          }
        );
    } catch (error) {
      this.logger.log('error', error);
    }
  }

  getFiltersData(data){
    const filterCol = "Platform";
    if(!this.filterTagOptions[filterCol] || !this.filterTagLabels[filterCol]){
      const set = new Set();
      let filterTags = [], filterTagOptions = {};
      filterTagOptions[filterCol] = [];
      data.forEach(row => {
        set.add(row[filterCol].valueText);
      })
      filterTags = Array.from(set);
      filterTags.sort();
      filterTags.forEach(tag => {
        filterTagOptions[filterCol].push({
          id: tag,
          name: tag
        });
      })
      this.filterTagLabels[filterCol] = filterTags;
      this.filterTagOptions = filterTagOptions;
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
      const keynames = Object.keys(getData[0]);

      let cellData;
      for (var row = 0; row < getData.length; row++) {
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
          } else if(col.toLowerCase() == "status"){
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
              chipList: [getData[row][col]],
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

  handleHeaderColNameSelection(event){
    this.headerColName = event.headerColName;
    this.direction = event.direction;
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
    const accountId = rowSelected["Account ID"].valueText;
    const provider = rowSelected["Platform"].valueText;
    if(action.toLowerCase() == "delete"){
      this.deleteAccount(accountId,provider);
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
      this.errorMessage = this.errorHandling.handleJavascriptError(error);
      this.logger.log("error", error);
    }
  }

  /**
   * This function get calls the keyword service before initializing
   * the filter array ,so that filter keynames are changed
   */

  getFilters() {
    this.filterErrorMessage = '';
    let isApiError = true;
    try {
      this.filterTypeLabels.push("Platform");
      this.filterTypeOptions.push({
        optionName: 'Platform',
        optionValue: 'Platform'
      })
      this.routerParam();
      this.getFilterArray();
      this.updateComponent();
    } catch (error) {
      this.filterErrorMessage = 'apiResponseError';
      this.errorMessage = this.errorHandling.handleJavascriptError(error);
      this.logger.log("error", error);
    }
    if(isApiError) this.filterErrorMessage = 'apiResponseError';
  }

  changeFilterType(value) {
    return new Promise((resolve) => {
      this.filterErrorMessage = '';
    try {
      this.currentFilterType = _.find(this.filterTypeOptions, {
        optionName: value,
      });
      if(!this.filterTagOptions[value] || !this.filterTagLabels[value]){
        if(value.toLowerCase()=="platform"){
        this.filterTagLabels[value] = ["aws", "azure","gcp"];
        this.filterTagOptions[value] = [
          {
            id: "aws",
            name: "aws"
          },
          {
            id: "azure",
            name: "azure"
          },
          {
            id: "gcp",
            name: "gcp"
          }
        ]
        resolve(this.filterTagLabels[value]);
        return;
      }
      }
    } catch (error) {
      this.filterErrorMessage = 'apiResponseError';
      this.errorMessage = this.errorHandling.handleJavascriptError(error);
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

