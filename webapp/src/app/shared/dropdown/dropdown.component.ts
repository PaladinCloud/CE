import { Component, Input, OnInit, EventEmitter, Output, OnChanges, SimpleChanges } from '@angular/core';

@Component({
  selector: 'app-dropdown',
  templateUrl: './dropdown.component.html',
  styleUrls: ['./dropdown.component.css']
})
export class DropdownComponent implements OnInit, OnChanges {

  @Input() items = [];
  @Input() required = false;
  @Input() isDisabled: boolean = false;
  @Input() optionImage = false;
  @Input() requiredInfo: boolean = false;
  @Input() placeholder: string;
  @Input() selectedItem: string;
  @Output() selected = new EventEmitter();
  @Output() closeEventEmitter = new EventEmitter();

  itemList = [];
  optionList = [];
  selectedOption: any;
  selectedOptionImage: string;
  constructor() { }

  onClose() {
    this.closeEventEmitter.emit();
  }

  massageData(list: any, selectedOption: any) {
    if (list) {
      this.itemList = [];
      if (list.length > 0 && typeof list[0] == 'object') {
        for (let i = 0; i < list.length; i++) {
          this.itemList.push(list[i].text);
        }
      }
      else {
        this.itemList = list;
      }
      this.optionList = [];
      let currentOptionImage = "";
      for (let i = 0; i < this.itemList.length; i++) {
        if (this.optionImage) {
          if (this.placeholder == "Category") {
            currentOptionImage = "../../../../assets/icons/category-" + this.itemList[i].toLowerCase() + ".svg";
          } else if (this.placeholder == "Severity") {
            currentOptionImage = "../../../../assets/icons/violations-" + this.itemList[i].toLowerCase() + "-icon.svg";
          } else if (this.placeholder == "Asset Group") {
            currentOptionImage = "../../../../assets/icons/" + this.itemList[i].toLowerCase() + "-color.svg";
          }
        }
        this.optionList.push({ "text": this.itemList[i], "img": currentOptionImage });
        currentOptionImage = "";
      }

      if (typeof selectedOption == 'object' && selectedOption[0]) {
        this.selectedOption = selectedOption[0].text;
      }
      else {
        this.selectedOption = selectedOption;
      }
    }
    if (this.optionImage) {
      if (this.placeholder == "Category") {
        this.selectedOptionImage = "../../../../assets/icons/category-" + this.selectedOption + ".svg";
      } else if (this.placeholder == "Severity") {
        this.selectedOptionImage = "../../../../assets/icons/violations-" + this.selectedOption + "-icon.svg";
      } else if (this.placeholder == "Asset Group") {
        this.selectedOptionImage = "../../../../assets/icons/" + this.selectedOption + "-color.svg";
      }
    }
  }

  ngOnChanges(changes: SimpleChanges): void {
    this.massageData(this.items, this.selectedItem);
  }

  ngOnInit(): void {
  }

  selectedValue(event: any) {
    this.selected.emit(event);
  }
}

