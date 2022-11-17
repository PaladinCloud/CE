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

import { Component, OnInit, ElementRef, ViewChild } from "@angular/core";
import { AssetGroupObservableService } from "../../../../core/services/asset-group-observable.service";
import { SelectComplianceDropdown } from "./../../../services/select-compliance-dropdown.service";
import { CommonResponseService } from "../../../../shared/services/common-response.service";
import { Subscription } from "rxjs";
import { environment } from "./../../../../../environments/environment";
import { ActivatedRoute, UrlSegment, Router } from "@angular/router";
import { IssueFilterService } from "../../../services/issue-filter.service";
import { LoggerService } from "../../../../shared/services/logger.service";
import { ErrorHandlingService } from "../../../../shared/services/error-handling.service";
import { DownloadService } from "../../../../shared/services/download.service";
import { UtilsService } from "../../../../shared/services/utils.service";
import * as _ from "lodash";
import { WorkflowService } from "../../../../core/services/workflow.service";
import { DomainTypeObservableService } from "../../../../core/services/domain-type-observable.service";
import { PacmanIssuesService } from "../../../services/pacman-issues.service";
import { RefactorFieldsService } from "../../../../shared/services/refactor-fields.service";
import { OverallComplianceService } from "src/app/pacman-features/services/overall-compliance.service";
import { MultilineChartService } from "src/app/pacman-features/services/multilinechart.service";
import { DATA_MAPPING } from "src/app/shared/constants/data-mapping";
import { TableStateService } from "src/app/core/services/table-state.service";

@Component({
  selector: "app-compliance-dashboard",
  templateUrl: "./compliance-dashboard.component.html",
  styleUrls: ["./compliance-dashboard.component.css"],
  animations: [],
  providers: [
    CommonResponseService,
    IssueFilterService,
    LoggerService,
    ErrorHandlingService,
    OverallComplianceService, 
    MultilineChartService
  ],
})
export class ComplianceDashboardComponent implements OnInit {
  @ViewChild("widget") widgetContainer: ElementRef;

  pageTitle = "Overview";
  filterArr: any = [];
  subscriptionToAssetGroup: Subscription;
  selectedAssetGroup: string;
  showingArr: any;
  ruleCatFilter;
  noMinHeight = false;
  paginatorSize = 20;
  totalRows = 0;
  bucketNumber = 0;
  searchTxt = "";
  complianceTableData: any = [];
  currentFilterType;
  filterTypeLabels = [];
  filterTagLabels = [];
  filterTypeOptions: any = [];
  filters: any = [];
  filterTagOptions: any = [];
  selectedDomain: any = "";
  searchPassed = "";
  tableDataLoaded = false;
  showSearchBar = false;
  showAddRemoveCol = false;
  private assetGroupSubscription: Subscription;
  private onFilterChange: Subscription;
  private routeSubscription: Subscription;
  private complianceTableSubscription: Subscription;
  private issueFilterSubscription: Subscription;
  private downloadSubscription: Subscription;
  private activatedRouteSubscription: Subscription;
  private subscriptionDomain: Subscription;
  private queryParameters: any = {};
  public pageLevel = 0;
  dataSubscriber: any;
  policyData: {
    color: string[];
    data: any[];
    legend: string[];
    legendTextcolor: string;
    totalCount: number;
    link: boolean;
    styling: { cursor: string };
  };
  policyDataError: string = '';
  pacmanIssues: any;
  pacmanCategories: any[];
  showdata: boolean;
  error: boolean;
  loaded: boolean;
  fetchedViolations: boolean = false;
  widgetWidth2: number;
  breakpoint1: number;
  breakpoint2: number;
  breakpoint3: number;
  breakpoint4: number;
  tableTitle = "Policy Compliance Overview";
  tableErrorMessage = '';
  errorMessage = '';
  headerColName;
  direction;
  complianceData = [];
  complianceDataError = '';
  assetsCountData = [];
  assetsCountDataError = '';
  breadcrumbArray = [];
  breadcrumbLinks = [];
  breadcrumbPresent = "Dashboard";
  columnNamesMap = {name: "Title", failed: "Violations", provider: "Cloud", severity:"Severity",ruleCategory: "Category"}
  columnWidths = {"Title": 3, "Violations": 1, "Cloud": 1, "Severity": 1, "Category": 1, "Compliance":1};
  columnsSortFunctionMap = {
    Severity: (a, b, isAsc) => {
      let severeness = {"low":1, "medium":2, "high":3, "critical":4}
      return (severeness[a["Severity"]] < severeness[b["Severity"]] ? -1 : 1) * (isAsc ? 1 : -1);
    },
    Compliance: (a: string, b: string, isAsc) => {
      a = a["Compliance"];
      b = b["Compliance"]

      if(a=="NR") isAsc?a="101%":a = "-1%";
      if(b=="NR") isAsc?b="101%":b = "-1%";

      a = a.substring(0, a.length-1);
      b = b.substring(0, b.length-1);

      let aNum = parseFloat(a);
      let bNum = parseFloat(b);
      
      return (aNum < bNum ? -1 : 1) * (isAsc ? 1 : -1);
    }
  };
  tableDataMap = {
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
      costOptimization:{
          image: "category-cost",
          imageOnly: true
      },
      cost: {
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
  displayedColumns;
  
  totalAssetsCountData = [];
  totalAssetsCountDataError = '';
  isStatePreserved = false;
  showDownloadBtn = true;
  tableScrollTop = 0;
  graphFromDate: Date = new Date(2022, 1, 1);
  graphToDate: Date = new Date(2200, 12, 31);

  massageAssetTrendGraphData(graphData){
    let data = [];
    data.push({"key":"Total Assets", "values":[], "info":{}})

    for(let i=0; i<data.length; i++){
        graphData.trend.forEach(e => {
        data[i].values.push({
            'date':new Date(e.date),
            'value':e.totalassets+i*25,
            'zero-value':e.totalassets+i*25==0
        });
      })   
      data[i].values.sort(function(a,b){
        return new Date(a.date).valueOf() - new Date(b.date).valueOf();
      });
      }
       

    data[0].info = {
      id: "TotalAssetsCountTrend",
      showLegend: true,
      yAxisLabel: 'Count',
      height: 320
    }

    return data;
  }
  openOverAllComplianceTrendModal = () => {
    this.router.navigate(
      ["/pl", { outlets: { modal: ["overall-compliance-trend"] } }],
      { queryParamsHandling: "merge" }
    );
  };

  openOverAllPolicyViolationsTrendModal = () => {
    this.router.navigate(
      ["/pl", { outlets: { modal: ["policy-violations-trend"] } }],
      { queryParamsHandling: "merge" }
    );
  };

  navigateToAssetDistribution = () => {
    this.workflowService.addRouterSnapshotToLevel(
        this.router.routerState.snapshot.root, 0, this.breadcrumbPresent,
      );
    this.router.navigate(["/pl/assets/asset-distribution/"], {
      queryParamsHandling: "merge",
    });
  };

  violationCards = [
    {id: 1, name: "critical", totalViolations: 0, subInfo: {Policy: 0, Assets: 0, "Average age": 0}},
    {id: 2, name: "high", totalViolations: 0, subInfo: {Policy: 0, Assets: 0, "Average age": 0}},
    {id: 3, name: "medium", totalViolations: 0, subInfo: {Policy: 0, Assets: 0, "Average age": 0}},
    {id: 4, name: "low", totalViolations: 0, subInfo: {Policy: 0, Assets: 0, "Average age": 0}},
  ]

  cards = [
    {
      id: 1,
      header: "Category Compliance",
      footer: "View Trends",
      cardButtonAction: this.openOverAllComplianceTrendModal,
    },
    {
      id: 2,
      header: "Violations by Severity",
      footer: "View Trends",
      cardButtonAction: this.openOverAllPolicyViolationsTrendModal,
    },
    {
      id: 3,
      header: "Asset Graph",
      footer: "View Asset Distribution",
      cardButtonAction: this.navigateToAssetDistribution,
    },
  ];

  constructor(
    private assetGroupObservableService: AssetGroupObservableService,
    private commonResponseService: CommonResponseService,
    private selectComplianceDropdown: SelectComplianceDropdown,
    private activatedRoute: ActivatedRoute,
    private utils: UtilsService,
    private logger: LoggerService,
    private router: Router,
    private errorHandling: ErrorHandlingService,
    private issueFilterService: IssueFilterService,
    private downloadService: DownloadService,
    private workflowService: WorkflowService,
    private pacmanIssuesService: PacmanIssuesService,
    private refactorFieldsService: RefactorFieldsService,
    private domainObservableService: DomainTypeObservableService,
    private overallComplianceService: OverallComplianceService, 
    private tableStateService: TableStateService,
    private multilineChartService: MultilineChartService
  ) {}

  handleHeaderColNameSelection(event) {
    this.headerColName = event.headerColName;
    this.direction = event.direction;
  }

  clearState(){
    this.tableStateService.clearState("dashboard");
    this.isStatePreserved = false;
  }

  storeState(state){
    this.tableStateService.setState("dashboard", state);    
  }

  getUpdatedUrl() {
    let updatedQueryParams = {};
    updatedQueryParams = {
      headerColName: this.headerColName,
      direction: this.direction,
      bucketNumber: this.bucketNumber,
      searchValue: this.searchTxt
    }

    this.router.navigate([], {
      relativeTo: this.activatedRoute,
      queryParams: updatedQueryParams,
      queryParamsHandling: 'merge',
    });
  }

  getDistributionBySeverity(){
    const distributionBySeverityUrl = environment.distributionBySeverity.url;
    const distributionBySeverityMethod = environment.distributionBySeverity.method;
    const queryParams = {
      ag: this.selectedAssetGroup,
      domain: this.selectedDomain,
    };

    try {
      this.commonResponseService.getData(distributionBySeverityUrl, distributionBySeverityMethod, {},queryParams).subscribe(response => {
        const data = response.distribution.distributionBySeverity;
        for(let i=0; i<this.violationCards.length; i++){
            this.violationCards[i].totalViolations =
                  data[this.violationCards[i].name].totalViolations;
            this.violationCards[i].subInfo = {
              Policy: data[this.violationCards[i].name].policyCount,
              Assets: data[this.violationCards[i].name].assetCount,
              "Average age": Math.round(data[this.violationCards[i].name].averageAge)
            }
        }
      })
    } catch (error) {
      
    }
  }

  getPacmanIssues() {
    if (this.dataSubscriber) {
      this.dataSubscriber.unsubscribe();
    }
    const queryParams = {
      ag: this.selectedAssetGroup,
      domain: this.selectedDomain,
    };
    const pacmanIssuesUrl = environment.pacmanIssues.url;
    const pacmanIssuesMethod = environment.pacmanIssues.method;
    try {
      this.dataSubscriber = this.pacmanIssuesService
        .getData(queryParams, pacmanIssuesUrl, pacmanIssuesMethod)
        .subscribe(
          (response) => {
            try {
              if (response.err) {
                throw response;
              }
              this.pacmanIssues = response;
              this.pacmanCategories = [];
              for (let i = 0; i < this.pacmanIssues.category.length; i++) {
                const obj = {
                  displayName:
                    this.refactorFieldsService.getDisplayNameForAKey(
                      Object.keys(
                        this.pacmanIssues.category[i]
                      )[0].toLowerCase()
                    ) || Object.keys(this.pacmanIssues.category[i])[0],
                  key: Object.keys(this.pacmanIssues.category[i])[0],
                  value:
                    this.pacmanIssues.category[i][
                    Object.keys(this.pacmanIssues.category[i])[0]
                    ],
                };
                this.pacmanCategories.push(obj);
              }
              let dataValue = [],
                totalCount = 0;
              for (let i = 0; i < this.pacmanIssues.severity.length; i++) {
                const count = this.pacmanIssues.severity[i][
                  Object.keys(this.pacmanIssues.severity[i])[0]
                ];
                totalCount += count;
                dataValue.push(count);
              }
              this.fetchedViolations = true;
              this.policyDataError = '';
              if (dataValue.length > 0) {
                this.policyData = {
                  color: ["#D14938", "#F58F6F", "#F5B66F", "#506EA7"],
                  data: dataValue,
                  legend: ["Critical", "High", "Medium", "Low"],
                  legendTextcolor: "#000",
                  totalCount: totalCount,
                  link: true,
                  styling: {
                    cursor: "pointer",
                  },
                };
              } else {
                this.policyDataError = 'noDataAvailable'
              }
              this.loaded = true;
              this.showdata = true;
              this.error = false;
            } catch (e) {
              this.policyDataError = 'apiResponseError';
              this.tableErrorMessage = this.errorHandling.handleJavascriptError(e);
              this.getErrorValues();
            }
          },
          (error) => {
            this.tableErrorMessage = error;
            this.getErrorValues();
          }
        );
    } catch (error) {
      this.tableErrorMessage = this.errorHandling.handleJavascriptError(error);
      this.getErrorValues();
    }
  }

  getErrorValues(): void {
    this.loaded = true;
    this.error = true;
  }

  ngOnInit() {
    const state = this.tableStateService.getState("dashboard") || {};    
      
    this.headerColName = state.headerColName || 'Compliance';
    this.direction = state.direction || 'asc';
    // this.bucketNumber = state.bucketNumber || 0;
    
    this.displayedColumns = Object.keys(this.columnWidths);
    this.whiteListColumns = state?.whiteListColumns || this.displayedColumns;
    this.complianceTableData = state?.data || [];
    this.tableDataLoaded = true;
    this.searchTxt = state?.searchTxt || '';
    this.tableScrollTop = state?.tableScrollTop;    
    this.totalRows = state.totalRows || 0;

    if(this.complianceTableData && this.complianceTableData.length>0){        
      this.isStatePreserved = true;
    }else{
      this.isStatePreserved = false;
    }


    this.assetGroupSubscription = this.subscriptionToAssetGroup =
      this.assetGroupObservableService
        .getAssetGroup()
        .subscribe((assetGroupName) => {  
          this.selectedAssetGroup = assetGroupName;
          this.updateComponent();
        });

    this.subscriptionDomain = this.domainObservableService
      .getDomainType()
      .subscribe((domain) => {        
        this.selectedDomain = domain;
        if(this.selectedAssetGroup){
          this.updateComponent();
        }
      });


    const breadcrumbInfo = this.workflowService.getDetailsFromStorage()["level0"];    
    
    if(breadcrumbInfo){
      this.breadcrumbArray = breadcrumbInfo.map(item => item.title);
      this.breadcrumbLinks = breadcrumbInfo.map(item => item.url);
    }

    this.breadcrumbPresent = "Dashboard";

    this.breakpoint1 = window.innerWidth <= 800 ? 2 : 4;
    this.breakpoint2 = window.innerWidth <= 800 ? 1 : 2;
    this.breakpoint3 = window.innerWidth <= 400 ? 1 : 1;
    this.breakpoint4 = window.innerWidth <= 400 ? 1 : 1;
  }

  getFilters() {
    try {
      this.issueFilterSubscription = this.issueFilterService
        .getFilters(
          { filterId: 4 },
          environment.issueFilter.url,
          environment.issueFilter.method
        )
        .subscribe((response) => {
          this.filterTypeLabels = _.map(response[0].response, "optionName");
          this.filterTypeOptions = response[0].response;
        });
    } catch (error) {
      this.errorMessage = this.errorHandling.handleJavascriptError(error);
      this.logger.log("error", error);
    }
  }

  updateComponent() {    
    if (this.complianceTableSubscription) {
      this.complianceTableSubscription.unsubscribe();
    }
    // below condition ensures that on initial landing, updatecomponent executes only once
    if(!this.selectedAssetGroup || !this.selectedDomain){
      return;
    }
    this.ruleCatFilter = undefined;
    this.noMinHeight = false;
    // this.bucketNumber = 0;
    // this.currentPointer = 0;
    
    this.assetsCountData = [];
    this.assetsCountDataError = '';
    this.complianceData = [];
    this.complianceDataError = '';
    this.policyDataError = '';
    if(this.isStatePreserved){      
      this.tableDataLoaded = true;
      this.clearState();
    }else{      
      this.tableScrollTop = 0;
      this.searchTxt = "";
      this.tableErrorMessage = '';
      this.errorMessage = '';
      this.tableDataLoaded = false;
      this.bucketNumber = 0;
      this.complianceTableData = [];
      this.getData();
      
    }
    this.getDistributionBySeverity();
    this.getPacmanIssues();
    this.getAssetsCountData({});
    this.getComplianceData();
  }

  changeFilterType(value) {
    try {
      this.currentFilterType = _.find(this.filterTypeOptions, {
        optionName: value.value,
      });
      this.issueFilterSubscription = this.issueFilterService
        .getFilters(
          {
            ag: this.selectedAssetGroup,
          },
          environment.base +
          this.utils.getParamsFromUrlSnippet(this.currentFilterType.optionURL)
            .url,
          "GET"
        )
        .subscribe((response) => {
          this.filterTagOptions = response[0].response;
          this.filterTagLabels = _.map(this.filterTagOptions, "name");
        });
    } catch (error) {
      this.errorMessage = this.errorHandling.handleJavascriptError(error);
      this.logger.log("error", error);
    }
  }

  changeFilterTags(value) {
    try {
      if (this.currentFilterType) {
        const filterTag = _.find(this.filterTagOptions, { name: value.value });
        this.utils.addOrReplaceElement(
          this.filters,
          {
            typeName: this.currentFilterType.optionName,
            typeValue: this.currentFilterType.optionValue,
            tagName: filterTag.name,
            tagValue: filterTag["id"],
            key: this.currentFilterType.optionName,
            value: filterTag.name,
          },
          (el) => {
            return el.key === this.currentFilterType.optionName;
          }
        );
        this.updateComponent();
      }
      this.utils.clickClearDropdown();
    } catch (error) {
      this.errorMessage = this.errorHandling.handleJavascriptError(error);
      this.logger.log("error", error);
    }
  }

  deleteFilters(event?) {
    /* TODO: Needs to follow the same thing as vulnerability,
        updating component and updating compliance observable should be independent */
    try {
      if (!event) {
        this.filters = [];
      } else {
        if (event.clearAll) {
          this.filters = [];
        } else {
          this.filters.splice(event.index, 1);
        }
        this.updateComponent();
      }
    } catch (error) {}
  }

  getFormattedDate(date: Date){
    const offset = date.getTimezoneOffset()
    let formattedDate = new Date(date.getTime() - (offset*60*1000)).toISOString().split('T')[0];
    return formattedDate;
  }

  getAssetsCountData(queryObj) {
    if(!this.selectedAssetGroup){
      return;
    }
    if(queryObj.from){
      this.graphFromDate = queryObj.from;
    }

    if(queryObj.to){
      this.graphToDate = queryObj.to;
    }
    const queryParams = {
      ag: this.selectedAssetGroup,
      domain: this.selectedDomain,
      from: this.getFormattedDate(this.graphFromDate),
      to: this.getFormattedDate(this.graphToDate)
    };

    this.totalAssetsCountDataError = '';
    this.totalAssetsCountData = [];

    try {
        this.multilineChartService.getAssetTrendData(queryParams).subscribe(response => {
            this.totalAssetsCountData = this.massageAssetTrendGraphData(response[0]);
            if(this.totalAssetsCountData.length==0){
                this.totalAssetsCountDataError = 'noDataAvailable';
            }
        });
    } catch (error) {
        this.totalAssetsCountDataError = "apiResponseError";
        this.logger.log("error", error);
    }
  }

  private getComplianceData() {    
    if(!this.selectedAssetGroup || !this.selectedDomain){
      return;
    }
    const queryParams = {
      ag: this.selectedAssetGroup,
      domain: this.selectedDomain,
    };

    const overallComplianceUrl = environment.overallCompliance.url;
    const overallComplianceMethod = environment.overallCompliance.method;
    this.overallComplianceService
      .getOverallCompliance(
        queryParams,
        overallComplianceUrl,
        overallComplianceMethod
      )
      .subscribe((response) => {  
        try {
          if(response[0].error){
            throw response[0];
          }
          this.complianceDataError = ''
          this.complianceData = [
            { class: "", title: "Security", val: "NR" },
            { class: "", title: "Cost", val: "NR" },
            { class: "", title: "Operations", val: "NR" },
            { class: "", title: "Tagging", val: "NR" },
          ];
          response[0].data.forEach((element) => {
            let category = element[1]["title"].toLowerCase();
            let index;
            switch(category){
              case "security":
                index = 0;
                break;
              case "cost":
                index = 1;
                break;
              case "operations":
                index = 2;
                break;
              case "tagging":
                index = 3;
                break;
            }
              this.complianceData[index].val = element[1]["val"];

            if (element[1]["val"] <= 40) {
              this.complianceData[index].class = "red";
            } else if (element[1]["val"] <= 75) {
              this.complianceData[index].class = "or";
            } else {
              this.complianceData[index].class = "gr";
            }
          });          
          if(this.complianceData.length==0){
            this.complianceDataError = 'noDataAvailable';
          }          
        } catch (error) {
          this.complianceDataError = 'apiResponseError';
          this.logger.log("error", error);
        }
      });
  }

  getData() {
    if(!this.selectedAssetGroup || !this.selectedDomain){
      return;
    }
    const filters = this.utils.arrayToObject(
      this.filters,
      "typeValue",
      "tagValue"
    );
    filters["domain"] = this.selectedDomain;

    const payload = {
      ag: this.selectedAssetGroup,
      filter: filters,
      from: this.bucketNumber * this.paginatorSize,
      searchtext: this.searchTxt,
      size: 0,
    };

    this.tableErrorMessage = '';
    const complianceTableUrl = environment.complianceTable.url;
    const complianceTableMethod = environment.complianceTable.method;
    this.complianceTableSubscription = this.commonResponseService
      .getData(complianceTableUrl, complianceTableMethod, payload, {})
      .subscribe(
        (response) => {
          this.totalRows = response.total;
          try {
            this.complianceTableData = this.massageData(response.data.response);            
            this.tableDataLoaded = true;
            if (this.complianceTableData.length === 0) {
              this.totalRows = 0;
              this.tableErrorMessage = 'noDataAvailable';
            }
            if (response.hasOwnProperty("total")) {
              this.totalRows = response.data.total;
            } else {
              this.totalRows = this.complianceTableData.length;
            }
          } catch (e) {
            this.tableDataLoaded = true;
            this.tableErrorMessage = this.errorHandling.handleJavascriptError(e);
          }
        },
        (error) => {
          this.tableDataLoaded = true;
          this.tableErrorMessage = "apiResponseError";
        }
      );
  }

  getRouteQueryParameters(): any {
    this.activatedRouteSubscription = this.activatedRoute.queryParams.subscribe(
      (params) => {
        if(this.selectedAssetGroup && this.selectedDomain){
          this.updateComponent();
        }
      }
    );
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
        newObj[elementnew] = DATA_MAPPING[newObj[elementnew]]?DATA_MAPPING[newObj[elementnew]]: newObj[elementnew];
      });
      newObj["Compliance"] = newObj["assetsScanned"]==0?'NR':newObj["Compliance"]+"%";
      newData.push(newObj);
    });
    return newData;
  }

  goToDetails(event) {    
    const selectedRow = event.rowSelected;
    const data = event.data;
    const state = {
      totalRows: this.totalRows,
      data: data,
      headerColName: this.headerColName,
      direction: this.direction,
      whiteListColumns: this.whiteListColumns,
      bucketNumber: this.bucketNumber,
      searchTxt: this.searchTxt,
      tableScrollTop: event.tableScrollTop
      // filterText: this.filterText
    }
    this.storeState(state);
    try {
      this.workflowService.addRouterSnapshotToLevel(
        this.router.routerState.snapshot.root, 0, this.breadcrumbPresent,
      );
      let updatedQueryParams = { ...this.activatedRoute.snapshot.queryParams };
      updatedQueryParams["searchValue"] = undefined;
      this.router.navigate(["../policy-details", selectedRow["Rule ID"]], {
        relativeTo: this.activatedRoute,
        queryParams: updatedQueryParams,
        queryParamsHandling: "merge",
      });
    } catch (error) {
      this.errorMessage = this.errorHandling.handleJavascriptError(error);
      this.logger.log("error", error);
    }
  }


  callNewSearch(searchVal){    
    this.searchTxt = searchVal;
    this.isStatePreserved = false;
    this.tableDataLoaded = false;
    this.getData();  
  }

  calculateDate(_JSDate) {
    if (!_JSDate) {
      return "No Data";
    }
    const date = new Date(_JSDate);
    const year = date.getFullYear().toString();
    const month = date.getMonth() + 1;
    let monthString;
    if (month < 10) {
      monthString = "0" + month.toString();
    } else {
      monthString = month.toString();
    }
    const day = date.getDate();
    let dayString;
    if (day < 10) {
      dayString = "0" + day.toString();
    } else {
      dayString = day.toString();
    }
    return monthString + "-" + dayString + "-" + year;
  }

  handlePopClick() {
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
          "ruleCategory.keyword": this.ruleCatFilter,
        },
        from: 0,
        searchtext: this.searchTxt,
        size: this.totalRows,
      };

      const downloadUrl = environment.download.url;
      const downloadMethod = environment.download.method;

      this.downloadService.requestForDownload(
        queryParams,
        downloadUrl,
        downloadMethod,
        downloadRequest,
        "Policy Compliance Overview",
        this.totalRows
      );
    } catch (error) {
      this.logger.log("error", error);
    }
  }

  ngOnDestroy() {
    try {
      if (this.assetGroupSubscription) {
        this.assetGroupSubscription.unsubscribe();
      }
      if (this.onFilterChange) {
        this.onFilterChange.unsubscribe();
      }
      if (this.routeSubscription) {
        this.routeSubscription.unsubscribe();
      }
      if (this.complianceTableSubscription) {
        this.complianceTableSubscription.unsubscribe();
      }
      if (this.subscriptionDomain) {
        this.subscriptionDomain.unsubscribe();
      }
      if (this.issueFilterSubscription) {
        this.issueFilterSubscription.unsubscribe();
      }
      if (this.activatedRouteSubscription) {
        this.activatedRouteSubscription.unsubscribe();
      }
    } catch (error) {
      this.logger.log("error", error);
    }
  }

  onresize(event): void {
    this.breakpoint1 = event.target.innerWidth <= 1000 ? 2 : 4;
    this.breakpoint2 = event.target.innerWidth <= 800 ? 1 : 2;
    this.breakpoint3 = event.target.innerWidth <= 400 ? 1 : 1;
    this.breakpoint4 = event.target.innerWidth <= 400 ? 1 : 1;
  }
}
