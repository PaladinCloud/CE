import { gql } from 'apollo-angular';

export const NOTIFICATIONS_SUBSCRIPTION = gql`
    subscription SubscribeToData($name: String!) {
        subscribe(name: $name) {
            name
            data
        }
    }
`;
