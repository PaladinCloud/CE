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
  strokeColor = '#fff';
  MainTextcolor = '#000';
  innerRadius: any = 85;
  outerRadius: any = 60;
  errorMessage: any;
  widgetWidth2: number;
  @Output() graphIntervalSelected = new EventEmitter<any>();
  
  isCustomSelected = false;
  

  constructor(
    private errorHandling: ErrorHandlingService,
    private utils: UtilsService,
    private loggerService: LoggerService,
    private workflowService: WorkflowService,
    private router: Router,
    private activatedRoute: ActivatedRoute
  ) { }

  ngOnInit() {
    
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
