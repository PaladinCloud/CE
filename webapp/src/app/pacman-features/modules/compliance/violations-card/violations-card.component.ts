import { Component, Input, OnChanges, OnInit, SimpleChanges } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { WorkflowService } from 'src/app/core/services/workflow.service';
import { UtilsService } from 'src/app/shared/services/utils.service';

@Component({
    selector: 'app-violations-card',
    templateUrl: './violations-card.component.html',
    styleUrls: ['./violations-card.component.css'],
})
export class ViolationsCardComponent implements OnInit, OnChanges {
  @Input() card: any;
  @Input() breadcrumbPresent;

  keyList = [];
  readonly ISSUE_LISTING_ROUTE = 'issue-listing';
  
  ngOnChanges(changes: SimpleChanges): void {
    this.keyList = this.getKeys(this.card.subInfo)
  }

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

  redirect(name) {
    name = name.toLowerCase();
    this.workflowService.addRouterSnapshotToLevel(this.router.routerState.snapshot.root, 0, this.breadcrumbPresent);
      let eachParams:any = { 'severity.keyword': this.card.name.toLowerCase(), "issueStatus.keyword": "open" };
      if(eachParams){
        const newParams = this.utils.makeFilterObj(eachParams);
        this.router.navigate(['../', this.ISSUE_LISTING_ROUTE], { relativeTo: this.activatedRoute, queryParams: {"tempFilters":true, ...newParams}, queryParamsHandling: 'merge' });
      }
    }
}
