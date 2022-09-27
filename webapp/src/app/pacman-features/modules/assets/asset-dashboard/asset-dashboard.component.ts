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

@Component({
  selector: 'app-asset-dashboard',
  templateUrl: './asset-dashboard.component.html',
  styleUrls: ['./asset-dashboard.component.css'],
  providers: [LoggerService, ErrorHandlingService, MultilineChartService]
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
  handleGraphIntervalSelection = (e) => {
    let date = new Date();
    e = e.toLowerCase();
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

    if(e != "all time" && e != "custom"){
      const offset = date.getTimezoneOffset()
      let fromDate = new Date(date.getTime() - (offset*60*1000)).toISOString().split('T')[0]
      queryParamObj["from"] = fromDate;
    }        
    this.getAssetsCountData(queryParamObj);
  }
  card = {
      id: 3,
      header: "Total Assets",
      footer: "View Asset Distribution",
      onSelectGraphInterval: this.handleGraphIntervalSelection,
    }

  constructor(
    private dataStore: DataCacheService,
    private router: Router,
    private logger: LoggerService,
    private workflowService: WorkflowService,
    private assetGroupObservableService: AssetGroupObservableService,
    private multilineChartService: MultilineChartService
  ) {
    this.config = CONFIGURATIONS;

    this.oss = this.config && this.config.optional && this.config.optional.general && this.config.optional.general.OSS;

    this.getAssetGroup();
  }

  ngOnInit() {

  }

  massageAssetTrendGraphData(graphData){
    let data = [];
    data.push({"key":"TotalAssetCount", "values":[]})
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

    return data;
  }

  private getAssetsCountData(queryObj) {
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

  getAssetGroup() {
    this.assetGroupObservableService.getAssetGroup().subscribe(
      assetGroupName => {
          this.backButtonRequired = this.workflowService.checkIfFlowExistsCurrently(this.pageLevel);
          this.assetGroupName = assetGroupName;
          this.getAssetsCountData({});
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
