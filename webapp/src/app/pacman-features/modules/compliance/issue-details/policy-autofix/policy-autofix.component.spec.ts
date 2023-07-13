import { ComponentFixture, TestBed } from '@angular/core/testing';

import { PolicyAutofixComponent } from './policy-autofix.component';

describe('PolicyAutofixComponent', () => {
    let component: PolicyAutofixComponent;
    let fixture: ComponentFixture<PolicyAutofixComponent>;

    beforeEach(async () => {
        await TestBed.configureTestingModule({
            declarations: [PolicyAutofixComponent],
        }).compileComponents();
    });

    beforeEach(() => {
        fixture = TestBed.createComponent(PolicyAutofixComponent);
        component = fixture.componentInstance;
        fixture.detectChanges();
    });

    it('should create', () => {
        expect(component).toBeTruthy();
    });
});
