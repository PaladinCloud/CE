import { Component, HostListener, Input, OnInit } from "@angular/core";
import { ActivatedRoute, Router } from "@angular/router";
import { environment } from "../../../../../environments/environment";
import { WorkflowService } from "../../../../core/services/workflow.service";
import { LoggerService } from "../../../../shared/services/logger.service";
import { UtilsService } from "../../../../shared/services/utils.service";
import { FetchResourcesService } from "../../../services/fetch-resources.service";
import { OverallComplianceService } from "../../../services/overall-compliance.service";

@Component({
  selector: "app-card",
  templateUrl: "./card.component.html",
  styleUrls: ["./card.component.css"],
  providers: [OverallComplianceService, FetchResourcesService],
})
export class CardComponent implements OnInit {
  @Input() card: any;
  @Input() policyData: any;

  widgetWidth = 225;
  widgetHeight = 250;
  strokeColor = 'red';
  MainTextcolor = '#000';
  innerRadius: any = 85;
  outerRadius: any = 60;
  errorHandling: any;
  errorMessage: any;
  widgetWidth2: number;
  @Input() cardButtonAction;
  complianceData = [];
  assetsCountData = [];

  private overallComplianceUrl = environment.overallCompliance.url;
  private overallComplianceMethod = environment.overallCompliance.method;
  private agAndDomain = {};

  constructor(
    private utils: UtilsService,
    private loggerService: LoggerService,
    private workflowService: WorkflowService,
    private overallComplianceService: OverallComplianceService,
    private fetchResourcesService: FetchResourcesService,
    private router: Router,
    private activatedRoute: ActivatedRoute
  ) { }

  ngOnInit() {
    this.activatedRoute.queryParams.subscribe((params) => {
      this.agAndDomain["ag"] = params["ag"];
      this.agAndDomain["domain"] = params["domain"];
      this.getComplianceData();
      this.getAssetsCountData();
    });
  }

  private getAssetsCountData() {
    this.fetchResourcesService
      .getAllResourceCounts(this.agAndDomain)
      .subscribe((results) => {
        try {
          this.assetsCountData = [];

          for (let asset of results["assetcount"]) {
            this.assetsCountData.push({
              asset: asset["type"],
              count: asset["count"],
            });
          }

          this.assetsCountData.sort((a, b) => b.count - a.count);
        } catch (error) {
          this.loggerService.log("error", error);
        }
      });
  }

  private getComplianceData() {
    this.overallComplianceService
      .getOverallCompliance(
        this.agAndDomain,
        this.overallComplianceUrl,
        this.overallComplianceMethod
      )
      .subscribe((response) => {
        try {
          this.complianceData = [];
          response[0].data.forEach((element) => {
            if(element[1].title=="Governance"){
              element[1].title="Operations";
            }
            else if(element[1].title=="Cost Optimization"){
              element[1].title="Cost";
            }
            if (element[1]["val"] <= 40) {
              element[1]["class"] = "red";
            } else if (element[1]["val"] <= 75) {
              element[1]["class"] = "or";
            } else {
              element[1]["class"] = "gr";
            }
            this.complianceData.push(element[1]);
          });
        } catch (e) {
          console.log(e);
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
