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

import { HttpClientModule } from '@angular/common/http';
import { NgModule } from '@angular/core';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { MatSelectModule } from '@angular/material/select';
import { BrowserModule } from '@angular/platform-browser';
import { BrowserAnimationsModule } from '@angular/platform-browser/animations';
import { RouterModule } from '@angular/router';

import { AppComponent } from './app.component';

/* Feature modules */
import { NgxGoogleAnalyticsModule, NgxGoogleAnalyticsRouterModule } from 'ngx-google-analytics';
import { CONFIGURATIONS } from 'src/config/configurations';
import { AppRoutingModule } from './app-routing.module';
import { CoreModule } from './core/core.module';
import { INITIALIZATION } from './core/services/initialization.service';
import { StoreModule } from './core/store/store.module';
import { LandingPageModule } from './landing-page/landing-page.module';
import { FetchResourcesService } from './pacman-features/services/fetch-resources.service';
import { PostLoginAppModule } from './post-login-app/post-login-app.module';
import { TokenResolverService } from './resolver/token-resolver.service';
import { ErrorComponent } from './error/error.component';

@NgModule({
    declarations: [AppComponent, ErrorComponent],
    imports: [
        BrowserModule,
        BrowserAnimationsModule,
        MatSelectModule,
        FormsModule,
        ReactiveFormsModule,
        HttpClientModule,
        RouterModule,
        AppRoutingModule,
        LandingPageModule,
        PostLoginAppModule,
        CoreModule,
        CONFIGURATIONS.optional.general.gaKey
            ? [
                  NgxGoogleAnalyticsModule.forRoot(CONFIGURATIONS.optional.general.gaKey),
                  NgxGoogleAnalyticsRouterModule,
              ]
            : [],
        StoreModule,
    ],
    providers: [INITIALIZATION, FetchResourcesService, TokenResolverService],
    bootstrap: [AppComponent],
})
export class AppModule {}
