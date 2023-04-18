import { ComponentFixture, TestBed } from '@angular/core/testing';

import { KeyvalueNotificationComponent } from './keyvalue-notification.component';

describe('KeyvalueNotificationComponent', () => {
  let component: KeyvalueNotificationComponent;
  let fixture: ComponentFixture<KeyvalueNotificationComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ KeyvalueNotificationComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(KeyvalueNotificationComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
