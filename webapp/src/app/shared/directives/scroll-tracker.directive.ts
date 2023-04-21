import { AfterViewInit, Directive, ElementRef, Input, NgZone, OnDestroy } from '@angular/core';
import { NavigationEnd, NavigationStart, Router } from '@angular/router';
import { Subject } from 'rxjs';
import { delayWhen, filter, map, pairwise, takeUntil, tap } from 'rxjs/operators';
import { ScrollTrackerService } from '../services/scroll-tracker.service';

@Directive({
    selector: '[appScrollTracker]',
})
export class ScrollTrackerDirective implements AfterViewInit, OnDestroy {
    @Input() appScrollTracker: string;
    private readonly INTERVAL_DURATION = 250;
    private readonly MAX_SCROLL_ATTEMPTS = 5;

    private el: HTMLElement;
    private scrollAttempts = 0;
    private intervalId: ReturnType<typeof setInterval>;
    private afterViewInit$ = new Subject<void>();
    private destroy$ = new Subject<void>();

    constructor(
        private elementRef: ElementRef,
        private router: Router,
        private scrollTrackerService: ScrollTrackerService,
        private ngZone: NgZone,
    ) {
        this.router.events
            .pipe(
                delayWhen(() => this.afterViewInit$),
                pairwise(),
                filter(
                    ([prevRouteEvent, curRouteEvent]) =>
                        prevRouteEvent instanceof NavigationEnd &&
                        curRouteEvent instanceof NavigationStart,
                ),
                tap(() => this.clearScrollSchecker()),
                map(([prevRouteEvent]) =>
                    this.scrollTrackerService.getUrlForRouteEvent(prevRouteEvent),
                ),
                takeUntil(this.destroy$),
            )
            .subscribe((url) => {
                this.scrollTrackerService.saveScrollPosition(url, {
                    elementId: this.appScrollTracker || null,
                    position: this.el.scrollTop,
                });
            });

        this.router.events
            .pipe(
                delayWhen(() => this.afterViewInit$),
                filter((event) => event instanceof NavigationEnd),
                map((event) => this.scrollTrackerService.getUrlForRouteEvent(event)),
                map((url) => this.scrollTrackerService.getScrollPosition(url)),
                filter(
                    (scrollPosition) =>
                        scrollPosition && scrollPosition.elementId === this.appScrollTracker,
                ),
                takeUntil(this.destroy$),
            )
            .subscribe((scrollPosition) => this.prepareScroll(scrollPosition.position));
    }

    ngAfterViewInit() {
        this.el = this.elementRef.nativeElement;
        this.afterViewInit$.next();
        this.afterViewInit$.complete();
    }

    prepareScroll(position: number) {
        this.ngZone.runOutsideAngular(() => {
            this.clearScrollSchecker();
            this.intervalId = setInterval(() => {
                this.attemptScroll(position);
            }, this.INTERVAL_DURATION);
        });
    }

    attemptScroll(position: number) {
        // If scroll atteempts reached maximum number and element
        // doesn't have a scrollHeigh, scroll the element to the bottom
        if (this.scrollAttempts === this.MAX_SCROLL_ATTEMPTS) {
            if (this.el.scrollHeight > 0) {
                this.el.scrollTop = this.el.scrollHeight;
            }
            return this.clearScrollSchecker();
        }

        if (this.el.scrollHeight >= position) {
            this.el.scrollTo({
                top: position,
                behavior: 'smooth',
            });
            return this.clearScrollSchecker();
        }

        this.scrollAttempts++;
    }

    clearScrollSchecker() {
        this.scrollAttempts = 0;
        if (!this.intervalId) {
            return;
        }

        this.ngZone.runOutsideAngular(() => clearInterval(this.intervalId));
    }

    ngOnDestroy(): void {
        this.clearScrollSchecker();
        this.destroy$.next();
        this.destroy$.complete();
    }
}
