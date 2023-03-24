import yaml
import os


def create_template(base_role,template_file,TenantRoleName):

    with open(template_file, 'r') as file:
        template = yaml.safe_load(file)

    

    old_value = 'arn:aws:iam::xxxxx:role/tenant_ro'
    new_value = base_role
    # template['Resources']['paladincloudAgentRole']['Properties']['AssumeRolePolicyDocument']['Statement'][0]['Principal']['AWS'][0]=new_value
    statements = template['Resources']['paladincloudAgentRole']['Properties']['AssumeRolePolicyDocument']['Statement']
    for statement in statements:
        if old_value in statement['Principal']['AWS']:
            statement['Principal']['AWS'][statement['Principal']['AWS'].index(old_value)] = new_value


    template['Resources']['paladincloudAgentRole']['Properties']['RoleName'] = TenantRoleName


    with open(template_file, 'w') as file:
        yaml.safe_dump(template, file, sort_keys=False)


if __name__ == "__main__":
    base_role = os.getenv('TENANT_BASE_ROLE_ARN')
    template_file = os.getenv('TEMPLATE_FILE_PATH')
    TenantRoleName = os.getenv('TENANT_ROLE_NAME')
    create_template(base_role,template_file,TenantRoleName)