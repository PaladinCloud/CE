from core.terraform.resources.aws import iam
from resources.iam.base_role import BaseRole
from core.config import Settings



class LambdaRolePolicyDocument(iam.IAMPolicyDocumentData):
    statement = [
        {
            'actions': ["sts:AssumeRole"],
            'principals': [
                {
                    'type': "Service",
                    'identifiers': ["lambda.amazonaws.com"]
                }
            ],
            'effect': "Allow"
        }
    ]

class NotificationRole(iam.IAMRoleResource):
    name = "lambda_notification"
    assume_role_policy = LambdaRolePolicyDocument.get_output_attr('json')
    force_detach_policies = True

class NotificationPolicyDocument(iam.IAMPolicyDocumentData):
    statement = [
         {
            "effect": "Allow",
            "actions": [
                "ec2:DescribeNetworkInterfaces",
                "ec2:CreateNetworkInterface",
                "ec2:DeleteNetworkInterface",
                "ec2:DescribeInstances",
                "ec2:AttachNetworkInterface"
            ],
            "resources": ["*"]
        }
    ]

class NotificationPolicyAttach(iam.IAMRolePolicyResource):
    name = "notification_vpc_policy"
    path ='/'
    policy = NotificationPolicyDocument.get_output_attr('json')

class NotificationPolicyAttach(iam.IAMRolePolicyAttachmentResource):
    role = NotificationRole.get_output_attr('name')
    policy_arn = NotificationPolicyAttach.get_output_attr('arn')

class NotificationBasePolicyAttach(iam.IAMRolePolicyAttachmentResource):
    role = NotificationRole.get_output_attr('name')
    policy_arn = base_role.get_output_attr('arn')

class NotiCloudWatchFullAccess(iam.IAMRolePolicyAttachmentResource):
    role = NotificationRole.get_output_attr('name')
    policy_arn = "arn:aws:iam::aws:policy/CloudWatchFullAccess"

class NotiAWSBatchFullAccess(iam.IAMRolePolicyAttachmentResource):
    role = NotificationRole.get_output_attr('name')
    policy_arn = "arn:aws:iam::aws:policy/AWSBatchFullAccess"

class NotiAWSLambda_FullAccess(iam.IAMRolePolicyAttachmentResource):
    role = NotificationRole.get_output_attr('name')
    policy_arn = "arn:aws:iam::aws:policy/AWSLambda_FullAccess"