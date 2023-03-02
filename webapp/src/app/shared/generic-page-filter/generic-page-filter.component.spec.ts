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

import { HttpClientTestingModule } from '@angular/common/http/testing';
import { async, ComponentFixture, TestBed } from '@angular/core/testing';
import { FormsModule } from '@angular/forms';
import { NgxSelectDropdownComponent } from 'ngx-select-dropdown';
import { AssetGroupObservableService } from 'src/app/core/services/asset-group-observable.service';
import { DataCacheService } from 'src/app/core/services/data-cache.service';
import { DomainTypeObservableService } from 'src/app/core/services/domain-type-observable.service';
import { SearchableDropdownComponent } from '../searchable-dropdown/searchable-dropdown.component';
import { ErrorHandlingService } from '../services/error-handling.service';
import { FilterManagementService } from '../services/filter-management.service';
import { HttpService } from '../services/http-response.service';
import { LoggerService } from '../services/logger.service';
import { RefactorFieldsService } from '../services/refactor-fields.service';
import { UtilsService } from '../services/utils.service';

import { GenericPageFilterComponent } from './generic-page-filter.component';

describe('GenericPageFilterComponent', () => {
  let component: GenericPageFilterComponent;
  let fixture: ComponentFixture<GenericPageFilterComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      imports: [ HttpClientTestingModule, FormsModule ],
      declarations: [GenericPageFilterComponent, SearchableDropdownComponent, NgxSelectDropdownComponent],
      providers: [
        AssetGroupObservableService,
        DataCacheService,
        DomainTypeObservableService,
        ErrorHandlingService,
        FilterManagementService,
        HttpService,
        LoggerService,
        RefactorFieldsService,
        UtilsService,
      ],
    }).compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(GenericPageFilterComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
