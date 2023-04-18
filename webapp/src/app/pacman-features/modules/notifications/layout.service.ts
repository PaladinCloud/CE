import { Injectable } from '@angular/core';

export enum LayoutType {
    JSON = 'JSON',
    KEYVALUE = 'KEYVALUE',
    ISSUE = 'ISSUE',
    ACS = 'ACS',
    VIOLATIONS = 'VIOLATIONS',
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
        switch (notification.eventCategory) {
            case 'issue':
                return LayoutType.ISSUE;
            case 'acs':
                return LayoutType.ACS;
            case 'violations':
                return LayoutType.VIOLATIONS;
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
}
