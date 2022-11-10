import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { CONFIGURATIONS } from './../../../config/configurations';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { WindowRefService } from 'src/app/pacman-features/services/window.service';
import { HttpService } from 'src/app/shared/services/http-response.service';
import { Router } from '@angular/router';

@Injectable({
  providedIn: 'root'
})
export class AwsCognitoService {

  constructor(private http: HttpClient,
    private httpService: HttpService,
    private router: Router) { }

  public getTokenDetailsFromCognito(callbackCode: string): Observable<any> {
    const details = {
      grant_type: 'authorization_code',
      code: callbackCode,
      scope: 'openid+profile',
      redirect_uri: CONFIGURATIONS.optional.auth.cognitoConfig.redirectURL
    };
    console.log('Inside AWS cognito service');

    //window.alert('Inside AWS cognito service, callBack code: '+ callbackCode);
    const formBody = Object.keys(details)
      .map(key => `${encodeURIComponent(key)}=${encodeURIComponent(details[key])}`)
      .join('&');
    console.log('formBody', formBody);

    const response = this.http.post(CONFIGURATIONS.optional.auth.cognitoConfig.cognitoTokenURL,
      formBody,
      {
        responseType: 'json',
        headers: new HttpHeaders({
          'Content-Type': 'application/x-www-form-urlencoded',
          Authorization: 'Basic ' + btoa(`${CONFIGURATIONS.optional.auth.cognitoConfig.sso_api_username}:${CONFIGURATIONS.optional.auth.cognitoConfig.sso_api_pwd}`)
        })
      }
    );
    response.subscribe(data => {
      console.log('Response data', data);
      this.router.navigate(['/pl/compliance/issue-listing']);
    })
    console.log('Type of response', typeof response);
    console.log('Token response', response);
    return response;
    // return this.http.post<any>(CONFIGURATIONS.optional.auth.cognitoConfig.cognitoTokenURL,
    //   formBody, {
    //   responseType: 'json',
    //   headers: new HttpHeaders({
    //     'Content-Type': 'application/x-www-form-urlencoded',
    //     Authorization: 'Basic ' + btoa(`${CONFIGURATIONS.optional.auth.cognitoConfig.sso_api_username}:${CONFIGURATIONS.optional.auth.cognitoConfig.sso_api_pwd}`)
    //   })
    // });
  }

  public logoutUserFromCognito(): Observable<any> {
    return this.http.get<any>(CONFIGURATIONS.optional.auth.cognitoConfig.logout);
  }
}
