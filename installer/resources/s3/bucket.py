from core.terraform.resources.aws.s3 import S3Bucket, S3Acl, AwsKmsKey,AwsS3Encryption
from core.terraform.resources.aws import iam
from core.config import Settings
from resources.iam.base_role import BaseRole
from resources.iam.ecs_role import ECSRole


class BucketStorage(S3Bucket):
    bucket = "data-" + Settings.AWS_REGION + "-" + Settings.AWS_ACCOUNT_ID
    force_destroy = True

class BucketAcl(S3Acl):
    bucket = BucketStorage.get_output_attr('id')
    acl = "private"

class KmsKey(AwsKmsKey):
    description = "created for encrypting s3 object in paladinclouds3bucket"

class BucketEncryption(AwsS3Encryption):
    bucket = BucketStorage.get_output_attr('id')
    kms_master_key_id = KmsKey.get_output_attr('id')
    sse_algorithm = "aws:kms"

class S3ResourcePolicyDocument(iam.IAMPolicyDocumentData):
    statement = [
        {
            "effect": "Allow",
            "actions": ["s3:*"],
            "resources": [
                BucketStorage.get_output_attr('arn') + "/*",  # Ex: "arn:aws:s3:::paladincloud-data-us-east-1-12345/*",
                BucketStorage.get_output_attr('arn')  # Ex: "arn:aws:s3:::paladincloud-data-us-east-1-12345"
            ],
            "Condition": [{
                    "Bool": {
                        "aws:SecureTransport": "false"
                    }
            }],
            "Principal": ["*"]
        }
    ]


class S3ResourcePolicy(iam.IAMRolePolicyResource):
    name = "s3"
    path = '/'
    policy = S3ResourcePolicyDocument.get_output_attr('json')


class S3ResourcePolicyAttachToBaseRole(iam.IAMRolePolicyAttachmentResource):
    role = BaseRole.get_output_attr('name')
    policy_arn = S3ResourcePolicy.get_output_attr('arn')


class S3ResourcePolicyAttachToBaseRoleToECSRole(iam.IAMRolePolicyAttachmentResource):
    role = ECSRole.get_output_attr('name')
    policy_arn = S3ResourcePolicy.get_output_attr('arn')
