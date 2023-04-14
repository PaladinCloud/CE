from core.terraform.resources.misc import NullResource
from resources.notification.appsync import ApiSyncIdKey, AppSyncNotification
from resources.cognito.userpool import AppCLient
from resources.s3.bucket import BucketStorage
from resources.pacbot_app.alb import ApplicationLoadBalancer
from core.terraform.utils import get_terraform_scripts_dir, get_terraform_provider_file
from core.terraform import PyTerraform
from core.config import Settings
import os


class BuildUiAndApis(NullResource):
    DEPENDS_ON = [BucketStorage, ApplicationLoadBalancer]

    def pre_generate_terraform(self):
        pom_file = os.path.join(Settings.PACBOT_CODE_DIR, "pom.xml")
        if not os.path.exists(pom_file):
            raise Exception("Pacbot CodeBase, %s,  is Not Found!" % pom_file)

    def get_provisioners(self):
        pacbot_build_script = os.path.join(get_terraform_scripts_dir(), 'build_pacbot.py')
        upload_dir = self._create_dir_to_store_build_ap()

        local_execs = [{
            'local-exec': {
                'command': pacbot_build_script,
                'environment': {
                    'PROVIDER_FILE': get_terraform_provider_file(),
                    'APPLICATION_DOMAIN': ApplicationLoadBalancer.get_pacbot_domain_url(),
                    'PACBOT_CODE_DIR': Settings.PACBOT_CODE_DIR,
                    'DIST_FILES_UPLOAD_DIR': upload_dir,
                    'LOG_DIR': Settings.LOG_DIR,
                    'S3_BUCKET': BucketStorage.get_output_attr('bucket'),
                    'S3_KEY_PREFIX': Settings.RESOURCE_NAME_PREFIX,
                    'ENABLE_VULNERABILITY_FEATURE': str(Settings.ENABLE_VULNERABILITY_FEATURE).lower(),
                    'AUTHENTICATION_TYPE': Settings.AUTHENTICATION_TYPE,
                    'AD_TENANT_ID': Settings.AD_TENANT_ID,
                    'AD_CLIENT_ID': Settings.AD_CLIENT_ID,
                    'COGNITO_CLIENT_ID' : AppCLient.get_output_attr('id'),
                    'COGNITO_CLIENT_SECRET':AppCLient.get_output_attr('client_secret'),
                    'COGNITO_DOMAIN': Settings.COGNITO_DOMAIN,
                    'AWS_REGION': Settings.AWS_REGION,
                    'APPSYNC_URL': AppSyncNotification.get_output_attr('uris["GRAPHQL"]'),
                    'APPSYNC_API_KEY': ApiSyncIdKey.get_output_attr('key')
                },
                'interpreter': [Settings.PYTHON_INTERPRETER]
            }
        }]

        return local_execs

    def _create_dir_to_store_build_ap(self):
        upload_dir = os.path.join(Settings.TERRAFORM_DIR, 'upload_to_s3')
        try:
            os.mkdir(upload_dir)
        except OSError as OE:
            if OE.errno != 17:  # If not Already exists
                raise Exception("Not able to create directory to store API Jars and UI code")

        return upload_dir

    def pre_terraform_destroy(self):
        # To support latest terraform version
        PyTerraform.change_tf_extension_to_tf_json()

    def pre_generate_terraform(self):
        # To support latest terraform version
        PyTerraform.change_tf_extension_to_tf_json()
