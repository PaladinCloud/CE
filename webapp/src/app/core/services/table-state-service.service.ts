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

import { Injectable } from '@angular/core';

@Injectable()
export class TableStateService {
    state: Object = {};

    setState(obj, componentKey){
        this.state[componentKey] = obj;        
    }

    getState(componentKey){
        return this.state?this.state[componentKey]:this.state;
    }

    persistOnly(componentKey){
        let componentState = this.getState(componentKey);
        this.clearAll();
        this.setState(componentState, componentKey);
    }

    clearState(componentKey){
        this.setState({}, componentKey);
    }

    clearAll(){
        this.state = {};
    }
}
