import { async, ComponentFixture, TestBed } from '@angular/core/testing';
import { UntypedFormGroup } from '@angular/forms';

import { FormsComponent } from './forms.component';

describe('FormsComponent', () => {
  let component: FormsComponent;
  let fixture: ComponentFixture<FormsComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ FormsComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(FormsComponent);
    component = fixture.componentInstance;
    component.parentForm = new UntypedFormGroup({});
    fixture.detectChanges();
  });

  // todo: change formsComponent.formComponentName type to string after check
  // currently the test crashes because of default new FormControl() value
  xit('should create', () => {
    expect(component).toBeTruthy();
  });
});
