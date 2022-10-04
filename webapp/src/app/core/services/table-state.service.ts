import { Injectable } from '@angular/core';

@Injectable()
export class TableStateService {
    state: Object = {};

    setState(componentKey, obj){
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
        this.setState(componentKey, {});
    }

    clearAll(){
        this.state = {};
    }
}