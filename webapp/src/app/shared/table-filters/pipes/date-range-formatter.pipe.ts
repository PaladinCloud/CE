import { formatDate } from '@angular/common';
import { Pipe, PipeTransform } from '@angular/core';

@Pipe({
    name: 'dateRangeFormatter',
})
export class DateRangeFormatterPipe implements PipeTransform {
    transform(dateRange: string): string {
        const dateArray = dateRange.split(' - ');

        if (
            dateArray.length === 2 &&
            this.isValidDate(dateArray[0]) &&
            this.isValidDate(dateArray[1])
        ) {
            const startDate = new Date(dateArray[0]);
            const endDate = new Date(dateArray[1]);

            const formattedStartDate = formatDate(startDate, 'MMM d, yyyy', 'en-US');
            const formattedEndDate = formatDate(endDate, 'MMM d, yyyy', 'en-US');

            return `${formattedStartDate} - ${formattedEndDate}`;
        } else {
            return dateRange;
        }
    }

    private isValidDate(dateString: string): boolean {
        const date = new Date(dateString);
        return !isNaN(date.getTime());
    }
}
