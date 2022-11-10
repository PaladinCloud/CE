/*
 *Copyright 2018 T Mobile, Inc. or its affiliates. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); You may not use
 * this file except in compliance with the License. A copy of the License is located at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * or in the "license" file accompanying this file. This file is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, express or
 * implied. See the License for the specific language governing permissions and
 * limitations under the License.
 */

export const AZURE_SSO = 'azuresso';
export const COGNITO = 'cognito';
const DB = 'db';
const LDAP = 'ldap';

export const CONFIGURATIONS = {
  required: {
    APP_NAME: 'Paladin Cloud',
    domains: {
      PROD_BASE_URL: 'https://dev.paladincloud.io/api', // Expected values: domain where the API is deployed, ex: http://beta.pacbot.com/api
      STG_BASE_URL: 'https://dev.paladincloud.io/api', // Expected values: domain where the API is deployed, ex: http://stgbeta.pacbot.com/api
      DEV_BASE_URL: 'https://dev.paladincloud.io/api', // Expected values: domain where the API is deployed, ex: http://devbeta.pacbot.com/api
      CLOUD_BASE_URL: 'https://dev.paladincloud.io/api', // Expected values: domain where the API is deployed
    },
    featureModules: {
      COMPLIANCE_MODULE: true, // Expected values: true || false
      ASSETS_MODULE: true, // Expected values: true || false
      OMNI_SEARCH_MODULE: true, // Expected values: true || false
      TOOLS_MODULE: false, // Expected values: true || false
      ADMIN_MODULE: true, // Expected values: true || false
    },
  },
  optional: {
    auth: {
      AUTH_TYPE: COGNITO, // AZURE_SSO | DB | LDAP
      adConfig: {
        tenant: 'us-east-1_4Y3ZEgfpy', // Expected values: Value expected if 'AD_AUTHENTICATION' is true
        clientId: '2i40k95uuql4kmanm47o4e6fho' // Expected values: Value expected if 'AD_AUTHENTICATION' is true
      },
      cognitoConfig: {
        sso_api_username: '2i40k95uuql4kmanm47o4e6fho',
        sso_api_pwd: '1abkqmf4bg3gv3i4tt85t7d3omrblqc0138t6p82ds97mn2psgp9',

        loginURL: 'https://test-domain-paladin-1.auth.us-east-1.amazoncognito.com/login?' +
          'client_id=2i40k95uuql4kmanm47o4e6fho&response_type=code&scope=openid+profile&' +
          'redirect_uri=http://localhost:4201/pl/compliance/issue-listing',

        redirectURL: 'http://localhost:4201/pl/compliance/issue-listing',

        cognitoTokenURL: 'https://test-domain-paladin-1.auth.us-east-1.amazoncognito.com/oauth2/token',

        logout: 'https://test-domain-paladin-1.auth.us-east-1.amazoncognito.com/logout?' +
          'client_id=2i40k95uuql4kmanm47o4e6fho&' +
          'logout_uri=http://localhost:4201/home'
      }
    },
    pacmanIssue: {
      CREATE_JIRA_TICKET_FOR_PACMAN_ISSUE: false, // Expected values: true || false || ''
      emailPacManIssue: {
        ISSUE_MAIL_TEMPLATE_URL: '',
        ISSUE_EMAIL_FROM_ID: 'autofix@paladincloud.io',
      }
    },
    assetDetails: {
      ASSET_DETAILS_TEMPLATE_URL: '',
      ASSET_DETAILS_FROM_ID: '',
    },
    general: {
      ACCESS_MANAGEMENT_PORTAL_URL: '',
      e2e: {
        DOMAIN: 'http://localhost:4200',
        EMAIL_ID: '',
        NT_ID: '', // Add NT ID for e2e login
        NT_PASSWORD: '' // Add respective password for e2e login
      },
      qualysEnabled: false,
      OSS: true
    }
  }
};