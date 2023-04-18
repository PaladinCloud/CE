import { Component, Input, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { WorkflowService } from 'src/app/core/services/workflow.service';

@Component({
    selector: 'app-keyvalue-notification',
    templateUrl: './keyvalue-notification.component.html',
    styleUrls: ['./keyvalue-notification.component.css'],
})
export class KeyvalueNotificationComponent implements OnInit {
    @Input() details: { [key: string]: unknown };
    constructor(private router: Router, private workflowService: WorkflowService) {}

    ngOnInit(): void {}

    navigateTo(url: string) {
        this.router.navigate([url.startsWith('/') ? url.slice(1) : url], {
            queryParamsHandling: 'merge',
        });
    }
}
