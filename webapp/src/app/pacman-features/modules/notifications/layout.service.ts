import { Injectable } from '@angular/core';

export enum LayoutType {
    AWS_ISSUE = 'AWS_ISSUE',
    REDHAT_ACS = 'REDHAT_ACS',
    PALADINCLOUD_VIOLATION = 'PALADINCLOUD_VIOLATION',
    JSON = 'JSON',
    KEYVALUE = 'KEYVALUE',
}

export interface CloudNotification {
    eventCategory: string;
    eventCategoryName: string;
    eventName: string;
    eventSource: string;
    eventSourceName: string;
    payload: { [key: string]: unknown };
    startTime: Date;
}

@Injectable()
export class LayoutService {
    constructor() {}

    getLayoutType(notification: CloudNotification): LayoutType {
        const payload = notification.payload;
        const eventType = this.getEventType(notification);
        switch (eventType) {
            case 'aws_issue':
            case 'aws_account':
            case 'aws_scheduled':
                return LayoutType.AWS_ISSUE;
            case 'redhat_acs':
                return LayoutType.REDHAT_ACS;
            case 'paladincloud_violations':
                return LayoutType.PALADINCLOUD_VIOLATION;
            default:
                for (const prop in payload) {
                    if (typeof payload[prop] === 'object' || Array.isArray(payload[prop])) {
                        return LayoutType.JSON;
                    }
                }
                break;
        }
        return LayoutType.KEYVALUE;
    }

    private getEventType(notification: CloudNotification) {
        return `${notification.eventSource}_${notification.eventCategory}`.toLowerCase();
    }
}
