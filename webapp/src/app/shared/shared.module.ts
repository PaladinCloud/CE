/*
 *Copyright 2018 T Mobile, Inc. or its affiliates. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); You may not use
 * this file except in compliance with the License. A copy of the License is located at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * or in the "license" file accompanying this file. This file is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, express or
 * implied. See the License for the specific language governing permissions and
 * limitations under the License.
 */

import { NgModule } from "@angular/core";
import { CommonModule } from "@angular/common";
import { FormsModule, ReactiveFormsModule } from "@angular/forms";
import { RouterModule } from "@angular/router";
import { MatSelectModule } from "@angular/material/select";
import { AgGridModule } from "ag-grid-angular";
import { CanvasSidePanelComponent } from "./canvas-side-panel/canvas-side-panel.component";
import { ButtonIconComponent } from "./button-icon/button-icon.component";
import { ErrorMessageComponent } from "./error-message/error-message.component";
import { ButtonComponent } from "./button/button.component";
import { FormInputComponent } from "./form-input/form-input.component";
import { LinkComponent } from "./link/link.component";
import { SubFooterComponent } from "./sub-footer/sub-footer.component";
import { TitleBurgerHeadComponent } from "./title-burger-head/title-burger-head.component";
import { OverlayComponent } from "./overlay/overlay.component";
import { OrderByPipe } from "./../shared/pipes/order-by.pipe";
import { SearchFilterPipe } from "./../shared/pipes/search-filter.pipe";
import { SearchPipe } from "./../shared/pipes/search.pipe";
import { AssetSummarySearchFilterPipe } from "./pipes/asset-summary-search-filter.pipe";
import { MainRoutingAnimationEventService } from "./services/main-routing-animation-event.service";
import { RefactorFieldsService } from "./services/refactor-fields.service";
import { RouterUtilityService } from "./services/router-utility.service";

// Imports for local data

import { HttpService } from "./services/http-response.service";
import { UtilsService } from "./services/utils.service";
import { SearchableDropdownComponent } from "./searchable-dropdown/searchable-dropdown.component";
import { BackNavigationComponent } from "./back-navigation/back-navigation.component";
import { FilteredSelectorComponent } from "./filtered-selector/filtered-selector.component";
import { BreadcrumbComponent } from "./breadcrumb/breadcrumb.component";
import { SearchInfoComponent } from "./search-info/search-info.component";
import { DataTableComponent } from "./data-table/data-table.component";
import { DateDropdownComponent } from "./date-dropdown/date-dropdown.component";
import { AuthGuardService } from "./../shared/services/auth-guard.service";
import { LoggerService } from "./services/logger.service";
import { ErrorHandlingService } from "./services/error-handling.service";
import { CommonResponseService } from "./services/common-response.service";
import { SearchBarComponent } from "./search-bar/search-bar.component";
import { OrderBySumPipe } from "./pipes/order-by-sum.pipe";
import { ContentSliderComponent } from "./content-slider/content-slider.component";
import { TableTabsComponent } from "./table-tabs/table-tabs.component";
import { StatsOverlayComponent } from "../post-login-app/stats-overlay/stats-overlay.component";
import { DoughnutChartComponent } from "./doughnut-chart/doughnut-chart.component";
import { GenericSummaryComponent } from "./generic-summary/generic-summary.component";
import { FilterInfoComponent } from "./filter-info/filter-info.component";
import { InputModalComponent } from "./input-modal/input-modal.component";
import { MulitidoughnutbandComponent } from "./mulitidoughnutband/mulitidoughnutband.component";
import { ToastNotificationComponent } from "./toast-notification/toast-notification.component";
import { HelpTextComponent } from "./help-text/help-text.component";
import { SearchbarDropdownComponent } from "./searchbar-dropdown/searchbar-dropdown.component";
// import {DownloadService} from './services/download.service';
import { ExceptionManagementService } from "./services/exception-management.service";
import { MainFilterComponent } from "./main-filter/main-filter.component";
import { RadioButtonComponent } from "./radio-button/radio-button.component";
import { CheckBoxBtnComponent } from "./check-box-btn/check-box-btn.component";
import { FilterManagementService } from "./services/filter-management.service";
import { GenericPageFilterComponent } from "./generic-page-filter/generic-page-filter.component";
import { CommonPageTemplateComponent } from "./common-page-template/common-page-template.component";
import { WidgetSectionStarterComponent } from "./widget-section-starter/widget-section-starter.component";
import { BarChartComponent } from "./bar-chart/bar-chart.component";
import { PrimaryPieChartComponent } from "./primary-pie-chart/primary-pie-chart.component";
import { OnlyNumberDirective } from "./directives/only-number.directive";
import { NestedAccordionComponent } from "./nested-accordion/nested-accordion.component";
import { MultilineTrendComponent } from "./multiline-trend/multiline-trend.component";
import { CopytoClipboardService } from "./services/copy-to-clipboard.service";
import { AgGridTableComponent } from "./ag-grid-table/ag-grid-table.component";
import { FormsComponent } from "./forms/forms.component";
import { ConfirmationBoxComponent } from "./confirmation-box/confirmation-box.component";
import { FormService } from "./services/form.service";
import { LoaderMsgComponent } from "./loader-msg/loader-msg.component";
import { GenericModalComponent } from "./generic-modal/generic-modal.component";
import { ConfigHistoryDropdownComponent } from "./config-history-dropdown/config-history-dropdown.component";
import { CopyElementComponent } from "./copy-element/copy-element.component";
import { ToastObservableService } from "./services/toast-observable.service";
import { SelectDropDownModule } from "ngx-select-dropdown";
import { TableListComponent } from "./table-list/table-list.component";
import { FilterDropdownComponent } from "./filter-dropdown/filter-dropdown.component";
import { MatIconModule } from "@angular/material/icon";
import { MatMenuModule } from '@angular/material/menu';
import { MatGridListModule } from "@angular/material/grid-list";
import { DropdownComponent } from "./dropdown/dropdown.component";
import { MatDatepickerModule } from '@angular/material/datepicker';
import { MatNativeDateModule } from '@angular/material/core';
import { MatInputModule } from "@angular/material/input";
import { TableComponent } from "./table/table.component";
import { MatTableModule } from "@angular/material/table";
import { MatSortModule } from "@angular/material/sort";
import { MultilineZoomGraphComponent } from "./multiline-zoom-graph/multiline-zoom-graph.component";
import { DateSelection } from "./date-selection/date-selection.component";
import { MatCardModule } from "@angular/material/card";
import { DialogBoxComponent } from './components/molecules/dialog-box/dialog-box.component';
import { SnackbarComponent } from './components/molecules/snackbar/snackbar.component';
import { MatSnackBarModule } from "@angular/material/snack-bar";
import { CustomButtonComponent } from './components/molecules/custom-button/custom-button.component';
import { MatDialogModule } from "@angular/material/dialog";
import { MatButtonModule } from "@angular/material/button";
import { ChipComponent } from './components/atoms/chip/chip.component';
import { TextComponent } from './components/atoms/text/text.component';
import { MatStepperModule } from '@angular/material/stepper';
import { StepperComponent } from "./stepper/stepper.component";
import { CustomCardComponent } from './components/molecules/custom-card/custom-card.component';
import { OverviewTileComponent } from './components/molecules/overview-tile/overview-tile.component';
import { TextWithIconComponent } from "./components/molecules/text-with-icon/text-with-icon.component";

@NgModule({
  imports: [
    MatButtonModule,
    MatStepperModule,
    MatDialogModule,
    MatSnackBarModule,
    MatGridListModule,
    MatCardModule,
    MatSelectModule,
    MatIconModule,
    MatMenuModule,
    CommonModule,
    FormsModule,
    ReactiveFormsModule,
    RouterModule,
    MatInputModule,
    MatTableModule,
    MatSortModule,
    MatDatepickerModule,
    MatNativeDateModule,
    SelectDropDownModule,
    AgGridModule
  ],
  declarations: [
    MultilineZoomGraphComponent,
    TableComponent,
    DateSelection,
    DropdownComponent,
    FilterDropdownComponent,
    CanvasSidePanelComponent,
    ButtonIconComponent,
    ButtonComponent,
    ErrorMessageComponent,
    FormInputComponent,
    LinkComponent,
    SubFooterComponent,
    TitleBurgerHeadComponent,
    OverlayComponent,
    SearchableDropdownComponent,
    FilteredSelectorComponent,
    OrderByPipe,
    OrderBySumPipe,
    SearchPipe,
    SearchFilterPipe,
    AssetSummarySearchFilterPipe,
    BackNavigationComponent,
    BreadcrumbComponent,
    SearchInfoComponent,
    DataTableComponent,
    DateDropdownComponent,
    SearchBarComponent,
    ContentSliderComponent,
    TableTabsComponent,
    StatsOverlayComponent,
    MulitidoughnutbandComponent,
    DoughnutChartComponent,
    GenericSummaryComponent,
    FilterInfoComponent,
    ToastNotificationComponent,
    HelpTextComponent,
    SearchbarDropdownComponent,
    MainFilterComponent,
    RadioButtonComponent,
    CheckBoxBtnComponent,
    GenericPageFilterComponent,
    CommonPageTemplateComponent,
    WidgetSectionStarterComponent,
    BarChartComponent,
    PrimaryPieChartComponent,
    OnlyNumberDirective,
    NestedAccordionComponent,
    MultilineTrendComponent,
    AgGridTableComponent,
    FormsComponent,
    CopyElementComponent,
    ConfirmationBoxComponent,
    LoaderMsgComponent,
    GenericModalComponent,
    ConfigHistoryDropdownComponent,
    InputModalComponent,
    BackNavigationComponent,
    TableListComponent,
    DialogBoxComponent,
    SnackbarComponent,
    CustomButtonComponent,
    ChipComponent,
    TextComponent,
    StepperComponent,
    CustomCardComponent,
    OverviewTileComponent,
    TextWithIconComponent
  ],
  exports: [
    TextWithIconComponent,
    CustomCardComponent,
    OverviewTileComponent,
    TextComponent,
    ChipComponent,
    StepperComponent,
    CustomButtonComponent,
    DialogBoxComponent,
    SnackbarComponent,
    MultilineZoomGraphComponent,
    TableComponent,
    DateSelection,
    DropdownComponent,
    DoughnutChartComponent,
    CommonModule,
    FormsModule,
    ReactiveFormsModule,
    RouterModule,
    CanvasSidePanelComponent,
    ButtonIconComponent,
    ButtonComponent,
    ErrorMessageComponent,
    FormInputComponent,
    LinkComponent,
    SubFooterComponent,
    TitleBurgerHeadComponent,
    MatSelectModule,
    OverlayComponent,
    SearchableDropdownComponent,
    FilteredSelectorComponent,
    OrderByPipe,
    OrderBySumPipe,
    SearchFilterPipe,
    SearchPipe,
    AssetSummarySearchFilterPipe,
    BreadcrumbComponent,
    SearchInfoComponent,
    DataTableComponent,
    DateDropdownComponent,
    SearchBarComponent,
    ContentSliderComponent,
    StatsOverlayComponent,
    MulitidoughnutbandComponent,
    DoughnutChartComponent,
    GenericSummaryComponent,
    CopyElementComponent,
    FilterInfoComponent,
    BackNavigationComponent,
    ToastNotificationComponent,
    HelpTextComponent,
    SearchbarDropdownComponent,
    MainFilterComponent,
    RadioButtonComponent,
    CheckBoxBtnComponent,
    GenericPageFilterComponent,
    WidgetSectionStarterComponent,
    BarChartComponent,
    PrimaryPieChartComponent,
    OnlyNumberDirective,
    NestedAccordionComponent,
    MultilineTrendComponent,
    AgGridTableComponent,
    FormsComponent,
    ConfirmationBoxComponent,
    LoaderMsgComponent,
    GenericModalComponent,
    ConfigHistoryDropdownComponent,
    InputModalComponent,
    BackNavigationComponent,
    SelectDropDownModule,
    TableListComponent,
  ],
  providers: [
    HttpService,
    UtilsService,
    ExceptionManagementService,
    RefactorFieldsService,
    OrderByPipe,
    SearchFilterPipe,
    SearchPipe,
    AssetSummarySearchFilterPipe,
    MainRoutingAnimationEventService,
    AuthGuardService,
    RouterUtilityService,
    LoggerService,
    ErrorHandlingService,
    FilterManagementService,
    ToastObservableService,
    CommonResponseService,
    CopytoClipboardService,
    FormService,
  ],
})
export class SharedModule { }
