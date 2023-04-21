import { Component, Input, OnInit } from '@angular/core';

@Component({
    selector: 'app-aws-issue-notification',
    templateUrl: './issue-notification.component.html',
    styleUrls: ['./issue-notification.component.css'],
})
export class AwsIssueNotificationComponent implements OnInit {
    @Input() details: {
        accountname: string;
        _cloudType: string;
        discoverydate: string;
        latestdescription: string;
    };
    @Input() title = '';

    constructor() {}

    ngOnInit(): void {}
}
