import { ComponentFixture, TestBed } from '@angular/core/testing';

import { PageTitleComponent } from './page-title.component';

describe('PageTitleComponent', () => {
    let component: PageTitleComponent;
    let fixture: ComponentFixture<PageTitleComponent>;

    beforeEach(async () => {
        await TestBed.configureTestingModule({
            declarations: [PageTitleComponent],
        }).compileComponents();
    });

    beforeEach(() => {
        fixture = TestBed.createComponent(PageTitleComponent);
        component = fixture.componentInstance;
        fixture.detectChanges();
    });

    it('should create', () => {
        expect(component).toBeTruthy();
    });
});
