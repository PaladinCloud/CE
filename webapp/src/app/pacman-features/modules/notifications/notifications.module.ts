import { CommonModule } from '@angular/common';
import { NgModule } from '@angular/core';
import { NgxJsonViewerModule } from 'ngx-json-viewer';
import { SharedModule } from 'src/app/shared/shared.module';
import { CloudNotificationsComponent } from './cloud-notifications/cloud-notifications.component';
import { AwsIssueNotificationComponent } from './notification-details/aws/issue-notification/issue-notification.component';
import { KeyvalueNotificationComponent } from './notification-details/keyvalue-notification/keyvalue-notification.component';
import { NotificationDetailsComponent } from './notification-details/notification-details.component';
import { PaladinCloudViolationNotificationComponent } from './notification-details/paladincloud/violation-notification/violation-notification.component';
import { RedhatAcsNotificationComponent } from './notification-details/redhat/acs-notification/acs-notification.component';
import { NotificationsRoutingModule } from './notifications-routing.module';
import { PropTitleCasePipe } from './prop-title-case.pipe';
import { PropValueLinkDirective } from './prop-value-link.directive';

@NgModule({
    declarations: [
        AwsIssueNotificationComponent,
        CloudNotificationsComponent,
        KeyvalueNotificationComponent,
        NotificationDetailsComponent,
        PaladinCloudViolationNotificationComponent,
        PropTitleCasePipe,
        PropValueLinkDirective,
        RedhatAcsNotificationComponent,
    ],
    imports: [CommonModule, SharedModule, NotificationsRoutingModule, NgxJsonViewerModule],
})
export class NotificationsModule {}
