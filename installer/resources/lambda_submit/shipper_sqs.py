from core.terraform.resources.aws.sns import SQSResources
from core.config import Settings

class ShipperdoneSQS(SQSResources):
    name                        = "shipper-done.fifo"
    fifo_queue                  = True
    content_based_deduplication = True
    deduplication_scope         = "messageGroup"
    visibility_timeout_seconds  = 900
    fifo_throughput_limit       = "perMessageGroupId"
  
class PolicydoneSQS(SQSResources):
    name                        = "policy-done.fifo"
    fifo_queue                  = True
    content_based_deduplication = True
    deduplication_scope         = "messageGroup"
    visibility_timeout_seconds  = 900
    fifo_throughput_limit       = "perMessageGroupId"
  