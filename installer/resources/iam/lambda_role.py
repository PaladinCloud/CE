from core.terraform.resources.aws import iam
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


class LambdaRole(iam.IAMRoleResource):
    name = "lambda_basic_execution"
    assume_role_policy = LambdaRolePolicyDocument.get_output_attr('json')
    force_detach_policies = True


class LambdaFullAccessPolicyAttach(iam.IAMRolePolicyAttachmentResource):
    role = LambdaRole.get_output_attr('name')
    policy_arn = "arn:aws:iam::aws:policy/AWSLambda_FullAccess"


class LambdaBatchFullAccessPolicyAttach(iam.IAMRolePolicyAttachmentResource):
    role = LambdaRole.get_output_attr('name')
    policy_arn = "arn:aws:iam::aws:policy/AWSBatchFullAccess"

class LambdaCloudWatchFullAccessPolicyAttach(iam.IAMRolePolicyAttachmentResource):
    role = LambdaRole.get_output_attr('name')
    policy_arn = "arn:aws:iam::aws:policy/CloudWatchFullAccess"

class PaladinClPaladinCloudEc2PermissionDocument(iam.IAMPolicyDocumentData):
    statement = [
        {
            "effect": "Allow",
            "actions": [  "ec2:DescribeNetworkInterfaces",
                "ec2:CreateNetworkInterface",
                "ec2:DeleteNetworkInterface",
                "ec2:DescribeInstances",
                "ec2:AttachNetworkInterface"],
            "resources": ["*"]
        }
    ]
    
class PaladinCloudEc2PermissionPolicy(iam.IAMRolePolicyResource):
    name = "PaladinCloudEc2PermissionPolicy"
    path = '/'
    policy = PaladinClPaladinCloudEc2PermissionDocument.get_output_attr('json')

class PaladinCloudEc2PermissionPolicyAttach(iam.IAMRolePolicyAttachmentResource):
    role = LambdaRole.get_output_attr('name')
    policy_arn = PaladinCloudEc2PermissionPolicy.get_output_attr('arn')
