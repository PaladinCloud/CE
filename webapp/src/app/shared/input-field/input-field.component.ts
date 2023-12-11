import { Component, EventEmitter, Input, OnInit, Output, forwardRef, AfterViewInit } from '@angular/core';
import { FormGroup , NG_VALUE_ACCESSOR} from '@angular/forms';
import { FormService } from '../services/form.service';

export const CUSTOM_INPUT_CONTROL_VALUE_ACCESSOR: any = {
  provide: NG_VALUE_ACCESSOR,
  useExisting: forwardRef(() => InputFieldComponent),
  multi: true
};

@Component({
  selector: 'app-input-field',
  templateUrl: './input-field.component.html',
  styleUrls: ['./input-field.component.css'],
  providers: [CUSTOM_INPUT_CONTROL_VALUE_ACCESSOR]
})

export class InputFieldComponent implements OnInit, AfterViewInit {

  @Input() type: string = "input";
  @Input() label:string = "";
  @Input() hint: string;
  @Input() height: string = "20px";
  @Input() placeholder: string = '';
  @Input() description: string = '';
  @Input() isRequired: boolean = false;
  @Input() shouldApplyNumericDirective: boolean = false;
  _value  = '';
  @Input() isDisabled: boolean = false;
  @Output() valueChange = new EventEmitter<string>();
  @Output() errorEventEmitter = new EventEmitter();

  // Form errors will be passed here
  @Input() formErrors;
  @Input() formControlName:string = "";
  @Input() parentForm : FormGroup;
  onChange: any = () => { };
  onTouched: any = () => { };

  @Input()
  set value(val) {
    this._value = val;
    this.onChange(this._value);
    this.onTouched(this._value);
  }

  get value() {
    return this._value;
  }

  onInputChange(){
    this.valueChange.emit(this.value);
  }

  ngOnInit(): void {
  }

  constructor(
    private formErrorService:FormService
  ){}

  ngAfterViewInit(): void {
    this.validateForm();
  }

  validateForm(){
    // on each value change we call the validateForm function
    // We only validate form controls that are dirty, meaning they are touched
    // the result is passed to the formErrors object
    if(this.formControlName){
      const formControl = this.parentForm.get(this.formControlName);
      if (formControl) {
        formControl.valueChanges.subscribe(() => {
          this.formErrors = this.formErrorService.validateForm(
            this.parentForm,
            this.formErrors,
            true
          );
        });
      }
    }

  }

  writeValue(value): void {
    if (value) {
      this.value = value;
    }
  }

  registerOnChange(fn): void {
    this.onChange = fn;
    this.validateForm();
  }

  registerOnTouched(fn) {
    this.onTouched = fn;
    this.validateForm();
  }
  setDisabledState?(isDisabled): void {
    throw new Error('Method not implemented.');
  }
}