import { HttpClientTestingModule } from '@angular/common/http/testing';
import { async, ComponentFixture, TestBed } from '@angular/core/testing';
import { RouterTestingModule } from '@angular/router/testing';
import { DataCacheService } from 'src/app/core/services/data-cache.service';
import { WorkflowService } from 'src/app/core/services/workflow.service';
import { ToastObservableService } from 'src/app/post-login-app/common/services/toast-observable.service';
import { CommonResponseService } from 'src/app/shared/services/common-response.service';
import { ErrorHandlingService } from 'src/app/shared/services/error-handling.service';
import { FormService } from 'src/app/shared/services/form.service';
import { HttpService } from 'src/app/shared/services/http-response.service';
import { LoggerService } from 'src/app/shared/services/logger.service';
import { RefactorFieldsService } from 'src/app/shared/services/refactor-fields.service';
import { RouterUtilityService } from 'src/app/shared/services/router-utility.service';
import { ToastObservableService as SharedToastObservableService  } from 'src/app/shared/services/toast-observable.service';
import { UtilsService } from 'src/app/shared/services/utils.service';
import { ToastNotificationComponent } from 'src/app/shared/toast-notification/toast-notification.component';

import { ConfigManagementComponent } from './config-management.component';

describe('ConfigManagementComponent', () => {
  let component: ConfigManagementComponent;
  let fixture: ComponentFixture<ConfigManagementComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule, RouterTestingModule],
      declarations: [ ConfigManagementComponent, ToastNotificationComponent ],
      providers: [
        CommonResponseService,
        DataCacheService,
        ErrorHandlingService,
        FormService,
        HttpService,
        LoggerService,
        UtilsService,
        RefactorFieldsService,
        RouterUtilityService,
        SharedToastObservableService,
        ToastObservableService,
        WorkflowService,
      ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(ConfigManagementComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
