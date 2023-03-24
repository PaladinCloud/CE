import { HttpClientTestingModule } from '@angular/common/http/testing';
import { ComponentFixture, fakeAsync, flush, TestBed, tick } from '@angular/core/testing';
import { FormsModule } from '@angular/forms';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { By } from '@angular/platform-browser';
import { NoopAnimationsModule } from '@angular/platform-browser/animations';
import { provideRoutes, Router } from '@angular/router';
import { RouterTestingModule } from '@angular/router/testing';
import { ApolloTestingController, ApolloTestingModule } from 'apollo-angular/testing';
import { NOTIFICATIONS_SUBSCRIPTION } from 'src/app/core/graphql/subscriptions/notifications.gql';
import { AdalService } from 'src/app/core/services/adal.service';
import { AssetGroupObservableService } from 'src/app/core/services/asset-group-observable.service';
import { AuthSessionStorageService } from 'src/app/core/services/auth-session-storage.service';
import { AuthService } from 'src/app/core/services/auth.service';
import { DataCacheService } from 'src/app/core/services/data-cache.service';
import { OnPremAuthenticationService } from 'src/app/core/services/onprem-authentication.service';
import { PermissionGuardService } from 'src/app/core/services/permission-guard.service';
import { TokenResolverService } from 'src/app/resolver/token-resolver.service';
import { CommonResponseService } from 'src/app/shared/services/common-response.service';
import { ErrorHandlingService } from 'src/app/shared/services/error-handling.service';
import { HttpService } from 'src/app/shared/services/http-response.service';
import { LoggerService } from 'src/app/shared/services/logger.service';
import { RefactorFieldsService } from 'src/app/shared/services/refactor-fields.service';
import { UtilsService } from 'src/app/shared/services/utils.service';
import { HeaderComponent } from './header.component';

describe('HeaderComponent', () => {
    let fixture: ComponentFixture<HeaderComponent>;
    let controller: ApolloTestingController;
    let router: Router;
    let fakeDataCacheService: any;
    let fakeAuthService: Pick<AuthService, 'doLogout'>;

    beforeEach(async () => {
        fakeAuthService = {
            doLogout() {},
        };
        fakeDataCacheService = {
            clearAll() {},
            isAdminCapability() {
                return true;
            },
            getUserDetailsValue() {
                return {
                    getFirstName() {
                        return 'John';
                    },
                    getEmail() {
                        return 'john.doe@example.com';
                    },
                };
            },
        };
        await TestBed.configureTestingModule({
            declarations: [HeaderComponent],
            imports: [
                ApolloTestingModule,
                FormsModule,
                HttpClientTestingModule,
                MatFormFieldModule,
                MatIconModule,
                MatInputModule,
                NoopAnimationsModule,
                RouterTestingModule,
            ],
            providers: [
                provideRoutes([
                    {
                        path: '**',
                        component: HeaderComponent,
                    },
                ]),
                AdalService,
                AssetGroupObservableService,
                {
                    provide: AuthService,
                    useValue: fakeAuthService,
                },
                AuthSessionStorageService,
                CommonResponseService,
                {
                    provide: DataCacheService,
                    useValue: fakeDataCacheService,
                },
                ErrorHandlingService,
                HttpService,
                LoggerService,
                OnPremAuthenticationService,
                PermissionGuardService,
                RefactorFieldsService,
                TokenResolverService,
                UtilsService,
            ],
        }).compileComponents();

        controller = TestBed.inject(ApolloTestingController);
        fixture = TestBed.createComponent(HeaderComponent);
        router = TestBed.inject(Router);
        fixture.detectChanges();
    });

    it('creates the header', () => {
        const header = fixture.debugElement.componentInstance;

        expect(header).toBeTruthy();
    });

    it('contains paladincloud logo', () => {
        const header = fixture.debugElement;
        const logoImg = header.query(By.css('.header-company-logo'));

        expect(logoImg).toBeTruthy();
        expect(logoImg.attributes.src).toEqual('/assets/images/Paladin_Logo.svg');
    });

    it('contains search field and input in it', () => {
        const { debugElement } = fixture;
        const searchField = debugElement.query(By.css('.header-search-full-width'));
        const searchInput = debugElement.query(By.css('.header-search-full-width input'));

        expect(searchField).toBeTruthy();
        expect(searchInput).toBeTruthy();
    });

    it('navigates to omnisearch upon search click', fakeAsync(() => {
        const { debugElement } = fixture;
        const searchWrapper = debugElement.query(By.css('.header-search-wrapper'));
        const spy = spyOn(debugElement.componentInstance, 'handleSearch').and.callThrough();

        searchWrapper.triggerEventHandler('click', null);
        tick();

        expect(spy).toHaveBeenCalledTimes(1);
        expect(router.url)
            .withContext('navigates to omnisearch')
            .toEqual('/pl/omnisearch/omni-search-page');

        flush();
    }));

    it('contains username and user email', () => {
        const { componentInstance, debugElement } = fixture;
        const userNameEl = debugElement.query(By.css('.user-name'));
        const userEmailEl = debugElement.query(By.css('.user-email'));

        expect(componentInstance.userName).toEqual('john');
        expect(componentInstance.userEmail).toEqual('john.doe@example.com');

        expect((userNameEl.nativeElement as HTMLDivElement).textContent).toEqual('john');
        expect(userEmailEl.attributes.title).toEqual('john.doe@example.com');
        expect((userEmailEl.nativeElement as HTMLDivElement).textContent).toEqual(
            'john.doe@example.com',
        );
    });

    it('contains user image as email first letter', () => {
        const { debugElement } = fixture;
        const userImage = debugElement.query(By.css('.user-image-wrapper > h3'));

        expect((userImage.nativeElement as HTMLElement).textContent).toEqual('j');
    });

    it('logouts upon logout icon wrapper click', () => {
        const { debugElement } = fixture;
        const logoutEl = debugElement.query(By.css('.user-logout-modal-btn'));
        const spy = spyOn(logoutEl.componentInstance, 'logout').and.callThrough();

        logoutEl.triggerEventHandler('click', null);

        expect(spy).toHaveBeenCalled();
    });

    describe('Apollo notifications', () => {
        afterEach(() => {
            controller.verify();
        });

        it('contains notification bell without dot', () => {
            const { debugElement } = fixture;
            const notificationEl = debugElement.query(By.css('.header-user-notification'));

            expect(notificationEl).toBeDefined();
            expect(notificationEl.classes.active).toBeFalsy();

            const op = controller.expectOne(NOTIFICATIONS_SUBSCRIPTION);
            op.flush(null);
        });

        it('renders red dot on the bell icon when received notification', () => {
            const { debugElement } = fixture;
            const notificationEl = debugElement.query(By.css('.header-user-notification'));

            expect(notificationEl.classes.active).toBeFalsy();

            const op = controller.expectOne(NOTIFICATIONS_SUBSCRIPTION);

            expect(op.operation.variables.name).toEqual('InAppNotification');

            op.flush({});

            fixture.detectChanges();

            expect(notificationEl.classes.active).toBeTruthy();
        });

        it('removes red dot icon and navigates to health notifications upon bell click', fakeAsync(() => {
            const { debugElement } = fixture;
            const notificationEl = debugElement.query(By.css('.header-user-notification'));
            const spy = spyOn(debugElement.componentInstance, 'openNotification').and.callThrough();

            expect(notificationEl.classes.active).toBeFalsy();

            const op = controller.expectOne(NOTIFICATIONS_SUBSCRIPTION);

            op.flush({});

            fixture.detectChanges();

            expect(notificationEl.classes.active).toBeTruthy();

            notificationEl.triggerEventHandler('click', null);

            expect(spy).toHaveBeenCalled();

            fixture.detectChanges();

            expect(notificationEl.classes.active).toBeFalsy();

            tick();
            expect(router.url)
                .withContext('navigates to health notifications')
                .toEqual('/pl/compliance/health-notifications');

            flush();
        }));
    });
});
