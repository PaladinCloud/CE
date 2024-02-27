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
import { API_RESPONSE_ERROR, NO_DATA_AVAILABLE } from 'src/app/shared/constants/global';
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

    colors = ['#6FAFE5','#5F9DC8','#4F8BBB','#3F7AAA','#2F689D','#1F5790','#105581','#004372','#003263','#002155']
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

        if (maxIndex == 0) {
            this.errorMessage = NO_DATA_AVAILABLE;
            return;
        }
        if(maxIndex==1){
            this.treemapData = [{
                x: this.awsResources[0].displayName || this.awsResources[0].type,
                y: 1
            }]
            this.buildTreeMap();
            return;
        }
        for (let i = 0; i < maxIndex; i++) {
            const obj = {
                x: this.awsResources[i].displayName,
                y: this.awsResources[i].count,
            };
            obj.x = this.awsResources[i].displayName?.split(' ') || this.awsResources[i].type;
            this.treemapData.push(obj);
        }

       // Initialize variables to store the maximum and minimum values
        let maxDataValue, minDataValue;

        // Find the maximum value within the treemap data
        maxDataValue = this.treemapData[this.treemapData.length - 1].y;

        // Create a square root scale for data values, mapping them to a range of [0, 20]
        const sqrtScale = d3.scaleSqrt().domain([0, maxDataValue]).range([0, 20]);

        // Scale down the data values using the square root scale
        this.treemapData = this.treemapData.map(dataPoint => ({
        ...dataPoint,
        y: sqrtScale(dataPoint.y)
        }));

        // Filter out data points with a value of 0
        const filteredTreemapData = this.treemapData.filter(dataPoint => dataPoint.y !== 0);

        // Find the minimum and maximum values within the scaled data
        if (filteredTreemapData.length) {
            minDataValue = filteredTreemapData[0].y;
            maxDataValue = filteredTreemapData[filteredTreemapData.length - 1].y;

            // Calculate the difference between the maximum and minimum scaled values
            const valueRange = (maxDataValue - minDataValue) / this.colors.length;

            // Initialize variables to track the range and color for each value range
            let fromValue = minDataValue;
            let toValue = minDataValue + valueRange;

            // Generate color ranges based on the number of colors available
            for (let i = 0; i < this.colors.length; i++) {
                this.colorRanges.push({
                    from: fromValue,
                    to: toValue,
                    color: this.colors[i],
                });

                fromValue = toValue;
                toValue = toValue + valueRange;

                // Ensure the last color range covers the entire remaining value range
                if (i === this.colors.length - 2) {
                    toValue = maxDataValue;
                }
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
                    this.logger.log(API_RESPONSE_ERROR, error);
                    this.errorMessage = API_RESPONSE_ERROR;                    
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
