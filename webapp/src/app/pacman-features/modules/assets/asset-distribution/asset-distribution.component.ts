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

import { AfterViewInit, Component, ElementRef, HostListener, OnDestroy, OnInit, ViewChild } from "@angular/core";
import { Subscription } from "rxjs";
import * as d3 from 'd3';
import { AwsResourceTypeSelectionService } from "src/app/pacman-features/services/aws-resource-type-selection.service";
import { ErrorHandlingService } from "src/app/shared/services/error-handling.service";
import { LoggerService } from "src/app/shared/services/logger.service";
import {
  ApexAxisChartSeries,
  ApexDataLabels,
  ApexChart,
  ApexPlotOptions,
  ApexTooltip
} from "ng-apexcharts";
import { Router } from "@angular/router";
import { WindowExpansionService } from "src/app/core/services/window-expansion.service";
import { WorkflowService } from "src/app/core/services/workflow.service";

export type ChartOptions = {
  series: ApexAxisChartSeries;
  chart: ApexChart;
  dataLabels: ApexDataLabels;
  plotOptions: ApexPlotOptions;
  tooltip: ApexTooltip;
};

export type colorOptions = {
  from: number,
  to: number,
  color: string
}

@Component({
  selector: "app-asset-distribution",
  templateUrl: "./asset-distribution.component.html",
  styleUrls: ["./asset-distribution.component.css"],
  providers: [LoggerService, ErrorHandlingService]
})

export class AssetDistributionComponent implements OnInit, OnDestroy, AfterViewInit {

  resourceTypeSelectionSubscription: Subscription;
  filteredResources: any[];
  awsResources: any;
  selectedResource: any;
  dataSubscription: Subscription;
  awsResourceDetails: any;
  errorMessage: any;
  assetGroupSubscription: any;
  subscriptionDomain: any;
  public chartOptions: ChartOptions;
  dataLoaded: boolean;
  treemapData = [];
  agAndDomain: any;
  totalAssetCount = 0;
  totalAssetTypes = 0;
  dataPointIndex: any = 0;
  isCollapsed = false;
  searchText: string;
  @ViewChild('widgetContainer') widgetContainer: ElementRef;
  graphWidth: number = 0;
  graphHeight: number = 0;
  colorRanges: Array<colorOptions> = [];
  pageTitle = "Asset Distribution";

  colors: Array<colorOptions> = [
    {
      'from': 0,
      'to': 100,
      'color': "#6691D6"
    },
    {
      'from': 100,
      'to': 1000,
      'color': "#336CC9"
    },
    {
      'from': 1000,
      'to': 10000,
      'color': "#0047BB"
    },
    {
      'from': 10000,
      'to': 100000,
      'color': "#002B70"
    }
  ];

  isSortedByName = true;
  isSortedByAssetNo = true;


  ngOnInit() {
    this.dataLoaded = false;
    this.buildTreeMap();
    this.getAwsResources();
    this.getAssetTypeAndCount();
  }

  constructor(
    private awsResourceTypeSelectionService: AwsResourceTypeSelectionService,
    private logger: LoggerService,
    private router: Router,
    private windowExpansionService: WindowExpansionService,
    private workflowService: WorkflowService)
  {
    this.windowExpansionService.getExpansionStatus().subscribe((countMap: any) => {
      this.setTreemap();
    });
  }

  ngAfterViewInit(): void {
    setTimeout(() => {
      this.setTreemap();
    },500)
  }

  @HostListener('window:resize', ['$event'])
  onWindowResize(event: any) {
    this.setTreemap();
  }

  setTreemap() {
    setTimeout(() => {
      this.graphWidth = parseInt(window.getComputedStyle(this.widgetContainer.nativeElement).getPropertyValue('width'), 10) - 40;
      this.graphHeight = parseInt(window.getComputedStyle(this.widgetContainer.nativeElement).height) - 40;
      d3.selectAll(".apexcharts-svg").remove();
      this.buildTreeMap();
    }, 300);
  }

  sortAssets(column: string) {

    if (column == 'col-2') {
      if (!this.isSortedByAssetNo)
        this.filteredResources.sort((a, b) => b.count - a.count);
      else {
        this.filteredResources.sort((a, b) => a.count - b.count);
      }
      this.isSortedByAssetNo = !this.isSortedByAssetNo;
    }
    else {
      if (!this.isSortedByName) {
        this.filteredResources.sort((a, b) => (a.displayName.toLowerCase() > b.displayName.toLowerCase() ? 1 : -1));
      }
      else {
        this.filteredResources.sort((a, b) => (a.displayName.toLowerCase() > b.displayName.toLowerCase() ? -1 : 1));
      }
      this.isSortedByName = !this.isSortedByName;
    }
  }

  setDataLoaded() {
    this.dataLoaded = true;
  }

  massageData() {
    this.treemapData = [];
    this.colorRanges = [];

    this.awsResources.sort((a: any, b: any) => a.count - b.count);

    for (let i = 0; i < this.awsResources.length; i++) {
      const obj = {
        x: this.awsResources[i].displayName,
        y: this.awsResources[i].count
      }
      this.treemapData.push(obj);
    }

    let max = -1;
    for (let j = 0; j < this.treemapData.length && this.treemapData[j].y <= 100; j++) {
      max = Math.max(this.treemapData[j].y, max);
    }

    let countMap = [];
    let x = 1, prev_x = 0, curr_x, r = 0;
    let curr_min = 1, m = 0, k = 10;

    for (let j = 0; j < this.treemapData.length;) {
      while (this.treemapData[j].y < k * 10) {
        let val = this.treemapData[j].y;
        if (k <= 10) {
          curr_x = (val / max);
          countMap.push(curr_x);
        }
        else {
          if (m == 0) {
            curr_x = x + (x * 25 / 100);
            curr_min = val;
            m = 1;
          }
          x = ((val * curr_x) / curr_min);
          x = x - curr_x;
          x = x / 3;
          x = x + curr_x;
          countMap.push(x);
        }
        j++;
        if (j >= this.treemapData.length) {
          break;
        }
      }
      this.colorRanges.push({
        "from": prev_x + 0.001,
        "to": x + 0.001,
        "color": this.colors[r++].color
      })
      prev_x = x;
      m = 0;
      k = k * 10;
    }

    for (let j = 0; j < this.treemapData.length; j++) {
      this.treemapData[j].y = countMap[j];
    }
    this.buildTreeMap();
  }

  buildTreeMap() {
    this.chartOptions = {
      series: [
        {
          data: this.treemapData,
        }
      ],
      tooltip: {
        custom: ({ series, seriesIndex, dataPointIndex, w }) => {
          const provider = this.awsResources[dataPointIndex].provider;
          const AssetType = this.awsResources[dataPointIndex].displayName;
          const AssetCount = this.awsResources[dataPointIndex].count;
          return '<div class="tooltip-box">' +
            '<div class=tooltip-header>' +
            '<img class="asset-img" src="/assets/icons/' + provider + '-color.svg">' +
            '<span class="asset-name">' + AssetType + '</span>' +
            '</div>' +
            '<span class="tooltip-assetCount">' + AssetCount + '</span>' +
            '</div>'
        }
      },
      chart: {
        width: this.graphWidth,
        height: this.graphHeight,
        type: "treemap",
        events: {
          click: (event, chartContext, Config) => {
            this.dataPointIndex = Config.dataPointIndex;
            const selectedTargetType = this.awsResources[this.dataPointIndex];
            // this.redirect(selectedTargetType);
          }
        }
      },
      dataLabels: {
        enabled: true,
        offsetY: -3
      },
      plotOptions: {
        treemap: {
          enableShades: false,
          colorScale: {
            ranges: this.colorRanges
          }
        }
      }
    };
  }

  getAssetTypeAndCount() {
    this.awsResourceTypeSelectionService.getAssetTypeCount().subscribe(data => {
      this.totalAssetCount = data.totalassets;
      this.totalAssetTypes = data.assettype;
    })
  }

  redirect(tagetType: any) {
    const selectedTargetType = tagetType.type;
    const queryParams = {
      filter: "resourceType=" + selectedTargetType
    }
    this.workflowService.addRouterSnapshotToLevel(this.router.routerState.snapshot.root, 0, this.pageTitle);
    this.router.navigate(['pl/assets/asset-list'], {
      queryParams: queryParams,
      queryParamsHandling: 'merge'
    });
  }

  expandAssetTypeList() {
    this.isCollapsed = !this.isCollapsed;
    this.setTreemap();
  }

  getAwsResources() {
    this.resourceTypeSelectionSubscription = this.awsResourceTypeSelectionService.getAllAwsResources().subscribe(
      allAwsResources => {
        this.filteredResources = [];
        this.treemapData = [];
        this.awsResources = allAwsResources;
        this.filteredResources = this.awsResources.slice();
        this.filteredResources.sort((a, b) => b.count - a.count);
        this.selectedResource = this.filteredResources[0];
        this.setDataLoaded();
        this.massageData();
      },
      error => {
        this.logger.log('error', error);
      }
    );
  }

  ngOnDestroy(): void {
    this.resourceTypeSelectionSubscription.unsubscribe();
  }
}

