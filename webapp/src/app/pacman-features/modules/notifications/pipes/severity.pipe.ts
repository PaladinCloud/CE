import { Pipe, PipeTransform } from '@angular/core';

@Pipe({
    name: 'severity',
})
export class SeverityPipe implements PipeTransform {
    transform(value: string) {
        return value.split('_')[0];
    }
}
