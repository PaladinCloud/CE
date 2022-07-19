from core.providers.aws.boto3 import prepare_aws_client_with_given_cred
import boto3

def get_kms_client(aws_auth_cred):
    """
    Returns the client object for AWS ECS

    Args:
        aws_auth (dict): Dict containing AWS credentials

    Returns:
        obj: AWS ECS Object
    """
    return prepare_aws_client_with_given_cred("kms", aws_auth_cred)

def check_kms_key_exists(kms_name, aws_auth_cred):
    client = get_kms_client(aws_auth_cred)
    try:
        response = client.kms_key_name(
            kmskeyidentifier=kms_name
        )
        return True if len(response['KMSname']) else False
    except:
        return False