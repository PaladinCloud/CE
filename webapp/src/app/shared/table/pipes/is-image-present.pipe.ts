import { Pipe, PipeTransform } from '@angular/core';

@Pipe({
    name: 'isImagePresent',
})
export class IsImagePresentPipe implements PipeTransform {
    transform(value: any): boolean {
        return value.imgSrc !== 'noImg';
    }
}
