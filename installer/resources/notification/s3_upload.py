from core.terraform.resources.aws.s3 import S3BucketObject
from resources.s3.bucket import BucketStorage
from core.terraform.utils import get_terraform_scripts_and_files_dir
from core.config import Settings
from resources.pacbot_app.build_ui_and_api import BuildUiAndApis
import os

FETCH_NOTIFICATION = "send-notification-service"
SEND_NOTIFICATION= "template-formatter-service"
INVOKE_NOTIFICATION = "invoke-notification-service"
INAPP_NOTIFICATION_FILE_NAME =  "paladincloud-inapp-notification"


class FetchNotificationFunctionJarFile(S3BucketObject):
    bucket = BucketStorage.get_output_attr('bucket')
    key = Settings.RESOURCE_NAME_PREFIX + "/v1/" + FETCH_NOTIFICATION + ".jar"
    source = os.path.join(
        get_terraform_scripts_and_files_dir(),
        FETCH_NOTIFICATION + ".jar")

class InvokeNotificationFunctionJarFile(S3BucketObject):
    bucket = BucketStorage.get_output_attr('bucket')
    key = Settings.RESOURCE_NAME_PREFIX + "/v1/" + INVOKE_NOTIFICATION + ".jar"
    source = os.path.join(
        get_terraform_scripts_and_files_dir(),
        SEND_NOTIFICATION + ".jar")



class SendNotificationFunctionJarFile(S3BucketObject):
    bucket = BucketStorage.get_output_attr('bucket')
    key = Settings.RESOURCE_NAME_PREFIX + "/v1/" + SEND_NOTIFICATION + ".jar"
    source = os.path.join(
        get_terraform_scripts_and_files_dir(),
        INVOKE_NOTIFICATION + ".jar")


class InAppNotificationFunctionZipFile(S3BucketObject):
    bucket = BucketStorage.get_output_attr('bucket')
    key = Settings.RESOURCE_NAME_PREFIX + "/v1/" + INAPP_NOTIFICATION_FILE_NAME + ".zip"
    source = source = os.path.join(
        get_terraform_scripts_and_files_dir(),
        INAPP_NOTIFICATION_FILE_NAME + ".zip")

