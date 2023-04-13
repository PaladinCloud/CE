import { Component, EventEmitter, Input, OnInit, Output } from '@angular/core';

export interface OverviewTile {
    mainContent: {
        count: number;
        image: string;
        title: string;
    };
    subContent: {
        count: number;
        title: string;
    };
}

@Component({
    selector: 'app-overview-tile',
    templateUrl: './overview-tile.component.html',
    styleUrls: ['./overview-tile.component.css'],
})
export class OverviewTileComponent implements OnInit {
    @Input() tile: OverviewTile;

    @Output() navigateTo = new EventEmitter<string>();

    constructor() {}

    ngOnInit(): void {}

    redirectTo(title: string) {
        this.navigateTo.emit(title);
    }
}
