import { Component, OnDestroy, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { Subject } from 'rxjs';
import { takeUntil } from 'rxjs/operators';
import { AssetGroupObservableService } from 'src/app/core/services/asset-group-observable.service';
import { WorkflowService } from 'src/app/core/services/workflow.service';
import { CommonResponseService } from 'src/app/shared/services/common-response.service';
import { LoggerService } from 'src/app/shared/services/logger.service';
import { environment } from 'src/environments/environment';
import { CloudNotification, LayoutService, LayoutType } from '../layout.service';

@Component({
    selector: 'app-notification-details',
    templateUrl: './notification-details.component.html',
    styleUrls: ['./notification-details.component.css'],
    providers: [LayoutService],
})
export class NotificationDetailsComponent implements OnInit, OnDestroy {
    private destroy$ = new Subject<void>();

    backButtonRequired: boolean;
    pageLevel = 0;
    breadcrumbArray: string[] = [];
    breadcrumbLinks: string[] = [];
    breadcrumbPresent: string;
    selectedAssetGroup: any;
    eventId: string;
    errorMessage = '';

    hasDetails: boolean;

    readonly LAYOUTTYPE = LayoutType;
    notificationlayoutType: LayoutType;

    eventName: string;

    notificationDetails: { [key: string]: unknown } = {};

    constructor(
        private activatedRoute: ActivatedRoute,
        private assetGroupObservableService: AssetGroupObservableService,
        private commonResponseService: CommonResponseService,
        private layoutService: LayoutService,
        private logger: LoggerService,
        private router: Router,
        private workflowService: WorkflowService,
    ) {
        this.backButtonRequired = this.workflowService.checkIfFlowExistsCurrently(this.pageLevel);

        this.assetGroupObservableService
            .getAssetGroup()
            .pipe(takeUntil(this.destroy$))
            .subscribe((assetGroupName) => {
                this.selectedAssetGroup = assetGroupName;
            });

        this.activatedRoute.queryParams.pipe(takeUntil(this.destroy$)).subscribe((params) => {
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
        const url = environment.getEventDetails.url;
        const method = environment.getEventDetails.method;
        const queryParams = {
            ag: this.selectedAssetGroup,
            eventId: this.eventId + '',
        };

        this.commonResponseService
            .getData(url, method, {}, queryParams)
            .subscribe((response: CloudNotification) => {
                this.notificationlayoutType = this.layoutService.getLayoutType(response);
                this.eventName = response.eventName;
                this.notificationDetails = response.payload;
            });
    }

    ngOnDestroy(): void {
        this.destroy$.next();
        this.destroy$.complete();
    }
}
