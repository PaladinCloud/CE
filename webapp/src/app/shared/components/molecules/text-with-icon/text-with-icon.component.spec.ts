import { ComponentFixture, TestBed } from '@angular/core/testing';

import { TextWithIconComponent } from './text-with-icon.component';

describe('TextWithIconComponent', () => {
  let component: TextWithIconComponent;
  let fixture: ComponentFixture<TextWithIconComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ TextWithIconComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(TextWithIconComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
