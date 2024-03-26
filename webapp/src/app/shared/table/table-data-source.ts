import { ListRange } from '@angular/cdk/collections';
import { CdkVirtualScrollViewport } from '@angular/cdk/scrolling';
import { DataSource } from '@angular/cdk/table';
import { Injectable, NgZone, OnDestroy } from '@angular/core';
import { MatTableDataSource } from '@angular/material/table';
import { combineLatest, Observable, Subscription, BehaviorSubject, Subject } from 'rxjs';
import { map, startWith, takeUntil } from 'rxjs/operators';
import { IRowObj } from './interfaces/table-props.interface';

@Injectable()
export class TableDataSource extends DataSource<any> implements OnDestroy {
    private _subscription!: Subscription;
    private _viewPort!: CdkVirtualScrollViewport;
    private readonly initialDataSliceStart = 100;
    destroy$ = new Subject<void>();

    intialCallFlag = true;
    // Create MatTableDataSource so we can have all sort,filter bells and whistles
    matTableDataSource: MatTableDataSource<IRowObj> = new MatTableDataSource();

    // Expose dataStream to simulate VirtualForOf.dataStream
    dataStream = this.matTableDataSource.connect().asObservable();

    renderedStream = new BehaviorSubject<any[]>([]);
    constructor(private ngZone: NgZone) {
        super();
    }

    attach(viewPort: CdkVirtualScrollViewport) {
        if (!viewPort) {
            throw new Error('ViewPort is undefined');
        }
        this._viewPort = viewPort;

        // this.initFetchingOnScrollUpdates();

        // Attach DataSource as CdkVirtualForOf so ViewPort can access dataStream
        this._viewPort.attach(this as any);

        // Trigger range change so that 1st page can be loaded
        this._viewPort.setRenderedRange({ start: 0, end: 10 });
    }

    // Called by CDK Table
    connect(): Observable<any[]> {
        const tableData = this.matTableDataSource.connect();
        const filtered =
            this._viewPort === undefined ? tableData : this.filterByRangeStream(tableData);

        filtered.pipe(takeUntil(this.destroy$)).subscribe((data) => {
            this.renderedStream.next(data);
        });

        return this.renderedStream.asObservable();
    }

    disconnect(): void {
        if (this._subscription) {
            this._subscription.unsubscribe();
        }
    }

    private filterByRangeStream(tableData: Observable<any[]>) {
        const rangeStream = this._viewPort.renderedRangeStream.pipe(startWith({} as ListRange));
        const filtered = combineLatest([tableData, rangeStream]).pipe(
            map(([data, { start, end }]) => {
                if (this.intialCallFlag) {
                    this.intialCallFlag = false;
                    return data.slice(0, this.initialDataSliceStart);
                }
                return data.slice(start, end);
            }),
        );
        return filtered;
    }

    ngOnDestroy(): void {
        this.destroy$.next();
        this.destroy$.complete();
    }
}
