from core.terraform.resources.aws.sns import SNSResources, SNSSubscription
from core.config import Settings
from resources.lambda_rule_engine.function import RuleEngineLambdaFunction

class PolicyDoneSNS(SNSResources):
    name = "policy-done"

class TemplateSubscription(SNSSubscription):
    topic_arn = PolicyDoneSNS.get_output_attr('arn')
    protocol = "lambda"
    endpoint =  RuleEngineLambdaFunction.get_output_attr('arn')
