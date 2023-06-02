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

import { Component, ElementRef, OnDestroy, OnInit, ViewChild } from '@angular/core';
import html2canvas from 'html2canvas';
import { Subscription } from 'rxjs';
import { CommonResponseService } from 'src/app/shared/services/common-response.service';
import { ErrorHandlingService } from 'src/app/shared/services/error-handling.service';
import { CONFIGURATIONS } from 'src/config/configurations';
import { environment } from 'src/environments/environment';

export interface StatsResponse {
    numberOfAwsAccounts: number;
    numberOfEventsProcessed: number;
    numberOfPoliciesEnforced: number;
    numberOfPolicyEvaluations: string; // TODO: should be fixed on backend
    numberOfPolicyWithAutoFixes: number;
    totalAutoFixesApplied: number;
    totalNumberOfAssets: number;
    totalViolations: TotalViolations;
}

export interface TotalViolations {
    critical: number;
    high: number;
    low: number;
    medium: number;
    totalViolations: number;
}

@Component({
    selector: 'app-statistics',
    templateUrl: './statistics.component.html',
    styleUrls: ['./statistics.component.css'],
    providers: [CommonResponseService],
})
export class StatisticsComponent implements OnInit, OnDestroy {
    readonly appName = CONFIGURATIONS.required.APP_NAME;
    @ViewChild('statisticsContainer') statisticsContainer: ElementRef<HTMLDivElement>;
    currentDate = new Date();
    selectedAssetGroup: string;
    apiData: any;
    applicationValue: any;
    errorMessage: string;
    dataComing = false;
    showLoader = true;
    tableHeaderData: any;
    placeHolderText: string;
    returnedSearch = '';
    seekdata = false;
    isScreenshoting = false;
    numberOfAwsAccounts = 0;
    numberOfEventsProcessed = 0;
    numberOfPolicyWithAutoFixes = 0;
    numberOfPolicyEvaluations = '';
    numberOfPoliciesEnforced = 0;
    totalNumberOfAssets = 0;
    totalViolationsGraph: any = [];
    doughNutData: any = [];
    widgetWidth: number;
    widgetHeight: number;
    MainTextcolor = '';
    innerRadius = 60;
    outerRadius = 50;
    strokeColor = 'transparent';
    totalAutoFixesApplied = 0;

    private dataSubscription: Subscription;

    constructor(
        private commonResponseService: CommonResponseService,
        private errorHandling: ErrorHandlingService,
    ) {}

    ngOnInit() {
        this.updateComponent();
    }

    ngOnDestroy() {
        try {
            if (this.dataSubscription) {
                this.dataSubscription.unsubscribe();
            }
        } catch (error) {
            this.errorMessage = this.errorHandling.handleJavascriptError(error);
            this.getErrorValues();
        }
    }

    takeScreenshot() {
        html2canvas(this.statisticsContainer.nativeElement, {
            onclone: (document) => {
                const el = document.getElementById('watermark');
                el.style.display = 'block';
            },
            ignoreElements: (el) => el.classList.contains('screenshot-btn'),
            backgroundColor: '#f2f3f5',
        })
            .then((canvas) => {
                const url = canvas.toDataURL('image/png');
                const a = document.createElement('a');
                a.download = 'PacBot-Statistics.png';
                a.href = url;

                a.click();
            })
            .finally(() => (this.isScreenshoting = false));
    }

    getDimensions() {
        const element = document.getElementById('statsDoughnut');
        if (element) {
            this.widgetWidth =
                parseInt(
                    window.getComputedStyle(element, null).getPropertyValue('width').split('px')[0],
                    10,
                ) + 20;
            this.widgetHeight =
                parseInt(
                    window
                        .getComputedStyle(element, null)
                        .getPropertyValue('height')
                        .split('px')[0],
                    10,
                ) - 20;
        }
    }

    updateComponent() {
        this.showLoader = true;
        this.dataComing = false;
        this.seekdata = false;
        this.getData();
    }

    getData() {
        this.getStatsData();
    }

    getStatsData() {
        const queryParams = {};

        const statspageUrl = environment.statspage.url;
        const statspageMethod = environment.statspage.method;
        this.dataSubscription = this.commonResponseService
            .getData(statspageUrl, statspageMethod, {}, queryParams)
            .subscribe(
                (response: { response: StatsResponse[] }) => {
                    try {
                        if (response.response.length === 0) {
                            this.getErrorValues();
                            this.errorMessage = 'noDataAvailable';
                        } else {
                            this.showLoader = false;
                            this.seekdata = false;
                            this.dataComing = true;
                            this.getDimensions();
                            this.processData(response.response[0]);
                        }
                    } catch (e) {
                        this.errorMessage = this.errorHandling.handleJavascriptError(e);
                        this.getErrorValues();
                    }
                },
                (error) => {
                    this.errorMessage = error;
                    this.getErrorValues();
                },
            );
    }

    // error values
    getErrorValues(message?: string) {
        this.showLoader = false;
        this.dataComing = false;
        this.seekdata = true;
        if (message) {
            this.errorMessage = message;
        }
    }

    processData(data: StatsResponse) {
        this.numberOfAwsAccounts = data.numberOfAwsAccounts;
        this.numberOfEventsProcessed = data.numberOfEventsProcessed;
        this.numberOfPolicyEvaluations = data.numberOfPolicyEvaluations;
        this.numberOfPoliciesEnforced = data.numberOfPoliciesEnforced;
        this.totalNumberOfAssets = data.totalNumberOfAssets;
        this.totalViolationsGraph = data.totalViolations;
        this.numberOfPolicyWithAutoFixes = data.numberOfPolicyWithAutoFixes;
        this.totalAutoFixesApplied = data.totalAutoFixesApplied;

        /**
         ------ this is the data for statspage doughnut chart for policy with violations ---------
         */
        this.MainTextcolor = '#fff';
        this.strokeColor = 'eff3f6';
        const colorTransData = ['#D95140', '#FF8888', '#FFCFCF', '#F1D668'];
        const graphLegend = ['Critical', 'High', 'Medium', 'Low'];
        const graphDataArray = [];
        const legendTextcolor = '#fff';
        /**
         * Added by Trinanjan on 02/03/2018
         * Inorder to sort objkeys in a logical way, objKeys are hardcoded
         */
        const objKeys = ['critical', 'high', 'medium', 'low', 'totalViolations'];
        /* ****************************************************************** */
        objKeys.splice(objKeys.indexOf('totalViolations'), 1);
        objKeys.forEach((element) => {
            graphDataArray.push(this.totalViolationsGraph[element]);
        });
        this.innerRadius = 70;
        this.outerRadius = 50;
        const formattedObject = {
            color: colorTransData,
            data: graphDataArray,
            legend: graphLegend,
            totalCount: this.totalViolationsGraph.totalViolations,
            legendTextcolor,
            link: false,
            styling: {
                cursor: 'text',
            },
        };
        this.doughNutData = formattedObject;
    }
}
