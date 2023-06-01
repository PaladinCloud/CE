import { Component, Input, OnInit } from '@angular/core';

@Component({
    selector: 'app-statistics-tile',
    templateUrl: './statistics-tile.component.html',
    styleUrls: ['./statistics-tile.component.css'],
})
export class StatisticsTileComponent implements OnInit {
    @Input() title: string;
    @Input() subTitle = ' ';
    @Input() icon: string;
    @Input() counter: number;
    @Input() isLoading = false;

    constructor() {}

    ngOnInit(): void {}
}
