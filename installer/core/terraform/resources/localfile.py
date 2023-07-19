from core.terraform.resources import TerraformResource
from core.config import Settings


class PrivateKeyFile(TerraformResource):
    """
    Base resource class for Terraform Local file

    Attributes:
        resource_instance_name (str): Type of resource instance
        available_args (dict): Instance configurations
    """
    resource_instance_name = "local_file"
    available_args = {
        'filename' :  {'required': False},
        'content' :  {'required': False},
    }