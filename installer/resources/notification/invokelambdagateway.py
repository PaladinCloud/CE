from core.terraform.resources.aws.aws_webhook import ApiGateWayAuthorizer, ApiGateWayRoute, ApiGateWayStage, ApiGateway, ApiGatewayIntegration
from core.terraform.resources.aws.aws_lambda import LambdaPermission
from resources.cognito.userpool import Appcredentials, UserPool,AppCLient
from resources.notification.function import InvokeNotificationFunction
from core.config import Settings


class InvokeBasicHttpApi(ApiGateway):
    name          = "invoke-api"
    protocol_type = "HTTP"
    
    
class InvokeApiIntegration(ApiGatewayIntegration):
    api_id = InvokeBasicHttpApi.get_output_attr('id')
    integration_type = "AWS_PROXY"
    integration_uri = InvokeNotificationFunction.get_output_attr('invoke_arn')
    integration_method = "POST"
    payload_format_version = "2.0"
    DEPENDS_ON = [InvokeBasicHttpApi]
    
class InvokeApiStage(ApiGateWayStage):
    api_id = InvokeBasicHttpApi.get_output_attr('id')
    name = "$default"
    auto_deploy = "true"
        
class InvokePostLambdaPermission(LambdaPermission):
    statement_id = "post"
    action = "lambda:InvokeFunction"
    function_name = InvokeNotificationFunction.get_output_attr('function_name')
    principal = "apigateway.amazonaws.com"
    source_arn = InvokeBasicHttpApi.get_output_attr('execution_arn') + "/*/POST/"
    
class InvokeApiAuthorization(ApiGateWayAuthorizer):
    api_id             = InvokeBasicHttpApi.get_output_attr('id')
    authorizer_type    = "JWT"
    identity_sources   = ["$request.header.Authorization"]
    name               = "invoke-jwt-authorizer"
    jwt_configuration = {
    		"audience" : [AppCLient.get_output_attr('id'),Appcredentials.get_output_attr('id')],
    		"issuer"   : "https://cognito-idp." + Settings.AWS_REGION + ".amazonaws.com/" +UserPool.get_output_attr('id') 
  	}
    DEPENDS_ON = [AppCLient,UserPool]
   
class InvokeApiPostRoute(ApiGateWayRoute):
    api_id = InvokeBasicHttpApi.get_output_attr('id')
    route_key =  "POST /"
    target = "integrations/" + InvokeApiIntegration.get_output_attr('id')
    authorization_type = "JWT"
    authorizer_id = InvokeApiAuthorization.get_output_attr('id')
    # DEPENDS_ON = [InvokeNotificationFunction]