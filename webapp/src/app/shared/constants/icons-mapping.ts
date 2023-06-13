/*
 *Copyright 2018 T Mobile, Inc. or its affiliates. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); You may not use
 * this file except in compliance with the License. A copy of the License is located at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * or in the "license" file accompanying this file. This file is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, express or
 * implied. See the License for the specific language governing permissions and
 * limitations under the License.
 */

enum CloudProvider {
    AWS = 'aws',
    GCP = 'gcp',
    AZURE = 'azure',
}

function isValidCloudProvider(provider: string): provider is CloudProvider {
    return (Object.keys(CloudProvider) as Array<keyof typeof CloudProvider>).some(
        (k) => CloudProvider[k] === provider,
    );
}

const UNKNOWN_SERVICE_ICON = 'assets/icons/question.svg';

const CLOUD_PROVIDER_SERVICE_ICONS: { [key in CloudProvider]: { [service: string]: string } } = {
    [CloudProvider.AWS]: {
        kms: 'assets/icons/cloudproviders/aws/security_identify_compliance/kms.svg',
        s3: 'assets/icons/cloudproviders/aws/storage/s3.svg',
        accessanalyzer:
            'src/assets/icons/cloudproviders/aws/security_identify_compliance/access_analyzer.svg',
        account: 'assets/icons/cloudproviders/aws/management_governance/organizations_acccount.svg',
        acmcertificate:
            'assets/icons/cloudproviders/aws/architecture_services/certificate_manager.svg',
        ami: 'assets/icons/cloudproviders/aws/compute/ec2_ami.svg',
        api: 'assets/icons/cloudproviders/aws/application_integration/api_gateway_endpoint.svg',
        appelb: 'assets/icons/cloudproviders/aws/networking_content_delivery/elb_application_load_balancer.svg',
        appflow: 'assets/icons/cloudproviders/aws/architecture_services/appflow.svg',
        asg: 'assets/icons/cloudproviders/aws/compute/ec2_auto_scaling.svg',
        awsathena: 'assets/icons/cloudproviders/aws/architecture_services/athena.svg',
        awscomprehend: 'assets/icons/cloudproviders/aws/architecture_services/comprehend.svg',
        backupvault: 'assets/icons/cloudproviders/aws/storage/backup_vault.svg',
        classicelb:
            'assets/icons/cloudproviders/aws/networking_content_delivery/elb_classic_load_balancer.svg',
        cloudfront: 'assets/icons/cloudproviders/aws/architecture_services/cloudfront.svg',
        cloudtrail: 'assets/icons/cloudproviders/aws/architecture_services/cloudtrail.svg',
        daxcluster: 'src/assets/icons/cloudproviders/aws/database/dynamodb_accelerator.svg',
        dms: 'assets/icons/cloudproviders/aws/architecture_services/database_migration_service.svg',
        documentdb: 'assets/icons/cloudproviders/aws/architecture_services/documentdb.svg',
        dynamodb: 'assets/icons/cloudproviders/aws/architecture_services/dynamodb.svg',
        ec2: 'assets/icons/cloudproviders/aws/architecture_services/ec2.svg',
        ecr: 'assets/icons/cloudproviders/aws/architecture_services/elastic_container_registry.svg',
        ecscluster: 'assets/icons/cloudproviders/aws/containers/registry.svg',
        ecstaskdefinition: 'assets/icons/cloudproviders/aws/containers/task.svg',
        efs: 'assets/icons/cloudproviders/aws/architecture_services/efs.svg',
        eks: 'assets/icons/cloudproviders/aws/architecture_services/elastic_kubernetes_service.svg',
        elasticache: 'assets/icons/cloudproviders/aws/architecture_services/elasticache.svg',
        elasticip: 'assets/icons/cloudproviders/aws/compute/elastic_ip_address.svg',
        elasticsearch: 'assets/icons/cloudproviders/aws/architecture_services/opensearch.svg',
        emr: 'assets/icons/cloudproviders/aws/architecture_services/emr.svg',
        eni: 'assets/icons/cloudproviders/aws/networking_content_delivery/elastic_network_interface.svg',
        iamcertificate:
            'assets/icons/cloudproviders/aws/security_identify_compliance/certificate_authority.svg',
        iampolicies: 'assets/icons/cloudproviders/aws/security_identify_compliance/permissions.svg',
        iamrole: 'assets/icons/cloudproviders/aws/security_identify_compliance/role.svg',
        iamuser: 'assets/icons/cloudproviders/aws/security_identify_compliance/user.svg',
        lambda: 'assets/icons/cloudproviders/aws/compute/lambda_function.svg',
        launchconfig: 'assets/icons/cloudproviders/aws/architecture_services/config.svg',
        networkacl:
            'assets/icons/cloudproviders/aws/networking_content_delivery/network_access_control_List.svg',
        peeringconnection:
            'assets/icons/cloudproviders/aws/networking_content_delivery/peering_connection.svg',
        rdsdb: 'assets/icons/cloudproviders/aws/architecture_services/rds.svg',
        rdssnapshot: 'assets/icons/cloudproviders/aws/database/rds_instance.svg',
        redshift: 'assets/icons/cloudproviders/aws/architecture_services/redshift.svg',
        sg: 'assets/icons/cloudproviders/aws/security_identify_compliance/iam_addon.svg',
        snapshot: 'assets/icons/cloudproviders/aws/storage/snapshot.svg',
        snstopic: 'assets/icons/cloudproviders/aws/application_integration/sns_topic.svg',
        sqs: 'assets/icons/cloudproviders/aws/architecture_services/sqs.svg',
        stack: 'assets/icons/cloudproviders/aws/management_governance/cloudformation_stack.svg',
        subnet: 'assets/icons/cloudproviders/aws/compute/ec2_instances.svg',
        volume: 'assets/icons/cloudproviders/aws/storage/ebs_volume.svg',
        vpc: 'assets/icons/cloudproviders/aws/architecture_services/vpc.svg',
        vpngateway: 'assets/icons/cloudproviders/aws/networking_content_delivery/vpn_gateway.svg',
    },
    [CloudProvider.AZURE]: {
        test: 'test',
    },
    [CloudProvider.GCP]: {
        test: 'test',
    },
};

export const ICONS = {
    awsResources: {
        ec2: 'assets/aws-icons/Compute/ec2.svg',
        asgpolicy: 'assets/aws-icons/Compute/asgpolicy.svg',
        nat: 'assets/aws-icons/Compute/nat.svg',
        appelb: 'assets/aws-icons/Compute/appelb.svg',
        asg: 'assets/aws-icons/Compute/asg.svg',
        eni: 'assets/aws-icons/Compute/eni.svg',
        lambda: 'assets/aws-icons/Compute/lambda.svg',
        snapshot: 'assets/aws-icons/Compute/snapshot.svg',
        targetgroup: 'assets/aws-icons/Compute/targetgroup.svg',
        classicelb: 'assets/aws-icons/Compute/classicelb.svg',
        vpc: 'assets/aws-icons/Compute/vpc.svg',
        subnet: 'assets/aws-icons/Compute/subnet.svg',
        sg: 'assets/aws-icons/Compute/sg.svg',
        launchconfig: 'assets/aws-icons/Compute/launchconfig.svg',
        s3: 'assets/aws-icons/Storage/s3.svg',
        efs: 'assets/aws-icons/Storage/efs.svg',
        volume: 'assets/aws-icons/Storage/volume.svg',
        emr: 'assets/aws-icons/Analytics/emr.svg',
        elasticsearch: 'assets/aws-icons/Analytics/elasticsearch.svg',
        api: 'assets/aws-icons/Application Service/api.svg',
        redshift: 'assets/aws-icons/Database/redshift.svg',
        rdscluster: 'assets/aws-icons/Database/rdscluster.svg',
        rdsdb: 'assets/aws-icons/Database/rdsdb.svg',
        rdssnapshot: 'assets/aws-icons/Database/rdssnapshot.svg',
        dynamodb: 'assets/aws-icons/Database/dynamodb.svg',
        stack: 'assets/aws-icons/Management Tools/stack.svg',
        iamuser: 'assets/aws-icons/Identity/iamuser.svg',
        iamrole: 'assets/aws-icons/Identity/iamrole.svg',
        kms: 'assets/aws-icons/Identity/kms.svg',
        routetable: 'assets/aws-icons/Networking & Content Delivery/routetable.svg',
        networkacl: 'assets/aws-icons/Networking & Content Delivery/networkacl.svg',
        cloudfront: 'assets/aws-icons/Networking & Content Delivery/cloudfront.svg',
        internetgateway: 'assets/aws-icons/Networking & Content Delivery/internetgateway.svg',
        vpngateway: 'assets/aws-icons/Networking & Content Delivery/vpngateway.svg',
        customergateway: 'assets/aws-icons/Networking & Content Delivery/customergateway.svg',
        dhcpoption: 'assets/aws-icons/Networking & Content Delivery/dhcpoption.svg',
        directconnect: 'assets/aws-icons/Networking & Content Delivery/directconnect.svg',
        elasticip: 'assets/aws-icons/Networking & Content Delivery/elasticip.svg',
        peeringconnection: 'assets/aws-icons/Networking & Content Delivery/peeringconnection.svg',
        virtualinterface: 'assets/aws-icons/Networking & Content Delivery/virtualinterface.svg',
        vpnconnection: 'assets/aws-icons/Networking & Content Delivery/vpnconnection.svg',
        unknown: 'assets/aws-icons/Extra/Extra.svg',
    },
    categories: {
        Analytics: '#F75C03',
        'Application Service': '#645EC5',
        Compute: '#289CF7',
        Database: '#F2425F',
        Identity: '#26BA9D',
        'Management Tools': '#00B946',
        'Networking & Content Delivery': '#FFB00D',
        Storage: '#1C5066',
        Extra: '#33BFCD',
    },
    path: '/assets/aws-icons/',
};

export function getCloudServiceIcon(provider: string, service: string) {
    const providerKey = provider.toLowerCase();
    const serviceKey = service.toLowerCase();
    let icon = UNKNOWN_SERVICE_ICON;
    if (isValidCloudProvider(providerKey)) {
        if (CLOUD_PROVIDER_SERVICE_ICONS[providerKey][serviceKey]) {
            icon = CLOUD_PROVIDER_SERVICE_ICONS[providerKey][serviceKey];
        }
    }
    return icon;
}
