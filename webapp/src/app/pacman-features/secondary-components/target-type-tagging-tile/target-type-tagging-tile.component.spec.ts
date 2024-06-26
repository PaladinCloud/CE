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
import { LoggerService } from 'src/app/shared/services/logger.service';
import { RefactorFieldsService } from 'src/app/shared/services/refactor-fields.service';

import { TargetTypeTaggingTileComponent } from './target-type-tagging-tile.component';

describe('TargetTypeTaggingComponent', () => {
    let component: TargetTypeTaggingTileComponent;
    let fixture: ComponentFixture<TargetTypeTaggingTileComponent>;

    beforeEach(async(() => {
        TestBed.configureTestingModule({
            declarations: [TargetTypeTaggingTileComponent],
            providers: [LoggerService, RefactorFieldsService],
        }).compileComponents();
    }));

    beforeEach(() => {
        fixture = TestBed.createComponent(TargetTypeTaggingTileComponent);
        component = fixture.componentInstance;
        fixture.detectChanges();
    });

    it('should create', () => {
        expect(component).toBeTruthy();
    });
});
