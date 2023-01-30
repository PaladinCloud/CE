from core.terraform.resources.misc import NullResource
from core.terraform.utils import get_terraform_scripts_and_files_dir, get_terraform_scripts_dir, \
    get_terraform_provider_file
from core.config import Settings
from resources.lambda_rule_engine.utils import number_of_aws_rules, number_of_azure_rules, number_of_gcp_rules
from resources.datastore.db import MySQLDatabase
from resources.datastore.es import ESDomain
from resources.data.aws_info import AwsAccount, AwsRegion
from resources.pacbot_app.cloudwatch_log_groups import UiCloudWatchLogGroup, ApiCloudWatchLogGroup
from resources.pacbot_app.ecr import APIEcrRepository, UIEcrRepository
from resources.data.aws_info import AwsRegion
from resources.pacbot_app.alb import ApplicationLoadBalancer
from resources.datastore.es import ESDomain
from resources.iam.ecs_role import ECSRole
from resources.iam.base_role import BaseRole
from resources.lambda_submit.function import SubmitJobLambdaFunction
from resources.lambda_rule_engine.function import RuleEngineLambdaFunction
from resources.s3.bucket import BucketStorage
from resources.pacbot_app.utils import need_to_enable_azure, need_to_enable_gcp
from shutil import copy2
import os
import json


class ReplaceSQLPlaceHolder(NullResource):
    dest_file = os.path.join(get_terraform_scripts_and_files_dir(), 'DB_With_Values.sql')
    azure_ad_dest_file = os.path.join(get_terraform_scripts_and_files_dir(), 'DB_Azure_AD_With_Values.sql')
    policy_dest_file = os.path.join(get_terraform_scripts_and_files_dir(), 'DB_Policy_With_Values.sql')
    cognito_query_file = os.path.join(get_terraform_scripts_and_files_dir(), 'DB_Cognito.sql')
    triggers = {'version': "1.1"}

    DEPENDS_ON = [MySQLDatabase, ESDomain]

    def prepare_azure_tenants_credentias(self):
        tenants = Settings.get('AZURE_TENANTS', [])
        credential_string = ""

        if need_to_enable_azure():
            for tenant in tenants:
                tenant_id = tenant['tenantId']
                client_id = tenant['clientId']
                seccret_id = tenant['secretValue']
                credential_string = "" if credential_string == "" else (credential_string + "##")
                credential_string += "tenant:%s,clientId:%s,secretId:%s" % (tenant_id, client_id, seccret_id)

        return credential_string

    def prepare_gcp_credential_string(self):
        credential_json = Settings.get('GCP_CREDENTIALS', [])
        credential_string = ""
        # if need_to_enable_gcp():
        #     with open(GCP_CREDENTIALS, 'r') as f:
        #         data = json.load(f)
        #         credential_string = json.dumps(data)
        if need_to_enable_gcp():
            credential_string = json.dumps(credential_json)
        return credential_string

    def get_provisioners(self):
        script = os.path.join(get_terraform_scripts_dir(), 'sql_replace_placeholder.py')
        db_user_name = MySQLDatabase.get_input_attr('username')
        db_password = MySQLDatabase.get_input_attr('password')
        db_host = MySQLDatabase.get_output_attr('endpoint')
        azure_credentails = self.prepare_azure_tenants_credentias()
        gcp_credentials = self.prepare_gcp_credential_string()
        job_interval = str(Settings.JOB_SCHEDULE_INTERVAL * 3600000)
        job_initialdelay = str(Settings.JOB_SCHEDULE_INITIALDELAY * 60000) 
        job_schedule_initialdelay_shipper = str(Settings.JOB_SCHEDULE_INITIALDELAY_SHIPPER * 60000)
        job_schedule_initialdelay_rules  = str(Settings.JOB_SCHEDULE_INITIALDELAY_RULES * 60000)
        local_execs = [
            {
                'local-exec': {
                    'command': script,
                    'environment': {
                        'SQL_FILE_PATH': self.dest_file,
                        'ENV_region': AwsRegion.get_output_attr('name'),
                        'ENV_account': AwsAccount.get_output_attr('account_id'),
                        'ENV_eshost': ESDomain.get_http_url(),
                        'ENV_esport': ESDomain.get_es_port(),
                        'ENV_LOGGING_ES_HOST_NAME': ESDomain.get_output_attr('endpoint'),
                        'ENV_LOGGING_ES_PORT': str(ESDomain.get_es_port()),
                        'ENV_ES_HOST_NAME': ESDomain.get_output_attr('endpoint'),
                        'ENV_ES_PORT': str(ESDomain.get_es_port()),
                        'ENV_ES_CLUSTER_NAME': ESDomain.get_input_attr('domain_name'),
                        'ENV_ES_PORT_ADMIN': str(ESDomain.get_es_port()),
                        'ENV_ES_HEIMDALL_HOST_NAME': ESDomain.get_output_attr('endpoint'),
                        'ENV_ES_HEIMDALL_PORT': str(ESDomain.get_es_port()),
                        'ENV_ES_HEIMDALL_CLUSTER_NAME': ESDomain.get_input_attr('domain_name'),
                        'ENV_ES_HEIMDALL_PORT_ADMIN': str(ESDomain.get_es_port()),
                        'ENV_ES_UPDATE_HOST': ESDomain.get_output_attr('endpoint'),
                        'ENV_ES_UPDATE_PORT': str(ESDomain.get_es_port()),
                        'ENV_ES_UPDATE_CLUSTER_NAME': ESDomain.get_input_attr('domain_name'),
                        'ENV_PACMAN_HOST_NAME': ApplicationLoadBalancer.get_http_url(),
                        'ENV_RDS_URL': MySQLDatabase.get_rds_db_url(),
                        'ENV_RDS_USERNAME': MySQLDatabase.get_input_attr('username'),
                        'ENV_RDS_PASSWORD': MySQLDatabase.get_input_attr('password'),
                        'ENV_JOB_BUCKET_REGION': AwsRegion.get_output_attr('name'),
                        'ENV_RULE_JOB_BUCKET_NAME': BucketStorage.get_output_attr('bucket'),
                        'ENV_JOB_LAMBDA_REGION': AwsRegion.get_output_attr('name'),
                        'ENV_JOB_FUNCTION_NAME': SubmitJobLambdaFunction.get_input_attr('function_name'),
                        'ENV_JOB_FUNCTION_ARN': SubmitJobLambdaFunction.get_output_attr('arn'),
                        'ENV_RULE_BUCKET_REGION': AwsRegion.get_output_attr('name'),
                        'ENV_RULE_LAMBDA_REGION': AwsRegion.get_output_attr('name'),
                        'ENV_RULE_FUNCTION_NAME': RuleEngineLambdaFunction.get_input_attr('function_name'),
                        'ENV_RULE_FUNCTION_ARN': RuleEngineLambdaFunction.get_output_attr('arn'),
                        'ENV_CLOUD_INSIGHTS_TOKEN_URL': "http://localhost",
                        'ENV_CLOUD_INSIGHTS_COST_URL': "http://localhost",
                        'ENV_SVC_CORP_USER_ID': "testid",
                        'ENV_SVC_CORP_PASSWORD': "password",
                        'ENV_CERTIFICATE_FEATURE_ENABLED': "false",
                        'ENV_PATCHING_FEATURE_ENABLED': "false",
                        'ENV_VULNERABILITY_FEATURE_ENABLED': str(
                            Settings.get('ENABLE_VULNERABILITY_FEATURE', False)).lower(),
                        'ENV_MAIL_SERVER': Settings.MAIL_SERVER,
                        'ENV_PACMAN_S3': "pacman-email-templates",
                        'ENV_DATA_IN_DIR': "inventory",
                        'ENV_DATA_BKP_DIR': "backup",
                        'ENV_PAC_ROLE': BaseRole.get_input_attr('name'),
                        'ENV_BASE_REGION': AwsRegion.get_output_attr('name'),
                        'ENV_DATA_IN_S3': BucketStorage.get_output_attr('bucket'),
                        'ENV_BASE_ACCOUNT': AwsAccount.get_output_attr('account_id'),
                        'ENV_PAC_RO_ROLE': BaseRole.get_input_attr('name'),
                        'ENV_MAIL_SERVER_PORT': Settings.MAIL_SERVER_PORT,
                        'ENV_MAIL_PROTOCOL': Settings.MAIL_PROTOCOL,
                        'ENV_MAIL_SERVER_USER': Settings.MAIL_SERVER_USER,
                        'ENV_MAIL_SERVER_PWD': Settings.MAIL_SERVER_PWD,
                        'ENV_MAIL_SMTP_AUTH': Settings.MAIL_SMTP_AUTH,
                        'ENV_MAIL_SMTP_SSL_ENABLE': Settings.MAIL_SMTP_SSL_ENABLE,
                        'ENV_MAIL_SMTP_SSL_TEST_CONNECTION': Settings.MAIL_SMTP_SSL_TEST_CONNECTION,
                        'ENV_PACMAN_LOGIN_USER_NAME': "admin@paladincloud.io",
                        'ENV_PACMAN_LOGIN_PASSWORD': "PaladinAdmin!!",
                        'ENV_CONFIG_CREDENTIALS': "dXNlcjpwYWNtYW4=",
                        'ENV_CONFIG_SERVICE_URL': ApplicationLoadBalancer.get_http_url() + "/api/config/rule/prd/latest",
                        'ENV_PACBOT_AUTOFIX_RESOURCEOWNER_FALLBACK_MAILID': Settings.get('USER_EMAIL_ID', ""),
                        'ENV_QUALYS_INFO': Settings.get('QUALYS_INFO', ""),
                        'ENV_QUALYS_API_URL': Settings.get('QUALYS_API_URL', ""),
                        'ENV_AZURE_CREDENTIALS': azure_credentails,
                        'ENV_GCP_CREDENTIALS': gcp_credentials,
                        'SQL_FILE_PATH_AD': self.azure_ad_dest_file,
                        'SQL_FILE_PATH_POLICY': self.policy_dest_file,
                        'AUTHENTICATION_TYPE' : Settings.get('AUTHENTICATION_TYPE',""),
                        'ENV_AD_TENANT_ID' : Settings.get('AD_TENANT_ID',""),
                        'ENV_AD_CLIENT_ID' : Settings.get('AD_CLIENT_ID',""),
                        'ENV_AD_SECRET_KEY' : Settings.get('AD_SECRET_KEY',""),
                        'ENV_AD_ENCRY_SECRET_KEY' : Settings.get('AD_ENCRY_SECRET_KEY',""),
                        'ENV_AD_PUBLIC_KEY_URL' : Settings.get('AD_PUBLIC_KEY_URL',""),
                        'ENV_AD_PUBLIC_KEY' : Settings.get('AD_PUBLIC_KEY',""),
                        'ENV_AD_ADMIN_USER_ID' : Settings.get('AD_ADMIN_USER_ID',""),
                        'ENV_JOB_SCHEDULE_INTERVAL' : job_interval,
                        'ENV_JOB_SCHEDULE_INITIALDELAY' : job_initialdelay,
                        'ENV_JOB_SCHEDULE_INITIALDELAY_SHIPPER' : job_schedule_initialdelay_shipper,
                        'ENV_JOB_SCHEDULE_INITIALDELAY_RULES' : job_schedule_initialdelay_rules,
                        'ENV_AWS_EVENTBRIDGE_BUS_DETAILS' : Settings.RESOURCE_NAME_PREFIX + "-" + "aws" + ":" + str(number_of_aws_rules()),
                        'ENV_AZURE_EVENTBRIDGE_BUS_DETAILS' : Settings.RESOURCE_NAME_PREFIX + "-" + "azure" + ":" + str(number_of_azure_rules()),
                        'ENV_GCP_EVENTBRIDGE_BUS_DETAILS'  : Settings.RESOURCE_NAME_PREFIX + "-" + "gcp" + ":" + str(number_of_gcp_rules()),
                        'ENV_AZURE_ENABLED' : str(need_to_enable_azure()).lower(),
                        'ENV_GCP_ENABLED' : str(need_to_enable_gcp()).lower(),
                        'ENV_JOB_SCHEDULER_NUMBER_OF_BATCHES' : str(Settings.JOB_SCHEDULER_NUMBER_OF_BATCHES),
                        'ENV_CURRENT_RELEASE': str(Settings.CURRENT_RELEASE),
                        'EVENT_BRIDGE_PRIFIX' : Settings.RESOURCE_NAME_PREFIX,
                        'ENV_MANDATORY_TAGS': str(Settings.MANDATORY_TAGS)
                    },
                    'interpreter': [Settings.PYTHON_INTERPRETER]
                }
            }
        ]

        return local_execs

    def pre_generate_terraform(self):
        src_file = os.path.join(Settings.BASE_APP_DIR, 'resources', 'pacbot_app', 'files', 'DB.sql')
        copy2(src_file, self.dest_file)
        src_policy_file = os.path.join(Settings.BASE_APP_DIR, 'resources', 'pacbot_app', 'files', 'DB_Policy.sql')
        copy2(src_policy_file, self.policy_dest_file)
        cognito_db_file = os.path.join(Settings.BASE_APP_DIR, 'resources', 'pacbot_app', 'files', 'DB_Cognito.sql')
        copy2(cognito_db_file, self.cognito_query_file)
        if Settings.AUTHENTICATION_TYPE == "AZURE_AD":
            src_azure_ad_file = os.path.join(Settings.BASE_APP_DIR, 'resources', 'pacbot_app', 'files', 'DB_Azure_AD.sql')
            copy2(src_azure_ad_file, self.azure_ad_dest_file)


class ImportDbSql(NullResource):
    triggers = {'version': "1.1"}

    DEPENDS_ON = [MySQLDatabase, ReplaceSQLPlaceHolder]

    def get_provisioners(self):
        db_user_name = MySQLDatabase.get_input_attr('username')
        db_password = MySQLDatabase.get_input_attr('password')
        db_host = MySQLDatabase.get_output_attr('address')
        local_execs = [
            {
                'local-exec': {
                    'command': "mysql -u %s --password=%s -h %s < %s" % (
                    db_user_name, db_password, db_host, ReplaceSQLPlaceHolder.dest_file)
                }
            },
             {
                'local-exec': {
                    'command': "mysql -u %s --password=%s -h %s < %s" % (
                    db_user_name, db_password, db_host, ReplaceSQLPlaceHolder.policy_dest_file)
                }
            },
            {
                 'local-exec': {
                    'command': "mysql -u %s --password=%s -h %s < %s" % (
                    db_user_name, db_password, db_host, ReplaceSQLPlaceHolder.cognito_query_file)
                 }
            }

        ]
        if Settings.AUTHENTICATION_TYPE == "AZURE_AD":
            local_execs = [
            {
                'local-exec': {
                    'command': "mysql -u %s --password=%s -h %s < %s" % (
                    db_user_name, db_password, db_host, ReplaceSQLPlaceHolder.dest_file)
                }
            }
            ,
            {
                'local-exec': {
                    'command': "mysql -u %s --password=%s -h %s < %s" % (
                    db_user_name, db_password, db_host, ReplaceSQLPlaceHolder.azure_ad_dest_file)
                }
            },
            {
                'local-exec': {
                    'command': "mysql -u %s --password=%s -h %s < %s" % (
                    db_user_name, db_password, db_host, ReplaceSQLPlaceHolder.cognito_query_file)
                }
            }

        ]
            

        return local_execs
