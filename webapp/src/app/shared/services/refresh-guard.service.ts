import { Injectable } from '@angular/core';
import { CanActivate } from '@angular/router';

@Injectable({
    providedIn: 'root',
})
export class RefreshGuard implements CanActivate {
    constructor() {}

    canActivate(): boolean {
        const navigationEntry = window.performance.getEntriesByType('navigation')[0];
        const navigationType = navigationEntry.toJSON().type;
        if (navigationType == 'reload') {
            window.location.href = '/home';
            return false;
        }
        return true;
    }
}
