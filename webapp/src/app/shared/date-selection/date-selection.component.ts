import {
    Component,
    EventEmitter,
    Input,
    OnChanges,
    OnInit,
    Output,
    SimpleChanges,
} from '@angular/core';
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
export class DateSelectionComponent implements OnInit, OnChanges {
    readonly today = new Date();

    @Input('minDate') minDate: Date;
    @Input() maxDate: Date = this.today;

    @Input() selectedRange?: DateRange<Date>;

    @Output() datesSelected = new EventEmitter<{ from: Date; to: Date }>();

    ngOnInit() {}

    ngOnChanges(changes: SimpleChanges): void {
        if (changes.minDate || changes.maxDate) {
            if (this.minDate && this.maxDate && this.minDate > this.maxDate) {
                const temp = this.minDate;
                this.minDate = this.maxDate;
                this.maxDate = temp;
            }
        }
    }

    customDateSelected() {
        this.datesSelected.emit({
            from: this.selectedRange.start,
            to: this.selectedRange.end,
        });
        this.selectedRange = null;
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
