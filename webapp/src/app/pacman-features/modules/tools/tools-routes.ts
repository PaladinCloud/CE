import { AuthGuardService } from 'src/app/shared/services/auth-guard.service';
import { ToolsLandingPageComponent } from './tools-landing-page/tools-landing-page.component';

export const TOOLS_ROUTES = [
    {
        path: 'tools-landing',
        component: ToolsLandingPageComponent,
        data: {
            title: 'Tools Overview',
        },
        canActivate: [AuthGuardService],
    },
];
