import { Injectable } from '@angular/core';
import { Resolve, Router } from '@angular/router';
import { Observable, of } from 'rxjs';
import { switchMap, finalize, catchError } from 'rxjs/operators';
import { Location } from '@angular/common';
import { AwsCognitoService } from './../core/services/aws-cognito.service';


@Injectable()
export class TokenResolverService implements Resolve<any> {

  i = 0;
  constructor(private location: Location,
    private awsCognitoService: AwsCognitoService,
    private router: Router) { }

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
    //window.alert('Inside AWS cognito service, callBack code: ' + code);
    // const responseData=this.awsCognitoService.getTokenDetailsFromCognito(code).subscribe(response => {

    //   console.log('Response from resolver service', response);
    //   localStorage.setItem('Resolver service token', response.access_token);
    //   console.log('Resolver service Type of response', typeof response);
    //   console.log('ResolverService Token response', response);
    //   return response;
    // });
    if (this.i < 1) {
      return this.awsCognitoService.getTokenDetailsFromCognito(code).pipe(
        switchMap((response: any) => {
          this.i++;
          console.log('Response: ', response);
          window.alert('Response: ' + response);
          localStorage.setItem('token', response.access_token);

          if (response) {
            this.router.navigate(['/pl']);
          }

          return of(response);
        }),
        catchError((error) => {
          return error;
        })
      );
    }
  }
}
