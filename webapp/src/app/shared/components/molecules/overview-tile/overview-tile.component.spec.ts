import { ComponentFixture, TestBed } from '@angular/core/testing';
import { TextComponent } from '../../atoms/text/text.component';
import { TextWithIconComponent } from '../text-with-icon/text-with-icon.component';

import { OverviewTileComponent } from './overview-tile.component';

describe('OverviewTileComponent', () => {
  let component: OverviewTileComponent;
  let fixture: ComponentFixture<OverviewTileComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ OverviewTileComponent, TextWithIconComponent, TextComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(OverviewTileComponent);
    component = fixture.componentInstance;
    component.tile = {
      mainContent: {
        title: 'AdminTitle',
        image: 'admin',
        count: 1,
      },
      subcontent: [
        {
          title: 'subtitle',
          count: 2
        }
      ]
    }
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
