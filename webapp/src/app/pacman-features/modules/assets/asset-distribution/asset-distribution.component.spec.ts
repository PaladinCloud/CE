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

 import { AssetDistributionComponent } from './asset-distribution.component';
 
 describe('AssetDistributionComponent', () => {
   let component: AssetDistributionComponent;
   let fixture: ComponentFixture<AssetDistributionComponent>;
 
   beforeEach(waitForAsync(() => {
     TestBed.configureTestingModule({
       declarations: [ AssetDistributionComponent ]
     })
     .compileComponents();
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
 