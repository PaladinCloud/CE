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

import { BrowserModule } from '@angular/platform-browser';
import { NgModule } from '@angular/core';
import { RouterModule } from '@angular/router';
import { BrowserAnimationsModule } from '@angular/platform-browser/animations';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { MatSelectModule } from '@angular/material/select';
import { HttpClientModule } from '@angular/common/http';

import { AppComponent } from './app.component';

/* Feature modules */
import { LandingPageModule } from './landing-page/landing-page.module';
import { PostLoginAppModule } from './post-login-app/post-login-app.module';
import { AppRoutingModule } from './app-routing.module';
import { CoreModule } from './core/core.module';
import { FetchResourcesService } from './pacman-features/services/fetch-resources.service';
import { TokenResolverService } from './resolver/token-resolver.service';
import { NgxGoogleAnalyticsModule, NgxGoogleAnalyticsRouterModule } from 'ngx-google-analytics';
import { CONFIGURATIONS } from "src/config/configurations";

@NgModule({
  declarations: [AppComponent],
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
    CONFIGURATIONS.optional.general.gaKey ? [
      NgxGoogleAnalyticsModule.forRoot(CONFIGURATIONS.optional.general.gaKey),
      NgxGoogleAnalyticsRouterModule,
    ] : [],
  ],
  providers: [FetchResourcesService, TokenResolverService],
  bootstrap: [AppComponent],
})
export class AppModule {}
