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

 import { Component, Input, EventEmitter, Output, OnChanges } from '@angular/core';
 import { AssetTilesService } from '../../../core/services/asset-tiles.service';
 
 @Component({
   selector: 'app-asset-group-details',
   templateUrl: './asset-group-details.component.html',
   styleUrls: ['./asset-group-details.component.css'],
   providers: [AssetTilesService]
 })
 
 export class AssetGroupDetailsComponent implements OnChanges {
 
     @Input() selectedValue: any;
     @Input() detailsVal: any = {};
     @Input() assetDetailsState = 0;
     accounts = 0;
 
     public errorMessage: any;
     @Output() navigatePage: EventEmitter<any> = new EventEmitter();
     provider = [];
     constructor () {
     }
 
     ngOnChanges() {
       this.createProviderArray();
     }
 
     capitalizeFirstLetter(string): any {
       return string.charAt(0).toUpperCase() + string.slice(1);
     }
 
    createProviderArray() {
      this.provider = [];
      const order = ["AWS", "Azure", "GCP"];
      const providerMap = {
        "aws": "AWS",
        "azure": "Azure",
        "gcp": "GCP"
      }
      let curr_provider = "";
      if (this.detailsVal && this.detailsVal.providers) {
        this.detailsVal.providers.forEach(element => {
          curr_provider = providerMap[element.provider.toLowerCase()];
          if(curr_provider)
          this.provider.push(curr_provider);
          else
          this.provider.push(element.provider);
        });
      }

      this.provider.sort();
    }

    instructParentToNavigate (data, agDetails) {
      const obj = {
        data: data,
        agDetails: agDetails
      };
      this.navigatePage.emit(obj);
   }

   getDisplayName(assertName:string){
     return assertName.replace(/,/g, " ");
   }

}
