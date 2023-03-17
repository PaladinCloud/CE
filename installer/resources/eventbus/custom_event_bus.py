from core.terraform.resources.aws.eventbus import CloudWatchEventBusResource
from core.config import Settings

class CloudWatchEventBusaws(CloudWatchEventBusResource):
    name = "aws"

class CloudWatchEventBusazure(CloudWatchEventBusResource):
    name = "azure"
    # PROCESS = need_to_enable_azure()

class CloudWatchEventBusgcp(CloudWatchEventBusResource):
    name = "gcp"
    # PROCESS = need_to_enable_gcp()

