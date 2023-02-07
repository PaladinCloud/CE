import { async, ComponentFixture, TestBed } from '@angular/core/testing';
import { OverlayComponent } from '../overlay/overlay.component';

import { ConfirmationBoxComponent } from './confirmation-box.component';

describe('ConfirmationBoxComponent', () => {
  let component: ConfirmationBoxComponent;
  let fixture: ComponentFixture<ConfirmationBoxComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ ConfirmationBoxComponent, OverlayComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(ConfirmationBoxComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
