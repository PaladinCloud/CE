import { Component, EventEmitter, Input, OnInit, Output } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { WorkflowService } from 'src/app/core/services/workflow.service';
import { LoggerService } from 'src/app/shared/services/logger.service';
import { UtilsService } from 'src/app/shared/services/utils.service';

@Component({
    selector: 'app-keyvalue-notification',
    templateUrl: './keyvalue-notification.component.html',
    styleUrls: ['./keyvalue-notification.component.css'],
})
export class KeyvalueNotificationComponent implements OnInit {
    @Input() details: { [key: string]: unknown };
    @Input() title = '';
    windowHostName = '';

    constructor(private router: Router,
        private logger: LoggerService,
        private workflowService: WorkflowService,
        private utils: UtilsService,
        private activatedRoute: ActivatedRoute) {
            this.windowHostName = window.location.origin;
        }

    ngOnInit(): void {}

    navigateTo(link: string) {
        try{
            if (!link || !link.includes(this.windowHostName)) {
                return;
            }
            const urlObj = this.utils.getParamsFromUrlSnippet(link);

            this.workflowService.addRouterSnapshotToLevel(
                this.router.routerState.snapshot.root,
                0,
                "Notification Details",
            );
            const parts = urlObj.url.replace(this.windowHostName, "").split('/');
            const urlToNavigate = parts.slice(0, 5).join('/') + '/' + encodeURIComponent(parts.slice(5).join('/'));
            
            this.router
                .navigate(["../../.." + urlToNavigate], {
                    relativeTo: this.activatedRoute,
                    queryParamsHandling: "merge"
                })
                .then((response) => {
                    this.logger.log('info', 'Successfully navigated to details page: ' + response);
                })
                .catch((error) => {
                    this.logger.log('error', 'Error in navigation - ' + error);
                });
        }catch(e){
            this.logger.log("jsError", e);
        }
    }

    isObject(item) {
        return typeof item.value === 'object' || Array.isArray(item.value);
    }

    isStringDate(str: string){
        // Attempt to create a Date object from the input string
        const date = new Date(str);

        // Check if the Date object is valid and the input string was successfully parsed as a date
        // Also, check if the date is not "Invalid Date" and it is not NaN
        return (
            Object.prototype.toString.call(date) === '[object Date]' &&
            !isNaN(date.getTime()) &&
            date.toString() !== 'Invalid Date'
        );
    }
}
