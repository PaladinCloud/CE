from core.terraform.resources import TerraformResource
from core.config import Settings
from core.providers.aws.boto3 import es

class KMSCMKResource(TerraformResource):
    """
    Base resource class for Terraform AWS KMS key resource

    Attributes:
        resource_instance_name (str): Type of resource instance
        available_args (dict): Instance configurations
    """
    resource_instance_name = "aws_kms_key"
    available_args = {
        'deletion_window_in_days': {'required': True},
        'policy' : {'required': True, 'type': 'json'}
    }
    description = Settings.RESOURCE_DESCRIPTION
    