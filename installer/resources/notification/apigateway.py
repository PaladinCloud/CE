from core.terraform.resources.aws.aws_webhook import ApiGateway, ApiGatewayIntegration, ApiGateWayStage, ApiGateWayRoute
from core.config import Settings
from resources.notification.s3_upload import  UploadLambdaAcsFile, ACS_NOTIFICATION_FILE_NAME
from core.terraform.resources.aws.aws_lambda import LambdaFunctionResource, LambdaPermission, LambdaFunctionUrl
from resources.iam.acs_lambda_role import LambdaAcsRole
from resources.s3.bucket import BucketStorage
from core.terraform.resources.aws.sns import SNSResources, SNSSubscription
from resources.notification.function import InvokeNotificationFunction

class AcsSNS(SNSResources):
    name = "Acs-event"

class AcsNotification(LambdaFunctionResource):
    function_name = ACS_NOTIFICATION_FILE_NAME
    role = LambdaAcsRole.get_output_attr('arn')
    handler =  ACS_NOTIFICATION_FILE_NAME + ".handler"
    memory_size = 512
    timeout = 180
    runtime = "nodejs18.x"
    s3_bucket = BucketStorage.get_output_attr('bucket')
    s3_key = UploadLambdaAcsFile.get_output_attr('id')
    environment = {
        'variables': {
            'WEBHOOK_TOPIC' : AcsSNS.get_output_attr('arn')
        }
    }
    tracing_config = {
        'mode' : "Active"
    }
    DEPENDS_ON = [AcsSNS]

class BasicHttpApi(ApiGateway):
    name          = "acs-api"
    protocol_type = "HTTP"
    
class GetApiIntegration(ApiGatewayIntegration):
    api_id = BasicHttpApi.get_output_attr('id')
    integration_type = "AWS_PROXY"
    integration_uri = AcsNotification.get_output_attr('invoke_arn')
    integration_method = "POST"
    payload_format_version = "2.0"
    DEPENDS_ON = [AcsNotification,BasicHttpApi]
    
class PostApiIntegration(ApiGatewayIntegration):
    api_id = BasicHttpApi.get_output_attr('id')
    integration_type = "AWS_PROXY"
    integration_uri = AcsNotification.get_output_attr('invoke_arn')
    integration_method = "POST"
    payload_format_version = "2.0"
    DEPENDS_ON = [AcsNotification,BasicHttpApi]
    
class ApiStage(ApiGateWayStage):
    api_id = BasicHttpApi.get_output_attr('id')
    name = "$default"
    auto_deploy = "true"

class ApiGetRoute(ApiGateWayRoute):
    api_id = BasicHttpApi.get_output_attr('id')
    route_key =  "GET /"
    target = "integrations/" + GetApiIntegration.get_output_attr('id')
    # DEPENDS_ON = [GetApiIntegration]

class ApiPostRoute(ApiGateWayRoute):
    api_id = BasicHttpApi.get_output_attr('id')
    route_key =  "POST /"
    target = "integrations/" + PostApiIntegration.get_output_attr('id')
    # DEPENDS_ON = [PostApiIntegration]
        
class AcsPostLambdaPermission(LambdaPermission):
    statement_id = "post"
    action = "lambda:InvokeFunction"
    function_name = AcsNotification.get_output_attr('function_name')
    principal = "apigateway.amazonaws.com"
    source_arn = BasicHttpApi.get_output_attr('execution_arn') + "/*/POST/"
    
class AcsGetLambdaPermission(LambdaPermission):
    statement_id = "get"
    action = "lambda:InvokeFunction"
    function_name = AcsNotification.get_output_attr('function_name')
    principal = "apigateway.amazonaws.com"
    source_arn = BasicHttpApi.get_output_attr('execution_arn') + "/*/GET/"
    
class AcsSubscription(SNSSubscription):
    topic_arn = AcsSNS.get_output_attr('arn')
    protocol = "lambda"
    endpoint =  InvokeNotificationFunction.get_output_attr('arn')

class InvokeAcsLambdaPermission(LambdaPermission):
    statement_id = "get"
    action = "lambda:InvokeFunction"
    function_name = InvokeNotificationFunction.get_output_attr('function_name')
    principal = "sns.amazonaws.com"
    source_arn = AcsSNS.get_output_attr('arn')
    
    
    
