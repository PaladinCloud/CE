 import { Injectable, Inject } from '@angular/core';
 import { HttpService } from '../../shared/services/http-response.service';
 import { ErrorHandlingService } from '../../shared/services/error-handling.service';
 import {environment} from '../../../environments/environment';
import { Observable, ReplaySubject, Subject } from 'rxjs';
 
@Injectable({
  providedIn: 'root'
})
 export class AssetTypeMapService{

  private assetTypeMapSubject = new ReplaySubject<Map<string, string>>(0);

    constructor(
        @Inject(HttpService) private httpService: HttpService,
                private errorHandlingService: ErrorHandlingService
    ){
    }
    
    fetchAssetTypes()
      {
        const url = environment.getAllAssetTypes.url;
        const method = environment.getAllAssetTypes.method;
        const payload = {};
        try {
          this.httpService.getHttpResponse(url, method, payload)
          .subscribe(response =>
            this.mapData(response))
        } catch (error) {
            this.errorHandlingService.handleJavascriptError(error);
        }
    }

    private mapData(assetTypesData){
      const assetTypeList = assetTypesData.targettypes;
      const assetTypeMap = new Map<string, string>();
      assetTypeList.forEach(item => {
        assetTypeMap.set(item.type, item.displayName);
      });
      this.assetTypeMapSubject.next(assetTypeMap);
    }

    getAssetMap():Observable<any>{
      return this.assetTypeMapSubject.asObservable();
    }

  }