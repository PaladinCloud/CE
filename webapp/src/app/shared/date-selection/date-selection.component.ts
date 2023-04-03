import { Component, EventEmitter, OnInit, Output } from '@angular/core';
import {
    DateRange,
    DefaultMatCalendarRangeStrategy,
    MAT_DATE_RANGE_SELECTION_STRATEGY,
} from '@angular/material/datepicker';

@Component({
    selector: 'app-date-selection',
    templateUrl: './date-selection.component.html',
    styleUrls: ['./date-selection.component.css'],
    providers: [
        {
            provide: MAT_DATE_RANGE_SELECTION_STRATEGY,
            useClass: DefaultMatCalendarRangeStrategy,
        },
    ],
})
export class DateSelectionComponent implements OnInit {
    readonly today = new Date();

    selectedRange?: DateRange<Date>;

    @Output() datesSelected = new EventEmitter<{ from: Date; to: Date }>();

    ngOnInit() {}

    customDateSelected() {
        this.datesSelected.emit({
            from: this.selectedRange.start,
            to: this.selectedRange.end,
        });
    }

    selectedDateChange(event: Date) {
        if (!this.selectedRange?.start || this.selectedRange?.end) {
            this.selectedRange = new DateRange<Date>(event, null);
        } else {
            const start = this.selectedRange.start;
            const end = event;
            if (end < start) {
                this.selectedRange = new DateRange<Date>(end, start);
            } else {
                this.selectedRange = new DateRange<Date>(start, end);
            }
        }
    }
}
