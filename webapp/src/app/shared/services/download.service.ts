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
 * Created by adiagrwl on 11/Jan/18.
 */

import { Injectable, Inject } from '@angular/core';
import { Observable, ReplaySubject } from 'rxjs';

import { HttpService } from './http-response.service';
import { ErrorHandlingService } from './error-handling.service';
import { ToastObservableService } from '../../post-login-app/common/services/toast-observable.service';
import { LoggerService } from './logger.service';
import { map } from 'rxjs/operators';
import { DialogBoxComponent } from '../components/molecules/dialog-box/dialog-box.component';
import { MatDialog } from '@angular/material/dialog';

@Injectable()
export class DownloadService {
    private subject = new ReplaySubject<any>(0);
    private compDownloadStatus: object = {};
    constructor(
        @Inject(HttpService) private httpService: HttpService,
        private errorHandling: ErrorHandlingService,
        private toastObservableService: ToastObservableService,
        private dialog: MatDialog,
        private loggerService: LoggerService,
    ) {}

    requestForDownload(
        queryParam,
        downloadUrl,
        downloadMethod,
        downloadRequest,
        pageTitle,
        dataLength,
    ) {
        const fileType = 'csv';
        let downloadSubscription;

        try {
            if (dataLength === 0) {
                this.toastObservableService.postMessage("The requested data isn't available");
                return;
            } else if (dataLength > 100000) {
                const message =
                    'We regret to inform you that the download limit is currently set at 100,000 records. To proceed, please refine your filters and try again. Thank you for your understanding.';
                this.showDialog(message, 'Information');
                return;
            } else {
                this.toastObservableService.postMessage('The download has been requested');
            }

            this.animateDownload({ [pageTitle]: true });
            this.compDownloadStatus[pageTitle] = true;
            downloadSubscription = this.downloadData(
                queryParam,
                downloadUrl,
                downloadMethod,
                downloadRequest,
                pageTitle,
            ).subscribe(
                (response) => {
                    this.animateDownload({ [pageTitle]: false });
                    this.compDownloadStatus[pageTitle] = false;
                    downloadSubscription.unsubscribe();
                },
                (error) => {
                    this.loggerService.log('error', error);
                    this.animateDownload({ [pageTitle]: false });
                    this.compDownloadStatus[pageTitle] = false;
                    this.toastObservableService.postMessage('Download failed. Please try later');
                    downloadSubscription.unsubscribe();
                },
            );
        } catch (error) {
            this.animateDownload({ [pageTitle]: false });
            this.compDownloadStatus[pageTitle] = false;
            this.loggerService.log('error', error);
            downloadSubscription.unsubscribe();
            this.toastObservableService.postMessage('Download failed. Please try later');
        }
    }

    showDialog(message, title?) {
        const dialogRef = this.dialog.open(DialogBoxComponent, {
            width: '500px',
            data: {
                title,
                message,
                yesButtonLabel: 'Ok',
            },
        });
    }

    downloadData(
        queryParam,
        downloadUrl,
        downloadMethod,
        downloadRequest,
        pageTitle,
    ): Observable<any> {
        const url = downloadUrl;
        const method = downloadMethod;
        const queryParams = queryParam;
        const payload = downloadRequest;

        try {
            return this.httpService.getBlobResponse(url, method, payload, queryParams).pipe(
                map((response) => {
                    const downloadResponse = response['_body'] || response;

                    const downloadUrlBlob = URL.createObjectURL(downloadResponse);

                    const file = document.createElement('a');
                    file.href = downloadUrlBlob;
                    if (response['type'] === 'text/csv') {
                        file.download = pageTitle + '.csv';
                    } else {
                        file.download = pageTitle + '.xls';
                    }

                    document.body.appendChild(file);
                    file.click();
                    setTimeout(function () {
                        document.body.removeChild(file);
                    }, 10);
                    return 'downloaded';
                }),
            );
        } catch (error) {
            this.errorHandling.handleJavascriptError(error);
        }
    }

    animateDownload(msg: object) {
        this.subject.next(msg);
    }

    getDownloadStatus(): Observable<any> {
        return this.subject.asObservable();
    }

    getComponentDownloadStatus(): object {
        return this.compDownloadStatus;
    }
}
