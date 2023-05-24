import { Component, EventEmitter, Input, OnInit, Output } from '@angular/core';

@Component({
    selector: 'app-table-filter-search',
    templateUrl: './table-filter-search.component.html',
    styleUrls: ['./table-filter-search.component.css'],
})
export class TableFilterSearchComponent implements OnInit {
    @Input() text = '';
    @Output() textChange = new EventEmitter<string>();

    constructor() {}

    ngOnInit(): void {}
}
