import { ComponentFixture, TestBed } from '@angular/core/testing';

import { AcsNotificationComponent } from './acs-notification.component';

describe('AcsNotificationComponent', () => {
  let component: AcsNotificationComponent;
  let fixture: ComponentFixture<AcsNotificationComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ AcsNotificationComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(AcsNotificationComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
