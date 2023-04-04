from core.terraform.resources.aws.appsync import AppSync, ApiSyncId
from core.config import Settings
import json

class AppSyncNotification(AppSync):
    authentication_type = "API_KEY"
    name = "paladincloud"
    schema = ''''type Channel' {
	            'data': 'AWSJSON!'
	            'name': 'String!'
            }

                'type Mutation' {
	            'publish(data: AWSJSON!, name: String!)': 'Channel'
            }

                'type Query' {
	            'getChannel: Channel'
            }

                'type Subscription' {
	            'subscribe(name: String!): Channel'
		        '@aws_subscribe(mutations: ["publish"])'
            }

            'schema' {
	            'query': 'Query'
	            'mutation': 'Mutation'
	            'subscription': 'Subscription'
            }'''


class ApiSyncIdKey(ApiSyncId):
    api_id = AppSyncNotification.get_output_attr('id')