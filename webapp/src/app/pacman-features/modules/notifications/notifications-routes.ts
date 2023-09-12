import { AuthGuardService } from 'src/app/shared/services/auth-guard.service';
import { CloudNotificationsComponent } from './cloud-notifications/cloud-notifications.component';
import { NotificationDetailsComponent } from './notification-details/notification-details.component';

export const NOTIFICATIONS_ROUTES = [
  {
    path: "notification-details",
    component: NotificationDetailsComponent,
    data: {
      title: "Notifications Details",
    },
    canActivate: [AuthGuardService],
  },
  {
    path: "notifications-list",
    component: CloudNotificationsComponent,
    data: {
      title: "Notifications",
    },
    canActivate: [AuthGuardService],
  },
];