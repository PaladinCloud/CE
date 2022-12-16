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
 * Created by sauravdutta on 11/10/17.
 */
import { Observable, of } from 'rxjs';
import { Injectable, Inject } from '@angular/core';
import { HttpHeaders, HttpRequest } from "@angular/common/http";

import { HttpService } from '../../shared/services/http-response.service';
import { ErrorHandlingService } from '../../shared/services/error-handling.service';
import { catchError, map } from 'rxjs/operators';

@Injectable()
export class PacmanIssuesService {
    constructor(
        private httpService: HttpService,
        private errorHandling: ErrorHandlingService) { }

    criticalValue: any;
    highValue: any;
    mediumValue: any;
    lowValue: any;
    securityValue: any;
    governanceValue: any;
    length: any;
    checkKey: any;
    totalIssues: any;
    valuePercent: any;
    keys: any;
    percent_keys: any;
    pacman_data: any;

    headers: any = new HttpHeaders({ 'Content-Type': 'application/json' });

    getData(queryParams, pacmanIssuesUrl, pacmanIssuesMethod): Observable<any> {

        const url = pacmanIssuesUrl;
        const method = pacmanIssuesMethod;
        const payload = {};

        try {
            return this.httpService.getHttpResponse(url, method, payload, queryParams)
                .pipe(map(response => {
                        this.dataCheck(response);
                        return this.massageData(response);
                }))
                .pipe(catchError(error => of(error)));
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
        this.keys = Object.keys(data[`distribution`].distribution_by_severity);
        this.percent_keys = Object.keys(data[`distribution`].policyCategory_percentage);
        this.length = this.keys.length;
        this.criticalValue = undefined;
        this.highValue = undefined;
        this.mediumValue = undefined;
        this.lowValue = undefined;

        // get the key values and respective count values of the severities.....

        for (let i = 0; i < this.length; i++) {
            this.checkKey = this.keys[i];
            switch (this.checkKey) {
                case 'critical':
                    this.criticalValue = data[`distribution`].distribution_by_severity[this.checkKey];
                    break;
                case 'high':
                    this.highValue = data[`distribution`].distribution_by_severity[this.checkKey];
                    break;
                case 'medium':
                    this.mediumValue = data[`distribution`].distribution_by_severity[this.checkKey];
                    break;
                case 'low':
                    this.lowValue = data[`distribution`].distribution_by_severity[this.checkKey];
                    break;
                default:
            }
        } // end of for loop

        if (this.criticalValue === undefined) {
            this.criticalValue = 0;
        }

        if (this.mediumValue === undefined) {
            this.mediumValue = 0;
        }

        if (this.highValue === undefined) {
            this.highValue = 0;
        }

        if (this.lowValue === undefined) {
            this.lowValue = 0;
        }

        this.totalIssues = data[`distribution`].total_issues;
        this.valuePercent = (this.criticalValue / this.totalIssues) * 100;
        const catArr = [];
        for (let i = 0; i < Object.keys(data[`distribution`].policyCategory_percentage).length; i++) {
            const catObj = {};
            catObj[Object.keys(data[`distribution`].policyCategory_percentage)[i]] =
                data[`distribution`].policyCategory_percentage[
                Object.keys(data[`distribution`].policyCategory_percentage)[i]
                ];
            catArr.push(catObj);
        }

        this.pacman_data = {
            'totalIssues': data[`distribution`].total_issues,
            'severity': [
                {
                    'critical': this.criticalValue
                },
                {
                    'high': this.highValue
                },
                {
                    'medium': this.mediumValue
                },
                {
                    'low': this.lowValue
                }
            ],
            'category': catArr,
            'valuePercent': this.valuePercent
        };
        return this.pacman_data;
    }
}
