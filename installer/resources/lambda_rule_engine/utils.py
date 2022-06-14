from resources.iam.base_role import BaseRole
from resources.pacbot_app.utils import need_to_enable_azure, need_to_enable_gcp
import json
from core.config import Settings

def get_rule_engine_cloudwatch_rules_var():
    """
    Read cloudwatch rule details from the json file and build dict with required details

    Returns:
        variable_dict_input (list): List of dict of rule details used to generate terraform variable file
    """
    with open("resources/lambda_rule_engine/files/rule_engine_cloudwatch_rules.json", "r") as fp:
        data = fp.read()
    data = data.replace("role/pacman_ro", "role/" +
                        BaseRole.get_input_attr('name'))

    variable_dict_input = json.loads(data)
    required_rules = []
    for index in range(len(variable_dict_input)):
        if variable_dict_input[index]['assetGroup'] == "azure" and not need_to_enable_azure():
            continue
        elif variable_dict_input[index]['assetGroup'] == "gcp" and not need_to_enable_gcp():
            continue
        mod = int(index % 20 + 5)
        mins = Settings.CURRENT_MINUTE + mod + Settings.BUFFER_TIME_IN_MINUTES_FOR_JOB_SCHEDULING 
        if mins >= 60: 
            hrs = Settings.CURRENT_HOUR + 1
            if hrs  > 23:
                hrs = 0
            mins = mins - 60
        else:
            hrs = Settings.CURRENT_HOUR
        modHours = hrs % Settings.JOB_SCHEDULER_INTERVAL_IN_HOURS
        item = {
            'ruleId': variable_dict_input[index]['ruleUUID'],
            'ruleParams': variable_dict_input[index]['ruleParams'],
            'schedule': "cron({} {}/{},{}-{}/{} * * ? *)" .format(str(mins),str(hrs),str(Settings.JOB_SCHEDULER_INTERVAL_IN_HOURS),str(modHours),str(hrs),str(Settings.JOB_SCHEDULER_INTERVAL_IN_HOURS)) 
        }

        required_rules.append(item)

    return required_rules
