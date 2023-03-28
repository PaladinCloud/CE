from core.terraform.resources.aws.aws_lambda import LambdaFunctionResource, LambdaPermission
from resources.iam.notification_lambda import NotificationRole
from core.config import Settings
from resource.notification.sns import NotificationSNS

PACKAGE_AND_CLASS_PATH_NAME_REQUEST = "com.paladincloud.FetchNotificationSettings"
PACKAGE_AND_CLASS_PATH_NAME_INVOICE = "com.paladincloud.InvokeNotificationsApi"
PACKAGE_AND_CLASS_PATH_NAME_API = "com.paladincloud.InvokeNotificationsApi"

class SendNotificationFunction(LambdaFunctionResource):
    function_name = "send-notification-service"
    role = NotificationRole.get_output_attr('arn')
    handler =  PACKAGE_AND_CLASS_PATH_NAME + "::handleRequest"
    runtime = "java11"
    s3_bucket = BucketStorage.get_output_attr('bucket')
    s3_key = Settings.RESOURCE_NAME_PREFIX + "/end-notification-service.jar"
    environment = {
        'variables': {
            'SNS_TOPIC_ARN': NotificationSNS.get_output_attr('arn')
        }
    }
    DEPENDS_ON = [NotificationSNS]



class TemplateFormatterFunction(LambdaFunctionResource):
    function_name = "template-formatter-service"
    role = NotificationRole.get_output_attr('arn')
    handler =  PACKAGE_AND_CLASS_PATH_NAME_API + "::handleRequest"
    runtime = "java11"
    s3_bucket = BucketStorage.get_output_attr('bucket')
    s3_key = Settings.RESOURCE_NAME_PREFIX + "/invoke-notification-service.jar"
    environment = {
        'variables': {
            'AUTH_API_URL' :	"https://"+ Settings.COGNITO_DOMAIN + ".auth." + Settings.AWS_REGION + ".amazoncognito.com"
            'CONFIG_SERVER_CREDENTIALS':	'dXNlcjpwYWNtYW4='
            'CONFIG_SERVER_URL'	: ApplicationLoadBalancer.get_api_base_url() + "/config/application/prd/latest"
            'NOTIFICATION_SETTINGS_URL' : ApplicationLoadBalancer.get_api_base_url()+ "/api/admin/notifications/preferences"     
        }
    }


class InvokeNotificationFunction(LambdaFunctionResource):
    function_name = "invoke-notification-service"
    role = NotificationRole.get_output_attr('arn')
    handler =  PACKAGE_AND_CLASS_PATH_NAME_INVOICE + "::handleRequest"
    runtime = "java11"
    s3_bucket = BucketStorage.get_output_attr('bucket')
    s3_key = Settings.RESOURCE_NAME_PREFIX + "/invoke-notification-service.jar"
    environment = {
        'variables': {
            'AUTH_API_URL' :	"https://"+ Settings.COGNITO_DOMAIN + ".auth." + Settings.AWS_REGION + ".amazoncognito.com""
            'INVOKE_NOTIFICATION_URL' :  ApplicationLoadBalancer.get_api_base_url ()+ "/api/notifications/send-plain-text-mail"
        }
    }

