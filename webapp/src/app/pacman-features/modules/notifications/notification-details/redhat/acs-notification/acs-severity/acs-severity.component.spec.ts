import { ComponentFixture, TestBed } from '@angular/core/testing';

import { AcsSeverityComponent } from './acs-severity.component';

describe('AcsSeverityComponent', () => {
  let component: AcsSeverityComponent;
  let fixture: ComponentFixture<AcsSeverityComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ AcsSeverityComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(AcsSeverityComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
