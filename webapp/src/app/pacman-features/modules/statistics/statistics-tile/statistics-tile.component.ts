import { animate, style, transition, trigger } from '@angular/animations';
import { Component, Input, OnInit } from '@angular/core';

@Component({
    selector: 'app-statistics-tile',
    templateUrl: './statistics-tile.component.html',
    styleUrls: ['./statistics-tile.component.css'],
    animations: [
        trigger('fadeInAnimation', [
            transition(':enter', [
                style({
                    opacity: 0,
                    width: 0,
                    overflow: 'hidden',
                    'white-space': 'nowrap',
                    'text-overflow': 'clip',
                }),
                animate('350ms ease-out', style({ opacity: 1, width: '*' })),
            ]),
        ]),
    ],
})
export class StatisticsTileComponent implements OnInit {
    @Input() title: string;
    @Input() subTitle: string;
    @Input() icon: string;
    @Input() counter: number;
    @Input() isLoading = false;

    constructor() {}

    ngOnInit(): void {}
}
