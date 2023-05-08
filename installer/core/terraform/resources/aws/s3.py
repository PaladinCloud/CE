from core.terraform.resources import TerraformResource
from core.config import Settings
import os


class S3Bucket(TerraformResource):
    """
    Base resource class for Terraform AWS S3 bucket resource

    Attributes:
        resource_instance_name (str): Type of resource instance
        available_args (dict): Instance configurations
    """
    resource_instance_name = "aws_s3_bucket"
    available_args = {
        'bucket': {'required': True, 'prefix': True, 'sep': '-'},
        'policy': {'required': False},
        'force_destroy': {'required': False},
        'tags': {'required': False}
    }
    
class S3PolicyControl(TerraformResource):
    """
    Base resource class for Terraform AWS S3 bucket resource

    Attributes:
        resource_instance_name (str): Type of resource instance
        available_args (dict): Instance configurations
    """
    resource_instance_name = "aws_s3_bucket_public_access_block"
    available_args = {
        'bucket' :{'required': True},
        'block_public_acls'       : {'required': True},
        'block_public_policy'     : {'required': False},
        'ignore_public_acls'      : {'required': False},
        'restrict_public_buckets' : {'required': False}
    }

class S3OwnershipControl(TerraformResource):
    """
    Base resource class for Terraform AWS S3 bucket resource

    Attributes:
        resource_instance_name (str): Type of resource instance
        available_args (dict): Instance configurations
    """
    resource_instance_name = "aws_s3_bucket_ownership_controls"
    available_args = {
        'bucket' : {'required':False},
        'rule' : {'required':False}
    }


class S3Acl(TerraformResource):
    """
    Base resource class for Terraform AWS S3 bucket resource

    Attributes:
        resource_instance_name (str): Type of resource instance
        available_args (dict): Instance configurations
    """
    resource_instance_name = "aws_s3_bucket_acl"
    available_args = {
        'bucket' : {'required': True},
        'acl' : {'required': True},
    }

class S3BucketPolicy(TerraformResource):
    resource_instance_name = "aws_s3_bucket_policy"
    available_args = {
        'bucket' : {'required': True},
        'policy' : {'required': True},
    }

class AwsS3Encryption(TerraformResource):
    resource_instance_name = "aws_s3_bucket_server_side_encryption_configuration"
    available_args = {
        'bucket' : {'required': False},
        'rule': {'required': False,},
    }

class S3BucketObject(TerraformResource):
    """
    Base resource class for Terraform AWS S3 bucket object resource

    Attributes:
        resource_instance_name (str): Type of resource instance
        available_args (dict): Instance configurations
    """
    resource_instance_name = "aws_s3_object"
    skip_source_exists_check = False
    available_args = {
        'bucket': {'required': True},
        'key': {'required': True},
        'source': {'required': True},
        'acl': {'required': False},
        'etag': {'required': False},
        'tags': {'required': False}
    }

    def pre_terraform_apply(self):
        if not os.path.exists(self.source) and self.skip_source_exists_check is not True:
            raise Exception("Source object not found for S3 upload. Source: %s, TF-Resource: %s" % (self.source, self.get_resource_id()))
