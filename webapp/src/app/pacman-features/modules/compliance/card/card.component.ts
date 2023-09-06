import {
    Component,
    EventEmitter,
    HostListener,
    Input,
    OnInit,
    Output,
    ViewChild,
} from '@angular/core';
import { MatMenuTrigger } from '@angular/material/menu';
import { ActivatedRoute, Router } from '@angular/router';
import { WorkflowService } from 'src/app/core/services/workflow.service';
import { ErrorHandlingService } from 'src/app/shared/services/error-handling.service';
import { LoggerService } from 'src/app/shared/services/logger.service';
import { UtilsService } from 'src/app/shared/services/utils.service';

@Component({
    selector: 'app-card',
    templateUrl: './card.component.html',
    styleUrls: ['./card.component.css'],
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

  @ViewChild('menuTrigger') matMenuTrigger: MatMenuTrigger;
  isCustomSelected = false;
  @Input("fromDate") fromDate: Date;
  toDate: Date = new Date();
  selectedItem = "All time";
  breadcrumbPresent: string = "Dashboard";


    constructor(
        private errorHandling: ErrorHandlingService,
        private utils: UtilsService,
        private loggerService: LoggerService,
        private workflowService: WorkflowService,
        private router: Router,
        private activatedRoute: ActivatedRoute
    ) {}

    ngOnInit() {}

    ifCustomSelected(){
        if (this.selectedItem == "Custom") {
            this.selectedItem = "-1";
        }
    }

    onDropdownClose(){
        if (this.selectedItem == "-1") {
            this.selectedItem = "Custom";
        }
    }

    handleGraphIntervalSelection = (e) => {
        this.toDate = new Date();
        this.selectedItem = e;
        e = e.toLowerCase();
        if (e == 'all time' || e == 'custom') {
            if (e == 'custom') {
                this.matMenuTrigger.openMenu();
                this.isCustomSelected = true;
                return;
            }
            this.dateIntervalSelected(new Date(2022, 0, 1), this.toDate);
            return;
        }
        const date = new Date();
        this.isCustomSelected = false;
        switch (e) {
            case '1 week':
                date.setDate(date.getDate() - 7);
                break;
            case '1 month':
                date.setMonth(date.getMonth() - 1);
                break;
            case '6 months':
                date.setMonth(date.getMonth() - 6);
                break;
            case '12 months':
                date.setFullYear(date.getFullYear() - 1);
                break;
        }

        this.dateIntervalSelected(date, this.toDate);
    };

    dateIntervalSelected(fromDate?, toDate?) {
        const queryParamObj = {};
        if (fromDate) {
            queryParamObj['from'] = fromDate;
        }
        if (toDate) {
            queryParamObj['to'] = toDate;
        }
        this.isCustomSelected = false;
        this.matMenuTrigger.closeMenu();
        this.graphIntervalSelected.emit(queryParamObj);
    }

    navigateToViolationsByCategory(bar) {
        const category = bar?.title;
        const percent = bar?.val;
        if(typeof percent === "number" && percent!=100){
            this.workflowService.addRouterSnapshotToLevel(
                this.router.routerState.snapshot.root,
                0,
                this.breadcrumbPresent,
            );
    
            const filterParam = this.utils.makeFilterObj({
                'policyCategory.keyword': category.toLowerCase(),
                'issueStatus.keyword': "open"
            });
    
            this.router.navigate(['/pl/compliance/issue-listing'], {
                relativeTo: this.activatedRoute,
                queryParams: {...filterParam, 'tempFilters': true},
                queryParamsHandling: 'merge',
            });
        }
    }

    navigateDataTable(event) {
        this.workflowService.addRouterSnapshotToLevel(this.router.routerState.snapshot.root);
        try {
            const queryObj = event;
            const eachParams = { 'severity.keyword': queryObj.toLowerCase() };
            const newParams = this.utils.makeFilterObj(eachParams);
            if (queryObj !== undefined) {
                this.router.navigate(['../../../../', 'compliance', 'issue-listing'], {
                    relativeTo: this.activatedRoute,
                    queryParams: newParams,
                    queryParamsHandling: 'merge',
                });
            }
        } catch (error) {
            this.errorMessage = this.errorHandling.handleJavascriptError(error);
            this.loggerService.log('error', error);
        }
    }

    @HostListener('window:resize', ['$event']) onResize(event) {
        const element_cpuUtilization = document.getElementById('cpuUtilization');
        if (element_cpuUtilization) {
            this.widgetWidth2 = parseInt(
                window
                    .getComputedStyle(element_cpuUtilization, null)
                    .getPropertyValue('width')
                    .split('px')[0],
                10,
            );
        }
        const element_statsDoughnut = document.getElementById('statsDoughnut');
        if (element_statsDoughnut) {
            let widthValue = parseInt(
                window
                    .getComputedStyle(element_statsDoughnut, null)
                    .getPropertyValue('width')
                    .split('px')[0],
                10,
            );
            widthValue = widthValue - 155;
            if (widthValue > 150) {
                this.widgetWidth = widthValue;
            }
        }
    }
}
