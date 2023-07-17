import { AfterViewInit, Component, OnDestroy, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import find from 'lodash/find';
import map from 'lodash/map';
import { Subscription } from 'rxjs';
import { AssetGroupObservableService } from 'src/app/core/services/asset-group-observable.service';
import { AssetTypeMapService } from 'src/app/core/services/asset-type-map.service';
import { DomainTypeObservableService } from 'src/app/core/services/domain-type-observable.service';
import { TableStateService } from 'src/app/core/services/table-state.service';
import { TourService } from 'src/app/core/services/tour.service';
import { WorkflowService } from 'src/app/core/services/workflow.service';
import { IssueFilterService } from 'src/app/pacman-features/services/issue-filter.service';
import { DATA_MAPPING } from 'src/app/shared/constants/data-mapping';
import { CommonResponseService } from 'src/app/shared/services/common-response.service';
import { DownloadService } from 'src/app/shared/services/download.service';
import { ErrorHandlingService } from 'src/app/shared/services/error-handling.service';
import { LoggerService } from 'src/app/shared/services/logger.service';
import { RefactorFieldsService } from 'src/app/shared/services/refactor-fields.service';
import { RouterUtilityService } from 'src/app/shared/services/router-utility.service';
import { UtilsService } from 'src/app/shared/services/utils.service';
import { environment } from 'src/environments/environment';

enum PolicyCategory {
    ALL_POLICIES = 'all policies',
    COST = 'cost',
    OPERATIONS = 'operations',
    SECURITY = 'security',
    TAGGING = 'tagging',
}

@Component({
  selector: 'app-policy-knowledgebase',
  templateUrl: './policy-knowledgebase.component.html',
  styleUrls: ['./policy-knowledgebase.component.css'],
  providers: [CommonResponseService, LoggerService, ErrorHandlingService]
})
export class PolicyKnowledgebaseComponent implements OnInit, AfterViewInit, OnDestroy {
  pageTitle = 'Policies';
  selectedAssetGroup: string;
  selectedDomain: string;
  subscriptionToAssetGroup: Subscription;
  domainSubscription: Subscription;
  complianceTableSubscription: Subscription;
  issueFilterSubscription: Subscription;
  tableDataLoaded = false;
  searchTxt = '';
  breadcrumbPresent;
  policyCategoryDic: { [key in PolicyCategory]: number } = {
    [PolicyCategory.ALL_POLICIES]: 0,
    [PolicyCategory.SECURITY]: 0,
    [PolicyCategory.OPERATIONS]: 0,
    [PolicyCategory.COST]: 0,
    [PolicyCategory.TAGGING]: 0,
  };
  policyCategories = [
    PolicyCategory.ALL_POLICIES,
    PolicyCategory.SECURITY,
    PolicyCategory.OPERATIONS,
    PolicyCategory.COST,
    PolicyCategory.TAGGING,
  ];
  errorMessage: any = '';
  currentPageLevel = 0;
  headerColName;
  direction;
  showSearchBar = true;
  showAddRemoveCol = true;
  filterText;
  queryParamsWithoutFilter;
  filters = [];
  filterTypeLabels = [];
  filterTagLabels = {};
  filterTypeOptions: any = [];
  filterTagOptions = {};
  currentFilterType;
  centeredColumns = {
    Policy: false,
    Source: true,
    Severity: true,
    Category: true,
    'Asset Type': false,
  };
  columnWidths = { Policy: 3, Source: 0.5, Severity: 0.75, Category: 0.75, 'Asset Type': 1};
  columnNamesMap = { name: 'Policy', provider: 'Source'};
  columnsSortFunctionMap = {
    Severity: (a, b, isAsc) => {
      const severeness = {"low":1, "medium":2, "high":3, "critical":4, "default": 5 * (isAsc ? 1 : -1)}

      const ASeverity = a["Severity"].valueText??"default";
      const BSeverity = b["Severity"].valueText??"default";
      return (severeness[ASeverity] < severeness[BSeverity] ? -1 : 1) * (isAsc ? 1 : -1);
    },
    Category: (a, b, isAsc) => {
      const priority = {"security":4, "operations":3, "cost":2, "tagging":1, "default": 5 * (isAsc ? 1 : -1)}

      const ACategory = a["Category"].valueText??"default";
      const BCategory = b["Category"].valueText??"default";
      return (priority[ACategory] < priority[BCategory] ? -1 : 1) * (isAsc ? 1 : -1);
    },
  };
  tableImageDataMap = {
      [PolicyCategory.ALL_POLICIES]: {
        image: 'policy-icon',
        imageOnly: true
      },
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
  }
  state: any = {};
  whiteListColumns;
  selectedRowIndex;
  displayedColumns;
  tableScrollTop = 0;
  tableData = [];
  isStatePreserved = false;
  doLocalSearch = true; // should be removed once tiles data is available from backend
  totalRows = 0;
  assetTypeMap: any;

  constructor(private assetGroupObservableService: AssetGroupObservableService,
    private router: Router,
    private activatedRoute: ActivatedRoute,
    private commonResponseService: CommonResponseService,
    private logger: LoggerService,
    private errorHandling: ErrorHandlingService,
    private workflowService: WorkflowService,
    private domainObservableService: DomainTypeObservableService,
    private routerUtilityService: RouterUtilityService,
    private refactorFieldsService: RefactorFieldsService,
    private tableStateService: TableStateService,
    private downloadService: DownloadService,
    private assetTypeMapService: AssetTypeMapService,
    private tourService: TourService,
    private utils: UtilsService,
    private issueFilterService: IssueFilterService
    ) {

      this.getPreservedState();
      this.subscriptionToAssetGroup = this.assetGroupObservableService.getAssetGroup().subscribe(assetGroupName => {
        this.selectedAssetGroup = assetGroupName;
        this.searchTxt = "";
        this.updateComponent();
      });
      this.domainSubscription = this.domainObservableService.getDomainType().subscribe(domain => {
        this.selectedDomain = domain;
        this.updateComponent();
      });
    }

    ngOnInit(): void {
      this.breadcrumbPresent = "Policies"

      this.currentPageLevel = this.routerUtilityService.getpageLevel(this.router.routerState.snapshot.root);
    }

  getPreservedState(){
      const state = this.tableStateService.getState("policyKnowledgebase") || {};

      this.searchTxt = this.activatedRoute.snapshot.queryParams.searchValue || '';
      this.displayedColumns = Object.keys(this.columnWidths);

      this.headerColName = state?.headerColName || 'Severity';
      this.direction = state?.direction || 'desc';
      this.displayedColumns = Object.keys(this.columnWidths);
      this.whiteListColumns = state?.whiteListColumns || this.displayedColumns;
      this.searchTxt = state?.searchTxt || '';
      this.tableData = state?.data || [];
      this.tableDataLoaded = true;
      this.tableScrollTop = state?.tableScrollTop;
      this.filters = state?.filters || [];
      this.totalRows = this.tableData.length;
      this.selectedRowIndex = state?.selectedRowIndex;

      if(this.tableData && this.tableData.length>0){
        this.isStatePreserved = true;
      }else{
        this.isStatePreserved = false;
      }

      if(state.filters){
        this.filters = state.filters;
        setTimeout(()=>this.getUpdatedUrl(),0);
      }
  }

  getRouteQueryParameters(): any {
    this.activatedRoute.queryParams.subscribe(
      (params) => {
        if(this.selectedAssetGroup && this.selectedDomain){
          this.updateComponent();
        }
      }
    );
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

  handleSearchInColumnsChange(event){
    // this.state.searchInColumns = event;
  }

  handlePopClick(event) {
    const fileType = "csv";

    try {
      let queryParams;

      queryParams = {
        fileFormat: "csv",
        serviceId: 2,
        fileType: fileType,
      };

      const downloadRequest = {
        ag: this.selectedAssetGroup,
        filter: {
          domain: this.selectedDomain,
        },
        from: 0,
        searchtext: event.searchTxt,
        size: this.totalRows,
      };

      const downloadUrl = environment.download.url;
      const downloadMethod = environment.download.method;

      this.downloadService.requestForDownload(
        queryParams,
        downloadUrl,
        downloadMethod,
        downloadRequest,
        "Policy",
        this.totalRows
      );
    } catch (error) {
      this.logger.log("error", error);
    }
  }

  clearState(){
    // this.tableStateService.clearState("policyKnowledgebase");
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
    this.tableStateService.setState("policyKnowledgebase", state);
  }

  /*
   * This function gets the urlparameter and queryObj
   *based on that different apis are being hit with different queryparams
   */
   routerParam() {
    try {
      // this.filterText saves the queryparam
      const currentQueryParams =
        this.routerUtilityService.getQueryParametersFromSnapshot(
          this.router.routerState.snapshot.root
        );
      if (currentQueryParams) {
        this.queryParamsWithoutFilter = JSON.parse(
          JSON.stringify(currentQueryParams)
        );
        delete this.queryParamsWithoutFilter["filter"];
        /**
         * The below code is added to get URLparameter and queryparameter
         * when the page loads ,only then this function runs and hits the api with the
         * filterText obj processed through processFilterObj function
         */
        this.filterText = this.utils.processFilterObj(currentQueryParams);
      }
    } catch (error) {
      this.errorMessage = this.errorHandling.handleJavascriptError(error);
      this.logger.log("error", error);
    }
  }
  getUpdatedUrl(){
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
      } else if(event.removeOnlyFilterValue) {
        this.getUpdatedUrl();
        this.getData();
        this.storeState();
      } else if(event.index && !this.filters[event.index].filterValue) {
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
        this.getData();
      }
    } catch (error) { }
    /* TODO: Aditya: Why are we not calling any updateCompliance function in observable to update the filters */
  }
  /*
   * this functin passes query params to filter component to show filter
   */
  getFilterArray() {
    try {
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

      const state = this.tableStateService.getState(this.pageTitle) ?? {};
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
            this.filters = [...this.filters];
            this.storeState();
          }
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
    return new Promise((resolve) => {
    try {
      this.issueFilterSubscription = this.issueFilterService
        .getFilters(
          { filterId: 13, domain: this.selectedDomain },
          environment.issueFilter.url,
          environment.issueFilter.method
        )
        .subscribe((response) => {
          this.filterTypeLabels = map(response[0].response, "optionName");
          resolve(true);
          this.filterTypeOptions = response[0].response;

          this.filterTypeLabels.sort();
          this.routerParam();
          // this.deleteFilters();
          this.getFilterArray();
          this.getData();
        });
    } catch (error) {
      this.errorMessage = this.errorHandling.handleJavascriptError(error);
      this.logger.log("error", error);
      resolve(false);
    }
    });
  }

  changeFilterType(value) {
    return new Promise((resolve) => {
    try {
      this.currentFilterType = find(this.filterTypeOptions, {
        optionName: value,
      });
      
      let filtersToBePassed = {};       
      Object.keys(this.filterText).map(key => {
        if(key=="domain" || this.currentFilterType.optionValue == key) return;
        filtersToBePassed[key.replace(".keyword", "")] = this.filterText[key].split(",");
      })
      const payload = {
        attributeName: this.currentFilterType["optionValue"]?.replace(".keyword", ""),
        ag: this.selectedAssetGroup,
        domain: this.selectedDomain,
        filter: filtersToBePassed
      }
        this.issueFilterSubscription = this.issueFilterService
        .getFilters(
          {},
          environment.base +
          this.utils.getParamsFromUrlSnippet(this.currentFilterType.optionURL)
            .url,
          "POST",
          payload
        )
        .subscribe((response) => {          
          let filterTagsData: {[key:string]: any}[] = (response[0].data.optionList || []).map(filterTag => {
            return {id: filterTag, name: filterTag};
          });
          if(value.toLowerCase()=="asset type"){
            this.assetTypeMapService.getAssetMap().subscribe(assetTypeMap=>{
              filterTagsData.map(filterOption => {
                filterOption["name"] = assetTypeMap.get(filterOption["name"]?.toLowerCase()) || filterOption["name"]
              });
            });
          }
          else if(value.toLowerCase()=="violations" || value.toLowerCase()=="compliance"){
            const numOfIntervals = 5;
            const min = response[0].data.optionRange.min;
            const max = response[0].data.optionRange.max;
            const intervals = this.utils.generateIntervals(min, max, numOfIntervals);
            intervals.forEach(interval => {
              filterTagsData.push({id: interval.lowerBound + "-" + interval.upperBound, name: interval.lowerBound + "-" + interval.upperBound});
            })
          }
          this.filterTagOptions[value] = filterTagsData;
          this.filterTagLabels = {
              ...this.filterTagLabels,
              ...{
                  [value]: map(filterTagsData, 'name').sort((a, b) =>
                      a.localeCompare(b),
                  ),
              },
          };
          resolve(this.filterTagOptions[value]);
          this.storeState();
        });
    } catch (error) {
      this.errorMessage = this.errorHandling.handleJavascriptError(error);
      this.logger.log("error", error);
    }
    });
  }

  changeFilterTags(event) {    
    const filterValues = event.filterValue;
    
    this.currentFilterType =  find(this.filterTypeOptions, {
      optionName: event.filterKeyDisplayValue,
    });
    try {
      if (this.currentFilterType) {
        const filterTags = filterValues.map(value => {
          const v = find(this.filterTagOptions[event.filterKeyDisplayValue], { name: value })["id"];
          return v;
        });
        
        this.utils.addOrReplaceElement(
          this.filters,
          {
            keyDisplayValue: event.filterKeyDisplayValue,
            filterValue: filterValues,
            key: this.currentFilterType.optionName,
            value: filterTags,
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
      this.filters = [...this.filters];
      
      this.storeState();
      this.getUpdatedUrl();
      this.getData();
    } catch (error) {
      this.errorMessage = this.errorHandling.handleJavascriptError(error);
      this.logger.log("error", error);
    }
  }

  callNewSearch(searchVal){
    if(!this.doLocalSearch){
      this.searchTxt = searchVal;
      // this.state.searchValue = searchVal;
      this.updateComponent();
    }else{
      this.searchTxt = searchVal;
    }
    this.storeState();
    // this.getUpdatedUrl();
  }

  ngAfterViewInit() {

  }

  updateComponent() {
    if(this.isStatePreserved){
      this.tableDataLoaded = true;
      this.clearState();
      this.tourService.setComponentReady();
    }else{
      this.tableDataLoaded = false;
      this.getFilters();
    }
  }

  processData(data) {
    let processedData = [];
      const getData = data;
      try {
      let innerArr = {};
      const totalVariablesObj = {};
      let cellObj = {};
      const keynames = Object.keys(getData[0]);

      this.assetTypeMapService.getAssetMap().subscribe(assetTypeMap=>{
        this.assetTypeMap = assetTypeMap;
      });

      let cellData;
      for (let row = 0; row < getData.length; row++) {
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
            // chipVariant: "", // this value exists if isChip is true,
            // menuItems: [], // add this if isMenuBtn
          }
          if(col.toLowerCase() === 'policy'){
            const autoFixAvailable = getData[row].autoFixAvailable;
            const autoFixEnabled = getData[row].autoFixEnabled;
            let imgSrc = 'noImg';
            let imageTitleText = "";
            if (autoFixAvailable) {
                imgSrc = autoFixEnabled ? 'autofix' : 'no-autofix';
                imageTitleText = autoFixEnabled ? 'Autofix Enabled': 'Autofix Available'
            }
            cellObj = {
                ...cellObj,
                isLink: true,
                imgSrc: imgSrc,
                imageTitleText: imageTitleText
            };
          } else if(col.toLowerCase() === 'asset type'){
              const currentAssetType = this.assetTypeMap.get(cellData);
              cellObj = {
              ...cellObj,
              text: currentAssetType?currentAssetType:cellData,
              titleText:  currentAssetType?currentAssetType:cellData, // text to show on hover
              valueText:  currentAssetType?currentAssetType:cellData
            };
          }
          // else if(col.toLowerCase() == "status"){
          //   let chipBackgroundColor,chipTextColor;
          //   if(getData[row]["Status"].toLowerCase() === "enabled"){
          //     chipBackgroundColor = "#E6F5EC";
          //     chipTextColor = "#00923f";
          //   }else{
          //     chipBackgroundColor = "#F2F3F5";
          //     chipTextColor = "#73777D";
          //   }
          //   cellObj = {
          //     ...cellObj,
          //     chipList: [getData[row][col]],
          //     text: getData[row][col],
          //     isChip: true,
          //     chipBackgroundColor: chipBackgroundColor,
          //     chipTextColor: chipTextColor
          //   };
          // }
          innerArr[col] = cellObj;
          totalVariablesObj[col] = "";
        });
        processedData.push(innerArr);
      }
      if (processedData.length > getData.length) {
        const halfLength = processedData.length / 2;
        processedData = processedData.splice(halfLength);
      }
    } catch (error) {
      this.errorMessage = this.errorHandling.handleJavascriptError(error);
      this.logger.log("error", error);
    }
    return processedData;
  }

  getTilesData(){
    if(this.policyCategoryDic["all policies"]){
      return;
    }
    const newPolicyDic: {[key in PolicyCategory]: number} = {
            [PolicyCategory.ALL_POLICIES]: 0,
            [PolicyCategory.COST]: 0,
            [PolicyCategory.OPERATIONS]: 0,
            [PolicyCategory.SECURITY]: 0,
            [PolicyCategory.TAGGING]: 0,
        };

    const payload = {
      ag: this.selectedAssetGroup,
      filter: {domain: this.selectedDomain},
      reqFilter: {},
      "includeDisabled" : false 
    };

    const complianceTableUrl = environment.complianceTable.url;
    const complianceTableMethod = environment.complianceTable.method;
    this.complianceTableSubscription = this.commonResponseService
    .getData(complianceTableUrl, complianceTableMethod, payload, {})
    .subscribe(
      (response) => {
        try {
          const getData = response.data.response;
          for (let i = 0; i < getData.length; i++) {
            newPolicyDic[PolicyCategory.ALL_POLICIES]++;
            newPolicyDic[getData[i]["policyCategory"].toLowerCase()]++
          }
          this.policyCategoryDic = newPolicyDic;
        }catch(e){
          this.logger.log("jsError", e);
        }
      }, error => {
        this.logger.log("apiError", error);
      })
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
      newObj["autofix info"] = newObj["autoFixAvailable"]?(newObj["autoFixEnabled"]?"autofix enabled":"autofix available"):"not available";
      newObj["Autofix status"] = newObj["autoFixAvailable"]?(newObj["autoFixEnabled"]?"enabled":"available"):"not available";
      newData.push(newObj);
    });
    return newData;
  }

  getData() {
    if(!this.selectedAssetGroup || !this.selectedDomain){
      return;
    }
    this.tableDataLoaded = false;
    this.errorMessage = '';
    if (this.complianceTableSubscription) {
      this.complianceTableSubscription.unsubscribe();
    }
    const filterToBePassed = {...this.filterText};

    Object.keys(filterToBePassed).forEach(filterKey => {
      if(filterKey=="domain") return;
      filterToBePassed[filterKey] = filterToBePassed[filterKey].split(",");
      if(filterKey=="failed" || filterKey=="compliance_percent"){
        filterToBePassed[filterKey] = filterToBePassed[filterKey].map(filterVal => {
          const [min, max] = filterVal.split("-");
          return {min, max}
        })
      }
    })

    const filters = {domain: this.selectedDomain};

    const payload = {
      ag: this.selectedAssetGroup,
      filter: filters,
      reqFilter: filterToBePassed,
      "includeDisabled" : false 
    };

    const complianceTableUrl = environment.complianceTable.url;
    const complianceTableMethod = environment.complianceTable.method;
    this.complianceTableSubscription = this.commonResponseService
    .getData(complianceTableUrl, complianceTableMethod, payload, {})
    .subscribe(
      (response) => {
        this.totalRows = response.total;
        try {
          const updatedResponse = this.massageData(response.data.response);
          const processedData = this.processData(updatedResponse);
          this.tableData = processedData;
          this.getTilesData();
          this.tableDataLoaded = true;
          if (this.tableData.length === 0) {
            this.totalRows = 0;
            this.errorMessage = 'noDataAvailable';
          }
          if (response.hasOwnProperty("total")) {
            this.totalRows = response.data.total;
          } else {
            this.totalRows = this.tableData.length;
          }
        } catch (e) {
          this.tableDataLoaded = true;
          this.errorMessage = this.errorHandling.handleJavascriptError(e);
        }
        this.tourService.setComponentReady();
      },
      (error) => {
        this.tableDataLoaded = true;
        this.errorMessage = "apiResponseError";
        this.tourService.setComponentReady();
      }
    );
  }

  /*
    * this function is used to fetch the rule id and to navigate to the next page
    */

  goToDetails(event) {
    // store in this function
    const tileData = event.rowSelected;
    const data = event.data;
    this.selectedRowIndex = event.selectedRowIndex;
    this.tableScrollTop = event.tableScrollTop;
    this.storeState(data);
   let autofixEnabled = false;
    if ( tileData.autoFixEnabled) {
      autofixEnabled = true;
    }
    const policyId = tileData["Policy ID"].valueText;
    try {
      this.workflowService.addRouterSnapshotToLevel(this.router.routerState.snapshot.root, 0, this.breadcrumbPresent);
      const updatedQueryParams = {...this.activatedRoute.snapshot.queryParams};
      updatedQueryParams["searchValue"] = undefined;
      this.router.navigate(
        ['pl', 'compliance', 'policy-knowledgebase-details', policyId, autofixEnabled],
        { queryParams: updatedQueryParams,
          queryParamsHandling: 'merge' });
    } catch (error) {
      this.errorMessage = this.errorHandling.handleJavascriptError(error);
      this.logger.log('error', error);
    }
  }

  applyFilterByCategory(policyCategory: PolicyCategory) {
      const key = 'Category';
      const newFilters = this.filters.filter((f) => f.key !== key);
      if (policyCategory !== PolicyCategory.ALL_POLICIES) {
          this.changeFilterType(key).then(() => {
            this.changeFilterTags({
              filterKeyDisplayValue: key,
              filterValue: [policyCategory],
            })
          })
      }
  }

  ngOnDestroy() {
    try {
      if (this.complianceTableSubscription) {
        this.complianceTableSubscription.unsubscribe();
      }
      if (this.subscriptionToAssetGroup) {
        this.subscriptionToAssetGroup.unsubscribe();
      }
      if (this.domainSubscription) {
        this.domainSubscription.unsubscribe();
      }
    } catch (error) {
      this.logger.log('error', '--- Error while unsubscribing ---');
    }
  }
}
