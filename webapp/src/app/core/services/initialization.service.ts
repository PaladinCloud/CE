import { APP_INITIALIZER, Provider } from '@angular/core';
import { Store } from '@ngxs/store';
import { ConfigActions } from '../store/states/config/config.actions';

export const INITIALIZATION: Provider = {
    provide: APP_INITIALIZER,
    useFactory: loadAppData,
    multi: true,
    deps: [Store],
};

function loadAppData(store: Store) {
    return () => {
        const config$ = store.dispatch(new ConfigActions.Get());
        return config$.toPromise();
    };
}
