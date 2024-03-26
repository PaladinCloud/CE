import { ComponentFixture, TestBed } from '@angular/core/testing';

import { AssetTrendGraphComponent } from './asset-trend-graph.component';

describe('AssetTrendGraphComponent', () => {
    let component: AssetTrendGraphComponent;
    let fixture: ComponentFixture<AssetTrendGraphComponent>;

    beforeEach(async () => {
        await TestBed.configureTestingModule({
            declarations: [AssetTrendGraphComponent],
        }).compileComponents();
    });

    beforeEach(() => {
        fixture = TestBed.createComponent(AssetTrendGraphComponent);
        component = fixture.componentInstance;
        fixture.detectChanges();
    });

    it('should create', () => {
        expect(component).toBeTruthy();
    });
});
