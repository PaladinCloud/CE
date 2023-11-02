
import { Component, EventEmitter, Input, OnInit, Output, ViewChild } from '@angular/core';
import { DateRange } from '@angular/material/datepicker';
import { MatMenuTrigger } from '@angular/material/menu';
import { Subscription } from 'rxjs';
import { AgDomainObservableService } from 'src/app/core/services/ag-domain-observable.service';

@Component({
  selector: 'app-custom-card',
  templateUrl: './custom-card.component.html',
  styleUrls: ['./custom-card.component.css']
})
export class CustomCardComponent implements OnInit {

  @Output() graphIntervalSelected = new EventEmitter();

  @Input() selectedItem = "All time";
  @Input() fromDate: Date;
  @Input() toDate: Date = new Date();
  @Input() minDate: Date = new Date();
  selectedRange?: DateRange<Date>;
  @Input() showDateDropdown = false;
  @Input() card = {
    header: "Default title"
  };
  isCustomSelected: boolean = false;
  @ViewChild('menuTrigger') matMenuTrigger: MatMenuTrigger;
  @Output() switchTabs = new EventEmitter<any>();
  agDomainSubscription: Subscription;


  constructor(private agDomainObservableService:AgDomainObservableService) {
    this.agDomainSubscription = this.agDomainObservableService.getAgDomain().subscribe(async([assetGroupName, domain]) => {
      this.selectedRange = null;
    })
   }

  ngOnInit(): void {
  }

  handleGraphIntervalSelection = (e) => {
    this.toDate = new Date();
    this.selectedItem = e;
    e = e.toLowerCase();
    if(e == "all time" || e == "custom"){
      if(e=="custom"){
        this.isCustomSelected = true;
        this.matMenuTrigger.openMenu()
        return;
      }
      this.dateIntervalSelected(this.minDate, this.toDate);
      return;
    }
    let date = new Date();
    this.isCustomSelected = false;
    this.selectedRange = null;
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

  dateIntervalSelected(from:Date, to:Date){
    if(this.isCustomSelected){
      this.selectedRange = new DateRange<Date>(from, to);
    }
    let event = {
      from: from,
      to: to,
      graphInterval: this.selectedItem
    }
    this.graphIntervalSelected.emit(event);
    this.matMenuTrigger.closeMenu();
  }

  onDropdownClose(){
    if(this.selectedItem=="-1"){
      this.selectedItem = "Custom";
    }
  }

  onCustomSelection(){
    if(this.selectedItem=="Custom"){
      this.selectedItem = "-1";
    }
  }

  ngOnDestroy(){
    if(this.agDomainSubscription) this.agDomainSubscription.unsubscribe();
  }
}
