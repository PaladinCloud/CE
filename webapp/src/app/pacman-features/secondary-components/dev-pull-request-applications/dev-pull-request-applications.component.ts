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

import { Component, OnInit, SimpleChanges, OnDestroy, Input, OnChanges } from '@angular/core';
import { Subscription } from 'rxjs';
import { environment } from './../../../../environments/environment';
import { LoggerService } from '../../../shared/services/logger.service';
import { AssetGroupObservableService } from '../../../core/services/asset-group-observable.service';
import { GridOptions } from 'ag-grid-community';
import { DevPullRequestApplicationService } from '../../services/dev-pull-request-application.service';

@Component({
  selector: 'app-dev-pull-request-applications',
  templateUrl: './dev-pull-request-applications.component.html',
  styleUrls: ['./dev-pull-request-applications.component.css'],
  providers: [DevPullRequestApplicationService]
})

export class DevPullRequestApplicationsComponent implements OnInit, OnDestroy, OnChanges {

  selectedAssetGroup: string;
  public errorMessage = 'apiResponseError';

  getContextMenuItems: any;
  gridApi: any;
  gridColumnApi: any;
  columns: any = [];

  public gridOptions: GridOptions;
  private subscriptionToAssetGroup: Subscription;
  private dataSubscription: Subscription;

  @Input() filter: any;

  public errorValue = 0;

  constructor(  private devPullRequestApplicationService: DevPullRequestApplicationService,
          private assetGroupObservableService: AssetGroupObservableService,
          private logger: LoggerService ) {

    this.gridOptions = <GridOptions>{};
    this.gridOptions.columnDefs = [];

    this.gridOptions.rowData = [];
        this.getContextMenuItems = function getContextMenuItems(params) {
            const result = [
              'toolPanel',
              'separator',
              'copy',
              'separator',
              'csvExport',
              'separator',
              'autoSizeAll',
              'resetColumns'
            ];
            return result;
        };

    this.subscriptionToAssetGroup = this.assetGroupObservableService.getAssetGroup().subscribe(
      assetGroupName => {
          this.selectedAssetGroup = assetGroupName;
            this.updateComponent();
    });

  }

  ngOnInit() {
  }

  downloadCsv() {
    this.gridApi.exportDataAsCsv();
  }

  ngOnChanges(changes: SimpleChanges) {
    try {
      const DataChange = changes['filter'];
      if (DataChange) {
        const cur  = JSON.stringify(DataChange.currentValue);
        const prev = JSON.stringify(DataChange.previousValue);
        if ((cur !== prev)) {
          this.getData();
        }
      }
    } catch (error) {
      this.errorValue = -1;
      this.errorMessage = 'jsError';
    }
  }

  updateComponent() {
      this.errorValue = 0;
      this.getData();
  }

  getData() {

    if (this.dataSubscription) {
        this.dataSubscription.unsubscribe();
      }
    const payload = {};
    const queryParam = {
      'ag': this.selectedAssetGroup,
      'application': this.filter
    };
    this.errorValue = 0;
    const tableUrl = environment.devStandardPullRequestApplications.url;
    const tableMethod = environment.devStandardPullRequestApplications.method;
    this.errorValue = 0;

    this.dataSubscription = this.devPullRequestApplicationService.getData(tableUrl, tableMethod, payload, queryParam).subscribe(
      response => {

        try {
            if (response.response.length) {
              this.errorValue = 1;
              this.processData(response);
            } else {
              this.errorValue = -1;
              this.errorMessage = 'noPullRequestsFound';
            }

        } catch (e) {
            this.errorValue = -1;
            this.errorMessage = 'jsError';
            this.logger.log('error', e);
        }
    },
    error => {
      this.errorValue = -1;
      this.errorMessage = 'apiResponseError';
      this.logger.log('error', error);
     });
  }

  processData(data) {
   this.columns = [];
   const ObjArr = data.response;
   let j = 0;
   while ( ObjArr[j] == null ) {
    j++;
   }
   const columns = Object.keys(ObjArr[j]);
   this.columns = columns;

   let eachObj = {};
   this.gridOptions.columnDefs = [];
   this.gridOptions.rowData = [];

   const objProperties = {
      minWidth: 160,
      maxWidth: 800
   };

   for ( let i = 0; i < columns.length; i++) {
     if (columns[i].toLowerCase() === 'application') {
         eachObj = {
           pinned: 'left',
           lockPosition: true,
           field: columns[i],
           headerName: 'Application',
           minWidth: 190,
           maxWidth: 800,
           order: 1
         };
      } else if (columns[i].toLowerCase() === 'openpullrequests') {
          eachObj = {
            field: columns[i],
            headerName: 'Open Pull Requests',
            order: 2
          };
          Object.assign(eachObj, objProperties);
    } else if (columns[i].toLowerCase() === 'avgagepr') {
      eachObj = {
        field: columns[i],
        headerName: 'Average PR age (days)',
        order: 3
      };
      Object.assign(eachObj, objProperties);
    } else if (columns[i].toLowerCase() === 'maxprage') {
      eachObj = {
        field: columns[i],
        headerName: 'Max PR Age (days)',
        order: 4
      };
      Object.assign(eachObj, objProperties);
     } else {
       eachObj = {
           field: columns[i],
           headerName: columns[i],
           minWidth: 160,
           maxWidth: 800
         };
     }
      this.gridOptions.columnDefs.push(eachObj);
    }
    // sortobject as per 'order' property set.
    this.gridOptions.columnDefs.sort((a, b) => {
      return a['order'] - b['order'];
    });
    this.gridOptions.rowData = data.response;
    if (this.gridApi) {
      this.gridApi.setColumnDefs(this.gridOptions.columnDefs);
      this.gridApi.setRowData(this.gridOptions.rowData);
      this.onresize();
    }
  }

  onresize() {
    if (this.columns.length < 6 && this.columns.length > 0) {
      setTimeout(() => {
        this.gridApi.sizeColumnsToFit();
      }, 3);
    } else {
      this.autoSizeAll();
    }
  }

  onGridReady(params) {
    this.gridApi = params.api;
    this.gridColumnApi = params.columnApi;
  }

  autoSizeAll() {
    const allColumnIds = [];
    if (this.gridColumnApi) {
      this.gridColumnApi.getAllColumns().forEach(function(column) {
        allColumnIds.push(column.colId);
      });
      this.gridColumnApi.autoSizeColumns(allColumnIds);
    }
  }

  ngOnDestroy() {
    try {
      if (this.subscriptionToAssetGroup) {
        this.subscriptionToAssetGroup.unsubscribe();
      }
      if (this.dataSubscription) {
        this.dataSubscription.unsubscribe();
      }
    } catch (error) {
      this.logger.log('error', '--- Error while unsubscribing ---');
    }
  }

}
