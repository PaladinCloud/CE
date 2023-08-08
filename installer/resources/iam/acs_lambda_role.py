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


class LambdaAcsRole(iam.IAMRoleResource):
    name = "lambda_acs_execution"
    assume_role_policy = LambdaRolePolicyDocument.get_output_attr('json')
    force_detach_policies = True
