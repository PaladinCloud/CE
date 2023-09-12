import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { NOTIFICATIONS_ROUTES } from './notifications-routes';

const routes: Routes = NOTIFICATIONS_ROUTES;

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule]
})
export class NotificationsRoutingModule { }