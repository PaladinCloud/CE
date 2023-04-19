import { Component, Input, OnInit } from '@angular/core';

@Component({
    selector: 'app-json-notification',
    templateUrl: './json-notification.component.html',
    styleUrls: ['./json-notification.component.css'],
})
export class JsonNotificationComponent implements OnInit {
    @Input() details: { [key: string]: unknown };
    @Input() title = '';

    constructor() {}

    ngOnInit(): void {}
}
