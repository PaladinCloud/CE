# Paladin Installation
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
    instances) based on the volume and specific resource requirements of the batch jobs submitted. The number of assets
    monitored will influence the quantity and type of EC2 instances created.
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
application. This will also build the application from the source code. The built JARs and Angular app are then deployed
in AWS ECS.

* [Prerequisites](https://github.com/PaladinCloud/CE/wiki/Installation#prerequisites)
* [Install](https://github.com/PaladinCloud/CE/wiki/Installation#install-and-deploy-paladin)
* [Limitations](https://github.com/PaladinCloud/CE/wiki/Installation#limitations)
## Prerequisites

Paladin installer is developed using Python and Terraform. For the installer to run, you will need to have below listed
dependencies installed correctly.

* Software Dependencies:
  1. Python supported version is 3.7 (There is an open issue with 3.9 and we will fix in next release)
  2. Following python packages are required.

  - docker-py (1.10)]
  - python-terraform (0.10)
  - boto3 (1.9)
  - gitpython

  3. Install the latest version of Terraform from https://learn.hashicorp.com/terraform/getting-started/install.html
  4. Install `node` version >=8.15.0 and <= 14.x.x
  5. Install `npm` version 6.4.1 or higher
  6. Install the following npm packages

  - Install `Angular-CLI` version 7.1.4 or higher
  - Install `yarn`

  7. Install `java` version openjdk1.8 or higher
  8. Install `mvn`(Maven) version 3.0 or higher
  9. Install `docker` version 18.06 or higher
  10. Install `MySQL` version 15.1 or higher
  11. Install Git

* AWS IAM Permission Installer would need an IAM account to launch and configure the AWS resources. To keep it simple
  you can create an IAM account with full access to above listed AWS service or temporarily assign
  Poweruser/Administrator permission. After the installation, you can remove the IAM account.

* Make sure that docker service is running during the installation time.

* The installer box or machine from where the installation is happening should be on the same VPC or should be able to
  connect to MySQL DB

* you can also install the dependencies using the provision.sh script.

  Run the shell script from the installer directory:
```
   chmod +x provison.sh
   ./provison.sh
```
## System Setup To Run Installer
1. Installer System:
```
    Recommended to use Amazon Linux / CentOS 7 / Ubuntu
```
2. System Configurations:

```
    Recommended instance type: t2.large (Minimum 8GB memory and 20GB disk space) or more
    VPC: Same as where Paladin Cloud is desired to be installed. This is required for the installer script to connect to MySQL DB
```
3. Install Git

```
    sudo yum install git
```

4. Install Pip & required modules (from the install directory under the downloaded code)
```
    sudo yum install -y epel-release python3-pip
    sudo pip3 install -r requirements.txt
```

5. Install other dependencies
```
     sudo yum -y install java-1.8.0-openjdk docker maven unzip mysql
     sudo systemctl start docker
```

6. To install terraform, download the latest version
```
     wget https://releases.hashicorp.com/terraform/0.11.15/terraform_0.11.15_linux_amd64.zip
     unzip terraform_0.11.15_linux_amd64.zip
     sudo mv terraform /usr/bin/
```

7. To install Node 14.X (instructions for Amazon Linux 2)
```
     sudo yum -y install curl
     curl -sL https://rpm.nodesource.com/setup_14.x | sudo -E bash -
     sudo yum install -y nodejs
```

8. To install yarn

```
     sudo npm install -g yarn
```

9. To install UI build dependencies, please
   click [here](https://github.com/PaladinCloud/CE/wiki/How-to-Dev-and-Build-the-UI)
## Install and Deploy Paladin

1. Navigate to releases and download the latest stable source code as a zip file
   from [here](https://github.com/PaladinCloud/CE/releases/latest)
2. Unzip the archive in a directory.
3. Go to the installer directory, once the zip file is extracted

4. Create settings/local.py file by copying from settings/default.local.py

5. Update settings/local.py file with the required values - Mandatory Changes
```
   VPC ID
   VPC CIDR
   SUBNET IDS (2 Subnets are required. Both the subnets should not be in the same AZ.)
```

6. Review other values in the local.py and update them, if needed.

7. Run the installer. (Go grab a coffee now :), it would take a while to provision the AWS resources)
```
    sudo python3 manager.py install
```

8. Installation logs will be available in the log directory

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

9. In case of any failures, please check the troubleshooting
   steps [here](https://github.com/PaladinCloud/CE/wiki/Installation#troubleshooting).

## Redeploy

Use this process if you want to update without changing your endpoints and URL AND don't want to lose your existing
data. Please follow the below steps to redeploy Paladin

* Go to PaldinCloud/Rev1 source code and pull the latest changes

```
    git pull --rebase
```

* Go to paladin-installer directory

1. Run the below command to redeploy the application
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

### Learn more about...<br/>
[How to Connect to an AWS Account](https://github.com/PaladinCloud/CE/wiki/How-to-Add-an-AWS-Account)<br/>
[How to Connect to an Azure Account](https://github.com/PaladinCloud/CE/wiki/How-to-Add-an-Azure-Account)<br/>
[How to Connect to a  GCP Account](https://github.com/PaladinCloud/CE/wiki/How-to-Add-a-GCP-Account)<br/>

## How to scale up Paladin infrastructure ?

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
### changing the scheduler interval for running the jobs and rules
* To change the scheduler interval, follow the below steps:
  1. Go to local.py file
  2. Set JOB_SCHEDULER_INTERVAL_IN_HOURS to the required interval in hours.
  3. JOB_SCHEDULER_INTERVAL_IN_HOURS = **6**
  4. Default value is **6** hours.

### Limitations:

AWS Fargate is not available in all AWS regions. Please visit AWS Region Table for more information on AWS regions and
services.
