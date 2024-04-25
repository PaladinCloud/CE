import { Injectable } from '@angular/core';
import { PreloadingStrategy, Route } from '@angular/router';
import { Observable, of, timer } from 'rxjs';
import { mergeMap } from 'rxjs/operators';

@Injectable({ providedIn: 'root' })
export class SelectivePreloadingStrategy implements PreloadingStrategy {
    preload(route: Route, load: () => Observable<any>): Observable<any> {
        if (route.data && route.data['shouldNotPreload']) {
            return of(null);
        } else {
            return timer(2000).pipe(
                mergeMap(() => load()), // Load the module after the delay
            );
        }
    }
}
