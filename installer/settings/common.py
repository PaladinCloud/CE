from pathlib import Path
import os
import sys

PROVIDER = 'AWS'
CURRENT_FILE_PATH = Path(os.path.join(os.path.abspath(os.path.dirname(__file__))))
BASE_APP_DIR = str(CURRENT_FILE_PATH.parent)
RESOURCES_FOLDER = 'resources'  # Provide only relative path

# This is commonn configuration should be used in all setup
SETUP_TITLE = "PALADINCLOUD"
SETUP_DESCRIPTION = "INFRA SETUP AND DEPLOYMENT"
LOADER_FILE_PATH = os.path.join(str(CURRENT_FILE_PATH), "loader")

# INSTALL_INPUTS_REQUIRED = [
#     {
#         'input_key': "USER_EMAIL_ID", 'input_msg': "Your email id to send emails: ", 'required': True
#     }
# ]

TOOLS_REQUIRED = {
    'Maven': "mvn --version",
    'Git': "git --version",
    'MySQL client': "mysql --version",
    'Terraform': "terraform --version",
    'Nodejs': "node --version",
    'npm': "npm --version",
    'Angular': "ng --version",
    'Yarn': 'yarn --version',
    'Docker': "docker --version"
}

PYTHON_PACKAGES_REQUIRED = [
    ("docker", "Client"),
    "boto3"
]

PROCESS_RESOURCES = {
    'data.aws_info': {'tags': ["roles"]},  # This should not be removed
    'iam.base_role': {'tags': ["roles"]},
    'iam.batch_role': {'tags': ["roles"]},
    'iam.ecs_role': {'tags': ["roles", "ecs_role"]},
    'iam.lambda_role': {'tags': ["roles"]},
    'iam.base_role_policy': {'tags': ["roles"]},
    'iam.post_auth' : {'tags': ["roles"]},
    'iam.all_read_role': {'tags': ["roles", "all_read_role"]},
    'vpc.security_group': {'tags': ["security"]},
    'datastore.db': {'tags': ["rds", "datastore"]},
    'datastore.es': {'tags': ["es", "datastore"]},
    'pacbot_app.alb': {'tags': ["infra"]},
    'pacbot_app.alb_target_groups': {'tags': ["infra", "deploy"]},
    'pacbot_app.alb_listener_rules': {'tags': ["infra", "deploy"]},
    'pacbot_app.ecr': {'tags': ["infra"]},
    'pacbot_app.cloudwatch_log_groups': {'tags': ["infra"]},
    'pacbot_app.build_ui_and_api': {'tags': ["deploy", "infra"]},
    'pacbot_app.import_db': {'tags': ["deploy", "app-import-db", "infra"]},
    'pacbot_app.ecs_task_defintions': {'tags': ["deploy", "task-definitions", "infra"]},
    'pacbot_app.ecs_services': {'tags': ["deploy", "ecs-services", "infra"]},
    'pacbot_app.create_template': {'tags': ["deploy", "infra"]},
    's3.bucket': {'tags': ["s3"]},
    'batch.env': {'tags': ["batch"]},
    'batch.ecr': {'tags': ["batch", "batch-ecr"]},
    'batch.job': {'tags': ["batch", "infra", "batch-job"]},
    'lambda_submit.s3_upload': {'tags': ["submit-job", "batch", "infra"]},
    'lambda_submit.function': {'tags': ["submit-job", "batch", "infra"]},
    'lambda_rule_engine.s3_upload': {'tags': ["rule-engine-job", "batch"]},
    'lambda_rule_engine.function': {'tags': ["rule-engine-job", "batch", "infra"]},
    'pacbot_app.upload_terraform': {'tags': ["upload_tf"]},
    'eventbus.custom_event_bus': {'tags': ["eventbus"]},
    'cognito.function' : {'tags' : ["cognito"]},
    'cognito.s3_upload' : {'tags' : ["cognito"]},
    'cognito.userpool' : {'tags' : ["cognito"]},
    'notification.s3_upload' : {'tags' : ["notification"]},
    'notification.function' : {'tags' : ["notification"]},
    'notification.appsync' : {'tags' : ["notification"]}
}

LAMBDA_PATH = "V1"
DATA_DIR = os.path.join(BASE_APP_DIR, 'data')
LOG_DIR = os.path.join(BASE_APP_DIR, 'log')
PROVISIONER_FILES_DIR_TO_COPY = os.path.join(BASE_APP_DIR, 'files')
ALB_PROTOCOL = "HTTP"

DESTROY_NUM_ATTEMPTS = 3
SKIP_RESOURCE_EXISTENCE_CHECK = False
RESOURCE_NAME_PREFIX = "paladincloud"
DEFAULT_RESOURCE_TAG = {"Application": "PaladinCloud"}
CUSTOM_RESOURCE_TAGS = []

RESOURCE_DESCRIPTION = "DO-NOT-DELETE-IT - This has been created as part of paladincloud installation"
AWS_POLICIES_REQUIRED = [
    "AmazonS3FullAccess",
    "AmazonRDSFullAccess",
    "AWSLambda_FullAccess",
    "AmazonEC2FullAccess",
    "IAMFullAccess",
    "AmazonESFullAccess"
]

AWS_ACCESS_KEY = ""
AWS_SECRET_KEY = ""
AWS_REGION = ""

VPC = {
    "ID": "vpc-1",
    "CIDR_BLOCKS": ["10.0.0.0/16"],
    "SUBNETS": ["subnet-1", "subnet-2"]
}
REQUIRE_SUBNETS_ON_DIFFERENT_ZONE = True

PACBOT_CODE_DIR = str(CURRENT_FILE_PATH.parent.parent)
PACBOT_LOGIN_CREDENTIALS = {
    'Admin': "admin@paladincloud.io / PaladinAdmin!!",
    'User': "user@paladincloud.io / PaladinUser!!"
}

MAKE_ALB_INTERNAL = True

MAIL_SERVER = "localhost"
MAIL_SERVER_PORT = 25
MAIL_PROTOCOL = "smtp"
MAIL_SERVER_USER = ""
MAIL_SERVER_PWD = ""
MAIL_SMTP_AUTH = ""
MAIL_SMTP_SSL_ENABLE = "true"
MAIL_SMTP_SSL_TEST_CONNECTION = "false"

ENABLE_VULNERABILITY_FEATURE = False

JOB_SCHEDULE_INITIALDELAY = 5 #scheduling jobs initial delay in minute
JOB_SCHEDULE_INITIALDELAY_SHIPPER = 15 #delay for shipper in minute (JOB_SCHEDULE_INITIALDELAY + 10 min)
JOB_SCHEDULE_INITIALDELAY_RULES = 20  #delay for rules in minute (JOB_SCHEDULE_INITIALDELAY + JOB_SCHEDULE_INITIALDELAY_SHIPPER + 5min )
JOB_SCHEDULE_INTERVAL = 6   #Job interval  in hrs
JOB_SCHEDULER_NUMBER_OF_BATCHES = 20 #number of buckets for rules 

# Azure AD integration 
AUTHENTICATION_TYPE = "COGNITO"	# login type value any one of this "AZURE_AD or DB "
AD_TENANT_ID = "xxxx- xxxxx_xxxx" # AD Tenant ID 
AD_CLIENT_ID = "xxx-xxx-xxxx"  # AD Client ID
AD_SECRET_KEY = "xxxxyyyyzzz"  # AD secret key
AD_ENCRY_SECRET_KEY = "xxyssxxxzz" # Encrypted AD secret key using bcrypt
AD_PUBLIC_KEY_URL = "https://login.microsoftonline.com/common/discovery/v2.0/keys"
AD_PUBLIC_KEY = "ssyyssdddd" # AD public key
AD_ADMIN_USER_ID = "adminuser" # Admin user user_id
ENABLE_AZURE_AD = False   #Azuread configuration
METADATA_XML_FILE = ""  #metadatafile
#event pattern job details
JOB_DETAIL_TYPE = "Paladin Cloud Job Scheduling Event" #please do not change these, as this may stop scheduling of jobs
JOB_SOURCE = "paladincloud.jobs-scheduler"

CURRENT_RELEASE = "v2.0.0"
DB_USERNAME = "paladin" 
DB_PASSWORD = "***PALADIN***" #Only printable ASCII characters besides '/', '@', '"', ' ' may be used.

MANDATORY_TAGS = "Application,Environment"
# Add your first ACCOUNT_ID,ACCOUNT_NAME and ACCOUNT_PLATFORM here
ACCOUNT_ID = ""
ACCOUNT_NAME = ""
ACCOUNT_PLATFORM = ""

# These settings are for the enabling and using Aqua Vulnerability feature and service
ENABLE_AQUA_VULNERABILITY_FEATURE = False
AQUA_API_URL="" # Aqua API Url without trailing slash
AQUA_CLIENT_DOMAIN_URL = "" # Aqua Client Domain Url without trailing slash
AQUA_USERNAME = "" # Aqua Client User name
AQUA_PASSWORD = "" # Aqua Client password
AQUA_API_DEFAULT_PAGE_SIZE=1000
AQUA_IMAGE_VULNERABILITY_QUERY_PARAMS= "include_vpatch_info=true&show_negligible=true&hide_base_image=false&severities=critical,high,medium,low,negligible"

try:
    from settings.local import *
except:
    pass

if ALB_PROTOCOL == "HTTPS":
    PROCESS_RESOURCES['pacbot_app.alb_https_listener'] = {'tags': ["deploy"]}  # This should not be removed



AUTHENTICATION_TYPE = "COGNITO"	