from core.terraform.resources.aws import iam
from resources.iam.base_role import BaseRole
from resources.iam.ecs_role import ECSRole
from resources.iam.batch_role import BatchRole
from core.config import Settings


class BaseRolePolicyDocument(iam.IAMPolicyDocumentData):
    statement = [
        {
            'actions': ["sts:AssumeRole"],
            'resources': "arn:aws:iam::" + Settings.ACCOUNT_ID + ":role/" + Settings.RESOURCE_NAME_PREFIX
        }
    ]


class BaseRolePolicy(iam.IAMRolePolicyResource):
    name = "ro"
    path = '/'
    policy = BaseRolePolicyDocument.get_output_attr('json')


class BatchBaseRolePolicyAttach(iam.IAMRolePolicyAttachmentResource):
    role = BatchRole.get_output_attr('name')
    policy_arn = BaseRolePolicy.get_output_attr('arn')


class EcsBaseRolePolicyAttach(iam.IAMRolePolicyAttachmentResource):
    role = ECSRole.get_output_attr('name')
    policy_arn = BaseRolePolicy.get_output_attr('arn')
