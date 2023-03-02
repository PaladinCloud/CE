import { ComponentFixture, TestBed } from '@angular/core/testing';
import { MatStepperModule } from '@angular/material/stepper';
import { NoopAnimationsModule } from '@angular/platform-browser/animations';

import { StepperComponent } from './stepper.component';

describe('StepperComponent', () => {
  let component: StepperComponent;
  let fixture: ComponentFixture<StepperComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [ MatStepperModule, NoopAnimationsModule ],
      declarations: [ StepperComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(StepperComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
