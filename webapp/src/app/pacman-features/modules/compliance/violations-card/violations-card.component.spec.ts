import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { ViolationsCardComponent } from './violations-card.component';

describe('ViolationsCardComponent', () => {
  let component: ViolationsCardComponent;
  let fixture: ComponentFixture<ViolationsCardComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ ViolationsCardComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(ViolationsCardComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
