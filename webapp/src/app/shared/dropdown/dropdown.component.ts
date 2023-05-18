import { Component, Input, EventEmitter, Output, OnChanges, SimpleChanges, AfterViewInit, OnInit, ChangeDetectorRef, ViewChild } from '@angular/core';
import { FormControl } from '@angular/forms';
import { MatCheckbox } from '@angular/material/checkbox';
import { MatSelect } from '@angular/material/select';

@Component({
  selector: 'app-dropdown',
  templateUrl: './dropdown.component.html',
  styleUrls: ['./dropdown.component.css']
})
export class DropdownComponent implements OnChanges {

  @Input() items = [];
  @Input() required = false;
  @Input() isDisabled: boolean = false;
  @Input() disableOptions = false;
  @Input() optionImage = false;
  @Input() requiredInfo: boolean = false;
  @Input() placeholder: string = "";
  @Input() selectedItem: string = "";
  @Input() isChipListEnabled: boolean = false;
  @Input() selectedList = [];
  @Input() selectAll: string = "";
  @Input() showApplyButton = false;
  @Input() customLabel;
  @Input() showOriginalText:boolean = false;
  @Input() isMultiSelect = false;
  @Input() isButton = false;
  @Input() buttonLabel;
  @Input() dropdownTitle;
  @Input() sortValues = false;

  @Output() selected = new EventEmitter();
  @Output() applyClick = new EventEmitter();
  @Output() optionClicked = new EventEmitter();
  @Output() closeEventEmitter = new EventEmitter();
  @Output() openEventEmitter = new EventEmitter();
  @ViewChild('selectedAll') selectedAll: MatCheckbox;
  @ViewChild('matSelectRef') matSelectRef: MatSelect;

  listControl = new FormControl([]);

  itemList = [];
  optionList = [];
  selectedOption: string = "";
  selectedOptionImage: string = "";
  constructor(private cdRef: ChangeDetectorRef) { }

  onClose() {
    this.closeEventEmitter.emit();
  }

  onOpen(){
    if(this.selectAll.length > 0)
    this.selectedAll.checked = this.selectedOption.length == this.optionList.length? true: false;

    this.openEventEmitter.emit();
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
      else if(selectedOption){
        this.selectedOption = selectedOption;
      }
    }
    if (this.optionImage) {
      if (this.placeholder == "Category") {
        this.selectedOptionImage = "../../../../assets/icons/category-" + this.selectedOption.toLowerCase() + ".svg";
      } else if (this.placeholder == "Severity") {
        this.selectedOptionImage = "../../../../assets/icons/violations-" + this.selectedOption.toLowerCase() + "-icon.svg";
      } else if (this.placeholder == "Asset Group") {
        this.selectedOptionImage = "../../../../assets/icons/" + this.selectedOption.toLowerCase() + "-color.svg";
      }
    }
  }

  ngAfterViewInit(): void {
    if(this.isChipListEnabled){
      this.listControl.setValue(this.selectedList);
    }
    if(this.selectAll.length > 0) this.selectedAll.checked = this.selectedOption.length == this.optionList.length? true: false;
    this.cdRef.detectChanges();
  }

  ngOnChanges(changes: SimpleChanges): void {
    if(this.isChipListEnabled){
      this.listControl.setValue(this.selectedList);
    }
    if(this.sortValues){
      this.items.sort();
      this.selectedList?.sort();
    }
    this.massageData(this.items, this.selectedItem);
  }

  selectedValue(event: any) {
    if(this.selectAll.length > 0)
      this.selectedAll.checked = this.selectedOption.length == this.optionList.length? true: false;
    this.selected.emit(event);
  }

  toggleAllSelection(event: any){
    if(event.checked){
      this.listControl.setValue(this.items);
      this.selected.emit(this.items);
    }
    else{
      this.listControl.setValue([]);
      this.selected.emit([]);
    }
  }

  updateChipsList(e:any, shouldCallApply?){
    this.listControl.setValue(e);
    if(shouldCallApply){
      this.applyClick.emit(this.listControl.value);
    }else{
      this.selected.emit(e);
    }
  }

  optionClick(e){
    this.optionClicked.emit(e);
  }

  updateSingleChip(e: any[]){
    if(e.length==0){
      this.selectedOption = "";
    }
    this.selected.emit(this.selectedOption);
  }

  handleSelection(e){
    e.stopPropagation()
    this.applyClick.emit(this.listControl.value);
  }

  isFirstCharNumber(option:string){
      if(!option) return "";
      return /^\d/.test(option);
  }

  capitalizeFirstLetter(option: string | unknown[]) {
    if(this.showOriginalText)
      return option;
    if (typeof option === 'string') {
      return option.charAt(0).toUpperCase() + option.slice(1);
    }
    return option;
  }

  disableOption(option:string){
    if(!this.selectedList) return false;
    for(let selectedOption of this.selectedList){
      if(selectedOption == option && this.selectedOption != option){
        return true;
      }
    }
    return false;
  }

}
