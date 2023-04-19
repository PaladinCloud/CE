import { Pipe, PipeTransform } from '@angular/core';

@Pipe({
    name: 'proptitlecase',
})
export class PropTitleCasePipe implements PipeTransform {
    transform(value: string): string {
        if (value.startsWith('__')) {
            return value;
        }
        return value
            .replace(/([^A-Z])([A-Z])/g, '$1 $2') // split cameCase
            .replace(/[_-]+/g, ' ') // split snake_case and lisp-case
            .toLowerCase()
            .replace(/(^\w|\b\w)/g, (s) => s.toUpperCase()) // title case words
            .replace(/\s+/g, ' ') // collapse repeated whitespace
            .trim(); // remove leading/trailing whitespace
    }
}
