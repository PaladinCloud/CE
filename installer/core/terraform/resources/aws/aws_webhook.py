from core.terraform.resources import TerraformResource
from core.config import Settings
# from core.providers.aws.boto3 import aws_lambda


class ApiGateway(TerraformResource):
    """
    Base resource class for Terraform AWS apigateway resource

    Attributes:
        resource_instance_name (str): Type of resource instance
        available_args (dict): Instance configurations
    """
    resource_instance_name = "aws_apigatewayv2_api"
    available_args = {
        'name': {'required': True, 'prefix': True, 'sep': '-'},
        'protocol_type':{'required': True},
        'tags': {'required': False},
    }
    description = Settings.RESOURCE_DESCRIPTION

class ApiGatewayIntegration(TerraformResource):
    """
    Base resource class for Terraform AWS apigateway Integration resource

    Attributes:
        resource_instance_name (str): Type of resource instance
        available_args (dict): Instance configurations
    """
    resource_instance_name = "aws_apigatewayv2_integration"
    available_args = {
        'api_id': {'required': True},
        'integration_type':{'required': True},
        'integration_uri': {'required': True},
        'payload_format_version' : {'required': True},
        'integration_method' :  {'required': False},
        'http_method' :  {'required': False},
        'passthrough_behavior' : {'required': False}
    }
    description = Settings.RESOURCE_DESCRIPTION


class ApiGateWayStage(TerraformResource):
    """
    Base resource class for Terraform AWS apigateway Integration resource

    Attributes:
        resource_instance_name (str): Type of resource instance
        available_args (dict): Instance configurations
    """
    resource_instance_name = "aws_apigatewayv2_stage"
    available_args = {
        'api_id': {'required': True},
        'name' : {'required':True},
        'auto_deploy' : {'required':True},
        'tags': {'required': False},
    }

class ApiGateWayRoute(TerraformResource):
    """
    Base resource class for Terraform AWS apigateway route resource

    Attributes:
        resource_instance_name (str): Type of resource instance
        available_args (dict): Instance configurations
    """
    resource_instance_name = "aws_apigatewayv2_route"
    available_args = {
        'api_id': {'required': True},
        'route_key' : {'required':True},
        'target' : {'required': False},
    }
