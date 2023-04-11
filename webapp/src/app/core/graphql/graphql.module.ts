import { NgModule } from '@angular/core';
import { ApolloClientOptions, ApolloLink, InMemoryCache } from '@apollo/client/core';
import { ApolloModule, APOLLO_OPTIONS } from 'apollo-angular';
import { HttpLink } from 'apollo-angular/http';
import { AuthOptions, AUTH_TYPE, createAuthLink } from 'aws-appsync-auth-link';
import { createSubscriptionHandshakeLink } from 'aws-appsync-subscription-link';
import { CONFIGURATIONS } from 'src/config/configurations';
import { environment } from 'src/environments/environment';

const { url, region, apiKey } = CONFIGURATIONS.optional.general.notifications;

const auth: AuthOptions = {
    type: AUTH_TYPE.API_KEY,
    apiKey,
};

export function createApollo(httpLink: HttpLink): ApolloClientOptions<any> {
    const linkParams = { url, region, auth };
    return {
        link: ApolloLink.from([
            createAuthLink(linkParams),
            createSubscriptionHandshakeLink(linkParams, httpLink.create({ uri: url })),
        ]),
        cache: new InMemoryCache(),
        connectToDevTools: !environment.production,
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
