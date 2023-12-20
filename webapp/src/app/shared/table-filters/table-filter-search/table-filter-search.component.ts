import { Component, EventEmitter, Input, AfterViewInit, Output, ViewChild, ElementRef, Renderer2 } from '@angular/core';

@Component({
    selector: 'app-table-filter-search',
    templateUrl: './table-filter-search.component.html',
    styleUrls: ['./table-filter-search.component.css'],
})
export class TableFilterSearchComponent implements AfterViewInit {
    @Input() text = '';
    @Output() textChange = new EventEmitter<string>();
    @ViewChild('filterSearchInput', { static: false }) filterSearchInput: ElementRef;

    constructor(private renderer: Renderer2) {}

    ngAfterViewInit(): void {
        // Focus on the input field when the component loads
        this.renderer.selectRootElement(this.filterSearchInput.nativeElement).focus();
    }
}