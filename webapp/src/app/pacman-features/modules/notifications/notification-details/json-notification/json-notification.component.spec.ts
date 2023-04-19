import { ComponentFixture, TestBed } from '@angular/core/testing';

import { JsonNotificationComponent } from './json-notification.component';

describe('JsonNotificationComponent', () => {
    let component: JsonNotificationComponent;
    let fixture: ComponentFixture<JsonNotificationComponent>;

    beforeEach(async () => {
        await TestBed.configureTestingModule({
            declarations: [JsonNotificationComponent],
        }).compileComponents();
    });

    beforeEach(() => {
        fixture = TestBed.createComponent(JsonNotificationComponent);
        component = fixture.componentInstance;
        fixture.detectChanges();
    });

    it('should create', () => {
        expect(component).toBeTruthy();
    });
});
