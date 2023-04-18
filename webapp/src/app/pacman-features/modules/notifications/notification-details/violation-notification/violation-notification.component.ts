import { Component, EventEmitter, Input, OnInit, Output } from '@angular/core';

@Component({
    selector: 'app-violation-notification',
    templateUrl: './violation-notification.component.html',
    styleUrls: ['./violation-notification.component.css'],
})
export class ViolationNotificationComponent implements OnInit {
    @Input() details: {
        policyName: string;
        policyNameLink: string;
        issueId: string;
        issueIdLink: string;
        scanTime: Date;
    };

    @Output() navigateTo = new EventEmitter<string>();

    constructor() {}

    ngOnInit(): void {}
}
