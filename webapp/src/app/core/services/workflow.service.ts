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

/*** Created by puneetbaser on 29/01/18. */

import { Injectable } from '@angular/core';
import { ActivatedRouteSnapshot, Router } from '@angular/router';
import { LoggerService } from 'src/app/shared/services/logger.service';
import { RouterUtilityService } from 'src/app/shared/services/router-utility.service';
import { DataCacheService } from './data-cache.service';

interface Level {
    [key: string]: LevelParams[];
}

interface LevelParams {
    title: string | null;
    url: string;
    queryParams: { [key: string]: string };
}

@Injectable()
export class WorkflowService {
    private level: Level = {};
    /* This would track which page user has clicked in a module */
    private trackOpenedPageInAModule: { [key: string]: string } = {};

    constructor(
        private logger: LoggerService,
        private dataStore: DataCacheService,
        private routerUtilityService: RouterUtilityService,
        private router: Router,
    ) {}

    addRouterSnapshotToLevel(
        routerSnapshot: ActivatedRouteSnapshot,
        currentLevel = 0,
        title = null,
    ) {
        this.level = this.getDetailsFromStorage();

        if (!this.level['level' + currentLevel]) {
            this.level['level' + currentLevel] = [];
        }

        const currentLevelArray = this.level['level' + currentLevel];

        if (
            currentLevelArray.length > 0 &&
            currentLevelArray[currentLevelArray.length - 1].title === title
        ) {
            return;
        }

        const url = this.routerUtilityService.getFullUrlFromSnapshopt(routerSnapshot);
        const queryParams =
            this.routerUtilityService.getQueryParametersFromSnapshot(routerSnapshot);

        const obj = {
            title,
            url,
            queryParams,
        };

        this.level['level' + currentLevel].push(obj);
        this.saveToStorage(this.level);
    }

    goBackToLastOpenedPageAndUpdateLevel(
        currentRouterSnapshot: ActivatedRouteSnapshot,
        currentLevel = 0,
    ) {
        let levelParams: LevelParams;
        this.level = this.getDetailsFromStorage();
        while (!levelParams && currentLevel >= 0) {
            if (
                this.level['level' + currentLevel] &&
                this.level['level' + currentLevel].length > 0
            ) {
                levelParams = this.level['level' + currentLevel].pop();
                break;
            }
            currentLevel--;
        }
        this.saveToStorage(this.level); // <-- update session storage after poping each obj

        const currentPageQueryParams =
            this.routerUtilityService.getQueryParametersFromSnapshot(currentRouterSnapshot);

        levelParams.queryParams = {
            ...levelParams.queryParams,
            ...{
                ag: currentPageQueryParams['ag'],
                domain: currentPageQueryParams['domain'],
            },
        };

        this.router.navigate([levelParams.url], {
            queryParams: levelParams.queryParams,
        });
    }

    checkIfFlowExistsCurrently(currentLevel?: number) {
        let flowExiststatus = false;
        this.level = this.getDetailsFromStorage();
        while (currentLevel >= 0) {
            if (this.level['level' + currentLevel]) {
                flowExiststatus = this.level['level' + currentLevel].length > 0;
            }
            currentLevel--;
        }

        return flowExiststatus;
    }

    clearAllLevels() {
        this.level = {};
        this.saveToStorage(this.level);
    }

    clearSpecificLevel(levelToBeCleared: number) {
        this.level['level' + levelToBeCleared] = [];
    }

    /**
     * Added By Trinanjan
     * saveToStorage() saves the current url to seesion storage
     * getDetailsFromStorage() gets the previous page url
     * These 2 functions are added so that user can go back even if they reload the page
     */
    saveToStorage(level: Level) {
        try {
            const levelToBeStroed = JSON.stringify(level);
            this.dataStore.set('StoredLevel', levelToBeStroed);
        } catch (error) {
            this.logger.log('error', error);
        }
    }

    getDetailsFromStorage() {
        try {
            let levelToBeRetrived = this.dataStore.get('StoredLevel');
            if (levelToBeRetrived !== undefined) {
                levelToBeRetrived = JSON.parse(levelToBeRetrived);
            } else {
                levelToBeRetrived = {};
            }
            return levelToBeRetrived;
        } catch (error) {
            this.logger.log('error', error);
        }
    }

    /** Below functions are used to get previously opened page in a particular module **/

    addPageToModuleTracker(moduleName: string, pageUrl: string) {
        this.trackOpenedPageInAModule[moduleName] = pageUrl;
    }

    addQueryParamsToModuleTracker(moduleName: string, queryParam: string) {
        if (queryParam) {
            this.trackOpenedPageInAModule[moduleName + 'queryparams'] = queryParam;
        }
    }

    getPreviouslyOpenedPageInModule(moduleName: string) {
        return this.trackOpenedPageInAModule[moduleName];
    }

    getPreviouslyOpenedPageQueryParamsInModule(moduleName: string) {
        return this.trackOpenedPageInAModule[moduleName + 'queryparams'];
    }

    goToLevel(level: number) {
        const currentLevel = this.level['level0'].length;
        this.level['level0'].splice(level, currentLevel);
        this.saveToStorage(this.level);
    }

    clearDataOfOpenedPageInModule() {
        this.trackOpenedPageInAModule = {};
    }
}
