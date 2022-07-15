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


import { Pipe, PipeTransform, Output, EventEmitter, Directive } from '@angular/core';
import { LoggerService } from '../services/logger.service';

@Pipe({ name: 'search' })
export class SearchPipe implements PipeTransform {
    @Output() pipeError = new EventEmitter();

    constructor(private loggerService: LoggerService) { }

    transform(input: any, searchQuery: string): any {
        if (!input) return [];
        if (!searchQuery) return input;

        try {
            return input.filter(item => {
                return item.toLowerCase().includes(searchQuery.toLowerCase())
            });
        } catch (error) {
            this.loggerService.log('infor', 'error in pipe' + error);
        }
    }
}
