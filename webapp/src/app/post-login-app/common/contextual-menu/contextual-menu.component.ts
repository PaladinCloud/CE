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

import {
  Component,
  Input,
  OnInit,
  OnChanges,
  SimpleChanges,
  SimpleChange,
  OnDestroy,
} from "@angular/core";
import { WorkflowService } from "../../../core/services/workflow.service";
import { ThemeObservableService } from "../../../core/services/theme-observable.service";
import { DomainTypeObservableService } from "../../../core/services/domain-type-observable.service";
import { AssetGroupObservableService } from "../../../core/services/asset-group-observable.service";
import { Subscription } from "rxjs";
import { LoggerService } from "../../../shared/services/logger.service";
import { DownloadService } from "../../../shared/services/download.service";
import { DataCacheService } from "../../../core/services/data-cache.service";
import { TreeNode } from "@circlon/angular-tree-component";
import { NavigationEnd, Router } from "@angular/router";
import { CommonResponseService } from "src/app/shared/services/common-response.service";
import { environment } from "src/environments/environment";
import * as _ from 'lodash';
import { AssetTypeMapService } from "src/app/core/services/asset-type-map.service";


@Component({
  selector: "app-contextual-menu",
  templateUrl: "./contextual-menu.component.html",
  styleUrls: ["./contextual-menu.component.css"],
})
export class ContextualMenuComponent implements OnInit, OnDestroy {
  currentParentId: number = 1;
  currentNodeId: number = 1;
  current_version: string = "";
  @Input() haveAdminPageAccess;
  @Input() expanded;
  nodes = [
    {
      id: "Dashboard",
      name: "Dashboard",
      image: "dashboard-icon",
      route: "/pl/compliance/compliance-dashboard",
    },
    {
      id: "Violations",
      name: "Violations",
      image: "violations-icon",
      route: "/pl/compliance/issue-listing"
    },
    {
      id: "Assets",
      name: "Assets",
      image: "assets-icon",
      children: [
        {
          id: "Summary",
          name: "Summary",
          parent: "Assets",
          route: "/pl/assets/asset-dashboard",
        },
        {
          "id": "Distribution",
          "name": "Distribution",
          "parent": "Assets",
          "route": "/pl/assets/asset-distribution"
        },
        {
          id: "List",
          name: "List",
          parent: "Assets",
          route: "/pl/assets/asset-list",
        }
      ],
    },
    {
      id: "Policy",
      name: "Policy",
      image: "policy-icon",
      route: "/pl/compliance/policy-knowledgebase",
    },
    {
      id: 7,
      name: "Tagging",
      image: "tagging-icon",
      route: "/pl/compliance/tagging-compliance",
    },
    {
      id: "Fix Central",
      name: "Fix Central",
      image: "fix-central-icon",
      children: [
        {
          id: "Health Notifications",
          name: "Health Notifications",
          parent: "Fix Central",
          route: "/pl/compliance/health-notifications",
          notDisplayIfAzure: true,
        },
        {
          id: "Recommendations",
          name: "Recommendations",
          parent: "Fix Central",
          route: "/pl/compliance/recommendations",
        },
        // { id: 11, name: "Fixes", parent: "Fix Central" },
      ],
    },
    {
      id: "Statistics",
      name: "Statistics",
      image: "statistics-icon",
      route: "stats-overlay",
      overlay: true,
    },
    {
      id: "Admin",
      name: "Admin",
      image: "admin-icon",
      children: [
        {
          id: "admin-policy",
          name: "Policy",
          parent: "Admin",
          route: "/pl/admin/policies",
          permissions: ["policy-management"]

        },
        {
          id: "Job Execution Manager",
          name: "Job Execution Manager",
          parent: "Admin",
          route: "/pl/admin/job-execution-manager",
          permissions: ["job-execution-management"]
        },
        {
          id: "Domains",
          name: "Domains",
          parent: "Admin",
          route: "/pl/admin/domains",
          permissions: ["domain-management"]
        },
        {
          id: "Asset Types",
          name: "Asset Types",
          parent: "Admin",
          route: "/pl/admin/target-types",
          permissions: ["target-type-management"]
        },
        {
          id: "Asset Groups",
          name: "Asset Groups",
          parent: "Admin",
          route: "/pl/admin/asset-groups",
          permissions: ["asset-group-management"]
        },
        {
          id: "Sticky Exceptions",
          name: "Sticky Exceptions",
          parent: "Admin",
          route: "/pl/admin/sticky-exceptions",
          permissions: ["exemption-management"]
        },
        {
          id: "Roles",
          name: "Roles",
          parent: "Admin",
          route: "/pl/admin/roles",
          permissions: ["user-management"]
        },
        {
          id: "User Management",
          name: "User Management",
          parent: "Admin",
          route: "/pl/admin/user-management",
          permissions: ["user-management"]
        },
        {
          id: "Account Management",
          name: "Account Management",
          parent: "Admin",
          route: "/pl/admin/account-management",
          permissions: ["account-management"]
        },
        {
          id: "Configuration Management",
          name: "Configuration Management",
          parent: "Admin",
          route: "/pl/admin/config-management",
          permissions: ["configuration-management"]
        },
        {
          id: "System Management",
          name: "System Management",
          parent: "Admin",
          route: "/pl/admin/system-management",
          permissions: ["system-management"]
        },
      ],
    },
  ];

  constructor(
    private workflowService: WorkflowService,
    private themeObservableService: ThemeObservableService,
    private assetGroupObservableService: AssetGroupObservableService,
    private domainObservableService: DomainTypeObservableService,
    private logger: LoggerService,
    private downloadService: DownloadService,
    private dataCacheService: DataCacheService,
    private router: Router,
    private commonResponseService: CommonResponseService,
    private assetTypeMapService:AssetTypeMapService
  ) {
    this.assetTypeMapService.fetchAssetTypes(); 
    this.router.events.subscribe((val) => {
      if (val instanceof NavigationEnd) {
        const currentRoute = this.router.url
          .split("?")[0]
          .replace("(", "")
          .replace(")", "");
        const currNodes = this.getCurrentNodesFromRoute(currentRoute);
        this.currentNodeId = currNodes[0];
        this.currentParentId = currNodes[1];
      }
    });

    const url = environment.getCurrentVersion.url;
    const urlMethod = environment.getCurrentVersion.method;
    const queryParam = {
      "cfkey": "current-release"
    }
    this.commonResponseService.getData(url, urlMethod, "", queryParam).subscribe(
      response => {
        this.current_version = response[0].value.substring(1);
      }
    )
  }

  private assetGroupSubscription: Subscription;
  private domainSubscription: Subscription;
  private downloadSubscription: Subscription;
  public agAndDomain = {};
  public theme: any;
  showContent = true;
  showPacLoader: any = [];
  provider = [];

  ngOnInit() {
    this.subscribeForThemeChange();
    this.subscribeToAgAndDomainChange();
    this.downloadSubscription = this.downloadService
      .getDownloadStatus()
      .subscribe((val) => {
        if (val) {
          this.showPacLoader.push("downloading");
        } else {
          this.showPacLoader.pop();
        }
      });
    const currentRoute = this.router.url
      .split("?")[0]
      .replace("(", "")
      .replace(")", "");

    const currNodes = this.getCurrentNodesFromRoute(currentRoute);
    this.currentNodeId = currNodes[0];
    this.currentParentId = currNodes[1];
  }

  hasRequiredPermission(node) {
    if (node.data.name === 'Admin') {
      const isAdmin = this.dataCacheService.isAdminCapability();
      return isAdmin;
    }
    const roleCapabilities = this.dataCacheService.getRoleCapabilities();
    if (node.data === undefined || node.data.permissions === undefined || node.data.permissions.length == 0) {
      //default permission, no specific role capability is required
      return true;
    } else {
      const isAccessible = node.data.permissions.some(role => roleCapabilities.includes(role));
      //console.log('Component is accessible?', isAccessible)
      return isAccessible;
    }
  }

  getCurrentNodesFromRoute(currentRoute) {
    let currNodeParentId, currNodeId;
    for (let parent of this.nodes) {
      currNodeParentId = parent.id;
      if (parent.children != undefined) {
        for (let child of parent.children) {
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
    return [1, 1];
  }

  selectNode(node: TreeNode, event: any) {
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
      let recentList = "";
      recentList = this.dataCacheService.getRecentlyViewedAssetGroups();
      if (recentList) {
        const currentAGDetails = JSON.parse(recentList).filter(
          (element) => element.ag === this.agAndDomain["ag"]
        );
        this.provider = this.fetchprovider(currentAGDetails);
      }
    } catch (error) {
      this.logger.log("error", error);
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

  subscribeForThemeChange() {
    this.themeObservableService.getTheme().subscribe((theme) => {
      this.theme = theme;
    });
  }

  subscribeToAgAndDomainChange() {
    this.assetGroupSubscription = this.assetGroupObservableService
      .getAssetGroup()
      .subscribe((assetGroup) => {
        this.agAndDomain["ag"] = assetGroup;
      });
    this.domainSubscription = this.domainObservableService
      .getDomainType()
      .subscribe((domain) => {
        this.agAndDomain["domain"] = domain;
        this.getProvider();
      });
  }

  ngOnDestroy() {
    try {
      this.assetGroupSubscription.unsubscribe();
      this.domainSubscription.unsubscribe();
    } catch (error) {
      this.logger.log("error", "js error - " + error);
    }
  }
}
