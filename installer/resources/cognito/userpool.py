from core.terraform.resources.aws.cognito import CreateUserPool,UserPoolClientResources , ServerPoolResource ,UserPoolDomain, CreateGroupPool, AddUserinGroup, UserPoolResoures
from core.config import Settings
from core.log import SysLog
from resources.pacbot_app.alb import ApplicationLoadBalancer
from resources.cognito.function import AuthPostLambdaFunction


class UserPool(UserPoolResoures):
    name = "Cognito"
    post_authentication = AuthPostLambdaFunction.get_output_attr('arn')
    post_confirmation = AuthPostLambdaFunction.get_output_attr('arn')
    schema = [
                {
                    'name': 'email',
                    'attribute_data_type': 'String',
                    'required': 'true',
                    'string_attribute_constraints' : {
                        "min_length" : 0,     
                        "max_length" : 2048 
                    } 
                },     
                {
                    'name': 'tenantId',
                    'attribute_data_type': 'String',
                    'required': 'false',  
                    'string_attribute_constraints' :{
                        "min_length" : 0,      
                        "max_length" : 2048 
                    }                    
                },          
                {
                    'name': 'userRole',
                    'attribute_data_type': 'String',
                    'string_attribute_constraints' :{
                        "min_length" : 0,    
                        "max_length" : 2048 
                    }  
                },
                {
                    'name': 'defaultAssetGroup',
                    'attribute_data_type': 'String',
                    'string_attribute_constraints' :{
                        "min_length" : 0,   
                        "max_length" : 2048 
                    }  
                }
    ]
    account_recovery_setting = {
        "recovery_mechanism" : {
            'name': "verified_email",
            'priority' : 1
            }
        }
    
    auto_verified_attributes = ['email']
    allow_admin_create_user_only =  True 
    invite_message_template = {
        "email_message" : "Login to Your paladinApplication" + ApplicationLoadBalancer.get_pacbot_domain_url() + "  with username {username} and temporary password {####}",
        "email_subject" :  "PaladinApplication Invite",
        "sms_message" : "" + ApplicationLoadBalancer.get_pacbot_domain_url() + "  with username {username} and temporary password {####}",

        }

class AppCLient(UserPoolClientResources):
    user_pool_id = UserPool.get_output_attr('id')
    name = 'paladincloud'
    generate_secret =  True
    allowed_oauth_flows_user_pool_client = True
    supported_identity_providers = ['COGNITO']
    allowed_oauth_scopes = ["email", "openid","profile"]
    callback_urls = [ApplicationLoadBalancer.get_pacbot_domain_url() + "/callback"]
    logout_urls = [ApplicationLoadBalancer.get_pacbot_domain_url() + "/home"]
    write_attributes = ['email','custom:tenantId']
    allowed_oauth_flows = ["code", "implicit"]

class ServerResoures(ServerPoolResource):
    user_pool_id = UserPool.get_output_attr('id')
    name = 'API_OPERATION'
    identifier = 'paladincloud'
    scope_name = 'READ'
    scope_description =  'Read api operation'

class Appcredentials(UserPoolClientResources):
    user_pool_id = UserPool.get_output_attr('id')
    name = 'credentials'
    generate_secret =  True
    allowed_oauth_flows_user_pool_client = True
    supported_identity_providers = ['COGNITO']
    allowed_oauth_scopes = ['paladincloud/READ']
    allowed_oauth_flows = ['client_credentials']
    DEPENDS_ON = [ServerResoures]
    @classmethod
    def get_cognito_info(cls):
        info = "%s:%s" % (cls.get_output_attr('id'), cls.get_output_attr('client_secret'))
        return info


class PoolDomain(UserPoolDomain):
    user_pool_id = UserPool.get_output_attr('id')
    domain = Settings.COGNITO_DOMAIN

class CreateUser(CreateUserPool):
    user_pool_id = UserPool.get_output_attr('id')
    username = Settings.COGNITO_USER_EMAIL_ID

class CreateUserGroup(CreateGroupPool):
    user_pool_id = UserPool.get_output_attr('id')
    name = 'ROLE_USER'

class CreateAdminGroup(CreateGroupPool):
    user_pool_id = UserPool.get_output_attr('id')
    name = 'ROLE_ADMIN'

class AddusertoGroup(AddUserinGroup):
    user_pool_id = UserPool.get_output_attr('id')
    username =  CreateUser.get_output_attr('username')
    group_name = CreateAdminGroup.get_output_attr('name')

class AddusertoGroup(AddUserinGroup):
    user_pool_id = UserPool.get_output_attr('id')
    username =  CreateUser.get_output_attr('username')
    group_name = CreateUserGroup.get_output_attr('name')