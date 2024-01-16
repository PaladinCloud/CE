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

/**
 * Created by adityaagarwal on 25/10/17.
 */

import { Injectable } from '@angular/core';
import { Observable ,  ReplaySubject } from 'rxjs';
import { DataCacheService } from './data-cache.service';
import { AssetTypeMapService } from './asset-type-map.service';

@Injectable()

export class AssetGroupObservableService {

    private assetGroupSubject = new ReplaySubject<string>(0);

    private updateTriggerStatus;

    constructor(
        private dataCacheService: DataCacheService,
        private assetTypeMapService: AssetTypeMapService,
    ) {}

    updateAssetGroup (groupName: string) {
        const previousAssetGroup = this.dataCacheService.getCurrentSelectedAssetGroup();
        const shouldNotUpdate = (previousAssetGroup === groupName && this.updateTriggerStatus) ? true : false;

        // Pass data only when there is valid asset group.
        if (groupName && !shouldNotUpdate) {
            this.dataCacheService.setCurrentSelectedAssetGroup(groupName);
            this.assetGroupSubject.next(groupName);
            this.assetTypeMapService.fetchAssetTypesForAg(groupName);
            this.updateTriggerStatus = true;
        }
    }

    getAssetGroup(): Observable<string> {
        return this.assetGroupSubject.asObservable();
    }

}
