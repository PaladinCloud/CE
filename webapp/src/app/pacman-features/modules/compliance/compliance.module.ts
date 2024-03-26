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

import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { SharedModule } from './../../../shared/shared.module';
import { ComplianceDashboardComponent } from './compliance-dashboard/compliance-dashboard.component';
import { ComplianceRoutingModule } from './compliance-routing.module';
import { PacmanIssuesComponent } from './../../secondary-components/pacman-issues/pacman-issues.component';
import { MultilineBrushZoomComponent } from './../../secondary-components/multiline-brush-zoom/multiline-brush-zoom.component';
import { MultiBandDonutComponent } from './../../secondary-components/multi-band-donut/multi-band-donut.component';
import { SelectComplianceDropdown } from './../../services/select-compliance-dropdown.service';
import { IssueListingComponent } from './issue-listing/issue-listing.component';
import { IssueDetailsComponent } from './issue-details/issue-details.component';
import { IssueBlocksComponent } from './../../secondary-components/issue-blocks/issue-blocks.component';
import { ListTableComponent } from './../../secondary-components/list-table/list-table.component';
import { IssuesTrendHistoryComponent } from './../../secondary-components/issues-trend-history/issues-trend-history.component';
import { TaggingComplianceComponent } from './tagging-compliance/tagging-compliance.component';
import { WindowRefService } from './../../services/window.service';
import { QuarterGraphComponent } from './../../secondary-components/quarter-graph/quarter-graph.component';
import { PolicyDetailsComponent } from './policy-details/policy-details.component';
import { PolicySummaryComponent } from './../../secondary-components/policy-summary/policy-summary.component';
import { PolicyAcrossApplicationComponent } from './../../secondary-components/policy-across-application/policy-across-application.component';
import { AllPolicyViolationsComponent } from './../../secondary-components/all-policy-violations/all-policy-violations.component';
import { PolicyTrendComponent } from './../../secondary-components/policy-trend/policy-trend.component';
import { TaggingSummaryComponent } from './../../secondary-components/tagging-summary/tagging-summary.component';
import { TotalTagComplianceComponent } from './../../secondary-components/total-tag-compliance/total-tag-compliance.component';
import { PolicyContentSliderComponent } from './../../secondary-components/policy-content-slider/policy-content-slider.component';
import { TargetTypeTaggingTileComponent } from './../../secondary-components/target-type-tagging-tile/target-type-tagging-tile.component';
import { TaggingAcrossTargetTypeComponent } from './../../secondary-components/tagging-across-target-type/tagging-across-target-type.component';
import { TaggingInstancesTableComponent } from './../../secondary-components/tagging-instances-table/tagging-instances-table.component';
import { ComplianceOverviewTrendComponent } from './../../secondary-components/compliance-overview-trend/compliance-overview-trend.component';
import { TaggingComplianceTrendComponent } from './../../secondary-components/tagging-compliance-trend/tagging-compliance-trend.component';
import { CertificatesComplianceTrendComponent } from './../../secondary-components/certificates-compliance-trend/certificates-compliance-trend.component';
import { PolicyKnowledgebaseComponent } from './policy-knowledgebase/policy-knowledgebase.component';
import { PolicyKnowledgebaseDetailsComponent } from './policy-knowledgebase-details/policy-knowledgebase-details.component';
import { CertificateAssetsTrendComponent } from './../../secondary-components/certificate-assets-trend/certificate-assets-trend.component';
import { TaggingAssetsTrendComponent } from './../../secondary-components/tagging-assets-trend/tagging-assets-trend.component';
import { PolicyAssetsTrendComponent } from './../../secondary-components/policy-assets-trend/policy-assets-trend.component';
import { VulnerabilitySummaryTableComponent } from './../../secondary-components/vulnerability-summary-table/vulnerability-summary-table.component';
import { AgGridModule } from 'ag-grid-angular';
import { PolicyViolationDescComponent } from './../../secondary-components/policy-violation-desc/policy-violation-desc.component';
import { IssueListingService } from '../../services/issue-listing.service';
import { RecommendationsComponent } from '../../modules/compliance/recommendations/recommendations.component';
import { RecommandCategoryComponent } from '../../secondary-components/recommand-category/recommand-category.component';
import { RecommendationsDetailsComponent } from './recommendations-details/recommendations-details.component';
import { OverallVulnerabilitiesComponent } from './../../secondary-components/overall-vulnerabilities/overall-vulnerabilities.component';
import { MatCardModule } from '@angular/material/card';
import { MatGridListModule } from '@angular/material/grid-list';
import { MatProgressBarModule } from '@angular/material/progress-bar';
import { ViolationsCardComponent } from './violations-card/violations-card.component';
import { CardComponent } from './card/card.component';
import { PolicyViolationSummaryService } from '../../services/policy-violation-summary.service';
import { PacmanIssuesService } from '../../services/pacman-issues.service';
import { ProgressBarChartComponent } from './progress-bar-chart/progress-bar-chart.component';
import { HorizontalBarChartComponent } from './horizontal-bar-chart/horizontal-bar-chart.component';
import { MatMenuModule } from '@angular/material/menu';
import { MatInputModule } from '@angular/material/input';
import { MatFormFieldModule } from '@angular/material/form-field';
import { PolicyAutofixComponent } from './issue-details/policy-autofix/policy-autofix.component';

@NgModule({
    imports: [
        MatInputModule,
        MatFormFieldModule,
        MatMenuModule,
        MatCardModule,
        MatGridListModule,
        MatProgressBarModule,
        CommonModule,
        ComplianceRoutingModule,
        SharedModule,
        AgGridModule,
    ],
    declarations: [
        ProgressBarChartComponent,
        HorizontalBarChartComponent,
        PacmanIssuesComponent,
        MultilineBrushZoomComponent,
        MultiBandDonutComponent,
        ComplianceDashboardComponent,
        IssueListingComponent,
        IssueDetailsComponent,
        IssueBlocksComponent,
        ListTableComponent,
        IssuesTrendHistoryComponent,
        TaggingComplianceComponent,
        QuarterGraphComponent,
        PolicyDetailsComponent,
        PolicySummaryComponent,
        PolicyAcrossApplicationComponent,
        AllPolicyViolationsComponent,
        PolicyTrendComponent,
        TaggingSummaryComponent,
        TotalTagComplianceComponent,
        PolicyContentSliderComponent,
        TargetTypeTaggingTileComponent,
        TaggingAcrossTargetTypeComponent,
        TaggingInstancesTableComponent,
        ComplianceOverviewTrendComponent,
        TaggingComplianceTrendComponent,
        CertificatesComplianceTrendComponent,
        PolicyKnowledgebaseComponent,
        PolicyKnowledgebaseDetailsComponent,
        CertificateAssetsTrendComponent,
        TaggingAssetsTrendComponent,
        PolicyAssetsTrendComponent,
        VulnerabilitySummaryTableComponent,
        PolicyViolationDescComponent,
        RecommendationsComponent,
        RecommandCategoryComponent,
        RecommendationsDetailsComponent,
        OverallVulnerabilitiesComponent,
        CardComponent,
        ViolationsCardComponent,
        PolicyAutofixComponent,
    ],
    providers: [
        SelectComplianceDropdown,
        WindowRefService,
        IssueListingService,
        PolicyViolationSummaryService,
        PacmanIssuesService,
    ],
})
export class ComplianceModule {}
