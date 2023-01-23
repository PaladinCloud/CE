from core.terraform.resources import TerraformResource
from core.config import Settings
# from core.providers.aws.boto3 import cognito

class UserPoolResoures(TerraformResource):
    """
    Base resource class for Terraform AWS User Pool resource

    Attributes:
        resource_instance_name (str): Type of resource instance
        available_args (dict): Instance configurations
    """
    resource_instance_name = "aws_cognito_user_pool"
    available_args = {
        'name': {'required': True, 'prefix': True, 'sep': '_'},
        'lambda_config' : {
            'required' : False,
            'inline_args' :{
                'post_authentication' : {'required': False},
                'post_confirmation' : {'required':False}
                }
        },
        'schema' : {'required': False},
        'account_recovery_setting' :{'required': False},
        'auto_verified_attributes' :{'required': False},
        'admin_create_user_config' : {
            'required' : False,
            'inline_args' :{
                'allow_admin_create_user_only' : {'required':False},
                'invite_message_template': {'required':False},
                'sms_message': {'required':False},
                }
            }
        }
    description = Settings.RESOURCE_DESCRIPTION

class UserPoolClientResources(TerraformResource):
    """
    Base resource class for Terraform AWS User Pool clinet resource
    Attributes:
        resource_instance_name (str): Type of resource instance
        available_args (dict): Instance configurations
    """
    resource_instance_name = "aws_cognito_user_pool_client"
    available_args = {
        'user_pool_id' : {'required': False},
        'name' : {'required': False},
        'generate_secret' : {'required': False},
        'allowed_oauth_flows_user_pool_client' :{'required': False},
        'allowed_oauth_flows' : {'required': False},
        'supported_identity_providers' : {'required': False},
        'allowed_oauth_scopes' : {'required': False},
        'callback_urls' : {'required': False},
        'logout_urls' : {'required': False},
        'write_attributes' :{'required': False},
    }

class ServerPoolResource(TerraformResource):
    """
    Base resource class for Terraform AWS server Pool clinet resource

    Attributes:
        resource_instance_name (str): Type of resource instance
        available_args (dict): Instance configurations
    """
    resource_instance_name = 'aws_cognito_resource_server'
    available_args = {
        'user_pool_id' : {'required': False},
        'name' : {'required': False},
        'identifier' : {'required': False},
        'allowed_oauth_flows' : {'required': False},
        'scope' :{
            'required' : False,
            'inline_args' :{
                'scope_name' : {'required': False},
                'scope_description' : {'required': False}
            }
        }
    }

class UserPoolDomain(TerraformResource):
    """
    Base resource class for Terraform AWS server domain clinet resource

    Attributes:
        resource_instance_name (str): Type of resource instance
        available_args (dict): Instance configurations
    """
    resource_instance_name = "aws_cognito_user_pool_domain"
    available_args = {
        'user_pool_id' : {'required': False},
        'domain' : {'required': False},
    }

class CreateUserPool(TerraformResource):
    """
    Base resource class for Terraform AWS server cognito user resource

    Attributes:
        resource_instance_name (str): Type of resource instance
        available_args (dict): Instance configurations
    """
    resource_instance_name = "aws_cognito_user"
    available_args = {
        'user_pool_id' : {'required': False},
        'username' : {'required': False},
    }


class CreateGroupPool(TerraformResource):
    """
    Base resource class for Terraform AWS server cognito user resource

    Attributes:
        resource_instance_name (str): Type of resource instance
        available_args (dict): Instance configurations
    """
    resource_instance_name = "aws_cognito_user_group"
    available_args = {
        'user_pool_id' : {'required': False},
        'name' : {'required': False},
    }
    description = Settings.RESOURCE_DESCRIPTION


class AddUserinGroup(TerraformResource):
    """
    Base resource class for Terraform AWS server cognito user ino cognito group resource

    Attributes:
        resource_instance_name (str): Type of resource instance
        available_args (dict): Instance configurations
    """
    resource_instance_name = "aws_cognito_user_in_group"
    available_args = {
        'user_pool_id' : {'required': False},
        'username' : {'required': False},
        'group_name' : {'required': False}
     }