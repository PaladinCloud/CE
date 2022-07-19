from core.terraform.resources import TerraformResource, TerraformData
from core.config import Settings
from core.providers.aws.boto3 import kms
class KMSkey(TerraformResource):
    resource_instance_name = "aws_kms_key"
    available_args = {
        'description': {'required': True, 'prefix': True, 'sep': '_'},
        'tags': {'required': False}
    }

class KMSkeyNAME(TerraformResource):
    resource_instance_name = "aws_kms_alias"
    available_args = {
        'name': {'required': True},
        'target_key_id':  {'required': True}
    }

    def check_exists_before(self, input, tf_outputs):
        """
        Check if the resource is already exists in AWS

        Args:
            input (instance): input object
            tf_outputs (dict): Terraform output dictionary

        Returns:
            exists (boolean): True if already exists in AWS else False
            checked_details (dict): Status of the existence check
        """
        checked_details = {'attr': "name", 'value': self.get_input_attr('name')}
        exists = False

        if not self.resource_in_tf_output(tf_outputs):
            exists = kms.check_kms_key_exists(
                checked_details['value'],
                input.AWS_AUTH_CRED)

        return exists, checked_details