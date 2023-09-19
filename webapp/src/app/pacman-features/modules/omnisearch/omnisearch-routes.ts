import { AuthGuardService } from 'src/app/shared/services/auth-guard.service';
import { OmniSearchDetailsComponent } from './omni-search-details/omni-search-details.component';
import { OmniSearchPageComponent } from './omni-search-page/omni-search-page.component';

export const OMNISEARCH_ROUTES = [
    {
      path: "omni-search-page",
      component: OmniSearchPageComponent,
      canActivate: [AuthGuardService],
    },
    {
      path: "omni-search-details",
      component: OmniSearchDetailsComponent,
      canActivate: [AuthGuardService],
    },
  ];