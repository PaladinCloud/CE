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
 * Created by sauravdutta on 16/01/18.
 */
import { Observable } from 'rxjs';
import { Injectable, Inject } from '@angular/core';

import { HttpService } from '../../shared/services/http-response.service';
import { ErrorHandlingService } from '../../shared/services/error-handling.service';
import { map } from 'rxjs/operators';

@Injectable()
export class CpuUtilizationService {
    values: any = [];
    dataArray: any = [];
    constructor(
        private httpService: HttpService,
        private errorHandling: ErrorHandlingService,
    ) {}

    getData(Url, Method): Observable<any> {
        const url = Url;
        const method = Method;
        const payload = {};
        const queryParams = {};
        try {
            return this.httpService.getHttpResponse(url, method, payload, queryParams).pipe(
                map((response) => {
                    try {
                        this.dataCheck(response);
                        return this.massageData(response);
                    } catch (error) {
                        this.errorHandling.handleJavascriptError(error);
                    }
                }),
            );
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

    massageData(data): any {
        this.dataArray = [];
        this.values = [];
        for (let i = 0; i < data.response.length; i++) {
            let obj = {};
            let dateValue = data.response[i].date;
            const value = data.response[i][`cpu-utilization`];
            const fixedValue = value.toFixed(2);
            const numValue = Number(fixedValue);
            dateValue = new Date(dateValue);
            data.response[i].date = dateValue;
            obj = {
                value: numValue,
                keys: ['cpu-utilization'],
                legends: ['CPU'],
                date: data.response[i].date,
            };
            this.values.push(obj);
        }
        this.dataArray = [
            {
                values: this.values,
            },
        ];
        return this.dataArray;
    }
}
