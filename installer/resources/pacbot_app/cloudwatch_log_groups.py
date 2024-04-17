from core.terraform.resources.aws.cloudwatch import CloudWatchLogGroupResource
from core.config import Settings


class ApiCloudWatchLogGroup(CloudWatchLogGroupResource):
    name = "apis"
    retention_in_days = Settings.RETENTION_IN_DAYS  


class UiCloudWatchLogGroup(CloudWatchLogGroupResource):
    name = "ui"
    retention_in_days = Settings.RETENTION_IN_DAYS  
