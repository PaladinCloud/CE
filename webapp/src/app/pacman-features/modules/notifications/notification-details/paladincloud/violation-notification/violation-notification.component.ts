import { Component, EventEmitter, Input, OnInit, Output } from '@angular/core';

@Component({
    selector: 'app-paladincloud-violation-notification',
    templateUrl: './violation-notification.component.html',
    styleUrls: ['./violation-notification.component.css'],
})
export class PaladinCloudViolationNotificationComponent implements OnInit {
    @Input() details: {
        policyName: string;
        policyNameLink: string;
        issueId: string;
        issueIdLink: string;
        scanTime: Date;
    };

    @Input() title = '';

    @Output() navigateTo = new EventEmitter<string>();

    constructor() {}

    ngOnInit(): void {}
}
