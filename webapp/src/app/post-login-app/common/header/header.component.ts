import { Component, OnDestroy, OnInit, TemplateRef, ViewChild } from '@angular/core';
import { MatIconRegistry } from '@angular/material/icon';
import { DomSanitizer } from '@angular/platform-browser';
import { ActivatedRoute, Router } from '@angular/router';
import { Apollo } from 'apollo-angular';
import { Subject } from 'rxjs';
import { takeUntil } from 'rxjs/operators';
import { NOTIFICATIONS_SUBSCRIPTION } from 'src/app/core/graphql/subscriptions/notifications.gql';
import { AuthService } from "../../../core/services/auth.service";
import { DataCacheService } from "../../../core/services/data-cache.service";
import { PermissionGuardService } from "../../../core/services/permission-guard.service";
import { LoggerService } from "../../../shared/services/logger.service";
import { environment } from "../../../../environments/environment";
import { CommonResponseService } from "src/app/shared/services/common-response.service";
import { MatDialog } from "@angular/material/dialog";
import { TourService } from 'src/app/core/services/tour.service';

@Component({
    selector: 'app-header',
    templateUrl: './header.component.html',
    styleUrls: ['./header.component.css'],
})
export class HeaderComponent implements OnInit, OnDestroy {

    currentVersion = '';
    showUserInfo = false;
    haveAdminPageAccess = false;
    userName: string;
    userEmail: string;
    userType;
    profilePictureSrc: any = '/assets/icons/profile-picture.svg';
    queryParams;
    isUserMenuOpen = false;
    searchQuery;

    haveNewNotification = false;
    private readonly NOTIFICATIONS_CHANNEL = 'InAppNotification';
    private destroy$ = new Subject<void>();

    constructor(
        private apollo: Apollo,
        private commonResponseService: CommonResponseService,
        private authenticateService: AuthService,
        private router: Router,
        private route: ActivatedRoute,
        private dataCacheService: DataCacheService,
        private permissions: PermissionGuardService,
        private loggerService: LoggerService,
        private matIconRegistry: MatIconRegistry,
        private domSanitizer: DomSanitizer,
        private tourService: TourService,
        ) {
        const userRoles = this.dataCacheService.getUserDetailsValue().getRoles();
        this.tourService.init(userRoles);
        this.matIconRegistry.addSvgIcon(
            `customSearchIcon`,
            this.domSanitizer.bypassSecurityTrustResourceUrl(
                '/assets/icons/header-search.svg',
            ),
        );

        const url = environment.getCurrentVersion.url;
        const urlMethod = environment.getCurrentVersion.method;
        const queryParam = {
            'cfkey': 'current-release',
        };
        this.commonResponseService.getData(url, urlMethod, '', queryParam).subscribe(
            response => {
                this.currentVersion = response[0].value;
            },
        );
    }

    ngOnInit() {
        try {
            this.haveAdminPageAccess = this.permissions.checkAdminPermission();
            this.userType = this.haveAdminPageAccess ? 'Admin' : '';
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

            this.route.queryParams.subscribe((params) => {
                this.queryParams = params;
                this.searchQuery = params["searchText"]
            });
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

    showTour(){
        this.tourService.start();
    }

    ngOnDestroy(): void {
        this.destroy$.next();
        this.destroy$.complete();
    }

    navigateTo(pageName) {
        if (pageName == 'health-notifications') {
            this.router
                .navigate(['/pl/notifications/notifications-list'], {
                    queryParams: this.queryParams,
                });
        }
    }

    openNotification() {
        if (this.haveNewNotification) {
            this.haveNewNotification = false;
        }
        this.router.navigate(['pl/notifications/notifications-list'], {
            queryParams: {...this.route.snapshot.queryParams, tempFilters: true, filter: undefined},
        });
    }

    handleSearch(event) {
        let searchTxt = event.target.value;
        this.searchQuery = searchTxt;

        if (event.keyCode === 13) {
            const queryParams = {
                ...this.queryParams,
                searchText: searchTxt
            }
            this.router
                .navigate(['/pl/omnisearch/omni-search-details'], {
                    queryParams: queryParams,
                })
                .then((response) => {
                    // Clearig page levels.
                });
        }
    }
  logout() {
      this.authenticateService.doLogout();
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
