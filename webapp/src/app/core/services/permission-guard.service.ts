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

/* Created by Puneet Baser 20/11/2017 */

import { Injectable } from '@angular/core';
import { Router, CanActivate, ActivatedRouteSnapshot, RouterStateSnapshot } from '@angular/router';
import { DataCacheService } from './data-cache.service';

@Injectable()
export class PermissionGuardService implements CanActivate {
    constructor(private dataCacheService: DataCacheService,
        private router: Router) { }

    canActivate(route: ActivatedRouteSnapshot, state: RouterStateSnapshot) {

        // this will be passed from the route config
        const urlPermissions = route.data.capabilities;
        const userRoles = this.dataCacheService.getUserDetailsValue().getRoles();
        const userPermissions = this.dataCacheService.getRoleCapabilities();
        console.log("User permissions: ", userPermissions);
        console.log("urlPermissions", urlPermissions);
        const canUserAccess = this.checkUserPermissionToAccessThisUrl(urlPermissions, userPermissions);
        //window.alert("canUserAccess" + canUserAccess);
        if (!canUserAccess) {
            this.router.navigate(['/home']);
            return false;
        }
        const redirectUrl = location.origin + state.url;
        if(!redirectUrl.includes("home"))
        localStorage.setItem("redirectUrl",redirectUrl);
        return true;
    }

    checkUserPermissionToAccessThisUrl(urlPermissions, userRoles) {
        if ((urlPermissions === 'undefined' || urlPermissions.length == 0)) {
            //default permission, no specific role capability is required
            return true;
        } else {
            return urlPermissions.some(role => userRoles.includes(role));
        }
    }

    checkAdminPermission() {
        return this.dataCacheService.isAdminCapability();
    }

    checkOnPremAdminPermission() {
        const userDetailsRoles = this.dataCacheService.getUserDetailsValue().getRoles();
        const adminAccess = userDetailsRoles.includes('ROLE_ONPREM_ADMIN');
        return adminAccess;
    }
}
