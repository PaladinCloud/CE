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
        'name': {'required': True, 'prefix': True, 'sep': '-'},
        'lambda_config' : {
            'required' : True,
            'inline_args' :{
                'post_authentication' : {'required': True},
                'post_confirmation' : {'required':True}
                }
        },
        'schema' : {'required': True},
        'account_recovery_setting' :{'required': True},
        'auto_verified_attributes' :{'required': True},
        'admin_create_user_config' : {
            'required' : True,
            'inline_args' :{
                'allow_admin_create_user_only' : {'required':True},
                'invite_message_template': {'required':True},
                'sms_message': {'required':False},
                }
            },
        'username_attributes' : {'required':False},
        'tags':{'required': True},
        'username_configuration' : {'required': True}
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
        'user_pool_id' : {'required': True},
        'name' : {'required': True},
        'generate_secret' : {'required': True},
        'allowed_oauth_flows_user_pool_client' :{'required': False},
        'allowed_oauth_flows' : {'required': False },
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
        'user_pool_id' : {'required': True},
        'name' : {'required': True},
        'identifier' : {'required': True},
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
        'user_pool_id' : {'required': True},
        'domain' : {'required': True},
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
        'user_pool_id' : {'required': True},
        'username' : {'required': True},
        'attributes' :{'required': True},
        'lifecycle' : {'required': False}
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
        'user_pool_id' : {'required': True},
        'name' : {'required': True},
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
        'user_pool_id' : {'required': True},
        'username' : {'required': True},
        'group_name' : {'required': True}
     }

class UiCognito(TerraformResource):
    """
    Base resource class for Terraform AWS server cognito user ino cognito group resource

    Attributes:
        resource_instance_name (str): Type of resource instance
        available_args (dict): Instance configurations
    """
    resource_instance_name = "aws_cognito_user_pool_ui_customization"
    available_args = {
        'user_pool_id' : {'required': True},
        'css' : {'required': False},
        'image_file' : {'required': False}
     }


class IdentityProvider(TerraformResource):
    """
    Base resource class for Terraform AWS server cognito user ino cognito group resource

    Attributes:
        resource_instance_name (str): Type of resource instance
        available_args (dict): Instance configurations
    """
    resource_instance_name = "aws_cognito_identity_provider"
    available_args = {
        'user_pool_id' : {'required': True},
        'provider_name' : {'required': True},
        'provider_type' : {'required': True},
        'provider_details' : {'required': False},
        'attribute_mapping' : {'required': False},
     }
