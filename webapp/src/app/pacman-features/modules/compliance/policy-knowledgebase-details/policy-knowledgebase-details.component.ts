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

import { Component, OnInit, OnDestroy, ViewChild, TemplateRef } from '@angular/core';
import { Subscription } from 'rxjs';
import { AssetGroupObservableService } from '../../../../core/services/asset-group-observable.service';
import { environment } from './../../../../../environments/environment';
import { Router, ActivatedRoute } from '@angular/router';
import { AutorefreshService } from '../../../services/autorefresh.service';
import { LoggerService } from '../../../../shared/services/logger.service';
import { ErrorHandlingService } from '../../../../shared/services/error-handling.service';
import { WorkflowService } from '../../../../core/services/workflow.service';
import { CommonResponseService } from '../../../../shared/services/common-response.service';
import { MatDialog } from '@angular/material/dialog';
import { DialogBoxComponent } from 'src/app/shared/components/molecules/dialog-box/dialog-box.component';
import { AdminService } from 'src/app/pacman-features/services/all-admin.service';
import { PermissionGuardService } from 'src/app/core/services/permission-guard.service';
import { NotificationObservableService } from 'src/app/shared/services/notification-observable.service';
import { DATA_MAPPING } from 'src/app/shared/constants/data-mapping';
import { AssetTypeMapService } from 'src/app/core/services/asset-type-map.service';

interface PolicyData {
    alexaKeyword: string;
    allowList: any;
    assetGroup: string;
    autoFixAvailable: string;
    autoFixEnabled: string;
    category: string;
    createdDate: Date;
    elapsedTime: number;
    fixMailSubject: string;
    fixMessage: string;
    fixType: string;
    maxEmailNotification: number;
    modifiedDate: Date;
    policyArn: string;
    policyDesc: string;
    policyDisplayName: string;
    policyExecutable: string;
    policyFrequency: string;
    policyId: string;
    policyName: string;
    policyParams: string;
    policyRestUrl: string;
    policyType: string;
    policyUUID: string;
    resolution: string;
    resolutionUrl: string;
    severity: string;
    status: string;
    targetType: string;
    templateColumns: any;
    templateName: string;
    userId: string;
    violationMessage: string;
    waitingTime: number;
    warningMailSubject: string;
    warningMessage: string;
}

interface PolicyParams {
    key: string;
    value: string;
    encrypt: boolean;
    isEdit?: boolean;
    isMandatory?: boolean;
    description?: string;
    defaultVal?: string;
    displayName?: string;
    isValueNew?: boolean;
}

enum ActionType {
    EDIT = 'Edit',
    ENABLE = 'Enable',
    DISABLE = 'Disable',
}

@Component({
    selector: 'app-policy-knowledgebase-details',
    templateUrl: './policy-knowledgebase-details.component.html',
    styleUrls: ['./policy-knowledgebase-details.component.css'],
    providers: [
        LoggerService,
        ErrorHandlingService,
        CommonResponseService,
        AutorefreshService,
        AdminService,
    ],
})
export class PolicyKnowledgebaseDetailsComponent implements OnInit, OnDestroy {
    @ViewChild('actionRef') actionRef: TemplateRef<any>;
    pageTitle = 'Policy Details';
    backButtonRequired: boolean;
    breadcrumbPresent: string;
    breadcrumbArray = ['Policy Knowledgebase'];
    breadcrumbLinks = ['policy-knowledgebase'];
    actionItems = [ActionType.DISABLE, ActionType.EDIT]; // TODO: add "Remove"
    selectedAssetGroup: string;
    haveAdminPageAccess = false;
    haveEditAccess = false;
    autoFix = false;
    policyID = '';
    dataComing = true;
    showLoader = true;
    seekdata = false;
    errorMessage: string;
    policyDescription = '';
    resolutionUrl: string;
    policyDetails: PolicyData;
    allpolicyParams: PolicyParams[];
    isAutofixEnabled = false;
    policyDisplayName: string;
    selectedSeverity: string;
    selectedCategory: string;
    assetGroup;
    status: string;
    assetType: string;
    action: string;
    assetTypeMap: any;
    private dataSubscriber: Subscription;
    private subscriptionToAssetGroup: Subscription;
    private routeSubscription: Subscription;
    private pageLevel = 0;

    constructor(
        private assetGroupObservableService: AssetGroupObservableService,
        private router: Router,
        private activatedRoute: ActivatedRoute,
        private commonResponseService: CommonResponseService,
        private logger: LoggerService,
        private errorHandling: ErrorHandlingService,
        private workflowService: WorkflowService,
        public dialog: MatDialog,
        private adminService: AdminService,
        private permissions: PermissionGuardService,
        private notificationObservableService: NotificationObservableService,
        private assetTypeMapService: AssetTypeMapService,
    ) {
        this.subscriptionToAssetGroup = this.assetGroupObservableService
            .getAssetGroup()
            .subscribe((assetGroupName) => {
                this.backButtonRequired = this.workflowService.checkIfFlowExistsCurrently(
                    this.pageLevel,
                );
                this.selectedAssetGroup = assetGroupName;
                this.updateComponent();
            });
    }

    ngOnInit() {
        try {
            this.haveAdminPageAccess = this.permissions.checkAdminPermission();
            const breadcrumbInfo = this.workflowService.getDetailsFromStorage()['level0'];

            if (breadcrumbInfo) {
                this.breadcrumbArray = breadcrumbInfo.map((item) => item.title);
                this.breadcrumbLinks = breadcrumbInfo.map((item) => item.url);
            }
            this.breadcrumbPresent = 'Policy Details ';
            if (this.breadcrumbArray[0] == 'Policies') {
                this.haveEditAccess = true;
            }
        } catch (error) {
            this.logger.log('error', error);
        }
    }

    /* Function to repaint component */
    updateComponent() {
        /* All functions variables which are required to be set for component to be reloaded should go here */
        this.seekdata = false;
        this.dataComing = false;
        this.showLoader = true;
        this.getData();
    }

    /* Function to get Data */
    getData() {
        /* All functions to get data should go here */
        this.getpolicyId();
        this.getProgressData();
    }

    /**
     * this funticn gets the policyid from the url
     */
    getpolicyId() {
        /*  TODO:Trinanjan Wrong way of doing it */
        this.routeSubscription = this.activatedRoute.params.subscribe((params) => {
            this.policyID = params['policyID'];
            this.autoFix = params['autoFix'] === 'true';
        });
    }

    getProgressData() {
        if (!this.policyID) {
            return;
        }
        if (this.dataSubscriber) {
            this.dataSubscriber.unsubscribe();
        }
        const queryParams = {
            policyId: this.policyID,
        };
        const getPolicyByIdUrl = environment.getPolicyById.url;
        const getPolicyByIdMethod = environment.getPolicyById.method;
        try {
            this.dataSubscriber = this.commonResponseService
                .getData(getPolicyByIdUrl, getPolicyByIdMethod, {}, queryParams)
                .subscribe(
                    (response) => {
                        try {
                            if (response) {
                                this.showLoader = false;
                                this.seekdata = false;
                                this.dataComing = true;
                                this.processData(response);
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
        } catch (error) {
            this.errorMessage = this.errorHandling.handleJavascriptError(error);
            this.getErrorValues();
        }
    }

    getErrorValues() {
        this.showLoader = false;
        this.dataComing = false;
        this.seekdata = true;
    }

    processData(data: PolicyData) {
        this.policyDetails = data;
        this.policyDescription = data.policyDesc;
        this.resolutionUrl = data.resolutionUrl || 'https://github.com/PaladinCloud/CE/wiki/Policy';
        this.policyDisplayName = data.policyDisplayName;
        this.selectedCategory =
            data.category == 'Governance' ? 'Operations' : this.uppercasefirst(data.category);
        this.selectedSeverity = this.uppercasefirst(data.severity);
        this.status = this.uppercasefirst(data.status);

        const policyParams = JSON.parse(this.policyDetails.policyParams);
        this.allpolicyParams = Array.isArray(policyParams.params)
            ? (policyParams.params as PolicyParams[]).filter(
                  (p) => p.key !== 'severity' && p.key !== 'policyCategory',
              )
            : [];

        if (policyParams.autofix) {
            this.isAutofixEnabled = policyParams.autofix;
        }

        this.actionItems = [
            ActionType.EDIT,
            this.status.toLowerCase() === 'enabled' ? ActionType.DISABLE : ActionType.ENABLE,
        ];

        this.assetTypeMapService.getAssetMap().subscribe((assetTypeMap) => {
            this.assetTypeMap = assetTypeMap;
            this.assetType = this.assetTypeMap.get(this.policyDetails.targetType);
        });

        this.assetGroup =
            !!this.policyDetails.assetGroup && DATA_MAPPING[this.policyDetails.assetGroup]
                ? DATA_MAPPING[this.policyDetails.assetGroup]
                : this.uppercasefirst(this.policyDetails.assetGroup);
    }

    /**
     * This function returns the first char as upper case
     */
    uppercasefirst(value: string | null) {
        if (value === null) {
            return 'Not assigned';
        }
        value = value.toLocaleLowerCase();
        return value.charAt(0).toUpperCase() + value.slice(1);
    }

    confirmAction(action: string) {
        this.action = action;
        const dialogRef = this.dialog.open(DialogBoxComponent, {
            width: '600px',
            data: {
                title: null,
                yesButtonLabel: action,
                noButtonLabel: 'Cancel',
                template: this.actionRef,
            },
        });
        dialogRef.afterClosed().subscribe((result) => {
            try {
                if (result == 'yes') {
                    this.enableDisableRuleOrJob(action);
                }
            } catch (error) {
                this.errorMessage = this.errorHandling.handleJavascriptError(error);
                this.logger.log('error', error);
            }
        });
    }

    enableDisableRuleOrJob(action: string) {
        if (!this.haveAdminPageAccess) {
            return;
        }
        try {
            const url = environment.enableDisableRuleOrJob.url;
            const method = environment.enableDisableRuleOrJob.method;
            const params = {
                policyId: this.policyID,
                action,
            };

            this.adminService.executeHttpAction(url, method, {}, params).subscribe(
                (response) => {
                    // change status
                    this.status = action + 'd';

                    // change actions list
                    this.actionItems = [
                        ActionType.EDIT,
                        this.status.toLowerCase() === 'enabled'
                            ? ActionType.DISABLE
                            : ActionType.ENABLE,
                    ];

                    const snackbarText = `Policy "${
                        this.policyDisplayName
                    }" ${this.status.toLowerCase()} successfully`;
                    this.openSnackBar(snackbarText, 'check-circle');
                    this.getData();
                },
                (error) => {
                    this.logger.log('error', error);
                },
            );
        } catch (error) {
            // this.errorMessage = this.errorHandling.handleJavascriptError(error);
            this.logger.log('error', error);
        }
    }

    openSnackBar(message: string, iconSrc: string) {
        this.notificationObservableService.postMessage(message, 3 * 1000, 'success', iconSrc);
    }

    goToDetails(event: string) {
        const action = event;
        if (action == 'Enable' || action == 'Disable') {
            this.confirmAction(action);
            return;
        }

        try {
            this.workflowService.addRouterSnapshotToLevel(
                this.router.routerState.snapshot.root,
                0,
                this.pageTitle,
            );
            if (action && action === 'Edit') {
                this.router.navigate(['/pl/admin/policies/create-edit-policy'], {
                    relativeTo: this.activatedRoute,
                    queryParamsHandling: 'merge',
                    queryParams: {
                        policyId: this.policyID,
                    },
                });
            }
        } catch (error) {
            this.errorMessage = this.errorHandling.handleJavascriptError(error);
            this.logger.log('error', error);
        }
    }

    /*
     * unsubscribing component
     */
    ngOnDestroy() {
        try {
            if (this.subscriptionToAssetGroup) {
                this.subscriptionToAssetGroup.unsubscribe();
            }
            if (this.dataSubscriber) {
                this.dataSubscriber.unsubscribe();
            }
            if (this.routeSubscription) {
                this.routeSubscription.unsubscribe();
            }
        } catch (error) {
            this.logger.log('info', '--- Error while unsubscribing ---');
        }
    }
}
