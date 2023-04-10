import {Component, OnDestroy, OnInit} from '@angular/core';
import {WorkflowService} from 'src/app/core/services/workflow.service';
import {CommonResponseService} from 'src/app/shared/services/common-response.service';
import {environment} from 'src/environments/environment';
import {ActivatedRoute, Router} from "@angular/router";
import {Subscription} from "rxjs";
import {AssetGroupObservableService} from 'src/app/core/services/asset-group-observable.service';
import * as notificationSchema from "../notifications-schema.json";
import {LoggerService} from 'src/app/shared/services/logger.service';


@Component({
    selector: 'app-notification-details',
    templateUrl: './notification-details.component.html',
    styleUrls: ['./notification-details.component.css'],
})
export class NotificationDetailsComponent implements OnInit, OnDestroy {
    routeSubscription: Subscription;
    getDetailsSubscription: Subscription;
    assetGroupSubscription: Subscription;

    backButtonRequired: boolean;
    pageLevel = 0;
    breadcrumbArray: string[] = [];
    breadcrumbLinks: string[] = [];
    breadcrumbPresent: string;
    selectedAssetGroup: any;
    eventId: any;
    errorMessage: string = '';

    eventDetails = [];
    summaryTitle = "";
    notificationDetails = {};
    hasDetails;

    constructor(
        private activatedRoute: ActivatedRoute,
        private logger: LoggerService,
        private workflowService: WorkflowService,
        private router: Router,
        private commonResponseService: CommonResponseService,
        private assetGroupObservableService: AssetGroupObservableService
    ) {
        this.backButtonRequired = this.workflowService.checkIfFlowExistsCurrently(this.pageLevel);

        this.assetGroupSubscription = this.assetGroupObservableService
            .getAssetGroup()
            .subscribe((assetGroupName) => {
                this.selectedAssetGroup = assetGroupName;
            });

        this.activatedRoute.queryParams.subscribe(params => {
            const urlParams = params;
            this.eventId = decodeURIComponent(urlParams.eventId);
        });

        const breadcrumbInfo = this.workflowService.getDetailsFromStorage()["level0"];

        if (breadcrumbInfo) {
            this.breadcrumbArray = breadcrumbInfo.map(item => item.title);
            this.breadcrumbLinks = breadcrumbInfo.map(item => item.url);
        }

        this.breadcrumbPresent = "Notification Details";

        this.getDetails();
    }

    ngOnInit(): void {
    }

    navigateTo(link) {
        if (!link) {
            return;
        }

        this.workflowService.addRouterSnapshotToLevel(this.router.routerState.snapshot.root, 0, this.breadcrumbPresent);
        this.router.navigate(
            [link], 
            {
                "queryParamsHandling": "merge"
            }).then(response => {
            this.logger.log('info', 'Successfully navigated to details page: ' + response);
        })
            .catch(error => {
                this.logger.log('error', 'Error in navigation - ' + error);
            });
    }

    getDetails() {
        if (this.getDetailsSubscription) {
            this.getDetailsSubscription.unsubscribe();
        }

        const Url = environment.getEventDetails.url;
        const Method = environment.getEventDetails.method;
        const queryParams = {
            ag: this.selectedAssetGroup,
            eventId: this.eventId + "",
            // global: this.global,
        };

        try {
            this.getDetailsSubscription = this.commonResponseService
                .getData(Url, Method, {}, queryParams)
                .subscribe(
                    (response) => {
                        try {
                            this.notificationDetails = response;
                            let details = [];
                            this.summaryTitle = response.eventName;
                            const source = response.eventSource?.toLowerCase();
                            const category = response.eventCategory?.toLowerCase(); // "violations" | "autofix" | "exemptions" | "sticky exemptions"
                            const action = response.payload.action?.toLowerCase();

                            const schema = JSON.parse(JSON.stringify(notificationSchema));

                            let detailsSchema = [];
                            if (schema[source] && schema[source][category]) {
                                detailsSchema = [
                                    ...schema[source][category]["common"]
                                ]

                                if (schema[source][category][action]) {
                                    detailsSchema = [
                                        ...detailsSchema,
                                        ...schema[source][category][action]
                                    ]
                                }
                            }

                            detailsSchema.forEach(obj => {
                                const value = response.payload[obj.key];
                                let preImgSrc;
                                if (obj.key == "severity") {
                                    preImgSrc = `/assets/icons/violations-${value?.toLowerCase()}-icon.svg`
                                }

                                details.push({
                                    name: obj.keyDisplayName,
                                    value: value,
                                    link: obj.link ? response.payload[obj.link].replace(window.location.origin, "") : '',
                                    preImgSrc: preImgSrc
                                })
                            })

                            this.eventDetails = details;
                            this.hasDetails = this.eventDetails.length > 0;
                            if (this.eventDetails.length == 0) {
                                this.errorMessage = 'noDataAvailable';
                            } else {
                                this.errorMessage = '';
                            }
                        } catch (e) {
                            this.logger.log("javascript error: ", e);
                            this.errorMessage = "jsError";
                        }
                    },
                    (error) => {
                        this.errorMessage = 'apiResponseError';
                    }
                );
        } catch (error) {
            this.errorMessage = 'jsError';
        }
    }

    ngOnDestroy(): void {
        if (this.getDetailsSubscription) this.getDetailsSubscription.unsubscribe();
        if (this.routeSubscription) this.routeSubscription.unsubscribe();
        if (this.assetGroupSubscription) this.assetGroupSubscription.unsubscribe();
    }
}
