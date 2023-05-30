import { ComponentFixture, TestBed } from '@angular/core/testing';

import { TableOptionsComponent } from './table-options.component';

describe('TableOptionsComponent', () => {
  let component: TableOptionsComponent;
  let fixture: ComponentFixture<TableOptionsComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ TableOptionsComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(TableOptionsComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
