import { Pipe, PipeTransform } from '@angular/core';

@Pipe({
    name: 'notificationDetails',
})
export class NotificationDetailsPipe implements PipeTransform {
    transform(value: string) {
        return value.replaceAll('[NL]', '<br/>');
    }
}
