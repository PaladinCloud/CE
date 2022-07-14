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


import {throwError as observableThrowError,  Observable, combineLatest } from 'rxjs';
/**
 * Created by adityaagarwal on 23/11/17.
 */

import { Injectable, Inject } from '@angular/core';

import { HttpService } from '../../shared/services/http-response.service';
import { ErrorHandlingService } from '../../shared/services/error-handling.service';
import { map } from 'rxjs/operators';

@Injectable()
export class IssueListingService {
    constructor(
                @Inject(HttpService) private httpService: HttpService,
                private errorHandling: ErrorHandlingService) {}

    getData(listingPayload, listingUrl, listingMethod): Observable<any> {

        try {
            const url = listingUrl;
            const method = listingMethod;
            const payload = listingPayload;
            const queryParams = {};
            return combineLatest(
                this.httpService.getHttpResponse(url, method, payload, queryParams)
                .pipe(map(response => {
                    try {
                        this.dataCheck(response);
                        return response['data'];
                    } catch (error) {
                        this.errorHandling.handleJavascriptError(error);
                    }
                })
            ));
        } catch (error) {
            this.errorHandling.handleJavascriptError(error);
        }
    }

    dataCheck(data) {
        const APIStatus = this.errorHandling.checkAPIResponseStatus(data);
        if (!APIStatus.dataAvailble) {
            throw new Error('noDataAvailable');
        }
    }

    handleError(error: any): Observable<any> {
        return observableThrowError(error.message || error);
    }

}
