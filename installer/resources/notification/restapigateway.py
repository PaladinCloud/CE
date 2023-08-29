from core.config import Settings
from core.terraform.resources.aws.aws_webhook import ApiRestGateway,ApiRestAuthorizer,ApiRestIntegration,ApiRestDeployment,ApiRestMethod,ApiRestMethodSettings,ApiRestResources
from installer.resources.cognito.userpool import UserPool
from installer.resources.notification.function import InvokeNotificationFunction

class RestGateway(ApiRestGateway):
    name = "lambda"
    
class ResourcesGateway(ApiRestResources):
    rest_api_id = RestGateway.get_output_attr('id')
    parent_id   = RestGateway.get_output_attr('root_resource_id')
    path_part   = "paladincloud"
    
class MethodGateway(ApiRestMethod):
    rest_api_id   = RestGateway.get_output_attr('id')
    resource_id   = ResourcesGateway.get_output_attr('id')
    http_method   = "POST"
    authorization = "COGNITO_USER_POOLS"

class IntegrationGateway(ApiRestIntegration):    
  rest_api_id             = RestGateway.get_output_attr('id')
  resource_id             = ResourcesGateway.get_output_attr('id')
  http_method             = MethodGateway.get_output_attr('http_method')
  integration_http_method = "POST"
  type                    = "MOCK"
  uri = InvokeNotificationFunction.get_output_attr('invoke_arn')
  
class DeploymentGateway(ApiRestDeployment):
  rest_api_id = RestGateway.get_output_attr('id')
  stage_name  = "prod" 
  DEPENDS_ON = [IntegrationGateway]

class AuthorizerGateway(ApiRestAuthorizer):
  name            = "cognito-authorizer"
  rest_api_id     = RestGateway.get_output_attr('id')
  identity_source = "method.request.header.Authorization"
  type            = "COGNITO_USER_POOLS"
  provider_arns   = [UserPool.get_output_attr('arn')]
  
class SettingsAuthorizer(ApiRestMethodSettings):
  rest_api_id = RestGateway.get_output_attr('id')
  stage_name  = "prod"
  method_path = ResourcesGateway.get_output_attr('path_part')
  http_method = MethodGateway.get_output_attr('http_method')

  settings = {
    "authorizer_id" : AuthorizerGateway.get_output_attr('id')
  }