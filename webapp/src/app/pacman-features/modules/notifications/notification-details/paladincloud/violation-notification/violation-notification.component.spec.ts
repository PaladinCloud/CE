import { ComponentFixture, TestBed } from '@angular/core/testing';

import { PaladinCloudViolationNotificationComponent } from './violation-notification.component';

describe('ViolationNotificationComponent', () => {
  let component: PaladinCloudViolationNotificationComponent;
  let fixture: ComponentFixture<PaladinCloudViolationNotificationComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ PaladinCloudViolationNotificationComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(PaladinCloudViolationNotificationComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
