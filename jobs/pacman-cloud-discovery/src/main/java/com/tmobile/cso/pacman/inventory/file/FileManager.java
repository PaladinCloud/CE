/*******************************************************************************
 * Copyright 2018 T Mobile, Inc. or its affiliates. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License.  You may obtain a copy
 * of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  See the
 * License for the specific language governing permissions and limitations under
 * the License.
 ******************************************************************************/
package com.tmobile.cso.pacman.inventory.file;

import com.amazonaws.services.apigateway.model.RestApi;
import com.amazonaws.services.athena.model.QueryExecution;
import com.amazonaws.services.autoscaling.model.LaunchConfiguration;
import com.amazonaws.services.autoscaling.model.ScalingPolicy;
import com.amazonaws.services.cloudformation.model.Stack;
import com.amazonaws.services.cloudwatch.model.MetricAlarm;
import com.amazonaws.services.comprehend.model.EntitiesDetectionJobProperties;
import com.amazonaws.services.databasemigrationservice.model.ReplicationInstance;
import com.amazonaws.services.directconnect.model.Connection;
import com.amazonaws.services.directconnect.model.VirtualInterface;
import com.amazonaws.services.ec2.model.*;
import com.amazonaws.services.elasticloadbalancingv2.model.LoadBalancer;
import com.amazonaws.services.elasticmapreduce.model.Cluster;
import com.amazonaws.services.identitymanagement.model.Policy;
import com.amazonaws.services.identitymanagement.model.Role;
import com.amazonaws.services.rds.model.DBSnapshot;
import com.amazonaws.services.securityhub.model.DescribeHubResult;
import com.amazonaws.services.simplesystemsmanagement.model.InstanceInformation;
import com.amazonaws.services.sns.model.Topic;
import com.tmobile.cso.pacman.inventory.util.InventoryConstants;
import com.tmobile.cso.pacman.inventory.vo.*;
import com.tmobile.pacman.commons.dto.ErrorVH;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * The Class FileManager.
 */
public class FileManager {

    /**
     * Instantiates a new file manager.
     */
    private FileManager() {

    }

    /**
     * Initialise.
     *
     * @param folderName the folder name
     */
    public static void initialise(String folderName) throws IOException {
        FileGenerator.folderName = folderName;
        new File(folderName).mkdirs();

        FileGenerator.writeToFile("aws-ec2.data", InventoryConstants.OPEN_ARRAY, false);
        FileGenerator.writeToFile("aws-ec2-tags.data", InventoryConstants.OPEN_ARRAY, false);
        FileGenerator.writeToFile("aws-ec2-secgroups.data", InventoryConstants.OPEN_ARRAY, false);
        FileGenerator.writeToFile("aws-ec2-productcodes.data", InventoryConstants.OPEN_ARRAY, false);
        FileGenerator.writeToFile("aws-ec2-blockdevices.data", InventoryConstants.OPEN_ARRAY, false);
        FileGenerator.writeToFile("aws-ec2-nwinterfaces.data", InventoryConstants.OPEN_ARRAY, false);
        FileGenerator.writeToFile("aws-eni.data", InventoryConstants.OPEN_ARRAY, false);
        FileGenerator.writeToFile("aws-eni-secgroups.data", InventoryConstants.OPEN_ARRAY, false);
        FileGenerator.writeToFile("aws-eni-ipv6.data", InventoryConstants.OPEN_ARRAY, false);
        FileGenerator.writeToFile("aws-eni-privateipaddr.data", InventoryConstants.OPEN_ARRAY, false);
        FileGenerator.writeToFile("aws-asg.data", InventoryConstants.OPEN_ARRAY, false);
        FileGenerator.writeToFile("aws-asg-instances.data", InventoryConstants.OPEN_ARRAY, false);
        FileGenerator.writeToFile("aws-asg-elb.data", InventoryConstants.OPEN_ARRAY, false);
        FileGenerator.writeToFile("aws-asg-tags.data", InventoryConstants.OPEN_ARRAY, false);
        FileGenerator.writeToFile("aws-asg-launchconfig.data", InventoryConstants.OPEN_ARRAY, false);
        FileGenerator.writeToFile("aws-stack.data", InventoryConstants.OPEN_ARRAY, false);
        FileGenerator.writeToFile("aws-stack-tags.data", InventoryConstants.OPEN_ARRAY, false);
        FileGenerator.writeToFile("aws-dynamodb.data", InventoryConstants.OPEN_ARRAY, false);
        FileGenerator.writeToFile("aws-dynamodb-tags.data", InventoryConstants.OPEN_ARRAY, false);
        FileGenerator.writeToFile("aws-efs.data", InventoryConstants.OPEN_ARRAY, false);
        FileGenerator.writeToFile("aws-efs-tags.data", InventoryConstants.OPEN_ARRAY, false);
        FileGenerator.writeToFile("aws-emr.data", InventoryConstants.OPEN_ARRAY, false);
        FileGenerator.writeToFile("aws-emr-tags.data", InventoryConstants.OPEN_ARRAY, false);
        FileGenerator.writeToFile("aws-lambda.data", InventoryConstants.OPEN_ARRAY, false);
        FileGenerator.writeToFile("aws-lambda-tags.data", InventoryConstants.OPEN_ARRAY, false);
        FileGenerator.writeToFile("aws-lambda-secgroups.data", InventoryConstants.OPEN_ARRAY, false);
        FileGenerator.writeToFile("aws-classicelb.data", InventoryConstants.OPEN_ARRAY, false);
        FileGenerator.writeToFile("aws-classicelb-instances.data", InventoryConstants.OPEN_ARRAY, false);
        FileGenerator.writeToFile("aws-classicelb-tags.data", InventoryConstants.OPEN_ARRAY, false);
        FileGenerator.writeToFile("aws-classicelb-secgroups.data", InventoryConstants.OPEN_ARRAY, false);
        FileGenerator.writeToFile("aws-appelb.data", InventoryConstants.OPEN_ARRAY, false);
        FileGenerator.writeToFile("aws-appelb-tags.data", InventoryConstants.OPEN_ARRAY, false);
        FileGenerator.writeToFile("aws-appelb-instances.data", InventoryConstants.OPEN_ARRAY, false);
        FileGenerator.writeToFile("aws-appelb-secgroups.data", InventoryConstants.OPEN_ARRAY, false);
        FileGenerator.writeToFile("aws-targetgroup.data", InventoryConstants.OPEN_ARRAY, false);
        FileGenerator.writeToFile("aws-targetgroup-instances.data", InventoryConstants.OPEN_ARRAY, false);
        FileGenerator.writeToFile("aws-nat.data", InventoryConstants.OPEN_ARRAY, false);
        FileGenerator.writeToFile("aws-nat-addresses.data", InventoryConstants.OPEN_ARRAY, false);
        FileGenerator.writeToFile("aws-rdscluster.data", InventoryConstants.OPEN_ARRAY, false);
        FileGenerator.writeToFile("aws-rdscluster-secgroups.data", InventoryConstants.OPEN_ARRAY, false);
        FileGenerator.writeToFile("aws-rdscluster-tags.data", InventoryConstants.OPEN_ARRAY, false);
        FileGenerator.writeToFile("aws-rdsdb.data", InventoryConstants.OPEN_ARRAY, false);
        FileGenerator.writeToFile("aws-rdsdb-secgroups.data", InventoryConstants.OPEN_ARRAY, false);
        FileGenerator.writeToFile("aws-rdsdb-tags.data", InventoryConstants.OPEN_ARRAY, false);
        FileGenerator.writeToFile("aws-s3.data", InventoryConstants.OPEN_ARRAY, false);
        FileGenerator.writeToFile("aws-s3-tags.data", InventoryConstants.OPEN_ARRAY, false);
        FileGenerator.writeToFile("aws-sg.data", InventoryConstants.OPEN_ARRAY, false);
        FileGenerator.writeToFile("aws-sg-tags.data", InventoryConstants.OPEN_ARRAY, false);
        FileGenerator.writeToFile("aws-sg-rules.data", InventoryConstants.OPEN_ARRAY, false);
        FileGenerator.writeToFile("aws-subnet.data", InventoryConstants.OPEN_ARRAY, false);
        FileGenerator.writeToFile("aws-subnet-tags.data", InventoryConstants.OPEN_ARRAY, false);
        FileGenerator.writeToFile("aws-checks.data", InventoryConstants.OPEN_ARRAY, false);
        FileGenerator.writeToFile("aws-checks-resources.data", InventoryConstants.OPEN_ARRAY, false);
        FileGenerator.writeToFile("aws-redshift.data", InventoryConstants.OPEN_ARRAY, false);
        FileGenerator.writeToFile("aws-redshift-secgroups.data", InventoryConstants.OPEN_ARRAY, false);
        FileGenerator.writeToFile("aws-redshift-tags.data", InventoryConstants.OPEN_ARRAY, false);
        FileGenerator.writeToFile("aws-volume.data", InventoryConstants.OPEN_ARRAY, false);
        FileGenerator.writeToFile("aws-volume-attachments.data", InventoryConstants.OPEN_ARRAY, false);
        FileGenerator.writeToFile("aws-volume-tags.data", InventoryConstants.OPEN_ARRAY, false);
        FileGenerator.writeToFile("aws-snapshot.data", InventoryConstants.OPEN_ARRAY, false);
        FileGenerator.writeToFile("aws-snapshot-tags.data", InventoryConstants.OPEN_ARRAY, false);
        FileGenerator.writeToFile("aws-vpc.data", InventoryConstants.OPEN_ARRAY, false);
        FileGenerator.writeToFile("aws-vpc-tags.data", InventoryConstants.OPEN_ARRAY, false);
        FileGenerator.writeToFile("aws-vpc-endpoints.data", InventoryConstants.OPEN_ARRAY, false);
        FileGenerator.writeToFile("aws-api.data", InventoryConstants.OPEN_ARRAY, false);
        FileGenerator.writeToFile("aws-iamuser.data", InventoryConstants.OPEN_ARRAY, false);
        FileGenerator.writeToFile("aws-iamuser-keys.data", InventoryConstants.OPEN_ARRAY, false);
        FileGenerator.writeToFile("aws-rdssnapshot.data", InventoryConstants.OPEN_ARRAY, false);
        FileGenerator.writeToFile("aws-iamrole.data", InventoryConstants.OPEN_ARRAY, false);
        FileGenerator.writeToFile("aws-kms.data", InventoryConstants.OPEN_ARRAY, false);
        FileGenerator.writeToFile("aws-kms-tags.data", InventoryConstants.OPEN_ARRAY, false);
        FileGenerator.writeToFile("aws-cloudfront.data", InventoryConstants.OPEN_ARRAY, false);
        FileGenerator.writeToFile("aws-cloudfront-tags.data", InventoryConstants.OPEN_ARRAY, false);
        FileGenerator.writeToFile("aws-beanstalk.data", InventoryConstants.OPEN_ARRAY, false);
        FileGenerator.writeToFile("aws-beanstalk-instance.data", InventoryConstants.OPEN_ARRAY, false);
        FileGenerator.writeToFile("aws-beanstalk-asg.data", InventoryConstants.OPEN_ARRAY, false);
        FileGenerator.writeToFile("aws-beanstalk-elb.data", InventoryConstants.OPEN_ARRAY, false);
        FileGenerator.writeToFile("aws-phd.data", InventoryConstants.OPEN_ARRAY, false);
        FileGenerator.writeToFile("aws-phd-entities.data", InventoryConstants.OPEN_ARRAY, false);
        FileGenerator.writeToFile("aws-routetable.data", InventoryConstants.OPEN_ARRAY, false);
        FileGenerator.writeToFile("aws-routetable-routes.data", InventoryConstants.OPEN_ARRAY, false);
        FileGenerator.writeToFile("aws-routetable-associations.data", InventoryConstants.OPEN_ARRAY, false);
        FileGenerator.writeToFile("aws-routetable-propagatingvgws.data", InventoryConstants.OPEN_ARRAY, false);
        FileGenerator.writeToFile("aws-routetable-tags.data", InventoryConstants.OPEN_ARRAY, false);
        FileGenerator.writeToFile("aws-networkacl.data", InventoryConstants.OPEN_ARRAY, false);
        FileGenerator.writeToFile("aws-networkacl-entries.data", InventoryConstants.OPEN_ARRAY, false);
        FileGenerator.writeToFile("aws-networkacl-associations.data", InventoryConstants.OPEN_ARRAY, false);
        FileGenerator.writeToFile("aws-networkacl-tags.data", InventoryConstants.OPEN_ARRAY, false);
        FileGenerator.writeToFile("aws-elasticip.data", InventoryConstants.OPEN_ARRAY, false);
        FileGenerator.writeToFile("aws-launchconfig.data", InventoryConstants.OPEN_ARRAY, false);
        FileGenerator.writeToFile("aws-launchconfig-blockdevicemappings.data", InventoryConstants.OPEN_ARRAY, false);
        FileGenerator.writeToFile("aws-internetgateway-attachments.data", InventoryConstants.OPEN_ARRAY, false);
        FileGenerator.writeToFile("aws-internetgateway.data", InventoryConstants.OPEN_ARRAY, false);
        FileGenerator.writeToFile("aws-internetgateway-tags.data", InventoryConstants.OPEN_ARRAY, false);
        FileGenerator.writeToFile("aws-vpngateway.data", InventoryConstants.OPEN_ARRAY, false);
        FileGenerator.writeToFile("aws-vpngateway-vpcattachments.data", InventoryConstants.OPEN_ARRAY, false);
        FileGenerator.writeToFile("aws-vpngateway-tags.data", InventoryConstants.OPEN_ARRAY, false);
        FileGenerator.writeToFile("aws-asgpolicy.data", InventoryConstants.OPEN_ARRAY, false);
        FileGenerator.writeToFile("aws-asgpolicy-stepadjustments.data", InventoryConstants.OPEN_ARRAY, false);
        FileGenerator.writeToFile("aws-asgpolicy-alarms.data", InventoryConstants.OPEN_ARRAY, false);
        FileGenerator.writeToFile("aws-snstopic.data", InventoryConstants.OPEN_ARRAY, false);
        FileGenerator.writeToFile("aws-egressgateway.data", InventoryConstants.OPEN_ARRAY, false);
        FileGenerator.writeToFile("aws-dhcpoption.data", InventoryConstants.OPEN_ARRAY, false);
        FileGenerator.writeToFile("aws-dhcpoption-tags.data", InventoryConstants.OPEN_ARRAY, false);
        FileGenerator.writeToFile("aws-peeringconnection.data", InventoryConstants.OPEN_ARRAY, false);
        FileGenerator.writeToFile("aws-peeringconnection-tags.data", InventoryConstants.OPEN_ARRAY, false);
        FileGenerator.writeToFile("aws-customergateway.data", InventoryConstants.OPEN_ARRAY, false);
        FileGenerator.writeToFile("aws-customergateway-tags.data", InventoryConstants.OPEN_ARRAY, false);
        FileGenerator.writeToFile("aws-vpnconnection.data", InventoryConstants.OPEN_ARRAY, false);
        FileGenerator.writeToFile("aws-vpnconnection-routes.data", InventoryConstants.OPEN_ARRAY, false);
        FileGenerator.writeToFile("aws-vpnconnection-telemetry.data", InventoryConstants.OPEN_ARRAY, false);
        FileGenerator.writeToFile("aws-vpnconnection-tags.data", InventoryConstants.OPEN_ARRAY, false);
        FileGenerator.writeToFile("aws-directconnect.data", InventoryConstants.OPEN_ARRAY, false);
        FileGenerator.writeToFile("aws-virtualinterface.data", InventoryConstants.OPEN_ARRAY, false);
        FileGenerator.writeToFile("aws-elasticsearch.data", InventoryConstants.OPEN_ARRAY, false);
        FileGenerator.writeToFile("aws-elasticsearch-tags.data", InventoryConstants.OPEN_ARRAY, false);
        FileGenerator.writeToFile("aws-reservedinstance.data", InventoryConstants.OPEN_ARRAY, false);
        FileGenerator.writeToFile("aws-reservedinstance-tags.data", InventoryConstants.OPEN_ARRAY, false);
        FileGenerator.writeToFile("aws-ec2-ssminfo.data", InventoryConstants.OPEN_ARRAY, false);
        FileGenerator.writeToFile("aws-elasticache.data", InventoryConstants.OPEN_ARRAY, false);
        FileGenerator.writeToFile("aws-elasticache-tags.data", InventoryConstants.OPEN_ARRAY, false);
        FileGenerator.writeToFile("aws-datastream.data", InventoryConstants.OPEN_ARRAY, false);
        FileGenerator.writeToFile("aws-datastream-tags.data", InventoryConstants.OPEN_ARRAY, false);
        FileGenerator.writeToFile("aws-sqs.data", InventoryConstants.OPEN_ARRAY, false);
        FileGenerator.writeToFile("aws-sqs-tags.data", InventoryConstants.OPEN_ARRAY, false);
        FileGenerator.writeToFile("aws-deliverystream.data", InventoryConstants.OPEN_ARRAY, false);
        FileGenerator.writeToFile("aws-deliverystream-tags.data", InventoryConstants.OPEN_ARRAY, false);
        FileGenerator.writeToFile("aws-videostream.data", InventoryConstants.OPEN_ARRAY, false);
        FileGenerator.writeToFile("aws-videostream-tags.data", InventoryConstants.OPEN_ARRAY, false);
        FileGenerator.writeToFile("aws-elasticache-nodes.data", InventoryConstants.OPEN_ARRAY, false);
        FileGenerator.writeToFile("aws-acmcertificate.data", InventoryConstants.OPEN_ARRAY, false);
        FileGenerator.writeToFile("aws-iamcertificate.data", InventoryConstants.OPEN_ARRAY, false);
        FileGenerator.writeToFile("aws-account.data", InventoryConstants.OPEN_ARRAY, false);
        FileGenerator.writeToFile("aws-iamgroup.data", InventoryConstants.OPEN_ARRAY, false);
        FileGenerator.writeToFile("aws-cloudtrail.data", InventoryConstants.OPEN_ARRAY, false);
        FileGenerator.writeToFile("aws-cloudtrail-eventselector.data", InventoryConstants.OPEN_ARRAY, false);
        FileGenerator.writeToFile("aws-classicelb-listeners.data", InventoryConstants.OPEN_ARRAY, false);
        FileGenerator.writeToFile("aws-appelb-listeners.data", InventoryConstants.OPEN_ARRAY, false);
        FileGenerator.writeToFile("aws-appelb-rules.data", InventoryConstants.OPEN_ARRAY, false);
        FileGenerator.writeToFile("aws-documentdb.data", InventoryConstants.OPEN_ARRAY, false);
        FileGenerator.writeToFile("aws-dms.data", InventoryConstants.OPEN_ARRAY, false);
        FileGenerator.writeToFile("aws-eks.data", InventoryConstants.OPEN_ARRAY, false);
        FileGenerator.writeToFile("aws-eks-tags.data", InventoryConstants.OPEN_ARRAY, false);
        FileGenerator.writeToFile("aws-daxcluster.data", InventoryConstants.OPEN_ARRAY, false);
        FileGenerator.writeToFile("aws-awsathena.data", InventoryConstants.OPEN_ARRAY, false);
        FileGenerator.writeToFile("aws-awscomprehend.data", InventoryConstants.OPEN_ARRAY, false);
        FileGenerator.writeToFile("aws-appflow.data", InventoryConstants.OPEN_ARRAY, false);
        FileGenerator.writeToFile("aws-appflow-tags.data", InventoryConstants.OPEN_ARRAY, false);
        FileGenerator.writeToFile("aws-ecstaskdefinition.data", InventoryConstants.OPEN_ARRAY, false);
        FileGenerator.writeToFile("aws-ecstaskdefinition-tags.data", InventoryConstants.OPEN_ARRAY, false);
        FileGenerator.writeToFile("aws-ecscluster.data", InventoryConstants.OPEN_ARRAY, false);
        FileGenerator.writeToFile("aws-ecscluster-tags.data", InventoryConstants.OPEN_ARRAY, false);
        FileGenerator.writeToFile("aws-securityhub.data", InventoryConstants.OPEN_ARRAY, false);
        FileGenerator.writeToFile("aws-accessanalyzer.data", InventoryConstants.OPEN_ARRAY, false);
        FileGenerator.writeToFile("aws-accessanalyzer-findings.data", InventoryConstants.OPEN_ARRAY, false);
        FileGenerator.writeToFile("aws-accessanalyzer-tags.data", InventoryConstants.OPEN_ARRAY, false);
        FileGenerator.writeToFile("aws-ami.data", InventoryConstants.OPEN_ARRAY, false);
        FileGenerator.writeToFile("aws-ami-blockdevicemap.data", InventoryConstants.OPEN_ARRAY, false);
        FileGenerator.writeToFile("aws-ami-tags.data", InventoryConstants.OPEN_ARRAY, false);
        FileGenerator.writeToFile("aws-backupvault.data", InventoryConstants.OPEN_ARRAY, false);
        FileGenerator.writeToFile("aws-iampolicies.data", InventoryConstants.OPEN_ARRAY, false);
        FileGenerator.writeToFile("aws-cloudwatchlogs.data", InventoryConstants.OPEN_ARRAY, false);
        FileGenerator.writeToFile("aws-cloudwatchlogs-metric.data", InventoryConstants.OPEN_ARRAY, false);
        FileGenerator.writeToFile("aws-cloudwatchalarm.data", InventoryConstants.OPEN_ARRAY, false);
        FileGenerator.writeToFile("aws-ecr.data", InventoryConstants.OPEN_ARRAY, false);
        FileGenerator.writeToFile("aws-vpc-cidrblock-association.data", InventoryConstants.OPEN_ARRAY, false);
        FileGenerator.writeToFile("aws-launchtemplate.data", InventoryConstants.OPEN_ARRAY, false);
    }

    public static void finalise() throws IOException {

        FileGenerator.writeToFile("aws-ec2.data", InventoryConstants.CLOSE_ARRAY, true);
        FileGenerator.writeToFile("aws-ec2-tags.data", InventoryConstants.CLOSE_ARRAY, true);
        FileGenerator.writeToFile("aws-ec2-secgroups.data", InventoryConstants.CLOSE_ARRAY, true);
        FileGenerator.writeToFile("aws-ec2-productcodes.data", InventoryConstants.CLOSE_ARRAY, true);
        FileGenerator.writeToFile("aws-ec2-blockdevices.data", InventoryConstants.CLOSE_ARRAY, true);
        FileGenerator.writeToFile("aws-ec2-nwinterfaces.data", InventoryConstants.CLOSE_ARRAY, true);
        FileGenerator.writeToFile("aws-eni.data", InventoryConstants.CLOSE_ARRAY, true);
        FileGenerator.writeToFile("aws-eni-secgroups.data", InventoryConstants.CLOSE_ARRAY, true);
        FileGenerator.writeToFile("aws-eni-ipv6.data", InventoryConstants.CLOSE_ARRAY, true);
        FileGenerator.writeToFile("aws-eni-privateipaddr.data", InventoryConstants.CLOSE_ARRAY, true);
        FileGenerator.writeToFile("aws-asg.data", InventoryConstants.CLOSE_ARRAY, true);
        FileGenerator.writeToFile("aws-asg-instances.data", InventoryConstants.CLOSE_ARRAY, true);
        FileGenerator.writeToFile("aws-asg-elb.data", InventoryConstants.CLOSE_ARRAY, true);
        FileGenerator.writeToFile("aws-asg-tags.data", InventoryConstants.CLOSE_ARRAY, true);
        FileGenerator.writeToFile("aws-asg-launchconfig.data", InventoryConstants.CLOSE_ARRAY, true);
        FileGenerator.writeToFile("aws-stack.data", InventoryConstants.CLOSE_ARRAY, true);
        FileGenerator.writeToFile("aws-stack-tags.data", InventoryConstants.CLOSE_ARRAY, true);
        FileGenerator.writeToFile("aws-dynamodb.data", InventoryConstants.CLOSE_ARRAY, true);
        FileGenerator.writeToFile("aws-dynamodb-tags.data", InventoryConstants.CLOSE_ARRAY, true);
        FileGenerator.writeToFile("aws-efs.data", InventoryConstants.CLOSE_ARRAY, true);
        FileGenerator.writeToFile("aws-efs-tags.data", InventoryConstants.CLOSE_ARRAY, true);
        FileGenerator.writeToFile("aws-emr.data", InventoryConstants.CLOSE_ARRAY, true);
        FileGenerator.writeToFile("aws-emr-tags.data", InventoryConstants.CLOSE_ARRAY, true);
        FileGenerator.writeToFile("aws-lambda.data", InventoryConstants.CLOSE_ARRAY, true);
        FileGenerator.writeToFile("aws-lambda-tags.data", InventoryConstants.CLOSE_ARRAY, true);
        FileGenerator.writeToFile("aws-lambda-secgroups.data", InventoryConstants.CLOSE_ARRAY, true);
        FileGenerator.writeToFile("aws-classicelb.data", InventoryConstants.CLOSE_ARRAY, true);
        FileGenerator.writeToFile("aws-classicelb-instances.data", InventoryConstants.CLOSE_ARRAY, true);
        FileGenerator.writeToFile("aws-classicelb-tags.data", InventoryConstants.CLOSE_ARRAY, true);
        FileGenerator.writeToFile("aws-classicelb-secgroups.data", InventoryConstants.CLOSE_ARRAY, true);
        FileGenerator.writeToFile("aws-appelb.data", InventoryConstants.CLOSE_ARRAY, true);
        FileGenerator.writeToFile("aws-appelb-tags.data", InventoryConstants.CLOSE_ARRAY, true);
        FileGenerator.writeToFile("aws-appelb-instances.data", InventoryConstants.CLOSE_ARRAY, true);
        FileGenerator.writeToFile("aws-appelb-secgroups.data", InventoryConstants.CLOSE_ARRAY, true);
        FileGenerator.writeToFile("aws-targetgroup.data", InventoryConstants.CLOSE_ARRAY, true);
        FileGenerator.writeToFile("aws-targetgroup-instances.data", InventoryConstants.CLOSE_ARRAY, true);
        FileGenerator.writeToFile("aws-nat.data", InventoryConstants.CLOSE_ARRAY, true);
        FileGenerator.writeToFile("aws-nat-addresses.data", InventoryConstants.CLOSE_ARRAY, true);
        FileGenerator.writeToFile("aws-rdscluster.data", InventoryConstants.CLOSE_ARRAY, true);
        FileGenerator.writeToFile("aws-rdscluster-secgroups.data", InventoryConstants.CLOSE_ARRAY, true);
        FileGenerator.writeToFile("aws-rdscluster-tags.data", InventoryConstants.CLOSE_ARRAY, true);
        FileGenerator.writeToFile("aws-rdsdb.data", InventoryConstants.CLOSE_ARRAY, true);
        FileGenerator.writeToFile("aws-rdsdb-secgroups.data", InventoryConstants.CLOSE_ARRAY, true);
        FileGenerator.writeToFile("aws-rdsdb-tags.data", InventoryConstants.CLOSE_ARRAY, true);
        FileGenerator.writeToFile("aws-s3.data", InventoryConstants.CLOSE_ARRAY, true);
        FileGenerator.writeToFile("aws-s3-tags.data", InventoryConstants.CLOSE_ARRAY, true);
        FileGenerator.writeToFile("aws-sg.data", InventoryConstants.CLOSE_ARRAY, true);
        FileGenerator.writeToFile("aws-sg-tags.data", InventoryConstants.CLOSE_ARRAY, true);
        FileGenerator.writeToFile("aws-sg-rules.data", InventoryConstants.CLOSE_ARRAY, true);
        FileGenerator.writeToFile("aws-subnet.data", InventoryConstants.CLOSE_ARRAY, true);
        FileGenerator.writeToFile("aws-subnet-tags.data", InventoryConstants.CLOSE_ARRAY, true);
        FileGenerator.writeToFile("aws-checks.data", InventoryConstants.CLOSE_ARRAY, true);
        FileGenerator.writeToFile("aws-checks-resources.data", InventoryConstants.CLOSE_ARRAY, true);
        FileGenerator.writeToFile("aws-redshift.data", InventoryConstants.CLOSE_ARRAY, true);
        FileGenerator.writeToFile("aws-redshift-secgroups.data", InventoryConstants.CLOSE_ARRAY, true);
        FileGenerator.writeToFile("aws-redshift-tags.data", InventoryConstants.CLOSE_ARRAY, true);
        FileGenerator.writeToFile("aws-volume.data", InventoryConstants.CLOSE_ARRAY, true);
        FileGenerator.writeToFile("aws-volume-attachments.data", InventoryConstants.CLOSE_ARRAY, true);
        FileGenerator.writeToFile("aws-volume-tags.data", InventoryConstants.CLOSE_ARRAY, true);
        FileGenerator.writeToFile("aws-snapshot.data", InventoryConstants.CLOSE_ARRAY, true);
        FileGenerator.writeToFile("aws-snapshot-tags.data", InventoryConstants.CLOSE_ARRAY, true);
        FileGenerator.writeToFile("aws-vpc.data", InventoryConstants.CLOSE_ARRAY, true);
        FileGenerator.writeToFile("aws-vpc-tags.data", InventoryConstants.CLOSE_ARRAY, true);
        FileGenerator.writeToFile("aws-vpc-endpoints.data", InventoryConstants.CLOSE_ARRAY, true);
        FileGenerator.writeToFile("aws-api.data", InventoryConstants.CLOSE_ARRAY, true);
        FileGenerator.writeToFile("aws-iamuser.data", InventoryConstants.CLOSE_ARRAY, true);
        FileGenerator.writeToFile("aws-iamuser-keys.data", InventoryConstants.CLOSE_ARRAY, true);
        FileGenerator.writeToFile("aws-rdssnapshot.data", InventoryConstants.CLOSE_ARRAY, true);
        FileGenerator.writeToFile("aws-iamrole.data", InventoryConstants.CLOSE_ARRAY, true);
        FileGenerator.writeToFile("aws-kms.data", InventoryConstants.CLOSE_ARRAY, true);
        FileGenerator.writeToFile("aws-kms-tags.data", InventoryConstants.CLOSE_ARRAY, true);
        FileGenerator.writeToFile("aws-cloudfront.data", InventoryConstants.CLOSE_ARRAY, true);
        FileGenerator.writeToFile("aws-cloudfront-tags.data", InventoryConstants.CLOSE_ARRAY, true);
        FileGenerator.writeToFile("aws-beanstalk.data", InventoryConstants.CLOSE_ARRAY, true);
        FileGenerator.writeToFile("aws-beanstalk-instance.data", InventoryConstants.CLOSE_ARRAY, true);
        FileGenerator.writeToFile("aws-beanstalk-asg.data", InventoryConstants.CLOSE_ARRAY, true);
        FileGenerator.writeToFile("aws-beanstalk-elb.data", InventoryConstants.CLOSE_ARRAY, true);
        FileGenerator.writeToFile("aws-phd.data", InventoryConstants.CLOSE_ARRAY, true);
        FileGenerator.writeToFile("aws-phd-entities.data", InventoryConstants.CLOSE_ARRAY, true);
        FileGenerator.writeToFile("aws-routetable.data", InventoryConstants.CLOSE_ARRAY, true);
        FileGenerator.writeToFile("aws-routetable-routes.data", InventoryConstants.CLOSE_ARRAY, true);
        FileGenerator.writeToFile("aws-routetable-associations.data", InventoryConstants.CLOSE_ARRAY, true);
        FileGenerator.writeToFile("aws-routetable-propagatingvgws.data", InventoryConstants.CLOSE_ARRAY, true);
        FileGenerator.writeToFile("aws-routetable-tags.data", InventoryConstants.CLOSE_ARRAY, true);
        FileGenerator.writeToFile("aws-networkacl.data", InventoryConstants.CLOSE_ARRAY, true);
        FileGenerator.writeToFile("aws-networkacl-entries.data", InventoryConstants.CLOSE_ARRAY, true);
        FileGenerator.writeToFile("aws-networkacl-associations.data", InventoryConstants.CLOSE_ARRAY, true);
        FileGenerator.writeToFile("aws-networkacl-tags.data", InventoryConstants.CLOSE_ARRAY, true);
        FileGenerator.writeToFile("aws-elasticip.data", InventoryConstants.CLOSE_ARRAY, true);
        FileGenerator.writeToFile("aws-launchconfig.data", InventoryConstants.CLOSE_ARRAY, true);
        FileGenerator.writeToFile("aws-launchconfig-blockdevicemappings.data", InventoryConstants.CLOSE_ARRAY, true);
        FileGenerator.writeToFile("aws-internetgateway-attachments.data", InventoryConstants.CLOSE_ARRAY, true);
        FileGenerator.writeToFile("aws-internetgateway.data", InventoryConstants.CLOSE_ARRAY, true);
        FileGenerator.writeToFile("aws-internetgateway-tags.data", InventoryConstants.CLOSE_ARRAY, true);
        FileGenerator.writeToFile("aws-vpngateway.data", InventoryConstants.CLOSE_ARRAY, true);
        FileGenerator.writeToFile("aws-vpngateway-vpcattachments.data", InventoryConstants.CLOSE_ARRAY, true);
        FileGenerator.writeToFile("aws-vpngateway-tags.data", InventoryConstants.CLOSE_ARRAY, true);
        FileGenerator.writeToFile("aws-asgpolicy.data", InventoryConstants.CLOSE_ARRAY, true);
        FileGenerator.writeToFile("aws-asgpolicy-stepadjustments.data", InventoryConstants.CLOSE_ARRAY, true);
        FileGenerator.writeToFile("aws-asgpolicy-alarms.data", InventoryConstants.CLOSE_ARRAY, true);
        FileGenerator.writeToFile("aws-snstopic.data", InventoryConstants.CLOSE_ARRAY, true);
        FileGenerator.writeToFile("aws-egressgateway.data", InventoryConstants.CLOSE_ARRAY, true);
        FileGenerator.writeToFile("aws-dhcpoption.data", InventoryConstants.CLOSE_ARRAY, true);
        FileGenerator.writeToFile("aws-dhcpoption-tags.data", InventoryConstants.CLOSE_ARRAY, true);
        FileGenerator.writeToFile("aws-peeringconnection.data", InventoryConstants.CLOSE_ARRAY, true);
        FileGenerator.writeToFile("aws-peeringconnection-tags.data", InventoryConstants.CLOSE_ARRAY, true);
        FileGenerator.writeToFile("aws-customergateway.data", InventoryConstants.CLOSE_ARRAY, true);
        FileGenerator.writeToFile("aws-customergateway-tags.data", InventoryConstants.CLOSE_ARRAY, true);
        FileGenerator.writeToFile("aws-vpnconnection.data", InventoryConstants.CLOSE_ARRAY, true);
        FileGenerator.writeToFile("aws-vpnconnection-routes.data", InventoryConstants.CLOSE_ARRAY, true);
        FileGenerator.writeToFile("aws-vpnconnection-telemetry.data", InventoryConstants.CLOSE_ARRAY, true);
        FileGenerator.writeToFile("aws-vpnconnection-tags.data", InventoryConstants.CLOSE_ARRAY, true);
        FileGenerator.writeToFile("aws-directconnect.data", InventoryConstants.CLOSE_ARRAY, true);
        FileGenerator.writeToFile("aws-virtualinterface.data", InventoryConstants.CLOSE_ARRAY, true);
        FileGenerator.writeToFile("aws-elasticsearch.data", InventoryConstants.CLOSE_ARRAY, true);
        FileGenerator.writeToFile("aws-elasticsearch-tags.data", InventoryConstants.CLOSE_ARRAY, true);
        FileGenerator.writeToFile("aws-reservedinstance.data", InventoryConstants.CLOSE_ARRAY, true);
        FileGenerator.writeToFile("aws-reservedinstance-tags.data", InventoryConstants.CLOSE_ARRAY, true);
        FileGenerator.writeToFile("aws-ec2-ssminfo.data", InventoryConstants.CLOSE_ARRAY, true);
        FileGenerator.writeToFile("aws-elasticache.data", InventoryConstants.CLOSE_ARRAY, true);
        FileGenerator.writeToFile("aws-elasticache-tags.data", InventoryConstants.CLOSE_ARRAY, true);
        FileGenerator.writeToFile("aws-datastream.data", InventoryConstants.CLOSE_ARRAY, true);
        FileGenerator.writeToFile("aws-datastream-tags.data", InventoryConstants.CLOSE_ARRAY, true);
        FileGenerator.writeToFile("aws-sqs.data", InventoryConstants.CLOSE_ARRAY, true);
        FileGenerator.writeToFile("aws-sqs-tags.data", InventoryConstants.CLOSE_ARRAY, true);
        FileGenerator.writeToFile("aws-deliverystream.data", InventoryConstants.CLOSE_ARRAY, true);
        FileGenerator.writeToFile("aws-deliverystream-tags.data", InventoryConstants.CLOSE_ARRAY, true);
        FileGenerator.writeToFile("aws-videostream.data", InventoryConstants.CLOSE_ARRAY, true);
        FileGenerator.writeToFile("aws-videostream-tags.data", InventoryConstants.CLOSE_ARRAY, true);
        FileGenerator.writeToFile("aws-elasticache-nodes.data", InventoryConstants.CLOSE_ARRAY, true);
        FileGenerator.writeToFile("aws-acmcertificate.data", InventoryConstants.CLOSE_ARRAY, true);
        FileGenerator.writeToFile("aws-iamcertificate.data", InventoryConstants.CLOSE_ARRAY, true);
        FileGenerator.writeToFile("aws-account.data", InventoryConstants.CLOSE_ARRAY, true);
        FileGenerator.writeToFile("aws-iamgroup.data", InventoryConstants.CLOSE_ARRAY, true);
        FileGenerator.writeToFile("aws-cloudtrail.data", InventoryConstants.CLOSE_ARRAY, true);
        FileGenerator.writeToFile("aws-cloudtrail-eventselector.data", InventoryConstants.CLOSE_ARRAY, true);
        FileGenerator.writeToFile("aws-classicelb-listeners.data", InventoryConstants.CLOSE_ARRAY, true);
        FileGenerator.writeToFile("aws-appelb-listeners.data", InventoryConstants.CLOSE_ARRAY, true);
        FileGenerator.writeToFile("aws-appelb-rules.data", InventoryConstants.CLOSE_ARRAY, true);
        FileGenerator.writeToFile("aws-documentdb.data", InventoryConstants.CLOSE_ARRAY, true);
        FileGenerator.writeToFile("aws-dms.data", InventoryConstants.CLOSE_ARRAY, true);
        FileGenerator.writeToFile("aws-eks.data", InventoryConstants.CLOSE_ARRAY, true);
        FileGenerator.writeToFile("aws-eks-tags.data", InventoryConstants.CLOSE_ARRAY, true);
        FileGenerator.writeToFile("aws-daxcluster.data", InventoryConstants.CLOSE_ARRAY, true);
        FileGenerator.writeToFile("aws-awsathena.data", InventoryConstants.CLOSE_ARRAY, true);
        FileGenerator.writeToFile("aws-awscomprehend.data", InventoryConstants.CLOSE_ARRAY, true);
        FileGenerator.writeToFile("aws-appflow.data", InventoryConstants.CLOSE_ARRAY, true);
        FileGenerator.writeToFile("aws-appflow-tags.data", InventoryConstants.CLOSE_ARRAY, true);
        FileGenerator.writeToFile("aws-ecstaskdefinition.data", InventoryConstants.CLOSE_ARRAY, true);
        FileGenerator.writeToFile("aws-ecstaskdefinition-tags.data", InventoryConstants.CLOSE_ARRAY, true);
        FileGenerator.writeToFile("aws-ecscluster.data", InventoryConstants.CLOSE_ARRAY, true);
        FileGenerator.writeToFile("aws-ecscluster-tags.data", InventoryConstants.CLOSE_ARRAY, true);
        FileGenerator.writeToFile("aws-securityhub.data", InventoryConstants.CLOSE_ARRAY, true);
        FileGenerator.writeToFile("aws-accessanalyzer.data", InventoryConstants.CLOSE_ARRAY, true);
        FileGenerator.writeToFile("aws-accessanalyzer-findings.data", InventoryConstants.CLOSE_ARRAY, true);
        FileGenerator.writeToFile("aws-accessanalyzer-tags.data", InventoryConstants.CLOSE_ARRAY, true);
        FileGenerator.writeToFile("aws-ami.data", InventoryConstants.CLOSE_ARRAY, true);
        FileGenerator.writeToFile("aws-ami-blockdevicemap.data", InventoryConstants.CLOSE_ARRAY, true);
        FileGenerator.writeToFile("aws-ami-tags.data", InventoryConstants.CLOSE_ARRAY, true);
        FileGenerator.writeToFile("aws-backupvault.data", InventoryConstants.CLOSE_ARRAY, true);
        FileGenerator.writeToFile("aws-iampolicies.data", InventoryConstants.CLOSE_ARRAY, true);
        FileGenerator.writeToFile("aws-cloudwatchlogs.data", InventoryConstants.CLOSE_ARRAY, true);
        FileGenerator.writeToFile("aws-cloudwatchlogs-metric.data", InventoryConstants.CLOSE_ARRAY, true);
        FileGenerator.writeToFile("aws-cloudwatchalarm.data", InventoryConstants.CLOSE_ARRAY, true);
        FileGenerator.writeToFile("aws-ecr.data", InventoryConstants.CLOSE_ARRAY, true);
        FileGenerator.writeToFile("aws-vpc-cidrblock-association.data", InventoryConstants.CLOSE_ARRAY, true);
        FileGenerator.writeToFile("aws-launchtemplate.data", InventoryConstants.CLOSE_ARRAY, true);
    }

    /**
     * Generate instance files.
     *
     * @param fileInfoMap the instance map
     */
    public static void generateInstanceFiles(Map<String, List<Instance>> fileInfoMap) {
        String fieldNames = "";
        String keys = "";

        fieldNames = "instanceId`amiLaunchIndex`architecture`clientToken`ebsOptimized`EnaSupport`Hypervisor`ImageId`InstanceLifecycle`InstanceType`KernelId`KeyName`LaunchTime`Platform`PrivateDnsName`"
                + "PrivateIpAddress`PublicDnsName`PublicIpAddress`RamdiskId`RootDeviceName`RootDeviceType`SourceDestCheck`SpotInstanceRequestId`SriovNetSupport`StateTransitionReason`SubnetId`VirtualizationType`"
                + "VpcId`IamInstanceProfile.Arn`IamInstanceProfile.Id`Monitoring.State`Placement.Affinity`Placement.AvailabilityZone`Placement.GroupName`Placement.HostId`Placement.Tenancy`State.Name`State.Code`StateReason.Message`StateReason.Code";
        keys = "discoverydate`accountid`accountname`region`instanceid`amilaunchindex`architecture`clienttoken`ebsoptimized`enasupport`hypervisor"
                + "`imageid`instancelifecycle`instancetype`kernelid`keyname`launchtime`platform`privatednsname`privateipaddress`"
                + "publicdnsname`publicipaddress`ramdiskid`rootdevicename`rootdevicetype`sourcedestcheck`spotinstancerequestid`"
                + "sriovnetsupport`statetransitionreason`subnetid`virtualizationtype`vpcid`iaminstanceprofilearn`iaminstanceprofileid"
                + "`monitoringstate`affinity`availabilityzone`groupname`hostid`tenancy`statename`statecode`statereasonmessage`statereasoncode";
        FileGenerator.generateJson(fileInfoMap, fieldNames, "aws-ec2.data", keys);

        fieldNames = "instanceId`tags.key`tags.value";
        keys = "discoverydate`accountid`accountname`region`instanceid`key`value";
        FileGenerator.generateJson(fileInfoMap, fieldNames, "aws-ec2-tags.data", keys);

        fieldNames = "instanceId`SecurityGroups.groupId`SecurityGroups.groupName";
        keys = "discoverydate`accountid`accountname`region`instanceid`securitygroupid`securitygroupname";
        FileGenerator.generateJson(fileInfoMap, fieldNames, "aws-ec2-secgroups.data", keys);

        fieldNames = "instanceId`ProductCodes.ProductCodeId`ProductCodes.ProductCodeType";
        keys = "discoverydate`accountid`accountname`region`instanceid`productcodeid`productcodetype";
        FileGenerator.generateJson(fileInfoMap, fieldNames, "aws-ec2-productcodes.data", keys);

        fieldNames = "instanceId`BlockDeviceMappings.deviceName`BlockDeviceMappings.ebs.VolumeId`BlockDeviceMappings.ebs.AttachTime`BlockDeviceMappings.ebs.DeleteOnTermination`BlockDeviceMappings.ebs.status";
        keys = "discoverydate`accountid`accountname`region`instanceid`devicename`volumeid`attachtime`delontermination`status";
        FileGenerator.generateJson(fileInfoMap, fieldNames, "aws-ec2-blockdevices.data", keys);

        fieldNames = "instanceId`NetworkInterfaces.NetworkInterfaceId`NetworkInterfaces.Description";
        keys = "discoverydate`accountid`accountname`region`instanceid`networkinterfaceid`networkinterfacedescription";
        FileGenerator.generateJson(fileInfoMap, fieldNames, "aws-ec2-nwinterfaces.data", keys);
    }

    /**
     * Generate nw interface files.
     *
     * @param fileInfoMap the fileInfoMap
     */
    public static void generateNwInterfaceFiles(Map<String, List<NetworkInterface>> fileInfoMap) {
        String fieldNames = "";
        String keys = "";

        fieldNames = "NetworkInterfaceId`Description`MacAddress`OwnerId`PrivateDnsName`PrivateIpAddress`SourceDestCheck`Status`SubnetId`VpcId`association.IpOwnerId`association.PublicDnsName`association.PublicIp`attachment.AttachmentId`attachment.AttachTime`attachment.DeleteOnTermination`attachment.DeviceIndex`attachment.status";
        keys = "discoverydate`accountid`accountname`region`networkinterfaceid`description`macaddress`ownerid`"
                + "privatednsname`privateipaddress`sourcedestcheck`status`subnetid`vpcid`associationipownerid`associationpubdnsname`associationpubip`attachmentid`attachmentattachtime`attachmentdelontermination`attachmentdeviceindex`attachmentstatus";
        FileGenerator.generateJson(fileInfoMap, fieldNames, "aws-eni.data", keys);

        fieldNames = "NetworkInterfaceId`groups.GroupId`groups.GroupName";
        keys = "discoverydate`accountid`accountname`region`networkinterfaceid`groupid`groupname";
        FileGenerator.generateJson(fileInfoMap, fieldNames, "aws-eni-secgroups.data", keys);

        fieldNames = "NetworkInterfaceId`Ipv6Addresses.Ipv6Address";
        keys = "discoverydate`accountid`accountname`region`networkinterfaceid`ipv6address";
        FileGenerator.generateJson(fileInfoMap, fieldNames, "aws-eni-ipv6.data", keys);

        fieldNames = "NetworkInterfaceId`PrivateIpAddresses.Primary`PrivateIpAddresses.PrivateDnsName`PrivateIpAddresses.PrivateIpAddress`PrivateIpAddresses.association.IpOwnerId`PrivateIpAddresses.association.PublicDnsName`PrivateIpAddresses.association.PublicIp";
        keys = "discoverydate`accountid`accountname`region`networkinterfaceid`privateipaddrprimary`privatednsname`privateipaddress`associpownerid`assocpubdnsname`assocpublicip";
        FileGenerator.generateJson(fileInfoMap, fieldNames, "aws-eni-privateipaddr.data", keys);
    }

    /**
     * Generate asg files.
     *
     * @param fileInfoMap the fileInfoMap
     */
    public static void generateAsgFiles(Map<String, List<ASGVH>> fileInfoMap) {
        String fieldNames;
        String keys;

        fieldNames = "asg.AutoScalingGroupARN`asg.AutoScalingGroupName`asg.AvailabilityZones`asg.CreatedTime`asg.DefaultCooldown`asg.DesiredCapacity`asg.HealthCheckGracePeriod`asg.HealthCheckType`asg.LaunchConfigurationName`asg.MaxSize`asg.MinSize`"
                + "asg.NewInstancesProtectedFromScaleIn`asg.PlacementGroup`asg.Status`asg.SuspendedProcesses`asg.TargetGroupARNs`asg.TerminationPolicies`asg.VPCZoneIdentifier";
        keys = "discoverydate`accountid`accountname`region`autoscalinggrouparn`autoscalinggroupname`availabilityzones`createdtime`defaultcooldown`desiredcapacity`healthcheckgraceperiod`healthchecktype`"
                + "launchconfigurationname`maxsize`minsize`newinstancesprotectedfromscalein`placementgroup`status`suspendedprocesses`targetgrouparns`terminationpolicies`vpczoneidentifier";
        FileGenerator.generateJson(fileInfoMap, fieldNames, "aws-asg.data", keys);

        fieldNames = "asg.AutoScalingGroupARN`asg.instances.instanceid";
        keys = "discoverydate`accountid`accountname`region`autoscalinggrouparn`instancesinstanceid";
        FileGenerator.generateJson(fileInfoMap, fieldNames, "aws-asg-instances.data", keys);

        fieldNames = "asg.AutoScalingGroupARN`asg.LoadBalancerNames";
        keys = "discoverydate`accountid`accountname`region`autoscalinggrouparn`loadbalancernames";
        FileGenerator.generateJson(fileInfoMap, fieldNames, "aws-asg-elb.data", keys);

        fieldNames = "asg.AutoScalingGroupARN`asg.tags.key`asg.tags.value";
        keys = "discoverydate`accountid`accountname`region`autoscalinggrouparn`key`value";
        FileGenerator.generateJson(fileInfoMap, fieldNames, "aws-asg-tags.data", keys);

        fieldNames = "asg.AutoScalingGroupARN`lauchConfigList.lauchConfig.launchConfigurationName`lauchConfigList.lauchConfig.launchConfigurationARN`lauchConfigList.lauchConfig.imageId`lauchConfigList.lauchConfig.keyName`lauchConfigList.lauchConfig.instanceMonitoring.enabled`lauchConfigList.lauchConfig.iamInstanceProfile`lauchConfigList.lauchConfig.ebsOptimized`lauchConfigList.securityGroups";
        keys = "discoverydate`accountid`accountname`region`autoscalinggrouparn`lcname`lcarn`imageid`keyname`monitorenable`iamprofile`ebsoptimized`securitygroups";
        FileGenerator.generateJson(fileInfoMap, fieldNames, "aws-asg-launchconfig.data", keys);
    }

    /**
     * Generate cloud formation stack files.
     *
     * @param fileInfoMap the fileInfoMap
     */
    public static void generateCloudFormationStackFiles(Map<String, List<Stack>> fileInfoMap) {
        String fieldNames;
        String keys;
        fieldNames = "StackId`StackName`ChangeSetId`CreationTime`Description`DisableRollback`LastUpdatedTime`RoleARN`StackStatus`StackStatusReason`TimeoutInMinutes";
        keys = "discoverydate`accountid`accountname`region`stackid`stackname`changesetid`creationtime`description`disablerollback`lastupdatedtime`rolearn`status`statusreason`timeoutinminutes";
        FileGenerator.generateJson(fileInfoMap, fieldNames, "aws-stack.data", keys);
        fieldNames = "StackId`tags.key`tags.value";
        keys = "discoverydate`accountid`accountname`region`stackid`key`value";
        FileGenerator.generateJson(fileInfoMap, fieldNames, "aws-stack-tags.data", keys);
    }

    /**
     * Generate dynamo db files.
     *
     * @param fileInfoMap the dynamo fileInfoMap
     */
    public static void generateDynamoDbFiles(Map<String, List<DynamoVH>> fileInfoMap) {
        String fieldNames;
        String keys;

        fieldNames = "table.TableArn`table.TableName`table.CreationDateTime`table.ItemCount`table.LatestStreamArn`table.LatestStreamLabel`table.TableSizeBytes`table.TableStatus`table.ProvisionedThroughput.ReadCapacityUnits`table.ProvisionedThroughput.WriteCapacityUnits`table.StreamSpecification.StreamEnabled`table.StreamSpecification.StreamViewType`table.sSEDescription.SSEType";
        keys = "discoverydate`accountid`accountname`region`tablearn`tablename`creationdatetime`itemcount`lateststreamarn`lateststreamlabel`tablesizebytes`tablestatus`readcapacityunits`writecapacityunits`streamenabled`streamviewtype`ssetype";
        FileGenerator.generateJson(fileInfoMap, fieldNames, "aws-dynamodb.data", keys);
        fieldNames = "table.TableArn`tags.key`tags.value";
        keys = "discoverydate`accountid`accountname`region`tablearn`key`value";
        FileGenerator.generateJson(fileInfoMap, fieldNames, "aws-dynamodb-tags.data", keys);
    }

    /**
     * Generate efs files.
     *
     * @param fileInfoMap the efsf fileInfoMap
     */
    public static void generateEfsFiles(Map<String, List<EfsVH>> fileInfoMap) {
        String fieldNames;
        String keys;
        fieldNames = "efs.FileSystemId`efs.Name`efs.CreationTime`efs.CreationToken`efs.LifeCycleState`efs.NumberOfMountTargets`efs.OwnerId`efs.PerformanceMode`efs.encrypted`efs.kmsKeyId";
        keys = "discoverydate`accountid`accountname`region`filesystemid`name`creationtime`creationtoken`lifecyclestate`noofmounttargets`ownerid`performancemode`encrypted`kmskeyid";
        FileGenerator.generateJson(fileInfoMap, fieldNames, "aws-efs.data", keys);
        fieldNames = "efs.FileSystemId`tags.key`tags.value";
        keys = "discoverydate`accountid`accountname`region`filesystemid`key`value";
        FileGenerator.generateJson(fileInfoMap, fieldNames, "aws-efs-tags.data", keys);
    }

    /**
     * Generate emr files.
     *
     * @param fileInfoMap the file inof fileInfoMap
     */
    public static void generateEmrFiles(Map<String, List<Cluster>> fileInfoMap) {
        String fieldNames;
        String keys;
        fieldNames = "Id`AutoScalingRole`AutoTerminate`InstanceCollectionType`LogUri`MasterPublicDnsName`Name`NormalizedInstanceHours`ReleaseLabel`RequestedAmiVersion`RunningAmiVersion`ScaleDownBehavior`SecurityConfiguration`ServiceRole`TerminationProtected`VisibleToAllUsers";
        keys = "discoverydate`accountid`accountname`region`id`autoscalingrole`autoterminate`instancecollectiontype`loguri`masterpubdnsname`name`norminstancehours`releaselabel`reqamiversion`runningamiversion`scaledownbehavior`securityconfig`servicerole`terminationprotected`visibletoallusers";
        FileGenerator.generateJson(fileInfoMap, fieldNames, "aws-emr.data", keys);
        fieldNames = "Id`tags.key`tags.value";
        keys = "discoverydate`accountid`accountname`region`id`key`value";
        FileGenerator.generateJson(fileInfoMap, fieldNames, "aws-emr-tags.data", keys);
    }

    /**
     * Generate lamda files.
     *
     * @param fileInfoMap the file inof map
     */
    public static void generateLamdaFiles(Map<String, List<LambdaVH>> fileInfoMap) {
        String fieldNames;
        String keys;
        fieldNames = "lambda.FunctionArn`lambda.CodeSha256`lambda.CodeSize`lambda.Description`lambda.FunctionName`lambda.Handler`lambda.KMSKeyArn`lambda.LastModified`lambda.MemorySize`lambda.Role`lambda.Runtime`lambda.Timeout`lambda.Version`lambda.VpcConfig.VpcId`lambda.VpcConfig.SubnetIds`lambda.VpcConfig.SecurityGroupIds";
        keys = "discoverydate`accountid`accountname`region`functionarn`codesha256`codesize`description`functionname`handler`kmskeyarn`lastmodified`memorysize`role`runtime`timeout`version`vpcconfigid`vpcconfigsubnetids`vpcconfigsecuritygroupids";
        FileGenerator.generateJson(fileInfoMap, fieldNames, "aws-lambda.data", keys);
        fieldNames = "lambda.FunctionArn`tags.key`tags.value";
        keys = "discoverydate`accountid`accountname`region`functionarn`key`value";
        FileGenerator.generateJson(fileInfoMap, fieldNames, "aws-lambda-tags.data", keys);
        fieldNames = "lambda.FunctionArn`lambda.vpcConfig.securityGroupIds";
        keys = "discoverydate`accountid`accountname`region`functionarn`securitygroupid";
        FileGenerator.generateJson(fileInfoMap, fieldNames, "aws-lambda-secgroups.data", keys);
    }

    /**
     * Generate eks files.
     *
     * @param fileInfoMap the file info map
     */
    public static void generateEKSFiles(Map<String, List<EKSVH>> fileInfoMap) {
        String fieldNames;
        String keys;
        fieldNames = "cluster.name`cluster.arn`cluster.status`cluster.version`cluster.resourcesVpcConfig.endpointPublicAccess" +
                "`cluster.resourcesVpcConfig.endpointPrivateAccess`cluster.resourcesVpcConfig.publicAccessCidrs" +
                "`cluster.logging.clusterLogging.enabled`cluster.resourcesVpcConfig.clusterSecurityGroupId" +
                "`cluster.encryptionConfig.provider.keyArn";
        keys = "discoverydate`accountid`accountname`region`clustername`clusterarn`status`version" +
                "`endpointpublicaccess`endpointprivateaccess`publicaccesscidrs`clusterloggingenabled" +
                "`clustersecuritygroupid`keyarn";

        // keyarn moved to last because the value is sometimes null and is causing corrupted data
        FileGenerator.generateJson(fileInfoMap, fieldNames, "aws-eks.data", keys);
        fieldNames = "cluster.arn`tags.key`tags.value";
        keys = "discoverydate`accountid`accountname`region`clusterarn`key`value";
        FileGenerator.generateJson(fileInfoMap, fieldNames, "aws-eks-tags.data", keys);
    }

    /**
     * Generate DAX Cluster files.
     *
     * @param fileInfoMap the file info map
     */
    public static void generateDAXClusterFiles(Map<String, List<com.amazonaws.services.dax.model.Cluster>> fileInfoMap) {
        String fieldNames;
        String keys;
        fieldNames = "clusterName`clusterArn`status`sSEDescription.status`clusterEndpointEncryptionType";
        keys = "discoverydate`accountid`accountname`region`clustername`clusterarn`status`ssestatus`endpointencrytype";
        FileGenerator.generateJson(fileInfoMap, fieldNames, "aws-daxcluster.data", keys);
    }

    /**
     * Generate AWS Athena files.
     *
     * @param fileInfoMap the file info map
     */
    public static void generateAWSAthenaFiles(Map<String, List<QueryExecution>> fileInfoMap) {
        String fieldNames;
        String keys;
        fieldNames = "queryExecutionId`query`resultConfiguration.encryptionConfiguration.encryptionOption`resultConfiguration.encryptionConfiguration.kmsKey`resultConfiguration.encryptionConfiguration.outputLocation`status.state";
        keys = "discoverydate`accountid`accountname`region`queryid`query`encryptionoption`kmskey`outputlocation`status";
        FileGenerator.generateJson(fileInfoMap, fieldNames, "aws-awsathena.data", keys);
    }

    /**
     * Generate AWS comprehend files.
     *
     * @param fileInfoMap the file info map
     */
    public static void generateAWSComprehendFiles(Map<String, List<EntitiesDetectionJobProperties>> fileInfoMap) {
        String fieldNames;
        String keys;
        fieldNames = "jobId`jobArn`jobName`jobStatus`outputDataConfig.s3Uri`outputDataConfig.kmsKeyId";
        keys = "discoverydate`accountid`accountname`region`jobid`jobarn`jobname`jobstatus`s3url`kmskeyid";
        FileGenerator.generateJson(fileInfoMap, fieldNames, "aws-awscomprehend.data", keys);


    }

    /**
     * Generates aws security hub files.
     *
     * @param fileInfoMap the file info map
     */
    public static void generateSecurityHubFiles(Map<String, List<DescribeHubResult>> fileInfoMap) {
        String fieldNames;
        String keys;
        fieldNames = "hubArn`subscribedAt`autoEnableControls";
        keys = "discoverydate`accountid`accountname`region`hubarn`subcribedat`autoenablecontrols";
        FileGenerator.generateJson(fileInfoMap, fieldNames, "aws-securityhub.data", keys);
    }

    /**
     * Generate AWS AppFlow files.
     *
     * @param fileInfoMap the file info map
     */
    public static void generateAWSAppFlowFiles(Map<String, List<AppFlowVH>> fileInfoMap) {
        String fieldNames;
        String keys;
        fieldNames = "flowDef.flowArn`flowDef.description`flowDef.flowName`flowDef.flowStatus`flowDef.sourceConnectorType`flowDef.destinationConnectorType`kmsArn";
        keys = "discoverydate`accountid`accountname`region`flowarn`description`flowname`flowstatus`sourceconntype`destconntype`kmsarn";
        FileGenerator.generateJson(fileInfoMap, fieldNames, "aws-appflow.data", keys);
        fieldNames = "flowDef.flowArn`tags.key`tags.value";
        keys = "discoverydate`accountid`accountname`region`flowarn`key`value";
        FileGenerator.generateJson(fileInfoMap, fieldNames, "aws-appflow-tags.data", keys);
    }

    /**
     * Generate AWS ECS TaskDefinition files.
     *
     * @param fileInfoMap the file info map
     */
    public static void generateAWSECSFiles(Map<String, List<ECSTaskDefinitionVH>> fileInfoMap) {
        String fieldNames;
        String keys;
        fieldNames = "taskDef.taskDefinitionArn`taskDef.family`taskDef.taskRoleArn`taskDef.executionRoleArn`taskDef.revision`taskDef.status`taskDef.cpu`taskDef.memory`taskDef.registeredAt`taskDef.registeredBy`taskDef.containerDefinitions.logConfiguration.logDriver";
        keys = "discoverydate`accountid`accountname`region`taskdefarn`family`taskrolearn`executionrolearn`revision`status`cpu`memory`registeredat`registeredby`logdriver";
        FileGenerator.generateJson(fileInfoMap, fieldNames, "aws-ecstaskdefinition.data", keys);
        fieldNames = "taskDef.taskDefinitionArn`tags.key`tags.value";
        keys = "discoverydate`accountid`accountname`region`taskdefarn`key`value";
        FileGenerator.generateJson(fileInfoMap, fieldNames, "aws-ecstaskdefinition-tags.data", keys);
    }

    /**
     * Generate AWS ECS Cluster files.
     *
     * @param fileInfoMap the file info map
     */
    public static void generateAWSECSClusterFiles(Map<String, List<ECSClusterVH>> fileInfoMap) {
        String fieldNames;
        String keys;
        fieldNames = "cluster.clusterArn`cluster.clusterName`cluster.status`cluster.registeredContainerInstancesCount`cluster.runningTasksCount`cluster.pendingTasksCount`cluster.activeServicesCount";
        keys = "discoverydate`accountid`accountname`region`clusterarn`clustername`status`reginstancecount`runningtaskcount`pendingtaskcount`activeservicescount";
        FileGenerator.generateJson(fileInfoMap, fieldNames, "aws-ecscluster.data", keys);
        fieldNames = "cluster.clusterArn`tags.key`tags.value";
        keys = "discoverydate`accountid`accountname`region`clusterarn`key`value";
        FileGenerator.generateJson(fileInfoMap, fieldNames, "aws-ecscluster-tags.data", keys);
    }

    /**
     * Generate AWS Access Analyzer files.
     *
     * @param fileInfoMap the file info map
     */
    public static void generateAccessAnalyzerFiles(Map<String, List<AccessAnalyzerVH>> fileInfoMap) {
        String fieldNames;
        String keys;
        fieldNames = "analyzer.arn`analyzer.lastResourceAnalyzed`analyzer.lastResourceAnalyzedAt`analyzer.name`analyzer.status`analyzer.type";
        keys = "discoverydate`accountid`accountname`region`analyzerarn`lastresanalyzed`lastresanalyzedat`name`status`type";
        FileGenerator.generateJson(fileInfoMap, fieldNames, "aws-accessanalyzer.data", keys);
        // analyzer findings data
        fieldNames = "analyzer.arn`finding.analyzedAt`finding.id`finding.isPublic`finding.resource`finding.resourceOwnerAccount`finding.status";
        keys = "discoverydate`accountid`accountname`region`analyzerarn`analyzedat`id`ispublic`resource`resourceowneraccount`status";
        FileGenerator.generateJson(fileInfoMap, fieldNames, "aws-accessanalyzer-findings.data", keys);
        // analyzer tags data
        fieldNames = "analyzer.arn`tags.key`tags.value";
        keys = "discoverydate`accountid`accountname`region`analyzerarn`key`value";
        FileGenerator.generateJson(fileInfoMap, fieldNames, "aws-accessanalyzer-tags.data", keys);
    }

    /**
     * Generate AWS AMI files.
     *
     * @param fileInfoMap the file info map
     */
    public static void generateAMIFiles(Map<String, List<AMIVH>> fileInfoMap) {
        String fieldNames;
        String keys;
        fieldNames = "image.architecture`image.creationDate`image.imageId`image.imageLocation`image.imageType`image.publicValue`image.platform`image.platformDetails`image.state`image.name`image.rootDeviceType`image.rootDeviceName";
        keys = "discoverydate`accountid`accountname`region`architecture`creationdate`imageid`imagelocation`imagetype`publicvalue`platform`platformdetails`state`name`rootdevicetype`rootdevicename";
        FileGenerator.generateJson(fileInfoMap, fieldNames, "aws-ami.data", keys);
        // ami blockDeviceMapping findings data
        fieldNames = "image.imageId`blockDeviceMapping.deviceName`blockDeviceMapping.virtualName`blockDeviceMapping.ebs.snapshotId`blockDeviceMapping.ebs.volumeSize`blockDeviceMapping.ebs.volumeType`blockDeviceMapping.ebs.encrypted`blockDeviceMapping.ebs.deleteOnTermination";
        keys = "discoverydate`accountid`accountname`region`imageid`devicename`virtualname`snapshotid`volumesize`volumetype`encrypted`deleteontermination";
        FileGenerator.generateJson(fileInfoMap, fieldNames, "aws-ami-blockdevicemap.data", keys);
        // ami tags data
        fieldNames = "image.imageId`tags.key`tags.value";
        keys = "discoverydate`accountid`accountname`region`imageid`key`value";
        FileGenerator.generateJson(fileInfoMap, fieldNames, "aws-ami-tags.data", keys);
    }

    /**
     * Generate AWS Backup vault files.
     *
     * @param fileInfoMap the file info map
     */
    public static void generateBackupvalut(Map<String, List<BackupVaultVH>> fileInfoMap) {
        String fieldNames;
        String keys;
        fieldNames = "backupvault.backupVaultName`backupvault.backupVaultArn`backupvault.encryptionKeyArn`backupvault.locked`backupvault.numberOfRecoveryPoints`backupvault.creationDate`backupvault.creatorRequestId`accessPolicy";
        keys = "discoverydate`accountid`accountname`region`backupVaultName`backupVaultArn`encryptionKeyArn`locked`numberOfRecoveryPoints`creationDate`creatorRequestId`accessPolicy";
        FileGenerator.generateJson(fileInfoMap, fieldNames, "aws-backupvault.data", keys);
    }

    /**
     * Generate classic elb files.
     *
     * @param fileInfoMap the elb map
     */
    public static void generateClassicElbFiles(Map<String, List<ClassicELBVH>> fileInfoMap) {
        String fieldNames;
        String keys;
        fieldNames = "elb.DNSName`elb.AvailabilityZones`elb.CanonicalHostedZoneName`elb.CanonicalHostedZoneNameID`elb.CreatedTime`elb.LoadBalancerName`elb.Scheme`elb.VPCId`elb.subnets`accessLogBucketName`accessLogBucketPrefix`accessLog";
        keys = "discoverydate`accountid`accountname`region`dnsname`availabilityzones`canonicalhostedzonename`canonicalhostedzonenameid`createdtime`loadbalancername`scheme`vpcid`subnets`accesslogbucketname`accesslogbucketprefix`accesslog";
        FileGenerator.generateJson(fileInfoMap, fieldNames, "aws-classicelb.data", keys);
        fieldNames = "elb.LoadBalancerName`elb.Instances.InstanceId";
        keys = "discoverydate`accountid`accountname`region`loadbalancername`instanceid";
        FileGenerator.generateJson(fileInfoMap, fieldNames, "aws-classicelb-instances.data", keys);
        fieldNames = "elb.LoadBalancerName`tags.key`tags.value";
        keys = "discoverydate`accountid`accountname`region`loadbalancername`key`value";
        FileGenerator.generateJson(fileInfoMap, fieldNames, "aws-classicelb-tags.data", keys);
        fieldNames = "elb.LoadBalancerName`elb.securityGroups";
        keys = "discoverydate`accountid`accountname`region`loadbalancername`securitygroupid";
        FileGenerator.generateJson(fileInfoMap, fieldNames, "aws-classicelb-secgroups.data", keys);
        fieldNames = "elb.LoadBalancerName`listnerDesc.Listener.Protocol`listnerDesc.Listener.LoadBalancerPort";
        keys = "discoverydate`accountid`accountname`region`loadbalancername`protocol`port";
        FileGenerator.generateJson(fileInfoMap, fieldNames, "aws-classicelb-listeners.data", keys);
    }

    /**
     * Generate application elb files.
     *
     * @param fileInfoMap the elb map
     */
    public static void generateApplicationElbFiles(Map<String, List<LoadBalancerVH>> fileInfoMap) {
        String fieldNames;
        String keys;
        fieldNames = "lb.LoadBalancerArn`lb.DNSName`lb.CanonicalHostedZoneID`lb.CreatedTime`lb.LoadBalancerName`lb.Scheme`lb.VPCId`AvailabilityZones`lb.type`subnets`accessLogBucketName`accessLogBucketPrefix`accessLog";
        keys = "discoverydate`accountid`accountname`region`loadbalancerarn`dnsname`canonicalhostedzoneid`createdtime`loadbalancername`scheme`vpcid`availabilityzones`type`subnets`accesslogbucketname`accesslogbucketprefix`accesslog";
        FileGenerator.generateJson(fileInfoMap, fieldNames, "aws-appelb.data", keys);
        fieldNames = "lb.LoadBalancerName`tags.key`tags.value";
        keys = "discoverydate`accountid`accountname`region`loadbalancername`key`value";
        FileGenerator.generateJson(fileInfoMap, fieldNames, "aws-appelb-tags.data", keys);
        fieldNames = "lb.LoadBalancerArn`lb.LoadBalancerName`lb.securityGroups";
        keys = "discoverydate`accountid`accountname`region`loadbalancerarn`loadbalancername`securitygroupid";
        FileGenerator.generateJson(fileInfoMap, fieldNames, "aws-appelb-secgroups.data", keys);
        fieldNames = "lb.LoadBalancerName`listenersList.listenerArn`listenersList.loadBalancerArn`listenersList.port`listenersList.protocol";
        keys = "discoverydate`accountid`accountname`region`loadbalancername`listenerArn`loadBalancerarn`port`protocol";
        FileGenerator.generateJson(fileInfoMap, fieldNames, "aws-appelb-listeners.data", keys);
        fieldNames = "lb.LoadBalancerName`listenersList.listenerArn`rules.RuleArn`rules.IsDefault`rules.Priority";
        keys = "discoverydate`accountid`accountname`region`loadbalancername`listenerArn`rulearn`isdefault`priority";
        FileGenerator.generateJson(fileInfoMap, fieldNames, "aws-appelb-rules.data", keys);
    }

    /**
     * Generate target group files.
     *
     * @param fileInfoMap the target grp map
     */
    public static void generateTargetGroupFiles(Map<String, List<TargetGroupVH>> fileInfoMap) {
        String fieldNames;
        String keys;
        fieldNames = "trgtGrp.TargetGroupArn`trgtGrp.TargetGroupName`trgtGrp.vpcid`trgtGrp.protocol`trgtGrp.port`trgtGrp.HealthyThresholdCount`trgtGrp.UnhealthyThresholdCount`trgtGrp.HealthCheckIntervalSeconds`trgtGrp.HealthCheckTimeoutSeconds`trgtGrp.LoadBalancerArns";
        keys = "discoverydate`accountid`accountname`region`targetgrouparn`targetgroupname`vpcid`protocol`port`healthythresholdcount`unhealthythresholdcount`healthcheckintervalseconds`healthchecktimeoutseconds`loadbalancerarns";
        FileGenerator.generateJson(fileInfoMap, fieldNames, "aws-targetgroup.data", keys);

        fieldNames = "trgtGrp.TargetGroupName`targets.target.id";
        keys = "discoverydate`accountid`accountname`region`targetgrouparn`targetgroupid";
        FileGenerator.generateJson(fileInfoMap, fieldNames, "aws-targetgroup-instances.data", keys);

        Map<String, List<LoadBalancerVH>> appElbInstanceMap = new HashMap<>();
        Iterator<Entry<String, List<TargetGroupVH>>> it = fileInfoMap.entrySet().iterator();

        while (it.hasNext()) {
            Entry<String, List<TargetGroupVH>> entry = it.next();
            String accntId = entry.getKey();
            List<TargetGroupVH> trgtList = entry.getValue();
            appElbInstanceMap.putIfAbsent(accntId, new ArrayList<LoadBalancerVH>());
            for (TargetGroupVH trgtGrp : trgtList) {
                List<String> elbList = trgtGrp.getTrgtGrp().getLoadBalancerArns();
                for (String elbarn : elbList) {
                    LoadBalancer elb = new LoadBalancer();
                    elb.setLoadBalancerArn(elbarn);
                    Matcher appMatcher = Pattern.compile("(?<=loadbalancer/(app|net)/)(.*)(?=/)").matcher(elbarn);
                    if (appMatcher.find()) {
                        elb.setLoadBalancerName(appMatcher.group());
                        LoadBalancerVH elbVH = new LoadBalancerVH(elb);
                        List<com.amazonaws.services.elasticloadbalancing.model.Instance> instances = new ArrayList<>();
                        elbVH.setInstances(instances);
                        trgtGrp.getTargets().forEach(trgtHealth -> {
                            instances.add(new com.amazonaws.services.elasticloadbalancing.model.Instance(trgtHealth.getTarget().getId()));
                        });
                        appElbInstanceMap.get(accntId).add(elbVH);
                    }
                }
            }
        }
        fieldNames = "lb.LoadBalancerName`Instances.InstanceId";
        keys = "discoverydate`accountid`accountname`region`loadbalancername`instanceid";
        FileGenerator.generateJson(appElbInstanceMap, fieldNames, "aws-appelb-instances.data", keys);
    }

    /**
     * Generate nat gateway files.
     *
     * @param fileInfoMap the gateway map
     */
    public static void generateNatGatewayFiles(Map<String, List<NatGateway>> fileInfoMap) {
        String fieldNames;
        String keys;
        fieldNames = "NatGatewayId`VpcId`SubnetId`State`CreateTime`DeleteTime`FailureCode`FailureMessage";
        keys = "discoverydate`accountid`accountname`region`natgatewayid`vpcid`subnetid`state`createtime`deletetime`failurecode`failuremessage";
        FileGenerator.generateJson(fileInfoMap, fieldNames, "aws-nat.data", keys);

        fieldNames = "NatGatewayId`NatGatewayAddresses.NetworkInterfaceId`NatGatewayAddresses.PrivateIp`NatGatewayAddresses.PublicIp`NatGatewayAddresses.AllocationId";
        keys = "discoverydate`accountid`accountname`region`natgatewayid`networkinterfaceid`privateip`publicip`allocationid";
        FileGenerator.generateJson(fileInfoMap, fieldNames, "aws-nat-addresses.data", keys);
    }

    /**
     * Generate RDS cluster files.
     *
     * @param fileInfoMap the rdscluster map
     */
    public static void generateRDSClusterFiles(Map<String, List<DBClusterVH>> fileInfoMap) {
        String fieldNames;
        String keys;
        fieldNames = "cluster.DBClusterArn`cluster.AllocatedStorage`cluster.AvailabilityZones`cluster.BackupRetentionPeriod`cluster.CharacterSetName`cluster.ClusterCreateTime`cluster.DatabaseName`cluster.DBClusterIdentifier`cluster.DBClusterParameterGroup"
                + "`cluster.DbClusterResourceId`cluster.DBSubnetGroup`cluster.EarliestRestorableTime`cluster.Endpoint`cluster.Engine`cluster.EngineVersion`cluster.HostedZoneId`cluster.IAMDatabaseAuthenticationEnabled"
                + "`cluster.KmsKeyId`cluster.LatestRestorableTime`cluster.MasterUsername`cluster.MultiAZ`cluster.PercentProgress`cluster.Port`cluster.PreferredBackupWindow`cluster.PreferredMaintenanceWindow`cluster.ReaderEndpoint"
                + "`cluster.ReadReplicaIdentifiers`cluster.ReplicationSourceIdentifier`cluster.Status`cluster.StorageEncrypted";
        keys = "discoverydate`accountid`accountname`region`dbclusterarn`allocatedstorage`availabilityzones`backupretentionperiod`charactersetname`clustercreatetime`databasename`dbclusteridentifier`dbclusterparametergroup"
                + "`dbclusterresourceid`dbsubnetgroup`earliestrestorabletime`endpoint`engine`engineversion`hostedzoneid`iamdatabaseauthenticationenabled"
                + "`kmskeyid`latestrestorabletime`masterusername`multiaz`percentprogress`port`preferredbackupwindow`preferredmaintenancewindow`readerendpoint"
                + "`readreplicaidentifiers`replicationsourceidentifier`status`storageencrypted";
        FileGenerator.generateJson(fileInfoMap, fieldNames, "aws-rdscluster.data", keys);

        fieldNames = "cluster.DBClusterArn`cluster.VpcSecurityGroups.VpcSecurityGroupId`cluster.VpcSecurityGroups.status";
        keys = "discoverydate`accountid`accountname`region`dbclusterarn`vpcsecuritygroupid`vpcsecuritygroupstatus";
        FileGenerator.generateJson(fileInfoMap, fieldNames, "aws-rdscluster-secgroups.data", keys);

        fieldNames = "cluster.DBClusterArn`tags.key`tags.value";
        keys = "discoverydate`accountid`accountname`region`dbclusterarn`key`value";
        FileGenerator.generateJson(fileInfoMap, fieldNames, "aws-rdscluster-tags.data", keys);
    }

    /**
     * Generate RDS instance files.
     *
     * @param fileInfoMap the rds intnc map
     */
    public static void generateRDSInstanceFiles(Map<String, List<DBInstanceVH>> fileInfoMap) {
        String fieldNames;
        String keys;
        fieldNames = "dbinst.DBInstanceArn`dbinst.AllocatedStorage`dbinst.AutoMinorVersionUpgrade`dbinst.AvailabilityZone`dbinst.BackupRetentionPeriod`dbinst.CACertificateIdentifier`dbinst.CharacterSetName`dbinst.CopyTagsToSnapshot"
                + "`dbinst.DBClusterIdentifier`dbinst.DBInstanceClass`dbinst.DBInstanceIdentifier`dbinst.DbInstancePort`dbinst.DBInstanceStatus`dbinst.DbiResourceId`dbinst.DBName`dbinst.Endpoint.Address`dbinst.Endpoint.Port`dbinst.Endpoint.HostedZoneID"
                + "`dbinst.Engine`dbinst.EngineVersion`dbinst.EnhancedMonitoringResourceArn`dbinst.IAMDatabaseAuthenticationEnabled`dbinst.InstanceCreateTime`dbinst.Iops`dbinst.KmsKeyId`dbinst.LatestRestorableTime`dbinst.LicenseModel`dbinst.MasterUsername`dbinst.MonitoringInterval"
                + "`dbinst.MonitoringRoleArn`dbinst.MultiAZ`dbinst.PreferredBackupWindow`dbinst.PreferredMaintenanceWindow`dbinst.PromotionTier`dbinst.PubliclyAccessible`dbinst.SecondaryAvailabilityZone`dbinst.StorageEncrypted`dbinst.StorageType`dbinst.TdeCredentialArn`dbinst.Timezone`dbinst.ReadReplicaDBClusterIdentifiers`dbinst.ReadReplicaDBInstanceIdentifiers`dbinst.ReadReplicaSourceDBInstanceIdentifier`dbinst.dBSubnetGroup.vpcId`subnets`securityGroups";

        keys = "discoverydate`accountid`accountname`region`dbclusterarn`allocatedstorage`autominorversionupgrade`availabilityzones`backupretentionperiod`cacertificateidentifier`charactersetname`copytagstosnapshot"
                + "`dbclusteridentifier`dbinstanceclass`dbinstanceidentifier`dbinstanceport`dbinstancestatus`dbiresourceid`dbname`endpointaddress`endpointport`endpointhostedzoneid"
                + "`engine`engineversion`enhancedmonitoringresourcearn`iamdatabaseauthenticationenabled`instancecreatetime`iops`kmskeyid`latestrestorabletime`licensemodel`masterusername`monitoringinterval"
                + "`monitoringrolearn`multiaz`preferredbackupwindow`preferredmaintenancewindow`promotiontier`publiclyaccessible`secondaryavailabilityzone`storageencrypted`storagetype`tdecredentialarn`timezone`"
                + "readreplicadbclusteridentifiers`readreplicadbinstanceidentifiers`readreplicasourcedbinstanceidentifier`vpcid`subnets`securitygroups";
        FileGenerator.generateJson(fileInfoMap, fieldNames, "aws-rdsdb.data", keys);

        fieldNames = "dbinst.DBInstanceArn`dbinst.VpcSecurityGroups.VpcSecurityGroupId`dbinst.VpcSecurityGroups.status";
        keys = "discoverydate`accountid`accountname`region`dbclusterarn`vpcsecuritygroupid`vpcsecuritygroupstatus";
        FileGenerator.generateJson(fileInfoMap, fieldNames, "aws-rdsdb-secgroups.data", keys);

        fieldNames = "dbinst.DBInstanceArn`tags.key`tags.value";
        keys = "discoverydate`accountid`accountname`region`dbclusterarn`key`value";
        FileGenerator.generateJson(fileInfoMap, fieldNames, "aws-rdsdb-tags.data", keys);
    }

    /**
     * Generate S3 files.
     *
     * @param fileInfoMap the bucket map
     */
    public static void generateS3Files(Map<String, List<BucketVH>> fileInfoMap) {
        String fieldNames;
        String keys;
        fieldNames = "bucket.Name`bucket.CreationDate`bucket.owner.displayname`bucket.owner.id`versionStatus`mfaDelete`location`bucketEncryp`websiteConfiguration`isLoggingEnabled`destinationBucketName`logFilePrefix`bucketPolicy";
        keys = "discoverydate`accountid`accountname`name`creationdate`ownerdisplayname`ownerid`versionstatus`mfadelete`region`bucketencryp`websiteConfiguration`isLoggingEnabled`destinationBucketName`logFilePrefix`bucketpolicy";
        FileGenerator.generateJson(fileInfoMap, fieldNames, "aws-s3.data", keys);
        fieldNames = "location`bucket.Name`tags.key`tags.value";
        keys = "discoverydate`accountid`accountname`region`name`key`value";
        FileGenerator.generateJson(fileInfoMap, fieldNames, "aws-s3-tags.data", keys);
    }

    /**
     * Generate sec group file.
     *
     * @param fileInfoMap the sec grp map
     */
    public static void generateSecGroupFile(Map<String, List<SecurityGroup>> fileInfoMap) {
        String fieldNames;
        String keys;
        fieldNames = "GroupId`Description`GroupName`OwnerId`vpcid";
        keys = "discoverydate`accountid`accountname`region`groupid`description`groupname`ownerid`vpcid";
        FileGenerator.generateJson(fileInfoMap, fieldNames, "aws-sg.data", keys);
        fieldNames = "GroupId`tags.key`tags.value";
        keys = "discoverydate`accountid`accountname`region`groupid`key`value";
        FileGenerator.generateJson(fileInfoMap, fieldNames, "aws-sg-tags.data", keys);

        Map<String, List<SGRuleVH>> secGrp = new HashMap<>();
        fileInfoMap.forEach((k, v) -> {
                    List<SGRuleVH> sgruleList = new ArrayList<>();
                    v.forEach(sg -> {
                        String groupId = sg.getGroupId();
                        sgruleList.addAll(getRuleInfo(groupId, "inbound", sg.getIpPermissions()));
                        sgruleList.addAll(getRuleInfo(groupId, "outbound", sg.getIpPermissionsEgress()));
                    });
                    secGrp.put(k, sgruleList);
                }
        );
        fieldNames = "groupId`type`ipProtocol`fromPort`toPort`cidrIp`cidrIpv6";
        keys = "discoverydate`accountid`accountname`region`groupid`type`ipprotocol`fromport`toport`cidrip`cidripv6";
        FileGenerator.generateJson(secGrp, fieldNames, "aws-sg-rules.data", keys);
    }

    /**
     * Gets the rule info.
     *
     * @param groupId     the group id
     * @param type        the type
     * @param permissions the permissions
     * @return the rule info
     */
    private static List<SGRuleVH> getRuleInfo(String groupId, String type, List<IpPermission> permissions) {
        List<SGRuleVH> sgruleList = new ArrayList<>();
        permissions.forEach(obj -> {
            String ipProtocol = obj.getIpProtocol();
            Integer fromPort = obj.getFromPort();
            Integer toPort = obj.getToPort();
            String fromPortStr;
            String toPortStr;
            fromPortStr = fromPort == null ? "" : fromPort == -1 ? "All" : fromPort.toString();
            toPortStr = toPort == null ? "" : toPort == -1 ? "All" : toPort.toString();
            obj.getIpv4Ranges().forEach(iprange -> {
                String cidrIp = iprange.getCidrIp();
                SGRuleVH rule = new SGRuleVH(groupId, type, fromPortStr, toPortStr, "", cidrIp, "-1".equals(ipProtocol) ? "All" : ipProtocol);
                sgruleList.add(rule);
            });
            obj.getIpv6Ranges().forEach(iprange -> {
                String cidrIpv6 = iprange.getCidrIpv6();
                SGRuleVH rule = new SGRuleVH(groupId, type, fromPortStr, toPortStr, cidrIpv6, "", "-1".equals(ipProtocol) ? "All" : ipProtocol);
                sgruleList.add(rule);
            });
            if (obj.getIpv4Ranges().isEmpty() && obj.getIpv6Ranges().isEmpty()) {
                SGRuleVH rule = new SGRuleVH(groupId, type, fromPortStr, toPortStr, "", "", "-1".equals(ipProtocol) ? "All" : ipProtocol);
                sgruleList.add(rule);
            }
        });

        return sgruleList;
    }

    /**
     * Generate subnet files.
     *
     * @param fileInfoMap the subnet map
     */
    public static void generateSubnetFiles(Map<String, List<Subnet>> fileInfoMap) {
        String fieldNames;
        String keys;
        fieldNames = "SubnetId`AssignIpv6AddressOnCreation`AvailabilityZone`AvailableIpAddressCount`CidrBlock`DefaultForAz`MapPublicIpOnLaunch`State`VpcId";
        keys = "discoverydate`accountid`accountname`region`subnetid`assignipv6addressoncreation`availabilityzone`availableipaddresscount`cidrblock`defaultforaz`mappubliciponlaunch`state`vpcid";
        FileGenerator.generateJson(fileInfoMap, fieldNames, "aws-subnet.data", keys);
        fieldNames = "SubnetId`tags.key`tags.value";
        keys = "discoverydate`accountid`accountname`region`subnetid`key`value";
        FileGenerator.generateJson(fileInfoMap, fieldNames, "aws-subnet-tags.data", keys);
    }

    /**
     * Generate trusted advisor files.
     *
     * @param fileInfoMap the checks map
     */
    public static void generateTrustedAdvisorFiles(Map<String, List<CheckVH>> fileInfoMap) {
        String fieldNames;
        String keys;
        fieldNames = "check.Id`check.Category`status`check.name`check.Description";
        keys = "discoverydate`accountid`accountname`region`checkid`checkcategory`status`checkname`checkdescription";
        FileGenerator.generateJson(fileInfoMap, fieldNames, "aws-checks.data", keys);

        Iterator<Entry<String, List<CheckVH>>> it = fileInfoMap.entrySet().iterator();
        Map<String, List<Resource>> resourceMap = new HashMap<>();
        while (it.hasNext()) {
            Entry<String, List<CheckVH>> entry = it.next();
            String account = entry.getKey();
            List<CheckVH> checksValue = entry.getValue();
            List<Resource> resources = new ArrayList<>();
            checksValue.forEach(obj -> {
                        resources.addAll(obj.getResources());
                    }
            );
            resourceMap.put(account, resources);
        }

        fieldNames = "checkid`id`status`data";
        keys = "discoverydate`accountid`accountname`region`checkid`id`status`resourceinfo";
        FileGenerator.generateJson(resourceMap, fieldNames, "aws-checks-resources.data", keys);
    }

    /**
     * Generate redshift files.
     *
     * @param fileInfoMap the redshift fileInfoMap
     */
    public static void generateRedshiftFiles(Map<String, List<RedshiftVH>> fileInfoMap) {
        String fieldNames;
        String keys;
        fieldNames = "cluster.ClusterIdentifier`cluster.AllowVersionUpgrade`cluster.AutomatedSnapshotRetentionPeriod`cluster.AvailabilityZone`cluster.ClusterCreateTime`cluster.ClusterPublicKey`"
                + "cluster.ClusterRevisionNumber`cluster.ClusterStatus`cluster.ClusterSubnetGroupName`cluster.ClusterVersion`cluster.DBName`cluster.ElasticIpStatus`cluster.Encrypted`cluster.Endpoint.Address`"
                + "cluster.Endpoint.Port`cluster.EnhancedVpcRouting`cluster.KmsKeyId`cluster.MasterUsername`cluster.ModifyStatus`cluster.NodeType`cluster.NumberOfNodes`cluster.PreferredMaintenanceWindow`cluster.PubliclyAccessible`cluster.VpcId`subnets";
        keys = "discoverydate`accountid`accountname`region`clusteridentifier`allowversionupgrade`automatedsnapshotretentionperiod`availabilityzone`clustercreatetime`clusterpublickey`"
                + "clusterrevisionnumber`clusterstatus`clustersubnetgroupname`clusterversion`dbname`elasticipstatus`encrypted`endpointaddress`endpointport`enhancedvpcrouting`kmskeyid`"
                + "masterusername`modifystatus`nodetype`numberofnodes`preferredmaintenancewindow`publiclyaccessible`vpcid`subnets";
        FileGenerator.generateJson(fileInfoMap, fieldNames, "aws-redshift.data", keys);

        fieldNames = "cluster.ClusterIdentifier`cluster.VpcSecurityGroups.VpcSecurityGroupId`cluster.VpcSecurityGroups.status";
        keys = "discoverydate`accountid`accountname`region`clusteridentifier`vpcsecuritygroupid`vpcsecuritygroupstatus";
        FileGenerator.generateJson(fileInfoMap, fieldNames, "aws-redshift-secgroups.data", keys);

        fieldNames = "cluster.ClusterIdentifier`cluster.tags.key`cluster.tags.value";
        keys = "discoverydate`accountid`accountname`region`clusteridentifier`key`value";
        FileGenerator.generateJson(fileInfoMap, fieldNames, "aws-redshift-tags.data", keys);

    }

    /**
     * Generate volume files.
     *
     * @param fileInfoMap the volume map
     */
    public static void generatefetchVolumeFiles(Map<String, List<Volume>> fileInfoMap) {
        String fieldNames;
        String keys;
        fieldNames = "VolumeId`VolumeType`AvailabilityZone`CreateTime`Encrypted`Iops`KmsKeyId`Size`SnapshotId`State";
        keys = "discoverydate`accountid`accountname`region`volumeid`volumetype`availabilityzone`createtime`encrypted`iops`kmskeyid`size`snapshotid`state";
        FileGenerator.generateJson(fileInfoMap, fieldNames, "aws-volume.data", keys);

        fieldNames = "VolumeId`attachments.InstanceId`attachments.AttachTime`attachments.DeleteOnTermination`attachments.Device`attachments.State";
        keys = "discoverydate`accountid`accountname`region`volumeid`instanceid`attachtime`deleteontermination`device`state";
        FileGenerator.generateJson(fileInfoMap, fieldNames, "aws-volume-attachments.data", keys);

        fieldNames = "VolumeId`tags.key`tags.value";
        keys = "discoverydate`accountid`accountname`region`volumeid`key`value";
        FileGenerator.generateJson(fileInfoMap, fieldNames, "aws-volume-tags.data", keys);
    }

    /**
     * Generate snapshot files.
     *
     * @param fileInfoMap the snapshot map
     */
    public static void generateSnapshotFiles(Map<String, List<SnapshotVH>> fileInfoMap) {
        String fieldNames;
        String keys;
        fieldNames = "snapshot.SnapshotId`snapshot.Description`snapshot.VolumeId`snapshot.VolumeSize`snapshot.Encrypted`snapshot.DataEncryptionKeyId"
                + "`snapshot.KmsKeyId`snapshot.OwnerAlias`snapshot.OwnerId`snapshot.Progress`snapshot.StartTime`snapshot.State`snapshot.StateMessage`isSnapshotPublic";
        keys = "discoverydate`accountid`accountname`region`snapshotid`description`volumeid`volumesize`encrypted`dataencryptionkeyid`"
                + "kmskeyid`owneralias`ownerid`progress`starttime`state`statemessage`ispublic";
        FileGenerator.generateJson(fileInfoMap, fieldNames, "aws-snapshot.data", keys);
        fieldNames = "snapshot.SnapshotId`snapshot.tags.key`snapshot.tags.value";
        keys = "discoverydate`accountid`accountname`region`snapshotid`key`value";
        FileGenerator.generateJson(fileInfoMap, fieldNames, "aws-snapshot-tags.data", keys);
    }

    /**
     * Generate vpc files.
     *
     * @param fileInfoMap the vpc map
     */
    public static void generateVpcFiles(Map<String, List<VpcVH>> fileInfoMap) {

        String fieldNames;
        String keys;
        fieldNames = "vpc.vpcId`vpc.cidrBlock`vpc.dhcpOptionsId`vpc.instanceTenancy`vpc.isDefault";
        keys = "discoverydate`accountid`accountname`region`vpcid`cidrblock`dhcpoptionsid`instancetenancy`isdefault";
        FileGenerator.generateJson(fileInfoMap, fieldNames, "aws-vpc.data", keys);
        fieldNames = "vpc.vpcId`vpc.tags.key`vpc.tags.value";
        keys = "discoverydate`accountid`accountname`region`vpcid`key`value";
        FileGenerator.generateJson(fileInfoMap, fieldNames, "aws-vpc-tags.data", keys);
        fieldNames = "vpcEndPoints.vpcId`vpcEndPoints.vpcEndpointId`vpcEndPoints.serviceName`vpcEndPoints.state`vpcEndPoints.creationTimestamp`vpcEndPoints.publicAccess`vpcEndPoints.policyDocument`vpcEndPoints.routeTableIds";
        keys = "discoverydate`accountid`accountname`region`vpcid`vpcendpointid`servicename`state`creationtimestamp`publicaccess`policydocument`routetableids";
        FileGenerator.generateJson(fileInfoMap, fieldNames, "aws-vpc-endpoints.data", keys);
        fieldNames = "vpc.vpcId`vpc.cidrBlockAssociationSet.cidrBlockState.state`vpc.cidrBlockAssociationSet.cidrBlockState.statusMessage`vpc.cidrBlockAssociationSet.associationId";
        keys = "discoverydate`accountid`accountname`region`vpcid`state`cidrblockset`cidrblockstate`cidrblockstatusmessage`cidrblockassociationid";
        FileGenerator.generateJson(fileInfoMap, fieldNames, "aws-vpc-cidrblock-association.data", keys);
    }

    /**
     * Generate api gateway files.
     *
     * @param fileInfoMap the api gateway map
     */
    public static void generateApiGatewayFiles(Map<String, List<RestApi>> fileInfoMap) {
        String fieldNames;
        String keys;
        fieldNames = "Id`Name`Description`CreatedDate`Version";
        keys = "discoverydate`accountid`accountname`region`id`name`description`createdTime`version";
        FileGenerator.generateJson(fileInfoMap, fieldNames, "aws-api.data", keys);
    }

    /**
     * Generate iam user files.
     *
     * @param fileInfoMap the user map
     */
    public static void generateIamUserFiles(Map<String, List<UserVH>> fileInfoMap) {
        String fieldNames;
        String keys;
        fieldNames = "user.username`user.userid`user.arn`user.CreateDate`user.path`passwordCreationDate`user.PasswordLastUsed`passwordResetRequired`mfa`groups";
        keys = "discoverydate`accountid`accountname`region`username`userid`arn`createdate`path`passwordcreationdate`passwordlastused`passwordresetrequired`mfaenabled`groups";
        FileGenerator.generateJson(fileInfoMap, fieldNames, "aws-iamuser.data", keys);
        fieldNames = "user.username`accessKeys.AccessKeyId`accessKeys.CreateDate`accessKeys.status`accessKeys.lastUsedDate";
        keys = "discoverydate`accountid`accountname`region`username`accesskey`createdate`status`lastuseddate";
        FileGenerator.generateJson(fileInfoMap, fieldNames, "aws-iamuser-keys.data", keys);

    }

    /**
     * Generate RDS snapshot files.
     *
     * @param fileInfoMap the db snapshots
     */
    public static void generateRDSSnapshotFiles(Map<String, List<DBSnapshot>> fileInfoMap) {

        String fieldNames;
        String keys;
        fieldNames = "DBSnapshotIdentifier`DBSnapshotArn`DBInstanceIdentifier`Status`snapshotCreateTime`snapshotType"
                + "`encrypted`engine`allocatedStorage`port`availabilityZone`vpcId`instanceCreateTime`masterUsername"
                + "`engineVersion`licenseModel`iops`optionGroupName`percentProgress`sourceRegion`sourceDBSnapshotIdentifier"
                + "`storageType`tdeCredentialArn`kmsKeyId`timezone`iAMDatabaseAuthenticationEnabled";
        keys = "discoverydate`accountid`accountname`region`dbsnapshotidentifier`dbsnapshotarn`dbinstanceidentifier`status`snapshotcreatetime`snapshottype`"
                + "encrypted`engine`allocatedstorage`port`availabilityzone`vpcid`instancecreatetime`masterusername`engineversion`licensemodel`"
                + "iops`optiongroupname`percentprogress`sourceregion`sourcedbsnapshotidentifier`storagetype`tdecredentialarn`kmskeyid`timezone`"
                + "iamdatabaseauthenticationenabled";
        FileGenerator.generateJson(fileInfoMap, fieldNames, "aws-rdssnapshot.data", keys);
    }

    /**
     * Generate iam role files.
     *
     * @param fileInfoMap the iam role map
     */
    public static void generateIamRoleFiles(Map<String, List<Role>> fileInfoMap) {
        String fieldNames;
        String keys;
        fieldNames = "roleName`roleId`arn`description`path`createDate`assumeRolePolicyDocument`maxSessionDuration`roleLastUsed.lastUsedDate";
        keys = "discoverydate`accountid`accountname`region`rolename`roleid`rolearn`description`path`createdate`assumedpolicydoc`maxsessionduration`lastuseddate";
        FileGenerator.generateJson(fileInfoMap, fieldNames, "aws-iamrole.data", keys);
    }

    /**
     * Generate KMS files.
     *
     * @param fileInfoMap the kms key map
     */
    public static void generateKMSFiles(Map<String, List<KMSKeyVH>> fileInfoMap) {
        String fieldNames;
        String keys;
        fieldNames = "key.keyId`key.arn`key.creationDate`key.aWSAccountId`key.description`key.keyState`key.enabled`key.keyUsage`key.deletionDate`key.validTo"
                + "`rotationStatus`alias.aliasName`alias.aliasArn`key.keyManager";
        keys = "discoverydate`accountid`accountname`region`keyid`arn`creationdate`awsaccountid`description`keystate`keyenabled`keyusage`deletiondate`validto`"
                + "rotationstatus`aliasname`aliasarn`keymanager";
        FileGenerator.generateJson(fileInfoMap, fieldNames, "aws-kms.data", keys);
        fieldNames = "key.keyId`tags.tagKey`tags.tagValue";
        keys = "discoverydate`accountid`accountname`region`keyid`key`value";
        FileGenerator.generateJson(fileInfoMap, fieldNames, "aws-kms-tags.data", keys);
    }

    /**
     *  Generate document db files.
     *
     * @param fileInfoMap the document map
     */
    public static void generateDocumentDbFiles(Map<String, List<DocumentDBVH>> fileInfoMap) {
        String fieldNames;
        String keys;

        fieldNames = "clusters.hostedZoneId`clusters.dbClusterResourceId`clusters.storageEncrypted`clusters.kmsKeyId";
        keys = "discoverydate`accountid`accountname`region`hostedzoneid`dbclusterresourceid`storageencrypted`kmskeyid";
        FileGenerator.generateJson(fileInfoMap, fieldNames, "aws-documentdb.data", keys);

    }

    /**
     *  Generate document db files.
     *
     * @param fileInfoMap the document map
     */
    public static void generateDMSFiles(Map<String, List<ReplicationInstance>> fileInfoMap) {
        String fieldNames;
        String keys;

        fieldNames = "replicationInstanceArn`replicationInstanceIdentifier`availabilityZone`multiAZ`kmsKeyId`publiclyAccessible";
        keys = "discoverydate`accountid`accountname`region`replicationinstancearn`replicationinstanceid`availabilityzone`multiaz`kmskeyid`publiclyAccessible";
        FileGenerator.generateJson(fileInfoMap, fieldNames, "aws-dms.data", keys);

    }

    /**
     * Generate cloud front files.
     *
     * @param fileInfoMap the cf map
     */
    public static void generateCloudFrontFiles(Map<String, List<CloudFrontVH>> fileInfoMap) {
        String fieldNames;
        String keys;
        fieldNames = "distSummary.id`distSummary.aRN`distSummary.status`distSummary.lastModifiedTime`distSummary.domainName`distSummary.enabled"
                + "`distSummary.comment`distSummary.priceClass`distSummary.webACLId`distSummary.httpVersion`distSummary.isIPV6Enabled`distSummary.viewerCertificate.iAMCertificateId"
                + "`distSummary.viewerCertificate.aCMCertificateArn`distSummary.viewerCertificate.cloudFrontDefaultCertificate`distSummary.viewerCertificate.sSLSupportMethod`distSummary.viewerCertificate.minimumProtocolVersion`distSummary.aliases.items`bucketName`accessLogEnabled`defaultRootObject";
        keys = "discoverydate`accountid`accountname`region`id`arn`status`lastmodifiedtime`domainName`enabled`comment`priceclass`webaclid`httpversion`ipv6enabled`viewercertificateid"
                + "`viewercertificatearn`viewercertificatedefaultcertificate`viewercertificatesslsupportmethod`viewercertificateminprotocolversion`aliases`bucketname`accesslogenabled`defaultRootObject";
        FileGenerator.generateJson(fileInfoMap, fieldNames, "aws-cloudfront.data", keys);
        fieldNames = "distSummary.id`tags.key`tags.value";
        keys = "discoverydate`accountid`accountname`region`id`key`value";
        FileGenerator.generateJson(fileInfoMap, fieldNames, "aws-cloudfront-tags.data", keys);
    }


    /**
     * Generate EBS files.
     *
     * @param fileInfoMap the ebs map
     */
    public static void generateEBSFiles(Map<String, List<EbsVH>> fileInfoMap) {
        String fieldNames;
        String keys;
        fieldNames = "app.applicationArn`app.applicationName`app.description`app.dateCreated`app.dateUpdated`app.versions`app.configurationTemplates`env.environmentName`env.environmentId`env.versionLabel`env.solutionStackName"
                + "`env.platformArn`env.templateName`env.description`env.endpointURL`env.cNAME`env.dateCreated`env.dateUpdated`env.status`env.abortableOperationInProgress`env.environmentArn"
                + "`env.health`env.healthStatus";
        keys = "discoverydate`accountid`accountname`region`applicationarn`applicationname`description`datecreated`dateupdated`versions`configtemplates`env_name`env_id`env_versionlabel`env_solutionstackname"
                + "`env_platformarn`env_templatename`env_description`env_endpointurl`env_cname`env_datecreated`env_dateupdated`env_status`env_abortableoperationinprogress`env_arn"
                + "`env_health`env_healthstatus";
        FileGenerator.generateJson(fileInfoMap, fieldNames, "aws-beanstalk.data", keys);

        fieldNames = "app.applicationName`env.environmentArn`envResource.instances.id";
        keys = "discoverydate`accountid`accountname`region`applicationname`env-arn`instanceid";
        FileGenerator.generateJson(fileInfoMap, fieldNames, "aws-beanstalk-instance.data", keys);

        fieldNames = "app.applicationName`env.environmentArn`envResource.autoScalingGroups.name";
        keys = "discoverydate`accountid`accountname`region`applicationname`env-arn`asgname";
        FileGenerator.generateJson(fileInfoMap, fieldNames, "aws-beanstalk-asg.data", keys);

        fieldNames = "app.applicationName`env.environmentArn`envResource.loadBalancers.name";
        keys = "discoverydate`accountid`accountname`region`applicationname`env-arn`loadbalancername";
        FileGenerator.generateJson(fileInfoMap, fieldNames, "aws-beanstalk-elb.data", keys);
    }

    /**
     * Generate PHD files.
     *
     * @param fileInfoMap the phd map
     */
    public static void generatePHDFiles(Map<String, List<PhdVH>> fileInfoMap) {
        String fieldNames;
        String keys;
        fieldNames = "eventDetails.event.arn`eventDetails.event.service`eventDetails.event.eventTypeCode`eventDetails.event.eventTypeCategory`eventDetails.event.region`"
                + "eventDetails.event.availabilityZone`eventDetails.event.startTime`eventDetails.event.endTime`eventDetails.event.lastUpdatedTime`eventDetails.event.statusCode"
                + "`eventDetails.eventDescription.latestDescription`eventDetails.eventMetadata";
        keys = "discoverydate`accountid`accountname`region`eventarn`eventservice`eventtypecode`eventtypecategory`eventregion`availabilityzone`starttime`endtime`"
                + "lastupdatedtime`statuscode`latestdescription`eventmetadata";
        FileGenerator.generateJson(fileInfoMap, fieldNames, "aws-phd.data", keys);
        fieldNames = "affectedEntities.eventArn`affectedEntities.entityArn`affectedEntities.awsAccountId`affectedEntities.entityValue`affectedEntities.lastUpdatedTime`affectedEntities.statusCode`affectedEntities.tags";
        keys = "discoverydate`accountid`accountname`region`eventarn`entityarn`awsaccountid`entityvalue`lastupdatedtime`statuscode`tags";
        FileGenerator.generateJson(fileInfoMap, fieldNames, "aws-phd-entities.data", keys);
    }

    /**
     * @param fileInfoMap the error map
     */
    public static synchronized void generateErrorFile(Map<String, List<ErrorVH>> fileInfoMap) {
        String fieldNames;
        String keys;
        fieldNames = "region`type`exception";
        keys = "discoverydate`accountid`region`type`message";
        FileGenerator.generateJson(fileInfoMap, fieldNames, "aws-loaderror.data", keys);
    }

    /**
     * Generate EC2 route table files.
     *
     * @param fileInfoMap the route table map
     */
    public static void generateEC2RouteTableFiles(Map<String, List<RouteTable>> fileInfoMap) {
        String fieldNames;
        String keys;
        fieldNames = "routeTableId`vpcId";
        keys = "discoverydate`accountid`accountname`region`routetableid`vpcid";
        FileGenerator.generateJson(fileInfoMap, fieldNames, "aws-routetable.data", keys);

        fieldNames = "routeTableId`routes.destinationCidrBlock`routes.destinationPrefixListId`routes.gatewayId`routes.instanceId`routes.instanceOwnerId`routes.networkInterfaceId`routes.vpcPeeringConnectionId`routes.natGatewayId"
                + "`routes.state`routes.origin`routes.destinationIpv6CidrBlock`routes.egressOnlyInternetGatewayId";
        keys = "discoverydate`accountid`accountname`region`routetableid`destinationcidrblock`destinationprefixlistid`gatewayid`instanceid`instanceownerid`networkinterfaceid`vpcpeeringconnectionid`natgatewayid"
                + "`state`origin`destinationipv6cidrblock`egressonlyinternetgatewayid";
        FileGenerator.generateJson(fileInfoMap, fieldNames, "aws-routetable-routes.data", keys);

        fieldNames = "routeTableId`associations.routeTableAssociationId`associations.subnetId`associations.main";
        keys = "discoverydate`accountid`accountname`region`routetableid`routetableassociationid`subnetid`main";
        FileGenerator.generateJson(fileInfoMap, fieldNames, "aws-routetable-associations.data", keys);

        fieldNames = "routeTableId`propagatingVgws.gatewayId";
        keys = "discoverydate`accountid`accountname`region`routetableid`gatewayid";
        FileGenerator.generateJson(fileInfoMap, fieldNames, "aws-routetable-propagatingvgws.data", keys);

        fieldNames = "routeTableId`tags.key`tags.value";
        keys = "discoverydate`accountid`accountname`region`routetableid`key`value";
        FileGenerator.generateJson(fileInfoMap, fieldNames, "aws-routetable-tags.data", keys);
    }

    /**
     * Generate network acl files.
     *
     * @param fileInfoMap the network acl map
     */
    public static void generateNetworkAclFiles(Map<String, List<NetworkAcl>> fileInfoMap) {
        String fieldNames;
        String keys;
        fieldNames = "networkAclId`vpcId`isDefault";
        keys = "discoverydate`accountid`accountname`region`networkaclid`vpcid`isdefault";
        FileGenerator.generateJson(fileInfoMap, fieldNames, "aws-networkacl.data", keys);

        fieldNames = "networkAclId`entries.ruleNumber`entries.protocol`entries.ruleAction`entries.egress`entries.cidrBlock`entries.ipv6CidrBlock`entries.icmpTypeCode.type`entries.icmpTypeCode.code"
                + "`entries.portRange.from`entries.portRange.to";
        keys = "discoverydate`accountid`accountname`region`networkaclid`rulenumber`protocol`ruleaction`egress`cidrblock`ipv6cidrblock`icmptype`icmptypecode`portrangefrom`portrangeto";
        FileGenerator.generateJson(fileInfoMap, fieldNames, "aws-networkacl-entries.data", keys);

        fieldNames = "networkAclId`associations.networkAclAssociationId`associations.subnetId";
        keys = "discoverydate`accountid`accountname`region`networkaclid`networkaclassociationid`subnetid";
        FileGenerator.generateJson(fileInfoMap, fieldNames, "aws-networkacl-associations.data", keys);

        fieldNames = "networkAclId`tags.key`tags.value";
        keys = "discoverydate`accountid`accountname`region`networkaclid`vpcid`key`value";
        FileGenerator.generateJson(fileInfoMap, fieldNames, "aws-networkacl-tags.data", keys);
    }

    /**
     * Generate elastic IP files.
     *
     * @param fileInfoMap the elastic IP map
     */
    public static void generateElasticIPFiles(Map<String, List<Address>> fileInfoMap) {
        String fieldNames;
        String keys;
        fieldNames = "instanceId`publicIp`allocationId`associationId`domain`networkInterfaceId`networkInterfaceOwnerId`privateIpAddress";
        keys = "discoverydate`accountid`accountname`region`instanceid`publicip`allocationid`associationid`domain`networkinterfaceid`networkinterfaceownerid`privateipaddress";
        FileGenerator.generateJson(fileInfoMap, fieldNames, "aws-elasticip.data", keys);
    }

    /**
     * Generate launch configurations files.
     *
     * @param fileInfoMap the launch configuration map
     */
    public static void generateLaunchConfigurationsFiles(Map<String, List<LaunchConfiguration>> fileInfoMap) {
        String fieldNames;
        String keys;
        fieldNames = "launchConfigurationName`launchConfigurationARN`imageId`keyName`classicLinkVPCId`userData`instanceType`kernelId`ramdiskId`spotPrice`iamInstanceProfile`createdTime`ebsOptimized`associatePublicIpAddress`placementTenancy"
                + "`securityGroups`classicLinkVPCSecurityGroups`instanceMonitoring.enabled";
        keys = "discoverydate`accountid`accountname`region`launchconfigurationname`launchconfigurationarn`imageid`keyname`classiclinkvpcid`userdata`instancetype`kernelid`ramdiskid`spotprice`iaminstanceprofile`createdtime`ebsoptimized`associatepublicipaddress`placementtenancy"
                + "`securitygroups`classiclinkvpcsecuritygroups`instancemonitoringenabled";
        FileGenerator.generateJson(fileInfoMap, fieldNames, "aws-launchconfig.data", keys);

        fieldNames = "launchConfigurationName`blockDeviceMappings.virtualName`blockDeviceMappings.deviceName`blockDeviceMappings.ebs.snapshotId`blockDeviceMappings.ebs.volumeSize"
                + "`blockDeviceMappings.ebs.volumeType`blockDeviceMappings.ebs.deleteOnTermination`blockDeviceMappings.ebs.iops`blockDeviceMappings.ebs.encrypted`blockDeviceMappings.noDevice";
        keys = "discoverydate`accountid`accountname`region`launchconfigurationname`virtualname`devicename`ebssnapshotid`ebsvolumesize"
                + "`ebsvolumetype`ebsdeleteontermination`ebsiops`ebsencrypted`nodevice";
        FileGenerator.generateJson(fileInfoMap, fieldNames, "aws-launchconfig-blockdevicemappings.data", keys);
    }

    /**
     * Generate internet gateway files.
     *
     * @param fileInfoMap the internet gateway map
     */
    public static void generateInternetGatewayFiles(Map<String, List<InternetGateway>> fileInfoMap) {
        String fieldNames;
        String keys;
        fieldNames = "internetGatewayId";
        keys = "discoverydate`accountid`accountname`region`internetgatewayid";
        FileGenerator.generateJson(fileInfoMap, fieldNames, "aws-internetgateway.data", keys);

        fieldNames = "internetGatewayId`attachments.vpcId`attachments.state";
        keys = "discoverydate`accountid`accountname`region`internetgatewayid`vpcid`state";
        FileGenerator.generateJson(fileInfoMap, fieldNames, "aws-internetgateway-attachments.data", keys);

        fieldNames = "internetGatewayId`tags.key`tags.value";
        keys = "discoverydate`accountid`accountname`region`internetgatewayid`key`value";
        FileGenerator.generateJson(fileInfoMap, fieldNames, "aws-internetgateway-tags.data", keys);
    }

    /**
     * Generate VPN gateway files.
     *
     * @param fileInfoMap the vpn gateway map
     */
    public static void generateVPNGatewayFiles(Map<String, List<VpnGateway>> fileInfoMap) {
        String fieldNames;
        String keys;
        fieldNames = "vpnGatewayId`state`type`availabilityZone`amazonSideAsn";
        keys = "discoverydate`accountid`accountname`region`vpngatewayid`state`type`availabilityzone`amazonsideasn";
        FileGenerator.generateJson(fileInfoMap, fieldNames, "aws-vpngateway.data", keys);

        fieldNames = "vpnGatewayId`vpcAttachments.vpcId`vpcAttachments.state";
        keys = "discoverydate`accountid`accountname`region`vpngatewayid`vpcid`state";
        FileGenerator.generateJson(fileInfoMap, fieldNames, "aws-vpngateway-vpcattachments.data", keys);

        fieldNames = "vpnGatewayId`tags.key`tags.value";
        keys = "discoverydate`accountid`accountname`region`vpngatewayid`key`value";
        FileGenerator.generateJson(fileInfoMap, fieldNames, "aws-vpngateway-tags.data", keys);
    }

    /**
     * Generate scaling policies.
     *
     * @param fileInfoMap the scaling policy map
     */
    public static void generateScalingPolicies(Map<String, List<ScalingPolicy>> fileInfoMap) {
        String fieldNames;
        String keys;
        fieldNames = "policyName`policyARN`autoScalingGroupName`policyType`adjustmentType`minAdjustmentStep`minAdjustmentMagnitude`scalingAdjustment`cooldown`metricAggregationType`estimatedInstanceWarmup";
        keys = "discoverydate`accountid`accountname`region`policyname`policyarn`autoscalinggroupname`policytype`adjustmenttype`minadjustmentstep`minadjustmentmagnitude`scalingadjustment`cooldown`metricaggregationtype`estimatedinstancewarmup";
        FileGenerator.generateJson(fileInfoMap, fieldNames, "aws-asgpolicy.data", keys);

        fieldNames = "policyName`stepAdjustments.metricIntervalLowerBound`stepAdjustments.metricIntervalUpperBound`stepAdjustments.scalingAdjustment";
        keys = "discoverydate`accountid`accountname`region`policyname`metricintervallowerbound`metricintervalupperbound`scalingadjustment";
        FileGenerator.generateJson(fileInfoMap, fieldNames, "aws-asgpolicy-stepadjustments.data", keys);

        fieldNames = "policyName`alarms.alarmName`alarms.alarmARN";
        keys = "discoverydate`accountid`accountname`region`policyname`alarmname`alarmarn";
        FileGenerator.generateJson(fileInfoMap, fieldNames, "aws-asgpolicy-alarms.data", keys);
    }

    /**
     * Generate SNS topics.
     *
     * @param fileInfoMap the subscription map
     */
    public static void generateSNSTopics(Map<String, List<Topic>> fileInfoMap) {
        String fieldNames;
        String keys;
        fieldNames = "topicArn";
        keys = "discoverydate`accountid`accountname`region`topicarn";
        FileGenerator.generateJson(fileInfoMap, fieldNames, "aws-snstopic.data", keys);

    }

    /**
     * Generate egress gateway.
     *
     * @param fileInfoMap the egress gateway map
     */
    public static void generateEgressGateway(Map<String, List<EgressOnlyInternetGateway>> fileInfoMap) {
        String fieldNames;
        String keys;
        fieldNames = "egressOnlyInternetGatewayId`attachments.vpcId`attachments.state";
        keys = "discoverydate`accountid`accountname`region`egressonlyinternetgatewayid`attachmentsvpcid`attachmentsstate";
        FileGenerator.generateJson(fileInfoMap, fieldNames, "aws-egressgateway.data", keys);
    }

    /**
     * Generate dhcp options.
     *
     * @param fileInfoMap the dhcp options map
     */
    public static void generateDhcpOptions(Map<String, List<DhcpOptions>> fileInfoMap) {
        String fieldNames;
        String keys;
        fieldNames = "dhcpOptionsId`dhcpConfigurations";
        keys = "discoverydate`accountid`accountname`region`dhcpoptionsid`dhcpconfigurations";
        FileGenerator.generateJson(fileInfoMap, fieldNames, "aws-dhcpoption.data", keys);

        fieldNames = "dhcpOptionsId`tags.key`tags.value";
        keys = "discoverydate`accountid`accountname`region`dhcpoptionsid`key`value";
        FileGenerator.generateJson(fileInfoMap, fieldNames, "aws-dhcpoption-tags.data", keys);
    }

    /**
     * Generate peering connections.
     *
     * @param fileInfoMap the peering connection map
     */
    public static void generatePeeringConnections(Map<String, List<VpcPeeringConnection>> fileInfoMap) {
        String fieldNames;
        String keys;
        fieldNames = "vpcPeeringConnectionId`status.code`expirationTime`requesterVpcInfo.ownerId`accepterVpcInfo.ownerId`requesterVpcInfo.vpcId`accepterVpcInfo.vpcId`requesterVpcInfo.cidrBlock`accepterVpcInfo.cidrBlock" +
                "`requesterVpcInfo.peeringOptions.allowDnsResolutionFromRemoteVpc`requesterVpcInfo.peeringOptions.allowEgressFromLocalClassicLinkToRemoteVpc`requesterVpcInfo.peeringOptions.allowEgressFromLocalVpcToRemoteClassicLink" +
                "`accepterVpcInfo.peeringOptions.allowDnsResolutionFromRemoteVpc`accepterVpcInfo.peeringOptions.allowEgressFromLocalClassicLinkToRemoteVpc`accepterVpcInfo.peeringOptions.allowEgressFromLocalVpcToRemoteClassicLink";
        keys = "discoverydate`accountid`accountname`region`vpcpeeringconnectionid`status`expirationtime`requestervpcownerid`acceptervpcownerid`requestervpcid`acceptervpcid`requestervpcinfocidrblock`acceptervpcinfocidrblock" +
                "`requestervpcallowdnsresolutionfromremotevpc`requestervpcallowegressfromlocalclassiclinktoremotevpc`requestervpcallowegressfromlocalvpctoremoteclassiclink" +
                "`acceptervpcallowdnsresolutionfromremotevpc`acceptervpcallowegressfromlocalclassiclinktoremotevpc`acceptervpcallowegressfromlocalvpctoremoteclassiclink";
        FileGenerator.generateJson(fileInfoMap, fieldNames, "aws-peeringconnection.data", keys);

        fieldNames = "vpcPeeringConnectionId`tags.key`tags.value";
        keys = "discoverydate`accountid`accountname`region`vpcpeeringconnectionid`key`value";
        FileGenerator.generateJson(fileInfoMap, fieldNames, "aws-peeringconnection-tags.data", keys);
    }

    /**
     * Generate customer gateway.
     *
     * @param fileInfoMap the customer gateway map
     */
    public static void generateCustomerGateway(Map<String, List<CustomerGateway>> fileInfoMap) {
        String fieldNames;
        String keys;
        fieldNames = "customerGatewayId`bgpAsn`ipAddress`state`type";
        keys = "discoverydate`accountid`accountname`region`customergatewayid`bgpasn`ipaddress`state`type";
        FileGenerator.generateJson(fileInfoMap, fieldNames, "aws-customergateway.data", keys);

        fieldNames = "customerGatewayId`tags.key`tags.value";
        keys = "discoverydate`accountid`accountname`region`customergatewayid`key`value";
        FileGenerator.generateJson(fileInfoMap, fieldNames, "aws-customergateway-tags.data", keys);
    }

    /**
     * Generate vpn connection.
     *
     * @param fileInfoMap the vpn connection map
     */
    public static void generateVpnConnection(Map<String, List<VpnConnection>> fileInfoMap) {
        String fieldNames;
        String keys;
        fieldNames = "vpnConnectionId`vpnGatewayId`customerGatewayId`state`category`type`options.staticRoutesOnly";
        keys = "discoverydate`accountid`accountname`region`vpnconnectionid`vpngatewayid`customergatewayid`state`category`type`optionsstaticroutesonly";
        FileGenerator.generateJson(fileInfoMap, fieldNames, "aws-vpnconnection.data", keys);

        fieldNames = "vpnConnectionId`routes.source`routes.state`routes.destinationCidrBlock";
        keys = "discoverydate`accountid`accountname`region`vpnconnectionid`routessource`routesstate`routesdestinationcidrblock";
        FileGenerator.generateJson(fileInfoMap, fieldNames, "aws-vpnconnection-routes.data", keys);

        fieldNames = "vpnConnectionId`vgwTelemetry.acceptedRouteCount`vgwTelemetry.outsideIpAddress`vgwTelemetry.lastStatusChange`vgwTelemetry.status`vgwTelemetry.statusMessage";
        keys = "discoverydate`accountid`accountname`region`vpnconnectionid`acceptedroutecount`outsideipaddress`laststatuschange`status`statusmessage";
        FileGenerator.generateJson(fileInfoMap, fieldNames, "aws-vpnconnection-telemetry.data", keys);

        fieldNames = "vpnConnectionId`tags.key`tags.value";
        keys = "discoverydate`accountid`accountname`region`vpnconnectionid`key`value";
        FileGenerator.generateJson(fileInfoMap, fieldNames, "aws-vpnconnection-tags.data", keys);
    }

    /**
     * Generate direct connection.
     *
     * @param fileInfoMap the direct connection map
     */
    public static void generateDirectConnection(Map<String, List<Connection>> fileInfoMap) {
        String fieldNames;
        String keys;
        fieldNames = "connectionId`connectionName`ownerAccount`connectionState`location`bandwidth`vlan`partnerName`loaIssueTime`lagId`awsDevice";
        keys = "discoverydate`accountid`accountname`region`connectionid`connectionname`owneraccount`connectionstate`location`bandwidth`vlan`partnername`loaissuetime`lagid`awsdevice";
        FileGenerator.generateJson(fileInfoMap, fieldNames, "aws-directconnect.data", keys);
    }

    /**
     * Generate direct connection virtual interfaces.
     *
     * @param fileInfoMap the direct connection virtual interfaces map
     */
    public static void generateDirectConnectionVirtualInterfaces(Map<String, List<VirtualInterface>> fileInfoMap) {
        String fieldNames;
        String keys;
        fieldNames = "virtualInterfaceId`ownerAccount`connectionId`location`virtualInterfaceType`virtualInterfaceName"
                + "`vlan`asn`amazonSideAsn`authKey`amazonAddress`customerAddress`addressFamily`virtualInterfaceState"
                + "`customerRouterConfig`virtualGatewayId`directConnectGatewayId`routeFilterPrefixes.cidr"
                + "`bgpPeers.asn`bgpPeers.authKey`bgpPeers.addressFamily`bgpPeers.amazonAddress`bgpPeers.customerAddress`bgpPeers.bgpPeerState`bgpPeers.bgpStatus";
        keys = "discoverydate`accountid`accountname`region`virtualinterfaceid`owneraccount`connectionid`location`virtualinterfacetype`virtualinterfacename"
                + "`vlan`asn`amazonsideasn`authkey`amazonaddress`customeraddress`addressfamily`virtualinterfacestate"
                + "`customerrouterconfig`virtualgatewayid`directconnectgatewayid`routefilterprefixescidr"
                + "`bgppeersasn`bgppeersauthkey`bgppeersaddressfamily`bgppeersamazonaddress`bgppeerscustomeraddress`bgppeersbgppeerstate`bgppeersbgpstatus";
        FileGenerator.generateJson(fileInfoMap, fieldNames, "aws-virtualinterface.data", keys);
    }

    /**
     * Generate ES domain.
     *
     * @param fileInfoMap the es domain map
     */
    public static void generateESDomain(Map<String, List<ElasticsearchDomainVH>> fileInfoMap) {
        String fieldNames;
        String keys;
        fieldNames = "domain.domainId`domain.domainName`domain.aRN`domain.created`domain.deleted`domain.endpoint`domain.processing`domain.elasticsearchVersion`domain.accessPolicies`domain.endpoints"
                + "`domain.elasticsearchClusterConfig.instanceType`domain.elasticsearchClusterConfig.instanceCount`domain.elasticsearchClusterConfig.dedicatedMasterEnabled`domain.elasticsearchClusterConfig.zoneAwarenessEnabled"
                + "`domain.elasticsearchClusterConfig.dedicatedMasterType`domain.elasticsearchClusterConfig.dedicatedMasterCount`domain.vPCOptions.vPCId`domain.vPCOptions.subnetIds`domain.vPCOptions.availabilityZones"
                + "`domain.vPCOptions.securityGroupIds`domain.advancedOptions`domain.encryptionAtRestOptions.enabled`domain.encryptionAtRestOptions.kmsKeyId`domain.nodeToNodeEncryptionOptions.enabled";
        keys = "discoverydate`accountid`accountname`region`domainid`domainname`arn`created`deleted`endpoint`processing`elasticsearchversion`accesspolicies`endpoints"
                + "`clusterinstancetype`clusterinstancecount`clusterdedicatedmasterenabled`clusterzoneawarenessenabled"
                + "`clusterdedicatedmastertype`clusterdedicatedmastercount`vpcid`subnetid`availabilityzone`securitygroupid`advancedoptions`encryptionenabled`encryptionkmskey`nodetonodeencryption";
        FileGenerator.generateJson(fileInfoMap, fieldNames, "aws-elasticsearch.data", keys);

        fieldNames = "domain.domainId`tags.key`tags.value";
        keys = "discoverydate`accountid`accountname`region`domainid`key`value";
        FileGenerator.generateJson(fileInfoMap, fieldNames, "aws-elasticsearch-tags.data", keys);
    }

    /**
     * Generate reserved instances.
     *
     * @param fileInfoMap the reserved instances map
     */
    public static void generateReservedInstances(Map<String, List<ReservedInstances>> fileInfoMap) {
        String fieldNames;
        String keys;
        fieldNames = "reservedInstancesId`instanceType`availabilityZone`duration`start`end`fixedPrice`instanceCount`productDescription`state`usagePrice`currencyCode"
                + "`instanceTenancy`offeringClass`offeringType`scope`recurringCharges.frequency`recurringCharges.amount";
        keys = "discoverydate`accountid`accountname`region`instanceid`instancetype`availabilityzone`duration`startdate`enddate`fixedprice`instancecount`productdescription`state`usageprice`currencycode"
                + "`instancetenancy`offeringclass`offeringtype`scope`recurringchargesfrequency`recurringchargesamount";
        FileGenerator.generateJson(fileInfoMap, fieldNames, "aws-reservedinstance.data", keys);

        fieldNames = "reservedInstancesId`tags.key`tags.value";
        keys = "discoverydate`accountid`accountname`region`instanceid`key`value";
        FileGenerator.generateJson(fileInfoMap, fieldNames, "aws-reservedinstance-tags.data", keys);
    }

    /**
     * Generate ssm files.
     *
     * @param fileInfoMap the ssm map
     */
    public static void generateSsmFiles(Map<String, List<InstanceInformation>> fileInfoMap) {
        String fieldNames;
        String keys;
        fieldNames = "instanceId`pingStatus`lastPingDateTime`agentVersion`isLatestVersion`platformType`platformName`platformVersion`activationId`iamRole`registrationDate`resourceType`name`iPAddress`computerName`associationStatus`lastAssociationExecutionDate`lastSuccessfulAssociationExecutionDate";
        keys = "discoverydate`accountid`accountname`region`instanceid`pingstatus`lastpingdatetime`agentversion`islatestversion`platformtype`platformname`platformversion`activationid`iamrole`registrationdate`"
                + "resourcetype`name`ipaddress`computername`associationstatus`lastassociationexecutiondate`lastsuccessfulassociationexecutiondate";
        FileGenerator.generateJson(fileInfoMap, fieldNames, "aws-ec2-ssminfo.data", keys);
    }

    /**
     * Generate elasti cache files.
     *
     * @param fileInfoMap the elasti cache map
     */
    public static void generateElastiCacheFiles(Map<String, List<ElastiCacheVH>> fileInfoMap) {
        String fieldNames;
        String keys;
        fieldNames = "arn`clusterName`description`noOfNodes`primaryOrConfigEndpoint`availabilityZones`cluster.cacheNodeType`cluster.engine`cluster.engineVersion`cluster.cacheClusterStatus"
                + "`cluster.cacheClusterCreateTime`cluster.preferredMaintenanceWindow`cluster.cacheSubnetGroupName`cluster.autoMinorVersionUpgrade`cluster.replicationGroupId`cluster.snapshotRetentionLimit`cluster.snapshotWindow`cluster.authTokenEnabled"
                + "`cluster.transitEncryptionEnabled`cluster.atRestEncryptionEnabled`cluster.notificationConfiguration.topicArn`cluster.notificationConfiguration.topicStatus"
                + "`securityGroups`parameterGroup`vpc`subnets";
        keys = "discoverydate`accountid`accountname`region`arn`clustername`description`noofnodes`primaryorconfigendpoint`availabilityzones`nodetype`engine`engineversion`clusterstatus"
                + "`clustercreatetime`preferredmaintenancewindow`subnetgroupname`autominorversionupgrade`replicationgroupid`snapshotretentionlimit`snapshotwindow`authtokenenabled"
                + "`transitencryptionenabled`atrestencryptionenabled`notificationconfigtopicarn`notificationconfigtopicstatus"
                + "`securitygroups`parametergroup`vpc`subnets";
        FileGenerator.generateJson(fileInfoMap, fieldNames, "aws-elasticache.data", keys);

        fieldNames = "arn`clusterName`tags.key`tags.value";
        keys = "discoverydate`accountid`accountname`region`arn`clustername`key`value";
        FileGenerator.generateJson(fileInfoMap, fieldNames, "aws-elasticache-tags.data", keys);

        fieldNames = "arn`clusterName`nodes.nodeName`nodes.node.cacheNodeStatus`nodes.node.cacheNodeCreateTime`nodes.node.parameterGroupStatus"
                + "`nodes.node.endpoint.address`nodes.node.endpoint.port`nodes.node.customerAvailabilityZone`nodes.tags";
        keys = "discoverydate`accountid`accountname`region`arn`clustername`nodeName`status`createdOn`parameterGroupStatus`endPointAddress`endPointPort`availabilityZone`tagStr";
        FileGenerator.generateJson(fileInfoMap, fieldNames, "aws-elasticache-nodes.data", keys);

    }

    public static void generateKinesisDataStreamFiles(Map<String, List<DataStreamVH>> fileInfoMap) {
        String fieldNames;
        String keys;
        fieldNames = "streamDescription.streamARN`streamDescription.streamName`streamDescription.streamStatus`streamDescription.retentionPeriodHours`streamDescription.streamCreationTimestamp`streamDescription.encryptionType`streamDescription.keyId"
                + "`streamDescription.enhancedMonitoring.shardLevelMetrics`streamDescription.shards.shardId`streamDescription.shards.parentShardId`streamDescription.shards.adjacentParentShardId`streamDescription.shards.hashKeyRange.startingHashKey`streamDescription.shards.hashKeyRange.endingHashKey"
                + "`streamDescription.shards.sequenceNumberRange.startingSequenceNumber`streamDescription.shards.sequenceNumberRange.endingSequenceNumber";
        keys = "discoverydate`accountid`accountname`region`streamarn`streamname`streamstatus`retentionperiodhours`streamcreationtimestamp`encryptiontype`keyid"
                + "`enhancedmonitoringshardlevelmetrics`shardid`parentshardid`adjacentparentshardid`startinghashkey`endinghashkey"
                + "`startingsequencenumber`endingsequencenumber";
        FileGenerator.generateJson(fileInfoMap, fieldNames, "aws-datastream.data", keys);

        fieldNames = "streamDescription.streamARN`tags.key`tags.value";
        keys = "discoverydate`accountid`accountname`region`streamarn`key`value";
        FileGenerator.generateJson(fileInfoMap, fieldNames, "aws-datastream-tags.data", keys);
    }

    public static void generateSQSFiles(Map<String, List<SQSVH>> fileInfoMap) {
        String fieldNames;
        String keys;
        fieldNames = "queueUrl`sqs.QueueArn`sqs.Policy`sqs.ApproximateNumberOfMessagesDelayed`sqs.ReceiveMessageWaitTimeSeconds`sqs.CreatedTimestamp`sqs.DelaySeconds`sqs.MessageRetentionPeriod`sqs.MaximumMessageSize"
                + "`sqs.VisibilityTimeout`sqs.ApproximateNumberOfMessages`sqs.ApproximateNumberOfMessagesNotVisible`sqs.LastModifiedTimestamp`sqs.KmsMasterKeyId`sqs.KmsDataKeyReusePeriodSeconds"
                + "`sqs.FifoQueue`sqs.ContentBasedDeduplication`sqs.RedrivePolicy";
        keys = "discoverydate`accountid`accountname`region`queueurl`queuearn`policy`approximatenumberofmessagesdelayed`receivemessagewaittimeseconds`createdtimestamp`delayseconds`messageretentionperiod`maximummessagesize"
                + "`visibilitytimeout`approximatenumberofmessages`approximatenumberofmessagesnotvisible`lastmodifiedtimestamp`kmsmasterkeyid`kmsdatakeyreuseperiodseconds"
                + "`fifoqueue`contentbaseddeduplication`redrivepolicy";
        FileGenerator.generateJson(fileInfoMap, fieldNames, "aws-sqs.data", keys);

        fieldNames = "sqs.QueueArn`tags.key`tags.value";
        keys = "discoverydate`accountid`accountname`region`queuearn`key`value";
        FileGenerator.generateJson(fileInfoMap, fieldNames, "aws-sqs-tags.data", keys);
    }

    public static void generateKinesisDeliveryStreamFiles(Map<String, List<DeliveryStreamVH>> fileInfoMap) {
        String fieldNames;
        String keys;
        fieldNames = "deliveryStreamDescription.deliveryStreamARN`deliveryStreamDescription.deliveryStreamName`deliveryStreamDescription.deliveryStreamStatus`deliveryStreamDescription.deliveryStreamType`deliveryStreamDescription.versionId"
                + "`deliveryStreamDescription.createTimestamp`deliveryStreamDescription.lastUpdateTimestamp`deliveryStreamDescription.source.kinesisStreamSourceDescription`destinationDescription";
        keys = "discoverydate`accountid`accountname`region`deliverystreamarn`deliverystreamname`deliverystreamstatus`deliverystreamtype`versionid"
                + "`createtimestamp`lastupdatetimestamp`sourcedescription`destinationdescription";
        FileGenerator.generateJson(fileInfoMap, fieldNames, "aws-deliverystream.data", keys);

        fieldNames = "deliveryStreamDescription.deliveryStreamARN`tags.key`tags.value";
        keys = "discoverydate`accountid`accountname`region`deliverystreamarn`key`value";
        FileGenerator.generateJson(fileInfoMap, fieldNames, "aws-deliverystream-tags.data", keys);
    }

    public static void generateKinesisVideoStreamFiles(Map<String, List<VideoStreamVH>> fileInfoMap) {
        String fieldNames;
        String keys;
        fieldNames = "streamInfo.streamARN`streamInfo.deviceName`streamInfo.streamName`streamInfo.mediaType`streamInfo.kmsKeyId"
                + "`streamInfo.version`streamInfo.status`streamInfo.creationTime`streamInfo.dataRetentionInHours";
        keys = "discoverydate`accountid`accountname`region`streamarn`devicename`streamname`mediatype`kmskeyid"
                + "`version`status`creationtime`dataretentioninhours";
        FileGenerator.generateJson(fileInfoMap, fieldNames, "aws-videostream.data", keys);

        fieldNames = "streamInfo.streamARN`tags.key`tags.value";
        keys = "discoverydate`accountid`accountname`region`streamarn`key`value";
        FileGenerator.generateJson(fileInfoMap, fieldNames, "aws-videostream-tags.data", keys);
    }

    //****** Changes For Federated Rules Start ******

    /**
     * Generate ACM SSL certificate files.
     *
     * @param fileInfoMap the sslCertificate map
     */
    public static void generateACMCertificateFiles(Map<String, List<SSLCertificateVH>> fileInfoMap) {
        String fieldNames;
        String keys;
        fieldNames = "domainName`certificateARN`expiryDate`status";
        keys = "discoverydate`accountid`accountname`region`domainname`certificatearn`expirydate`status";
        FileGenerator.generateJson(fileInfoMap, fieldNames, "aws-acmcertificate.data", keys);
    }

    /**
     * Generate IAM certificate files.
     *
     * @param fileInfoMap the fileInfoMap map
     */
    public static void generateIAMCertificateFiles(Map<String, List<IAMCertificateVH>> fileInfoMap) {
        String fieldNames;
        String keys;
        fieldNames = "serverCertificateName`arn`expiryDate";
        keys = "discoverydate`accountid`accountname`region`servercertificatename`arn`expirydate";
        FileGenerator.generateJson(fileInfoMap, fieldNames, "aws-iamcertificate.data", keys);
    }

    /**
     * Generate Account files.
     *
     * @param fileInfoMap file the iamCertificate map
     */
    public static void generateAccountFiles(Map<String, List<AccountVH>> fileInfoMap) {
        String fieldNames;
        String keys;
        fieldNames = "cloudtrailName`securityTopicARN`securityTopicEndpoint";
        keys = "discoverydate`accountid`accountname`region`cloudtrailname`securitytopicarn`securitytopicendpoint";
        FileGenerator.generateJson(fileInfoMap, fieldNames, "aws-account.data", keys);
    }

    /**
     * Generate IamGroup files.
     *
     * @param fileInfoMap file the iamCertificate map
     */
    public static void generateIamGroupFiles(Map<String, List<GroupVH>> fileInfoMap) {
        String fieldNames;
        String keys;
        fieldNames = "group.groupName`group.groupID`group.arn`group.createDate`policies";
        keys = "discoverydate`accountid`accountname`region`groupname`groupid`grouparn`createdate`policies";
        FileGenerator.generateJson(fileInfoMap, fieldNames, "aws-iamgroup.data", keys);
    }

    /**
     * Generate CloudTrail files.
     *
     * @param fileInfoMap map
     */
    public static void generateCloudTrailFiles(Map<String, List<CloudTrailVH>> fileInfoMap) {
        String fieldNames;
        String keys;
        fieldNames = "trail.Name`trail.S3BucketName`trail.IncludeGlobalServiceEvents"
                + "`trail.IsMultiRegionTrail`trail.HomeRegion`trail.TrailARN`"
                + "trail.LogFileValidationEnabled`trail.HasCustomEventSelectors`logginEnabled`trail.KmsKeyId`trail.cloudWatchLogsLogGroupArn`latestCloudWatchLogsDeliveryTime";
        keys = "discoverydate`accountid`accountname`region`name`s3bucketname`includeglobalserviceevents"
                + "`ismultiregiontrail`homeregion`trailarn`logfilevalidationenabled`hascustomeventselectors`islogging`kmskeyid`cloudWatchLogsLogGroupArn`latestCloudWatchLogsDeliveryTime";
        FileGenerator.generateJson(fileInfoMap, fieldNames, "aws-cloudtrail.data", keys);

        fieldNames = "trail.TrailARN`evenSelectorList.readWriteType`evenSelectorList.includeManagementEvents`evenSelectorList.dataResourcesType"
                + "`evenSelectorList.dataResourcesValue";
        keys = "discoverydate`accountid`accountname`region`trailarn`readwritetype`includemanagementevents`datresourcestype`dataresourcesvalue";
        FileGenerator.generateJson(fileInfoMap, fieldNames, "aws-cloudtrail-eventselector.data", keys);
    }

    /**
     * Generate CloudWatch fileInfoMap files.
     *
     * @param fileInfoMap cloudwatch log and filter map
     */
    public static void generateCloudWatchLogsFiles(Map<String, List<CloudWatchLogsVH>> fileInfoMap) {
        String fieldNames;
        String keys;
        fieldNames = "logGroup.arn`logGroup.logGroupName`logGroup.creationTime`logGroup.retentionInDays`logGroup.metricFilterCount`logGroup.storedBytes";
        keys = "discoverydate`accountid`accountname`region`logarn`loggroupname`creationtime"
                + "`retentionindays`metricfiltercount`storedbytes";
        FileGenerator.generateJson(fileInfoMap, fieldNames, "aws-cloudwatchlogs.data", keys);

        fieldNames = "logGroup.arn`metricFilterVH.metricName`metricFilterVH.metricNamespace`metricFilterVH.metricValue`metricFilterVH.metricFilter.filterName`metricFilterVH.metricFilter.filterPattern";
        keys = "discoverydate`accountid`accountname`region`logarn`metricname`metricnamespace`metricvalue`filtername`filterpattern";
        FileGenerator.generateJson(fileInfoMap, fieldNames, "aws-cloudwatchlogs-metric.data", keys);
    }

    /**
     * Generate CloudWatch Alarm files.
     *
     * @param fileInfoMap log and filter map
     */
    public static void generateCloudWatchAlarm(Map<String, List<MetricAlarm>> fileInfoMap) {
        String fieldNames;
        String keys;
        fieldNames = "alarmArn`alarmName`alarmDescription`alarmConfigurationUpdatedTimestamp`actionsEnabled`metricName`namespace`datapointsToAlarm";
        keys = "discoverydate`accountid`accountname`region`alarmarn`alarmname`description`configupdatetime`actionsenabled`metricname`namespace`datapointstoalarm";
        FileGenerator.generateJson(fileInfoMap, fieldNames, "aws-cloudwatchalarm.data", keys);

    }
    //****** Changes For Federated Rules End ******


    /**
     * Generate IAM customer managed policies.
     *
     * @param fileInfoMap the customer managed policies map
     */
    public static void generateIamPolicyFiles(Map<String, List<Policy>> fileInfoMap) {
        String fieldNames;
        String keys;
        fieldNames = "policyName`policyId`arn`defaultVersionId`createDate`path`attachmentCount`permissionsBoundaryUsageCount`isAttachable`updateDate";
        keys = "discoverydate`accountid`accountname`region`policyname`policyid`policyarn`defaultversionid`createdate`path`attachmentcount`permissionsboundaryusagecount`isattachable`updatedate";
        FileGenerator.generateJson(fileInfoMap, fieldNames, "aws-iampolicies.data", keys);
    }

    /**
     * Generate Repository Files.
     *
     * @param fileInfoMap Repository Files map
     */
    public static void generateRepositoryFiles(Map<String, List<RegistryVH>> fileInfoMap) {

        String fieldNames = "";
        String keys = "";

        fieldNames = "repository.repositoryArn`repository.registryId`imageDetail.imagePushedAt`imageDetail.artifactMediaType`imageDetail.imageTags";
        keys = "discoverydate`accountid`accountname`region`repositoryArn`registryId`imagePushedAt`artifactMediaType`tags";
        FileGenerator.generateJson(fileInfoMap, fieldNames, "aws-ecr.data", keys);
    }

    /**
     * Generate Launch Template Files.
     *
     * @param fileInfoMap Launch Template map
     */
    public static void generateLaunchTemplateFiles(Map<String, List<LaunchTemplateVH>> fileInfoMap) {
        String fieldNames;
        String keys;

        fieldNames = "launchTemplateId`launchTemplateName`imageId`securityGroupIds";
        keys = "discoverydate`accountid`accountname`region`launchTemplateId`launchTemplateName`imageid`securityGroupIds";
        FileGenerator.generateJson(fileInfoMap, fieldNames, "aws-launchtemplate.data", keys);
    }
}
