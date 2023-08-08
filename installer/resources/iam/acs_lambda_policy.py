from core.terraform.resources.aws import iam
from core.config import Settings
from resources.notification.apigateway import AcsSNS
from resources.iam.acs_lambda_role import LambdaAcsRole

class LambdaFullAccessPolicyAttach(iam.IAMRolePolicyAttachmentResource):
    role = LambdaAcsRole.get_output_attr('name')
    policy_arn = "arn:aws:iam::aws:policy/AWSLambda_FullAccess"


class LambdaXrayFullAccessPolicyAttach(iam.IAMRolePolicyAttachmentResource):
    role = LambdaAcsRole.get_output_attr('name')
    policy_arn = "arn:aws:iam::aws:policy/AWSXrayWriteOnlyAccess"


class SnsAcsDocument(iam.IAMPolicyDocumentData):
	statement = [	 
        	{
            	"effect": "Allow",
            	"actions": ["sns:Publish"],
            	"resources": [AcsSNS.get_output_attr('arn')]
        	}
	]
class SnsAcsPolicy(iam.IAMRolePolicyResource):
    name = "PaladinCloudSNSAcessForACSLambda"
    path = '/'
    policy = SnsAcsDocument.get_output_attr('json')



class SnsAcsPolicyAttach(iam.IAMRolePolicyAttachmentResource):
    role = LambdaAcsRole.get_output_attr('name')
    policy_arn = SnsAcsPolicy.get_output_attr('arn')
