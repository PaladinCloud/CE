import { async, ComponentFixture, TestBed } from '@angular/core/testing';
import { SharedModule } from '../shared.module';

import { FilterDropdownComponent } from './filter-dropdown.component';

describe('FilterDropdownComponent', () => {
  let component: FilterDropdownComponent;
  let fixture: ComponentFixture<FilterDropdownComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      imports: [ SharedModule ],
      declarations: [ FilterDropdownComponent ],
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(FilterDropdownComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
