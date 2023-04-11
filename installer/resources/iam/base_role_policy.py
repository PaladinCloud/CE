from core.terraform.resources.aws import iam
from resources.iam.base_role import BaseRole
from resources.iam.ecs_role import ECSRole
from resources.iam.batch_role import BatchRole
from core.config import Settings


class BaseRolePolicyDocument(iam.IAMPolicyDocumentData):
    statement = [
        {
            'actions': ["sts:AssumeRole"],
            'resources': ["arn:aws:iam::" + Settings.ACCOUNT_ID + ":role/" + Settings.RESOURCE_NAME_PREFIX + "_ro"]
        }
    ]

class BaseRolePolicy(iam.IAMRolePolicyResource):
    name = "ro"
    path = '/'
    policy = BaseRolePolicyDocument.get_output_attr('json')
    lifecycle = {
        "ignore_changes" : [
        "policy"
        ]
    }


class EcsBaseRolePolicyAttach(iam.IAMRolePolicyAttachmentResource):
    role = ECSRole.get_output_attr('name')
    policy_arn = BaseRolePolicy.get_output_attr('arn')

class BaseBasepolicy(iam.IAMRolePolicyAttachmentResource):
    role = BaseRole.get_output_attr('name')
    policy_arn = BaseRolePolicy.get_output_attr('arn')
    lifecycle = {
        "ignore_changes" : [
        "policy_arn"
        ]
    }
    