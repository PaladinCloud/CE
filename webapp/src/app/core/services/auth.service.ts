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


import { throwError as observableThrowError, Observable, Observer } from 'rxjs';
import { Injectable } from '@angular/core';
import { HttpHeaders } from '@angular/common/http';
import { AdalService } from './adal.service';
import { CONFIGURATIONS } from './../../../config/configurations';
import { OnPremAuthenticationService } from './onprem-authentication.service';
import { Router, UrlTree } from '@angular/router';
import { AssetGroupObservableService } from './asset-group-observable.service';
import { LoggerService } from './../../shared/services/logger.service';
import { HttpService } from '../../shared/services/http-response.service';
import { DataCacheService } from './data-cache.service';
import { UtilsService } from '../../shared/services/utils.service';
import { environment } from '../../../environments/environment';
import { CommonResponseService } from '../../shared/services/common-response.service';
import { catchError, map } from 'rxjs/operators';
import { TokenResolverService } from 'src/app/resolver/token-resolver.service';
import { AwsCognitoService } from './aws-cognito.service';
import { AuthSessionStorageService } from './auth-session-storage.service';

@Injectable()
export class AuthService {

    private adAuthentication: boolean;
    private cognitoAuthentication: boolean;

    constructor(private adalService: AdalService,
        private onPremAuthentication: OnPremAuthenticationService,
        private awsCognitoService: AwsCognitoService,
        private tokenResolver: TokenResolverService,
        private router: Router,
        private assetGroupObservableService: AssetGroupObservableService,
        private loggerService: LoggerService,
        private httpService: HttpService,
        private dataStore: DataCacheService,
        private utilService: UtilsService,
        private commonResponseService: CommonResponseService,
        private logger: LoggerService,
        private authSessionStorage: AuthSessionStorageService) {

        this.adAuthentication = CONFIGURATIONS.optional.auth.AUTH_TYPE === 'azuresso';
        this.cognitoAuthentication = CONFIGURATIONS.optional.auth.AUTH_TYPE === 'cognito';
    }

    /*
    desc: This initiates the login process based on configuration
    */
    doLogin() {
        if (this.authenticated) {
            const userDefaultAssetGroup = this.dataStore.getUserDefaultAssetGroup();
            this.redirectPostLogin(userDefaultAssetGroup);
        } else {
            const loginUrl = '/home/login';

            this.router.navigateByUrl(loginUrl).then(result => {
                this.loggerService.log('info', 'Redirected to login page successfully - ' + result);
            },
                error => {
                    this.loggerService.log('error', 'Error navigating to login - ' + error);
                });
        }
    }

    doLogout() {
        console.log('Logout called')
        if (this.adAuthentication) {
            this.clearSessionStorage();
            this.adalService.logout();
        } else {

            this.clearSessionStorage();
            this.onPremAuthentication.logout();
            //  this.awsCognitoService.logoutUserFromCognito();

        }
    }

    clearSessionStorage() {
        this.dataStore.clearAll(); // Calling clear session from data store
        localStorage.setItem('logout', 'true');
        localStorage.removeItem('logout');
    }

    authenticateUserOnPrem(url: string, method: string, payload: { clientId?: string; username?: any; password?: string; }, headers: {}) {

        return this.httpService.getHttpResponse(url, method, payload, {}, headers)
            .pipe(map(response => {
                return response;
            }))
        // .pipe(catchError(error => {
        //     return observableThrowError(error.message || error);
        // }));
    }

    refreshToken() {
        // Write API code to refresh token
        try {

            const tokenObj = this.dataStore.getUserDetailsValue().getAuthToken();
            if (!tokenObj || !tokenObj.refresh_token) {
                return null;
            }

            return new Observable((observer: Observer<string>) => {
                const refreshToken = tokenObj.refresh_token;
                const url = environment.refresh.url;
                const method = environment.refresh.method;

                const payload = {
                    refreshToken: refreshToken
                };

                let userLoginDetails = JSON.parse(this.dataStore.getCurrentUserLoginDetails());
                this.commonResponseService.getData(url, method, payload, {}).subscribe(response => {
                    if (response && response.success && response.access_token) {
                        // Successful response
                        /* Response will have user info and access tokens. */
                        userLoginDetails = response;
                        this.dataStore.setCurrentUserLoginDetails(JSON.stringify(userLoginDetails));
                        observer.next(userLoginDetails.access_token);
                        observer.complete();
                    } else {
                        const errorMessage = response.message || 'Error renewing the access token';
                        this.logger.log('error ', errorMessage);
                        observer.error(null);
                    }
                },
                    error => {
                        this.logger.log('info', '**Error renewing the access token**');
                        observer.error(null);
                    });
            });
        } catch (error) {
            this.logger.log('error', 'JS Error - ' + error);
        }
    }

    getAuthToken() {
        /* Get the custom access token retuned from API */
        // Get the token object from data store and return access token

        let accessToken: any;

        const tokenObject = this.dataStore.getUserDetailsValue().getAuthToken();
        accessToken = tokenObject.access_token || null;
        return accessToken;
    }

    redirectPostLogin(defaultAssetGroup?: string) {
        const redirectUrl = this.redirectUrl;

        if (redirectUrl && redirectUrl !== '') {
            const redirect = this.utilService.getContextUrlExceptDomain(redirectUrl);

            if (redirect && this.redirectIsNotHomePage(redirect)) {
                this.router.navigateByUrl(redirect).then(result => {
                    this.loggerService.log('info', 'returnUrl navigated successfully');
                },
                    error => {
                        this.loggerService.log('error', 'returnUrl - error in navigation - ' + error);
                    });
            } else {
                this.redirectToPostLoginDefault(defaultAssetGroup);
            }
        } else {
            this.redirectToPostLoginDefault(defaultAssetGroup);
        }
    }

    private redirectToPostLoginDefault(defaultAssetGroup: string) {
        console.log('redirectToPostLoginDefault with ag', defaultAssetGroup)
        let url: string | UrlTree;
        if (!defaultAssetGroup || defaultAssetGroup === '') {
            url = '/pl/first-time-user-journey';
        } else {
            this.assetGroupObservableService.updateAssetGroup(defaultAssetGroup);
            url = '/pl/compliance/compliance-dashboard?ag=' + defaultAssetGroup;
        }
        this.router.navigateByUrl(url).then(result => {
            if (result) {
                this.loggerService.log('info', 'Successful navigation to ' + url);
            } else {
                this.loggerService.log('info', 'You are not authorised to access ' + url);
            }
        },
            error => {
                this.loggerService.log('error', 'Error while navigating - ' + error);
            });
    }

    get authenticated(): boolean {
        let authenticationStatus: boolean;
        let authStatus: any;
        let currentUserDetails: any;
        // If adAuthentication is enabled for this app.
        if (this.adAuthentication) {
            authenticationStatus = this.adalService.userInfo ? this.adalService.userInfo.authenticated : false;

        } else {
            authenticationStatus = this.onPremAuthentication.isAuthenticated();
        }
        return authenticationStatus;
    }

    get redirectUrl(): string {
        let redirectUrl = '';

        redirectUrl = this.dataStore.getRedirectUrl() || redirectUrl;
        return redirectUrl;
    }

    redirectIsNotHomePage(redirect: string) {
        return redirect !== '/home' && redirect !== '/home/login';
    }

    /* User informatin like user roles, user id */
    setUserFetchedInformation() {
        try {

            const idToken = this.adalService.getIdToken();
            const authToken = idToken;

            return new Observable(observer => {
                const url = environment.azureAuthorize.url;
                const method = environment.azureAuthorize.method;

                let headers: HttpHeaders = new HttpHeaders();
                headers = headers.set('Content-Type', 'application/json');
                headers = headers.set('Authorization', 'Bearer ' + authToken);

                const httpOptions = {
                    headers: headers
                };

                let userLoginDetails = JSON.parse(this.dataStore.getCurrentUserLoginDetails());
                this.commonResponseService.getData(url, method, {}, {}, httpOptions).subscribe(response => {
                    if (response && response.success) {
                        // Successful response
                        /* Response will have user info and access tokens. */
                        userLoginDetails = response;
                        this.dataStore.setCurrentUserLoginDetails(JSON.stringify(userLoginDetails));
                        this.dataStore.setUserDefaultAssetGroup(userLoginDetails.userInfo.defaultAssetGroup);

                        observer.next('success');
                        observer.complete();
                    } else {
                        const errorMessage = response.message || 'Error authenticating the id_token';
                        this.logger.log('error ', errorMessage);
                        userLoginDetails.userInfo.defaultAssetGroup = 'aws-all';
                        this.dataStore.setCurrentUserLoginDetails(JSON.stringify(userLoginDetails));
                        this.dataStore.setUserDefaultAssetGroup(userLoginDetails.userInfo.defaultAssetGroup);
                        observer.error(errorMessage);
                    }
                },
                    error => {
                        this.logger.log('info', '**Error fetching the user roles from backend**');
                        userLoginDetails.userInfo.defaultAssetGroup = 'aws-all';

                        this.dataStore.setCurrentUserLoginDetails(JSON.stringify(userLoginDetails));
                        this.dataStore.setUserDefaultAssetGroup(userLoginDetails.userInfo.defaultAssetGroup);
                        observer.error('error');
                    });
            });
        } catch (error) {
            this.logger.log('error', 'JS Error - ' + error);
        }
    }

    setUserFetchedInformationCognito() {
        try {

            const authToken = this.getIdToken();
            let userLoginDetails = JSON.parse(this.dataStore.getCurrentUserLoginDetails());

            return new Observable(observer => {
                const url = environment.azureAuthorize.url;
                const method = environment.azureAuthorize.method;

                let headers: HttpHeaders = new HttpHeaders();
                headers = headers.set('Content-Type', 'application/json');
                headers = headers.set('Authorization', 'Bearer ' + authToken);

                const httpOptions = {
                    headers: headers
                };

                let userLoginDetails = JSON.parse(this.dataStore.getCurrentUserLoginDetails());
                this.commonResponseService.getData(url, method, {}, {}, httpOptions).subscribe(response => {
                    if (response && response.success) {
                        // Successful response
                        /* Response will have user info and access tokens. */

                        console.log("Response from authorize API", response);

                        //userLoginDetails.userInfo = response;
                        Object.assign(userLoginDetails, response);

                        console.log("UserLogin Details", userLoginDetails);
                        this.dataStore.setCurrentUserLoginDetails(JSON.stringify(userLoginDetails));
                        this.dataStore.setUserDefaultAssetGroup(userLoginDetails.userInfo.defaultAssetGroup);

                        console.log("Calling role mapping API");

                        this.fetchRolePermissionMapping().subscribe(result => {
                            this.logger.log('info', '**Successfully set user role capabilities mapping Fetched information**');

                        },
                            error => {
                                this.logger.log('info', '**Error in setting user role capabilities information**');
                            });
                        observer.next('success');
                        observer.complete();
                    } else {
                        const errorMessage = response.message || 'Error authenticating the id_token';
                        this.logger.log('error ', errorMessage);
                        userLoginDetails.userInfo.defaultAssetGroup = 'aws-all';
                        this.dataStore.setCurrentUserLoginDetails(JSON.stringify(userLoginDetails));
                        this.dataStore.setUserDefaultAssetGroup(userLoginDetails.userInfo.defaultAssetGroup);
                        observer.error(errorMessage);
                    }
                },
                    error => {
                        this.logger.log('info', '**Error fetching the user roles from backend**');
                        userLoginDetails.userInfo.defaultAssetGroup = 'aws-all';

                        this.dataStore.setCurrentUserLoginDetails(JSON.stringify(userLoginDetails));
                        this.dataStore.setUserDefaultAssetGroup(userLoginDetails.userInfo.defaultAssetGroup);
                        observer.error('error');
                    });
            });
        } catch (error) {
            this.logger.log('error', 'JS Error - ' + error);
        }
    }

    getIdToken() {
        let accessToken: any;
        const tokenObject = this.dataStore.getUserDetailsValue().getAuthToken();
        accessToken = tokenObject.id_token || null;
        return accessToken;
    }

    parseJwt(token: any) {
        console.log("Parsing the id token: ", token);
        var base64Url = token.split('.')[1];
        var base64 = base64Url.replace(/-/g, '+').replace(/_/g, '/');
        var jsonPayload = decodeURIComponent(window.atob(base64).split('').map(function (c) {
            return '%' + ('00' + c.charCodeAt(0).toString(16)).slice(-2);
        }).join(''));
        console.log("Parsed json payload ", jsonPayload);

        return JSON.parse(jsonPayload);
    }
    fetchRolePermissionMapping() {
        try {
            console.log('Inside fetchRolePermissionMapping method');
            let accessToken: any;
            const tokenObject = this.dataStore.getUserDetailsValue().getAuthToken();
            accessToken = tokenObject.access_token || null;
            let userLoginDetails = JSON.parse(this.dataStore.getCurrentUserLoginDetails());
            const roles = userLoginDetails.userInfo.userRoles.join(',');
            const queryParams = { "roles": roles };

            return new Observable(observer => {
                const url = environment.roleMappingUrl.url;
                const method = environment.roleMappingUrl.method;

                let headers: HttpHeaders = new HttpHeaders();
                headers = headers.set('Content-Type', 'application/json');
                headers = headers.set('Authorization', 'Bearer ' + accessToken);

                const httpOptions = {
                    headers: headers
                };
                this.commonResponseService.getData(url, method, {}, queryParams, httpOptions).subscribe(response => {
                    console.log('role mapping api response', response);
                    if (response && response.status === 'SUCCESS') {

                        let capabilities = [];
                        console.log("Role mapping details", response.roleMappings);
                        response.roleMappings.forEach(role => {
                            capabilities.push(role.permissions.map(p => p.permissionName));
                        });

                        console.log("Capabilities from API: ", capabilities);
                        this.dataStore.setRoleCapabilities(capabilities);
                        let isAdmin = this.checkIfAdminCapability(response);
                        console.log("Is user Capability admin: ", isAdmin);
                        this.dataStore.setAdminCapability(isAdmin);
                        console.log("User Capabilities: ", this.dataStore.getRoleCapabilities());
                        observer.next('success');
                        observer.complete();
                    } else {
                        const errorMessage = response.message || 'Error fetching role mapping';
                        this.logger.log('error ', errorMessage);
                        observer.error(errorMessage);
                    }
                },
                    error => {
                        this.logger.log('info', '**Error fetching the user roles from backend**');
                        observer.error('error');
                    });
            });
        } catch (error) {
            this.logger.log('error', 'JS Error - ' + error);
        }
    }

    checkIfAdminCapability(response) {
        let isAdmin = false;
        response.roleMappings.forEach(mapping => {
            mapping.permissions.forEach(item => {
                if (item.adminCapability) {
                    isAdmin = true;
                    return isAdmin;
                }
            });
        });
        return isAdmin;
    }
}
