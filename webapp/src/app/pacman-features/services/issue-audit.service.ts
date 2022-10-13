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
 * Created by sauravdutta on 20/11/17.
 */

import { Injectable, Inject } from '@angular/core';

import { HttpService } from '../../shared/services/http-response.service';
import { map } from 'rxjs/operators';

@Injectable()
export class IssueAuditService {

    constructor( @Inject(HttpService) private httpService: HttpService) { }

    getHoursValue: any;
    getMinutesValue: any;
    getSecondsValue: any;
    getDateValue: any;
    getMonthValue: any;
    getMethod: any;
    getYearValue: any;

    getData(payload, IssueAuditUrl, IssueAuditMethod): Observable<any> {

        const url = IssueAuditUrl;
        const method = IssueAuditMethod;
        const queryParams = {};
        this.getMethod = IssueAuditMethod;

        try {
            return this.httpService.getHttpResponse(url, method, payload, queryParams);
        } catch (error) {
            this.handleError(error);
        }
    }


    handleError(error: any): Observable<any> {
        return observableThrowError(error.message || error);
    }

}