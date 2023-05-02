import { TestBed } from '@angular/core/testing';

import { DashboardArrangementService } from './dashboard-arrangement.service';

describe('DashboardArrangementService', () => {
    let service: DashboardArrangementService;

    beforeEach(() => {
        TestBed.configureTestingModule({});
        service = TestBed.inject(DashboardArrangementService);
    });

    it('should be created', () => {
        expect(service).toBeTruthy();
    });
});
