import { NgModule } from '@angular/core';
import { ApolloModule, APOLLO_OPTIONS } from 'apollo-angular';
import { ApolloClientOptions, InMemoryCache, ApolloLink } from '@apollo/client/core';
import { HttpLink } from 'apollo-angular/http';
import { AUTH_TYPE, createAuthLink, AuthOptions } from 'aws-appsync-auth-link';
import { createSubscriptionHandshakeLink } from 'aws-appsync-subscription-link';
import { CONFIGURATIONS } from 'src/config/configurations';

const uri = 'configurations.appsyncGraphqlEndpoint'; // <-- add the URL of the GraphQL server here
const region = 'configurations.appsyncRegion';

const auth: AuthOptions = {
    type: AUTH_TYPE.AMAZON_COGNITO_USER_POOLS,
    jwtToken: () =>
        `Basic ${btoa(
            `${CONFIGURATIONS.optional.auth.cognitoConfig.sso_api_username}:${CONFIGURATIONS.optional.auth.cognitoConfig.sso_api_pwd}`,
        )}`,
};

export function createApollo(httpLink: HttpLink): ApolloClientOptions<any> {
    const linkParams = { url: uri, region, auth };
    return {
        link: ApolloLink.from([
            createAuthLink(linkParams),
            createSubscriptionHandshakeLink(linkParams, httpLink.create({ uri })),
        ]),
        cache: new InMemoryCache(),
        connectToDevTools: true,
    };
}

@NgModule({
    exports: [ApolloModule],
    providers: [
        {
            provide: APOLLO_OPTIONS,
            useFactory: createApollo,
            deps: [HttpLink],
        },
    ],
})
export class GraphQLModule {}
