import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { NOTIFICATIONS_ROUTES } from 'src/app/shared/constants/routes';

const routes: Routes = NOTIFICATIONS_ROUTES;

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule]
})
export class NotificationsRoutingModule { }