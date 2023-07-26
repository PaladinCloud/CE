from core.terraform.resources import TerraformResource
from core.config import Settings


class TlsSelfSignedCert(TerraformResource):
    """
    Base resource class for Terraform Signed Cert

    Attributes:
        resource_instance_name (str): Type of resource instance
        available_args (dict): Instance configurations
    """
    resource_instance_name = "tls_self_signed_cert"
    available_args = {
        'allowed_uses': {'required': False},
        'private_key_pem': {'required': False},
        'subject': {'required': False},
        'validity_period_hours': {'required': False},
    }

class TlsPrivateKey(TerraformResource):
    """
    Base resource class for Terraform Private key

    Attributes:
        resource_instance_name (str): Type of resource instance
        available_args (dict): Instance configurations
    """
    resource_instance_name = "tls_private_key"
    available_args = {
        'algorithm' :  {'required': False},
    }
    
