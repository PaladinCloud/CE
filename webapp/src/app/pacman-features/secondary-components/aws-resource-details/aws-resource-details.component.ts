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

import { Component, OnInit, OnDestroy, Input, OnChanges, SimpleChanges, AfterViewInit } from '@angular/core';
import { ActivatedRoute } from '@angular/router';

import { Subscription } from 'rxjs';
import { AssetGroupObservableService } from '../../../core/services/asset-group-observable.service';
import { AwsResourceTypeSelectionService } from './../../services/aws-resource-type-selection.service';
import { ErrorHandlingService } from '../../../shared/services/error-handling.service';
import { ICONS } from './../../../shared/constants/icons-mapping';
import { DomainTypeObservableService } from '../../../core/services/domain-type-observable.service';
import { LoggerService } from '../../../shared/services/logger.service';
import { CONFIGURATIONS } from './../../../../config/configurations';
import { UtilsService } from '../../../shared/services/utils.service';

@Component({
  selector: 'app-aws-resource-details',
  templateUrl: './aws-resource-details.component.html',
  styleUrls: ['./aws-resource-details.component.css'],
})

export class AwsResourceDetailsComponent implements OnInit, OnDestroy {
  private awsResources: any = [];
  private activeTileIndex: any = 0;
  public categories = [];
  private categoryNames = [];
  public filteredResources: any = [];
  public activeFilterCategory: any;
  public searchTxt = '';
  routeTo = 'asset-list';

  private resourceTypeSelectionSubscription: Subscription;
  selectedResource: any = {
    type: undefined
  };
  dataLoaded: boolean;
  loading: boolean;
  errorMessage: string;
  error: boolean;

  private selectedResourceTypeFromUrl: string;
  private awsResourceDetails: any = [];
  private selectedResourceRecommendation: any = [];
  private allAvailableCategories: any = [];

  private selectedAssetGroup: any;
  awsResourcesCache: any = [];

  private showViewMore = false;

  private assetGroupSubscription: Subscription;
  private routeSubscription: Subscription;
  private resourceSelectionSubscription: Subscription;
  subscriptionDomain: Subscription;
  selectedDomain: any;

  @Input() pageLevel: number;

  public config;
  public oss;

  saveSelectedResource() {
    this.awsResourceTypeSelectionService.awsResourceSelected(this.selectedResource);
  }

  filterByCategory(category) {
    this.activeFilterCategory = category;
    const resources = this.awsResources.slice();
    this.filteredResources = [];
    resources.forEach(element => {
      if (element.category === category.name || category.name === 'All') {
        this.filteredResources.push(element);
      }
    });
    this.filteredResources.sort((a, b) => b.count - a.count);
    this.selectedResource = this.filteredResources[0];
  }

  getAllCategories() {
    this.categories = [];

    // Temporary array to store unique categories
    this.categoryNames = [];

    this.awsResources.forEach(resource => {
      const category = ICONS.categories[resource.category] === undefined ? 'Extra' : resource['category'];
      resource.category = category;   // Update the category of current resource depending on the result of the above line
      if (this.categoryNames.indexOf(category) === -1) {
        this.categoryNames.push(category);
        const obj = {
          'name': category,
          'color': ICONS.categories[category]
        };
        this.categories.push(obj);
      }
    });

    // If extra categories are present, push them to the end
    if (this.categoryNames.indexOf('Extra') > -1) {
      const extraCategory = this.categories.splice(this.categoryNames.indexOf('Extra'), 1)[0];
      this.categories.push(extraCategory);
    }

    // Push 'All' type at the front if categories array
    this.categories.unshift({
      'name': 'All',
      'color': '#333333'
    });

    this.activeFilterCategory = this.categories[0];
  }

  getAwsResources() {
    this.resourceTypeSelectionSubscription = this.awsResourceTypeSelectionService.getAllAwsResources().subscribe(
      allAwsResources => {
        this.filteredResources = [];
        this.awsResources = allAwsResources;
        this.filteredResources = this.awsResources.slice();
        this.filteredResources.sort((a, b) => b.count - a.count);
        this.selectedResource = this.filteredResources[0];
        this.getAllCategories();
        this.setDataLoaded();
      },
      error => {
        this.logger.log('error', error);
      }
    );
  }
  constructor(
    private utils: UtilsService,
    private route: ActivatedRoute,
    private assetGroupObservableService: AssetGroupObservableService,
    private awsResourceTypeSelectionService: AwsResourceTypeSelectionService,
    private errorHandling: ErrorHandlingService,
    private domainObservableService: DomainTypeObservableService,
    private logger: LoggerService) {


    this.config = CONFIGURATIONS;

    this.oss = this.config && this.config.optional && this.config.optional.general && this.config.optional.general.OSS;

    this.assetGroupSubscription = this.assetGroupObservableService.getAssetGroup()
      .subscribe(
        assetGroupName => {
          this.selectedAssetGroup = assetGroupName;
        });
    this.subscriptionDomain = this.domainObservableService.getDomainType().subscribe(domain => {
      this.selectedDomain = domain;
      this.init();
    });

    this.routeSubscription = this.route.queryParams.subscribe(params => {
      if (params['type']) {
        this.selectedResourceTypeFromUrl = params['type'];
      }


    });

  }

  ngOnInit() {
    // Reset all variables
    this.loading = false;
    this.dataLoaded = false;
    this.error = false;
    this.errorMessage = 'apiResponseError';
    this.awsResourcesCache = [];
    this.selectedResource['recommendations'] = [];
    this.getData();
    this.viewAllSetup();
    this.getAwsResources();
  }

  init() {
    this.getData();
  }

  setDataLoading() {
    this.loading = true;
    this.dataLoaded = false;
    this.error = false;
  }

  setDataLoaded() {
    this.loading = false;
    this.dataLoaded = true;
    this.error = false;
  }

  setError(error) {
    this.loading = false;
    this.dataLoaded = false;
    this.error = true;
    this.logger.log('error', error);
  }

  viewAllSetup() {
    try {

      if (this.resourceSelectionSubscription) {
        this.resourceSelectionSubscription.unsubscribe();
      }

      // SeelctedResource selected from viewAll.

      this.resourceSelectionSubscription = this.awsResourceTypeSelectionService.getSelectedResource()
        .subscribe(
          selectedResourceInViewAll => {
            if (this.awsResourceDetails !== undefined) {
              this.awsResourceDetails.forEach(element => {
                if (element.type === selectedResourceInViewAll) {

                  // If the selected element from 'view-all' is already present in 'awsResourcesCache', then remove it
                  for (let i = 0; i < this.awsResourcesCache.length; i++) {
                    if (this.awsResourcesCache[i].type === selectedResourceInViewAll) {
                      this.awsResourcesCache.splice(i, 1);
                    }
                  }

                  // Add the freshly selected resource to the first index of array 'awsResourcesCache
                  this.awsResourcesCache.unshift(element);

                  // Limit the items in 'awsResourcesCache' to maximum of 7
                  if (this.awsResourcesCache.length > 7) {
                    this.awsResourcesCache = this.awsResourcesCache.slice(0, 7);
                  }

                  this.selectedResource = Object.assign(element);
                  this.selectResourceTile(this.selectedResource);

                }
              });
            }
          });
    } catch (error) {
      this.errorMessage = this.errorHandling.handleJavascriptError(error);
      this.setError(error);
      this.logger.log('error', error);
    }
  }

  getData() {
    this.setDataLoading();
  }

  assignIconsToResources() {
    const categoriesObj = [];

    // Temporary array to store unique categories
    this.allAvailableCategories = [];

    this.awsResourceDetails.forEach(resources => {
      const category = ICONS.categories[resources.category] === undefined ? 'Extra' : resources['category'];
      resources.category = category;   // Update the category of current resource depending on the result of the above line
      if (this.allAvailableCategories.indexOf(category) === -1) {
        this.allAvailableCategories.push(category);
        const obj = {
          'name': category,
          'color': ICONS.categories[category]
        };
        categoriesObj.push(obj);
      }
    });

    // If extra categories are present, push them to the end
    if (this.allAvailableCategories.indexOf('Extra') > -1) {
      const extraCategory = categoriesObj.splice(this.allAvailableCategories.indexOf('Extra'), 1);
      categoriesObj.push(extraCategory);
    }

    this.awsResourceDetails.forEach(resources => {
      resources['iconPath'] = ICONS.awsResources[resources.type];
    });

  }

  sortAwsResources() {

    this.awsResourceDetails.sort(function (a, b) {
      return b.count - a.count;     // For descending order
      // return a.count - b.count;  // For ascending  order
    });

    let computeCategoryPresent = false;
    let firstComputeIndex = 0;
    // Check if 'compute' category assets are there. If present, sort them with Compute category resources being first.
    const allResources = this.awsResourceDetails.slice();
    this.awsResourceDetails.forEach((element, index) => {
      if (element.category.toLowerCase() === 'compute') {
        computeCategoryPresent = true;
        firstComputeIndex += 1;
        allResources.splice(index, 1);
        allResources.unshift(element);
      }
    });

    // Sort compute category items
    const computeCategoryResources = allResources.slice(0, firstComputeIndex);
    computeCategoryResources.sort(function (a, b) {
      return b.count - a.count;     // For descending order
      // return a.count - b.count;  // For ascending  order
    });

    for (let i = 0; i < computeCategoryResources.length; i++) {
      allResources[i] = computeCategoryResources[i];
    }

    if (computeCategoryPresent) {
      this.awsResourceDetails = allResources.slice();
    }

  }

  setupMainPageResourceTypes() {
    if (this.awsResourceDetails.length > 7) {
      this.awsResourcesCache = this.awsResourceDetails.slice(0, 7);
      this.showViewMore = true;
    } else {
      this.awsResourcesCache = this.awsResourceDetails.slice();
      this.showViewMore = false;
    }
    // if (!this.selectedResourceTypeFromUrl) {
    //   this.selectResourceTile(this.awsResourceDetails[0]);
    // } else {
    //   this.selectResourceTile(this.selectedResourceTypeFromUrl);
    //   //look into it
    // }
  }

  awsTileClicked(resource) {
    if (this.filteredResources.length == 0) {
      return;
    }
    this.selectedResource = resource;
    this.selectedResourceRecommendation = this.selectedResource['recommendations'];
  }

  removeTargetTypesOfCategoryOthers(resourceTypes, categoryType) {
    const updatedResourceTypes = resourceTypes.filter((value) => {
      return value.category.toLowerCase() !== categoryType.toLowerCase();
    });
    return updatedResourceTypes;
  }

  selectResourceTile(resources) {
    this.awsTileClicked(resources);
  }

  getResourceTypeObjectFromType(resources) {
    const tileIndex = this.awsResourceDetails.findIndex((value) => {
      return value.type.toLowerCase() === resources.toLowerCase();
    });
    return this.awsResourceDetails[tileIndex];
  }

  ngOnDestroy() {
    try {
      this.routeSubscription.unsubscribe();
      this.assetGroupSubscription.unsubscribe();
      this.resourceSelectionSubscription.unsubscribe();
      this.subscriptionDomain.unsubscribe();
      this.resourceTypeSelectionSubscription.unsubscribe();
    } catch (error) {
      this.errorMessage = this.errorHandling.handleJavascriptError(error);
    }
  }

}
