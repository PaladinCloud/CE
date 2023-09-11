from core.terraform.resources.aws.s3 import S3BucketObject
from resources.s3.bucket import BucketStorage
from core.terraform.utils import get_terraform_scripts_and_files_dir
from core.config import Settings
import os


BATCH_JOB_FILE_NAME = "paladincloud-submitBatchjob"
PATH = "/"+Settings.LAMBDA_PATH+"/"

class UploadLambdaSubmitJobZipFile(S3BucketObject):
    bucket = BucketStorage.get_output_attr('bucket')
    key = Settings.RESOURCE_NAME_PREFIX + PATH + BATCH_JOB_FILE_NAME + ".zip"
    source = os.path.join(
        get_terraform_scripts_and_files_dir(),
        BATCH_JOB_FILE_NAME + ".zip")

BATCH_LONGRUNNING_JOB_FILE_NAME = "paladincloud-longrunningjob"

class UploadLambdaSubmitJobZipFile(S3BucketObject):
    bucket = BucketStorage.get_output_attr('bucket')
    key = Settings.RESOURCE_NAME_PREFIX + PATH + BATCH_LONGRUNNING_JOB_FILE_NAME + ".zip"
    source = os.path.join(
        get_terraform_scripts_and_files_dir(),
        BATCH_LONGRUNNING_JOB_FILE_NAME + ".zip")