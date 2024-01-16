import { Injectable, Inject } from '@angular/core';
import { HttpService } from '../../shared/services/http-response.service';
import { ErrorHandlingService } from '../../shared/services/error-handling.service';
import {environment} from '../../../environments/environment';
import { Observable, ReplaySubject} from 'rxjs';
import { CommonResponseService } from 'src/app/shared/services/common-response.service';
import { LoggerService } from 'src/app/shared/services/logger.service';
import { API_RESPONSE_ERROR } from 'src/app/shared/constants/global';

@Injectable({
  providedIn: 'root'
})
 export class AssetTypeMapService{
  private assetTypeMapSubject = new ReplaySubject<Map<string, string>>(0);
  private assetTypesForAgSubject = new ReplaySubject<Map<string, string>>(0);

  AssetTypesDataForAg
  constructor(
    @Inject(HttpService) private httpService: HttpService,
    private errorHandlingService: ErrorHandlingService,
    private commonResponseService: CommonResponseService,
    private logger: LoggerService,
  ){}

  fetchAssetTypes()
    {
      const url = environment.getAllAssetTypes.url;
      const method = environment.getAllAssetTypes.method;
      try {
        this.httpService.getHttpResponse(url, method, {})
        .subscribe(
          response => this.assetTypeMapSubject.next(this.mapData(response)),
          error => {
            this.assetTypesForAgSubject.next();
            this.logger.log(API_RESPONSE_ERROR, error)
          }
        )
      } catch (error) {
          this.errorHandlingService.handleJavascriptError(error);
      }
  }

  fetchAssetTypesForAg(ag:string) {
    try {
      const { url, method } = environment.targetType;
      this.commonResponseService.getData( url, method, {}, {ag})
        .subscribe(
          response => this.assetTypesForAgSubject.next(this.mapData(response)),
          error => {
            this.assetTypesForAgSubject.next();
            this.logger.log(API_RESPONSE_ERROR, error)
          }
        )
    } catch (error) {
      this.errorHandlingService.handleJavascriptError(error);
    }
  }

  private mapData(data){
    const {targettypes: types} = data;
    const assetTypeMap = new Map<string, string>();
    types.forEach(item => assetTypeMap.set(item.type, item.displayName));
    return assetTypeMap;
  }

  getAssetMap():Observable<Map<string, string>>{
    return this.assetTypeMapSubject.asObservable();
  }

  getAssetTypeMapForGroup():Observable<Map<string, string>>{
    return this.assetTypesForAgSubject.asObservable();
  }
}