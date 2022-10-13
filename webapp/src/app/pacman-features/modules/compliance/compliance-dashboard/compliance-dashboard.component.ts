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
import { FetchResourcesService } from "src/app/pacman-features/services/fetch-resources.service";

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
    FetchResourcesService
  ],
})
export class ComplianceDashboardComponent implements OnInit {
  @ViewChild("widget") widgetContainer: ElementRef;

  pageTitle = "Overview";
  complianceDropdowns: any;
  searchDropdownData: any = {};
  selectedDD = "";
  currentObj: any = {};
  filterArr: any = [];
  subscriptionToAssetGroup: Subscription;
  selectedAssetGroup: string;
  outerArr: any = [];
  dataLoaded = false;
  errorMessage: any;
  showingArr: any;
  ruleCatFilter;
  allColumns: any = [];
  noMinHeight = false;
  paginatorSize = 20;
  totalRows = 0;
  currentBucket: any = [];
  bucketNumber = 0;
  firstPaginator = 1;
  popRows: any = ["Download Data"];
  lastPaginator: number;
  currentPointer = 0;
  seekdata = false;
  UI_pagination_mode = false;
  searchTxt = "";
  complianceTableData: any = [];
  currentFilterType;
  filterTypeLabels = [];
  filterTagLabels = [];
  filterTypeOptions: any = [];
  filters: any = [];
  filterTagOptions: any = [];
  returnType = false;
  selectedDomain: any = "";
  errorValue = 0;
  showGenericMessage = false;
  urlToRedirect: any = "";
  searchPassed = "";
  tableDataLoaded = false;
  tabArr: any = ["All", "Security", "Governance"];
  private assetGroupSubscription: Subscription;
  private onFilterChange: Subscription;
  private routeSubscription: Subscription;
  private complianceTableSubscription: Subscription;
  private issueFilterSubscription: Subscription;
  private downloadSubscription: Subscription;
  private activatedRouteSubscription: Subscription;
  private subscriptionDomain: Subscription;
  private queryParameters: any = {};
  public carouselState = "";
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
  headerColName;
  direction;
  complianceData = [];
  complianceDataError = '';
  assetsCountData = [];
  assetsCountDataError = '';

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

  navigateToAssetSummary = () => {
    this.router.navigate(["/pl/assets/asset-dashboard/"], {
      queryParamsHandling: "merge",
    });
  };

  violationCards = [
    { id: 1, name: "Critical", num: 0 },
    { id: 2, name: "High", num: 0 },
    { id: 3, name: "Medium", num: 0 },
    { id: 4, name: "Low", num: 0 },
  ];

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
      header: "Asset Summary",
      footer: "View all Assets",
      cardButtonAction: this.navigateToAssetSummary,
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
    private fetchResourcesService: FetchResourcesService
  ) {
    this.headerColName = this.activatedRoute.snapshot.queryParams.headerColName;
    this.direction = this.activatedRoute.snapshot.queryParams.direction;
    this.bucketNumber = this.activatedRoute.snapshot.queryParams.bucketNumber || 0;
    this.searchPassed = this.activatedRoute.snapshot.queryParams.searchValue || '';
    this.assetGroupSubscription = this.subscriptionToAssetGroup =
      this.assetGroupObservableService
        .getAssetGroup()
        .subscribe((assetGroupName) => {
          this.selectedAssetGroup = assetGroupName;
        });

    this.subscriptionDomain = this.domainObservableService
      .getDomainType()
      .subscribe((domain) => {
        this.selectedDomain = domain;
        this.updateComponent();
      });

    this.getRouteQueryParameters();
  }

  handleHeaderColNameSelection(event) {
    this.headerColName = event.headerColName;
    this.direction = event.direction;
    this.getUpdatedUrl();
  }

  getUpdatedUrl() {
    let updatedQueryParams = {};
    updatedQueryParams = {
      headerColName: this.headerColName,
      direction: this.direction,
      bucketNumber: this.bucketNumber,
      searchValue: this.searchPassed
    }

    this.router.navigate([], {
      relativeTo: this.activatedRoute,
      queryParams: updatedQueryParams,
      queryParamsHandling: 'merge',
    });
  }

  getPacmanIssues() {
    if(!this.queryParameters.ag){
      return;
    }
    if (this.dataSubscriber) {
      this.dataSubscriber.unsubscribe();
    }
    const queryParams = {
      ag: this.queryParameters.ag,
      domain: this.queryParameters.domain,
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
                this.violationCards[i].num =
                  this.pacmanIssues.severity[i][
                  Object.keys(this.pacmanIssues.severity[i])[0]
                  ];
                totalCount += this.violationCards[i].num;
                dataValue.push(this.violationCards[i].num);
              }
              this.fetchedViolations = true;
              this.policyDataError = '';
              if (dataValue.length > 0) {
                this.policyData = {
                  color: ["#D95140", "#FF8888", "#FFCFCF", "#F1D668"],
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
              this.errorMessage = this.errorHandling.handleJavascriptError(e);
              this.getErrorValues();
            }
          },
          (error) => {
            this.errorMessage = error;
            this.getErrorValues();
          }
        );
    } catch (error) {
      this.errorMessage = this.errorHandling.handleJavascriptError(error);
      this.getErrorValues();
    }
  }

  getErrorValues(): void {
    this.loaded = true;
    this.error = true;
    this.seekdata = true;
  }

  ngOnInit() {
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
    this.outerArr = [];
    this.searchTxt = "";
    this.ruleCatFilter = undefined;
    this.currentBucket = [];
    this.noMinHeight = false;
    // this.bucketNumber = 0;
    this.firstPaginator = 1;
    this.complianceTableData = [];
    // this.currentPointer = 0;
    this.tableDataLoaded = false;
    this.errorValue = 0;
    this.dataLoaded = false;
    this.seekdata = false;
    this.showGenericMessage = false;
    this.assetsCountData = [];
    this.assetsCountDataError = '';
    this.complianceData = [];
    this.complianceDataError = '';
    this.policyDataError = ''
    this.getData();
    this.getPacmanIssues();
    this.getAssetsCountData();
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

  private getAssetsCountData() {
    if(!this.queryParameters.ag){
      return;
    }
    const queryParams = {
      ag: this.queryParameters.ag,
      domain: this.queryParameters.domain,
    };
    this.fetchResourcesService
      .getAllResourceCounts(queryParams)
      .subscribe((results) => {
        try {          
          if(results.error){
            throw results;
          }
          this.assetsCountDataError = '';
          this.assetsCountData = [];

          for (let asset of results["assetcount"]) {
            this.assetsCountData.push({
              asset: asset["type"],
              count: asset["count"],
            });
          }

          this.assetsCountData.sort((a, b) => b.count - a.count);

          if(this.assetsCountData.length==0){
            this.assetsCountDataError = 'noDataAvailable';
          }
        } catch (error) {          
          this.assetsCountDataError = 'apiResponseError';
          this.logger.log("error", error);
        }
      });
  }

  private getComplianceData() {    
    if(!this.queryParameters.ag || !this.queryParameters.domain){
      return;
    }
    const queryParams = {
      ag: this.queryParameters.ag,
      domain: this.queryParameters.domain,
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
          this.complianceData = [];
          response[0].data.forEach((element) => {
            if (element[1]["val"] <= 40) {
              element[1]["class"] = "red";
            } else if (element[1]["val"] <= 75) {
              element[1]["class"] = "or";
            } else {
              element[1]["class"] = "gr";
            }
            this.complianceData.push(element[1]);
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
    if(!this.queryParameters.ag){
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

    this.errorValue = 0;
    const complianceTableUrl = environment.complianceTable.url;
    const complianceTableMethod = environment.complianceTable.method;
    this.complianceTableSubscription = this.commonResponseService
      .getData(complianceTableUrl, complianceTableMethod, payload, {})
      .subscribe(
        (response) => {
          this.showGenericMessage = false;
          try {
            this.errorValue = 1;
            this.complianceTableData = response.data.response;
            this.dataLoaded = true;
            this.seekdata = false;
            this.tableDataLoaded = true;
            if (this.complianceTableData.length === 0) {
              this.errorValue = -1;
              this.totalRows = 0;
            }
            if (response.hasOwnProperty("total")) {
              this.totalRows = response.data.total;
            } else {
              this.totalRows = this.complianceTableData.length;
              this.UI_pagination_mode = true;
            }

            this.firstPaginator = this.bucketNumber * this.paginatorSize + 1;
            this.lastPaginator =
              this.bucketNumber * this.paginatorSize + this.paginatorSize;

            this.currentPointer = this.bucketNumber;
            if (
              this.lastPaginator > this.totalRows ||
              !response.hasOwnProperty("total")
            ) {
              this.lastPaginator = this.totalRows;
            }

            const data = this.massageData(this.complianceTableData);
            this.currentBucket[this.bucketNumber] = data;
            this.processData(data);
          } catch (e) {
            this.errorValue = 0;
            this.outerArr = [];
            this.errorValue = -1;
            this.dataLoaded = true;
            this.seekdata = true;
            this.errorMessage = this.errorHandling.handleJavascriptError(e);
          }
        },
        (error) => {
          this.showGenericMessage = true;
          this.outerArr = [];
          this.errorValue = -1;
          this.dataLoaded = true;
          this.seekdata = true;
          this.errorMessage = "apiResponseError";
        }
      );
  }

  massageData(data) {
    for (let i = 0; i < data.length; i++) {
      data[i][`Policy Title`] = data[i].name;
      data[i][`Last Scanned`] = data[i].lastScan;
      data[i][`Compliance %`] = data[i].assetsScanned == 0 ? 'NA' : data[i].compliance_percent;
      data[i][`Policy Severity`] = data[i].severity;
      data[i][`Contribution %`] = data[i].contribution_percent;
      data[i][`Resource Type`] = data[i].resourcetType;
      data[i][`Assets Scanned`] = data[i].assetsScanned;
      data[i][`Rule ID`] = data[i].ruleId;
      data[i][`Rule Category`] = data[i].ruleCategory;

      delete data[i].name;
      delete data[i].lastScan;
      delete data[i].compliance_percent;
      delete data[i].severity;
      delete data[i].contribution_percent;
      delete data[i].resourcetType;
      delete data[i].assetsScanned;
      delete data[i].ruleId;
      delete data[i].ruleCategory;
    }
    return data;
  }

  processData(data) {
    try {
      let innerArr = {};
      const totalVariablesObj = {};
      let cellObj = {};
      this.outerArr = [];
      const getData = this.addCompliance(data);
      const getCols = Object.keys(getData[0]);
      for (let row = 0; row < getData.length; row++) {
        innerArr = {};
        for (let col = 0; col < getCols.length; col++) {
          if (getCols[col] && getCols[col].toLowerCase() === "compliance") {
            if (
              getData[row][getCols[col]] &&
              getData[row][getCols[col]].toLowerCase() === "full_compliance"
            ) {
              cellObj = {
                link: "",
                properties: {
                  color: "#000",
                  "justify-content": "center",
                },
                textProp: {
                  display: "none",
                },
                colName: getCols[col],
                imgProp: { height: "1.2em" },
                hasPreImg: true,
                imgLink: "../assets/icons/Compliant.svg",

                text: "Compliant",
                valText: 1,
              };
            } else if (
              getData[row][getCols[col]] &&
              getData[row][getCols[col]].toLowerCase() === "good_compliance"
            ) {
              cellObj = {
                link: "",
                properties: {
                  color: "#000",
                  "justify-content": "center",
                },
                textProp: {
                  display: "none",
                },
                colName: getCols[col],
                imgProp: { height: "1.2em" },
                hasPreImg: true,
                imgLink: "../assets/icons/good-compliance.svg",
                text: "Not Compliant",
                valText: 3,
              };
            } else {
              cellObj = {
                link: "",
                properties: {
                  color: "#000",
                  "justify-content": "center",
                },
                textProp: {
                  display: "none",
                },
                colName: getCols[col],
                imgProp: { height: "1.2em" },
                hasPreImg: true,
                imgLink: "../assets/icons/bad-compliance.svg",
                text: "Not Compliant",
                valText: 2,
              };
            }
          } else if (
            getCols[col] &&
            getCols[col].toLowerCase() === "policy title"
          ) {
            cellObj = {
              link: "true",
              properties: {
                "font-size": "1.04em",
                "text-shadow": "0.1px 0",
              },
              colName: getCols[col],
              hasPreImg: false,
              imgLink: "",
              text: getData[row][getCols[col]],
              valText: getData[row][getCols[col]],
            };
          } else if (
            getCols[col] &&
            getCols[col].toLowerCase() === "policy severity"
          ) {
            if (
              getData[row][getCols[col]] &&
              getData[row][getCols[col]].toLowerCase() === "low"
            ) {
              cellObj = {
                link: "",
                properties: {
                  color: "#000",
                  "text-transform": "capitalize",
                },
                colName: getCols[col],
                hasPreImg: false,
                imgLink: "",
                text: getData[row][getCols[col]],
                valText: 1,
              };
            } else if (
              getData[row][getCols[col]] &&
              getData[row][getCols[col]].toLowerCase() === "medium"
            ) {
              cellObj = {
                link: "",
                properties: {
                  color: "#000",
                  "text-transform": "capitalize",
                },
                colName: getCols[col],
                hasPreImg: false,
                imgLink: "",
                text: getData[row][getCols[col]],
                valText: 2,
              };
            } else if (
              getData[row][getCols[col]] &&
              getData[row][getCols[col]].toLowerCase() === "high"
            ) {
              cellObj = {
                link: "",
                properties: {
                  color: "#000",
                  "text-transform": "capitalize",
                },
                colName: getCols[col],
                hasPreImg: false,
                imgLink: "",
                valText: 3,
                text: getData[row][getCols[col]],
              };
            } else {
              cellObj = {
                link: "",
                properties: {
                  color: "#000",
                  "text-transform": "capitalize",
                },
                colName: getCols[col],
                hasPreImg: false,
                imgLink: "",
                text: getData[row][getCols[col]],
                valText: 4,
              };
            }
          } else if (
            getCols[col] &&
            getCols[col].toLowerCase() === "compliance %"
          ) {
            cellObj = {
              link: "",
              properties: {
                color: "#000",
                "font-size": "1.04em",
              },
              colName: getCols[col],
              hasPreImg: false,
              imgLink: "",
              valText: getData[row][getCols[col]],
              text: getData[row][getCols[col]] == 'NA' ? getData[row][getCols[col]] : getData[row][getCols[col]] + "%",
            };
          } else if (
            getCols[col] &&
            getCols[col].toLowerCase() === "last scanned"
          ) {
            cellObj = {
              link: "",
              properties: {
                color: "#000",
              },
              colName: getCols[col],
              hasPreImg: false,
              imgLink: "",
              valText: new Date(getData[row][getCols[col]]).getTime(),
              text: this.utils.calculateDateAndTime(getData[row][getCols[col]],true),
            };
          } else {
            cellObj = {
              link: "",
              properties: {
                color: "",
              },
              colName: getCols[col],
              hasPreImg: false,
              imgLink: "",
              valText: getData[row][getCols[col]],
              text: getData[row][getCols[col]],
            };
          }
          innerArr[getCols[col]] = cellObj;
          totalVariablesObj[getCols[col]] = "";
        }
        this.outerArr.push(innerArr);
      }

      if (this.outerArr.length > getData.length) {
        const halfLength = this.outerArr.length / 2;
        this.outerArr = this.outerArr.splice(halfLength);
      }

      this.allColumns = Object.keys(totalVariablesObj);
    } catch (error) {
      this.dataLoaded = true;
      this.seekdata = true;
      this.errorMessage = this.errorHandling.handleJavascriptError(error);
    }
  }

  addCompliance(data) {
    for (let i = 0; i < data.length; i++) {
      if (data[i]["Compliance %"] === 100) {
        data[i].compliance = "full_compliance";
      } else if (
        data[i]["Compliance %"] < 100 &&
        data[i]["Compliance %"] > 49
      ) {
        data[i].compliance = "bad_compliance";
      } else {
        data[i].compliance = "good_compliance";
      }
    }
    return data;
  }

  goToDetails(selectedRow) {
    try {
      this.workflowService.addRouterSnapshotToLevel(
        this.router.routerState.snapshot.root
      );
      let updatedQueryParams = { ...this.activatedRoute.snapshot.queryParams };
      updatedQueryParams["searchValue"] = undefined;
      this.router.navigate(["../policy-details", selectedRow.row["Rule ID"].text], {
        relativeTo: this.activatedRoute,
        queryParams: updatedQueryParams,
        queryParamsHandling: "merge",
      });
    } catch (error) {
      this.errorMessage = this.errorHandling.handleJavascriptError(error);
      this.logger.log("error", error);
    }
  }

  searchCalled(search) {
    this.searchTxt = search;
    if (this.searchTxt !== "") {
      this.searchPassed = this.searchTxt;
      this.getUpdatedUrl();
    }
  }

  callNewSearch() {
    this.searchPassed = this.searchTxt;
    this.getUpdatedUrl();
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

  getRouteQueryParameters(): any {
    this.activatedRouteSubscription = this.activatedRoute.queryParams.subscribe(
      (params) => {
        this.queryParameters = params;
        this.updateComponent();
      }
    );
  }

  handlePopClick(rowText) {
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
    this.breakpoint1 = event.target.innerWidth <= 800 ? 2 : 4;
    this.breakpoint2 = event.target.innerWidth <= 800 ? 1 : 2;
    this.breakpoint3 = event.target.innerWidth <= 400 ? 1 : 1;
    this.breakpoint4 = event.target.innerWidth <= 400 ? 1 : 1;
  }
}
