import {
    Component,
    Input,
    Output,
    EventEmitter,
    ViewChild,
    ElementRef,
    OnChanges,
    HostListener,
    NgZone,
    AfterViewInit,
} from '@angular/core';
import { DecimalPipe } from '@angular/common';
import * as d3 from 'd3-selection';
import * as d3Shape from 'd3-shape';
import * as d3Scale from 'd3-scale';
import * as d3Array from 'd3-array';
import * as d3Axis from 'd3-axis';
import * as d3Zoom from 'd3-zoom';
import * as d3Brush from 'd3-brush';
import * as d3TimeFormat from 'd3-time-format';
import { LoggerService } from '../services/logger.service';
import { WindowExpansionService } from 'src/app/core/services/window-expansion.service';
import { FormControl } from '@angular/forms';

@Component({
    selector: 'app-multiline-zoom-graph',
    templateUrl: './multiline-zoom-graph.component.html',
    styleUrls: ['./multiline-zoom-graph.component.css'],
    providers: [DecimalPipe],
})
export class MultilineZoomGraphComponent implements AfterViewInit, OnChanges {
    @Input() id: any;
    @Input() graphWidth: any = 700;
    @Input() graphHeight: number = 290;
    @Input() graphLinesData: any = [];
    @Input() yAxisLabel = '';
    @Input() xAxisLabel = 'Timeline';
    @Input() showLegend = true;
    @Input() showArea = false;
    @Input() singlePercentLine = false;
    @Input() hoverActive = true;
    @Input() doNotShowContext;
    @Input() showLandingTooltip = true;
    axisMinValue;
    axisMaxValue;
    graphTickValues;
    @Input() errorMessage = '';
    allLines = [];
    selectedLines = [];
    multiSelectForm = new FormControl();
    duplicateGraphData = [];
    numberOfLinesToDisplay = 10;
    triangleShape = d3Shape.symbol().type(d3Shape.symbolTriangle).size(100);
    circleShape = d3Shape.symbol().type(d3Shape.symbolCircle).size(100);
    squareShape = d3Shape.symbol().type(d3Shape.symbolSquare).size(100);

    tooltipWidth = '260px';

    @Output() error: EventEmitter<any> = new EventEmitter();

    @ViewChild('graphContainer') graphContainer: ElementRef;

    private margin = { top: 0, right: 20, bottom: 30, left: 60 };
    private margin2;

    private lineColorsObject = {
        // Colors for different type lines
        total: '#3F4A59', // Dark blue(shade)
        overall: '#3F4A59', // Dark blue(shade)
        tagging: '#f2425f', // Red
        security: '#00569d', // Blue
        Compliance: '#00B946', // Green
        patching: '#00569D', // Dark blue(shade)
        'other policies': '#F2425F', // Red
        costOptimization: '#289cf7', // Light Blue
        certificate: '#289CF7', // Sky Blue
        governance: '#26ba9d', // Green
        vulnerability: '#645EC5', // Purple
        high: '#F75C03', // Orange
        low: '#FFE00D', // Green
        medium: '#FFB00D', // Sky blue
        critical: '#D40325', // Red
        extra1: '#00b946', // Green
        noncompliant: '#D40325', // Red
        compliant: '#00B946', // Green
        pullrequest: '#f2425f', // Red
        repository: '#3f4a59', // Dark Blue,
        noOfAlerts: '#3F4A59', // Dark Blue,
        benchmark: '#CF62A8',
        company: '#D4772C',
        category: '#B072CC',
        'total assets': '#506EA7',
    };
    private lineColorsArray = Object.keys(this.lineColorsObject);
    private countInRange: any;

    // Lowest and Highest Line in the graph
    private lowerLine: any;
    private lowerLineIndex: any = 0;
    private higherLine: any;
    private higherLineIndex: any = 1;

    // Smaller and longer line (to help plot the area between the bottom and top lines)
    private smallerLine: any;
    private longerLine: any;

    private width: number;
    private timeLineWidth: number;
    private height: number;
    private height2: number;
    private x: any;
    private y: any;
    private x2: any;
    private y2: any;
    private svg: any;
    private line: d3Shape.Line<[number, number]>;
    private line2: d3Shape.Line<[number, number]>;
    private area: any;
    private combinedData: any = [];
    private data: any;

    // For zoom
    private context: any;
    private brush: any;
    private brush2: any;
    private zoom: any;
    private focus: any;

    // Main graph highlighted area start and end points
    private highlightAreaStart: any;
    private highlightAreaEnd: any;
    private formattedStartDate: any;
    private formattedEndDate: any;

    graphData: any = [];
    private interval: any;

    // Variables to hold start and end date of available data
    private dataStartDate: any;
    private dataEndDate: any;

    private yLogAxis = false;

    private legendHover: any = [];
    private searchAnObjectFromArray: any;
    private bisectDate: any;
    private firstMouseMove = 0;
    private highlightedLine;

    constructor(
        private loggerService: LoggerService,
        private windowExpansionService: WindowExpansionService,
        private ngZone: NgZone,
        private numbersPipe: DecimalPipe,
    ) {
        // window.onresize = (e) => {
        //   // ngZone.run will help to run change detection
        //   this.ngZone.run(() => {
        //   this.graphWidth = parseInt(window.getComputedStyle(this.graphContainer.nativeElement, null).getPropertyValue('width'), 10);
        //   });
        // };
    }

    @HostListener('window:resize', ['$event']) onSizeChanges() {
        this.graphWidth = parseInt(
            window
                .getComputedStyle(this.graphContainer.nativeElement, null)
                .getPropertyValue('width'),
            10,
        );
        // this.graphHeight = parseInt(window.getComputedStyle(this.graphContainer.nativeElement, null).getPropertyValue('height'), 10)-70;
        this.init();
    }

    handleLegendClick(e) {
        for (let i = 0; i < this.graphData.length; i++) {
            const processedKey = this.getHyphenSeperatedString(this.graphData[i].key);
            const lineElement = document.querySelector('.' + processedKey);
            lineElement.setAttribute('opacity', '1');
        }
        if (e.toLowerCase() == this.highlightedLine) {
            this.highlightedLine = undefined;
        } else {
            this.highlightedLine = e.toLowerCase();
        }
        if (this.highlightedLine) {
            const currentKey = this.getHyphenSeperatedString(e);
            for (let i = 0; i < this.graphData.length; i++) {
                const processedKey = this.getHyphenSeperatedString(this.graphData[i].key);
                if (processedKey != currentKey) {
                    const lineElement = document.querySelector('.' + processedKey);
                    lineElement.setAttribute('opacity', '0.1');
                    // const tooltipElement = document.querySelector("."+processedKey+"-tooltip");
                    // tooltipElement.setAttribute("display", "none");
                }
            }
        }
    }

    plotGraph() {
        try {
            this.removeZeroValues();
            this.initSvg();
            this.initComponents();
            if (this.graphLinesData.length >= 2 && this.showArea === true) {
                this.computeLowerAndHigherLines();
                this.formatDataForArea();
            }
            this.drawAxisAndGrid();
            this.drawLine();
            if (this.hoverActive && this.data.length > 1) {
                this.drawHover();
                if (this.firstMouseMove == 0 && this.showLandingTooltip) this.drawlandingToolTip();
            }
        } catch (error) {
            this.error.emit('jsError');
            this.loggerService.log('error', error);
        }
    }

    removeEmptyDataObject() {
        const tempArray = [];
        this.graphLinesData.forEach((line) => {
            if (line?.values?.length > 0) {
                tempArray.push(line);
            }
        });
        this.graphLinesData = tempArray.slice();
    }

    private removeInvalidValues() {
        this.graphLinesData.forEach((line) => {
            if (line.key === 'compliance_percent') {
                line.key = 'Compliance';
            }
            line.values.forEach((value) => {
                if (isNaN(value['value'])) {
                    value['value'] = 1;
                }
            });
        });
        this.graphData = this.graphLinesData;
    }

    init() {
        try {
            if (this.graphLinesData && this.id) {
                this.removeEmptyDataObject();
                this.removeInvalidValues();

                this.graphData = this.graphLinesData;
                this.interval = this.graphData[0].values.length;

                // Set dimensions for the graph and timeline axis

                this.width = this.graphWidth - this.margin.left - this.margin.right;
                this.timeLineWidth = this.width * 1;
                this.height = this.graphHeight - this.margin.top - this.margin.bottom - 70;
                this.height2 = this.graphHeight - this.margin2.top - this.margin2.bottom - 4;

                this.graphData = this.graphLinesData;

                // To remove the graph content if its present before re-plotting
                this.removeGraphSvg();

                // Plot the graph and do all associated processes
                this.plotGraph();
            }
        } catch (error) {
            this.error.emit('jsError');
            this.loggerService.log('error', error);
        }
    }

    resizeGraph() {
        try {
            if (this.graphLinesData) {
                // Reset the dimensions
                this.width = this.graphWidth - this.margin.left - this.margin.right;
                this.timeLineWidth = this.width * 1;
                this.height = this.graphHeight - this.margin.top - this.margin.bottom - 70;
                this.height2 = this.graphHeight - this.margin2.top - this.margin2.bottom - 4;

                // To remove the graph content if its present before re-plotting
                this.removeGraphSvg();

                // Plot the graph and do all associated processes
                this.plotGraph();
            }
        } catch (error) {
            this.error.emit('jsError');
            this.loggerService.log('error', error);
        }
    }

    removeGraphSvg() {
        if (
            d3
                .selectAll('#' + this.id)
                .select('svg')
                .selectAll('g') !== undefined
        ) {
            d3.selectAll('#' + this.id)
                .select('svg')
                .selectAll('g')
                .remove();
            d3.selectAll('#' + this.id)
                .select('svg')
                .append('g');
        }
    }

    ngAfterViewInit(): void {
        setTimeout(() => {
            this.graphWidth = parseInt(
                window
                    .getComputedStyle(this.graphContainer.nativeElement, null)
                    .getPropertyValue('width'),
                10,
            );
            this.init();
        }, 0);

        this.windowExpansionService.getExpansionStatus().subscribe((countMap: any) => {
            setTimeout(() => {
                this.graphWidth = parseInt(
                    window
                        .getComputedStyle(this.graphContainer.nativeElement, null)
                        .getPropertyValue('width'),
                    10,
                );
                this.init();
            }, 0);
        });
    }

    getInnerHeight(elm) {
        let computed = getComputedStyle(elm),
            padding = parseInt(computed.paddingTop) + parseInt(computed.paddingBottom);

        return elm.clientHeight - padding;
    }

    changeDisplayedLines(index: number, lineText: string) {
        if (this.multiSelectForm.value.length == 0) {
            this.multiSelectForm.setValue(this.selectedLines);
            return;
        }
        try {
            if (this.multiSelectForm.value.length <= this.numberOfLinesToDisplay) {
                this.selectedLines = this.multiSelectForm.value;

                if (this.multiSelectForm.value.indexOf(lineText) >= 0) {
                    const indexOfLine = this.duplicateGraphData.findIndex(
                        (item) => item.key == lineText,
                    );
                    this.graphLinesData.push(this.duplicateGraphData[indexOfLine]);
                } else {
                    const indexOfLine = this.graphLinesData.findIndex(
                        (item) => item.key == lineText,
                    );
                    this.graphLinesData.splice(indexOfLine, 1);
                }
                this.init();
            } else {
                this.multiSelectForm.setValue(this.selectedLines);
            }

            this.focus
                .selectAll('.hover.rectCoverDate')
                .attr('height', this.multiSelectForm.value.length * 27 + 44 + 'px')
                .attr('width', this.tooltipWidth);
        } catch (e) {
            this.loggerService.log('error', e);
        }
    }

    ngOnChanges() {
        this.firstMouseMove = 0;
        this.duplicateGraphData = this.graphLinesData;
        let colorObj = [
            '#CF62A8',
            '#B072CC',
            '#D4772C',
            '#7986D8',
            '#E7A500',
            '#D66BAE',
            '#AA64C5',
            '#C66E29',
            '#7C8EDD',
            '#F4B800',
        ];
        this.allLines = [];
        this.graphLinesData.forEach((element, i) => {
            if (element && element.key) {
                this.allLines.push(element.key);
                // const color = Math.floor(Math.random()*16777215).toString(16);
                if (!this.lineColorsObject[element.key?.toLowerCase()])
                    this.lineColorsObject[element.key.toLowerCase()] = colorObj[i];
            }
        });
        this.selectedLines = this.allLines.slice(0, this.numberOfLinesToDisplay);
        this.multiSelectForm.setValue(this.selectedLines);
        this.graphLinesData = this.duplicateGraphData.slice(0, this.numberOfLinesToDisplay);

        this.axisMinValue = Infinity;
        this.axisMaxValue = 0;
        for (let i = 0; i < this.graphLinesData.length; i++) {
            this.graphLinesData[i]?.values?.forEach((element) => {
                if (element.value < this.axisMinValue) {
                    this.axisMinValue = element.value;
                }
                if (element.value > this.axisMaxValue) {
                    this.axisMaxValue = element.value;
                }
            });
        }

        this.graphTickValues = this.getTickValuesForYAxis();
        this.margin2 = {
            top: this.graphHeight - 40,
            right: 20,
            bottom: 30,
            left: 20,
        };
    }

    // rounds off to nearest multiple of roundOffToVal bounding up to valToRoundOff
    roundOff(valToRoundOff, roundOffToVal, max = true) {
        if (roundOffToVal < 0 || valToRoundOff === 0) return 0;
        if (max) return Math.ceil(valToRoundOff / roundOffToVal) * roundOffToVal;
        else return Math.floor(valToRoundOff / roundOffToVal) * roundOffToVal;
    }

    getNearestPowerOf10(val, offSet = 0) {
        if (val > 0) {
            return Math.pow(10, Math.floor(Math.log10(val)) - offSet); // 10^( |_ log10(val) _| - 1 )
        } else {
            return 1;
        }
    }

    getTickValuesForYAxis() {
        const roundOffMultiple = 2;
        const maxNumberOfTickValues = 6;
        let graphTickValues = [];
        if (this.singlePercentLine) {
            graphTickValues = [0, 20, 40, 60, 80, 100];
        } else {
            let roundOffToVal =
                roundOffMultiple *
                this.getNearestPowerOf10(this.axisMaxValue - this.axisMinValue, 1);
            // we need floor of val to get tickVal below minVal, minval should be rounded off to nearest roundOffToVal
            const yMin = this.roundOff(this.axisMinValue, roundOffToVal, false);
            const yMax = this.roundOff(this.axisMaxValue, roundOffToVal);
            let y = this.roundOff(
                Math.ceil((yMax - yMin) / (maxNumberOfTickValues - 1)),
                roundOffToVal,
            );
            let x = yMin;
            for (let i = 0; i < maxNumberOfTickValues; i++) {
                const tickVal = x + i * y;
                graphTickValues.push(tickVal);
                if (tickVal > this.axisMaxValue) break;
            }
            this.axisMinValue = graphTickValues[0];
            this.axisMaxValue = graphTickValues[graphTickValues.length - 1];
        }
        return graphTickValues;
    }

    private removeZeroValues() {
        try {
            for (let i = 0; i < this.graphData.length; i++) {
                for (let j = 0; j < this.graphData[i].values.length; j++) {
                    if (this.graphData[i].values[j].value === 0) {
                        this.graphData[i].values[j].value = 1;
                    }
                }
            }
        } catch (error) {
            this.error.emit('jsError');
            this.loggerService.log('error', error);
        }
    }

    private getDataRangePoints() {
        if (this.graphLinesData) {
            try {
                this.graphData = this.graphLinesData;
                let dataStartObject;
                let dataEndObject;

                for (let i = 0; i < this.graphData.length; i++) {
                    if (this.graphData[i]['values'].length > 0) {
                        dataStartObject = new Date(this.graphData[i]['values'][0]['date']);
                        dataEndObject = new Date(
                            this.graphData[i]['values'][this.graphData[i]['values'].length - 1][
                                'date'
                            ],
                        );
                    }
                }

                let initialValueObtained = false;
                const lineStartDates = [];
                const lineEndDates = [];

                this.graphData.forEach((line) => {
                    initialValueObtained = false;
                    line['values'].forEach((day) => {
                        const currentDay = new Date(day.date);

                        if (
                            day.value > 0 &&
                            day['zero-value'] === false &&
                            initialValueObtained === false &&
                            dataStartObject <= currentDay
                        ) {
                            lineStartDates.push(currentDay);
                            initialValueObtained = true;
                        } else {
                            if (
                                (day.value === 0 ||
                                    (day.value === 1 && day['zero-value'] === true)) &&
                                initialValueObtained === true &&
                                dataStartObject >= currentDay
                            ) {
                                lineEndDates.push(currentDay);
                            }
                        }
                    });
                });

                this.dataStartDate =
                    lineStartDates.length > 0
                        ? d3Array.min(lineStartDates, (c) => {
                              return c;
                          })
                        : dataStartObject;
                this.dataEndDate =
                    lineEndDates.length > 0
                        ? d3Array.max(lineEndDates, (c) => {
                              return c;
                          })
                        : dataEndObject;
            } catch (error) {
                this.error.emit('jsError');
                this.loggerService.log('error', error);
            }
        }
    }

    private initSvg() {
        d3.selectAll('#' + this.id)
            .select('svg')
            .attr('width', this.graphWidth);
        d3.selectAll('#' + this.id)
            .select('svg')
            .attr('height', this.graphHeight)
            .attr('viewBox', '0 0 ' + this.graphWidth + ' ' + this.graphHeight);
        this.svg = d3
            .select('#' + this.id)
            .select('svg')
            .append('g')
            .attr('transform', 'translate(' + 60 + ',' + this.margin.top + ')');
    }

    private revertBackZeroValues() {
        this.graphLinesData.forEach((line) => {
            line.values.forEach((value) => {
                if (value['zero-value'] === true && value['value'] === 1) {
                    value['value'] = 0;
                }
            });
        });
        this.graphData = this.graphLinesData;
    }

    private initComponents() {
        this.data = this.graphData.map((v) => v.values.map((z) => z.date))[0];

        this.x = d3Scale.scaleTime().range([0, this.width]);
        const maxValue = d3Array.max(this.graphData, (c) => {
            return d3Array.max(c[`values`], (d) => {
                return d[`value`];
            });
        });
        if (maxValue < 1000) {
            this.yLogAxis = false;
            this.y = d3Scale.scaleLinear().range([this.height, 0]);
            this.revertBackZeroValues();
            if (maxValue > 1) {
                if (this.singlePercentLine) {
                    // To show a scale of 1-100 if we're showing a single percentage line (out of 100)
                    this.y.domain([0, 100]).nice();
                } else {
                    this.y.domain([this.axisMinValue, this.axisMaxValue]).nice();
                }
            } else {
                // If the max value itself if 0, we'll be keeping a default range of 0-10
                this.y.domain([0, 10]).nice();
            }
        } else {
            this.yLogAxis = false;
            this.y = d3Scale.scaleLinear().range([this.height, 0]);

            this.y.domain([this.axisMinValue, this.axisMaxValue]).nice();
        }
        this.x2 = d3Scale.scaleTime().range([0, this.timeLineWidth]);
        this.y2 = d3Scale.scaleLinear().range([this.height2, 0]);

        // To get the starting and ending dates within which data value is > 0
        this.getDataRangePoints();

        // this.x.domain(d3Array.extent(this.data, (d: Date) => d ));

        this.x.domain([this.dataStartDate, this.dataEndDate]);
        // Note : You can add '.nice()' function at the end of this.x.domain() to have evenly spaced ticks with starting and
        //        ending point included

        // this.x2.domain(d3Array.extent(this.data, (d: Date) => d ));

        this.x2.domain([this.dataStartDate, this.dataEndDate]);
        // Note : You can add '.nice()' function at the end of this.x.domain() to have evenly spaced ticks with starting and
        //        ending point included

        this.brush = d3Brush
            .brushX()
            .extent([
                [0, 0],
                [this.timeLineWidth, this.height2 / 2],
            ])
            .on('brush end', this.brushed.bind(this));

        this.brush2 = d3Brush
            .brushX()
            .extent([
                [0, 0],
                [this.width, this.height],
            ])
            .on('brush end', this.areaHighlighter.bind(this));

        this.zoom = d3Zoom
            .zoom()
            .scaleExtent([1, Infinity])
            .translateExtent([
                [0, 0],
                [this.width, this.height],
            ])
            .extent([
                [0, 0],
                [this.width, this.height],
            ])
            .on('zoom', this.zoomed.bind(this));

        this.svg
            .append('defs')
            .append('clipPath')
            .attr('id', 'clip')
            .append('rect')
            .attr('width', 0)
            .attr('height', this.height + 7);

        this.focus = this.svg
            .append('g')
            .attr('class', 'focus')
            .attr('transform', 'translate(0,' + (2 * this.margin.top + this.height2 + 40) + ')');
        if (!this.doNotShowContext) {
            this.context = this.svg
                .append('g')
                .attr('class', 'context')
                .attr('width', this.timeLineWidth)
                .attr('transform', 'translate(' + -this.margin2.left + ',' + this.margin.top + ')')
                .attr('class', 'hide');
        }
    }

    private areaHighlighter() {
        try {
            this.focus.select('.handle.handle--w').attr('display', 'block');
            this.focus.select('.handle.handle--e').attr('display', 'block');
            this.highlightAreaStart = this.x.invert(
                parseInt(this.focus.select('.handle.handle--w').attr('x'), 10),
            );
            this.highlightAreaEnd = this.x.invert(
                parseInt(this.focus.select('.handle.handle--e').attr('x'), 10),
            );

            const allData = this.graphData;
            this.countInRange = 0;

            for (let i = 0; i < allData.length; i++) {
                for (let j = 0; j < allData[i].values.length; j++) {
                    const date = allData[i].values[j].date;
                    if (date >= this.highlightAreaStart && date <= this.highlightAreaEnd) {
                        if (!allData[i].values[j][`zero-value`]) {
                            this.countInRange = this.countInRange + allData[i].values[j].value;
                        }
                    }
                }
            }
            this.formattedStartDate =
                this.highlightAreaStart.getMonth() +
                1 +
                '/' +
                this.highlightAreaStart.getDate() +
                '/' +
                this.highlightAreaStart.getFullYear();
            this.formattedEndDate =
                this.highlightAreaEnd.getMonth() +
                1 +
                '/' +
                this.highlightAreaEnd.getDate() +
                '/' +
                this.highlightAreaEnd.getFullYear();
            this.updateMainAreaLabels();
        } catch (error) {
            this.error.emit('jsError');
            this.loggerService.log('error', error);
        }
    }

    private checkNumberOfTicks() {
        try {
            // Function to limit the number of ticks to <= 9

            const x = this.focus.select('.axis--x').selectAll('.tick')._groups[0];
            if (x !== undefined && x !== null && x.length >= 9) {
                for (let i = 0; i < x.length; i++) {
                    x[i].style['display'] = '';
                }
                for (let j = 0; j < x.length; j++) {
                    if (j % 2 === 0) {
                        x[j].style['display'] = 'none';
                    }
                }
            } else {
                if (x) {
                    for (let k = 0; k < x.length; k++) {
                        x[k].style['display'] = 'block';
                    }
                }
            }
        } catch (error) {
            this.error.emit('jsError');
            this.loggerService.log('error', error);
        }
    }

    private computeLowerAndHigherLines() {
        // Computing the Lowest / Highest line and their indices respectively
        this.lowerLine = this.graphData[0];
        for (let i = 0; i < this.graphData.length; i++) {
            if (this.graphData[i][`values`].length < this.lowerLineIndex) {
                this.lowerLineIndex = i;
            } else {
                if (this.graphData[i][`values`].length > this.higherLineIndex) {
                    this.higherLineIndex = i;
                }
            }
        }
        this.lowerLine = this.graphData[this.lowerLineIndex];
        this.higherLine = this.graphData[this.higherLineIndex];

        if (this.lowerLine[`values`].length > this.higherLine[`values`].length) {
            this.smallerLine = this.higherLine;
            this.longerLine = this.lowerLine;
        } else {
            this.smallerLine = this.lowerLine;
            this.longerLine = this.higherLine;
        }
    }

    private formatDataForArea() {
        // Merging the data of top and bottom lines to supply to plot shaded area
        // between top and bottom graph lines
        this.combinedData = [];
        for (let i = 0; i < this.smallerLine[`values`].length; i++) {
            const lowerX = new Date(this.smallerLine[`values`][i].date);
            let lowerY = 0;
            let higherX = 0;
            let higherY = 0;

            // Forming mm/dd/yyyy of both higher and lower line data points as we cannot directly compare both,
            // as time may change in the data point for any given day

            const smallerLineDate = new Date(this.smallerLine[`values`][i].date);
            const smallerLineFormattedDate =
                smallerLineDate.getMonth() +
                '/' +
                smallerLineDate.getDate() +
                '/' +
                smallerLineDate.getFullYear();

            for (let j = 0; j < this.longerLine[`values`].length; j++) {
                const longerLineDate = new Date(this.longerLine[`values`][j].date);
                const longerLineFormattedDate =
                    longerLineDate.getMonth() +
                    '/' +
                    longerLineDate.getDate() +
                    '/' +
                    longerLineDate.getFullYear();

                if (longerLineFormattedDate === smallerLineFormattedDate) {
                    higherX = this.longerLine[`values`][j].date;
                    this.longerLine[`values`][j].value === 0
                        ? (higherY = 1)
                        : (higherY = this.longerLine[`values`][j].value);
                    this.smallerLine[`values`][i].value === 0
                        ? (lowerY = 1)
                        : (lowerY = this.smallerLine[`values`][i].value);
                    const obj = {
                        x0: higherX,
                        x1: lowerX,
                        y0: higherY,
                        y1: lowerY,
                    };

                    this.combinedData.push(obj);
                    break;
                }
            }
        }
    }

    private updateMainAreaLabels() {
        try {
            // Updating Main area Highlighter labels value and location
            if (!isNaN(this.highlightAreaStart.getMonth())) {
                this.focus
                    .selectAll('.brush')
                    .select('.area-start')
                    .text(
                        this.highlightAreaStart.getMonth() +
                            1 +
                            '/' +
                            this.highlightAreaStart.getDate() +
                            '/' +
                            this.highlightAreaStart.getFullYear(),
                    );
                this.focus
                    .selectAll('.brush')
                    .select('.area-end')
                    .text(
                        this.highlightAreaEnd.getMonth() +
                            1 +
                            '/' +
                            this.highlightAreaEnd.getDate() +
                            '/' +
                            this.highlightAreaEnd.getFullYear(),
                    );
            } else {
                this.focus.selectAll('.brush').select('.area-start').text('');
                this.focus.selectAll('.brush').select('.area-end').text('');
            }
            this.focus
                .selectAll('.brush')
                .select('.area-start')
                .attr('x', this.focus.select('.handle.handle--w').attr('x'));
            this.focus
                .selectAll('.brush')
                .select('.area-end')
                .attr('x', this.focus.select('.handle.handle--e').attr('x'));

            // Check if main-graph area highlight block is shown or not
            if (!isNaN(parseInt(this.focus.select('.handle.handle--w').attr('x'), 10))) {
                this.focus
                    .selectAll('.brush')
                    .select('.line-head-1-container')
                    .attr('display', 'block');
                this.focus
                    .selectAll('.brush')
                    .select('.line-head-2-container')
                    .attr('display', 'block');
                this.focus
                    .selectAll('.brush')
                    .select('.line-head-1-container')
                    .attr(
                        'transform',
                        'translate(' +
                            (parseInt(this.focus.select('.handle.handle--w').attr('x'), 10) + 3) +
                            ', 0)',
                    );
                this.focus
                    .selectAll('.brush')
                    .select('.line-head-2-container')
                    .attr(
                        'transform',
                        'translate(' +
                            (parseInt(this.focus.select('.handle.handle--e').attr('x'), 10) + 3) +
                            ', 0)',
                    );
            } else {
                // hide the diamond-shaped blocks if the area highter is not shown
                this.focus
                    .selectAll('.brush')
                    .select('.line-head-1-container')
                    .attr('display', 'none');
                this.focus
                    .selectAll('.brush')
                    .select('.line-head-2-container')
                    .attr('display', 'none');
            }

            // TODO: To darken the tick text which are within the time range selected in both main graph and timeline scrollbar
        } catch (error) {
            this.error.emit('jsError');
            this.loggerService.log('error', error);
        }
    }

    private updateTimeLineLabels() {
        try {
            // Updating Timeline labels value and location
            this.context
                .selectAll('.brush')
                .select('.brush-value1')
                .text(this.x.domain()[0].getFullYear());
            this.context
                .selectAll('.brush')
                .select('.brush-value1')
                .attr('x', this.context.select('.handle.handle--w').attr('x'));
            this.context
                .selectAll('.brush')
                .select('.brush-value2')
                .text(this.x.domain()[1].getFullYear());
            this.context
                .selectAll('.brush')
                .select('.brush-value2')
                .attr('x', this.context.select('.handle.handle--e').attr('x'));
        } catch (error) {
            this.error.emit('jsError');
            this.loggerService.log('error', error);
        }
    }

    private brushed() {
        try {
            if (d3.event.sourceEvent && d3.event.sourceEvent.type === 'zoom') {
                return;
            } // ignore brush-by-zoom
            const s = d3.event.selection || this.x2.range();
            const domainData = s.map(this.x2.invert, this.x2);
            this.x.domain(s.map(this.x2.invert, this.x2));
            this.areaHighlighter();

            // Re-render / update graph lines and area in between them
            this.focus.selectAll('.line').attr('d', this.line);
            this.focus.select('.line.line2').attr('d', this.line);
            this.focus.selectAll('.area').attr('d', this.area);

            // To retain the spherical shape of the timeline end points
            this.context
                .selectAll('.handle')
                .attr('height', '4')
                .attr('y', '0')
                .attr('ry', '6')
                .attr('rx', '6')
                .attr('width', '4');
            this.context.select('.brush').select('.selection').attr('height', '4');
            this.context.select('.brush').select('.overlay').attr('height', '3');

            // Updating the vertical grid lines according to the change in x-axis
            this.svg.selectAll('.grid.vertical').call(
                d3Axis
                    .axisBottom(this.x)
                    .tickSize(-this.height)
                    .tickFormat((d) => ''),
            );

            this.checkNumberOfTicks();

            // To adjust number of ticks on x-axis depending on time-range
            const ticksLengthNumber = this.checkDataLength();

            // checks time range on zooming....
            const checkDataLengthNumber = this.checkTimeRange(domainData);

            // Keeping the x-axis tick text in the date format - mm / dd
            if (checkDataLengthNumber[`value`] === false) {
                if (checkDataLengthNumber[`ticks`] === undefined) {
                    this.focus
                        .selectAll('.axis--x')
                        .call(
                            d3Axis
                                .axisBottom(this.x)
                                .ticks(ticksLengthNumber)
                                .tickFormat(d3TimeFormat.timeFormat('%m / %d')),
                        );
                } else {
                    this.focus
                        .selectAll('.axis--x')
                        .call(
                            d3Axis
                                .axisBottom(this.x)
                                .ticks(checkDataLengthNumber[`ticks`])
                                .tickFormat(d3TimeFormat.timeFormat('%m / %d')),
                        );
                }
            } else {
                this.focus
                    .selectAll('.axis--x')
                    .call(d3Axis.axisBottom(this.x).ticks(ticksLengthNumber));
            }

            this.focus
                .selectAll('.axis--x.zoom-axis')
                .call(
                    d3Axis
                        .axisBottom(this.x)
                        .ticks(10)
                        .tickFormat(d3TimeFormat.timeFormat('%b / %d')),
                );

            this.svg
                .select('.zoom')
                .call(
                    this.zoom.transform,
                    d3Zoom.zoomIdentity.scale(this.width / (s[1] - s[0])).translate(-s[0], 0),
                );

            this.updateMainAreaLabels();
            this.updateTimeLineLabels();
        } catch (error) {
            this.error.emit('jsError');
            this.loggerService.log('error', error);
        }
    }

    private checkTimeRange(domainData) {
        try {
            const date1 = domainData[0];
            const date2 = domainData[1];
            let dataObj = {};
            const hours = Math.abs(date1 - date2) / 36e5;
            if (hours < 68 && hours > 51) {
                dataObj = {
                    value: false,
                    ticks: 3,
                };
            } else if (hours <= 51 && hours > 25) {
                dataObj = {
                    value: false,
                    ticks: 2,
                };
            } else if (hours <= 25) {
                dataObj = {
                    value: true,
                    ticks: 2,
                };
            } else {
                dataObj = {
                    value: false,
                    ticks: undefined,
                };
            }

            return dataObj;
        } catch (error) {
            this.error.emit('jsError');
            this.loggerService.log('error', error);
        }
    }

    private checkDataLength() {
        try {
            let ticksNumber;
            if (this.interval < 4) {
                ticksNumber = 3;
            } else if (this.interval >= 4 && this.interval < 8) {
                ticksNumber = 4;
            } else {
                ticksNumber = 6;
            }

            return ticksNumber;
        } catch (error) {
            this.error.emit('jsError');
            this.loggerService.log('error', error);
        }
    }

    private zoomed() {
        try {
            if (d3.event.sourceEvent && d3.event.sourceEvent.type === 'brush') {
                return;
            } // ignore zoom-by-brush
            const t = d3.event.transform;
            this.x.domain(t.rescaleX(this.x2).domain());
            this.areaHighlighter();

            // Re-render / update graph lines and area in between them
            this.focus.selectAll('.line').attr('d', this.line);
            this.focus.select('.line.line2').attr('d', this.line);
            this.focus.selectAll('.area').attr('d', this.area);

            // To retain the spherical shape of the timeline end points
            this.context.selectAll('.handle').attr('height', '4').attr('y', '0').attr('width', '4');
            this.context.select('.brush').select('.selection').attr('height', '4');
            this.context.select('.brush').select('.overlay').attr('height', '3');

            // Updating the vertical grid lines according to the change in x-axis
            this.svg.selectAll('.grid.vertical').call(
                d3Axis
                    .axisBottom(this.x)
                    .tickSize(-this.height)
                    .tickFormat((d) => ''),
            );

            this.checkNumberOfTicks();

            // Keeping the x-axis tick text in the date format - mm / dd
            this.focus
                .selectAll('.axis--x')
                .call(
                    d3Axis
                        .axisBottom(this.x)
                        .ticks(10)
                        .tickFormat(d3TimeFormat.timeFormat('%m / %d')),
                );
            this.focus
                .selectAll('.axis--x.zoom-axis')
                .call(
                    d3Axis
                        .axisBottom(this.x)
                        .ticks(10)
                        .tickFormat(d3TimeFormat.timeFormat('%b / %d')),
                );

            this.context.select('.brush').call(this.brush.move, this.x2.range().map(t.invertX, t));

            this.updateMainAreaLabels();
            this.updateTimeLineLabels();
        } catch (error) {
            this.error.emit('jsError');
            this.loggerService.log('error', error);
        }
    }

    private logFormat(d) {
        const x = Math.log(d) / Math.log(10) + 1e-6;
        return Math.abs(x - Math.floor(x)) < 0.7 ? this.abbreviateNumber(d) : '';
    }

    private drawAxisAndGrid() {
        // Main Graph x-axis
        this.focus
            .append('g')
            .attr('class', 'axis axis--x')
            .attr('transform', 'translate(0,' + this.height + ')')
            .call(
                d3Axis
                    .axisBottom(this.x)
                    // .ticks(ticksNumber)
                    .ticks(this.interval)
                    .tickSizeInner(6)
                    .tickSizeOuter(0)
                    .tickFormat(d3TimeFormat.timeFormat('%b / %d')),
                /* .tickValues(this.x.ticks(10).concat(this.x.domain()))  <---- Add this line if you want to force the starting and ending
                                                                            data point's dates in the x-axis */
            );

        // Main Graph hidden top axis associated with main area highlighter
        this.focus
            .append('g')
            .attr('class', 'axis axis--x area-zoom')
            .attr('transform', 'translate(0,0)')
            .call(
                d3Axis.axisBottom(this.x2),
                // .ticks(ticksNumber)
            );

        const ticksLengthValue = this.checkDataLength();

        // Axis associated with Timeline / scrollbar
        this.context
            .append('g')
            .attr('class', 'axis axis--x zoom-axis')
            .attr('transform', 'translate(0,' + 1 + ')')
            .attr('width', this.width * 0.5)
            .call(
                d3Axis
                    .axisBottom(this.x2)
                    .ticks(ticksLengthValue)
                    .tickSizeInner(10)
                    .tickSizeOuter(0)
                    .tickFormat(d3TimeFormat.timeFormat('%b / %d')),
                /* .tickValues(this.x2.ticks(10).concat(this.x2.domain()))  <---- Add this line if you want to force the starting and ending
                                                                            data point's dates in the x-axis */
            );

        // Horizontal Grid Lines
        this.svg
            .append('g')
            .attr('class', 'grid horizontal')
            .attr('transform', 'translate(0,' + (2 * this.margin.top + this.height2 + 40) + ')')
            .call(
                d3Axis
                    .axisLeft(this.y)
                    .tickValues(this.graphTickValues)
                    .tickSize(-this.width)
                    .tickFormat((d) => ''),
            );

        // Vertical Grid Lines
        this.svg
            .append('g')
            .attr('class', 'grid vertical')
            .attr(
                'transform',
                'translate(0,' + (2 * this.margin.top + this.height2 + this.height + 40) + ')',
            )
            .call(
                d3Axis
                    .axisBottom(this.x2)
                    .ticks(7)
                    .tickSize(-this.height)
                    .tickFormat((d) => ''),
            );

        // Main Graph y-axis and associated Label
        this.focus
            .append('g')
            .attr('class', 'axis axis--y')
            .attr('stroke-width', '0')
            .attr('stroke', '#fff')
            .call(
                d3Axis
                    .axisLeft(this.y)
                    .tickValues(this.graphTickValues)
                    .tickFormat((d) => this.abbreviateNumber(d)),
            )
            .append('text')
            .attr('class', 'axis-title')
            .attr('transform', 'rotate(-90)')
            .attr('y', -60)
            .attr('x', -60)
            // .attr('dx', '-10%')
            .attr('dy', '.71em')
            .attr('stroke-width', '0.5')
            .attr('fill', '#2c2e3d')
            .attr('stroke', '#2c2e3d')
            .style('text-anchor', 'end')
            .text(this.yAxisLabel);

        // this.focus
        // .append("g")
        // .attr("transform", "translate(0," + (this.graphHeight) + ")")
        // .append("text")
        // .attr("class", "axis-title")
        // .attr("stroke-width", "0.5")
        // .attr("fill", "#2c2e3d")
        // .attr("stroke", "#2c2e3d")
        // .style("text-anchor", "end")
        // .attr("y", 0)
        // .attr("x", this.graphWidth/2 - 30)
        // .text(this.xAxisLabel);
    }

    abbreviateNumber(number) {
        number = parseInt(number, 10);
        number =
            number > 1000000
                ? number / 1000000 + 'M'
                : number > 1000
                  ? number / 1000 + 'K'
                  : number;
        return number;
    }

    private drawLine() {
        // Line Graphs

        this.line = d3Shape
            .line()
            .x((d: any) => this.x(d.date))
            .y((d: any) => this.y(d.value))
            .curve(d3Shape.curveMonotoneX);

        this.line2 = d3Shape
            .line()
            .x((d: any) => this.x2(d.date))
            .y((d: any) => this.y2(d.value))
            .curve(d3Shape.curveMonotoneX);

        const lineKeys = Object.keys(this.lineColorsObject);
        for (let i = 0; i < this.graphData.length; i++) {
            const processedKey = this.getHyphenSeperatedString(this.graphData[i].key);
            const lineColor =
                this.lineColorsObject[this.graphData[i].key?.toLowerCase()] ||
                this.lineColorsObject[lineKeys[i]];

            this.focus
                .append('path')
                .datum(this.graphData[i].values)
                .attr('clip-path', 'url(#clip)')
                // .transition()
                // .duration(5000)
                .attr('class', `line ${processedKey} line + ${i + 1}`)
                .attr('fill', 'none')
                .attr('stroke-width', '2px')
                .attr('stroke', lineColor)
                .attr('d', this.line)
                .attr('opacity', 1)
                .attr('display', i > 4 ? 'none' : 'block');
        }

        this.area = d3Shape
            .area()
            .x((d: any) => this.x(d.date))
            .y0(this.height)
            .y1((d: any) => this.y(d.value))
            .curve(d3Shape.curveMonotoneX);

        this.focus
            .append('g')
            .attr('class', 'brush')
            .call(this.brush2)
            .call(this.brush2.move, this.x.range());

        this.context
            .append('g')
            .attr('class', 'brush')
            .call(this.brush)
            .call(this.brush.move, this.x2.range());

        // Diamond shaped head on the draggable lines on the main graph
        this.focus
            .select('.brush')
            .append('g')
            .attr('class', 'line-head-1-container')
            .attr('transform', 'translate(0, 0)')
            .append('rect')
            .attr('class', 'line-head-1')
            .attr('width', '7')
            .attr('height', '7')
            .attr('fill', '#336cc9')
            .attr('transform', 'translate(0, -5) rotate(45)');

        this.focus
            .select('.brush')
            .append('g')
            .attr('class', 'line-head-2-container')
            .attr('transform', 'translate(' + this.width + ', 0)')
            .append('rect')
            .attr('class', 'line-head-2')
            .attr('width', '7')
            .attr('height', '7')
            .attr('fill', '#336cc9')
            .attr('transform', 'translate(0, -5) rotate(45)');

        // Dynamic Label Text associated with draggable lines on the time-line selector
        this.context
            .selectAll('.brush')
            .append('text')
            .attr('class', 'brush-value1')
            .attr('transform', 'rotate(0)')
            .attr('dy', '-1em')
            .attr('stroke-width', '1')
            .attr('x', '0')
            .attr('transform', 'translate(-9, 0)')
            .attr('fill', 'none')
            .attr('stroke', '#2c2e3d')
            .text(this.x2.domain()[0].getFullYear());

        this.context
            .selectAll('.brush')
            .append('text')
            .attr('class', 'brush-value2')
            .attr('transform', 'rotate(0)')
            .attr('dy', '-1em')
            .attr('x', this.timeLineWidth)
            .attr('transform', 'translate(-9, 0)')
            .attr('stroke-width', '1')
            .attr('fill', 'none')
            .attr('stroke', '#2c2e3d')
            .text(this.x2.domain()[1].getFullYear());

        if (this.showArea) {
            this.focus
                .append('path')
                .datum(this.graphData[this.graphData.length - 1]['values'])
                .attr('class', 'area')
                .attr('fill', '#ccc')
                .attr('stroke-width', '0.5')
                .attr('stroke', '#2c2e3d')
                .attr('d', this.area);
        }

        this.context.selectAll('.handle').attr('height', '4').attr('y', '0');

        this.context
            .select('.brush')
            .select('.overlay')
            .attr('width', this.timeLineWidth)
            .attr('height', '3');
        this.context.select('.brush').selectAll('.handle').attr('width', '4').attr('height', '4');
        this.context.select('.brush').selectAll('.selection').attr('height', '4');

        this.svg.selectAll('.grid').lower();
        this.focus.selectAll('.line').lower();
        this.focus.selectAll('.area').lower();
        this.focus.selectAll('.selection').raise();
        this.focus.selectAll('.handle').raise();

        this.svg
            .select('#clip rect')
            // .transition()
            // .duration(2000)
            .attr('width', this.width)
            .attr('transform', 'translate(0,' + -7 + ')');

        // Temporarily hide the area selection feature on top of the graph
        this.focus.selectAll('.brush').attr('display', 'none');

        if (this.yLogAxis) {
            this.checkNumberOfLogAxisTicks();
        }
    }

    private checkNumberOfLogAxisTicks() {
        try {
            // Function to limit the number of ticks on Y-axis, when its a log scale, if the number
            // of ticks are more than 4

            let tickGroups = this.focus
                .select('.axis.axis--y')
                .selectAll('.tick')
                .selectAll('text')._parents;
            const grids = this.svg.selectAll('.grid.horizontal').selectAll('.tick')._groups;
            const numOfTicks = tickGroups.length;
            let last = 0;
            let count = 0;
            tickGroups.forEach((item, i) => {
                // TO DO : need to write more logic here to cut down extra tick lines when logscale is used.
                if (
                    (tickGroups[i - 1] &&
                        parseFloat(tickGroups[i - 1].textContent) <= this.axisMaxValue &&
                        parseFloat(tickGroups[i - 1].textContent) >= this.axisMinValue) ||
                    (tickGroups[i + 1] &&
                        parseFloat(tickGroups[i + 1].textContent) >= this.axisMinValue &&
                        parseFloat(tickGroups[i + 1].textContent) <= this.axisMaxValue)
                ) {
                    tickGroups[i].style['display'] = 'block';
                    grids.forEach((grid) => {
                        grid[i].style['display'] = 'block';
                    });
                } else if (
                    parseFloat(tickGroups[i]) <= this.axisMaxValue &&
                    parseFloat(tickGroups[i]) >= this.axisMinValue
                ) {
                    tickGroups[i].style['display'] = 'block';
                    grids.forEach((grid) => {
                        grid[i].style['display'] = 'block';
                    });
                } else {
                    tickGroups[i].style['display'] = 'none';
                    grids.forEach((grid) => {
                        grid[i].style['display'] = 'none';
                    });
                }
            });
        } catch (error) {
            this.loggerService.log('error', error);
        }
    }

    getValueBasedShape(value) {
        let shape, color;
        if (typeof value == 'string' && value.toLowerCase() == 'no data') {
            shape = this.squareShape;
            color = 'transparent';
        } else if (value < 80) {
            shape = this.circleShape;
            color = '#CC6262';
        } else if (value >= 80 && value < 90) {
            shape = this.triangleShape;
            color = '#F5B66F';
        } else {
            shape = this.squareShape;
            color = '#6AAA75';
        }
        return [shape, color];
    }

    getTypeBasedValue(str) {
        if (this.singlePercentLine) {
            return str + '%';
        }
        return str;
    }

    getTrimmedString(str: string) {
        if (str.length > 30) {
            str = str.substring(0, 27) + '...';
        }
        return str;
    }

    getHyphenSeperatedString(str) {
        return str.replace(/[^a-zA-Z]/g, '').toLowerCase();
    }

    private drawHover() {
        const self = this;
        const numOfLines = this.graphLinesData.length - 1;

        this.legendHover = this.graphLinesData.map((eachLine) => {
            return eachLine.values;
        });

        this.searchAnObjectFromArray = (key, value, array) => {
            const obj = array.filter((objs) => {
                return objs[key] === value;
            })[0];

            return obj;
        };

        this.bisectDate = d3Array.bisector((d) => d[`date`]).left;

        if (this.firstMouseMove > 0) {
            this.focus
                .append('rect')
                .attr('class', 'hover rectCoverDate')
                .attr('fill', '#fff')
                // .attr("fill-opacity", "0.9")
                .attr('height', this.multiSelectForm.value.length * 27 + 44 + 'px')
                .attr('width', this.tooltipWidth)
                .attr('stroke', '#DFE6EE')
                .attr('display', 'none')
                .attr('text-align', 'middle')
                .attr('rx', 8)
                .attr('ry', 8)
                .attr('x', 15)
                .attr('y', -7);

            this.focus
                .append('text')
                .attr('class', 'hover dateData')
                .attr('x', 2)
                .attr('dy', '.35em');

            for (let i = 0; i < self.graphLinesData.length; i++) {
                const processedKey = this.getHyphenSeperatedString(this.graphData[i].key);

                self.focus
                    .append('text')
                    .attr('class', 'hover ' + processedKey + '-tooltip' + ' valueData')
                    .attr('x', 90)
                    .attr('dy', '0.50em');

                self.focus
                    .append('text')
                    .attr('class', 'hover ' + processedKey + '-tooltip' + ' rectText')
                    .style('stroke-width', '0px')
                    .style('fill', '#000')
                    .style('text-transform', 'capitalize')
                    .style('font-size', '10px')
                    .attr('x', 9)
                    .attr('dy', '.35em');

                self.focus
                    .append('rect')
                    .attr('class', 'hover ' + processedKey + '-tooltip' + ' rectData')
                    .attr('fill', self.lineColorsObject[self.graphLinesData[i].key?.toLowerCase()])
                    .attr('height', '14px')
                    .attr('width', '14px')
                    .attr('display', 'none')
                    .attr('text-align', 'middle')
                    .style(
                        'stroke',
                        self.lineColorsObject[self.graphLinesData[i].key?.toLowerCase()],
                    )
                    .attr('x', -110)
                    .attr('y', -7);

                self.focus
                    .append('path')
                    .attr('class', 'hover ' + processedKey + '-tooltip' + ' valueBasedShape');
            }

            this.focus
                .append('line')
                .attr('class', 'x hover')
                .style('stroke', '#bbb')
                .style('stroke-width', '2px')
                .style('opacity', 1)
                .attr('display', 'none')
                .attr('y1', 0)
                .attr('y2', this.height);
        }
        this.svg
            .append('rect')
            .attr('transform', 'translate(' + 0 + ',' + (2 * this.margin.top + 40) + ')')
            .attr('class', 'overlay')
            .attr('width', this.width)
            .attr('height', this.height)
            .on('mouseover', () => {
                this.multiSelectForm.value.forEach((key, i) => {
                    const processedKey = this.getHyphenSeperatedString(key);

                    if (this.highlightedLine) {
                        if (processedKey == this.highlightedLine) {
                            self.focus
                                .selectAll(`.hover.${processedKey}-tooltip`)
                                .style('display', 'block');
                        } else {
                            self.focus
                                .selectAll(`.hover.${processedKey}-tooltip`)
                                .style('display', 'none');
                        }
                    } else {
                        self.focus
                            .selectAll(`.hover.${processedKey}-tooltip`)
                            .style('display', 'block');
                    }
                    if (this.firstMouseMove) {
                        self.focus
                            .selectAll(`.hover.${processedKey}-tooltip`)
                            .style('opacity', '1');
                    } else {
                        self.focus
                            .selectAll(`.hover.${processedKey}-tooltip`)
                            .style('opacity', '0');
                    }
                });
                self.focus.selectAll('.x.hover').style('display', 'block');
                self.focus.selectAll('.hover.rectCoverDate').style('display', 'block');
                self.focus.selectAll('.hover.dateData').style('display', 'block');
            })
            .on('mouseout', () => {
                self.focus.selectAll('.hover').style('display', 'none');
            })
            .on('mousemove', mousemove);

        function mousemove() {
            try {
                self.firstMouseMove++;
                if (self.firstMouseMove === 1) {
                    self.drawHover();
                }
                const mousePosition = d3.mouse(this)[0];
                const formatDate = d3TimeFormat.timeFormat('%b %d');
                const formatYear = d3TimeFormat.timeFormat('%Y');
                const label = self.x.invert(d3.mouse(this)[0]);
                const dobj = {};
                const axisRange = self.x2.range()[1];
                dobj[`label`] = label;

                self.legendHover.map(function (legend) {
                    for (let i = 0; i < self.graphLinesData.length; i++) {
                        const processedKey = self.getHyphenSeperatedString(
                            self.graphLinesData[i].key,
                        );
                        const currentLineValues = self.graphLinesData[i].values;

                        dobj['value-' + processedKey] = 'No Data';
                        for (let j = 0; j < currentLineValues.length; j++) {
                            if (currentLineValues[j]['zero-value']) {
                                currentLineValues[j].value = 0;
                            }
                            const valueDate = new Date(currentLineValues[j].date);
                            const hoverDate = new Date(label);
                            valueDate.setHours(0, 0, 0, 0);
                            hoverDate.setHours(0, 0, 0, 0);
                            if (valueDate.toString() === hoverDate.toString()) {
                                dobj['value-' + processedKey] = self.numbersPipe.transform(
                                    currentLineValues[j].value,
                                );
                                break;
                            }
                        }
                    }
                });

                self.focus
                    .select('.x.hover')
                    .attr('transform', 'translate(' + self.x(dobj[`label`]) + ',' + 0 + ')')
                    .attr('y2', self.height);

                const valueData = {};
                const rectCoverData = {};
                const rectCoverDate = {};
                const rectText = {};
                const rectData = {};
                const dateData = {};
                const yearData = {};
                const valueBasedShapePos = {};

                rectText['dx'] =
                    mousePosition < axisRange / 4
                        ? '4em'
                        : mousePosition > axisRange * 0.75
                          ? -265
                          : '5em';
                // rectData["dx"] = mousePosition < axisRange / 4 ? "70" : "-140";
                valueBasedShapePos['x'] =
                    mousePosition < axisRange / 4
                        ? 255
                        : mousePosition > axisRange * 0.75
                          ? -45
                          : 270;
                valueBasedShapePos['y'] =
                    mousePosition < axisRange / 4 ? 42 : mousePosition > axisRange * 0.75 ? 42 : 42;
                valueData['dx'] =
                    mousePosition < axisRange / 4
                        ? '11.5em'
                        : mousePosition > axisRange * 0.75
                          ? -180
                          : '12em';
                valueData['dy'] =
                    mousePosition < axisRange / 4
                        ? '3.5em'
                        : mousePosition > axisRange * 0.75
                          ? '3.8em'
                          : '3.5em';
                rectData['dx'] =
                    mousePosition < axisRange / 4
                        ? '2.1em'
                        : mousePosition > axisRange * 0.75
                          ? -275
                          : '3.1em';
                rectData['dy'] =
                    mousePosition < axisRange / 4
                        ? '3.5em'
                        : mousePosition > axisRange * 0.75
                          ? '3.8em'
                          : '3.5em';
                rectData['y'] =
                    mousePosition < axisRange / 4 ? 28 : mousePosition > axisRange * 0.75 ? 31 : 28;
                rectCoverDate['dx'] =
                    mousePosition < axisRange / 4
                        ? '15'
                        : mousePosition > axisRange * 0.75
                          ? -285
                          : '25';
                dateData['dx'] =
                    mousePosition < axisRange / 4
                        ? '2em'
                        : mousePosition > axisRange * 0.75
                          ? -275
                          : '3em';
                dateData['dy'] =
                    mousePosition < axisRange / 4
                        ? '1.5em'
                        : mousePosition > axisRange * 0.75
                          ? '1.6em'
                          : '1.5em';
                yearData['dx'] =
                    mousePosition < axisRange / 4
                        ? '1em'
                        : mousePosition > axisRange * 0.75
                          ? '-14em'
                          : '2em';
                yearData['dy'] =
                    mousePosition < axisRange / 4
                        ? '2.5em'
                        : mousePosition > axisRange * 0.75
                          ? '2.8em'
                          : '2.5em';

                for (let m = 0; m < self.multiSelectForm.value.length; m++) {
                    const processedKey = self.getHyphenSeperatedString(
                        self.multiSelectForm.value[m],
                    );
                    const yPosFact = self.highlightedLine ? 0 : m;

                    self.focus
                        .select('.dateData')
                        .attr('transform', 'translate(' + self.x(dobj[`label`]) + ',' + 0 + ')')
                        .text(formatDate(dobj[`label`]).toUpperCase())
                        .attr('dx', dateData['dx'])
                        .attr('dy', dateData['dy']);

                    self.focus
                        .select('.valueData' + '.' + processedKey + '-tooltip')
                        .attr('transform', 'translate(' + self.x(dobj[`label`]) + ',' + 0 + ')')
                        .text(self.getTypeBasedValue(dobj['value-' + processedKey]))
                        .attr('dx', valueData['dx'])
                        .attr('dy', 4 + yPosFact * 2.5 + 'em');

                    self.focus
                        .select('.rectData' + '.' + processedKey + '-tooltip')
                        .attr('transform', 'translate(' + self.x(dobj[`label`]) + ',' + 0 + ')')
                        .attr('x', rectData['dx'])
                        .attr('y', 3 + yPosFact * 2.5 + 'em');

                    if (
                        self.singlePercentLine &&
                        self.getValueBasedShape(dobj['value-' + processedKey])
                    ) {
                        const [shape, color] = self.getValueBasedShape(
                            dobj['value-' + processedKey],
                        );
                        self.focus
                            .select('.valueBasedShape' + '.' + processedKey + '-tooltip')
                            .attr('d', shape)
                            .attr('fill', color)
                            .attr(
                                'transform',
                                'translate(' +
                                    (self.x(dobj[`label`]) + valueBasedShapePos['x']) +
                                    ',' +
                                    (yPosFact * 27.1 + valueBasedShapePos['y']) +
                                    ')',
                            );
                    }

                    const legend = self.multiSelectForm.value[m];
                    self.focus
                        .select('.rectText' + '.' + processedKey + '-tooltip')
                        .attr('transform', 'translate(' + self.x(dobj[`label`]) + ',' + 0 + ')')
                        .text(self.getTrimmedString(legend))
                        .attr('dx', rectText['dx'])
                        .attr('dy', 4.2 + yPosFact * 2.78 + 'em');

                    self.focus
                        .select('.rectCoverDate')
                        .attr('transform', 'translate(' + self.x(dobj[`label`]) + ',' + 0 + ')')
                        .attr('x', rectCoverDate['dx'])
                        .attr('y', -10 + yPosFact);
                }
            } catch (error) {
                self.loggerService.log('error', 'Error in mouse over - ' + error);
            }
        }
    }

    private drawlandingToolTip() {
        this.svg.selectAll('.overlay').style('display', 'none');
        this.svg.selectAll('.hover').style('display', 'none');
        const self = this;
        const numOfLines = this.graphLinesData.length - 1;

        this.legendHover = this.graphLinesData.map((eachLine) => {
            return eachLine.values;
        });

        this.searchAnObjectFromArray = (key, value, array) => {
            const obj = array.filter((objs) => {
                return objs[key] === value;
            })[0];

            return obj;
        };

        this.bisectDate = d3Array.bisector((d) => d[`date`]).left;

        this.focus
            .append('rect')
            .attr('class', 'landingToolTip rectCoverDate')
            .attr('fill', '#fff')
            // .attr("fill-opacity", "0.9")
            .attr('height', this.multiSelectForm.value.length * 27 + 44 + 'px')
            .attr('width', this.tooltipWidth)
            .attr('stroke', '#DFE6EE')
            .attr('display', 'none')
            .attr('text-align', 'middle')
            .attr('rx', 8)
            .attr('ry', 8)
            .attr('x', 15)
            .attr('y', -7);

        this.focus
            .append('text')
            .attr('class', 'landingToolTip dateData')
            .attr('x', 2)
            .attr('dy', '.35em');

        for (let i = 0; i < self.graphLinesData.length; i++) {
            const processedKey = self.getHyphenSeperatedString(self.graphData[i].key);

            self.focus
                .append('text')
                .attr('class', 'landingToolTip hover ' + processedKey + '-tooltip' + ' valueData')
                .attr('x', 90)
                .attr('dy', '0.50em');

            self.focus
                .append('text')
                .attr('class', 'landingToolTip hover ' + processedKey + '-tooltip' + ' rectText')
                .style('stroke-width', '0px')
                .style('fill', '#000')
                .style('text-transform', 'capitalize')
                .style('font-size', '10px')
                .attr('x', 9)
                .attr('dy', '.35em');

            self.focus
                .append('rect')
                .attr('class', 'landingToolTip hover ' + processedKey + '-tooltip' + ' rectData')
                .attr('fill', self.lineColorsObject[self.graphLinesData[i].key?.toLowerCase()])
                .attr('height', '14px')
                .attr('width', '14px')
                .attr('display', 'none')
                .attr('text-align', 'middle')
                .style('stroke', self.lineColorsObject[self.graphLinesData[i].key?.toLowerCase()])
                .attr('x', -110)
                .attr('y', -7);

            self.focus
                .append('path')
                .attr(
                    'class',
                    'landingToolTip hover ' + processedKey + '-tooltip' + ' valueBasedShape',
                );
        }

        this.focus
            .append('line')
            .attr('class', 'x landingToolTip')
            .style('stroke', '#bbb')
            .style('stroke-width', '2px')
            .style('opacity', 1)
            .attr('display', 'none')
            .attr('y1', 0)
            .attr('y2', this.height);

        this.svg
            .append('rect')
            .attr('transform', 'translate(' + 0 + ',' + (2 * this.margin.top + 40) + ')')
            .attr('class', 'landingToolTipOverlay')
            .attr('width', this.width)
            .attr('height', this.height)
            .on('mouseover', () => {
                this.focus.selectAll('.landingToolTip').remove();
                this.svg.select('.landingToolTipOverlay').remove();
                this.svg.selectAll('.overlay').style('display', 'block');
            });
        this.focus.selectAll('.landingToolTip').style('display', 'block');
        mousemove();

        function mousemove() {
            try {
                const mousePosition = self.graphWidth - 85;
                const formatDate = d3TimeFormat.timeFormat('%b %d');
                const formatYear = d3TimeFormat.timeFormat('%Y');
                const label = self.x.invert(mousePosition);
                const dobj = {};
                const axisRange = self.x2.range()[1];
                dobj[`label`] = label;

                self.legendHover.map(function (legend) {
                    for (let i = 0; i < self.graphLinesData.length; i++) {
                        const processedKey = self.getHyphenSeperatedString(
                            self.graphLinesData[i].key,
                        );
                        const currentLineValues = self.graphLinesData[i].values;

                        dobj['value-' + processedKey] = 'No Data';
                        for (let j = 0; j < currentLineValues.length; j++) {
                            if (currentLineValues[j]['zero-value']) {
                                currentLineValues[j].value = 0;
                            }
                            const valueDate = new Date(currentLineValues[j].date);
                            const hoverDate = new Date(label);
                            valueDate.setHours(0, 0, 0, 0);
                            hoverDate.setHours(0, 0, 0, 0);
                            if (valueDate.toString() === hoverDate.toString()) {
                                dobj['value-' + processedKey] = self.numbersPipe.transform(
                                    currentLineValues[j].value,
                                );
                                break;
                            }
                        }
                    }
                });

                self.focus
                    .select('.x.landingToolTip')
                    .attr('transform', 'translate(' + self.x(dobj[`label`]) + ',' + 0 + ')')
                    .attr('y2', self.height);

                const valueData = {};
                const rectCoverData = {};
                const rectCoverDate = {};
                const rectText = {};
                const rectData = {};
                const dateData = {};
                const yearData = {};
                const valueBasedShapePos = {};

                rectText['dx'] = -265;

                // rectData["dx"] = mousePosition < axisRange / 4 ? "70" : "-140";
                valueBasedShapePos['x'] = -45;

                valueBasedShapePos['y'] = 42;

                valueData['dx'] = -180;

                valueData['dy'] = '3.5em';

                rectData['dx'] = -275;

                rectData['dy'] = '3.5em';

                rectData['y'] = 28;

                rectCoverDate['dx'] = -285;

                dateData['dx'] = -275;

                dateData['dy'] = '1.5em';

                yearData['dx'] = '1em';

                yearData['dy'] = '2.5em';

                for (let m = 0; m < self.multiSelectForm.value.length; m++) {
                    const processedKey = self.getHyphenSeperatedString(
                        self.multiSelectForm.value[m],
                    );
                    const yPosFact = self.highlightedLine ? 0 : m;

                    self.focus
                        .select('.dateData')
                        .attr('transform', 'translate(' + self.x(dobj[`label`]) + ',' + 0 + ')')
                        .text(formatDate(dobj[`label`]).toUpperCase())
                        .attr('dx', dateData['dx'])
                        .attr('dy', dateData['dy']);

                    if (
                        self.singlePercentLine &&
                        self.getValueBasedShape(dobj['value-' + processedKey])
                    ) {
                        const [shape, color] = self.getValueBasedShape(
                            dobj['value-' + processedKey],
                        );
                        self.focus
                            .select('.valueBasedShape' + '.' + processedKey + '-tooltip')
                            .attr('d', shape)
                            .attr('fill', color)
                            .attr(
                                'transform',
                                'translate(' +
                                    (self.x(dobj[`label`]) + valueBasedShapePos['x']) +
                                    ',' +
                                    (yPosFact * 27.1 + valueBasedShapePos['y']) +
                                    ')',
                            );
                    }

                    self.focus
                        .select('.valueData' + '.' + processedKey + '-tooltip')
                        .attr('transform', 'translate(' + self.x(dobj[`label`]) + ',' + 0 + ')')
                        .text(self.getTypeBasedValue(dobj['value-' + processedKey]))
                        .attr('dx', valueData['dx'])
                        .attr('dy', 4 + yPosFact * 2.5 + 'em');

                    self.focus
                        .select('.rectData' + '.' + processedKey + '-tooltip')
                        .attr('transform', 'translate(' + self.x(dobj[`label`]) + ',' + 0 + ')')
                        .attr('x', rectData['dx'])
                        .attr('y', 3 + yPosFact * 2.5 + 'em');

                    const legend = self.multiSelectForm.value[m];
                    self.focus
                        .select('.rectText' + '.' + processedKey + '-tooltip')
                        .attr('transform', 'translate(' + self.x(dobj[`label`]) + ',' + 0 + ')')
                        .text(self.getTrimmedString(legend))
                        .attr('dx', rectText['dx'])
                        .attr('dy', 4.2 + yPosFact * 2.78 + 'em');

                    self.focus
                        .select('.rectCoverDate')
                        .attr('transform', 'translate(' + self.x(dobj[`label`]) + ',' + 0 + ')')
                        .attr('x', rectCoverDate['dx'])
                        .attr('y', -10 + yPosFact);
                }
            } catch (error) {
                self.loggerService.log('error', 'Error in mouse over - ' + error);
            }
        }
    }
}
