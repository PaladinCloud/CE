# Overview

This document describes the installation process of Paladin. Currently, Paladin is built to be deployed in AWS using
managed services. There are 4 major components in Paladin:

* Paladin Rule Engine
* Paladin Web Application (UI & APIs)
* Paladin Inventory Collector
* Paladin Data Management

```
Paladin Rule Engine                  : CloudWatch Rules, Lambda, AWS Batch

Paladin Web Application (UI & APIs)  : AWS ECS, Fargate

Paladin Inventory Collector          : Cloudwatch Rules, AWS Batch

Paladin Data Management              : AWS ElasticSearch, RDS, S3
```

## Prerequisites

* The Paladin installer is developed using Python and Terraform. You need an EC2 instance to start the installation. We
  have a public AMI
  PaladinCloud-Installer with all the required prerequisites installed. Refer to the table below for region-specific AMI
  ids. Please use the same to launch the installer machine.
* If the AMI, PaladinCloud-Installer is not available in your region, please copy the AMI from any of the regions (
  listed in the table below) before launching the machine.

**Note:** Please let us know if your region is not on the list. We will make it available for your region soon.

### Region-Specific AMI IDs

|Region | AMI ID |
|:----: | :---:  |
| Us-east-1(N. Virginia) | ami-08fe6537d003e100d |
| Us-east-2(Ohio) | ami-0c72761d90a289a72 |
| Us-west-1(N. California) | ami-0a27b07625c74cf3c |
| Us-west-2(Oregon) | ami-03e2babb4296bdd60 |

* Use the following configuration:

```
 Recommended instance type: t2.large (Minimum 8GB memory and 25GB disk space) or more
    VPC: Same as where Paladin Cloud is desired to be installed. 
    **This is required for the installer script to connect to MySQL DB**
```

* AWS IAM Permission Installer needs an IAM account to launch and configure the AWS resources. Create an IAM account
  with full access to the AWS services (listed below) or temporarily assign Power user/Administrator permission. After
  the installation, you may remove the IAM account. This permission/role is used
  to [select the authentication type](https://github.com/PaladinCloud/CE/wiki/How-to-Select-AWS-Authentication-Mechanism#how-to-select-aws-authentication-mechanism)
  within local.py before installation.
* Make sure that the docker service is running during the installation time.
* The installer box or machine from where the installation is happening should be on the same VPC or should be able to
  connect to MySQL DB.

**Note:** Our AMI is in AWS Linux and we continuously test our application using Amazon AWS Linux-based installer
boards. If you want any other OS flavor, please raise a
ticket [here](https://github.com/PaladinCloud/CE/issues/new/choose).

Our Installation Guide is available in two formats: Quick Installation and Standard Installation.

## Quick Installation Guide

Click [here](https://github.com/PaladinCloud/CE/wiki/Quick-Installation-Guide#quick-installation-guide) for the Quick
Installation Guide with minimal instructions for those already comfortable with performing such installations.

## Standard Installation Guide

Click [here](https://github.com/PaladinCloud/CE/wiki/Standard-Installation-Guide#standard-installation-guide) for the
Standard Installation Guide with detailed step-by-step instructions to install Paladin.

## Status

If you need to check the status after deployment, use the following command:

`Sudo python3 manager.py status`

This command gives the list of resources deployed in AWS. It also gives DB details, user, password, and ALB URL.

## Redeploy Paladin

Please use the redeploy option to make changes and rerun the Install.
Click [here](https://github.com/PaladinCloud/CE/wiki/How-to-Upgrade-Redeploy-Paladin#upgraderedeploy-paladin) for steps
to Redeploy Paladin.

## Uninstall

If you need to remove Paladin, run the following command to uninstall Paladin.

`Sudo python3 manager.py destroy`

**Note:** This command will terminate all the AWS resources created during installation.

## Troubleshooting

* Verify that the permissions required by the installer are correct.
* Verify that the Dependencies are installed properly.

* Please look at the common causes of the failures in the FAQs
  document [here](https://github.com/PaladinCloud/CE/wiki/Installation-FAQs).
* If the issues persist, contact CommunitySupport@PaladinCloud.io or raise an
  issue [here](https://github.com/PaladinCloud/CE/issues/new/choose).

## Monitoring other Cloud Providers

Paladin can monitor multiple AWS accounts. It can also monitor your Azure or GCP clouds. For enabling these, please
review the below guides.

* [How to Connect to an AWS Account](https://github.com/PaladinCloud/CE/wiki/How-to-Add-an-AWS-Account)
* [How to Connect to an Azure Account](https://github.com/PaladinCloud/CE/wiki/How-to-Add-an-Azure-Account)
* [How to Connect to a GCP Account](https://github.com/PaladinCloud/CE/wiki/How-to-Add-a-GCP-Account)

## Limitations

AWS Fargate is not available in all AWS regions. Please
visit [AWS Region Table](https://github.com/PaladinCloud/CE/wiki/Installation/_edit#region-specific-ami-ids) for more
information on AWS regions and services.

## Important Links

* [How to Setup SSL](https://github.com/PaladinCloud/CE/wiki/How-to-Setup-SSL#how-to-setup-ssl)
* [Upgrade Redeploy Paladin](https://github.com/PaladinCloud/CE/wiki/How-to-Upgrade-Redeploy-Paladin#upgraderedeploy-paladin)
* [Scale Up Paladin Infrastructure](https://github.com/PaladinCloud/CE/wiki/How-to-Scale-Paladin-Infrastructure#how-to-scale-paladin-infrastructure)
* [List of AWS Resources Created by the Installer](https://github.com/PaladinCloud/CE/wiki/List-of-AWS-Resources-Created-by-the-Installer#list-of-aws-resources-created-by-the-installer)
* [Changing the Scheduler Interval for Running the Jobs and Rules](https://github.com/PaladinCloud/CE/wiki/Changing-the-Scheduler-Interval-for-Running-the-Jobs-and-Rules#changing-the-scheduler-interval-for-running-the-jobs-and-rules)
* [How to Add and Select AWS Authentication Mechanism](https://github.com/PaladinCloud/CE/wiki/How-to-Select-AWS-Authentication-Mechanism#how-to-select-aws-authentication-mechanism)
* [Connecting to the Portal after the Installation](https://github.com/PaladinCloud/CE/wiki/Connect-to-the-Portal-after-Installation)
* [Five Main Commands of Paladin](https://github.com/PaladinCloud/CE/wiki/Five-Main-Commands-of-Paladin#commands-used-in-paladin)
