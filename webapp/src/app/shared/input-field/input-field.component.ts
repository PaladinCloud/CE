import { Component, EventEmitter, Input, OnInit, Output } from '@angular/core';

@Component({
  selector: 'app-input-field',
  templateUrl: './input-field.component.html',
  styleUrls: ['./input-field.component.css']
})
export class InputFieldComponent implements OnInit {

  @Input() placeholder: string = "";
  @Input() description: string = "";
  @Input() value: string = "";
  @Output() valueChange = new EventEmitter<string>();

  onInputChange(){
    this.valueChange.emit(this.value);
  }

  ngOnInit(): void {

  }

}
