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

import { async, ComponentFixture, TestBed } from '@angular/core/testing';
import { FormsModule } from '@angular/forms';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatSelectModule } from '@angular/material/select';
import { NoopAnimationsModule } from '@angular/platform-browser/animations';
import { RouterTestingModule } from '@angular/router/testing';
import { AssetGroupObservableService } from 'src/app/core/services/asset-group-observable.service';
import { DataCacheService } from 'src/app/core/services/data-cache.service';
import { DomainTypeObservableService } from 'src/app/core/services/domain-type-observable.service';
import { ErrorMessageComponent } from 'src/app/shared/error-message/error-message.component';
import { ErrorHandlingService } from 'src/app/shared/services/error-handling.service';
import { LoggerService } from 'src/app/shared/services/logger.service';
import { RefactorFieldsService } from 'src/app/shared/services/refactor-fields.service';
import { UtilsService } from 'src/app/shared/services/utils.service';
import { AwsResourceTypeSelectionService } from '../../services/aws-resource-type-selection.service';

import { AwsResourceDetailsComponent } from './aws-resource-details.component';

describe('AwsResourceDetailsComponent', () => {
  let component: AwsResourceDetailsComponent;
  let fixture: ComponentFixture<AwsResourceDetailsComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      imports: [
        FormsModule,
        MatFormFieldModule,
        MatSelectModule,
        NoopAnimationsModule,
        RouterTestingModule,
      ],
      declarations: [AwsResourceDetailsComponent, ErrorMessageComponent],
      providers: [
        AssetGroupObservableService,
        AwsResourceTypeSelectionService,
        DataCacheService,
        DomainTypeObservableService,
        ErrorHandlingService,
        LoggerService,
        RefactorFieldsService,
        UtilsService,
      ],
    }).compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(AwsResourceDetailsComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
