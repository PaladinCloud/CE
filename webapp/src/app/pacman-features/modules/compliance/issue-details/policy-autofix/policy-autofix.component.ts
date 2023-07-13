import { Component, Input, OnInit } from '@angular/core';

export interface AutofixDetails {
    name: string;
    status: string;
    endDate: Date;
    playItemsCount: number;
}

@Component({
    selector: 'app-policy-autofix',
    templateUrl: './policy-autofix.component.html',
    styleUrls: ['./policy-autofix.component.css'],
})
export class PolicyAutofixComponent implements OnInit {
    @Input() autofixDetails: AutofixDetails;

    constructor() {}

    ngOnInit(): void {}
}
