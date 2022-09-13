import json
from core.config import Settings


def need_to_deploy_vulnerability_service():
    feature_status = Settings.get('ENABLE_VULNERABILITY_FEATURE', False)

    return feature_status


def need_to_enable_azure():
    feature_status = Settings.get('ENABLE_AZURE', False)

    return feature_status


def get_azure_tenants():
    if need_to_enable_azure():
        tenants = Settings.get('AZURE_TENANTS', [])
        tenant_ids = [tenant['tenantId'] for tenant in tenants]

        return ",".join(tenant_ids)
    else:
        return ""

def get_aws_account_details():
    aws_accounts = Settings.get('AWS_ACCOUNT_DETAILS', [])
    account_info = [account['accountId'] +  ':' +account['accountName'] for account in aws_accounts]

    return ",".join(account_info)


def need_to_enable_gcp():
    feature_status = Settings.get('ENABLE_GCP', False)

    return feature_status


def get_gcp_project_ids():
    if need_to_enable_gcp():
        project_ids = Settings.get('GCP_PROJECT_IDS', [])
        projects = ",".join(map(str, project_ids))
        return projects
    else:
        return ""
