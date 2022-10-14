import { Component, EventEmitter, HostListener, Input, OnInit, Output } from "@angular/core";
import { ActivatedRoute, Router } from "@angular/router";
import { ErrorHandlingService } from "src/app/shared/services/error-handling.service";
import { WorkflowService } from "../../../../core/services/workflow.service";
import { LoggerService } from "../../../../shared/services/logger.service";
import { UtilsService } from "../../../../shared/services/utils.service";

@Component({
  selector: "app-card",
  templateUrl: "./card.component.html",
  styleUrls: ["./card.component.css"],
})
export class CardComponent implements OnInit {
  @Input() card: any;
  @Input() data: any;
  @Input() dataError = '';

  errorType = '';
  widgetWidth = 225;
  widgetHeight = 250;
  strokeColor = 'red';
  MainTextcolor = '#000';
  innerRadius: any = 85;
  outerRadius: any = 60;
  errorMessage: any;
  widgetWidth2: number;
  @Output() graphIntervalSelected = new EventEmitter<any>();
  
  isCustomSelected = false;
  years = [];
  allMonths = [
    { text: 'January', id: 0 },
    { text: 'February', id: 1 },
    { text: 'March', id: 2 },
    { text: 'April', id: 3 },
    { text: 'May', id: 4 },
    { text: 'June', id: 5 },
    { text: 'July', id: 6 },
    { text: 'August', id: 7 },
    { text: 'September', id: 8 },
    { text: 'October', id: 9 },
    { text: 'November', id: 10 },
    { text: 'December', id: 11 }
  ];
  allMonthDays = [];
  fromDate: Date = new Date(2022, 1, 1);
  toDate: Date = new Date(2200, 12, 31);

  constructor(
    private errorHandling: ErrorHandlingService,
    private utils: UtilsService,
    private loggerService: LoggerService,
    private workflowService: WorkflowService,
    private router: Router,
    private activatedRoute: ActivatedRoute
  ) { }

  ngOnInit() {
    for (let i = 2022; i <= 2200; i++) {
      this.years.push(i);
    }
  }

  private getNumberOfDays = function (year, monthId: any) {
    const isLeap = ((year % 4) === 0 && ((year % 100) !== 0 || (year % 400) === 0));
    return [31, (isLeap ? 29 : 28), 31, 30, 31, 30, 31, 31, 30, 31, 30, 31][monthId];
  };

  onSelectYear(date: Date, selectedYear){
    date.setFullYear(selectedYear);
  }

  getMonthId(selectedMonth){
    let monthId = 0;
    for (let id = 0; id < this.allMonths.length; id++) {
      if (this.allMonths[id].text == selectedMonth) {
        monthId = id;
      }
    }
    return monthId;
  }

  getMonth(date: Date){
    let selectedMonth = "";
    for (let id = 0; id < this.allMonths.length; id++) {
      if (this.allMonths[id].id == date.getMonth()) {
        selectedMonth = this.allMonths[id].text;
      }
    }
    return selectedMonth;
  }

  onSelectMonth(date: Date, selectedMonth: any) {
    const monthDays: any = [];
    let monthId = this.getMonthId(selectedMonth);
    
    const daysCount = this.getNumberOfDays(date.getFullYear(), monthId);
    for (let dayNo = 1; dayNo <= daysCount; dayNo++) {
      monthDays.push({ id: dayNo, text: dayNo.toString() });
    }
    this.allMonthDays = monthDays;
    date.setMonth(monthId);
  }

  onSelectDay(date: Date, selectedDay: any) {
    date.setDate(selectedDay);    
  }

  handleGraphIntervalSelection = (e) => {
    e = e.toLowerCase();
    if(e == "all time" || e == "custom"){
      if(e=="custom"){
        this.isCustomSelected = true;
        return;
      }
      this.dateIntervalSelected();
      return;
    }
    let date = new Date();
    this.isCustomSelected = false;
    let queryParamObj = {};
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

    this.dateIntervalSelected(date); 
  }

  getFormattedDate(date: Date){
    const offset = date.getTimezoneOffset()
    let formattedDate = new Date(date.getTime() - (offset*60*1000)).toISOString().split('T')[0];
    return formattedDate;
  }

  dateIntervalSelected(fromDate?, toDate?){
    let queryParamObj = {}
    if(fromDate){
      queryParamObj["from"] = this.getFormattedDate(fromDate);
    }
    if(toDate){
      queryParamObj["to"] = this.getFormattedDate(toDate);
    }    
    this.isCustomSelected = false;
    this.graphIntervalSelected.emit(queryParamObj);
  }


  navigateDataTable(event) {
    this.workflowService.addRouterSnapshotToLevel(
      this.router.routerState.snapshot.root
    );
    try {
      const queryObj = event;
      const eachParams = { "severity.keyword": queryObj.toLowerCase() };
      const newParams = this.utils.makeFilterObj(eachParams);
      if (queryObj !== undefined) {
        this.router.navigate(["../../../../", "compliance", "issue-listing"], {
          relativeTo: this.activatedRoute,
          queryParams: newParams,
          queryParamsHandling: "merge",
        });
      }
    } catch (error) {
      this.errorMessage = this.errorHandling.handleJavascriptError(error);
      this.loggerService.log("error", error);
    }
  }

  @HostListener("window:resize", ["$event"]) onResize(event) {
    const element_cpuUtilization = document.getElementById("cpuUtilization");
    if (element_cpuUtilization) {
      this.widgetWidth2 = parseInt(
        window
          .getComputedStyle(element_cpuUtilization, null)
          .getPropertyValue("width")
          .split("px")[0],
        10
      );
    }
    const element_statsDoughnut = document.getElementById("statsDoughnut");
    if (element_statsDoughnut) {
      let widthValue = parseInt(
        window
          .getComputedStyle(element_statsDoughnut, null)
          .getPropertyValue("width")
          .split("px")[0],
        10
      );
      widthValue = widthValue - 155;
      if (widthValue > 150) {
        this.widgetWidth = widthValue;
      }
    }
  }
}
