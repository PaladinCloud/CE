/*
 * Copyright 2023 Paladin Cloud, Inc or its affiliates. All Rights Reserved.
 */

import { Injectable } from '@angular/core';
import { combineLatest, Observable } from 'rxjs';
import { AssetGroupObservableService } from './asset-group-observable.service';
import { DomainTypeObservableService } from './domain-type-observable.service';

@Injectable()
export class AgDomainObservableService {
    constructor(
        private assetGroupObservableService: AssetGroupObservableService,
        private domainObservableService: DomainTypeObservableService,
    ) {}

    getAgDomain(): Observable<any> {
        return combineLatest([
            this.assetGroupObservableService.getAssetGroup(),
            this.domainObservableService.getDomainType(),
        ]);
    }
}
