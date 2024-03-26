import { Component, EventEmitter, Input, OnInit, Output } from '@angular/core';

@Component({
    selector: 'app-multi-tab-switcher',
    templateUrl: './multi-tab-switcher.component.html',
    styleUrls: ['./multi-tab-switcher.component.css'],
})
export class MultiTabSwitcherComponent implements OnInit {
    @Input() tabs;
    @Input() tabSelected;

    @Output() switchView = new EventEmitter();

    constructor() {}

    ngOnInit(): void {}

    handleSwitchView(e) {
        this.switchView.emit(e);
    }
}
