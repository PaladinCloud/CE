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

import { HttpClient, HttpHandler, HttpResponse } from '@angular/common/http';
import { NO_ERRORS_SCHEMA, Renderer2 } from '@angular/core';
import { async, ComponentFixture, TestBed } from '@angular/core/testing';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { MatSelectModule } from '@angular/material/select';
import { MatTableModule } from '@angular/material/table';
import { By } from '@angular/platform-browser';
import { BrowserAnimationsModule } from '@angular/platform-browser/animations';
import { Router } from '@angular/router';
import { RouterTestingModule } from '@angular/router/testing';
import { Observable, of, ReplaySubject } from 'rxjs';
import { AssetGroupObservableService } from 'src/app/core/services/asset-group-observable.service';
import { AuthService } from 'src/app/core/services/auth.service';
import { DataCacheService } from 'src/app/core/services/data-cache.service';
import { DomainTypeObservableService } from 'src/app/core/services/domain-type-observable.service';
import { WorkflowService } from 'src/app/core/services/workflow.service';
import { BackNavigationComponent } from 'src/app/shared/back-navigation/back-navigation.component';
import { CommonResponseService } from 'src/app/shared/services/common-response.service';
import { ErrorHandlingService } from 'src/app/shared/services/error-handling.service';
import { HttpService } from 'src/app/shared/services/http-response.service';
import { LoggerService } from 'src/app/shared/services/logger.service';
import { RefactorFieldsService } from 'src/app/shared/services/refactor-fields.service';
import { RouterUtilityService } from 'src/app/shared/services/router-utility.service';
import { UtilsService } from 'src/app/shared/services/utils.service';
import { TableWrapperComponent } from 'src/app/shared/table-wrapper/table-wrapper.component';
import { TableComponent } from 'src/app/shared/table/table.component';

import { PolicyKnowledgebaseComponent } from './policy-knowledgebase.component';


describe('Policyknowledgebase', () => {
  let component: PolicyKnowledgebaseComponent;
  let fixture: ComponentFixture<PolicyKnowledgebaseComponent>;

  let fakeCommonResponseService: CommonResponseService;
  let fakeAssetGroupObservableService: AssetGroupObservableService;
  let fakeLoggerService: LoggerService;
  let fakeErrorHandlingService: ErrorHandlingService;
  let fakeWorkflowService: WorkflowService;
  let fakeDomainTypeObservableService: DomainTypeObservableService;
  let fakeRouterUtilityService: RouterUtilityService;

  beforeEach(async () => {

    await TestBed.configureTestingModule({
      imports: [RouterTestingModule, MatSelectModule, MatTableModule, BrowserAnimationsModule, FormsModule, ReactiveFormsModule],
      declarations: [ PolicyKnowledgebaseComponent ],
      providers: [
        CommonResponseService,
        AssetGroupObservableService,
        LoggerService,
        ErrorHandlingService,
        WorkflowService,
        DomainTypeObservableService,
        RouterUtilityService,
        ErrorHandlingService,
        DataCacheService, 
        UtilsService, 
        RefactorFieldsService,
        HttpService, HttpClient, HttpHandler
        ],
        schemas: [NO_ERRORS_SCHEMA],
    })
    .compileComponents();
  // fakeCommonResponseService = TestBed.inject(CommonResponseService);

  });


  beforeEach(() => {
    fixture = TestBed.createComponent(PolicyKnowledgebaseComponent);
    component = fixture.componentInstance;

    fakeCommonResponseService = fixture.debugElement.injector.get(CommonResponseService);
    spyOn(fakeCommonResponseService, 'getData').and.returnValue(of({data:{response: [{col1:"row1 col1", col2:"row1 col2"}]}}));

    fakeDomainTypeObservableService = fixture.debugElement.injector.get(DomainTypeObservableService);
    spyOn(fakeDomainTypeObservableService, 'getDomainType').and.returnValue(of("aws"));
    
    // service2.getDomainType().subscribe(val => {
    // console.log(val);
    // });
    

    // fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('renders an independent table wrapper', () => {
    component.getData();
    fixture.detectChanges();
    const { debugElement } = fixture;
    const tableWrapperComponent = debugElement.query(By.css('app-table-wrapper'));
    expect(tableWrapperComponent).toBeTruthy();
  });

  it('renders an independent back navigation', () => {
    const { debugElement } = fixture;
    const backNavigationComponent = debugElement.query(By.css('app-back-navigation'));
    expect(backNavigationComponent).toBeTruthy();
  });

  it("selected domain should be aws", () => {
    fixture.detectChanges();
    expect(component.selectedDomain).toBe("aws");
  })

  it("should call CommonResponseService getData method", ()=> {
    component.getData();
    fixture.detectChanges();
    expect(fakeCommonResponseService.getData).toHaveBeenCalled();
  })

  it("should call DomainTypeObservableService.getDomainType method", ()=> {
    expect(fakeDomainTypeObservableService.getDomainType).toHaveBeenCalled();
  })
});
