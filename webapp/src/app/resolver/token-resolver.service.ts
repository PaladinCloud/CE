import { Injectable } from '@angular/core';
import { Resolve } from '@angular/router';
import { Observable, of } from 'rxjs';
import { switchMap, finalize, catchError } from 'rxjs/operators';
import { Location } from '@angular/common';
import { AwsCognitoService } from './../core/services/aws-cognito.service';
import { DataCacheService } from '../core/services/data-cache.service';


@Injectable()
export class TokenResolverService implements Resolve<any> {

  constructor(private location: Location,
    private awsCognitoService: AwsCognitoService,
    private dataCacheService: DataCacheService) { }

  resolve(): Observable<any | null> {

    const urlParams: URLSearchParams = new URLSearchParams(window.location.search);
    const code: string = urlParams.get('code');

    if (!code) {
      return of(null);
    }

    return this.getTokenDetailsFromCognito(code).pipe(
      finalize(() => {
        this.location.replaceState(window.location.pathname);
      })
    );
  }

  getTokenDetailsFromCognito(code: string): Observable<any | null> {
    console.log('Fething token from cognito, code is', code);
    return this.awsCognitoService.getTokenDetailsFromCognito(code).pipe(
      switchMap((response: any) => {

        let currentUserLoginDetails =
        {
          "access_token": response.access_token,
          "refresh_token": response.refresh_token,
          "id_token": response.id_token,
          "success": true,
          "token_type": response.token_type,
          "expires_in": response.expires_in
        }

        this.dataCacheService.setCurrentUserLoginDetails(JSON.stringify(currentUserLoginDetails));
        if (response) {
          console.log('Recieved response in token resolver');
        }

        return of(response);
      }),
      catchError((error) => {
        return error;
      })
    );

  }
}
