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

import {
  Component,
  OnInit,
  OnDestroy
} from '@angular/core';
import { Router } from '@angular/router';
import { DataCacheService } from '../../../../core/services/data-cache.service';
import { LoggerService } from '../../../../shared/services/logger.service';
import { ErrorHandlingService } from '../../../../shared/services/error-handling.service';
import { WorkflowService } from '../../../../core/services/workflow.service';
import { AssetGroupObservableService } from '../../../../core/services/asset-group-observable.service';
import { CONFIGURATIONS } from './../../../../../config/configurations';
import { MultilineChartService } from 'src/app/pacman-features/services/multilinechart.service';
import { environment } from 'src/environments/environment';
import { CommonResponseService } from 'src/app/shared/services/common-response.service';
import { AssetTilesService } from 'src/app/core/services/asset-tiles.service';
import { FetchResourcesService } from 'src/app/pacman-features/services/fetch-resources.service';

@Component({
  selector: 'app-asset-dashboard',
  templateUrl: './asset-dashboard.component.html',
  styleUrls: ['./asset-dashboard.component.css'],
  providers: [LoggerService, ErrorHandlingService, MultilineChartService, FetchResourcesService]
})
export class AssetDashboardComponent implements OnInit, OnDestroy {

  pageTitle = 'Overview';
  showNotif = false;
  beepCount = 0;
  public errorMessage: any;
  urlToRedirect: any = '';
  public pageLevel = 0;
  public backButtonRequired;
  public config;
  public oss;
  assetGroupName;
  domainName;
  totalAssetsCountData = [];
  totalAssetsCountDataError = '';
  dataSubscription;
  
  card = {
      id: 3,
      header: "Total Assets",
    }

    tiles = [
      {
        mainContent: {
          title: "Total Assets",
          count: 0,
          image: "total-assets-icon"
        },
        subContent: [
          {
            title: "Asset Types",
            count: 0
          }
        ]
      },
      {
        mainContent: {
          title: "Exempted Assets",
          count: 0,
          image: "exempted-assets-icon"
        },
        subContent: [
          {
            title: "My Exemptions",
            count: 0,
          }
        ]
      },
      {
        mainContent: {
          title: "Tagged Assets",
          count: 0,
          image: "category-tagging"
        },
        subContent: [
          {
            title: "Untagged Assets",
            count: 0
          }
        ]
      }
    ];
  years = [];
  allMonths = [];
  allDays = [];
  isCustomSelected: boolean = false;
  fromDate: Date = new Date(2022, 1, 1);
  toDate: Date = new Date(2200, 12, 31);

  constructor(
    private dataStore: DataCacheService,
    private router: Router,
    private logger: LoggerService,
    private workflowService: WorkflowService,
    private assetGroupObservableService: AssetGroupObservableService,
    private multilineChartService: MultilineChartService,
    private commonResponseService: CommonResponseService,
    private assetGroupsService: AssetTilesService,
    private fetchResourcesService: FetchResourcesService
  ) {
    this.config = CONFIGURATIONS;

    this.oss = this.config && this.config.optional && this.config.optional.general && this.config.optional.general.OSS;

    this.getAssetGroup();
  }

  ngOnInit() {}


  handleGraphIntervalSelection = (e) => {
    e = e.toLowerCase();
    if(e == "all time" || e == "custom"){
      if(e=="custom"){
        this.isCustomSelected = true;
        return;
      }
      this.dateIntervalSelected();
      return;
    }
    let date = new Date();
    this.isCustomSelected = false;
    let queryParamObj = {};
    switch(e){
      case "1 week":
        date.setDate(date.getDate() - 7);
        break;
      case "1 month":
        date.setMonth(date.getMonth() - 1);
        break;
      case "6 months":
        date.setMonth(date.getMonth() - 6);
        break;
      case "12 months":
        date.setFullYear(date.getFullYear() - 1);
        break;
    }

    this.dateIntervalSelected(date); 
  }

  getFormattedDate(date: Date){
    const offset = date.getTimezoneOffset()
    let formattedDate = new Date(date.getTime() - (offset*60*1000)).toISOString().split('T')[0];
    return formattedDate;
  }

  dateIntervalSelected(fromDate?, toDate?){
    let queryParamObj = {}
    if(fromDate){
      queryParamObj["from"] = this.getFormattedDate(fromDate);
    }
    if(toDate){
      queryParamObj["to"] = this.getFormattedDate(toDate);
    }    
    this.isCustomSelected = false;
    this.getAssetsCountData(queryParamObj);
  }

  getAssetsTileData(){
    const taggingSummaryUrl = environment.taggingSummary.url;
    const taggingSummaryMethod = environment.taggingSummary.method;

    let queryParams = {
      ag: this.assetGroupName,
    }

    try {
        this.commonResponseService.getData( taggingSummaryUrl, taggingSummaryMethod, {}, queryParams).subscribe(
          response => {
            this.tiles[2].mainContent.count = response.output.tagged;
            this.tiles[2].subContent[0].count = response.output.untagged;
          }
        )
    }catch(e){
        this.logger.log("error: ", e);
      }
          
  }

  massageAssetTrendGraphData(graphData){
    let data = [];
    data.push({"key":"TotalAssetCount", "values":[], "info": {}})
    graphData.trend.forEach(e => {
       data[0].values.push({
            'date':new Date(e.date),
            'value':e.totalassets,
            'zero-value':e.totalassets==0
        });
    })   
    data[0].values.sort(function(a,b){
        return new Date(a.date).valueOf() - new Date(b.date).valueOf();
    });

    data[0].info = {
    id: "AssetsCountTrend",
    showLegend: true,
    yAxisLabel: 'Total Assets',
    height: 320
  }

    return data;
  }

  public getAssetsCountData(queryObj) {
    if(!this.assetGroupName){
      return;
    }
    const queryParams = {
      ag: this.assetGroupName,
      ...queryObj
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

  getExemtedAssetsCount(){
    const exemptedAssetCountUrl = environment.exemptedAssetCount.url;
    const exemptedAssetCountMethod = environment.exemptedAssetCount.method;

    let queryParams = {
      ag: this.assetGroupName,
    }

    try {
        this.commonResponseService.getData( exemptedAssetCountUrl, exemptedAssetCountMethod, {}, queryParams).subscribe(
          response => {
            this.tiles[1].mainContent.count = response.exemptedAssetsCount;
            this.tiles[1].subContent[0].count = response.exemptedAssetsCount;
          }
        )
    }catch(e){
        this.logger.log("error: ", e);
      }
  }

  getResourceTypeAndCountAndRecommendation() {
    try {
      if (this.dataSubscription) {
        this.dataSubscription.unsubscribe();
      }
      const queryParams = {
        'ag': this.assetGroupName,
        'domain': this.domainName
      };

      const output = this.fetchResourcesService.getResourceTypesAndCount(queryParams);

      this.dataSubscription = output.subscribe(results => {          
          this.tiles[0].mainContent.count = results[1]["totalassets"];
          this.tiles[0].subContent[0].count = results[1]["assettype"];
        })
      }catch(e){
        this.logger.log("error: ", e);
      }
    }

    updateComponent(){
      this.getAssetsCountData({});
      this.getAssetsTileData();
      this.getExemtedAssetsCount();
      this.getResourceTypeAndCountAndRecommendation();
    }

  getAssetGroup() {
    this.assetGroupObservableService.getAssetGroup().subscribe(
      assetGroupName => {
          this.backButtonRequired = this.workflowService.checkIfFlowExistsCurrently(this.pageLevel);
          this.assetGroupName = assetGroupName;
          this.updateComponent();
    });
  }

  ngOnDestroy() {
    try {
      this.dataStore.set('urlToRedirect', this.urlToRedirect);
    } catch (error) {
      this.logger.log('error', '--- Error while unsubscribing ---');
    }
  }
}
