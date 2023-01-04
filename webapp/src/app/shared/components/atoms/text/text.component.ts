import { Component, Input, OnInit } from '@angular/core';

@Component({
  selector: 'app-text',
  templateUrl: './text.component.html',
  styleUrls: ['./text.component.css']
})
export class TextComponent implements OnInit {

  @Input() text: string;
  @Input() classNames: string;
  @Input() color: string;
  @Input() sx: Object;

  constructor() { }

  ngOnInit(): void {
  }

}