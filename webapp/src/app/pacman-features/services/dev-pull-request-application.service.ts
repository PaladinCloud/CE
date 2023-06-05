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

import { Injectable, Inject } from '@angular/core';
import { Observable } from 'rxjs';

import { HttpService } from '../../shared/services/http-response.service';
import { ErrorHandlingService } from '../../shared/services/error-handling.service';
import orderBy from 'lodash/orderBy';
import { map } from 'rxjs/operators';

@Injectable()
export class DevPullRequestApplicationService {

    getMethod: any;
    constructor( @Inject(HttpService) private httpService: HttpService,
                private errorHandling: ErrorHandlingService) { }

    getData(tableUrl, tableMethod, payload, queryParam): Observable<any> {

        const url = tableUrl;
        const method = tableMethod;
        const queryParams = queryParam;
        this.getMethod = tableMethod;

        try {
            return this.httpService.getHttpResponse(url, method, payload, queryParams)
                    .pipe(map(response => {
                        return this.massageData(response);
                    }));
        } catch (error) {
            this.errorHandling.handleJavascriptError(error);
        }
    }

    massageData(data) {
        // sort data as per scope
        data.response = orderBy(data.response, ['openPullRequests'], ['desc']);
        return data;
    }
}
