from core.terraform.resources.aws.aws_lambda import LambdaFunctionResource, LambdaPermission, LambdaFunctionUrl
from resources.vpc.security_group import InfraSecurityGroupResource
from resources.pacbot_app.build_ui_and_api import BuildUiAndApis
from resources.datastore.es import ESDomain
from resources.iam.lambda_role import LambdaRole
from core.config import Settings
from resources.s3.bucket import BucketStorage
from resources.pacbot_app.alb import ApplicationLoadBalancer
from resources.notification.appsync import AppSyncNotification, AppSyncIdKey
from resources.notification.s3_upload import UploadLambdaInappFile, INAPP_NOTIFICATION_FILE_NAME
from core.terraform.resources.aws.sns import SNSResources, SNSSubscription

INVOKE_NOTIFICATION = "notification-invoke-service"
TEMPLATE_NOTIFICATION= "notification-template-formatter-service"
NOTIFICATION_LOG_TO_ES = "notification-es-logging-service"
SEND_NOTIFICATION = "notification-send-email-service"
PATH = "/"+Settings.LAMBDA_PATH+"/"

class NotificationSNS(SNSResources):
    name = "notification-event"

class EmailSNS(SNSResources):
    name = "notification-email-event" 
class InvokeNotificationFunction(LambdaFunctionResource):
    function_name = INVOKE_NOTIFICATION
    role = LambdaRole.get_output_attr('arn')
    handler =  "com.paladincloud.SendNotification::handleRequest"
    memory_size = 512
    timeout = 180
    memory_size = 512
    timeout = 180
    runtime = "java11"
    s3_bucket = BucketStorage.get_output_attr('bucket')
    s3_key = Settings.RESOURCE_NAME_PREFIX + PATH + INVOKE_NOTIFICATION + ".jar"
    environment = {
        'variables': {
            'SNS_TOPIC_ARN': NotificationSNS.get_output_attr('arn')
        }
    }
    DEPENDS_ON = [NotificationSNS,BuildUiAndApis]
    
class SendNotificationFunctionUrl(LambdaFunctionUrl):
    function_name = InvokeNotificationFunction.get_output_attr('function_name')
    authorization_type = "NONE"

class SendNotificationFunction(LambdaFunctionResource):
    function_name = SEND_NOTIFICATION
    role = LambdaRole.get_output_attr('arn')
    handler =  "com.paladincloud.InvokeNotificationsApi::handleRequest"
    runtime = "java11"
    memory_size = 512
    timeout = 180
    s3_bucket = BucketStorage.get_output_attr('bucket')
    s3_key =  Settings.RESOURCE_NAME_PREFIX +  PATH + SEND_NOTIFICATION + ".jar"
    environment = {
        'variables': {
            'AUTH_API_URL' :	"https://"+ Settings.COGNITO_DOMAIN + ".auth." + Settings.AWS_REGION + ".amazoncognito.com",
            'CONFIG_SERVER_CREDENTIALS' : "dXNlcjpwYWNtYW4=",
            'CONFIG_SERVER_URL'	: ApplicationLoadBalancer.get_api_base_url() + "/config/application/prd/latest",
            'INVOKE_NOTIFICATION_URL' : ApplicationLoadBalancer.get_api_base_url()+ "/notifications/send-plain-text-mail"     
        }
    }
    DEPENDS_ON = [BuildUiAndApis]

class TemplateFormatterFunction(LambdaFunctionResource):
    function_name = TEMPLATE_NOTIFICATION
    role = LambdaRole.get_output_attr('arn')
    handler =  "com.paladincloud.FetchNotificationSettings::handleRequest"
    runtime = "java11"
    memory_size = 512
    timeout = 180
    s3_bucket = BucketStorage.get_output_attr('bucket')
    s3_key = Settings.RESOURCE_NAME_PREFIX  +  PATH + TEMPLATE_NOTIFICATION + ".jar"
    environment = {
        'variables': {
            'AUTH_API_URL' :	"https://"+ Settings.COGNITO_DOMAIN + ".auth." + Settings.AWS_REGION + ".amazoncognito.com",
            'CONFIG_SERVER_CREDENTIALS' : "dXNlcjpwYWNtYW4=",
            'CONFIG_SERVER_URL'	: ApplicationLoadBalancer.get_api_base_url() + "/config/application/prd/latest",
            'NOTIFICATION_SETTINGS_URL'	: ApplicationLoadBalancer.get_api_base_url()+ "/admin/notifications/preferences-and-configs"
        }
    }
    DEPENDS_ON = [BuildUiAndApis]
    
class InAppNotificationFunction(LambdaFunctionResource):
    function_name = INAPP_NOTIFICATION_FILE_NAME
    role = LambdaRole.get_output_attr('arn')
    handler =  INAPP_NOTIFICATION_FILE_NAME + ".lambda_handler"
    runtime = "python3.7"
    memory_size = 512
    timeout = 180
    s3_bucket = BucketStorage.get_output_attr('bucket')
    s3_key = UploadLambdaInappFile.get_output_attr('id')
    environment = {
        'variables': {
            'API_KEY':	AppSyncIdKey.get_output_attr('key'),
            'APPSYNC_API_ENDPOINT_URL' :  AppSyncNotification.get_output_attr('uris["GRAPHQL"]')
        }
    }
    DEPENDS_ON = [AppSyncNotification,BuildUiAndApis]
    

class LogEsNotificationFunction(LambdaFunctionResource):
    function_name = NOTIFICATION_LOG_TO_ES
    role = LambdaRole.get_output_attr('arn')
    handler =  "com.paladincloud.notification_log.LogNotificationToOpenSearch::handleRequest"
    runtime = "java8"
    memory_size = 512
    timeout = 180
    s3_bucket = BucketStorage.get_output_attr('bucket')
    s3_key = Settings.RESOURCE_NAME_PREFIX + PATH + NOTIFICATION_LOG_TO_ES + ".jar"
    subnet_ids = Settings.get('VPC')['SUBNETS']
    security_group_ids = [InfraSecurityGroupResource.get_output_attr('id')]
    environment = {
        'variables': {
            'AUTH_API_URL' :	"https://"+ Settings.COGNITO_DOMAIN + ".auth." + Settings.AWS_REGION + ".amazoncognito.com",
            'CONFIG_CREDENTIALS' : "dXNlcjpwYWNtYW4=",
            'CONFIG_SERVICE_URL'	: ApplicationLoadBalancer.get_api_base_url() + "/config/rule/prd/latest",
            'ES_URI' : ESDomain.get_http_url_with_port()
        }
    }
    DEPENDS_ON = [BuildUiAndApis]
    
class TemplateSubscription(SNSSubscription):
    topic_arn = NotificationSNS.get_output_attr('arn')
    protocol = "lambda"
    endpoint =  TemplateFormatterFunction.get_output_attr('arn')
    # DEPENDS_ON = [NotificationSNS]
class InAppSubscription(SNSSubscription):
    topic_arn = NotificationSNS.get_output_attr('arn')
    protocol = "lambda"
    endpoint =  InAppNotificationFunction.get_output_attr('arn')
    # DEPENDS_ON = [NotificationSNS]

class SendSubscription(SNSSubscription):
    topic_arn = EmailSNS.get_output_attr('arn')
    protocol = "lambda"
    endpoint = SendNotificationFunction.get_output_attr('arn')
    # DEPENDS_ON = [EmailSNS]
class LogEsSubscription(SNSSubscription):
    topic_arn = NotificationSNS.get_output_attr('arn')
    protocol = "lambda"
    endpoint = LogEsNotificationFunction.get_output_attr('arn')
    # DEPENDS_ON = [EmailSNS]
    
class TemplateLambdaPermission(LambdaPermission):
    statement_id = "Event"
    action = "lambda:InvokeFunction"
    function_name = TemplateFormatterFunction.get_output_attr('function_name')
    principal = "sns.amazonaws.com"
    source_arn = NotificationSNS.get_output_attr('arn')
class InAppLambdaPermission(LambdaPermission):
    statement_id = "Event"
    action = "lambda:InvokeFunction"
    function_name = InAppNotificationFunction.get_output_attr('function_name')
    principal = "sns.amazonaws.com"
    source_arn = NotificationSNS.get_output_attr('arn')
class SendLambdaPermission(LambdaPermission):
    statement_id = "Event"
    action = "lambda:InvokeFunction"
    function_name = SendNotificationFunction.get_output_attr('function_name')
    principal = "sns.amazonaws.com"
    source_arn = EmailSNS.get_output_attr('arn')
class LogEsLambdaPermission(LambdaPermission):
    statement_id = "Event"
    action = "lambda:InvokeFunction"
    function_name = LogEsNotificationFunction.get_output_attr('function_name')
    principal = "sns.amazonaws.com"
    source_arn = NotificationSNS.get_output_attr('arn')