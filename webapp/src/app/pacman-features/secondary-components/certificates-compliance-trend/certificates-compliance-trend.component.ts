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

import { Component, OnInit, ViewEncapsulation, OnDestroy, ViewChild, ElementRef } from '@angular/core';
import { ComplianceOverviewService } from '../../services/compliance-overview.service';
import { Subscription } from 'rxjs';
import { AssetGroupObservableService } from '../../../core/services/asset-group-observable.service';
import { SelectComplianceDropdown } from '../../services/select-compliance-dropdown.service';
import { LoggerService } from '../../../shared/services/logger.service';
import { ErrorHandlingService } from '../../../shared/services/error-handling.service';
import { environment } from './../../../../environments/environment';
import { AutorefreshService } from '../../services/autorefresh.service';

@Component({
  selector: 'app-certificates-compliance-trend',
  templateUrl: './certificates-compliance-trend.component.html',
  styleUrls: ['./certificates-compliance-trend.component.css'],
  encapsulation: ViewEncapsulation.None,
  providers: [ ComplianceOverviewService , AutorefreshService ],
  // eslint-disable-next-line
  host: {
    '(window:resize)': 'onResize($event)'
  }
})
export class CertificatesComplianceTrendComponent implements OnInit, OnDestroy {

  @ViewChild('certificatesComplianceOverviewContainer') widgetContainer: ElementRef;

  private assetGroupSubscription: Subscription;
  private complianceDropdownSubscription: Subscription;
  private issuesSubscription: Subscription;

  private selectedAssetGroup: any = 'rebellion';
  private selectedComplianceDropdown: any = {
      'Target Types': '',
      'Applications': '',
      'Environments': ''
  };

  private graphWidth: any;
  private graphData: any;
  public dataLoaded: any = false;
  public error: any = false;
  private loading: any = false;
  public errorMessage: any = 'apiResponseError';
  private distributedFiltersObject: any = {};

  // Graph customization variables
  private yAxisLabel = 'Compliance %';
  private showGraphLegend = true;
  private showArea = false;
  private singlePercentLine = true;

  private autorefreshInterval;

  durationParams: any;
    autoRefresh: boolean;

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
      const element = document.getElementById('certificatesComplianceOverview');
      if (element) {
          this.graphWidth = parseInt((window.getComputedStyle(element, null).getPropertyValue('width')).split('px')[0], 10);
      }
  }


  getOverview() {
      try {

        if (this.issuesSubscription) {
          this.issuesSubscription.unsubscribe();
        }

          const complianceOverviewUrl = environment.certificatesComplianceTrend.url;
          const method = environment.certificatesComplianceTrend.method;

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

                      response.forEach(type => {
                          if (type.key.toLowerCase() === 'compliance_percent') {
                            this.graphData = [type];
                          }
                      });

                      if (response.length) {
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

      try {
          this.graphWidth = parseInt(window.getComputedStyle(this.widgetContainer.nativeElement, null).getPropertyValue('width'), 10);
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



