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
    AfterViewInit,
    Component,
    ElementRef,
    HostListener,
    NgZone,
    OnDestroy,
    OnInit,
    ViewChild,
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
    providers: [LoggerService, ErrorHandlingService],
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
            color: '#90CAF9',
        },
        {
            from: 100,
            to: 1000,
            color: '#64B5F6',
        },
        {
            from: 1000,
            to: 10000,
            color: '#1976D2',
        },
        {
            from: 10000,
            to: 100000,
            color: '#0D47A1',
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
        private ngZone: NgZone,
        private awsResourceTypeSelectionService: AwsResourceTypeSelectionService,
        private logger: LoggerService,
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

        for (let i = 0; i < maxIndex; i++) {
            const obj = {
                x: this.awsResources[i].displayName,
                y: this.awsResources[i].count,
            };
            obj.x = this.awsResources[i].displayName.split(' ');
            this.treemapData.push(obj);
        }

        const values = this.treemapData.map(function (d) {
            return d.y;
        });

        const quantile = d3.scaleQuantile().domain(values).range(d3.range(values.length));

        let max = -1;
        max = this.awsResources[maxIndex - 1].count;

        for (let j = 0; j < this.treemapData.length; j++) {
            if (this.treemapData[j].y > 0) this.treemapData[j].y = quantile(this.treemapData[j].y);
        }
        const maxVal = this.treemapData[maxIndex - 1].y;
        const diff = maxVal / 4;
        let from = 0,
            to = diff;
        let r = 0;

        for (let i = 0; i < 4; i++) {
            this.colorRanges.push({
                from: from,
                to: to,
                color: this.colors[r++].color,
            });
            from = to;
            to = to + diff;
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
                        AssetCount +
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
            filter: 'resourceType=' + selectedTargetType,
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
