import { AfterViewInit, Component, ElementRef, HostListener, Input, OnChanges, Output, SimpleChanges, ViewChild } from '@angular/core';
import { Subject } from 'rxjs';

@Component({
  selector: 'app-chip',
  templateUrl: './chip.component.html',
  styleUrls: ['./chip.component.css']
})
export class ChipComponent implements AfterViewInit {

  @Input() backgroundColor = "white";
  @Input() color = "black";
  @Input() chipsList :any[] = [];
  @Input() isRemovable = false;
  @Input() maxChips = 3;
  @Output() updatedChipsList = new Subject();

  constructor() {}

  ngAfterViewInit(): void {}

  onReomve(selectedItem: any) {
    let selectedList = this.chipsList as string[];
    this.removeFirst(selectedList, selectedItem);
    this.updatedChipsList.next(selectedList);
  }

  private removeFirst<T>(array: T[], toRemove: T): void {
    const index = array.indexOf(toRemove);
    if (index !== -1) {
      array.splice(index, 1);
    }
  }
}