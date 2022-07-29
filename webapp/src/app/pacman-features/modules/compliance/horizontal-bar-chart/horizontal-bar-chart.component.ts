import {
  AfterViewInit,
  Component,
  ElementRef,
  Input,
  NgZone,
  OnChanges,
  OnInit,
  SimpleChanges,
  ViewChild,
} from "@angular/core";
import * as d3 from "d3";

@Component({
  selector: "app-horizontal-bar-chart",
  templateUrl: "./horizontal-bar-chart.component.html",
  styleUrls: ["./horizontal-bar-chart.component.css"],
})
export class HorizontalBarChartComponent implements OnInit, OnChanges, AfterViewInit {
  margin;
  width;
  height;
  maxValue;
  svg;
  @Input() data;
  valueUpperLimit;
  barHeight = 26;
  padding = 0.5;
  @ViewChild('barChart') barChart: ElementRef;

  constructor(private ngZone: NgZone) {
    window.onresize = (e) => {
      // ngZone.run will help to run change detection
      this.ngZone.run(() => {
        this.width = this.barChart?(parseInt(window.getComputedStyle(this.barChart.nativeElement, null).getPropertyValue('width'), 10) - 20):1000;
        this.createSvg();
      });
    };
  }

  ngOnChanges(changes: SimpleChanges): void {
    this.createSvg();
  }

  ngAfterViewInit(): void {
    this.width = this.barChart?parseInt(window.getComputedStyle(this.barChart.nativeElement, null).getPropertyValue('width'), 10) - 20 - this.margin.left - this.margin.right:700;
    this.createSvg();
  }

  createSvg() {
    d3.select("#horizontal-bar-chart > *").remove();
    this.maxValue = this.data[0].count;
    this.init();
    this.initSvg();
    this.drawAxisAndBars();
  }

  private getHeight(): number {
    if (this.data.length != 0) {
      return this.data.length * this.barHeight * (1 + this.padding);
    }
    return 0;
  }

  private getTrimString = (str) => {
    if (str.length < 11) return str;
    return str.substr(0, 9) + "...";
  };

  init() {
    this.valueUpperLimit = this.maxValue;
    // set the dimensions and margins of the graph
    this.margin = { top: 0, right: 0, bottom: 0, left: 100 };
    this.width = this.barChart?parseInt(window.getComputedStyle(this.barChart.nativeElement, null).getPropertyValue('width'), 10) - 20 - this.margin.left - this.margin.right:700;
    this.height = this.getHeight() - this.margin.top - this.margin.bottom;
  }

  initSvg() {
    // append the svg object to the body of the page
    this.svg = d3
      .select("#horizontal-bar-chart")
      .append("svg")
      .attr("width", this.width + this.margin.left + this.margin.right)
      .attr("height", this.height + this.margin.top + this.margin.bottom)
      .append("g")
      .attr(
        "transform",
        "translate(" + this.margin.left + "," + (this.margin.top - 10) + ")"
      );
  }

  processData() { }

  drawAxisAndBars() {
    // Add X axis
    var x = d3
      .scaleLinear()
      .domain([0, this.valueUpperLimit * 1.25])
      .range([0, this.width]);

    // Y axis
    var y = d3
      .scaleBand()
      .range([0, this.height])
      .domain(
        this.data.map((d) => {
          return this.getTrimString(d.asset);
        })
      )
      .padding(this.padding);

    this.svg
      .append("g")
      .selectAll("text.bar")
      .data(this.data)
      .enter()
      .append("text")
      .attr("class", "label") //x position is 3 pixels to the right of the bar
      .attr("x", (d: any) => {
        return +x(d.count) + 3;
      })
      .attr("y", (d: any) => {
        return +y(this.getTrimString(d.asset)) + 18;
      })
      .attr("width", (d: any) => {
        return x(d.count);
      })
      .attr("height", this.barHeight)
      .text((d) => {
        return d.count;
      });
    this.svg
      .append("g")
      .call(d3.axisLeft(y))
      .selectAll("text")
      .attr("dy", "0.5em");

    this.svg
      .selectAll(".tick")
      .data(this.data)
      .append("title")
      .text((d) => {
        return d.asset;
      });

    this.svg.selectAll(".tick line").attr("class", "hide");
    this.svg.selectAll(".domain").attr("class", "hide");

    //Bars
    this.svg
      .selectAll("myRect")
      .data(this.data)
      .enter()
      .append("rect")
      .attr("class", "bar")
      .attr("x", x(0))
      .attr("y", (d: any) => {
        return y(this.getTrimString(d.asset));
      })
      .attr("width", (d: any) => {
        return x(d.count);
      })
      .attr("height", this.barHeight)
      .attr("fill", "#506EA7");
  }

  ngOnInit() { }
}
