import { ComponentFixture, TestBed } from '@angular/core/testing';

import { NotificationDetailsComponent } from './notification-details.component';

describe('NotificationDetailsComponent', () => {
  let component: NotificationDetailsComponent;
  let fixture: ComponentFixture<NotificationDetailsComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ NotificationDetailsComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(NotificationDetailsComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
