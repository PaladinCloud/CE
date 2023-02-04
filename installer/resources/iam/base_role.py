from core.terraform.resources.aws import iam
from resources.iam.ecs_role import ECSRole
from core.config import Settings


class PolicyDocumentForBaseRole(iam.IAMPolicyDocumentData):
    statement = [
        {
            'actions': ["sts:AssumeRole"],
            'principals': {
                'type': "Service",
                'identifiers': [
                    "batch.amazonaws.com",
                    "ecs-tasks.amazonaws.com"
                ]
            }
        },
        {
            'actions': ["sts:AssumeRole"],
            'principals': {
                'type': "AWS",
                'identifiers': [ECSRole.get_output_attr('arn')]
            }
        }
    ]


class BaseRole(iam.IAMRoleResource):
    name = "ro"
    assume_role_policy = PolicyDocumentForBaseRole.get_output_attr('json')
    force_detach_policies = True

class BaseGuardDutyPolicyAttach(iam.IAMRolePolicyAttachmentResource):
    role = BaseRole.get_output_attr('name')
    policy_arn = "arn:aws:iam::aws:policy/AmazonGuardDutyReadOnlyAccess"


class BaseAWSSupportPolicyAttach(iam.IAMRolePolicyAttachmentResource):
    role = BaseRole.get_output_attr('name')
    policy_arn = "arn:aws:iam::aws:policy/AWSSupportAccess"

class BaseCloudWatchEventFullAcessPolicyAttach(iam.IAMRolePolicyAttachmentResource):
    role = BaseRole.get_output_attr('name')
    policy_arn = "arn:aws:iam::aws:policy/CloudWatchEventsFullAccess"


class ECSTaskExecutionRolePolicyDocument(iam.IAMPolicyDocumentData):
    statement = [
        {
            "effect": "Allow",
            "actions": ["ecr:*"],
            "resources": [
                "arn:aws:ecr:%s:%s:repository/%s-*" % (Settings.AWS_REGION, str(Settings.AWS_ACCOUNT_ID), Settings.RESOURCE_NAME_PREFIX)
            ]
        },
        {
            "effect": "Allow",
            "actions": ["logs:*"],
            "resources": ["*"]
        },
        {
            "effect": "Allow",
            "actions": ["s3:getPublicAccessBlock"],
            "resources": ["*"]
        }
    ]


class ECSTaskExecutionRolePolicy(iam.IAMRolePolicyResource):
    name = "ecs_task_exec"
    path = '/'
    policy = ECSTaskExecutionRolePolicyDocument.get_output_attr('json')


class BaseECSTaskExecPolicyAttach(iam.IAMRolePolicyAttachmentResource):
    role = BaseRole.get_output_attr('name')
    policy_arn = ECSTaskExecutionRolePolicy.get_output_attr('arn')

class PolicySpecificReadOnlyAccessPolicyDocument(iam.IAMPolicyDocumentData):
    statement = [
        {
            "effect": "Allow",
            "actions": [
				"access-analyzer:ListAnalyzers",
                "acm:DescribeCertificate",
                "acm:ListCertificates",
                "apigateway:GET",
                "appflow:ListFlows",
                "athena:ListQueryExecutions",
                "autoscaling:DescribeAutoScalingGroups",
                "autoscaling:DescribeLaunchConfigurations",
                "autoscaling:DescribePolicies",
                "backup:ListBackupVaults",
                "cloudformation:DescribeStacks",
                "cloudformation:ListStacks",
                "cloudfront:GetDistributionConfig",
                "cloudfront:ListDistributions",
                "cloudtrail:DescribeTrails",
                "cloudtrail:GetEventSelectors",
                "cloudtrail:GetTrailStatus",
                "cloudtrail:LookupEvents",
                "cloudwatch:DescribeAlarms",
                "comprehend:ListEntitiesDetectionJobs",
                "dax:DescribeClusters",
                "directconnect:Describe*",
                "dms:DescribeReplicationInstances",
                "dynamodb:Describe*",
                "dynamodb:List*",
                "ec2:Describe*",
                "ecs:DescribeClusters",
                "ecs:DescribeTaskDefinition",
                "ecs:List*",
                "eks:ListClusters",
                "elasticache:Describe*",
                "elasticache:List*",
                "elasticfilesystem:DescribeFileSystems",
                "elasticloadbalancing:DescribeListeners",
                "elasticloadbalancing:DescribeLoadBalancerAttributes",
                "elasticloadbalancing:DescribeLoadBalancers",
                "elasticloadbalancing:DescribeRules",
                "elasticloadbalancing:DescribeTags",
                "elasticloadbalancing:DescribeTargetGroups",
                "elasticloadbalancing:DescribeTargetHealth",
                "elasticmapreduce:ListClusters",
                "es:DescribeElasticsearchDomains",
                "es:ListDomainNames",
                "es:ListTags",
                "firehose:DescribeDeliveryStream",
                "firehose:ListDeliveryStreams",
                "firehose:ListTagsForDeliveryStream",
                "health:DescribeAffectedEntities",
                "health:DescribeEventDetails",
                "health:DescribeEvents",
                "iam:GetAccessKeyLastUsed",
                "iam:ListAccessKeys",
                "iam:ListAttachedGroupPolicies",
                "iam:ListGroups",
                "iam:ListGroupsForUser",
                "iam:ListMFADevices",
                "iam:ListPolicies",
                "iam:ListRoles",
                "iam:ListServerCertificates",
                "iam:ListUsers",
                "kinesis:ListStreams",
                "kinesisvideo:ListStreams",
                "kms:DescribeKey",
                "kms:GetKeyRotationStatus",
                "kms:ListAliases",
                "kms:ListKeys",
                "kms:ListResourceTags",
                "lambda:GetPolicy",
                "lambda:List*",
                "rds:Describe*",
                "rds:List*",
                "redshift:DescribeClusters",
                "redshift:DescribeLoggingStatus",
                "route53:List*",
                "s3:GetBucketLocation",
                "s3:GetBucketLogging",
                "s3:GetBucketNotification",
                "s3:GetBucketTagging",
                "s3:ListAllMyBuckets",
                "s3:PutBucketNotification",
                "securityhub:DescribeHub",
                "sns:List*",
                "SNS:ListTopics",
                "sqs:ListQueues",
                "ssm:DescribeInstanceInformation"
			
			],
            "resources": ["*"]
        }
    ]


class PaladinCloudReadOnlyAccessRolePolicy(iam.IAMRolePolicyResource):
    name = "ReadOnlyAccessForAWSPolicies"
    path = '/'
    policy = PolicySpecificReadOnlyAccessPolicyDocument.get_output_attr('json')


class PaladinCloudReadOnlyAccessPolicyAttach(iam.IAMRolePolicyAttachmentResource):
    role = BaseRole.get_output_attr('name')
    policy_arn = PaladinCloudReadOnlyAccessRolePolicy.get_output_attr('arn')


class PaladinCognitoUserPoolFullAccessDocument(iam.IAMPolicyDocumentData):
	statement = [	 
        	{
            	"effect": "Allow",
            	"actions": ["cognito-idp:*"],
            	"resources": ["*"]
        	}
	]
class PaladinCognitoUserPoolFullAccessPolicy(iam.IAMRolePolicyResource):
    name = "PaladinCognitoUserPoolFullAccess"
    path = '/'
    policy = PaladinCognitoUserPoolFullAccessDocument.get_output_attr('json')



class PaladinCognitoUserPoolFullAccessAttach(iam.IAMRolePolicyAttachmentResource):
    role = BaseRole.get_output_attr('name')
    policy_arn = PaladinCognitoUserPoolFullAccessPolicy.get_output_attr('arn')