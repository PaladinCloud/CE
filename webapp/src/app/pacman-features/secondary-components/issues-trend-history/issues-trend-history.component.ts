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
  ViewEncapsulation,
  OnDestroy,
  ViewChild,
  ElementRef,
} from "@angular/core";
import { IssuesHistoryService } from "../../services/issues-history.service";
import { Subscription } from "rxjs";
import { AssetGroupObservableService } from "../../../core/services/asset-group-observable.service";
import { SelectComplianceDropdown } from "../../services/select-compliance-dropdown.service";
import { environment } from "../../../../environments/environment";
import { AutorefreshService } from "../../services/autorefresh.service";
import { DomainTypeObservableService } from "../../../core/services/domain-type-observable.service";
import { ActivatedRoute, Router } from "@angular/router";

@Component({
  selector: "app-issues-trend-history",
  templateUrl: "./issues-trend-history.component.html",
  styleUrls: ["./issues-trend-history.component.css"],
  providers: [IssuesHistoryService, AutorefreshService],
  encapsulation: ViewEncapsulation.None,
  // tslint:disable-next-line:use-host-property-decorator
  host: {
    "(window:resize)": "onResize($event)",
  },
})
export class IssuesTrendHistoryComponent implements OnInit, OnDestroy {
  @ViewChild("issuesHistoryContainer") widgetContainer: ElementRef;

  private assetGroupSubscription: Subscription;
  private complianceDropdownSubscription: Subscription;
  subscriptionDomain: Subscription;
  selectedDomain: any;
  private issuesSubscription: Subscription;

  private selectedAssetGroup: any = "rebellion";
  private selectedComplianceDropdown: any = {
    "Target Types": "",
    Applications: "",
    Environments: "",
  };

  private graphWidth: any;
  private graphData: any;
  private dataLoaded: any = false;
  private error: any = false;
  private loading: any = false;
  private errorMessage: any = "jsError";
  private distributedFiltersObject: any = {};

  // Graph customization variables
  private yAxisLabel = "Violations";
  private showGraphLegend = true;
  private showArea = true;
  private autorefreshInterval;

  durationParams: any;
  autoRefresh: boolean;
  subtractGraphWidthBy = 50;

  constructor(
    private router: Router,
    private activatedRoute: ActivatedRoute,
    private issuesHistoryService: IssuesHistoryService,
    private assetGroupObservableService: AssetGroupObservableService,
    private selectComplianceDropdown: SelectComplianceDropdown,
    private autorefreshService: AutorefreshService,
    private domainObservableService: DomainTypeObservableService
  ) {
    // Get latest asset group selected and re-plot the graph
    this.assetGroupSubscription = this.assetGroupObservableService
      .getAssetGroup()
      .subscribe((assetGroupName) => {
        this.selectedAssetGroup = assetGroupName;
      });

    this.subscriptionDomain = this.domainObservableService
      .getDomainType()
      .subscribe((domain) => {
        this.selectedDomain = domain;
        this.init();
      });

    // Get latest targetType/Application/Environment
    this.complianceDropdownSubscription = this.selectComplianceDropdown
      .getCompliance()
      .subscribe((distributedFiltersObject) => {
        this.distributedFiltersObject = distributedFiltersObject;
      });
  }

  closeOverallComplianceTrendModal(value: String) {
    const navigationParams = {
      relativeTo: this.activatedRoute.parent, // <-- Parent activated route
    };

    const agValue = value ? value : "";

    if (agValue) {
      navigationParams["queryParams"] = { ag: agValue };
    } else {
      navigationParams["queryParamsHandling"] = "merge";
    }

    this.router.navigate(
      [
        // No relative path pagination
        {
          outlets: {
            modal: null,
          },
        },
      ],
      navigationParams
    );
  }

  onResize() {
    const element = document.getElementById("issuesHistory");
    if (element) {
      this.graphWidth =
        parseInt(
          window
            .getComputedStyle(element, null)
            .getPropertyValue("width")
            .split("px")[0],
          10
        ) - this.subtractGraphWidthBy;
    }
  }

  getIssues() {
    try {
      if (this.issuesSubscription) {
        this.issuesSubscription.unsubscribe();
      }
      const url = environment.issueOverviewTrend.url;
      const method = environment.issueOverviewTrend.method;
      const prevDate = new Date();
      prevDate.setMonth(prevDate.getMonth() - 1);
      let fromDay;
      fromDay = prevDate.toISOString().split("T")[0];

      const payload = {
        ag: this.selectedAssetGroup,
        filters: {
          domain: this.selectedDomain,
        },
        from: fromDay,
      };

      this.issuesSubscription = this.issuesHistoryService
        .getData(url, method, payload, {})
        .subscribe(
          (response) => {
            try {
              this.setDataLoaded();
              this.graphData = response;
              if (
                this.graphData.constructor.name === "Object" ||
                this.graphData.length === 0
              ) {
                this.setError("noDataAvailable");
              }
            } catch (error) {
              this.setError("jsError");
            }
          },
          (error) => {
            this.setError("apiResponseError");
          }
        );
    } catch (error) {
      this.setError("jsError");
    }
  }

  getData() {
    this.getIssues();
  }

  init() {
    if (this.issuesSubscription) {
      this.issuesSubscription.unsubscribe();
    }
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
      if (this.autoRefresh === true || this.autoRefresh.toString() === "true") {
        this.autorefreshInterval = setInterval(function () {
          afterLoad.getData();
        }, this.durationParams);
      }
    }

    try {
      this.graphWidth =
        parseInt(
          window
            .getComputedStyle(this.widgetContainer.nativeElement, null)
            .getPropertyValue("width"),
          10
        ) - this.subtractGraphWidthBy;
    } catch (error) {
      this.setError("jsError");
    }
    // this.init();
  }

  ngOnDestroy() {
    try {
      this.issuesSubscription.unsubscribe();
      this.subscriptionDomain.unsubscribe();
      this.assetGroupSubscription.unsubscribe();
      this.complianceDropdownSubscription.unsubscribe();
      clearInterval(this.autorefreshInterval);
    } catch (error) {}
  }
}
