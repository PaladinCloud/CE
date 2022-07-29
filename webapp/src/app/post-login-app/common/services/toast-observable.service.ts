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

/**
 * Created by adityaagarwal on 29/01/18.
 */

import { Injectable } from '@angular/core';
import { Observable ,  Subject } from 'rxjs';

@Injectable()

export class ToastObservableService {
    private subject = new Subject;

    postMessage (msg: String, duration?) {
        if (msg) {
            const obj = {
                'msg': msg,
                'duration': duration
            };
            this.subject.next(obj);
        }
    }

    getMessage(): Observable<any> {
        return this.subject.asObservable();
    }

}
