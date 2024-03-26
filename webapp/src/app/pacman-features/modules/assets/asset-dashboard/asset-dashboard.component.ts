// Import necessary Angular modules and services
import { Component, OnInit, OnDestroy, ViewChild, AfterViewInit } from '@angular/core';
import { Router } from '@angular/router';
import { Subject } from 'rxjs';
import { takeUntil } from 'rxjs/operators';

// Import necessary custom services and components
import { DataCacheService } from 'src/app/core/services/data-cache.service';
import { LoggerService } from 'src/app/shared/services/logger.service';
import { ErrorHandlingService } from 'src/app/shared/services/error-handling.service';
import { WorkflowService } from 'src/app/core/services/workflow.service';
import { AssetGroupObservableService } from 'src/app/core/services/asset-group-observable.service';
import { MultilineChartService } from 'src/app/pacman-features/services/multilinechart.service';
import { environment } from 'src/environments/environment';
import { CommonResponseService } from 'src/app/shared/services/common-response.service';
import { FetchResourcesService } from 'src/app/pacman-features/services/fetch-resources.service';
import { AwsResourceTypeSelectionService } from 'src/app/pacman-features/services/aws-resource-type-selection.service';
import { MatMenuTrigger } from '@angular/material/menu';
import { OverviewTile } from 'src/app/shared/components/molecules/overview-tile/overview-tile.component';
import { TourService } from 'src/app/core/services/tour.service';

// Constants for error handling
import { API_RESPONSE_ERROR, JS_ERROR } from 'src/app/shared/constants/global';
import {
    ASSET_TYPE,
    EXEMPT_ASSETS,
    EXEMPT_ASSET_TYPES,
    TAGGED_ASSETS,
    TOTAL_ASSETS,
    UNTAGGED_ASSETS,
} from 'src/app/shared/constants/asset-trend-graph';

@Component({
    selector: 'app-asset-dashboard',
    templateUrl: './asset-dashboard.component.html',
    styleUrls: ['./asset-dashboard.component.css'],
    providers: [LoggerService, ErrorHandlingService, MultilineChartService, FetchResourcesService],
})
export class AssetDashboardComponent implements OnInit, AfterViewInit, OnDestroy {
    @ViewChild('menuTrigger') matMenuTrigger: MatMenuTrigger;
    private destroy$: Subject<void> = new Subject<void>();

    // Properties for page title, URL redirection, page level, and back button requirement
    public pageTitle = 'Asset Summary';
    public urlToRedirect = '';
    public pageLevel = 0;
    public backButtonRequired;

    // Properties for asset group name, domain name, and overview tiles
    public assetGroupName;
    public domainName;
    public tiles: OverviewTile[] = [
        {
            mainContent: {
                title: TOTAL_ASSETS,
                count: 0,
                image: 'total-assets-icon',
            },
            subContent: [
                {
                    title: ASSET_TYPE,
                    count: 0,
                },
            ],
        },
        {
            mainContent: {
                title: EXEMPT_ASSETS,
                count: 0,
                image: 'exempted-assets-icon',
            },
            subContent: [
                {
                    title: EXEMPT_ASSET_TYPES,
                    count: 0,
                },
            ],
        },
        {
            mainContent: {
                title: UNTAGGED_ASSETS,
                count: 0,
                image: 'category-tagging',
            },
            subContent: [
                {
                    title: TAGGED_ASSETS,
                    count: 0,
                },
            ],
        },
    ];

    constructor(
        private dataStore: DataCacheService,
        private router: Router,
        private logger: LoggerService,
        private workflowService: WorkflowService,
        private assetGroupObservableService: AssetGroupObservableService,
        private commonResponseService: CommonResponseService,
        private fetchResourcesService: FetchResourcesService,
        private tourService: TourService,
        private awsResourceTypeSelectionService: AwsResourceTypeSelectionService,
    ) {}

    ngAfterViewInit(): void {
        this.tourService.setComponentReady();
    }

    ngOnInit() {
        this.getAssetGroup();
    }

    // Method to format date
    getFormattedDate(date: Date) {
        const offset = date.getTimezoneOffset();
        const formattedDate = new Date(date.getTime() - offset * 60 * 1000)
            .toISOString()
            .split('T')[0];
        return formattedDate;
    }

    // Method to fetch data for untagged assets
    getAssetsTileData() {
        const queryParams = { ag: this.assetGroupName };
        const { url, method } = environment.taggingSummary;
        try {
            this.commonResponseService
                .getData(url, method, {}, queryParams)
                .pipe(takeUntil(this.destroy$))
                .subscribe(
                    (response) => {
                        try {
                            this.tiles[2].mainContent.count = response.output.untagged;
                            this.tiles[2].subContent[0].count = response.output.tagged;
                        } catch (e) {
                            this.logger.log(JS_ERROR, e);
                        }
                    },
                    (error) => this.logger.log(API_RESPONSE_ERROR, error),
                );
        } catch (error) {
            this.logger.log(JS_ERROR, error);
        }
    }

    // Method to redirect to specific pages based on user selection
    redirectTo(data: any) {
        this.workflowService.addRouterSnapshotToLevel(
            this.router.routerState.snapshot.root,
            0,
            this.pageTitle,
        );
        const tempQueryParam = {
            tempFilters: true,
        };
        if (data == 'Exempt Assets' || data == 'Exempt Asset Types') {
            const queryParams = {
                filter: 'exempted=true',
                ...tempQueryParam,
            };
            this.router.navigate(['pl/assets/asset-list'], {
                queryParams: queryParams,
                queryParamsHandling: 'merge',
            });
        } else if (data == 'Total Assets') {
            this.router.navigate(['pl/assets/asset-list'], {
                queryParams: { ...tempQueryParam },
                queryParamsHandling: 'merge',
            });
        } else if (data == 'Asset Types') {
            this.router.navigate(['pl/assets/asset-distribution'], {
                queryParams: { ...tempQueryParam },
                queryParamsHandling: 'merge',
            });
        } else if (data == 'Tagged Assets' || data == 'UnTagged Assets') {
            const queryParams = {
                filter: data == 'Tagged Assets' ? 'tagged=true' : 'tagged=false',
                ...tempQueryParam,
            };
            this.router.navigate(['pl/assets/asset-list'], {
                queryParams: queryParams,
                queryParamsHandling: 'merge',
            });
        }
    }

    // Method to fetch the count of exempted assets
    getExemtedAssetsCount() {
        const { url, method } = environment.exemptedAssetCount;
        const queryParams = { ag: this.assetGroupName };

        try {
            this.commonResponseService
                .getData(url, method, {}, queryParams)
                .pipe(takeUntil(this.destroy$))
                .subscribe((response) => {
                    this.tiles[1].mainContent.count = response.totalassets || 0;
                    this.tiles[1].subContent[0].count = response.assettype;
                });
        } catch (e) {}
    }

    // Method to fetch resource type, count, and recommendations
    getResourceTypeAndCountAndRecommendation() {
        try {
            const queryParams = {
                ag: this.assetGroupName,
                domain: this.domainName,
            };

            this.fetchResourcesService.getResourceTypesAndCount(queryParams);

            this.awsResourceTypeSelectionService.getAssetTypeCount().subscribe((asset) => {
                this.tiles[0].mainContent.count =
                    asset[TOTAL_ASSETS.toLowerCase().replace(/\s/g, '')];
                this.tiles[0].subContent[0].count =
                    asset[ASSET_TYPE.toLowerCase().replace(/\s/g, '')];
            });
        } catch (error) {
            this.logger.log(JS_ERROR, error);
        }
    }

    // Method to get the selected asset group
    getAssetGroup() {
        this.assetGroupObservableService
            .getAssetGroup()
            .pipe(takeUntil(this.destroy$))
            .subscribe((assetGroupName) => {
                this.backButtonRequired = this.workflowService.checkIfFlowExistsCurrently(
                    this.pageLevel,
                );
                this.assetGroupName = assetGroupName;
                this.updateComponent();
            });
    }

    // Method to update the component with relevant data
    updateComponent() {
        this.getAssetsTileData();
        this.getExemtedAssetsCount();
        this.getResourceTypeAndCountAndRecommendation();
    }

    ngOnDestroy() {
        this.dataStore.set('urlToRedirect', this.urlToRedirect);
        this.destroy$.next();
        this.destroy$.complete();
    }
}
