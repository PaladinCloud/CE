import { Component, OnInit, OnDestroy, Input } from '@angular/core';
import { Subject } from 'rxjs';
import { takeUntil } from 'rxjs/operators';
import { AssetGroupObservableService } from 'src/app/core/services/asset-group-observable.service';
import { AssetTypeMapService } from 'src/app/core/services/asset-type-map.service';
import {
  ASSET_TREND,
  ASSET_TYPE,
  TOTAL_ASSETS,
} from 'src/app/shared/constants/asset-trend-graph';
import { ALL_TIME, API_RESPONSE_ERROR, ERROR } from 'src/app/shared/constants/global';
import { LoggerService } from 'src/app/shared/services/logger.service';
import { UtilsService } from 'src/app/shared/services/utils.service';
import { MultilineChartService } from 'src/app/pacman-features/services/multilinechart.service';
import { DataCacheService } from 'src/app/core/services/data-cache.service';

// Interface to define the structure of the AssetTypesFilters object
interface AssetTypesFilters {
  list: string[];
  listData: { [key: string]: boolean };
  category: string;
  shortFilters: string[];
  numberOfAllowedFilters: number;
}

// Interface to define the structure of the data emitted on filter change
interface AssetTypeFilterChangeData {
  category: string;
  filterName: string;
  filterValue: boolean;
}

@Component({
  selector: 'app-asset-trend-graph',
  templateUrl: './asset-trend-graph.component.html',
  styleUrls: ['./asset-trend-graph.component.css'],
})
export class AssetTrendGraphComponent implements OnInit, OnDestroy {
  @Input() header: string = ASSET_TREND;

  // Subject for managing the component lifecycle
  private destroy$: Subject<void> = new Subject<void>();

  // Private properties
  private defaultFilterKey = TOTAL_ASSETS;
  private defaultFilter = {};
  private assetTypesList;
  private assetGroupName: string;
  private savedFilterList:{
    [key: string]: boolean;
  };

  // Public properties
  public graphInterval = ALL_TIME;
  public fromDate: Date = new Date(2022, 0, 1);
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
    // Initialize saved or default state 
    this.initializeState();
    
    // Subscribe to asset group and asset type map services
    this.subscribeToAssetGroup();
    this.subscribeToAssetTypeMapForAG();
  }

  private initializeState(){
    this.card = { ...{ header:this.header }};
    const retriveState = this.dataStore.getAssetTrendGraphFiltersList();
    this.savedFilterList = retriveState ? retriveState : undefined;
    if(this.savedFilterList){
      this.assetTypeFilters.listData = this.savedFilterList;
    }else{
      this.defaultFilter[TOTAL_ASSETS] = true;
      this.assetTypeFilters.listData = {...this.assetTypeFilters.listData, ...this.defaultFilter}
    }
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
        const list = this.utils.mapValuesToArray(data);
        this.assetTypesList = this.utils.mapToObject(data);
        this.assetTypesList.totalassets = TOTAL_ASSETS;
        this.assetTypeFilters.list = [TOTAL_ASSETS, ...list];
        if(this.savedFilterList){
          this.assetTypeFilters.listData = {
            ...this.assetTypeFilters.listData,
          };
        }else{
          this.assetTypeFilters.listData = {
            ...this.assetTypeFilters.listData,
            ...Object.fromEntries(list.map((key) => [key, false])),
          };
        }
        this.assetTypeFilters = { ...this.assetTypeFilters };
        this.getAssetsCountData({});
      });
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

    const { from, to, graphInterval } = dateInterval;

    this.fromDate = from ? from : new Date(2022, 1, 1);
    this.toDate = to ? to : new Date();
    this.graphInterval = graphInterval ? graphInterval : ALL_TIME;

    try {
      this.multilineChartService
        .getAssetTrendData(this.constructPayload())
        .pipe(takeUntil(this.destroy$))
        .subscribe(
          (response) => this.updateAssetTrendGraph(response[0]?.data?.trend),
          (error) => {
            this.logger.log(ERROR, error);
            this.assetsTrendError = API_RESPONSE_ERROR;
          }
        );
    } catch (error) {
      this.assetsTrendError = API_RESPONSE_ERROR;
      this.logger.log(ERROR, error);
    }
  }

  // Construct payload for fetching asset trend data
  private constructPayload(): { ag: string; startDate: string; endDate: string; type?: any } {
    const selectedAssetTypes = this.getSelectedAssetTypes();

    return {
      ag: this.assetGroupName,
      startDate: this.getFormattedDate(this.fromDate),
      endDate: this.getFormattedDate(this.toDate),
      type:
        selectedAssetTypes.length !== 0
          ? Object.keys(this.assetTypesList).filter((key) => selectedAssetTypes.includes(this.assetTypesList[key]))
          : [TOTAL_ASSETS.toLowerCase().replace(/\s/g, '')],
    };
  }

  // Get selected asset types based on filters
  private getSelectedAssetTypes(): string[] {
    return Object.keys(this.assetTypeFilters.listData).filter((key) => this.assetTypeFilters.listData[key] === true);
  }

  // Update asset trend graph data for rendering
  private updateAssetTrendGraph(trendData): void {
    if (!trendData) {
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
    transformedValue.forEach((item) => (item['key'] = this.assetTypesList[item['key']] || item['key']));
    this.assetsTrendData = [...transformedValue];
    this.storeState();
  }

  // Get minimum date for the date selector
  getMinDateForDateSelector(dataList) {
    if (this.graphInterval == ALL_TIME) {
      if (dataList.length > 0) {
        this.minDate = new Date(dataList[0].date);
      } else {
        this.minDate = new Date();
      }
    }
  }

  // Format date to a string
  getFormattedDate(date: Date) {
    const offset = date.getTimezoneOffset();
    const formattedDate = new Date(date.getTime() - offset * 60 * 1000).toISOString().split('T')[0];
    return formattedDate;
  }

  private storeState(){
    this.dataStore.setAssetTrendGraphFiltersList(this.assetTypeFilters.listData);
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }
}
