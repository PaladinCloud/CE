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

 import { Component, Input, Output, EventEmitter, OnDestroy, AfterViewInit, ViewChild, TemplateRef } from '@angular/core';
 import {ActivatedRoute, Router} from '@angular/router';
 import { AssetTilesService } from '../../core/services/asset-tiles.service';
 import { AssetGroupObservableService } from '../../core/services/asset-group-observable.service';
 import { UpdateRecentAGService } from './../common/services/update-recent-ag.service';
 import { Subscription } from 'rxjs';
 import { AutorefreshService } from '../../pacman-features/services/autorefresh.service';
 import { environment } from './../../../environments/environment';
 import { ErrorHandlingService } from '../../shared/services/error-handling.service';
 import { DataCacheService } from '../../core/services/data-cache.service';
 import { LoggerService } from '../../shared/services/logger.service';
 import { UtilsService } from '../../shared/services/utils.service';
 import { WorkflowService } from 'src/app/core/services/workflow.service';
 import { MatDialog, MatDialogRef } from '@angular/material/dialog';
 import { DialogBoxComponent } from 'src/app/shared/components/molecules/dialog-box/dialog-box.component';
 import { TourService } from 'src/app/core/services/tour.service';
 
 @Component({
   selector: 'app-asset-groups',
   templateUrl: './asset-groups.component.html',
   styleUrls: ['./asset-groups.component.css'],
   providers: [AssetTilesService, AutorefreshService, LoggerService, ErrorHandlingService, UpdateRecentAGService]
 
 })
 export class AssetGroupsComponent implements AfterViewInit, OnDestroy {
 
 
   @Input() clickedVal = false;
   @Input() hideCloseButton;
   @Input() notLoadedAsModel;
   @Output() closeAssetGroup = new EventEmitter();
   recentlyViewedAssets = [];
   chipList;
   assetTabNames: any;
   selectedTab = 0;
   selectedTabName;
   returnedSearch = '';
   searchQuery= '';
   assetTiles;
   loaded: boolean;
   assetTile: string;
   selectedGroup: string;
   assetDetailTiles: any;
   recentTiles: any = [];
   userDetails: any;
   assetGroup: any = {};
   showError = false;
   assetDetailsState = 0;
   providerMap = {
     "aws": "AWS",
     "azure": "Azure",
     "gcp": "GCP"
   }
   placeHolderText = "search";
   @ViewChild("assetGroupSelectorRef") assetGroupSelectorRef: TemplateRef<any>;
 
   private subscriptionToAssetGroup: Subscription;
   private assetDetailsSubscription: Subscription;
   private assetTilesSubscription: Subscription;
   private updateRecentAGSubscription: Subscription;
   dialogRef: MatDialogRef<DialogBoxComponent>;
   currentAssetTile: string;
 
 
   constructor(
     private assetGroupsService: AssetTilesService,
     private assetGroupObservableService: AssetGroupObservableService,
     private activatedRoute: ActivatedRoute,
     private dataStore: DataCacheService,
     private dialog: MatDialog,
     private errorHandlingService: ErrorHandlingService,
     private logger: LoggerService,
     private router: Router,
     private tourService: TourService,
     private updateRecentAGService: UpdateRecentAGService,
     private utils: UtilsService,
     private workflowService: WorkflowService,
     ) {
     this.subscriptionToAssetGroup = this.assetGroupObservableService.getAssetGroup().subscribe(
     assetGroupName => {
         if(!this.assetTile) this.assetTileClicked(assetGroupName);
         this.assetTile = assetGroupName;
         this.currentAssetTile = this.assetTile;
         this.selectedGroup = assetGroupName;
     });
     const recentlyViewedList = this.dataStore.getRecentlyViewedAssetGroups();
     const recentTiles = JSON.parse(recentlyViewedList);
     const selectedAssetGroup = recentTiles[0].displayName;
     this.chipList = ["Active Asset Group: "+ this.getDisplayName(selectedAssetGroup)];
 }
 
   ngAfterViewInit() {
     try {
       if (this.subscriptionToAssetGroup) {
         this.subscriptionToAssetGroup.unsubscribe();
       }
       this.loaded = false;
       this.retrieveFragment();
       this.getAssetTiles();
     } catch (error) {
       this.errorHandlingService.handleJavascriptError(error);
       this.logger.log('error', error);
     }
     this.openAssetGroupSelector();
   }
 
 
   openAssetGroupSelector(){
     this.dialogRef = this.dialog.open(DialogBoxComponent, {
       width: '1000px',
       data: { 
             title: null,
             noButtonLabel: "Cancel",
             yesButtonLabel: "Select",
             template: this.assetGroupSelectorRef
           } 
     });
     this.dialogRef.afterOpened().subscribe(() => {
       this.tourService.setComponentReady();
     })
     this.dialogRef.afterClosed().subscribe(result => {
       if(result == "yes")
         this.setDefault(this.currentAssetTile.toLowerCase());
       else
         this.instructParentToCloseAssetGroup();
     });
   }
 
   retrieveFragment() {
     this.activatedRoute.fragment.subscribe((fragment: string) => {
       this.selectedTabName = fragment;
       this.getRecentlyViewed();
     });
   }
 
   handleSearch(searchText:string){
     this.returnedSearch = searchText;
   }
 
   getAssetTiles(): void {
     this.showError = false;
 
     const assetGroupList = this.dataStore.getListOfAssetGroups();
 
     if (!assetGroupList || assetGroupList === 'undefined') {
 
         const assetUrl = environment.assetTiles.url;
         const assetMethod = environment.assetTiles.method;
 
         this.assetTilesSubscription = this.assetGroupsService.getAssetTiles(assetUrl, assetMethod).subscribe(
             response => {
                 this.assetTiles = response[0];
                 this.dataStore.setListOfAssetGroups(JSON.stringify(this.assetTiles));
                 this.processData();
             },
             error => {
                 this.loaded = true;
                 this.showError = true;
                 this.logger.log('error', error);
             });
     } else {
         this.assetTiles = JSON.parse(assetGroupList);
         this.assetTiles.sort((a, b) => a.displayname.localeCompare(b.displayname, 'en', { sensitivity: 'base' }));
         this.processData();
     }
   }
 
   getDisplayName(assetName){
     return assetName.replace(/,/g, " ");
   }
 
   assetTileClicked(assetGroupName:string) {
     this.currentAssetTile = assetGroupName.toLowerCase();
      this.getAssetDetailTiles(this.currentAssetTile );
      this.updateRecentAssetGroup(this.currentAssetTile);
   }
 
   setDefault(assetGroupName:string="") {
     if(assetGroupName){
      this.assetTile = assetGroupName.toLowerCase();
     }
     this.workflowService.clearAllLevels();
 
     try {
 
       this.instructParentToCloseAssetGroup(this.assetTile.toLowerCase());
 
       const userDefaultAssetGroup = this.dataStore.getUserDefaultAssetGroup();
 
       if (this.assetTile !== userDefaultAssetGroup) {
         this.updateDefaultAssetGroupForUser(this.assetTile.toLowerCase());
       }
     } catch (error) {
       this.errorHandlingService.handleJavascriptError(error);
       this.logger.log('error', error);
     }
   }
 
   selectAsset(assetGroup) {
     this.workflowService.clearAllLevels();
     try {
       this.instructParentToCloseAssetGroup(assetGroup.name);
       if (assetGroup.name !== this.selectedGroup) {
         this.selectedGroup = assetGroup.name;
 
         this.assetTileClicked(assetGroup.name);
       }
     } catch (error) {
       this.errorHandlingService.handleJavascriptError(error);
       this.logger.log('error', error);
     }
   }
 
   updateDefaultAssetGroupForUser(assetGroup) {
     try {
 
       const updateAssetGroupUrl = environment.saveDefaultAssetGroup.url;
       const updateAssetGroupMethod = environment.saveDefaultAssetGroup.method;
 
       const userId = this.dataStore.getUserDetailsValue().getUserId();
 
       this.assetTilesSubscription = this.assetGroupsService.updateDefaultAssetGroupForUser(updateAssetGroupUrl, updateAssetGroupMethod, assetGroup, userId).subscribe(
         response => {
           this.dataStore.setUserDefaultAssetGroup(assetGroup);
           const redirectUrl = location.href;
           localStorage.setItem("redirectUrl",redirectUrl);
         },
         error => {
        });
     } catch (error) {
       this.errorHandlingService.handleJavascriptError(error);
     }
   }
 
   processData() {
     try {
       const typeObj = {
       'all': 'typeVal',
       'user': 'typeVal',
       'recently viewed': 'typeVal',
       };
       const newTypeObj = {};
       for ( let i = 0 ; i < this.assetTiles.length; i++) {
         if(this.assetTiles[i].type.toLowerCase() != "user")
            newTypeObj[this.assetTiles[i].type.toLowerCase()] = 'typeVal';
       }
       delete typeObj[''];
       delete newTypeObj[''];
       let typeArr = [], newTypeArr = [];
       typeArr = Object.keys(typeObj);
       newTypeArr = Object.keys(newTypeObj);
       newTypeArr.sort();
       typeArr = [...typeArr,...newTypeArr];
       this.assetTabNames = typeArr;
       this.selectedTabName = this.assetTabNames[0];
       this.loaded = true;
     } catch (error) {
       this.errorHandlingService.handleJavascriptError(error);
       this.logger.log('error', error);
     }
   }
 
   getSelectedTabAssets(assettileType){
     if(this.selectedTabName && assettileType){
       if(assettileType == "stakeholder")
           assettileType = "user";
       if(this.selectedTabName.toLowerCase() == assettileType.toLowerCase()){
         return true;
       }
     }
     return false;
   }
 
   getAssetDetailTiles(groupName) {
 
     const assetDetailUrl = environment.assetTilesdata.url;
 
     const assetDetailMethod = environment.assetTilesdata.method;
 
     const queryParams = {
       'ag': groupName
     };
 
     this.assetDetailsState = 0;
 
      if (queryParams['ag'] !== undefined) {
 
      this.assetDetailsSubscription = this.assetGroupsService.getAssetdetailTiles(queryParams, assetDetailUrl, assetDetailMethod).subscribe(
        response => {
          this.assetDetailsState = 1;
          this.assetDetailTiles = response[0];
       },
       error => {
           this.assetDetailsState = -1;
       });
      }
   }
 
   updateRecentAssetGroup(groupName) {
     const updateRecentAGUrl = environment.updateRecentAG.url;
     const updateRecentAGMethod = environment.updateRecentAG.method;
     const userId = this.dataStore.getUserDetailsValue().getUserId();
     const queryParams = {
       'ag': groupName,
       'userId': userId.split(".")[0]
     };
 
     if (queryParams['ag'] !== undefined) {
 
      this.updateRecentAGSubscription = this.updateRecentAGService.updateRecentlyViewedAG(queryParams, updateRecentAGUrl, updateRecentAGMethod).subscribe(
        response => {
          this.recentTiles = response.data.response[0].recentlyViewedAg;
       },
       error => {
         this.logger.log("Error in fetching Recent Tiles", error);
       });
     }
   }
 
   instructParentToCloseAssetGroup (assetGroupName:string='') {
     this.closeAssetGroup.emit(assetGroupName);
     this.dialogRef.close();
   }
 
   tabsClicked(assetTabName:string){
     this.selectedTabName = assetTabName;
     if(this.selectedTabName.toLowerCase()=="recently viewed"){
       this.getRecentlyViewed();
     }
   }
 
   getRecentlyViewed() {
     if (!this.selectedTabName) {
       return;
     }
     this.recentlyViewedAssets = [];
     const tiles = this.recentTiles.map(item => {
       return item['ag'];
     });
     if (this.selectedTabName.toLowerCase() === 'recently viewed') {
       tiles.forEach(recentTileName => {
         this.assetTiles.forEach(assetTile => {
              if (assetTile.name == recentTileName) {
                this.recentlyViewedAssets.push(assetTile);
                }
            });
       });
     }
   }
 
   checkIsRecentlyViewedOrAll() {
     if (this.selectedTabName && (this.selectedTabName.toLowerCase() === 'recently viewed' || this.selectedTabName.toLowerCase() === 'all')) {
       return true;
     }
     return false;
   }
 
     /**
    * This function navigates the page mentioned  with a ruleID
    */
     navigatePage(data1, data2) {
       /**
        * selectAsset function closes the modal window and update the asset group
        * after that router.navigate is used to navigate
        */
 
     try {
       const clickText = data1;
       const apiTarget = {'TypeAsset' : 'TotalAsset'};
             /**
              * Router navigation is not working , need to check --> Trinanjan
              */
 
             if (clickText.toLowerCase() === 'total asset' ) {
                 /**
                  * This router.navigate function is added By Trinanjan on 31.01.2018
                  * This router.navigate function first closes the modal window and then navigates to the path specified
                  */
                 const eachParams = {};
                 let newParams = this.utils.makeFilterObj(eachParams);
                 newParams = Object.assign(newParams, apiTarget);
                 newParams['ag'] = data2;
                 this.router.navigate([{
                     outlets: {
                         modal: null
                     }
                 }],
                     {
                         relativeTo: this.activatedRoute.parent,
                         queryParamsHandling: 'merge'
                     }).then(() => this.router.navigate(['pl', 'assets' , 'asset-list' ], {queryParams: newParams, queryParamsHandling: 'merge'}));
             }
     } catch (error) {
       this.logger.log('error', error);
     }
   }
 
   ngOnDestroy() {
     try {
       this.subscriptionToAssetGroup.unsubscribe();
     } catch (error) {
 
     }
   }
 
 }
 