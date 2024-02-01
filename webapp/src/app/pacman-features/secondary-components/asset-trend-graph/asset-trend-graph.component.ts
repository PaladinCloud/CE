import { Component, OnInit, OnDestroy, Input } from '@angular/core';
import { Subject } from 'rxjs';
import { takeUntil } from 'rxjs/operators';
import { AssetGroupObservableService } from 'src/app/core/services/asset-group-observable.service';
import { AssetTypeMapService } from 'src/app/core/services/asset-type-map.service';
import { ASSET_TREND, ASSET_TYPE, TOTAL_ASSETS } from 'src/app/shared/constants/asset-trend-graph';
import {
    ALL_TIME,
    API_RESPONSE_ERROR,
    ERROR,
    NO_DATA_AVAILABLE,
    WAIT_FOR_DATA,
} from 'src/app/shared/constants/global';
import { LoggerService } from 'src/app/shared/services/logger.service';
import { UtilsService } from 'src/app/shared/services/utils.service';
import { MultilineChartService } from 'src/app/pacman-features/services/multilinechart.service';
import { DataCacheService } from 'src/app/core/services/data-cache.service';

interface AssetTypesFilters {
    list: string[];
    listData: { [key: string]: boolean };
    category: string;
    shortFilters: string[];
    numberOfAllowedFilters: number;
}

interface AssetTypeFilterChangeData {
    category: string;
    filterName: string;
    filterValue: boolean;
}

type GraphInterval = 'All time' | '1 week' | '1 month' | '6 months' | '12 months' | 'Custom';
interface AssetGraphSavedState {
    assetTypeList: string[];
    fromDate: Date;
    toDate: Date;
    graphInterval: GraphInterval;
}

@Component({
    selector: 'app-asset-trend-graph',
    templateUrl: './asset-trend-graph.component.html',
    styleUrls: ['./asset-trend-graph.component.css'],
})
export class AssetTrendGraphComponent implements OnInit, OnDestroy {
    @Input() header: string = ASSET_TREND;

    private destroy$: Subject<void> = new Subject<void>();

    // Private properties
    private defaultFilterKey = TOTAL_ASSETS;
    private defaultFilter = {};
    private assetTypesList;
    private assetGroupName: string;
    private savedState: AssetGraphSavedState;
    private defaultFromDate: Date = new Date(2022, 0, 1);

    // Public properties
    public graphInterval: GraphInterval = ALL_TIME;
    public fromDate: Date = this.defaultFromDate;
    public toDate: Date = new Date();
    public minDate: Date;
    public card = {
        header: this.header,
    };
    public graphHeight: number = 360;
    public filterTitle = ASSET_TYPE;
    public assetTypeFilters: AssetTypesFilters = {
        list: [],
        listData: this.defaultFilter,
        category: this.filterTitle,
        shortFilters: [this.defaultFilterKey],
        numberOfAllowedFilters: 10,
    };
    public assetsTrendData = [];
    public assetsTrendError: string = '';

    constructor(
        private assetGroupObservableService: AssetGroupObservableService,
        private assetTypeMapService: AssetTypeMapService,
        private multilineChartService: MultilineChartService,
        private utils: UtilsService,
        private logger: LoggerService,
        private dataStore: DataCacheService,
    ) {}

    ngOnInit(): void {
        // Subscribe to asset group and asset type map services
        this.subscribeToAssetGroup();
        this.subscribeToAssetTypeMapForAG();
    }

    // Subscribe to asset group changes
    private subscribeToAssetGroup() {
        this.assetGroupObservableService
            .getAssetGroup()
            .pipe(takeUntil(this.destroy$))
            .subscribe((assetGroupName) => (this.assetGroupName = assetGroupName));
    }

    // Subscribe to asset type map changes for the asset group
    private subscribeToAssetTypeMapForAG() {
        this.assetTypeMapService
            .getAssetTypeMapForGroup()
            .pipe(takeUntil(this.destroy$))
            .subscribe((data) => {
                if (!data) {
                    this.assetsTrendData = [];
                    this.assetsTrendError = API_RESPONSE_ERROR;
                    return;
                }
                const list = this.utils.mapValuesToArray(data);
                this.assetTypesList = this.utils.mapToObject(data);
                this.assetTypesList.totalassets = TOTAL_ASSETS;
                this.assetTypeFilters.list = [TOTAL_ASSETS, ...list];
                this.assetTypeFilters.listData = Object.fromEntries(
                    list.map((key) => [key, false]),
                );
                this.initializeState();
                this.assetTypeFilters.listData[TOTAL_ASSETS] = !this.savedState.assetTypeList;
                if (this.savedState.assetTypeList) {
                    for (const key in this.assetTypeFilters.listData) {
                        if (this.savedState.assetTypeList.includes(key)) {
                            this.assetTypeFilters.listData[key] = true;
                        }
                    }
                }
                this.assetTypeFilters = { ...this.assetTypeFilters };
                this.getAssetsCountData({});
            });
    }

    private initializeState() {
        this.card = { ...{ header: this.header } };
        const retrieveState = this.dataStore.getAssetTrendGraphState();
        if (!retrieveState) {
            const { toDate, fromDate, graphInterval } = this;
            this.savedState = {
                assetTypeList: undefined,
                fromDate,
                toDate,
                graphInterval,
            };
            this.assetTypeFilters.listData[TOTAL_ASSETS] = true;
        } else {
            const { toDate, fromDate, graphInterval, assetTypeList } = retrieveState;
            this.savedState = {
                assetTypeList,
                fromDate,
                toDate,
                graphInterval,
            };
            this.fromDate = fromDate;
            this.toDate = toDate;
            this.graphInterval = graphInterval;
        }
    }

    // Handle filter change event
    public onFilterChange(e: AssetTypeFilterChangeData) {
        const { filterName, filterValue } = e;
        this.assetTypeFilters.listData[filterName] = filterValue;
        this.assetTypeFilters = { ...this.assetTypeFilters };
        this.getAssetsCountData({});
    }

    // Fetch asset count data based on selected filters and date interval
    getAssetsCountData(dateInterval) {
        if (!this.assetGroupName) {
            return;
        }

        this.assetsTrendError = '';
        this.assetsTrendData = [];

        const { from: fromDate, to: toDate, graphInterval } = dateInterval;
        this.fromDate = fromDate || this.fromDate;
        this.toDate = toDate || this.toDate;

        if (graphInterval) {
            this.graphInterval = graphInterval;
            if (graphInterval === ALL_TIME) {
                this.fromDate = this.defaultFromDate;
            }
        }

        this.graphInterval = graphInterval || this.graphInterval;

        try {
            this.multilineChartService
                .getAssetTrendData(this.constructPayload())
                .pipe(takeUntil(this.destroy$))
                .subscribe(
                    (response) => this.updateAssetTrendGraph(response?.[0]?.data?.trend),
                    (error) => {
                        this.logger.log(ERROR, error);
                        this.assetsTrendError = API_RESPONSE_ERROR;
                    },
                    () => this.storeState(),
                );
        } catch (error) {
            this.assetsTrendError = API_RESPONSE_ERROR;
            this.logger.log(ERROR, error);
        }
    }

    // Construct payload for fetching asset trend data
    private constructPayload(): { ag: string; fromDate: string; toDate: string; type?: any } {
        const selectedAssetTypes = this.getSelectedAssetTypes();
        return {
            ag: this.assetGroupName,
            fromDate: this.getFormattedDate(this.fromDate),
            toDate: this.getFormattedDate(this.toDate),
            type:
                selectedAssetTypes.length !== 0
                    ? Object.keys(this.assetTypesList).filter((key) =>
                          selectedAssetTypes.includes(this.assetTypesList[key]),
                      )
                    : [TOTAL_ASSETS.toLowerCase().replace(/\s/g, '')],
        };
    }

    // Get selected asset types based on filters
    private getSelectedAssetTypes(): string[] {
        return Object.keys(this.assetTypeFilters.listData).filter(
            (key) => this.assetTypeFilters.listData[key] === true,
        );
    }

    // Update asset trend graph data for rendering
    private updateAssetTrendGraph(trendData): void {
        if (!trendData || trendData.length === 0) {
            this.assetsTrendError = NO_DATA_AVAILABLE;
            return;
        } else {
            this.setMinDateForDateSelector(trendData[0]?.date);
        }

        if (
            trendData.length === 1 &&
            this.utils.getDifferenceBetweenDateByDays(this.minDate, new Date()) < 2
        ) {
            this.assetsTrendError = WAIT_FOR_DATA;
            return;
        }
        const transformedData = {};
        trendData.forEach((entry) => {
            Object.keys(entry).forEach((key) => {
                if (key !== 'date') {
                    if (!transformedData[key]) {
                        transformedData[key] = {
                            key,
                            values: [],
                        };
                    }
                    transformedData[key].values.push({
                        date: new Date(entry.date),
                        value: entry[key],
                        'zero-value': false,
                        'no-data': false,
                    });
                }
            });
        });
        const transformedValue = Object.values(transformedData);
        transformedValue.forEach(
            (item) => (item['key'] = this.assetTypesList[item['key']] || item['key']),
        );
        this.assetsTrendData = [...transformedValue];
    }

    // Format date to a string
    getFormattedDate(date: Date) {
        if (typeof date === 'string') date = new Date(date);
        const offset = date.getTimezoneOffset();
        const formattedDate = new Date(date.getTime() - offset * 60 * 1000)
            .toISOString()
            .split('T')[0];
        return formattedDate;
    }

    onChipDropdownClose() {
        if (this.getSelectedAssetTypes().length === 0) {
            this.assetTypeFilters.listData[TOTAL_ASSETS] = true;
            this.assetTypeFilters = { ...this.assetTypeFilters };
            this.storeState();
        }
    }

    private storeState() {
        const { toDate, fromDate, graphInterval } = this;
        const state = {
            assetTypeList: Object.keys(this.assetTypeFilters.listData).filter(
                (key) => this.assetTypeFilters.listData[key] === true,
            ),
            toDate,
            fromDate,
            graphInterval,
        };
        this.dataStore.setAssetTrendGraphState(state);
    }

    private setMinDateForDateSelector(date) {
        if (date) this.minDate = new Date(date);
    }

    ngOnDestroy(): void {
        this.storeState();
        this.destroy$.next();
        this.destroy$.complete();
    }
}
