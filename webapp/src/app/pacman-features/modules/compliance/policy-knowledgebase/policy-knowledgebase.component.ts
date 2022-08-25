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

import { Component, OnDestroy, ViewChild, ElementRef, AfterViewInit, Renderer2 } from '@angular/core';
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

@Component({
  selector: 'app-policy-knowledgebase',
  templateUrl: './policy-knowledgebase.component.html',
  styleUrls: ['./policy-knowledgebase.component.css'],
  providers: [CommonResponseService, LoggerService, ErrorHandlingService]
})
export class PolicyKnowledgebaseComponent implements AfterViewInit, OnDestroy {
  pageTitle = 'Policies';
  selectedAssetGroup: string;
  selectedDomain: string;
  subscriptionToAssetGroup: Subscription;
  domainSubscription: Subscription;
  complianceTableSubscription: Subscription;
  assetTiles: any = ['Security', 'Governance'];
  assetTabName: any;
  selectedTabName = 'All';
  dataLoaded = false;
  searchTxt = '';
  knowledgebaseData: any = [];
  tabName: any = [];
  count = [];
  num = 0;
  selName: any = [];
  selectedTab = 0;
  selectedFilter = 0;
  selectedFilterName = '';
  typeObj;
  searchPassed = '';
  loaded = false;
  datacoming = false;
  seekdata = false;
  errorMessage: any = '';
  urlToRedirect: any = '';
  public agAndDomain = {};
  currentPageLevel = 0;
  headerColName;
  direction;
  showSearchBar = true;
  showAddRemoveCol = true;
  columnWidths = {name: 3, provider: 1, severity: 1, ruleCategory: 1, resourcetType: 1};
  columnNamesMap = {name: "Policy Name", provider: "Cloud Type", severity:"Severity", ruleCategory: "Category", resourcetType: "Asset Type"}
  columnsSortFunctionMap = {
    severity: (a, b, isAsc) => {
      let severeness = {"low":1, "medium":2, "high":3, "critical":4}
      return (severeness[a.severity] < severeness[b.severity] ? -1 : 1) * (isAsc ? 1 : -1);
    },
  };

  @ViewChild('pkInp') pkInp: ElementRef;

  constructor(private assetGroupObservableService: AssetGroupObservableService,
    private router: Router,
    private activatedRoute: ActivatedRoute,
    private commonResponseService: CommonResponseService,
    private logger: LoggerService,
    private errorHandling: ErrorHandlingService,
    private workflowService: WorkflowService,
    private domainObservableService: DomainTypeObservableService,
    private routerUtilityService: RouterUtilityService) {

      this.headerColName = this.activatedRoute.snapshot.queryParams.headerColName;
      this.direction = this.activatedRoute.snapshot.queryParams.direction;
      // this.bucketNumber = this.activatedRoute.snapshot.queryParams.bucketNumber || 0;
      this.searchPassed = this.activatedRoute.snapshot.queryParams.searchValue || '';

    this.subscriptionToAssetGroup = this.assetGroupObservableService.getAssetGroup().subscribe(assetGroupName => {
      this.selectedAssetGroup = assetGroupName;
      this.agAndDomain['ag'] = this.selectedAssetGroup;
    });
    this.domainSubscription = this.domainObservableService.getDomainType().subscribe(domain => {  
      this.selectedDomain = domain;
      this.agAndDomain['domain'] = this.selectedDomain;
      this.updateComponent();
    });
    this.currentPageLevel = this.routerUtilityService.getpageLevel(this.router.routerState.snapshot.root);
  }

  handleHeaderColNameSelection(event){
    this.headerColName = event.headerColName;
    this.direction = event.direction;
    this.getUpdatedUrl();
  }

  getUpdatedUrl(){
    let updatedQueryParams = {};
    updatedQueryParams = {
      headerColName: this.headerColName,
      direction : this.direction,
      // bucketNumber : this.bucketNumber,
      searchValue: this.searchPassed
    }

    this.router.navigate([], {
      relativeTo: this.activatedRoute,
      queryParams: updatedQueryParams,
      queryParamsHandling: 'merge',
    });
  }

  callNewSearch(searchVal){
    this.searchPassed = searchVal;
    this.getUpdatedUrl();
  }

  ngAfterViewInit() {

  }

  updateComponent() {
    this.loaded = false;
    this.datacoming = false;
    this.seekdata = false;
    this.knowledgebaseData = [];
    // this.typeObj = undefined;
    this.getData();
  }

  processData(data) {
    try {
      const getData = data;
      this.typeObj = {
        'All Policies': 0
      };
      for (let i = 0; i < getData.length; i++) {
        this.typeObj[getData[i].ruleCategory] = 0;
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
        this.typeObj[getData[i].severity] = 0;
      }
      this.typeObj[`Auto Fix`] = 0;
      delete this.typeObj[''];
      for (let i = 0; i < getData.length; i++) {
        this.typeObj['All Policies']++;
        if(getData[i].ruleCategory=="costOptimization"){
          this.typeObj["cost"]++;
        }else{
          this.typeObj[getData[i].ruleCategory]++;
        }
        this.typeObj[getData[i].severity]++;
        if (getData[i].autoFixEnabled === true) {
          this.typeObj['Auto Fix']++;
        }
      }

      let typeArr = [];
      typeArr = Object.keys(this.typeObj);
      // this.tabName = typeArr;
      this.tabName = ["All Policies", "security", "operations", "cost", "tagging"];
      this.selectedTabName = this.tabName[this.selectedTab];
    } catch (error) {
      this.logger.log('error', error);
    }
  }

  getData() {
    this.seekdata = false;
    this.dataLoaded = false;
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
            this.datacoming = true;
            this.knowledgebaseData = response.data.response;
            this.dataLoaded = true;
            const x = this;
            setTimeout(function () {
              x.loaded = true;
              if (x.pkInp) {
                x.pkInp.nativeElement.focus();
              }
            }, 200);
            this.processData(this.knowledgebaseData);
          } else {
            this.datacoming = false;
            this.dataLoaded = true;
            this.seekdata = true;
            this.errorMessage = 'noDataAvailable';
          }
        },
        error => {
          this.datacoming = false;
          this.dataLoaded = true;
          this.seekdata = true;
          this.errorMessage = 'apiResponseError';
        });
  }

  /*
    * this function is used to fetch the rule id and to navigate to the next page
    */

  gotoNextPage(tileData){
   let autofixEnabled = false;
    if ( tileData.autoFixEnabled) {
      autofixEnabled = true;
    }
    const ruleId = tileData.ruleId;
    try {
      this.workflowService.addRouterSnapshotToLevel(this.router.routerState.snapshot.root);
      let updatedQueryParams = {...this.activatedRoute.snapshot.queryParams};
      updatedQueryParams["headerColName"] = undefined;
      updatedQueryParams["direction"] = undefined;
      // updatedQueryParams["bucketNumber"] = undefined;
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
