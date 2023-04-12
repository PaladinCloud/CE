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

import { ArrayDataSource } from '@angular/cdk/collections';
import { NestedTreeControl } from '@angular/cdk/tree';
import { AfterViewInit, Component, Input, OnDestroy, OnInit, ViewChild } from '@angular/core';
import { NavigationEnd, Router } from '@angular/router';
import { TreeComponent, TreeNode } from '@circlon/angular-tree-component';
import { Subscription } from 'rxjs';
import { AssetGroupObservableService } from 'src/app/core/services/asset-group-observable.service';
import { AssetTypeMapService } from 'src/app/core/services/asset-type-map.service';
import { DataCacheService } from 'src/app/core/services/data-cache.service';
import { DomainTypeObservableService } from 'src/app/core/services/domain-type-observable.service';
import { ThemeObservableService } from 'src/app/core/services/theme-observable.service';
import { WorkflowService } from 'src/app/core/services/workflow.service';
import { CommonResponseService } from 'src/app/shared/services/common-response.service';
import { DownloadService } from 'src/app/shared/services/download.service';
import { LoggerService } from 'src/app/shared/services/logger.service';
import { environment } from 'src/environments/environment';
import { MenuItem, MenuItemChild, MENU_NODES } from './contextual-menu-nodes';

@Component({
    selector: 'app-contextual-menu',
    templateUrl: './contextual-menu.component.html',
    styleUrls: ['./contextual-menu.component.css'],
})
export class ContextualMenuComponent implements OnInit, AfterViewInit, OnDestroy {
    @Input() haveAdminPageAccess: boolean;
    @Input() expanded: boolean;
    @ViewChild('treeMenu') tree: TreeComponent;
    treeControl = new NestedTreeControl<MenuItem>((node) => node.children);
    dataSource = new ArrayDataSource(MENU_NODES);

    currentParentId = '';
    currentNodeId = '';
    current_version = '';
    agAndDomain = {};
    theme: string;
    showContent = true;
    showPacLoader: string[] = [];
    provider = [];

    nodes = MENU_NODES;

    private assetGroupSubscription: Subscription;
    private domainSubscription: Subscription;
    private downloadSubscription: Subscription;

    constructor(
        private assetGroupObservableService: AssetGroupObservableService,
        private assetTypeMapService: AssetTypeMapService,
        private commonResponseService: CommonResponseService,
        private dataCacheService: DataCacheService,
        private domainObservableService: DomainTypeObservableService,
        private downloadService: DownloadService,
        private logger: LoggerService,
        private router: Router,
        private themeObservableService: ThemeObservableService,
        private workflowService: WorkflowService,
    ) {
        this.assetTypeMapService.fetchAssetTypes();
        this.router.events.subscribe((val) => {
            if (val instanceof NavigationEnd) {
                const currentRoute = this.router.url
                    .split('?')[0]
                    .replace('(', '')
                    .replace(')', '');
                const currNodes = this.getCurrentNodesFromRoute(currentRoute);
                this.currentNodeId = currNodes[0];
                this.currentParentId = currNodes[1];
                this.selectCurrentNode();
            }
        });

        const url = environment.getCurrentVersion.url;
        const urlMethod = environment.getCurrentVersion.method;
        const queryParam = {
            cfkey: 'current-release',
        };
        this.commonResponseService.getData(url, urlMethod, '', queryParam).subscribe((response) => {
            this.current_version = response[0].value.substring(1);
        });
    }

    ngOnInit() {
        this.subscribeForThemeChange();
        this.subscribeToAgAndDomainChange();
        this.downloadSubscription = this.downloadService.getDownloadStatus().subscribe((val) => {
            if (val) {
                this.showPacLoader.push('downloading');
            } else {
                this.showPacLoader.pop();
            }
        });
        const currentRoute = this.router.url.split('?')[0].replace('(', '').replace(')', '');

        const currNodes = this.getCurrentNodesFromRoute(currentRoute);
        this.currentNodeId = currNodes[0];
        this.currentParentId = currNodes[1];
    }

    ngAfterViewInit() {
        this.selectCurrentNode();
        console.log(this.treeControl.dataNodes, this.treeControl.getDescendants(this.nodes[2]));
    }

    private selectCurrentNode() {
        const node: TreeNode | null = this.tree?.treeModel.getNodeById(this.currentNodeId);
        if (node?.parent?.data?.name) {
            node.parent.expand();
        }
    }

    hasRequiredPermission(node: TreeNode) {
        if (node.data.name === 'Admin') {
            const isAdmin = this.dataCacheService.isAdminCapability();
            return isAdmin;
        }
        const roleCapabilities = this.dataCacheService.getRoleCapabilities();
        if (
            node.data === undefined ||
            node.data.permissions === undefined ||
            node.data.permissions.length == 0
        ) {
            //default permission, no specific role capability is required
            return true;
        } else {
            const isAccessible = node.data.permissions.some((role) =>
                roleCapabilities.includes(role),
            );
            //console.log('Component is accessible?', isAccessible)
            return isAccessible;
        }
    }

    getCurrentNodesFromRoute(currentRoute: string) {
        let currNodeParentId: string;
        let currNodeId: string;

        for (const parent of this.nodes) {
            currNodeParentId = parent.id;
            if (parent.children != undefined) {
                for (const child of parent.children) {
                    if (currentRoute.includes(child.route)) {
                        currNodeId = child.id;
                        return [currNodeId, currNodeParentId];
                    }
                }
            } else if (currentRoute.includes(parent.route)) {
                currNodeId = parent.id;
                currNodeParentId = parent.id;
                return [currNodeId, currNodeParentId];
            }
        }
        return ['', ''];
    }

    selectNode(node: TreeNode, event: Event) {
        this.workflowService.clearAllLevels();
        node.toggleExpanded();
        this.currentNodeId = node.id;
        this.currentParentId = node.parent.id;
        if (node.hasChildren) {
            this.currentParentId = node.id;
        }
        node.mouseAction('click', event);
    }

    getProvider() {
        /* Store the recently viewed asset list in stringify format */
        try {
            let recentList = '';
            recentList = this.dataCacheService.getRecentlyViewedAssetGroups();
            if (recentList) {
                const currentAGDetails = JSON.parse(recentList).filter(
                    (element) => element.ag === this.agAndDomain['ag'],
                );
                this.provider = this.fetchprovider(currentAGDetails);
            }
        } catch (error) {
            this.logger.log('error', error);
        }
    }

    fetchprovider(assetGroupObject) {
        const provider = [];
        if (assetGroupObject.length && assetGroupObject[0].providers) {
            assetGroupObject[0].providers.forEach((element) => {
                provider.push(element.provider);
            });
        }
        return provider;
    }

    hasChild(_: number, node: MenuItem) {
        return node.children?.length > 0;
    }

    isChild(_: number, node: MenuItemChild) {
        return !!node.parent;
    }

    subscribeForThemeChange() {
        this.themeObservableService.getTheme().subscribe((theme) => {
            this.theme = theme;
        });
    }

    subscribeToAgAndDomainChange() {
        this.assetGroupSubscription = this.assetGroupObservableService
            .getAssetGroup()
            .subscribe((assetGroup) => {
                this.agAndDomain['ag'] = assetGroup;
            });
        this.domainSubscription = this.domainObservableService
            .getDomainType()
            .subscribe((domain) => {
                this.agAndDomain['domain'] = domain;
                this.getProvider();
            });
    }

    ngOnDestroy() {
        try {
            this.assetGroupSubscription.unsubscribe();
            this.domainSubscription.unsubscribe();
            this.downloadSubscription.unsubscribe();
        } catch (error) {
            this.logger.log('error', 'js error - ' + error);
        }
    }
}
