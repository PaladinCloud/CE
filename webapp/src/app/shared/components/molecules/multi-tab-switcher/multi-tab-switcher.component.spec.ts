import { ComponentFixture, TestBed } from '@angular/core/testing';

import { MultiTabSwitcherComponent } from './multi-tab-switcher.component';

describe('MultiTabSwitcherComponent', () => {
  let component: MultiTabSwitcherComponent;
  let fixture: ComponentFixture<MultiTabSwitcherComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ MultiTabSwitcherComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(MultiTabSwitcherComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
