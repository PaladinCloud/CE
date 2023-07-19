from core.terraform.resources.localfile import PrivateKeyFile
from core.terraform.resources.tls import TlsPrivateKey, TlsSelfSignedCert
from core.terraform.resources.aws.acm import AcmCertificate

class SslPrivateKey(TlsPrivateKey):
    algorithm = 'RSA'

class TlsSelfSignedCert(TlsSelfSignedCert):
    allowed_uses = [ "key_encipherment","digital_signature","server_auth"]
    private_key_pem = SslPrivateKey.get_output_attr('private_key_pem')
    subject = [
        {
            "common_name": "*.amazonaws.com",
            "organization": "paladincloud"
        }
    ]
    validity_period_hours = 8760
    
class PrivateKey(PrivateKeyFile):
    filename = "paladincloudssl-key.pem"
    content = SslPrivateKey.get_output_attr('private_key_pem')
    
class SelfSignedCert(PrivateKeyFile):
    filename = "selfsignedcertificate-key.pem"
    content = TlsSelfSignedCert.get_output_attr('cert_pem')

class AcmCertificate(AcmCertificate):
    certificate_body       = SelfSignedCert.get_output_attr('content')
    private_key            = PrivateKey.get_output_attr('content')
    certificate_chain      = SelfSignedCert.get_output_attr('content')
    
    

    
    
    
