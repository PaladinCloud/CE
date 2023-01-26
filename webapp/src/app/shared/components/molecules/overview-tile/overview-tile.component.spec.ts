import { ComponentFixture, TestBed } from '@angular/core/testing';

import { OverviewTileComponent } from './overview-tile.component';

describe('OverviewTileComponent', () => {
  let component: OverviewTileComponent;
  let fixture: ComponentFixture<OverviewTileComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ OverviewTileComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(OverviewTileComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
