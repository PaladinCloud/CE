import { Component, Input, OnInit } from '@angular/core';

@Component({
  selector: 'app-text-with-icon',
  templateUrl: './text-with-icon.component.html',
  styleUrls: ['./text-with-icon.component.css']
})
export class TextWithIconComponent implements OnInit {

  @Input() text;
  @Input() iconUrl;
  @Input() textVariant;

  constructor() { }

  ngOnInit(): void {
  }

}
