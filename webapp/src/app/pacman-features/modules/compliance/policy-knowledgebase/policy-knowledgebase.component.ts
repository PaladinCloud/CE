import { Component, OnDestroy, ViewChild, ElementRef, AfterViewInit, Renderer2, OnInit } from '@angular/core';
import { AssetGroupObservableService } from '../../../../core/services/asset-group-observable.service';
import { Subscription } from 'rxjs';
import { CommonResponseService } from '../../../../shared/services/common-response.service';
import { environment } from './../../../../../environments/environment';
import { ActivatedRoute, Router } from '@angular/router';
import { LoggerService } from '../../../../shared/services/logger.service';
import { ErrorHandlingService } from '../../../../shared/services/error-handling.service';
import { WorkflowService } from '../../../../core/services/workflow.service';
import { DomainTypeObservableService } from '../../../../core/services/domain-type-observable.service';
import { RouterUtilityService } from '../../../../shared/services/router-utility.service';
import { RefactorFieldsService } from 'src/app/shared/services/refactor-fields.service';
import { DATA_MAPPING } from 'src/app/shared/constants/data-mapping';
import { DownloadService } from 'src/app/shared/services/download.service';
import { TableStateService } from 'src/app/core/services/table-state.service';

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
  tableDataLoaded = false;
  searchTxt = '';
  breadcrumbPresent;
  typeObj;
  tabName: any = [];
  errorMessage: any = '';
  currentPageLevel = 0;
  headerColName;
  direction;
  showSearchBar = true;
  showAddRemoveCol = true;
  columnWidths = {'Title': 3, 'Cloud Type': 1, 'Severity': 1, 'Category': 1, 'Asset Type': 1};
  columnNamesMap = {name: "Title"};
  columnsSortFunctionMap = {
    Severity: (a, b, isAsc) => {
      let severeness = {"low":1, "medium":2, "high":3, "critical":4}
      return (severeness[a["Severity"]] < severeness[b["Severity"]] ? -1 : 1) * (isAsc ? 1 : -1);
    },
  };
  state: any = {};
  whiteListColumns;
  displayedColumns;
  tableScrollTop = 0;
  tableData = [];
  isStatePreserved = false;
  doLocalSearch = true; // should be removed once tiles data is available from backend

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
    private downloadService: DownloadService) {
      
      this.subscriptionToAssetGroup = this.assetGroupObservableService.getAssetGroup().subscribe(assetGroupName => {
        this.selectedAssetGroup = assetGroupName;
        this.searchTxt = "";
      });
      this.domainSubscription = this.domainObservableService.getDomainType().subscribe(domain => {  
        this.selectedDomain = domain;
      });
    }

    ngOnInit(): void {
      const state = this.tableStateService.getState("policyKnowledgebase") || {};
      
      this.searchTxt = this.activatedRoute.snapshot.queryParams.searchValue || '';
      this.displayedColumns = Object.keys(this.columnWidths);

      this.headerColName = state?.headerColName || '';
      this.direction = state?.direction || '';
      this.displayedColumns = Object.keys(this.columnWidths);
      this.whiteListColumns = state?.whiteListColumns || this.displayedColumns;
      this.searchTxt = state?.searchTxt || '';
      this.tableData = state?.data || [];
      this.tableDataLoaded = true;
      this.tableScrollTop = state?.tableScrollTop;

      if(this.tableData && this.tableData.length>0){
        this.isStatePreserved = true;
      }else{
        this.isStatePreserved = false;
      }

      this.breadcrumbPresent = "Policy"

    this.currentPageLevel = this.routerUtilityService.getpageLevel(this.router.routerState.snapshot.root);
    this.getRouteQueryParameters();
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
  }

  handleWhitelistColumnsChange(event){
    this.whiteListColumns = event;
  }

  handleSearchInColumnsChange(event){
    // this.state.searchInColumns = event;
  }

  handlePopClick() {
    const fileType = "csv";

    try {
      let queryParams;

      queryParams = {
        fileFormat: "csv",
        serviceId: 1,
        fileType: fileType,
      };

      const downloadRequest = {
        ag: this.selectedAssetGroup,
        from: 0,
        searchtext: this.searchTxt,
        size: this.typeObj['All Policies'],
      };

      const downloadUrl = environment.download.url;
      const downloadMethod = environment.download.method;

      this.downloadService.requestForDownload(
        queryParams,
        downloadUrl,
        downloadMethod,
        downloadRequest,
        "Policy Knowledgebase",
        this.typeObj['All Policies']
      );
    } catch (error) {
      this.logger.log("error", error);
    }
  }

  clearState(){
    this.tableStateService.clearState("policyKnowledgebase");
    this.isStatePreserved = false;
  }

  storeState(state){
    this.tableStateService.setState("policyKnowledgebase", state);
  }

  getUpdatedUrl(){
    let updatedQueryParams = {};
    updatedQueryParams = {
      // searchValue: this.searchTxt,
    }

    this.router.navigate([], {
      relativeTo: this.activatedRoute,
      queryParams: updatedQueryParams,
      queryParamsHandling: 'merge',
    });
  }

  callNewSearch(searchVal){
    if(!this.doLocalSearch){
      this.searchTxt = searchVal;
      // this.state.searchValue = searchVal;
      this.updateComponent();
    }else{
      this.searchTxt = searchVal;
    }
    // this.getUpdatedUrl();
  }

  ngAfterViewInit() {

  }

  updateComponent() {
    if(this.isStatePreserved){
      this.processData(this.tableData);
      this.tableDataLoaded = true;
      this.clearState();
    }else{
      this.tableDataLoaded = false;
      this.getData();      
    }
    // this.typeObj = undefined;
  }

  processData(data) {
    try {
      const getData = data;
      this.typeObj = {
        'All Policies': 0
      };
      for (let i = 0; i < getData.length; i++) {
        this.typeObj[getData[i].Category] = 0;
      }
      this.typeObj[`critical`] = 0;
      this.typeObj[`high`] = 0;
      this.typeObj[`medium`] = 0;
      this.typeObj[`low`] = 0;
      this.typeObj["cost"]=0;
      this.typeObj["operations"]=0;
      this.typeObj["security"]=0;
      this.typeObj["tagging"]=0;
      for (let i = 0; i < getData.length; i++) {
        this.typeObj[getData[i].Severity] = 0;
      }
      this.typeObj[`Auto Fix`] = 0;
      delete this.typeObj[''];
      for (let i = 0; i < getData.length; i++) {
        this.typeObj['All Policies']++;
        this.typeObj[getData[i].Category.toLowerCase()]++;
        this.typeObj[getData[i].Severity.toLowerCase()]++;
        if (getData[i].autoFixEnabled === true) {
          this.typeObj['Auto Fix']++;
        }
      }

      let typeArr = [];
      typeArr = Object.keys(this.typeObj);
      this.tabName = ["All Policies", "security", "operations", "cost", "tagging"];
    } catch (error) {
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
        newObj[elementnew] = DATA_MAPPING[newObj[elementnew]]?DATA_MAPPING[newObj[elementnew]]: newObj[elementnew];
      });
      newData.push(newObj);
    });
    return newData;
  }

  getData() {
    this.tableDataLoaded = false;
    if (this.complianceTableSubscription) {
      this.complianceTableSubscription.unsubscribe();
    }
    const payload = {
      'ag': this.selectedAssetGroup,
      'searchtext': this.searchTxt,
      'filter': {
        'domain': this.selectedDomain
      },
      'from': 0,
      'size': 10
    };

    const queryParams = {};
    const complianceTableUrl = environment.complianceTable.url;
    const complianceTableMethod = environment.complianceTable.method;
    this.complianceTableSubscription = this.commonResponseService.getData(
      complianceTableUrl, complianceTableMethod, payload, queryParams).subscribe(
        response => {
          if (response.data.response.length !== 0) {
            this.errorMessage = '';
            this.tableData = this.massageData(response.data.response);
            
            this.tableDataLoaded = true;
            this.processData(this.tableData);
          } else {
            this.tableDataLoaded = true;
            this.errorMessage = 'noDataAvailable';
          }
        },
        error => {
          this.tableDataLoaded = true;
          this.errorMessage = 'apiResponseError';
        });
  }

  /*
    * this function is used to fetch the rule id and to navigate to the next page
    */

  goToDetails(event) {
    // store in this function    
    const tileData = event.rowSelected;
    const data = event.data;
    const state = {
      data: data,
      headerColName: this.headerColName,
      direction: this.direction,
      whiteListColumns: this.whiteListColumns,
      searchTxt: this.searchTxt,
      tableScrollTop: event.tableScrollTop
      // filterText: this.filterText
    }
    this.storeState(state);
   let autofixEnabled = false;
    if ( tileData.autoFixEnabled) {
      autofixEnabled = true;
    }
    const ruleId = tileData["Rule ID"];
    try {
      this.workflowService.addRouterSnapshotToLevel(this.router.routerState.snapshot.root, 0, this.breadcrumbPresent);
      let updatedQueryParams = {...this.activatedRoute.snapshot.queryParams};
      updatedQueryParams["searchValue"] = undefined;
      this.router.navigate(
        ['pl', 'compliance', 'policy-knowledgebase-details', ruleId, autofixEnabled],
        { queryParams: updatedQueryParams,
          queryParamsHandling: 'merge' });
    } catch (error) {
      this.errorMessage = this.errorHandling.handleJavascriptError(error);
      this.logger.log('error', error);
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
