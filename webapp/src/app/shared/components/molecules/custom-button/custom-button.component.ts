import { Component, EventEmitter, Input, OnInit, Output } from '@angular/core';

declare type ButtonType = "stroked" | "flat" | "raised" | "default";

@Component({
  selector: 'app-custom-button',
  templateUrl: './custom-button.component.html',
  styleUrls: ['./custom-button.component.css']
})
export class CustomButtonComponent implements OnInit {

  @Input() buttonType: ButtonType = "default";
  @Input() disabled;
  @Input() label;

  @Output() onclick = new EventEmitter();

  constructor() { }

  ngOnInit(): void {
  }

  handleClick(e){
    this.onclick.emit(e);
  }

}
