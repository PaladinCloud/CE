from core.terraform.resources.aws import iam
from resources.eventbus.custom_event_bus import CloudWatchEventBusPlugin, CloudWatchEventBusaws, CloudWatchEventBusazure, CloudWatchEventBusGcp

class EventBridgePolicyDocument(iam.IAMPolicyDocumentData):
    statement = [
        {
            'actions': ["sts:AssumeRole"],
            'principals': {
                'type': "Service",
                'identifiers': [
                    "events.amazonaws.com"
                ]
            }
        }
    ]

class EventBridgePolicyRole(iam.IAMRoleResource):
    name = "eventbridge_role"
    assume_role_policy = EventBridgePolicyDocument.get_output_attr('json')


class EventBridgeExecutionRolePolicyDocument(iam.IAMPolicyDocumentData):
    statement = [
        {
            "effect": "Allow",
            "actions": ["events:PutEvents"],
            "resources": [
                CloudWatchEventBusaws.get_output_attr('arn'),
                CloudWatchEventBusazure.get_output_attr('arn'),
                CloudWatchEventBusGcp.get_output_attr('arn'),
                CloudWatchEventBusPlugin.get_output_attr('arn')
            ]
        }
    ]


class EnventBridgePushPolicy(iam.IAMRolePolicyResource):
    name = "event_bridge_push"
    path = '/'
    policy = EventBridgeExecutionRolePolicyDocument.get_output_attr('json')

class BaseECSTaskExecPolicyAttach(iam.IAMRolePolicyAttachmentResource):
    role = EventBridgePolicyRole.get_output_attr('name')
    policy_arn = EnventBridgePushPolicy.get_output_attr('arn')
