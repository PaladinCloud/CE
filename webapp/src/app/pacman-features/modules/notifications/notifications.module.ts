import { CommonModule } from '@angular/common';
import { NgModule } from '@angular/core';
import { NgxJsonViewerModule } from 'ngx-json-viewer';
import { SharedModule } from 'src/app/shared/shared.module';
import { CloudNotificationsComponent } from './cloud-notifications/cloud-notifications.component';
import { AcsNotificationComponent } from './notification-details/acs-notification/acs-notification.component';
import { IssueNotificationComponent } from './notification-details/issue-notification/issue-notification.component';
import { KeyvalueNotificationComponent } from './notification-details/keyvalue-notification/keyvalue-notification.component';
import { NotificationDetailsComponent } from './notification-details/notification-details.component';
import { ViolationNotificationComponent } from './notification-details/violation-notification/violation-notification.component';
import { NotificationsRoutingModule } from './notifications-routing.module';
import { PropTitleCasePipe } from './prop-title-case.pipe';
import { PropValueLinkDirective } from './prop-value-link.directive';

@NgModule({
    declarations: [
        CloudNotificationsComponent,
        NotificationDetailsComponent,
        KeyvalueNotificationComponent,
        AcsNotificationComponent,
        IssueNotificationComponent,
        ViolationNotificationComponent,
        PropTitleCasePipe,
        PropValueLinkDirective,
    ],
    imports: [CommonModule, SharedModule, NotificationsRoutingModule, NgxJsonViewerModule],
})
export class NotificationsModule {}
