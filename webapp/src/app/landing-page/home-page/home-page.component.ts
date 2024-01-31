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
import { DataCacheService } from '../../core/services/data-cache.service';
import { LoggerService } from '../../shared/services/logger.service';
import { AuthService } from '../../core/services/auth.service';
import { CONFIGURATIONS } from '../../../config/configurations';
import { Router } from '@angular/router';

@Component({
    selector: 'app-home-page',
    templateUrl: './home-page.component.html',
    styleUrls: ['./home-page.component.css'],
})
export class HomePageComponent implements OnInit {
    public roleAndDefaultAssetGroupLoaded = true;

    constructor(
        private router: Router,
        private dataStore: DataCacheService,
        private logger: LoggerService,
        private authService: AuthService,
    ) {}

    ngOnInit() {
        try {
            /* Will not redirect user to post login automatically. User will be redirected when he clicks on Go to dashboard */
            this.redirectLoggedinUser();
        } catch (error) {
            this.logger.log('error', error);
        }
    }

    redirectLoggedinUser() {
        this.logger.log('info', '**Redirection triggered from Home page**');
        this.logger.log('info', '**Home page redirection - To check if user is authenticated**');
        if (this.authService.authenticated) {
            this.logger.log(
                'info',
                '**Home page redirection - User is authenticated to proceed with post login section**',
            );
            // If user is already logged in
            const userDefaultAssetGroup = this.dataStore.getUserDefaultAssetGroup();

            if (CONFIGURATIONS.optional.auth.AUTH_TYPE === 'cognito') {
                this.logger.log(
                    'info',
                    '**Fetching users default asset group and roles from cognito**',
                );
                // Get information when default asset group is not set
                this.roleAndDefaultAssetGroupLoaded = false;

                console.log(
                    '*****roleAndDefaultAssetGroupLoaded****: ',
                    this.roleAndDefaultAssetGroupLoaded,
                );
                this.authService.setUserFetchedInformationCognito().subscribe(
                    (response) => {
                        this.logger.log('info', '**Successfully set user Fetched information**');
                        this.authService.redirectPostLogin(
                            this.dataStore.getUserDefaultAssetGroup(),
                        );
                    },
                    (error) => {
                        this.logger.log('info', '**Error in setting user Fetched information**');
                        this.authService.redirectPostLogin(
                            this.dataStore.getUserDefaultAssetGroup(),
                        );
                    },
                );
            } else {
                // Redirect when default asset group is already set
                this.authService.redirectPostLogin(userDefaultAssetGroup);
            }
        } else {
            this.logger.log(
                'info',
                '**Home page redirection - user is not authenticated to move to post login**',
            );
            this.router.navigate(['/home/login']);
        }
    }
}
