import { AuthGuardService } from 'src/app/shared/services/auth-guard.service';
import { ComplianceDashboardComponent } from './compliance-dashboard/compliance-dashboard.component';
import { IssueDetailsComponent } from './issue-details/issue-details.component';
import { IssueListingComponent } from './issue-listing/issue-listing.component';
import { PolicyDetailsComponent } from './policy-details/policy-details.component';
import { PolicyKnowledgebaseDetailsComponent } from './policy-knowledgebase-details/policy-knowledgebase-details.component';
import { PolicyKnowledgebaseComponent } from './policy-knowledgebase/policy-knowledgebase.component';
import { RecommendationsDetailsComponent } from './recommendations-details/recommendations-details.component';
import { RecommendationsComponent } from './recommendations/recommendations.component';
import { TaggingComplianceComponent } from './tagging-compliance/tagging-compliance.component';

export const COMPLIANCE_ROUTES = [
    {
        path: 'compliance-dashboard',
        component: ComplianceDashboardComponent,
        canActivate: [AuthGuardService],
        data: {
            title: 'Overview',
        },
    },
    {
        path: 'issue-listing',
        component: IssueListingComponent,
        data: {
            title: 'Policy Violations',
        },
        canActivate: [AuthGuardService],
    },
    {
        path: 'issue-listing/issue-details/:issueId',
        component: IssueDetailsComponent,
        data: {
            title: 'Policy Violation Details',
        },
        canActivate: [AuthGuardService],
    },
    {
        path: 'tagging-compliance',
        component: TaggingComplianceComponent,
        data: {
            title: 'Tagging Compliance',
            tileName: 'app-overview-tagging',
        },
        canActivate: [AuthGuardService],
    },
    {
        path: 'policy-details/:ruleID',
        component: PolicyDetailsComponent,
        data: {
            title: 'Policy Compliance View',
        },
        canActivate: [AuthGuardService],
    },
    {
        path: 'policy-knowledgebase',
        component: PolicyKnowledgebaseComponent,
        data: {
            title: 'Policy Knowledgebase',
        },
        canActivate: [AuthGuardService],
    },
    {
        path: 'policy-knowledgebase-details/:policyID/:autoFix',
        component: PolicyKnowledgebaseDetailsComponent,
        data: {
            title: 'Policy Details',
        },
        canActivate: [AuthGuardService],
    },
    {
        path: 'recommendations',
        component: RecommendationsComponent,
        data: {
            title: 'Recommendations',
            pageLevel: 0,
        },
        canActivate: [AuthGuardService],
    },
    {
        path: 'recommendations-detail/:recommendationId/:name/:general',
        component: RecommendationsDetailsComponent,
        data: {
            title: 'Recommendations Detail',
        },
        canActivate: [AuthGuardService],
    },
];
