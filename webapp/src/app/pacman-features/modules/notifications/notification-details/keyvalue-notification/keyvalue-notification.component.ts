import { Component, Input, OnInit } from '@angular/core';

@Component({
    selector: 'app-keyvalue-notification',
    templateUrl: './keyvalue-notification.component.html',
    styleUrls: ['./keyvalue-notification.component.css'],
})
export class KeyvalueNotificationComponent implements OnInit {
    @Input() details: { [key: string]: unknown };
    constructor() {}

    ngOnInit(): void {}
}
