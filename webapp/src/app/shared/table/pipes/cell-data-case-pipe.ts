import { Pipe, PipeTransform } from '@angular/core';
import { DATA_MAPPING } from '../../constants/data-mapping';

@Pipe({
    name: 'celldatacase',
})
export class CellDataCasePipe implements PipeTransform {
    transform(value: string): string {
        if(value && typeof value==="string"){
            if (value.startsWith('__') || value.includes("@")) {
                return value;
            }
            if(DATA_MAPPING[value.toLowerCase()]){
                return DATA_MAPPING[value.toLowerCase()];
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
