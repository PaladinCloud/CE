from core.terraform.resources.aws.kms import KMSCMKResource
from core.config import Settings
import json
from resources.data.aws_info import AwsAccount

class kms_key_resources(KMSCMKResource):
    deletion_window_in_days = 7
    policy = {
    "Version": "2012-10-17",
    "Statement": [
      {
        "Sid": "Enable IAM User Permissions",
        "Effect": "Allow",
        "Principal": {
          "AWS": "arn:aws:iam::" + AwsAccount.get_output_attr('account_id') + ":root"
        },
        "Action": "kms:*",
        "Resource": "*"
      }
    ]
  }

