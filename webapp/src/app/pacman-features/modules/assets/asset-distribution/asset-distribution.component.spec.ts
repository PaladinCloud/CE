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

 import { async, ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';
import { RouterTestingModule } from '@angular/router/testing';
import { DataCacheService } from 'src/app/core/services/data-cache.service';
import { WindowExpansionService } from 'src/app/core/services/window-expansion.service';
import { WorkflowService } from 'src/app/core/services/workflow.service';
import { AwsResourceTypeSelectionService } from 'src/app/pacman-features/services/aws-resource-type-selection.service';
import { SearchFilterPipe } from 'src/app/shared/pipes/search-filter.pipe';
import { LoggerService } from 'src/app/shared/services/logger.service';
import { RefactorFieldsService } from 'src/app/shared/services/refactor-fields.service';
import { RouterUtilityService } from 'src/app/shared/services/router-utility.service';
import { UtilsService } from 'src/app/shared/services/utils.service';
import { NgApexchartsModule } from 'ng-apexcharts';

import { AssetDistributionComponent } from './asset-distribution.component';
import { FormsModule } from '@angular/forms';
 
 describe('AssetDistributionComponent', () => {
   let component: AssetDistributionComponent;
   let fixture: ComponentFixture<AssetDistributionComponent>;
 
   beforeEach(waitForAsync(() => {
     TestBed.configureTestingModule({
       imports: [NgApexchartsModule, FormsModule, RouterTestingModule],
       declarations: [AssetDistributionComponent, SearchFilterPipe],
       providers: [
         AwsResourceTypeSelectionService,
         DataCacheService,
         LoggerService,
         RefactorFieldsService,
         RouterUtilityService,
         UtilsService,
         WindowExpansionService,
         WorkflowService,
       ],
     }).compileComponents();
   }));
 
   beforeEach(() => {
     fixture = TestBed.createComponent(AssetDistributionComponent);
     component = fixture.componentInstance;
     fixture.detectChanges();
   });
 
   it('should create', () => {
     expect(component).toBeTruthy();
   });
 });
 