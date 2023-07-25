# CREATE local.py file by renaming/copying default.local.py

#set the prefix for resources, needs changed if you run more than one instance of Paladin Cloud
RESOURCE_NAME_PREFIX = "paladincloud"

# User should update the VPC details below in local.py
VPC = {
    "ID": "vpc-1",
    "CIDR_BLOCKS": ["10.0.0.0/16"],
    "SUBNETS": ["subnet-1", "subnet-2"]
}

# Custom tags that can be defined by user
# User can add upto 6tags
CUSTOM_RESOURCE_TAGS = {
    'Application': "PaladinCloud",
    'Environment': "Prod",
    'Created By': "customer-name"
}

# ALB related configurations
# False if ALB needs to be public (internet facing) else True
MAKE_ALB_INTERNAL = True
ALB_PROTOCOL = "HTTP"
SSL_CERTIFICATE_ARN = ""  # Required only if ALB_PROTOCOL is defined as HTTPS
PALADINCLOUD_DOMAIN = ""  # Required only if you point a CNAME record to ALB ex: app.paladincloud.com

#Cognito Configuration
COGNITO_ADMIN_EMAIL_ID = "" #email_id of admin user 
COGNITO_DOMAIN = ""       #example xyzpaladincloud

# System reads below data from user if not updated here
# Value should be numeric 1 or 2 or 3 
AWS_AUTH_MECHANISM = None
# if AWS_AUTH_MECHANISM == 1
AWS_ACCESS_KEY = ""
AWS_SECRET_KEY = ""
AWS_REGION = ""
# If AWS_AUTH_MECHANISM == 2, AWS_ASSUME_ROLE_ARN is required
AWS_ASSUME_ROLE_ARN = ""

# Add your first ACCOUNT_ID,ACCOUNT_NAME  here
AWS_ENABLED = "true"
ACCOUNT_ID = ""
ACCOUNT_NAME = ""

# RDS Related Configurations
# Possibble values db.m5.large, db.t3.large etc
RDS_INSTANCE_TYPE = "db.t3.medium"
#RDS username and password
DB_USERNAME = "paladin" 
DB_PASSWORD = "***PALADIN***" #Only printable ASCII characters besides '/', '@', '"', ' ' may be used.


#ECS Related Configuration
ECS_CPU = 1024

# ElasticSearch Related Configurations
# Possibble values m5.xlarge.elasticsearch  etc
ES_INSTANCE_TYPE = "m5.large.elasticsearch"
ES_VOLUME_SIZE = 20
ES_NODE_COUNT = 1
ES_MASTER_INSTANCE_TYPE = "m5.large.elasticsearch"
ES_DEDICATED_MASTER_ENABLED = False
ES_MASTER_NODE_COUNT = 1

# MAIL Server configuration
MAIL_SERVER = "localhost"
MAIL_SERVER_PORT = 587
MAIL_PROTOCOL = "smtp"
MAIL_SERVER_USER = ""
MAIL_SERVER_PWD = ""
MAIL_SMTP_AUTH = ""
MAIL_SMTP_SSL_ENABLE = "true"
MAIL_SMTP_SSL_TEST_CONNECTION = "false"

#job/rules intervals
JOB_SCHEDULE_INITIALDELAY = 5 #scheduling jobs initial delay in minute
JOB_SCHEDULE_INITIALDELAY_SHIPPER = 15 #delay for shipper in minute (JOB_SCHEDULE_INITIALDELAY + 10 min)
JOB_SCHEDULE_INITIALDELAY_RULES = 20  #delay for rules in minute (JOB_SCHEDULE_INITIALDELAY + JOB_SCHEDULE_INITIALDELAY_SHIPPER + 5min )
JOB_SCHEDULE_INTERVAL = 6   #Job interval  in hrs
JOB_SCHEDULER_NUMBER_OF_BATCHES = 20 #number of buckets for rules 

#mandatory tags for tagging polices
MANDATORY_TAGS = "Application,Environment"

# Flag to turn on/off aws by default
AWS_ENABLED = "true"


#BATCH CONFIGURATION 
#optinal configuration 
BATCH_JOB_MEMORY = 3072
BATCH_JOB_VCPU = 1
BATCH_INSTANCE_TYPE = "m5.xlarge"     #m4.xlarge,m5.xlarge & c5.xlarge (follow batch upgrade document)

#AZURE_AD_CONFIGURATION
#optianal configuration if you would like to setup AzureAD AUTHENTICATION  
ENABLE_AZURE_AD = False  
METADATA_XML_FILE = "" 

#AutoFix Configuration
DEFAULT_RESOURCE_OWNER = ""   #Enter Email ID, All AutoFix notification send to this mail ID.
