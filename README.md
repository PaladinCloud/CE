[![Latest release](https://img.shields.io/badge/release-v1.0-blue)](https://github.com/PaladinCloud/CE/releases/latest)
[![Build Status](https://github.com/PaladinCloud/CE/blob/master/wiki/images/gitter.svg)](https://github.com/PaladinCloud/CE/actions?query=branch%3Amaster)
[![GitHub license](https://github.com/PaladinCloud/CE/blob/master/wiki/license_apache.svg)](https://github.com/PaladinCloud/CE/blob/master/LICENSE)
[![Gitter](https://github.com/PaladinCloud/CE/blob/master/wiki/images/chat.svg)](https://gitter.im/PaladinCloud/community)

![Paladin Cloud, Inc](https://github.com/PaladinCloud/Rev1/raw/master/wiki/images/banner_paladincloud.png)

# Introduction

Paladin Cloud is an extensible, Security-as-Code platform to help developers and security teams significantly reduce
risks in their cloud environments. The platform functions as a policy management plane across multi-cloud and enterprise
systems to protect your applications and data. The platform contains best practice security policies and performs
continuous monitoring of your cloud assets, while prioritizing security violations based on severity levels to help you
focus on the events that matter. The platform’s resource discovery capability creates an asset inventory, then security
policies are evaluated against each asset. Powerful visualization allows developers to quickly see and remediate
violations on a risk adjusted basis. An auto-fix framework provides the ability to automatically respond to policy
violations by taking predefined actions. Paladin Cloud is more than a tool to manage cloud misconfiguration, it’s a
holistic cloud security platform that can be used to do continuous monitoring and reporting for any domain.

# Extend your Coverage

Paladin Cloud's plugin-based connector architecture allows for data ingestion from multiple sources. Plugins allow you
to pull data from various cloud-based enterprise systems, such as Kubernetes management, API gateways and threat
intelligence systems in order to holistically manage cloud security. Examples include, Qualys Vulnerability Assessment
Platform, Bitbucket, TrendMicro Deep Security, Tripwire, Venafi Certificate Management, and Redhat. You can write rules
based on data collected by these plugins to get a complete picture of your cloud security posture.

# How Does It Work?

Assess -> Report -> Remediate -> Repeat

Assess -> Report -> Remediate -> Repeat
Paladin Cloud constantly assesses and monitors you cloud security posture on a near real-time basis. The platform
discovers assets, evaluates policy, creates issues for policy violations, and prioritizes remediation. If an auto-fix is
configured with the policy, those auto-fixes are executed when the resources fail the evaluation.
Policy violations cannot be closed manually, the issue has to be fixed at the inspected asset, then Paladin Cloud will
mark it closed in the next scan. Exceptions can be added to policy violations. Sticky exceptions (exception based on
resource attribute matching criteria) can be added to exempt the similar resources that may be created in future. Note,
use exceptions sparingly and only if aligned with corporate security guidelines.
Asset groups are a powerful way to visualize cloud security and compliance. Asset groups are created by defining one or
more target resource's attribute matching criteria. For example, you could create an asset group of all running assets
by defining criteria to match all EC2 instances with attribute instancestate.name=running. Any new EC2 instance launched
after the creation of an asset group will be automatically included in the group. In the Paladin Cloud UI, you can
select the scope of the portal to a specific asset group. All the data points shown in the UI will be confined to the
selected asset group. It is common practice to create an asset groups per account (or subscription, project), per
application, per business unit or per environment.
Asset groups are not for just setting the scope of the data shown in the UI. The groups can be used to scope rule
execution as well. Policies contain 1 or more rules. These rules can be configured to run against all resources or a
specific asset group. The rules will evaluate all resources in the asset group configured as the scope for the rule.
This provides an opportunity to write policies which are very specific to an application or Org. A good example is, some
of the teams would like to enforce additional tagging standards beyond the global requirements. They implement this
policy with their custom rules and configure that to run only on their assets.

# Paladin Cloud Key Capabilities

* Continuous asset discovery
* Continuous security policy evaluation
* Detailed reporting
* Auto-Fix for policy violations
* Ability to search all discovered resources
* Simplified policy violation tracking and prioritization
* Easy to use Self-Service portal
* Custom policies and custom auto-fix actions
* Dynamic asset grouping to view compliance
* Ability to create multiple compliance domains
* Exception management
* Email digests
* Supports unlimited AWS, Azure, and GCP accounts
* Completely automated installer
* OAuth2 Support
* Azure AD integration for login
* Role-based access control

# Technology Stack

* Front End - Angular
* Backend End APIs, Jobs, Rules - Java
* Installer - Python and Terraform

# Deployment Stack

* AWS ECS & ECR - For hosting UI and APIs
* AWS Batch - For rules and resource collection jobs
* AWS CloudWatch Rules - For rule trigger, scheduler
* AWS Elastic Search - Primary data store used by the web application
* AWS RDS - For admin CRUD functionalities
* AWS S3 - For storing inventory files and persistent storage of historical data
* AWS Lambda - policy execution

Paladin Cloud installer automatically launches all of these services and configures them. A
typical [installation](https://github.com/PaladinCloud/Rev1/wiki/Installation) takes about 20 minutes.

# Paladin Cloud User Interface

**insert Overview Screen**

**insert Violation List View**

**insert Violation Trend View by category**

**insert Asset Dashboard**

**insert Asset Detail View**

**insert Recommendations**

**insert Search results**

**insert Category Summary**

**insert Asset Group Selection**

# Installation

Detailed installation instructions are available [here](https://github.com/PaladinCloud/CE/wiki/Installation)

# Usage

The installer will launch required AWS services listed in
the [installation instructions](https://github.com/PaladinCloud/CE/wiki/Installation). After successful installation hit
the UI load balancer URL. Log into the application using the credentials supplied during the installation. The results
from the policy evaluation will start getting populated within 30 minutes. Trend line widgets will be populated when
there are at least two data points.

When you install Paladin Cloud, the AWS account where you install is the **source** account. Paladin Cloud can then
monitor other **target** AWS accounts. Refer to the
instructions [here](https://github.com/PaladinCloud/CE/wiki/Installation) to add new accounts to Paladin Cloud. By
default the **source** account will be monitored by Paladin Cloud.

Login as Admin user and go to the Admin page from the top menu. In the Admin section, you can

* Create/Manage Policies
* Create/Manage Rules and associate Rules with Policies
* Create/Manage Asset Groups
* Create/Manage Sticky Exception
* Manage Jobs
* Create/Manage Access Roles
* Manage Paladin cloud Configurations
* See detailed instruction with screenshots on how to use the admin
  feature [here](https://github.com/paladincloud/rev1/wiki/Admin-Features)

## User Guide / Wiki

Wiki is [here](https://github.com/PaladinCloud/CE/wiki).

# License

Paladin Cloud is a derivative of [T-Mobile's PacBot project](https://github.com/tmobile/pacbot). Paladin Cloud is
open-sourced under the terms of section 7 of the Apache 2.0 license and is released AS-IS WITHOUT WARRANTIES OR
CONDITIONS OF ANY KIND.
