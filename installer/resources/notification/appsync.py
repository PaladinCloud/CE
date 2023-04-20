from core.terraform.resources.aws.appsync import AppSyncResolvers, AppSync, AppSyncId, AppSyncDataSource
from core.config import Settings
import json

class AppSyncNotification(AppSync):
    authentication_type = "API_KEY"
    name = "notification-appsync-service"
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

class AppSyncIdKey(AppSyncId):
    api_id = AppSyncNotification.get_output_attr('id')
    expires = "2024-01-03T04:00:00Z"
    DEPENDS_ON = [AppSyncNotification]
class AppSyncDataSource(AppSyncDataSource):
    api_id      = AppSyncNotification.get_output_attr('id')
    name        = "NoneDatasource"
    description = "A None Datasource."
    type        = "NONE"
    
class AppSyncResolvers(AppSyncResolvers):
    api_id             = AppSyncNotification.get_output_attr('id')
    type          = "Mutation"
    field         = "publish"
    data_source   = AppSyncDataSource.get_output_attr('name')
    request_template   = '''
    {
      "version": "2017-02-28",
      "payload": {
        "name": "$context.arguments.name",
        "data": $util.toJson($context.arguments.data)
      }
    }'''
    response_template  = "$util.toJson($context.result)"

