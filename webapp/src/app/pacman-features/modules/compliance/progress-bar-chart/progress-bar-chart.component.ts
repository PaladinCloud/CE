import { Component, EventEmitter, Input, OnInit, Output } from '@angular/core';

interface BarItem {
    class: string;
    title: string;
    val: number;
}

@Component({
    selector: 'app-progress-bar-chart',
    templateUrl: './progress-bar-chart.component.html',
    styleUrls: ['./progress-bar-chart.component.css'],
})
export class ProgressBarChartComponent implements OnInit {
    @Input() bars: BarItem[] = [];

    @Output() navigateTo = new EventEmitter<BarItem>();

    constructor() {}

    ngOnInit() {}
}
