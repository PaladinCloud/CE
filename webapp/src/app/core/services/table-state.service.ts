import { Injectable } from '@angular/core';
import { LoggerService } from 'src/app/shared/services/logger.service';

@Injectable()
export class TableStateService {
    stateLabel = "allTableStates";

    constructor(
        private logger: LoggerService
    ){}

    setState(componentKey, obj){
        try{
            const state = this.getStateFromLocalStorage();        
            state[componentKey] = obj;
            localStorage.setItem(this.stateLabel, JSON.stringify(state));
        }catch(e){
            this.logger.log(componentKey, ": Error in saving state: "+e);
        }
    }

    getState(componentKey){
        let componentState:any = {};
        try{
            const state = this.getStateFromLocalStorage();
            componentState = state[componentKey];    
        }catch(e){
            this.logger.log(componentKey, ": Error in getting state: "+e);
        }
        return componentState;
    }

    getStateFromLocalStorage(){
        const state = localStorage.getItem(this.stateLabel); 
        if(!(state && state!="undefined")){
            localStorage.setItem("allTableStates", JSON.stringify({}));
            return {};
        }
        return JSON.parse(state);
    }

    persistOnly(componentKey){
        let componentState = this.getState(componentKey);
        this.clearAll();
        this.setState(componentKey, componentState);
    }

    clearState(componentKey){
        try{
            const componentState = this.getState(componentKey);
            const keys = Object.keys(componentState);
            const columnsListKey = keys.find(key => key.includes("whiteList"));
            const filterListKey = keys.find(key => key.includes("filter"));
            componentState[filterListKey]?.forEach(filter => {
                filter.value = undefined;
                filter.filterValue = undefined;
            })
            const newState = {};
            newState[columnsListKey] = componentState[columnsListKey];
            newState[filterListKey] = componentState[filterListKey];
            this.setState(componentKey, newState);
        }catch(e){
            this.logger.log(componentKey, ": Error in clearing state: "+e);
        }
    }

    clearAll(){
        const state = this.getStateFromLocalStorage();
        if(state){
            const storedStatesKeys = Object.keys(state);
            storedStatesKeys.forEach(state => {
                this.clearState(state);
            })
        }
    }
}