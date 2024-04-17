from core.terraform.resources import TerraformResource
from core.config import Settings

class SQSResources(TerraformResource):
    """
    Base resource class for Terraform AWS SNS resource

    Attributes:
        resource_instance_name (str): Type of resource instance
        available_args (dict): Instance configurations
    """
    resource_instance_name = "aws_sqs_queue"
    available_args = {
        'name': {'required': True, 'prefix': True, 'sep': '-'},
        'fifo_queue': {'required': False},
        'content_based_deduplication': {'required': False},
        'deduplication_scope': {'required': False},
        'fifo_throughput_limit': {'required': False},
        'visibility_timeout_seconds' : {'required': False},
        'tags': {'required': False}
    }
    description = Settings.RESOURCE_DESCRIPTION

class SQSQueuePolicy(TerraformResource):
    """
    Base resource class for Terraform AWS SNS resource

    Attributes:
        resource_instance_name (str): Type of resource instance
        available_args (dict): Instance configurations
    """
    resource_instance_name = "aws_sqs_queue_policy"
    available_args = {
        'queue_url': {'required': True},
        'policy': {'required': False}
    }
    description = Settings.RESOURCE_DESCRIPTION