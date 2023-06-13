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
        accessanalyzer: awsIcon('security_identify_compliance/access_analyzer'),
        account: awsIcon('management_governance/organizations_acccount'),
        acmcertificate: awsIcon('architecture_services/certificate_manager'),
        ami: awsIcon('compute/ec2_ami'),
        api: awsIcon('application_integration/api_gateway_endpoint'),
        appelb: awsIcon('networking_content_delivery/elb_application_load_balancer'),
        appflow: awsIcon('architecture_services/appflow'),
        asg: awsIcon('compute/ec2_auto_scaling'),
        awsathena: awsIcon('architecture_services/athena'),
        awscomprehend: awsIcon('architecture_services/comprehend'),
        backupvault: awsIcon('storage/backup_vault'),
        classicelb: awsIcon('networking_content_delivery/elb_classic_load_balancer'),
        cloudfront: awsIcon('architecture_services/cloudfront'),
        cloudtrail: awsIcon('architecture_services/cloudtrail'),
        daxcluster: awsIcon('database/dynamodb_accelerator'),
        dms: awsIcon('architecture_services/database_migration_service'),
        documentdb: awsIcon('architecture_services/documentdb'),
        dynamodb: awsIcon('architecture_services/dynamodb'),
        ec2: awsIcon('architecture_services/ec2.svg'),
        ecr: awsIcon('architecture_services/elastic_container_registry'),
        ecscluster: awsIcon('containers/registry'),
        ecstaskdefinition: awsIcon('containers/task'),
        efs: awsIcon('architecture_services/efs'),
        eks: awsIcon('architecture_services/elastic_kubernetes_service'),
        elasticache: awsIcon('architecture_services/elasticache'),
        elasticip: awsIcon('compute/elastic_ip_address'),
        elasticsearch: awsIcon('architecture_services/opensearch'),
        emr: awsIcon('architecture_services/emr'),
        eni: awsIcon('networking_content_delivery/elastic_network_interface'),
        iamcertificate: awsIcon('security_identify_compliance/certificate_authority'),
        iampolicies: awsIcon('security_identify_compliance/permissions'),
        iamrole: awsIcon('security_identify_compliance/role'),
        iamuser: awsIcon('security_identify_compliance/user'),
        kms: awsIcon('security_identify_compliance/kms'),
        lambda: awsIcon('compute/lambda_function'),
        launchconfig: awsIcon('architecture_services/config'),
        networkacl: awsIcon('networking_content_delivery/network_access_control_List'),
        peeringconnection: awsIcon('networking_content_delivery/peering_connection'),
        rdsdb: awsIcon('architecture_services/rds'),
        rdssnapshot: awsIcon('database/rds_instance'),
        redshift: awsIcon('architecture_services/redshift'),
        s3: awsIcon('storage/s3'),
        sg: awsIcon('security_identify_compliance/iam_addon'),
        snapshot: awsIcon('storage/snapshot'),
        snstopic: awsIcon('application_integration/sns_topic'),
        sqs: awsIcon('architecture_services/sqs'),
        stack: awsIcon('management_governance/cloudformation_stack'),
        subnet: awsIcon('compute/ec2_instances'),
        volume: awsIcon('storage/ebs_volume'),
        vpc: awsIcon('/architecture_services/vpc'),
        vpngateway: awsIcon('networking_content_delivery/vpn_gateway'),
    },
    [CloudProvider.AZURE]: {
        test: 'test',
    },
    [CloudProvider.GCP]: {
        apikeys: gcpIcon('api.svg'),
        bigquerydataset: gcpIcon('bigquery'),
        bigquerytable: gcpIcon('bigquery'),
        clouddns: gcpIcon('cloud_dns'),
        cloudfunction: gcpIcon('cloud_functions'),
        cloudfunctiongen1: gcpIcon('cloud_functions'),
        cloudsql: gcpIcon('cloud_sql'),
        cloudsql_mysqlserver: gcpIcon('cloud_sql'),
        cloudsql_postgres: gcpIcon('cloud_sql'),
        cloudsql_sqlserver: gcpIcon('cloud_sql'),
        cloudstorage: gcpIcon('cloud_storage'),
        dataproc: gcpIcon('dataproc'),
        gcploadbalancer: gcpIcon('cloud_load_balancing'),
        gkecluster: gcpIcon('gke'),
        iamusers: gcpIcon('identity_and_access_management'),
        kmskey: gcpIcon('key_management_service'),
        networks: gcpIcon('cloud_network'),
        project: gcpIcon('project'),
        pubsub: gcpIcon('pubsub'),
        serviceaccounts: gcpIcon('identity_and_access_management'),
        vminstance: gcpIcon('compute_engine'),
        vpcfirewall: gcpIcon('cloud_firewall_rules'),
    },
};

function awsIcon(path: string) {
    return `assets/icons/cloudproviders/aws/${path}.svg`;
}

function gcpIcon(path: string) {
    return `assets/icons/cloudproviders/gcp/${path}.svg`;
}

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
