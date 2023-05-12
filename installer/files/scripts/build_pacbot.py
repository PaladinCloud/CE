import os
import shutil
import subprocess
import time

from datetime import datetime
from utils import prepare_aws_client_with_given_aws_details, get_provider_details

class Buildpacbot(object):
    """
    Build PacBot services

    Attributes:
        mvn_build_command (str): Maven build command to be executed
        mvn_clean_command (str): Maven clean command to be executed
        archive_type (str): Archive format
        issue_email_template (str): file to make public after uploading to s3
    """
    mvn_build_command = "/opt/apache-maven-3.6.3/bin/mvn install -DskipTests=true -Dmaven.javadoc.skip=true -B -V"
    mvn_clean_command = "/opt/apache-maven-3.6.3/bin/mvn clean"
    archive_type = "zip"  # What type of archive is required
    issue_email_template = ''

    def __init__(self, aws_details, api_domain_url, upload_dir, log_dir, pacbot_code_dir, enable_vulnerability_feature,
                 auth_type, tenant_id, client_id):
        self.api_domain_url = api_domain_url
        self.cwd = pacbot_code_dir
        self.codebase_root_dir = pacbot_code_dir
        self.debug_log = os.path.join(log_dir, "debug.log")
        self.maven_build_log = os.path.join(log_dir, "maven_build.log")
        self.upload_dir = upload_dir
        self.s3_client = prepare_aws_client_with_given_aws_details('s3', aws_details)
        self.enable_vulnerability_feature = enable_vulnerability_feature
        self.auth_type = auth_type
        self.tenant_id = tenant_id
        self.client_id = client_id
        self.client_cognito_id = client_cognito_id
        self.client_cognito_secret = client_cognito_secret
        self.domain_cognito_url = domain_cognito_url
        self.cognito_callback_url = cognito_callback_url
        self.cognito_logout_url = cognito_logout_url
        self.cloudformation_template_url = cloudformation_template_url
        self.appsyncapikey = appsyncapikey
        self.region = region
        self.appsync_url = appsync_url
        self.lambda_path = lambda_path
        self.google_analytics = google_analytics

    def _clean_up_all(self):
        os.chdir(self.cwd)

    def build_api_and_ui_apps(self, bucket, s3_key_prefix):
        self.upload_ui_files_to_s3(bucket)

        print("Maven build started...\n")

        self.build_jar_and_ui_from_code(bucket, s3_key_prefix)
        self.archive_ui_app_build(bucket, s3_key_prefix)
        self.write_to_debug_log("Maven build completed!!!")
        print("Maven build completed!!!\n")

        self._clean_up_all()

    def upload_ui_files_to_s3(self, bucket):
        """
        Upload email template files to s3 from codebase

        Args:
            bucket (str): S3 bucket name
        """
        print("Uploading Email templates to S3...............\n")
        self.write_to_debug_log("Uploading email template files to S3...")
        folder_to_upload = "pacman-v2-email-template"
        local_folder_path = os.path.join(self.codebase_root_dir, 'emailTemplates', folder_to_upload)
        files_to_upload = []

        for (dirpath, dirnames, file_names) in os.walk(local_folder_path):
            files_to_upload = file_names

        if not files_to_upload:
            raise Exception("Email template files are not found in %s" % str(local_folder_path))

        for file_name in files_to_upload:
            file_path = os.path.join(local_folder_path, file_name)
            extra_args = {'ACL': 'public-read'}  # To make this public
            key = folder_to_upload + '/' + file_name

            # To be added in config.ts
            self.issue_email_template = '%s/%s/%s' % (self.s3_client.meta.endpoint_url, bucket, folder_to_upload)
            self.s3_client.upload_file(file_path, bucket, key, ExtraArgs=extra_args)

        self.write_to_debug_log("Email templates upload to S3 completed!!!")
        print("Email templates upload to S3 completed!!!\n")

    def run_bash_command(self, command, exec_dir):
        """
        Run bash command supplied to be run

        Args:
            command (str): Command to run
            exec_dir (path): from which dir the command to be run

        Returns:
            stdout (str): Response from command prompt
            stderr (str): Error occured if any
        """
        command = command + ' &>>' + self.maven_build_log
        os.chdir(exec_dir)
        p = subprocess.Popen(command, shell=True, stdout=subprocess.PIPE)
        time.sleep(5)
        stdout, stderr = p.communicate()
        if p.returncode == 1:
            raise Exception("Error: PacBot maven build failed. Please check log, %s " % str(self.maven_build_log))
        elif stderr:
            raise Exception("Error: PacBot maven build failed. Please check log, %s " % str(self.maven_build_log))

        return stdout, stderr

    def build_jar_and_ui_from_code(self, bucket, s3_key_prefix):
        """
        Build jars and upload to S3

        Args:
            bucket (str): S3 bucket name
            s3_key_prefix (str): Under which folder this has be uploaded
        """
        webapp_dir = self._get_web_app_directory()
        self._update_variables_in_ui_config(webapp_dir)

        self.build_api_job_jars(self.codebase_root_dir)
        self._replace_webapp_new_config_with_original(webapp_dir)
        self.upload_jar_files(self.codebase_root_dir, bucket, s3_key_prefix)
        self.update_lambda_jar_files(self.codebase_root_dir, bucket, s3_key_prefix)

    def build_api_job_jars(self, working_dir):
        print("Started building the jar...............\n")
        self.write_to_debug_log(
            "Maven build started...(Please check %s log for more details)" % str(self.maven_build_log))

        stdout, stderr = self.run_bash_command(self.mvn_clean_command, working_dir)
        stdout, stderr = self.run_bash_command(self.mvn_build_command, working_dir)
        self.write_to_debug_log("Build Completed...")

    def upload_jar_files(self, working_dir, bucket, s3_key_prefix):
        folders = [
            os.path.join(working_dir, "dist", "api"),
            os.path.join(working_dir, "dist", "jobs"),
            os.path.join(working_dir, "dist", "lambda")
        ]

        for folder in folders:
            if os.path.exists(folder):
                files = os.walk(folder).__next__()[2]
                for jarfile in files:
                    copy_file_from = os.path.join(folder, jarfile)
                    s3_jar_file_key = str(os.path.join(s3_key_prefix, jarfile))
                    self.write_to_debug_log("JAR File: %s, Uploading to S3..." % s3_jar_file_key)
                    self.s3_client.upload_file(copy_file_from, bucket, s3_jar_file_key)
                    self.write_to_debug_log("JAR File: %s, Uploaded to S3" % s3_jar_file_key)

    def update_lambda_jar_files(self, working_dir, bucket, s3_key_prefix):
        folders = [
            os.path.join(working_dir, "dist", "lambda")
        ]

        for folder in folders:
            if os.path.exists(folder):
                files = os.walk(folder).__next__()[2]
                for jarfile in files:
                    copy_file_from = os.path.join(folder, jarfile)
                    s3_jar_file_key = str(os.path.join(s3_key_prefix, self.lambda_path ,jarfile))
                    self.write_to_debug_log("JAR File: %s, Uploading to S3..." % s3_jar_file_key)
                    self.s3_client.upload_file(copy_file_from, bucket, s3_jar_file_key)
                    self.write_to_debug_log("JAR File: %s, Uploaded to S3" % s3_jar_file_key)

    def _get_web_app_directory(self):
        return os.path.join(self.codebase_root_dir, "webapp")

    def _update_variables_in_ui_config(self, webapp_dir):
        config_file = os.path.join(webapp_dir, "src", "config", "configurations.ts")
        with open(config_file, 'r') as f:
            lines = f.readlines()
        shutil.copy2(config_file, config_file + ".original")  # Backup of the original to be used to replace later
        for idx, line in enumerate(lines):
            if "DEV_BASE_URL: ''" in line or "STG_BASE_URL: ''" in line or "PROD_BASE_URL: ''" in line:
                lines[idx] = lines[idx].replace("_BASE_URL: ''", "_BASE_URL: '" + self.api_domain_url + "/api'")

            if "AD_AUTHENTICATION: false" in line:
                lines[idx] = lines[idx].replace("AD_AUTHENTICATION: false", "AD_AUTHENTICATION: true")

            if "qualysEnabled: false" in line:
                lines[idx] = lines[idx].replace("qualysEnabled: false",
                                                "qualysEnabled: %s" % self.enable_vulnerability_feature)

            if "ISSUE_MAIL_TEMPLATE_URL: ''" in line:
                lines[idx] = lines[idx].replace("ISSUE_MAIL_TEMPLATE_URL: ''",
                                                "ISSUE_MAIL_TEMPLATE_URL: '" + self.issue_email_template + "'")

            if "url: ''" in line:
                lines[idx] = lines[idx].replace("url: ''",
                                                "url: '" + self.appsync_url + "'")
                
            if "region: ''" in line:
                lines[idx] = lines[idx].replace("region: ''",
                                                "region: '" + self.region + "'")
            if "apiKey: ''" in line:
                lines[idx] = lines[idx].replace("apiKey: ''",
                                                "apiKey: '" + self.appsyncapikey + "'")
            
            if "gaKey: ''" in line:
                lines[idx] = lines[idx].replace("gaKey: ''",
                                                "gaKey: '" + self.google_analytics + "'")
            
            
            if self.auth_type == "AZURE_AD":
                if "AUTH_TYPE: DB" in line:
                    lines[idx] = lines[idx].replace("AUTH_TYPE: DB", "AUTH_TYPE: AZURE_SSO")
                if "tenant: '" in line:
                    lines[idx] = "tenant: '" + self.tenant_id + "',\n"
                if "clientId: '" in line:
                    lines[idx] = "clientId: '" + self.client_id + "'"

            if self.auth_type == "DB":
                if "AUTH_TYPE: AZURE_SSO" in line:
                    lines[idx] = lines[idx].replace("AUTH_TYPE: AZURE_SSO", "AUTH_TYPE: DB")

            if self.auth_type == "COGNITO":
                if "AUTH_TYPE: DB" in line:
                    lines[idx] = lines[idx].replace("AUTH_TYPE: DB", "AUTH_TYPE: COGNITO")
                if "sso_api_username: '" in line:
                    lines[idx] = "sso_api_username: '" + self.client_cognito_id + "',\n"
                if "sso_api_pwd: '" in line:
                    lines[idx] = "sso_api_pwd: '" + self.client_cognito_secret + "',\n"
                if "loginURL: '" in line:
                    lines[
                        idx] = "loginURL: '" + self.domain_cognito_url + "/login?" + "client_id=" + self.client_cognito_id + "&response_type=code&scope=openid+profile&" + "redirect_uri=" + self.cognito_callback_url + "',\n"
                if "redirectURL: '" in line:
                    lines[idx] = "redirectURL: '" + self.cognito_callback_url + "',\n"
                if "cognitoTokenURL: '" in line:
                    lines[idx] = "cognitoTokenURL: '" + self.domain_cognito_url + "/oauth2/token" + "',\n"
                if "logout: '" in line:
                    lines[idx] = "logout: '" + self.domain_cognito_url + "/logout?" + "client_id=" + self.client_cognito_id + "&response_type=code&scope=openid+profile&" + "redirect_uri=" + self.cognito_callback_url + "',\n"
                if "CloudformationTemplateUrl: '" in line:
                    lines[idx] = "CloudformationTemplateUrl:'" + self.cloudformation_template_url + "'\n"

        with open(config_file, 'w') as f:
            f.writelines(lines)

    def _replace_webapp_new_config_with_original(self, webapp_dir):
        '''
        This method replace the modified new file with the original configuration.ts file
        and keep the modified file in configuration.ts.new
        '''
        config_file = os.path.join(webapp_dir, "src", "config", "configurations.ts")
        original_config_file = config_file + ".original"
        new_config_file = config_file + ".new"
        shutil.copy2(config_file, new_config_file)
        shutil.copy2(original_config_file, config_file)
        os.remove(original_config_file)

    def archive_ui_app_build(self, bucket, s3_key_prefix):
        zip_file_name = self.upload_dir + "/dist"

        print("Started creating zip file...")
        dir_to_archive = os.path.join(self.codebase_root_dir, "dist/pacmanspa")
        shutil.make_archive(zip_file_name, self.archive_type, dir_to_archive)

        s3_zip_file_key = str(os.path.join(s3_key_prefix, "dist.zip"))
        self.write_to_debug_log("Zip File: %s, Uploading to S3..." % s3_zip_file_key)
        self.s3_client.upload_file(zip_file_name + ".zip", bucket, s3_zip_file_key)
        self.write_to_debug_log("Zip File: %s, Uploaded to S3" % s3_zip_file_key)
        os.remove(zip_file_name + ".zip")

    def write_to_debug_log(self, msg):
        now = datetime.now().strftime('%Y-%m-%d %H:%M:%S')
        with open(self.debug_log, 'a+') as logfile:
            logfile.write("%s: %s\n" % (now, msg))

if __name__ == "__main__":
    """
    This script is executed from the provisioner of terraform resource to build pacbot app
    """
    api_domain_url = os.getenv('APPLICATION_DOMAIN')
    pacbot_code_dir = os.getenv('PACBOT_CODE_DIR')
    dist_files_upload_dir = os.getenv('DIST_FILES_UPLOAD_DIR')
    log_dir = os.getenv('LOG_DIR')
    provider_json_file = os.getenv('PROVIDER_FILE')
    s3_bucket = os.getenv('S3_BUCKET')
    s3_key_prefix = os.getenv('S3_KEY_PREFIX')
    enable_vulnerability_feature = os.getenv('ENABLE_VULNERABILITY_FEATURE')
    aws_details = get_provider_details("aws", provider_json_file)
    auth_type = os.getenv('AUTHENTICATION_TYPE')
    tenant_id = os.getenv('AD_TENANT_ID')
    client_id = os.getenv('AD_CLIENT_ID')
    client_cognito_id = os.getenv('COGNITO_CLIENT_ID')
    client_cognito_secret = os.getenv('COGNITO_CLIENT_SECRET')
    domain_cognito_url = "https://" + str(os.getenv('COGNITO_DOMAIN')) + ".auth." + str(
        os.getenv('AWS_REGION')) + ".amazoncognito.com"
    cognito_callback_url = os.getenv('APPLICATION_DOMAIN') + "/callback"
    cognito_logout_url = "https://" + os.getenv('APPLICATION_DOMAIN') + "/home"
    cloudformation_template_url = "https://" + os.getenv('S3_BUCKET') + ".s3." + os.getenv(
        'AWS_REGION') + ".amazonaws.com/deployment.yaml"
    appsync_url =  os.getenv('APPSYNC_URL')
    region =  os.getenv('AWS_REGION')
    appsyncapikey =  os.getenv('APPSYNC_API_KEY')
    lambda_path = os.getenv('LAMBDA_PATH')
    google_analytics = os.getenv('GOOGLE_ANALYTICS')
    Buildpacbot(
        aws_details,
        api_domain_url,
        dist_files_upload_dir,
        log_dir,
        pacbot_code_dir,
        enable_vulnerability_feature, auth_type, tenant_id, client_id).build_api_and_ui_apps(
        s3_bucket,
        s3_key_prefix
    )
