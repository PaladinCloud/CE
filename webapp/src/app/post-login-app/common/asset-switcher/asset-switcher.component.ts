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
  OnInit,
  OnDestroy,
  Input,
  OnChanges,
  SimpleChanges,
} from "@angular/core";
import { Subscription } from "rxjs";
import { DataCacheService } from "../../../core/services/data-cache.service";
import { WorkflowService } from "../../../core/services/workflow.service";
import { LoggerService } from "../../../shared/services/logger.service";
import { DomainMappingService } from "../../../core/services/domain-mapping.service";
import { DomainTypeObservableService } from "../../../core/services/domain-type-observable.service";
import { AssetGroupObservableService } from "../../../core/services/asset-group-observable.service";
import { PermissionGuardService } from "../../../core/services/permission-guard.service";
import { CONFIGURATIONS } from "../../../../config/configurations";
import { CONTENT } from "./../../../../config/static-content";
import { RecentlyViewedObservableService } from "../../../core/services/recently-viewed-observable.service";
import { environment } from "./../../../../environments/environment";
import { ActivatedRoute, Router } from "@angular/router";
import { UpdateRecentAGService } from "../../common/services/update-recent-ag.service";
import { RouterUtilityService } from "../../../shared/services/router-utility.service";
import { HttpHeaders } from "@angular/common/http";
import { AdalService } from "../../../core/services/adal.service";
import { HttpService } from "../../../shared/services/http-response.service";
import { UtilsService } from "../../../shared/services/utils.service";

@Component({
  selector: "app-asset-switcher",
  templateUrl: "./asset-switcher.component.html",
  styleUrls: ["./asset-switcher.component.css"],
  providers: [UpdateRecentAGService],
})
export class AssetSwitcherComponent implements OnInit, OnDestroy {
  assetCount;
  config;
  dynamicIconPath;
  showRecents = false;
  staticContent;
  assetGroupSubscription: Subscription;
  subscriptionToDomainType: Subscription;
  recentSubscription: Subscription;
  haveAdminPageAccess = false;
  currentAg;
  recentTiles = [];
  provider = [];
  cloudIconDataLoaded = false;
  public showMenu;
  public environment;
  tvState;
  querySubscription: Subscription;
  updateRecentAGSubscription: Subscription;
  subscriptionToAssetGroup: Subscription;
  queryParams;
  @Input() isExpanded = false;

  constructor(
    private dataCacheService: DataCacheService,
    private workflowService: WorkflowService,
    private loggerService: LoggerService,
    private activatedRoute: ActivatedRoute,
    private router: Router,
    private recentAssetsObservableService: RecentlyViewedObservableService,
    private assetGroupObservableService: AssetGroupObservableService,
    private permissions: PermissionGuardService,
    private routerUtilityService: RouterUtilityService,
    private updateRecentAGService: UpdateRecentAGService,
  ) {
    this.config = CONFIGURATIONS;
    this.staticContent = CONTENT;
    this.environment = environment;
    this.subscriptionToAssetGroup = this.assetGroupObservableService
      .getAssetGroup()
      .subscribe((assetGroupName) => {
        if (assetGroupName) {
          this.currentAg = assetGroupName;
          this.cloudIconDataLoaded = false;
          this.updateRecentAssetGroup(this.currentAg);
        }
      });

    this.recentSubscription = this.recentAssetsObservableService
      .getRecentAssets()
      .subscribe((recentList) => {
        this.recentTiles = recentList;
      });
  }

  ngOnInit() {
    try {
      document.addEventListener("keyup", this.logKey.bind(this));
      this.querySubscription = this.activatedRoute.queryParams.subscribe(
        (queryParams) => {
          this.tvState = queryParams["tv"];
        }
      );
      this.dynamicIconPath =
        "../assets/icons/" +
        this.config.required.APP_NAME.toLowerCase() +
        "-white-text-logo.svg";
      this.haveAdminPageAccess = this.permissions.checkAdminPermission();

    } catch (error) {
      this.loggerService.log("error", "JS Error" + error);
    }
  }

  logKey(e) {
    e.stopPropagation();
    if (e.keyCode === 27) {
      this.showMenu = false;
    }
  }

  clearPageLevel() {
    this.workflowService.clearAllLevels();
  }


  changeAg(agData) {
    const updatedFilters = JSON.parse(
      JSON.stringify(
        this.routerUtilityService.getQueryParametersFromSnapshot(
          this.router.routerState.snapshot.root
        )
      )
    );
    updatedFilters["ag"] = agData.ag;
    this.router.navigate([], {
      relativeTo: this.activatedRoute,
      queryParams: updatedFilters,
    });
    this.showRecents = false;
  }

  updateRecentAssetGroup(groupName) {
    if (this.updateRecentAGSubscription) {
      this.updateRecentAGSubscription.unsubscribe();
    }
    const updateRecentAGUrl = environment.updateRecentAG.url;
    const updateRecentAGMethod = environment.updateRecentAG.method;
    const userId = this.dataCacheService.getUserDetailsValue().getUserName();
    const queryParams = {
      ag: groupName,
      userId: userId,
    };
    if (queryParams["ag"] !== undefined) {
      this.updateRecentAGSubscription = this.updateRecentAGService
        .updateRecentlyViewedAG(
          queryParams,
          updateRecentAGUrl,
          updateRecentAGMethod
        )
        .subscribe(
          (response) => {
            this.recentTiles = response.data.response[0].recentlyViewedAg;
            /* Store the recently viewed asset list in stringify format */
            this.dataCacheService.setRecentlyViewedAssetGroups(
              JSON.stringify(this.recentTiles)
            );
            const currentAGDetails = this.recentTiles.filter(
              (element) => element.ag === groupName
            );
            this.provider = this.fetchprovider(currentAGDetails);
            this.cloudIconDataLoaded = true;
            this.recentAssetsObservableService.updateRecentAssets(
              this.recentTiles
            );
          },
          (error) => { }
        );
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

  openAgModal() {
    this.router.navigate(
      ["/pl", { outlets: { modal: ["change-default-asset-group"] } }],
      { queryParamsHandling: "merge" }
    );
  }


  ngOnDestroy() {
    if (this.assetGroupSubscription) {
      this.assetGroupSubscription.unsubscribe();
    }
    if (this.subscriptionToDomainType) {
      this.subscriptionToDomainType.unsubscribe();
    }
  }
}
