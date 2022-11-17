import { ComponentFixture, TestBed } from '@angular/core/testing';

import { MultilineGraphWithHeaderComponent } from './multiline-graph-with-header.component';

describe('MultilineGraphWithHeaderComponent', () => {
  let component: MultilineGraphWithHeaderComponent;
  let fixture: ComponentFixture<MultilineGraphWithHeaderComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ MultilineGraphWithHeaderComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(MultilineGraphWithHeaderComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
