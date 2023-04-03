from core.terraform.resources.aws.s3 import S3BucketObject
from resources.s3.bucket import BucketStorage
from core.terraform.utils import get_terraform_scripts_and_files_dir
from core.config import Settings
import os


POST_AUTH_FILE_NAME = "paladincloud-cognito"


class UploadLambdaPostAuthFile(S3BucketObject):
    bucket = BucketStorage.get_output_attr('bucket')
    key = Settings.RESOURCE_NAME_PREFIX + "/v1/" + POST_AUTH_FILE_NAME + ".zip"
    source = os.path.join(
        get_terraform_scripts_and_files_dir(),
        POST_AUTH_FILE_NAME + ".zip")
