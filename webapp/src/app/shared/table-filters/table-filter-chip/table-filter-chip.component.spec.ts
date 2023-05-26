import { ComponentFixture, TestBed } from '@angular/core/testing';

import { TableFilterChipComponent } from './table-filter-chip.component';

describe('TableFilterChipComponent', () => {
    let component: TableFilterChipComponent;
    let fixture: ComponentFixture<TableFilterChipComponent>;

    beforeEach(async () => {
        await TestBed.configureTestingModule({
            declarations: [TableFilterChipComponent],
        }).compileComponents();
    });

    beforeEach(() => {
        fixture = TestBed.createComponent(TableFilterChipComponent);
        component = fixture.componentInstance;
        fixture.detectChanges();
    });

    it('should create', () => {
        expect(component).toBeTruthy();
    });
});
