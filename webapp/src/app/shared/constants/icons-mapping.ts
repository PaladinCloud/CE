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
        api: 'assets/aws-icons/Application Service/api.svg',
        appelb: 'assets/aws-icons/Compute/appelb.svg',
        asg: 'assets/aws-icons/Compute/asg.svg',
        asgpolicy: 'assets/aws-icons/Compute/asgpolicy.svg',
        classicelb: 'assets/aws-icons/Compute/classicelb.svg',
        cloudfront: 'assets/aws-icons/Networking & Content Delivery/cloudfront.svg',
        customergateway: 'assets/aws-icons/Networking & Content Delivery/customergateway.svg',
        dhcpoption: 'assets/aws-icons/Networking & Content Delivery/dhcpoption.svg',
        directconnect: 'assets/aws-icons/Networking & Content Delivery/directconnect.svg',
        dynamodb: 'assets/aws-icons/Database/dynamodb.svg',
        ec2: 'assets/aws-icons/Compute/ec2.svg',
        efs: 'assets/aws-icons/Storage/efs.svg',
        elasticip: 'assets/aws-icons/Networking & Content Delivery/elasticip.svg',
        elasticsearch: 'assets/aws-icons/Analytics/elasticsearch.svg',
        emr: 'assets/aws-icons/Analytics/emr.svg',
        eni: 'assets/aws-icons/Compute/eni.svg',
        iamrole: 'assets/aws-icons/Identity/iamrole.svg',
        iamuser: 'assets/aws-icons/Identity/iamuser.svg',
        internetgateway: 'assets/aws-icons/Networking & Content Delivery/internetgateway.svg',
        kms: 'assets/aws-icons/Identity/kms.svg',
        lambda: 'assets/aws-icons/Compute/lambda.svg',
        launchconfig: 'assets/aws-icons/Compute/launchconfig.svg',
        nat: 'assets/aws-icons/Compute/nat.svg',
        networkacl: 'assets/aws-icons/Networking & Content Delivery/networkacl.svg',
        peeringconnection: 'assets/aws-icons/Networking & Content Delivery/peeringconnection.svg',
        rdscluster: 'assets/aws-icons/Database/rdscluster.svg',
        rdsdb: 'assets/aws-icons/Database/rdsdb.svg',
        rdssnapshot: 'assets/aws-icons/Database/rdssnapshot.svg',
        redshift: 'assets/aws-icons/Database/redshift.svg',
        routetable: 'assets/aws-icons/Networking & Content Delivery/routetable.svg',
        s3: 'assets/aws-icons/Storage/s3.svg',
        sg: 'assets/aws-icons/Compute/sg.svg',
        snapshot: 'assets/aws-icons/Compute/snapshot.svg',
        stack: 'assets/aws-icons/Management Tools/stack.svg',
        subnet: 'assets/aws-icons/Compute/subnet.svg',
        targetgroup: 'assets/aws-icons/Compute/targetgroup.svg',
        virtualinterface: 'assets/aws-icons/Networking & Content Delivery/virtualinterface.svg',
        volume: 'assets/aws-icons/Storage/volume.svg',
        vpc: 'assets/aws-icons/Compute/vpc.svg',
        vpnconnection: 'assets/aws-icons/Networking & Content Delivery/vpnconnection.svg',
        vpngateway: 'assets/aws-icons/Networking & Content Delivery/vpngateway.svg',
    },
    [CloudProvider.AZURE]: {
        'test': 'test',
    },
    [CloudProvider.GCP]: {
        'test': 'test'
    },
};

export const ICONS = {
    'awsResources': {
        'ec2' : 'assets/aws-icons/Compute/ec2.svg',
        'asgpolicy' : 'assets/aws-icons/Compute/asgpolicy.svg',
        'nat' : 'assets/aws-icons/Compute/nat.svg',
        'appelb' : 'assets/aws-icons/Compute/appelb.svg',
        'asg' : 'assets/aws-icons/Compute/asg.svg',
        'eni' : 'assets/aws-icons/Compute/eni.svg',
        'lambda' : 'assets/aws-icons/Compute/lambda.svg',
        'snapshot' : 'assets/aws-icons/Compute/snapshot.svg',
        'targetgroup' : 'assets/aws-icons/Compute/targetgroup.svg',
        'classicelb' : 'assets/aws-icons/Compute/classicelb.svg',
        'vpc' : 'assets/aws-icons/Compute/vpc.svg',
        'subnet': 'assets/aws-icons/Compute/subnet.svg',
        'sg' : 'assets/aws-icons/Compute/sg.svg',
        'launchconfig' : 'assets/aws-icons/Compute/launchconfig.svg',
        's3' : 'assets/aws-icons/Storage/s3.svg',
        'efs' : 'assets/aws-icons/Storage/efs.svg',
        'volume' : 'assets/aws-icons/Storage/volume.svg',
        'emr' : 'assets/aws-icons/Analytics/emr.svg',
        'elasticsearch' : 'assets/aws-icons/Analytics/elasticsearch.svg',
        'api' : 'assets/aws-icons/Application Service/api.svg',
        'redshift' : 'assets/aws-icons/Database/redshift.svg',
        'rdscluster' : 'assets/aws-icons/Database/rdscluster.svg',
        'rdsdb' : 'assets/aws-icons/Database/rdsdb.svg',
        'rdssnapshot' : 'assets/aws-icons/Database/rdssnapshot.svg',
        'dynamodb' : 'assets/aws-icons/Database/dynamodb.svg',
        'stack' : 'assets/aws-icons/Management Tools/stack.svg',
        'iamuser' : 'assets/aws-icons/Identity/iamuser.svg',
        'iamrole' : 'assets/aws-icons/Identity/iamrole.svg',
        'kms' : 'assets/aws-icons/Identity/kms.svg',
        'routetable' : 'assets/aws-icons/Networking & Content Delivery/routetable.svg',
        'networkacl' : 'assets/aws-icons/Networking & Content Delivery/networkacl.svg',
        'cloudfront' : 'assets/aws-icons/Networking & Content Delivery/cloudfront.svg',
        'internetgateway' : 'assets/aws-icons/Networking & Content Delivery/internetgateway.svg',
        'vpngateway' : 'assets/aws-icons/Networking & Content Delivery/vpngateway.svg',
        'customergateway' : 'assets/aws-icons/Networking & Content Delivery/customergateway.svg',
        'dhcpoption' : 'assets/aws-icons/Networking & Content Delivery/dhcpoption.svg',
        'directconnect' : 'assets/aws-icons/Networking & Content Delivery/directconnect.svg',
        'elasticip' : 'assets/aws-icons/Networking & Content Delivery/elasticip.svg',
        'peeringconnection' : 'assets/aws-icons/Networking & Content Delivery/peeringconnection.svg',
        'virtualinterface' : 'assets/aws-icons/Networking & Content Delivery/virtualinterface.svg',
        'vpnconnection' : 'assets/aws-icons/Networking & Content Delivery/vpnconnection.svg',
        'unknown' : 'assets/aws-icons/Extra/Extra.svg'
    },
    'categories': {
        'Analytics': '#F75C03',
        'Application Service': '#645EC5',
        'Compute' : '#289CF7',
        'Database': '#F2425F',
        'Identity': '#26BA9D',
        'Management Tools' : '#00B946',
        'Networking & Content Delivery' : '#FFB00D',
        'Storage' : '#1C5066',
        'Extra': '#33BFCD'
    },
    'path': '/assets/aws-icons/'
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
