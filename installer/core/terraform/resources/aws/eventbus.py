from core.terraform.resources import TerraformResource
from core.config import Settings
    
class CloudWatchEventBusResource(TerraformResource):
    """
    Base resource class for Terraform AWS Cloudwatch event rule resource

    Attributes:
        resource_instance_name (str): Type of resource instance
        available_args (dict): Instance configurations
    """
    resource_instance_name = "aws_cloudwatch_event_bus"
    available_args = {
        'name': {'required': True, 'prefix': True, 'sep': '-'},
        'tags':{'required': False}
    }
    description = Settings.RESOURCE_DESCRIPTION
