from core.terraform.resources.aws.batch import BatchJobDefinitionResource, BatchJobQueueResource
from core.providers.aws.boto3.ecs import deregister_task_definition
from core.config import Settings
from resources.lambda_submit.policy_sns import PolicyDoneSNS
from resources.lambda_submit.shipper_sqs import ShipperdoneSQS
from resources.iam.base_role import BaseRole
from resources.cognito.userpool import Appcredentials
from resources.datastore.es import ESDomain
from resources.batch.env import RuleEngineBatchJobEnv
from resources.batch.ecr import RuleEngineEcrRepository
from resources.data.aws_info import AwsAccount
from resources.pacbot_app.alb import ApplicationLoadBalancer
from resources.batch import utils
import json
import shutil


class SubmitAndRuleEngineJobDefinition(BatchJobDefinitionResource):
    name = 'rule-engine'
    jd_type = 'container'
    attempts = 2
    container_properties = json.dumps({
        'command': [
            "~/fetch_and_run.sh",
            "Ref::executableName",
            "Ref::params",
            "Ref::jvmMemParams",
            "Ref::ruleEngineExecutableName",
            "Ref::entryPoint"
        ],
        'image': RuleEngineEcrRepository.get_output_attr('repository_url'),
        'memory': Settings.get('BATCH_JOB_MEMORY', 3072),
        'vcpus': Settings.get('BATCH_JOB_VCPU', 1),
        'environment': [
            {'name': "ES_HOST", 'value': ESDomain.get_http_url_with_port()},
            {'name': "BASE_AWS_ACCOUNT", 'value': AwsAccount.get_output_attr('account_id')},
            {'name': "ES_URI", 'value': ESDomain.get_http_url_with_port()},
            {'name': "HEIMDALL_URI", 'value': ESDomain.get_http_url_with_port()},
            {'name': "PACMAN_API_URI", 'value': ApplicationLoadBalancer.get_api_base_url()},
            {'name': "CONFIG_CREDENTIALS", 'value': "dXNlcjpwYWNtYW4="},
            {'name': "CONFIG_SERVICE_URL",
             'value': ApplicationLoadBalancer.get_http_url() + "/api/config/rule,batch/prd/latest"},
            {'name': "AUTH_API_URL",
             'value': "https://" + Settings.COGNITO_DOMAIN + ".auth." + Settings.AWS_REGION + ".amazoncognito.com"},
            {'name': "POLICY_DETAILS_URL",
             'value': ApplicationLoadBalancer.get_http_url() + "/api/compliance/policy-details-for-policy-engine"},
            {'name': "AWS_STS_REGIONAL_ENDPOINTS", 'value': "regional"},
            {'name': "POLICY_DONE_SNS_TOPIC_ARN","value": PolicyDoneSNS.get_output_attr('arn')}
        ]
    })

    def post_terraform_destroy(self):
        deregister_task_definition(
            self.get_input_attr('name'),
            Settings.AWS_AUTH_CRED
        )

    def pre_terraform_destroy(self):
        compute_env = RuleEngineBatchJobEnv.get_input_attr('compute_environment_name')
        job_definition = self.get_input_attr('name')
        utils.remove_batch_job_related_resources(compute_env, job_definition)


class RuleEngineJobQueue(BatchJobQueueResource):
    name = "rule-engine"
    state = Settings.get('JOB_QUEUE_STATUS', "ENABLED")
    priority = 6
    compute_environments = [RuleEngineBatchJobEnv.get_output_attr('arn')]
    
class BatchJobsQueue(BatchJobQueueResource):
    name = "data"
    state = Settings.get('JOB_QUEUE_STATUS', "ENABLED")
    priority = 6
    compute_environments = [RuleEngineBatchJobEnv.get_output_attr('arn')]

class SubmitAndQualysJobDefinition(BatchJobDefinitionResource):
    name = 'qualys-engine'
    jd_type = 'container'
    attempts = 2
    container_properties = json.dumps({
        'command': [
            "~/fetch_and_run.sh",
            "Ref::executableName",
            "Ref::params",
            "Ref::jvmMemParams",
            "Ref::ruleEngineExecutableName",
            "Ref::entryPoint"
        ],
        'image': RuleEngineEcrRepository.get_output_attr('repository_url'),
        'memory': Settings.get('QUALYS_JOB_MEMORY', 8192),
        'vcpus': Settings.get('QUALYS_JOB_VCPU', 2),
        'environment': [
            {'name': "ES_HOST", 'value': ESDomain.get_http_url_with_port()},
            {'name': "BASE_AWS_ACCOUNT", 'value': AwsAccount.get_output_attr('account_id')},
            {'name': "ES_URI", 'value': ESDomain.get_http_url_with_port()},
            {'name': "HEIMDALL_URI", 'value': ESDomain.get_http_url_with_port()},
            {'name': "PACMAN_API_URI", 'value': ApplicationLoadBalancer.get_api_base_url()},
            {'name': "CONFIG_CREDENTIALS", 'value': "dXNlcjpwYWNtYW4="},
            {'name': "CONFIG_SERVICE_URL", 'value': ApplicationLoadBalancer.get_http_url() + "/api/config/rule,batch/prd/latest"},
            {'name': "AUTH_API_URL",'value': "https://"+ Settings.COGNITO_DOMAIN + ".auth." + Settings.AWS_REGION + ".amazoncognito.com"},
            {'name': "POLICY_DETAILS_URL", 'value': ApplicationLoadBalancer.get_http_url() + "/api/compliance/policy-details-for-policy-engine"},
            {'name': "PALADINCLOUD_RO",'value': BaseRole.get_output_attr('name')},
            {'name': "REGION",'value':Settings.AWS_REGION},
            {'name': "SHIPPER_SQS_QUEUE_URL",'value': ShipperdoneSQS.get_output_attr('url')}
        ]
    })

    def post_terraform_destroy(self):
        deregister_task_definition(
            self.get_input_attr('name'),
            Settings.AWS_AUTH_CRED
        )

    def pre_terraform_destroy(self):
        compute_env = RuleEngineBatchJobEnv.get_input_attr('compute_environment_name')
        job_definition = self.get_input_attr('name')
        utils.remove_batch_job_related_resources(compute_env, job_definition)


class QualysBatchJobsQueue(BatchJobQueueResource):
    name = "qualys"
    state = Settings.get('JOB_QUEUE_STATUS', "ENABLED")
    priority = 6
    compute_environments = [RuleEngineBatchJobEnv.get_output_attr('arn')]
