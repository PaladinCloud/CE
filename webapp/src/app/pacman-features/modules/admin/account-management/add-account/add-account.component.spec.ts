import { ComponentFixture, TestBed } from '@angular/core/testing';

import { AddAccountComponent } from './add-account.component';

describe('AddAccountComponent', () => {
  let component: AddAccountComponent;
  let fixture: ComponentFixture<AddAccountComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ AddAccountComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(AddAccountComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
