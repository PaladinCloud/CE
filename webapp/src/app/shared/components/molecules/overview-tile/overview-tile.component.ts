import { Component, EventEmitter, Input, OnInit, Output } from '@angular/core';

@Component({
  selector: 'app-overview-tile',
  templateUrl: './overview-tile.component.html',
  styleUrls: ['./overview-tile.component.css']
})
export class OverviewTileComponent implements OnInit {

  @Input() tile;
  @Input() showIcon = true;

  @Output() navigateTo = new EventEmitter<any>();

  constructor() { }

  ngOnInit(): void {
    
  }

  redirectTo(tile){
    this.navigateTo.emit(tile);
  }

}