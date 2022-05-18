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


def need_to_enable_gcp():
    feature_status = Settings.get('ENABLE_GCP', False)

    return feature_status


def get_gcp_project_ids():
    if need_to_enable_gcp():
        gcp_credentials = Settings.get('GCP_CREDENTIALS', {})
        project_id = gcp_credentials["project_id"]
        # return ",".join(project_id)
        return project_id
    else:
        return ""
