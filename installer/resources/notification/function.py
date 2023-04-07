from core.terraform.resources.aws.aws_lambda import LambdaFunctionResource, LambdaPermission
from resources.datastore.es import ESDomain
from resources.iam.lambda_role import LambdaRole
from core.config import Settings
from resources.s3.bucket import BucketStorage
from resources.pacbot_app.alb import ApplicationLoadBalancer
from resources.notification.appsync import AppSyncNotification, ApiSyncIdKey
from resources.notification.s3_upload import FetchNotificationFunctionJarFile, FetchNotificationFunctionJarFile, SendNotificationFunctionJarFile, InvokeNotificationFunctionJarFile, SEND_NOTIFICATION, TEMPLATE_NOTIFICATION, INAPP_NOTIFICATION_FILE_NAME, INVOKE_NOTIFICATION, NOTIFICATION_LOG_TO_ES, GET_STAKEHOLDER_RESOURCES
from core.terraform.resources.aws.sns import SNSResoures, SNSSubscription

class NotificationSNS(SNSResoures):
    name = "notification-topic"
    # DEPENDS_ON = [SendNotificationFunction]

class EmailSNS(SNSResoures):
    name = "email-topic"
    # DEPENDS_ON = [InvokeNotificationFunction,SendNotificationFunction,TemplateFormatterFunction]

class SendNotificationFunction(LambdaFunctionResource):
    function_name = SEND_NOTIFICATION
    role = LambdaRole.get_output_attr('arn')
    handler =  "com.paladincloud.FetchNotificationSettings::handleRequest"
    runtime = "java11"
    s3_bucket = BucketStorage.get_output_attr('bucket')
    s3_key = Settings.RESOURCE_NAME_PREFIX + "/v1/" + SEND_NOTIFICATION + ".jar"
    environment = {
        'variables': {
            'SNS_TOPIC_ARN': NotificationSNS.get_output_attr('arn')
        }
    }
    DEPENDS_ON = [NotificationSNS,FetchNotificationFunctionJarFile,FetchNotificationFunctionJarFile,SendNotificationFunctionJarFile,InvokeNotificationFunctionJarFile]

class TemplateFormatterFunction(LambdaFunctionResource):
    function_name = INVOKE_NOTIFICATION
    role = LambdaRole.get_output_attr('arn')
    handler =  "com.paladincloud.InvokeNotificationsApi::handleRequest"
    runtime = "java11"
    s3_bucket = BucketStorage.get_output_attr('bucket')
    s3_key =  Settings.RESOURCE_NAME_PREFIX + "/v1/" + INVOKE_NOTIFICATION + ".jar"
    environment = {
        'variables': {
            'AUTH_API_URL' :	"https://"+ Settings.COGNITO_DOMAIN + ".auth." + Settings.AWS_REGION + ".amazoncognito.com",
            'CONFIG_SERVER_CREDENTIALS' : "dXNlcjpwYWNtYW4=",
            'CONFIG_SERVER_URL'	: ApplicationLoadBalancer.get_api_base_url() + "/config/application/prd/latest",
            'NOTIFICATION_SETTINGS_URL' : ApplicationLoadBalancer.get_api_base_url()+ "/api/admin/notifications/preferences"     
        }
    }

class InvokeNotificationFunction(LambdaFunctionResource):
    function_name = TEMPLATE_NOTIFICATION
    role = LambdaRole.get_output_attr('arn')
    handler =  "com.paladincloud.InvokeNotificationsApi::handleRequest"
    runtime = "java11"
    s3_bucket = BucketStorage.get_output_attr('bucket')
    s3_key = Settings.RESOURCE_NAME_PREFIX + "/v1/" + TEMPLATE_NOTIFICATION + ".jar"
    environment = {
        'variables': {
            'AUTH_API_URL' :	"https://"+ Settings.COGNITO_DOMAIN + ".auth." + Settings.AWS_REGION + ".amazoncognito.com",
            'INVOKE_NOTIFICATION_URL' :  ApplicationLoadBalancer.get_api_base_url ()+ "/api/notifications/send-plain-text-mail"
        }
    }
    
class InAppNotificationFunction(LambdaFunctionResource):
    function_name = INAPP_NOTIFICATION_FILE_NAME
    role = LambdaRole.get_output_attr('arn')
    handler =  INAPP_NOTIFICATION_FILE_NAME + ".lambda_handler"
    runtime = "python3.8"
    s3_bucket = BucketStorage.get_output_attr('bucket')
    s3_key = Settings.RESOURCE_NAME_PREFIX + "/v1/" + INAPP_NOTIFICATION_FILE_NAME + ".zip"
    environment = {
        'variables': {
            'API_KEY':	ApiSyncIdKey.get_output_attr('key'),
            'APPSYNC_API_ENDPOINT_URL' :  AppSyncNotification.get_output_attr('uris["GRAPHQL"]')
        }
    }
    DEPENDS_ON = [AppSyncNotification]

class LogEsNotificationFunction(LambdaFunctionResource):
    function_name = NOTIFICATION_LOG_TO_ES
    role = LambdaRole.get_output_attr('arn')
    handler =  "com.paladincloud.notification_log.LogNotificationToOpenSearch::handleRequest"
    runtime = "java8"
    s3_bucket = BucketStorage.get_output_attr('bucket')
    s3_key = Settings.RESOURCE_NAME_PREFIX + "/v1/" + NOTIFICATION_LOG_TO_ES + ".jar"
    environment = {
        'variables': {
            'AUTH_API_URL' :	"https://"+ Settings.COGNITO_DOMAIN + ".auth." + Settings.AWS_REGION + ".amazoncognito.com",
            'CONFIG_SERVER_CREDENTIALS' : "dXNlcjpwYWNtYW4=",
            'CONFIG_SERVICE_URL'	: ApplicationLoadBalancer.get_api_base_url() + "/api/config/rule/prd/latest",
            'ES_URI' : ESDomain.get_http_url_with_port()
        }
    }
    
class NotificationSubscription(SNSSubscription):
    topic_arn = NotificationSNS.get_output_attr('arn')
    protocol = "lambda"
    endpoint =  TemplateFormatterFunction.get_output_attr('arn')
    # DEPENDS_ON = [NotificationSNS]
class InAppSubscription(SNSSubscription):
    topic_arn = NotificationSNS.get_output_attr('arn')
    protocol = "lambda"
    endpoint =  InAppNotificationFunction.get_output_attr('arn')
    # DEPENDS_ON = [NotificationSNS]

class EmailSubscription(SNSSubscription):
    topic_arn = EmailSNS.get_output_attr('arn')
    protocol = "lambda"
    endpoint = InvokeNotificationFunction.get_output_attr('arn')
    # DEPENDS_ON = [EmailSNS]
class EmailSubscription(SNSSubscription):
    topic_arn = NotificationSNS.get_output_attr('arn')
    protocol = "lambda"
    endpoint = LogEsNotificationFunction.get_output_attr('arn')
    # DEPENDS_ON = [EmailSNS]
    