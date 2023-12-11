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
import { of } from 'rxjs';
import { catchError, map } from 'rxjs/operators';
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
     constructor (private assetTilesService: AssetTilesService) {
     }
 
     ngOnChanges() {
       this.createProviderArray();
     }
 
     capitalizeFirstLetter(string): any {
       return string.charAt(0).toUpperCase() + string.slice(1);
     }
 
     getAssetGroupDisplayName(ag: string): Promise<string> {
      return this.assetTilesService.getAssetGroupDisplayName(ag)
        .pipe(map((agDisplayName: string) => agDisplayName ?? ag),
          catchError((error) => {
            console.error('Error occurred: ', error);
            return of(ag);
          })
        )
        .toPromise();
      }

    async createProviderArray() {
      this.provider = [];      
      if (this.detailsVal && this.detailsVal.providers) {
        const providers = await Promise.all(this.detailsVal.providers.map((element) => this.getAssetGroupDisplayName(element.provider)));
        this.provider = providers.sort((a,b) => a.localeCompare(b));
      }
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
