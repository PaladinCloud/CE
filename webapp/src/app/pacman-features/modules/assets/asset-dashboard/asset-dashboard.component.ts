import { Component, OnInit, OnDestroy, ViewChild, AfterViewInit } from '@angular/core';
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
import { AwsResourceTypeSelectionService } from 'src/app/pacman-features/services/aws-resource-type-selection.service';
import { MatMenuTrigger } from '@angular/material/menu';
import { Subscription } from 'rxjs';
import { OverviewTile } from 'src/app/shared/components/molecules/overview-tile/overview-tile.component';
import { TourService } from 'src/app/core/services/tour.service';

@Component({
    selector: 'app-asset-dashboard',
    templateUrl: './asset-dashboard.component.html',
    styleUrls: ['./asset-dashboard.component.css'],
    providers: [LoggerService, ErrorHandlingService, MultilineChartService, FetchResourcesService],
})
export class AssetDashboardComponent implements OnInit, AfterViewInit, OnDestroy {
    @ViewChild('menuTrigger') matMenuTrigger: MatMenuTrigger;

    pageTitle = 'Asset Summary';
    showNotif = false;
    beepCount = 0;
    errorMessage: string;
    urlToRedirect = '';
    pageLevel = 0;
    backButtonRequired;
    config;
    oss;
    assetGroupName;
    domainName;
    totalAssetsCountData = [];
    totalAssetsCountDataError = '';
    dataSubscription: Subscription;
    trendDataSubscription: Subscription;
    taggedTileDataSubscription: Subscription;
    unTaggedTileDataSubscription: Subscription;
    exemptTileDataSubscription: Subscription;
    assetGroupSubscription: Subscription;
    domainSubscription: Subscription;

    graphHeight: number;

    card = {
        id: 3,
        header: 'Asset Trend',
    };

    tiles: OverviewTile[] = [
        {
            mainContent: {
                title: 'Total Assets',
                count: 0,
                image: 'total-assets-icon',
            },
            subContent: [{
                title: 'Asset Types',
                count: 0,
            }],
        },
        {
            mainContent: {
                title: 'Exempt Assets',
                count: 0,
                image: 'exempted-assets-icon',
            },
            subContent: [{
                title: 'Exempt Asset Types',
                count: 0,
            }],
        },
        {
            mainContent: {
                title: 'UnTagged Assets',
                count: 0,
                image: 'category-tagging',
            },
            subContent: [{
                title: 'Tagged Assets',
                count: 0,
            }],
        },
    ];

    selectedItem = 'All time';
    isCustomSelected = false;
    fromDate: Date = new Date(2022, 0, 1);
    toDate: Date = new Date();
    dataSubscriber: Subscription;

    constructor(
        private dataStore: DataCacheService,
        private router: Router,
        private logger: LoggerService,
        private workflowService: WorkflowService,
        private assetGroupObservableService: AssetGroupObservableService,
        private multilineChartService: MultilineChartService,
        private commonResponseService: CommonResponseService,
        private assetGroupsService: AssetTilesService,
        private fetchResourcesService: FetchResourcesService,
        private tourService: TourService,
        private awsResourceTypeSelectionService: AwsResourceTypeSelectionService,
    ) {
        this.config = CONFIGURATIONS;

        this.oss =
            this.config &&
            this.config.optional &&
            this.config.optional.general &&
            this.config.optional.general.OSS;

        this.getAssetGroup();
    }
    ngAfterViewInit(): void {
        this.tourService.setComponentReady();
    }

    ngOnInit() {
        this.graphHeight = 320;
    }

    ifCustomSelected() {
        if (this.selectedItem == 'Custom') {
            this.selectedItem = '';
        }
    }

    onDropdownClose() {
        if (this.selectedItem == '') {
            this.selectedItem = 'Custom';
        }
    }

    getFormattedDate(date: Date) {
        const offset = date.getTimezoneOffset();
        const formattedDate = new Date(date.getTime() - offset * 60 * 1000)
            .toISOString()
            .split('T')[0];
        return formattedDate;
    }

    getAssetsTileData() {
        const queryParams = {
            'ag': this.assetGroupName
    }

    const taggingSummaryUrl = environment.taggingSummary.url;
    const taggingSummaryMethod = environment.taggingSummary.method;

        try {
            this.dataSubscriber = this.commonResponseService.getData( taggingSummaryUrl, taggingSummaryMethod, {}, queryParams).subscribe(
            response => {
                try {
                    this.tiles[2].mainContent.count = response.output.untagged;
                    this.tiles[2].subContent[0].count = response.output.tagged;                    
                }catch (e) {
                  this.logger.log('jserror', e);
              }
              }, error => {
                this.logger.log("apiError", error);
            });
        } catch (error) {
          this.logger.log('jsError', error);
        } 
    }

    massageAssetTrendGraphData(graphData) {
        const data = [];
        data.push({ key: 'Total Assets', values: [], info: {} });
        graphData.trend.forEach((e) => {
            data[0].values.push({
                date: new Date(e.date),
                value: e.totalassets,
                'zero-value': e.totalassets == 0,
            });
        });
        data[0].values.sort(function (a, b) {
            return new Date(a.date).valueOf() - new Date(b.date).valueOf();
        });

        data[0].info = {
            id: 'AssetsCountTrend',
            showLegend: true,
            yAxisLabel: 'Total Assets',
            height: 320,
        };

        return data;
    }

    redirectTo(data: any) {
        this.workflowService.addRouterSnapshotToLevel(
            this.router.routerState.snapshot.root,
            0,
            this.pageTitle,
        );
        const tempQueryParam = {
            "tempFilters": true
        }
        if (data == 'Exempt Assets' || data == 'Exempt Asset Types') {
            const queryParams = {
                filter: 'exempted=true', ...tempQueryParam
            };
            this.router.navigate(['pl/assets/asset-list'], {
                queryParams: queryParams,
                queryParamsHandling: 'merge',
            });
        } else if (data == 'Total Assets') {
            this.router.navigate(['pl/assets/asset-list'], {
                queryParams: {...tempQueryParam},
                queryParamsHandling: 'merge',
            });
        } else if (data == 'Asset Types') {
            this.router.navigate(['pl/assets/asset-distribution'], {
                queryParams: {...tempQueryParam},
                queryParamsHandling: 'merge',
            });
        } else if (data == 'Tagged Assets' || data == 'UnTagged Assets') {
            const queryParams = {
                filter: data == 'Tagged Assets' ? 'tagged=true' : 'tagged=false',
                ...tempQueryParam
            };
            this.router.navigate(['pl/assets/asset-list'], {
                queryParams: queryParams,
                queryParamsHandling: 'merge',
            });
        }
    }

    getAssetsCountData(queryObj) {
        if (!this.assetGroupName) {
            return;
        }

        if (this.trendDataSubscription) {
            this.trendDataSubscription.unsubscribe();
        }
        if (queryObj.from) {
            this.fromDate = queryObj.from;
        }

        if (queryObj.to) {
            this.toDate = queryObj.to;
        }
        const queryParams = {
            ag: this.assetGroupName,
            domain: this.domainName,
            from: this.getFormattedDate(this.fromDate),
            to: this.getFormattedDate(this.toDate),
        };

        this.totalAssetsCountDataError = '';
        this.totalAssetsCountData = [];

        try {
            this.trendDataSubscription = this.multilineChartService
                .getAssetTrendData(queryParams)
                .subscribe(
                    (response) => {
                        this.totalAssetsCountData = this.massageAssetTrendGraphData(response[0]);
                        if (response[0].trend.length < 2) {
                            this.totalAssetsCountDataError = 'waitForData';
                        }
                    },
                    (error) => {
                        this.logger.log('error', error);
                        this.totalAssetsCountDataError = 'apiResponseError';
                    },
                );
        } catch (error) {
            this.totalAssetsCountDataError = 'apiResponseError';
            this.logger.log('error', error);
        }
    }

    getExemtedAssetsCount() {
        const exemptedAssetCountUrl = environment.exemptedAssetCount.url;
        const exemptedAssetCountMethod = environment.exemptedAssetCount.method;

        const queryParams = {
            ag: this.assetGroupName,
        };

        try {
            this.exemptTileDataSubscription = this.commonResponseService
                .getData(exemptedAssetCountUrl, exemptedAssetCountMethod, {}, queryParams)
                .subscribe((response) => {
                    this.tiles[1].mainContent.count = response.totalassets || 0;
                    this.tiles[1].subContent[0].count = response.assettype;
                });
        } catch (e) {}
    }

    getResourceTypeAndCountAndRecommendation() {
        try {
            if (this.dataSubscription) {
                this.dataSubscription.unsubscribe();
            }
            const queryParams = {
                ag: this.assetGroupName,
                domain: this.domainName,
            };

            this.fetchResourcesService.getResourceTypesAndCount(queryParams);

            this.awsResourceTypeSelectionService.getAssetTypeCount().subscribe((asset) => {
                this.tiles[0].mainContent.count = asset['totalassets'];
                this.tiles[0].subContent[0].count = asset['assettype'];
            });
        } catch (e) {
            this.logger.log(e, 'error');
        }
    }

    getAssetGroup() {
        this.assetGroupSubscription = this.assetGroupObservableService.getAssetGroup().subscribe((assetGroupName) => {
            this.backButtonRequired = this.workflowService.checkIfFlowExistsCurrently(
                this.pageLevel,
            );
            this.assetGroupName = assetGroupName;
            this.getAssetsCountData({
                from: this.fromDate,
                to: this.toDate,
            });
            this.getAssetsTileData();
            this.getExemtedAssetsCount();
            this.getResourceTypeAndCountAndRecommendation();
        });
    }

    ngOnDestroy() {
        try {
            if (this.trendDataSubscription) {
                this.trendDataSubscription.unsubscribe();
            }
            this.taggedTileDataSubscription.unsubscribe();
            this.unTaggedTileDataSubscription.unsubscribe();
            this.exemptTileDataSubscription.unsubscribe();
            this.assetGroupSubscription.unsubscribe();
            this.dataStore.set('urlToRedirect', this.urlToRedirect);
        } catch (error) {
            this.logger.log('error', '--- Error while unsubscribing ---');
        }
    }
}
