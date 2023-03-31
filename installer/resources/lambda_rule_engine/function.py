from core.terraform.resources.aws.aws_lambda import LambdaFunctionResource, LambdaPermission
from core.terraform.resources.aws.cloudwatch import CloudWatchEventRuleResource, CloudWatchEventTargetResource
from core.terraform.resources.variable import TerraformVariable
from resources.datastore.es import ESDomainPolicy
from resources.iam.lambda_role import LambdaRole
from resources.s3.bucket import BucketStorage
from resources.batch.job import SubmitAndRuleEngineJobDefinition, RuleEngineJobQueue
from resources.data.aws_info import AwsAccount, AwsRegion
from resources.lambda_rule_engine.s3_upload import UploadLambdaRuleEngineZipFile, RULE_ENGINE_JOB_FILE_NAME
from resources.lambda_rule_engine.utils import get_rule_engine_cloudwatch_rules_aws_var, get_rule_engine_cloudwatch_rules_azure_var, get_rule_engine_cloudwatch_rules_gcp_var
from core.config import Settings
from core.providers.aws.boto3 import cloudwatch_event
from core.mixins import MsgMixin
from resources.pacbot_app.alb import ApplicationLoadBalancer
import sys
from resources.eventbus.custom_event_bus import CloudWatchEventBusaws, CloudWatchEventBusazure, CloudWatchEventBusgcp

class RuleEngineLambdaFunction(LambdaFunctionResource):
    function_name = "ruleengine"
    role = LambdaRole.get_output_attr('arn')
    handler = RULE_ENGINE_JOB_FILE_NAME + ".lambda_handler"
    runtime = "python3.8"
    s3_bucket = BucketStorage.get_output_attr('bucket')
    s3_key = UploadLambdaRuleEngineZipFile.get_output_attr('id')
    environment = {
        'variables': {
            'JOB_QUEUE': RuleEngineJobQueue.get_input_attr('name'),
            'JOB_DEFINITION': SubmitAndRuleEngineJobDefinition.get_output_attr('arn'),
            'CONFIG_CREDENTIALS': "dXNlcjpwYWNtYW4=",
            'CONFIG_SERVICE_URL': ApplicationLoadBalancer.get_http_url() + "/api/config/rule,batch/prd/latest",
            'CONFIG_URL': ApplicationLoadBalancer.get_http_url() + "/api/config/rule,aqua-enricher/prd/latest",
            'POLICY_DETAILS_URL' : ApplicationLoadBalancer.get_http_url() + "/api/compliance/v1/policy-details-by-uuid?policyUUID="
        }
    }

    DEPENDS_ON = [SubmitAndRuleEngineJobDefinition, RuleEngineJobQueue]


class RulesListVariableAws(TerraformVariable):
    variable_name = "awsrules"
    variable_type = "list"
    default_value = []
    variable_dict_input = get_rule_engine_cloudwatch_rules_aws_var()

class RulesListVariableAzure(TerraformVariable):
    variable_name = "azurerules"
    variable_type = "list"
    default_value = []
    variable_dict_input = get_rule_engine_cloudwatch_rules_azure_var()
    # PROCESS = need_to_enable_azure()

class RulesListVariableGcp(TerraformVariable):
    variable_name = "gcprules"
    variable_type = "list"
    default_value = []
    variable_dict_input = get_rule_engine_cloudwatch_rules_gcp_var()
    # PROCESS = need_to_enable_gcp()

class RuleEngineEventRulesAws(CloudWatchEventRuleResource):
    count = RulesListVariableAws.length()
    name = RulesListVariableAws.lookup('policyId')
    event_bus_name = CloudWatchEventBusaws.get_output_attr('arn')
    event_pattern = RulesListVariableAws.lookup('event')
    
    VARIABLES = [RulesListVariableAws]
    DEPENDS_ON = [RuleEngineLambdaFunction, ESDomainPolicy]
    available_args = {
        'name': {'required': True},
        'schedule_expression': {'required': False},
        'event_bus_name' :  {'required':True},
        'event_pattern': {'required':True},
        'role_arn ': {'required': False},
        'is_enabled ': {'required': False},
        'description': {'required': False},
        'tags' : {'required':False}
    }

class RuleEngineEventRulesAzure(CloudWatchEventRuleResource):
    count = RulesListVariableAzure.length()
    name = RulesListVariableAzure.lookup('policyId')
    event_bus_name = CloudWatchEventBusazure.get_output_attr('arn')
    event_pattern = RulesListVariableAzure.lookup('event')
    
    VARIABLES = [RulesListVariableAzure]
    DEPENDS_ON = [RuleEngineLambdaFunction, ESDomainPolicy]
    available_args = {
        'name': {'required': True},
        'schedule_expression': {'required': False},
        'event_bus_name' :  {'required':True},
        'event_pattern': {'required':True},
        'role_arn ': {'required': False},
        'is_enabled ': {'required': False},
        'description': {'required': False},
        'tags' : {'required':False}
    }
    # PROCESS = need_to_enable_azure()
class RuleEngineEventRulesGcp(CloudWatchEventRuleResource):
    count = RulesListVariableGcp.length()
    name = RulesListVariableGcp.lookup('policyId')
    event_bus_name = CloudWatchEventBusgcp.get_output_attr('arn')
    event_pattern = RulesListVariableGcp.lookup('event')
    
    VARIABLES = [RulesListVariableGcp]
    DEPENDS_ON = [RuleEngineLambdaFunction, ESDomainPolicy]
    available_args = {
        'name': {'required': True},
        'schedule_expression': {'required': False},
        'event_bus_name' :  {'required':True},
        'event_pattern': {'required':False,},
        'role_arn ': {'required': False},
        'is_enabled ': {'required': False},
        'description': {'required': False},
        'tags' : {'required':False}
    }
    # PROCESS = need_to_enable_gcp()

    def check_exists_before(self, input, tf_outputs):
        """
        This method overrides the base class method, since here we need to check the existennce of a list of CW rules
        """
        exists = False
        checked_details = {}

        if not self.resource_in_tf_output(tf_outputs):
            for rule in get_rule_engine_cloudwatch_rules_aws_var():
                rule_name = rule['policyId']
                exists = cloudwatch_event.check_rule_exists(
                    rule_name,
                    input.AWS_AUTH_CRED)
            
                if exists:
                    checked_details = {'attr': "name", 'value': rule_name}
                    break

            for rule in get_rule_engine_cloudwatch_rules_azure_var():
                # if  not need_to_enable_azure():
                #     continue
                rule_name = rule['policyId']
                exists = cloudwatch_event.check_rule_exists(
                    rule_name,
                    input.AWS_AUTH_CRED)
            
                if exists:
                    checked_details = {'attr': "name", 'value': rule_name}
                    break

            for rule in get_rule_engine_cloudwatch_rules_gcp_var():
                # if  not need_to_enable_gcp():
                #     continue
                rule_name = rule['policyId']
                exists = cloudwatch_event.check_rule_exists(
                    rule_name,
                    input.AWS_AUTH_CRED)
            
                if exists:
                    checked_details = {'attr': "name", 'value': rule_name}
                    break


        return exists, checked_details
        
            

    def pre_terraform_destroy(self):
        """
        Remove all targets from the coudwatch rules before starting destroy. This is required as it would be possible to delete
        and re create cloudwatch rules and then attach targets in the PacBot application. So terraform cannot track them and fail to
        execute destroy.
        """
        for rule in get_rule_engine_cloudwatch_rules_aws_var():
            rule_name = rule['policyId']
            try:
                cloudwatch_event.remove_all_targets_of_a_rule(
                    rule_name,
                    Settings.AWS_AUTH_CRED)
            except Exception as e:
                message = "\n\t ** Not able to remove targets from the rule: %s: Reason: %s **\n" % (rule_name, str(e))
                print(MsgMixin.BERROR_ANSI + message + MsgMixin.RESET_ANSI)
                sys.exit()

        for rule in get_rule_engine_cloudwatch_rules_azure_var():
            # if  not need_to_enable_azure():
            #     continue
            rule_name = rule['policyId']
            try:
                cloudwatch_event.remove_all_targets_of_a_rule(
                    rule_name,
                    Settings.AWS_AUTH_CRED)
            except Exception as e:
                message = "\n\t ** Not able to remove targets from the rule: %s: Reason: %s **\n" % (rule_name, str(e))
                print(MsgMixin.BERROR_ANSI + message + MsgMixin.RESET_ANSI)
                sys.exit()

        for rule in get_rule_engine_cloudwatch_rules_gcp_var():
            # if  not need_to_enable_gcp():
            #     continue
            rule_name = rule['policyId']
            try:
                cloudwatch_event.remove_all_targets_of_a_rule(
                    rule_name,
                    Settings.AWS_AUTH_CRED)
            except Exception as e:
                message = "\n\t ** Not able to remove targets from the rule: %s: Reason: %s **\n" % (rule_name, str(e))
                print(MsgMixin.BERROR_ANSI + message + MsgMixin.RESET_ANSI)
                sys.exit()


class RuleEngineCloudWatchEventTargetsAws(CloudWatchEventTargetResource):
    count = RulesListVariableAws.length()
    rule = RulesListVariableAws.lookup('policyId')
    arn = RuleEngineLambdaFunction.get_output_attr('arn')
    target_id = RuleEngineLambdaFunction.get_input_attr('function_name') + '-target'
    target_input = RulesListVariableAws.lookup('policyParams')
    event_bus_name = CloudWatchEventBusaws.get_output_attr('arn')
    DEPENDS_ON = [RuleEngineEventRulesAws]

class RuleEngineCloudWatchEventTargetsAzure(CloudWatchEventTargetResource):
    count = RulesListVariableAzure.length()
    rule = RulesListVariableAzure.lookup('policyId')
    arn = RuleEngineLambdaFunction.get_output_attr('arn')
    target_id = RuleEngineLambdaFunction.get_input_attr('function_name') + '-target'
    target_input = RulesListVariableAzure.lookup('policyParams')
    event_bus_name = CloudWatchEventBusazure.get_output_attr('arn')
    DEPENDS_ON = [RuleEngineEventRulesAzure]
    
class RuleEngineCloudWatchEventTargetsGcp(CloudWatchEventTargetResource):
    count = RulesListVariableGcp.length()
    rule = RulesListVariableGcp.lookup('policyId')
    arn = RuleEngineLambdaFunction.get_output_attr('arn')
    target_id = RuleEngineLambdaFunction.get_input_attr('function_name') + '-target'
    target_input = RulesListVariableGcp.lookup('policyParams')
    event_bus_name = CloudWatchEventBusgcp.get_output_attr('arn')
    DEPENDS_ON = [RuleEngineEventRulesGcp]
    

class EventRulesLambdaPermissions(LambdaPermission):
    statement_id = "sid-" + Settings.AWS_ACCOUNT_ID
    action = "lambda:InvokeFunction"
    function_name = RuleEngineLambdaFunction.get_output_attr('function_name')
    principal = "events.amazonaws.com"
    # source_arn = RuleEngineEventRulesAWS.get_output_attr(key='arn', index=True) #Not required since there are huge list of rules

    DEPENDS_ON = [RuleEngineCloudWatchEventTargetsAws]
