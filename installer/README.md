> # Paladin Installation (on AWS cloud)
## Overview

This page describes the steps to install Paladin Cloud. Paladin is built to be deployed in AWS mostly using managed
services. There are 3 major components in Paladin.
```
Paladin Rule Engine                  : CloudWatch Rules, Lambda, AWS Batch, AWS ElasticSearch

Paladin Web Application (UI & APIs)  : AWS ECS, Fargate, AWS ElasticSearch, RDS

Paladin Inventory Collector          : Cloudwatch Rules, AWS Batch, AWS ElasticSearch, RDS
```
## List of AWS resources that will be created by the installer.
* IAM Roles
* IAM Policies
* S3 Bucket
* RDS
  * MySQL 5.7.X
* Elasticsearch Service
  * Elasticsearch version 5.5
* Batch
    * Compute environments, Job Definitions and Job Queues
    * AWS Batch dynamically provisions the optimal quantity and type of compute resources (e.g., CPU or memory optimized
      instances) based on the volume and specific resource requirements of the batch jobs submitted. The number of
      assets monitored will influence the quantity and type of EC2 instances created.
* Elastic Container Registry
  * Repositories - for batch job, API and UI
* Elastic Container Service - [AWS Fargate](https://aws.amazon.com/fargate/)
  * Clusters - for APIs, UI and Batch
  * Task Definitions - for APIs and UI
* Lambda Functions
  * SubmitBatchJob and SubmitRuleJob
* CloudWatch Rules
## Steps to Install

The python installer script will launch the above listed AWS resources and configure them as required for the Paladin
Cloud application. This will also build the application from the source code. The built JARs and Angular app are then
deployed in AWS ECS.
**Please review the release notes on the release [page](https://github.com/PaladinCloud/CE/releases/latest) before
starting the installation**.

* [Prerequisites](https://github.com/PaladinCloud/CE/wiki/Installation#prerequisites)
* [Install](https://github.com/PaladinCloud/CE/wiki/Installation#install-and-deploy-paladin)
* [Limitations](https://github.com/PaladinCloud/CE/wiki/Installation#limitations)
## Prerequisites

Paladin installer is developed using Python and Terraform. You need an EC2 instance to start the installation.
We have a public AMI **PaladinCloud-Installer** with all the required prerequisites installed.
You can use the same to launch the installer machine. Please use the below configuration:

```
    Recommended instance type: t2.large (Minimum 8GB memory and 20GB disk space) or more
    VPC: Same as where Paladin Cloud is desired to be installed. 
    **This is required for the installer script to connect to MySQL DB**
```

* if the AMI, **PaladinCloud-Installer** is not available in your region, please copy the AMI from **us-east-1** region
  before launching the machine.

* If you are not using the AMI, please make sure you
  install [these](https://github.com/PaladinCloud/CE/wiki/Prerequisities) dependencies correctly.

* AWS IAM Permission Installer would need an IAM account to launch and configure the AWS resources. To keep it simple
  you can create an IAM account with full access to above listed AWS service or temporarily assign
  Poweruser/Administrator permission. After the installation, you can remove the IAM account.

* Make sure that docker service is running during the installation time.

* The installer box or machine from where the installation is happening should be on the same VPC or should be able to
  connect to MySQL DB

## Install and Deploy Paladin

1. SSH into the installer EC2 machine.

2. Navigate to releases and copy the latest released(tag)
   number [here](https://github.com/PaladinCloud/CE/releases/latest)

3. Fetch the tags:
   ```
   git fetch --tags
   ```
4. Checkout to the tag number copied in step no 1:
   ```
   git checkout <tag no>
   ```
   E.g:
   ```
   git checkout 1.2.0
   ```
5. Create settings/local.py file by copying from settings/default.local.py

6. Update settings/local.py file with the required values - Mandatory Changes
    ```
   VPC ID
   VPC CIDR
   SUBNET IDS (2 Subnets are required. Both the subnets should not be in the same AZ.)
    ```
   Also, specify the mandatory tags as per your organization policy in the local.py. So that all the installation
   resources will be tagged accordingly.
7. By default, Paladin Cloud application is not accessible from outside VPC. If you need to make it publicly
   accessible (SECURITY RISK) please follow your organization's policy to expose a website to the Internet.

8. If you want to monitor Azure or GCP, please review the
   documentation [here](https://github.com/PaladinCloud/CE/wiki/Installation#monitoring-other-cloud-providers).

9. Review other values in the local.py and update them, if needed.

10. Run the installer. (Go grab a coffee now :), it would take a while to provision the AWS resources)
     ```
     sudo python3 manager.py install
     ```
11. Installation logs will be available in the log directory
    ```
    tail -f log/debug.log -> To see the debug log
    tail -f log/error.log -> To see the error log
    tail -f log/terraform_install.log -> To see Terraform Installation log
    tail -f log/terraform_destroy.log -> To see Terraform Destroy log
    ```
    **Once the installation is complete, go to the Paladin ELB URL to access the web application. Use the default
    credentials**

    Admin User : admin@paladincloud.io / PaladinAdmin!!<br/>
    Readonly User : user@paladincloud.io / PaladinUser!!

12. In case of any failures, please check the troubleshooting
    steps [here](https://github.com/PaladinCloud/CE/wiki/Installation#troubleshooting).

## Redeploy

**If you are planning to upgrade to release 1.2.0, please follow
this [link](https://github.com/PaladinCloud/CE/wiki/Upgrading-to-Release-1.2.0). For versions other than 1.2.0, please
follow the below steps.**

Use this process if you want to update the Paladin Cloud version without changing your endpoints and URL AND don't want
to lose your existing data. Please follow the below steps to redeploy Paladin

### If you have installed previously using a downloaded zip file, please follow the below steps:

* Create a new directory for the Paladin Cloud installation process. This would remain as an installation/Redeploy
  directory going forward.
* Clone the repo and fetch tags
   ```
   git clone https://github.com/PaladinCloud/CE.git
   git fetch --tags
   ```
* Navigate to the Release page and copy the latest release number
* Checkout to the tag
   ```
   git checkout <tag#>
   ```
* Either get the installer/data contents from the previous installation directory or from the Paladin Cloud S3 bucket.
  Terraform backup file name is **paladincloud-terraform-installer-backup.zip**.
* Replace the /installer/data directory in the current installation folder with the contents from the above step.
* Run the below command to redeploy the application
   ```
     sudo python3 manager.py redeploy
   ```

### If you have installed it by cloning the git repo already, you can follow the below steps to redeploy:

* Navigate to the Release page and copy the latest release number
* Run the below commands, from the same installation directory:
  ```
  git pull
  git fetch --tags
  git checkout <latest tag#>
  ```
* Run the below command to redeploy the application
   ```
       sudo python3 manager.py redeploy
   ```

## Uninstall

This process will terminate all the AWS resources created during installation.

```
    sudo python3 manager.py destroy
```

## Troubleshooting

* Verify Permission required by the installer is correct.
* Verify Dependencies are installed properly
* Please look at the common causes of the failures in the FAQs
  document [here](https://github.com/PaladinCloud/CE/wiki/Installation-FAQs).
* If you are still having problems contact CommunitySupport@PaladinCloud.io or raise an
  issue [here](https://github.com/PaladinCloud/CE/issues/new/choose).

## How to setup SSL

You can secure Paladin by enabling SSL. To enable this you have to follow the below steps Configure the below variables
in local.py

* Set **ALB_PROTOCOL** to **HTTPS**
* Set **SSL_CERTIFICATE_ARN** with the ARN obtained from ACM
* Set **PALADINCLOUD_DOMAIN** if you have any else remove it.

Example 1: With self signed AWS internal URL
```
  ALB_PROTOCOL = "HTTPS" 
  SSL_CERTIFICATE_ARN = "arn:aws:acm:us-east-1:account_id:certificate/54d327ce-5f17-4a3a-9cb3-77dc10fa8371"
  PALADINCLOUD_DOMAIN = ""
```

Example 2: With external domain
```
  ALB_PROTOCOL = "HTTPS" 
  SSL_CERTIFICATE_ARN = "arn:aws:acm:us-east-1:account_id:certificate/54d327ce-5f17-4a3a-9cb3-77dc10fa8371"
  PALADINCLOUD_DOMAIN = "paladincloud.com"
```

After making the above changes, you can run install command if you are installing for first time or redeploy command if
you already installed Paladin

## Configure SSL with AWS internal URL
If you don't have any domain for Paladin and want to use AWS internal URL with https, follow the below steps

1. Create a self signed certificate for the internal URL
2. Upload it in AWS ACM (Certificate Manager) and copy ARN of that certificate
3. Update local.py to have the ALB_PROTOCOL=HTTPS and SSL_CERTIFICATE_ARN=< copied-arn-value >
4. Run sudo python manager.py install if you are installing for first time or sudo python manager.py redeploy if you
   already installed Paladin
5. Refer Example 1

## Configure SSL for specific domain

1. Create a domain for Paladin
2. Obtain SSL certificate for the domain Or Create a self signed certificate
3. Upload it in AWS ACM (Certificate Manager) and copy ARN of that certificate
4. Update local.py to have the

* **ALB_PROTOCOL**=**HTTPS**
* **SSL_CERTIFICATE_ARN**=< copied-arn-value >
* **PALADINCLOUD_DOMAIN**=< created-domain-name >

5. Run sudo python manager.py install if you are installing for first time or sudo python manager.py redeploy if you
   already installed Paladin
6. Refer Example 2

## Monitoring other Cloud Providers<br/>

Paladin Cloud can monitor multiple AWS accounts. It can also monitor your Azure or GCP clouds. For enabling these please
review the below guides.

[How to Connect to an AWS Account](https://github.com/PaladinCloud/CE/wiki/How-to-Add-an-AWS-Account)<br/>
[How to Connect to an Azure Account](https://github.com/PaladinCloud/CE/wiki/How-to-Add-an-Azure-Account)<br/>
[How to Connect to a  GCP Account](https://github.com/PaladinCloud/CE/wiki/How-to-Add-a-GCP-Account)<br/>

## How to scale up Paladin infrastructure?

Paladin is capable of monitoring thousands of AWS, Azure and GCP accounts. As you add more accounts the infrastructure
may need scaled to support the increased data volume. If you are experiencing performance issues consider below:

### Upgrade RDS instance

Paladin's default instance type for RDS-MySQL is **db.t2.medium**, consider a larger instance to improve performance. To
upgrade RDS follow the below steps:

Go to local.py file

1. Set RDS_INSTANCE_TYPE to a type you require.
2. RDS_INSTANCE_TYPE = **db.t2.large**
   ```
   Run the command, sudo python3 manager.py upgrade to upgrade the server instance type
   ```

### Upgrade Elasticsearch instance

Paladin's default instance type for Elasticsearch cluster is **m4.large.elasticsearch**, consider a larger instance to
improve performance. To upgrade Elasticsearch follow the below steps:

Go to local.py file

1. Set ES_INSTANCE_TYPE to an instance type you require.
2. ES_INSTANCE_TYPE = **m4.xlarge.elasticsearch**
   ```
   Run the command, sudo python3 manager.py upgrade to upgrade the server instance type
   ```

### Changing the scheduler interval for running the jobs and rules

* To change the scheduler interval, follow the below steps before install or redeploy process:
    1. Go to local.py file
    2. Set JOB_SCHEDULE_INTERVAL to the required interval in hours.
    3. JOB_SCHEDULE_INTERVAL = **6**
    4. Default value is **6** hours.

### Limitations:

AWS Fargate is not available in all AWS regions. Please visit AWS Region Table for more information on AWS regions and
services.