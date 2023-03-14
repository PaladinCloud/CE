import { Component, ContentChild, EventEmitter, Input, OnChanges, OnInit, Output, SimpleChanges, TemplateRef, ViewChild } from '@angular/core';

@Component({
  selector: 'app-stepper',
  templateUrl: './stepper.component.html',
  styleUrls: ['./stepper.component.css']
})
export class StepperComponent implements OnInit {

  @Input() currentStepperIndex = 0;
  @Output() selectedStepperIndex = new EventEmitter<any>();
  @ContentChild('stepsHeader', { read: TemplateRef }) stepsHeader: TemplateRef<any>;
  @ContentChild('stepsContent', { read: TemplateRef }) stepsContent: TemplateRef<any>;
  @Input()  stepperData = [];
  constructor() { }
  ngOnInit(): void {
  }

  selectionChange(event: any) {
    this.selectedStepperIndex.emit(event.selectedIndex);
  }

}
