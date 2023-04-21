from core.terraform.resources import TerraformResource, TerraformData
from core.config import Settings
# from core.providers.aws.boto3 import cognito

class AppSync(TerraformResource):
    """
    Base resource class for Terraform AWS appsync resource

    Attributes:
        resource_instance_name (str): Type of resource instance
        available_args (dict): Instance configurations
    """
    resource_instance_name = "aws_appsync_graphql_api"
    available_args = {
        'name': {'required': True, 'prefix': True, 'sep': '-'},
        'authentication_type':{'required': True},
        'schema' : {'required':True},
        'tags': {'required': False},
    }
    description = Settings.RESOURCE_DESCRIPTION


class AppSyncId(TerraformResource):
    resource_instance_name = "aws_appsync_api_key"
    available_args = {
        'api_id': {'required': True},
        'expires' : {'required': False}
    }

class AppSyncDataSource(TerraformResource):
    resource_instance_name = "aws_appsync_datasource"
    available_args = {
        'api_id': {'required': True},
        'name'   :{'required': True},
        'description' : {'required': True},
        'type'  : {'required': True}
    }

class AppSyncResolvers(TerraformResource):
    resource_instance_name = "aws_appsync_resolver"
    available_args = {
        'api_id': {'required': True},
        'type'   :{'required': True},
        'field' : {'required': True},
        'data_source'  : {'required': True},
        'request_template': {'required':True},
        'response_template' :{'required':True},
    }
