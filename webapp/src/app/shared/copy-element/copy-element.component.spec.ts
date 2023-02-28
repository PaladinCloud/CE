import { async, ComponentFixture, TestBed } from '@angular/core/testing';
import { CopyElementService } from '../services/copy-element.service';
import { ErrorHandlingService } from '../services/error-handling.service';
import { LoggerService } from '../services/logger.service';
import { RefactorFieldsService } from '../services/refactor-fields.service';
import { ToastObservableService } from '../services/toast-observable.service';
import { UtilsService } from '../services/utils.service';

import { CopyElementComponent } from './copy-element.component';

describe('CopyElementComponent', () => {
  let component: CopyElementComponent;
  let fixture: ComponentFixture<CopyElementComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ CopyElementComponent ],
      providers: [
        CopyElementService,
        ErrorHandlingService,
        LoggerService,
        RefactorFieldsService,
        ToastObservableService,
        UtilsService,
      ],
    }).compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(CopyElementComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
