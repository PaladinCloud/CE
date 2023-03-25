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
 * Created by Mohammed_Furqan on 10/10/17.
 */
import { Observable } from 'rxjs';
import { Injectable, Inject } from '@angular/core';

import { environment } from './../../../environments/environment';
import { HttpService } from '../../shared/services/http-response.service';
import { LoggerService } from '../../shared/services/logger.service';
import { ErrorHandlingService } from '../../shared/services/error-handling.service';
import { map } from 'rxjs/operators';

export interface IssueHistoryItem {
    key: string;
    values: IssueHistoryItemValue[];
}
interface IssueHistoryItemValue {
    date: Date;
    value: number;
    'zero-value': boolean;
}

@Injectable()
export class IssuesHistoryService {
    constructor(
        @Inject(HttpService) private httpService: HttpService,
        private logger: LoggerService,
        private errorHandling: ErrorHandlingService,
    ) {}

    getData(url, method, payload, queryParameters) {
        try {
            return this.httpService
                .getHttpResponse(url, method, payload, queryParameters)
                .pipe(
                    map((response) =>
                        method === 'POST'
                            ? this.massagePostResponse(response)
                            : this.massageGetResponse(response),
                    ),
                );
        } catch (error) {
            this.logger.log('error', error);
            this.errorHandling.handleJavascriptError(error);
        }
    }

    getSeverity(queryParameters) {
        const url = environment.severity.url;
        const method = environment.severity.method;
        try {
            return this.httpService.getHttpResponse(url, method, {}, queryParameters);
        } catch (error) {
            this.errorHandling.handleJavascriptError(error);
        }
    }

    massageGetResponse(data) {
        const apiResponse = Object.assign({}, data);
        const values: IssueHistoryItemValue[] = [];
        const allDates = Object.keys(apiResponse).sort();
        for (let i = 0; i < allDates.length; i++) {
            // Additional property 'zero-value' being added to keep track of zero values, as the zero values are replaced
            // with 1 during plotting graph with a log axis (as [log 0]  is infinity)
            const obj = {
                date: new Date(allDates[i]),
                value: apiResponse[allDates[i]],
                'zero-value': apiResponse[allDates[i]] === 0,
            };
            values.push(obj);
        }
        const formattedObject: IssueHistoryItem = {
            key: 'total',
            values,
        };
        return formattedObject;
    }

    massagePostResponse(data) {
        const finalData: IssueHistoryItem[] = [];
        let apiResponse = data.data.response['issues_info'];
        if (data.data.response['issues_info'] && apiResponse.constructor.name === 'Array') {
            apiResponse = apiResponse.sort((a, b) => {
                a = new Date(a.date);
                b = new Date(b.date);
                const x = a < b ? -1 : 1;
                return x;
            });

            const types = Object.keys(apiResponse[0]);
            types.splice(types.indexOf('date'), 1);
            types.forEach(type => {
                const values = [];
                apiResponse.forEach(details => {
                    const obj = {
                        date: new Date(details['date']),
                        value: details[type],
                        'zero-value': details[type] === 0,
                    };
                    values.push(obj);
                });
                const formattedObject: IssueHistoryItem = {
                    key: type,
                    values,
                };
                if (type.toLowerCase() !== 'overall' && type.toLowerCase() !== 'total') {
                    if (type.toLowerCase() !== 'security' && type.toLowerCase() !== 'governance') {
                        finalData.unshift(formattedObject);
                    }
                } else {
                    finalData.push(formattedObject);
                }
            });
        }
        return finalData;
    }
}
