import { Injectable } from '@angular/core';
import { Action, Selector, State, StateContext } from '@ngxs/store';
import { ConfigActions } from './config.actions';

export interface ConfigStateModel {
    required: {
        APP_NAME: string;
        domains: {
            PROD_BASE_URL: string;
            STG_BASE_URL: string;
            DEV_BASE_URL: string;
            CLOUD_BASE_URL: string;
        };
        featureModules: {
            COMPLIANCE_MODULE: boolean;
            ASSETS_MODULE: boolean;
            OMNI_SEARCH_MODULE: boolean;
            TOOLS_MODULE: boolean;
            ADMIN_MODULE: boolean;
        };
    };
    optional: {
        auth: {
            AUTH_TYPE: 'azuresso' | 'cognito' | 'db' | 'ldap';
            adConfig: {
                tenant: string;
                clientId: string;
            };
            cognitoConfig: {
                sso_api_username: string;
                sso_api_pwd: string;
                loginURL: string;
                redirectURL: string;
                cognitoTokenURL: string;
                logout: string;
                CloudformationTemplateUrl: string;
            };
        };
        pacmanIssue: {
            CREATE_JIRA_TICKET_FOR_PACMAN_ISSUE: boolean;
            emailPacManIssue: {
                ISSUE_MAIL_TEMPLATE_URL: string;
                ISSUE_EMAIL_FROM_ID: string;
            };
        };
        assetDetails: {
            ASSET_DETAILS_TEMPLATE_URL: string;
            ASSET_DETAILS_FROM_ID: string;
        };
        general: {
            ACCESS_MANAGEMENT_PORTAL_URL: string;
            e2e: {
                DOMAIN: string;
                EMAIL_ID: string;
                NT_ID: string;
                NT_PASSWORD: string;
            };
            qualysEnabled: boolean;
            OSS: boolean;
            gaKey: string;
            notifications: {
                url: string;
                region: string;
                apiKey: string;
            };
            Interval: {
                JobInterval: string;
            };
        };
    };
}

@State<ConfigStateModel>({
    name: 'Config',
    defaults: null,
})
@Injectable()
export class ConfigState {
    constructor() {}

    @Action(ConfigActions.Get)
    get(ctx: StateContext<ConfigStateModel>) {
        console.debug('should obtain the config from configService');
        // provide configService in the constructor
        // replace with commented code when configService will be ready
        //
        // return configService.get().pipe(
        //     tap((config) => ctx.setState(config))
        // )
        return Promise.resolve(() => {
            // set dummy payload
            const payload = null;
            ctx.setState(payload);
        });
    }

    @Selector()
    config(state: ConfigStateModel) {
        return state;
    }
}
