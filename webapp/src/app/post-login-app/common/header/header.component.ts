import { Component, OnDestroy, OnInit } from '@angular/core';
import { MatIconRegistry } from '@angular/material/icon';
import { DomSanitizer } from '@angular/platform-browser';
import { ActivatedRoute, Router } from '@angular/router';
import { Apollo } from 'apollo-angular';
import { Subject } from 'rxjs';
import { takeUntil } from 'rxjs/operators';
import { NOTIFICATIONS_SUBSCRIPTION } from 'src/app/core/graphql/subscriptions/notifications.gql';
import { AuthService } from '../../../core/services/auth.service';
import { DataCacheService } from '../../../core/services/data-cache.service';
import { PermissionGuardService } from '../../../core/services/permission-guard.service';
import { LoggerService } from '../../../shared/services/logger.service';

@Component({
    selector: 'app-header',
    templateUrl: './header.component.html',
    styleUrls: ['./header.component.css'],
})
export class HeaderComponent implements OnInit, OnDestroy {
    showUserInfo = false;
    haveAdminPageAccess = false;
    userName: string;
    userEmail: string;
    profilePictureSrc = '/assets/icons/profile-picture.svg';

    haveNewNotification = false;

    private readonly NOTIFICATIONS_CHANNEL = 'InAppNotification';
    private destroy$ = new Subject<void>();

    constructor(
        private apollo: Apollo,
        private authenticateService: AuthService,
        private dataCacheService: DataCacheService,
        private domSanitizer: DomSanitizer,
        private loggerService: LoggerService,
        private matIconRegistry: MatIconRegistry,
        private permissions: PermissionGuardService,
        private route: ActivatedRoute,
        private router: Router,
    ) {
        this.matIconRegistry.addSvgIcon(
            'customSearchIcon',
            this.domSanitizer.bypassSecurityTrustResourceUrl('/assets/icons/header-search.svg'),
        );
    }

    ngOnInit() {
        try {
            this.haveAdminPageAccess = this.permissions.checkAdminPermission();
            this.userName = 'Guest';
            this.userEmail = 'Guest';
            const detailsData = this.dataCacheService.getUserDetailsValue();
            const userNameData = detailsData.getFirstName();
            const emailData = detailsData.getEmail();
            if (userNameData) {
                this.userName = userNameData;
            }
            if (emailData) {
                this.userEmail = emailData;
                this.userName = this.userEmail.split('@')[0].split('.')[0];
            }

            this.getProfilePictureOfUser();
        } catch (error) {
            this.loggerService.log('error', 'JS Error' + error);
        }

        this.apollo
            .subscribe({
                query: NOTIFICATIONS_SUBSCRIPTION,
                variables: {
                    name: this.NOTIFICATIONS_CHANNEL,
                },
            })
            .pipe(takeUntil(this.destroy$))
            .subscribe({
                next: () => (this.haveNewNotification = true),
                error: (errors: Error[]) =>
                    this.loggerService.log('error', 'GraphQL error: ' + JSON.stringify(errors)),
            });
    }

    ngOnDestroy(): void {
        this.destroy$.next();
        this.destroy$.complete();
    }

    handleSearch() {
        this.router.navigate(['/pl/omnisearch/omni-search-page'], {
            queryParams: this.route.snapshot.queryParams,
        });
    }

    getProfilePictureOfUser() {
        // Get profile picture of user from azure ad.
        // this.adalService.acquireToken(CONFIGURATIONS.optional.auth.resource).subscribe(token => {
        //     const api = environment.fetchProfilePic.url;
        //     const httpMethod = environment.fetchProfilePic.method;
        //     const header = new HttpHeaders();
        //     const updatedHeader = header.append('Authorization', 'Bearer ' + token);
        //     this.httpResponseService.getBlobHttpResponse(api, httpMethod, {}, {}, {headers: updatedHeader}).subscribe(response => {
        //         this.utilService.generateBase64String(response).subscribe(image => {
        //             this.loggerService.log('info', 'user profile pic received');
        //             this.dataCacheService.setUserProfileImage(image);
        //             this.profilePictureSrc = image;
        //         });
        //     },
        //     error => {
        //         this.loggerService.log('error', 'error while fetching image from azure ad - ' + error);
        //     });
        // }, error => {
        //     this.loggerService.log('error', 'Error while fetching access token for resource - ' + error);
        // });
    }

    logout() {
        this.authenticateService.doLogout();
    }

    openNotification() {
        if (this.haveNewNotification) {
            this.haveNewNotification = false;
        }
        this.router.navigate(['pl/notifications/notifications-list'], {
            queryParams: this.route.snapshot.queryParams,
        });
    }

    openDashboard() {
        const queryParams = this.route.snapshot.queryParams;
        this.router.navigate(['pl/compliance/compliance-dashboard'], {
            queryParams: {
                ag: queryParams.ag,
                domain: queryParams.domain,
            },
        });
    }
}
