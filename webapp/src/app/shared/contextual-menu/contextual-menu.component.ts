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
import { WorkflowService } from "../../core/services/workflow.service";
import { ThemeObservableService } from "../../core/services/theme-observable.service";
import { DomainTypeObservableService } from "../../core/services/domain-type-observable.service";
import { AssetGroupObservableService } from "../../core/services/asset-group-observable.service";
import { Subscription } from "rxjs";
import { LoggerService } from "../services/logger.service";
import { DownloadService } from "../../shared/services/download.service";
import { DataCacheService } from "../../core/services/data-cache.service";
import { TreeNode } from "angular-tree-component";

@Component({
  selector: "app-contextual-menu",
  templateUrl: "./contextual-menu.component.html",
  styleUrls: ["./contextual-menu.component.css"],
  providers: [],
})
export class ContextualMenuComponent implements OnInit, OnChanges, OnDestroy {
  currentParentId: number = 1;
  currentNodeId: number = 1;
  nodes = [
    {
      id: 1,
      name: "Dashboard",
      image: "compliance",
      route: "/pl/compliance/compliance-dashboard",
    },
    {
      id: 2,
      name: "Violations",
      image: "assets",
      route: "/pl/compliance/issue-listing",
    },
    {
      id: 3,
      name: "Assets",
      image: "assets",
      children: [
        {
          id: 4,
          name: "Summary",
          parent: "Assets",
          route: "/pl/assets/asset-dashboard",
        },
        {
          id: 5,
          name: "Details",
          parent: "Assets",
          route: "/pl/assets/asset-list",
        },
      ],
    },
    {
      id: 6,
      name: "Policy",
      image: "admin",
      route: "/pl/compliance/policy-knowledgebase",
    },
    {
      id: 7,
      name: "Tagging",
      image: "admin",
      route: "/pl/compliance/tagging-compliance",
    },
    {
      id: 8,
      name: "Fix Central",
      image: "compliance",
      children: [
        {
          id: 9,
          name: "Health Notifications",
          parent: "Fix Central",
          route: "/pl/compliance/health-notifications",
        },
        {
          id: 10,
          name: "Recommendations",
          parent: "Fix Central",
          route: "/pl/compliance/recommendations",
        },
        // { id: 11, name: "Fixes", parent: "Fix Central" },
      ],
    },
    {
      id: 13,
      name: "Statistics",
      image: "admin",
      route:
        "modalBGMenu:stats-overlay?ag=all-clouds&domain=Infra %26 Platforms",
    },
    {
      id: 14,
      name: "Admin",
      image: "admin",
      route: "/pl/admin/policies",
      children: [
        {
          id: 15,
          name: "Policies",
          parent: "Admin",
          route: "/pl/admin/policies",
        },
        {
          id: 16,
          name: "Rules",
          parent: "Admin",
          route: "/pl/admin/rules",
        },
        {
          id: 17,
          name: "Job Execution Manager",
          parent: "Admin",
          route: "/pl/admin/job-execution-manager",
        },
        {
          id: 18,
          name: "Domains",
          parent: "Admin",
          route: "/pl/admin/domains",
        },
        {
          id: 19,
          name: "Target Types",
          parent: "Admin",
          route: "/pl/admin/target-types",
        },
        {
          id: 20,
          name: "Asset Groups",
          parent: "Admin",
          route: "/pl/admin/asset-groups",
        },
        {
          id: 21,
          name: "Sticky Exceptions",
          parent: "Admin",
          route: "/pl/admin/sticky-exceptions",
        },
        {
          id: 22,
          name: "Roles",
          parent: "Admin",
          route: "/pl/admin/roles",
        },
        {
          id: 23,
          name: "Configuration Management",
          parent: "Admin",
          route: "/pl/admin/config-management",
        },
        {
          id: 24,
          name: "System Management",
          parent: "Admin",
          route: "/pl/admin/system-management",
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
    private dataCacheService: DataCacheService
  ) {}

  @Input() pageTitle;
  @Input() listOfMenus;
  private assetGroupSubscription: Subscription;
  private domainSubscription: Subscription;
  private downloadSubscription: Subscription;
  public agAndDomain = {};
  public theme: any;
  showContent = true;
  selectedMenu;
  sortedMenu = [];
  modifiedList = [];
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
    this.processMenu();
  }

  ngOnChanges(changes: SimpleChanges) {
    for (const property in changes) {
      if (property === "listOfMenus") {
        this.processMenu();
      }
    }
  }

  handleClick(node: TreeNode) {
    node.toggleExpanded();
    this.currentNodeId = node.id;
    this.currentParentId = node.parent.id;
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

  onSelect(menu): void {
    this.selectedMenu = menu;
    /**
     * added by Trinanjan on 09/02/2018 for back button functionality
     * To clear page levels
     */
    this.workflowService.clearAllLevels();
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

  toggleSubContent() {
    this.showContent = !this.showContent;
  }

  sortListbyGroup(each_menu, callback) {
    if (each_menu.groupBy) {
      for (let j = 0; j < this.modifiedList.length; j++) {
        if (each_menu.groupBy === this.modifiedList[j].route) {
          if (
            this.modifiedList[j].subList &&
            this.modifiedList[j].subList.length > 0
          ) {
            let counter = 0;
            for (let k = 0; k < this.modifiedList[j].subList.length; k++) {
              if (this.modifiedList[j].subList[k].route !== each_menu.route) {
                counter++;
              } else {
                break;
              }
              if (counter === this.modifiedList[j].subList.length) {
                this.modifiedList[j].subList.push(each_menu);
                callback();
              }
            }
          } else {
            this.modifiedList[j].subList = [];
            this.modifiedList[j].subList.push(each_menu);
            callback();
          }
          break;
        }
      }
    } else {
      this.modifiedList.push(each_menu);
      callback();
    }
  }

  processMenu() {
    let menuCounter = 0;
    this.sortedMenu = [];
    this.modifiedList = [];
    this.listOfMenus.forEach((each_menu, index, array) => {
      each_menu.subList = [];
      menuCounter++;
      this.sortListbyGroup(each_menu, () => {
        if (menuCounter === array.length) {
          this.sortedMenu = this.modifiedList;
        }
      });
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
