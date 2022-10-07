import { Component, Input, OnChanges, OnInit, SimpleChanges } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { WorkflowService } from '../../../../core/services/workflow.service';
import { UtilsService } from '../../../../shared/services/utils.service';

@Component({
  selector: 'app-violations-card',
  templateUrl: './violations-card.component.html',
  styleUrls: ['./violations-card.component.css']
})
export class ViolationsCardComponent implements OnInit {
  @Input() card: any;
  constructor(
    private router: Router,
    private workflowService: WorkflowService,
    private utils: UtilsService,
    private activatedRoute: ActivatedRoute
  ) { }


  routeTo = 'issue-listing';

  ngOnInit() {
  }

  getKeys(obj){
    return Object.keys(obj);
  }

  redirect() {
    this.workflowService.addRouterSnapshotToLevel(this.router.routerState.snapshot.root);
    if (this.routeTo !== undefined) {
      const eachParams = { 'severity.keyword': this.card.name.toLowerCase() };
      const newParams = this.utils.makeFilterObj(eachParams);
      this.router.navigate(['../', this.routeTo], { relativeTo: this.activatedRoute, queryParams: newParams, queryParamsHandling: 'merge' });
    }
  }
}
