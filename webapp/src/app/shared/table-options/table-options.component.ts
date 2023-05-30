import { ConnectedPosition } from '@angular/cdk/overlay';
import { Component, EventEmitter, Input, OnInit, Output } from '@angular/core';

@Component({
    selector: 'app-table-options',
    templateUrl: './table-options.component.html',
    styleUrls: ['./table-options.component.css'],
})
export class TableOptionsComponent implements OnInit {
    @Input() isDownloadEnabled = false;
    @Input() isColumnsEnabled = false;
    @Input() availableColumns: string[] = [];
    @Input() set selectedColumns(values) {
        this._selectedColumns = values;
        this.selectedColumnsDict = values.reduce((acc, next) => {
            acc[next] = true;
            return acc;
        }, {});
    }
    get selectedColumns() {
        return this._selectedColumns;
    }

    @Output() download = new EventEmitter<void>();
    @Output() selectColumns = new EventEmitter<string[]>();

    readonly overlayPositions: ConnectedPosition[] = [
        {
            originX: 'start',
            originY: 'bottom',
            overlayX: 'center',
            overlayY: 'top',
            offsetX: -25,
            weight: 2,
        },
        {
            originX: 'start',
            originY: 'bottom',
            overlayX: 'center',
            overlayY: 'bottom',
            offsetX: -25,
            weight: 1,
        },
    ];

    selectedColumnsDict: { [key: string]: boolean } = {};

    isMenuOpen = false;
    isColumnOptionsOpen = false;
    private _selectedColumns: string[] = [];

    constructor() {}

    ngOnInit(): void {}

    overlayKeyDown(event: KeyboardEvent) {
        if (event.key === 'Escape') {
            this.hideMenus();
        }
    }

    openMenu() {
        this.isMenuOpen = !this.isMenuOpen;
    }

    hideMenus() {
        this.isMenuOpen = false;
        this.isColumnOptionsOpen = false;
    }

    selectColumn(column: string, isSelected: boolean) {
        this.selectColumns.emit(
            isSelected
                ? [...this.selectedColumns, column]
                : this.selectedColumns.filter((s) => s !== column),
        );
    }

    selectAllColumns(isAllSelected: boolean) {
        this.selectColumns.emit(isAllSelected ? this.availableColumns : []);
    }
}
