import { Component, Input, OnInit } from '@angular/core';

declare type ButtonType = "stroked" | "flat" | "raised" | "default";

@Component({
  selector: 'app-custom-button',
  templateUrl: './custom-button.component.html',
  styleUrls: ['./custom-button.component.css']
})
export class CustomButtonComponent implements OnInit {

  @Input() buttonType: ButtonType;
  @Input() disabled;
  @Input() label;

  constructor() { }

  ngOnInit(): void {
  }

}
