import { Injectable } from '@angular/core';
import { Action, State, StateContext } from '@ngxs/store';
import { ConfigActions } from './config.actions';

export interface ConfigStateModel {}

@State<ConfigStateModel>({
    name: 'Config',
    defaults: {},
})
@Injectable()
export class ConfigState {
    @Action(ConfigActions.Get)
    get(ctx: StateContext<ConfigStateModel>) {
        console.debug('should obtain the config');
        return Promise.resolve(() => ctx.setState({}));
    }
}
