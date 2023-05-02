import { Component, Input, OnInit } from '@angular/core';

@Component({
    selector: 'app-acs-severity',
    templateUrl: './acs-severity.component.html',
    styleUrls: ['./acs-severity.component.css'],
})
export class AcsSeverityComponent implements OnInit {
    @Input() severity: string;

    constructor() {}

    ngOnInit(): void {}
}
