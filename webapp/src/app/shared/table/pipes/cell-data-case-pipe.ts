import { Pipe, PipeTransform } from '@angular/core';

@Pipe({
    name: 'celldatacase',
})
export class CellDataCasePipe implements PipeTransform {
    transform(value: string): string {
        if(value && typeof value==="string"){
            if (value.startsWith('__') || value.includes("@")) {
                return value;
            }
            return value
                .replace(/(^\w|\b\w)/g, (s) => s.toUpperCase()) // title case words
                .replace(/\s+/g, ' ') // collapse repeated whitespace
                .trim(); // remove leading/trailing whitespace
        }else{
            return value;
        }
    }
}
