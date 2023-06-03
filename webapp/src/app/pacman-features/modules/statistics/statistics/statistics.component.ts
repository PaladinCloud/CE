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

import { Component, ElementRef, OnInit, ViewChild } from '@angular/core';
import html2canvas from 'html2canvas';
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
export class StatisticsComponent implements OnInit {
    readonly appName = CONFIGURATIONS.required.APP_NAME;
    readonly innerRadius = 70;
    readonly outerRadius = 50;
    readonly currentDate = new Date();

    @ViewChild('statisticsContainer') statisticsContainer: ElementRef<HTMLDivElement>;

    errorMessage: string;
    showLoader = true;

    numberOfAwsAccounts = 0;
    numberOfEventsProcessed = 0;
    numberOfPolicyWithAutoFixes = 0;
    numberOfPolicyEvaluations = '';
    numberOfPoliciesEnforced = 0;
    totalNumberOfAssets = 0;
    totalAutoFixesApplied = 0;

    doughNutData = {};

    widgetWidth: number;
    widgetHeight: number;

    constructor(
        private commonResponseService: CommonResponseService,
        private errorHandling: ErrorHandlingService,
    ) {}

    ngOnInit() {
        this.getStatsData();
    }

    takeScreenshot() {
        html2canvas(this.statisticsContainer.nativeElement, {
            backgroundColor: '#f2f3f5',
            ignoreElements: (el) => el.classList.contains('screenshot-btn'),
            onclone: (document) => {
                const el = document.getElementById('watermark');
                el.style.display = 'block';
            },
        }).then((canvas) => {
            const url = canvas.toDataURL('image/png');
            const a = document.createElement('a');
            a.download = 'PacBot-Statistics.png';
            a.href = url;

            a.click();
        });
    }

    private getDimensions() {
        const element = document.getElementById('statsDoughnut');
        if (!element) {
            return;
        }

        this.widgetWidth =
            parseInt(
                window.getComputedStyle(element, null).getPropertyValue('width').split('px')[0],
                10,
            ) + 20;
        this.widgetHeight =
            parseInt(
                window.getComputedStyle(element, null).getPropertyValue('height').split('px')[0],
                10,
            ) - 20;
    }

    private getStatsData() {
        this.showLoader = true;
        this.errorMessage = '';

        const { url, method } = environment.statspage;

        this.commonResponseService.getData(url, method).subscribe(
            (response: { response: StatsResponse[] }) => {
                if (response.response.length === 0) {
                    this.showErrorMessage('noDataAvailable');
                    return;
                }

                this.getDimensions();
                this.updateStatCounters(response.response[0]);
                this.updateChartData(response.response[0]);
                this.showLoader = false;
            },
            (error) => {
                this.showErrorMessage(this.errorHandling.handleJavascriptError(error));
            },
        );
    }

    private showErrorMessage(message: string) {
        this.showLoader = false;
        if (message) {
            this.errorMessage = message;
        }
    }

    private updateStatCounters(data: StatsResponse) {
        this.numberOfAwsAccounts = data.numberOfAwsAccounts;
        this.numberOfEventsProcessed = data.numberOfEventsProcessed;
        this.numberOfPolicyEvaluations = data.numberOfPolicyEvaluations;
        this.numberOfPoliciesEnforced = data.numberOfPoliciesEnforced;
        this.totalNumberOfAssets = data.totalNumberOfAssets;
        this.numberOfPolicyWithAutoFixes = data.numberOfPolicyWithAutoFixes;
        this.totalAutoFixesApplied = data.totalAutoFixesApplied;
    }

    private updateChartData({ totalViolations }: StatsResponse) {
        const legend = ['Critical', 'High', 'Medium', 'Low'];
        const data = legend.reduce(
            (acc, next) => [...acc, totalViolations[next.toLowerCase()]],
            [],
        );
        this.doughNutData = {
            color: ['#D95140', '#FF8888', '#FFCFCF', '#F1D668'],
            legend,
            data,
            totalCount: totalViolations.totalViolations,
            legendTextcolor: '#fff',
            link: false,
            styling: {
                cursor: 'text',
            },
        };
    }
}
