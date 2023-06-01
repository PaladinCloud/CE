import { ComponentFixture, TestBed } from '@angular/core/testing';

import { StatisticsTileComponent } from './statistics-tile.component';

describe('StatisticsTileComponent', () => {
  let component: StatisticsTileComponent;
  let fixture: ComponentFixture<StatisticsTileComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ StatisticsTileComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(StatisticsTileComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
