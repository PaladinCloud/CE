from core.terraform.resources.aws.eventbus import CloudWatchEventBusResource
from core.config import Settings
from resources.pacbot_app.utils import need_to_enable_azure, need_to_enable_gcp

class CloudWatchEventBusaws(CloudWatchEventBusResource):
    name = "aws"

class CloudWatchEventBusazure(CloudWatchEventBusResource):
    name = "azure"
    # PROCESS = need_to_enable_azure()

class CloudWatchEventBusgcp(CloudWatchEventBusResource):
    name = "gcp"
    # PROCESS = need_to_enable_gcp()

