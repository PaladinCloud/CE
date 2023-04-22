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

import { Component, OnDestroy, OnInit } from "@angular/core";
import { ActivatedRoute, Router } from "@angular/router";
import { AssetGroupObservableService } from "../core/services/asset-group-observable.service";
import { Subscription } from "rxjs";
import { LoggerService } from "../shared/services/logger.service";
import { DataCacheService } from "../core/services/data-cache.service";
import { DownloadService } from "../shared/services/download.service";
import { DomainTypeObservableService } from "../core/services/domain-type-observable.service";
import { ThemeObservableService } from "../core/services/theme-observable.service";
import { WorkflowService } from "../core/services/workflow.service";
import { PermissionGuardService } from "../core/services/permission-guard.service";
import { WindowExpansionService } from "../core/services/window-expansion.service";
import { SnackbarComponent } from "../shared/components/molecules/snackbar/snackbar.component";
import { NotificationObservableService } from "../shared/services/notification-observable.service";
import { MatSnackBar } from "@angular/material/snack-bar";

declare var Offline: any;

@Component({
  selector: "app-post-login-app",
  templateUrl: "./post-login-app.component.html",
  styleUrls: ["./post-login-app.component.css"],
})
export class PostLoginAppComponent implements OnInit, OnDestroy {
  navigationDetails: any;
  domainList: string;
  queryParameters: any = {};
  private agAndDomainKey: string;
  showPacLoader: any = [];
  public haveAdminPageAccess = false;

  private notificationObservableServiceSubscription: Subscription;
  private themeSubscription: Subscription;
  private activatedRouteSubscription: Subscription;
  private downloadSubscription: Subscription;
  public theme;
  private pageReloadInterval; // Default time is 30 minutes in miliseconds
  private reloadTimeout;
  isOffline = false;
  isExpanded = true;
  mode = 'side';
  sidenavExpanderLeft = 250;
  rotationVar = 'rotate(180deg)';

  sidenavExpanderClicked() {
    this.isExpanded = !this.isExpanded;
    if (this.isExpanded) {
      this.sidenavExpanderLeft = 250;
      this.rotationVar = 'rotate(180deg)';
    }
    else {
      this.sidenavExpanderLeft = 70;
      this.rotationVar = 'rotate(0)';
    }
    this.windowExpansionService.setExpansionStatus(this.isExpanded);
  }

  mouseenter() {
    if (!this.isExpanded) {
      this.isExpanded = true;
      this.sidenavExpanderLeft = 250;
      this.rotationVar = 'rotate(180deg)';
      this.mode = "over";
      this.windowExpansionService.setExpansionStatus(this.isExpanded);
    }
  }

  mouseleave() {
    if (this.mode == "over") {
      this.isExpanded = false;
      this.sidenavExpanderLeft = 70;
      this.rotationVar = 'rotate(0)';
      this.mode = "side";
      this.windowExpansionService.setExpansionStatus(this.isExpanded);
    }
  }

  constructor(
    private permissions: PermissionGuardService,
    private router: Router,
    private activatedRoute: ActivatedRoute,
    private assetGroupObservableService: AssetGroupObservableService,
    private dataStore: DataCacheService,
    private logger: LoggerService,
    private workflowService: WorkflowService,
    private downloadService: DownloadService,
    private domainTypeObservableService: DomainTypeObservableService,
    private themeObservableService: ThemeObservableService,
    private windowExpansionService: WindowExpansionService,
    private notificationObservableService: NotificationObservableService,
    private snackBar: MatSnackBar,
  ) {
    if (this.pageReloadInterval) {
      this.reloadTimeout = this.setReloadTimeOut(this.pageReloadInterval);
    }

    this.getRouteQueryParameters();
  }

  ngOnInit() {
    window.addEventListener('popstate', (event) => {
      this.workflowService.goBackToLastOpenedPageAndUpdateLevel(this.router.routerState.snapshot.root,);
    });
    this.haveAdminPageAccess = this.permissions.checkAdminPermission();
    try {
      this.agAndDomainKey = "";

      Offline.on(
        "down",
        () => {
          this.logger.log("info", "Connection lost :-(");
          this.isOffline = true;
        },
        this
      );

      Offline.on(
        "up",
        () => {
          this.logger.log("info", "Connection was lost, It is back now");
          this.isOffline = false;
          location.reload();
        },
        this
      );

      this.notificationObservableServiceSubscription = this.notificationObservableService.getMessage().subscribe(data => {
        this.snackBar.openFromComponent(SnackbarComponent, {
          horizontalPosition: "right",
          verticalPosition: "top",
          data: {
            message: data.msg,
            iconSrc: data.image,
            variant: data.category
          },
          duration: data.duration,
        });
      })

      this.downloadSubscription = this.downloadService
        .getDownloadStatus()
        .subscribe((val) => {
          if (val) {
            this.showPacLoader.push("downloading");
          } else {
            this.showPacLoader.pop();
          }
        });

      this.themeSubscription = this.themeObservableService
        .getTheme()
        .subscribe((theme) => {
          this.theme = theme;
        });
    } catch (error) {
      this.logger.log("error", error);
    }
  }

  navigateBackToRootLevel() {
    this.dataStore.set("selectedApplicationSummary", JSON.stringify(null));
    this.workflowService.goBackToLastOpenedPageAndUpdateLevel(
      this.router.routerState.snapshot.root,
      0
    );
    const navigationParams = {
      relativeTo: this.activatedRoute, // <-- Parent activated route
    };

    navigationParams["queryParams"] = {
      ag: this.queryParameters["ag"],
      domain: this.queryParameters["domain"],
    };

    this.router.navigate(
      [
        {
          outlets: {
            details: null,
          },
        },
      ],
      navigationParams
    );
    this.workflowService.goBackToLastOpenedPageAndUpdateLevel(
      this.router.routerState.snapshot.root,
      0
    );
    this.workflowService.clearAllLevels();
  }

  setReloadTimeOut(timeoutInterval) {
    this.logger.log(
      "info",
      "Setting the page reload interval to: " + timeoutInterval
    );
    const reloadTimeout = setTimeout(function () {
      window.location.reload();
    }, timeoutInterval);

    return reloadTimeout;
  }

  updateAssetGroup(assetGroupName) {
    if (assetGroupName) {
      this.assetGroupObservableService.updateAssetGroup(assetGroupName);
    } else {
      this.router.navigate([
        "/post-login",
        { outlets: { modal: ["change-default-asset-group"] } },
      ]);
    }
  }

  updateDomainName(domainName) {
    const currentAssetGroup = this.dataStore.getCurrentSelectedAssetGroup();
    if (domainName) {
      this.domainTypeObservableService.updateDomainType(
        domainName,
        currentAssetGroup
      );
    }
  }

  getRouteQueryParameters(): any {
    this.activatedRouteSubscription = this.activatedRoute.queryParams.subscribe(
      (params) => {
        if (params["ag"] || params["domain"]) {
          const newKey = params["ag"] + params["domain"];

          if (newKey === this.agAndDomainKey) {
            return false;
          }

          this.agAndDomainKey = newKey;
          // this.queryParameters = params;

          // this.updateAssetGroup(this.queryParameters["ag"]);
          // this.updateDomainName(this.queryParameters["domain"]);

          if(params["ag"] && params["ag"]!=this.queryParameters["ag"]){
            this.queryParameters["ag"] = params["ag"];
            this.updateAssetGroup(this.queryParameters["ag"]);
          }
          if(params["domain"] && params["domain"]!=this.queryParameters["domain"]){
            this.queryParameters["domain"] = params["domain"];
            this.updateDomainName(this.queryParameters["domain"]);
          }
        }
        /* User will enter it in minutes */
        if (params["reload"]) {
          this.pageReloadInterval = params["reload"] * 60000;
          if (this.reloadTimeout) {
            clearTimeout(this.reloadTimeout);
            this.reloadTimeout = this.setReloadTimeOut(this.pageReloadInterval);
          }
        }
      }
    );
  }

  ngOnDestroy() {
    try {
      this.activatedRouteSubscription.unsubscribe();
      this.notificationObservableServiceSubscription.unsubscribe();
    } catch (error) {
      this.logger.log("error", error);
    }
  }
}
