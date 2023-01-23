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

class PostAuth(iam.IAMRoleResource):
    name = "lambdarole_for_post_auth"
    assume_role_policy = LambdaRolePolicyDocument.get_output_attr('json')
    force_detach_policies = True

class PostIamPolicyDocument(iam.IAMPolicyDocumentData):
    statement = [
         {
            "effect": "Allow",
            "actions": ["logs:*"],
            "resources": ["*"]
        },
         {
            "effect": "Allow",
            "actions": ["cognito-idp:*"],
            "resources": ["*"]
        }
    ]

class PostAuthPolicyAttach(iam.IAMRolePolicyResource):
    name = "post_auth_policy"
    path ='/'
    policy = PostIamPolicyDocument.get_output_attr('json')

class PostAuthRolePolicyAttach(iam.IAMRolePolicyAttachmentResource):
    role = PostAuth.get_output_attr('name')
    policy_arn = PostAuthPolicyAttach.get_output_attr('arn')