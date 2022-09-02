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
  OnDestroy,
  Input,
  Output,
  EventEmitter
} from '@angular/core';
import { Subscription } from 'rxjs';
import { CommonResponseService } from '../../../shared/services/common-response.service';
import { AssetGroupObservableService } from '../../../core/services/asset-group-observable.service';
import { AutorefreshService } from '../../services/autorefresh.service';
import { environment } from './../../../../environments/environment';
import { LoggerService } from '../../../shared/services/logger.service';
import { ErrorHandlingService } from '../../../shared/services/error-handling.service';
import { RefactorFieldsService } from '../../../shared/services/refactor-fields.service';

@Component({
  selector: 'app-open-ports',
  templateUrl: './open-ports.component.html',
  styleUrls: ['./open-ports.component.css'],
  providers: [CommonResponseService, AutorefreshService]
})

export class OpenPortsComponent implements OnInit, OnDestroy {
  public somedata: any;
  public outerArr: any;
  public allColumns: any;
  selectedAssetGroup: string;
  public apiData: any;
  public applicationValue: any;
  public errorMessage: any;
  public dataComing = true;
  public showLoader = true;
  public tableHeaderData: any;
  private subscriptionToAssetGroup: Subscription;
  private dataSubscription: Subscription;
  public seekdata = false;
  durationParams: any;
  autoRefresh: boolean;
  totalRows = 0;
  bucketNumber = 0;
  paginatorSize = 10;
  currentBucket: any = [];
  firstPaginator = 1;
  lastPaginator: number;
  dataTableData: any = [];
  tableDataLoaded = false;
  currentPointer = 0;
  errorValue = 0;
  searchTxt = '';
  firstTimeLoad = true;
  showGenericMessage = false;

  @Input() resourceId = '';
  @Input() resourceType = 'ec2';
  @Output() errorOccured = new EventEmitter<any>();

  constructor(
    private commonResponseService: CommonResponseService,
    private assetGroupObservableService: AssetGroupObservableService,
    private autorefreshService: AutorefreshService,
    private logger: LoggerService,
    private errorHandling: ErrorHandlingService,
    private refactorFieldsService: RefactorFieldsService
  ) {
    this.subscriptionToAssetGroup = this.assetGroupObservableService.getAssetGroup().subscribe(
       assetGroupName => {
           this.selectedAssetGroup = assetGroupName;
           this.updateComponent();
     });

    this.durationParams = this.autorefreshService.getDuration();
    this.durationParams = parseInt(this.durationParams, 10);
    this.autoRefresh = this.autorefreshService.autoRefresh;
  }

  ngOnInit() {
    this.updateComponent();
  }

  updateComponent() {
    /* All functions variables which are required to be set for component to be reloaded should go here */
    try {
      if (this.resourceId !== '') {
        this.outerArr = [];
        this.searchTxt = '';
        this.currentBucket = [];
        this.bucketNumber = 0;
        this.firstPaginator = 1;
        this.dataTableData = [];
        this.tableDataLoaded = false;
        this.currentPointer = 0;
        this.showLoader = true;
        this.dataComing = false;
        this.seekdata = false;
        this.errorValue = 0;
        this.showGenericMessage = false;
        this.getData();
      }
    } catch (error) {
      this.logger.log('error', 'js error - ' + error);
    }
  }

  getData() {
    /* All functions to get data should go here */
    this.getAllPatchingDetails();
  }

  getAllPatchingDetails() {

    if (this.dataSubscription) {
      this.dataSubscription.unsubscribe();
    }
    const payload = {
      ag: this.selectedAssetGroup,
      filter: {},
      from: this.bucketNumber * this.paginatorSize,
      searchtext: this.searchTxt,
      size: this.paginatorSize
    };

    const queryParam = {
      from: this.bucketNumber * this.paginatorSize,
      searchtext: this.searchTxt,
      size: this.paginatorSize
    };

    this.errorValue = 0;
    const openPortsUrl = environment.openPorts.url;
    const newUrl = this.replaceUrl(openPortsUrl);
    const openPortsMethod = environment.openPorts.method;

    this.dataSubscription = this.commonResponseService
      .getData( newUrl, openPortsMethod, payload, queryParam)
      .subscribe(
        response => {
          this.showGenericMessage = false;
          try {
            this.errorValue = 1;
            this.showLoader = false;
            this.dataTableData = response.response;
            this.dataComing = true;
            this.seekdata = false;
            this.dataComing = true;
            if (response.response.length === 0 && this.firstTimeLoad) {
              this.errorOccured.emit();
            }
            this.firstTimeLoad = false;
            this.totalRows = response.total;
            this.firstPaginator = this.bucketNumber * this.paginatorSize + 1;
            this.lastPaginator =
            this.bucketNumber * this.paginatorSize + this.paginatorSize;
            this.currentPointer = this.bucketNumber;

            if (this.lastPaginator > this.totalRows) {
              this.lastPaginator = this.totalRows;
            }
            const updatedResponse = this.massageData(response.response);
            this.currentBucket[this.bucketNumber] = updatedResponse;
            this.processData(updatedResponse);
          } catch (e) {
            this.errorValue = 0;
            this.errorMessage = this.errorHandling.handleJavascriptError(e);
            this.getErrorValues();
            this.errorOccured.emit();
          }
        },
        error => {
          this.showGenericMessage = true;
          this.errorMessage = error;
          this.getErrorValues();
          this.errorOccured.emit();
        }
      );
  }

  getErrorValues(): void {
    this.errorValue = -1;
    this.showLoader = false;
    this.dataComing = false;
    this.seekdata = true;
  }
  massageData(data) {
    /*
    * added by Trinanjan 14/02/2017
    * the funciton replaces keys of the table header data to a readable format
    */
    const refactoredService = this.refactorFieldsService;
    const newData = [];
    data.map(function(eachRow) {
      const KeysTobeChanged = Object.keys(eachRow);
      let newObj = {};
      KeysTobeChanged.forEach(element => {
        const elementnew =
          refactoredService.getDisplayNameForAKey(
            element.toLocaleLowerCase()
          ) || element;
        newObj = Object.assign(newObj, { [elementnew]: eachRow[element] });
      });
      newData.push(newObj);
    });
    return newData;
  }
  processData(data) {
    let innerArr = {};
    const totalVariablesObj = {};
    let cellObj = {};
    this.outerArr = [];
    const getData = data;

    const getCols = Object.keys(getData[0]);

    for ( let row = 0; row < getData.length; row++) {
      innerArr = {};
      for ( let col = 0; col < getCols.length; col++) {
        cellObj = {
          link: '',
          properties: {
            color: ''
          },
          colName: getCols[col],
          hasPreImg: false,
          imgLink: '',
          text: getData[row][getCols[col]],
          valText: getData[row][getCols[col]]
        };

        // innerArr.push(cellObj);
        innerArr[getCols[col]] = cellObj;
        totalVariablesObj[getCols[col]] = '';
      }
      this.outerArr.push(innerArr);
    }
    if (this.outerArr.length > getData.length) {
      const halfLength = this.outerArr.length / 2;
      this.outerArr = this.outerArr.splice(halfLength);
    }
    this.allColumns = Object.keys(totalVariablesObj);
  }

  replaceUrl(url) {
    let replacedUrl = url.replace('{resourceId}', this.resourceId.toString());
    replacedUrl = replacedUrl.replace(
      '{assetGroup}',
      this.selectedAssetGroup.toString()
    );
    replacedUrl = replacedUrl.replace(
      '{resourceType}',
      this.resourceType.toString()
    );
    return replacedUrl;
  }

  prevPg() {
    this.currentPointer--;
    this.processData(this.currentBucket[this.currentPointer]);

    this.firstPaginator = this.currentPointer * this.paginatorSize + 1;
    this.lastPaginator =
      this.currentPointer * this.paginatorSize + this.paginatorSize;
  }

  nextPg() {
    if (this.currentPointer < this.bucketNumber) {
      this.currentPointer++;
      this.processData(this.currentBucket[this.currentPointer]);
      this.firstPaginator = this.currentPointer * this.paginatorSize + 1;
      this.lastPaginator =
        this.currentPointer * this.paginatorSize + this.paginatorSize;
      if (this.lastPaginator > this.totalRows) {
        this.lastPaginator = this.totalRows;
      }
    } else {
      this.bucketNumber++;
      this.getData();
    }
  }

  searchCalled(search) {
    this.searchTxt = search;
  }

  callNewSearch() {
    this.bucketNumber = 0;
    this.currentBucket = [];
    this.getData();
  }

  ngOnDestroy() {
    try {
      this.subscriptionToAssetGroup.unsubscribe();
      if (this.dataSubscription) {
        this.dataSubscription.unsubscribe();
      }
    } catch (error) {
      this.errorMessage = this.errorHandling.handleJavascriptError(error);
      this.getErrorValues();
    }
  }
}
