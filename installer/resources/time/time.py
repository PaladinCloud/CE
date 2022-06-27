from core.terraform.resources import TerraformResource
from core.config import Settings
import os
from core.terraform.resources.aws.time import CurrentTime


class localtime(CurrentTime):
    current_timestamp  = "timestamp()"
    current_hrs        = "formatdate(""yy"",local.current_timestamp)"
    current_mins       = "formatdate(""mm"",local.current_timestamp)"
 