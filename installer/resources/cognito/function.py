from core.terraform.resources.aws.aws_lambda import LambdaFunctionResource
from resources.cognito.s3_upload import UploadLambdaPostAuthFile ,POST_AUTH_FILE_NAME
from resources.s3.bucket import BucketStorage
from resources.iam.post_auth import PostAuth

class AuthPostLambdaFunction(LambdaFunctionResource):
    function_name = "postauth"
    role = PostAuth.get_output_attr('arn')
    handler = POST_AUTH_FILE_NAME + ".lambda_handler"
    runtime = "python3.8"
    s3_bucket = BucketStorage.get_output_attr('bucket')
    s3_key = UploadLambdaPostAuthFile.get_output_attr('id') 
