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

import { Component, OnInit, ViewEncapsulation, OnDestroy, ViewChild, ElementRef, AfterViewInit } from '@angular/core';
import { ComplianceOverviewService } from '../../services/compliance-overview.service';
import { Subscription } from 'rxjs';
import { AssetGroupObservableService } from '../../../core/services/asset-group-observable.service';
import { SelectComplianceDropdown } from '../../services/select-compliance-dropdown.service';
import { LoggerService } from '../../../shared/services/logger.service';
import { ErrorHandlingService } from '../../../shared/services/error-handling.service';
import { environment } from './../../../../environments/environment';
import { AutorefreshService } from '../../services/autorefresh.service';

@Component({
  selector: 'app-tagging-assets-trend',
  templateUrl: './tagging-assets-trend.component.html',
  styleUrls: ['./tagging-assets-trend.component.css'],
  providers: [ ComplianceOverviewService , AutorefreshService],
  encapsulation: ViewEncapsulation.None,
  // eslint-disable-next-line
  host: {
    '(window:resize)': 'onResize($event)'
  }
})
export class TaggingAssetsTrendComponent implements OnInit, OnDestroy, AfterViewInit {

  @ViewChild('taggingAssetsOverviewContainer') widgetContainer: ElementRef;

  private assetGroupSubscription: Subscription;
  private complianceDropdownSubscription: Subscription;
  private issuesSubscription: Subscription;

  private selectedAssetGroup: any = 'rebellion';
  durationParams: any;
  autoRefresh: boolean;
  private graphWidth: any;
  private graphData: any;
  public dataLoaded: any = false;
  public error: any = false;
  private loading: any = false;
  public errorMessage: any = 'apiResponseError';
  private distributedFiltersObject: any = {};
  private autorefreshInterval;

  constructor(private complianceOverviewService: ComplianceOverviewService,
              private assetGroupObservableService: AssetGroupObservableService,
              private selectComplianceDropdown: SelectComplianceDropdown,
              private autorefreshService: AutorefreshService,
              private logger: LoggerService, private errorHandling: ErrorHandlingService) {

        // Get latest asset group selected and re-plot the graph
        this.assetGroupSubscription = this.assetGroupObservableService.getAssetGroup().subscribe(
            assetGroupName => {
                this.selectedAssetGroup = assetGroupName;
                this.init();
            });

        // Get latest targetType/Application/Environment
        this.complianceDropdownSubscription = this.selectComplianceDropdown.getCompliance().subscribe(
            distributedFiltersObject => {
                this.distributedFiltersObject = distributedFiltersObject;
            });

  }

  onResize() {
      const element = document.getElementById('taggingAssetsOverview');
      if (element) {
          this.graphWidth = parseInt((window.getComputedStyle(element, null).getPropertyValue('width')).split('px')[0], 10);
      }
  }

  getOverview() {
      try {

        if (this.issuesSubscription) {
          this.issuesSubscription.unsubscribe();
        }

          const complianceOverviewUrl = environment.taggingComplianceTrend.url;
          const method = environment.taggingComplianceTrend.method;

          const prevDate = new Date();
          prevDate.setMonth(prevDate.getMonth() - 1);
          let fromDay;
          fromDay = prevDate.toISOString().split('T')[0];
          const queryParameters = {
              'ag': this.selectedAssetGroup,
              'from': fromDay,
              'filters': {}
          };

          this.issuesSubscription = this.complianceOverviewService.getWeeklyData(complianceOverviewUrl, method, queryParameters).subscribe(
              response => {
                  try {

                      this.graphData = [];
                      response.forEach(type => {
                        const key = type.key.toLowerCase();
                          if (key === 'total' || key === 'compliant') {
                            this.graphData.push(type);
                          }
                      });

                      if (this.graphData.length) {
                        this.setDataLoaded();
                      } else {
                        this.setError('noDataAvailable');
                      }

                  } catch (error) {
                      this.setError('jsError');
                  }
              },
              error => {
                  this.setError('apiResponseError');
              }
          );
      } catch (error) {
          this.setError('jsError');
      }
  }

  getData() {
      this.getOverview();
  }

  init() {
      this.setDataLoading();
      this.getData();
  }

  setDataLoaded() {
      this.dataLoaded = true;
      this.error = false;
      this.loading = false;
  }

  setDataLoading() {
      this.dataLoaded = false;
      this.error = false;
      this.loading = true;
  }

  setError(message?: any) {
      this.dataLoaded = false;
      this.error = true;
      this.loading = false;
      if (message) {
          this.errorMessage = message;
      }
  }

  ngOnInit() {

      this.durationParams = this.autorefreshService.getDuration();
      this.durationParams = parseInt(this.durationParams, 10);
      this.autoRefresh = this.autorefreshService.autoRefresh;

      const afterLoad = this;
        if (this.autoRefresh !== undefined) {
          if ((this.autoRefresh === true ) || (this.autoRefresh.toString() === 'true')) {

            this.autorefreshInterval = setInterval(function() {
              afterLoad.getData();
            }, this.durationParams);
          }
        }
  }

  ngAfterViewInit(){
      try {
          this.graphWidth = this.widgetContainer?parseInt(window.getComputedStyle(this.widgetContainer.nativeElement, null).getPropertyValue('width'), 10):700;
      } catch (error) {
          this.errorMessage = this.errorHandling.handleJavascriptError(error);
          this.setError(error);
      }
  }

  ngOnDestroy() {
      try {
          this.issuesSubscription.unsubscribe();
          this.assetGroupSubscription.unsubscribe();
          this.complianceDropdownSubscription.unsubscribe();
          clearInterval(this.autorefreshInterval);
      } catch (error) {
          this.logger.log('error', '--- Error while unsubscribing ---');
      }
  }

}

