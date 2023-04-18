import { ComponentFixture, TestBed } from '@angular/core/testing';

import { IssueNotificationComponent } from './issue-notification.component';

describe('IssueNotificationComponent', () => {
  let component: IssueNotificationComponent;
  let fixture: ComponentFixture<IssueNotificationComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ IssueNotificationComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(IssueNotificationComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
