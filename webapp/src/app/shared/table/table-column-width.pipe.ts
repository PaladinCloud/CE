import { Pipe, PipeTransform } from '@angular/core';

@Pipe({
    name: 'tableColumnWidth',
})
export class TableColumnWidthPipe implements PipeTransform {
    transform(denominator: number, fraction: number) {
        if (denominator <= 0) {
            denominator = 1;
        }
        return (100 / denominator) * fraction;
    }
}
