
import { Component, EventEmitter, Input, OnInit, Output, ViewChild } from '@angular/core';
import { MatMenuTrigger } from '@angular/material/menu';

@Component({
  selector: 'app-custom-card',
  templateUrl: './custom-card.component.html',
  styleUrls: ['./custom-card.component.css']
})
export class CustomCardComponent implements OnInit {

  @Output() graphIntervalSelected = new EventEmitter();

  @Input() selectedItem = "All time";
  @Input() fromDate: Date = new Date(2022, 0, 1);
  @Input() toDate: Date = new Date();
  @Input() showDateDropdown = false;
  @Input() card = {
    header: "Default title"
  };
  isCustomSelected: boolean = false;
  @ViewChild('menuTrigger') matMenuTrigger: MatMenuTrigger;


  constructor() { }

  ngOnInit(): void {
  }

  handleGraphIntervalSelection = (e) => {
    this.fromDate = new Date(2022, 0, 1);
    this.toDate = new Date();
    this.selectedItem = e;
    e = e.toLowerCase();
    if(e == "all time" || e == "custom"){
      if(e=="custom"){
        this.isCustomSelected = true;
        this.matMenuTrigger.openMenu()
        return;
      }
      this.dateIntervalSelected(this.fromDate, this.toDate);
      return;
    }
    let date = new Date();
    this.isCustomSelected = false;
    switch(e){
      case "1 week":
        date.setDate(date.getDate() - 7);
        break;
      case "1 month":
        date.setMonth(date.getMonth() - 1);
        break;
      case "6 months":
        date.setMonth(date.getMonth() - 6);
        break;
      case "12 months":
        date.setFullYear(date.getFullYear() - 1);
        break;
    }
    this.dateIntervalSelected(date, this.toDate);
  }

  dateIntervalSelected(from?, to?){
    let event = {
      from: from,
      to: to,
      selectedItem: this.selectedItem
    }
    this.graphIntervalSelected.emit(event);
    this.matMenuTrigger.closeMenu();
  }

  onDropdownClose(){
    if(this.selectedItem==""){
      this.selectedItem = "Custom";
    }
  }

  onCustomSelection(){
    if(this.selectedItem=="Custom"){
      this.selectedItem = "";
    }
  }

}
