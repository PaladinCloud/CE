from core.terraform.resources.aws.vpc import SecurityGroupResource
from core.config import Settings


class InfraSecurityGroupResource(SecurityGroupResource):
    name = ""
    vpc_id = Settings.get('VPC')['ID']

    ingress = [                           #https and internal alb 
            {
            'from_port': 443,
            'to_port': 443,
            'protocol': "tcp",
            'cidr_blocks': Settings.get('VPC')['CIDR_BLOCKS'],
            'ipv6_cidr_blocks': [],
            'prefix_list_ids': [],
            'description': "",
            'self': False,
            'security_groups': []   
            }
        ,
            {
            'from_port': 80,
            'to_port': 80,
            'protocol': "tcp",
            'cidr_blocks': Settings.get('VPC')['CIDR_BLOCKS'],
            'ipv6_cidr_blocks': [],
            'prefix_list_ids': [],
            'description': "",
            'self': False,
            'security_groups': []   
            }
        ,
            {
            'from_port': 3306,
            'to_port': 3306,
            'protocol': "tcp",
            'cidr_blocks': Settings.get('VPC')['CIDR_BLOCKS'],
            'ipv6_cidr_blocks': [],
            'prefix_list_ids': [],
            'description': "",
            'self': False,
            'security_groups': []   
            }
        ]
    if Settings.get('ALB_PROTOCOL') == "HTTP" and Settings.MAKE_ALB_INTERNAL == True:
         ingress = [
            {
            'from_port': 443,
            'to_port': 443,
            'protocol': "tcp",
            'cidr_blocks': ["0.0.0.0/0"],
            'ipv6_cidr_blocks': [],
            'prefix_list_ids': [],
            'description': "",
            'self': False,
            'security_groups': []   
            }
            ,
            {
            'from_port': 80,
            'to_port': 80,
            'protocol': "tcp",
            'cidr_blocks':  Settings.get('VPC')['CIDR_BLOCKS'],
            'ipv6_cidr_blocks': [],
            'prefix_list_ids': [],
            'description': "",
            'self': False,
            'security_groups': []   
            }
        ,
            {
            'from_port': 3306,
            'to_port': 3306,
            'protocol': "tcp",
            'cidr_blocks': Settings.get('VPC')['CIDR_BLOCKS'],
            'ipv6_cidr_blocks': [],
            'prefix_list_ids': [],
            'description': "",
            'self': False,
            'security_groups': []   
            }
        ]

    if Settings.get('ALB_PROTOCOL') == "HTTPS" and Settings.MAKE_ALB_INTERNAL == False:
        ingress = [
            {
            'from_port': 443,
            'to_port': 443,
            'protocol': "tcp",
            'cidr_blocks': ["0.0.0.0/0"],
            'ipv6_cidr_blocks': [],
            'prefix_list_ids': [],
            'description': "",
            'self': False,
            'security_groups': []   
            }
        ,
            {
            'from_port': 80,
            'to_port': 80,
            'protocol': "tcp",
            'cidr_blocks': Settings.get('VPC')['CIDR_BLOCKS'],
            'ipv6_cidr_blocks': [],
            'prefix_list_ids': [],
            'description': "",
            'self': False,
            'security_groups': []   
            }
        ,
            {
            'from_port': 3306,
            'to_port': 3306,
            'protocol': "tcp",
            'cidr_blocks': Settings.get('VPC')['CIDR_BLOCKS'],
            'ipv6_cidr_blocks': [],
            'prefix_list_ids': [],
            'description': "",
            'self': False,
            'security_groups': []   
            }
        ]


    egress = [
        {
            'from_port': 0,
            'to_port': 0,
            'protocol': "-1",
            'cidr_blocks': ["0.0.0.0/0"],
            'ipv6_cidr_blocks': [],
            'prefix_list_ids': [],
            'description': "",
            'self': False,
            'security_groups': []
        }
    ]
