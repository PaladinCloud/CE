from core.terraform.resources.aws.load_balancer import ALBListenerResource, ALBListenerRuleResource
from core.config import Settings
from resources.pacbot_app.alb import ApplicationLoadBalancer
from resources.pacbot_app import alb_target_groups as tg
from resources.pacbot_app.utils import need_to_deploy_vulnerability_service
from core.mixins import MsgMixin
import sys

# PATH_PREFIX = '/api/'


class PacBotHttpsListener(ALBListenerResource):
    load_balancer_arn = ApplicationLoadBalancer.get_output_attr('arn')
    port = 443
    protocol = "HTTPS"
    ssl_policy = "ELBSecurityPolicy-2016-08"
    certificate_arn = Settings.get('SSL_CERTIFICATE_ARN')
    default_action_target_group_arn = tg.NginxALBTargetGroup.get_output_attr('arn')
    default_action_type = "forward"


    def pre_generate_terraform(self):
        warn_msg = "MAKE_ALB_INTERNAL can be false"
        if ((Settings.MAKE_ALB_INTERNAL == False) and (Settings.ALB_PROTOCOL == "HTTP")):
            message = "\n\t ** %s **\n" % warn_msg
            print(MsgMixin.BERROR_ANSI + message + MsgMixin.RESET_ANSI)
            sys.exit()


class BaseLR:
    listener_arn = PacBotHttpsListener.get_output_attr('arn')
    action_type = "forward"
    # condition_field = "path_pattern"


class ConfigALBHttpsListenerRule(ALBListenerRuleResource, BaseLR):
    action_target_group_arn = tg.ConfigALBTargetGroup.get_output_attr('arn')
    condition = {
        "path_pattern" : {
            "values" : ["/api/config*"]
        }
    }


class AdminALBHttpsListenerRule(ALBListenerRuleResource, BaseLR):
    action_target_group_arn = tg.AdminALBTargetGroup.get_output_attr('arn')
    condition = {
        "path_pattern" : {
            "values" : ["/api/admin*"]
        }
    }


class ComplianceALBHttpsListenerRule(ALBListenerRuleResource, BaseLR):
    action_target_group_arn = tg.ComplianceALBTargetGroup.get_output_attr('arn')
    condition = {
        "path_pattern" : {
            "values" : ["/api/compliance*"]
        }
    }


class NotificationsALBHttpsListenerRule(ALBListenerRuleResource, BaseLR):
    action_target_group_arn = tg.NotificationsALBTargetGroup.get_output_attr('arn')
    condition = {
        "path_pattern" : {
            "values" : ["/api/notifications*"]
        }
    }


class StatisticsALBHttpsListenerRule(ALBListenerRuleResource, BaseLR):
    action_target_group_arn = tg.StatisticsALBTargetGroup.get_output_attr('arn')
    condition = {
        "path_pattern" : {
            "values" : ["/api/statistics*"]
        }
    }


class AssetALBHttpsListenerRule(ALBListenerRuleResource, BaseLR):
    action_target_group_arn = tg.AssetALBTargetGroup.get_output_attr('arn')
    condition = {
        "path_pattern" : {
            "values" : ["/api/asset*"]
        }
    }


class AuthALBHttpsListenerRule(ALBListenerRuleResource, BaseLR):
    action_target_group_arn = tg.AuthALBTargetGroup.get_output_attr('arn')
    condition = {
        "path_pattern" : {
            "values" : ["/api/auth*"]
        }
    }


class VulnerabilityALBHttpsListenerRule(ALBListenerRuleResource, BaseLR):
    action_target_group_arn = tg.VulnerabilityALBTargetGroup.get_output_attr('arn')
    condition = {
        "path_pattern" : {
            "values" : ["/api/vulnerability*"]
        }
    }
    PROCESS = need_to_deploy_vulnerability_service()
