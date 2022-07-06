import { Component, Input, OnInit } from "@angular/core";
@Component({
  selector: "app-progress-bar-chart",
  templateUrl: "./progress-bar-chart.component.html",
  styleUrls: ["./progress-bar-chart.component.css"],
})
export class ProgressBarChartComponent implements OnInit {
  @Input() bars = [];

  constructor() {}

  ngOnInit() {}
}
