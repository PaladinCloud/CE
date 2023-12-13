import { AfterViewInit, Component, OnDestroy, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import find from 'lodash/find';
import map from 'lodash/map';
import { Subscription } from 'rxjs';
import { AssetTypeMapService } from 'src/app/core/services/asset-type-map.service';
import { TableStateService } from 'src/app/core/services/table-state.service';
import { TourService } from 'src/app/core/services/tour.service';
import { WorkflowService } from 'src/app/core/services/workflow.service';
import { IssueFilterService } from 'src/app/pacman-features/services/issue-filter.service';
import { ComponentKeys } from 'src/app/shared/constants/component-keys';
import { CommonResponseService } from 'src/app/shared/services/common-response.service';
import { DownloadService } from 'src/app/shared/services/download.service';
import { ErrorHandlingService } from 'src/app/shared/services/error-handling.service';
import { LoggerService } from 'src/app/shared/services/logger.service';
import { RefactorFieldsService } from 'src/app/shared/services/refactor-fields.service';
import { RouterUtilityService } from 'src/app/shared/services/router-utility.service';
import { UtilsService } from 'src/app/shared/services/utils.service';
import { environment } from 'src/environments/environment';
import { AgDomainObservableService } from 'src/app/core/services/ag-domain-observable.service';
import { FilterManagementService } from 'src/app/shared/services/filter-management.service';

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
  saveStateKey: String = ComponentKeys.UserPolicyList;
  selectedAssetGroup: string;
  selectedDomain: string;
  subscriptionToAssetGroup: Subscription;
  domainSubscription: Subscription;
  complianceTableSubscription: Subscription;
  issueFilterSubscription: Subscription;
  agDomainSubscription: Subscription;
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
  columnWidths = { Policy: 3, Source: 0.5, Severity: 0.75, Category: 0.75, 'Asset Type': 1 };
  columnNamesMap = { name: 'Policy', provider: 'Source' };
  columnsSortFunctionMap = {
    Severity: (a, b, isAsc) => {
      const severeness = { "low": 1, "medium": 2, "high": 3, "critical": 4, "default": 5 * (isAsc ? 1 : -1) }

      const ASeverity = a["Severity"].valueText ?? "default";
      const BSeverity = b["Severity"].valueText ?? "default";
      return (severeness[ASeverity] < severeness[BSeverity] ? -1 : 1) * (isAsc ? 1 : -1);
    },
    Category: (a, b, isAsc) => {
      const priority = { "security": 4, "operations": 3, "cost": 2, "tagging": 1, "default": 5 * (isAsc ? 1 : -1) }

      const ACategory = a["Category"].valueText ?? "default";
      const BCategory = b["Category"].valueText ?? "default";
      return (priority[ACategory] < priority[BCategory] ? -1 : 1) * (isAsc ? 1 : -1);
    },
  };
  tableImageDataMap = {
    [PolicyCategory.ALL_POLICIES]: {
      image: 'policy-icon',
      imageOnly: true
    },
    security: {
      image: "category-security",
      imageOnly: true
    },
    operations: {
      image: "category-operations",
      imageOnly: true
    },
    cost: {
      image: "category-cost",
      imageOnly: true
    },
    tagging: {
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
  selectedRowId: string;
  displayedColumns;
  tableScrollTop = 0;
  tableData = [];
  isStatePreserved = false;
  doLocalSearch = true; // should be removed once tiles data is available from backend
  totalRows = 0;
  assetTypeMap: any;

  constructor(private agDomainObservableService: AgDomainObservableService,
    private router: Router,
    private activatedRoute: ActivatedRoute,
    private commonResponseService: CommonResponseService,
    private logger: LoggerService,
    private errorHandling: ErrorHandlingService,
    private workflowService: WorkflowService,
    private routerUtilityService: RouterUtilityService,
    private tableStateService: TableStateService,
    private downloadService: DownloadService,
    private assetTypeMapService: AssetTypeMapService,
    private tourService: TourService,
    private utils: UtilsService,
    private issueFilterService: IssueFilterService,
    private filterManagementService: FilterManagementService
    ) {

      this.assetTypeMapService.getAssetMap().subscribe(assetTypeMap => {
        this.assetTypeMap = assetTypeMap;
      });
      this.agDomainSubscription = this.agDomainObservableService.getAgDomain().subscribe(([ag, domain]) => { 
        this.selectedAssetGroup = ag;
        this.selectedDomain = domain;
        this.getPreservedState();
        this.getFilters();
      })
    }

    ngOnInit(): void {
      this.breadcrumbPresent = "Policies"
      this.currentPageLevel = this.routerUtilityService.getpageLevel(this.router.routerState.snapshot.root);
      window.onbeforeunload = () => this.storeState();
    }

  getPreservedState(){
      const state = this.tableStateService.getState(this.saveStateKey) || {};
      
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
      this.selectedRowId = state?.selectedRowId;
      if(state?.policyCategoryDic)
      this.policyCategoryDic = state.policyCategoryDic;

      if(this.tableData && this.tableData.length>0){
        this.isStatePreserved = true;
      }else{
      this.isStatePreserved = false;
        }

    if (state.filters) {
      this.filters = state.filters;
      Promise.resolve().then(() => this.getUpdatedUrl());
    }
  }

  getRouteQueryParameters(): any {
    this.activatedRoute.queryParams.subscribe(
      (params) => {
        if (this.selectedAssetGroup && this.selectedDomain) {
          this.updateComponent();
        }
      }
    );
  }

  handleHeaderColNameSelection(event) {
    this.headerColName = event.headerColName;
    this.direction = event.direction;
  }

  handleWhitelistColumnsChange(event) {
    this.whiteListColumns = event;
  }

  handleSearchInColumnsChange(event) {
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

      const filtersToBePassed = this.getFilterPayloadForDataAPI();

      const downloadRequest = {
        ag: this.selectedAssetGroup,
        filter: {
          domain: this.selectedDomain,
        },
        reqFilter: filtersToBePassed,
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

  clearState() {
    // this.tableStateService.clearState("policyKnowledgebase");
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
      selectedRowId: this.selectedRowId,
      policyCategoryDic: this.policyCategoryDic
    }
    this.tableStateService.setState(this.saveStateKey, state);
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

  deleteFilters (event?) {
    let shouldUpdateComponent = false;
    [this.filters, shouldUpdateComponent] = this.filterManagementService.deleteFilters(event, this.filters);
    if (shouldUpdateComponent) {
      this.getUpdatedUrl();
      this.updateComponent();
    }
  }
  
  /*
   * this functin passes query params to filter component to show filter
   */
  async getFilterArray () {
    try {
      const filterText = this.filterText;
      const filterTypeOptions = this.filterTypeOptions;
      let filters = this.filters;

      const formattedFilters = this.filterManagementService.getFormattedFilters(filterText, filterTypeOptions);

      for (let i = 0; i < formattedFilters.length; i++) {
        filters = await this.processAndAddFilterItem({ formattedFilterItem: formattedFilters[i], filters });
        this.filters = filters;
      }
    } catch (error) {
      this.errorMessage = this.errorHandling.handleJavascriptError(error);
      this.logger.log("error", error);
    }
  }

  async processAndAddFilterItem ({ formattedFilterItem, filters }) {

    const keyDisplayValue = this.utils.getFilterKeyDisplayValue(formattedFilterItem, this.filterTypeOptions);
    const filterKey = formattedFilterItem.filterkey;

    const existingFilterObjIndex = filters.findIndex(filter => filter.keyDisplayValue === keyDisplayValue);
    if (existingFilterObjIndex < 0) {
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
    return new Promise((resolve) => {
    try {
      this.issueFilterSubscription = this.issueFilterService
        .getFilters(
          { filterId: 16, domain: this.selectedDomain },
          environment.issueFilter.url,
          environment.issueFilter.method
        )
        .subscribe(async(response) => {
          this.filterTypeLabels = map(response[0].response, "optionName");
          resolve(true);
          this.filterTypeOptions = response[0].response;

          this.filterTypeLabels.sort();
          this.routerParam();
          await this.getFilterArray();
          await Promise.resolve().then(() => this.getUpdatedUrl());
          this.updateComponent();
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

        const excludedKeys = [
          "domain",
          this.currentFilterType.optionValue
        ]
        let filtersToBePassed = this.getFilterPayloadForDataAPI();
        filtersToBePassed = Object.keys(filtersToBePassed).reduce((result, key) => {
          const normalizedKey = key.replace(".keyword", "");
          if ((!excludedKeys.includes(normalizedKey))) {
            result[normalizedKey] = filtersToBePassed[key];
          }
          return result;
        }, {});
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
            let filterTagsData: { [key: string]: any }[] = (response[0].data.optionList || []).map(filterTag => {
              return { id: filterTag, name: filterTag };
            });
            if (value.toLowerCase() == "asset type") {
              this.assetTypeMapService.getAssetMap().subscribe(assetTypeMap => {
                filterTagsData.map(filterOption => {
                  filterOption["name"] = assetTypeMap.get(filterOption["name"]?.toLowerCase()) || filterOption["name"]
                });
              });
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

    this.currentFilterType = find(this.filterTypeOptions, {
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
      
      this.getUpdatedUrl();
      this.getData();
    } catch (error) {
      this.errorMessage = this.errorHandling.handleJavascriptError(error);
      this.logger.log("error", error);
    }
  }

  callNewSearch(searchVal) {
    if (!this.doLocalSearch) {
      this.searchTxt = searchVal;
      // this.state.searchValue = searchVal;
      this.updateComponent();
    } else {
      this.searchTxt = searchVal;
    }
  }

  ngAfterViewInit() {

  }

  updateComponent() {
    if (this.isStatePreserved) {
      this.tableDataLoaded = true;
      this.clearState();
      this.tourService.setComponentReady();
    } else {
      this.tableDataLoaded = false;
      this.getData();
    }
  }

  processData (data) {
    try {
      return this.utils.processTableData(data, this.tableImageDataMap, (row, col, cellObj) => {
        const cellData = row[col];
        if (col.toLowerCase() === 'policy') {
          const autoFixAvailable = typeof row.autoFixAvailable == "string" ? row.autoFixAvailable == "true" : row.autoFixAvailable;
          const autoFixEnabled = typeof row.autoFixEnabled == "string" ? row.autoFixEnabled == "true" : row.autoFixEnabled;
          let imgSrc = 'noImg';
          let imageTitleText = "";
          if (autoFixAvailable) {
            imgSrc = autoFixEnabled ? 'autofix' : 'no-autofix';
            imageTitleText = autoFixEnabled ? 'Autofix Enabled' : 'Autofix Available'
          }
          cellObj = {
            ...cellObj,
            isLink: true,
            imgSrc: imgSrc,
            imageTitleText: imageTitleText
          };
        } else if (col.toLowerCase() === 'asset type') {
          const currentAssetType = this.assetTypeMap.get(cellData);
          cellObj = {
            ...cellObj,
            text: currentAssetType ? currentAssetType : cellData,
            titleText: currentAssetType ? currentAssetType : cellData, // text to show on hover
            valueText: currentAssetType ? currentAssetType : cellData
          };
        }
        return cellObj;
      });

    } catch (error) {
      this.errorMessage = this.errorHandling.handleJavascriptError(error);
      this.logger.log("error", error);
    }
  }

  getTilesData() {
    const newPolicyDic: { [key in PolicyCategory]: number } = {
      [PolicyCategory.ALL_POLICIES]: 0,
      [PolicyCategory.COST]: 0,
      [PolicyCategory.OPERATIONS]: 0,
      [PolicyCategory.SECURITY]: 0,
      [PolicyCategory.TAGGING]: 0,
    };

    const payload = {
      ag: this.selectedAssetGroup,
      filter: { domain: this.selectedDomain },
      reqFilter: {},
      "includeDisabled": false
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
          } catch (e) {
            this.logger.log("jsError", e);
          }
        }, error => {
          this.logger.log("apiError", error);
        })
  }

  getFilterPayloadForDataAPI(){
    const filterToBePassed = {...this.filterText};

    Object.keys(filterToBePassed).forEach(filterKey => {
      if (filterKey == "domain") return;
      filterToBePassed[filterKey] = filterToBePassed[filterKey].split(",");
    })

    return filterToBePassed;
  }

  getData() {
    if (!this.selectedAssetGroup || !this.selectedDomain) {
      return;
    }
    this.tableDataLoaded = false;
    this.errorMessage = '';
    if (this.complianceTableSubscription) {
      this.complianceTableSubscription.unsubscribe();
    }

    const filters = { domain: this.selectedDomain };

    const payload = {
      ag: this.selectedAssetGroup,
      filter: filters,
      reqFilter: this.getFilterPayloadForDataAPI(),
      "includeDisabled": false
    };

    const complianceTableUrl = environment.complianceTable.url;
    const complianceTableMethod = environment.complianceTable.method;
    this.complianceTableSubscription = this.commonResponseService
      .getData(complianceTableUrl, complianceTableMethod, payload, {})
      .subscribe(
        (response) => {
          this.totalRows = response.total;
          try {
            const updatedResponse = this.utils.massageTableData(response.data.response, this.columnNamesMap);
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
    this.selectedRowId = event.selectedRowId;
    this.tableScrollTop = event.tableScrollTop;
    let autofixEnabled = false;
    if ( tileData.autoFixEnabled) {
      autofixEnabled = true;
    }
    const policyId = tileData["Policy ID"].valueText;
    try {
      this.workflowService.addRouterSnapshotToLevel(this.router.routerState.snapshot.root, 0, this.breadcrumbPresent);
      const updatedQueryParams = { ...this.activatedRoute.snapshot.queryParams };
      updatedQueryParams["searchValue"] = undefined;
      this.router.navigate(
        ['pl', 'compliance', 'policy-knowledgebase-details', policyId, autofixEnabled],
        {
          queryParams: updatedQueryParams,
          queryParamsHandling: 'merge'
        });
    } catch (error) {
      this.errorMessage = this.errorHandling.handleJavascriptError(error);
      this.logger.log('error', error);
    }
  }

  async applyFilterByCategory(policyCategory: PolicyCategory) {
    if (this.policyCategoryDic[policyCategory] == 0) return;
    const key = 'Category';
    this.filters = [];
    await Promise.resolve().then(() => this.getUpdatedUrl());
    if (policyCategory !== PolicyCategory.ALL_POLICIES) {
      await this.changeFilterType(key)
      this.changeFilterTags({
        filterKeyDisplayValue: key,
        filterValue: [policyCategory],
      })
    } else {
      await this.changeFilterType(key)
      this.changeFilterTags({
        filterKeyDisplayValue: key,
        filterValue: this.filterTagOptions[key].map(item => item.id),
      })
    }
  }

  ngOnDestroy() {
    try {
      this.storeState();
      if(this.agDomainSubscription){
        this.agDomainSubscription.unsubscribe();
      }
      if (this.complianceTableSubscription) {
        this.complianceTableSubscription.unsubscribe();
      }
      if (this.subscriptionToAssetGroup) {
        this.subscriptionToAssetGroup.unsubscribe();
      }
      if (this.domainSubscription) {
        this.domainSubscription.unsubscribe();
      }
      if(this.agDomainSubscription){
         this.agDomainSubscription.unsubscribe();
      }
    } catch (error) {
      this.logger.log('error', '--- Error while unsubscribing ---');
    }
  }
}
