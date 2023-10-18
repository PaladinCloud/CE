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

import { DecimalPipe } from '@angular/common';
import {
    AfterViewInit,
    Component,
    ElementRef,
    HostListener,
    NgZone,
    OnDestroy,
    OnInit,
    ViewChild
} from '@angular/core';
import { Router } from '@angular/router';
import * as d3 from 'd3';
import {
    ApexAxisChartSeries,
    ApexChart,
    ApexDataLabels,
    ApexPlotOptions,
    ApexTooltip,
} from 'ng-apexcharts';
import { Subscription } from 'rxjs';
import { WindowExpansionService } from 'src/app/core/services/window-expansion.service';
import { WorkflowService } from 'src/app/core/services/workflow.service';
import { AwsResourceTypeSelectionService } from 'src/app/pacman-features/services/aws-resource-type-selection.service';
import { ErrorHandlingService } from 'src/app/shared/services/error-handling.service';
import { LoggerService } from 'src/app/shared/services/logger.service';

export type ChartOptions = {
    series: ApexAxisChartSeries;
    chart: ApexChart;
    dataLabels: ApexDataLabels;
    plotOptions: ApexPlotOptions;
    tooltip: ApexTooltip;
};

export type ColorOptions = {
    from: number;
    to: number;
    color: string;
};

@Component({
    selector: 'app-asset-distribution',
    templateUrl: './asset-distribution.component.html',
    styleUrls: ['./asset-distribution.component.css'],
    providers: [LoggerService, ErrorHandlingService, DecimalPipe],
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
    chartOptions: ChartOptions;
    dataLoaded: boolean;
    treemapData = [];
    totalAssetCount = 0;
    totalAssetTypes = 0;
    dataPointIndex = 0;
    isCollapsed = false;
    searchText: string;
    @ViewChild('widgetContainer') widgetContainer: ElementRef;
    graphWidth = 0;
    graphHeight = 0;
    colorRanges: Array<ColorOptions> = [];
    pageTitle = 'Asset Distribution';
    breadcrumbArray = [];
    breadcrumbLinks = [];
    backButtonRequired = false;
    pageLevel = 0;

    colors: ColorOptions[] = [
        {
          from: 0,
          to: 100,
          color: '#6FAFE5',
        },
        {
          from: 100,
          to: 200,
          color: '#5F9DC8',
        },
        {
          from: 200,
          to: 300,
          color: '#4F8BBB',
        },
        {
          from: 300,
          to: 400,
          color: '#3F7AAA',
        },
        {
          from: 400,
          to: 500,
          color: '#2F689D',
        },
        {
          from: 500,
          to: 600,
          color: '#1F5790',
        },
        {
          from: 600,
          to: 700,
          color: '#105581',
        },
        {
          from: 700,
          to: 800,
          color: '#004372',
        },
        {
          from: 800,
          to: 900,
          color: '#003263',
        },
        {
          from: 900,
          to: 1000,
          color: '#002155',
        },
      ];

    isSortedByName = true;
    isSortedByAssetNo = true;

    ngOnInit() {
        const breadcrumbInfo = this.workflowService.getDetailsFromStorage()['level0'];

        if (breadcrumbInfo) {
            this.breadcrumbArray = breadcrumbInfo.map((item) => item.title);
            this.breadcrumbLinks = breadcrumbInfo.map((item) => item.url);
        }

        this.backButtonRequired = this.workflowService.checkIfFlowExistsCurrently(this.pageLevel);

        this.dataLoaded = false;
        this.buildTreeMap();
        this.getAwsResources();
        this.getAssetTypeAndCount();
    }

    constructor(
        private awsResourceTypeSelectionService: AwsResourceTypeSelectionService,
        private logger: LoggerService,
        private ngZone: NgZone,
        private numbersPipe: DecimalPipe,
        private router: Router,
        private windowExpansionService: WindowExpansionService,
        private workflowService: WorkflowService,
    ) {
        this.windowExpansionService.getExpansionStatus().subscribe(() => {
            this.setTreemap();
        });
    }

    ngAfterViewInit(): void {
        setTimeout(() => {
            this.setTreemap();
        }, 500);
    }

    @HostListener('window:resize', ['$event'])
    onWindowResize() {
        this.setTreemap();
    }

    setTreemap() {
        setTimeout(() => {
            this.graphWidth =
                parseInt(
                    window
                        .getComputedStyle(this.widgetContainer.nativeElement)
                        .getPropertyValue('width'),
                    10,
                ) - 40;
            this.graphHeight =
                parseInt(window.getComputedStyle(this.widgetContainer.nativeElement).height) - 40;
            d3.selectAll('.apexcharts-svg').remove();
            this.buildTreeMap();
        }, 300);
    }

    sortAssets(column: string) {
        if (column == 'col-2') {
            if (!this.isSortedByAssetNo) this.filteredResources.sort((a, b) => b.count - a.count);
            else {
                this.filteredResources.sort((a, b) => a.count - b.count);
            }
            this.isSortedByAssetNo = !this.isSortedByAssetNo;
        } else {
            if (!this.isSortedByName) {
                this.filteredResources.sort((a, b) =>
                    a.displayName.toLowerCase() > b.displayName.toLowerCase() ? 1 : -1,
                );
            } else {
                this.filteredResources.sort((a, b) =>
                    a.displayName.toLowerCase() > b.displayName.toLowerCase() ? -1 : 1,
                );
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
        const maxIndex = this.awsResources.length;

        if(maxIndex==1){
            this.treemapData = [{
                x: this.awsResources[0].displayName,
                y: 1
            }]
            return;
        }
        for (let i = 0; i < maxIndex; i++) {
            const obj = {
                x: this.awsResources[i].displayName,
                y: this.awsResources[i].count,
            };
            obj.x = this.awsResources[i].displayName.split(' ');
            this.treemapData.push(obj);
        }
        let maxVal = -1 , minVal = 100000;
        const values = this.treemapData.map(function (d) {
            maxVal = Math.max(maxVal,d.y);
            return d.y;
        });

        const sqrtScale = d3.scaleSqrt().domain([0, maxVal]).range([0, 20]); 


        for (let j = 0; j < this.treemapData.length; j++) {
            if (this.treemapData[j].y > 0) this.treemapData[j].y = sqrtScale(this.treemapData[j].y);
            if(minVal == 100000 && this.treemapData[j].y > 0){
                minVal = this.treemapData[j].y;
            }
        }
        maxVal = this.treemapData[maxIndex - 1].y;
        const diff = (maxVal-minVal) / 10;
        let from = minVal, to = minVal + diff;

        for (let i = 0; i < 10; i++) {
            this.colorRanges.push({
                from: from,
                to: to,
                color: this.colors[i].color,
            });
            from = to;
            to = to + diff;
            if(i==8){
                to = maxVal;
            }
        }
        
        this.buildTreeMap();
    }

    buildTreeMap() {
        this.chartOptions = {
            series: [
                {
                    data: this.treemapData,
                },
            ],
            tooltip: {
                custom: ({ dataPointIndex }) => {
                    const provider = this.awsResources[dataPointIndex].provider;
                    const AssetType = this.awsResources[dataPointIndex].displayName;
                    const AssetCount = this.awsResources[dataPointIndex].count;
                    return (
                        '<div class="tooltip-box">' +
                        '<div class=tooltip-header>' +
                        '<img class="asset-img" src="/assets/icons/' +
                        provider +
                        '-color.svg">' +
                        '<span class="asset-name">' +
                        AssetType +
                        '</span>' +
                        '</div>' +
                        '<span class="tooltip-assetCount">' +
                        this.numbersPipe.transform(AssetCount) +
                        '</span>' +
                        '</div>'
                    );
                },
            },
            chart: {
                width: this.graphWidth,
                height: this.graphHeight,
                type: 'treemap',
                events: {
                    click: (event, chartContext, Config) => {
                        this.dataPointIndex = Config.dataPointIndex;
                        const selectedTargetType = this.awsResources[this.dataPointIndex];
                        this.redirect(selectedTargetType);
                    },
                    dataPointMouseEnter: (event) => {
                        event.target.style.cursor = 'pointer';
                    },
                },
            },
            dataLabels: {
                enabled: true,
                offsetY: -6,
                style: {
                    fontSize: '10px',
                },
            },
            plotOptions: {
                treemap: {
                    enableShades: false,
                    distributed: true,
                    colorScale: {
                        ranges: this.colorRanges,
                    },
                },
            },
        };
    }

    getAssetTypeAndCount() {
        this.awsResourceTypeSelectionService.getAssetTypeCount().subscribe((data) => {
            this.totalAssetCount = data.totalassets;
            this.totalAssetTypes = data.assettype;
        });
    }

    redirect(tagetType: any) {
        const selectedTargetType = tagetType.type;
        const queryParams = {
            filter: '_entitytype.keyword=' + selectedTargetType,
            tempFilters: true
        };
        this.workflowService.addRouterSnapshotToLevel(
            this.router.routerState.snapshot.root,
            0,
            this.pageTitle,
        );
        this.ngZone.run(() => {
            this.router.navigate(['pl', 'assets', 'asset-list'], {
                queryParams: queryParams,
                queryParamsHandling: 'merge',
            });
        });
    }

    expandAssetTypeList() {
        this.isCollapsed = !this.isCollapsed;
        this.setTreemap();
    }

    getAwsResources() {
        this.resourceTypeSelectionSubscription = this.awsResourceTypeSelectionService
            .getAllAwsResources()
            .subscribe(
                (allAwsResources) => {
                    this.filteredResources = [];
                    this.treemapData = [];
                    this.awsResources = allAwsResources;
                    this.filteredResources = this.awsResources.slice();
                    this.filteredResources.sort((a, b) => b.count - a.count);
                    this.selectedResource = this.filteredResources[0];
                    this.setDataLoaded();
                    this.massageData();
                },
                (error) => {
                    this.logger.log('error', error);
                },
            );
    }

    navigateBack() {
        try {
            this.workflowService.goBackToLastOpenedPageAndUpdateLevel(
                this.router.routerState.snapshot.root,
            );
        } catch (error) {
            this.logger.log('error', error);
        }
    }

    ngOnDestroy(): void {
        this.resourceTypeSelectionSubscription.unsubscribe();
    }
}
