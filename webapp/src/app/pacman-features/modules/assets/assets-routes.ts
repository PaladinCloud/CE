import { AuthGuardService } from 'src/app/shared/services/auth-guard.service';
import { AssetDashboardComponent } from './asset-dashboard/asset-dashboard.component';
import { AssetDetailsComponent } from './asset-details/asset-details.component';
import { AssetDistributionComponent } from './asset-distribution/asset-distribution.component';
import { AssetListComponent } from './asset-list/asset-list.component';

export const ASSETS_ROUTES = [
    {
        path: 'asset-dashboard',
        component: AssetDashboardComponent,
        data: {
            title: 'Asset Dashboard',
        },
        canActivate: [AuthGuardService],
    },
    {
        path: 'asset-list/:resourceType/:resourceId',
        component: AssetDetailsComponent,
        data: {
            title: 'Asset 360',
        },
        canActivate: [AuthGuardService],
    },
    {
        path: 'asset-distribution',
        component: AssetDistributionComponent,
        data: {
            title: 'Asset Distribution',
        },
        canActivate: [AuthGuardService],
    },
    {
        path: 'asset-list',
        component: AssetListComponent,
        data: {
            title: 'Asset List',
        },
        canActivate: [AuthGuardService],
    },
  ];
