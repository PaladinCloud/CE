import {NgModule} from '@angular/core';
import {CommonModule} from '@angular/common';
import {NotificationsRoutingModule} from './notifications-routing.module';
import {SharedModule} from 'src/app/shared/shared.module';
import {CloudNotificationsComponent} from './cloud-notifications/cloud-notifications.component';
import {NotificationDetailsComponent} from './notification-details/notification-details.component';
import {NgxJsonViewerModule} from "ngx-json-viewer";


@NgModule({
    declarations: [
        CloudNotificationsComponent,
        NotificationDetailsComponent
    ],
    imports: [
        CommonModule,
        SharedModule,
        NotificationsRoutingModule,
        NgxJsonViewerModule
    ]
})
export class NotificationsModule { }
