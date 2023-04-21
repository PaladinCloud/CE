import { ComponentFixture, TestBed } from '@angular/core/testing';

import { AwsIssueNotificationComponent } from './issue-notification.component';

describe('IssueNotificationComponent', () => {
  let component: AwsIssueNotificationComponent;
  let fixture: ComponentFixture<AwsIssueNotificationComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ AwsIssueNotificationComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(AwsIssueNotificationComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
