import { TestBed } from '@angular/core/testing';

import { ScrollTrackerService } from './scroll-tracker.service';

describe('ScrollTrackerService', () => {
    let service: ScrollTrackerService;

    beforeEach(() => {
        TestBed.configureTestingModule({});
        service = TestBed.inject(ScrollTrackerService);
    });

    it('should be created', () => {
        expect(service).toBeTruthy();
    });
});
