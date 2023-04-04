from core.terraform.resources import TerraformResource
from core.config import Settings
# from core.providers.aws.boto3 import cognito

class SNSResoures(TerraformResource):
    """
    Base resource class for Terraform AWS SNS resource

    Attributes:
        resource_instance_name (str): Type of resource instance
        available_args (dict): Instance configurations
    """
    resource_instance_name = "aws_sns_topic"
    available_args = {
        'name': {'required': True, 'prefix': True, 'sep': '-'},
    }
    description = Settings.RESOURCE_DESCRIPTION


class SNSSubscription(TerraformResource):
    """
    Base resource class for Terraform AWS SNSSubscription resource

    Attributes:
        resource_instance_name (str): Type of resource instance
        available_args (dict): Instance configurations
    """
    resource_instance_name = "aws_sns_topic_subscription"
    available_args = {
        'topic_arn' : {'required': True},
        'protocol'  : {'required': True},
        'endpoint'  : {'required': True}
    }
    description = Settings.RESOURCE_DESCRIPTION