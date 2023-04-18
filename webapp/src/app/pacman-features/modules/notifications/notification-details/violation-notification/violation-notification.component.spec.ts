import { ComponentFixture, TestBed } from '@angular/core/testing';

import { ViolationNotificationComponent } from './violation-notification.component';

describe('ViolationNotificationComponent', () => {
  let component: ViolationNotificationComponent;
  let fixture: ComponentFixture<ViolationNotificationComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ ViolationNotificationComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(ViolationNotificationComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
