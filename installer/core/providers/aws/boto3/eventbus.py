from core.providers.aws.boto3 import prepare_aws_client_with_given_cred
import boto3

def get_event_client(aws_auth_cred):
    """
    Returns the client object for AWS Events

    Args:
        aws_auth (dict): Dict containing AWS credentials

    Returns:
        obj: AWS Cloudwatch Event Client Obj
    """
    return prepare_aws_client_with_given_cred("events", aws_auth_cred)

def check_es_event_bus_exists(name, aws_auth_cred):
    """
    Check wheter the given ES Domain already exists in the AWS Account

    Args:
        domain_name (str): ES Domain name
        aws_auth (dict): Dict containing AWS credentials

    Returns:
        Boolean: True if env exists else False
    """
    client = get_event_client(aws_auth_cred)
    try:
        response = client.describe_event_bus(
            Name=name
        )
        return True if response['Name'] else False
    except:
        return False