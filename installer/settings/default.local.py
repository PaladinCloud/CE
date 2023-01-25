# CREATE local.py file by renaming/copying default.local.py
# User should update the VPC details below in local.py
VPC = {
    "ID": "vpc-1",
    "CIDR_BLOCKS": ["10.0.0.0/16"],
    "SUBNETS": ["subnet-1", "subnet-2"]
}


# Custom tags that can be defined by user
CUSTOM_RESOURCE_TAGS = {
    'Application': "PaladinCloud",
    'Environment': "Prod",
    'Created By': "customer-name"
}

#set the prefix for resources, needs changed if you run more than one instance of Paladin Coud
RESOURCE_NAME_PREFIX = "paladincloud"

# RDS Related Configurations
# Possibble values db.m5.large, db.t3.large etc
RDS_INSTANCE_TYPE = "db.t3.medium"


# ElasticSearch Related Configurations
# Possibble values m5.xlarge.elasticsearch  etc
ES_INSTANCE_TYPE = "m5.large.elasticsearch"
ES_VOLUME_SIZE = 20
ES_NODE_COUNT = 1
ES_MASTER_INSTANCE_TYPE = "m5.large.elasticsearch"
ES_DEDICATED_MASTER_ENABLED = False
ES_MASTER_NODE_COUNT = 1

# ALB related configurations
# False if ALB needs to be public (internet facing) else True
MAKE_ALB_INTERNAL = True
ALB_PROTOCOL = "HTTP"
SSL_CERTIFICATE_ARN = ""  # Required only if ALB_PROTOCOL is defined as HTTPS
PALADINCLOUD_DOMAIN = ""  # Required only if you point a CNAME record to ALB ex: app.paladincloud.com


# MAIL Server configuration
MAIL_SERVER = "localhost"
MAIL_SERVER_PORT = 587
MAIL_PROTOCOL = "smtp"
MAIL_SERVER_USER = ""
MAIL_SERVER_PWD = ""
MAIL_SMTP_AUTH = ""
MAIL_SMTP_SSL_ENABLE = "true"
MAIL_SMTP_SSL_TEST_CONNECTION = "false"

USER_EMAIL_ID = ""

# System reads below data from user if not updated here
# Value should be numeric 1 or 2 or 3 
AWS_AUTH_MECHANISM = None
# if AWS_AUTH_MECHANISM == 1
AWS_ACCESS_KEY = ""
AWS_SECRET_KEY = ""
AWS_REGION = ""
# If AWS_AUTH_MECHANISM == 2, AWS_ASSUME_ROLE_ARN is required
AWS_ASSUME_ROLE_ARN = ""

# These settings enable the Vulnerability feature and service
ENABLE_VULNERABILITY_FEATURE = False
QUALYS_API_URL = ""  # Qualys API Url without trailing slash
QUALYS_INFO = ""  # Base64 encoded user:password of qualys

# Settings for enabling AZURE  
ENABLE_AZURE = False
# Tenants should be a list of dict containing tenantId, clientId and secretValue
AZURE_TENANTS = [
    {
        'tenantId': "t111",
        'clientId': "c111",
        'secretValue': "s111"
    },
    {
        'tenantId': "t222",
        'clientId': "c222",
        'secretValue': "s222"
    },
]
# Settings for enabling GCP 
ENABLE_GCP = False
GCP_PROJECT_IDS = []
GCP_CREDENTIALS = {}

# Azure AD integration 
AUTHENTICATION_TYPE = "DB"	# login type value should be any one of these "AZURE_AD or DB "
AD_TENANT_ID = "xxxx- xxxxx_xxxx" # AD Tenant ID 
AD_CLIENT_ID = "xxx-xxx-xxxx"  # AD Client ID
AD_SECRET_KEY = "xxxxyyyyzzz"  # AD secret key
AD_ENCRY_SECRET_KEY = "xxyssxxxzz" # Encrypted AD secret key using bcrypt
AD_PUBLIC_KEY_URL = "https://login.microsoftonline.com/common/discovery/v2.0/keys"
AD_PUBLIC_KEY = "ssyyssdddd" # AD public key
AD_ADMIN_USER_ID = "adminuser" # Admin user user_id


#job/rules intervals
JOB_SCHEDULE_INTERVAL = 6 #by default it is 6hrs

# AWS accountId,name for multiple accounts
AWS_ACCOUNT_DETAILS = [
    {
        'accountId': "176332",
        'accountName': "baseAccount"
    },
    {
        'accountId': "2345",
        'accountName': "clientAccount1"
    },
    {
        'accountId': "234565",
        'accountName': "clientAccount2"
    }
]

#RDS username and password
DB_USERNAME = "paladin" 
DB_PASSWORD = "***PALADIN***" #Only printable ASCII characters besides '/', '@', '"', ' ' may be used.

MANDATORY_TAGS = "Application,Environment"

#BATCH CONFIGURATION 
BATCH_JOB_MEMORY = 3072
BATCH_JOB_VCPU = 1
BATCH_INSTANCE_TYPE = "m5.xlarge"     #m4.xlarge,m5.xlarge & c5.xlarge (follow batch upgrade document)