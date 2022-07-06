import { async, ComponentFixture, TestBed } from "@angular/core/testing";

import { HorizontalBarComponent } from "./horizontal-bar-chart.component";

describe("HorizontalBarComponent", () => {
  let component: HorizontalBarComponent;
  let fixture: ComponentFixture<HorizontalBarComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [HorizontalBarComponent],
    }).compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(HorizontalBarComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it("should create", () => {
    expect(component).toBeTruthy();
  });
});
