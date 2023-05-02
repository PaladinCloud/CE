import { Injectable } from '@angular/core';
import { NavigationEnd, NavigationStart, Event } from '@angular/router';

export interface RouteScrollPositions {
    [url: string]: RouteScrollPosition;
}

export interface RouteScrollPosition {
    position: number;
    elementId: string;
}

@Injectable()
export class ScrollTrackerService {
    private routeScrollPositions: RouteScrollPositions = {};

    constructor() {}

    saveScrollPosition(url: string, scrollPosition: RouteScrollPosition) {
        this.routeScrollPositions[url] = scrollPosition;
    }

    getScrollPosition(url: string) {
        return this.routeScrollPositions[url];
    }

    getUrlForRouteEvent(event: Event) {
        if (event instanceof NavigationStart) {
            return event.url.split(';', 1)[0];
        } else if (event instanceof NavigationEnd) {
            return (event.urlAfterRedirects || event.url).split(';', 1)[0];
        }
    }
}
