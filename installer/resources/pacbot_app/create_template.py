from core.terraform.resources.misc import NullResource
from core.terraform.utils import get_terraform_scripts_and_files_dir, get_terraform_scripts_dir, \
    get_terraform_provider_file
from resources.s3.bucket import BucketStorage
from resources.iam.base_role import BaseRole
from core.terraform import PyTerraform
from shutil import copy2
from core.config import Settings
import os
from core.terraform.resources.aws.s3 import S3BucketObject



class CreateTemplate(NullResource):
    triggers = {'id' : "${timestamp()}"}
    dest_file = os.path.join(get_terraform_scripts_and_files_dir(), 'CloudFormation_Values.yaml')
    DEPENDS_ON = [BucketStorage]

    def get_provisioners(self):
        paladin_build_script = os.path.join(get_terraform_scripts_dir(), 'cloudformation_script.py')

        local_execs = [{
            'local-exec': {
                'command': paladin_build_script,
                'environment': {
                    'TENANT_BASE_ROLE_ARN': BaseRole.get_output_attr('arn'),
                    'TEMPLATE_FILE_PATH': self.dest_file,
                    'TENANT_ROLE_NAME':str(Settings.RESOURCE_NAME_PREFIX + "_ro") 
                },
                'interpreter': [Settings.PYTHON_INTERPRETER]
            }
        }]

        return local_execs

    def pre_generate_terraform(self):
        src_file = os.path.join(Settings.BASE_APP_DIR, 'resources', 'pacbot_app', 'files', 'CloudFormation.yaml')
        copy2(src_file, self.dest_file)



class UploadLambdaSubmitJobZipFile(S3BucketObject):
    bucket = BucketStorage.get_output_attr('bucket')
    key = "deployment.yaml"
    source = os.path.join(Settings.BASE_APP_DIR, 'data', 'terraform', 'scripts_and_files','CloudFormation_Values.yaml')
    acl = "public-read"
    DEPENDS_ON = [CreateTemplate]
