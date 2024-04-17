from core.terraform.resources.aws import iam
from core.config import Settings
from resources.s3.bucket import BucketStorage


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

class LambdaS3FullAccessPolicyAttach(iam.IAMRolePolicyAttachmentResource):
    role = LambdaRole.get_output_attr('name')
    policy_arn = "arn:aws:iam::aws:policy/AmazonS3FullAccess"

class PaladinCloudEc2S3permission(iam.IAMPolicyDocumentData):
    statement = [
        {
            "effect": "Allow",
            "actions": [  "ec2:DescribeNetworkInterfaces",
                "ec2:CreateNetworkInterface",
                "ec2:DeleteNetworkInterface",
                "ec2:DescribeInstances",
                "ec2:AttachNetworkInterface",
                "sqs:*"]
            "resources": ["*"]
        }
    ]
    
class PaladinCloudEc2S3PermissionPolicy(iam.IAMRolePolicyResource):
    name = "PaladinCloudEc2S3PermissionPolicy"
    path = '/'
    policy = PaladinCloudEc2S3permission.get_output_attr('json')

class PaladinCloudEc2S3PermissionPolicyAttach(iam.IAMRolePolicyAttachmentResource):
    role = LambdaRole.get_output_attr('name')
    policy_arn = PaladinCloudEc2S3PermissionPolicy.get_output_attr('arn')

