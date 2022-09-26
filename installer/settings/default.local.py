# CREATE local.py file by renaming/copying default.local.py
# User should update the VPC details below in local.py
VPC = {
    "ID": "vpc-1",
    "CIDR_BLOCKS": ["10.0.0.0/16"],
    "SUBNETS": ["subnet-1", "subnet-2"]
}


# CUstom tags that can be defined by user
CUSTOM_RESOURCE_TAGS = {
    'Application': "PaladinCloud",
    'Environment': "Prod",
    'Created By': "customer-name"
}


# RDS Related Configurations
# Possibble values db.m5.large, db.t3.large etc
RDS_INSTANCE_TYPE = "db.t3.medium"


# ElasticSearch Related Configurations
# Possibble values m5.xlarge.elasticsearch  etc
ES_INSTANCE_TYPE = "m5.large.elasticsearch"
ES_VOLUME_SIZE = 20
ES_NODE_COUNT = 3
ES_MASTER_INSTANCE_TYPE = "m5.large.elasticsearch"
ES_DEDICATED_MASTER_ENABLED = True
ES_MASTER_NODE_COUNT = 3

# ALB related configurations
# False if ALB need to be public(internet facing) else True
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
# Value should be numeric 1 or 2 or 3. I. If kept like this input is read from
AWS_AUTH_MECHANISM = None
# if AWS_AUTH_MECHANISM == 1
AWS_ACCESS_KEY = ""
AWS_SECRET_KEY = ""
AWS_REGION = ""
# If AWS_AUTH_MECHANISM == 2, AWS_ASSUME_ROLE_ARN is required
AWS_ASSUME_ROLE_ARN = ""

# This settings enable Vulnerability feature and servie
ENABLE_VULNERABILITY_FEATURE = False
QUALYS_API_URL = ""  # Qualys API Url without trailing slash
QUALYS_INFO = ""  # Base64 encoded user:password of qualys

# Settings for enable AZURE  
ENABLE_AZURE = False
# Tenants should be a list of dict containing tenantId, clientId and secretId
AZURE_TENANTS = [
    {
        'tenantId': "t111",
        'clientId': "c111",
        'secretId': "s111"
    },
    {
        'tenantId': "t222",
        'clientId': "c222",
        'secretId': "s222"
    },
]
# Settings for enable GCP 
ENABLE_GCP = False
GCP_PROJECT_IDS = []
GCP_CREDENTIALS = {}

# Azure AD integration 
AUTHENTICATION_TYPE = "DB"	# login type value any one of this "AZURE_AD or DB "
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