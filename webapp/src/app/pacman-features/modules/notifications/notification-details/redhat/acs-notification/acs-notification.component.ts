import { Component, Input, OnInit } from '@angular/core';

@Component({
    selector: 'app-redhat-acs-notification',
    templateUrl: './acs-notification.component.html',
    styleUrls: ['./acs-notification.component.css'],
})
export class RedhatAcsNotificationComponent implements OnInit {
    @Input() details: {
        severity: string;
        lastUpdated: Date;
        goToViolation: string;
        clusterName: string;
        namespace: string;
        description: string;
        id: string;
    };

    @Input() title = '';

    constructor() {}

    ngOnInit(): void {}
}
