from core.terraform.resources import TerraformResource
from core.config import Settings


class CurrentTime(TerraformResource):
    """
    Base resource class for Current Time

    Attributes:
        resource_instance_name (str): Type of resource instance
        available_args (dict): Instance configurations
    """
    resource_instance_name = "locals"
    setup_time = 60
    available_args = {
        'name': {'required': True, 'prefix': False, 'sep': '-'},
        'family': {'required': False},
        'description': {'required': False},
        'parameter': {'required': False}
    }