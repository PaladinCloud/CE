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

import { CommonModule } from '@angular/common';
import { NgModule } from '@angular/core';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatChipsModule } from '@angular/material/chips';
import { MatNativeDateModule } from '@angular/material/core';
import { MatDatepickerModule } from '@angular/material/datepicker';
import { MatDialogModule } from '@angular/material/dialog';
import { MatGridListModule } from '@angular/material/grid-list';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatMenuModule } from '@angular/material/menu';
import { MatSelectModule } from '@angular/material/select';
import { MatSnackBarModule } from '@angular/material/snack-bar';
import { MatSortModule } from '@angular/material/sort';
import { MatStepperModule } from '@angular/material/stepper';
import { MatTableModule } from '@angular/material/table';
import { MatTooltipModule } from '@angular/material/tooltip';
import { DragDropModule } from '@angular/cdk/drag-drop';
import { RouterModule } from '@angular/router';
import { AgGridModule } from 'ag-grid-angular';
import { SelectDropDownModule } from 'ngx-select-dropdown';
import { StatsOverlayComponent } from '../post-login-app/stats-overlay/stats-overlay.component';
import { AgGridTableComponent } from './ag-grid-table/ag-grid-table.component';
import { BackNavigationComponent } from './back-navigation/back-navigation.component';
import { BarChartComponent } from './bar-chart/bar-chart.component';
import { BreadcrumbComponent } from './breadcrumb/breadcrumb.component';
import { ButtonIconComponent } from './button-icon/button-icon.component';
import { ButtonComponent } from './button/button.component';
import { CanvasSidePanelComponent } from './canvas-side-panel/canvas-side-panel.component';
import { CheckBoxBtnComponent } from './check-box-btn/check-box-btn.component';
import { CommonPageTemplateComponent } from './common-page-template/common-page-template.component';
import { ChipComponent } from './components/atoms/chip/chip.component';
import { TextComponent } from './components/atoms/text/text.component';
import { CustomButtonComponent } from './components/molecules/custom-button/custom-button.component';
import { CustomCardComponent } from './components/molecules/custom-card/custom-card.component';
import { DialogBoxComponent } from './components/molecules/dialog-box/dialog-box.component';
import { OverviewTileComponent } from './components/molecules/overview-tile/overview-tile.component';
import { SnackbarComponent } from './components/molecules/snackbar/snackbar.component';
import { TextWithIconComponent } from './components/molecules/text-with-icon/text-with-icon.component';
import { ConfigHistoryDropdownComponent } from './config-history-dropdown/config-history-dropdown.component';
import { ConfirmationBoxComponent } from './confirmation-box/confirmation-box.component';
import { ContentSliderComponent } from './content-slider/content-slider.component';
import { CopyElementComponent } from './copy-element/copy-element.component';
import { DataTableComponent } from './data-table/data-table.component';
import { DateDropdownComponent } from './date-dropdown/date-dropdown.component';
import { DateSelectionComponent } from './date-selection/date-selection.component';
import { OnlyNumberDirective } from './directives/only-number.directive';
import { ScrollTrackerDirective } from './directives/scroll-tracker.directive';
import { DoughnutChartComponent } from './doughnut-chart/doughnut-chart.component';
import { DropdownComponent } from './dropdown/dropdown.component';
import { ErrorMessageComponent } from './error-message/error-message.component';
import { FilterDropdownComponent } from './filter-dropdown/filter-dropdown.component';
import { FilterInfoComponent } from './filter-info/filter-info.component';
import { FilteredSelectorComponent } from './filtered-selector/filtered-selector.component';
import { FormInputComponent } from './form-input/form-input.component';
import { FormsComponent } from './forms/forms.component';
import { GenericModalComponent } from './generic-modal/generic-modal.component';
import { GenericPageFilterComponent } from './generic-page-filter/generic-page-filter.component';
import { GenericSummaryComponent } from './generic-summary/generic-summary.component';
import { HelpTextComponent } from './help-text/help-text.component';
import { InputFieldComponent } from './input-field/input-field.component';
import { InputModalComponent } from './input-modal/input-modal.component';
import { LinkComponent } from './link/link.component';
import { LoaderMsgComponent } from './loader-msg/loader-msg.component';
import { MainFilterComponent } from './main-filter/main-filter.component';
import { MulitidoughnutbandComponent } from './mulitidoughnutband/mulitidoughnutband.component';
import { MultilineTrendComponent } from './multiline-trend/multiline-trend.component';
import { MultilineZoomGraphComponent } from './multiline-zoom-graph/multiline-zoom-graph.component';
import { NestedAccordionComponent } from './nested-accordion/nested-accordion.component';
import { OverlayComponent } from './overlay/overlay.component';
import { AssetSummarySearchFilterPipe } from './pipes/asset-summary-search-filter.pipe';
import { OrderBySumPipe } from './pipes/order-by-sum.pipe';
import { OrderByPipe } from './pipes/order-by.pipe';
import { SearchFilterPipe } from './pipes/search-filter.pipe';
import { SearchPipe } from './pipes/search.pipe';
import { PrimaryPieChartComponent } from './primary-pie-chart/primary-pie-chart.component';
import { RadioButtonComponent } from './radio-button/radio-button.component';
import { SearchBarComponent } from './search-bar/search-bar.component';
import { SearchInfoComponent } from './search-info/search-info.component';
import { SearchableDropdownComponent } from './searchable-dropdown/searchable-dropdown.component';
import { SearchbarDropdownComponent } from './searchbar-dropdown/searchbar-dropdown.component';
import { AuthGuardService } from './services/auth-guard.service';
import { CommonResponseService } from './services/common-response.service';
import { CopytoClipboardService } from './services/copy-to-clipboard.service';
import { ErrorHandlingService } from './services/error-handling.service';
import { ExceptionManagementService } from './services/exception-management.service';
import { FilterManagementService } from './services/filter-management.service';
import { FormService } from './services/form.service';
import { HttpService } from './services/http-response.service';
import { LoggerService } from './services/logger.service';
import { RefactorFieldsService } from './services/refactor-fields.service';
import { RouterUtilityService } from './services/router-utility.service';
import { ScrollTrackerService } from './services/scroll-tracker.service';
import { ToastObservableService } from './services/toast-observable.service';
import { UtilsService } from './services/utils.service';
import { StepperComponent } from './stepper/stepper.component';
import { SubFooterComponent } from './sub-footer/sub-footer.component';
import { TableListComponent } from './table-list/table-list.component';
import { TableTabsComponent } from './table-tabs/table-tabs.component';
import { TableComponent } from './table/table.component';
import { TitleBurgerHeadComponent } from './title-burger-head/title-burger-head.component';
import { ToastNotificationComponent } from './toast-notification/toast-notification.component';
import { WidgetSectionStarterComponent } from './widget-section-starter/widget-section-starter.component';

@NgModule({
    imports: [
        AgGridModule,
        CommonModule,
        DragDropModule,
        FormsModule,
        MatButtonModule,
        MatCardModule,
        MatChipsModule,
        MatDatepickerModule,
        MatDialogModule,
        MatGridListModule,
        MatIconModule,
        MatInputModule,
        MatMenuModule,
        MatNativeDateModule,
        MatSelectModule,
        MatSnackBarModule,
        MatSortModule,
        MatStepperModule,
        MatTableModule,
        MatTooltipModule,
        MatTooltipModule,
        ReactiveFormsModule,
        RouterModule,
        SelectDropDownModule,
    ],
    declarations: [
        AgGridTableComponent,
        AssetSummarySearchFilterPipe,
        BackNavigationComponent,
        BackNavigationComponent,
        BarChartComponent,
        BreadcrumbComponent,
        ButtonComponent,
        ButtonIconComponent,
        CanvasSidePanelComponent,
        CheckBoxBtnComponent,
        ChipComponent,
        CommonPageTemplateComponent,
        ConfigHistoryDropdownComponent,
        ConfirmationBoxComponent,
        ContentSliderComponent,
        CopyElementComponent,
        CustomButtonComponent,
        CustomCardComponent,
        DataTableComponent,
        DateDropdownComponent,
        DateSelectionComponent,
        DialogBoxComponent,
        DoughnutChartComponent,
        DropdownComponent,
        ErrorMessageComponent,
        FilterDropdownComponent,
        FilteredSelectorComponent,
        FilterInfoComponent,
        FormInputComponent,
        FormsComponent,
        GenericModalComponent,
        GenericPageFilterComponent,
        GenericSummaryComponent,
        HelpTextComponent,
        InputFieldComponent,
        InputModalComponent,
        LinkComponent,
        LoaderMsgComponent,
        MainFilterComponent,
        MulitidoughnutbandComponent,
        MultilineTrendComponent,
        MultilineZoomGraphComponent,
        NestedAccordionComponent,
        OnlyNumberDirective,
        OrderByPipe,
        OrderBySumPipe,
        OverlayComponent,
        OverviewTileComponent,
        PrimaryPieChartComponent,
        RadioButtonComponent,
        ScrollTrackerDirective,
        SearchableDropdownComponent,
        SearchBarComponent,
        SearchbarDropdownComponent,
        SearchFilterPipe,
        SearchInfoComponent,
        SearchPipe,
        SnackbarComponent,
        StatsOverlayComponent,
        StepperComponent,
        SubFooterComponent,
        TableComponent,
        TableListComponent,
        TableTabsComponent,
        TextComponent,
        TextWithIconComponent,
        TitleBurgerHeadComponent,
        ToastNotificationComponent,
        WidgetSectionStarterComponent,
    ],
    exports: [
        AgGridTableComponent,
        AssetSummarySearchFilterPipe,
        BackNavigationComponent,
        BackNavigationComponent,
        BarChartComponent,
        BreadcrumbComponent,
        ButtonComponent,
        ButtonIconComponent,
        CanvasSidePanelComponent,
        CheckBoxBtnComponent,
        ChipComponent,
        CommonModule,
        ConfigHistoryDropdownComponent,
        ConfirmationBoxComponent,
        ContentSliderComponent,
        CopyElementComponent,
        CustomButtonComponent,
        CustomCardComponent,
        DataTableComponent,
        DateDropdownComponent,
        DateSelectionComponent,
        DialogBoxComponent,
        DoughnutChartComponent,
        DoughnutChartComponent,
        DragDropModule,
        DropdownComponent,
        ErrorMessageComponent,
        FilteredSelectorComponent,
        FilterInfoComponent,
        FormInputComponent,
        FormsComponent,
        FormsModule,
        GenericModalComponent,
        GenericPageFilterComponent,
        GenericSummaryComponent,
        HelpTextComponent,
        InputFieldComponent,
        InputModalComponent,
        LinkComponent,
        LoaderMsgComponent,
        MainFilterComponent,
        MatIconModule,
        MatSelectModule,
        MatTooltipModule,
        MulitidoughnutbandComponent,
        MultilineTrendComponent,
        MultilineZoomGraphComponent,
        NestedAccordionComponent,
        OnlyNumberDirective,
        OrderByPipe,
        OrderBySumPipe,
        OverlayComponent,
        OverviewTileComponent,
        PrimaryPieChartComponent,
        RadioButtonComponent,
        ReactiveFormsModule,
        RouterModule,
        ScrollTrackerDirective,
        SearchableDropdownComponent,
        SearchBarComponent,
        SearchbarDropdownComponent,
        SearchFilterPipe,
        SearchInfoComponent,
        SearchPipe,
        SelectDropDownModule,
        SnackbarComponent,
        StatsOverlayComponent,
        StepperComponent,
        SubFooterComponent,
        TableComponent,
        TableListComponent,
        TextComponent,
        TextWithIconComponent,
        TitleBurgerHeadComponent,
        ToastNotificationComponent,
        WidgetSectionStarterComponent,
    ],
    providers: [
        AssetSummarySearchFilterPipe,
        AuthGuardService,
        CommonResponseService,
        CopytoClipboardService,
        ErrorHandlingService,
        ExceptionManagementService,
        FilterManagementService,
        FormService,
        HttpService,
        LoggerService,
        OrderByPipe,
        RefactorFieldsService,
        RouterUtilityService,
        ScrollTrackerService,
        SearchFilterPipe,
        SearchPipe,
        ToastObservableService,
        UtilsService,
    ],
})
export class SharedModule {}
