from core.terraform.resources.aws.cognito import CreateUserPool,UserPoolClientResources , ServerPoolResource ,UserPoolDomain, CreateGroupPool, AddUserinGroup, UserPoolResoures, UiCognito
from core.config import Settings
from core.log import SysLog
from resources.pacbot_app.alb import ApplicationLoadBalancer
from resources.cognito.function import AuthPostLambdaFunction
from core.terraform.resources.aws.aws_lambda import LambdaPermission
import base64


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
        "email_message" : "Login to your PaladinCloud application " + ApplicationLoadBalancer.get_pacbot_domain_url() + "  with username {username} and temporary password {####}",
        "email_subject" :  "PaladinCloud Application Invite",
        "sms_message" : "" + ApplicationLoadBalancer.get_pacbot_domain_url() + "  with username {username} and temporary password {####}",

    }

class CognitoRuleLambdaPermission(LambdaPermission):
    statement_id = "Event"
    action = "lambda:InvokeFunction"
    function_name = AuthPostLambdaFunction.get_output_attr('function_name')
    principal = "cognito-idp.amazonaws.com"
    source_arn = UserPool.get_output_attr('arn')

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
    identifier = 'API_OPERATION'
    scope_name = 'READ'
    scope_description =  'Read api operation'

class Appcredentials(UserPoolClientResources):
    user_pool_id = UserPool.get_output_attr('id')
    name = 'credentials'
    generate_secret =  True
    allowed_oauth_flows_user_pool_client = True
    supported_identity_providers = ['COGNITO']
    allowed_oauth_scopes = ['API_OPERATION/READ']
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
    attributes ={ 'email':Settings.COGNITO_USER_EMAIL_ID , 'email_verified' : True, 'custom:defaultAssetGroup':'aws'}
    lifecycle = {
        "ignore_changes" : [
        "attributes"
        ]
    }

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

class CognitoUi(UiCognito):
    with open("resources/cognito/image/paladinlog.png", "rb") as image2string:
        converted_string = str(base64.b64encode(image2string.read()))
    string = converted_string[2:-1]
    user_pool_id = UserPool.get_output_attr('id')
    css = ".label-customizable {font-weight: 28px;}"
    image_file = string