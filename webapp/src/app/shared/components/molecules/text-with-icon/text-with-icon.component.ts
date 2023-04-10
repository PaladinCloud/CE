import { Component, Input, OnInit } from '@angular/core';

@Component({
  selector: 'app-text-with-icon',
  templateUrl: './text-with-icon.component.html',
  styleUrls: ['./text-with-icon.component.css']
})
export class TextWithIconComponent implements OnInit {

  @Input() text;
  @Input() preImagePath;
  @Input() postImagePath;
  @Input() textVariant;
  @Input() gap = 8;

  constructor() { }

  ngOnInit(): void {
  }

}
