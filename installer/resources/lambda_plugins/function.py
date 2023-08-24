from core.config import Settings
from core.terraform.resources.aws.aws_lambda import LambdaFunctionResource,  LambdaFunctionUrl

from resources.iam.lambda_role import LambdaRole
from resources.s3.bucket import BucketStorage


PATH = "/"+Settings.LAMBDA_PATH+"/"

class SubmitDataMapperLambdaFunction(LambdaFunctionResource):
    function_name = 'datamapper'
    role = LambdaRole.get_output_attr('arn')
    handler =  "com.paladincloud.datamapper.StartMapper::handleRequest"
    runtime = "java8"
    memory_size = 1024
    timeout = 180
    s3_bucket = BucketStorage.get_output_attr('bucket')
    s3_key = Settings.RESOURCE_NAME_PREFIX + PATH + "data-mapper.jar"
    environment = {
        'variables': {
            'MAPPER_BUCKET_NAME':BucketStorage.get_output_attr('bucket'),
			'DESTINATION_BUCKET':BucketStorage.get_output_attr('bucket'),
			'DESTINATION_FOLDER':"redhatacs-inventory",
			'MAPPER_FOLDER_NAME':"paladincloud/mapper"
        }
    }
	
class DataMapperFunctionUrl(LambdaFunctionUrl):
    function_name = SubmitDataMapperLambdaFunction.get_output_attr('function_name')
    authorization_type = "NONE"
