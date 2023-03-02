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
import { HttpClientTestingModule } from '@angular/common/http/testing';
import { NO_ERRORS_SCHEMA, Renderer2 } from '@angular/core';
import { async, ComponentFixture, fakeAsync, TestBed, tick } from '@angular/core/testing';
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
import { TableStateService } from 'src/app/core/services/table-state.service';
import { WorkflowService } from 'src/app/core/services/workflow.service';
import { BackNavigationComponent } from 'src/app/shared/back-navigation/back-navigation.component';
import { CommonResponseService } from 'src/app/shared/services/common-response.service';
import { DownloadService } from 'src/app/shared/services/download.service';
import { ErrorHandlingService } from 'src/app/shared/services/error-handling.service';
import { HttpService } from 'src/app/shared/services/http-response.service';
import { LoggerService } from 'src/app/shared/services/logger.service';
import { RefactorFieldsService } from 'src/app/shared/services/refactor-fields.service';
import { RouterUtilityService } from 'src/app/shared/services/router-utility.service';
import { UtilsService } from 'src/app/shared/services/utils.service';

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
  let spy;

  beforeEach(async () => {

    await TestBed.configureTestingModule({
      imports: [RouterTestingModule, MatSelectModule, MatTableModule, BrowserAnimationsModule, FormsModule, ReactiveFormsModule, HttpClientTestingModule],
      declarations: [ PolicyKnowledgebaseComponent ],
      providers: [
        DataCacheService,
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
        {provide: DownloadService, useValue: {
          requestForDownload: (queryParam, downloadUrl, downloadMethod, downloadRequest, pageTitle, dataLength) => null,
          downloadData : ( queryParam, downloadUrl, downloadMethod, downloadRequest, pageTitle ) => of(null),
          animateDownload : (msg: boolean) => of(null),
          getDownloadStatus: () => of(null),
        }},
        HttpService,
        TableStateService,
        ],
        schemas: [NO_ERRORS_SCHEMA],
    })
    .compileComponents();

  });


  beforeEach(() => {
    fixture = TestBed.createComponent(PolicyKnowledgebaseComponent);
    component = fixture.componentInstance;

    fakeCommonResponseService = fixture.debugElement.injector.get(CommonResponseService);
    spyOn(fakeCommonResponseService, 'getData').and.returnValue(of({data:{response: [{col1:"row1 col1", col2:"row1 col2"}]}}));

    component['domainObservableService'].updateDomainType('Infra Platforms', 'key123');
    component['assetGroupObservableService'].updateAssetGroup('aws');


    // fakeDomainTypeObservableService = fixture.debugElement.injector.get(DomainTypeObservableService);
    // let spy = spyOn(fakeDomainTypeObservableService, 'getDomainType').and.returnValue(of("aws"));

    // service2.getDomainType().subscribe(val => {
    // console.log(val);
    // });
    

    // fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('renders an independent table component', () => {
    component.getData();
    fixture.detectChanges();
    const { debugElement } = fixture;
    const tableComponent = debugElement.query(By.css('app-table'));
    expect(tableComponent).toBeTruthy();
  });

  it('renders an independent back navigation', () => {
    const { debugElement } = fixture;
    const backNavigationComponent = debugElement.query(By.css('app-back-navigation'));
    expect(backNavigationComponent).toBeTruthy();
  });

  it("selected domain should be Infra Platforms", () => {
    expect(component.selectedDomain).toBe("Infra Platforms");
  })

  it("should call CommonResponseService getData method", ()=> {
    fixture.detectChanges();
    expect(fakeCommonResponseService.getData).toHaveBeenCalled();
  });

  it("selected asset group should be aws", () => {
    expect(component.selectedAssetGroup).toBe("aws");
  })

  it('should tell ROUTER to navigate when row clicked', fakeAsync(() => {
    const router = TestBed.inject(Router);
    spyOn(router, 'navigate').and.stub();
    let tile = {autofixEnabled:false, "Rule ID": 'NonStandardRegionRule_version-1_version-1_SubnetWithNonStandardRegion_subnet'};
    component.goToDetails(tile);
    tick();
    expect(router.navigate).toHaveBeenCalledWith(['pl', 'compliance', 'policy-knowledgebase-details', tile["Rule ID"], tile.autofixEnabled],
        { 
          queryParams: {
            // ...component.agAndDomain,
            searchValue: undefined
          },
          queryParamsHandling: 'merge',
         });
    }))
});
