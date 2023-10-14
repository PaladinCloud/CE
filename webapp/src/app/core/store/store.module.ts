import { NgModule } from '@angular/core';
import { NgxsReduxDevtoolsPluginModule } from '@ngxs/devtools-plugin';
import { NgxsLoggerPluginModule } from '@ngxs/logger-plugin';
import { NgxsModule } from '@ngxs/store';
import { environment } from 'src/environments/environment';
import { STATES } from './states';

const devStorePluginModules = environment.production
    ? []
    : [
          NgxsLoggerPluginModule.forRoot(),
          NgxsReduxDevtoolsPluginModule.forRoot({
              name: 'PaladinCloud',
          }),
      ];

@NgModule({
    imports: [
        NgxsModule.forRoot(STATES, {
            developmentMode: !environment.production,
        }),
        devStorePluginModules,
    ],
})
export class StoreModule {}
