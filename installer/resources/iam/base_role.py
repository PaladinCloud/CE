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


#class BaseReadOnlyAccessPolicyAttach(iam.IAMRolePolicyAttachmentResource):
 #   role = BaseRole.get_output_attr('name')
  #  policy_arn = "arn:aws:iam::aws:policy/ReadOnlyAccess"


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
				"access-analyzer:GetAccessPreview",
				"access-analyzer:GetAnalyzedResource",
				"access-analyzer:GetAnalyzer",
				"access-analyzer:GetArchiveRule",
				"access-analyzer:GetFinding",
				"access-analyzer:GetGeneratedPolicy",
				"access-analyzer:ListAccessPreviewFindings",
				"access-analyzer:ListAccessPreviews",
				"access-analyzer:ListAnalyzedResources",
				"access-analyzer:ListAnalyzers",
				"access-analyzer:ListArchiveRules",
				"access-analyzer:ListFindings",
				"access-analyzer:ListPolicyGenerations",
				"access-analyzer:ListTagsForResource",
				"access-analyzer:ValidatePolicy",
				"account:GetAlternateContact",
				"acm-pca:Describe*",
				"acm-pca:Get*",
				"acm-pca:List*",
				"acm:Describe*",
				"acm:Get*",
				"acm:List*",
				"apigateway:GET",
				"appflow:DescribeConnectorEntity",
				"appflow:DescribeConnectorFields",
				"appflow:DescribeConnectorProfiles",
				"appflow:DescribeConnectors",
				"appflow:DescribeFlowExecution",
				"appflow:DescribeFlows",
				"appflow:ListConnectorEntities",
				"appflow:ListConnectorFields",
				"appflow:ListFlows",
				"appflow:ListTagsForResource",
				"athena:Batch*",
				"athena:Get*",
				"athena:List*",
				"autoscaling:Describe*",
				"autoscaling:GetPredictiveScalingForecast",
				"autoscaling-plans:Describe*",
				"autoscaling-plans:GetScalingPlanResourceForecastData",
				"backup:Describe*",
                "backup:Get*",
                "backup:List*",
				"cloudformation:Describe*",
				"cloudformation:Detect*",
				"cloudformation:Estimate*",
				"cloudformation:Get*",
				"cloudformation:List*",
				"cloudfront:DescribeFunction",
				"cloudfront:Get*",
				"cloudfront:List*",
				"cloudtrail:Describe*",
				"cloudtrail:Get*",
				"cloudtrail:List*",
				"cloudtrail:LookupEvents",
				"cloudwatch:Describe*",
				"cloudwatch:Get*",
				"cloudwatch:List*",
				"comprehend:BatchDetect*",
				"comprehend:Classify*",
				"comprehend:Contains*",
				"comprehend:Describe*",
				"comprehend:Detect*",
				"comprehend:List*",
				"dax:BatchGetItem",
				"dax:Describe*",
				"dax:GetItem",
				"dax:ListTags",
				"dax:Query",
				"dax:Scan",
				"directconnect:Describe*",
				"dms:Describe*",
				"dms:List*",
				"dms:Test*",
				"dynamodb:BatchGet*",
				"dynamodb:Describe*",
				"dynamodb:Get*",
				"dynamodb:List*",
				"dynamodb:Query",
				"dynamodb:Scan",
				"ecs:Describe*",
				"ecs:List*",
				"ec2:Describe*",
				"ec2:Get*",
				"ec2:ListImagesInRecycleBin",
				"ec2:ListSnapshotsInRecycleBin",
				"ec2:SearchLocalGatewayRoutes",
				"ec2:SearchTransitGatewayRoutes",
				"ec2messages:Get*",
				"eks:Describe*",
				"eks:List*",
				"elasticache:Describe*",
				"elasticache:List*",
				"elasticfilesystem:Describe*",
				"elasticloadbalancing:Describe*",
				"elasticmapreduce:Describe*",
                "elasticmapreduce:GetBlockPublicAccessConfiguration",
                "elasticmapreduce:List*",
                "elasticmapreduce:View*",
				"emr-containers:DescribeJobRun",
				"emr-containers:DescribeManagedEndpoint",
				"emr-containers:DescribeVirtualCluster",
				"emr-containers:ListJobRuns",
				"emr-containers:ListManagedEndpoints",
				"emr-containers:ListTagsForResource",
				"emr-containers:ListVirtualClusters",
				"es:Describe*",
				"es:ESHttpGet",
				"es:ESHttpHead",
				"es:Get*",
				"es:List*",
				"firehose:Describe*",
                "firehose:List*",
				"health:Describe*",
				"iam:Generate*",
				"iam:Get*",
				"iam:List*",
				"iam:Simulate*",
				"kinesis:Describe*",
				"kinesis:Get*",
				"kinesis:List*",
				"kinesisanalytics:Describe*",
				"kinesisanalytics:Discover*",
				"kinesisanalytics:Get*",
				"kinesisanalytics:List*",
				"kinesisvideo:Describe*",
				"kinesisvideo:Get*",
				"kinesisvideo:List*",
				"kms:Describe*",
				"kms:Get*",
				"kms:List*",
				"lambda:Get*",
				"lambda:List*",
				"networkmanager:GetCustomerGatewayAssociations",
				"rds:Describe*",
				"rds:Download*",
				"rds:List*",
				"redshift:Describe*",
				"redshift:GetReservedNodeExchangeOfferings",
				"redshift:View*",
				"securityhub:BatchGetStandardsControlAssociations",
                "securityhub:Describe*",
                "securityhub:Get*",
                "securityhub:List*",
				"ssm:Describe*",
                "ssm:Get*",
                "ssm:List*",
				"sns:Check*",
				"sns:Get*",
				"sns:List*",
				"sqs:Get*",
                "sqs:List*",
                "sqs:Receive*",
				"s3-object-lambda:GetObject",
				"s3-object-lambda:GetObjectAcl",
				"s3-object-lambda:GetObjectLegalHold",
				"s3-object-lambda:GetObjectRetention",
				"s3-object-lambda:GetObjectTagging",
				"s3-object-lambda:GetObjectVersion",
				"s3-object-lambda:GetObjectVersionAcl",
				"s3-object-lambda:GetObjectVersionTagging",
				"s3-object-lambda:ListBucket",
				"s3-object-lambda:ListBucketMultipartUploads",
				"s3-object-lambda:ListBucketVersions",
				"s3-object-lambda:ListMultipartUploadParts",
				"s3:DescribeJob",
				"s3:Get*",
				"s3:List*",
				"trustedadvisor:Describe*"
			
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
