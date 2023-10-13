import { APP_INITIALIZER, Provider } from '@angular/core';

export const INITIALIZATION: Provider = {
    provide: APP_INITIALIZER,
    useValue: loadAppData,
    multi: true,
};

function loadAppData() {
    return () => {
        console.debug('load data here');
        return Promise.reject();
    };
}
