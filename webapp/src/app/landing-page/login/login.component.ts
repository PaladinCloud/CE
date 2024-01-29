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

import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';

import { CONFIGURATIONS } from 'src/config/configurations';

@Component({
    selector: 'app-login',
    templateUrl: './login.component.html',
})
export class LoginComponent implements OnInit {
    constructor(private router: Router) {}
    ngOnInit() {
        // Check if the configuration is valid; if not, navigate to the error page

        if (this._verifyConfiguration()) this._init();
        else this.router.navigate(['/error']);
    }

    private _verifyConfiguration() {
        if (!CONFIGURATIONS.optional.auth.AUTH_TYPE) {
            console.error('AUTH_TYPE is missing in configuration');
            return false;
        }
        if (!CONFIGURATIONS.optional.auth.cognitoConfig.loginURL) {
            console.error('Login URL is missing in configuration');
            return false;
        }
        return true;
    }
    private _init() {
        // Private method to initialize the application based on the configuration

        console.log('Auth type:', CONFIGURATIONS.optional.auth.AUTH_TYPE);
        window.location.assign(CONFIGURATIONS.optional.auth.cognitoConfig.loginURL);
    }
}
