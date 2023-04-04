from core.terraform.resources.aws.aws_lambda import LambdaFunctionResource, LambdaPermission
from resources.iam.lambda_role import LambdaRole
from core.config import Settings
from resources.s3.bucket import BucketStorage
from resources.pacbot_app.alb import ApplicationLoadBalancer
from resources.notification.appsync import AppSyncNotification, ApiSyncIdKey
from resources.notification.s3_upload import FetchNotificationFunctionJarFile, FetchNotificationFunctionJarFile, SendNotificationFunctionJarFile, InvokeNotificationFunctionJarFile, FETCH_NOTIFICATION, SEND_NOTIFICATION, INAPP_NOTIFICATION_FILE_NAME, INVOKE_NOTIFICATION
from core.terraform.resources.aws.sns import SNSResoures, SNSSubscription

class NotificationSNS(SNSResoures):
    name = "notification-topic"
    # DEPENDS_ON = [SendNotificationFunction]

class EmailSNS(SNSResoures):
    name = "email-topic"
    # DEPENDS_ON = [InvokeNotificationFunction,SendNotificationFunction,TemplateFormatterFunction]

class SendNotificationFunction(LambdaFunctionResource):
    function_name = "send-notification-service"
    role = LambdaRole.get_output_attr('arn')
    handler =  "com.paladincloud.FetchNotificationSettings::handleRequest"
    runtime = "java11"
    s3_bucket = BucketStorage.get_output_attr('bucket')
    s3_key = Settings.RESOURCE_NAME_PREFIX + "/v1/" + FETCH_NOTIFICATION + ".jar"
    environment = {
        'variables': {
            'SNS_TOPIC_ARN': NotificationSNS.get_output_attr('arn')
        }
    }
    DEPENDS_ON = [NotificationSNS,FetchNotificationFunctionJarFile,FetchNotificationFunctionJarFile,SendNotificationFunctionJarFile,InvokeNotificationFunctionJarFile]



class TemplateFormatterFunction(LambdaFunctionResource):
    function_name = "template-formatter-service"
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
    function_name = "invoke-notification-service"
    role = LambdaRole.get_output_attr('arn')
    handler =  "com.paladincloud.InvokeNotificationsApi::handleRequest"
    runtime = "java11"
    s3_bucket = BucketStorage.get_output_attr('bucket')
    s3_key = Settings.RESOURCE_NAME_PREFIX + "/v1/" + SEND_NOTIFICATION + ".jar"
    environment = {
        'variables': {
            'AUTH_API_URL' :	"https://"+ Settings.COGNITO_DOMAIN + ".auth." + Settings.AWS_REGION + ".amazoncognito.com",
            'INVOKE_NOTIFICATION_URL' :  ApplicationLoadBalancer.get_api_base_url ()+ "/api/notifications/send-plain-text-mail"
        }
    }
    


class InAppNotificationFunction(LambdaFunctionResource):
    function_name = "inapp-notification-service"
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




class NotificationSubscription(SNSSubscription):
    topic_arn = NotificationSNS.get_output_attr('arn')
    protocol = "lambda"
    endpoint =  TemplateFormatterFunction.get_output_attr('arn')
    # DEPENDS_ON = [NotificationSNS]

class EmailSubscription(SNSSubscription):
    topic_arn = EmailSNS.get_output_attr('arn')
    protocol = "lambda"
    endpoint = SendNotificationFunction.get_output_attr('arn')
    # DEPENDS_ON = [EmailSNS]