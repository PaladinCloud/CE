/*
 *Copyright 2018 T Mobile, Inc. or its affiliates. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); You may not use
 * this file except in compliance with the License. A copy of the License is located at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * or in the "license" file accompanying this file. This file is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, express or
 * implied. See the License for the specific language governing permissions and
 * limitations under the License.
 */

import { Component, OnInit, Input, OnDestroy, OnChanges, SimpleChanges} from '@angular/core';
import { AssetGroupObservableService } from '../../core/services/asset-group-observable.service';
import { Subscription } from 'rxjs';
import { Router } from '@angular/router';
import {WorkflowService} from '../../core/services/workflow.service';
import {DomainTypeObservableService} from '../../core/services/domain-type-observable.service';
import {LoggerService} from '../services/logger.service';

@Component({
  selector: 'app-breadcrumb',
  templateUrl: './breadcrumb.component.html',
  styleUrls: ['./breadcrumb.component.css']
})
export class BreadcrumbComponent implements OnInit, OnDestroy {

  @Input() breadcrumbArray: any;
  @Input() breadcrumbLinks: any;
  @Input() breadcrumbQueryParams: any;
  @Input() breadcrumbPresent: any;
  @Input() asset: any;
  @Input() isCustomParentRoute: boolean;
  @Input() parentRouteName: any;

  private assetGroupSubscription: Subscription;
  private domainSubscription: Subscription;
  private agAndDomain = {};

  constructor(private assetGroupObservableService: AssetGroupObservableService,
              private router: Router,
              private workflowService: WorkflowService,
              private domainObservableService: DomainTypeObservableService,
              private logger: LoggerService) {


  }

  ngOnInit() {
    this.initializeSubscriptions();
  }

  ngOnChanges(){
    // remove taking breadcrumb array, links as input from all components.
    const breadcrumbInfo = this.workflowService.getDetailsFromStorage()["level0"];        
    if(breadcrumbInfo){
      this.breadcrumbArray = breadcrumbInfo.map(item => item.title);
      this.breadcrumbLinks = breadcrumbInfo.map(item => item.url);
      this.breadcrumbQueryParams = breadcrumbInfo.map(item => item.queryParams);
    }
  }

  initializeSubscriptions() {
    this.assetGroupSubscription = this.assetGroupObservableService.getAssetGroup().subscribe(assetGroupName => {
      this.agAndDomain['ag'] = assetGroupName;
    });
    this.domainSubscription = this.domainObservableService.getDomainType().subscribe(domain => {
      this.agAndDomain['domain'] = domain;
    });
  }

  ngOnDestroy() {
      try {
        if (this.assetGroupSubscription) {
          this.assetGroupSubscription.unsubscribe();
        }
        if (this.domainSubscription) {
          this.domainSubscription.unsubscribe();
        }
      }catch (error) {
        this.logger.log('error', error);
      }
  }

  navigateRespective(index): any {
    let pathArr = [this.breadcrumbLinks[index]];
    this.workflowService.goToLevel(index);
    if(this.breadcrumbQueryParams){
      this.router.navigate(pathArr, {queryParams: this.breadcrumbQueryParams[index]});
    }else{
      this.router.navigate(pathArr, {queryParams: this.agAndDomain});
    }
  }

}
