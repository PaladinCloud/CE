from core.terraform.resources.aws.s3 import S3BucketObject
from resources.s3.bucket import BucketStorage
from core.terraform.utils import get_terraform_scripts_and_files_dir
from core.config import Settings
import os

INAPP_NOTIFICATION_FILE_NAME =  "paladincloud-inapp-notification-service"
class UploadLambdaInappFile(S3BucketObject):
    bucket = BucketStorage.get_output_attr('bucket')
    key = Settings.RESOURCE_NAME_PREFIX + "/v1/" + INAPP_NOTIFICATION_FILE_NAME + ".zip"
    source = os.path.join(
        get_terraform_scripts_and_files_dir(),
        INAPP_NOTIFICATION_FILE_NAME + ".zip")
