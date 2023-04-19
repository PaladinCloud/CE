import { Component, Input, OnInit } from '@angular/core';
import { Router } from '@angular/router';

@Component({
    selector: 'app-keyvalue-notification',
    templateUrl: './keyvalue-notification.component.html',
    styleUrls: ['./keyvalue-notification.component.css'],
})
export class KeyvalueNotificationComponent implements OnInit {
    @Input() details: { [key: string]: unknown };
    @Input() title = '';

    constructor(private router: Router) {}

    ngOnInit(): void {}

    navigateTo(url: string) {
        this.router.navigate([url.startsWith('/') ? url.slice(1) : url], {
            queryParamsHandling: 'merge',
        });
    }
}
