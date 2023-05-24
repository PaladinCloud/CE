import { ComponentFixture, TestBed } from '@angular/core/testing';

import { TableFilterSearchComponent } from './table-filter-search.component';

describe('TableFilterSearchComponent', () => {
    let component: TableFilterSearchComponent;
    let fixture: ComponentFixture<TableFilterSearchComponent>;

    beforeEach(async () => {
        await TestBed.configureTestingModule({
            declarations: [TableFilterSearchComponent],
        }).compileComponents();
    });

    beforeEach(() => {
        fixture = TestBed.createComponent(TableFilterSearchComponent);
        component = fixture.componentInstance;
        fixture.detectChanges();
    });

    it('should create', () => {
        expect(component).toBeTruthy();
    });
});
