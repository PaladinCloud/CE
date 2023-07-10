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

 import { Component, OnInit, Input, Output, EventEmitter } from '@angular/core';
 import { LoggerService } from '../../../shared/services/logger.service';
 import { ErrorHandlingService } from '../../../shared/services/error-handling.service';
 import {ActivatedRoute, Router} from '@angular/router';
 import {UtilsService} from '../../../shared/services/utils.service';
 import {WorkflowService} from '../../../core/services/workflow.service';
 
 @Component({
   selector: 'app-attribute',
   templateUrl: './attribute.component.html',
   styleUrls: ['./attribute.component.css'],
   providers: [
     LoggerService,
     ErrorHandlingService
   ]
 })
 export class AttributeComponent implements OnInit {
   @Input() data: any;
   @Input() pageLevel: number;
   @Input() dataObj: any;
   @Output() assetCloudType = new EventEmitter();
   urlToRedirect: any = '';
 
   dataShow = false;
   dataShowforAssets = false;
   showData = false;
 
   dataObjArray = [];
   constructor(
     private logger: LoggerService, private errorHandling: ErrorHandlingService, private router: Router,
     private activatedRoute: ActivatedRoute,
     private utilityService: UtilsService, private workflowService: WorkflowService) {
 }
 
   ngOnInit() {
     this.urlToRedirect = this.router.routerState.snapshot.url;
     this.massageData(this.dataObj);
   }
 
 /* Function for massaging the raw data into array */
 
   massageData(data) {
     let dataObjContainer = [];
     const keys = Object.keys(data);
     let keyValues, cloudType;
     let obj = {};
     for (let i = 0; i < keys.length; i++) {
        keyValues = data[keys[i]];
        keyValues.sort((a,b)=>a.name.localeCompare(b.name, 'en', { sensitivity: 'base' }));
        for (let i = 0; i < keyValues.length; i++) {
         let { name, value } = keyValues[i];
         if(typeof(value)=="string"){
           keyValues[i].value = [value];
         }
         if (name.toLowerCase() === "cloud type") {
           cloudType = value[0];
           break;
         }
        }
      }

      return data;

    } catch (error) {

    }

  /**
   * This function navigates the page mentioned  with a ruleID
   */
  navigatePage(event) {
    try {
          let resourceType = '';
          const resourceID = event;
          if(resourceID.includes("vol")){
             resourceType = 'volume'
          }else{
            resourceType = 'sg'
          }
        this.router.navigate(['../../', resourceType, resourceID],
          {relativeTo: this.activatedRoute, queryParamsHandling: "merge"}
          );
    } catch (error) {
      this.logger.log('error', error);
    }
  }
  /* navigatePage function ends here */
}
