import { AuthGuardService } from 'src/app/shared/services/auth-guard.service';
import { OmniSearchDetailsComponent } from './omni-search-details/omni-search-details.component';

export const OMNISEARCH_ROUTES = [
    {
      path: "omni-search-details",
      component: OmniSearchDetailsComponent,
      canActivate: [AuthGuardService],
    },
];
