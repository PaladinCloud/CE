import { AuthGuardService } from 'src/app/shared/services/auth-guard.service';
import { CertificateComplianceComponent } from './certificate-compliance/certificate-compliance.component';
import { CertificatesComponent } from './certificates/certificates.component';
import { ComplianceDashboardComponent } from './compliance-dashboard/compliance-dashboard.component';
import { DigitalDevDashboardComponent } from './digital-dev-dashboard/digital-dev-dashboard.component';
import { IssueDetailsComponent } from './issue-details/issue-details.component';
import { IssueListingComponent } from './issue-listing/issue-listing.component';
import { PatchingComplianceComponent } from './patching-compliance/patching-compliance.component';
import { PatchingProjectionsComponent } from './patching-projections/patching-projections.component';
import { PolicyDetailsComponent } from './policy-details/policy-details.component';
import { PolicyKnowledgebaseDetailsComponent } from './policy-knowledgebase-details/policy-knowledgebase-details.component';
import { PolicyKnowledgebaseComponent } from './policy-knowledgebase/policy-knowledgebase.component';
import { RecommendationsDetailsComponent } from './recommendations-details/recommendations-details.component';
import { RecommendationsComponent } from './recommendations/recommendations.component';
import { TaggingComplianceComponent } from './tagging-compliance/tagging-compliance.component';
import { VulnerabilitiesComplianceComponent } from './vulnerabilities-compliance/vulnerabilities-compliance.component';
import { VulnerabilitiesComponent } from './vulnerabilities/vulnerabilities.component';
import { VulnerabilityDetailsComponent } from './vulnerability-details/vulnerability-details.component';

export const COMPLIANCE_ROUTES = [
    {
      path: "compliance-dashboard",
      component: ComplianceDashboardComponent,
      canActivate: [AuthGuardService],
      data: {
        title: "Overview",
  
      },
    },
    {
      path: "issue-listing",
      component: IssueListingComponent,
      data: {
        title: "Policy Violations",
      },
      canActivate: [AuthGuardService],
    },
    {
      path: "issue-listing/issue-details/:issueId",
      component: IssueDetailsComponent,
      data: {
        title: "Policy Violation Details",
      },
      canActivate: [AuthGuardService],
    },
    {
      path: "vulnerabilities-compliance",
      component: VulnerabilitiesComplianceComponent,
      data: {
        title: "Vulnerabilities",
        tileName: "app-overview-vulnerabilities",
      },
      canActivate: [AuthGuardService],
    },
    {
      path: "patching-compliance",
      component: PatchingComplianceComponent,
      data: {
        title: "Patching Compliance",
        tileName: "app-overview-patching",
      },
      canActivate: [AuthGuardService],
    },
    {
      path: "tagging-compliance",
      component: TaggingComplianceComponent,
      data: {
        title: "Tagging Compliance",
        tileName: "app-overview-tagging",
      },
      canActivate: [AuthGuardService],
    },
    {
      path: "certificate-compliance",
      component: CertificateComplianceComponent,
      data: {
        title: "Certificate Compliance",
        tileName: "app-overview-certificates",
      },
      canActivate: [AuthGuardService],
    },
    {
      path: "policy-details/:ruleID",
      component: PolicyDetailsComponent,
      data: {
        title: "Policy Compliance View",
      },
      canActivate: [AuthGuardService],
    },
    {
      path: "policy-knowledgebase",
      component: PolicyKnowledgebaseComponent,
      data: {
        title: "Policy Knowledgebase",
      },
      canActivate: [AuthGuardService],
    },
    {
      path: "policy-knowledgebase-details/:policyID/:autoFix",
      component: PolicyKnowledgebaseDetailsComponent,
      data: {
        title: "Policy Details",
      },
      canActivate: [AuthGuardService],
    },
    {
      path: "certificates",
      component: CertificatesComponent,
      data: {
        title: "Certificates List",
      },
      canActivate: [AuthGuardService],
    },
    {
      path: "vulnerabilities",
      component: VulnerabilitiesComponent,
      data: {
        title: "Vulnerabilities List",
      },
      canActivate: [AuthGuardService],
    },
    {
      path: "patching-projections",
      component: PatchingProjectionsComponent,
      data: {
        title: "Patching Projections",
        roles: ["ROLE_ONPREM_ADMIN"],
      },
      canActivate: [AuthGuardService],
    },
    {
      path: "dev-standard-dashboard",
      component: DigitalDevDashboardComponent,
      data: {
        title: "Digital Dev Dashboard",
      },
      canActivate: [AuthGuardService],
    },
    {
      path: "vulnerabilities/vulnerability-details/:qid",
      component: VulnerabilityDetailsComponent,
      data: {
        title: "Vulnerability Details",
      },
      canActivate: [AuthGuardService],
    },
    {
      path: "vulnerabilities-compliance/:details",
      component: VulnerabilitiesComponent,
      data: {
        title: "Vulnerabilities",
      },
      canActivate: [AuthGuardService],
    },
    {
      path: "recommendations",
      component: RecommendationsComponent,
      data: {
        title: "Recommendations",
        pageLevel: 0,
      },
      canActivate: [AuthGuardService],
    },
    {
      path: "recommendations-detail/:recommendationId/:name/:general",
      component: RecommendationsDetailsComponent,
      data: {
        title: "Recommendations Detail",
      },
      canActivate: [AuthGuardService],
    },
  ];