from core.terraform.resources.aws.aws_lambda import LambdaFunctionResource, LambdaPermission
from core.terraform.resources.aws.cloudwatch import CloudWatchEventRuleResource, CloudWatchEventTargetResource
from resources.cognito.userpool import Appcredentials
from resources.datastore.es import ESDomainPolicy
from resources.datastore.es import ESDomain
from resources.datastore.db import MySQLDatabase
from resources.iam.lambda_role import LambdaRole
from resources.iam.base_role import BaseRole
from resources.s3.bucket import BucketStorage
from resources.batch.job import SubmitAndRuleEngineJobDefinition, BatchJobsQueue
from resources.data.aws_info import AwsAccount, AwsRegion
from resources.lambda_submit.s3_upload import UploadLambdaSubmitJobZipFile, BATCH_JOB_FILE_NAME
from resources.pacbot_app.alb import ApplicationLoadBalancer
from resources.eventbus.custom_event_bus import CloudWatchEventBusaws, CloudWatchEventBusgcp, CloudWatchEventBusazure
from resources.pacbot_app.utils import  get_azure_tenants,  get_gcp_project_ids, get_aws_account_details
import json
from core.config import Settings


class SubmitJobLambdaFunction(LambdaFunctionResource):
    function_name = "datacollector"
    role = LambdaRole.get_output_attr('arn')
    handler = BATCH_JOB_FILE_NAME + ".lambda_handler"
    runtime = "python3.8"
    s3_bucket = BucketStorage.get_output_attr('bucket')
    s3_key = UploadLambdaSubmitJobZipFile.get_output_attr('id') 
    environment = {
        'variables': {
            'JOB_QUEUE': BatchJobsQueue.get_input_attr('name'),
            'JOB_DEFINITION': SubmitAndRuleEngineJobDefinition.get_output_attr('arn'),
            'CONFIG_URL': ApplicationLoadBalancer.get_api_base_url() + "/config/batch,inventory/prd/latest",
            'CONFIG_CREDENTIALS': "dXNlcjpwYWNtYW4=",
            'CONFIG_SERVICE_URL': ApplicationLoadBalancer.get_http_url() + "/api/config/rule/prd/latest"
        }
    }

    DEPENDS_ON = [SubmitAndRuleEngineJobDefinition, BatchJobsQueue]


class DataCollectorEventRule(CloudWatchEventRuleResource):
    name = "AWS-Data-Collector"
    event_bus_name = CloudWatchEventBusaws.get_output_attr('arn')
    event_pattern = {
    "detail-type": [Settings.JOB_DETAIL_TYPE],
    "source": [Settings.JOB_SOURCE],
    "detail": {
        "batchNo": [1],
        "cloudName": ["aws"],
        "isCollector": [True],
        "isShipper":[False],
        "isRule": [False],
        "submitJob": [True]
        }
    }
    DEPENDS_ON = [SubmitJobLambdaFunction]


class DataCollectorEventRuleLambdaPermission(LambdaPermission):
    statement_id = "AllowExecutionFromDataCollectorEvent"
    action = "lambda:InvokeFunction"
    function_name = SubmitJobLambdaFunction.get_output_attr('function_name')
    principal = "events.amazonaws.com"
    source_arn = DataCollectorEventRule.get_output_attr('arn')


class DataCollectorCloudWatchEventTarget(CloudWatchEventTargetResource):
    rule = DataCollectorEventRule.get_output_attr('name')
    arn = SubmitJobLambdaFunction.get_output_attr('arn')
    event_bus_name = CloudWatchEventBusaws.get_output_attr('arn')
    target_id = 'DataCollectorTarget'  # Unique identifier
    target_input = json.dumps({
        'jobName': "AWS-Data-Collector",
        'jobUuid': "pacman-aws-inventory-jar-with-dependencies",
        'jobType': "jar",
        'jobDesc': "AWS-Data-Collection",
        'environmentVariables': [
            {'name': "CONFIG_URL", 'value': ApplicationLoadBalancer.get_api_base_url(
            ) + "/config/batch,inventory/prd/latest"},
            {'name': "CONFIG_CREDENTIALS", 'value': "dXNlcjpwYWNtYW4="},
            {'name': "CONFIG_SERVICE_URL", 'value': ApplicationLoadBalancer.get_http_url(
            ) + "/api/config/rule/prd/latest"}
        ],
        'params': [
            {'encrypt': False, 'key': "package_hint",
                'value': "com.tmobile.cso.pacman"},
            {'encrypt': False, 'key': "config_creds", 'value': "dXNlcjpwYWNtYW4="},
#             {'encrypt': False, 'key': "accountinfo",
#                 'value': AwsAccount.get_output_attr('account_id')},
            {'encrypt': False, 'key': "accountinfo",
                                'value': get_aws_account_details()},
            {'encrypt': False,'key':"AUTH_API_URL",'value': "https://"+ Settings.COGNITO_DOMAIN + ".auth." + Settings.AWS_REGION + ".amazoncognito.com"}
        ]
    })

class DataShipperEventRule(CloudWatchEventRuleResource):
    name = "aws-redshift-es-data-shipper"
    event_bus_name = CloudWatchEventBusaws.get_output_attr('arn')
    event_pattern = {
    "detail-type": [Settings.JOB_DETAIL_TYPE],
    "source": [Settings.JOB_SOURCE],
    "detail": {
        "batchNo": [1],
        "cloudName": ["aws"],
        "isCollector": [False],
        "isShipper":[True],
        "isRule": [False],
        "submitJob": [True]
        }
    }
    DEPENDS_ON = [SubmitJobLambdaFunction, ESDomainPolicy]


class DataShipperEventRuleLambdaPermission(LambdaPermission):
    statement_id = "AllowExecutionFromDataShipper"
    action = "lambda:InvokeFunction"
    function_name = SubmitJobLambdaFunction.get_output_attr('function_name')
    principal = "events.amazonaws.com"
    source_arn = DataShipperEventRule.get_output_attr('arn')


class DataShipperCloudWatchEventTarget(CloudWatchEventTargetResource):
    rule = DataShipperEventRule.get_output_attr('name')
    arn = SubmitJobLambdaFunction.get_output_attr('arn')
    event_bus_name = CloudWatchEventBusaws.get_output_attr('arn')
    target_id = 'DataShipperTarger'  # Unique identifier
    target_input = json.dumps({
        'jobName': "aws-redshift-es-data-shipper",
        'jobUuid': "data-shipper-jar-with-dependencies",
        'jobType': "jar",
        'jobDesc': "Ship aws data periodically from redshfit to ES",
        'environmentVariables': [
            {'name': "CONFIG_URL", 'value': ApplicationLoadBalancer.get_api_base_url(
            ) + "/config/batch,data-shipper/prd/latest"},
            {'name': "ASSET_API_URL",
                'value': ApplicationLoadBalancer.get_api_version_url('asset')},
            {'name': "CMPL_API_URL",
                'value': ApplicationLoadBalancer.get_api_version_url('compliance')},
            # {'name': "AUTH_API_URL",
            #     'value': ApplicationLoadBalancer.get_api_version_url('auth')},
            {'name': "CONFIG_CREDENTIALS", 'value': "dXNlcjpwYWNtYW4="},
            {'name': "CONFIG_SERVICE_URL", 'value': ApplicationLoadBalancer.get_http_url(
            ) + "/api/config/rule/prd/latest"}
        ] + ([{
            'name': "VULN_API_URL",
            'value': ApplicationLoadBalancer.get_api_version_url('vulnerability')}
        ]),
        'params': [
            {'encrypt': False, 'key': "package_hint", 'value': "com.tmobile"},
            {'encrypt': False, 'key': "datasource", 'value': "aws"},
            {'encrypt': False, 'key': "config_creds", 'value': "dXNlcjpwYWNtYW4="},
            {'encrypt': False,'key': "AUTH_API_URL",'value': "https://"+ Settings.COGNITO_DOMAIN + ".auth." + Settings.AWS_REGION + ".amazoncognito.com"}
        ]
    })


class RecommendationsCollectorEventRule(CloudWatchEventRuleResource):
    name = "AWS-Recommendations-Collector"
    event_bus_name = CloudWatchEventBusaws.get_output_attr('arn')
    event_pattern = {
    "detail-type": [Settings.JOB_DETAIL_TYPE],
    "source": [Settings.JOB_SOURCE],
    "detail": {
        "batchNo": [1],
        "cloudName": ["aws"],
        "isCollector": [False],
        "isShipper":[True],
        "isRule": [False],
        "submitJob": [True]
        }
    }
    DEPENDS_ON = [SubmitJobLambdaFunction]


class RecommendationsCollectorEventRuleLambdaPermission(LambdaPermission):
    statement_id = "AllowExecutionFromRecommendationsCollectorEvent"
    action = "lambda:InvokeFunction"
    function_name = SubmitJobLambdaFunction.get_output_attr('function_name')
    principal = "events.amazonaws.com"
    source_arn = RecommendationsCollectorEventRule.get_output_attr('arn')


class RecommendationsCollectorCloudWatchEventTarget(CloudWatchEventTargetResource):
    rule = RecommendationsCollectorEventRule.get_output_attr('name')
    arn = SubmitJobLambdaFunction.get_output_attr('arn')
    event_bus_name = CloudWatchEventBusaws.get_output_attr('arn')
    target_id = 'RecommendationsCollectorTarget'  # Unique identifier
    target_input = json.dumps({
        'jobName': "aws-recommendations-collector",
        'jobUuid': "recommendation-enricher-jar-with-dependencies",
        'jobType': "jar",
        'jobDesc': "Index trusted advisor checks as recommendations",
        'environmentVariables': [
            {'name': "CONFIG_URL", 'value': ApplicationLoadBalancer.get_api_base_url(
            ) + "/config/batch,recommendation-enricher/prd/latest"},
            {'name': "PACMAN_API_URI",
                'value': ApplicationLoadBalancer.get_api_base_url()},
            {'name': "LOGGING_ES_HOST_NAME",
                'value': ESDomain.get_http_url_with_port()},
            {'name': "ES_URI", 'value': ESDomain.get_http_url_with_port()},
            {'name': "ENVIRONMENT", 'value': "prd"},
            {'name': "APP_NAME", 'value': "aws-recommendations-collector"},
            {'name': "APP_TYPE", 'value': "etl"},
            {'name': "HEIMDALL_URI", 'value': ESDomain.get_http_url_with_port()},
            {'name': "BASE_AWS_ACCOUNT",
                'value': AwsAccount.get_output_attr('account_id')},
        ],
        'params': [
            {'encrypt': False, 'key': "package_hint",
                'value': "com.tmobile.cso.pacbot"},
            {'encrypt': False, 'key': "config_creds", 'value': "dXNlcjpwYWNtYW4="}
        ]
    })


class CloudNotificationCollectorEventRule(CloudWatchEventRuleResource):
    name = "AWS-CloudNotification-Collector"
    event_bus_name = CloudWatchEventBusaws.get_output_attr('arn')
    event_pattern = {
    "detail-type": [Settings.JOB_DETAIL_TYPE],
    "source": [Settings.JOB_SOURCE],
    "detail": {
        "batchNo": [1],
        "cloudName": ["aws"],
        "isCollector": [False],
        "isShipper":[True],
        "isRule": [False],
        "submitJob": [True]
        }
    }
    DEPENDS_ON = [SubmitJobLambdaFunction]


class CloudNotificationCollectorEventRuleLambdaPermission(LambdaPermission):
    statement_id = "AllowExecutionFromCloudNotificationCollectorEvent"
    action = "lambda:InvokeFunction"
    function_name = SubmitJobLambdaFunction.get_output_attr('function_name')
    principal = "events.amazonaws.com"
    source_arn = CloudNotificationCollectorEventRule.get_output_attr('arn')


class CloudNotificationCollectorCloudWatchEventTarget(CloudWatchEventTargetResource):
    rule = CloudNotificationCollectorEventRule.get_output_attr('name')
    arn = SubmitJobLambdaFunction.get_output_attr('arn')
    event_bus_name = CloudWatchEventBusaws.get_output_attr('arn')
    target_id = 'CloudNotificationCollectorTarget'  # Unique identifier
    target_input = json.dumps({
        'jobName': "aws-cloud-notification-collector",
        'jobUuid': "pacman-cloud-notifications-jar-with-dependencies",
        'jobType': "jar",
        'jobDesc': "Health Notification Collector",
        'environmentVariables': [
            {'name': "CONFIG_URL", 'value': ApplicationLoadBalancer.get_api_base_url(
            ) + "/config/api/prd/latest"},
            {'name': "PACMAN_API_URI",
                'value': ApplicationLoadBalancer.get_api_base_url()},
            {'name': "LOGGING_ES_HOST_NAME",
                'value': ESDomain.get_http_url_with_port()},
            {'name': "ES_URI", 'value': ESDomain.get_http_url_with_port()},
            {'name': "ENVIRONMENT", 'value': "prd"},
            {'name': "APP_NAME", 'value': "aws-cloud-notification-collector"},
            {'name': "APP_TYPE", 'value': "etl"},
            {'name': "BASE_AWS_ACCOUNT",
                'value': AwsAccount.get_output_attr('account_id')},
        ],
        'params': [
            {'encrypt': False, 'key': "package_hint", 'value': "com.tmobile"},
            {'encrypt': False, 'key': "config_creds", 'value': "dXNlcjpwYWNtYW4="},
            {'encrypt': False, 'key': "conf_src",
                'value': "api-prd,application-prd"},
        ]
    })


class QualysKBCollectorEventRule(CloudWatchEventRuleResource):
    name = "qualys-kb-collector"
    schedule_expression = "cron(0 0 * * ? *)"
    DEPENDS_ON = [SubmitJobLambdaFunction]
    # PROCESS = need_to_deploy_vulnerability_service()


class QualysKBCollectorEventRuleLambdaPermission(LambdaPermission):
    statement_id = "AllowExecutionFromQualysKBCollectorEvent"
    action = "lambda:InvokeFunction"
    function_name = SubmitJobLambdaFunction.get_output_attr('function_name')
    principal = "events.amazonaws.com"
    source_arn = QualysKBCollectorEventRule.get_output_attr('arn')
    # PROCESS = need_to_deploy_vulnerability_service()


class QualysKBCollectorCloudWatchEventTarget(CloudWatchEventTargetResource):
    rule = QualysKBCollectorEventRule.get_output_attr('name')
    arn = SubmitJobLambdaFunction.get_output_attr('arn')
    target_id = 'QualysKBCollectorTarget'  # Unique identifier
    target_input = json.dumps({
        'jobName': "qualys-kb-collector",
        'jobUuid': "qualys-kb-collector",
        'jobType': "jar",
        'jobDesc': "Qualys KB Collector",
        'environmentVariables': [
            {'name': "CONFIG_URL", 'value': ApplicationLoadBalancer.get_api_base_url(
            ) + "/config/batch,qualys-enricher/prd/latest"},
        ],
        'params': [
            {'encrypt': False, 'key': "package_hint", 'value': "com.tmobile"},
            {'encrypt': False, 'key': "config_creds", 'value': "dXNlcjpwYWNtYW4="},
            {'encrypt': False, 'key': "job_hint", 'value': "qualys-kb"},
        ]
    })

    # PROCESS = need_to_deploy_vulnerability_service()


class QualysAssetDataImporterEventRule(CloudWatchEventRuleResource):
    name = "qualys-asset-data-importer"
    schedule_expression = "cron(0 1 * * ? *)"
    DEPENDS_ON = [SubmitJobLambdaFunction]
    # PROCESS = need_to_deploy_vulnerability_service()


class QualysAssetDataImporterEventRuleLambdaPermission(LambdaPermission):
    statement_id = "AllowExecutionFromQualysAssetDataImporterEvent"
    action = "lambda:InvokeFunction"
    function_name = SubmitJobLambdaFunction.get_output_attr('function_name')
    principal = "events.amazonaws.com"
    source_arn = QualysAssetDataImporterEventRule.get_output_attr('arn')
    # PROCESS = need_to_deploy_vulnerability_service()


class QualysAssetDataImporterCloudWatchEventTarget(CloudWatchEventTargetResource):
    rule = QualysAssetDataImporterEventRule.get_output_attr('name')
    arn = SubmitJobLambdaFunction.get_output_attr('arn')
    target_id = 'QualysAssetDataImporterTarget'  # Unique identifier
    target_input = json.dumps({
        'jobName': "qualys-asset-data-importer",
        'jobUuid': "qualys-asset-data-importer",
        'jobType': "jar",
        'jobDesc': "Qualys Asset Data Importer",
        'environmentVariables': [
            {'name': "CONFIG_URL", 'value': ApplicationLoadBalancer.get_api_base_url(
            ) + "/config/batch,qualys-enricher/prd/latest"},
        ],
        'params': [
            {'encrypt': False, 'key': "package_hint", 'value': "com.tmobile"},
            {'encrypt': False, 'key': "config_creds", 'value': "dXNlcjpwYWNtYW4="},
            {'encrypt': False, 'key': "job_hint", 'value': "qualys"},
            {'encrypt': False, 'key': "server_type", 'value': "ec2"},
            {'encrypt': False, 'key': "datasource", 'value': "aws"}
        ]
    })
    # PROCESS = need_to_deploy_vulnerability_service()

class AquaImageVulnerabilityCollectorEventRule(CloudWatchEventRuleResource):
    name = "aqua-image-vulnerability-collector"
    schedule_expression = "cron(0 0 * * ? *)"
    DEPENDS_ON = [SubmitJobLambdaFunction]



class AquaImageVulnerabilityCollectorLambdaPermission(LambdaPermission):
    statement_id = "AllowExecutionFromAquaImageVulnerabilityCollectorEvent"
    action = "lambda:InvokeFunction"
    function_name = SubmitJobLambdaFunction.get_output_attr('function_name')
    principal = "events.amazonaws.com"
    source_arn = AquaImageVulnerabilityCollectorEventRule.get_output_attr('arn')



class AquaImageVulnerabilityCollectorCloudWatchEventTarget(CloudWatchEventTargetResource):
    rule = AquaImageVulnerabilityCollectorEventRule.get_output_attr('name')
    arn = SubmitJobLambdaFunction.get_output_attr('arn')
    target_id = 'AquaImageVulnerabilityCollectorTarget'  # Unique identifier
    target_input = json.dumps({
        'jobName': "aqua-image-vulnerability-collector",
        'jobUuid': "aqua-collector",
        'jobType': "jar",
        'jobDesc': "Aqua Image Vulnerability Collector",
        'environmentVariables': [
            {'name': "CONFIG_URL", 'value': ApplicationLoadBalancer.get_api_base_url(
            ) + "/config/batch,aqua-enricher/prd/latest"},
        ],
        'params': [
            {'encrypt': False, 'key': "package_hint", 'value': "com.tmobile"},
            {'encrypt': False, 'key': "config_creds", 'value': "dXNlcjpwYWNtYW4="},
            {'encrypt': False, 'key': "job_hint", 'value': "aqua_image_vulnerability"},
        ]
    })


class TenableVMVulnerabilityCollectorEventRule(CloudWatchEventRuleResource):
    name = "tenable-vm-vulnerability-collector"
    schedule_expression = "cron(0 0 * * ? *)"
    DEPENDS_ON = [SubmitJobLambdaFunction]



class TenableVMVulnerabilityCollectorLambdaPermission(LambdaPermission):
    statement_id = "AllowExecutionFromTenableVMVulnerabilityCollectorEvent"
    action = "lambda:InvokeFunction"
    function_name = SubmitJobLambdaFunction.get_output_attr('function_name')
    principal = "events.amazonaws.com"
    source_arn = TenableVMVulnerabilityCollectorEventRule.get_output_attr('arn')



class TenableVMVulnerabilityCollectorCloudWatchEventTarget(CloudWatchEventTargetResource):
    rule = TenableVMVulnerabilityCollectorEventRule.get_output_attr('name')
    arn = SubmitJobLambdaFunction.get_output_attr('arn')
    target_id = 'TenableVMVulnerabilityCollectorTarget'  # Unique identifier
    target_input = json.dumps({
        'jobName': "tenable-vm-vulnerability-collector",
        'jobUuid': "tenable-collector",
        'jobType': "jar",
        'jobDesc': "Tenable VM Vulnerability Collector",
        'environmentVariables': [
            {'name': "CONFIG_URL", 'value': ApplicationLoadBalancer.get_api_base_url(
            ) + "/config/batch,tenable-enricher/prd/latest"},
        ],
        'params': [
            {'encrypt': False, 'key': "package_hint", 'value': "com.tmobile"},
            {'encrypt': False, 'key': "config_creds", 'value': "dXNlcjpwYWNtYW4="},
            {'encrypt': False, 'key': "job_hint", 'value': "tenable_vm_vulnerability"},
            {'encrypt': False, 'key': "days", 'value': "7"}
        ]
    })

class AzureDataCollectorEventRule(CloudWatchEventRuleResource):
    name = "azure-discovery"
    event_bus_name = CloudWatchEventBusazure.get_output_attr('arn')
    event_pattern = {
    "detail-type": [Settings.JOB_DETAIL_TYPE],
    "source": [Settings.JOB_SOURCE],
    "detail": {
        "batchNo": [1],
        "cloudName": ["azure"],
        "isCollector": [True],
        "isShipper": [False],
        "isRule": [False],
        "submitJob": [True]
        }
    }
    DEPENDS_ON = [SubmitJobLambdaFunction]
    # PROCESS = need_to_enable_azure()


class AzureDataCollectorEventRuleLambdaPermission(LambdaPermission):
    statement_id = "AllowExecutionFromAzureDataCollectorEvent"
    action = "lambda:InvokeFunction"
    function_name = SubmitJobLambdaFunction.get_output_attr('function_name')
    principal = "events.amazonaws.com"
    source_arn = AzureDataCollectorEventRule.get_output_attr('arn')
    # PROCESS = need_to_enable_azure()


class AzureDataCollectorCloudWatchEventTarget(CloudWatchEventTargetResource):
    rule = AzureDataCollectorEventRule.get_output_attr('name')
    arn = SubmitJobLambdaFunction.get_output_attr('arn')
    event_bus_name = CloudWatchEventBusazure.get_output_attr('arn')
    target_id = 'AzureDataCollectorTarget'  # Unique identifier
    target_input = json.dumps({
        'jobName': "pacbot-azure-discovery",
        'jobUuid': "pacbot-azure-discovery",
        'jobType': "jar",
        'jobDesc': "Collects azure data and upload to S3",
        'environmentVariables': [
            {'name': "CONFIG_URL", 'value': ApplicationLoadBalancer.get_api_base_url(
            ) + "/config/batch,azure-discovery/prd/latest"},
        ],
        'params': [
            {'encrypt': False, 'key': "package_hint",
                'value': "com.tmobile.pacbot"},
            {'encrypt': False, 'key': "file.path",
                'value': "/home/ec2-user/azure-data"},
            {'encrypt': False, 'key': "config_creds", 'value': "dXNlcjpwYWNtYW4="},
            {'encrypt': False, 'key': "tenants", 'value': get_azure_tenants()}
        ]
    })
    # PROCESS = need_to_enable_azure()


class AzureDataShipperEventRule(CloudWatchEventRuleResource):
    name = "data-shipper-azure"
    event_bus_name = CloudWatchEventBusazure.get_output_attr('arn')
    event_pattern = {
    "detail-type": [Settings.JOB_DETAIL_TYPE],
    "source": [Settings.JOB_SOURCE],
    "detail": {
        "batchNo": [1],
        "cloudName": ["azure"],
        "isCollector": [False],
        "isShipper":[True],
        "isRule": [False],
        "submitJob": [True]
        }
    }
    DEPENDS_ON = [SubmitJobLambdaFunction, ESDomainPolicy]
    # PROCESS = need_to_enable_azure()


class AzureDataShipperEventRuleLambdaPermission(LambdaPermission):
    statement_id = "AllowExecutionFromAzureDataShipper"
    action = "lambda:InvokeFunction"
    function_name = SubmitJobLambdaFunction.get_output_attr('function_name')
    principal = "events.amazonaws.com"
    source_arn = AzureDataShipperEventRule.get_output_attr('arn')
    # PROCESS = need_to_enable_azure()


class AzureDataShipperCloudWatchEventTarget(CloudWatchEventTargetResource):
    rule = AzureDataShipperEventRule.get_output_attr('name')
    arn = SubmitJobLambdaFunction.get_output_attr('arn')
    event_bus_name = CloudWatchEventBusazure.get_output_attr('arn')
    target_id = 'AzureDataShipperTarget'  # Unique identifier
    target_input = json.dumps({
        'jobName': "data-shipper-azure",
        'jobUuid': "data-shipper-azure",
        'jobType': "jar",
        'jobDesc': "Ship Azure Data from S3 to PacBot ES",
        'environmentVariables': [
            {'name': "CONFIG_URL", 'value': ApplicationLoadBalancer.get_api_base_url(
            ) + "/config/batch,azure-discovery/prd/latest"},
        ],
        'params': [
            {'encrypt': False, 'key': "package_hint",
                'value': "com.tmobile.cso.pacman"},
            {'encrypt': False, 'key': "config_creds", 'value': "dXNlcjpwYWNtYW4="},
            {'encrypt': False, 'key': "datasource", 'value': "azure"},
            {'encrypt': False, 'key': "s3.data", 'value': "azure-inventory"}
        ]
    })
    # PROCESS = need_to_enable_azure()


class GCPDataCollectorEventRule(CloudWatchEventRuleResource):
    name = "gcp-discovery"
    event_bus_name = CloudWatchEventBusgcp.get_output_attr('arn')
    event_pattern = {
    "detail-type": [Settings.JOB_DETAIL_TYPE],
    "source": [Settings.JOB_SOURCE],
    "detail": {
        "batchNo": [1],
        "cloudName": ["gcp"],
        "isCollector": [True],
        "isShipper":[False],
        "isRule": [False],
        "submitJob": [True]
        }
    }
    DEPENDS_ON = [SubmitJobLambdaFunction]
    # PROCESS = need_to_enable_gcp()


class GCPDataCollectorEventRuleLambdaPermission(LambdaPermission):
    statement_id = "AllowExecutionFromGCPDataCollectorEvent"
    action = "lambda:InvokeFunction"
    function_name = SubmitJobLambdaFunction.get_output_attr('function_name')
    principal = "events.amazonaws.com"
    source_arn = GCPDataCollectorEventRule.get_output_attr('arn')
    # PROCESS = need_to_enable_gcp()


class GCPDataCollectorCloudWatchEventTarget(CloudWatchEventTargetResource):
    rule = GCPDataCollectorEventRule.get_output_attr('name')
    arn = SubmitJobLambdaFunction.get_output_attr('arn')
    event_bus_name = CloudWatchEventBusgcp.get_output_attr('arn')
    target_id = 'GCPDataCollectorTarget'  # Unique identifier
    target_input = json.dumps({
        'jobName': "pacbot-gcp-discovery",
        'jobUuid': "pacbot-gcp-discovery",
        'jobType': "jar",
        'jobDesc': "Collects gcp data and upload to S3",
        'environmentVariables': [
            {'name': "CONFIG_URL", 'value': ApplicationLoadBalancer.get_api_base_url(
            ) + "/config/batch,gcp-discovery/prd/latest"},
        ],
        'params': [
            {'encrypt': False, 'key': "package_hint",
                'value': "com.tmobile.pacbot"},
            {'encrypt': False, 'key': "file.path",
                'value': "/home/ec2-user/gcp-data"},
            {'encrypt': False, 'key': "config_creds", 'value': "dXNlcjpwYWNtYW4="},
            {'encrypt': False, 'key': "project_ids",
                'value': get_gcp_project_ids()}
        ]
    })
    # PROCESS = need_to_enable_gcp()


class GCPDataShipperEventRule(CloudWatchEventRuleResource):
    name = "data-shipper-gcp"
    event_bus_name = CloudWatchEventBusgcp.get_output_attr('arn')
    event_pattern = {
    "detail-type": [Settings.JOB_DETAIL_TYPE],
    "source": [Settings.JOB_SOURCE],
    "detail": {
        "batchNo": [1],
        "cloudName": ["gcp"],
        "isCollector": [False],
        "isShipper": [True],
        "isRule": [False],
        "submitJob": [True]
        }
    }
    DEPENDS_ON = [SubmitJobLambdaFunction, ESDomainPolicy]
    # PROCESS = need_to_enable_gcp()


class GCPDataShipperEventRuleLambdaPermission(LambdaPermission):
    statement_id = "AllowExecutionFromgcpDataShipper"
    action = "lambda:InvokeFunction"
    function_name = SubmitJobLambdaFunction.get_output_attr('function_name')
    principal = "events.amazonaws.com"
    source_arn = GCPDataShipperEventRule.get_output_attr('arn')
    # PROCESS = need_to_enable_gcp()


class GCPDataShipperCloudWatchEventTarget(CloudWatchEventTargetResource):
    rule = GCPDataShipperEventRule.get_output_attr('name')
    arn = SubmitJobLambdaFunction.get_output_attr('arn')
    event_bus_name = CloudWatchEventBusgcp.get_output_attr('arn')
    target_id = 'GCPDataShipperTarget'  # Unique identifier
    target_input = json.dumps({
        'jobName': "data-shipper-gcp",
        'jobUuid': "data-shipper-gcp",
        'jobType': "jar",
        'jobDesc': "Ship GCP Data from S3 to PacBot ES",
        'environmentVariables': [
            {'name': "CONFIG_URL", 'value': ApplicationLoadBalancer.get_api_base_url(
            ) + "/config/batch,gcp-discovery/prd/latest"},
        ],
        'params': [
            {'encrypt': False, 'key': "package_hint",
                'value': "com.tmobile.cso.pacman"},
            {'encrypt': False, 'key': "config_creds", 'value': "dXNlcjpwYWNtYW4="},
            {'encrypt': False, 'key': "datasource", 'value': "gcp"},
            {'encrypt': False, 'key': "s3.data", 'value': "gcp-inventory"}
        ]
    })
    # PROCESS = need_to_enable_gcp()
