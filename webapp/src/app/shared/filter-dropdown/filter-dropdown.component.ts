import { Component, EventEmitter, Input, OnInit, Output } from '@angular/core';

@Component({
  selector: 'app-filter-dropdown',
  templateUrl: './filter-dropdown.component.html',
  styleUrls: ['./filter-dropdown.component.css']
})
export class FilterDropdownComponent implements OnInit {

  @Input() filterTypeLabels = [];
  @Input() filterTagLabels = [];
  @Output() selection = new EventEmitter();
  @Output() selectedFilters = new EventEmitter();
  filterName: String;
  searchText: string;

  dropDownList = ["equals"];

  isExpanded: boolean = false;
  issueFilterSubscription: any;
  @Input() errorMessage: string = '';
  constructor() {
  }

  ngOnInit() {
  }

  selectFilter(name) {
    this.selectedFilters.emit(name);
    this.closeExpander();
  }

  expand(filterName: String) {
    this.isExpanded = true;
    this.filterName = filterName;
    this.selection.emit(this.filterName);
  }

  closeExpander() {
    this.filterTagLabels = [];
    this.isExpanded = false;
  }
}
