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


 import {throwError as observableThrowError,  Observable, combineLatest } from 'rxjs';
 import { Injectable } from '@angular/core';
 
 import { HttpService } from './http-response.service';
 import {environment} from '../../../environments/environment';
 import {UtilsService} from './utils.service';
 import {LoggerService} from './logger.service';
 import {RefactorFieldsService} from './refactor-fields.service';
 import { map } from 'rxjs/operators';
 import { IssueFilterService } from 'src/app/pacman-features/services/issue-filter.service';
 import { IFilterObj, IFilterOption } from '../table/interfaces/table-props.interface';
 import { find } from 'lodash';
import { ActivatedRoute, Router } from '@angular/router';
import { WorkflowService } from 'src/app/core/services/workflow.service';
 
 interface IFilterPayload {
     ag?: string;
     domain?: string;
     type?: string;
     attributeName: any;
     searchText?: any;
     filter: any;
 }
 
 @Injectable()
 export class FilterManagementService {
 
     constructor(
      private httpService: HttpService,
      private router: Router,
      private activatedRoute: ActivatedRoute,
      private utils: UtilsService,
      private issueFilterService: IssueFilterService,
      private logger: LoggerService,
      private refactorFieldsService: RefactorFieldsService,
      private workflowService: WorkflowService
     ) { }
 
     getApplicableFilters(filterId, filterParams = {}) {
 
         const url = environment.issueFilter.url;
         const method = environment.issueFilter.method;
         const payload = {};
         const queryParams = filterParams;
         queryParams['filterId'] = filterId;
 
         try {
             return combineLatest(
                 this.httpService.getHttpResponse(url, method, payload, queryParams)
                     .pipe(map(response => this.massageData(response) )
                     // .catch(this.handleError)
             ));
         } catch (error) {
             this.handleError(error);
         }
 
     }
 
     getValuesForFilterType(currentFilterType, queryParam = {}, payload = {}) {
 
         const url = environment.base + this.utils.getParamsFromUrlSnippet(currentFilterType.optionURL).url;
         const method = 'GET';
 
         try {
             return combineLatest(
                 this.httpService.getHttpResponse(url, method, payload, queryParam)
                     .pipe(map(response => this.massageData(response) ))
                     // .catch(this.handleError)
             );
         } catch (error) {
             this.handleError(error);
         }
     }
 
     getFilterArray(pageLevelAppliedFilters) {
         try {
             const localFilters = []; // <<-- this filter is used to store data for filter
             const filterObjKeys = Object.keys(pageLevelAppliedFilters);
             const dataArray = [];
             for ( let i = 0; i < filterObjKeys.length; i++) {
                 let obj = {};
                 obj = {
                     name: filterObjKeys[i]
                 };
                 dataArray.push(obj);
             }
 
             const filterValues = dataArray;
             const formattedFilters = dataArray.map(data => {
                 data.name = this.refactorFieldsService.getDisplayNameForAKey(data.name) || data.name;
                 return data;
             });
 
             for ( let i = 0; i < formattedFilters.length; i++) {
                 const eachObj = {
                     key: formattedFilters[i].name, // <-- displayKey-- Resource Type
                     value: pageLevelAppliedFilters[filterObjKeys[i]], // <<-- value to be shown in the filter UI-- S2
                     filterkey: filterObjKeys[i].trim(), // <<-- filter key that to be passed -- 'resourceType '
                     compareKey: filterObjKeys[i].toLowerCase().trim() // <<-- key to compare whether a key is already present -- 'resourcetype'
                 };
                 localFilters.push(eachObj);
             }
 
             return localFilters;
 
         } catch (error) {
             this.logger.log('error', error);
         }
     }
 
     getFilters(filterId: number): Observable<IFilterOption[]> {
         return this.issueFilterService
         .getFilters(
             { filterId },
             environment.issueFilter.url,
             environment.issueFilter.method
         ).pipe(map(response => response[0].response));
     }
 
     async getFilterTagsData(payload, optionUrl) {
         return this.issueFilterService.getFilters({}, 
          environment.base  
          + this.utils.getParamsFromUrlSnippet(optionUrl).url, "POST", payload)
             .toPromise()
           .then(response => {
             if (response[0].data.response) {
               return response[0].data.response;
             } else if (response[0].data.optionList) {
               return response[0].data.optionList;
             }
             return response[0].data;
             });
     }
 
   // TODO: getting order from url might sometimes lead to inconsistency because
   // the filter might be present (say at 2nd position) with 0 selected but since it
   // is 0 selected, it might not be present in the URL.
   // Thus, filter order cannot be derived from url.
   getFiltersAppliedOrderFromURL(filterString){
     try{
       if(filterString){
         // Split the string by '**'
         const splitByDoubleAsterisk = filterString.split('**');
 
         // Initialize an array to hold the keys
         const keys = [];
 
         // Iterate through the substrings obtained from the split
         splitByDoubleAsterisk.forEach(substring => {
           // Split each substring by '=' to get key-value pairs
           const keyValuePair = substring.split('=');
           if (keyValuePair.length === 2) {
             // Extract the key from the key-value pair and add it to the keys array
             keys.push(decodeURIComponent(keyValuePair[0]).replace(".keyword", ""));
           }
         });
 
         return keys;
       }
     }catch(e){
       this.logger.log("jsError",e);
     }
   }
 
   getFormattedFilters(filterText, filterTypeOptions){
     return Object.keys(filterText).map(filterKey => {
         const keyDisplayValue = filterTypeOptions.find(option => option.optionValue === filterKey)?.optionName;
           return {
             keyDisplayValue,
             filterkey: filterKey,
           };
         });
   }
 
   deleteFilters(event, filters){
     let shouldUpdateComponent = false;
     try {
       if((event && event.index!=undefined && filters[event.index].filterValue==undefined) || !event.clearAll){
         shouldUpdateComponent = true;
       }
       if (!event) {
         filters = [];
       } else if (event.removeOnlyFilterValue) {
         filters = this.removeFiltersOnRightOfIndex(filters, event.index);
        } else if (event.index !== undefined && !filters[event.index].filterValue) {
          filters.splice(event.index, 1);
          shouldUpdateComponent = false;
       } else if (!event.clearAll) {
         filters.splice(event.index, 1);
       } else {
         filters = [];
       }
     } catch (error) {
       this.logger.log('jsError', error);
     }
     return [filters, shouldUpdateComponent];
   }
 
   removeFiltersOnRightOfIndex(filters, index: number){
     for(let i=index+1; i<filters.length && i>0; i++){
       filters[i].filterValue = [];
       filters[i].value = [];
     }
     filters = [...filters];
     return filters;
   }
 
   changeFilterTags(filters, filterTagOptions, currentFilterType, event){    
     try {
         if (currentFilterType) {
           const filterTags = event.filterValue.flatMap(value => {
             const v = filterTagOptions[event.filterKeyDisplayValue].filter(availableOption => availableOption.name === value);
             return v ? v.map(item => item.id) : [value];
           });
           this.utils.addOrReplaceElement(
             filters,
             {
               keyDisplayValue: event.filterKeyDisplayValue,
               filterValue: event.filterValue,
               key: currentFilterType.optionName,
               value: filterTags,
               filterkey: currentFilterType.optionValue.trim(),
               compareKey: currentFilterType.optionValue.toLowerCase().trim(),
             },
             (el) => {
               return (
                 el.compareKey ===
                 currentFilterType.optionValue.toLowerCase().trim()
               );
             }
           );
         }
         const index = filters.findIndex(filter => filter.keyDisplayValue===currentFilterType.optionName);
         this.removeFiltersOnRightOfIndex(filters, index);
         return [...filters];
       } catch (error) {
         this.logger.log("error", error);
       }
   }
 
   async changeFilterType ({ currentFilterType, searchText, filterText, currentQueryParams, filtersToBePassed, type, agAndDomain, updateFilterTags, labelsToExcludeSort, ignoreAttributeName = false, extraPayloadProps = {}}){
    if(!currentFilterType){
      return [{}, []];
    }; 
    const {ag, domain} = agAndDomain;
     const filterOrder = this.getFiltersAppliedOrderFromURL(currentQueryParams.filter);;
     const urlObj = this.utils.getParamsFromUrlSnippet(currentFilterType.optionURL);
       const excludedKeys = [
         currentFilterType.optionValue,
         "domain",
         "include_exempt",
         currentFilterType["optionValue"]?.replace(".keyword", "")
       ];

       if(urlObj.params["attribute"]){
        excludedKeys.push(urlObj.params["attribute"]);
       }
       
       const index = filterOrder?.indexOf(currentFilterType.optionValue?.replace(".keyword", ""));
       const excludedKeysInUrl = Object.keys(filterText).filter(key => urlObj.url.includes(key));
   
       filtersToBePassed = Object.keys(filtersToBePassed).reduce((result, key) => {
         const normalizedKey = key.replace(".keyword", "");
         if ((!excludedKeys.includes(normalizedKey) && !excludedKeysInUrl.includes(normalizedKey))) {
           result[key] = filtersToBePassed[key];
         }
         return result;
       }, {});
       
       const sortedFiltersToBePassed = filterOrder?.slice(0, index)?.reduce((result, key) => {
         if (filtersToBePassed.hasOwnProperty(key)) {
           result[key] = filtersToBePassed[key];
         }
         return result;
       }, {});
   
       let payload: IFilterPayload = {
         attributeName: ignoreAttributeName ? undefined : currentFilterType["optionValue"]?.replace(".keyword", ""),
         //filter: sortedFiltersToBePassed && index>=0?sortedFiltersToBePassed:filtersToBePassed,
         filter: filtersToBePassed,
         ...extraPayloadProps
       };
 
       if(ag && domain){
         payload = {
             ...payload,
             ag, domain
         };
       }

       if(searchText){
        payload = {
          ...payload,
          searchText
        }
       }
 
       if(type){
         payload = {
             ...payload,
             type
         }
       }
   
       let filterTagsData = await this.getFilterTagsData(payload, currentFilterType.optionURL);
       if(updateFilterTags) {
         filterTagsData = updateFilterTags(filterTagsData, currentFilterType.optionName);
       }
       const filterTagOptions = filterTagsData;
       let filterTagLabels = filterTagsData.map(option => option.name);
 
       if (!labelsToExcludeSort?.toString().toLowerCase().includes(currentFilterType.optionName.toLowerCase())) {
         filterTagLabels = filterTagLabels.sort((a, b) => a.localeCompare(b));
       }
     return [filterTagOptions, Array.from(new Set(filterTagLabels))];
   }
 
   createFilterObj (keyDisplayValue: string, filterKey: string, validFilterValues: {name: string, id: string}[]): IFilterObj {
     return {
       keyDisplayValue: keyDisplayValue,
       filterValue: validFilterValues.map(valObj => valObj.name),
       key: keyDisplayValue,
       value: validFilterValues.map(valObj => valObj.id),
       filterkey: filterKey?.trim(),
       compareKey: filterKey?.toLowerCase().trim(),
     };
   }
 
   getValidFilterValues(keyDisplayValue, filterKey, filterText, filterTagOptions, filterTagLabels){
     const filterValues = filterText[filterKey]?.split(',') || [];
     const filterTagOptionsForKey = filterTagOptions[keyDisplayValue];
     const filterTagLabelsForKey = filterTagLabels[keyDisplayValue];
 
     
     const validFilterValues = filterValues
     .reduce((result, val) => {
     const valObj = filterTagOptionsForKey?.find(obj => obj.id === val);
     if (valObj && filterTagLabelsForKey?.includes(valObj.name)) {
         // here we push valid filter option to validFilterValues array
         result.push(valObj);
     }else{
         // here we also push filter option that is not present in filterTagOptions[key] (i.e, options list) to validFilterValues array
         // but here, we take id and displayname to be same
         // this case is to handle when some filter is applied while navigating from other screen and if that filter option is not in the list.
         result.push({id: val, name:val});
     }
     return result;
     }, []);
 
     return validFilterValues;
   }
 
    handleError(error: any): Observable<any> {
        return observableThrowError(error.message || error);
    }

    massageData(data): any {
        return data;
    }

   hasFilterQueryParam () {
     return this.activatedRoute.snapshot.queryParamMap.has("filter");
   }

   getUpdatedUrl (filters: any[]) {
     const filterText = this.utils.arrayToObject(filters, 'filterkey', 'value');
     const updatedFilterText = this.utils.makeFilterObj(filterText);

     const updatedQueryParams = {
       filter: updatedFilterText['filter'],
     };

     const processedFilterText = this.utils.processFilterObj(updatedFilterText);

     this.router.navigate([], {
       relativeTo: this.activatedRoute,
       queryParams: updatedQueryParams,
       queryParamsHandling: 'merge',
     });

     // Use processedFilterText as needed
     return processedFilterText;
   }

   applyPreservedFilters (state) {
     let shouldUpdateFilters = false;
     let shouldUpdateData = false;
     let preApply = false;
     let filterText;     
     if (!this.hasFilterQueryParam()) {
       const navDirection = this.workflowService.getNavigationDirection();
       if (navDirection <= 0) {
         shouldUpdateFilters = true;
         if (navDirection == 0) {
           preApply = true;
         } else {
           if (state.data && state.data.length > 0) {
             shouldUpdateData = true;
           }
         }
         filterText = this.getUpdatedUrl(state.filters);
       }
     }
     return {
       shouldUpdateFilters, shouldUpdateData, filterText, preApply
     };
   }

   getRangeFilterOptions (filterTagsData, isRangePercentage = false) {
     const numOfIntervals = 5;
     const { min, max } = filterTagsData.optionRange;
     const intervals = this.utils.generateIntervals(min, max, numOfIntervals);

     filterTagsData = [];
     let includeLastOption = false;
     intervals.forEach(interval => {
       const lb = Math.round(interval.lowerBound);
       let up = Math.round(interval.upperBound);
       includeLastOption = up === 100;
       if (isRangePercentage && up === 100 && lb !== up) {
         up--;
       }
       if (!(isRangePercentage && lb===100)) {
         filterTagsData.push({ id: `${lb}-${up}`, name: `${lb}-${up}` });
       }
     });
     if (isRangePercentage && includeLastOption) {
       filterTagsData.push({ id: "100-100", name: "100-100" });
     }
     return filterTagsData;
   }
 
 }
 