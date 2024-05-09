from core.terraform.resources.aws.aws_lambda import LambdaFunctionResource, LambdaEventSourceMapping
from core.terraform.resources.aws.cloudwatch import  CloudWatchLogGroupResource
from core.terraform.resources.variable import TerraformVariable
from core.terraform.resources.aws.sqs import SQSResources
from resources.datastore.es import ESDomainPolicy
from resources.iam.lambda_role import LambdaRole
from resources.s3.bucket import BucketStorage
from resources.batch.job import SubmitAndRuleEngineJobDefinition, RuleEngineJobQueue
from resources.data.aws_info import AwsAccount, AwsRegion
from resources.lambda_rule_engine.s3_upload import UploadLambdaRuleEngineZipFile, RULE_ENGINE_JOB_FILE_NAME
from core.config import Settings
from core.providers.aws.boto3 import cloudwatch_event
from core.mixins import MsgMixin
from resources.pacbot_app.alb import ApplicationLoadBalancer
import sys
    
class RuleEngineLambdaFunction(LambdaFunctionResource):
    function_name = "ruleengine"
    role = LambdaRole.get_output_attr('arn')
    handler = RULE_ENGINE_JOB_FILE_NAME + ".lambda_handler"
    runtime = "python3.12"
    s3_bucket = BucketStorage.get_output_attr('bucket')
    s3_key = UploadLambdaRuleEngineZipFile.get_output_attr('id')
    environment = {
        'variables': {
            'JOB_QUEUE': RuleEngineJobQueue.get_input_attr('name'),
            'JOB_DEFINITION': SubmitAndRuleEngineJobDefinition.get_output_attr('arn'),
            'CONFIG_CREDENTIALS': "dXNlcjpwYWNtYW4=",
            'CONFIG_SERVICE_URL': ApplicationLoadBalancer.get_http_url() + "/api/config/rule,batch/prd/latest",
            'CONFIG_URL': ApplicationLoadBalancer.get_http_url() + "/api/config/rule/prd/latest",
            'POLICY_DETAILS_URL' : ApplicationLoadBalancer.get_http_url() + "/api/compliance/policy-details",
            'AUTH_API_URL': "https://"+ Settings.COGNITO_DOMAIN + ".auth." + Settings.AWS_REGION + ".amazoncognito.com",
            'COMPLIANCE_URL': ApplicationLoadBalancer.get_http_url() + "/api/compliance",
            'CONFIG_APP_SERVICE_URL': ApplicationLoadBalancer.get_http_url() + "/api/config/application/prd/latest"
        }
    }
    DEPENDS_ON = [SubmitAndRuleEngineJobDefinition, RuleEngineJobQueue]

class ShipperdoneSQS(SQSResources):
    name                        = "shipper-done.fifo"
    fifo_queue                  = True
    content_based_deduplication = True
    deduplication_scope         = "messageGroup"
    visibility_timeout_seconds  = 900
    fifo_throughput_limit       = "perMessageGroupId"

class EventSourceSQS(LambdaEventSourceMapping):
    event_source_arn = ShipperdoneSQS.get_output_attr('arn')
    function_name = RuleEngineLambdaFunction.get_output_attr('arn')
    DEPENDS_ON = [ShipperdoneSQS]

class EnricherdoneSQS(SQSResources):
    name                        = "enricher-done.fifo"
    fifo_queue                  = True
    content_based_deduplication = True
    deduplication_scope         = "messageGroup"
    visibility_timeout_seconds  = 900
    fifo_throughput_limit       = "perMessageGroupId"

class EnricherSourceSQS(LambdaEventSourceMapping):
    event_source_arn = EnricherdoneSQS.get_output_attr('arn')
    function_name = RuleEngineLambdaFunction.get_output_attr('arn')
    DEPENDS_ON = [EnricherdoneSQS]

class PolicydoneSQS(SQSResources):
    name                        = "policy-done.fifo"
    fifo_queue                  = True
    content_based_deduplication = True
    deduplication_scope         = "messageGroup"
    visibility_timeout_seconds  = 900
    fifo_throughput_limit       = "perMessageGroupId"

class PolicySourceSQS(LambdaEventSourceMapping):
    event_source_arn = PolicydoneSQS.get_output_attr('arn')
    function_name = RuleEngineLambdaFunction.get_output_attr('arn')
    DEPENDS_ON = [PolicydoneSQS]

class RuleEngineCloudWatchLogGroup(CloudWatchLogGroupResource):
    name = "/aws/lambda/" + RuleEngineLambdaFunction.get_output_attr('function_name')
    retention_in_days = Settings.RETENTION_IN_DAYS  