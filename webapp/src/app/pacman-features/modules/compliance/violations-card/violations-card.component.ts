import { Component, Input, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { WorkflowService } from 'src/app/core/services/workflow.service';
import { UtilsService } from 'src/app/shared/services/utils.service';

@Component({
    selector: 'app-violations-card',
    templateUrl: './violations-card.component.html',
    styleUrls: ['./violations-card.component.css'],
})
export class ViolationsCardComponent implements OnInit {
    @Input() card: any;
    @Input() breadcrumbPresent;

    readonly ISSUE_LISTING_ROUTE = 'issue-listing';

    constructor(
        private router: Router,
        private workflowService: WorkflowService,
        private utils: UtilsService,
        private activatedRoute: ActivatedRoute,
    ) {}

    ngOnInit() {}

    getKeys(obj) {
        return Object.keys(obj);
    }

    redirect() {
        this.workflowService.addRouterSnapshotToLevel(
            this.router.routerState.snapshot.root,
            0,
            this.breadcrumbPresent,
        );
        const eachParams = {
            'severity.keyword': this.card.name.toLowerCase(),
            'issueStatus.keyword': 'open',
        };
        const queryParams = this.utils.makeFilterObj(eachParams);
        this.router.navigate(['../', this.ISSUE_LISTING_ROUTE], {
            relativeTo: this.activatedRoute,
            queryParams,
            queryParamsHandling: 'merge',
        });
    }
}
