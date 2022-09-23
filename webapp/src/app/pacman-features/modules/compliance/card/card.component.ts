import { Component, HostListener, Input, OnInit } from "@angular/core";
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
  @Input() policyData: any;

  errorType = '';
  widgetWidth = 225;
  widgetHeight = 250;
  strokeColor = 'red';
  MainTextcolor = '#000';
  innerRadius: any = 85;
  outerRadius: any = 60;
  errorMessage: any;
  widgetWidth2: number;
  @Input() cardButtonAction;
  @Input() complianceData = [];
  @Input() assetsCountData = [];
  @Input() complianceDataError:string = '';
  @Input() assetsCountDataError: string = '';
  @Input() policyDataError: string = '';

  private agAndDomain = {};

  constructor(
    private errorHandling: ErrorHandlingService,
    private utils: UtilsService,
    private loggerService: LoggerService,
    private workflowService: WorkflowService,
    private router: Router,
    private activatedRoute: ActivatedRoute
  ) { }

  ngOnInit() {
    this.activatedRoute.queryParams.subscribe((params) => {
      this.agAndDomain["ag"] = params["ag"];
      this.agAndDomain["domain"] = params["domain"];
      if(this.agAndDomain["ag"] && this.agAndDomain["domain"]){        
      }
    });
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
