import { Component, OnDestroy, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { Subscription } from 'rxjs';
import { AssetGroupObservableService } from 'src/app/core/services/asset-group-observable.service';
import { WorkflowService } from 'src/app/core/services/workflow.service';
import { CommonResponseService } from 'src/app/shared/services/common-response.service';
import { LoggerService } from 'src/app/shared/services/logger.service';
import { environment } from 'src/environments/environment';
import * as notificationSchema from '../notifications-schema.json';

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
    eventId: string;
    errorMessage = '';

    eventDetails = [];
    summaryTitle = '';
    notificationDetails = {};
    hasDetails: boolean;

    constructor(
        private activatedRoute: ActivatedRoute,
        private logger: LoggerService,
        private workflowService: WorkflowService,
        private router: Router,
        private commonResponseService: CommonResponseService,
        private assetGroupObservableService: AssetGroupObservableService,
    ) {
        this.backButtonRequired = this.workflowService.checkIfFlowExistsCurrently(this.pageLevel);

        this.assetGroupSubscription = this.assetGroupObservableService
            .getAssetGroup()
            .subscribe((assetGroupName) => {
                this.selectedAssetGroup = assetGroupName;
            });

        this.activatedRoute.queryParams.subscribe((params) => {
            const urlParams = params;
            this.eventId = decodeURIComponent(urlParams.eventId);
        });

        const breadcrumbInfo = this.workflowService.getDetailsFromStorage()['level0'];

        if (breadcrumbInfo) {
            this.breadcrumbArray = breadcrumbInfo.map((item) => item.title);
            this.breadcrumbLinks = breadcrumbInfo.map((item) => item.url);
        }

        this.breadcrumbPresent = 'Notification Details';

        this.getDetails();
    }

    ngOnInit(): void {}

    navigateTo(link: string) {
        if (!link) {
            return;
        }

        this.workflowService.addRouterSnapshotToLevel(
            this.router.routerState.snapshot.root,
            0,
            this.breadcrumbPresent,
        );
        this.router
            .navigate([link], {
                queryParamsHandling: 'merge',
            })
            .then((response) => {
                this.logger.log('info', 'Successfully navigated to details page: ' + response);
            })
            .catch((error) => {
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
            eventId: this.eventId + '',
            // global: this.global,
        };

        try {
            this.getDetailsSubscription = this.commonResponseService
                .getData(Url, Method, {}, queryParams)
                .subscribe(
                    (response) => {
                        try {
                            this.notificationDetails = response;
                            const details = [];
                            this.summaryTitle = response.eventName;
                            const source = response.eventSource?.toLowerCase();
                            const category = response.eventCategory?.toLowerCase(); // "violations" | "autofix" | "exemptions" | "sticky exemptions"
                            const action = response.payload.action?.toLowerCase();

                            const schema = JSON.parse(JSON.stringify(notificationSchema));

                            let detailsSchema = [];
                            if (schema[source] && schema[source][category]) {
                                detailsSchema = [...schema[source][category]['common']];

                                if (schema[source][category][action]) {
                                    detailsSchema = [
                                        ...detailsSchema,
                                        ...schema[source][category][action],
                                    ];
                                }
                            }

                            detailsSchema.forEach((obj) => {
                                let preImgSrc;
                                let value = response.payload[obj.key];
                                if (obj.keyDisplayName == 'Description') {
                                    value = this.formatDescription(value);
                                }
                                if (obj.key == 'severity') {
                                    preImgSrc = `/assets/icons/violations-${value?.toLowerCase()}-icon.svg`;
                                }
                                details.push({
                                    name: obj.keyDisplayName,
                                    value: value,
                                    link: obj.link
                                        ? response.payload[obj.link].replace(
                                              window.location.origin,
                                              '',
                                          )
                                        : '',
                                    preImgSrc: preImgSrc,
                                });
                            });

                            this.eventDetails = details;
                            this.hasDetails = this.eventDetails.length > 0;
                            if (this.eventDetails.length == 0) {
                                this.errorMessage = 'noDataAvailable';
                            } else {
                                this.errorMessage = '';
                            }
                        } catch (e) {
                            this.logger.log('javascript error: ', e);
                            this.errorMessage = 'jsError';
                        }
                    },
                    (error) => {
                        this.errorMessage = 'apiResponseError';
                    },
                );
        } catch (error) {
            this.errorMessage = 'jsError';
        }
    }

    formatDescription(description) {
        description = description.replaceAll('*', '<h6>*');
        description = description.replaceAll('?[NL]', '?</h6>');
        if (
            description.indexOf(
                'https://console.aws.amazon.com/ec2/v2/home?region=us-west-2#Events',
            ) > 0
        ) {
            description = description.replaceAll(
                'https://console.aws.amazon.com/ec2/v2/home?region=us-west-2#Events',
                '<a href ="https://console.aws.amazon.com/ec2/v2/home?region=us-west-2#Events" target="_blank">https://console.aws.amazon.com/ec2/v2/home?region=us-west-2#Events</a>',
            );
        }
        if (
            description.indexOf(
                'https://docs.aws.amazon.com/AWSEC2/latest/UserGuide/monitoring-instances-status-check_sched.html#schedevents_actions_reboot',
            ) > 0
        ) {
            description = description.replaceAll(
                'https://docs.aws.amazon.com/AWSEC2/latest/UserGuide/monitoring-instances-status-check_sched.html#schedevents_actions_reboot',
                '<a href ="https://docs.aws.amazon.com/AWSEC2/latest/UserGuide/monitoring-instances-status-check_sched.html#schedevents_actions_reboot" target="_blank">https://docs.aws.amazon.com/AWSEC2/latest/UserGuide/monitoring-instances-status-check_sched.html#schedevents_actions_reboot</a>',
            );
        }
        if (
            description.indexOf(
                'at https://docs.aws.amazon.com/AWSEC2/latest/UserGuide/monitoring-instances-status-check_sched.html',
            ) > 0
        ) {
            description = description.replaceAll(
                'at https://docs.aws.amazon.com/AWSEC2/latest/UserGuide/monitoring-instances-status-check_sched.html',
                '<a href ="https://docs.aws.amazon.com/AWSEC2/latest/UserGuide/monitoring-instances-status-check_sched.html" target="_blank">https://docs.aws.amazon.com/AWSEC2/latest/UserGuide/monitoring-instances-status-check_sched.html</a>',
            );
        }
        if (
            description.indexOf(
                'https://docs.aws.amazon.com/AWSEC2/latest/UserGuide/Stop_Start.html',
            ) > 0
        ) {
            description = description.replaceAll(
                'https://docs.aws.amazon.com/AWSEC2/latest/UserGuide/Stop_Start.html',
                '<a href ="https://docs.aws.amazon.com/AWSEC2/latest/UserGuide/Stop_Start.html" target="_blank">https://docs.aws.amazon.com/AWSEC2/latest/UserGuide/Stop_Start.html</a>',
            );
        }
        if (
            description.indexOf(
                'https://console.aws.amazon.com/ec2/v2/home?region=us-east-1#Events',
            ) > 0
        ) {
            description = description.replaceAll(
                'https://console.aws.amazon.com/ec2/v2/home?region=us-east-1#Events',
                '<a href ="https://console.aws.amazon.com/ec2/v2/home?region=us-east-1#Events" target="_blank">https://console.aws.amazon.com/ec2/v2/home?region=us-east-1#Events</a>',
            );
        }
        if (description.indexOf('[3] https://github.com/awslabs/aws-vpn-migration-scripts') > 0) {
            description = description.replaceAll(
                '[3] https://github.com/awslabs/aws-vpn-migration-scripts',
                '<a href ="https://github.com/awslabs/aws-vpn-migration-scripts" target="_blank">https://github.com/awslabs/aws-vpn-migration-scripts</a>',
            );
        }
        if (
            description.indexOf(
                'http://docs.aws.amazon.com/AWSEC2/latest/UserGuide/instance-retirement.html',
            ) > 0
        ) {
            description = description.replaceAll(
                ' http://docs.aws.amazon.com/AWSEC2/latest/UserGuide/instance-retirement.html',
                '<a href =" http://docs.aws.amazon.com/AWSEC2/latest/UserGuide/instance-retirement.html" target="_blank">http://docs.aws.amazon.com/AWSEC2/latest/UserGuide/instance-retirement.html</a>',
            );
        }
        if (description.indexOf('(https://aws.amazon.com/support)') > 0) {
            description = description.replaceAll(
                '(https://aws.amazon.com/support)',
                '<a href ="https://aws.amazon.com/support" target="_blank">https://aws.amazon.com/support</a>',
            );
        }
        if (description.indexOf('(http://aws.amazon.com/support)') > 0) {
            description = description.replaceAll(
                '(http://aws.amazon.com/support)',
                '<a href ="http://aws.amazon.com/support" target="_blank">http://aws.amazon.com/support</a>',
            );
        }
        if (description.indexOf('http://aws.amazon.com/support') > 0) {
            description = description.replaceAll(
                'http://aws.amazon.com/support',
                '<a href ="http://aws.amazon.com/support" target="_blank">http://aws.amazon.com/support</a>',
            );
        }
        if (description.indexOf('[1] https://console.aws.amazon.com') > 0) {
            description = description.replaceAll(
                '[1] https://console.aws.amazon.com',
                '<a href ="https://console.aws.amazon.com" target="_blank">https://console.aws.amazon.com</a>',
            );
        }
        if (description.indexOf('http://aws.amazon.com/architecture') > 0) {
            description = description.replaceAll(
                'http://aws.amazon.com/architecture',
                '<a href ="http://aws.amazon.com/architecture" target="_blank">http://aws.amazon.com/architecture</a>',
            );
        }
        if (description.indexOf('https://aws.amazon.com/support') > 0) {
            description = description.replaceAll(
                'https://aws.amazon.com/support',
                '<a href ="https://aws.amazon.com/support" target="_blank">https://aws.amazon.com/support</a>',
            );
        }
        if (
            description.indexOf(
                'https://aws.amazon.com/premiumsupport/knowledge-center/migrate-classic-vpn-new',
            ) > 0
        ) {
            description = description.replaceAll(
                'https://aws.amazon.com/premiumsupport/knowledge-center/migrate-classic-vpn-new',
                '<a href ="https://aws.amazon.com/premiumsupport/knowledge-center/migrate-classic-vpn-new" target="_blank">https://aws.amazon.com/premiumsupport/knowledge-center/migrate-classic-vpn-new</a>',
            );
        }
        if (
            description.indexOf(
                'https://docs.aws.amazon.com/amazon-mq/latest/developer-guide/using-amazon-mq-securely.html#amazon-mq-vpc-security-groups',
            ) > 0
        ) {
            description = description.replaceAll(
                'https://docs.aws.amazon.com/amazon-mq/latest/developer-guide/using-amazon-mq-securely.html#amazon-mq-vpc-security-groups',
                '<a href ="https://docs.aws.amazon.com/amazon-mq/latest/developer-guide/using-amazon-mq-securely.html#amazon-mq-vpc-security-groups" target="_blank">https://docs.aws.amazon.com/amazon-mq/latest/developer-guide/using-amazon-mq-securely.html#amazon-mq-vpc-security-groups</a>',
            );
        }
        if (
            description.indexOf(
                '[3] http://activemq.apache.org/security-advisories.data/CVE-2019-0222-announcement.txt',
            ) > 0
        ) {
            description = description.replaceAll(
                '[3] http://activemq.apache.org/security-advisories.data/CVE-2019-0222-announcement.txt',
                '<a href ="http://activemq.apache.org/security-advisories.data/CVE-2019-0222-announcement.txt" target="_blank">http://activemq.apache.org/security-advisories.data/CVE-2019-0222-announcement.txt</a>',
            );
        }
        if (
            description.indexOf(
                'https://docs.aws.amazon.com/amazon-mq/latest/developer-guide/amazon-mq-editing-broker-preferences.html',
            ) > 0
        ) {
            description = description.replaceAll(
                'https://docs.aws.amazon.com/amazon-mq/latest/developer-guide/amazon-mq-editing-broker-preferences.html',
                '<a href ="https://docs.aws.amazon.com/amazon-mq/latest/developer-guide/amazon-mq-editing-broker-preferences.html" target="_blank">https://docs.aws.amazon.com/amazon-mq/latest/developer-guide/amazon-mq-editing-broker-preferences.html</a>',
            );
        }
        if (
            description.indexOf(
                'https://docs.aws.amazon.com/vpn/latest/s2svpn/VPC_VPN.html#vpn-categories',
            ) > 0
        ) {
            description = description.replaceAll(
                'https://docs.aws.amazon.com/vpn/latest/s2svpn/VPC_VPN.html#vpn-categories',
                '<a href ="https://docs.aws.amazon.com/vpn/latest/s2svpn/VPC_VPN.html#vpn-categories" target="_blank">https://docs.aws.amazon.com/vpn/latest/s2svpn/VPC_VPN.html#vpn-categories</a>',
            );
        }
        if (description.indexOf('https://nodejs.org/en/blog/release/v6.9.0/') > 0) {
            description = description.replaceAll(
                'https://nodejs.org/en/blog/release/v6.9.0/',
                '<a href ="https://nodejs.org/en/blog/release/v6.9.0" target="_blank">https://nodejs.org/en/blog/release/v6.9.0/</a>',
            );
        }
        if (
            description.indexOf(
                'https://docs.aws.amazon.com/lambda/latest/dg/runtime-support-policy.html',
            ) > 0
        ) {
            description = description.replaceAll(
                'https://docs.aws.amazon.com/lambda/latest/dg/runtime-support-policy.html',
                '<a href ="https://docs.aws.amazon.com/lambda/latest/dg/runtime-support-policy.html" target="_blank">https://docs.aws.amazon.com/lambda/latest/dg/runtime-support-policy.html</a>',
            );
        }
        if (
            description.indexOf(
                'https://aws.amazon.com/blogs/compute/node-js-8-10-runtime-now-available-in-aws-lambda/',
            ) > 0
        ) {
            description = description.replaceAll(
                'https://aws.amazon.com/blogs/compute/node-js-8-10-runtime-now-available-in-aws-lambda/',
                '<a href ="https://aws.amazon.com/blogs/compute/node-js-8-10-runtime-now-available-in-aws-lambda" target="_blank">https://aws.amazon.com/blogs/compute/node-js-8-10-runtime-now-available-in-aws-lambda</a>',
            );
        }
        if (
            description.indexOf(
                'https://aws.amazon.com/blogs/compute/upcoming-updates-to-the-aws-lambda-execution-environment/',
            ) > 0
        ) {
            description = description.replaceAll(
                'https://aws.amazon.com/blogs/compute/upcoming-updates-to-the-aws-lambda-execution-environment/',
                '<a href ="https://aws.amazon.com/blogs/compute/upcoming-updates-to-the-aws-lambda-execution-environment" target="_blank">https://aws.amazon.com/blogs/compute/upcoming-updates-to-the-aws-lambda-execution-environment</a>',
            );
        }
        if (
            description.indexOf(
                'https://aws.amazon.com/blogs/compute/updated-timeframe-for-the-upcoming-aws-lambda-and-aws-lambdaedge-execution-environment-update/',
            ) > 0
        ) {
            description = description.replaceAll(
                'https://aws.amazon.com/blogs/compute/updated-timeframe-for-the-upcoming-aws-lambda-and-aws-lambdaedge-execution-environment-update/',
                '<a href ="https://aws.amazon.com/blogs/compute/updated-timeframe-for-the-upcoming-aws-lambda-and-aws-lambdaedge-execution-environment-update" target="_blank">https://aws.amazon.com/blogs/compute/updated-timeframe-for-the-upcoming-aws-lambda-and-aws-lambdaedge-execution-environment-update</a>',
            );
        }

        description = description
            .replaceAll('[1]', '')
            .replaceAll('[2]', '')
            .replaceAll('[3]', '')
            .replaceAll('[4]', '')
            .replaceAll('[NL][NL]', '<br><br>')
            .replaceAll('(https:', '<a href="https:')
            .replaceAll('[NL]', '<br>')
            .replaceAll(' [3]', '')
            .replaceAll('[NL]', '<br>');

        // description = description.replace("[", "<h1>").replace("]", "</h1>");
        return description;
    }

    ngOnDestroy(): void {
        if (this.getDetailsSubscription) this.getDetailsSubscription.unsubscribe();
        if (this.routeSubscription) this.routeSubscription.unsubscribe();
        if (this.assetGroupSubscription) this.assetGroupSubscription.unsubscribe();
    }
}
