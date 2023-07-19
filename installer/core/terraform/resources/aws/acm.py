from core.terraform.resources import TerraformResource
from core.config import Settings



class AcmCertificate(TerraformResource):
    """
    Base resource class for Terraform AWS appsync resource

    Attributes:
        resource_instance_name (str): Type of resource instance
        available_args (dict): Instance configurations
    """
    resource_instance_name = "aws_acm_certificate"
    available_args = {
        'certificate_body': {'required': True, 'prefix': True, 'sep': '-'},
        'private_key':{'required': True},
        'certificate_chain' : {'required':True},
        'tags': {'required': False},
    }
    description = Settings.RESOURCE_DESCRIPTION