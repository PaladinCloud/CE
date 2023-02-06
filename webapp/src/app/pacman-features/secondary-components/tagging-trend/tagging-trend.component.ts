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
import { IssuesHistoryService } from '../../services/issues-history.service';
import { Subscription } from 'rxjs';
import { AssetGroupObservableService } from '../../../core/services/asset-group-observable.service';
import { SelectComplianceDropdown } from '../../services/select-compliance-dropdown.service';
import { environment } from '../../../../environments/environment';

@Component({
  selector: 'app-tagging-trend',
  templateUrl: './tagging-trend.component.html',
  styleUrls: ['./tagging-trend.component.css'],
  providers: [IssuesHistoryService],
  encapsulation: ViewEncapsulation.None,
  // eslint-disable-next-line
  host: {
    '(window:resize)': 'onResize($event)'
  }
})
export class TaggingTrendComponent implements OnInit, OnDestroy, AfterViewInit {

  @ViewChild('certificateHistoryContainer') widgetContainer: ElementRef;

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
    public dataLoaded:  any = false;
    public error: any = false;
    private loading: any = false;
    public errorMessage: any = 'apiResponseError';

    // Graph customization variables
    private yAxisLabel = 'Number of Tagging Policy Violations';
    private showGraphLegend = true;

    constructor(private issuesHistoryService: IssuesHistoryService,
                private assetGroupObservableService: AssetGroupObservableService,
                private selectComplianceDropdown: SelectComplianceDropdown) {

                  // Get latest asset group selected and re-plot the graph
                  this.assetGroupSubscription = this.assetGroupObservableService.getAssetGroup().subscribe(
                    assetGroupName => {
                        this.selectedAssetGroup = assetGroupName;
                        this.init();
                  });

                  // Get latest targetType/Application/Environment
                  this.complianceDropdownSubscription = this.selectComplianceDropdown.getCompliance().subscribe(
                  complianceName => {
                      this.selectedComplianceDropdown = complianceName;
                  });

                }

    getIssues() {

      if (this.issuesSubscription) {
          this.issuesSubscription.unsubscribe();
        }

      const prevDate = new Date();
      prevDate.setMonth(prevDate.getMonth() - 1);
      let fromDay;
      fromDay = prevDate.toISOString().split('T')[0];
      const payload = {
          'ag': this.selectedAssetGroup,
          'frdt': fromDay,
          'policyId': '',
          'app': '',
          'env': ''
      };

      const url = environment.issueTrends.url;
      const method = environment.issueTrends.method;

      this.issuesSubscription = this.issuesHistoryService.getData(url, method, payload, {}).subscribe(
        response => {
          try {

              this.setDataLoaded();
              this.graphData = response;
              if  (this.graphData.length === 0) {
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
    }

    onResize() {
        const element = document.getElementById('taggingTrend');
        if (element) {
            this.graphWidth = parseInt((window.getComputedStyle(element, null).getPropertyValue('width')).split('px')[0], 10);
        }
    }

    getData() {
      this.getIssues();
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
    }

    ngAfterViewInit(): void {
      this.graphWidth = this.widgetContainer?parseInt(window.getComputedStyle(this.widgetContainer.nativeElement, null).getPropertyValue('width'), 10):700;
    }

    ngOnDestroy() {
      try {
        this.issuesSubscription.unsubscribe();
        this.assetGroupSubscription.unsubscribe();
        this.complianceDropdownSubscription.unsubscribe();
      } catch (error) {

      }
    }

}
