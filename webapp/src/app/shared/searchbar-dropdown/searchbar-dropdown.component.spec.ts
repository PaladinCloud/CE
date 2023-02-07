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
import { DataCacheService } from 'src/app/core/services/data-cache.service';
import { LoggerService } from '../services/logger.service';
import { RefactorFieldsService } from '../services/refactor-fields.service';
import { UtilsService } from '../services/utils.service';

import { SearchbarDropdownComponent } from './searchbar-dropdown.component';

describe('SearchbarDropdownComponent', () => {
  let component: SearchbarDropdownComponent;
  let fixture: ComponentFixture<SearchbarDropdownComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      imports: [FormsModule],
      declarations: [ SearchbarDropdownComponent ],
      providers: [ DataCacheService, LoggerService, RefactorFieldsService, UtilsService ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(SearchbarDropdownComponent);
    component = fixture.componentInstance;
    component.dropDownSelectedValue = 'id1';
    component.dropdownData = [
      {
        id: 'id1',
        value: 'id1'
      }
    ]
    component.ngAfterViewInit();
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
