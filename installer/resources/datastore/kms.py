from core.terraform.resources.aws.kms import KMSkey, KMSkeyNAME 
class AWSKMSkey(KMSkey):
    description = "kmskey"

class AWSKMSkeyname(KMSkeyNAME):
    name = "alias/paladincloud"
    target_key_id = AWSKMSkey.get_output_attr('arn')


