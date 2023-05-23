/*
SQLyog Ultimate v12.09 (32 bit)
MySQL - 5.6.27-log : Database - pacmandata
*********************************************************************
*/


/*!40101 SET NAMES utf8 */;

/*!40101 SET SQL_MODE=''*/;

/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;
CREATE DATABASE /*!32312 IF NOT EXISTS*/`pacmandata` /*!40100 DEFAULT CHARACTER SET utf8 COLLATE utf8_bin */;

USE `pacmandata`;

/*Table structure for table `ASGC_Issues` */

SET @region='$region';
SET @account='$account';
SET @eshost='$eshost';
SET @esport='$esport';
SET @LOGGING_ES_HOST_NAME='$LOGGING_ES_HOST_NAME';
SET @LOGGING_ES_PORT='$LOGGING_ES_PORT';
SET @ES_HOST_NAME='$ES_HOST_NAME';
SET @ES_PORT='$ES_PORT';
SET @ES_CLUSTER_NAME='$ES_CLUSTER_NAME';
SET @ES_PORT_ADMIN='$ES_PORT_ADMIN';
SET @ES_HEIMDALL_HOST_NAME='$ES_HEIMDALL_HOST_NAME';
SET @ES_HEIMDALL_PORT='$ES_HEIMDALL_PORT';
SET @ES_HEIMDALL_CLUSTER_NAME='$ES_HEIMDALL_CLUSTER_NAME';
SET @ES_HEIMDALL_PORT_ADMIN='$ES_HEIMDALL_PORT_ADMIN';
SET @ES_UPDATE_HOST='$ES_UPDATE_HOST';
SET @ES_UPDATE_PORT='$ES_UPDATE_PORT';
SET @ES_UPDATE_CLUSTER_NAME='$ES_UPDATE_CLUSTER_NAME';
SET @REDSHIFT_URL='$REDSHIFT_URL';
SET @REDSHIFT_USER_NAME='$REDSHIFT_USER_NAME';
SET @REDSHIFT_PASSWORD='$REDSHIFT_PASSWORD';
SET @PACMAN_HOST_NAME='$PACMAN_HOST_NAME';
SET @RDS_URL='$RDS_URL';
SET @RDS_USERNAME='$RDS_USERNAME';
SET @RDS_PASSWORD='$RDS_PASSWORD';
SET @CURRENT_RELEASE='$CURRENT_RELEASE';
SET @JOB_BUCKET_REGION='$JOB_BUCKET_REGION';
SET @RULE_JOB_BUCKET_NAME='$RULE_JOB_BUCKET_NAME';
SET @JOB_LAMBDA_REGION='$JOB_LAMBDA_REGION';
SET @JOB_FUNCTION_NAME='$JOB_FUNCTION_NAME';
SET @JOB_FUNCTION_ARN='$JOB_FUNCTION_ARN';
SET @RULE_BUCKET_REGION='$RULE_BUCKET_REGION';
SET @RULE_LAMBDA_REGION='$RULE_LAMBDA_REGION';
SET @RULE_FUNCTION_NAME='$RULE_FUNCTION_NAME';
SET @RULE_FUNCTION_ARN='$RULE_FUNCTION_ARN';
SET @CLOUD_INSIGHTS_TOKEN_URL='$CLOUD_INSIGHTS_TOKEN_URL';
SET @CLOUD_INSIGHTS_COST_URL='$CLOUD_INSIGHTS_COST_URL';
SET @SVC_CORP_USER_ID='$SVC_CORP_USER_ID';
SET @SVC_CORP_PASSWORD='$SVC_CORP_PASSWORD';
SET @CERTIFICATE_FEATURE_ENABLED='$CERTIFICATE_FEATURE_ENABLED';
SET @PATCHING_FEATURE_ENABLED='$PATCHING_FEATURE_ENABLED';
SET @VULNERABILITY_FEATURE_ENABLED='$VULNERABILITY_FEATURE_ENABLED';
SET @MAIL_SERVER='$MAIL_SERVER';
SET @PACMAN_S3='$PACMAN_S3';
SET @DATA_IN_DIR='$DATA_IN_DIR';
SET @CREDENTIAL_DIR='$CREDENTIAL_DIR';
SET @DATA_BKP_DIR='$DATA_BKP_DIR';
SET @PAC_ROLE='$PAC_ROLE';
SET @BASE_REGION='$BASE_REGION';
SET @DATA_IN_S3='$DATA_IN_S3';
SET @BASE_ACCOUNT='$BASE_ACCOUNT';
SET @PAC_RO_ROLE='$PAC_RO_ROLE';
SET @MAIL_SERVER_PORT='$MAIL_SERVER_PORT';
SET @MAIL_PROTOCOL='$MAIL_PROTOCOL';
SET @MAIL_SERVER_USER='$MAIL_SERVER_USER';
SET @MAIL_SERVER_PWD='$MAIL_SERVER_PWD';
SET @MAIL_SMTP_AUTH='$MAIL_SMTP_AUTH';
SET @MAIL_SMTP_SSL_ENABLE='$MAIL_SMTP_SSL_ENABLE';
SET @MAIL_SMTP_SSL_TEST_CONNECTION='$MAIL_SMTP_SSL_TEST_CONNECTION';
SET @PACMAN_LOGIN_USER_NAME='$PACMAN_LOGIN_USER_NAME';
SET @PACMAN_LOGIN_PASSWORD='$PACMAN_LOGIN_PASSWORD';
SET @CONFIG_CREDENTIALS='$CONFIG_CREDENTIALS';
SET @CONFIG_SERVICE_URL='$CONFIG_SERVICE_URL';
SET @PACBOT_AUTOFIX_RESOURCEOWNER_FALLBACK_MAILID='$PACBOT_AUTOFIX_RESOURCEOWNER_FALLBACK_MAILID';
SET @QUALYS_INFO='$QUALYS_INFO';
SET @QUALYS_API_URL='$QUALYS_API_URL';
SET @AZURE_CREDENTIALS='$AZURE_CREDENTIALS';
SET @GCP_CREDENTIALS='$GCP_CREDENTIALS';
SET @JOB_SCHEDULE_INTERVAL='$JOB_SCHEDULE_INTERVAL';
SET @JOB_SCHEDULE_INITIALDELAY='$JOB_SCHEDULE_INITIALDELAY';
SET @JOB_SCHEDULE_INITIALDELAY_SHIPPER='$JOB_SCHEDULE_INITIALDELAY_SHIPPER';
SET @JOB_SCHEDULE_INITIALDELAY_RULES='$JOB_SCHEDULE_INITIALDELAY_RULES';
SET @AZURE_EVENTBRIDGE_BUS_DETAILS='$AZURE_EVENTBRIDGE_BUS_DETAILS';
SET @GCP_EVENTBRIDGE_BUS_DETAILS='$GCP_EVENTBRIDGE_BUS_DETAILS';
SET @AWS_EVENTBRIDGE_BUS_DETAILS='$AWS_EVENTBRIDGE_BUS_DETAILS';
SET @AZURE_ENABLED='$AZURE_ENABLED';
SET @GCP_ENABLED='$GCP_ENABLED';
SET @JOB_SCHEDULER_NUMBER_OF_BATCHES='$JOB_SCHEDULER_NUMBER_OF_BATCHES';
SET @EVENT_BRIDGE_PREFIX='$EVENT_BRIDGE_PREFIX';
SET @MANDATORY_TAGS='$MANDATORY_TAGS';
SET @API_CLIENT_ID='$API_CLIENT_ID';
SET @API_SCERET_ID='$API_SCERET_ID';
SET @COGNITO_INFO='$COGNITO_INFO';
SET @ACCOUNT_ID='$ACCOUNT_ID';
SET @ACCOUNT_NAME='$ACCOUNT_NAME';
SET @ACCOUNT_PLATFORM='$ACCOUNT_PLATFORM';
SET @AQUA_API_URL='$AQUA_API_URL';
SET @AQUA_CLIENT_DOMAIN_URL='$AQUA_CLIENT_DOMAIN_URL';
SET @AQUA_USERNAME='$AQUA_USERNAME';
SET @AQUA_PASSWORD='$AQUA_PASSWORD';
SET @AQUA_API_DEFAULT_PAGE_SIZE='$AQUA_API_DEFAULT_PAGE_SIZE';
SET @AQUA_IMAGE_VULNERABILITY_QUERY_PARAMS='$AQUA_IMAGE_VULNERABILITY_QUERY_PARAMS';
SET @NOTIFICATION_FUNCTION_URL='$NOTIFICATION_FUNCTION_URL';
SET @TOPIC_ARN='$TOPIC_ARN';
SET @EMAIL_TOPIC_ARN='$EMAIL_TOPIC_ARN';
SET @NOTIFICATION_EMAIL_ID='$NOTIFICATION_EMAIL_ID';


CREATE TABLE IF NOT EXISTS `OmniSearch_Config` (
  `SEARCH_CATEGORY` varchar(100) COLLATE utf8_bin NOT NULL,
  `RESOURCE_TYPE` varchar(100) COLLATE utf8_bin NOT NULL,
  `REFINE_BY_FIELDS` varchar(100) COLLATE utf8_bin DEFAULT NULL,
  `RETURN_FIELDS` varchar(100) COLLATE utf8_bin DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_bin;

/*Table structure for table `PacmanSubscriptions` */

CREATE TABLE IF NOT EXISTS `PacmanSubscriptions` (
  `subscriptionId` bigint(75) NOT NULL AUTO_INCREMENT,
  `emailId` varchar(100) COLLATE utf8_bin DEFAULT NULL,
  `subscriptionValue` varchar(100) COLLATE utf8_bin DEFAULT NULL,
  `createdDate` datetime DEFAULT NULL,
  `modifiedDate` datetime DEFAULT NULL,
  PRIMARY KEY (`subscriptionId`)
) ENGINE=InnoDB AUTO_INCREMENT=7 DEFAULT CHARSET=utf8 COLLATE=utf8_bin;

/*Table structure for table `Pacman_Asset_Config` */

CREATE TABLE IF NOT EXISTS `Pacman_Asset_Config` (
  `resourceId` varchar(75) COLLATE utf8_bin NOT NULL,
  `configType` varchar(75) COLLATE utf8_bin NOT NULL,
  `config` text COLLATE utf8_bin,
  `createdDate` datetime NOT NULL,
  PRIMARY KEY (`resourceId`,`configType`,`createdDate`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_bin;

/*Table structure for table `cf_AssetGroupDetails` */

CREATE TABLE IF NOT EXISTS `cf_AssetGroupDetails` (
  `groupId` varchar(75) COLLATE utf8_bin NOT NULL DEFAULT '',
  `groupName` varchar(75) COLLATE utf8_bin DEFAULT NULL,
  `dataSource` varchar(75) COLLATE utf8_bin DEFAULT NULL,
  `displayName` varchar(75) COLLATE utf8_bin DEFAULT NULL,
  `groupType` varchar(75) COLLATE utf8_bin DEFAULT NULL,
  `createdBy` varchar(75) COLLATE utf8_bin DEFAULT NULL,
  `createdUser` varchar(75) COLLATE utf8_bin DEFAULT NULL,
  `createdDate` varchar(75) COLLATE utf8_bin DEFAULT NULL,
  `modifiedUser` varchar(75) COLLATE utf8_bin DEFAULT NULL,
  `modifiedDate` varchar(75) COLLATE utf8_bin DEFAULT NULL,
  `description` text COLLATE utf8_bin,
  `aliasQuery` text COLLATE utf8_bin,
  `isVisible` tinyint(1) DEFAULT NULL,
  PRIMARY KEY (`groupId`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_bin;

/*Table structure for table `cf_AssetGroupException` */

CREATE TABLE IF NOT EXISTS `cf_AssetGroupException` (
  `id_` bigint(20) NOT NULL AUTO_INCREMENT,
  `groupName` varchar(75) COLLATE utf8_bin DEFAULT NULL,
  `targetType` varchar(75) COLLATE utf8_bin DEFAULT NULL,
  `policyName` varchar(200) COLLATE utf8_bin DEFAULT NULL,
  `policyId` varchar(200) COLLATE utf8_bin DEFAULT NULL,
  `expiryDate` date DEFAULT NULL,
  `exceptionName` varchar(75) COLLATE utf8_bin DEFAULT NULL,
  `exceptionReason` varchar(2000) COLLATE utf8_bin DEFAULT NULL,
  `dataSource` varchar(75) COLLATE utf8_bin DEFAULT NULL,
  `createdBy` VARCHAR(100) NULL,
  `createdOn` DATE NULL ,
  `modifiedBy` VARCHAR(100) NULL,
  `modifiedOn` DATE NULL,
  PRIMARY KEY (`id_`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_bin;

DELIMITER $$
DROP PROCEDURE IF EXISTS alter_cf_assetGroupException_table $$
CREATE PROCEDURE alter_cf_assetGroupException_table()
BEGIN
IF NOT EXISTS( SELECT NULL
            FROM INFORMATION_SCHEMA.COLUMNS
           WHERE table_name = 'cf_AssetGroupException'
             AND table_schema = 'pacmandata'
             AND column_name = 'createdBy')  THEN
ALTER TABLE `cf_AssetGroupException`  
ADD COLUMN `createdBy` VARCHAR(100) NULL,
ADD COLUMN `createdOn` DATE NULL,
ADD COLUMN `modifiedBy` VARCHAR(100) NULL,
ADD COLUMN `modifiedOn` DATE NULL;
END IF;
END $$
DELIMITER ;
CALL alter_cf_assetGroupException_table();

/* Procedure to change column names for cf_AssetGroupException*/
DELIMITER $$
DROP PROCEDURE IF EXISTS alter_cf_AssetGroupException_table_change_column_names $$
CREATE PROCEDURE alter_cf_AssetGroupException_table_change_column_names()
BEGIN
IF EXISTS( SELECT NULL
            FROM INFORMATION_SCHEMA.COLUMNS
           WHERE table_name = 'cf_AssetGroupException'
             AND table_schema = 'pacmandata'
             AND column_name = 'ruleId')  THEN
 ALTER TABLE cf_AssetGroupException change column ruleId policyId varchar(200) NULL DEFAULT NULL;
END IF;
IF EXISTS( SELECT NULL
            FROM INFORMATION_SCHEMA.COLUMNS
           WHERE table_name = 'cf_AssetGroupException'
             AND table_schema = 'pacmandata'
             AND column_name = 'ruleName')  THEN
ALTER TABLE cf_AssetGroupException change column ruleName policyName varchar(200) NULL DEFAULT NULL;
END IF;
END $$
DELIMITER ;

CALL alter_cf_AssetGroupException_table_change_column_names();

/*Table structure for table `cf_AssetGroupOwnerDetails` */

CREATE TABLE IF NOT EXISTS `cf_AssetGroupOwnerDetails` (
  `ownerId` varchar(100) COLLATE utf8_bin NOT NULL,
  `ownnerName` varchar(100) COLLATE utf8_bin DEFAULT NULL,
  `assetGroupName` varchar(100) COLLATE utf8_bin DEFAULT NULL,
  `ownerEmailId` text COLLATE utf8_bin,
  PRIMARY KEY (`ownerId`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_bin;

/*Table structure for table `cf_AssetGroupTargetDetails` */

CREATE TABLE IF NOT EXISTS `cf_AssetGroupTargetDetails` (
  `id_` varchar(75) COLLATE utf8_bin NOT NULL DEFAULT '',
  `groupId` varchar(75) COLLATE utf8_bin DEFAULT NULL,
  `targetType` varchar(75) COLLATE utf8_bin DEFAULT NULL,
  `attributeName` varchar(75) COLLATE utf8_bin DEFAULT NULL,
  `attributeValue` varchar(75) COLLATE utf8_bin DEFAULT NULL,
  PRIMARY KEY (`id_`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_bin;

/*Table structure for table `cf_AssetGroupUserRoles` */

CREATE TABLE IF NOT EXISTS `cf_AssetGroupUserRoles` (
  `agUserRoleId` varchar(75) COLLATE utf8_bin NOT NULL,
  `assetGroupName` varchar(75) COLLATE utf8_bin NOT NULL,
  `assetGroupRole` int(75) NOT NULL,
  PRIMARY KEY (`agUserRoleId`,`assetGroupName`,`assetGroupRole`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_bin;

CREATE TABLE IF NOT EXISTS `cf_Certificate` (
  `id_` bigint(20) NOT NULL,
  `domainName` varchar(75) COLLATE utf8_bin DEFAULT NULL,
  `certType` varchar(75) COLLATE utf8_bin DEFAULT NULL,
  `validFrom` datetime DEFAULT NULL,
  `validTo` datetime DEFAULT NULL,
  `application` varchar(75) COLLATE utf8_bin DEFAULT NULL,
  `environment` varchar(75) COLLATE utf8_bin DEFAULT NULL,
  `appContact` varchar(75) COLLATE utf8_bin DEFAULT NULL,
  `description` varchar(75) COLLATE utf8_bin DEFAULT NULL,
  `updatedDate` datetime DEFAULT NULL,
  `updatedBy` varchar(75) COLLATE utf8_bin DEFAULT NULL,
  `certStatus` varchar(75) COLLATE utf8_bin DEFAULT NULL,
  PRIMARY KEY (`id_`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_bin;



/*Table structure for table `cf_Datasource` */

CREATE TABLE IF NOT EXISTS `cf_Datasource` (
  `dataSourceId` bigint(20) NOT NULL,
  `dataSourceName` varchar(75) COLLATE utf8_bin DEFAULT NULL,
  `dataSourceDesc` varchar(75) COLLATE utf8_bin DEFAULT NULL,
  `config` varchar(75) COLLATE utf8_bin DEFAULT NULL,
  `createdDate` date DEFAULT NULL,
  `modifiedDate` date DEFAULT NULL,
  PRIMARY KEY (`dataSourceId`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_bin;

/*Table structure for table `cf_Domain` */

CREATE TABLE IF NOT EXISTS `cf_Domain` (
  `domainName` varchar(75) COLLATE utf8_bin NOT NULL,
  `domainDesc` varchar(75) COLLATE utf8_bin DEFAULT NULL,
  `config` varchar(75) COLLATE utf8_bin DEFAULT NULL,
  `createdDate` date DEFAULT NULL,
  `modifiedDate` date DEFAULT NULL,
  `userId` varchar(75) COLLATE utf8_bin DEFAULT NULL,
  PRIMARY KEY (`domainName`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_bin;


/*Table structure for table `cf_JobScheduler` */

CREATE TABLE IF NOT EXISTS `cf_JobScheduler` (
  `jobId` varchar(75) COLLATE utf8_bin NOT NULL,
  `jobUUID` varchar(75) COLLATE utf8_bin DEFAULT NULL,
  `jobName` varchar(150) COLLATE utf8_bin DEFAULT NULL,
  `jobType` varchar(75) COLLATE utf8_bin DEFAULT NULL,
  `jobParams` text COLLATE utf8_bin,
  `jobFrequency` varchar(75) COLLATE utf8_bin DEFAULT NULL,
  `jobExecutable` varchar(75) COLLATE utf8_bin DEFAULT NULL,
  `jobArn` varchar(200) COLLATE utf8_bin DEFAULT NULL,
  `status` varchar(75) COLLATE utf8_bin DEFAULT NULL,
  `userId` varchar(75) COLLATE utf8_bin DEFAULT NULL,
  `createdDate` datetime DEFAULT NULL,
  `modifiedDate` datetime DEFAULT NULL,
  PRIMARY KEY (`jobId`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_bin;

/*Table structure for table `cf_Jobs` */

CREATE TABLE IF NOT EXISTS `cf_Jobs` (
  `jobId` bigint(20) NOT NULL,
  `rulesetId` bigint(20) DEFAULT NULL,
  `cronExpression` varchar(75) COLLATE utf8_bin DEFAULT NULL,
  PRIMARY KEY (`jobId`),
  KEY `IX_6A2145F9` (`rulesetId`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_bin;



/*Table structure for table `cf_OwnerDetails` */

CREATE TABLE IF NOT EXISTS `cf_OwnerDetails` (
  `contactId` varchar(100) COLLATE utf8_bin NOT NULL,
  `ownerName` varchar(100) COLLATE utf8_bin DEFAULT NULL,
  `ownerEmail` varchar(100) COLLATE utf8_bin DEFAULT NULL,
  PRIMARY KEY (`contactId`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_bin;


/*Table structure for table `cf_PatchStats_Kernel` */

CREATE TABLE IF NOT EXISTS `cf_PatchStats_Kernel` (
  `awsaccount` varchar(75) COLLATE utf8_bin DEFAULT NULL,
  `instanceid` varchar(75) COLLATE utf8_bin NOT NULL,
  `rectype` varchar(75) COLLATE utf8_bin DEFAULT NULL,
  `ipaddressaws` varchar(75) COLLATE utf8_bin DEFAULT NULL,
  `nametag` varchar(75) COLLATE utf8_bin DEFAULT NULL,
  `vpcid` varchar(75) COLLATE utf8_bin DEFAULT NULL,
  `ipaddressrhs` varchar(75) COLLATE utf8_bin DEFAULT NULL,
  `rhshostname` varchar(75) COLLATE utf8_bin DEFAULT NULL,
  `systemid` varchar(75) COLLATE utf8_bin DEFAULT NULL,
  `gid` varchar(75) COLLATE utf8_bin DEFAULT NULL,
  `group_` varchar(75) COLLATE utf8_bin DEFAULT NULL,
  `kernel` varchar(75) COLLATE utf8_bin DEFAULT NULL,
  `nopendingerratas` varchar(75) COLLATE utf8_bin DEFAULT NULL,
  `erratadetails` varchar(75) COLLATE utf8_bin DEFAULT NULL,
  `iscompliant` tinyint(4) DEFAULT NULL,
  `isregistered` tinyint(4) DEFAULT NULL,
  PRIMARY KEY (`instanceid`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_bin;




/*Table structure for table `cf_Rbac` */

CREATE TABLE IF NOT EXISTS `cf_Rbac` (
  `rbacId` bigint(20) NOT NULL,
  `rbacType` varchar(75) COLLATE utf8_bin DEFAULT NULL,
  `userOrGroupId` bigint(20) DEFAULT NULL,
  `applicationName` varchar(75) COLLATE utf8_bin DEFAULT NULL,
  `environmentName` varchar(75) COLLATE utf8_bin DEFAULT NULL,
  `stackName` varchar(75) COLLATE utf8_bin DEFAULT NULL,
  `roleName` varchar(75) COLLATE utf8_bin DEFAULT NULL,
  `createDate` datetime DEFAULT NULL,
  `modifiedDate` datetime DEFAULT NULL,
  PRIMARY KEY (`rbacId`),
  KEY `IX_18DB1388` (`rbacType`,`userOrGroupId`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_bin;


/*Table structure for table `cf_RemediationCriteria` */

CREATE TABLE IF NOT EXISTS `cf_RemediationCriteria` (
  `action` varchar(200) COLLATE utf8_bin DEFAULT NULL,
  `matchingString` varchar(200) COLLATE utf8_bin DEFAULT NULL,
  `subAction` varchar(200) COLLATE utf8_bin DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_bin;







/*Table structure for table `cf_SystemConfiguration` */

CREATE TABLE IF NOT EXISTS `cf_SystemConfiguration` (
  `id_` int(11) DEFAULT NULL,
  `environment` varchar(75) COLLATE utf8_bin NOT NULL,
  `keyname` varchar(75) COLLATE utf8_bin NOT NULL,
  `value` varchar(2000) COLLATE utf8_bin DEFAULT NULL,
  `createdDate` datetime DEFAULT NULL,
  `modifiedDate` datetime DEFAULT NULL,
  PRIMARY KEY (`environment`,`keyname`),
  KEY `IX_7196BB48` (`environment`,`keyname`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_bin;

/*Table structure for table `cf_Target` */

CREATE TABLE IF NOT EXISTS `cf_Target` (
  `targetName` varchar(75) COLLATE utf8_bin NOT NULL,
  `targetDesc` varchar(75) COLLATE utf8_bin DEFAULT NULL,
  `category` varchar(75) COLLATE utf8_bin DEFAULT NULL,
  `dataSourceName` varchar(75) COLLATE utf8_bin DEFAULT NULL,
  `targetConfig` text COLLATE utf8_bin,
  `status` varchar(75) COLLATE utf8_bin DEFAULT NULL,
  `userId` varchar(75) COLLATE utf8_bin DEFAULT NULL,
  `endpoint` text COLLATE utf8_bin,
  `createdDate` date DEFAULT NULL,
  `modifiedDate` date DEFAULT NULL,
  `domain` varchar(75) COLLATE utf8_bin DEFAULT NULL,
  PRIMARY KEY (`targetName`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_bin;

/*Table structure for table `cf_Accounts` */

CREATE TABLE IF NOT EXISTS  cf_Accounts(
    accountName varchar(255),
    accountId varchar(255),
    assets varchar(100),
    violations varchar(100),
    accountStatus varchar(100),
    platform varchar(255),
    PRIMARY KEY(accountId)
);
/* Add columns createdby and createdTime */
DELIMITER $$
DROP PROCEDURE IF EXISTS alter_cf_Accounts_table_add_createdBy_createdTime_if_not_exists $$
CREATE PROCEDURE alter_cf_Accounts_table_add_createdBy_createdTime_if_not_exists()
BEGIN
IF NOT EXISTS( SELECT NULL
            FROM INFORMATION_SCHEMA.COLUMNS
           WHERE table_name = 'cf_Accounts'
             AND table_schema = 'pacmandata'
             AND column_name = 'createdBy')  THEN



 ALTER TABLE `cf_Accounts` ADD `createdBy` varchar(150) COLLATE utf8_bin DEFAULT 'userName';



END IF;
IF NOT EXISTS( SELECT NULL
            FROM INFORMATION_SCHEMA.COLUMNS
           WHERE table_name = 'cf_Accounts'
             AND table_schema = 'pacmandata'
             AND column_name = 'createdTime')  THEN



 ALTER TABLE `cf_Accounts` ADD `createdTime` varchar(255) COLLATE utf8_bin DEFAULT 'dateTime';



END IF;
END $$
DELIMITER ;
CALL alter_cf_Accounts_table_add_createdBy_createdTime_if_not_exists();


/* Insert one account */

insert ignore into cf_Accounts values(concat(@ACCOUNT_NAME,''),concat(@ACCOUNT_ID,''),0,0,'configured',concat(@ACCOUNT_PLATFORM,''),'system','dateTime');

DELIMITER $$
DROP PROCEDURE IF EXISTS alter_cf_target_table_add_display_name_if_not_exists $$
CREATE PROCEDURE alter_cf_target_table_add_display_name_if_not_exists()
BEGIN
IF NOT EXISTS( SELECT NULL
            FROM INFORMATION_SCHEMA.COLUMNS
           WHERE table_name = 'cf_Target'
             AND table_schema = 'pacmandata'
             AND column_name = 'displayName')  THEN



 ALTER TABLE `cf_Target` ADD `displayName` varchar(100) COLLATE utf8_bin DEFAULT NULL;



END IF;
END $$
DELIMITER ;
CALL alter_cf_target_table_add_display_name_if_not_exists();



/* deleting old data */
TRUNCATE TABLE cf_Target;

/*Table structure for table `cf_pac_updatable_fields` */

CREATE TABLE IF NOT EXISTS `cf_pac_updatable_fields` (
  `resourceType` varchar(100) COLLATE utf8_bin DEFAULT NULL,
  `displayFields` text COLLATE utf8_bin,
  `updatableFields` longtext COLLATE utf8_bin
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_bin;


/*Table structure for table `oauth_access_token` */

CREATE TABLE IF NOT EXISTS `oauth_access_token` (
  `token_id` varchar(255) DEFAULT NULL,
  `token` mediumblob,
  `authentication_id` varchar(255) NOT NULL,
  `user_name` varchar(255) DEFAULT NULL,
  `client_id` varchar(255) DEFAULT NULL,
  `authentication` mediumblob,
  `refresh_token` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`authentication_id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

/*Table structure for table `oauth_approvals` */

CREATE TABLE IF NOT EXISTS `oauth_approvals` (
  `userId` varchar(255) DEFAULT NULL,
  `clientId` varchar(255) DEFAULT NULL,
  `scope` varchar(255) DEFAULT NULL,
  `status` varchar(10) DEFAULT NULL,
  `expiresAt` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `lastModifiedAt` timestamp NOT NULL DEFAULT '0000-00-00 00:00:00'
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

/*Table structure for table `oauth_client_details` */

CREATE TABLE IF NOT EXISTS `oauth_client_details` (
  `client_id` varchar(255) NOT NULL,
  `resource_ids` varchar(255) DEFAULT NULL,
  `client_secret` varchar(255) DEFAULT NULL,
  `scope` varchar(255) DEFAULT NULL,
  `authorized_grant_types` varchar(255) DEFAULT NULL,
  `web_server_redirect_uri` varchar(255) DEFAULT NULL,
  `authorities` varchar(255) DEFAULT NULL,
  `access_token_validity` int(11) DEFAULT NULL,
  `refresh_token_validity` int(11) DEFAULT NULL,
  `additional_information` varchar(4096) DEFAULT NULL,
  `autoapprove` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`client_id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

/*Table structure for table `oauth_client_owner` */

CREATE TABLE IF NOT EXISTS `oauth_client_owner` (
  `clientId` varchar(75) COLLATE utf8_bin NOT NULL,
  `user` varchar(75) COLLATE utf8_bin NOT NULL,
  PRIMARY KEY (`clientId`,`user`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_bin;

/*Table structure for table `oauth_client_token` */

CREATE TABLE IF NOT EXISTS `oauth_client_token` (
  `token_id` varchar(255) DEFAULT NULL,
  `token` mediumblob,
  `authentication_id` varchar(255) NOT NULL,
  `user_name` varchar(255) DEFAULT NULL,
  `client_id` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`authentication_id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

/*Table structure for table `oauth_code` */

CREATE TABLE IF NOT EXISTS `oauth_code` (
  `code` varchar(255) DEFAULT NULL,
  `authentication` mediumblob
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

/*Table structure for table `oauth_refresh_token` */

CREATE TABLE IF NOT EXISTS `oauth_refresh_token` (
  `token_id` varchar(255) DEFAULT NULL,
  `token` mediumblob,
  `authentication` mediumblob
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

/*Table structure for table `oauth_user` */

CREATE TABLE IF NOT EXISTS `oauth_user` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `user_id` varchar(75) DEFAULT NULL,
  `user_name` varchar(75) DEFAULT NULL,
  `first_name` varchar(75) DEFAULT NULL,
  `last_name` varchar(75) DEFAULT NULL,
  `email` varchar(100) DEFAULT NULL,
  `created_date` datetime DEFAULT NULL,
  `modified_date` datetime DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=340 DEFAULT CHARSET=latin1;

/*Table structure for table `pac_rule_engine_autofix_actions` */

CREATE TABLE IF NOT EXISTS `pac_rule_engine_autofix_actions` (
  `resourceId` varchar(100) COLLATE utf8_bin NOT NULL,
  `lastActionTime` datetime NOT NULL,
  `action` varchar(100) COLLATE utf8_bin DEFAULT NULL,
  PRIMARY KEY (`resourceId`,`lastActionTime`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_bin;

/*Table structure for table `pac_v2_projections` */

CREATE TABLE IF NOT EXISTS `pac_v2_projections` (
  `resourceType` varchar(100) COLLATE utf8_bin NOT NULL,
  `year` decimal(65,0) NOT NULL,
  `quarter` decimal(65,0) NOT NULL,
  `week` decimal(65,0) NOT NULL,
  `projection` bigint(65) DEFAULT NULL,
  PRIMARY KEY (`resourceType`,`year`,`quarter`,`week`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_bin;



/*Table structure for table `pac_v2_ui_download_filters` */

CREATE TABLE IF NOT EXISTS `pac_v2_ui_download_filters` (
  `serviceId` int(100) NOT NULL AUTO_INCREMENT,
  `serviceName` varchar(100) COLLATE utf8_bin DEFAULT NULL,
  `serviceEndpoint` varchar(1000) COLLATE utf8_bin DEFAULT NULL,
  PRIMARY KEY (`serviceId`)
) ENGINE=InnoDB AUTO_INCREMENT=14 DEFAULT CHARSET=utf8 COLLATE=utf8_bin;

/*Table structure for table `pac_v2_ui_filters` */

CREATE TABLE IF NOT EXISTS `pac_v2_ui_filters` (
  `filterId` int(25) NOT NULL AUTO_INCREMENT,
  `filterName` varchar(25) COLLATE utf8_bin DEFAULT NULL,
  PRIMARY KEY (`filterId`)
) ENGINE=InnoDB AUTO_INCREMENT=10 DEFAULT CHARSET=utf8 COLLATE=utf8_bin;

/*Table structure for table `pac_v2_ui_options` */

CREATE TABLE IF NOT EXISTS `pac_v2_ui_options` (
  `optionId` int(25) NOT NULL AUTO_INCREMENT,
  `filterId` int(25) NOT NULL,
  `optionName` varchar(25) COLLATE utf8_bin DEFAULT NULL,
  `optionValue` varchar(25) COLLATE utf8_bin DEFAULT NULL,
  `optionURL` varchar(100) COLLATE utf8_bin DEFAULT NULL,
  PRIMARY KEY (`optionId`),
  KEY `filterId` (`filterId`),
  CONSTRAINT `filterId` FOREIGN KEY (`filterId`) REFERENCES `pac_v2_ui_filters` (`filterId`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=17 DEFAULT CHARSET=utf8 COLLATE=utf8_bin;

/*Table structure for table `pac_v2_ui_widget_faqs` */

CREATE TABLE IF NOT EXISTS `pac_v2_ui_widget_faqs` (
  `faqId` int(11) NOT NULL AUTO_INCREMENT,
  `widgetId` int(11) NOT NULL,
  `widgetName` varchar(50) COLLATE utf8_bin DEFAULT NULL,
  `faqName` text COLLATE utf8_bin,
  `faqAnswer` text COLLATE utf8_bin,
  PRIMARY KEY (`faqId`),
  KEY `widgetId` (`widgetId`),
  CONSTRAINT `pac_v2_ui_widget_faqs_ibfk_1` FOREIGN KEY (`widgetId`) REFERENCES `pac_v2_ui_widgets` (`widgetId`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=11 DEFAULT CHARSET=utf8 COLLATE=utf8_bin;

/*Table structure for table `pac_v2_ui_widgets` */

CREATE TABLE IF NOT EXISTS `pac_v2_ui_widgets` (
  `widgetId` int(11) NOT NULL AUTO_INCREMENT,
  `pageName` varchar(25) COLLATE utf8_bin DEFAULT NULL,
  `widgetName` varchar(50) COLLATE utf8_bin DEFAULT NULL,
  PRIMARY KEY (`widgetId`)
) ENGINE=InnoDB AUTO_INCREMENT=7 DEFAULT CHARSET=utf8 COLLATE=utf8_bin;

/*Table structure for table `pac_v2_userpreferences` */

CREATE TABLE IF NOT EXISTS `pac_v2_userpreferences` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `userId` varchar(100) COLLATE utf8_bin DEFAULT NULL,
  `defaultAssetGroup` text COLLATE utf8_bin,
  `recentlyViewedAG` text COLLATE utf8_bin,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=336 DEFAULT CHARSET=utf8 COLLATE=utf8_bin;

/*Table structure for table `qartz_BLOB_TRIGGERS` */

CREATE TABLE IF NOT EXISTS `qartz_BLOB_TRIGGERS` (
  `SCHED_NAME` varchar(120) NOT NULL,
  `TRIGGER_NAME` varchar(200) NOT NULL,
  `TRIGGER_GROUP` varchar(200) NOT NULL,
  `BLOB_DATA` blob,
  PRIMARY KEY (`SCHED_NAME`,`TRIGGER_NAME`,`TRIGGER_GROUP`),
  KEY `SCHED_NAME` (`SCHED_NAME`,`TRIGGER_NAME`,`TRIGGER_GROUP`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

/*Table structure for table `qartz_CALENDARS` */

CREATE TABLE IF NOT EXISTS `qartz_CALENDARS` (
  `SCHED_NAME` varchar(120) NOT NULL,
  `CALENDAR_NAME` varchar(200) NOT NULL,
  `CALENDAR` blob NOT NULL,
  PRIMARY KEY (`SCHED_NAME`,`CALENDAR_NAME`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

/*Table structure for table `qartz_CRON_TRIGGERS` */

CREATE TABLE IF NOT EXISTS `qartz_CRON_TRIGGERS` (
  `SCHED_NAME` varchar(120) NOT NULL,
  `TRIGGER_NAME` varchar(200) NOT NULL,
  `TRIGGER_GROUP` varchar(200) NOT NULL,
  `CRON_EXPRESSION` varchar(120) NOT NULL,
  `TIME_ZONE_ID` varchar(80) DEFAULT NULL,
  PRIMARY KEY (`SCHED_NAME`,`TRIGGER_NAME`,`TRIGGER_GROUP`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

/*Table structure for table `qartz_FIRED_TRIGGERS` */

CREATE TABLE IF NOT EXISTS `qartz_FIRED_TRIGGERS` (
  `SCHED_NAME` varchar(120) NOT NULL,
  `ENTRY_ID` varchar(95) NOT NULL,
  `TRIGGER_NAME` varchar(200) NOT NULL,
  `TRIGGER_GROUP` varchar(200) NOT NULL,
  `INSTANCE_NAME` varchar(200) NOT NULL,
  `FIRED_TIME` bigint(13) NOT NULL,
  `SCHED_TIME` bigint(13) NOT NULL,
  `PRIORITY` int(11) NOT NULL,
  `STATE` varchar(16) NOT NULL,
  `JOB_NAME` varchar(200) DEFAULT NULL,
  `JOB_GROUP` varchar(200) DEFAULT NULL,
  `IS_NONCONCURRENT` varchar(1) DEFAULT NULL,
  `REQUESTS_RECOVERY` varchar(1) DEFAULT NULL,
  PRIMARY KEY (`SCHED_NAME`,`ENTRY_ID`),
  KEY `IDX_QARTZ_FT_TRIG_INST_NAME` (`SCHED_NAME`,`INSTANCE_NAME`),
  KEY `IDX_QARTZ_FT_INST_JOB_REQ_RCVRY` (`SCHED_NAME`,`INSTANCE_NAME`,`REQUESTS_RECOVERY`),
  KEY `IDX_QARTZ_FT_J_G` (`SCHED_NAME`,`JOB_NAME`,`JOB_GROUP`),
  KEY `IDX_QARTZ_FT_JG` (`SCHED_NAME`,`JOB_GROUP`),
  KEY `IDX_QARTZ_FT_T_G` (`SCHED_NAME`,`TRIGGER_NAME`,`TRIGGER_GROUP`),
  KEY `IDX_QARTZ_FT_TG` (`SCHED_NAME`,`TRIGGER_GROUP`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

/*Table structure for table `qartz_JOB_DETAILS` */

CREATE TABLE IF NOT EXISTS `qartz_JOB_DETAILS` (
  `SCHED_NAME` varchar(120) NOT NULL,
  `JOB_NAME` varchar(200) NOT NULL,
  `JOB_GROUP` varchar(200) NOT NULL,
  `DESCRIPTION` varchar(250) DEFAULT NULL,
  `JOB_CLASS_NAME` varchar(250) NOT NULL,
  `IS_DURABLE` varchar(1) NOT NULL,
  `IS_NONCONCURRENT` varchar(1) NOT NULL,
  `IS_UPDATE_DATA` varchar(1) NOT NULL,
  `REQUESTS_RECOVERY` varchar(1) NOT NULL,
  `JOB_DATA` blob,
  PRIMARY KEY (`SCHED_NAME`,`JOB_NAME`,`JOB_GROUP`),
  KEY `IDX_QARTZ_J_REQ_RECOVERY` (`SCHED_NAME`,`REQUESTS_RECOVERY`),
  KEY `IDX_QARTZ_J_GRP` (`SCHED_NAME`,`JOB_GROUP`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

/*Table structure for table `qartz_LOCKS` */

CREATE TABLE IF NOT EXISTS `qartz_LOCKS` (
  `SCHED_NAME` varchar(120) NOT NULL,
  `LOCK_NAME` varchar(40) NOT NULL,
  PRIMARY KEY (`SCHED_NAME`,`LOCK_NAME`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

/*Table structure for table `qartz_PAUSED_TRIGGER_GRPS` */

CREATE TABLE IF NOT EXISTS `qartz_PAUSED_TRIGGER_GRPS` (
  `SCHED_NAME` varchar(120) NOT NULL,
  `TRIGGER_GROUP` varchar(200) NOT NULL,
  PRIMARY KEY (`SCHED_NAME`,`TRIGGER_GROUP`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

/*Table structure for table `qartz_SCHEDULER_STATE` */

CREATE TABLE IF NOT EXISTS `qartz_SCHEDULER_STATE` (
  `SCHED_NAME` varchar(120) NOT NULL,
  `INSTANCE_NAME` varchar(200) NOT NULL,
  `LAST_CHECKIN_TIME` bigint(13) NOT NULL,
  `CHECKIN_INTERVAL` bigint(13) NOT NULL,
  PRIMARY KEY (`SCHED_NAME`,`INSTANCE_NAME`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

/*Table structure for table `qartz_SIMPLE_TRIGGERS` */

CREATE TABLE IF NOT EXISTS `qartz_SIMPLE_TRIGGERS` (
  `SCHED_NAME` varchar(120) NOT NULL,
  `TRIGGER_NAME` varchar(200) NOT NULL,
  `TRIGGER_GROUP` varchar(200) NOT NULL,
  `REPEAT_COUNT` bigint(7) NOT NULL,
  `REPEAT_INTERVAL` bigint(12) NOT NULL,
  `TIMES_TRIGGERED` bigint(10) NOT NULL,
  PRIMARY KEY (`SCHED_NAME`,`TRIGGER_NAME`,`TRIGGER_GROUP`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

/*Table structure for table `qartz_SIMPROP_TRIGGERS` */

CREATE TABLE IF NOT EXISTS `qartz_SIMPROP_TRIGGERS` (
  `SCHED_NAME` varchar(120) NOT NULL,
  `TRIGGER_NAME` varchar(200) NOT NULL,
  `TRIGGER_GROUP` varchar(200) NOT NULL,
  `STR_PROP_1` varchar(512) DEFAULT NULL,
  `STR_PROP_2` varchar(512) DEFAULT NULL,
  `STR_PROP_3` varchar(512) DEFAULT NULL,
  `INT_PROP_1` int(11) DEFAULT NULL,
  `INT_PROP_2` int(11) DEFAULT NULL,
  `LONG_PROP_1` bigint(20) DEFAULT NULL,
  `LONG_PROP_2` bigint(20) DEFAULT NULL,
  `DEC_PROP_1` decimal(13,4) DEFAULT NULL,
  `DEC_PROP_2` decimal(13,4) DEFAULT NULL,
  `BOOL_PROP_1` varchar(1) DEFAULT NULL,
  `BOOL_PROP_2` varchar(1) DEFAULT NULL,
  PRIMARY KEY (`SCHED_NAME`,`TRIGGER_NAME`,`TRIGGER_GROUP`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

/*Table structure for table `qartz_TRIGGERS` */

CREATE TABLE IF NOT EXISTS `qartz_TRIGGERS` (
  `SCHED_NAME` varchar(120) NOT NULL,
  `TRIGGER_NAME` varchar(200) NOT NULL,
  `TRIGGER_GROUP` varchar(200) NOT NULL,
  `JOB_NAME` varchar(200) NOT NULL,
  `JOB_GROUP` varchar(200) NOT NULL,
  `DESCRIPTION` varchar(250) DEFAULT NULL,
  `NEXT_FIRE_TIME` bigint(13) DEFAULT NULL,
  `PREV_FIRE_TIME` bigint(13) DEFAULT NULL,
  `PRIORITY` int(11) DEFAULT NULL,
  `TRIGGER_STATE` varchar(16) NOT NULL,
  `TRIGGER_TYPE` varchar(8) NOT NULL,
  `START_TIME` bigint(13) NOT NULL,
  `END_TIME` bigint(13) DEFAULT NULL,
  `CALENDAR_NAME` varchar(200) DEFAULT NULL,
  `MISFIRE_INSTR` smallint(2) DEFAULT NULL,
  `JOB_DATA` blob,
  PRIMARY KEY (`SCHED_NAME`,`TRIGGER_NAME`,`TRIGGER_GROUP`),
  KEY `IDX_QARTZ_T_J` (`SCHED_NAME`,`JOB_NAME`,`JOB_GROUP`),
  KEY `IDX_QARTZ_T_JG` (`SCHED_NAME`,`JOB_GROUP`),
  KEY `IDX_QARTZ_T_C` (`SCHED_NAME`,`CALENDAR_NAME`),
  KEY `IDX_QARTZ_T_G` (`SCHED_NAME`,`TRIGGER_GROUP`),
  KEY `IDX_QARTZ_T_STATE` (`SCHED_NAME`,`TRIGGER_STATE`),
  KEY `IDX_QARTZ_T_N_STATE` (`SCHED_NAME`,`TRIGGER_NAME`,`TRIGGER_GROUP`,`TRIGGER_STATE`),
  KEY `IDX_QARTZ_T_N_G_STATE` (`SCHED_NAME`,`TRIGGER_GROUP`,`TRIGGER_STATE`),
  KEY `IDX_QARTZ_T_NEXT_FIRE_TIME` (`SCHED_NAME`,`NEXT_FIRE_TIME`),
  KEY `IDX_QARTZ_T_NFT_ST` (`SCHED_NAME`,`TRIGGER_STATE`,`NEXT_FIRE_TIME`),
  KEY `IDX_QARTZ_T_NFT_MISFIRE` (`SCHED_NAME`,`MISFIRE_INSTR`,`NEXT_FIRE_TIME`),
  KEY `IDX_QARTZ_T_NFT_ST_MISFIRE` (`SCHED_NAME`,`MISFIRE_INSTR`,`NEXT_FIRE_TIME`,`TRIGGER_STATE`),
  KEY `IDX_QARTZ_T_NFT_ST_MISFIRE_GRP` (`SCHED_NAME`,`MISFIRE_INSTR`,`NEXT_FIRE_TIME`,`TRIGGER_GROUP`,`TRIGGER_STATE`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

/*Table structure for table `qartz_TRIGGERS` */

CREATE TABLE IF NOT EXISTS `oauth_user_role_mapping` (
  `userRoleId` varchar(225) DEFAULT NULL,
  `userId` varchar(225) DEFAULT NULL,
  `roleId` varchar(225) DEFAULT NULL,
  `clientId` varchar(300) DEFAULT NULL,
  `allocator` varchar(300) DEFAULT NULL,
  `createdDate` datetime DEFAULT NULL,
  `modifiedDate` datetime DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

DELETE FROM oauth_user_role_mapping where userRoleId in ("4747c0cf-63cc-4829-a1e8-f1e957ec5dd6","4747c0cf-63cc-4829-a1e8-f1e957ec5dd7","f5b2a689-c185-11e8-9c73-12d01119b604");
DELIMITER $$
DROP PROCEDURE IF EXISTS create_primary_key_if_not_exists_for_user_role_mapping $$
CREATE PROCEDURE create_primary_key_if_not_exists_for_user_role_mapping()
BEGIN
 IF((SELECT COUNT(1) AS index_exists FROM information_schema.statistics WHERE TABLE_SCHEMA = DATABASE() and table_name='oauth_user_role_mapping' AND index_name='PRIMARY') < 1) THEN
   SET @query = 'ALTER TABLE oauth_user_role_mapping ADD PRIMARY KEY (userRoleId);';
   PREPARE stmt FROM @query;
   EXECUTE stmt;
 END IF;
END $$
DELIMITER ;
CALL create_primary_key_if_not_exists_for_user_role_mapping();


CREATE TABLE IF NOT EXISTS `oauth_user_credentials` (
    `id` bigint (75),
    `password` varchar (225),
    `type` varchar (225)
);


CREATE TABLE IF NOT EXISTS `oauth_user_roles` (
  `roleId` varchar(225) DEFAULT NULL,
  `roleName` varchar(225) DEFAULT NULL,
  `roleDesc` varchar(225) DEFAULT NULL,
  `writePermission` int(15) DEFAULT NULL,
  `owner` varchar(225) DEFAULT NULL,
  `client` varchar(225) DEFAULT NULL,
  `createdDate` datetime DEFAULT NULL,
  `modifiedDate` datetime DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

DELETE FROM oauth_user_roles WHERE roleId in ("1", "703");
DELIMITER $$
DROP PROCEDURE IF EXISTS create_primary_key_if_not_exists_for_user_roles $$
CREATE PROCEDURE create_primary_key_if_not_exists_for_user_roles()
BEGIN
 IF((SELECT COUNT(1) AS index_exists FROM information_schema.statistics WHERE TABLE_SCHEMA = DATABASE() and table_name='oauth_user_roles' AND index_name='PRIMARY') < 1) THEN
   SET @query = 'ALTER TABLE oauth_user_roles ADD PRIMARY KEY (roleId)';
   PREPARE stmt FROM @query;
   EXECUTE stmt;
 END IF;
END $$
DELIMITER ;
CALL create_primary_key_if_not_exists_for_user_roles();


CREATE TABLE IF NOT EXISTS `task` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `index` varchar(100) DEFAULT NULL,
  `mappings` longtext,
  `data` longtext,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=latin1;


CREATE TABLE IF NOT EXISTS `pac_config_relation` (
  `application` varchar(2048) COLLATE utf8_bin NOT NULL,
  `parent` varchar(2048) COLLATE utf8_bin NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_bin;


CREATE TABLE IF NOT EXISTS `pac_config_key_metadata` (
  `cfkey` varchar(200) COLLATE utf8_bin NOT NULL,
  `description` varchar(200) COLLATE utf8_bin NOT NULL,
  UNIQUE KEY `cfkey` (`cfkey`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_bin;


CREATE TABLE IF NOT EXISTS pac_config_properties
(
   cfkey varchar(250),
   value text,
   application varchar(50),
   profile varchar(15),
   label varchar(10),
   createdBy varchar(200),
   createdDate varchar(50),
   modifiedBy varchar(200),
   modifiedDate varchar(50)
);
/* ALter statement for existing installations */
ALTER TABLE pac_config_properties MODIFY COLUMN cfkey varchar(250), MODIFY COLUMN application varchar(50), MODIFY COLUMN profile varchar(15), MODIFY COLUMN label varchar(10);


-- This procedure create unique_key contraint if not exists
DELIMITER $$
DROP PROCEDURE IF EXISTS create_unique_key_if_not_exists $$
CREATE PROCEDURE create_unique_key_if_not_exists()
BEGIN
 IF((SELECT COUNT(1) AS index_exists FROM information_schema.statistics WHERE TABLE_SCHEMA = DATABASE() and table_name='pac_config_properties' AND index_name='unique_key') < 1) THEN
   SET @query = 'ALTER TABLE pac_config_properties ADD CONSTRAINT unique_key UNIQUE (application,cfkey,profile,label)';
   PREPARE stmt FROM @query;
   EXECUTE stmt;
 END IF;
END $$
DELIMITER ;
CALL create_unique_key_if_not_exists();


CREATE TABLE IF NOT EXISTS pacman_field_override
(
   resourcetype varchar(50),
   _resourceid text,
   fieldname varchar(100),
   fieldvalue varchar(200),
   updatedby varchar(100),
   updatedon varchar(50)
);


ALTER TABLE `cf_pac_updatable_fields` MODIFY COLUMN `displayFields` longtext;

-- CREATE TABLE IF NOT EXISTS cf_pac_updatable_fields
-- (
--    resourceType varchar(100),
--    displayFields longtext,
--    updatableFields longtext
-- );


CREATE TABLE IF NOT EXISTS cf_Aws_Accounts
(
   accountName varchar(200),
   accountId varchar(200),
   accountDesc longtext,
   createdBy varchar(100),
   createdDate varchar(20),
   modifiedBy varchar(100),
   modifiedDate varchar(20),
   id varchar(100) NOT NULL,
   roleCreated varchar(50),
   policiesAttached varchar(50),
   accountTrustUpdate varchar(50),
   baseAccountPolicyUpdate varchar(50),
   status varchar(50)
);

CREATE TABLE IF NOT EXISTS pac_config_audit
(
  cfkey varchar(2048) COLLATE utf8_bin DEFAULT NULL,
  `oldvalue` varchar(2048) COLLATE utf8_bin DEFAULT NULL,
  `newvalue` varchar(2048) COLLATE utf8_bin DEFAULT NULL,
  `application` varchar(200) COLLATE utf8_bin DEFAULT NULL,
  `profile` varchar(20) COLLATE utf8_bin DEFAULT NULL,
  `label` varchar(20) COLLATE utf8_bin DEFAULT NULL,
  `modifiedBy` varchar(100) COLLATE utf8_bin DEFAULT NULL,
  `modifiedDate` varchar(20) COLLATE utf8_bin DEFAULT NULL,
  `userMessage` varchar(2048) COLLATE utf8_bin DEFAULT NULL,
  `systemMessage` varchar(2048) COLLATE utf8_bin DEFAULT NULL,
  `id` varchar(200) COLLATE utf8_bin NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_bin;


CREATE TABLE IF NOT EXISTS `Recommendation_Mappings` (
  `checkId` varchar(20) COLLATE utf8_bin NOT NULL,
  `type` varchar(50) COLLATE utf8_bin DEFAULT NULL,
  `resourceInfo` varchar(200) COLLATE utf8_bin DEFAULT NULL,
  `_resourceId` varchar(200) COLLATE utf8_bin DEFAULT NULL,
  `monthlySavingsField` varchar(200) COLLATE utf8_bin DEFAULT NULL,
  PRIMARY KEY (`checkId`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_bin;


CREATE TABLE IF NOT EXISTS `CloudNotification_mapping` (
  `NotificationId` varchar(10) COLLATE utf8_bin NOT NULL,
  `eventType` varchar(45) COLLATE utf8_bin DEFAULT NULL,
  `resourceIdKey` varchar(45) COLLATE utf8_bin DEFAULT NULL,
  `resourceIdVal` varchar(45) COLLATE utf8_bin DEFAULT NULL,
  `esIndex` varchar(45) COLLATE utf8_bin DEFAULT NULL,
  `phdEntityKey` varchar(45) COLLATE utf8_bin DEFAULT NULL,
  PRIMARY KEY (`NotificationId`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_bin;



/*Create table for azure tenant and subscription mapping*/
 CREATE TABLE IF NOT EXISTS cf_AzureTenantSubscription(
 tenant varchar(255),
 subscription varchar(255),
 PRIMARY KEY(subscription)
 );

/*Insert task to necessary tables*/
INSERT IGNORE INTO `task`(`id`,`index`,`mappings`,`data`) values (1,'exceptions','{\"mappings\":{\"sticky_exceptions\":{\"properties\":{\"assetGroup\":{\"type\":\"text\",\"fields\":{\"keyword\":{\"type\":\"keyword\",\"ignore_above\":256}}},\"dataSource\":{\"type\":\"text\",\"fields\":{\"keyword\":{\"type\":\"keyword\",\"ignore_above\":256}}},\"exceptionName\":{\"type\":\"text\",\"fields\":{\"keyword\":{\"type\":\"keyword\",\"ignore_above\":256}}},\"exceptionReason\":{\"type\":\"text\",\"fields\":{\"keyword\":{\"type\":\"keyword\",\"ignore_above\":256}}},\"expiryDate\":{\"type\":\"date\"},\"targetTypes\":{\"properties\":{\"name\":{\"type\":\"text\",\"fields\":{\"keyword\":{\"type\":\"keyword\",\"ignore_above\":256}}},\"rules\":{\"properties\":{\"ruleId\":{\"type\":\"text\",\"fields\":{\"keyword\":{\"type\":\"keyword\",\"ignore_above\":256}}},\"ruleName\":{\"type\":\"text\",\"fields\":{\"keyword\":{\"type\":\"keyword\",\"ignore_above\":256}}}}}}}}}}}',NULL),(2,'faqs','{\"mappings\":{\"widgetinfo\":{\"properties\":{\"widgetid\":{\"type\":\"text\",\"fields\":{\"keyword\":{\"type\":\"keyword\",\"ignore_above\":256}}},\"widgetname\":{\"type\":\"text\",\"fields\":{\"keyword\":{\"type\":\"keyword\",\"ignore_above\":256}}}}},\"faqinfo\":{\"properties\":{\"answer\":{\"type\":\"text\",\"fields\":{\"keyword\":{\"type\":\"keyword\",\"ignore_above\":256}}},\"faqid\":{\"type\":\"text\",\"fields\":{\"keyword\":{\"type\":\"keyword\",\"ignore_above\":256}}},\"faqname\":{\"type\":\"text\",\"fields\":{\"keyword\":{\"type\":\"keyword\",\"ignore_above\":256}}},\"tag\":{\"type\":\"text\",\"fields\":{\"keyword\":{\"type\":\"keyword\",\"ignore_above\":256}}},\"widgetid\":{\"type\":\"text\",\"fields\":{\"keyword\":{\"type\":\"keyword\",\"ignore_above\":256}}}}}}}','{\"index\": {\"_index\": \"faqs\", \"_type\": \"widgetinfo\", \"_id\": \"w1\"}}\r{\"widgetid\":\"w1\",\"widgetname\":\"compliance overview\"}\r{\"index\": {\"_index\": \"faqs\", \"_type\": \"widgetinfo\", \"_id\": \"w2\"}}\r{\"widgetid\":\"w2\",\"widgetname\":\"patching\"}\r{\"index\": {\"_index\": \"faqs\", \"_type\": \"widgetinfo\", \"_id\": \"w3\"}}\r{\"widgetid\":\"w3\",\"widgetname\":\"tagging\"}\r{\"index\": {\"_index\": \"faqs\", \"_type\": \"widgetinfo\", \"_id\": \"w4\"}}\r{\"widgetid\":\"w4\",\"widgetname\":\"vulnerabilities\"}\r{\"index\": {\"_index\": \"faqs\", \"_type\": \"widgetinfo\", \"_id\": \"w5\"}}\r{\"widgetid\":\"w5\",\"widgetname\":\"certificates\"}\r{\"index\": {\"_index\": \"faqs\", \"_type\": \"faqinfo\", \"_id\": \"w2q7\"}}\r{\"faqid\":\"q7\",\"faqname\":\"How is unpatched count calculated ?\",\"answer\":\"Total assets which does not have updated kernel version.\",\"widgetid\":\"w2\",\"tag\":\"patching\"}\r{\"index\": {\"_index\": \"faqs\", \"_type\": \"faqinfo\", \"_id\": \"w3q4\"}}\r{\"faqid\":\"q4\",\"faqname\":\"How is tagging compliance % calculated ?\",\"answer\":\"Tagging compliance is calculated by dividing total taggable assets by total tagged assets.\",\"widgetid\":\"w3\",\"tag\":\"tagging\"}\r{\"index\": {\"_index\": \"faqs\", \"_type\": \"faqinfo\", \"_id\": \"w1q1\"}}\r{\"faqid\":\"q1\",\"faqname\":\"What is shown in this graph?\",\"answer\":\"This multi ring donut represents the overall compliance percentage. Policies are grouped into categories like security, governance, cost optimization and tagging. Rings in the donut represents compliance percentage for each of those categories.  The rolled up percentage value for a given category is calculated by doing a weighted average of compliance percentage values of individual policies in that category. Weights are assigned based on the importance of the policy. Overall rolled up number in the middle of the donut represents uber compliance percentage for the selected asset group. This value is calculated by doing a simple average of compliance percentage values of the four categories.\",\"widgetid\":\"w1\",\"tag\":\"over-all\"}\r{\"index\": {\"_index\": \"faqs\", \"_type\": \"faqinfo\", \"_id\": \"w4q5\"}}\r{\"faqid\":\"q5\",\"faqname\":\"How is vulnerabilities compliance % calculated ?\",\"answer\":\"Vulnerabilities compliance is calculated by dividing total vulnerable assets by total servers, if an asset is not scanned by qualys , then the asset is considered as vulnerable.\",\"widgetid\":\"w4\",\"tag\":\"vulnerabilities\"}\r{\"index\": {\"_index\": \"faqs\", \"_type\": \"faqinfo\", \"_id\": \"w5q3\"}}\r{\"faqid\":\"q3\",\"faqname\":\"How is certificates compliance % calculated ?\",\"answer\":\"Total non-expired certificates divided by total certificates\",\"widgetid\":\"w5\",\"tag\":\"certificates\"}\r{\"index\": {\"_index\": \"faqs\", \"_type\": \"faqinfo\", \"_id\": \"w3q8\"}}\r{\"faqid\":\"q8\",\"faqname\":\"How is untagged count calculated ?\",\"answer\":\"Total assets which is missing either application/environment tags or both tags.\",\"widgetid\":\"w3\",\"tag\":\"tagging\"}\r{\"index\": {\"_index\": \"faqs\", \"_type\": \"faqinfo\", \"_id\": \"w2q2\"}}\r{\"faqid\":\"q2\",\"faqname\":\"How is patching compliance % calculated ?\",\"answer\":\"Total patched resources divided by total running resources\",\"widgetid\":\"w2\",\"tag\":\"patching\"}');

/*Insert Data Source to necessary tables*/

INSERT IGNORE INTO `cf_Datasource`(`dataSourceId`,`dataSourceName`,`dataSourceDesc`,`config`,`createdDate`,`modifiedDate`) VALUES (1,'aws','Amazon WebService','N/A','2017-08-01','2018-03-09');
INSERT IGNORE INTO `cf_Datasource` (dataSourceId,dataSourceName,dataSourceDesc,config,createdDate,modifiedDate) VALUES (2,'azure','Azure','N/A',{d '2019-11-13'},{d '2019-11-13'});
INSERT IGNORE INTO `cf_Datasource` (dataSourceId,dataSourceName,dataSourceDesc,config,createdDate,modifiedDate) VALUES (3,'gcp','GCP','N/A',{d '2022-05-18'},{d '2022-05-18'});

/*Insert Data Asset Group to necessary tables*/

INSERT IGNORE INTO cf_AssetGroupDetails (groupId,groupName,dataSource,displayName,groupType,createdBy,createdUser,createdDate,modifiedUser,modifiedDate,description,aliasQuery,isVisible) VALUES ('201','aws','aws','AWS','admin','Cloud Security','','','pacman','03/26/2018 23:00','Asset Group to segregate all data related to aws.','',true);
INSERT IGNORE INTO `cf_AssetGroupDetails` (`groupId`, `groupName`, `dataSource`, `displayName`, `groupType`, `createdBy`, `createdUser`, `createdDate`, `modifiedUser`, `modifiedDate`, `description`, `aliasQuery`, `isVisible`) values('cdffb9cd-71de-4e29-9cae-783c2aa211ac','azure','aws','Azure','Admin','Sree','admin@pacbot.org','11/13/2019 10:43','admin@pacbot.org','11/15/2019 11:13','All Azure','{\"actions\":[{\"add\":{\"filter\":{\"bool\":{\"should\":[{\"has_parent\":{\"query\":{\"match_all\":{}},\"parent_type\":\"blobcontainer\"}},{\"bool\":{\"must\":[{\"term\":{\"_type\":{\"value\":\"blobcontainer\"}}}]}}]}},\"index\":\"azure_blobcontainer\",\"alias\":\"azure\"}},{\"add\":{\"filter\":{\"bool\":{\"should\":[{\"has_parent\":{\"query\":{\"match_all\":{}},\"parent_type\":\"workflows\"}},{\"bool\":{\"must\":[{\"term\":{\"_type\":{\"value\":\"workflows\"}}}]}}]}},\"index\":\"azure_workflows\",\"alias\":\"azure\"}},{\"add\":{\"filter\":{\"bool\":{\"should\":[{\"has_parent\":{\"query\":{\"match_all\":{}},\"parent_type\":\"virtualmachine\"}},{\"bool\":{\"must\":[{\"term\":{\"_type\":{\"value\":\"virtualmachine\"}}}]}}]}},\"index\":\"azure_virtualmachine\",\"alias\":\"azure\"}},{\"add\":{\"filter\":{\"bool\":{\"should\":[{\"has_parent\":{\"query\":{\"match_all\":{}},\"parent_type\":\"cosmosdb\"}},{\"bool\":{\"must\":[{\"term\":{\"_type\":{\"value\":\"cosmosdb\"}}}]}}]}},\"index\":\"azure_cosmosdb\",\"alias\":\"azure\"}},{\"add\":{\"filter\":{\"bool\":{\"should\":[{\"has_parent\":{\"query\":{\"match_all\":{}},\"parent_type\":\"securitycenter\"}},{\"bool\":{\"must\":[{\"term\":{\"_type\":{\"value\":\"securitycenter\"}}}]}}]}},\"index\":\"azure_securitycenter\",\"alias\":\"azure\"}},{\"add\":{\"filter\":{\"bool\":{\"should\":[{\"has_parent\":{\"query\":{\"match_all\":{}},\"parent_type\":\"sites\"}},{\"bool\":{\"must\":[{\"term\":{\"_type\":{\"value\":\"sites\"}}}]}}]}},\"index\":\"azure_sites\",\"alias\":\"azure\"}}]}','1');
INSERT IGNORE INTO `cf_AssetGroupDetails` (`groupId`, `groupName`, `dataSource`, `displayName`, `groupType`, `createdBy`, `createdUser`, `createdDate`, `modifiedUser`, `modifiedDate`, `description`, `aliasQuery`, `isVisible`) values('e0008397-f74e-4deb-9066-10bdf11202ae','gcp','gcp','GCP','Admin','Cloud Security','admin@pacbot.org','05/18/2022 06:13','admin@pacbot.org','05/18/2022 06:13','All GCP','{\"actions\":[{\"add\":{\"filter\":{\"bool\":{\"should\":[{\"has_parent\":{\"query\":{\"match_all\":{}},\"parent_type\":\"cloudstorage\"}},{\"bool\":{\"must\":[{\"term\":{\"_type\":{\"value\":\"cloudstorage\"}}}]}}]}},\"index\":\"gcp_cloudstorage\",\"alias\":\"gcp\"}},{\"add\":{\"filter\":{\"bool\":{\"should\":[{\"has_parent\":{\"query\":{\"match_all\":{}},\"parent_type\":\"workflows\"}},{\"bool\":{\"must\":[{\"term\":{\"_type\":{\"value\":\"workflows\"}}}]}}]}},\"index\":\"gcp_workflows\",\"alias\":\"gcp\"}},{\"add\":{\"filter\":{\"bool\":{\"should\":[{\"has_parent\":{\"query\":{\"match_all\":{}},\"parent_type\":\"vminstance\"}},{\"bool\":{\"must\":[{\"term\":{\"_type\":{\"value\":\"vminstance\"}}}]}}]}},\"index\":\"gcp_vminstance",\"alias\":\"gcp\"}},{\"add\":{\"filter\":{\"bool\":{\"should\":[{\"has_parent\":{\"query\":{\"match_all\":{}},\"parent_type\":\"datastore\"}},{\"bool\":{\"must\":[{\"term\":{\"_type\":{\"value\":\"datastore\"}}}]}}]}},\"index\":\"gcp_datastore\",\"alias\":\"gcp\"}},{\"add\":{\"filter\":{\"bool\":{\"should\":[{\"has_parent\":{\"query\":{\"match_all\":{}},\"parent_type\":\"cloudsecuritycommandcenter\"}},{\"bool\":{\"must\":[{\"term\":{\"_type\":{\"value\":\"cloudsecuritycommandcenter\"}}}]}}]}},\"index\":\"gcp_cloudsecuritycommandcenter\",\"alias\":\"gcp\"}},{\"add\":{\"filter\":{\"bool\":{\"should\":[{\"has_parent\":{\"query\":{\"match_all\":{}},\"parent_type\":\"sites\"}},{\"bool\":{\"must\":[{\"term\":{\"_type\":{\"value\":\"sites\"}}}]}}]}},\"index\":\"gcp_sites\",\"alias\":\"gcp\"}}]}','1');

/*Insert Target data in required table*/
INSERT IGNORE INTO `cf_Target` (`targetName`,`displayName`,`targetDesc`,`category`,`dataSourceName`,`targetConfig`,`status`,`userId`,`endpoint`,`createdDate`,`modifiedDate`,`domain`) VALUES ('subscription','Subscription','Azure subscription','','azure','{"key":"id","id":"id"}','enabled','admin',concat(@eshost,':',@esport,'/azure_subscription'),'2022-06-23','2022-06-23','Infra & Platforms');
INSERT IGNORE INTO cf_Target (targetName,displayName,targetDesc,category,dataSourceName,targetConfig,status,userId,endpoint,createdDate,modifiedDate,domain) VALUES ('account','Account','Aws Accounts','Other','aws','{"key":"accountid","id":"accountid"}','enabled',null,concat(@eshost,':',@esport,'/aws_account/account'),{d '2017-09-07'},{d '2017-09-07'},'Infra & Platforms');
INSERT IGNORE INTO cf_Target (targetName,displayName,targetDesc,category,dataSourceName,targetConfig,status,userId,endpoint,createdDate,modifiedDate,domain) VALUES ('api','API Gateway','api','Application Service','aws','{"key":"accountid,region,id","id":"id"}','enabled',null,concat(@eshost,':',@esport,'/aws_api/api'),{d '2017-07-17'},{d '2017-07-17'},'Infra & Platforms');
INSERT IGNORE INTO cf_Target (targetName,displayName,targetDesc,category,dataSourceName,targetConfig,status,userId,endpoint,createdDate,modifiedDate,domain) VALUES ('appelb','ALB','ALB','Compute','aws','{"key":"accountid,region,loadbalancername","id":"loadbalancername"}','enabled',null,concat(@eshost,':',@esport,'/aws_appelb/appelb'),{d '2017-07-17'},{d '2017-07-17'},'Infra & Platforms');
INSERT IGNORE INTO cf_Target (targetName,displayName,targetDesc,category,dataSourceName,targetConfig,status,userId,endpoint,createdDate,modifiedDate,domain) VALUES ('asg','ASG','asg','Compute','aws','{"key":"accountid,region,autoscalinggrouparn","id":"autoscalinggrouparn"}','enabled',null,concat(@eshost,':',@esport,'/aws_asg/asg'),{d '2017-07-17'},{d '2017-07-17'},'Infra & Platforms');
INSERT IGNORE INTO cf_Target (targetName,displayName,targetDesc,category,dataSourceName,targetConfig,status,userId,endpoint,createdDate,modifiedDate,domain) VALUES ('asgpolicy','ASG Policy','ASG Scaling policy','Compute','aws','{"key":"accountid,region,policyname","id":"policyname"}','active',920825,concat(@eshost,':',@esport,'/aws_asgpolicy/asgpolicy'),{d '2017-11-29'},{d '2017-11-29'},'Infra & Platforms');
INSERT IGNORE INTO cf_Target (targetName,displayName,targetDesc,category,dataSourceName,targetConfig,status,userId,endpoint,createdDate,modifiedDate,domain) VALUES ('checks',' Trusted Advisor Check','Trusted Advisor Checks','Other','aws','{"key":"accountid,checkid","id":"checkid"}','finding',null,concat(@eshost,':',@esport,'/aws_checks/checks'),{d '2017-09-27'},{d '2017-09-27'},'Infra & Platforms');
INSERT IGNORE INTO cf_Target (targetName,displayName,targetDesc,category,dataSourceName,targetConfig,status,userId,endpoint,createdDate,modifiedDate,domain) VALUES ('classicelb','CLB','classicelb','Compute','aws','{"key":"accountid,region,loadbalancername","id":"loadbalancername"}','enabled',null,concat(@eshost,':',@esport,'/aws_classicelb/classicelb'),{d '2017-07-17'},{d '2017-07-17'},'Infra & Platforms');
INSERT IGNORE INTO cf_Target (targetName,displayName,targetDesc,category,dataSourceName,targetConfig,status,userId,endpoint,createdDate,modifiedDate,domain) VALUES ('cloudfront','CloudFront','Cloud Front','Networking & Content Delivery','aws','{"key":"accountid,id","id":"id"}','enabled',null,concat(@eshost,':',@esport,'/aws_cloudfront/cloudfront'),{d '2017-10-24'},{d '2017-10-24'},'Infra & Platforms');
INSERT IGNORE INTO cf_Target (targetName,displayName,targetDesc,category,dataSourceName,targetConfig,status,userId,endpoint,createdDate,modifiedDate,domain) VALUES ('customergateway','Customer Gateway','Customer Gateway','Networking & Content Delivery','aws','{"key":"accountid,region,customergatewayid","id":"customergatewayid"}','active',20433,concat(@eshost,':',@esport,'/aws_customergateway/customergateway'),{d '2018-03-26'},{d '2018-03-26'},'Infra & Platforms');
INSERT IGNORE INTO cf_Target (targetName,displayName,targetDesc,category,dataSourceName,targetConfig,status,userId,endpoint,createdDate,modifiedDate,domain) VALUES ('dhcpoption','DHCP Option Set','DHCP Option Sets','Networking & Content Delivery','aws','{"key":"accountid,region,dhcpoptionsid","id":"dhcpoptionsid"}','active',20433,concat(@eshost,':',@esport,'/aws_dhcpoption/dhcpoption'),{d '2018-03-26'},{d '2018-03-26'},'Infra & Platforms');
INSERT IGNORE INTO cf_Target (targetName,displayName,targetDesc,category,dataSourceName,targetConfig,status,userId,endpoint,createdDate,modifiedDate,domain) VALUES ('directconnect','Direct Connect','Direct Connect','Networking & Content Delivery','aws','{"key":"accountid,region,connectionid","id":"connectionid"}','active',20433,concat(@eshost,':',@esport,'/aws_directconnect/directconnect'),{d '2018-03-26'},{d '2018-03-26'},'Infra & Platforms');
INSERT IGNORE INTO cf_Target (targetName,displayName,targetDesc,category,dataSourceName,targetConfig,status,userId,endpoint,createdDate,modifiedDate,domain) VALUES ('dynamodb','DynamoDB','dynamodb','Database','aws','{"key":"accountid,region,tablearn","id":"tablearn"}','enabled',null,concat(@eshost,':',@esport,'/aws_dynamodb/dynamodb'),{d '2017-07-17'},{d '2017-07-17'},'Infra & Platforms');
INSERT IGNORE INTO cf_Target (targetName,displayName,targetDesc,category,dataSourceName,targetConfig,status,userId,endpoint,createdDate,modifiedDate,domain) VALUES ('ec2','EC2','ec2','Compute','aws','{"key":"accountid,region,instanceid","id":"instanceid"}','enabled',null,concat(@eshost,':',@esport,'/aws_ec2/ec2'),{d '2017-07-17'},{d '2017-07-17'},'Infra & Platforms');
INSERT IGNORE INTO cf_Target (targetName,displayName,targetDesc,category,dataSourceName,targetConfig,status,userId,endpoint,createdDate,modifiedDate,domain) VALUES ('efs','EFS','efs','Storage','aws','{"key":"accountid,region,filesystemid","id":"filesystemid"}','enabled',null,concat(@eshost,':',@esport,'/aws_efs/efs'),{d '2017-07-17'},{d '2017-07-17'},'Infra & Platforms');
INSERT IGNORE INTO cf_Target (targetName,displayName,targetDesc,category,dataSourceName,targetConfig,status,userId,endpoint,createdDate,modifiedDate,domain) VALUES ('elasticip','Elastic IP','Elastic IP','Networking & Content Delivery','aws','{"key":"accountid,region,publicip","id":"publicip"}','active',920825,concat(@eshost,':',@esport,'/aws_elasticip/elasticip'),{d '2017-11-29'},{d '2017-11-29'},'Infra & Platforms');
INSERT IGNORE INTO cf_Target (targetName,displayName,targetDesc,category,dataSourceName,targetConfig,status,userId,endpoint,createdDate,modifiedDate,domain) VALUES ('elasticsearch','OpenSearch','Elasticsearch Service','Analytics','aws','{"key":"accountid,region,domainid","id":"domainid"}','active',20433,concat(@eshost,':',@esport,'/aws_elasticsearch/elasticsearch'),{d '2018-03-26'},{d '2018-03-26'},'Infra & Platforms');
INSERT IGNORE INTO cf_Target (targetName,displayName,targetDesc,category,dataSourceName,targetConfig,status,userId,endpoint,createdDate,modifiedDate,domain) VALUES ('emr','EMR','emr','Analytics','aws','{"key":"accountid,region,id","id":"id"}','enabled',null,concat(@eshost,':',@esport,'/aws_emr/emr'),{d '2017-07-17'},{d '2017-07-17'},'Infra & Platforms');
INSERT IGNORE INTO cf_Target (targetName,displayName,targetDesc,category,dataSourceName,targetConfig,status,userId,endpoint,createdDate,modifiedDate,domain) VALUES ('eni','ENI','eni','Compute','aws','{"key":"accountid,region,networkinterfaceid","id":"networkinterfaceid"}','enabled',null,concat(@eshost,':',@esport,'/aws_eni/eni'),{d '2017-07-13'},{d '2017-07-13'},'Infra & Platforms');
INSERT IGNORE INTO cf_Target (targetName,displayName,targetDesc,category,dataSourceName,targetConfig,status,userId,endpoint,createdDate,modifiedDate,domain) VALUES ('iamrole','IAM Role','IAM Role','Identity','aws','{"key":"rolearn","id":"rolearn"}','enabled',null,concat(@eshost,':',@esport,'/aws_iamrole/iamrole'),{d '2017-08-28'},{d '2017-08-28'},'Infra & Platforms');
INSERT IGNORE INTO cf_Target (targetName,displayName,targetDesc,category,dataSourceName,targetConfig,status,userId,endpoint,createdDate,modifiedDate,domain) VALUES ('iamuser','IAM User','IAM User','Identity','aws','{"key":"accountid,username","id":"username"}','enabled',null,concat(@eshost,':',@esport,'/aws_iamuser/iamuser'),{d '2017-08-08'},{d '2017-08-08'},'Infra & Platforms');
INSERT IGNORE INTO cf_Target (targetName,displayName,targetDesc,category,dataSourceName,targetConfig,status,userId,endpoint,createdDate,modifiedDate,domain) VALUES ('internetgateway','Internet Gateway','Internet gate way','Networking & Content Delivery','aws','{"key":"accountid,region,internetgatewayid","id":"internetgatewayid"}','active',920825,concat(@eshost,':',@esport,'/aws_internetgateway/internetgateway'),{d '2017-11-29'},{d '2017-11-29'},'Infra & Platforms');
INSERT IGNORE INTO cf_Target (targetName,displayName,targetDesc,category,dataSourceName,targetConfig,status,userId,endpoint,createdDate,modifiedDate,domain) VALUES ('kms','KMS Key','KMS','Identity','aws','{"key":"accountid,region,keyid","id":"keyid"}','enabled',null,concat(@eshost,':',@esport,'/aws_kms/kms'),{d '2017-10-24'},{d '2017-10-24'},'Infra & Platforms');
INSERT IGNORE INTO cf_Target (targetName,displayName,targetDesc,category,dataSourceName,targetConfig,status,userId,endpoint,createdDate,modifiedDate,domain) VALUES ('lambda','Lambda','lambda','Compute','aws','{"key":"accountid,region,functionarn","id":"functionarn"}','enabled',null,concat(@eshost,':',@esport,'/aws_lambda/lambda'),{d '2017-07-17'},{d '2017-07-17'},'Infra & Platforms');
INSERT IGNORE INTO cf_Target (targetName,displayName,targetDesc,category,dataSourceName,targetConfig,status,userId,endpoint,createdDate,modifiedDate,domain) VALUES ('launchconfig','ASG Launch Config','ASG Launch Configurations','Compute','aws','{"key":"accountid,region,launchconfigurationname","id":"launchconfigurationname"}','active',920825,concat(@eshost,':',@esport,'/aws_launchconfig/launchconfig'),{d '2017-11-29'},{d '2017-11-29'},'Infra & Platforms');
INSERT IGNORE INTO cf_Target (targetName,displayName,targetDesc,category,dataSourceName,targetConfig,status,userId,endpoint,createdDate,modifiedDate,domain) VALUES ('nat','NAT Gateway','nat','Compute','aws','{"key":"accountid,region,natgatewayid","id":"natgatewayid"}','enabled',null,concat(@eshost,':',@esport,'/aws_nat/nat'),{d '2017-07-17'},{d '2017-07-17'},'Infra & Platforms');
INSERT IGNORE INTO cf_Target (targetName,displayName,targetDesc,category,dataSourceName,targetConfig,status,userId,endpoint,createdDate,modifiedDate,domain) VALUES ('networkacl','Network ACL','Network ACL','Networking & Content Delivery','aws','{"key":"accountid,region,networkaclid","id":"networkaclid"}','active',920825,concat(@eshost,':',@esport,'/aws_networkacl/networkacl'),{d '2017-11-28'},{d '2017-11-28'},'Infra & Platforms');
INSERT IGNORE INTO cf_Target (targetName,displayName,targetDesc,category,dataSourceName,targetConfig,status,userId,endpoint,createdDate,modifiedDate,domain) VALUES ('peeringconnection','VPC Peering Connection','Peering Connection','Networking & Content Delivery','aws','{"key":"accountid,region,vpcpeeringconnectionid","id":"vpcpeeringconnectionid"}','active',20433,concat(@eshost,':',@esport,'/aws_peeringconnection/peeringconnection'),{d '2018-03-26'},{d '2018-03-26'},'Infra & Platforms');
INSERT IGNORE INTO cf_Target (targetName,displayName,targetDesc,category,dataSourceName,targetConfig,status,userId,endpoint,createdDate,modifiedDate,domain) VALUES ('phd','Health Dashboard','Personal Dashboard Info','Other','aws','{"key":"accountid,eventarn","id":"eventarn"}','finding',null,concat(@eshost,':',@esport,'/aws_phd/phd'),{d '2017-10-24'},{d '2017-10-24'},'Infra & Platforms');
INSERT IGNORE INTO cf_Target (targetName,displayName,targetDesc,category,dataSourceName,targetConfig,status,userId,endpoint,createdDate,modifiedDate,domain) VALUES ('rdscluster','RDS Cluster','rdscluster','Database','aws','{"key":"accountid,region,dbclusterarn","id":"dbclusterarn"}','enabled',123,concat(@eshost,':',@esport,'/aws_rdscluster/rdscluster'),{d '2017-07-17'},{d '2018-08-03'},'Infra & Platforms');
INSERT IGNORE INTO cf_Target (targetName,displayName,targetDesc,category,dataSourceName,targetConfig,status,userId,endpoint,createdDate,modifiedDate,domain) VALUES ('rdsdb','RDS Database','rdsdb','Database','aws','{"key":"accountid,region,dbclusterarn","id":"dbclusterarn"}','enabled',null,concat(@eshost,':',@esport,'/aws_rdsdb/rdsdb'),{d '2017-07-17'},{d '2017-07-17'},'Infra & Platforms');
INSERT IGNORE INTO cf_Target (targetName,displayName,targetDesc,category,dataSourceName,targetConfig,status,userId,endpoint,createdDate,modifiedDate,domain) VALUES ('rdssnapshot','RDS Snapshot','RDS Snapshot','Database','aws','{"key":"accountid,region,dbsnapshotidentifier","id":"dbsnapshotidentifier"}','enabled',null,concat(@eshost,':',@esport,'/aws_rdssnapshot/rdssnapshot'),{d '2017-08-28'},{d '2017-08-28'},'Infra & Platforms');
INSERT IGNORE INTO cf_Target (targetName,displayName,targetDesc,category,dataSourceName,targetConfig,status,userId,endpoint,createdDate,modifiedDate,domain) VALUES ('redshift','RedShift','redshift','Database','aws','{"key":"accountid,region,clusteridentifier","id":"clusteridentifier"}','enabled',20433,concat(@eshost,':',@esport,'/aws_redshift/redshift'),{d '2017-07-17'},{d '2017-09-06'},'Infra & Platforms');
INSERT IGNORE INTO cf_Target (targetName,displayName,targetDesc,category,dataSourceName,targetConfig,status,userId,endpoint,createdDate,modifiedDate,domain) VALUES ('routetable','Route Table','Route Table','Networking & Content Delivery','aws','{"key":"accountid,region,routetableid","id":"routetableid"}','active',920825,concat(@eshost,':',@esport,'/aws_routetable/routetable'),{d '2017-11-28'},{d '2017-11-28'},'Infra & Platforms');
INSERT IGNORE INTO cf_Target (targetName,displayName,targetDesc,category,dataSourceName,targetConfig,status,userId,endpoint,createdDate,modifiedDate,domain) VALUES ('s3','S3','s3','Storage','aws','{"key":"accountid,region,name","id":"name"}','enabled',null,concat(@eshost,':',@esport,'/aws_s3/s3'),{d '2017-07-17'},{d '2017-07-17'},'Infra & Platforms');
INSERT IGNORE INTO cf_Target (targetName,displayName,targetDesc,category,dataSourceName,targetConfig,status,userId,endpoint,createdDate,modifiedDate,domain) VALUES ('sg','SG','sg','Compute','aws','{"key":"accountid,region,groupid","id":"groupid"}','enabled',null,concat(@eshost,':',@esport,'/aws_sg/sg'),{d '2017-07-17'},{d '2017-07-17'},'Infra & Platforms');
INSERT IGNORE INTO cf_Target (targetName,displayName,targetDesc,category,dataSourceName,targetConfig,status,userId,endpoint,createdDate,modifiedDate,domain) VALUES ('snapshot','EBS Snapshot','snapshot','Compute','aws','{"key":"accountid,region,snapshotid","id":"snapshotid"}','enabled',null,concat(@eshost,':',@esport,'/aws_snapshot/snapshot'),{d '2017-07-17'},{d '2017-07-17'},'Infra & Platforms');
INSERT IGNORE INTO cf_Target (targetName,displayName,targetDesc,category,dataSourceName,targetConfig,status,userId,endpoint,createdDate,modifiedDate,domain) VALUES ('snstopic','SNS Topic','Simple Notification Service topics','Application Services','aws','{"key":"accountid,region,topicarn","id":"topicarn"}','active',20433,concat(@eshost,':',@esport,'/aws_snstopic/snstopic'),{d '2018-03-26'},{d '2018-03-26'},'Infra & Platforms');
INSERT IGNORE INTO cf_Target (targetName,displayName,targetDesc,category,dataSourceName,targetConfig,status,userId,endpoint,createdDate,modifiedDate,domain) VALUES ('stack','Stack','stack','Management Tools','aws','{"key":"accountid,region,stackid","id":"stackid"}','enabled',null,concat(@eshost,':',@esport,'/aws_stack/stack'),{d '2017-07-17'},{d '2017-07-17'},'Infra & Platforms');
INSERT IGNORE INTO cf_Target (targetName,displayName,targetDesc,category,dataSourceName,targetConfig,status,userId,endpoint,createdDate,modifiedDate,domain) VALUES ('subnet','Subnet','subnet','Compute','aws','{"key":"accountid,region,subnetid","id":"subnetid"}','enabled',null,concat(@eshost,':',@esport,'/aws_subnet/subnet'),{d '2017-07-17'},{d '2017-07-17'},'Infra & Platforms');
INSERT IGNORE INTO cf_Target (targetName,displayName,targetDesc,category,dataSourceName,targetConfig,status,userId,endpoint,createdDate,modifiedDate,domain) VALUES ('targetgroup','Target Group','targetgroup','Compute','aws','{"key":"accountid,region,targetgroupname","id":"targetgroupname"}','enabled',null,concat(@eshost,':',@esport,'/aws_targetgroup/targetgroup'),{d '2017-07-17'},{d '2017-07-17'},'Infra & Platforms');
INSERT IGNORE INTO cf_Target (targetName,displayName,targetDesc,category,dataSourceName,targetConfig,status,userId,endpoint,createdDate,modifiedDate,domain) VALUES ('virtualinterface','Virtual Interface','Virtual Interface','Networking & Content Delivery','aws','{"key":"accountid,region,virtualinterfaceid","id":"virtualinterfaceid"}','active',20433,concat(@eshost,':',@esport,'/aws_virtualinterface/virtualinterface'),{d '2018-03-26'},{d '2018-03-26'},'Infra & Platforms');
INSERT IGNORE INTO cf_Target (targetName,displayName,targetDesc,category,dataSourceName,targetConfig,status,userId,endpoint,createdDate,modifiedDate,domain) VALUES ('volume','EBS Volume','volume','Storage','aws','{"key":"accountid,region,volumeid","id":"volumeid"}','enabled',20433,concat(@eshost,':',@esport,'/aws_volume/volume'),{d '2017-07-17'},{d '2017-11-03'},'Infra & Platforms');
INSERT IGNORE INTO cf_Target (targetName,displayName,targetDesc,category,dataSourceName,targetConfig,status,userId,endpoint,createdDate,modifiedDate,domain) VALUES ('vpc','VPC','vpc','Compute','aws','{"key":"accountid,region,vpcid","id":"vpcid"}','enabled',20433,concat(@eshost,':',@esport,'/aws_vpc/vpc'),{d '2017-07-17'},{d '2017-11-28'},'Infra & Platforms');
INSERT IGNORE INTO cf_Target (targetName,displayName,targetDesc,category,dataSourceName,targetConfig,status,userId,endpoint,createdDate,modifiedDate,domain) VALUES ('vpnconnection','VPN Connection','VPN Connection','Networking & Content Delivery','aws','{"key":"accountid,region,vpnconnectionid","id":"vpnconnectionid"}','active',20433,concat(@eshost,':',@esport,'/aws_vpnconnection/vpnconnection'),{d '2018-03-26'},{d '2018-03-26'},'Infra & Platforms');
INSERT IGNORE INTO cf_Target (targetName,displayName,targetDesc,category,dataSourceName,targetConfig,status,userId,endpoint,createdDate,modifiedDate,domain) VALUES ('vpngateway','VPN Gateway','VPN Gateway','Networking & Content Delivery','aws','{"key":"accountid,region,vpngatewayid","id":"vpngatewayid"}','active',920825,concat(@eshost,':',@esport,'/aws_vpngateway/vpngateway'),{d '2017-11-29'},{d '2017-11-29'},'Infra & Platforms');
INSERT IGNORE INTO cf_Target (targetName,displayName,targetDesc,category,dataSourceName,targetConfig,status,userId,endpoint,createdDate,modifiedDate,domain) VALUES ('elasticache','ElastiCache','ElastiCache','Database','aws','{"key":"account,region,clustername","id":"arn"}','enabled',null,concat(@eshost,':',@esport,'/aws_elasticache/elasticache'),{d '2017-11-13'},{d '2017-11-13'},'Infra & Platforms');
INSERT IGNORE INTO cf_Target (targetName,displayName,targetDesc,category,dataSourceName,targetConfig,status,userId,endpoint,createdDate,modifiedDate,domain) VALUES ('datastream','Kinesis Data Stream','Kinesis Datastream','Analytics','aws','{"key":"streamarn","id":"streamarn"}','enabled','123',concat(@eshost,':',@esport,'/aws_datastream/datastream'),{d '2018-10-30'},{d '2018-10-30'},'Infra & Platforms');
INSERT IGNORE INTO cf_Target (targetName,displayName,targetDesc,category,dataSourceName,targetConfig,status,userId,endpoint,createdDate,modifiedDate,domain) VALUES ('deliverystream','Kinesis Delivery Stream','Kinesis Fireshose','Analytics','aws','{"key":"deliverystreamarn","id":"deliverystreamarn"}','enabled','123',concat(@eshost,':',@esport,'/aws_deliverystream/deliverystream'),{d '2018-10-30'},{d '2018-10-30'},'Infra & Platforms');
INSERT IGNORE INTO cf_Target (targetName,displayName,targetDesc,category,dataSourceName,targetConfig,status,userId,endpoint,createdDate,modifiedDate,domain) VALUES ('videostream','Kinesis Video Stream','Kinesis Videostream','Analytics','aws','{"key":"streamarn","id":"streamarn"}','enabled','123',concat(@eshost,':',@esport,'/aws_videostream/videostream'),{d '2018-10-30'},{d '2018-10-30'},'Infra & Platforms');
INSERT IGNORE INTO cf_Target (targetName,displayName,targetDesc,category,dataSourceName,targetConfig,status,userId,endpoint,createdDate,modifiedDate,domain) VALUES ('reservedinstance','EC2 Reserved Instance','Reserved Instances','Compute','aws','{"key":"instanceid","id":"instanceid"}','','123',concat(@eshost,':',@esport,'/aws_reservedinstance/reservedinstance'),{d '2018-11-01'},{d '2018-11-01'},'Infra & Platforms');
INSERT IGNORE INTO cf_Target (targetName,displayName,targetDesc,category,dataSourceName,targetConfig,status,userId,endpoint,createdDate,modifiedDate,domain) VALUES ('route53','Route 53','Route 53','Networking & Content Delivery','aws','{"key":"hostedZoneId","id":"hostedZoneId"}','','123',concat(@eshost,':',@esport,'/aws_route53/route53'),{d '2019-08-03'},{d '2019-08-03'},'Infra & Platforms');
INSERT IGNORE INTO cf_Target (targetName,displayName,targetDesc,category,dataSourceName,targetConfig,status,userId,endpoint,createdDate,modifiedDate,domain) VALUES ('acmcertificate','ACM Certifcate','acmcertificate','Identity & Compliance','aws','{\"key\":\"accountid,domainname\",\"id\":\"domainname\"}','enabled','admin@pacbot.org',concat(@eshost,':',@esport,'/aws_acmcertificate/acmcertificate'),'2019-02-15','2019-02-18','Infra & Platforms');
INSERT IGNORE INTO cf_Target (targetName,displayName,targetDesc,category,dataSourceName,targetConfig,status,userId,endpoint,createdDate,modifiedDate,domain) VALUES ('iamcertificate','IAM Certificate','iamcertificate','Identity & Compliance','aws','{\"key\":\"accountid,servercertificatename\",\"id\":\"servercertificatename\"}','enabled','admin@pacbot.org',concat(@eshost,':',@esport,'/aws_iamcertificate/iamcertificate'),'2019-02-15','2019-02-18','Infra & Platforms');
INSERT IGNORE INTO cf_Target (targetName,displayName,targetDesc,category,dataSourceName,targetConfig,status,userId,endpoint,createdDate,modifiedDate,domain) VALUES ('iamgroup','IAM Group','IAM groups','Identity & Compliance','aws','{\"key\":\"accountid,groupname\",\"id\":\"groupname\"}','enabled','admin@pacbot.org',concat(@eshost,':',@esport,'/aws_iamgroup/iamgroup'),'2019-02-26','2019-02-26','Infra & Platforms');
INSERT IGNORE INTO cf_Target (targetName,displayName,targetDesc,category,dataSourceName,targetConfig,status,userId,endpoint,createdDate,modifiedDate,domain) VALUES ('cloudtrail','CloudTrail','AWS Cloud Trail','Management & Governance','aws','{\"key\":\"trailarn\",\"id\":\"trailarn\"}','enabled','admin@pacbot.org',concat(@eshost,':',@esport,'/aws_cloudtrail/cloudtrail'),'2019-02-26','2019-02-26','Infra & Platforms');
INSERT IGNORE INTO cf_Target (targetName,displayName,targetDesc,category,dataSourceName,targetConfig,status,userId,endpoint,createdDate,modifiedDate,domain) VALUES ('cloudwatchlogs','CloudWatch Logs','AWS CloudWatch Logs','Management & Governance','aws','{\"key\":\"logarn\",\"id\":\"logarn\"}','enabled','admin@pacbot.org',concat(@eshost,':',@esport,'/aws_cloudwatchlogs/cloudwatchlogs'),'2022-05-26','2022-05-26','Infra & Platforms');
INSERT IGNORE INTO cf_Target (targetName,displayName,targetDesc,category,dataSourceName,targetConfig,status,userId,endpoint,createdDate,modifiedDate,domain) VALUES ('cloudwatchalarm','CloudWatch Alarm','CloudWatch Alarm','Management & Governance','aws','{\"key\":\"alarmarn\",\"id\":\"alarmarn\"}','enabled','admin@pacbot.org',concat(@eshost,':',@esport,'/aws_cloudwatchalarm/cloudwatchalarm'),'2022-05-26','2022-05-26','Infra & Platforms');
INSERT IGNORE INTO `cf_Target` (`targetName`,`displayName`,`targetDesc`,`category`,`dataSourceName`,`targetConfig`,`status`,`userId`,`endpoint`,`createdDate`,`modifiedDate`,`domain`) VALUES ('vminstance','VM','GCP Virtual Machine','Compute','gcp','{"key":"id","id":"id"}','enabled','admin',concat(@eshost,':',@esport,'/gcp_vminstance/vminstance'),'2022-06-01','2022-06-01','Infra & Platforms');
INSERT IGNORE INTO cf_Target (targetName,displayName,targetDesc,category,dataSourceName,targetConfig,status,userId,endpoint,createdDate,modifiedDate,domain) VALUES ('documentdb','DocumentDB','AWS Documentdb','Database','aws','{\"key\":\"dbclusterresourceid\",\"id\":\"dbclusterresourceid\"}','enabled','admin@pacbot.org',concat(@eshost,':',@esport,'/aws_documentdb/documentdb'),'2022-05-06','2022-05-06','Infra & Platforms');
INSERT IGNORE INTO cf_Target (targetName,displayName,targetDesc,category,dataSourceName,targetConfig,status,userId,endpoint,createdDate,modifiedDate,domain) VALUES ('dms','Data Migration Service','AWS Database Migration service','Database','aws','{\"key\":\"replicationinstancearn\",\"id\":\"replicationinstancearn\"}','enabled','admin@pacbot.org',concat(@eshost,':',@esport,'/aws_dms/dms'),'2022-05-06','2022-05-06','Infra & Platforms');
INSERT IGNORE INTO cf_Target (targetName,displayName,targetDesc,category,dataSourceName,targetConfig,status,userId,endpoint,createdDate,modifiedDate,domain) VALUES ('eks','EKS','Amazon Elastic Kubernetes Service (EKS)','Compute','aws','{\"key\":\"clusterarn\",\"id\":\"clusterarn\"}','enabled','admin@pacbot.org',concat(@eshost,':',@esport,'/aws_eks/eks'),'2022-05-06','2022-05-06','Infra & Platforms');
INSERT IGNORE INTO cf_Target (targetName,displayName,targetDesc,category,dataSourceName,targetConfig,status,userId,endpoint,createdDate,modifiedDate,domain) VALUES ('awsathena','Athena','AWS Athena Query services','Database','aws','{\"key\":\"queryid\",\"id\":\"queryid\"}','enabled','admin@pacbot.org',concat(@eshost,':',@esport,'/aws_awsathena/awsathena'),'2022-05-06','2022-05-06','Infra & Platforms');
INSERT IGNORE INTO cf_Target (targetName,displayName,targetDesc,category,dataSourceName,targetConfig,status,userId,endpoint,createdDate,modifiedDate,domain) VALUES ('awscomprehend','Comprehend','AWS Comprehend','Management Tools','aws','{\"key\":\"jobarn\",\"id\":\"jobarn\"}','enabled','admin@pacbot.org',concat(@eshost,':',@esport,'/aws_awscomprehend/awscomprehend'),'2022-05-06','2022-05-06','Infra & Platforms');
INSERT IGNORE INTO cf_Target (targetName,displayName,targetDesc,category,dataSourceName,targetConfig,status,userId,endpoint,createdDate,modifiedDate,domain) VALUES ('daxcluster','DAX Cluster','AWS DAX cluster','Database','aws','{\"key\":\"clusterarn\",\"id\":\"clusterarn\"}','enabled','admin@pacbot.org',concat(@eshost,':',@esport,'/aws_daxcluster/daxcluster'),'2022-05-06','2022-05-06','Infra & Platforms');
INSERT IGNORE INTO cf_Target (targetName,displayName,targetDesc,category,dataSourceName,targetConfig,status,userId,endpoint,createdDate,modifiedDate,domain) VALUES ('appflow','AppFlow','AWS AppFlow','Management Tools','aws','{\"key\":\"flowarn\",\"id\":\"flowarn\"}','enabled','admin@pacbot.org',concat(@eshost,':',@esport,'/aws_appflow/appflow'),'2022-05-06','2022-05-06','Infra & Platforms');
INSERT IGNORE INTO cf_Target (targetName,displayName,targetDesc,category,dataSourceName,targetConfig,status,userId,endpoint,createdDate,modifiedDate,domain) VALUES ('ecstaskdefinition','ECS Task Definitions','AWS ECS Task Definitions','Compute','aws','{\"key\":\"taskdefarn\",\"id\":\"taskdefarn\"}','enabled','admin@pacbot.org',concat(@eshost,':',@esport,'/aws_ecstaskdefinition/ecstaskdefinition'),'2022-05-06','2022-05-06','Infra & Platforms');
INSERT IGNORE INTO cf_Target (targetName,displayName,targetDesc,category,dataSourceName,targetConfig,status,userId,endpoint,createdDate,modifiedDate,domain) VALUES ('ecscluster','ECS Clusters','AWS Clusters','Compute','aws','{\"key\":\"clusterarn\",\"id\":\"clusterarn\"}','enabled','admin@pacbot.org',concat(@eshost,':',@esport,'/aws_ecscluster/ecscluster'),'2022-05-06','2022-05-06','Infra & Platforms');
INSERT IGNORE INTO cf_Target (targetName,displayName,targetDesc,category,dataSourceName,targetConfig,status,userId,endpoint,createdDate,modifiedDate,domain) VALUES ('accessanalyzer','IAM Access Analyzer','AWS Access Analyzer','Application Services','aws','{\"key\":\"analyzerarn\",\"id\":\"analyzerarn\"}','enabled','admin@pacbot.org',concat(@eshost,':',@esport,'/aws_accessanalyzer/accessanalyzer'),'2022-05-06','2022-05-06','Infra & Platforms');
INSERT IGNORE INTO cf_Target (targetName,displayName,targetDesc,category,dataSourceName,targetConfig,status,userId,endpoint,createdDate,modifiedDate,domain) VALUES ('ami','AMI','AWS EC2 AMI','Compute','aws','{\"key\":\"imageid\",\"id\":\"imageid\"}','enabled','admin@pacbot.org',concat(@eshost,':',@esport,'/aws_ami/ami'),'2022-05-06','2022-05-06','Infra & Platforms');
INSERT IGNORE INTO cf_Target (targetName,displayName,targetDesc,category,dataSourceName,targetConfig,status,userId,endpoint,createdDate,modifiedDate,domain) VALUES ('iampolicies','IAM Customer Managed Policy','IAM Customer Managed Policies','Identity','aws','{"key":"policyarn","id":"policyarn"}','enabled','admin@paladincloud.io',concat(@eshost,':',@esport,'/aws_iampolicies/iampolicies'),{d '2022-08-17'},{d '2022-08-17'},'Infra & Platforms');
INSERT IGNORE INTO cf_Target (targetName,displayName,targetDesc,category,dataSourceName,targetConfig,status,userId,endpoint,createdDate,modifiedDate,domain) VALUES ('ecr','ECR','ecr','Other','aws','{"key":"accountid,region,repositoryArn","id":"repositoryArn"}','enabled',null,concat(@eshost,':',@esport,'/aws_ecr/ecr'),{d '2023-03-09'},{d '2023-03-09'},'Infra & Platforms');
INSERT IGNORE INTO `cf_Target` (`targetName`,`displayName`,`targetDesc`,`category`,`dataSourceName`,`targetConfig`,`status`,`userId`,`endpoint`,`createdDate`,`modifiedDate`,`domain`) VALUES ('bigquerytable','BigQuery Table','GCP Bigquery Dataset table','BigQuery','gcp','{"key":"id","id":"id"}','enabled','admin',concat(@eshost,':',@esport,'/gcp_bigquerytable'),'2022-06-24','2022-06-24','Infra & Platforms');
INSERT IGNORE INTO `cf_Target` (`targetName`,`displayName`,`targetDesc`,`category`,`dataSourceName`,`targetConfig`,`status`,`userId`,`endpoint`,`createdDate`,`modifiedDate`,`domain`) VALUES ('bigquerydataset','BigQuery Dataset','GCP Bigquery Dataset','BigQuery','gcp','{"key":"id","id":"id"}','enabled','admin',concat(@eshost,':',@esport,'/gcp_bigquerydataset'),'2022-06-23','2022-06-23','Infra & Platforms');
INSERT IGNORE INTO `cf_Target` (`targetName`,`displayName`,`targetDesc`,`category`,`dataSourceName`,`targetConfig`,`status`,`userId`,`endpoint`,`createdDate`,`modifiedDate`,`domain`) VALUES ('cloudsql','Cloud SQL','GCP Cloud SQL','CloudSQL','gcp','{"key":"id","id":"id"}','enabled','admin',concat(@eshost,':',@esport,'/gcp_cloudsql'),'2022-06-29','2022-06-29','Infra & Platforms');
INSERT IGNORE INTO `cf_Target` (`targetName`,`displayName`,`targetDesc`,`category`,`dataSourceName`,`targetConfig`,`status`,`userId`,`endpoint`,`createdDate`,`modifiedDate`,`domain`) VALUES ('kmskey','KMS Key','GCP Cloud Key Management Service','KMSKey','gcp','{"key":"id","id":"id"}','enabled','admin',concat(@eshost,':',@esport,'/gcp_kmskey'),'2022-07-14','2022-07-14','Infra & Platforms');
INSERT IGNORE INTO `cf_Target` (`targetName`,`displayName`, `targetDesc`, `category`, `dataSourceName`, `targetConfig`, `status`, `userId`, `endpoint`, `createdDate`, `modifiedDate`, `domain`) VALUES('batchaccounts','Batch Account','Azure batchaccounts','Compute','azure','{\"key\":\"id\",\"id\":\"id\"}','enabled','admin@pacbot.org',concat(@eshost,':',@esport,'/azure_batchaccounts/batchaccounts'),'2019-09-19','2019-09-19','Infra & Platforms');
INSERT IGNORE INTO `cf_Target` (`targetName`,`displayName`, `targetDesc`, `category`, `dataSourceName`, `targetConfig`, `status`, `userId`, `endpoint`, `createdDate`, `modifiedDate`, `domain`) VALUES('blobcontainer','Blob Container','Azure blobcontainer','Storage','azure','{\"key\":\"id\",\"id\":\"id\"}','enabled','admin@pacbot.org',concat(@eshost,':',@esport,'/azure_blobcontainer/blobcontainer'),'2019-06-27','2019-06-27','Infra & Platforms');
INSERT IGNORE INTO `cf_Target` (`targetName`,`displayName`, `targetDesc`, `category`, `dataSourceName`, `targetConfig`, `status`, `userId`, `endpoint`, `createdDate`, `modifiedDate`, `domain`) VALUES('cosmosdb','Cosmos DB','Azure cosmosdb)','Databases','azure','{\"key\":\"id\",\"id\":\"id\"}','enabled','admin@pacbot.org',concat(@eshost,':',@esport,'/azure_cosmosdb/cosmosdb'),'2019-06-27','2019-06-27','Infra & Platforms');
INSERT IGNORE INTO `cf_Target` (`targetName`,`displayName`, `targetDesc`, `category`, `dataSourceName`, `targetConfig`, `status`, `userId`, `endpoint`, `createdDate`, `modifiedDate`, `domain`) VALUES('databricks','Databricks','Azure databricks)','Analytics','azure','{\"key\":\"id\",\"id\":\"id\"}','enabled','admin@pacbot.org',concat(@eshost,':',@esport,'/azure_databricks/databricks'),'2019-06-27','2019-06-27','Infra & Platforms');
INSERT IGNORE INTO `cf_Target` (`targetName`,`displayName`, `targetDesc`, `category`, `dataSourceName`, `targetConfig`, `status`, `userId`, `endpoint`, `createdDate`, `modifiedDate`, `domain`) VALUES('disk','Managed Disk','Azure Disk','Compute','azure','{\"key\":\"id\",\"id\":\"id\"}','enabled',NULL,concat(@eshost,':',@esport,'/azure_disk/disk'),'2019-11-05','2019-11-05','Infra & Platforms');
INSERT IGNORE INTO `cf_Target` (`targetName`,`displayName`, `targetDesc`, `category`, `dataSourceName`, `targetConfig`, `status`, `userId`, `endpoint`, `createdDate`, `modifiedDate`, `domain`) VALUES('loadbalancer','Load Balancer','Azure Loadbalancer','Networking','azure','{\"key\":\"id\",\"id\":\"id\"}','enabled',NULL,concat(@eshost,':',@esport,'/azure_loadbalancer/loadbalancer'),'2019-11-05','2019-11-05','Infra & Platforms');
INSERT IGNORE INTO `cf_Target` (`targetName`,`displayName`, `targetDesc`, `category`, `dataSourceName`, `targetConfig`, `status`, `userId`, `endpoint`, `createdDate`, `modifiedDate`, `domain`) VALUES('mariadb','MariaDB','Azure mariadb','Databases','azure','{\"key\":\"id\",\"id\":\"id\"}','enabled','admin@pacbot.org',concat(@eshost,':',@esport,'/azure_mariadb/mariadb'),'2019-06-27','2019-06-27','Infra & Platforms');
INSERT IGNORE INTO `cf_Target` (`targetName`,`displayName`, `targetDesc`, `category`, `dataSourceName`, `targetConfig`, `status`, `userId`, `endpoint`, `createdDate`, `modifiedDate`, `domain`) VALUES('mysqlserver','MySQL Server','Azure mysqlserver','Databases','azure','{\"key\":\"id\",\"id\":\"id\"}','enabled','admin@pacbot.org',concat(@eshost,':',@esport,'/azure_mysqlserver/mysqlserver'),'2019-06-27','2019-06-27','Infra & Platforms');
INSERT IGNORE INTO `cf_Target` (`targetName`,`displayName`, `targetDesc`, `category`, `dataSourceName`, `targetConfig`, `status`, `userId`, `endpoint`, `createdDate`, `modifiedDate`, `domain`) VALUES('namespaces','Service Bus Namespace','Azure namespaces','Web','azure','{\"key\":\"id\",\"id\":\"id\"}','enabled','admin@pacbot.org',concat(@eshost,':',@esport,'/azure_namespaces/namespaces'),'2019-09-19','2019-09-19','Infra & Platforms');
INSERT IGNORE INTO `cf_Target` (`targetName`,`displayName`, `targetDesc`, `category`, `dataSourceName`, `targetConfig`, `status`, `userId`, `endpoint`, `createdDate`, `modifiedDate`, `domain`) VALUES('networkinterface','Network Interface','Azure Network Interface','Networking','azure','{\"key\":\"id\",\"id\":\"id\"}','enabled',NULL,concat(@eshost,':',@esport,'/azure_networkinterface/networkinterface'),'2019-11-05','2019-11-05','Infra & Platforms');
INSERT IGNORE INTO `cf_Target` (`targetName`,`displayName`, `targetDesc`, `category`, `dataSourceName`, `targetConfig`, `status`, `userId`, `endpoint`, `createdDate`, `modifiedDate`, `domain`) VALUES('nsg','NSG','Azure Network Security Group','Networking','azure','{\"key\":\"id\",\"id\":\"id\"}','enabled',NULL,concat(@eshost,':',@esport,'/azure_nsg/nsg'),'2019-11-05','2019-11-05','Infra & Platforms');
INSERT IGNORE INTO `cf_Target` (`targetName`,`displayName`, `targetDesc`, `category`, `dataSourceName`, `targetConfig`, `status`, `userId`, `endpoint`, `createdDate`, `modifiedDate`, `domain`) VALUES('policydefinitions','Policy Definition','Azure policydefinitions','Governance','azure','{\"key\":\"id\",\"id\":\"id\"}','finding','admin@pacbot.org',concat(@eshost,':',@esport,'/azure_policydefinitions/policydefinitions'),'2019-08-08','2019-08-08','Infra & Platforms');
INSERT IGNORE INTO `cf_Target` (`targetName`,`displayName`, `targetDesc`, `category`, `dataSourceName`, `targetConfig`, `status`, `userId`, `endpoint`, `createdDate`, `modifiedDate`, `domain`) VALUES('policyevaluationresults','Policy Evaluation Result','Azure policyevaluationresults','Governance','azure','{\"key\":\"id,policyDefinitionId\",\"id\":\"id\"}','finding','admin@pacbot.org',concat(@eshost,':',@esport,'/azure_policyevaluationresults/policyevaluationresults'),'2019-08-08','2019-08-08','Infra & Platforms');
INSERT IGNORE INTO `cf_Target` (`targetName`,`displayName`, `targetDesc`, `category`, `dataSourceName`, `targetConfig`, `status`, `userId`, `endpoint`, `createdDate`, `modifiedDate`, `domain`) VALUES('postgresql','Postgre SQL','Azure postgresql','Databases','azure','{\"key\":\"id\",\"id\":\"id\"}','enabled','admin@pacbot.org',concat(@eshost,':',@esport,'/azure_postgresql/postgresql'),'2019-06-27','2019-06-27','Infra & Platforms');
INSERT IGNORE INTO `cf_Target` (`targetName`,`displayName`, `targetDesc`, `category`, `dataSourceName`, `targetConfig`, `status`, `userId`, `endpoint`, `createdDate`, `modifiedDate`, `domain`) VALUES('publicipaddress','Public IP Address','Azure publicipaddress','Networking','azure','{\"key\":\"id\",\"id\":\"id\"}','enabled','admin@pacbot.org',concat(@eshost,':',@esport,'/azure_publicipaddress/publicipaddress'),'2019-07-01','2019-07-01','Infra & Platforms');
INSERT IGNORE INTO `cf_Target` (`targetName`,`displayName`, `targetDesc`, `category`, `dataSourceName`, `targetConfig`, `status`, `userId`, `endpoint`, `createdDate`, `modifiedDate`, `domain`) VALUES('resourcegroup','Resource Group','Azure resourcegroup','General','azure','{\"key\":\"id\",\"id\":\"id\"}','enabled','admin@pacbot.org',concat(@eshost,':',@esport,'/azure_resourcegroup/resourcegroup'),'2019-06-27','2019-06-27','Infra & Platforms');
INSERT IGNORE INTO `cf_Target` (`targetName`,`displayName`, `targetDesc`, `category`, `dataSourceName`, `targetConfig`, `status`, `userId`, `endpoint`, `createdDate`, `modifiedDate`, `domain`) VALUES('searchservices','Cognitive Search','Azure searchservices','Web','azure','{\"key\":\"id\",\"id\":\"id\"}','enabled','admin@pacbot.org',concat(@eshost,':',@esport,'/azure_searchservices/searchservices'),'2019-09-19','2019-09-19','Infra & Platforms');
INSERT IGNORE INTO `cf_Target` (`targetName`,`displayName`, `targetDesc`, `category`, `dataSourceName`, `targetConfig`, `status`, `userId`, `endpoint`, `createdDate`, `modifiedDate`, `domain`) VALUES('securityalerts','Security Alerts','Azure securityalerts','Governance','azure','{\"key\":\"id\",\"id\":\"id\"}','enabled','admin@pacbot.org',concat(@eshost,':',@esport,'/azure_securityalerts/securityalerts'),'2019-08-08','2019-08-08','Infra & Platforms');
INSERT IGNORE INTO `cf_Target` (`targetName`,`displayName`, `targetDesc`, `category`, `dataSourceName`, `targetConfig`, `status`, `userId`, `endpoint`, `createdDate`, `modifiedDate`, `domain`) VALUES('securitycenter','Security Center','Azure Security Center','Security','azure','{\"key\":\"id\",\"id\":\"id\"}','enabled',NULL,concat(@eshost,':',@esport,'/azure_securitycenter/securitycenter'),'2019-11-05','2019-11-05','Infra & Platforms');
INSERT IGNORE INTO `cf_Target` (`targetName`,`displayName`, `targetDesc`, `category`, `dataSourceName`, `targetConfig`, `status`, `userId`, `endpoint`, `createdDate`, `modifiedDate`, `domain`) VALUES('sites','sites','Azure sites','Internet of things','azure','{\"key\":\"id\",\"id\":\"id\"}','enabled','admin@pacbot.org',concat(@eshost,':',@esport,'/azure_sites/sites'),'2019-09-19','2019-09-19','Infra & Platforms');
INSERT IGNORE INTO `cf_Target` (`targetName`,`displayName`, `targetDesc`, `category`, `dataSourceName`, `targetConfig`, `status`, `userId`, `endpoint`, `createdDate`, `modifiedDate`, `domain`) VALUES('sqldatabase','SQL Database','Azure SQL Database','Databases','azure','{\"key\":\"databaseId\",\"id\":\"databaseId\"}','enabled',NULL,concat(@eshost,':',@esport,'/azure_sqldatabase/sqldatabase'),'2019-11-05','2019-11-05','Infra & Platforms');
INSERT IGNORE INTO `cf_Target` (`targetName`,`displayName`, `targetDesc`, `category`, `dataSourceName`, `targetConfig`, `status`, `userId`, `endpoint`, `createdDate`, `modifiedDate`, `domain`) VALUES('sqlserver','SQL Server','Azure sqlserver','Databases','azure','{\"key\":\"id\",\"id\":\"id\"}','enabled',NULL,concat(@eshost,':',@esport,'/azure_sqlserver/sqlserver'),'2019-11-05','2019-11-05','Infra & Platforms');
INSERT IGNORE INTO `cf_Target` (`targetName`,`displayName`, `targetDesc`, `category`, `dataSourceName`, `targetConfig`, `status`, `userId`, `endpoint`, `createdDate`, `modifiedDate`, `domain`) VALUES('storageaccount','Storage Account','Azure Object Storage Accounts','Storage','azure','{\"key\":\"id\",\"id\":\"id\"}','enabled',NULL,concat(@eshost,':',@esport,'/azure_storageaccount/storageaccount'),'2019-11-05','2019-11-05','Infra & Platforms');
INSERT IGNORE INTO `cf_Target` (`targetName`,`displayName`, `targetDesc`, `category`, `dataSourceName`, `targetConfig`, `status`, `userId`, `endpoint`, `createdDate`, `modifiedDate`, `domain`) VALUES('subnets','Subnet','Azure subnets','Networking','azure','{\"key\":\"id\",\"id\":\"id\"}','enabled','admin@pacbot.org',concat(@eshost,':',@esport,'/azure_subnets/subnets'),'2019-09-19','2019-09-19','Infra & Platforms');
INSERT IGNORE INTO `cf_Target` (`targetName`,`displayName`, `targetDesc`, `category`, `dataSourceName`, `targetConfig`, `status`, `userId`, `endpoint`, `createdDate`, `modifiedDate`, `domain`) VALUES('vaults','Key Vault','Azure vaults','Security','azure','{\"key\":\"id\",\"id\":\"id\"}','enabled','admin@pacbot.org',concat(@eshost,':',@esport,'/azure_vaults/vaults'),'2019-09-19','2019-09-19','Infra & Platforms');
INSERT IGNORE INTO `cf_Target` (`targetName`,`displayName`, `targetDesc`, `category`, `dataSourceName`, `targetConfig`, `status`, `userId`, `endpoint`, `createdDate`, `modifiedDate`, `domain`) VALUES('virtualmachine','VM','Azure Virtual Machines','Compute','azure','{\"key\":\"vmId\",\"id\":\"id\"}','enabled',NULL,concat(@eshost,':',@esport,'/azure_virtualmachine/virtualmachine'),'2019-11-05','2019-11-05','Infra & Platforms');
INSERT IGNORE INTO `cf_Target` (`targetName`,`displayName`, `targetDesc`, `category`, `dataSourceName`, `targetConfig`, `status`, `userId`, `endpoint`, `createdDate`, `modifiedDate`, `domain`) VALUES('vnet','Virtual Network','Azure Disk','Networking','azure','{\"key\":\"id\",\"id\":\"id\"}','enabled',NULL,concat(@eshost,':',@esport,'/azure_vnet/vnet'),'2019-11-05','2019-11-05','Infra & Platforms');
INSERT IGNORE INTO `cf_Target` (`targetName`,`displayName`, `targetDesc`, `category`, `dataSourceName`, `targetConfig`, `status`, `userId`, `endpoint`, `createdDate`, `modifiedDate`, `domain`) VALUES('workflows','Work Flow','Azure workflows','Internet of things','azure','{\"key\":\"id\",\"id\":\"id\"}','enabled','admin@pacbot.org',concat(@eshost,':',@esport,'/azure_workflows/workflows'),'2019-09-19','2019-09-19','Infra & Platforms');
INSERT IGNORE INTO `cf_Target` (`targetName`,`displayName`, `targetDesc`, `category`, `dataSourceName`, `targetConfig`, `status`, `userId`, `endpoint`, `createdDate`, `modifiedDate`, `domain`) VALUES('activitylogalert','Activity Log Alert','Azure activitylog','','azure','{\"key\":\"id\",\"id\":\"id\"}','finding','admin@pacbot.org',concat(@eshost,':',@esport,'/azure_activitylog/activitylog'),'2019-06-27','2019-06-27','Infra & Platforms');
INSERT IGNORE INTO `cf_Target` (`targetName`,`displayName`, `targetDesc`, `category`, `dataSourceName`, `targetConfig`, `status`, `userId`, `endpoint`, `createdDate`, `modifiedDate`, `domain`) VALUES('rediscache','Redis Cache','Azure rediscache','','azure','{\"key\":\"id\",\"id\":\"id\"}','enabled','admin@pacbot.org',concat(@eshost,':',@esport,'/azure_rediscache/rediscache'),'2022-05-25','2022-05-25','Infra & Platforms');
INSERT IGNORE INTO `cf_Target` (`targetName`,`displayName`, `targetDesc`, `category`, `dataSourceName`, `targetConfig`, `status`, `userId`, `endpoint`, `createdDate`, `modifiedDate`, `domain`) VALUES('securitypricings','Security Pricing','Azure Security Pricing','Security','azure','{\"key\":\"id\",\"id\":\"id\"}','enabled','admin@pacbot.org',concat(@eshost,':',@esport,'/azure_securitypricings/securitypricings'),'2022-05-19','2022-05-19','Infra & Platforms');
INSERT IGNORE INTO `cf_Target` (`targetName`,`displayName`, `targetDesc`, `category`, `dataSourceName`, `targetConfig`, `status`, `userId`, `endpoint`, `createdDate`, `modifiedDate`, `domain`) VALUES('webapp','Web App','Azure webapp','','azure','{\"key\":\"id\",\"id\":\"id\"}','enabled','admin@pacbot.org',concat(@eshost,':',@esport,'/azure_webapp/webapp'),'2019-06-27','2019-06-27','Infra & Platforms');
INSERT IGNORE INTO `cf_Target` (`targetName`,`displayName`,`targetDesc`,`category`,`dataSourceName`,`targetConfig`,`status`,`userId`,`endpoint`,`createdDate`,`modifiedDate`,`domain`) VALUES ('vpcfirewall','VPC Firewall','GCP VPC firewall','Compute','gcp','{"key":"id","id":"name"}','enabled','admin',concat(@eshost,':',@esport,'/gcp_vpcfirewall/vpcfirewall'),'2022-06-01','2022-06-01','Infra & Platforms');
INSERT IGNORE INTO `cf_Target` (`targetName`,`displayName`,`targetDesc`,`category`,`dataSourceName`,`targetConfig`,`status`,`userId`,`endpoint`,`createdDate`,`modifiedDate`,`domain`) VALUES ('cloudstorage','Cloud Storage','GCP cloud storage','Compute','gcp','{"key":"id","id":"id"}','enabled','admin',concat(@eshost,':',@esport,'/gcp_cloudstorage/cloudstorage'),'2022-06-01','2022-06-01','Infra & Platforms');
INSERT IGNORE INTO `cf_Target` (`targetName`,`displayName`,`targetDesc`,`category`,`dataSourceName`,`targetConfig`,`status`,`userId`,`endpoint`,`createdDate`,`modifiedDate`,`domain`) VALUES ('pubsub','Pub/Sub','GCP pub sub topic','Compute','gcp','{"key":"id","id":"id"}','enabled','admin',concat(@eshost,':',@esport,'/gcp_pubsub/pubsub'),'2022-06-29','2022-06-29','Infra & Platforms');
INSERT IGNORE INTO `cf_Target` (`targetName`,`displayName`,`targetDesc`,`category`,`dataSourceName`,`targetConfig`,`status`,`userId`,`endpoint`,`createdDate`,`modifiedDate`,`domain`) VALUES ('dataproc','Dataproc Cluster','GCP dataproc clusters','Compute','gcp','{"key":"id","id":"id"}','enabled','admin',concat(@eshost,':',@esport,'/gcp_dataproc/dataproc'),'2022-07-19','2022-07-19','Infra & Platforms');
INSERT IGNORE INTO `cf_Target` (`targetName`,`displayName`,`targetDesc`,`category`,`dataSourceName`,`targetConfig`,`status`,`userId`,`endpoint`,`createdDate`,`modifiedDate`,`domain`) VALUES ('clouddns','Cloud DNS','GCP Cloud DNS','Google Cloud','gcp','{"key":"id","id":"id"}','enabled','admin',concat(@eshost,':',@esport,'/gcp_clouddns/clouddns'),'2022-06-01','2022-06-01','Infra & Platforms');
INSERT IGNORE INTO `cf_Target` (`targetName`,`displayName`,`targetDesc`,`category`,`dataSourceName`,`targetConfig`,`status`,`userId`,`endpoint`,`createdDate`,`modifiedDate`,`domain`) VALUES ('cloudsql_sqlserver','SQL Server','GCP Cloud SQL Server','CloudSQLServer','gcp','{"key":"id","id":"id"}','enabled','admin',concat(@eshost,':',@esport,'/gcp_cloudsql_sqlserver'),'2022-06-29','2022-06-29','Infra & Platforms');
INSERT IGNORE INTO `cf_Target` (`targetName`,`displayName`,`targetDesc`,`category`,`dataSourceName`,`targetConfig`,`status`,`userId`,`endpoint`,`createdDate`,`modifiedDate`,`domain`) VALUES ('cloudsql_mysqlserver','MySQL Server','GCP Cloud MySQL Server','CloudMySQLServer','gcp','{"key":"id","id":"id"}','enabled','admin',concat(@eshost,':',@esport,'/gcp_cloudsql_mysqlserver'),'2022-10-04','2022-10-04','Infra & Platforms');
INSERT IGNORE INTO `cf_Target` (`targetName`,`displayName`,`targetDesc`,`category`,`dataSourceName`,`targetConfig`,`status`,`userId`,`endpoint`,`createdDate`,`modifiedDate`,`domain`) VALUES ('cloudsql_postgres','PostgreSQL','GCP Cloud postgres Server','CloudPostgresServer','gcp','{"key":"id","id":"id"}','enabled','admin',concat(@eshost,':',@esport,'/gcp_cloudsql_postgres'),'2022-10-07','2022-10-07','Infra & Platforms');
INSERT IGNORE INTO `cf_Target` (`targetName`,`displayName`,`targetDesc`,`category`,`dataSourceName`,`targetConfig`,`status`,`userId`,`endpoint`,`createdDate`,`modifiedDate`,`domain`) VALUES ('networks','Cloud VPC','GCP CloudVPC','CloudVPC','gcp','{"key":"id","id":"id"}','enabled','admin',concat(@eshost,':',@esport,'/gcp_networks'),'2022-06-29','2022-06-29','Infra & Platforms');
INSERT IGNORE INTO `cf_Target` (`targetName`,`displayName`,`targetDesc`,`category`,`dataSourceName`,`targetConfig`,`status`,`userId`,`endpoint`,`createdDate`,`modifiedDate`,`domain`) VALUES ('project','Project','Project data','Tagging','gcp','{"key":"id","id":"id"}','enabled','admin',concat(@eshost,':',@esport,'/gcp_project'),'2022-06-29','2022-06-29','Infra & Platforms');
INSERT IGNORE INTO `cf_Target` (`targetName`,`displayName`, `targetDesc`, `category`, `dataSourceName`, `targetConfig`, `status`, `userId`, `endpoint`, `createdDate`, `modifiedDate`, `domain`) VALUES('blobservice','Blob Service','Azure blobService','Storage','azure','{\"key\":\"id\",\"id\":\"id\"}','enabled','admin@pacbot.org',concat(@eshost,':',@esport,'/azure_blobservice/blobservice'),'2022-09-15','2022-09-15','Infra & Platforms');
INSERT IGNORE INTO `cf_Target` (`targetName`,`displayName`, `targetDesc`, `category`, `dataSourceName`, `targetConfig`, `status`, `userId`, `endpoint`, `createdDate`, `modifiedDate`, `domain`) VALUES('securityhub','Security Hub','Security Hub','Security','aws','{\"key\":\"hubarn\",\"id\":\"hubarn\"}','finding','admin@pacbot.org',concat(@eshost,':',@esport,'/aws_securityhub/securityhub'),'2022-10-27','2022-10-27','Infra & Platforms');
INSERT IGNORE INTO `cf_Target` (`targetName`,`displayName`, `targetDesc`, `category`, `dataSourceName`, `targetConfig`, `status`, `userId`, `endpoint`, `createdDate`, `modifiedDate`, `domain`) VALUES('backupvault','Backup Vault','AWS Backup Vault','storage','aws', '{"key":"backupVaultArn","id":"backupVaultArn"}','active','admin@pacbot.org',concat(@eshost,':',@esport,'/aws_backupvault/backupvault'),'2017-11-29','2017-11-29','Infra & Platforms');
INSERT IGNORE INTO `cf_Target` (`targetName`,`displayName`, `targetDesc`, `category`, `dataSourceName`, `targetConfig`, `status`, `userId`, `endpoint`, `createdDate`, `modifiedDate`, `domain`) VALUES('sqs','Simple Queue Service','Simple Queue Service','QueuingService','aws','{"key":"queuearn","id":"queuearn"}','active','admin@pacbot.org',concat(@eshost,':',@esport,'/aws_sqs/sqs'),'2017-11-29','2017-11-29','Infra & Platforms');

INSERT IGNORE INTO `cf_Target` (`targetName`,`displayName`,`targetDesc`,`category`,`dataSourceName`,`targetConfig`,`status`,`userId`,`endpoint`,`createdDate`,`modifiedDate`,`domain`) VALUES ('serviceaccounts','Service Accounts','Service Account','Security','gcp','{"key":"id","id":"id"}','enabled','admin',concat(@eshost,':',@esport,'/gcp_serviceaccounts'),'2022-06-29','2022-06-29','Infra & Platforms');
INSERT IGNORE INTO `cf_Target` (`targetName`,`displayName`, `targetDesc`, `category`, `dataSourceName`, `targetConfig`, `status`, `userId`, `endpoint`, `createdDate`, `modifiedDate`, `domain`) VALUES('defender','Defender for Cloud','Azure defender','Governance','azure','{\"key\":\"id\",\"id\":\"id\"}','enabled','admin@pacbot.org',concat(@eshost,':',@esport,'/azure_defender/defender'),'2022-10-14','2022-10-14','Infra & Platforms');
INSERT IGNORE INTO `cf_Target` (`targetName`,`displayName`, `targetDesc`, `category`, `dataSourceName`, `targetConfig`, `status`, `userId`, `endpoint`, `createdDate`, `modifiedDate`, `domain`) VALUES('kubernetes','AKS','Azure kubernetes cluster','Governance','azure','{\"key\":\"id\",\"id\":\"id\"}','enabled','admin@pacbot.org',concat(@eshost,':',@esport,'/azure_kubernetes/kubernetes'),'2022-10-27','2022-10-27','Infra & Platforms');
INSERT IGNORE INTO `cf_Target` (`targetName`,`displayName`, `targetDesc`, `category`, `dataSourceName`, `targetConfig`, `status`, `userId`, `endpoint`, `createdDate`, `modifiedDate`, `domain`) VALUES('apikeys','Api Keys','GCP API Keys','security','gcp','{\"key\":\"id\",\"id\":\"id\"}','enabled','admin@pacbot.org',concat(@eshost,':',@esport,'/gcp_apikeys/apikeys'),'2022-12-5','2022-12-5','Infra & Platforms');
INSERT IGNORE INTO `cf_Target` (`targetName`,`displayName`, `targetDesc`, `category`, `dataSourceName`, `targetConfig`, `status`, `userId`, `endpoint`, `createdDate`, `modifiedDate`, `domain`) VALUES('iamusers','IAMUser','collects IAMUser details','security','gcp','{\"key\":\"id\",\"id\":\"id\"}','enabled','admin@pacbot.org',concat(@eshost,':',@esport,'/gcp_iamusers/iamusers'),'2019-09-19','2019-09-19','Infra & Platforms');
INSERT IGNORE INTO `cf_Target` (`targetName`,`displayName`,`targetDesc`,`category`,`dataSourceName`,`targetConfig`,`status`,`userId`,`endpoint`,`createdDate`,`modifiedDate`,`domain`) VALUES ('gcploadbalancer','GCP Load balancer','load balancer data','Security','gcp','{"key":"id","id":"id"}','enabled','admin',concat(@eshost,':',@esport,'/gcp_loadbalancer'),'2022-12-07','2022-12-07','Infra & Platforms');


INSERT IGNORE INTO cf_Target (`targetName`,`targetDesc`,`displayName`,`category`,`dataSourceName`,`targetConfig`,`status`,`userId`,`endpoint`,`createdDate`,`modifiedDate`,`domain`) VALUES ('cloudfunction','GCP Cloud Functions','GCP cloud functions','Security','gcp','{"key":"id","id":"id"}','enabled','admin',concat(@eshost,':',@esport,'/gcp_cloudfunction'),'2023-01-10','2023-01-10','Infra & Platforms');
INSERT IGNORE INTO cf_Target (`targetName`,`targetDesc`,`displayName`,`category`,`dataSourceName`,`targetConfig`,`status`,`userId`,`endpoint`,`createdDate`,`modifiedDate`,`domain`) VALUES ('cloudfunctiongen1','GCP Cloud Functions Generation 1','GCP cloud functions Generation 1','Security','gcp','{"key":"id","id":"id"}','enabled','admin',concat(@eshost,':',@esport,'/gcp_cloudfunctiongen1'),'2023-01-10','2023-01-10','Infra & Platforms');

INSERT IGNORE INTO cf_AssetGroupTargetDetails (id_,groupId,targetType,attributeName,attributeValue) VALUES ('11501','201','ec2','all','all');
INSERT IGNORE INTO cf_AssetGroupTargetDetails (id_,groupId,targetType,attributeName,attributeValue) VALUES ('11502','201','s3','all','all');
INSERT IGNORE INTO cf_AssetGroupTargetDetails (id_,groupId,targetType,attributeName,attributeValue) VALUES ('11503','201','appelb','all','all');
INSERT IGNORE INTO cf_AssetGroupTargetDetails (id_,groupId,targetType,attributeName,attributeValue) VALUES ('11504','201','asg','all','all');
INSERT IGNORE INTO cf_AssetGroupTargetDetails (id_,groupId,targetType,attributeName,attributeValue) VALUES ('11505','201','classicelb','all','all');
INSERT IGNORE INTO cf_AssetGroupTargetDetails (id_,groupId,targetType,attributeName,attributeValue) VALUES ('11506','201','stack','all','all');
INSERT IGNORE INTO cf_AssetGroupTargetDetails (id_,groupId,targetType,attributeName,attributeValue) VALUES ('11507','201','dynamodb','all','all');
INSERT IGNORE INTO cf_AssetGroupTargetDetails (id_,groupId,targetType,attributeName,attributeValue) VALUES ('11508','201','efs','all','all');
INSERT IGNORE INTO cf_AssetGroupTargetDetails (id_,groupId,targetType,attributeName,attributeValue) VALUES ('11509','201','emr','all','all');
INSERT IGNORE INTO cf_AssetGroupTargetDetails (id_,groupId,targetType,attributeName,attributeValue) VALUES ('11510','201','lambda','all','all');
INSERT IGNORE INTO cf_AssetGroupTargetDetails (id_,groupId,targetType,attributeName,attributeValue) VALUES ('11511','201','nat','all','all');
INSERT IGNORE INTO cf_AssetGroupTargetDetails (id_,groupId,targetType,attributeName,attributeValue) VALUES ('11512','201','eni','all','all');
INSERT IGNORE INTO cf_AssetGroupTargetDetails (id_,groupId,targetType,attributeName,attributeValue) VALUES ('11513','201','rdscluster','all','all');
INSERT IGNORE INTO cf_AssetGroupTargetDetails (id_,groupId,targetType,attributeName,attributeValue) VALUES ('11514','201','rdsdb','all','all');
INSERT IGNORE INTO cf_AssetGroupTargetDetails (id_,groupId,targetType,attributeName,attributeValue) VALUES ('11515','201','redshift','all','all');
INSERT IGNORE INTO cf_AssetGroupTargetDetails (id_,groupId,targetType,attributeName,attributeValue) VALUES ('11516','201','sg','all','all');
INSERT IGNORE INTO cf_AssetGroupTargetDetails (id_,groupId,targetType,attributeName,attributeValue) VALUES ('11517','201','snapshot','all','all');
INSERT IGNORE INTO cf_AssetGroupTargetDetails (id_,groupId,targetType,attributeName,attributeValue) VALUES ('11518','201','subnet','all','all');
INSERT IGNORE INTO cf_AssetGroupTargetDetails (id_,groupId,targetType,attributeName,attributeValue) VALUES ('11519','201','targetgroup','all','all');
INSERT IGNORE INTO cf_AssetGroupTargetDetails (id_,groupId,targetType,attributeName,attributeValue) VALUES ('11520','201','volume','all','all');
INSERT IGNORE INTO cf_AssetGroupTargetDetails (id_,groupId,targetType,attributeName,attributeValue) VALUES ('11521','201','vpc','all','all');
INSERT IGNORE INTO cf_AssetGroupTargetDetails (id_,groupId,targetType,attributeName,attributeValue) VALUES ('11522','201','api','all','all');
INSERT IGNORE INTO cf_AssetGroupTargetDetails (id_,groupId,targetType,attributeName,attributeValue) VALUES ('11523','201','iamuser','all','all');
INSERT IGNORE INTO cf_AssetGroupTargetDetails (id_,groupId,targetType,attributeName,attributeValue) VALUES ('11526','201','iamrole','all','all');
INSERT IGNORE INTO cf_AssetGroupTargetDetails (id_,groupId,targetType,attributeName,attributeValue) VALUES ('11527','201','rdssnapshot','all','all');
INSERT IGNORE INTO cf_AssetGroupTargetDetails (id_,groupId,targetType,attributeName,attributeValue) VALUES ('11528','201','account','all','all');
INSERT IGNORE INTO cf_AssetGroupTargetDetails (id_,groupId,targetType,attributeName,attributeValue) VALUES ('11529','201','checks','all','all');
INSERT IGNORE INTO cf_AssetGroupTargetDetails (id_,groupId,targetType,attributeName,attributeValue) VALUES ('11530','201','kms','all','all');
INSERT IGNORE INTO cf_AssetGroupTargetDetails (id_,groupId,targetType,attributeName,attributeValue) VALUES ('11531','201','phd','all','all');
INSERT IGNORE INTO cf_AssetGroupTargetDetails (id_,groupId,targetType,attributeName,attributeValue) VALUES ('11532','201','cloudfront','all','all');
INSERT IGNORE INTO cf_AssetGroupTargetDetails (id_,groupId,targetType,attributeName,attributeValue) VALUES ('11533','201','cert','all','all');
INSERT IGNORE INTO cf_AssetGroupTargetDetails (id_,groupId,targetType,attributeName,attributeValue) VALUES ('11534','201','wafdomain','all','all');
INSERT IGNORE INTO cf_AssetGroupTargetDetails (id_,groupId,targetType,attributeName,attributeValue) VALUES ('11535','201','corpdomain','all','all');
INSERT IGNORE INTO cf_AssetGroupTargetDetails (id_,groupId,targetType,attributeName,attributeValue) VALUES ('11536','201','elasticip','all','all');
INSERT IGNORE INTO cf_AssetGroupTargetDetails (id_,groupId,targetType,attributeName,attributeValue) VALUES ('11537','201','routetable','all','all');
INSERT IGNORE INTO cf_AssetGroupTargetDetails (id_,groupId,targetType,attributeName,attributeValue) VALUES ('67701','201','internetgateway','all','all');
INSERT IGNORE INTO cf_AssetGroupTargetDetails (id_,groupId,targetType,attributeName,attributeValue) VALUES ('67702','201','launchconfig','all','all');
INSERT IGNORE INTO cf_AssetGroupTargetDetails (id_,groupId,targetType,attributeName,attributeValue) VALUES ('67703','201','networkacl','all','all');
INSERT IGNORE INTO cf_AssetGroupTargetDetails (id_,groupId,targetType,attributeName,attributeValue) VALUES ('67704','201','vpngateway','all','all');
INSERT IGNORE INTO cf_AssetGroupTargetDetails (id_,groupId,targetType,attributeName,attributeValue) VALUES ('67705','201','asgpolicy','all','all');
INSERT IGNORE INTO cf_AssetGroupTargetDetails (id_,groupId,targetType,attributeName,attributeValue) VALUES ('67706','201','snstopic','all','all');
INSERT IGNORE INTO cf_AssetGroupTargetDetails (id_,groupId,targetType,attributeName,attributeValue) VALUES ('67707','201','dhcpoption','all','all');
INSERT IGNORE INTO cf_AssetGroupTargetDetails (id_,groupId,targetType,attributeName,attributeValue) VALUES ('67708','201','peeringconnection','all','all');
INSERT IGNORE INTO cf_AssetGroupTargetDetails (id_,groupId,targetType,attributeName,attributeValue) VALUES ('67709','201','customergateway','all','all');
INSERT IGNORE INTO cf_AssetGroupTargetDetails (id_,groupId,targetType,attributeName,attributeValue) VALUES ('67710','201','vpnconnection','all','all');
INSERT IGNORE INTO cf_AssetGroupTargetDetails (id_,groupId,targetType,attributeName,attributeValue) VALUES ('67711','201','directconnect','all','all');
INSERT IGNORE INTO cf_AssetGroupTargetDetails (id_,groupId,targetType,attributeName,attributeValue) VALUES ('67712','201','virtualinterface','all','all');
INSERT IGNORE INTO cf_AssetGroupTargetDetails (id_,groupId,targetType,attributeName,attributeValue) VALUES ('67713','201','elasticsearch','all','all');
INSERT IGNORE INTO cf_AssetGroupTargetDetails (id_,groupId,targetType,attributeName,attributeValue) VALUES ('67714','201','elasticache','all','all');
INSERT IGNORE INTO cf_AssetGroupTargetDetails (id_,groupId,targetType,attributeName,attributeValue) VALUES ('71501','201','documentdb','all','all');
INSERT IGNORE INTO cf_AssetGroupTargetDetails (id_,groupId,targetType,attributeName,attributeValue) VALUES ('71502','201','dms','all','all');
INSERT IGNORE INTO cf_AssetGroupTargetDetails (id_,groupId,targetType,attributeName,attributeValue) VALUES ('71503','201','eks','all','all');
INSERT IGNORE INTO cf_AssetGroupTargetDetails (id_,groupId,targetType,attributeName,attributeValue) VALUES ('715014','201','daxcluster','all','all');
INSERT IGNORE INTO cf_AssetGroupTargetDetails (id_,groupId,targetType,attributeName,attributeValue) VALUES ('715015','201','awsathena','all','all');
INSERT IGNORE INTO cf_AssetGroupTargetDetails (id_,groupId,targetType,attributeName,attributeValue) VALUES ('715016','201','awscomprehend','all','all');
INSERT IGNORE INTO cf_AssetGroupTargetDetails (id_,groupId,targetType,attributeName,attributeValue) VALUES ('715017','201','appflow','all','all');
INSERT IGNORE INTO cf_AssetGroupTargetDetails (id_,groupId,targetType,attributeName,attributeValue) VALUES ('715018','201','ecstaskdefinition','all','all');
INSERT IGNORE INTO cf_AssetGroupTargetDetails (id_,groupId,targetType,attributeName,attributeValue) VALUES ('715125','201','ecscluster','all','all');
INSERT IGNORE INTO cf_AssetGroupTargetDetails (id_,groupId,targetType,attributeName,attributeValue) VALUES ('715019','201','accessanalyzer','all','all');
INSERT IGNORE INTO cf_AssetGroupTargetDetails (id_,groupId,targetType,attributeName,attributeValue) VALUES ('715020','201','ami','all','all');
INSERT IGNORE INTO cf_AssetGroupTargetDetails (id_,groupId,targetType,attributeName,attributeValue) VALUES ('715022','201','iampolicies','all','all');
INSERT IGNORE INTO cf_AssetGroupTargetDetails (id_,groupId,targetType,attributeName,attributeValue) VALUES ('715023','201','cloudwatchlogs','all','all');
INSERT IGNORE INTO cf_AssetGroupTargetDetails (id_,groupId,targetType,attributeName,attributeValue) VALUES ('715024','201','cloudwatchalarm','all','all');
INSERT IGNORE INTO cf_AssetGroupTargetDetails (id_,groupId,targetType,attributeName,attributeValue) VALUES ('815024','201','securityhub','all','all');
INSERT IGNORE INTO cf_AssetGroupTargetDetails (id_,groupId,targetType,attributeName,attributeValue) VALUES ('21501','201','backupvault','all','all');
INSERT IGNORE INTO cf_AssetGroupTargetDetails (id_,groupId,targetType,attributeName,attributeValue) VALUES ('21502','201','sqs','all','all');

INSERT IGNORE INTO `cf_AssetGroupTargetDetails` (`id_`, `groupId`, `targetType`, `attributeName`, `attributeValue`) VALUES('00021aac-d0e6-4481-a1e7-8460154482ca','cdffb9cd-71de-4e29-9cae-783c2aa211ac','virtualmachine','all','all');
INSERT IGNORE INTO `cf_AssetGroupTargetDetails` (`id_`, `groupId`, `targetType`, `attributeName`, `attributeValue`) VALUES('ad076972-5c61-4e02-8c4b-7619db880f7f','cdffb9cd-71de-4e29-9cae-783c2aa211ac','blobcontainer','all','all');
INSERT IGNORE INTO `cf_AssetGroupTargetDetails` (`id_`, `groupId`, `targetType`, `attributeName`, `attributeValue`) VALUES('a448c3a1-02c2-471d-a4b5-ea870eacbd12','cdffb9cd-71de-4e29-9cae-783c2aa211ac','cosmosdb','all','all');
INSERT IGNORE INTO `cf_AssetGroupTargetDetails` (`id_`, `groupId`, `targetType`, `attributeName`, `attributeValue`) VALUES('ac57da97-ad1b-4cd0-9add-e8d23d5eca03','cdffb9cd-71de-4e29-9cae-783c2aa211ac','databricks','all','all');
INSERT IGNORE INTO `cf_AssetGroupTargetDetails` (`id_`, `groupId`, `targetType`, `attributeName`, `attributeValue`) VALUES('abcf3e8a-9d11-42b3-9008-d548f1958d42','cdffb9cd-71de-4e29-9cae-783c2aa211ac','disk','all','all');
INSERT IGNORE INTO `cf_AssetGroupTargetDetails` (`id_`, `groupId`, `targetType`, `attributeName`, `attributeValue`) VALUES('9beb0437-3571-4732-ac97-6b6d8cc050e4','cdffb9cd-71de-4e29-9cae-783c2aa211ac','mariadb','all','all');
INSERT IGNORE INTO `cf_AssetGroupTargetDetails` (`id_`, `groupId`, `targetType`, `attributeName`, `attributeValue`) VALUES('abc56fc7-159d-4984-883e-bd3025b645b9','cdffb9cd-71de-4e29-9cae-783c2aa211ac','mysqlserver','all','all');
INSERT IGNORE INTO `cf_AssetGroupTargetDetails` (`id_`, `groupId`, `targetType`, `attributeName`, `attributeValue`) VALUES('abc4c00c-5fd4-4367-a899-62d7399d86ac','cdffb9cd-71de-4e29-9cae-783c2aa211ac','networkinterface','all','all');
INSERT IGNORE INTO `cf_AssetGroupTargetDetails` (`id_`, `groupId`, `targetType`, `attributeName`, `attributeValue`) VALUES('a4293ded-951e-4b01-8633-6a10ec4b9457','cdffb9cd-71de-4e29-9cae-783c2aa211ac','nsg','all','all');
INSERT IGNORE INTO `cf_AssetGroupTargetDetails` (`id_`, `groupId`, `targetType`, `attributeName`, `attributeValue`) VALUES('a4293ded-951e-4b01-8633-6a10ec4b9458','cdffb9cd-71de-4e29-9cae-783c2aa211ac','namespace','all','all');
INSERT IGNORE INTO `cf_AssetGroupTargetDetails` (`id_`, `groupId`, `targetType`, `attributeName`, `attributeValue`) VALUES('a32495ca-ffc5-48af-ba26-316e7cb90012','cdffb9cd-71de-4e29-9cae-783c2aa211ac','postgresql','all','all');
INSERT IGNORE INTO `cf_AssetGroupTargetDetails` (`id_`, `groupId`, `targetType`, `attributeName`, `attributeValue`) VALUES('a32495ca-ffc5-48af-ba26-316e7cb90013','cdffb9cd-71de-4e29-9cae-783c2aa211ac','publicipaddress','all','all');
INSERT IGNORE INTO `cf_AssetGroupTargetDetails` (`id_`, `groupId`, `targetType`, `attributeName`, `attributeValue`) VALUES('9d115e83-2821-4eeb-8224-ba2bbba1a5fa','cdffb9cd-71de-4e29-9cae-783c2aa211ac','resourcegroup','all','all');
INSERT IGNORE INTO `cf_AssetGroupTargetDetails` (`id_`, `groupId`, `targetType`, `attributeName`, `attributeValue`) VALUES('9d115e83-2821-4eeb-8224-ba2bbba1a5fb','cdffb9cd-71de-4e29-9cae-783c2aa211ac','searchservices','all','all');
INSERT IGNORE INTO `cf_AssetGroupTargetDetails` (`id_`, `groupId`, `targetType`, `attributeName`, `attributeValue`) VALUES('9d115e83-2821-4eeb-8224-ba2bbba1a5fc','cdffb9cd-71de-4e29-9cae-783c2aa211ac','securityalerts','all','all');
INSERT IGNORE INTO `cf_AssetGroupTargetDetails` (`id_`, `groupId`, `targetType`, `attributeName`, `attributeValue`) VALUES('a5eecbfc-4a0e-4113-8301-13a44e3522d7','cdffb9cd-71de-4e29-9cae-783c2aa211ac','securitycenter','all','all');
INSERT IGNORE INTO `cf_AssetGroupTargetDetails` (`id_`, `groupId`, `targetType`, `attributeName`, `attributeValue`) VALUES('a5eecbfc-4a0e-4113-8301-13a44e3522d8','cdffb9cd-71de-4e29-9cae-783c2aa211ac','sites','all','all');
INSERT IGNORE INTO `cf_AssetGroupTargetDetails` (`id_`, `groupId`, `targetType`, `attributeName`, `attributeValue`) VALUES('a61e23d5-7453-4bfe-b97c-27c706674e60','cdffb9cd-71de-4e29-9cae-783c2aa211ac','sqldatabase','all','all');
INSERT IGNORE INTO `cf_AssetGroupTargetDetails` (`id_`, `groupId`, `targetType`, `attributeName`, `attributeValue`) VALUES('aad7068e-e5d2-4171-8e65-634aedfba6b2','cdffb9cd-71de-4e29-9cae-783c2aa211ac','sqlserver','all','all');
INSERT IGNORE INTO `cf_AssetGroupTargetDetails` (`id_`, `groupId`, `targetType`, `attributeName`, `attributeValue`) VALUES('a1480aa8-7239-4604-9ab7-916621792fd6','cdffb9cd-71de-4e29-9cae-783c2aa211ac','storageaccount','all','all');
INSERT IGNORE INTO `cf_AssetGroupTargetDetails` (`id_`, `groupId`, `targetType`, `attributeName`, `attributeValue`) VALUES('a1480aa8-7239-4604-9ab7-916621792fd7','cdffb9cd-71de-4e29-9cae-783c2aa211ac','subnets','all','all');
INSERT IGNORE INTO `cf_AssetGroupTargetDetails` (`id_`, `groupId`, `targetType`, `attributeName`, `attributeValue`) VALUES('a1480aa8-7239-4604-9ab7-916621792fd8','cdffb9cd-71de-4e29-9cae-783c2aa211ac','vaults','all','all');
INSERT IGNORE INTO `cf_AssetGroupTargetDetails` (`id_`, `groupId`, `targetType`, `attributeName`, `attributeValue`) VALUES('a1480aa8-7239-4604-9ab7-916621792fd9','cdffb9cd-71de-4e29-9cae-783c2aa211ac','vnet','all','all');
INSERT IGNORE INTO `cf_AssetGroupTargetDetails` (`id_`, `groupId`, `targetType`, `attributeName`, `attributeValue`) VALUES('a1480aa8-7239-4604-9ab7-916621792f10','cdffb9cd-71de-4e29-9cae-783c2aa211ac','workflows','all','all');
INSERT IGNORE INTO `cf_AssetGroupTargetDetails` (`id_`, `groupId`, `targetType`, `attributeName`, `attributeValue`) VALUES('a1480aa8-7239-4604-9ab7-916621792f11','cdffb9cd-71de-4e29-9cae-783c2aa211ac','batchaccounts','all','all');
INSERT IGNORE INTO `cf_AssetGroupTargetDetails` (`id_`, `groupId`, `targetType`, `attributeName`, `attributeValue`) VALUES('a1480aa8-7239-4604-9ab7-916621792f12','cdffb9cd-71de-4e29-9cae-783c2aa211ac','loadbalancer','all','all');
INSERT IGNORE INTO `cf_AssetGroupTargetDetails` (`id_`, `groupId`, `targetType`, `attributeName`, `attributeValue`) VALUES('f043449a-3f6d-472c-8537-534a2e5a50ff','cdffb9cd-71de-4e29-9cae-783c2aa211ac','rediscache','all','all');
INSERT IGNORE INTO `cf_AssetGroupTargetDetails` (`id_`, `groupId`, `targetType`, `attributeName`, `attributeValue`) VALUES('f043449a-3f6d-472c-8537-534a2e5a50ff','cdffb9cd-71de-4e29-9cae-783c2aa211ac','activitylogalert','all','all');
INSERT IGNORE INTO `cf_AssetGroupTargetDetails` (`id_`, `groupId`, `targetType`, `attributeName`, `attributeValue`) VALUES('f043449a-3f6d-472c-8537-534a2e5a50ff','cdffb9cd-71de-4e29-9cae-783c2aa211ac','securitypricings','all','all');
INSERT IGNORE INTO `cf_AssetGroupTargetDetails` (`id_`, `groupId`, `targetType`, `attributeName`, `attributeValue`) VALUES('e33111d2-0ca0-4dbe-93d3-0e0aa2391ec0','cdffb9cd-71de-4e29-9cae-783c2aa211ac','webapp','all','all');
INSERT IGNORE INTO `cf_AssetGroupTargetDetails` (`id_`, `groupId`, `targetType`, `attributeName`, `attributeValue`) VALUES('e33111d2-0ca0-4dbe-93d3-0e0aa2391ec1','cdffb9cd-71de-4e29-9cae-783c2aa211ac','subscription','all','all');
INSERT IGNORE INTO `cf_AssetGroupTargetDetails` (`id_`, `groupId`, `targetType`, `attributeName`, `attributeValue`) VALUES('42c2f507-30cb-4205-9ca9-e13ad02c1068','cdffb9cd-71de-4e29-9cae-783c2aa211ac','dataproc','all','all');
INSERT IGNORE INTO `cf_AssetGroupTargetDetails` (`id_`, `groupId`, `targetType`, `attributeName`, `attributeValue`) VALUES('93733da9-916d-454c-9913-41a3aa8159e9','cdffb9cd-71de-4e29-9cae-783c2aa211ac','functionapp','all','all');
INSERT IGNORE INTO `cf_AssetGroupTargetDetails` (`id_`, `groupId`, `targetType`, `attributeName`, `attributeValue`) VALUES('ad076972-5c61-4e02-8c4b-7718dc880g7g','cdffb9cd-71de-4e29-9cae-783c2aa211ac','blobservice','all','all');
INSERT IGNORE INTO `cf_AssetGroupTargetDetails` (`id_`, `groupId`, `targetType`, `attributeName`, `attributeValue`) VALUES('285c3063-4b91-4607-a43d-bf80025794b5','cdffb9cd-71de-4e29-9cae-783c2aa211ac','mysqlflexible','all','all');
INSERT IGNORE INTO `cf_AssetGroupTargetDetails` (`id_`, `groupId`, `targetType`, `attributeName`, `attributeValue`) VALUES('00d91be0-ab7e-4869-b08e-b937aa551397','cdffb9cd-71de-4e29-9cae-783c2aa211ac','diagnosticsetting','all','all');
INSERT IGNORE INTO `cf_AssetGroupTargetDetails` (`id_`, `groupId`, `targetType`, `attributeName`, `attributeValue`) VALUES('00d91be0-ab7e-4869-b08e-b937aa551398','cdffb9cd-71de-4e29-9cae-783c2aa211ac','defender','all','all');
INSERT IGNORE INTO `cf_AssetGroupTargetDetails` (`id_`, `groupId`, `targetType`, `attributeName`, `attributeValue`) VALUES('00d91be0-ab7e-4869-b08e-b937aa551399','cdffb9cd-71de-4e29-9cae-783c2aa211ac','kubernetes','all','all');
INSERT IGNORE INTO `cf_AssetGroupTargetDetails` (`id_`, `groupId`, `targetType`, `attributeName`, `attributeValue`) VALUES('22c59287-4e12-4bd5-b4b7-f28021208df8','cdffb9cd-71de-4e29-9cae-783c2aa211ac','vaultsrbac','all','all');

INSERT IGNORE INTO `cf_AssetGroupTargetDetails` (`id_`, `groupId`, `targetType`, `attributeName`, `attributeValue`) VALUES('69867fcd-653c-4500-b1b5-0171f8f4c63b','e0008397-f74e-4deb-9066-10bdf11202ae','vminstance','all','all');
INSERT IGNORE INTO `cf_AssetGroupTargetDetails` (`id_`, `groupId`, `targetType`, `attributeName`, `attributeValue`) VALUES('50dbdcf8-9465-4095-9612-4990d4e47d40','e0008397-f74e-4deb-9066-10bdf11202ae','vpcfirewall','all','all');
INSERT IGNORE INTO `cf_AssetGroupTargetDetails` (`id_`, `groupId`, `targetType`, `attributeName`, `attributeValue`) VALUES('a9udqucf8-3465-4235-9612-4990d4er5td40','e0008397-f74e-4deb-9066-10bdf11202ae','bigquerytable','all','all');
INSERT IGNORE INTO `cf_AssetGroupTargetDetails` (`id_`, `groupId`, `targetType`, `attributeName`, `attributeValue`) VALUES('50dthdcf8-3465-4235-9612-4990d4er5td40','e0008397-f74e-4deb-9066-10bdf11202ae','bigquerydataset','all','all');
INSERT IGNORE INTO `cf_AssetGroupTargetDetails` (`id_`, `groupId`, `targetType`, `attributeName`, `attributeValue`) VALUES('bkg763cf8-3434-4235-0749-4990d4v4u5td40','e0008397-f74e-4deb-9066-10bdf11202ae','kmskey','all','all');
INSERT IGNORE INTO `cf_AssetGroupTargetDetails` (`id_`, `groupId`, `targetType`, `attributeName`, `attributeValue`) VALUES('b9udr432cf8-3465-4235-9612-4990d4ercrt60','e0008397-f74e-4deb-9066-10bdf11202ae','cloudsql','all','all');
INSERT IGNORE INTO `cf_AssetGroupTargetDetails` (`id_`, `groupId`, `targetType`, `attributeName`, `attributeValue`) VALUES('0bf61e09-4512-41e2-86db-19fc39be9347','e0008397-f74e-4deb-9066-10bdf11202ae','cloudstorage','all','all');
INSERT IGNORE INTO `cf_AssetGroupTargetDetails` (`id_`, `groupId`, `targetType`, `attributeName`, `attributeValue`) VALUES('49bea363-83bc-4376-b5d9-dff7a8df6b81','e0008397-f74e-4deb-9066-10bdf11202ae','pubsub','all','all');
INSERT IGNORE INTO `cf_AssetGroupTargetDetails` (`id_`, `groupId`, `targetType`, `attributeName`, `attributeValue`) VALUES('49bea363-83bc-4376-b5d9-dff7a8df6b82','e0008397-f74e-4deb-9066-10bdf11202ae','gkecluster','all','all');
INSERT IGNORE INTO `cf_AssetGroupTargetDetails` (`id_`, `groupId`, `targetType`, `attributeName`, `attributeValue`) VALUES('1bf61e09-4512-41e2-86db-19fc39be9359','e0008397-f74e-4deb-9066-10bdf11202ae','clouddns','all','all');
INSERT IGNORE INTO `cf_AssetGroupTargetDetails` (`id_`, `groupId`, `targetType`, `attributeName`, `attributeValue`) VALUES('b9udr432cf8-3465-4235-9612-4990d4ercrt61-23','e0008397-f74e-4deb-9066-10bdf11202ae','cloudsql_sqlserver','all','all');
INSERT IGNORE INTO `cf_AssetGroupTargetDetails` (`id_`, `groupId`, `targetType`, `attributeName`, `attributeValue`) VALUES('9d65d4ad-98dc-4394-85c4-f74f2ca38eb5','e0008397-f74e-4deb-9066-10bdf11202ae','cloudsql_mysqlserver','all','all');
INSERT IGNORE INTO `cf_AssetGroupTargetDetails` (`id_`, `groupId`, `targetType`, `attributeName`, `attributeValue`) VALUES('48ef4973-1904-4eae-bf5e-73450628b703','e0008397-f74e-4deb-9066-10bdf11202ae','cloudsql_postgres','all','all');
INSERT IGNORE INTO `cf_AssetGroupTargetDetails` (`id_`, `groupId`, `targetType`, `attributeName`, `attributeValue`) VALUES('b9udr432cf8-3465-4235-9612-4990d4ercrt69-29','e0008397-f74e-4deb-9066-10bdf11202ae','gcp_networks','all','all');
INSERT IGNORE INTO `cf_AssetGroupTargetDetails` (`id_`, `groupId`, `targetType`, `attributeName`, `attributeValue`) VALUES('b9udr432cf8-3465-4235-9612-4990d4ercrt70-30','e0008397-f74e-4deb-9066-10bdf11202ae','gcp_project','all','all');
INSERT IGNORE INTO `cf_AssetGroupTargetDetails` (`id_`, `groupId`, `targetType`, `attributeName`, `attributeValue`) VALUES('b9udr432cf8-3465-4235-9612-4990d4erdrt80-40','e0008397-f74e-4deb-9066-10bdf11202ae','gcp_serviceaccounts','all','all');
INSERT IGNORE INTO `cf_AssetGroupTargetDetails` (`id_`, `groupId`, `targetType`, `attributeName`, `attributeValue`) VALUES('de364119-0f2b-4f63-8d61-81fa4d1d33fb','e0008397-f74e-4deb-9066-10bdf11202ae','iamusers','all','all');
INSERT IGNORE INTO `cf_AssetGroupTargetDetails` (`id_`, `groupId`, `targetType`, `attributeName`, `attributeValue`) VALUES('25e615a5-e7d3-444e-95a3-2dedaef0890e','e0008397-f74e-4deb-9066-10bdf11202ae','gcp_apikeys','all','all');
INSERT IGNORE INTO `cf_AssetGroupTargetDetails` (`id_`, `groupId`, `targetType`, `attributeName`, `attributeValue`) VALUES('9b942f42-4bd0-4911-8fd3-a1661f0cbc97','e0008397-f74e-4deb-9066-10bdf11202ae','gcp_loadbalancers','all','all');



INSERT IGNORE INTO cf_AssetGroupTargetDetails (`id_`, groupId, targetType, attributeName, `attributeValue`) VALUES('a1480aa8-7239-4604-9ab7-916621792f00','e0008397-f74e-4deb-9066-10bdf11202ae','cloudfunction','all','all');
INSERT IGNORE INTO cf_AssetGroupTargetDetails (`id_`, groupId, targetType, attributeName, `attributeValue`) VALUES('a1480aa8-7239-4604-9ab7-916621792f01','e0008397-f74e-4deb-9066-10bdf11202ae','cloudfunctiongen1','all','all');

delete from `cf_AssetGroupTargetDetails` where targetType ='ecs';
delete from `cf_AssetGroupTargetDetails` where targetType = 'policydefinitions';
delete from `cf_AssetGroupTargetDetails` where targetType = 'policyevaluationresults';

/*Insert Domain in required table*/

INSERT IGNORE INTO cf_Domain (domainName,domainDesc,config,createdDate,modifiedDate,userId) VALUES ('Infra & Platforms','Domain for Infra & Platforms','{}',{d '2018-04-09'},{d '2018-08-03'},'user123');



/* Auth Related data */
INSERT IGNORE INTO `oauth_client_details`(`client_id`,`resource_ids`,`client_secret`,`scope`,`authorized_grant_types`,`web_server_redirect_uri`,`authorities`,`access_token_validity`,`refresh_token_validity`,`additional_information`,`autoapprove`) values ('22e14922-87d7-4ee4-a470-da0bb10d45d3',NULL,'$2a$10$Is6r80wW65hKHUq6Wa8B6O3BLKqGOb5McDGbJUwVwfVvyeJBCf7ta','resource-access','implicit,authorization_code,refresh_token,password,client_credentials',NULL,'ROLE_CLIENT,ROLE_USER',NULL,NULL,NULL,'');
INSERT IGNORE INTO `oauth_user`(`id`,`user_id`,`user_name`,`first_name`,`last_name`,`email`,`created_date`,`modified_date`) values (1,'user@paladincloud.io','user','user','','user@paladincloud.io','2018-06-26 18:21:56','2018-06-26 18:21:56'),(2,'admin@paladincloud.io','admin','admin','','admin@paladincloud.io','2018-06-26 18:21:56','2018-06-26 18:21:56');
INSERT IGNORE INTO `oauth_user_credentials` (`id`, `password`, `type`) values('1','$2a$10$A3x6YNcaE.FzVMSz/zqQAeaECoWewI8atkUyJnDZPDZPgka3aOMKK','db');
INSERT IGNORE INTO `oauth_user_credentials` (`id`, `password`, `type`) values('2','$2a$10$Q4VelltsKsp9Owq9Nf8SgO.csoLBIsQhdvK7VX4obEtEULIXcyUn2','db');
INSERT IGNORE INTO `oauth_user_roles`(`roleId`,`roleName`,`roleDesc`,`writePermission`,`owner`,`client`,`createdDate`,`modifiedDate`) values ('1','ROLE_USER','ROLE_USER',0,'asgc','22e14922-87d7-4ee4-a470-da0bb10d45d3','2018-01-23 00:00:00','2018-01-23 00:00:00'),('703','ROLE_ADMIN','ROLE_ADMIN',1,'asgc','22e14922-87d7-4ee4-a470-da0bb10d45d3','2018-03-13 17:26:58','2018-03-13 17:26:58');
INSERT IGNORE INTO `oauth_user_role_mapping`(`userRoleId`,`userId`,`roleId`,`clientId`,`allocator`,`createdDate`,`modifiedDate`) values ('4747c0cf-63cc-4829-a1e8-f1e957ec5dd6','user@paladincloud.io','1','22e14922-87d7-4ee4-a470-da0bb10d45d3','user123','2018-01-09 16:11:47','2018-01-09 16:11:47'),('4747c0cf-63cc-4829-a1e8-f1e957ec5dd7','admin@paladincloud.io','1','22e14922-87d7-4ee4-a470-da0bb10d45d3','user123','2018-01-09 16:11:47','2018-01-09 16:11:47'),('f5b2a689-c185-11e8-9c73-12d01119b604','admin@paladincloud.io','703','22e14922-87d7-4ee4-a470-da0bb10d45d3','user123','2018-01-09 16:11:47','2018-01-09 16:11:47');

/* Display and Update Fields */
TRUNCATE TABLE cf_pac_updatable_fields;
INSERT IGNORE INTO cf_pac_updatable_fields  (resourceType,displayFields,updatableFields) VALUES
 ('all_list','_resourceid,tags.Application,tags.Environment,_entitytype',null),
 ('all_taggable','_resourceid,tags.Application,tags.Environment,_entitytype,targetType,accountid,accountname,region',null),
 ('all_vulnerable','_resourceid,tags.Application,tags.Environment,_entitytype,accountid,accountname,region',null),
 ('all_patchable','_resourceid,tags.Application,tags.Environment,_entitytype',null);


INSERT IGNORE INTO OmniSearch_Config (SEARCH_CATEGORY,RESOURCE_TYPE,REFINE_BY_FIELDS,RETURN_FIELDS) VALUES ('Assets','All','accountname,region,tags.Application,tags.Environment,tags.Stack,tags.Role','_resourceid,searchcategory,tags[],accountname,_entitytype');
INSERT IGNORE INTO OmniSearch_Config (SEARCH_CATEGORY,RESOURCE_TYPE,REFINE_BY_FIELDS,RETURN_FIELDS) VALUES ('Assets','api','','region,name');
INSERT IGNORE INTO OmniSearch_Config (SEARCH_CATEGORY,RESOURCE_TYPE,REFINE_BY_FIELDS,RETURN_FIELDS) VALUES ('Assets','appelb','scheme,vpcid,type','region,scheme,vpcid,type');
INSERT IGNORE INTO OmniSearch_Config (SEARCH_CATEGORY,RESOURCE_TYPE,REFINE_BY_FIELDS,RETURN_FIELDS) VALUES ('Assets','asg','healthchecktype','region,healthchecktype');
INSERT IGNORE INTO OmniSearch_Config (SEARCH_CATEGORY,RESOURCE_TYPE,REFINE_BY_FIELDS,RETURN_FIELDS) VALUES ('Assets','asgpolicy','policytype,adjustmenttype','region,autoscalinggroupname,policytype');
INSERT IGNORE INTO OmniSearch_Config (SEARCH_CATEGORY,RESOURCE_TYPE,REFINE_BY_FIELDS,RETURN_FIELDS) VALUES ('Assets','cert','','');
INSERT IGNORE INTO OmniSearch_Config (SEARCH_CATEGORY,RESOURCE_TYPE,REFINE_BY_FIELDS,RETURN_FIELDS) VALUES ('Assets','checks','','');
INSERT IGNORE INTO OmniSearch_Config (SEARCH_CATEGORY,RESOURCE_TYPE,REFINE_BY_FIELDS,RETURN_FIELDS) VALUES ('Assets','classicelb','scheme,vpcid','region,scheme,vpcid');
INSERT IGNORE INTO OmniSearch_Config (SEARCH_CATEGORY,RESOURCE_TYPE,REFINE_BY_FIELDS,RETURN_FIELDS) VALUES ('Assets','cloudfront','status,enabled,priceclass,httpversion,ipv6enabled','domainname,status,httpversion,aliases');
INSERT IGNORE INTO OmniSearch_Config (SEARCH_CATEGORY,RESOURCE_TYPE,REFINE_BY_FIELDS,RETURN_FIELDS) VALUES ('Assets','corpdomain','','');
INSERT IGNORE INTO OmniSearch_Config (SEARCH_CATEGORY,RESOURCE_TYPE,REFINE_BY_FIELDS,RETURN_FIELDS) VALUES ('Assets','dynamodb','tablestatus','region,tablestatus');
INSERT IGNORE INTO OmniSearch_Config (SEARCH_CATEGORY,RESOURCE_TYPE,REFINE_BY_FIELDS,RETURN_FIELDS) VALUES ('Assets','ec2 ','availabilityzone,statename,instancetype,imageid,platform,subnetid','availabilityzone,privateipaddress,statename,instancetype');
INSERT IGNORE INTO OmniSearch_Config (SEARCH_CATEGORY,RESOURCE_TYPE,REFINE_BY_FIELDS,RETURN_FIELDS) VALUES ('Assets','efs','performancemode,lifecyclestate','region,performancemode,lifecyclestate');
INSERT IGNORE INTO OmniSearch_Config (SEARCH_CATEGORY,RESOURCE_TYPE,REFINE_BY_FIELDS,RETURN_FIELDS) VALUES ('Assets','elasticip','','networkinterfaceid,privateipaddress,region');
INSERT IGNORE INTO OmniSearch_Config (SEARCH_CATEGORY,RESOURCE_TYPE,REFINE_BY_FIELDS,RETURN_FIELDS) VALUES ('Assets','emr','instancecollectiontype,releaselabel','region,instancecollectiontype,releaselabel');
INSERT IGNORE INTO OmniSearch_Config (SEARCH_CATEGORY,RESOURCE_TYPE,REFINE_BY_FIELDS,RETURN_FIELDS) VALUES ('Assets','eni','status,sourcedestcheck,vpcid,subnetid','region,privateipaddress,status,vpcid,subnetid');
INSERT IGNORE INTO OmniSearch_Config (SEARCH_CATEGORY,RESOURCE_TYPE,REFINE_BY_FIELDS,RETURN_FIELDS) VALUES ('Assets','iamrole','','description');
INSERT IGNORE INTO OmniSearch_Config (SEARCH_CATEGORY,RESOURCE_TYPE,REFINE_BY_FIELDS,RETURN_FIELDS) VALUES ('Assets','iamuser','passwordresetrequired,mfaenabled','');
INSERT IGNORE INTO OmniSearch_Config (SEARCH_CATEGORY,RESOURCE_TYPE,REFINE_BY_FIELDS,RETURN_FIELDS) VALUES ('Assets','internetgateway','','region');
INSERT IGNORE INTO OmniSearch_Config (SEARCH_CATEGORY,RESOURCE_TYPE,REFINE_BY_FIELDS,RETURN_FIELDS) VALUES ('Assets','kms','keystate,keyenabled,keyusage,rotationstatus','region,keystate');
INSERT IGNORE INTO OmniSearch_Config (SEARCH_CATEGORY,RESOURCE_TYPE,REFINE_BY_FIELDS,RETURN_FIELDS) VALUES ('Assets','lambda','memorysize,runtime,timeout','region,runtime');
INSERT IGNORE INTO OmniSearch_Config (SEARCH_CATEGORY,RESOURCE_TYPE,REFINE_BY_FIELDS,RETURN_FIELDS) VALUES ('Assets','launchconfig','instancetype,ebsoptimized,instancemonitoringenabled','instancetype,region');
INSERT IGNORE INTO OmniSearch_Config (SEARCH_CATEGORY,RESOURCE_TYPE,REFINE_BY_FIELDS,RETURN_FIELDS) VALUES ('Assets','nat','vpcid,subnetid,state','region,vpcid,subnetid,state');
INSERT IGNORE INTO OmniSearch_Config (SEARCH_CATEGORY,RESOURCE_TYPE,REFINE_BY_FIELDS,RETURN_FIELDS) VALUES ('Assets','networkacl','vpcid,isdefault','vpcid,region');
INSERT IGNORE INTO OmniSearch_Config (SEARCH_CATEGORY,RESOURCE_TYPE,REFINE_BY_FIELDS,RETURN_FIELDS) VALUES ('Assets','onpremserver','os,used_for,u_business_service,location,company,firewall_status,u_patching_director,install_staus','ip_address,os,os_version,comapny');
INSERT IGNORE INTO OmniSearch_Config (SEARCH_CATEGORY,RESOURCE_TYPE,REFINE_BY_FIELDS,RETURN_FIELDS) VALUES ('Assets','phd','','');
INSERT IGNORE INTO OmniSearch_Config (SEARCH_CATEGORY,RESOURCE_TYPE,REFINE_BY_FIELDS,RETURN_FIELDS) VALUES ('Assets','rdscluster','multiaz,engine,engineversion','region,engine,engineversion');
INSERT IGNORE INTO OmniSearch_Config (SEARCH_CATEGORY,RESOURCE_TYPE,REFINE_BY_FIELDS,RETURN_FIELDS) VALUES ('Assets','rdsdb','dbinstanceclass,dbinstancestatus,engine,engineversion,licensemodel,multiaz,publiclyaccessible','region,engine,engineversion');
INSERT IGNORE INTO OmniSearch_Config (SEARCH_CATEGORY,RESOURCE_TYPE,REFINE_BY_FIELDS,RETURN_FIELDS) VALUES ('Assets','rdssnapshot','snapshottype,encrypted,engine,engineversion,storagetype','vpcid,availabilityzone,engine,engineversion');
INSERT IGNORE INTO OmniSearch_Config (SEARCH_CATEGORY,RESOURCE_TYPE,REFINE_BY_FIELDS,RETURN_FIELDS) VALUES ('Assets','redshift','nodetype,publiclyaccessible','region,nodetype,vpcid');
INSERT IGNORE INTO OmniSearch_Config (SEARCH_CATEGORY,RESOURCE_TYPE,REFINE_BY_FIELDS,RETURN_FIELDS) VALUES ('Assets','routetable','vpcid','vpcid,region');
INSERT IGNORE INTO OmniSearch_Config (SEARCH_CATEGORY,RESOURCE_TYPE,REFINE_BY_FIELDS,RETURN_FIELDS) VALUES ('Assets','s3','versionstatus','region,creationdate,versionstatus');
INSERT IGNORE INTO OmniSearch_Config (SEARCH_CATEGORY,RESOURCE_TYPE,REFINE_BY_FIELDS,RETURN_FIELDS) VALUES ('Assets','sg','vpcid','region,vpcid');
INSERT IGNORE INTO OmniSearch_Config (SEARCH_CATEGORY,RESOURCE_TYPE,REFINE_BY_FIELDS,RETURN_FIELDS) VALUES ('Assets','snapshot','encrypted,state','region,volumeid,state');
INSERT IGNORE INTO OmniSearch_Config (SEARCH_CATEGORY,RESOURCE_TYPE,REFINE_BY_FIELDS,RETURN_FIELDS) VALUES ('Assets','stack','disablerollback,status','region,status');
INSERT IGNORE INTO OmniSearch_Config (SEARCH_CATEGORY,RESOURCE_TYPE,REFINE_BY_FIELDS,RETURN_FIELDS) VALUES ('Assets','subnet','vpcid,availabilityzone,defaultforaz,state','availabilityzone,cidrblock,state');
INSERT IGNORE INTO OmniSearch_Config (SEARCH_CATEGORY,RESOURCE_TYPE,REFINE_BY_FIELDS,RETURN_FIELDS) VALUES ('Assets','targetgroup','','region,vpcid,protocol,port');
INSERT IGNORE INTO OmniSearch_Config (SEARCH_CATEGORY,RESOURCE_TYPE,REFINE_BY_FIELDS,RETURN_FIELDS) VALUES ('Assets','volume','volumetype,availabilityzone,encrypted,state','volumetype,availabilityzone,state');
INSERT IGNORE INTO OmniSearch_Config (SEARCH_CATEGORY,RESOURCE_TYPE,REFINE_BY_FIELDS,RETURN_FIELDS) VALUES ('Assets','vpc','','region,cidrblock,state');
INSERT IGNORE INTO OmniSearch_Config (SEARCH_CATEGORY,RESOURCE_TYPE,REFINE_BY_FIELDS,RETURN_FIELDS) VALUES ('Assets','vpngateway','state,type','region,state,type');
INSERT IGNORE INTO OmniSearch_Config (SEARCH_CATEGORY,RESOURCE_TYPE,REFINE_BY_FIELDS,RETURN_FIELDS) VALUES ('Assets','elasticache','engine,nodetype,engineversion','region,engine');
INSERT IGNORE INTO OmniSearch_Config (SEARCH_CATEGORY,RESOURCE_TYPE,REFINE_BY_FIELDS,RETURN_FIELDS) VALUES ('Assets','wafdomain','','');
INSERT IGNORE INTO OmniSearch_Config (SEARCH_CATEGORY,RESOURCE_TYPE,REFINE_BY_FIELDS,RETURN_FIELDS) VALUES ('Policy Violations','All','severity,policyId','_id,issueid,resourceid,severity,_entitytype,_resourceid');
INSERT IGNORE INTO OmniSearch_Config (SEARCH_CATEGORY,RESOURCE_TYPE,REFINE_BY_FIELDS,RETURN_FIELDS) VALUES ('Vulnerabilities','All','severity,category,vulntype','qid,vulntype,category,_entitytype,_resourceid');



/* UI FIlter */

INSERT IGNORE INTO pac_v2_ui_filters (filterId,filterName) VALUES (1,'Issue');
INSERT IGNORE INTO pac_v2_ui_filters (filterId,filterName) VALUES (2,'vulnerbility');
INSERT IGNORE INTO pac_v2_ui_filters (filterId,filterName) VALUES (3,'asset');
INSERT IGNORE INTO pac_v2_ui_filters (filterId,filterName) VALUES (4,'compliance');
INSERT IGNORE INTO pac_v2_ui_filters (filterId,filterName) VALUES (5,'tagging');
INSERT IGNORE INTO pac_v2_ui_filters (filterId,filterName) VALUES (6,'certificates');
INSERT IGNORE INTO pac_v2_ui_filters (filterId,filterName) VALUES (7,'patching');
INSERT IGNORE INTO pac_v2_ui_filters (filterId,filterName) VALUES (8,'AssetListing');
INSERT IGNORE INTO pac_v2_ui_filters (filterId,filterName) VALUES (9,'digitaldev');
INSERT IGNORE INTO pac_v2_ui_filters (filterId,filterName) VALUES (10,'notification');

/* UI Filter Options */

INSERT IGNORE INTO pac_v2_ui_options (optionId,filterId,optionName,optionValue,optionURL) VALUES (3,1,'Region','region.keyword','/compliance/v1/filters/regions?ag=aws');
INSERT IGNORE INTO pac_v2_ui_options (optionId,filterId,optionName,optionValue,optionURL) VALUES (4,1,'AccountName','accountid.keyword','/compliance/v1/filters/accounts?ag=aws');
INSERT IGNORE INTO pac_v2_ui_options (optionId,filterId,optionName,optionValue,optionURL) VALUES (5,1,'Application','tags.Application.keyword','/compliance/v1/filters/application?ag=aws');
INSERT IGNORE INTO pac_v2_ui_options (optionId,filterId,optionName,optionValue,optionURL) VALUES (6,1,'Environment','tags.Environment.keyword','/compliance/v1/filters/environment?ag=aws&application=aws');
INSERT IGNORE INTO pac_v2_ui_options (optionId,filterId,optionName,optionValue,optionURL) VALUES (7,2,'Application','tags.Application.keyword','/compliance/v1/filters/application?ag=aws');
INSERT IGNORE INTO pac_v2_ui_options (optionId,filterId,optionName,optionValue,optionURL) VALUES (8,2,'Environment','tags.Environment.keyword','/compliance/v1/filters/environment?ag=aws&application=aws');
INSERT IGNORE INTO pac_v2_ui_options (optionId,filterId,optionName,optionValue,optionURL) VALUES (9,3,'Application','tags.Application.keyword','/compliance/v1/filters/application?ag=aws');
INSERT IGNORE INTO pac_v2_ui_options (optionId,filterId,optionName,optionValue,optionURL) VALUES (12,4,'Resource Type','targetType.keyword','/compliance/v1/filters/targettype?ag=aws');
INSERT IGNORE INTO pac_v2_ui_options (optionId,filterId,optionName,optionValue,optionURL) VALUES (13,8,'Application ','application ','/compliance/v1/filters/application?ag=aws');
INSERT IGNORE INTO pac_v2_ui_options (optionId,filterId,optionName,optionValue,optionURL) VALUES (14,8,'Environment  ','environment ','/compliance/v1/filters/environment?ag=aws&application=aws');
INSERT IGNORE INTO pac_v2_ui_options (optionId,filterId,optionName,optionValue,optionURL) VALUES (15,8,'Resource Type','resourceType ','/compliance/v1/filters/targettype?ag=aws');
INSERT IGNORE INTO pac_v2_ui_options (optionId,filterId,optionName,optionValue,optionURL) VALUES (16,9,'Application','tags.Application.keyword','/compliance/v1/filters/application?ag=aws');
INSERT IGNORE INTO pac_v2_ui_options (optionId,filterId,optionName,optionValue,optionURL) VALUES (17,1,'Severity','severity.keyword','/compliance/v1/filters/severities?ag=aws');
INSERT IGNORE INTO pac_v2_ui_options (optionId,filterId,optionName,optionValue,optionURL) VALUES (18,1,'Category','category.keyword','/compliance/v1/filters/categories?ag=aws');
INSERT IGNORE INTO pac_v2_ui_options (optionId,filterId,optionName,optionValue,optionURL) VALUES (19,1,'Status','issueStatus.keyword','/compliance/v1/filters/issuestatus?ag=aws');
INSERT IGNORE INTO pac_v2_ui_options (optionId,filterId,optionName,optionValue,optionURL) VALUES (20,10,'Type','eventCategoryName','/compliance/v1/filters/eventtype');
INSERT IGNORE INTO pac_v2_ui_options (optionId,filterId,optionName,optionValue,optionURL) VALUES (21,10,'Source','eventSourceName','/compliance/v1/filters/eventsource');
INSERT IGNORE INTO pac_v2_ui_options (optionId,filterId,optionName,optionValue,optionURL) VALUES (22,8,'Tagged','tagged','/compliance/v1/filters/taggedStatus?ag=aws');
INSERT IGNORE INTO pac_v2_ui_options (optionId,filterId,optionName,optionValue,optionURL) VALUES (23,8,'Exempted','exempted','/compliance/v1/filters/taggedStatus?ag=aws');

/* Violation Filters */
INSERT IGNORE INTO pac_v2_ui_options (optionId,filterId,optionName,optionValue,optionURL) VALUES (24,1,'AssetType','targetType.keyword','/compliance/v1/filters/attribute?ag=aws&attribute=targetType&type=issue');
INSERT IGNORE INTO pac_v2_ui_options (optionId,filterId,optionName,optionValue,optionURL) VALUES (25,1,'Policy','policyName.keyword','/compliance/v1/filters/attribute?ag=aws&attribute=policyName&type=issue');
INSERT IGNORE INTO pac_v2_ui_options (optionId,filterId,optionName,optionValue,optionURL) VALUES (26,1,'Asset ID','_resourceid.keyword','/compliance/v1/filters/attribute?ag=aws&attribute=_resourceid&type=issue');
INSERT IGNORE INTO pac_v2_ui_options (optionId,filterId,optionName,optionValue,optionURL) VALUES (27,1,'Violation ID','annotationid.keyword','/compliance/v1/filters/attribute?ag=aws&attribute=annotationid&type=issue');
INSERT IGNORE INTO pac_v2_ui_options (optionId,filterId,optionName,optionValue,optionURL) VALUES (28,1,'Age','createdDate','/compliance/v1/filters/violationAge?ag=aws');
UPDATE pac_v2_ui_options set optionName='Asset Type' where optionId=24;
DELETE IGNORE from pac_v2_ui_options where optionId=4;
INSERT IGNORE INTO pac_v2_ui_options (optionId,filterId,optionName,optionValue,optionURL) VALUES (4,1,'Account Name','accountname.keyword','/compliance/v1/filters/attribute?ag=aws&attribute=accountname&type=issue');
INSERT IGNORE INTO pac_v2_ui_options (optionId,filterId,optionName,optionValue,optionURL) VALUES (35,1,'Account ID','accountid.keyword','/compliance/v1/filters/attribute?ag=aws&attribute=accountid&type=issue');
/* AssetList Filters */
INSERT IGNORE INTO pac_v2_ui_options (optionId,filterId,optionName,optionValue,optionURL) VALUES (29,8,'Account ID','accountid.keyword','/compliance/v1/filters/attribute?ag=aws&attribute=accountid&type=asset');
INSERT IGNORE INTO pac_v2_ui_options (optionId,filterId,optionName,optionValue,optionURL) VALUES (30,8,'Account Name','accountname.keyword','/compliance/v1/filters/attribute?ag=aws&attribute=accountname&type=asset');
INSERT IGNORE INTO pac_v2_ui_options (optionId,filterId,optionName,optionValue,optionURL) VALUES (31,8,'Asset ID','_resourceid.keyword','/compliance/v1/filters/attribute?ag=aws&attribute=_resourceid&type=asset');
INSERT IGNORE INTO pac_v2_ui_options (optionId,filterId,optionName,optionValue,optionURL) VALUES (32,8,'Cloud Type','_cloudType.keyword','/compliance/v1/filters/attribute?ag=aws&attribute=_cloudType&type=asset');
INSERT IGNORE INTO pac_v2_ui_options (optionId,filterId,optionName,optionValue,optionURL) VALUES (33,8,'Region','region.keyword','/compliance/v1/filters/attribute?ag=aws&attribute=region&type=asset');

/* Notification filters */
INSERT IGNORE INTO pac_v2_ui_options (optionId,filterId,optionName,optionValue,optionURL) VALUES (34,10,'Event','eventName','/compliance/v1/filters/attribute?ag=aws&attribute=accountid&type=asset');

/* UI Widgets */
INSERT IGNORE INTO pac_v2_ui_widgets (widgetId,pageName,widgetName) VALUES (1,'Tagging','TaggingSummary');
INSERT IGNORE INTO pac_v2_ui_widgets (widgetId,pageName,widgetName) VALUES (2,'Tagging','Total Tag Compliance');
INSERT IGNORE INTO pac_v2_ui_widgets (widgetId,pageName,widgetName) VALUES (3,'Tagging','Tagging Compliance Trend');
INSERT IGNORE INTO pac_v2_ui_widgets (widgetId,pageName,widgetName) VALUES (4,'ComplianceOverview','OverallCompliance,tagging,patching,vulnerabilites');

/* UI Widgets faqs */
INSERT IGNORE INTO pac_v2_ui_widget_faqs (faqId,widgetId,widgetName,faqName,faqAnswer) VALUES (1,1,'Tagging Summary','How overall Compliance% calculated ?','Total assets which has Application and Environment tag devided by total taggable Assets.');
INSERT IGNORE INTO pac_v2_ui_widget_faqs (faqId,widgetId,widgetName,faqName,faqAnswer) VALUES (2,1,'Tagging Summary','How an AssetGroup Un-tagged count calculted ?','Total assets which is missing application,Environment tag.');
INSERT IGNORE INTO pac_v2_ui_widget_faqs (faqId,widgetId,widgetName,faqName,faqAnswer) VALUES (3,4,'OverallCompliance,tagging,patching,vulnerabilites','How overall % calculated ?','It''s average of patching,certificates,tagging,vulnerbilites and other policies percentage');
INSERT IGNORE INTO pac_v2_ui_widget_faqs (faqId,widgetId,widgetName,faqName,faqAnswer) VALUES (7,4,'OverallCompliance,tagging,patching,vulnerabilites','How patching % calculated ?','total patched running ec2 instances /total running ec2 instances');
INSERT IGNORE INTO pac_v2_ui_widget_faqs (faqId,widgetId,widgetName,faqName,faqAnswer) VALUES (8,4,'OverallCompliance,tagging,patching,vulnerabilites','How tagging % calculated ?','total tagged assets /total assets');
INSERT IGNORE INTO pac_v2_ui_widget_faqs (faqId,widgetId,widgetName,faqName,faqAnswer) VALUES (9,4,'OverallCompliance,tagging,patching,vulnerabilites','How vulnerabilities % calculated ?','total vulnerable ec2 assets/total ec2 assets.  ');
INSERT IGNORE INTO pac_v2_ui_widget_faqs (faqId,widgetId,widgetName,faqName,faqAnswer) VALUES (10,4,'OverallCompliance,tagging,patching,vulnerabilites','How other policies % calculated',null);


INSERT IGNORE INTO pac_v2_ui_download_filters (serviceId,serviceName,serviceEndpoint) VALUES
 (1,'Violations','/api/compliance/v1/issues'),
 (2,'NonComplaincePolicies','/api/compliance/v1/noncompliancepolicy'),
 (3,'PatchingDetails','/api/compliance/v1/patching/detail'),
 (4,'TaggingDetailsByApplication','/api/compliance/v1/tagging/summarybyapplication'),
 (5,'CertificateDetails','/api/compliance/v1/certificates/detail'),
 (6,'VulnerabilitiesDetails','/api/compliance/v1/vulnerabilities/detail'),
 (7,'Assets','/api/asset/v1//list/assets'),
 (8,'PatchableAssets','/api/asset/v1/list/assets/patchable'),
 (9,'ScannedAssets','/api/asset/v1/list/assets/scanned'),
 (10,'TaggableAssets','/api/asset/v1/list/assets/taggable'),
 (11,'VulnerableAssets','/api/asset/v1/list/assets/vulnerable'),
 (12,'PullRequestAssetsByState','/api/devstandards/v1/pullrequests/asset/bystates'),
 (13,'PullRequestAsstesByAge','/api/devstandards/v1/pullrequests/assets/openstate'),
 (14,'ApplicationOrRepositoryDistribution','/api/devstandards/v1/repositories/assets/repositoryorapplicationdistribution'),
 (15,'RecommendationDetails','/api/asset/v1/recommendations/detail'),
 (16,'Recommendation','/api/asset/v1/recommendations'),
 (17,'CloudNotificationsWithOutGlobal','/api/asset/v1/cloud/notifications?global=false'),
 (18,'CloudNotificationsWithGlobal','/api/asset/v1/cloud/notifications?global=true');


INSERT IGNORE INTO pac_config_relation (`application`,`parent`) VALUES ('application','root');
INSERT IGNORE INTO pac_config_relation (`application`,`parent`) VALUES ('batch','application');
INSERT IGNORE INTO pac_config_relation (`application`,`parent`) VALUES ('api','application');
INSERT IGNORE INTO pac_config_relation (`application`,`parent`) VALUES ('compliance-service','api');
INSERT IGNORE INTO pac_config_relation (`application`,`parent`) VALUES ('asset-service','api');
INSERT IGNORE INTO pac_config_relation (`application`,`parent`) VALUES ('notification-service','api');
INSERT IGNORE INTO pac_config_relation (`application`,`parent`) VALUES ('statistics-service','api');
INSERT IGNORE INTO pac_config_relation (`application`,`parent`) VALUES ('auth-service','api');
INSERT IGNORE INTO pac_config_relation (`application`,`parent`) VALUES ('dev-standards-service','api');
INSERT IGNORE INTO pac_config_relation (`application`,`parent`) VALUES ('admin-service','api');
INSERT IGNORE INTO pac_config_relation (`application`,`parent`) VALUES ('magenta-skill','api');
INSERT IGNORE INTO pac_config_relation (application,parent) VALUES ('data-shipper','batch');
INSERT IGNORE INTO pac_config_relation (application,parent) VALUES ('inventory','batch');
INSERT IGNORE INTO pac_config_relation (`application`,`parent`) VALUES ('rule','application');
INSERT IGNORE INTO pac_config_relation (application,parent) VALUES ('rule-engine','rule');
INSERT IGNORE INTO pac_config_relation (application,parent) VALUES ('recommendation-enricher','batch');
INSERT IGNORE INTO pac_config_relation (application,parent) VALUES ('qualys-enricher','batch');
INSERT IGNORE INTO pac_config_relation (application,parent) VALUES ('azure-discovery','batch');

INSERT IGNORE INTO pac_config_key_metadata (`cfkey`,`description`) VALUES ('admin.api-role','Description PlaceHolder');
INSERT IGNORE INTO pac_config_key_metadata (`cfkey`,`description`) VALUES ('admin.push.notification.pollinterval.milliseconds','description');
INSERT IGNORE INTO pac_config_key_metadata (`cfkey`,`description`) VALUES ('api.services[0].name','Description PlaceHolder');
INSERT IGNORE INTO pac_config_key_metadata (`cfkey`,`description`) VALUES ('api.services[0].url','Description PlaceHolder');
INSERT IGNORE INTO pac_config_key_metadata (`cfkey`,`description`) VALUES ('api.services[0].version','Description PlaceHolder');
INSERT IGNORE INTO pac_config_key_metadata (`cfkey`,`description`) VALUES ('api.services[1].name','Description PlaceHolder');
INSERT IGNORE INTO pac_config_key_metadata (`cfkey`,`description`) VALUES ('api.services[1].url','Description PlaceHolder');
INSERT IGNORE INTO pac_config_key_metadata (`cfkey`,`description`) VALUES ('api.services[1].version','Description PlaceHolder');
INSERT IGNORE INTO pac_config_key_metadata (`cfkey`,`description`) VALUES ('api.services[2].name','Description PlaceHolder');
INSERT IGNORE INTO pac_config_key_metadata (`cfkey`,`description`) VALUES ('api.services[2].url','Description PlaceHolder');
INSERT IGNORE INTO pac_config_key_metadata (`cfkey`,`description`) VALUES ('api.services[2].version','Description PlaceHolder');
INSERT IGNORE INTO pac_config_key_metadata (`cfkey`,`description`) VALUES ('api.services[3].name','Description PlaceHolder');
INSERT IGNORE INTO pac_config_key_metadata (`cfkey`,`description`) VALUES ('api.services[3].url','Description PlaceHolder');
INSERT IGNORE INTO pac_config_key_metadata (`cfkey`,`description`) VALUES ('api.services[3].version','Description PlaceHolder');
INSERT IGNORE INTO pac_config_key_metadata (`cfkey`,`description`) VALUES ('api.services[4].name','Description PlaceHolder');
INSERT IGNORE INTO pac_config_key_metadata (`cfkey`,`description`) VALUES ('api.services[4].url','Description PlaceHolder');
INSERT IGNORE INTO pac_config_key_metadata (`cfkey`,`description`) VALUES ('api.services[4].version','Description PlaceHolder');
INSERT IGNORE INTO pac_config_key_metadata (`cfkey`,`description`) VALUES ('api.services[5].name','Description PlaceHolder');
INSERT IGNORE INTO pac_config_key_metadata (`cfkey`,`description`) VALUES ('api.services[5].url','Description PlaceHolder');
INSERT IGNORE INTO pac_config_key_metadata (`cfkey`,`description`) VALUES ('api.services[5].version','Description PlaceHolder');
INSERT IGNORE INTO pac_config_key_metadata (`cfkey`,`description`) VALUES ('api.services[6].name','Description PlaceHolder');
INSERT IGNORE INTO pac_config_key_metadata (`cfkey`,`description`) VALUES ('api.services[6].url','Description PlaceHolder');
INSERT IGNORE INTO pac_config_key_metadata (`cfkey`,`description`) VALUES ('api.services[6].version','Description PlaceHolder');
INSERT IGNORE INTO pac_config_key_metadata (`cfkey`,`description`) VALUES ('application.cors.allowed.domains','Description PlaceHolder');
INSERT IGNORE INTO pac_config_key_metadata (`cfkey`,`description`) VALUES ('auth.active','Description PlaceHolder');
INSERT IGNORE INTO pac_config_key_metadata (`cfkey`,`description`) VALUES ('aws.access-key','Description PlaceHolder');
INSERT IGNORE INTO pac_config_key_metadata (`cfkey`,`description`) VALUES ('aws.secret-key','Description PlaceHolder');
INSERT IGNORE INTO pac_config_key_metadata (`cfkey`,`description`) VALUES ('azure.activedirectory.client-id','Description PlaceHolder');
INSERT IGNORE INTO pac_config_key_metadata (`cfkey`,`description`) VALUES ('azure.activedirectory.client-secret','Description PlaceHolder');
INSERT IGNORE INTO pac_config_key_metadata (`cfkey`,`description`) VALUES ('azure.activedirectory.scope','Description PlaceHolder');
INSERT IGNORE INTO pac_config_key_metadata (`cfkey`,`description`) VALUES ('azure.activedirectory.scopeDesc','Description PlaceHolder');
INSERT IGNORE INTO pac_config_key_metadata (`cfkey`,`description`) VALUES ('azure.activedirectory.state','Description PlaceHolder');
INSERT IGNORE INTO pac_config_key_metadata (`cfkey`,`description`) VALUES ('azure.activedirectory.tenant-id','Description PlaceHolder');
INSERT IGNORE INTO pac_config_key_metadata (`cfkey`,`description`) VALUES ('azure.authorizeEndpoint','Description PlaceHolder');
INSERT IGNORE INTO pac_config_key_metadata (`cfkey`,`description`) VALUES ('azure.id-token.claims.email','Description PlaceHolder');
INSERT IGNORE INTO pac_config_key_metadata (`cfkey`,`description`) VALUES ('azure.id-token.claims.first-name','Description PlaceHolder');
INSERT IGNORE INTO pac_config_key_metadata (`cfkey`,`description`) VALUES ('azure.id-token.claims.last-name','Description PlaceHolder');
INSERT IGNORE INTO pac_config_key_metadata (`cfkey`,`description`) VALUES ('azure.id-token.claims.user-id','Description PlaceHolder');
INSERT IGNORE INTO pac_config_key_metadata (`cfkey`,`description`) VALUES ('azure.id-token.claims.user-name','Description PlaceHolder');
INSERT IGNORE INTO pac_config_key_metadata (`cfkey`,`description`) VALUES ('azure.issuer','Description PlaceHolder');
INSERT IGNORE INTO pac_config_key_metadata (`cfkey`,`description`) VALUES ('azure.public-key','Description PlaceHolder');
INSERT IGNORE INTO pac_config_key_metadata (`cfkey`,`description`) VALUES ('base.account','');
INSERT IGNORE INTO pac_config_key_metadata (`cfkey`,`description`) VALUES ('branch.maxBranchAge','Description PlaceHolder');
INSERT IGNORE INTO pac_config_key_metadata (`cfkey`,`description`) VALUES ('cloudinsights.corp-password','Description PlaceHolder');
INSERT IGNORE INTO pac_config_key_metadata (`cfkey`,`description`) VALUES ('cloudinsights.corp-user-id','Description PlaceHolder');
INSERT IGNORE INTO pac_config_key_metadata (`cfkey`,`description`) VALUES ('cloudinsights.costurl','Description PlaceHolder');
INSERT IGNORE INTO pac_config_key_metadata (`cfkey`,`description`) VALUES ('cloudinsights.tokenurl','Description PlaceHolder');
INSERT IGNORE INTO pac_config_key_metadata (`cfkey`,`description`) VALUES ('cron.frequency.weekly-report-sync-trigger','Description PlaceHolder');
INSERT IGNORE INTO pac_config_key_metadata (`cfkey`,`description`) VALUES ('date.format','Description PlaceHolder');
INSERT IGNORE INTO pac_config_key_metadata (`cfkey`,`description`) VALUES ('days-range.age','Description PlaceHolder');
INSERT IGNORE INTO pac_config_key_metadata (`cfkey`,`description`) VALUES ('discovery.role','');
INSERT IGNORE INTO pac_config_key_metadata (`cfkey`,`description`) VALUES ('elastic-search.admin-host','Description PlaceHolder');
INSERT IGNORE INTO pac_config_key_metadata (`cfkey`,`description`) VALUES ('elastic-search.clusterName','Description PlaceHolder');
INSERT IGNORE INTO pac_config_key_metadata (`cfkey`,`description`) VALUES ('elastic-search.clusterName-heimdall','Description PlaceHolder');
INSERT IGNORE INTO pac_config_key_metadata (`cfkey`,`description`) VALUES ('elastic-search.dev-ingest-host','Description PlaceHolder');
INSERT IGNORE INTO pac_config_key_metadata (`cfkey`,`description`) VALUES ('elastic-search.dev-ingest-port','Description PlaceHolder');
INSERT IGNORE INTO pac_config_key_metadata (`cfkey`,`description`) VALUES ('elastic-search.host','Description PlaceHolder');
INSERT IGNORE INTO pac_config_key_metadata (`cfkey`,`description`) VALUES ('elastic-search.host-heimdall','Description PlaceHolder');
INSERT IGNORE INTO pac_config_key_metadata (`cfkey`,`description`) VALUES ('elastic-search.port','Description PlaceHolder');
INSERT IGNORE INTO pac_config_key_metadata (`cfkey`,`description`) VALUES ('elastic-search.port-admin','Description PlaceHolder');
INSERT IGNORE INTO pac_config_key_metadata (`cfkey`,`description`) VALUES ('elastic-search.port-admin-heimdall','Description PlaceHolder');
INSERT IGNORE INTO pac_config_key_metadata (`cfkey`,`description`) VALUES ('elastic-search.port-heimdall','Description PlaceHolder');
INSERT IGNORE INTO pac_config_key_metadata (`cfkey`,`description`) VALUES ('elastic-search.update-clusterName','Description PlaceHolder');
INSERT IGNORE INTO pac_config_key_metadata (`cfkey`,`description`) VALUES ('elastic-search.update-host','Description PlaceHolder');
INSERT IGNORE INTO pac_config_key_metadata (`cfkey`,`description`) VALUES ('elastic-search.update-port','Description PlaceHolder');
INSERT IGNORE INTO pac_config_key_metadata (`cfkey`,`description`) VALUES ('endpoints.refresh.sensitive','Description PlaceHolder');
INSERT IGNORE INTO pac_config_key_metadata (`cfkey`,`description`) VALUES ('features.certificate.enabled','Description PlaceHolder');
INSERT IGNORE INTO pac_config_key_metadata (`cfkey`,`description`) VALUES ('features.patching.enabled','Description PlaceHolder');
INSERT IGNORE INTO pac_config_key_metadata (`cfkey`,`description`) VALUES ('features.vulnerability.enabled','Description PlaceHolder');
INSERT IGNORE INTO pac_config_key_metadata (`cfkey`,`description`) VALUES ('formats.date','Description PlaceHolder');
INSERT IGNORE INTO pac_config_key_metadata (`cfkey`,`description`) VALUES ('hystrix.command.default.execution.isolation.thread.timeoutInMilliseconds','Description PlaceHolder');
INSERT IGNORE INTO pac_config_key_metadata (`cfkey`,`description`) VALUES ('hystrix.shareSecurityContext','Description PlaceHolder');
INSERT IGNORE INTO pac_config_key_metadata (`cfkey`,`description`) VALUES ('job.lambda.action-disabled','Description PlaceHolder');
INSERT IGNORE INTO pac_config_key_metadata (`cfkey`,`description`) VALUES ('job.lambda.action-enabled','Description PlaceHolder');
INSERT IGNORE INTO pac_config_key_metadata (`cfkey`,`description`) VALUES ('job.lambda.function-arn','Description PlaceHolder');
INSERT IGNORE INTO pac_config_key_metadata (`cfkey`,`description`) VALUES ('job.lambda.function-name','Description PlaceHolder');
INSERT IGNORE INTO pac_config_key_metadata (`cfkey`,`description`) VALUES ('job.lambda.principal','Description PlaceHolder');
INSERT IGNORE INTO pac_config_key_metadata (`cfkey`,`description`) VALUES ('job.lambda.target-id','Description PlaceHolder');
INSERT IGNORE INTO pac_config_key_metadata (`cfkey`,`description`) VALUES ('job.s3.bucket-name','Description PlaceHolder');
INSERT IGNORE INTO pac_config_key_metadata (`cfkey`,`description`) VALUES ('ldap.ad.domain','Description PlaceHolder');
INSERT IGNORE INTO pac_config_key_metadata (`cfkey`,`description`) VALUES ('ldap.ad.provider-url','Description PlaceHolder');
INSERT IGNORE INTO pac_config_key_metadata (`cfkey`,`description`) VALUES ('ldap.ad.search-base','Description PlaceHolder');
INSERT IGNORE INTO pac_config_key_metadata (`cfkey`,`description`) VALUES ('ldap.baseDn','Description PlaceHolder');
INSERT IGNORE INTO pac_config_key_metadata (`cfkey`,`description`) VALUES ('ldap.connectionTimeout','Description PlaceHolder');
INSERT IGNORE INTO pac_config_key_metadata (`cfkey`,`description`) VALUES ('ldap.domain','Description PlaceHolder');
INSERT IGNORE INTO pac_config_key_metadata (`cfkey`,`description`) VALUES ('ldap.hostList','Description PlaceHolder');
INSERT IGNORE INTO pac_config_key_metadata (`cfkey`,`description`) VALUES ('ldap.naming.authentication','Description PlaceHolder');
INSERT IGNORE INTO pac_config_key_metadata (`cfkey`,`description`) VALUES ('ldap.naming.context-factory','Description PlaceHolder');
INSERT IGNORE INTO pac_config_key_metadata (`cfkey`,`description`) VALUES ('ldap.nt.domain','Description PlaceHolder');
INSERT IGNORE INTO pac_config_key_metadata (`cfkey`,`description`) VALUES ('ldap.nt.provider-url','Description PlaceHolder');
INSERT IGNORE INTO pac_config_key_metadata (`cfkey`,`description`) VALUES ('ldap.nt.search-base','Description PlaceHolder');
INSERT IGNORE INTO pac_config_key_metadata (`cfkey`,`description`) VALUES ('ldap.port','Description PlaceHolder');
INSERT IGNORE INTO pac_config_key_metadata (`cfkey`,`description`) VALUES ('ldap.responseTimeout','Description PlaceHolder');
INSERT IGNORE INTO pac_config_key_metadata (`cfkey`,`description`) VALUES ('logging.config','Description PlaceHolder');
INSERT IGNORE INTO pac_config_key_metadata (`cfkey`,`description`) VALUES ('logging.consoleLoggingLevel','Description PlaceHolder');
INSERT IGNORE INTO pac_config_key_metadata (`cfkey`,`description`) VALUES ('logging.esHost','Description PlaceHolder');
INSERT IGNORE INTO pac_config_key_metadata (`cfkey`,`description`) VALUES ('logging.esLoggingLevel','Description PlaceHolder');
INSERT IGNORE INTO pac_config_key_metadata (`cfkey`,`description`) VALUES ('logging.esPort','Description PlaceHolder');
INSERT IGNORE INTO pac_config_key_metadata (`cfkey`,`description`) VALUES ('magenta.cache.name','Description PlaceHolder');
INSERT IGNORE INTO pac_config_key_metadata (`cfkey`,`description`) VALUES ('magenta.default-background','Description PlaceHolder');
INSERT IGNORE INTO pac_config_key_metadata (`cfkey`,`description`) VALUES ('magenta.error-background','Description PlaceHolder');
INSERT IGNORE INTO pac_config_key_metadata (`cfkey`,`description`) VALUES ('magenta.goodbye-background','Description PlaceHolder');
INSERT IGNORE INTO pac_config_key_metadata (`cfkey`,`description`) VALUES ('magenta.goodbye-greeting','Description PlaceHolder');
INSERT IGNORE INTO pac_config_key_metadata (`cfkey`,`description`) VALUES ('magenta.welcome-background','Description PlaceHolder');
INSERT IGNORE INTO pac_config_key_metadata (`cfkey`,`description`) VALUES ('magenta.welcome-greeting','Description PlaceHolder');
INSERT IGNORE INTO pac_config_key_metadata (`cfkey`,`description`) VALUES ('management.endpoints.web.exposure.include','Description PlaceHolder');
INSERT IGNORE INTO pac_config_key_metadata (`cfkey`,`description`) VALUES ('management.health.rabbit.enabled','Description PlaceHolder');
INSERT IGNORE INTO pac_config_key_metadata (`cfkey`,`description`) VALUES ('management.security.enabled','Description PlaceHolder');
INSERT IGNORE INTO pac_config_key_metadata (`cfkey`,`description`) VALUES ('monitoring.contextRootNames','Description PlaceHolder');
INSERT IGNORE INTO pac_config_key_metadata (`cfkey`,`description`) VALUES ('pacman.api.oauth2.client-id','Description PlaceHolder');
INSERT IGNORE INTO pac_config_key_metadata (`cfkey`,`description`) VALUES ('pacman.api.oauth2.client-secret','Description PlaceHolder');
INSERT IGNORE INTO pac_config_key_metadata (`cfkey`,`description`) VALUES ('pacman.service-password','Description PlaceHolder');
INSERT IGNORE INTO pac_config_key_metadata (`cfkey`,`description`) VALUES ('pacman.service-user','Description PlaceHolder');
INSERT IGNORE INTO pac_config_key_metadata (`cfkey`,`description`) VALUES ('pacman.url','Description PlaceHolder');
INSERT IGNORE INTO pac_config_key_metadata (`cfkey`,`description`) VALUES ('projections.assetgroups','Description PlaceHolder');
INSERT IGNORE INTO pac_config_key_metadata (`cfkey`,`description`) VALUES ('projections.targetTypes','Description PlaceHolder');
INSERT IGNORE INTO pac_config_key_metadata (`cfkey`,`description`) VALUES ('redshift.password','Description PlaceHolder');
INSERT IGNORE INTO pac_config_key_metadata (`cfkey`,`description`) VALUES ('redshift.url','Description PlaceHolder');
INSERT IGNORE INTO pac_config_key_metadata (`cfkey`,`description`) VALUES ('redshift.userName','Description PlaceHolder');
INSERT IGNORE INTO pac_config_key_metadata (`cfkey`,`description`) VALUES ('remind.cron','Description PlaceHolder');
INSERT IGNORE INTO pac_config_key_metadata (`cfkey`,`description`) VALUES ('remind.email.subject','Description PlaceHolder');
INSERT IGNORE INTO pac_config_key_metadata (`cfkey`,`description`) VALUES ('remind.email.text','Description PlaceHolder');
INSERT IGNORE INTO pac_config_key_metadata (`cfkey`,`description`) VALUES ('rule-engine.invoke.url','Description PlaceHolder');
INSERT IGNORE INTO pac_config_key_metadata (`cfkey`,`description`) VALUES ('rule.lambda.action-disabled','Description PlaceHolder');
INSERT IGNORE INTO pac_config_key_metadata (`cfkey`,`description`) VALUES ('rule.lambda.action-enabled','Description PlaceHolder');
INSERT IGNORE INTO pac_config_key_metadata (`cfkey`,`description`) VALUES ('rule.lambda.function-arn','Description PlaceHolder');
INSERT IGNORE INTO pac_config_key_metadata (`cfkey`,`description`) VALUES ('rule.lambda.function-name','Description PlaceHolder');
INSERT IGNORE INTO pac_config_key_metadata (`cfkey`,`description`) VALUES ('rule.lambda.principal','Description PlaceHolder');
INSERT IGNORE INTO pac_config_key_metadata (`cfkey`,`description`) VALUES ('rule.lambda.target-id','Description PlaceHolder');
INSERT IGNORE INTO pac_config_key_metadata (`cfkey`,`description`) VALUES ('rule.s3.bucket-name','Description PlaceHolder');
INSERT IGNORE INTO pac_config_key_metadata (`cfkey`,`description`) VALUES ('security.basic.enabled','Description PlaceHolder');
INSERT IGNORE INTO pac_config_key_metadata (`cfkey`,`description`) VALUES ('security.oauth2.resource.user-info-uri','Description PlaceHolder');
INSERT IGNORE INTO pac_config_key_metadata (`cfkey`,`description`) VALUES ('server.context-path','Description PlaceHolder');
INSERT IGNORE INTO pac_config_key_metadata (`cfkey`,`description`) VALUES ('server.contextPath','Description PlaceHolder');
INSERT IGNORE INTO pac_config_key_metadata (`cfkey`,`description`) VALUES ('server.servlet.context-path','Description PlaceHolder');
INSERT IGNORE INTO pac_config_key_metadata (`cfkey`,`description`) VALUES ('service.dns.name','Description PlaceHolder');
INSERT IGNORE INTO pac_config_key_metadata (`cfkey`,`description`) VALUES ('service.url.admin','Description PlaceHolder');
INSERT IGNORE INTO pac_config_key_metadata (`cfkey`,`description`) VALUES ('service.url.asset','Description PlaceHolder');
INSERT IGNORE INTO pac_config_key_metadata (`cfkey`,`description`) VALUES ('service.url.auth','Description PlaceHolder');
INSERT IGNORE INTO pac_config_key_metadata (`cfkey`,`description`) VALUES ('service.url.compliance','Description PlaceHolder');
INSERT IGNORE INTO pac_config_key_metadata (`cfkey`,`description`) VALUES ('service.url.devstandards','Description PlaceHolder');
INSERT IGNORE INTO pac_config_key_metadata (`cfkey`,`description`) VALUES ('service.url.pac_auth','Description PlaceHolder');
INSERT IGNORE INTO pac_config_key_metadata (`cfkey`,`description`) VALUES ('service.url.statistics','Description PlaceHolder');
INSERT IGNORE INTO pac_config_key_metadata (`cfkey`,`description`) VALUES ('spring.boot.admin.client.instance.health-url','Description PlaceHolder');
INSERT IGNORE INTO pac_config_key_metadata (`cfkey`,`description`) VALUES ('spring.boot.admin.client.instance.management-url','Description PlaceHolder');
INSERT IGNORE INTO pac_config_key_metadata (`cfkey`,`description`) VALUES ('spring.boot.admin.client.instance.service-url','Description PlaceHolder');
INSERT IGNORE INTO pac_config_key_metadata (`cfkey`,`description`) VALUES ('spring.boot.admin.client.password','Description PlaceHolder');
INSERT IGNORE INTO pac_config_key_metadata (`cfkey`,`description`) VALUES ('spring.boot.admin.client.url[0]','Description PlaceHolder');
INSERT IGNORE INTO pac_config_key_metadata (`cfkey`,`description`) VALUES ('spring.boot.admin.client.username','Description PlaceHolder');
INSERT IGNORE INTO pac_config_key_metadata (`cfkey`,`description`) VALUES ('spring.cache.cache-names','Description PlaceHolder');
INSERT IGNORE INTO pac_config_key_metadata (`cfkey`,`description`) VALUES ('spring.cache.caffeine.spec','Description PlaceHolder');
INSERT IGNORE INTO pac_config_key_metadata (`cfkey`,`description`) VALUES ('spring.cloud.bus.enabled','Description PlaceHolder');
INSERT IGNORE INTO pac_config_key_metadata (`cfkey`,`description`) VALUES ('spring.datasource.driver-class-name','Description PlaceHolder');
INSERT IGNORE INTO pac_config_key_metadata (`cfkey`,`description`) VALUES ('spring.datasource.password','Description PlaceHolder');
INSERT IGNORE INTO pac_config_key_metadata (`cfkey`,`description`) VALUES ('spring.datasource.url','Description PlaceHolder');
INSERT IGNORE INTO pac_config_key_metadata (`cfkey`,`description`) VALUES ('spring.datasource.username','Description PlaceHolder');
INSERT IGNORE INTO pac_config_key_metadata (`cfkey`,`description`) VALUES ('spring.jpa.hibernate.naming.physical-strategy','Description PlaceHolder');
INSERT IGNORE INTO pac_config_key_metadata (`cfkey`,`description`) VALUES ('spring.mail.defaultEncoding','Description PlaceHolder');
INSERT IGNORE INTO pac_config_key_metadata (`cfkey`,`description`) VALUES ('spring.mail.host','Description PlaceHolder');
INSERT IGNORE INTO pac_config_key_metadata (`cfkey`,`description`) VALUES ('spring.mail.port','Description PlaceHolder');
INSERT IGNORE INTO pac_config_key_metadata (`cfkey`,`description`) VALUES ('spring.mail.protocol','Description PlaceHolder');
INSERT IGNORE INTO pac_config_key_metadata (`cfkey`,`description`) VALUES ('spring.sleuth.sampler.probability','');
INSERT IGNORE INTO pac_config_key_metadata (`cfkey`,`description`) VALUES ('spring.zipkin.baseUrl','');
INSERT IGNORE INTO pac_config_key_metadata (`cfkey`,`description`) VALUES ('spring.zipkin.sender.type','');
INSERT IGNORE INTO pac_config_key_metadata (`cfkey`,`description`) VALUES ('swagger.auth.whitelist','Description PlaceHolder');
INSERT IGNORE INTO pac_config_key_metadata (`cfkey`,`description`) VALUES ('tagging.mandatoryTags','Description PlaceHolder');
INSERT IGNORE INTO pac_config_key_metadata (`cfkey`,`description`) VALUES ('target-types.categories','Description PlaceHolder');
INSERT IGNORE INTO pac_config_key_metadata (`cfkey`,`description`) VALUES ('template.digest-mail.url','Description PlaceHolder');
INSERT IGNORE INTO pac_config_key_metadata (`cfkey`,`description`) VALUES ('time.zone','');
INSERT IGNORE INTO pac_config_key_metadata (`cfkey`,`description`) VALUES ('vulnerability.summary.severity','Description PlaceHolder');
INSERT IGNORE INTO pac_config_key_metadata (`cfkey`,`description`) VALUES ('vulnerability.types','Description PlaceHolder');
INSERT IGNORE INTO pac_config_key_metadata (`cfkey`,`description`) VALUES ('s3.role','');
INSERT IGNORE INTO pac_config_key_metadata (`cfkey`,`description`) VALUES ('s3.region','');
INSERT IGNORE INTO pac_config_key_metadata (`cfkey`,`description`) VALUES ('s3.processed','');
INSERT IGNORE INTO pac_config_key_metadata (`cfkey`,`description`) VALUES ('s3.data','');
INSERT IGNORE INTO pac_config_key_metadata (`cfkey`,`description`) VALUES ('s3','');
INSERT IGNORE INTO pac_config_key_metadata (`cfkey`,`description`) VALUES ('region.ignore','');
INSERT IGNORE INTO pac_config_key_metadata (`cfkey`,`description`) VALUES ('file.path','');
INSERT IGNORE INTO pac_config_key_metadata (`cfkey`,`description`) VALUES ('base.region','');
INSERT IGNORE INTO pac_config_key_metadata (`cfkey`,`description`) VALUES ('spring.mail.username','Description PlaceHolder');
INSERT IGNORE INTO pac_config_key_metadata (`cfkey`,`description`) VALUES ('spring.mail.password','Description PlaceHolder');
INSERT IGNORE INTO pac_config_key_metadata (`cfkey`,`description`) VALUES ('spring.mail.properties.mail.smtp.auth','Description PlaceHolder');
INSERT IGNORE INTO pac_config_key_metadata (`cfkey`,`description`) VALUES ('spring.mail.properties.mail.smtp.ssl.trust','Description PlaceHolder');
INSERT IGNORE INTO pac_config_key_metadata (`cfkey`,`description`) VALUES ('spring.mail.properties.mail.smtp.starttls.enable','Description PlaceHolder');
INSERT IGNORE INTO pac_config_key_metadata (`cfkey`,`description`) VALUES ('spring.mail.test-connection','Description PlaceHolder');
INSERT IGNORE INTO pac_config_key_metadata (`cfkey`,`description`) VALUES ('email.banner','Description PlaceHolder');
INSERT IGNORE INTO pac_config_key_metadata (`cfkey`,`description`) VALUES ('pacbot.autofix.resourceowner.fallbak.email','Description PlaceHolder');
INSERT IGNORE INTO pac_config_key_metadata (`cfkey`,`description`) VALUES ('scheduler.interval','Description PlaceHolder');
INSERT IGNORE INTO pac_config_key_metadata (`cfkey`,`description`) VALUES ('scheduler.rules.initial.delay','Description PlaceHolder');
INSERT IGNORE INTO pac_config_key_metadata (`cfkey`,`description`) VALUES ('scheduler.shipper.initial.delay','Description PlaceHolder');
INSERT IGNORE INTO pac_config_key_metadata (`cfkey`,`description`) VALUES ('scheduler.role','Description PlaceHolder');
INSERT IGNORE INTO pac_config_key_metadata (`cfkey`,`description`) VALUES ('scheduler.collector.initial.delay','Description PlaceHolder');
INSERT IGNORE INTO pac_config_key_metadata (`cfkey`,`description`) VALUES ('azure.enabled','Description PlaceHolder');
INSERT IGNORE INTO pac_config_key_metadata (`cfkey`,`description`) VALUES ('gcp.enabled','Description PlaceHolder');
INSERT IGNORE INTO pac_config_key_metadata (`cfkey`,`description`) VALUES ('aws.eventbridge.bus.details','Description PlaceHolder');
INSERT IGNORE INTO pac_config_key_metadata (`cfkey`,`description`) VALUES ('azure.eventbridge.bus.details','Description PlaceHolder');
INSERT IGNORE INTO pac_config_key_metadata (`cfkey`,`description`) VALUES ('gcp.eventbridge.bus.details','Description PlaceHolder');
INSERT IGNORE INTO pac_config_key_metadata (`cfkey`,`description`) VALUES ('scheduler.total.batches','Description PlaceHolder');


INSERT IGNORE INTO `pac_config_key_metadata` (`cfkey`, `description`) VALUES('pacman.es.host','Description PlaceHolder');
INSERT IGNORE INTO `pac_config_key_metadata` (`cfkey`, `description`) VALUES('pacman.es.port','Description PlaceHolder');
INSERT IGNORE INTO `pac_config_key_metadata` (`cfkey`, `description`) VALUES('esLoggingLevel','Description PlaceHolder');
INSERT IGNORE INTO `pac_config_key_metadata` (`cfkey`, `description`) values('heimdall-host','Description PlaceHolder');
INSERT IGNORE INTO `pac_config_key_metadata` (`cfkey`, `description`) values('heimdall-port','Description PlaceHolder');
INSERT IGNORE INTO `pac_config_key_metadata` (`cfkey`, `description`) values('pacman.host','Description PlaceHolder');
INSERT IGNORE INTO `pac_config_key_metadata` (`cfkey`, `description`) values('pacman.auto.fix.mail.cc.to','Description PlaceHolder');
INSERT IGNORE INTO `pac_config_key_metadata` (`cfkey`, `description`) values('pacman.auto.fix.orphan.resource.owner','Description PlaceHolder');
INSERT IGNORE INTO `pac_config_key_metadata` (`cfkey`, `description`) values('pacman.auto.fix.role.name','Description PlaceHolder');
INSERT IGNORE INTO `pac_config_key_metadata` (`cfkey`, `description`) values('pacman.integrations.slack.webhook.url','Description PlaceHolder');
INSERT IGNORE INTO `pac_config_key_metadata` (`cfkey`, `description`) values('pacman.target.type.alias','Description PlaceHolder');
INSERT IGNORE INTO `pac_config_key_metadata` (`cfkey`, `description`) values('autofix.allowlist.accounts.S3GlobalAccess_version-1_S3BucketShouldnotpubliclyaccessble_s3','Description PlaceHolder');
INSERT IGNORE INTO `pac_config_key_metadata` (`cfkey`, `description`) values('autofix.allowlist.accounts.EC2WithPublicIPAccess_version-1_Ec2WithPublicAccess_ec2','Description PlaceHolder');
INSERT IGNORE INTO `pac_config_key_metadata` (`cfkey`, `description`) values('autofix.cufoff.date','Description PlaceHolder');
INSERT IGNORE INTO `pac_config_key_metadata` (`cfkey`, `description`) values('api.backup.asset.config','Description PlaceHolder');
INSERT IGNORE INTO `pac_config_key_metadata` (`cfkey`, `description`) values('api.resource.creationdate','Description PlaceHolder');
INSERT IGNORE INTO `pac_config_key_metadata` (`cfkey`, `description`) values('api.getlastaction','Description PlaceHolder');
INSERT IGNORE INTO `pac_config_key_metadata` (`cfkey`, `description`) values('api.postlastaction','Description PlaceHolder');
INSERT IGNORE INTO `pac_config_key_metadata` (`cfkey`, `description`) values('api.register.reactors.url','Description PlaceHolder');
INSERT IGNORE INTO `pac_config_key_metadata` (`cfkey`, `description`) values('api.auth.owner.slack.handle','Description PlaceHolder');
INSERT IGNORE INTO `pac_config_key_metadata` (`cfkey`, `description`) values('pacman.auto.fix.tag.name','Description PlaceHolder');
INSERT IGNORE INTO `pac_config_key_metadata` (`cfkey`, `description`) values('pacman.auto.fix.max.email.notifications','Description PlaceHolder');
INSERT IGNORE INTO `pac_config_key_metadata` (`cfkey`, `description`) values('pacman.auto.fix.resource.name.filter.pattern','Description PlaceHolder');
INSERT IGNORE INTO `pac_config_key_metadata` (`cfkey`, `description`) values('pacman.es.stats.index','Description PlaceHolder');
INSERT IGNORE INTO `pac_config_key_metadata` (`cfkey`, `description`) values('pacman.es.stats.type','Description PlaceHolder');
INSERT IGNORE INTO `pac_config_key_metadata` (`cfkey`, `description`) values('pacman.es.auto.fix.transaction.index','Description PlaceHolder');
INSERT IGNORE INTO `pac_config_key_metadata` (`cfkey`, `description`) values('pacman.es.auto.fix.transaction.type','Description PlaceHolder');
INSERT IGNORE INTO `pac_config_key_metadata` (`cfkey`, `description`) values('pacman.api.sendmail','Description PlaceHolder');
INSERT IGNORE INTO `pac_config_key_metadata` (`cfkey`, `description`) values('pacman.es.reactors.index','Description PlaceHolder');
INSERT IGNORE INTO `pac_config_key_metadata` (`cfkey`, `description`) values('pacman.es.reactors.registry','Description PlaceHolder');
INSERT IGNORE INTO `pac_config_key_metadata` (`cfkey`, `description`) values('square.one.slack.channel','Description PlaceHolder');
INSERT IGNORE INTO `pac_config_key_metadata` (`cfkey`, `description`) values('com.tmobile.pacman.reactors.impl.s3.S3CreateBucketAndUpdateBucketPolicyReactor.account.whitelist','Description PlaceHolder');
INSERT IGNORE INTO `pac_config_key_metadata` (`cfkey`, `description`) values('com.tmobile.pacman.reactors.impl.sample.SampleReactor.account.whitelist','Description PlaceHolder');
INSERT IGNORE INTO `pac_config_key_metadata` (`cfkey`, `description`) values('com.tmobile.pacman.reactors.impl.sample.SampleReactor2.account.whitelist','Description PlaceHolder');
INSERT IGNORE INTO `pac_config_key_metadata` (`cfkey`, `description`) values('pacman.auto.fix.mail.from','Description PlaceHolder');
INSERT IGNORE INTO `pac_config_key_metadata` (`cfkey`, `description`) values('pacman.auto.fix.tag.salt','Description PlaceHolder');
INSERT IGNORE INTO `pac_config_key_metadata` (`cfkey`, `description`) values('pacman.auto.fix.tag.encyption.algorithm','Description PlaceHolder');
INSERT IGNORE INTO `pac_config_key_metadata` (`cfkey`, `description`) values('pacman.exempted.mail.subject','Description PlaceHolder');
INSERT IGNORE INTO `pac_config_key_metadata` (`cfkey`, `description`) values('pacman.autofix.exempted.types.for.cutoff.data','Description PlaceHolder');
INSERT IGNORE INTO `pac_config_key_metadata` (`cfkey`, `description`) values('pacman.autofix.non.taggable.services','Description PlaceHolder');
INSERT IGNORE INTO `pac_config_key_metadata` (`cfkey`, `description`) values('pacman.autofix.policy.url.S3GlobalAccess_version-1_S3BucketShouldnotpubliclyaccessble_s3','Description PlaceHolder');
INSERT IGNORE INTO `pac_config_key_metadata` (`cfkey`, `description`) values('pacman.auto.fix.mail.subject.S3GlobalAccess_version-1_S3BucketShouldnotpubliclyaccessble_s3','Description PlaceHolder');
INSERT IGNORE INTO `pac_config_key_metadata` (`cfkey`, `description`) values('pacman.auto.warning.mail.subject.S3GlobalAccess_version-1_S3BucketShouldnotpubliclyaccessble_s3','Description PlaceHolder');
INSERT IGNORE INTO `pac_config_key_metadata` (`cfkey`, `description`) values('pacman.autofix.rule.violation.message.S3GlobalAccess_version-1_S3BucketShouldnotpubliclyaccessble_s3','Description PlaceHolder');
INSERT IGNORE INTO `pac_config_key_metadata` (`cfkey`, `description`) values('pacman.autofix.rule.warning.message.S3GlobalAccess_version-1_S3BucketShouldnotpubliclyaccessble_s3','Description PlaceHolder');
INSERT IGNORE INTO `pac_config_key_metadata` (`cfkey`, `description`) values('pacman.autofix.rule.post.fix.message.S3GlobalAccess_version-1_S3BucketShouldnotpubliclyaccessble_s3','Description PlaceHolder');
INSERT IGNORE INTO `pac_config_key_metadata` (`cfkey`, `description`) values('pacman.autofix.waittime.S3GlobalAccess_version-1_S3BucketShouldnotpubliclyaccessble_s3','Description PlaceHolder');
INSERT IGNORE INTO `pac_config_key_metadata` (`cfkey`, `description`) values('pacman.auto.fix.max.email.notifications.S3GlobalAccess_version-1_S3BucketShouldnotpubliclyaccessble_s3','Description PlaceHolder');
INSERT IGNORE INTO `pac_config_key_metadata` (`cfkey`, `description`) values('pacman.autofix.policy.url.EC2WithPublicIPAccess_version-1_Ec2WithPublicAccess_ec2','Description PlaceHolder');
INSERT IGNORE INTO `pac_config_key_metadata` (`cfkey`, `description`) values('pacman.auto.fix.mail.subject.EC2WithPublicIPAccess_version-1_Ec2WithPublicAccess_ec2','Description PlaceHolder');
INSERT IGNORE INTO `pac_config_key_metadata` (`cfkey`, `description`) values('pacman.auto.warning.mail.subject.EC2WithPublicIPAccess_version-1_Ec2WithPublicAccess_ec2','Description PlaceHolder');
INSERT IGNORE INTO `pac_config_key_metadata` (`cfkey`, `description`) values('pacman.autofix.rule.violation.message.EC2WithPublicIPAccess_version-1_Ec2WithPublicAccess_ec2','Description PlaceHolder');
INSERT IGNORE INTO `pac_config_key_metadata` (`cfkey`, `description`) values('pacman.autofix.rule.warning.message.EC2WithPublicIPAccess_version-1_Ec2WithPublicAccess_ec2','Description PlaceHolder');
INSERT IGNORE INTO `pac_config_key_metadata` (`cfkey`, `description`) values('pacman.autofix.rule.post.fix.message.EC2WithPublicIPAccess_version-1_Ec2WithPublicAccess_ec2','Description PlaceHolder');
INSERT IGNORE INTO `pac_config_key_metadata` (`cfkey`, `description`) values('pacman.autofix.waittime.EC2WithPublicIPAccess_version-1_Ec2WithPublicAccess_ec2','Description PlaceHolder');
INSERT IGNORE INTO `pac_config_key_metadata` (`cfkey`, `description`) values('pacman.auto.fix.max.email.notifications.EC2WithPublicIPAccess_version-1_Ec2WithPublicAccess_ec2','Description PlaceHolder');
INSERT IGNORE INTO `pac_config_key_metadata` (`cfkey`, `description`) values('pacman.auto.fix.mail.template.columns.EC2WithPublicIPAccess_version-1_Ec2WithPublicAccess_ec2','Description PlaceHolder');
INSERT IGNORE INTO `pac_config_key_metadata` (`cfkey`, `description`) values('pacman.auto.fix.common.email.notifications.EC2WithPublicIPAccess_version-1_Ec2WithPublicAccess_ec2','Description PlaceHolder');
INSERT IGNORE INTO `pac_config_key_metadata` (`cfkey`, `description`) values('pacman.login.user.name','Description PlaceHolder');
INSERT IGNORE INTO `pac_config_key_metadata` (`cfkey`, `description`) values('pacman.login.password','Description PlaceHolder');

INSERT IGNORE INTO `pac_config_key_metadata` (`cfkey`, `description`) values('autofix.allowlist.accounts.ElbWithPublicAccess_version-1_ApplicationElbWithPublicAccess_appelb','Description PlaceHolder');
INSERT IGNORE INTO `pac_config_key_metadata` (`cfkey`, `description`) values('pacman.auto.fix.mail.subject.ElbWithPublicAccess_version-1_ApplicationElbWithPublicAccess_appelb','Description PlaceHolder');
INSERT IGNORE INTO `pac_config_key_metadata` (`cfkey`, `description`) values('pacman.auto.warning.mail.subject.ElbWithPublicAccess_version-1_ApplicationElbWithPublicAccess_appelb','Description PlaceHolder');
INSERT IGNORE INTO `pac_config_key_metadata` (`cfkey`, `description`) values('pacman.autofix.rule.violation.message.ElbWithPublicAccess_version-1_ApplicationElbWithPublicAccess_appelb','Description PlaceHolder');
INSERT IGNORE INTO `pac_config_key_metadata` (`cfkey`, `description`) values('pacman.autofix.rule.warning.message.ElbWithPublicAccess_version-1_ApplicationElbWithPublicAccess_appelb','Description PlaceHolder');
INSERT IGNORE INTO `pac_config_key_metadata` (`cfkey`, `description`) values('pacman.autofix.rule.post.fix.message.ElbWithPublicAccess_version-1_ApplicationElbWithPublicAccess_appelb','Description PlaceHolder');
INSERT IGNORE INTO `pac_config_key_metadata` (`cfkey`, `description`) values('pacman.autofix.waittime.ElbWithPublicAccess_version-1_ApplicationElbWithPublicAccess_appelb','Description PlaceHolder');
INSERT IGNORE INTO `pac_config_key_metadata` (`cfkey`, `description`) values('pacman.auto.fix.max.email.notifications.ElbWithPublicAccess_version-1_ApplicationElbWithPublicAccess_appelb','Description PlaceHolder');
INSERT IGNORE INTO `pac_config_key_metadata` (`cfkey`, `description`) values('pacman.auto.fix.mail.template.columns.ElbWithPublicAccess_version-1_ApplicationElbWithPublicAccess_appelb','Description PlaceHolder');
INSERT IGNORE INTO `pac_config_key_metadata` (`cfkey`, `description`) values('pacman.auto.fix.common.email.notifications.ElbWithPublicAccess_version-1_ApplicationElbWithPublicAccess_appelb','Description PlaceHolder');
INSERT IGNORE INTO `pac_config_key_metadata` (`cfkey`, `description`) values('pacman.autofix.policy.url.path','Description PlaceHolder');

INSERT IGNORE INTO `pac_config_key_metadata` (`cfkey`, `description`) values('autofix.allowlist.accounts.ElbWithPublicAccess_version-1_ClassicElbWithPublicAccess_classicelb','Description PlaceHolder');
INSERT IGNORE INTO `pac_config_key_metadata` (`cfkey`, `description`) values('pacman.auto.fix.mail.subject.ElbWithPublicAccess_version-1_ClassicElbWithPublicAccess_classicelb','Description PlaceHolder');
INSERT IGNORE INTO `pac_config_key_metadata` (`cfkey`, `description`) values('pacman.auto.warning.mail.subject.ElbWithPublicAccess_version-1_ClassicElbWithPublicAccess_classicelb','Description PlaceHolder');
INSERT IGNORE INTO `pac_config_key_metadata` (`cfkey`, `description`) values('pacman.autofix.rule.violation.message.ElbWithPublicAccess_version-1_ClassicElbWithPublicAccess_classicelb','Description PlaceHolder');
INSERT IGNORE INTO `pac_config_key_metadata` (`cfkey`, `description`) values('pacman.autofix.rule.warning.message.ElbWithPublicAccess_version-1_ClassicElbWithPublicAccess_classicelb','Description PlaceHolder');
INSERT IGNORE INTO `pac_config_key_metadata` (`cfkey`, `description`) values('pacman.autofix.rule.post.fix.message.ElbWithPublicAccess_version-1_ClassicElbWithPublicAccess_classicelb','Description PlaceHolder');
INSERT IGNORE INTO `pac_config_key_metadata` (`cfkey`, `description`) values('pacman.autofix.waittime.ElbWithPublicAccess_version-1_ClassicElbWithPublicAccess_classicelb','Description PlaceHolder');
INSERT IGNORE INTO `pac_config_key_metadata` (`cfkey`, `description`) values('pacman.auto.fix.max.email.notifications.ElbWithPublicAccess_version-1_ClassicElbWithPublicAccess_classicelb','Description PlaceHolder');
INSERT IGNORE INTO `pac_config_key_metadata` (`cfkey`, `description`) values('pacman.auto.fix.mail.template.columns.ElbWithPublicAccess_version-1_ClassicElbWithPublicAccess_classicelb','Description PlaceHolder');
INSERT IGNORE INTO `pac_config_key_metadata` (`cfkey`, `description`) values('pacman.auto.fix.common.email.notifications.ElbWithPublicAccess_version-1_ClassicElbWithPublicAccess_classicelb','Description PlaceHolder');


INSERT IGNORE INTO `pac_config_key_metadata` (`cfkey`, `description`) values('autofix.allowlist.accounts.RedShiftPublicAccess_version-1_RedShiftPublicAccess_redshift','Description PlaceHolder');
INSERT IGNORE INTO `pac_config_key_metadata` (`cfkey`, `description`) values('pacman.auto.fix.mail.subject.RedShiftPublicAccess_version-1_RedShiftPublicAccess_redshift','Description PlaceHolder');
INSERT IGNORE INTO `pac_config_key_metadata` (`cfkey`, `description`) values('pacman.auto.warning.mail.subject.RedShiftPublicAccess_version-1_RedShiftPublicAccess_redshift','Description PlaceHolder');
INSERT IGNORE INTO `pac_config_key_metadata` (`cfkey`, `description`) values('pacman.autofix.rule.violation.message.RedShiftPublicAccess_version-1_RedShiftPublicAccess_redshift','Description PlaceHolder');
INSERT IGNORE INTO `pac_config_key_metadata` (`cfkey`, `description`) values('pacman.autofix.rule.warning.message.RedShiftPublicAccess_version-1_RedShiftPublicAccess_redshift','Description PlaceHolder');
INSERT IGNORE INTO `pac_config_key_metadata` (`cfkey`, `description`) values('pacman.autofix.rule.post.fix.message.RedShiftPublicAccess_version-1_RedShiftPublicAccess_redshift','Description PlaceHolder');
INSERT IGNORE INTO `pac_config_key_metadata` (`cfkey`, `description`) values('pacman.autofix.waittime.RedShiftPublicAccess_version-1_RedShiftPublicAccess_redshift','Description PlaceHolder');
INSERT IGNORE INTO `pac_config_key_metadata` (`cfkey`, `description`) values('pacman.auto.fix.max.email.notifications.RedShiftPublicAccess_version-1_RedShiftPublicAccess_redshift','Description PlaceHolder');
INSERT IGNORE INTO `pac_config_key_metadata` (`cfkey`, `description`) values('pacman.auto.fix.mail.template.columns.RedShiftPublicAccess_version-1_RedShiftPublicAccess_redshift','Description PlaceHolder');
INSERT IGNORE INTO `pac_config_key_metadata` (`cfkey`, `description`) values('pacman.auto.fix.common.email.notifications.RedShiftPublicAccess_version-1_RedShiftPublicAccess_redshift','Description PlaceHolder');

INSERT IGNORE INTO `pac_config_key_metadata` (`cfkey`, `description`) values('autofix.allowlist.accounts.rdsdb_version-1_RdsDbPublicAccess_rdsdb','Description PlaceHolder');
INSERT IGNORE INTO `pac_config_key_metadata` (`cfkey`, `description`) values('pacman.auto.fix.mail.subject.rdsdb_version-1_RdsDbPublicAccess_rdsdb','Description PlaceHolder');
INSERT IGNORE INTO `pac_config_key_metadata` (`cfkey`, `description`) values('pacman.auto.warning.mail.subject.rdsdb_version-1_RdsDbPublicAccess_rdsdb','Description PlaceHolder');
INSERT IGNORE INTO `pac_config_key_metadata` (`cfkey`, `description`) values('pacman.autofix.rule.violation.message.rdsdb_version-1_RdsDbPublicAccess_rdsdb','Description PlaceHolder');
INSERT IGNORE INTO `pac_config_key_metadata` (`cfkey`, `description`) values('pacman.autofix.rule.warning.message.rdsdb_version-1_RdsDbPublicAccess_rdsdb','Description PlaceHolder');
INSERT IGNORE INTO `pac_config_key_metadata` (`cfkey`, `description`) values('pacman.autofix.rule.post.fix.message.rdsdb_version-1_RdsDbPublicAccess_rdsdb','Description PlaceHolder');
INSERT IGNORE INTO `pac_config_key_metadata` (`cfkey`, `description`) values('pacman.autofix.waittime.rdsdb_version-1_RdsDbPublicAccess_rdsdb','Description PlaceHolder');
INSERT IGNORE INTO `pac_config_key_metadata` (`cfkey`, `description`) values('pacman.auto.fix.max.email.notifications.rdsdb_version-1_RdsDbPublicAccess_rdsdb','Description PlaceHolder');
INSERT IGNORE INTO `pac_config_key_metadata` (`cfkey`, `description`) values('pacman.auto.fix.mail.template.columns.rdsdb_version-1_RdsDbPublicAccess_rdsdb','Description PlaceHolder');
INSERT IGNORE INTO `pac_config_key_metadata` (`cfkey`, `description`) values('pacman.auto.fix.common.email.notifications.rdsdb_version-1_RdsDbPublicAccess_rdsdb','Description PlaceHolder');

INSERT IGNORE INTO `pac_config_key_metadata` (`cfkey`, `description`) values('autofix.allowlist.accounts.Unused-Security-group_version-1_UnusedSecurityGroup_sg','Description PlaceHolder');
INSERT IGNORE INTO `pac_config_key_metadata` (`cfkey`, `description`) values('pacman.autofix.contact.Unused-Security-group_version-1_UnusedSecurityGroup_sg','Description PlaceHolder');
INSERT IGNORE INTO `pac_config_key_metadata` (`cfkey`, `description`) values('pacman.autofix.fix.type.Unused-Security-group_version-1_UnusedSecurityGroup_sg','Description PlaceHolder');
INSERT IGNORE INTO `pac_config_key_metadata` (`cfkey`, `description`) values('pacman.auto.fix.mail.subject.Unused-Security-group_version-1_UnusedSecurityGroup_sg','Description PlaceHolder');
INSERT IGNORE INTO `pac_config_key_metadata` (`cfkey`, `description`) values('pacman.autofix.rule.post.fix.message.Unused-Security-group_version-1_UnusedSecurityGroup_sg','Description PlaceHolder');
INSERT IGNORE INTO `pac_config_key_metadata` (`cfkey`, `description`) values('pacman.autofix.fix.notify.Unused-Security-group_version-1_UnusedSecurityGroup_sg','Description PlaceHolder');
INSERT IGNORE INTO `pac_config_key_metadata` (`cfkey`, `description`) values('pacman.auto.fix.mail.template.columns.Unused-Security-group_version-1_UnusedSecurityGroup_sg','Description PlaceHolder');
INSERT IGNORE INTO `pac_config_key_metadata` (`cfkey`, `description`) values('pacman.auto.fix.common.email.notifications.Unused-Security-group_version-1_UnusedSecurityGroup_sg','Description PlaceHolder');

INSERT IGNORE INTO `pac_config_key_metadata` (`cfkey`, `description`) values('autofix.allowlist.accounts.ElasticSearchPublicAccess_version-1_ElasticSearchPublicAccessRule_elasticsearch','Description PlaceHolder');
INSERT IGNORE INTO `pac_config_key_metadata` (`cfkey`, `description`) values('pacman.auto.fix.mail.subject.ElasticSearchPublicAccess_version-1_ElasticSearchPublicAccessRule_elasticsearch','Description PlaceHolder');
INSERT IGNORE INTO `pac_config_key_metadata` (`cfkey`, `description`) values('pacman.auto.warning.mail.subject.ElasticSearchPublicAccess_version-1_ElasticSearchPublicAccessRule_elasticsearch','Description PlaceHolder');
INSERT IGNORE INTO `pac_config_key_metadata` (`cfkey`, `description`) values('pacman.autofix.rule.violation.message.ElasticSearchPublicAccess_version-1_ElasticSearchPublicAccessRule_elasticsearch','Description PlaceHolder');
INSERT IGNORE INTO `pac_config_key_metadata` (`cfkey`, `description`) values('pacman.autofix.rule.warning.message.ElasticSearchPublicAccess_version-1_ElasticSearchPublicAccessRule_elasticsearch','Description PlaceHolder');
INSERT IGNORE INTO `pac_config_key_metadata` (`cfkey`, `description`) values('pacman.autofix.rule.post.fix.message.ElasticSearchPublicAccess_version-1_ElasticSearchPublicAccessRule_elasticsearch','Description PlaceHolder');
INSERT IGNORE INTO `pac_config_key_metadata` (`cfkey`, `description`) values('pacman.autofix.waittime.ElasticSearchPublicAccess_version-1_ElasticSearchPublicAccessRule_elasticsearch','Description PlaceHolder');
INSERT IGNORE INTO `pac_config_key_metadata` (`cfkey`, `description`) values('pacman.auto.fix.max.email.notifications.ElasticSearchPublicAccess_version-1_ElasticSearchPublicAccessRule_elasticsearch','Description PlaceHolder');
INSERT IGNORE INTO `pac_config_key_metadata` (`cfkey`, `description`) values('pacman.auto.fix.mail.template.columns.ElasticSearchPublicAccess_version-1_ElasticSearchPublicAccessRule_elasticsearch','Description PlaceHolder');
INSERT IGNORE INTO `pac_config_key_metadata` (`cfkey`, `description`) values('pacman.auto.fix.common.email.notifications.ElasticSearchPublicAccess_version-1_ElasticSearchPublicAccessRule_elasticsearch','Description PlaceHolder');
INSERT IGNORE INTO `pac_config_key_metadata` (`cfkey`, `description`) values('deleteSgTag','Description PlaceHolder');

INSERT IGNORE INTO `pac_config_key_metadata` (`cfkey`, `description`) values('autofix.allowlist.accounts.UnusedElasticIpRule_version-1_UnusedElasticIpRule_elasticip','Description PlaceHolder');
INSERT IGNORE INTO `pac_config_key_metadata` (`cfkey`, `description`) values('pacman.autofix.contact.UnusedElasticIpRule_version-1_UnusedElasticIpRule_elasticip','Description PlaceHolder');
INSERT IGNORE INTO `pac_config_key_metadata` (`cfkey`, `description`) values('pacman.autofix.fix.type.UnusedElasticIpRule_version-1_UnusedElasticIpRule_elasticip','Description PlaceHolder');
INSERT IGNORE INTO `pac_config_key_metadata` (`cfkey`, `description`) values('pacman.auto.fix.mail.subject.UnusedElasticIpRule_version-1_UnusedElasticIpRule_elasticip','Description PlaceHolder');
INSERT IGNORE INTO `pac_config_key_metadata` (`cfkey`, `description`) values('pacman.autofix.rule.post.fix.message.UnusedElasticIpRule_version-1_UnusedElasticIpRule_elasticip','Description PlaceHolder');
INSERT IGNORE INTO `pac_config_key_metadata` (`cfkey`, `description`) values('pacman.autofix.fix.notify.UnusedElasticIpRule_version-1_UnusedElasticIpRule_elasticip','Description PlaceHolder');
INSERT IGNORE INTO `pac_config_key_metadata` (`cfkey`, `description`) values('pacman.auto.fix.mail.template.columns.UnusedElasticIpRule_version-1_UnusedElasticIpRule_elasticip','Description PlaceHolder');
INSERT IGNORE INTO `pac_config_key_metadata` (`cfkey`, `description`) values('pacman.auto.fix.common.email.notifications.UnusedElasticIpRule_version-1_UnusedElasticIpRule_elasticip','Description PlaceHolder');
INSERT IGNORE INTO `pac_config_key_metadata` (`cfkey`, `description`) values('pacman.autofix.issue.creation.time.elapsed.UnusedElasticIpRule_version-1_UnusedElasticIpRule_elasticip','Description PlaceHolder');
INSERT IGNORE INTO pac_config_key_metadata (`cfkey`,`description`) VALUES ('service.url.vulnerability','Description PlaceHolder');
INSERT IGNORE INTO pac_config_key_metadata (`cfkey`,`description`) VALUES ('vulnerability.application.occurance','Description PlaceHolder');
INSERT IGNORE INTO pac_config_key_metadata (`cfkey`,`description`) VALUES ('vulnerability.application.resourcedetails','Description PlaceHolder');
INSERT IGNORE INTO pac_config_key_metadata (`cfkey`,`description`) VALUES ('vulnerability.application.resourcedetailsboth','Description PlaceHolder');
INSERT IGNORE INTO `pac_config_key_metadata` (`cfkey`, `description`) values('qualys_info','Base64 encoded user:password of qualys');
INSERT IGNORE INTO `pac_config_key_metadata` (`cfkey`, `description`) values('qualys_api_url','Qualys api url');
INSERT IGNORE INTO `pac_config_key_metadata` (`cfkey`, `description`) values('aqua_client_domain_url','Aqua clinet domain URL');
INSERT IGNORE INTO `pac_config_key_metadata` (`cfkey`, `description`) values('aqua_api_url','Aqua API URL');
INSERT IGNORE INTO `pac_config_key_metadata` (`cfkey`, `description`) values('aqua_username','aqua username');
INSERT IGNORE INTO `pac_config_key_metadata` (`cfkey`, `description`) values('aqua_password','aqua password');
INSERT IGNORE INTO `pac_config_key_metadata` (`cfkey`, `description`) values('default_page_size','Base64 encoded user:password of qualys');
INSERT IGNORE INTO `pac_config_key_metadata` (`cfkey`, `description`) values('aqua_image_vul_query_params','Base64 encoded user:password of qualys');
INSERT IGNORE INTO `pac_config_key_metadata` (`cfkey`, `description`) values('notification.lambda.function.url','lambda_notification');
INSERT IGNORE INTO `pac_config_key_metadata` (`cfkey`, `description`) values('notification.topic.arn','lambda_notification');
INSERT IGNORE INTO `pac_config_key_metadata` (`cfkey`, `description`) values('notification.email.topic.arn','lambda_notification');
INSERT IGNORE INTO `pac_config_key_metadata` (`cfkey`, `description`) values('notification.to.emailid','lambda_notification');

-- delete configs containing http url
DELETE IGNORE FROM pac_config_properties where value like 'http://%elb.amazonaws.com%';
DELETE IGNORE FROM pac_config_properties where cfkey  in ('apiauthinfo');
DELETE IGNORE FROM pac_config_properties where cfkey in ('qualys_info', 'qualys_api_url');
DELETE IGNORE FROM pac_config_properties where cfkey in ('aqua_client_domain_url', 'aqua_api_url','aqua_username','aqua_password','default_page_size','aqua_image_vul_query_params');
DELETE IGNORE FROM pac_config_properties where cfkey in ('notification.lambda.function.url','notification.topic.arn','notification.email.topic.arn','notification.to.emailid');
DELETE IGNORE FROM pac_config_properties where cfkey  in ('pacman.auto.fix.role.name','pacman.auto.fix.mail.cc.to','pacman.auto.fix.orphan.resource.owner');


INSERT IGNORE INTO pac_config_properties (`cfkey`,`value`,`application`,`profile`,`label`,`createdBy`,`createdDate`,`modifiedBy`,`modifiedDate`) VALUES ('notification.lambda.function.url',concat(@NOTIFICATION_FUNCTION_URL,''),'application','prd','latest',NULL,NULL,NULL,NULL);
INSERT IGNORE INTO pac_config_properties (`cfkey`,`value`,`application`,`profile`,`label`,`createdBy`,`createdDate`,`modifiedBy`,`modifiedDate`) VALUES ('notification.topic.arn',concat(@TOPIC_ARN,''),'application','prd','latest',NULL,NULL,NULL,NULL);
INSERT IGNORE INTO pac_config_properties (`cfkey`,`value`,`application`,`profile`,`label`,`createdBy`,`createdDate`,`modifiedBy`,`modifiedDate`) VALUES ('notification.email.topic.arn',concat(@EMAIL_TOPIC_ARN,''),'application','prd','latest',NULL,NULL,NULL,NULL);
INSERT IGNORE INTO pac_config_properties (`cfkey`,`value`,`application`,`profile`,`label`,`createdBy`,`createdDate`,`modifiedBy`,`modifiedDate`) VALUES ('notification.to.emailid',concat(@NOTIFICATION_EMAIL_ID,''),'application','prd','latest',NULL,NULL,NULL,NULL);

INSERT IGNORE INTO pac_config_properties(`cfkey`,`value`,`application`,`profile`,`label`,`createdBy`,`createdDate`,`modifiedBy`,`modifiedDate`) VALUES ('apiauthinfo',REPLACE(TO_BASE64(concat(@API_CLIENT_ID,':',@API_SCERET_ID)),'\n',''),'application','prd','latest',NULL,NULL,NULL,NULL);
INSERT IGNORE INTO pac_config_properties (`cfkey`,`value`,`application`,`profile`,`label`,`createdBy`,`createdDate`,`modifiedBy`,`modifiedDate`) VALUES ('logging.config','classpath:spring-logback.xml','application','prd','latest',NULL,NULL,NULL,NULL);
INSERT IGNORE INTO pac_config_properties (`cfkey`,`value`,`application`,`profile`,`label`,`createdBy`,`createdDate`,`modifiedBy`,`modifiedDate`) VALUES ('logging.esLoggingLevel','WARN','application','prd','latest',NULL,NULL,NULL,NULL);
INSERT IGNORE INTO pac_config_properties (`cfkey`,`value`,`application`,`profile`,`label`,`createdBy`,`createdDate`,`modifiedBy`,`modifiedDate`) VALUES ('logging.consoleLoggingLevel','INFO','application','prd','latest',NULL,NULL,NULL,NULL);
INSERT IGNORE INTO pac_config_properties (`cfkey`,`value`,`application`,`profile`,`label`,`createdBy`,`createdDate`,`modifiedBy`,`modifiedDate`) VALUES ('logging.esHost',concat(@LOGGING_ES_HOST_NAME,''),'application','prd','latest',NULL,NULL,NULL,NULL);
INSERT IGNORE INTO pac_config_properties (`cfkey`,`value`,`application`,`profile`,`label`,`createdBy`,`createdDate`,`modifiedBy`,`modifiedDate`) VALUES ('logging.esPort',concat(@LOGGING_ES_PORT,''),'application','prd','latest',NULL,NULL,NULL,NULL);
INSERT IGNORE INTO pac_config_properties (`cfkey`,`value`,`application`,`profile`,`label`,`createdBy`,`createdDate`,`modifiedBy`,`modifiedDate`) VALUES ('elastic-search.host',concat(@ES_HOST_NAME,''),'application','prd','latest',NULL,NULL,NULL,NULL);
INSERT IGNORE INTO pac_config_properties (`cfkey`,`value`,`application`,`profile`,`label`,`createdBy`,`createdDate`,`modifiedBy`,`modifiedDate`) VALUES ('elastic-search.port',concat(@ES_PORT,''),'application','prd','latest',NULL,NULL,NULL,NULL);
INSERT IGNORE INTO pac_config_properties (`cfkey`,`value`,`application`,`profile`,`label`,`createdBy`,`createdDate`,`modifiedBy`,`modifiedDate`) VALUES ('elastic-search.clusterName',concat(@ES_CLUSTER_NAME,''),'application','prd','latest',NULL,NULL,NULL,NULL);
INSERT IGNORE INTO pac_config_properties (`cfkey`,`value`,`application`,`profile`,`label`,`createdBy`,`createdDate`,`modifiedBy`,`modifiedDate`) VALUES ('elastic-search.port-admin',concat(@ES_PORT_ADMIN,''),'application','prd','latest',NULL,NULL,NULL,NULL);
INSERT IGNORE INTO pac_config_properties (`cfkey`,`value`,`application`,`profile`,`label`,`createdBy`,`createdDate`,`modifiedBy`,`modifiedDate`) VALUES ('elastic-search.host-heimdall',concat(@ES_HEIMDALL_HOST_NAME,''),'application','prd','latest',NULL,NULL,NULL,NULL);
INSERT IGNORE INTO pac_config_properties (`cfkey`,`value`,`application`,`profile`,`label`,`createdBy`,`createdDate`,`modifiedBy`,`modifiedDate`) VALUES ('elastic-search.port-heimdall',concat(@ES_HEIMDALL_PORT,''),'application','prd','latest',NULL,NULL,NULL,NULL);
INSERT IGNORE INTO pac_config_properties (`cfkey`,`value`,`application`,`profile`,`label`,`createdBy`,`createdDate`,`modifiedBy`,`modifiedDate`) VALUES ('elastic-search.clusterName-heimdall',concat(@ES_HEIMDALL_CLUSTER_NAME,''),'application','prd','latest',NULL,NULL,NULL,NULL);
INSERT IGNORE INTO pac_config_properties (`cfkey`,`value`,`application`,`profile`,`label`,`createdBy`,`createdDate`,`modifiedBy`,`modifiedDate`) VALUES ('elastic-search.port-admin-heimdall',concat(@ES_HEIMDALL_PORT_ADMIN,''),'application','prd','latest',NULL,NULL,NULL,NULL);
INSERT IGNORE INTO pac_config_properties (`cfkey`,`value`,`application`,`profile`,`label`,`createdBy`,`createdDate`,`modifiedBy`,`modifiedDate`) VALUES ('elastic-search.update-host',concat(@ES_UPDATE_HOST,''),'application','prd','latest',NULL,NULL,NULL,NULL);
INSERT IGNORE INTO pac_config_properties (`cfkey`,`value`,`application`,`profile`,`label`,`createdBy`,`createdDate`,`modifiedBy`,`modifiedDate`) VALUES ('elastic-search.update-port',concat(@ES_UPDATE_PORT,''),'application','prd','latest',NULL,NULL,NULL,NULL);
INSERT IGNORE INTO pac_config_properties (`cfkey`,`value`,`application`,`profile`,`label`,`createdBy`,`createdDate`,`modifiedBy`,`modifiedDate`) VALUES ('elastic-search.update-clusterName',concat(@ES_UPDATE_CLUSTER_NAME,''),'application','prd','latest',NULL,NULL,NULL,NULL);
INSERT IGNORE INTO pac_config_properties (`cfkey`,`value`,`application`,`profile`,`label`,`createdBy`,`createdDate`,`modifiedBy`,`modifiedDate`) VALUES ('formats.date','yyyy-MM-dd\'T\'HH:mm:ss.SSSZ','application','prd','latest',NULL,NULL,NULL,NULL);
INSERT IGNORE INTO pac_config_properties (`cfkey`,`value`,`application`,`profile`,`label`,`createdBy`,`createdDate`,`modifiedBy`,`modifiedDate`) VALUES ('ldap.naming.context-factory','com.sun.jndi.ldap.LdapCtxFactory','application','prd','latest',NULL,NULL,NULL,NULL);
INSERT IGNORE INTO pac_config_properties (`cfkey`,`value`,`application`,`profile`,`label`,`createdBy`,`createdDate`,`modifiedBy`,`modifiedDate`) VALUES ('ldap.naming.authentication','simple','application','prd','latest',NULL,NULL,NULL,NULL);
INSERT IGNORE INTO pac_config_properties(`cfkey`,`value`,`application`,`profile`,`label`,`createdBy`,`createdDate`,`modifiedBy`,`modifiedDate`) VALUES ('service.dns.name',concat(@PACMAN_HOST_NAME,''),'api','prd','latest',NULL,NULL,NULL,NULL);
INSERT IGNORE INTO pac_config_properties(`cfkey`,`value`,`application`,`profile`,`label`,`createdBy`,`createdDate`,`modifiedBy`,`modifiedDate`) VALUES ('service.url.compliance',concat(@PACMAN_HOST_NAME,'/api/compliance'),'api','prd','latest',NULL,NULL,NULL,NULL);
INSERT IGNORE INTO pac_config_properties(`cfkey`,`value`,`application`,`profile`,`label`,`createdBy`,`createdDate`,`modifiedBy`,`modifiedDate`) VALUES ('service.url.asset',concat(@PACMAN_HOST_NAME,'/api/asset'),'api','prd','latest',NULL,NULL,NULL,NULL);
INSERT IGNORE INTO pac_config_properties(`cfkey`,`value`,`application`,`profile`,`label`,`createdBy`,`createdDate`,`modifiedBy`,`modifiedDate`) VALUES ('service.url.statistics',concat(@PACMAN_HOST_NAME,'/api/statistics'),'api','prd','latest',NULL,NULL,NULL,NULL);
INSERT IGNORE INTO pac_config_properties(`cfkey`,`value`,`application`,`profile`,`label`,`createdBy`,`createdDate`,`modifiedBy`,`modifiedDate`) VALUES ('service.url.devstandards',concat(@PACMAN_HOST_NAME,'/api/devstandards'),'api','prd','latest',NULL,NULL,NULL,NULL);
INSERT IGNORE INTO pac_config_properties(`cfkey`,`value`,`application`,`profile`,`label`,`createdBy`,`createdDate`,`modifiedBy`,`modifiedDate`) VALUES ('service.url.auth',concat(@PACMAN_HOST_NAME,'/api/auth'),'api','prd','latest',NULL,NULL,NULL,NULL);
INSERT IGNORE INTO pac_config_properties(`cfkey`,`value`,`application`,`profile`,`label`,`createdBy`,`createdDate`,`modifiedBy`,`modifiedDate`) VALUES ('service.url.admin',concat(@PACMAN_HOST_NAME,'/api/admin'),'api','prd','latest',NULL,NULL,NULL,NULL);
INSERT IGNORE INTO pac_config_properties(`cfkey`,`value`,`application`,`profile`,`label`,`createdBy`,`createdDate`,`modifiedBy`,`modifiedDate`) VALUES ('endpoints.refresh.sensitive','false','api','prd','latest',NULL,NULL,NULL,NULL);
INSERT IGNORE INTO pac_config_properties(`cfkey`,`value`,`application`,`profile`,`label`,`createdBy`,`createdDate`,`modifiedBy`,`modifiedDate`) VALUES ('hystrix.command.default.execution.isolation.thread.timeoutInMilliseconds','100000','api','prd','latest',NULL,NULL,NULL,NULL);
INSERT IGNORE INTO pac_config_properties(`cfkey`,`value`,`application`,`profile`,`label`,`createdBy`,`createdDate`,`modifiedBy`,`modifiedDate`) VALUES ('application.cors.allowed.domains','all','api','prd','latest',NULL,NULL,NULL,NULL);
INSERT IGNORE INTO pac_config_properties(`cfkey`,`value`,`application`,`profile`,`label`,`createdBy`,`createdDate`,`modifiedBy`,`modifiedDate`) VALUES ('monitoring.contextRootNames','asset,compliance,statistics,devstandards,auth,admin','api','prd','latest',NULL,NULL,NULL,NULL);
DELETE IGNORE FROM  pac_config_properties  where cfkey="auth.active";
INSERT IGNORE INTO pac_config_properties(`cfkey`,`value`,`application`,`profile`,`label`,`createdBy`,`createdDate`,`modifiedBy`,`modifiedDate`) VALUES ('auth.active','cognito','api','prd','latest',NULL,NULL,NULL,NULL);
INSERT IGNORE INTO pac_config_properties(`cfkey`,`value`,`application`,`profile`,`label`,`createdBy`,`createdDate`,`modifiedBy`,`modifiedDate`) VALUES ('spring.cache.cache-names','trends,compliance,assets,trendsvuln','api','prd','latest',NULL,NULL,NULL,NULL);
INSERT IGNORE INTO pac_config_properties(`cfkey`,`value`,`application`,`profile`,`label`,`createdBy`,`createdDate`,`modifiedBy`,`modifiedDate`) VALUES ('spring.cache.caffeine.spec','maximumSize=500, expireAfterWrite=6h','api','prd','latest',NULL,NULL,NULL,NULL);
DELETE IGNORE FROM pac_config_properties where cfKey in ('spring.datasource.url','spring.datasource.username','spring.datasource.password');
INSERT IGNORE INTO pac_config_properties(`cfkey`,`value`,`application`,`profile`,`label`,`createdBy`,`createdDate`,`modifiedBy`,`modifiedDate`) VALUES ('spring.datasource.url',concat(@RDS_URL,''),'api','prd','latest',NULL,NULL,NULL,NULL);
INSERT IGNORE INTO pac_config_properties(`cfkey`,`value`,`application`,`profile`,`label`,`createdBy`,`createdDate`,`modifiedBy`,`modifiedDate`) VALUES ('spring.datasource.username',concat(@RDS_USERNAME,''),'api','prd','latest',NULL,NULL,NULL,NULL);
INSERT IGNORE INTO pac_config_properties(`cfkey`,`value`,`application`,`profile`,`label`,`createdBy`,`createdDate`,`modifiedBy`,`modifiedDate`) VALUES ('spring.datasource.password',concat(@RDS_PASSWORD,''),'api','prd','latest',NULL,NULL,NULL,NULL);
INSERT IGNORE INTO pac_config_properties(`cfkey`,`value`,`application`,`profile`,`label`,`createdBy`,`createdDate`,`modifiedBy`,`modifiedDate`) VALUES ('spring.datasource.driver-class-name','com.mysql.jdbc.Driver','api','prd','latest',NULL,NULL,NULL,NULL);
INSERT IGNORE INTO pac_config_properties(`cfkey`,`value`,`application`,`profile`,`label`,`createdBy`,`createdDate`,`modifiedBy`,`modifiedDate`) VALUES ('spring.cloud.bus.enabled','false','api','prd','latest',NULL,NULL,NULL,NULL);
INSERT IGNORE INTO pac_config_properties(`cfkey`,`value`,`application`,`profile`,`label`,`createdBy`,`createdDate`,`modifiedBy`,`modifiedDate`) VALUES ('api.services[0].name','Admin Service','api','prd','latest',NULL,NULL,NULL,NULL);
INSERT IGNORE INTO pac_config_properties(`cfkey`,`value`,`application`,`profile`,`label`,`createdBy`,`createdDate`,`modifiedBy`,`modifiedDate`) VALUES ('api.services[0].url','${PACMAN_HOST_NAME:http://localhost:8080}/api/admin/v2/api-docs','api','prd','latest',NULL,NULL,NULL,NULL);
INSERT IGNORE INTO pac_config_properties(`cfkey`,`value`,`application`,`profile`,`label`,`createdBy`,`createdDate`,`modifiedBy`,`modifiedDate`) VALUES ('api.services[0].version','2','api','prd','latest',NULL,NULL,NULL,NULL);
INSERT IGNORE INTO pac_config_properties(`cfkey`,`value`,`application`,`profile`,`label`,`createdBy`,`createdDate`,`modifiedBy`,`modifiedDate`) VALUES ('api.services[1].name','Auth Service','api','prd','latest',NULL,NULL,NULL,NULL);
INSERT IGNORE INTO pac_config_properties(`cfkey`,`value`,`application`,`profile`,`label`,`createdBy`,`createdDate`,`modifiedBy`,`modifiedDate`) VALUES ('api.services[1].url','${PACMAN_HOST_NAME:http://localhost:8080}/api/auth/v2/api-docs','api','prd','latest',NULL,NULL,NULL,NULL);
INSERT IGNORE INTO pac_config_properties(`cfkey`,`value`,`application`,`profile`,`label`,`createdBy`,`createdDate`,`modifiedBy`,`modifiedDate`) VALUES ('api.services[1].version','2','api','prd','latest',NULL,NULL,NULL,NULL);
INSERT IGNORE INTO pac_config_properties(`cfkey`,`value`,`application`,`profile`,`label`,`createdBy`,`createdDate`,`modifiedBy`,`modifiedDate`) VALUES ('api.services[2].name','Asset Service','api','prd','latest',NULL,NULL,NULL,NULL);
INSERT IGNORE INTO pac_config_properties(`cfkey`,`value`,`application`,`profile`,`label`,`createdBy`,`createdDate`,`modifiedBy`,`modifiedDate`) VALUES ('api.services[2].url','${PACMAN_HOST_NAME:http://localhost:8080}/api/asset/v2/api-docs','api','prd','latest',NULL,NULL,NULL,NULL);
INSERT IGNORE INTO pac_config_properties(`cfkey`,`value`,`application`,`profile`,`label`,`createdBy`,`createdDate`,`modifiedBy`,`modifiedDate`) VALUES ('api.services[2].version','2','api','prd','latest',NULL,NULL,NULL,NULL);
INSERT IGNORE INTO pac_config_properties(`cfkey`,`value`,`application`,`profile`,`label`,`createdBy`,`createdDate`,`modifiedBy`,`modifiedDate`) VALUES ('api.services[3].name','Notification Service','api','prd','latest',NULL,NULL,NULL,NULL);
INSERT IGNORE INTO pac_config_properties(`cfkey`,`value`,`application`,`profile`,`label`,`createdBy`,`createdDate`,`modifiedBy`,`modifiedDate`) VALUES ('api.services[3].url','${PACMAN_HOST_NAME:http://localhost:8080}/api/notifications/v2/api-docs','api','prd','latest',NULL,NULL,NULL,NULL);
INSERT IGNORE INTO pac_config_properties(`cfkey`,`value`,`application`,`profile`,`label`,`createdBy`,`createdDate`,`modifiedBy`,`modifiedDate`) VALUES ('api.services[3].version','2','api','prd','latest',NULL,NULL,NULL,NULL);
INSERT IGNORE INTO pac_config_properties(`cfkey`,`value`,`application`,`profile`,`label`,`createdBy`,`createdDate`,`modifiedBy`,`modifiedDate`) VALUES ('api.services[4].name','Compliance Service','api','prd','latest',NULL,NULL,NULL,NULL);
INSERT IGNORE INTO pac_config_properties(`cfkey`,`value`,`application`,`profile`,`label`,`createdBy`,`createdDate`,`modifiedBy`,`modifiedDate`) VALUES ('api.services[4].url','${PACMAN_HOST_NAME:http://localhost:8080}/api/compliance/v2/api-docs','api','prd','latest',NULL,NULL,NULL,NULL);
INSERT IGNORE INTO pac_config_properties(`cfkey`,`value`,`application`,`profile`,`label`,`createdBy`,`createdDate`,`modifiedBy`,`modifiedDate`) VALUES ('api.services[4].version','2','api','prd','latest',NULL,NULL,NULL,NULL);
INSERT IGNORE INTO pac_config_properties(`cfkey`,`value`,`application`,`profile`,`label`,`createdBy`,`createdDate`,`modifiedBy`,`modifiedDate`) VALUES ('api.services[5].name','Statistics Service','api','prd','latest',NULL,NULL,NULL,NULL);
INSERT IGNORE INTO pac_config_properties(`cfkey`,`value`,`application`,`profile`,`label`,`createdBy`,`createdDate`,`modifiedBy`,`modifiedDate`) VALUES ('api.services[5].url','${PACMAN_HOST_NAME:http://localhost:8080}/api/statistics/v2/api-docs','api','prd','latest',NULL,NULL,NULL,NULL);
INSERT IGNORE INTO pac_config_properties(`cfkey`,`value`,`application`,`profile`,`label`,`createdBy`,`createdDate`,`modifiedBy`,`modifiedDate`) VALUES ('api.services[5].version','2','api','prd','latest',NULL,NULL,NULL,NULL);
INSERT IGNORE INTO pac_config_properties(`cfkey`,`value`,`application`,`profile`,`label`,`createdBy`,`createdDate`,`modifiedBy`,`modifiedDate`) VALUES ('tagging.mandatoryTags',concat(@MANDATORY_TAGS,''),'application','prd','latest',NULL,NULL,NULL,NULL);
INSERT IGNORE INTO pac_config_properties(`cfkey`,`value`,`application`,`profile`,`label`,`createdBy`,`createdDate`,`modifiedBy`,`modifiedDate`) VALUES ('vulnerability.types','ec2,onpremserver','api','prd','latest',NULL,NULL,NULL,NULL);
INSERT IGNORE INTO pac_config_properties(`cfkey`,`value`,`application`,`profile`,`label`,`createdBy`,`createdDate`,`modifiedBy`,`modifiedDate`) VALUES ('vulnerability.summary.severity','5','api','prd','latest',NULL,NULL,NULL,NULL);
INSERT IGNORE INTO pac_config_properties(`cfkey`,`value`,`application`,`profile`,`label`,`createdBy`,`createdDate`,`modifiedBy`,`modifiedDate`) VALUES ('swagger.auth.whitelist','/configuration/security,/swagger-ui.html,/api.html,/webjars/**,/user,/public/**,/api.html,/css/styles.js,/js/swagger.js,/js/swagger-ui.js,/js/swagger-oauth.js,/images/pacman_logo.svg,/images/favicon-32x32.png,/images/favicon-16x16.png,/images/favicon.ico,/docs/v1/api.html,/v2/api-docs/**,/v2/swagger.json,/webjars/springfox-swagger-ui/css/**,/webjars/springfox-swagger-ui/js/**,/configuration/ui,/swagger-resources/**,/configuration/**,/imgs/**,/css/**,/css/font/**,/proxy*/**,/hystrix/monitor/**,/hystrix/**/images/pacman_logo.svg,/images/favicon-32x32.png,/images/favicon-16x16.png,/images/favicon.ico,/docs/v1/api.html,/v2/api-docs/**,/v2/swagger.json,/webjars/springfox-swagger-ui/css/**,/webjars/springfox-swagger-ui/js/**,/configuration/ui,/swagger-resources/**,/configuration/**,/imgs/**,/css/**,/css/font/**,/proxy*/**,/hystrix/monitor/**,/hystrix/**,/refresh','api','prd','latest',NULL,NULL,NULL,NULL);
INSERT IGNORE INTO pac_config_properties (`cfkey`,`value`,`application`,`profile`,`label`,`createdBy`,`createdDate`,`modifiedBy`,`modifiedDate`) VALUES ('server.servlet.context-path','/api/admin','admin-service','prd','latest',NULL,NULL,NULL,NULL);
INSERT IGNORE INTO pac_config_properties (`cfkey`,`value`,`application`,`profile`,`label`,`createdBy`,`createdDate`,`modifiedBy`,`modifiedDate`) VALUES ('aws.access-key','','admin-service','prd','latest',NULL,NULL,NULL,NULL);
INSERT IGNORE INTO pac_config_properties (`cfkey`,`value`,`application`,`profile`,`label`,`createdBy`,`createdDate`,`modifiedBy`,`modifiedDate`) VALUES ('aws.secret-key','','admin-service','prd','latest',NULL,NULL,NULL,NULL);
INSERT IGNORE INTO pac_config_properties (`cfkey`,`value`,`application`,`profile`,`label`,`createdBy`,`createdDate`,`modifiedBy`,`modifiedDate`) VALUES ('admin.api-role','ROLE_ADMIN2, ROLE_ADMIN','admin-service','prd','latest',NULL,NULL,NULL,NULL);
INSERT IGNORE INTO pac_config_properties (`cfkey`,`value`,`application`,`profile`,`label`,`createdBy`,`createdDate`,`modifiedBy`,`modifiedDate`) VALUES ('job.s3.bucket-region',concat(@JOB_BUCKET_REGION,''),'admin-service','prd','latest',NULL,NULL,NULL,NULL);
INSERT IGNORE INTO pac_config_properties (`cfkey`,`value`,`application`,`profile`,`label`,`createdBy`,`createdDate`,`modifiedBy`,`modifiedDate`) VALUES ('job.s3.bucket-name',concat(@RULE_JOB_BUCKET_NAME,''),'admin-service','prd','latest',NULL,NULL,NULL,NULL);
INSERT IGNORE INTO pac_config_properties (`cfkey`,`value`,`application`,`profile`,`label`,`createdBy`,`createdDate`,`modifiedBy`,`modifiedDate`) VALUES ('job.lambda.region',concat(@JOB_LAMBDA_REGION,''),'admin-service','prd','latest',NULL,NULL,NULL,NULL);
INSERT IGNORE INTO pac_config_properties (`cfkey`,`value`,`application`,`profile`,`label`,`createdBy`,`createdDate`,`modifiedBy`,`modifiedDate`) VALUES ('job.lambda.target-id','jobTargetId','admin-service','prd','latest',NULL,NULL,NULL,NULL);
INSERT IGNORE INTO pac_config_properties (`cfkey`,`value`,`application`,`profile`,`label`,`createdBy`,`createdDate`,`modifiedBy`,`modifiedDate`) VALUES ('job.lambda.function-name',concat(@JOB_FUNCTION_NAME,''),'admin-service','prd','latest',NULL,NULL,NULL,NULL);
INSERT IGNORE INTO pac_config_properties (`cfkey`,`value`,`application`,`profile`,`label`,`createdBy`,`createdDate`,`modifiedBy`,`modifiedDate`) VALUES ('job.lambda.function-arn',concat(@JOB_FUNCTION_ARN,''),'admin-service','prd','latest',NULL,NULL,NULL,NULL);
INSERT IGNORE INTO pac_config_properties (`cfkey`,`value`,`application`,`profile`,`label`,`createdBy`,`createdDate`,`modifiedBy`,`modifiedDate`) VALUES ('job.lambda.principal','events.amazonaws.com','admin-service','prd','latest',NULL,NULL,NULL,NULL);
INSERT IGNORE INTO pac_config_properties (`cfkey`,`value`,`application`,`profile`,`label`,`createdBy`,`createdDate`,`modifiedBy`,`modifiedDate`) VALUES ('job.lambda.action-enabled','lambda:InvokeFunction','admin-service','prd','latest',NULL,NULL,NULL,NULL);
INSERT IGNORE INTO pac_config_properties (`cfkey`,`value`,`application`,`profile`,`label`,`createdBy`,`createdDate`,`modifiedBy`,`modifiedDate`) VALUES ('job.lambda.action-disabled','lambda:DisableInvokeFunction','admin-service','prd','latest',NULL,NULL,NULL,NULL);
INSERT IGNORE INTO pac_config_properties (`cfkey`,`value`,`application`,`profile`,`label`,`createdBy`,`createdDate`,`modifiedBy`,`modifiedDate`) VALUES ('rule.s3.bucket-region',concat(@RULE_BUCKET_REGION,''),'admin-service','prd','latest',NULL,NULL,NULL,NULL);
INSERT IGNORE INTO pac_config_properties (`cfkey`,`value`,`application`,`profile`,`label`,`createdBy`,`createdDate`,`modifiedBy`,`modifiedDate`) VALUES ('rule.s3.bucket-name',concat(@RULE_JOB_BUCKET_NAME,''),'admin-service','prd','latest',NULL,NULL,NULL,NULL);
INSERT IGNORE INTO pac_config_properties (`cfkey`,`value`,`application`,`profile`,`label`,`createdBy`,`createdDate`,`modifiedBy`,`modifiedDate`) VALUES ('rule.lambda.region',concat(@RULE_LAMBDA_REGION,''),'admin-service','prd','latest',NULL,NULL,NULL,NULL);
INSERT IGNORE INTO pac_config_properties (`cfkey`,`value`,`application`,`profile`,`label`,`createdBy`,`createdDate`,`modifiedBy`,`modifiedDate`) VALUES ('rule.lambda.target-id','ruleTargetId','admin-service','prd','latest',NULL,NULL,NULL,NULL);
INSERT IGNORE INTO pac_config_properties (`cfkey`,`value`,`application`,`profile`,`label`,`createdBy`,`createdDate`,`modifiedBy`,`modifiedDate`) VALUES ('rule.lambda.function-name',concat(@RULE_FUNCTION_NAME,''),'admin-service','prd','latest',NULL,NULL,NULL,NULL);
INSERT IGNORE INTO pac_config_properties (`cfkey`,`value`,`application`,`profile`,`label`,`createdBy`,`createdDate`,`modifiedBy`,`modifiedDate`) VALUES ('rule.lambda.function-arn',concat(@RULE_FUNCTION_ARN,''),'admin-service','prd','latest',NULL,NULL,NULL,NULL);
INSERT IGNORE INTO pac_config_properties (`cfkey`,`value`,`application`,`profile`,`label`,`createdBy`,`createdDate`,`modifiedBy`,`modifiedDate`) VALUES ('rule.lambda.principal','events.amazonaws.com','admin-service','prd','latest',NULL,NULL,NULL,NULL);
INSERT IGNORE INTO pac_config_properties (`cfkey`,`value`,`application`,`profile`,`label`,`createdBy`,`createdDate`,`modifiedBy`,`modifiedDate`) VALUES ('rule.lambda.action-enabled','lambda:InvokeFunction','admin-service','prd','latest',NULL,NULL,NULL,NULL);
INSERT IGNORE INTO pac_config_properties (`cfkey`,`value`,`application`,`profile`,`label`,`createdBy`,`createdDate`,`modifiedBy`,`modifiedDate`) VALUES ('rule.lambda.action-disabled','lambda:DisableInvokeFunction','admin-service','prd','latest',NULL,NULL,NULL,NULL);
INSERT IGNORE INTO pac_config_properties (`cfkey`,`value`,`application`,`profile`,`label`,`createdBy`,`createdDate`,`modifiedBy`,`modifiedDate`) VALUES ('management.security.enabled','false','admin-service','prd','latest',NULL,NULL,NULL,NULL);
INSERT IGNORE INTO pac_config_properties (`cfkey`,`value`,`application`,`profile`,`label`,`createdBy`,`createdDate`,`modifiedBy`,`modifiedDate`) VALUES ('security.basic.enabled','false','admin-service','prd','latest',NULL,NULL,NULL,NULL);
INSERT IGNORE INTO pac_config_properties (`cfkey`,`value`,`application`,`profile`,`label`,`createdBy`,`createdDate`,`modifiedBy`,`modifiedDate`) VALUES ('security.oauth2.client.user-authorization-uri',concat(@PACMAN_HOST_NAME,'/api/auth/oauth/authorize'),'admin-service','prd','latest',NULL,NULL,NULL,NULL);
INSERT IGNORE INTO pac_config_properties (`cfkey`,`value`,`application`,`profile`,`label`,`createdBy`,`createdDate`,`modifiedBy`,`modifiedDate`) VALUES ('elastic-search.dev-ingest-host',concat(@ES_UPDATE_HOST,''),'admin-service','prd','latest',NULL,NULL,NULL,NULL);
INSERT IGNORE INTO pac_config_properties (`cfkey`,`value`,`application`,`profile`,`label`,`createdBy`,`createdDate`,`modifiedBy`,`modifiedDate`) VALUES ('elastic-search.dev-ingest-port',concat(@ES_UPDATE_PORT,''),'admin-service','prd','latest',NULL,NULL,NULL,NULL);
INSERT IGNORE INTO pac_config_properties (`cfkey`,`value`,`application`,`profile`,`label`,`createdBy`,`createdDate`,`modifiedBy`,`modifiedDate`) VALUES ('elastic-search.port',concat(@ES_UPDATE_PORT,''),'admin-service','prd','latest',NULL,NULL,NULL,NULL);
INSERT IGNORE INTO pac_config_properties (`cfkey`,`value`,`application`,`profile`,`label`,`createdBy`,`createdDate`,`modifiedBy`,`modifiedDate`) VALUES ('elastic-search.host',concat(@ES_UPDATE_HOST,''),'admin-service','prd','latest',NULL,NULL,NULL,NULL);
INSERT IGNORE INTO pac_config_properties (`cfkey`,`value`,`application`,`profile`,`label`,`createdBy`,`createdDate`,`modifiedBy`,`modifiedDate`) VALUES ('target-types.categories','Compute,Developer Tools,Analytics,Application Services,Storage,Management Tools,Messaging,Artificial Intelligence,Database,Business Productivity,Security,Identity & Compliance,Networking & Content Delivery,Contact Center,Internet Of Things,Desktop & App Streaming,Desktop & App Streaming,Migration,Mobile Services,Game Development,Contact Center,Application Integration','admin-service','prd','latest',NULL,NULL,NULL,NULL);
INSERT IGNORE INTO pac_config_properties (`cfkey`,`value`,`application`,`profile`,`label`,`createdBy`,`createdDate`,`modifiedBy`,`modifiedDate`) VALUES ('pacman.api.oauth2.client-id','22e14922-87d7-4ee4-a470-da0bb10d45d3','admin-service','prd','latest',NULL,NULL,NULL,NULL);
INSERT IGNORE INTO pac_config_properties (`cfkey`,`value`,`application`,`profile`,`label`,`createdBy`,`createdDate`,`modifiedBy`,`modifiedDate`) VALUES ('server.servlet.context-path','/api/asset','asset-service','prd','latest',NULL,NULL,NULL,NULL);
INSERT IGNORE INTO pac_config_properties (`cfkey`,`value`,`application`,`profile`,`label`,`createdBy`,`createdDate`,`modifiedBy`,`modifiedDate`) VALUES ('cloudinsights.tokenurl',concat(@CLOUD_INSIGHTS_TOKEN_URL,''),'asset-service','prd','latest',NULL,NULL,NULL,NULL);
INSERT IGNORE INTO pac_config_properties (`cfkey`,`value`,`application`,`profile`,`label`,`createdBy`,`createdDate`,`modifiedBy`,`modifiedDate`) VALUES ('cloudinsights.costurl',concat(@CLOUD_INSIGHTS_COST_URL,''),'asset-service','prd','latest',NULL,NULL,NULL,NULL);
INSERT IGNORE INTO pac_config_properties (`cfkey`,`value`,`application`,`profile`,`label`,`createdBy`,`createdDate`,`modifiedBy`,`modifiedDate`) VALUES ('cloudinsights.corp-user-id',concat(@SVC_CORP_USER_ID,''),'asset-service','prd','latest',NULL,NULL,NULL,NULL);
INSERT IGNORE INTO pac_config_properties (`cfkey`,`value`,`application`,`profile`,`label`,`createdBy`,`createdDate`,`modifiedBy`,`modifiedDate`) VALUES ('cloudinsights.corp-password',concat(@SVC_CORP_PASSWORD,''),'asset-service','prd','latest',NULL,NULL,NULL,NULL);
INSERT IGNORE INTO pac_config_properties (`cfkey`,`value`,`application`,`profile`,`label`,`createdBy`,`createdDate`,`modifiedBy`,`modifiedDate`) VALUES ('server.servlet.context-path','/api/auth','auth-service','prd','latest',NULL,NULL,NULL,NULL);
INSERT IGNORE INTO pac_config_properties (`cfkey`,`value`,`application`,`profile`,`label`,`createdBy`,`createdDate`,`modifiedBy`,`modifiedDate`) VALUES ('pacman.api.oauth2.client-id','22e14922-87d7-4ee4-a470-da0bb10d45d3','auth-service','prd','latest',NULL,NULL,NULL,NULL);
INSERT IGNORE INTO pac_config_properties (`cfkey`,`value`,`application`,`profile`,`label`,`createdBy`,`createdDate`,`modifiedBy`,`modifiedDate`) VALUES ('pacman.api.oauth2.client-secret','csrWpc5p7JFF4vEZBkwGCAh67kGQGwXv46qug7v5ZwtKg','auth-service','prd','latest',NULL,NULL,NULL,NULL);
INSERT IGNORE INTO pac_config_properties(`cfkey`,`value`,`application`,`profile`,`label`,`createdBy`,`createdDate`,`modifiedBy`,`modifiedDate`) VALUES ('server.servlet.context-path','/api/compliance','compliance-service','prd','latest',NULL,NULL,NULL,NULL);
INSERT IGNORE INTO pac_config_properties(`cfkey`,`value`,`application`,`profile`,`label`,`createdBy`,`createdDate`,`modifiedBy`,`modifiedDate`) VALUES ('rule-engine.invoke.url','submitRuleExecutionJob','compliance-service','prd','latest',NULL,NULL,NULL,NULL);
INSERT IGNORE INTO pac_config_properties(`cfkey`,`value`,`application`,`profile`,`label`,`createdBy`,`createdDate`,`modifiedBy`,`modifiedDate`) VALUES ('projections.assetgroups','cloud-vm,onprem-vm,all-vm','compliance-service','prd','latest',NULL,NULL,NULL,NULL);
INSERT IGNORE INTO pac_config_properties(`cfkey`,`value`,`application`,`profile`,`label`,`createdBy`,`createdDate`,`modifiedBy`,`modifiedDate`) VALUES ('projections.targetTypes','onpremserver,ec2','compliance-service','prd','latest',NULL,NULL,NULL,NULL);
INSERT IGNORE INTO pac_config_properties(`cfkey`,`value`,`application`,`profile`,`label`,`createdBy`,`createdDate`,`modifiedBy`,`modifiedDate`) VALUES ('features.certificate.enabled',concat(@CERTIFICATE_FEATURE_ENABLED,''),'compliance-service','prd','latest',NULL,NULL,NULL,NULL);
INSERT IGNORE INTO pac_config_properties(`cfkey`,`value`,`application`,`profile`,`label`,`createdBy`,`createdDate`,`modifiedBy`,`modifiedDate`) VALUES ('features.patching.enabled',concat(@PATCHING_FEATURE_ENABLED,''),'compliance-service','prd','latest',NULL,NULL,NULL,NULL);
INSERT IGNORE INTO pac_config_properties(`cfkey`,`value`,`application`,`profile`,`label`,`createdBy`,`createdDate`,`modifiedBy`,`modifiedDate`) VALUES ('features.vulnerability.enabled',concat(@VULNERABILITY_FEATURE_ENABLED,''),'compliance-service','prd','latest',NULL,NULL,NULL,NULL);
INSERT IGNORE INTO pac_config_properties (`cfkey`,`value`,`application`,`profile`,`label`,`createdBy`,`createdDate`,`modifiedBy`,`modifiedDate`) VALUES ('server.servlet.context-path','/api/notifications','notification-service','prd','latest',NULL,NULL,NULL,NULL);
INSERT IGNORE INTO pac_config_properties (`cfkey`,`value`,`application`,`profile`,`label`,`createdBy`,`createdDate`,`modifiedBy`,`modifiedDate`) VALUES ('remind.cron','0 0 0 * * *','notification-service','prd','latest',NULL,NULL,NULL,NULL);
INSERT IGNORE INTO pac_config_properties (`cfkey`,`value`,`application`,`profile`,`label`,`createdBy`,`createdDate`,`modifiedBy`,`modifiedDate`) VALUES ('remind.email.text','Hey, {0}! We\'ve missed you here on Pacman. It\'s time to check your compliance, Pacman team','notification-service','prd','latest',NULL,NULL,NULL,NULL);
INSERT IGNORE INTO pac_config_properties (`cfkey`,`value`,`application`,`profile`,`label`,`createdBy`,`createdDate`,`modifiedBy`,`modifiedDate`) VALUES ('remind.email.subject','Pacman reminder','notification-service','prd','latest',NULL,NULL,NULL,NULL);
INSERT IGNORE INTO pac_config_properties (`cfkey`,`value`,`application`,`profile`,`label`,`createdBy`,`createdDate`,`modifiedBy`,`modifiedDate`) VALUES ('backup.cron','0 0 12 * * *','notification-service','prd','latest',NULL,NULL,NULL,NULL);
INSERT IGNORE INTO pac_config_properties (`cfkey`,`value`,`application`,`profile`,`label`,`createdBy`,`createdDate`,`modifiedBy`,`modifiedDate`) VALUES ('backup.email.text','Howdy, {0}. Your account backup is ready.\r\n\r\nCheers,\r\nPiggyMetrics team','notification-service','prd','latest',NULL,NULL,NULL,NULL);
INSERT IGNORE INTO pac_config_properties (`cfkey`,`value`,`application`,`profile`,`label`,`createdBy`,`createdDate`,`modifiedBy`,`modifiedDate`) VALUES ('backup.email.subject','PiggyMetrics account backup','notification-service','prd','latest',NULL,NULL,NULL,NULL);
INSERT IGNORE INTO pac_config_properties (`cfkey`,`value`,`application`,`profile`,`label`,`createdBy`,`createdDate`,`modifiedBy`,`modifiedDate`) VALUES ('backup.email.attachment','backup.json','notification-service','prd','latest',NULL,NULL,NULL,NULL);
INSERT IGNORE INTO pac_config_properties (`cfkey`,`value`,`application`,`profile`,`label`,`createdBy`,`createdDate`,`modifiedBy`,`modifiedDate`) VALUES ('spring.freemarker.suffix','.html','notification-service','prd','latest',NULL,NULL,NULL,NULL);
INSERT IGNORE INTO pac_config_properties (`cfkey`,`value`,`application`,`profile`,`label`,`createdBy`,`createdDate`,`modifiedBy`,`modifiedDate`) VALUES ('spring.mail.host',concat(@MAIL_SERVER,''),'notification-service','prd','latest',NULL,NULL,NULL,NULL);
INSERT IGNORE INTO pac_config_properties (`cfkey`,`value`,`application`,`profile`,`label`,`createdBy`,`createdDate`,`modifiedBy`,`modifiedDate`) VALUES ('spring.mail.port',concat(@MAIL_SERVER_PORT, ''),'notification-service','prd','latest',NULL,NULL,NULL,NULL);
INSERT IGNORE INTO pac_config_properties (`cfkey`,`value`,`application`,`profile`,`label`,`createdBy`,`createdDate`,`modifiedBy`,`modifiedDate`) VALUES ('spring.mail.protocol',concat(@MAIL_PROTOCOL,''),'notification-service','prd','latest',NULL,NULL,NULL,NULL);
INSERT IGNORE INTO pac_config_properties (`cfkey`,`value`,`application`,`profile`,`label`,`createdBy`,`createdDate`,`modifiedBy`,`modifiedDate`) VALUES ('spring.mail.defaultEncoding','UTF-8','notification-service','prd','latest',NULL,NULL,NULL,NULL);
INSERT IGNORE INTO pac_config_properties (`cfkey`,`value`,`application`,`profile`,`label`,`createdBy`,`createdDate`,`modifiedBy`,`modifiedDate`) VALUES ('cron.frequency.weekly-report-sync-trigger','0 0 9 ? * MON *','notification-service','prd','latest',NULL,NULL,NULL,NULL);
INSERT IGNORE INTO pac_config_properties (`cfkey`,`value`,`application`,`profile`,`label`,`createdBy`,`createdDate`,`modifiedBy`,`modifiedDate`) VALUES ('template.digest-mail.url',concat('https://s3.amazonaws.com/',@PACMAN_S3,'/index.html'),'notification-service','prd','latest',NULL,NULL,NULL,NULL);
INSERT IGNORE INTO pac_config_properties (`cfkey`,`value`,`application`,`profile`,`label`,`createdBy`,`createdDate`,`modifiedBy`,`modifiedDate`) VALUES ('server.servlet.context-path','/api/statistics','statistics-service','prd','latest',NULL,NULL,NULL,NULL);
INSERT IGNORE INTO pac_config_properties (cfkey,value,application,profile,label,createdBy,createdDate,modifiedBy,modifiedDate) VALUES ('region.ignore','us-gov-west-1,cn-north-1,cn-northwest-1','inventory','prd','latest',null,null,null,null);
INSERT IGNORE INTO pac_config_properties (cfkey,value,application,profile,label,createdBy,createdDate,modifiedBy,modifiedDate) VALUES ('file.path','/home/ec2-user/data','inventory','prd','latest',null,null,null,null);
INSERT IGNORE INTO pac_config_properties (cfkey,value,application,profile,label,createdBy,createdDate,modifiedBy,modifiedDate) VALUES ('spring.datasource.url',concat(@RDS_URL,''),'batch','prd','latest',null,null,null,null);
INSERT IGNORE INTO pac_config_properties (cfkey,value,application,profile,label,createdBy,createdDate,modifiedBy,modifiedDate) VALUES ('spring.datasource.username',concat(@RDS_USERNAME,''),'batch','prd','latest',null,null,null,null);
INSERT IGNORE INTO pac_config_properties (cfkey,value,application,profile,label,createdBy,createdDate,modifiedBy,modifiedDate) VALUES ('spring.datasource.password',concat(@RDS_PASSWORD,''),'batch','prd','latest',null,null,null,null);
INSERT IGNORE INTO pac_config_properties (cfkey,value,application,profile,label,createdBy,createdDate,modifiedBy,modifiedDate) VALUES ('s3.data',concat(@DATA_IN_DIR,''),'batch','prd','latest',null,null,null,null);
INSERT IGNORE INTO pac_config_properties (cfkey,value,application,profile,label,createdBy,createdDate,modifiedBy,modifiedDate) VALUES ('s3.processed',concat(@DATA_BKP_DIR,''),'batch','prd','latest',null,null,null,null);
INSERT IGNORE INTO pac_config_properties (cfkey,value,application,profile,label,createdBy,createdDate,modifiedBy,modifiedDate) VALUES ('s3.role',concat(@PAC_ROLE,''),'batch','prd','latest',null,null,null,null);
INSERT IGNORE INTO pac_config_properties (cfkey,value,application,profile,label,createdBy,createdDate,modifiedBy,modifiedDate) VALUES ('s3.region',concat(@BASE_REGION,''),'batch','prd','latest',null,null,null,null);
INSERT IGNORE INTO pac_config_properties (cfkey,value,application,profile,label,createdBy,createdDate,modifiedBy,modifiedDate) VALUES ('s3',concat(@DATA_IN_S3,''),'batch','prd','latest',null,null,null,null);
INSERT IGNORE INTO pac_config_properties (cfkey,value,application,profile,label,createdBy,createdDate,modifiedBy,modifiedDate) VALUES ('base.account',concat(@BASE_ACCOUNT,''),'batch','prd','latest',null,null,null,null);
INSERT IGNORE INTO pac_config_properties (cfkey,value,application,profile,label,createdBy,createdDate,modifiedBy,modifiedDate) VALUES ('base.region',concat(@BASE_REGION,''),'batch','prd','latest',null,null,null,null);
INSERT IGNORE INTO pac_config_properties (cfkey,value,application,profile,label,createdBy,createdDate,modifiedBy,modifiedDate) VALUES ('discovery.role',concat(@PAC_RO_ROLE,''),'inventory','prd','latest',null,null,null,null);
INSERT IGNORE INTO pac_config_properties (cfkey,value,application,profile,label,createdBy,createdDate,modifiedBy,modifiedDate) VALUES ('elastic-search.host',concat(@ES_HOST_NAME,''),'batch','prd','latest',null,null,null,null);
INSERT IGNORE INTO pac_config_properties (cfkey,value,application,profile,label,createdBy,createdDate,modifiedBy,modifiedDate) VALUES ('elastic-search.port',concat(@ES_PORT,''),'batch','prd','latest',null,null,null,null);
INSERT IGNORE INTO pac_config_properties (`cfkey`,`value`,`application`,`profile`,`label`,`createdBy`,`createdDate`,`modifiedBy`,`modifiedDate`) VALUES ('spring.mail.username',concat(@MAIL_SERVER_USER,''),'notification-service','prd','latest',NULL,NULL,NULL,NULL);
INSERT IGNORE INTO pac_config_properties (`cfkey`,`value`,`application`,`profile`,`label`,`createdBy`,`createdDate`,`modifiedBy`,`modifiedDate`) VALUES ('spring.mail.password',concat(@MAIL_SERVER_PWD,''),'notification-service','prd','latest',NULL,NULL,NULL,NULL);
INSERT IGNORE INTO pac_config_properties (`cfkey`,`value`,`application`,`profile`,`label`,`createdBy`,`createdDate`,`modifiedBy`,`modifiedDate`) VALUES ('spring.mail.properties.mail.smtp.auth',concat(@MAIL_SMTP_AUTH,''),'notification-service','prd','latest',NULL,NULL,NULL,NULL);
INSERT IGNORE INTO pac_config_properties (`cfkey`,`value`,`application`,`profile`,`label`,`createdBy`,`createdDate`,`modifiedBy`,`modifiedDate`) VALUES ('spring.mail.properties.mail.smtp.ssl.trust',concat(@MAIL_SERVER,''),'notification-service','prd','latest',NULL,NULL,NULL,NULL);
INSERT IGNORE INTO pac_config_properties (`cfkey`,`value`,`application`,`profile`,`label`,`createdBy`,`createdDate`,`modifiedBy`,`modifiedDate`) VALUES ('spring.mail.properties.mail.smtp.starttls.enable',concat(@MAIL_SMTP_SSL_ENABLE,''),'notification-service','prd','latest',NULL,NULL,NULL,NULL);
INSERT IGNORE INTO pac_config_properties (`cfkey`,`value`,`application`,`profile`,`label`,`createdBy`,`createdDate`,`modifiedBy`,`modifiedDate`) VALUES ('spring.mail.test-connection',concat(@MAIL_SMTP_SSL_TEST_CONNECTION,''),'notification-service','prd','latest',NULL,NULL,NULL,NULL);

DELETE IGNORE FROM pac_config_properties where cfKey in ('scheduler.interval','gcp.eventbridge.bus.details','aws.eventbridge.bus.details','scheduler.rules.initial.delay','scheduler.total.batches','scheduler.shipper.initial.delay','scheduler.collector.initial.delay');

INSERT IGNORE INTO pac_config_properties (`cfkey`,`value`,`application`,`profile`,`label`,`createdBy`,`createdDate`,`modifiedBy`,`modifiedDate`) VALUES ('scheduler.interval',concat(@JOB_SCHEDULE_INTERVAL,''),'job-scheduler','prd','latest',NULL,NULL,NULL,NULL);
INSERT IGNORE INTO pac_config_properties (`cfkey`,`value`,`application`,`profile`,`label`,`createdBy`,`createdDate`,`modifiedBy`,`modifiedDate`) VALUES ('scheduler.rules.initial.delay',concat(@JOB_SCHEDULE_INITIALDELAY_RULES,''),'job-scheduler','prd','latest',NULL,NULL,NULL,NULL);
INSERT IGNORE INTO pac_config_properties (`cfkey`,`value`,`application`,`profile`,`label`,`createdBy`,`createdDate`,`modifiedBy`,`modifiedDate`) VALUES ('scheduler.shipper.initial.delay',concat(@JOB_SCHEDULE_INITIALDELAY_SHIPPER,''),'job-scheduler','prd','latest',NULL,NULL,NULL,NULL);
INSERT IGNORE INTO pac_config_properties (`cfkey`,`value`,`application`,`profile`,`label`,`createdBy`,`createdDate`,`modifiedBy`,`modifiedDate`) VALUES ('scheduler.role',concat(@PAC_ROLE,''),'job-scheduler','prd','latest',NULL,NULL,NULL,NULL);
INSERT IGNORE INTO pac_config_properties (`cfkey`,`value`,`application`,`profile`,`label`,`createdBy`,`createdDate`,`modifiedBy`,`modifiedDate`) VALUES ('scheduler.collector.initial.delay',concat(@JOB_SCHEDULE_INITIALDELAY,''),'job-scheduler','prd','latest',NULL,NULL,NULL,NULL);
INSERT IGNORE INTO pac_config_properties (`cfkey`,`value`,`application`,`profile`,`label`,`createdBy`,`createdDate`,`modifiedBy`,`modifiedDate`) VALUES ('gcp.eventbridge.bus.details',concat(@GCP_EVENTBRIDGE_BUS_DETAILS,''),'job-scheduler','prd','latest',NULL,NULL,NULL,NULL);
INSERT IGNORE INTO pac_config_properties (`cfkey`,`value`,`application`,`profile`,`label`,`createdBy`,`createdDate`,`modifiedBy`,`modifiedDate`) VALUES ('azure.eventbridge.bus.details',concat(@AZURE_EVENTBRIDGE_BUS_DETAILS,''),'job-scheduler','prd','latest',NULL,NULL,NULL,NULL);
INSERT IGNORE INTO pac_config_properties (`cfkey`,`value`,`application`,`profile`,`label`,`createdBy`,`createdDate`,`modifiedBy`,`modifiedDate`) VALUES ('aws.eventbridge.bus.details',concat(@AWS_EVENTBRIDGE_BUS_DETAILS,''),'job-scheduler','prd','latest',NULL,NULL,NULL,NULL);
INSERT IGNORE INTO pac_config_properties (`cfkey`,`value`,`application`,`profile`,`label`,`createdBy`,`createdDate`,`modifiedBy`,`modifiedDate`) VALUES ('scheduler.total.batches',concat(@JOB_SCHEDULER_NUMBER_OF_BATCHES,''),'job-scheduler','prd','latest',NULL,NULL,NULL,NULL);
INSERT IGNORE INTO pac_config_properties (`cfkey`,`value`,`application`,`profile`,`label`,`createdBy`,`createdDate`,`modifiedBy`,`modifiedDate`) VALUES ('azure.enabled',concat(@AZURE_ENABLED,''),'job-scheduler','prd','latest',NULL,NULL,NULL,NULL);
INSERT IGNORE INTO pac_config_properties (`cfkey`,`value`,`application`,`profile`,`label`,`createdBy`,`createdDate`,`modifiedBy`,`modifiedDate`) VALUES ('gcp.enabled',concat(@GCP_ENABLED,''),'job-scheduler','prd','latest',NULL,NULL,NULL,NULL);
INSERT IGNORE INTO pac_config_properties (`cfkey`,`value`,`application`,`profile`,`label`,`createdBy`,`createdDate`,`modifiedBy`,`modifiedDate`) VALUES ('base.account',concat(@BASE_ACCOUNT,''),'job-scheduler','prd','latest',null,null,null,null);
INSERT IGNORE INTO pac_config_properties (`cfkey`,`value`,`application`,`profile`,`label`,`createdBy`,`createdDate`,`modifiedBy`,`modifiedDate`) VALUES ('base.region',concat(@BASE_REGION,''),'job-scheduler','prd','latest',null,null,null,null);

INSERT IGNORE INTO `pac_config_properties` (`cfkey`, `value`, `application`, `profile`, `label`, `createdBy`, `createdDate`, `modifiedBy`, `modifiedDate`) values('pacman.es.host',concat(@ES_HOST_NAME,''),'rule','prd','latest',NULL,NULL,NULL,NULL);
INSERT IGNORE INTO `pac_config_properties` (`cfkey`, `value`, `application`, `profile`, `label`, `createdBy`, `createdDate`, `modifiedBy`, `modifiedDate`) values('pacman.es.port',concat(@ES_PORT,''),'rule','prd','latest',NULL,NULL,NULL,NULL);
INSERT IGNORE INTO `pac_config_properties` (`cfkey`, `value`, `application`, `profile`, `label`, `createdBy`, `createdDate`, `modifiedBy`, `modifiedDate`) values('esLoggingLevel','DEBUG','rule','prd','latest',NULL,NULL,NULL,NULL);
INSERT IGNORE INTO `pac_config_properties` (`cfkey`, `value`, `application`, `profile`, `label`, `createdBy`, `createdDate`, `modifiedBy`, `modifiedDate`) values('heimdall-host',concat(@ES_HEIMDALL_HOST_NAME,''),'rule','prd','latest',NULL,NULL,NULL,NULL);
INSERT IGNORE INTO `pac_config_properties` (`cfkey`, `value`, `application`, `profile`, `label`, `createdBy`, `createdDate`, `modifiedBy`, `modifiedDate`) values('heimdall-port',concat(@ES_HEIMDALL_PORT,''),'rule','prd','latest',NULL,NULL,NULL,NULL);
INSERT IGNORE INTO `pac_config_properties` (`cfkey`, `value`, `application`, `profile`, `label`, `createdBy`, `createdDate`, `modifiedBy`, `modifiedDate`) values('pacman.host',concat(@PACMAN_HOST_NAME,''),'rule','prd','latest',NULL,NULL,NULL,NULL);
INSERT IGNORE INTO `pac_config_properties` (`cfkey`, `value`, `application`, `profile`, `label`, `createdBy`, `createdDate`, `modifiedBy`, `modifiedDate`) values('pacman.auto.fix.mail.cc.to','mail@paladincloud.io','rule','prd','latest',NULL,NULL,NULL,NULL);
INSERT IGNORE INTO `pac_config_properties` (`cfkey`, `value`, `application`, `profile`, `label`, `createdBy`, `createdDate`, `modifiedBy`, `modifiedDate`) values('pacman.auto.fix.orphan.resource.owner','mail@paladincloud.io','rule','prd','latest',NULL,NULL,NULL,NULL);
INSERT IGNORE INTO `pac_config_properties` (`cfkey`, `value`, `application`, `profile`, `label`, `createdBy`, `createdDate`, `modifiedBy`, `modifiedDate`) values('pacman.auto.fix.role.name',concat('role/',@EVENT_BRIDGE_PREFIX),'rule','prd','latest',NULL,NULL,NULL,NULL);
INSERT IGNORE INTO `pac_config_properties` (`cfkey`, `value`, `application`, `profile`, `label`, `createdBy`, `createdDate`, `modifiedBy`, `modifiedDate`) values('pacman.integrations.slack.webhook.url','','rule','prd','latest',NULL,NULL,NULL,NULL);
INSERT IGNORE INTO `pac_config_properties` (`cfkey`, `value`, `application`, `profile`, `label`, `createdBy`, `createdDate`, `modifiedBy`, `modifiedDate`) values('pacman.target.type.alias','account=iam,volume=ec2,snapshot=ec2,rdsdb=rds,dynamodb=dyndb,appelb=elb_app,classicelb=elb_classic,sg=ec2,elasticip=ec2,iamuser=iam,iamrole=iam','rule','prd','latest',NULL,NULL,NULL,NULL);
INSERT IGNORE INTO `pac_config_properties` (`cfkey`, `value`, `application`, `profile`, `label`, `createdBy`, `createdDate`, `modifiedBy`, `modifiedDate`) values('autofix.allowlist.accounts.S3GlobalAccess_version-1_S3BucketShouldnotpubliclyaccessble_s3','','rule','prd','latest',NULL,NULL,NULL,NULL);
INSERT IGNORE INTO `pac_config_properties` (`cfkey`, `value`, `application`, `profile`, `label`, `createdBy`, `createdDate`, `modifiedBy`, `modifiedDate`) values('autofix.allowlist.accounts.EC2WithPublicIPAccess_version-1_Ec2WithPublicAccess_ec2','','rule','prd','latest',NULL,NULL,NULL,NULL);
INSERT IGNORE INTO `pac_config_properties` (`cfkey`, `value`, `application`, `profile`, `label`, `createdBy`, `createdDate`, `modifiedBy`, `modifiedDate`) values('autofix.cufoff.date','3/28/2018','rule','prd','latest',NULL,NULL,NULL,NULL);
INSERT IGNORE INTO `pac_config_properties` (`cfkey`, `value`, `application`, `profile`, `label`, `createdBy`, `createdDate`, `modifiedBy`, `modifiedDate`) values('api.backup.asset.config',concat(@PACMAN_HOST_NAME,'/api/asset/v1/save-asset-config'),'rule','prd','latest',NULL,NULL,NULL,NULL);
INSERT IGNORE INTO `pac_config_properties` (`cfkey`, `value`, `application`, `profile`, `label`, `createdBy`, `createdDate`, `modifiedBy`, `modifiedDate`) values('api.resource.creationdate',concat(@PACMAN_HOST_NAME,'/api/asset/v1/get-resource-created-date'),'rule','prd','latest',NULL,NULL,NULL,NULL);
INSERT IGNORE INTO `pac_config_properties` (`cfkey`, `value`, `application`, `profile`, `label`, `createdBy`, `createdDate`, `modifiedBy`, `modifiedDate`) values('api.getlastaction',concat(@PACMAN_HOST_NAME,'/api/compliance/v1/get-last-action'),'rule','prd','latest',NULL,NULL,NULL,NULL);
INSERT IGNORE INTO `pac_config_properties` (`cfkey`, `value`, `application`, `profile`, `label`, `createdBy`, `createdDate`, `modifiedBy`, `modifiedDate`) values('api.postlastaction',concat(@PACMAN_HOST_NAME,'/api/compliance/v1/post-action'),'rule','prd','latest',NULL,NULL,NULL,NULL);
INSERT IGNORE INTO `pac_config_properties` (`cfkey`, `value`, `application`, `profile`, `label`, `createdBy`, `createdDate`, `modifiedBy`, `modifiedDate`) values('api.register.reactors.url',concat(@PACMAN_HOST_NAME,'/api/admin/reactors'),'rule','prd','latest',NULL,NULL,NULL,NULL);
INSERT IGNORE INTO `pac_config_properties` (`cfkey`, `value`, `application`, `profile`, `label`, `createdBy`, `createdDate`, `modifiedBy`, `modifiedDate`) values('api.auth.owner.slack.handle','','rule','prd','latest',NULL,NULL,NULL,NULL);
INSERT IGNORE INTO `pac_config_properties` (`cfkey`, `value`, `application`, `profile`, `label`, `createdBy`, `createdDate`, `modifiedBy`, `modifiedDate`) values('pacman.auto.fix.tag.name','pac_auto_fix_do_not_delete','rule','prd','latest',NULL,NULL,NULL,NULL);
INSERT IGNORE INTO `pac_config_properties` (`cfkey`, `value`, `application`, `profile`, `label`, `createdBy`, `createdDate`, `modifiedBy`, `modifiedDate`) values('pacman.auto.fix.max.email.notifications','2','rule','prd','latest',NULL,NULL,NULL,NULL);
INSERT IGNORE INTO `pac_config_properties` (`cfkey`, `value`, `application`, `profile`, `label`, `createdBy`, `createdDate`, `modifiedBy`, `modifiedDate`) values('pacman.auto.fix.resource.name.filter.pattern','','rule','prd','latest',NULL,NULL,NULL,NULL);
INSERT IGNORE INTO `pac_config_properties` (`cfkey`, `value`, `application`, `profile`, `label`, `createdBy`, `createdDate`, `modifiedBy`, `modifiedDate`) values('pacman.es.stats.index','fre-stats','rule','prd','latest',NULL,NULL,NULL,NULL);
INSERT IGNORE INTO `pac_config_properties` (`cfkey`, `value`, `application`, `profile`, `label`, `createdBy`, `createdDate`, `modifiedBy`, `modifiedDate`) values('pacman.es.stats.type','execution-stats','rule','prd','latest',NULL,NULL,NULL,NULL);
INSERT IGNORE INTO `pac_config_properties` (`cfkey`, `value`, `application`, `profile`, `label`, `createdBy`, `createdDate`, `modifiedBy`, `modifiedDate`) values('pacman.es.auto.fix.transaction.index','fre-auto-fix-tran-log','rule','prd','latest',NULL,NULL,NULL,NULL);
INSERT IGNORE INTO `pac_config_properties` (`cfkey`, `value`, `application`, `profile`, `label`, `createdBy`, `createdDate`, `modifiedBy`, `modifiedDate`) values('pacman.es.auto.fix.transaction.type','transaction-log','rule','prd','latest',NULL,NULL,NULL,NULL);
INSERT IGNORE INTO `pac_config_properties` (`cfkey`, `value`, `application`, `profile`, `label`, `createdBy`, `createdDate`, `modifiedBy`, `modifiedDate`) values('pacman.api.sendmail',concat(@PACMAN_HOST_NAME,'/api/notifications/send-plain-text-mail'),'rule','prd','latest',NULL,NULL,NULL,NULL);
INSERT IGNORE INTO `pac_config_properties` (`cfkey`, `value`, `application`, `profile`, `label`, `createdBy`, `createdDate`, `modifiedBy`, `modifiedDate`) values('pacman.es.reactors.index','pac-reactor','rule','prd','latest',NULL,NULL,NULL,NULL);
INSERT IGNORE INTO `pac_config_properties` (`cfkey`, `value`, `application`, `profile`, `label`, `createdBy`, `createdDate`, `modifiedBy`, `modifiedDate`) values('pacman.es.reactors.registry','events-log','rule','prd','latest',NULL,NULL,NULL,NULL);
INSERT IGNORE INTO `pac_config_properties` (`cfkey`, `value`, `application`, `profile`, `label`, `createdBy`, `createdDate`, `modifiedBy`, `modifiedDate`) values('square.one.slack.channel','#square-1-alerts','rule','prd','latest',NULL,NULL,NULL,NULL);
INSERT IGNORE INTO `pac_config_properties` (`cfkey`, `value`, `application`, `profile`, `label`, `createdBy`, `createdDate`, `modifiedBy`, `modifiedDate`) values('com.tmobile.pacman.reactors.impl.s3.S3CreateBucketAndUpdateBucketPolicyReactor.account.whitelist','','rule','prd','latest',NULL,NULL,NULL,NULL);
INSERT IGNORE INTO `pac_config_properties` (`cfkey`, `value`, `application`, `profile`, `label`, `createdBy`, `createdDate`, `modifiedBy`, `modifiedDate`) values('com.tmobile.pacman.reactors.impl.sample.SampleReactor.account.whitelist','','rule','prd','latest',NULL,NULL,NULL,NULL);
INSERT IGNORE INTO `pac_config_properties` (`cfkey`, `value`, `application`, `profile`, `label`, `createdBy`, `createdDate`, `modifiedBy`, `modifiedDate`) values('com.tmobile.pacman.reactors.impl.sample.SampleReactor2.account.whitelist','','rule','prd','latest',NULL,NULL,NULL,NULL);
INSERT IGNORE INTO `pac_config_properties` (`cfkey`, `value`, `application`, `profile`, `label`, `createdBy`, `createdDate`, `modifiedBy`, `modifiedDate`) values('pacman.auto.fix.mail.from','noreply@pacman-tmobile.com','rule','prd','latest',NULL,NULL,NULL,NULL);
INSERT IGNORE INTO `pac_config_properties` (`cfkey`, `value`, `application`, `profile`, `label`, `createdBy`, `createdDate`, `modifiedBy`, `modifiedDate`) values('pacman.auto.fix.tag.salt','','rule','prd','latest',NULL,NULL,NULL,NULL);
INSERT IGNORE INTO `pac_config_properties` (`cfkey`, `value`, `application`, `profile`, `label`, `createdBy`, `createdDate`, `modifiedBy`, `modifiedDate`) values('pacman.auto.fix.tag.encyption.algorithm','AES','rule','prd','latest',NULL,NULL,NULL,NULL);
INSERT IGNORE INTO `pac_config_properties` (`cfkey`, `value`, `application`, `profile`, `label`, `createdBy`, `createdDate`, `modifiedBy`, `modifiedDate`) values('pacman.exempted.mail.subject','PacMan AutoFix - Vulnerable resource is now exempted','rule','prd','latest',NULL,NULL,NULL,NULL);
INSERT IGNORE INTO `pac_config_properties` (`cfkey`, `value`, `application`, `profile`, `label`, `createdBy`, `createdDate`, `modifiedBy`, `modifiedDate`) values('pacman.autofix.exempted.types.for.cutoff.data','iam,account,ec2,volume,snapshot,elasticsearch,efs,redshift,s3,dyndb,rds,elb_app,elb_classic,elasticip','rule','prd','latest',NULL,NULL,NULL,NULL);
INSERT IGNORE INTO `pac_config_properties` (`cfkey`, `value`, `application`, `profile`, `label`, `createdBy`, `createdDate`, `modifiedBy`, `modifiedDate`) values('pacman.autofix.non.taggable.services','iam,account','rule','prd','latest',NULL,NULL,NULL,NULL);
INSERT IGNORE INTO `pac_config_properties` (`cfkey`, `value`, `application`, `profile`, `label`, `createdBy`, `createdDate`, `modifiedBy`, `modifiedDate`) values('pacman.autofix.policy.url.S3GlobalAccess_version-1_S3BucketShouldnotpubliclyaccessble_s3',concat(@PACMAN_HOST_NAME,'/pl/compliance/policy-knowledgebase-details/S3GlobalAccess_version-1_S3BucketShouldnotpubliclyaccessble_s3?ag=aws-all&domain=Infra%20%26%20Platforms'),'rule','prd','latest',NULL,NULL,NULL,NULL);
INSERT IGNORE INTO `pac_config_properties` (`cfkey`, `value`, `application`, `profile`, `label`, `createdBy`, `createdDate`, `modifiedBy`, `modifiedDate`) values('pacman.auto.fix.mail.subject.S3GlobalAccess_version-1_S3BucketShouldnotpubliclyaccessble_s3','PaladinCloud autofix action - S3 bucket policy with anonymous read/write access restored back','rule','prd','latest',NULL,NULL,NULL,NULL);
INSERT IGNORE INTO `pac_config_properties` (`cfkey`, `value`, `application`, `profile`, `label`, `createdBy`, `createdDate`, `modifiedBy`, `modifiedDate`) values('pacman.auto.warning.mail.subject.S3GlobalAccess_version-1_S3BucketShouldnotpubliclyaccessble_s3','PaladinCloud autofix - S3 bucket detected with anonymous access','rule','prd','latest',NULL,NULL,NULL,NULL);
INSERT IGNORE INTO `pac_config_properties` (`cfkey`, `value`, `application`, `profile`, `label`, `createdBy`, `createdDate`, `modifiedBy`, `modifiedDate`) values('pacman.autofix.rule.violation.message.S3GlobalAccess_version-1_S3BucketShouldnotpubliclyaccessble_s3','a S3 bucket  (<b>${RESOURCE_ID}</b>) from account (<b>${ACCOUNT_ID}</b>) of region (<b>${REGION}</b>) created by you is open to internet for anonymous access','rule','prd','latest',NULL,NULL,NULL,NULL);
INSERT IGNORE INTO `pac_config_properties` (`cfkey`, `value`, `application`, `profile`, `label`, `createdBy`, `createdDate`, `modifiedBy`, `modifiedDate`) values('pacman.autofix.rule.warning.message.S3GlobalAccess_version-1_S3BucketShouldnotpubliclyaccessble_s3','The permissions for this S3 bucket will be automatically fixed by PaladinCloud after {days} days if no exception is granted.','rule','prd','latest',NULL,NULL,NULL,NULL);
INSERT IGNORE INTO `pac_config_properties` (`cfkey`, `value`, `application`, `profile`, `label`, `createdBy`, `createdDate`, `modifiedBy`, `modifiedDate`) values('pacman.autofix.rule.post.fix.message.S3GlobalAccess_version-1_S3BucketShouldnotpubliclyaccessble_s3','PaladinCloud has now automatically revoked the public permissions of s3 bucket (<b>${RESOURCE_ID}</b>) created by you as it was a violation of','rule','prd','latest',NULL,NULL,NULL,NULL);
INSERT IGNORE INTO `pac_config_properties` (`cfkey`, `value`, `application`, `profile`, `label`, `createdBy`, `createdDate`, `modifiedBy`, `modifiedDate`) values('pacman.autofix.waittime.S3GlobalAccess_version-1_S3BucketShouldnotpubliclyaccessble_s3','48','rule','prd','latest',NULL,NULL,NULL,NULL);
INSERT IGNORE INTO `pac_config_properties` (`cfkey`, `value`, `application`, `profile`, `label`, `createdBy`, `createdDate`, `modifiedBy`, `modifiedDate`) values('pacman.auto.fix.max.email.notifications.S3GlobalAccess_version-1_S3BucketShouldnotpubliclyaccessble_s3','4','rule','prd','latest',NULL,NULL,NULL,NULL);
INSERT IGNORE INTO `pac_config_properties` (`cfkey`, `value`, `application`, `profile`, `label`, `createdBy`, `createdDate`, `modifiedBy`, `modifiedDate`) values('pacman.autofix.policy.url.EC2WithPublicIPAccess_version-1_Ec2WithPublicAccess_ec2',concat(@PACMAN_HOST_NAME,'/pl/compliance/policy-knowledgebase-details/EC2WithPublicIPAccess_version-1_Ec2WithPublicAccess_ec2?ag=aws-all&domain=Infra%20%26%20Platforms'),'rule','prd','latest',NULL,NULL,NULL,NULL);
INSERT IGNORE INTO `pac_config_properties` (`cfkey`, `value`, `application`, `profile`, `label`, `createdBy`, `createdDate`, `modifiedBy`, `modifiedDate`) values('pacman.auto.fix.mail.subject.EC2WithPublicIPAccess_version-1_Ec2WithPublicAccess_ec2','PaladinCloud autofix action - Ec2 with public access restored back','rule','prd','latest',NULL,NULL,NULL,NULL);
INSERT IGNORE INTO `pac_config_properties` (`cfkey`, `value`, `application`, `profile`, `label`, `createdBy`, `createdDate`, `modifiedBy`, `modifiedDate`) values('pacman.auto.warning.mail.subject.EC2WithPublicIPAccess_version-1_Ec2WithPublicAccess_ec2','PaladinCloud autofix - Ec2 instance detected with public access','rule','prd','latest',NULL,NULL,NULL,NULL);
INSERT IGNORE INTO `pac_config_properties` (`cfkey`, `value`, `application`, `profile`, `label`, `createdBy`, `createdDate`, `modifiedBy`, `modifiedDate`) values('pacman.autofix.rule.violation.message.EC2WithPublicIPAccess_version-1_Ec2WithPublicAccess_ec2','an Ec2 instance  (<b>${RESOURCE_ID}</b>) from account (<b>${ACCOUNT_ID}</b>) of region (<b>${REGION}</b>) created by you is open to internet','rule','prd','latest',NULL,NULL,NULL,NULL);
INSERT IGNORE INTO `pac_config_properties` (`cfkey`, `value`, `application`, `profile`, `label`, `createdBy`, `createdDate`, `modifiedBy`, `modifiedDate`) values('pacman.autofix.rule.warning.message.EC2WithPublicIPAccess_version-1_Ec2WithPublicAccess_ec2','The access to this Ec2 instance will be automatically fixed by PaladinCloud after {days} days if no exception is granted.','rule','prd','latest',NULL,NULL,NULL,NULL);
INSERT IGNORE INTO `pac_config_properties` (`cfkey`, `value`, `application`, `profile`, `label`, `createdBy`, `createdDate`, `modifiedBy`, `modifiedDate`) values('pacman.autofix.rule.post.fix.message.EC2WithPublicIPAccess_version-1_Ec2WithPublicAccess_ec2','PaladinCloud has now automatically revoked the public access of this Ec2 instance created by you as it was a violation of','rule','prd','latest',NULL,NULL,NULL,NULL);
INSERT IGNORE INTO `pac_config_properties` (`cfkey`, `value`, `application`, `profile`, `label`, `createdBy`, `createdDate`, `modifiedBy`, `modifiedDate`) values('pacman.autofix.waittime.EC2WithPublicIPAccess_version-1_Ec2WithPublicAccess_ec2','48','rule','prd','latest',NULL,NULL,NULL,NULL);
INSERT IGNORE INTO `pac_config_properties` (`cfkey`, `value`, `application`, `profile`, `label`, `createdBy`, `createdDate`, `modifiedBy`, `modifiedDate`) values('pacman.auto.fix.max.email.notifications.EC2WithPublicIPAccess_version-1_Ec2WithPublicAccess_ec2','4','rule','prd','latest',NULL,NULL,NULL,NULL);
INSERT IGNORE INTO `pac_config_properties` (`cfkey`, `value`, `application`, `profile`, `label`, `createdBy`, `createdDate`, `modifiedBy`, `modifiedDate`) values('pacman.auto.fix.mail.template.columns.EC2WithPublicIPAccess_version-1_Ec2WithPublicAccess_ec2','Resource Id,Account Id,Region,Attached Sg,Detached Sg','rule','prd','latest',NULL,NULL,NULL,NULL);
INSERT IGNORE INTO `pac_config_properties` (`cfkey`, `value`, `application`, `profile`, `label`, `createdBy`, `createdDate`, `modifiedBy`, `modifiedDate`) values('pacman.auto.fix.common.email.notifications.EC2WithPublicIPAccess_version-1_Ec2WithPublicAccess_ec2','commonTemplate','rule','prd','latest',NULL,NULL,NULL,NULL);
INSERT IGNORE INTO `pac_config_properties` (`cfkey`, `value`, `application`, `profile`, `label`, `createdBy`, `createdDate`, `modifiedBy`, `modifiedDate`) VALUES('pacman.login.user.name',concat(@PACMAN_LOGIN_USER_NAME,''),'rule','prd','latest',NULL,NULL,NULL,NULL);
INSERT IGNORE INTO `pac_config_properties` (`cfkey`, `value`, `application`, `profile`, `label`, `createdBy`, `createdDate`, `modifiedBy`, `modifiedDate`) VALUES('pacman.login.password',concat(@PACMAN_LOGIN_PASSWORD,''),'rule','prd','latest',NULL,NULL,NULL,NULL);
INSERT IGNORE INTO `pac_config_properties` (`cfkey`, `value`, `application`, `profile`, `label`, `createdBy`, `createdDate`, `modifiedBy`, `modifiedDate`) VALUES('email.banner','','rule','prd','latest',NULL,NULL,NULL,NULL);
INSERT IGNORE INTO `pac_config_properties` (`cfkey`, `value`, `application`, `profile`, `label`, `createdBy`, `createdDate`, `modifiedBy`, `modifiedDate`) VALUES('pacbot.autofix.resourceowner.fallbak.email',concat(@PACBOT_AUTOFIX_RESOURCEOWNER_FALLBACK_MAILID,''),'rule','prd','latest',NULL,NULL,NULL,NULL);

INSERT IGNORE INTO `pac_config_properties` (`cfkey`, `value`, `application`, `profile`, `label`, `createdBy`, `createdDate`, `modifiedBy`, `modifiedDate`) values('pacman.autofix.policy.url.path',concat(@PACMAN_HOST_NAME,'/pl/compliance/policy-knowledgebase-details/${RULE_ID}?ag=aws-all&domain=Infra%20%26%20Platforms'),'rule','prd','latest',NULL,NULL,NULL,NULL);
INSERT IGNORE INTO `pac_config_properties` (`cfkey`, `value`, `application`, `profile`, `label`, `createdBy`, `createdDate`, `modifiedBy`, `modifiedDate`) values('autofix.allowlist.accounts.ElbWithPublicAccess_version-1_ApplicationElbWithPublicAccess_appelb','','rule','prd','latest',NULL,NULL,NULL,NULL);
INSERT IGNORE INTO `pac_config_properties` (`cfkey`, `value`, `application`, `profile`, `label`, `createdBy`, `createdDate`, `modifiedBy`, `modifiedDate`) values('pacman.auto.fix.mail.subject.ElbWithPublicAccess_version-1_ApplicationElbWithPublicAccess_appelb','PaladinCloud autofix action - Application ELB with public access restored back','rule','prd','latest',NULL,NULL,NULL,NULL);
INSERT IGNORE INTO `pac_config_properties` (`cfkey`, `value`, `application`, `profile`, `label`, `createdBy`, `createdDate`, `modifiedBy`, `modifiedDate`) values('pacman.auto.warning.mail.subject.ElbWithPublicAccess_version-1_ApplicationElbWithPublicAccess_appelb','PaladinCloud autofix - Application ELB detected with public access','rule','prd','latest',NULL,NULL,NULL,NULL);
INSERT IGNORE INTO `pac_config_properties` (`cfkey`, `value`, `application`, `profile`, `label`, `createdBy`, `createdDate`, `modifiedBy`, `modifiedDate`) values('pacman.autofix.rule.violation.message.ElbWithPublicAccess_version-1_ApplicationElbWithPublicAccess_appelb','An Application ELB (<b>${RESOURCE_ID}</b>) from account (<b>${ACCOUNT_ID}</b>) of region (<b>${REGION}</b>) created by you is open to internet','rule','prd','latest',NULL,NULL,NULL,NULL);
INSERT IGNORE INTO `pac_config_properties` (`cfkey`, `value`, `application`, `profile`, `label`, `createdBy`, `createdDate`, `modifiedBy`, `modifiedDate`) values('pacman.autofix.rule.warning.message.ElbWithPublicAccess_version-1_ApplicationElbWithPublicAccess_appelb','The access to this Application elb will be automatically fixed by PaladinCloud after {days} days if no exception is granted.','rule','prd','latest',NULL,NULL,NULL,NULL);
INSERT IGNORE INTO `pac_config_properties` (`cfkey`, `value`, `application`, `profile`, `label`, `createdBy`, `createdDate`, `modifiedBy`, `modifiedDate`) values('pacman.autofix.rule.post.fix.message.ElbWithPublicAccess_version-1_ApplicationElbWithPublicAccess_appelb','PaladinCloud has now automatically revoked the public access of this Application ELB created by you as it was a violation of','rule','prd','latest',NULL,NULL,NULL,NULL);
INSERT IGNORE INTO `pac_config_properties` (`cfkey`, `value`, `application`, `profile`, `label`, `createdBy`, `createdDate`, `modifiedBy`, `modifiedDate`) values('pacman.autofix.waittime.ElbWithPublicAccess_version-1_ApplicationElbWithPublicAccess_appelb','48','rule','prd','latest',NULL,NULL,NULL,NULL);
INSERT IGNORE INTO `pac_config_properties` (`cfkey`, `value`, `application`, `profile`, `label`, `createdBy`, `createdDate`, `modifiedBy`, `modifiedDate`) values('pacman.auto.fix.max.email.notifications.ElbWithPublicAccess_version-1_ApplicationElbWithPublicAccess_appelb','4','rule','prd','latest',NULL,NULL,NULL,NULL);
INSERT IGNORE INTO `pac_config_properties` (`cfkey`, `value`, `application`, `profile`, `label`, `createdBy`, `createdDate`, `modifiedBy`, `modifiedDate`) values('pacman.auto.fix.mail.template.columns.ElbWithPublicAccess_version-1_ApplicationElbWithPublicAccess_appelb','Resource Id,Account Id,Region,Attached Sg,Detached Sg','rule','prd','latest',NULL,NULL,NULL,NULL);
INSERT IGNORE INTO `pac_config_properties` (`cfkey`, `value`, `application`, `profile`, `label`, `createdBy`, `createdDate`, `modifiedBy`, `modifiedDate`) values('pacman.auto.fix.common.email.notifications.ElbWithPublicAccess_version-1_ApplicationElbWithPublicAccess_appelb','commonTemplate','rule','prd','latest',NULL,NULL,NULL,NULL);
INSERT IGNORE INTO `pac_config_properties` (`cfkey`, `value`, `application`, `profile`, `label`, `createdBy`, `createdDate`, `modifiedBy`, `modifiedDate`) values('api.close-expired-exemptions',concat(@PACMAN_HOST_NAME,'/api/admin/policy/close-expired-exemption?policyUUID='),'rule','prd','latest',NULL,NULL,NULL,NULL);


INSERT IGNORE INTO `pac_config_properties` (`cfkey`, `value`, `application`, `profile`, `label`, `createdBy`, `createdDate`, `modifiedBy`, `modifiedDate`) values('autofix.allowlist.accounts.ElbWithPublicAccess_version-1_ClassicElbWithPublicAccess_classicelb','','rule','prd','latest',NULL,NULL,NULL,NULL);
INSERT IGNORE INTO `pac_config_properties` (`cfkey`, `value`, `application`, `profile`, `label`, `createdBy`, `createdDate`, `modifiedBy`, `modifiedDate`) values('pacman.auto.fix.mail.subject.ElbWithPublicAccess_version-1_ClassicElbWithPublicAccess_classicelb','PaladinCloud autofix action - Classic ELB with public access restored back','rule','prd','latest',NULL,NULL,NULL,NULL);
INSERT IGNORE INTO `pac_config_properties` (`cfkey`, `value`, `application`, `profile`, `label`, `createdBy`, `createdDate`, `modifiedBy`, `modifiedDate`) values('pacman.auto.warning.mail.subject.ElbWithPublicAccess_version-1_ClassicElbWithPublicAccess_classicelb','PaladinCloud autofix - Classic ELB detected with public access','rule','prd','latest',NULL,NULL,NULL,NULL);
INSERT IGNORE INTO `pac_config_properties` (`cfkey`, `value`, `application`, `profile`, `label`, `createdBy`, `createdDate`, `modifiedBy`, `modifiedDate`) values('pacman.autofix.rule.violation.message.ElbWithPublicAccess_version-1_ClassicElbWithPublicAccess_classicelb','An Classic ELB (<b>${RESOURCE_ID}</b>) from account (<b>${ACCOUNT_ID}</b>) of region (<b>${REGION}</b>) created by you is open to internet','rule','prd','latest',NULL,NULL,NULL,NULL);
INSERT IGNORE INTO `pac_config_properties` (`cfkey`, `value`, `application`, `profile`, `label`, `createdBy`, `createdDate`, `modifiedBy`, `modifiedDate`) values('pacman.autofix.rule.warning.message.ElbWithPublicAccess_version-1_ClassicElbWithPublicAccess_classicelb','The access to this Classic elb will be automatically fixed by PaladinCloud after {days} days if no exception is granted.','rule','prd','latest',NULL,NULL,NULL,NULL);
INSERT IGNORE INTO `pac_config_properties` (`cfkey`, `value`, `application`, `profile`, `label`, `createdBy`, `createdDate`, `modifiedBy`, `modifiedDate`) values('pacman.autofix.rule.post.fix.message.ElbWithPublicAccess_version-1_ClassicElbWithPublicAccess_classicelb','PaladinCloud has now automatically revoked the public access of this Classic ELB created by you as it was a violation of','rule','prd','latest',NULL,NULL,NULL,NULL);
INSERT IGNORE INTO `pac_config_properties` (`cfkey`, `value`, `application`, `profile`, `label`, `createdBy`, `createdDate`, `modifiedBy`, `modifiedDate`) values('pacman.autofix.waittime.ElbWithPublicAccess_version-1_ClassicElbWithPublicAccess_classicelb','48','rule','prd','latest',NULL,NULL,NULL,NULL);
INSERT IGNORE INTO `pac_config_properties` (`cfkey`, `value`, `application`, `profile`, `label`, `createdBy`, `createdDate`, `modifiedBy`, `modifiedDate`) values('pacman.auto.fix.max.email.notifications.ElbWithPublicAccess_version-1_ClassicElbWithPublicAccess_classicelb','4','rule','prd','latest',NULL,NULL,NULL,NULL);
INSERT IGNORE INTO `pac_config_properties` (`cfkey`, `value`, `application`, `profile`, `label`, `createdBy`, `createdDate`, `modifiedBy`, `modifiedDate`) values('pacman.auto.fix.mail.template.columns.ElbWithPublicAccess_version-1_ClassicElbWithPublicAccess_classicelb','Resource Id,Account Id,Region,Attached Sg,Detached Sg','rule','prd','latest',NULL,NULL,NULL,NULL);
INSERT IGNORE INTO `pac_config_properties` (`cfkey`, `value`, `application`, `profile`, `label`, `createdBy`, `createdDate`, `modifiedBy`, `modifiedDate`) values('pacman.auto.fix.common.email.notifications.ElbWithPublicAccess_version-1_ClassicElbWithPublicAccess_classicelb','commonTemplate','rule','prd','latest',NULL,NULL,NULL,NULL);

INSERT IGNORE INTO `pac_config_properties` (`cfkey`, `value`, `application`, `profile`, `label`, `createdBy`, `createdDate`, `modifiedBy`, `modifiedDate`) values('autofix.allowlist.accounts.RedShiftPublicAccess_version-1_RedShiftPublicAccess_redshift','','rule','prd','latest',NULL,NULL,NULL,NULL);
INSERT IGNORE INTO `pac_config_properties` (`cfkey`, `value`, `application`, `profile`, `label`, `createdBy`, `createdDate`, `modifiedBy`, `modifiedDate`) values('pacman.auto.fix.mail.subject.RedShiftPublicAccess_version-1_RedShiftPublicAccess_redshift','PaladinCloud autofix action - Redshift with public access restored back','rule','prd','latest',NULL,NULL,NULL,NULL);
INSERT IGNORE INTO `pac_config_properties` (`cfkey`, `value`, `application`, `profile`, `label`, `createdBy`, `createdDate`, `modifiedBy`, `modifiedDate`) values('pacman.auto.warning.mail.subject.RedShiftPublicAccess_version-1_RedShiftPublicAccess_redshift','PaladinCloud autofix - Redshift detected with public access','rule','prd','latest',NULL,NULL,NULL,NULL);
INSERT IGNORE INTO `pac_config_properties` (`cfkey`, `value`, `application`, `profile`, `label`, `createdBy`, `createdDate`, `modifiedBy`, `modifiedDate`) values('pacman.autofix.rule.violation.message.RedShiftPublicAccess_version-1_RedShiftPublicAccess_redshift','Redshift (<b>${RESOURCE_ID}</b>) from account (<b>${ACCOUNT_ID}</b>) of region (<b>${REGION}</b>) created by you is open to internet','rule','prd','latest',NULL,NULL,NULL,NULL);
INSERT IGNORE INTO `pac_config_properties` (`cfkey`, `value`, `application`, `profile`, `label`, `createdBy`, `createdDate`, `modifiedBy`, `modifiedDate`) values('pacman.autofix.rule.warning.message.RedShiftPublicAccess_version-1_RedShiftPublicAccess_redshift','The access to this Redshift will be automatically fixed by PaladinCloud after {days} days if no exception is granted.','rule','prd','latest',NULL,NULL,NULL,NULL);
INSERT IGNORE INTO `pac_config_properties` (`cfkey`, `value`, `application`, `profile`, `label`, `createdBy`, `createdDate`, `modifiedBy`, `modifiedDate`) values('pacman.autofix.rule.post.fix.message.RedShiftPublicAccess_version-1_RedShiftPublicAccess_redshift','PaladinCloud has now automatically revoked the public access of this Redshift created by you as it was a violation of','rule','prd','latest',NULL,NULL,NULL,NULL);
INSERT IGNORE INTO `pac_config_properties` (`cfkey`, `value`, `application`, `profile`, `label`, `createdBy`, `createdDate`, `modifiedBy`, `modifiedDate`) values('pacman.autofix.waittime.RedShiftPublicAccess_version-1_RedShiftPublicAccess_redshift','48','rule','prd','latest',NULL,NULL,NULL,NULL);
INSERT IGNORE INTO `pac_config_properties` (`cfkey`, `value`, `application`, `profile`, `label`, `createdBy`, `createdDate`, `modifiedBy`, `modifiedDate`) values('pacman.auto.fix.max.email.notifications.RedShiftPublicAccess_version-1_RedShiftPublicAccess_redshift','4','rule','prd','latest',NULL,NULL,NULL,NULL);
INSERT IGNORE INTO `pac_config_properties` (`cfkey`, `value`, `application`, `profile`, `label`, `createdBy`, `createdDate`, `modifiedBy`, `modifiedDate`) values('pacman.auto.fix.mail.template.columns.RedShiftPublicAccess_version-1_RedShiftPublicAccess_redshift','Resource Id,Account Id,Region,Attached Sg,Detached Sg','rule','prd','latest',NULL,NULL,NULL,NULL);
INSERT IGNORE INTO `pac_config_properties` (`cfkey`, `value`, `application`, `profile`, `label`, `createdBy`, `createdDate`, `modifiedBy`, `modifiedDate`) values('pacman.auto.fix.common.email.notifications.RedShiftPublicAccess_version-1_RedShiftPublicAccess_redshift','commonTemplate','rule','prd','latest',NULL,NULL,NULL,NULL);

INSERT IGNORE INTO `pac_config_properties` (`cfkey`, `value`, `application`, `profile`, `label`, `createdBy`, `createdDate`, `modifiedBy`, `modifiedDate`) values('autofix.allowlist.accounts.rdsdb_version-1_RdsDbPublicAccess_rdsdb','','rule','prd','latest',NULL,NULL,NULL,NULL);
INSERT IGNORE INTO `pac_config_properties` (`cfkey`, `value`, `application`, `profile`, `label`, `createdBy`, `createdDate`, `modifiedBy`, `modifiedDate`) values('pacman.auto.fix.mail.subject.rdsdb_version-1_RdsDbPublicAccess_rdsdb','PaladinCloud autofix action - Rds DB with public access restored back','rule','prd','latest',NULL,NULL,NULL,NULL);
INSERT IGNORE INTO `pac_config_properties` (`cfkey`, `value`, `application`, `profile`, `label`, `createdBy`, `createdDate`, `modifiedBy`, `modifiedDate`) values('pacman.auto.warning.mail.subject.rdsdb_version-1_RdsDbPublicAccess_rdsdb','PaladinCloud autofix - Rds DB detected with public access','rule','prd','latest',NULL,NULL,NULL,NULL);
INSERT IGNORE INTO `pac_config_properties` (`cfkey`, `value`, `application`, `profile`, `label`, `createdBy`, `createdDate`, `modifiedBy`, `modifiedDate`) values('pacman.autofix.rule.violation.message.rdsdb_version-1_RdsDbPublicAccess_rdsdb','Rds DB (<b>${RESOURCE_ID}</b>) from account (<b>${ACCOUNT_ID}</b>) of region (<b>${REGION}</b>) created by you is open to internet','rule','prd','latest',NULL,NULL,NULL,NULL);
INSERT IGNORE INTO `pac_config_properties` (`cfkey`, `value`, `application`, `profile`, `label`, `createdBy`, `createdDate`, `modifiedBy`, `modifiedDate`) values('pacman.autofix.rule.warning.message.rdsdb_version-1_RdsDbPublicAccess_rdsdb','The access to this Rds DB will be automatically fixed by PaladinCloud after {days} days if no exception is granted.','rule','prd','latest',NULL,NULL,NULL,NULL);
INSERT IGNORE INTO `pac_config_properties` (`cfkey`, `value`, `application`, `profile`, `label`, `createdBy`, `createdDate`, `modifiedBy`, `modifiedDate`) values('pacman.autofix.rule.post.fix.message.rdsdb_version-1_RdsDbPublicAccess_rdsdb','PaladinCloud has now automatically revoked the public access of this Rds DB created by you as it was a violation of','rule','prd','latest',NULL,NULL,NULL,NULL);
INSERT IGNORE INTO `pac_config_properties` (`cfkey`, `value`, `application`, `profile`, `label`, `createdBy`, `createdDate`, `modifiedBy`, `modifiedDate`) values('pacman.autofix.waittime.rdsdb_version-1_RdsDbPublicAccess_rdsdb','48','rule','prd','latest',NULL,NULL,NULL,NULL);
INSERT IGNORE INTO `pac_config_properties` (`cfkey`, `value`, `application`, `profile`, `label`, `createdBy`, `createdDate`, `modifiedBy`, `modifiedDate`) values('pacman.auto.fix.max.email.notifications.rdsdb_version-1_RdsDbPublicAccess_rdsdb','4','rule','prd','latest',NULL,NULL,NULL,NULL);
INSERT IGNORE INTO `pac_config_properties` (`cfkey`, `value`, `application`, `profile`, `label`, `createdBy`, `createdDate`, `modifiedBy`, `modifiedDate`) values('pacman.auto.fix.mail.template.columns.rdsdb_version-1_RdsDbPublicAccess_rdsdb','Resource Id,Account Id,Region,Attached Sg,Detached Sg','rule','prd','latest',NULL,NULL,NULL,NULL);
INSERT IGNORE INTO `pac_config_properties` (`cfkey`, `value`, `application`, `profile`, `label`, `createdBy`, `createdDate`, `modifiedBy`, `modifiedDate`) values('pacman.auto.fix.common.email.notifications.rdsdb_version-1_RdsDbPublicAccess_rdsdb','commonTemplate','rule','prd','latest',NULL,NULL,NULL,NULL);

INSERT IGNORE INTO `pac_config_properties` (`cfkey`, `value`, `application`, `profile`, `label`, `createdBy`, `createdDate`, `modifiedBy`, `modifiedDate`) values('autofix.allowlist.accounts.Unused-Security-group_version-1_UnusedSecurityGroup_sg','','rule','prd','latest',NULL,NULL,NULL,NULL);
INSERT IGNORE INTO `pac_config_properties` (`cfkey`, `value`, `application`, `profile`, `label`, `createdBy`, `createdDate`, `modifiedBy`, `modifiedDate`) values('pacman.autofix.contact.Unused-Security-group_version-1_UnusedSecurityGroup_sg','','rule','prd','latest',NULL,NULL,NULL,NULL);
INSERT IGNORE INTO `pac_config_properties` (`cfkey`, `value`, `application`, `profile`, `label`, `createdBy`, `createdDate`, `modifiedBy`, `modifiedDate`) values('pacman.autofix.fix.type.Unused-Security-group_version-1_UnusedSecurityGroup_sg','','rule','prd','latest',NULL,NULL,NULL,NULL);
INSERT IGNORE INTO `pac_config_properties` (`cfkey`, `value`, `application`, `profile`, `label`, `createdBy`, `createdDate`, `modifiedBy`, `modifiedDate`) values('pacman.auto.fix.mail.subject.Unused-Security-group_version-1_UnusedSecurityGroup_sg','PaladinCloud - Unused AWS Security Group Auto Deleted Report which are created by PaladinCloud','rule','prd','latest',NULL,NULL,NULL,NULL);
INSERT IGNORE INTO `pac_config_properties` (`cfkey`, `value`, `application`, `profile`, `label`, `createdBy`, `createdDate`, `modifiedBy`, `modifiedDate`) values('pacman.autofix.fix.notify.Unused-Security-group_version-1_UnusedSecurityGroup_sg','','rule','prd','latest',NULL,NULL,NULL,NULL);
INSERT IGNORE INTO `pac_config_properties` (`cfkey`, `value`, `application`, `profile`, `label`, `createdBy`, `createdDate`, `modifiedBy`, `modifiedDate`) values('pacman.autofix.rule.post.fix.message.Unused-Security-group_version-1_UnusedSecurityGroup_sg','PaladinCloud has now automatically deleted the following list of unused security group resources which are created by PaladinCloud','rule','prd','latest',NULL,NULL,NULL,NULL);
INSERT IGNORE INTO `pac_config_properties` (`cfkey`, `value`, `application`, `profile`, `label`, `createdBy`, `createdDate`, `modifiedBy`, `modifiedDate`) values('pacman.auto.fix.mail.template.columns.Unused-Security-group_version-1_UnusedSecurityGroup_sg','Resource Id,Account Id,Region,Group Name','rule','prd','latest',NULL,NULL,NULL,NULL);
INSERT IGNORE INTO `pac_config_properties` (`cfkey`, `value`, `application`, `profile`, `label`, `createdBy`, `createdDate`, `modifiedBy`, `modifiedDate`) values('pacman.auto.fix.common.email.notifications.Unused-Security-group_version-1_UnusedSecurityGroup_sg','commonTemplate','rule','prd','latest',NULL,NULL,NULL,NULL);
INSERT IGNORE INTO `pac_config_properties` (`cfkey`, `value`, `application`, `profile`, `label`, `createdBy`, `createdDate`, `modifiedBy`, `modifiedDate`) values('pacman.auto.warning.mail.subject.Unused-Security-group_version-1_UnusedSecurityGroup_sg','PaladinCloud autofix - Found Unused AWS Security Group','rule','prd','latest',NULL,NULL,NULL,NULL);
INSERT IGNORE INTO `pac_config_properties` (`cfkey`, `value`, `application`, `profile`, `label`, `createdBy`, `createdDate`, `modifiedBy`, `modifiedDate`) values('pacman.autofix.rule.warning.message.Unused-Security-group_version-1_UnusedSecurityGroup_sg','The unused Security group will be automatically deleted by PaladinCloud after {days} days if no exception is granted.','rule','prd','latest',NULL,NULL,NULL,NULL);
INSERT IGNORE INTO `pac_config_properties` (`cfkey`, `value`, `application`, `profile`, `label`, `createdBy`, `createdDate`, `modifiedBy`, `modifiedDate`) values('pacman.autofix.rule.violation.message.Unused-Security-group_version-1_UnusedSecurityGroup_sg','Security group (<b>${RESOURCE_ID}</b>) from account (<b>${ACCOUNT_ID}</b>) of region (<b>${REGION}</b>) is unused.','rule','prd','latest',NULL,NULL,NULL,NULL);
INSERT IGNORE INTO `pac_config_properties` (`cfkey`, `value`, `application`, `profile`, `label`, `createdBy`, `createdDate`, `modifiedBy`, `modifiedDate`) values('pacman.autofix.waittime.Unused-Security-group_version-1_UnusedSecurityGroup_sg','48','rule','prd','latest',NULL,NULL,NULL,NULL);
INSERT IGNORE INTO `pac_config_properties` (`cfkey`, `value`, `application`, `profile`, `label`, `createdBy`, `createdDate`, `modifiedBy`, `modifiedDate`) values('pacman.auto.fix.max.email.notifications.Unused-Security-group_version-1_UnusedSecurityGroup_sg','2','rule','prd','latest',NULL,NULL,NULL,NULL);

INSERT IGNORE INTO `pac_config_properties` (`cfkey`, `value`, `application`, `profile`, `label`, `createdBy`, `createdDate`, `modifiedBy`, `modifiedDate`) values('autofix.allowlist.accounts.ElasticSearchPublicAccess_version-1_ElasticSearchPublicAccessRule_elasticsearch','','rule','prd','latest',NULL,NULL,NULL,NULL);
INSERT IGNORE INTO `pac_config_properties` (`cfkey`, `value`, `application`, `profile`, `label`, `createdBy`, `createdDate`, `modifiedBy`, `modifiedDate`) values('pacman.auto.fix.mail.subject.ElasticSearchPublicAccess_version-1_ElasticSearchPublicAccessRule_elasticsearch','PaladinCloud autofix action - Elasticsearch with public access restored back','rule','prd','latest',NULL,NULL,NULL,NULL);
INSERT IGNORE INTO `pac_config_properties` (`cfkey`, `value`, `application`, `profile`, `label`, `createdBy`, `createdDate`, `modifiedBy`, `modifiedDate`) values('pacman.auto.warning.mail.subject.ElasticSearchPublicAccess_version-1_ElasticSearchPublicAccessRule_elasticsearch','PaladinCloud autofix - Elasticsearch detected with public access','rule','prd','latest',NULL,NULL,NULL,NULL);
INSERT IGNORE INTO `pac_config_properties` (`cfkey`, `value`, `application`, `profile`, `label`, `createdBy`, `createdDate`, `modifiedBy`, `modifiedDate`) values('pacman.autofix.rule.violation.message.ElasticSearchPublicAccess_version-1_ElasticSearchPublicAccessRule_elasticsearch','Elasticsearch (<b>${RESOURCE_ID}</b>) from account (<b>${ACCOUNT_ID}</b>) of region (<b>${REGION}</b>) created by you is open to internet','rule','prd','latest',NULL,NULL,NULL,NULL);
INSERT IGNORE INTO `pac_config_properties` (`cfkey`, `value`, `application`, `profile`, `label`, `createdBy`, `createdDate`, `modifiedBy`, `modifiedDate`) values('pacman.autofix.rule.warning.message.ElasticSearchPublicAccess_version-1_ElasticSearchPublicAccessRule_elasticsearch','The access to this Elasticsearch will be automatically fixed by PaladinCloud after {days} days if no exception is granted.','rule','prd','latest',NULL,NULL,NULL,NULL);
INSERT IGNORE INTO `pac_config_properties` (`cfkey`, `value`, `application`, `profile`, `label`, `createdBy`, `createdDate`, `modifiedBy`, `modifiedDate`) values('pacman.autofix.rule.post.fix.message.ElasticSearchPublicAccess_version-1_ElasticSearchPublicAccessRule_elasticsearch','PaladinCloud has now automatically revoked the public access of this Elasticsearch created by you as it was a violation of','rule','prd','latest',NULL,NULL,NULL,NULL);
INSERT IGNORE INTO `pac_config_properties` (`cfkey`, `value`, `application`, `profile`, `label`, `createdBy`, `createdDate`, `modifiedBy`, `modifiedDate`) values('pacman.autofix.waittime.ElasticSearchPublicAccess_version-1_ElasticSearchPublicAccessRule_elasticsearch','48','rule','prd','latest',NULL,NULL,NULL,NULL);
INSERT IGNORE INTO `pac_config_properties` (`cfkey`, `value`, `application`, `profile`, `label`, `createdBy`, `createdDate`, `modifiedBy`, `modifiedDate`) values('pacman.auto.fix.max.email.notifications.ElasticSearchPublicAccess_version-1_ElasticSearchPublicAccessRule_elasticsearch','4','rule','prd','latest',NULL,NULL,NULL,NULL);
INSERT IGNORE INTO `pac_config_properties` (`cfkey`, `value`, `application`, `profile`, `label`, `createdBy`, `createdDate`, `modifiedBy`, `modifiedDate`) values('pacman.auto.fix.mail.template.columns.ElasticSearchPublicAccess_version-1_ElasticSearchPublicAccessRule_elasticsearch','Resource Id,Account Id,Region,Attached Sg,Detached Sg','rule','prd','latest',NULL,NULL,NULL,NULL);
INSERT IGNORE INTO `pac_config_properties` (`cfkey`, `value`, `application`, `profile`, `label`, `createdBy`, `createdDate`, `modifiedBy`, `modifiedDate`) values('pacman.auto.fix.common.email.notifications.ElasticSearchPublicAccess_version-1_ElasticSearchPublicAccessRule_elasticsearch','commonTemplate','rule','prd','latest',NULL,NULL,NULL,NULL);
INSERT IGNORE INTO `pac_config_properties` (`cfkey`, `value`, `application`, `profile`, `label`, `createdBy`, `createdDate`, `modifiedBy`, `modifiedDate`) values('deleteSgTag','pacbot-delete-sg','rule','prd','latest',NULL,NULL,NULL,NULL);

INSERT IGNORE INTO pac_config_properties (`cfkey`,`value`,`application`,`profile`,`label`,`createdBy`,`createdDate`,`modifiedBy`,`modifiedDate`) VALUES ('pacman.autofix.rule.post.fix.message.Azure_Enable_Network_Security_for_SSH','PaladinCloud has now automatically revoked the public permissions of network security group ( <b> ${RESOURCE_ID} </b> ) created by you as it was a violation','application','prd','latest',NULL,NULL,NULL,NULL);
INSERT IGNORE INTO pac_config_properties (`cfkey`,`value`,`application`,`profile`,`label`,`createdBy`,`createdDate`,`modifiedBy`,`modifiedDate`) VALUES ('pacman.auto.fix.mail.template.columns.Azure_Enable_Network_Security_for_SSH','Resource Id,Subscription,Region,Name','application','prd','latest',NULL,NULL,NULL,NULL);
INSERT IGNORE INTO pac_config_properties (`cfkey`,`value`,`application`,`profile`,`label`,`createdBy`,`createdDate`,`modifiedBy`,`modifiedDate`) VALUES ('pacman.auto.fix.common.email.notifications.Azure_Enable_Network_Security_for_SSH','commonTemplate','application','prd','latest',NULL,NULL,NULL,NULL);
INSERT IGNORE INTO pac_config_properties (`cfkey`,`value`,`application`,`profile`,`label`,`createdBy`,`createdDate`,`modifiedBy`,`modifiedDate`) VALUES ('pacman.autofix.fix.notify.Azure_Enable_Network_Security_for_SSH','dheeraj.kholia@paladincloud.io','application','prd','latest',NULL,NULL,NULL,NULL);
INSERT IGNORE INTO pac_config_properties (`cfkey`,`value`,`application`,`profile`,`label`,`createdBy`,`createdDate`,`modifiedBy`,`modifiedDate`) VALUES ('pacman.auto.fix.mail.subject.Azure_Enable_Network_Security_for_SSH','PaladinCloud autofix action - Anonymous access to azure network security group port revoked','application','prd','latest',NULL,NULL,NULL,NULL);
INSERT IGNORE INTO pac_config_properties (`cfkey`,`value`,`application`,`profile`,`label`,`createdBy`,`createdDate`,`modifiedBy`,`modifiedDate`) VALUES ('pacman.autofix.fix.type.Azure_Enable_Network_Security_for_SSH','silent','application','prd','latest',NULL,NULL,NULL,NULL);
INSERT IGNORE INTO pac_config_properties (`cfkey`,`value`,`application`,`profile`,`label`,`createdBy`,`createdDate`,`modifiedBy`,`modifiedDate`) VALUES ('pacman.autofix.contact.Azure_Enable_Network_Security_for_SSH','dheeraj.kholia@paladincloud.io','application','prd','latest',NULL,NULL,NULL,NULL);

INSERT IGNORE INTO pac_config_properties (`cfkey`,`value`,`application`,`profile`,`label`,`createdBy`,`createdDate`,`modifiedBy`,`modifiedDate`)VALUES ('pacman.autofix.rule.post.fix.message.VPC_firewall_RDP_port_3389_should_not_be_publicly_accessible','PaladinCloud has now automatically revoked the public permissions of firewall rule (<b> ${RESOURCE_ID} </b>) created by you as it was a violation','application','prd','latest',NULL,NULL,NULL,NULL);
INSERT IGNORE INTO pac_config_properties (`cfkey`,`value`,`application`,`profile`,`label`,`createdBy`,`createdDate`,`modifiedBy`,`modifiedDate`) VALUES ('pacman.auto.fix.mail.template.columns.VPC_firewall_RDP_port_3389_should_not_be_publicly_accessible','Resource Id,ProjectName,Firewall Rule Name','application','prd','latest',NULL,NULL,NULL,NULL);
INSERT IGNORE INTO pac_config_properties (`cfkey`,`value`,`application`,`profile`,`label`,`createdBy`,`createdDate`,`modifiedBy`,`modifiedDate`) VALUES ('pacman.auto.fix.common.email.notifications.VPC_firewall_RDP_port_3389_should_not_be_publicly_accessible','commonTemplate','application','prd','latest',NULL,NULL,NULL,NULL);
INSERT IGNORE INTO pac_config_properties (`cfkey`,`value`,`application`,`profile`,`label`,`createdBy`,`createdDate`,`modifiedBy`,`modifiedDate`) VALUES ('pacman.autofix.fix.notify.VPC_firewall_RDP_port_3389_should_not_be_publicly_accessible','dheeraj.kholia@paladincloud.io','application','prd','latest',NULL,NULL,NULL,NULL);
INSERT IGNORE INTO pac_config_properties (`cfkey`,`value`,`application`,`profile`,`label`,`createdBy`,`createdDate`,`modifiedBy`,`modifiedDate`) VALUES ('pacman.auto.fix.mail.subject.VPC_firewall_RDP_port_3389_should_not_be_publicly_accessible','PaladinCloud autofix action - Anonymous access to port revoked','application','prd','latest',NULL,NULL,NULL,NULL);
INSERT IGNORE INTO pac_config_properties (`cfkey`,`value`,`application`,`profile`,`label`,`createdBy`,`createdDate`,`modifiedBy`,`modifiedDate`) VALUES ('pacman.autofix.fix.type.VPC_firewall_RDP_port_3389_should_not_be_publicly_accessible','silent','application','prd','latest',NULL,NULL,NULL,NULL);
INSERT IGNORE INTO pac_config_properties (`cfkey`,`value`,`application`,`profile`,`label`,`createdBy`,`createdDate`,`modifiedBy`,`modifiedDate`) VALUES ('pacman.autofix.contact.VPC_firewall_RDP_port_3389_should_not_be_publicly_accessible','dheeraj.kholia@paladincloud.io','application','prd','latest',NULL,NULL,NULL,NULL);

INSERT IGNORE INTO pac_config_properties (`cfkey`,`value`,`application`,`profile`,`label`,`createdBy`,`createdDate`,`modifiedBy`,`modifiedDate`) VALUES ('pacman.autofix.rule.post.fix.message.UnrestrictedSqlDatabaseAccessRule_version-1','PaladinCloud has now automatically revoked the unrestricted access to SQL database instance (<b> ${RESOURCE_ID} </b>) created by you as it was a violation','application','prd','latest',NULL,NULL,NULL,NULL);
INSERT IGNORE INTO pac_config_properties (`cfkey`,`value`,`application`,`profile`,`label`,`createdBy`,`createdDate`,`modifiedBy`,`modifiedDate`) VALUES ('pacman.auto.fix.mail.template.columns.UnrestrictedSqlDatabaseAccessRule_version-1','Resource Id,Subscription,Name','application','prd','latest',NULL,NULL,NULL,NULL);
INSERT IGNORE INTO pac_config_properties (`cfkey`,`value`,`application`,`profile`,`label`,`createdBy`,`createdDate`,`modifiedBy`,`modifiedDate`) VALUES ('pacman.auto.fix.common.email.notifications.UnrestrictedSqlDatabaseAccessRule_version-1','commonTemplate','application','prd','latest',NULL,NULL,NULL,NULL);
INSERT IGNORE INTO pac_config_properties (`cfkey`,`value`,`application`,`profile`,`label`,`createdBy`,`createdDate`,`modifiedBy`,`modifiedDate`) VALUES ('pacman.autofix.fix.notify.UnrestrictedSqlDatabaseAccessRule_version-1','dheeraj.kholia@paladincloud.io','application','prd','latest',NULL,NULL,NULL,NULL);
INSERT IGNORE INTO pac_config_properties (`cfkey`,`value`,`application`,`profile`,`label`,`createdBy`,`createdDate`,`modifiedBy`,`modifiedDate`) VALUES ('pacman.auto.fix.mail.subject.UnrestrictedSqlDatabaseAccessRule_version-1','PaladinCloud autofix action - Unrestricted access to SQL database revoked','application','prd','latest',NULL,NULL,NULL,NULL);
INSERT IGNORE INTO pac_config_properties (`cfkey`,`value`,`application`,`profile`,`label`,`createdBy`,`createdDate`,`modifiedBy`,`modifiedDate`) VALUES ('pacman.autofix.fix.type.UnrestrictedSqlDatabaseAccessRule_version-1','silent','application','prd','latest',NULL,NULL,NULL,NULL);
INSERT IGNORE INTO pac_config_properties (`cfkey`,`value`,`application`,`profile`,`label`,`createdBy`,`createdDate`,`modifiedBy`,`modifiedDate`) VALUES ('pacman.autofix.contact.UnrestrictedSqlDatabaseAccessRule_version-1','dheeraj.kholia@paladincloud.io','application','prd','latest',NULL,NULL,NULL,NULL);

INSERT IGNORE INTO pac_config_properties (`cfkey`,`value`,`application`,`profile`,`label`,`createdBy`,`createdDate`,`modifiedBy`,`modifiedDate`) VALUES ('pacman.autofix.rule.post.fix.message.Azure_Check_Public_Access_For_Storage_Account','PaladinCloud has now automatically revoked the unrestricted access to SQL database instance (<b> ${RESOURCE_ID} </b>) created by you as it was a violation','application','prd','latest',NULL,NULL,NULL,NULL);
INSERT IGNORE INTO pac_config_properties (`cfkey`,`value`,`application`,`profile`,`label`,`createdBy`,`createdDate`,`modifiedBy`,`modifiedDate`) VALUES ('pacman.auto.fix.mail.template.columns.Azure_Check_Public_Access_For_Storage_Account','Resource Id,Subscription,Name','application','prd','latest',NULL,NULL,NULL,NULL);
INSERT IGNORE INTO pac_config_properties (`cfkey`,`value`,`application`,`profile`,`label`,`createdBy`,`createdDate`,`modifiedBy`,`modifiedDate`) VALUES ('pacman.auto.fix.common.email.notifications.Azure_Check_Public_Access_For_Storage_Account','commonTemplate','application','prd','latest',NULL,NULL,NULL,NULL);
INSERT IGNORE INTO pac_config_properties (`cfkey`,`value`,`application`,`profile`,`label`,`createdBy`,`createdDate`,`modifiedBy`,`modifiedDate`) VALUES ('pacman.autofix.fix.notify.Azure_Check_Public_Access_For_Storage_Account','dheeraj.kholia@paladincloud.io','application','prd','latest',NULL,NULL,NULL,NULL);
INSERT IGNORE INTO pac_config_properties (`cfkey`,`value`,`application`,`profile`,`label`,`createdBy`,`createdDate`,`modifiedBy`,`modifiedDate`) VALUES ('pacman.auto.fix.mail.subject.Azure_Check_Public_Access_For_Storage_Account','PaladinCloud autofix action - Public access to azure storage account revoked','application','prd','latest',NULL,NULL,NULL,NULL);
INSERT IGNORE INTO pac_config_properties (`cfkey`,`value`,`application`,`profile`,`label`,`createdBy`,`createdDate`,`modifiedBy`,`modifiedDate`) VALUES ('pacman.autofix.fix.type.Azure_Check_Public_Access_For_Storage_Account','silent','application','prd','latest',NULL,NULL,NULL,NULL);
INSERT IGNORE INTO pac_config_properties (`cfkey`,`value`,`application`,`profile`,`label`,`createdBy`,`createdDate`,`modifiedBy`,`modifiedDate`) VALUES ('pacman.autofix.contact.Azure_Check_Public_Access_For_Storage_Account','dheeraj.kholia@paladincloud.io','application','prd','latest',NULL,NULL,NULL,NULL);


INSERT IGNORE INTO pac_config_properties (`cfkey`,`value`,`application`,`profile`,`label`,`createdBy`,`createdDate`,`modifiedBy`,`modifiedDate`) VALUES ('pacman.autofix.rule.post.fix.message.GCP_kms_public_access_rule','PaladinCloud has now automatically revoked the public access to GCP KMS key (<b> ${RESOURCE_ID} </b>) created by you as it was a violation','application','prd','latest',NULL,NULL,NULL,NULL);
INSERT IGNORE INTO pac_config_properties (`cfkey`,`value`,`application`,`profile`,`label`,`createdBy`,`createdDate`,`modifiedBy`,`modifiedDate`) VALUES ('pacman.auto.fix.mail.template.columns.GCP_kms_public_access_rule','Resource Id,Project,Name','application','prd','latest',NULL,NULL,NULL,NULL);
INSERT IGNORE INTO pac_config_properties (`cfkey`,`value`,`application`,`profile`,`label`,`createdBy`,`createdDate`,`modifiedBy`,`modifiedDate`) VALUES ('pacman.auto.fix.common.email.notifications.GCP_kms_public_access_rule','commonTemplate','application','prd','latest',NULL,NULL,NULL,NULL);
INSERT IGNORE INTO pac_config_properties (`cfkey`,`value`,`application`,`profile`,`label`,`createdBy`,`createdDate`,`modifiedBy`,`modifiedDate`) VALUES ('pacman.autofix.fix.notify.GCP_kms_public_access_rule','dheeraj.kholia@paladincloud.io','application','prd','latest',NULL,NULL,NULL,NULL);
INSERT IGNORE INTO pac_config_properties (`cfkey`,`value`,`application`,`profile`,`label`,`createdBy`,`createdDate`,`modifiedBy`,`modifiedDate`) VALUES ('pacman.auto.fix.mail.subject.GCP_kms_public_access_rule','PaladinCloud autofix action - Public access to GCP KMS key revoked','application','prd','latest',NULL,NULL,NULL,NULL);
INSERT IGNORE INTO pac_config_properties (`cfkey`,`value`,`application`,`profile`,`label`,`createdBy`,`createdDate`,`modifiedBy`,`modifiedDate`) VALUES ('pacman.autofix.fix.type.GCP_kms_public_access_rule','silent','application','prd','latest',NULL,NULL,NULL,NULL);
INSERT IGNORE INTO pac_config_properties (`cfkey`,`value`,`application`,`profile`,`label`,`createdBy`,`createdDate`,`modifiedBy`,`modifiedDate`) VALUES ('pacman.autofix.contact.GCP_kms_public_access_rule','dheeraj.kholia@paladincloud.io','application','prd','latest',NULL,NULL,NULL,NULL);


INSERT IGNORE INTO pac_config_properties (cfkey,value,application,profile,label,createdBy,createdDate,modifiedBy,modifiedDate) VALUES ('credential.file.path','/home/ec2-user/credential','inventory','prd','latest',null,null,null,null);
INSERT IGNORE INTO pac_config_properties (cfkey,value,application,profile,label,createdBy,createdDate,modifiedBy,modifiedDate) VALUES ('credential.file.path','/home/ec2-user/credential','batch','prd','latest',null,null,null,null);
INSERT IGNORE INTO pac_config_properties (cfkey,value,application,profile,label,createdBy,createdDate,modifiedBy,modifiedDate) VALUES ('secret.manager.path','paladincloud/secret','inventory','prd','latest',null,null,null,null);
INSERT IGNORE INTO pac_config_properties (cfkey,value,application,profile,label,createdBy,createdDate,modifiedBy,modifiedDate) VALUES ('secret.manager.path','paladincloud/secret','batch','prd','latest',null,null,null,null);
INSERT IGNORE INTO pac_config_properties (cfkey,value,application,profile,label,createdBy,createdDate,modifiedBy,modifiedDate) VALUES ('s3.cred.data',concat(@CREDENTIAL_DIR,''),'batch','prd','latest',null,null,null,null);
INSERT IGNORE INTO pac_config_properties (cfkey,value,application,profile,label,createdBy,createdDate,modifiedBy,modifiedDate) VALUES ('cloud-provider','aws','admin-service','prd','latest',null,null,null,null);


INSERT IGNORE INTO `pac_config_properties` (`cfkey`, `value`, `application`, `profile`, `label`, `createdBy`, `createdDate`, `modifiedBy`, `modifiedDate`) values('autofix.allowlist.accounts.UnusedElasticIpRule_version-1_UnusedElasticIpRule_elasticip','','rule','prd','latest',NULL,NULL,NULL,NULL);
INSERT IGNORE INTO `pac_config_properties` (`cfkey`, `value`, `application`, `profile`, `label`, `createdBy`, `createdDate`, `modifiedBy`, `modifiedDate`) values('pacman.autofix.contact.UnusedElasticIpRule_version-1_UnusedElasticIpRule_elasticip','','rule','prd','latest',NULL,NULL,NULL,NULL);
INSERT IGNORE INTO `pac_config_properties` (`cfkey`, `value`, `application`, `profile`, `label`, `createdBy`, `createdDate`, `modifiedBy`, `modifiedDate`) values('pacman.autofix.fix.type.UnusedElasticIpRule_version-1_UnusedElasticIpRule_elasticip','silent','rule','prd','latest',NULL,NULL,NULL,NULL);
INSERT IGNORE INTO `pac_config_properties` (`cfkey`, `value`, `application`, `profile`, `label`, `createdBy`, `createdDate`, `modifiedBy`, `modifiedDate`) values('pacman.auto.fix.mail.subject.UnusedElasticIpRule_version-1_UnusedElasticIpRule_elasticip','PaladinCloud - AWS Unassociated Elastic IP Addresses Auto Delete Report','rule','prd','latest',NULL,NULL,NULL,NULL);
INSERT IGNORE INTO `pac_config_properties` (`cfkey`, `value`, `application`, `profile`, `label`, `createdBy`, `createdDate`, `modifiedBy`, `modifiedDate`) values('pacman.autofix.fix.notify.UnusedElasticIpRule_version-1_UnusedElasticIpRule_elasticip','','rule','prd','latest',NULL,NULL,NULL,NULL);
INSERT IGNORE INTO `pac_config_properties` (`cfkey`, `value`, `application`, `profile`, `label`, `createdBy`, `createdDate`, `modifiedBy`, `modifiedDate`) values('pacman.autofix.issue.creation.time.elapsed.UnusedElasticIpRule_version-1_UnusedElasticIpRule_elasticip','72','rule','prd','latest',NULL,NULL,NULL,NULL);
INSERT IGNORE INTO `pac_config_properties` (`cfkey`, `value`, `application`, `profile`, `label`, `createdBy`, `createdDate`, `modifiedBy`, `modifiedDate`) values('pacman.autofix.rule.post.fix.message.UnusedElasticIpRule_version-1_UnusedElasticIpRule_elasticip','PaladinCloud has now automatically deleted the following list of Unassociated Elastic IP Addresses','rule','prd','latest',NULL,NULL,NULL,NULL);
INSERT IGNORE INTO `pac_config_properties` (`cfkey`, `value`, `application`, `profile`, `label`, `createdBy`, `createdDate`, `modifiedBy`, `modifiedDate`) values('pacman.auto.fix.mail.template.columns.UnusedElasticIpRule_version-1_UnusedElasticIpRule_elasticip','Resource Id,Account Id,Region,Allocation Id','rule','prd','latest',NULL,NULL,NULL,NULL);
INSERT IGNORE INTO `pac_config_properties` (`cfkey`, `value`, `application`, `profile`, `label`, `createdBy`, `createdDate`, `modifiedBy`, `modifiedDate`) values('pacman.auto.fix.common.email.notifications.UnusedElasticIpRule_version-1_UnusedElasticIpRule_elasticip','commonTemplate','rule','prd','latest',NULL,NULL,NULL,NULL);
INSERT IGNORE INTO `pac_config_properties` (`cfkey`, `value`, `application`, `profile`, `label`, `createdBy`, `createdDate`, `modifiedBy`, `modifiedDate`) VALUES('service.url.vulnerability',concat(@PACMAN_HOST_NAME,'/api/vulnerability'),'api','prd','latest',NULL,NULL,NULL,NULL);
INSERT IGNORE INTO pac_config_properties(`cfkey`,`value`,`application`,`profile`,`label`,`createdBy`,`createdDate`,`modifiedBy`,`modifiedDate`) VALUES ('api.services[6].name','Vulnerability Service','api','prd','latest',NULL,NULL,NULL,NULL);
INSERT IGNORE INTO pac_config_properties(`cfkey`,`value`,`application`,`profile`,`label`,`createdBy`,`createdDate`,`modifiedBy`,`modifiedDate`) VALUES ('api.services[6].url','${PACMAN_HOST_NAME:http://localhost:8080}/api/vulnerability/v2/api-docs','api','prd','latest',NULL,NULL,NULL,NULL);
INSERT IGNORE INTO pac_config_properties(`cfkey`,`value`,`application`,`profile`,`label`,`createdBy`,`createdDate`,`modifiedBy`,`modifiedDate`) VALUES ('api.services[6].version','2','api','prd','latest',NULL,NULL,NULL,NULL);
INSERT IGNORE INTO pac_config_properties (`cfkey`,`value`,`application`,`profile`,`label`,`createdBy`,`createdDate`,`modifiedBy`,`modifiedDate`) VALUES ('server.servlet.context-path','/api/vulnerability','vulnerability-service','prd','latest',NULL,NULL,NULL,NULL);
INSERT IGNORE INTO pac_config_properties (`cfkey`,`value`,`application`,`profile`,`label`,`createdBy`,`createdDate`,`modifiedBy`,`modifiedDate`) VALUES ('qualys_info',concat(@QUALYS_INFO,''),'qualys-enricher','prd','latest',NULL,NULL,NULL,NULL);
INSERT IGNORE INTO pac_config_properties (`cfkey`,`value`,`application`,`profile`,`label`,`createdBy`,`createdDate`,`modifiedBy`,`modifiedDate`) VALUES ('qualys_api_url',concat(@QUALYS_API_URL,''),'qualys-enricher','prd','latest',NULL,NULL,NULL,NULL);
INSERT IGNORE INTO pac_config_properties (`cfkey`,`value`,`application`,`profile`,`label`,`createdBy`,`createdDate`,`modifiedBy`,`modifiedDate`) VALUES ('s3.data','azure-inventory','azure-discovery','prd','latest',NULL,NULL,NULL,NULL);
INSERT IGNORE INTO pac_config_properties (`cfkey`,`value`,`application`,`profile`,`label`,`createdBy`,`createdDate`,`modifiedBy`,`modifiedDate`) VALUES ('s3.processed','backup-azure','azure-discovery','prd','latest',NULL,NULL,NULL,NULL);
DELETE IGNORE FROM pac_config_properties where cfKey = 'azure.credentials';
INSERT IGNORE INTO pac_config_properties (`cfkey`,`value`,`application`,`profile`,`label`,`createdBy`,`createdDate`,`modifiedBy`,`modifiedDate`) VALUES ('azure.credentials',concat(@AZURE_CREDENTIALS,''),'azure-discovery','prd','latest',NULL,NULL,NULL,NULL);
INSERT IGNORE INTO pac_config_properties (`cfkey`,`value`,`application`,`profile`,`label`,`createdBy`,`createdDate`,`modifiedBy`,`modifiedDate`) VALUES ('s3.data','gcp-inventory','gcp-discovery','prd','latest',NULL,NULL,NULL,NULL);
INSERT IGNORE INTO pac_config_properties (`cfkey`,`value`,`application`,`profile`,`label`,`createdBy`,`createdDate`,`modifiedBy`,`modifiedDate`) VALUES ('s3.processed','backup-gcp','gcp-discovery','prd','latest',NULL,NULL,NULL,NULL);
DELETE IGNORE FROM pac_config_properties where cfKey='gcp.credentials';
INSERT IGNORE INTO pac_config_properties (`cfkey`,`value`,`application`,`profile`,`label`,`createdBy`,`createdDate`,`modifiedBy`,`modifiedDate`) VALUES ('gcp.credentials',concat(@GCP_CREDENTIALS,''),'gcp-discovery','prd','latest',NULL,NULL,NULL,NULL);

DELETE IGNORE FROM pac_config_properties where cfKey='current-release';
INSERT IGNORE INTO pac_config_properties (`cfkey`,`value`,`application`,`profile`,`label`,`createdBy`,`createdDate`,`modifiedBy`,`modifiedDate`) VALUES ('current-release',concat(@CURRENT_RELEASE,''),'application','prd','latest',NULL,NULL,NULL,NULL);


INSERT IGNORE INTO pac_config_properties (`cfkey`,`value`,`application`,`profile`,`label`,`createdBy`,`createdDate`,`modifiedBy`,`modifiedDate`) VALUES ('vulnerability.application.occurance','severity,_resourceid,pciflag,_vulnage,vulntype,title,classification,_firstFound,_lastFound,qid,patchable,category','vulnerability-service','prd','latest',NULL,NULL,NULL,NULL);
INSERT IGNORE INTO pac_config_properties (`cfkey`,`value`,`application`,`profile`,`label`,`createdBy`,`createdDate`,`modifiedBy`,`modifiedDate`) VALUES ('vulnerability.application.resourcedetails','tags.Name,accountid,accountname,tags.Environment,tags.Application,privateipaddress,instanceid,region,availabilityzone,imageid,platform,privatednsname,instancetype,subnetid,_resourceid,publicipaddress,publicdnsname,vpcid','vulnerability-service','prd','latest',NULL,NULL,NULL,NULL);
INSERT IGNORE INTO pac_config_properties (`cfkey`,`value`,`application`,`profile`,`label`,`createdBy`,`createdDate`,`modifiedBy`,`modifiedDate`) VALUES ('vulnerability.application.resourcedetailsboth','tags.Name,tags.Environment,tags.Application,ip_address,privateipaddress,_entitytype,_resourceid','vulnerability-service','prd','latest',NULL,NULL,NULL,NULL);
INSERT IGNORE INTO pac_config_properties (`cfkey`,`value`,`application`,`profile`,`label`,`createdBy`,`createdDate`,`modifiedBy`,`modifiedDate`) VALUES ('application.prefix',concat(@EVENT_BRIDGE_PREFIX,''),'application','prd','latest',NULL,NULL,NULL,NULL);
INSERT IGNORE INTO pac_config_properties (`cfkey`,`value`,`application`,`profile`,`label`,`createdBy`,`createdDate`,`modifiedBy`,`modifiedDate`) VALUES ('aqua_client_domain_url',concat(@AQUA_CLIENT_DOMAIN_URL,''),'aqua-enricher','prd','latest',NULL,NULL,NULL,NULL);
INSERT IGNORE INTO pac_config_properties (`cfkey`,`value`,`application`,`profile`,`label`,`createdBy`,`createdDate`,`modifiedBy`,`modifiedDate`) VALUES ('default_page_size',concat(@AQUA_API_DEFAULT_PAGE_SIZE,''),'aqua-enricher','prd','latest',NULL,NULL,NULL,NULL);
INSERT IGNORE INTO pac_config_properties (`cfkey`,`value`,`application`,`profile`,`label`,`createdBy`,`createdDate`,`modifiedBy`,`modifiedDate`) VALUES ('aqua_image_vul_query_params',concat(@AQUA_IMAGE_VULNERABILITY_QUERY_PARAMS,''),'qualys-enricher','prd','latest',NULL,NULL,NULL,NULL);
INSERT IGNORE INTO pac_config_properties (`cfkey`,`value`,`application`,`profile`,`label`,`createdBy`,`createdDate`,`modifiedBy`,`modifiedDate`) VALUES ('aqua_api_url',concat(@AQUA_API_URL,''),'aqua-enricher','prd','latest',NULL,NULL,NULL,NULL);
INSERT IGNORE INTO pac_config_properties (`cfkey`,`value`,`application`,`profile`,`label`,`createdBy`,`createdDate`,`modifiedBy`,`modifiedDate`) VALUES ('aqua_username',concat(@AQUA_USERNAME,''),'aqua-enricher','prd','latest',NULL,NULL,NULL,NULL);
INSERT IGNORE INTO pac_config_properties (`cfkey`,`value`,`application`,`profile`,`label`,`createdBy`,`createdDate`,`modifiedBy`,`modifiedDate`) VALUES ('aqua_password',concat(@AQUA_PASSWORD,''),'aqua-enricher','prd','latest',NULL,NULL,NULL,NULL);

INSERT IGNORE INTO pac_config_properties (cfkey,value,application,profile,label,createdBy,createdDate,modifiedBy,modifiedDate) VALUES ('credential.file.path','/home/ec2-user/credential','inventory','prd','latest',null,null,null,null);
INSERT IGNORE INTO pac_config_properties (cfkey,value,application,profile,label,createdBy,createdDate,modifiedBy,modifiedDate) VALUES ('secret.manager.path','paladincloud/secret','inventory','prd','latest',null,null,null,null);
INSERT IGNORE INTO pac_config_properties (cfkey,value,application,profile,label,createdBy,createdDate,modifiedBy,modifiedDate) VALUES ('s3.cred.data',concat(@CREDENTIAL_DIR,''),'batch','prd','latest',null,null,null,null);
INSERT IGNORE INTO pac_config_properties(`cfkey`,`value`,`application`,`profile`,`label`,`createdBy`,`createdDate`,`modifiedBy`,`modifiedDate`)
 VALUES ('policy-engine.invoke.url','submitRuleExecutionJob','compliance-service','prd','latest',NULL,NULL,NULL,NULL);
INSERT IGNORE INTO `pac_config_properties` (`cfkey`, `value`, `application`, `profile`, `label`, `createdBy`, `createdDate`, `modifiedBy`, `modifiedDate`) values('pacman.host',concat(@PACMAN_HOST_NAME,''),'application','prd','latest',NULL,NULL,NULL,NULL);
INSERT IGNORE INTO `Recommendation_Mappings`(`checkId`,`type`,`resourceInfo`,`_resourceId`,`monthlySavingsField`) values ('H7IgTzjTYb','volume','Volume ID','volumeid',NULL),('DAvU99Dc4C','volume','Volume ID','volumeid','Monthly Storage Cost'),('Qch7DwouX1','ec2','Instance ID','instanceid','Estimated Monthly Savings'),('1iG5NDGVre','sg','Security Group ID','groupid',NULL),('HCP4007jGY','sg','Security Group ID','groupid',NULL),('BueAdJ7NrP','s3','Bucket Name','name',NULL),('iqdCTZKCUp','classicelb','Load Balancer Name','loadbalancername',NULL),('R365s2Qddf','s3','Bucket Name','name',NULL),('Pfx0RwqBli','s3','Bucket Name','name',NULL),('a2sEc6ILx','classicelb','Load Balancer Name','loadbalancername',NULL),('xdeXZKIUy','classicelb','Load Balancer Name','loadbalancername',NULL),('CLOG40CDO8','asg','Auto Scaling Group Name','autoscalinggroupname',NULL),('7qGXsKIUw','classicelb','Load Balancer Name','loadbalancername',NULL),('hjLMh88uM8','classicelb','Load Balancer Name','loadbalancername','Estimated Monthly Savings'),('DqdJqYeRm5','iamuser','IAM User','username',NULL),('j3DFqYTe29','ec2','Instance ID','instanceid',NULL),('f2iK5R6Dep','rdsdb','DB Instance','dbinstanceidentifier',NULL),('1MoPEMsKx6','reservedinstance','Instance Type','instancetype','Estimated Monthly Savings'),('Ti39halfu8','rdsdb','DB Instance Name','dbinstanceidentifier','Estimated Monthly Savings (On Demand)'),('Wnwm9Il5bG','ec2','Instance ID','instanceid',NULL),('V77iOLlBqz','ec2','Instance ID','instanceid',NULL),('Z4AUBRNSmz','elasticip','IP Address','publicip',NULL),('8CNsSllI5v','asg','Auto Scaling Group Name','autoscalinggroupname',NULL),('N420c450f2','cloudfront','Distribution ID','id',NULL),('TyfdMXG69d','ec2','Instance ID','instanceid',NULL),('tfg86AVHAZ','sg','Group ID','groupid',NULL),('yHAGQJV9K5','ec2','Instance ID','instanceid',NULL),('S45wrEXrLz','vpnconnection','VPN ID','vpnconnectionid',NULL),('PPkZrjsH2q','volume','Volume ID','volumeid',NULL),('opQPADkZvH','rdsdb','DB Instance','dbinstanceidentifier',NULL),('796d6f3D83','s3','Bucket Name','name',NULL),('G31sQ1E9U','redshift','Cluster','clusteridentifier','Estimated Monthly Savings'),('xSqX82fQu','classicelb','Load Balancer Name','loadbalancername',NULL),('ZRxQlPsb6c','ec2','Instance ID','instanceid',NULL),('N430c450f2','cloudfront','Distribution ID','id',NULL),('4g3Nt5M1Th','virtualinterface','Gateway ID','virtualgatewayid',NULL),('0t121N1Ty3','directconnect','Connection ID','connectionid',NULL),('N425c450f2','cloudfront','Distribution ID','id',NULL),('xuy7H1avtl','rdscluster','Cluster','dbclusteridentifier',NULL),('1e93e4c0b5','reservedinstance','Reserved Instance ID','instanceid','Estimated Monthly Savings'),('51fC20e7I2','route53','Hosted Zone ID','hostedZoneId',NULL),('cF171Db240','route53','Hosted Zone ID','hostedZoneId',NULL),('Cb877eB72b','route53','Hosted Zone ID','hostedZoneId',NULL),('b73EEdD790','route53','Hosted Zone ID','hostedZoneId',NULL),('C056F80cR3','route53','Hosted Zone ID','hostedZoneId',NULL),('B913Ef6fb4','route53','Hosted Zone ID','hostedZoneId',NULL);

INSERT IGNORE INTO `CloudNotification_mapping`(`NotificationId`,`eventType`,`resourceIdKey`,`resourceIdVal`,`esIndex`,`phdEntityKey`) values ('02BUF','CLOUDTRAIL','_resourceid.keyword','_resourceid','cloudtrl','entityvalue'),('4GIGN','S3','_resourceid.keyword','_resourceid','s3','entityvalue'),('5U846','ELASTICSEARCH','arn.keyword','arn','elasticsearch','entityvalue'),('DI3Q3','SQS','_resourceid.keyword','_resourceid','sqs','entityvalue'),('FZC49','VPN','vpnconnectionid.keyword','vpnconnectionid','vpnconnection','entityvalue'),('G30R7','KMS','_resourceid.keyword','_resourceid','kms','entityvalue'),('G4AIH','RDS','dbinstanceidentifier.keyword','dbinstanceidentifier','rdsdb','entityvalue'),('HL28B','EC2','_resourceid.keyword','_resourceid','ec2','entityvalue'),('KBDY2','DIRECTCONNECT','_resourceid.keyword','_resourceid','directconnect','entityvalue'),('LPB7Z','LAMBDA','_resourceid.keyword','_resourceid','lambda','entityvalue'),('PKI3S','CONFIG','_resourceid.keyword','_resourceid','config','entityvalue'),('S2QIA','REDSHIFT','clusteridentifier.keyword','clusteridentifier','redshift','entityvalue'),('W45AP','IAM','arn.keyword','arn','iamuser','entityvalue'),('X9GYT','VPC','_resourceid.keyword','_resourceid','vpc','entityvalue'),('YCFSX','CLOUDFRONT','_resourceid.keyword','_resourceid','cloudfront','entityvalue'),('YGS02','DYNAMODB','tablearn.keyword','tablearn','dynamodb','entityvalue'),('YGS03','MQ','_resourceid.keyword','_resourceid','mq','entityvalue'),('YGS05','APIGATEWAY','_resourceid.keyword','_resourceid','apigtw','entityvalue');






INSERT IGNORE INTO `cf_JobScheduler`(`jobId`,`jobUUID`,`jobName`,`jobType`,`jobParams`,`jobFrequency`,`jobExecutable`,`jobArn`,`status`,`userId`,`createdDate`,`modifiedDate`) values ('pacbot-AWS-Data-Collector','pacbot-AWS-Data-Collector','AWS-Data-Collector','jar','','0 0/2 * * ? *','inventory-fetch.jar',concat('arn:aws:events:',@region,':',@account,':rule/pacbot-AWS-Data-Collector'),'ENABLED','20433','2017-10-17 00:18:43','2017-11-03 12:48:23');
INSERT IGNORE INTO `cf_JobScheduler`(`jobId`,`jobUUID`,`jobName`,`jobType`,`jobParams`,`jobFrequency`,`jobExecutable`,`jobArn`,`status`,`userId`,`createdDate`,`modifiedDate`) values('pacbot-aws-redshift-es-data-shipper','pacbot-aws-redshift-es-data-shipper','aws-redshift-es-data-shipper','jar','','30 0/2 * * ? *','data-shipper.jar',concat('arn:aws:events:',@region,':',@account,':rule/pacbot-aws-redshift-es-data-shipper'),'ENABLED','20433','2017-11-02 23:56:53','2017-11-03 12:48:49');



/* This is to delete row with below entry as we need only entry with application='application' which is added in insert query*/
DELETE FROM pac_config_properties WHERE cfkey = 'tagging.mandatoryTags' AND application='api' AND profile='prd' AND label='latest';


/* Update query for updating the description field of pac_config_key_metadata */
UPDATE `pacmandata`.`pac_config_key_metadata` SET `description` = 'Admin Role key' WHERE `cfkey` = 'admin.api-role';
UPDATE `pacmandata`.`pac_config_key_metadata` SET `description` = 'Time in for admin push notification polling' WHERE `cfkey` = 'admin.push.notification.pollinterval.milliseconds';
UPDATE `pacmandata`.`pac_config_key_metadata` SET `description` = 'Slack handle for auth service' WHERE `cfkey` = 'api.auth.owner.slack.handle';
UPDATE `pacmandata`.`pac_config_key_metadata` SET `description` = 'Configuration for backing up asset' WHERE `cfkey` = 'api.backup.asset.config';
UPDATE `pacmandata`.`pac_config_key_metadata` SET `description` = 'Getting last action by rule engine' WHERE `cfkey` = 'api.getlastaction';
UPDATE `pacmandata`.`pac_config_key_metadata` SET `description` = 'Posting last action by rule engine' WHERE `cfkey` = 'api.postlastaction';
UPDATE `pacmandata`.`pac_config_key_metadata` SET `description` = 'Reactors URL for register' WHERE `cfkey` = 'api.register.reactors.url';
UPDATE `pacmandata`.`pac_config_key_metadata` SET `description` = 'Resource creation date key' WHERE `cfkey` = 'api.resource.creationdate';
UPDATE `pacmandata`.`pac_config_key_metadata` SET `description` = 'Name of API service at 0 position' WHERE `cfkey` = 'api.services[0].name';
UPDATE `pacmandata`.`pac_config_key_metadata` SET `description` = 'URL of API service at 0 position' WHERE `cfkey` = 'api.services[0].url';
UPDATE `pacmandata`.`pac_config_key_metadata` SET `description` = 'Version of API service at 0 position' WHERE `cfkey` = 'api.services[0].version';
UPDATE `pacmandata`.`pac_config_key_metadata` SET `description` = 'Name of API service at 1 position' WHERE `cfkey` = 'api.services[1].name';
UPDATE `pacmandata`.`pac_config_key_metadata` SET `description` = 'URL of API service at 1 position' WHERE `cfkey` = 'api.services[1].url';
UPDATE `pacmandata`.`pac_config_key_metadata` SET `description` = 'Version of API service at 1 position' WHERE `cfkey` = 'api.services[1].version';
UPDATE `pacmandata`.`pac_config_key_metadata` SET `description` = 'Name of API service at 2 position' WHERE `cfkey` = 'api.services[2].name';
UPDATE `pacmandata`.`pac_config_key_metadata` SET `description` = 'URL of API service at 2 position' WHERE `cfkey` = 'api.services[2].url';
UPDATE `pacmandata`.`pac_config_key_metadata` SET `description` = 'Version of API service at 2 position' WHERE `cfkey` = 'api.services[2].version';
UPDATE `pacmandata`.`pac_config_key_metadata` SET `description` = 'Name of API service at 3 position' WHERE `cfkey` = 'api.services[3].name';
UPDATE `pacmandata`.`pac_config_key_metadata` SET `description` = 'URL of API service at 3 position' WHERE `cfkey` = 'api.services[3].url';
UPDATE `pacmandata`.`pac_config_key_metadata` SET `description` = 'Version of API service at 3 position' WHERE `cfkey` = 'api.services[3].version';
UPDATE `pacmandata`.`pac_config_key_metadata` SET `description` = 'Name of API service at 4 position' WHERE `cfkey` = 'api.services[4].name';
UPDATE `pacmandata`.`pac_config_key_metadata` SET `description` = 'URL of API service at 4 position' WHERE `cfkey` = 'api.services[4].url';
UPDATE `pacmandata`.`pac_config_key_metadata` SET `description` = 'Version of API service at 4 position' WHERE `cfkey` = 'api.services[4].version';
UPDATE `pacmandata`.`pac_config_key_metadata` SET `description` = 'Name of API service at 5 position' WHERE `cfkey` = 'api.services[5].name';
UPDATE `pacmandata`.`pac_config_key_metadata` SET `description` = 'URL of API service at 5 position' WHERE `cfkey` = 'api.services[5].url';
UPDATE `pacmandata`.`pac_config_key_metadata` SET `description` = 'Version of API service at 5 position' WHERE `cfkey` = 'api.services[5].version';
UPDATE `pacmandata`.`pac_config_key_metadata` SET `description` = 'Name of API service at 6 position' WHERE `cfkey` = 'api.services[6].name';
UPDATE `pacmandata`.`pac_config_key_metadata` SET `description` = 'URL of API service at 6 position' WHERE `cfkey` = 'api.services[6].url';
UPDATE `pacmandata`.`pac_config_key_metadata` SET `description` = 'Version of API service at 6 position' WHERE `cfkey` = 'api.services[6].version';
UPDATE `pacmandata`.`pac_config_key_metadata` SET `description` = 'Domains to be allowed in CORS' WHERE `cfkey` = 'application.cors.allowed.domains';
UPDATE `pacmandata`.`pac_config_key_metadata` SET `description` = 'Authication type' WHERE `cfkey` = 'auth.active';
UPDATE `pacmandata`.`pac_config_key_metadata` SET `description` = 'Autofix cut off date' WHERE `cfkey` = 'autofix.cufoff.date';
UPDATE `pacmandata`.`pac_config_key_metadata` SET `description` = 'Accounts for applying autofix for EC2WithPublicIPAccess_version-1_Ec2WithPublicAccess_ec2' WHERE `cfkey` = 'autofix.allowlist.accounts.EC2WithPublicIPAccess_version-1_Ec2WithPublicAccess_ec2';
UPDATE `pacmandata`.`pac_config_key_metadata` SET `description` = 'Accounts for applying autofix for ElasticSearchPublicAccess_version-1_ElasticSearchPublicAccessRule_elasticsearch' WHERE `cfkey` = 'autofix.allowlist.accounts.ElasticSearchPublicAccess_version-1_ElasticSearchPublicAccessRule_elasticsearch';
UPDATE `pacmandata`.`pac_config_key_metadata` SET `description` = 'Accounts for applying autofix for ElbWithPublicAccess_version-1_ApplicationElbWithPublicAccess_appelb' WHERE `cfkey` = 'autofix.allowlist.accounts.ElbWithPublicAccess_version-1_ApplicationElbWithPublicAccess_appelb';
UPDATE `pacmandata`.`pac_config_key_metadata` SET `description` = 'Accounts for applying autofix for ElbWithPublicAccess_version-1_ClassicElbWithPublicAccess_classicelb' WHERE `cfkey` = 'autofix.allowlist.accounts.ElbWithPublicAccess_version-1_ClassicElbWithPublicAccess_classicelb';
UPDATE `pacmandata`.`pac_config_key_metadata` SET `description` = 'Accounts for applying autofix for RedShiftPublicAccess_version-1_RedShiftPublicAccess_redshift' WHERE `cfkey` = 'autofix.allowlist.accounts.RedShiftPublicAccess_version-1_RedShiftPublicAccess_redshift';
UPDATE `pacmandata`.`pac_config_key_metadata` SET `description` = 'Accounts for applying autofix for S3GlobalAccess_version-1_S3BucketShouldnotpubliclyaccessble_s3' WHERE `cfkey` = 'autofix.allowlist.accounts.S3GlobalAccess_version-1_S3BucketShouldnotpubliclyaccessble_s3';
UPDATE `pacmandata`.`pac_config_key_metadata` SET `description` = 'Accounts for applying autofix for Unused-Security-group_version-1_UnusedSecurityGroup_sg' WHERE `cfkey` = 'autofix.allowlist.accounts.Unused-Security-group_version-1_UnusedSecurityGroup_sg';
UPDATE `pacmandata`.`pac_config_key_metadata` SET `description` = 'Accounts for applying autofix for UnusedElasticIpRule_version-1_UnusedElasticIpRule_elasticip' WHERE `cfkey` = 'autofix.allowlist.accounts.UnusedElasticIpRule_version-1_UnusedElasticIpRule_elasticip';
UPDATE `pacmandata`.`pac_config_key_metadata` SET `description` = 'Accounts for applying autofix for rdsdb_version-1_RdsDbPublicAccess_rdsdb' WHERE `cfkey` = 'autofix.allowlist.accounts.rdsdb_version-1_RdsDbPublicAccess_rdsdb';
UPDATE `pacmandata`.`pac_config_key_metadata` SET `description` = 'AWS access key' WHERE `cfkey` = 'aws.access-key';
UPDATE `pacmandata`.`pac_config_key_metadata` SET `description` = 'AWS secret key' WHERE `cfkey` = 'aws.secret-key';
UPDATE `pacmandata`.`pac_config_key_metadata` SET `description` = 'Azure active directory client id' WHERE `cfkey` = 'azure.activedirectory.client-id';
UPDATE `pacmandata`.`pac_config_key_metadata` SET `description` = 'Azure active directory client secret' WHERE `cfkey` = 'azure.activedirectory.client-secret';
UPDATE `pacmandata`.`pac_config_key_metadata` SET `description` = 'Azure active directory scope' WHERE `cfkey` = 'azure.activedirectory.scope';
UPDATE `pacmandata`.`pac_config_key_metadata` SET `description` = 'Azure active directory scope description' WHERE `cfkey` = 'azure.activedirectory.scopeDesc';
UPDATE `pacmandata`.`pac_config_key_metadata` SET `description` = 'Azure active directory scope state' WHERE `cfkey` = 'azure.activedirectory.state';
UPDATE `pacmandata`.`pac_config_key_metadata` SET `description` = 'Azure active directory tenant-id' WHERE `cfkey` = 'azure.activedirectory.tenant-id';
UPDATE `pacmandata`.`pac_config_key_metadata` SET `description` = 'Azure active directory authorizeEndpoint' WHERE `cfkey` = 'azure.authorizeEndpoint';
UPDATE `pacmandata`.`pac_config_key_metadata` SET `description` = 'Azure active directory claims email' WHERE `cfkey` = 'azure.id-token.claims.email';
UPDATE `pacmandata`.`pac_config_key_metadata` SET `description` = 'Azure active directory claims first-name' WHERE `cfkey` = 'azure.id-token.claims.first-name';
UPDATE `pacmandata`.`pac_config_key_metadata` SET `description` = 'Azure active directory claims last-name' WHERE `cfkey` = 'azure.id-token.claims.last-name';
UPDATE `pacmandata`.`pac_config_key_metadata` SET `description` = 'Azure active directory claims user-id' WHERE `cfkey` = 'azure.id-token.claims.user-id';
UPDATE `pacmandata`.`pac_config_key_metadata` SET `description` = 'Azure active directory claims user-name' WHERE `cfkey` = 'azure.id-token.claims.user-name';
UPDATE `pacmandata`.`pac_config_key_metadata` SET `description` = 'Azure active directory issuer' WHERE `cfkey` = 'azure.issuer';
UPDATE `pacmandata`.`pac_config_key_metadata` SET `description` = 'Azure active directory public-key' WHERE `cfkey` = 'azure.public-key';
UPDATE `pacmandata`.`pac_config_key_metadata` SET `description` = 'AWS base account' WHERE `cfkey` = 'base.account';
UPDATE `pacmandata`.`pac_config_key_metadata` SET `description` = 'AWS base region' WHERE `cfkey` = 'base.region';
UPDATE `pacmandata`.`pac_config_key_metadata` SET `description` = 'Branch max age' WHERE `cfkey` = 'branch.maxBranchAge';
UPDATE `pacmandata`.`pac_config_key_metadata` SET `description` = 'Cloud insight CORP password' WHERE `cfkey` = 'cloudinsights.corp-password';
UPDATE `pacmandata`.`pac_config_key_metadata` SET `description` = 'Cloud insight CORP user-id' WHERE `cfkey` = 'cloudinsights.corp-user-id';
UPDATE `pacmandata`.`pac_config_key_metadata` SET `description` = 'Cloud insight costurl' WHERE `cfkey` = 'cloudinsights.costurl';
UPDATE `pacmandata`.`pac_config_key_metadata` SET `description` = 'Cloud insight tokenurl' WHERE `cfkey` = 'cloudinsights.tokenurl';
UPDATE `pacmandata`.`pac_config_key_metadata` SET `description` = 'Accounts to allowlist for S3CreateBucketAndUpdateBucketPolicyReactor' WHERE `cfkey` = 'com.tmobile.pacman.reactors.impl.s3.S3CreateBucketAndUpdateBucketPolicyReactor.account.whitelist';
UPDATE `pacmandata`.`pac_config_key_metadata` SET `description` = 'Accounts to allowlist for SampleReactor' WHERE `cfkey` = 'com.tmobile.pacman.reactors.impl.sample.SampleReactor.account.whitelist';
UPDATE `pacmandata`.`pac_config_key_metadata` SET `description` = 'Accounts to allowlist for SampleReactor2' WHERE `cfkey` = 'com.tmobile.pacman.reactors.impl.sample.SampleReactor2.account.whitelist';
UPDATE `pacmandata`.`pac_config_key_metadata` SET `description` = 'Cron frequency for weekly-report-sync-trigger' WHERE `cfkey` = 'cron.frequency.weekly-report-sync-trigger';
UPDATE `pacmandata`.`pac_config_key_metadata` SET `description` = 'Date format across the application' WHERE `cfkey` = 'date.format';
UPDATE `pacmandata`.`pac_config_key_metadata` SET `description` = 'Date range age across the application' WHERE `cfkey` = 'days-range.age';
UPDATE `pacmandata`.`pac_config_key_metadata` SET `description` = 'Delete Sg Tag' WHERE `cfkey` = 'deleteSgTag';
UPDATE `pacmandata`.`pac_config_key_metadata` SET `description` = 'AWS role where data discovery is done' WHERE `cfkey` = 'discovery.role';
UPDATE `pacmandata`.`pac_config_key_metadata` SET `description` = 'Elastic search admin host' WHERE `cfkey` = 'elastic-search.admin-host';
UPDATE `pacmandata`.`pac_config_key_metadata` SET `description` = 'Elastic search clusterName' WHERE `cfkey` = 'elastic-search.clusterName';
UPDATE `pacmandata`.`pac_config_key_metadata` SET `description` = 'Elastic search clusterName-heimdall' WHERE `cfkey` = 'elastic-search.clusterName-heimdall';
UPDATE `pacmandata`.`pac_config_key_metadata` SET `description` = 'Elastic search dev-ingest-host' WHERE `cfkey` = 'elastic-search.dev-ingest-host';
UPDATE `pacmandata`.`pac_config_key_metadata` SET `description` = 'Elastic search dev-ingest-port' WHERE `cfkey` = 'elastic-search.dev-ingest-port';
UPDATE `pacmandata`.`pac_config_key_metadata` SET `description` = 'Elastic search host' WHERE `cfkey` = 'elastic-search.host';
UPDATE `pacmandata`.`pac_config_key_metadata` SET `description` = 'Elastic search host-heimdall' WHERE `cfkey` = 'elastic-search.host-heimdall';
UPDATE `pacmandata`.`pac_config_key_metadata` SET `description` = 'Elastic search port' WHERE `cfkey` = 'elastic-search.port';
UPDATE `pacmandata`.`pac_config_key_metadata` SET `description` = 'Elastic search port-admin' WHERE `cfkey` = 'elastic-search.port-admin';
UPDATE `pacmandata`.`pac_config_key_metadata` SET `description` = 'Elastic search port-admin-heimdall' WHERE `cfkey` = 'elastic-search.port-admin-heimdall';
UPDATE `pacmandata`.`pac_config_key_metadata` SET `description` = 'Elastic search port-heimdall' WHERE `cfkey` = 'elastic-search.port-heimdall';
UPDATE `pacmandata`.`pac_config_key_metadata` SET `description` = 'Elastic search update-clusterName' WHERE `cfkey` = 'elastic-search.update-clusterName';
UPDATE `pacmandata`.`pac_config_key_metadata` SET `description` = 'Elastic search update-host' WHERE `cfkey` = 'elastic-search.update-host';
UPDATE `pacmandata`.`pac_config_key_metadata` SET `description` = 'Elastic search update-port' WHERE `cfkey` = 'elastic-search.update-port';
UPDATE `pacmandata`.`pac_config_key_metadata` SET `description` = 'Email banner name' WHERE `cfkey` = 'email.banner';
UPDATE `pacmandata`.`pac_config_key_metadata` SET `description` = 'Endpoints refresh required' WHERE `cfkey` = 'endpoints.refresh.sensitive';
UPDATE `pacmandata`.`pac_config_key_metadata` SET `description` = 'Logging level that needs to be logged in ES' WHERE `cfkey` = 'esLoggingLevel';
UPDATE `pacmandata`.`pac_config_key_metadata` SET `description` = 'Feature certificates are enabled or not' WHERE `cfkey` = 'features.certificate.enabled';
UPDATE `pacmandata`.`pac_config_key_metadata` SET `description` = 'Feature patching are enabled or not' WHERE `cfkey` = 'features.patching.enabled';
UPDATE `pacmandata`.`pac_config_key_metadata` SET `description` = 'Feature vulnerability are enabled or not' WHERE `cfkey` = 'features.vulnerability.enabled';
UPDATE `pacmandata`.`pac_config_key_metadata` SET `description` = 'File path for storing the inventory collected data' WHERE `cfkey` = 'file.path';
UPDATE `pacmandata`.`pac_config_key_metadata` SET `description` = 'Date format across the application' WHERE `cfkey` = 'formats.date';
UPDATE `pacmandata`.`pac_config_key_metadata` SET `description` = 'Heimdall host' WHERE `cfkey` = 'heimdall-host';
UPDATE `pacmandata`.`pac_config_key_metadata` SET `description` = 'Heimdall port' WHERE `cfkey` = 'heimdall-port';
UPDATE `pacmandata`.`pac_config_key_metadata` SET `description` = 'Hystrix command default execution isolation thread timeout In Milliseconds' WHERE `cfkey` = 'hystrix.command.default.execution.isolation.thread.timeoutInMilliseconds';
UPDATE `pacmandata`.`pac_config_key_metadata` SET `description` = 'Hystrix shareSecurityContext enabled' WHERE `cfkey` = 'hystrix.shareSecurityContext';
UPDATE `pacmandata`.`pac_config_key_metadata` SET `description` = 'Rule engine lambda action disabled or not' WHERE `cfkey` = 'job.lambda.action-disabled';
UPDATE `pacmandata`.`pac_config_key_metadata` SET `description` = 'Rule engine lambda action enabled or not' WHERE `cfkey` = 'job.lambda.action-enabled';
UPDATE `pacmandata`.`pac_config_key_metadata` SET `description` = 'Rule engine lambda function-arn value' WHERE `cfkey` = 'job.lambda.function-arn';
UPDATE `pacmandata`.`pac_config_key_metadata` SET `description` = 'Rule engine lambda function-name value' WHERE `cfkey` = 'job.lambda.function-name';
UPDATE `pacmandata`.`pac_config_key_metadata` SET `description` = 'Rule engine lambda principal value' WHERE `cfkey` = 'job.lambda.principal';
UPDATE `pacmandata`.`pac_config_key_metadata` SET `description` = 'Rule engine lambda target-id value' WHERE `cfkey` = 'job.lambda.target-id';
UPDATE `pacmandata`.`pac_config_key_metadata` SET `description` = 'Inventory collection bucket-name' WHERE `cfkey` = 'job.s3.bucket-name';
UPDATE `pacmandata`.`pac_config_key_metadata` SET `description` = 'LDAP AD domain name' WHERE `cfkey` = 'ldap.ad.domain';
UPDATE `pacmandata`.`pac_config_key_metadata` SET `description` = 'LDAP AD provider URL' WHERE `cfkey` = 'ldap.ad.provider-url';
UPDATE `pacmandata`.`pac_config_key_metadata` SET `description` = 'LDAP AD search-base' WHERE `cfkey` = 'ldap.ad.search-base';
UPDATE `pacmandata`.`pac_config_key_metadata` SET `description` = 'LDAP baseDn' WHERE `cfkey` = 'ldap.baseDn';
UPDATE `pacmandata`.`pac_config_key_metadata` SET `description` = 'LDAP connection timeout' WHERE `cfkey` = 'ldap.connectionTimeout';
UPDATE `pacmandata`.`pac_config_key_metadata` SET `description` = 'LDAP domain' WHERE `cfkey` = 'ldap.domain';
UPDATE `pacmandata`.`pac_config_key_metadata` SET `description` = 'LDAP host list' WHERE `cfkey` = 'ldap.hostList';
UPDATE `pacmandata`.`pac_config_key_metadata` SET `description` = 'LDAP authentication naming' WHERE `cfkey` = 'ldap.naming.authentication';
UPDATE `pacmandata`.`pac_config_key_metadata` SET `description` = 'LDAP context-factory naming' WHERE `cfkey` = 'ldap.naming.context-factory';
UPDATE `pacmandata`.`pac_config_key_metadata` SET `description` = 'LDAP NT domain' WHERE `cfkey` = 'ldap.nt.domain';
UPDATE `pacmandata`.`pac_config_key_metadata` SET `description` = 'LDAP NT provider-url' WHERE `cfkey` = 'ldap.nt.provider-url';
UPDATE `pacmandata`.`pac_config_key_metadata` SET `description` = 'LDAP NT search-base' WHERE `cfkey` = 'ldap.nt.search-base';
UPDATE `pacmandata`.`pac_config_key_metadata` SET `description` = 'LDAP port' WHERE `cfkey` = 'ldap.port';
UPDATE `pacmandata`.`pac_config_key_metadata` SET `description` = 'LDAP response timeout' WHERE `cfkey` = 'ldap.responseTimeout';
UPDATE `pacmandata`.`pac_config_key_metadata` SET `description` = 'Logging configuration' WHERE `cfkey` = 'logging.config';
UPDATE `pacmandata`.`pac_config_key_metadata` SET `description` = 'Logging console level' WHERE `cfkey` = 'logging.consoleLoggingLevel';
UPDATE `pacmandata`.`pac_config_key_metadata` SET `description` = 'ES host for logging' WHERE `cfkey` = 'logging.esHost';
UPDATE `pacmandata`.`pac_config_key_metadata` SET `description` = 'ES logging level' WHERE `cfkey` = 'logging.esLoggingLevel';
UPDATE `pacmandata`.`pac_config_key_metadata` SET `description` = 'ES port logging' WHERE `cfkey` = 'logging.esPort';
UPDATE `pacmandata`.`pac_config_key_metadata` SET `description` = 'Magenta skill cache name' WHERE `cfkey` = 'magenta.cache.name';
UPDATE `pacmandata`.`pac_config_key_metadata` SET `description` = 'Magenta skill default background image url' WHERE `cfkey` = 'magenta.default-background';
UPDATE `pacmandata`.`pac_config_key_metadata` SET `description` = 'Magenta skill error background image url' WHERE `cfkey` = 'magenta.error-background';
UPDATE `pacmandata`.`pac_config_key_metadata` SET `description` = 'Magenta skill goodbye background image url' WHERE `cfkey` = 'magenta.goodbye-background';
UPDATE `pacmandata`.`pac_config_key_metadata` SET `description` = 'Magenta skill goodbye greeting text' WHERE `cfkey` = 'magenta.goodbye-greeting';
UPDATE `pacmandata`.`pac_config_key_metadata` SET `description` = 'Magenta skill welcome background image url' WHERE `cfkey` = 'magenta.welcome-background';
UPDATE `pacmandata`.`pac_config_key_metadata` SET `description` = 'Magenta skill welcome greeting text' WHERE `cfkey` = 'magenta.welcome-greeting';
UPDATE `pacmandata`.`pac_config_key_metadata` SET `description` = 'Management endpoints which should be exposed' WHERE `cfkey` = 'management.endpoints.web.exposure.include';
UPDATE `pacmandata`.`pac_config_key_metadata` SET `description` = 'Management health rabbit enabled' WHERE `cfkey` = 'management.health.rabbit.enabled';
UPDATE `pacmandata`.`pac_config_key_metadata` SET `description` = 'Management health security enabled' WHERE `cfkey` = 'management.security.enabled';
UPDATE `pacmandata`.`pac_config_key_metadata` SET `description` = 'Services which needs to be monitored' WHERE `cfkey` = 'monitoring.contextRootNames';
UPDATE `pacmandata`.`pac_config_key_metadata` SET `description` = 'Pacbot resource owner email when autofix fallback occurs ' WHERE `cfkey` = 'pacbot.autofix.resourceowner.fallbak.email';
UPDATE `pacmandata`.`pac_config_key_metadata` SET `description` = 'Oauth2 client-id for pacbot api' WHERE `cfkey` = 'pacman.api.oauth2.client-id';
UPDATE `pacmandata`.`pac_config_key_metadata` SET `description` = 'Oauth2 client-secret for pacbot api' WHERE `cfkey` = 'pacman.api.oauth2.client-secret';
UPDATE `pacmandata`.`pac_config_key_metadata` SET `description` = 'Pacbot API for sending mail' WHERE `cfkey` = 'pacman.api.sendmail';
UPDATE `pacmandata`.`pac_config_key_metadata` SET `description` = 'Pacbot autofix common notification template for EC2WithPublicIPAccess_version-1_Ec2WithPublicAccess_ec2' WHERE `cfkey` = 'pacman.auto.fix.common.email.notifications.EC2WithPublicIPAccess_version-1_Ec2WithPublicAccess_ec2';
UPDATE `pacmandata`.`pac_config_key_metadata` SET `description` = 'Pacbot autofix common notification template for ElasticSearchPublicAccess_version-1_ElasticSearchPublicAccessRule_elasticsearch' WHERE `cfkey` = 'pacman.auto.fix.common.email.notifications.ElasticSearchPublicAccess_version-1_ElasticSearchPublicAccessRule_elasticsearch';
UPDATE `pacmandata`.`pac_config_key_metadata` SET `description` = 'Pacbot autofix common notification template for ElbWithPublicAccess_version-1_ApplicationElbWithPublicAccess_appelb' WHERE `cfkey` = 'pacman.auto.fix.common.email.notifications.ElbWithPublicAccess_version-1_ApplicationElbWithPublicAccess_appelb';
UPDATE `pacmandata`.`pac_config_key_metadata` SET `description` = 'Pacbot autofix common notification template for ElbWithPublicAccess_version-1_ClassicElbWithPublicAccess_classicelb' WHERE `cfkey` = 'pacman.auto.fix.common.email.notifications.ElbWithPublicAccess_version-1_ClassicElbWithPublicAccess_classicelb';
UPDATE `pacmandata`.`pac_config_key_metadata` SET `description` = 'Pacbot autofix common notification template for RedShiftPublicAccess_version-1_RedShiftPublicAccess_redshift' WHERE `cfkey` = 'pacman.auto.fix.common.email.notifications.RedShiftPublicAccess_version-1_RedShiftPublicAccess_redshift';
UPDATE `pacmandata`.`pac_config_key_metadata` SET `description` = 'Pacbot autofix common notification template for Unused-Security-group_version-1_UnusedSecurityGroup_sg' WHERE `cfkey` = 'pacman.auto.fix.common.email.notifications.Unused-Security-group_version-1_UnusedSecurityGroup_sg';
UPDATE `pacmandata`.`pac_config_key_metadata` SET `description` = 'Pacbot autofix common notification template for UnusedElasticIpRule_version-1_UnusedElasticIpRule_elasticip' WHERE `cfkey` = 'pacman.auto.fix.common.email.notifications.UnusedElasticIpRule_version-1_UnusedElasticIpRule_elasticip';
UPDATE `pacmandata`.`pac_config_key_metadata` SET `description` = 'Pacbot autofix common notification template for rdsdb_version-1_RdsDbPublicAccess_rdsdb' WHERE `cfkey` = 'pacman.auto.fix.common.email.notifications.rdsdb_version-1_RdsDbPublicAccess_rdsdb';
UPDATE `pacmandata`.`pac_config_key_metadata` SET `description` = 'Pacbot autofix mail recipients' WHERE `cfkey` = 'pacman.auto.fix.mail.cc.to';
UPDATE `pacmandata`.`pac_config_key_metadata` SET `description` = 'Pacbot autofix mail from mailId' WHERE `cfkey` = 'pacman.auto.fix.mail.from';
UPDATE `pacmandata`.`pac_config_key_metadata` SET `description` = 'Pacbot autofix mail subject for EC2WithPublicIPAccess_version-1_Ec2WithPublicAccess_ec2' WHERE `cfkey` = 'pacman.auto.fix.mail.subject.EC2WithPublicIPAccess_version-1_Ec2WithPublicAccess_ec2';
UPDATE `pacmandata`.`pac_config_key_metadata` SET `description` = 'Pacbot autofix mail subject for ElasticSearchPublicAccess_version-1_ElasticSearchPublicAccessRule_elasticsearch' WHERE `cfkey` = 'pacman.auto.fix.mail.subject.ElasticSearchPublicAccess_version-1_ElasticSearchPublicAccessRule_elasticsearch';
UPDATE `pacmandata`.`pac_config_key_metadata` SET `description` = 'Pacbot autofix mail subject for ElbWithPublicAccess_version-1_ApplicationElbWithPublicAccess_appelb' WHERE `cfkey` = 'pacman.auto.fix.mail.subject.ElbWithPublicAccess_version-1_ApplicationElbWithPublicAccess_appelb';
UPDATE `pacmandata`.`pac_config_key_metadata` SET `description` = 'Pacbot autofix mail subject for ElbWithPublicAccess_version-1_ClassicElbWithPublicAccess_classicelb' WHERE `cfkey` = 'pacman.auto.fix.mail.subject.ElbWithPublicAccess_version-1_ClassicElbWithPublicAccess_classicelb';
UPDATE `pacmandata`.`pac_config_key_metadata` SET `description` = 'Pacbot autofix mail subject for RedShiftPublicAccess_version-1_RedShiftPublicAccess_redshift' WHERE `cfkey` = 'pacman.auto.fix.mail.subject.RedShiftPublicAccess_version-1_RedShiftPublicAccess_redshift';
UPDATE `pacmandata`.`pac_config_key_metadata` SET `description` = 'Pacbot autofix mail subject for S3GlobalAccess_version-1_S3BucketShouldnotpubliclyaccessble_s3' WHERE `cfkey` = 'pacman.auto.fix.mail.subject.S3GlobalAccess_version-1_S3BucketShouldnotpubliclyaccessble_s3';
UPDATE `pacmandata`.`pac_config_key_metadata` SET `description` = 'Pacbot autofix mail subject for Unused-Security-group_version-1_UnusedSecurityGroup_sg' WHERE `cfkey` = 'pacman.auto.fix.mail.subject.Unused-Security-group_version-1_UnusedSecurityGroup_sg';
UPDATE `pacmandata`.`pac_config_key_metadata` SET `description` = 'Pacbot autofix mail subject for UnusedElasticIpRule_version-1_UnusedElasticIpRule_elasticip' WHERE `cfkey` = 'pacman.auto.fix.mail.subject.UnusedElasticIpRule_version-1_UnusedElasticIpRule_elasticip';
UPDATE `pacmandata`.`pac_config_key_metadata` SET `description` = 'Pacbot autofix mail subject for rdsdb_version-1_RdsDbPublicAccess_rdsdb' WHERE `cfkey` = 'pacman.auto.fix.mail.subject.rdsdb_version-1_RdsDbPublicAccess_rdsdb';
UPDATE `pacmandata`.`pac_config_key_metadata` SET `description` = 'Pacbot autofix mail template columns for EC2WithPublicIPAccess_version-1_Ec2WithPublicAccess_ec2' WHERE `cfkey` = 'pacman.auto.fix.mail.template.columns.EC2WithPublicIPAccess_version-1_Ec2WithPublicAccess_ec2';
UPDATE `pacmandata`.`pac_config_key_metadata` SET `description` = 'Pacbot autofix mail template columns for ElasticSearchPublicAccess_version-1_ElasticSearchPublicAccessRule_elasticsearch' WHERE `cfkey` = 'pacman.auto.fix.mail.template.columns.ElasticSearchPublicAccess_version-1_ElasticSearchPublicAccessRule_elasticsearch';
UPDATE `pacmandata`.`pac_config_key_metadata` SET `description` = 'Pacbot autofix mail template columns for ElbWithPublicAccess_version-1_ApplicationElbWithPublicAccess_appelb' WHERE `cfkey` = 'pacman.auto.fix.mail.template.columns.ElbWithPublicAccess_version-1_ApplicationElbWithPublicAccess_appelb';
UPDATE `pacmandata`.`pac_config_key_metadata` SET `description` = 'Pacbot autofix mail template columns for ElbWithPublicAccess_version-1_ClassicElbWithPublicAccess_classicelb' WHERE `cfkey` = 'pacman.auto.fix.mail.template.columns.ElbWithPublicAccess_version-1_ClassicElbWithPublicAccess_classicelb';
UPDATE `pacmandata`.`pac_config_key_metadata` SET `description` = 'Pacbot autofix mail template columns for RedShiftPublicAccess_version-1_RedShiftPublicAccess_redshift' WHERE `cfkey` = 'pacman.auto.fix.mail.template.columns.RedShiftPublicAccess_version-1_RedShiftPublicAccess_redshift';
UPDATE `pacmandata`.`pac_config_key_metadata` SET `description` = 'Pacbot autofix mail template columns for Unused-Security-group_version-1_UnusedSecurityGroup_sg' WHERE `cfkey` = 'pacman.auto.fix.mail.template.columns.Unused-Security-group_version-1_UnusedSecurityGroup_sg';
UPDATE `pacmandata`.`pac_config_key_metadata` SET `description` = 'Pacbot autofix mail template columns for UnusedElasticIpRule_version-1_UnusedElasticIpRule_elasticip' WHERE `cfkey` = 'pacman.auto.fix.mail.template.columns.UnusedElasticIpRule_version-1_UnusedElasticIpRule_elasticip';
UPDATE `pacmandata`.`pac_config_key_metadata` SET `description` = 'Pacbot autofix mail template columns for rdsdb_version-1_RdsDbPublicAccess_rdsdb' WHERE `cfkey` = 'pacman.auto.fix.mail.template.columns.rdsdb_version-1_RdsDbPublicAccess_rdsdb';
UPDATE `pacmandata`.`pac_config_key_metadata` SET `description` = 'Pacbot autofix maximum number of email notification' WHERE `cfkey` = 'pacman.auto.fix.max.email.notifications';
UPDATE `pacmandata`.`pac_config_key_metadata` SET `description` = 'Pacbot autofix maximum number of email notification for EC2WithPublicIPAccess_version-1_Ec2WithPublicAccess_ec2' WHERE `cfkey` = 'pacman.auto.fix.max.email.notifications.EC2WithPublicIPAccess_version-1_Ec2WithPublicAccess_ec2';
UPDATE `pacmandata`.`pac_config_key_metadata` SET `description` = 'Pacbot autofix maximum number of email notification for ElasticSearchPublicAccess_version-1_ElasticSearchPublicAccessRule_elasticsearch' WHERE `cfkey` = 'pacman.auto.fix.max.email.notifications.ElasticSearchPublicAccess_version-1_ElasticSearchPublicAccessRule_elasticsearch';
UPDATE `pacmandata`.`pac_config_key_metadata` SET `description` = 'Pacbot autofix maximum number of email notification for ElbWithPublicAccess_version-1_ApplicationElbWithPublicAccess_appelb' WHERE `cfkey` = 'pacman.auto.fix.max.email.notifications.ElbWithPublicAccess_version-1_ApplicationElbWithPublicAccess_appelb';
UPDATE `pacmandata`.`pac_config_key_metadata` SET `description` = 'Pacbot autofix maximum number of email notification for ElbWithPublicAccess_version-1_ClassicElbWithPublicAccess_classicelb' WHERE `cfkey` = 'pacman.auto.fix.max.email.notifications.ElbWithPublicAccess_version-1_ClassicElbWithPublicAccess_classicelb';
UPDATE `pacmandata`.`pac_config_key_metadata` SET `description` = 'Pacbot autofix maximum number of email notification for RedShiftPublicAccess_version-1_RedShiftPublicAccess_redshift' WHERE `cfkey` = 'pacman.auto.fix.max.email.notifications.RedShiftPublicAccess_version-1_RedShiftPublicAccess_redshift';
UPDATE `pacmandata`.`pac_config_key_metadata` SET `description` = 'Pacbot autofix maximum number of email notification for S3GlobalAccess_version-1_S3BucketShouldnotpubliclyaccessble_s3' WHERE `cfkey` = 'pacman.auto.fix.max.email.notifications.S3GlobalAccess_version-1_S3BucketShouldnotpubliclyaccessble_s3';
UPDATE `pacmandata`.`pac_config_key_metadata` SET `description` = 'Pacbot autofix maximum number of email notification for rdsdb_version-1_RdsDbPublicAccess_rdsdb' WHERE `cfkey` = 'pacman.auto.fix.max.email.notifications.rdsdb_version-1_RdsDbPublicAccess_rdsdb';
UPDATE `pacmandata`.`pac_config_key_metadata` SET `description` = 'Pacbot autofix orphan resource owner email' WHERE `cfkey` = 'pacman.auto.fix.orphan.resource.owner';
UPDATE `pacmandata`.`pac_config_key_metadata` SET `description` = 'Pacbot autofix resource name filter pattern' WHERE `cfkey` = 'pacman.auto.fix.resource.name.filter.pattern';
UPDATE `pacmandata`.`pac_config_key_metadata` SET `description` = 'Pacbot autofix role name' WHERE `cfkey` = 'pacman.auto.fix.role.name';
UPDATE `pacmandata`.`pac_config_key_metadata` SET `description` = 'Pacbot autofix tag algorithm' WHERE `cfkey` = 'pacman.auto.fix.tag.encyption.algorithm';
UPDATE `pacmandata`.`pac_config_key_metadata` SET `description` = 'Pacbot autofix tag name' WHERE `cfkey` = 'pacman.auto.fix.tag.name';
UPDATE `pacmandata`.`pac_config_key_metadata` SET `description` = 'Pacbot autofix tag salt' WHERE `cfkey` = 'pacman.auto.fix.tag.salt';
UPDATE `pacmandata`.`pac_config_key_metadata` SET `description` = 'Pacbot autofix warning mail subject' WHERE `cfkey` = 'pacman.auto.warning.mail.subject.EC2WithPublicIPAccess_version-1_Ec2WithPublicAccess_ec2';
UPDATE `pacmandata`.`pac_config_key_metadata` SET `description` = 'Pacbot autofix warning mail subject for ElasticSearchPublicAccess_version-1_ElasticSearchPublicAccessRule_elasticsearch' WHERE `cfkey` = 'pacman.auto.warning.mail.subject.ElasticSearchPublicAccess_version-1_ElasticSearchPublicAccessRule_elasticsearch';
UPDATE `pacmandata`.`pac_config_key_metadata` SET `description` = 'Pacbot autofix warning mail subject for ElbWithPublicAccess_version-1_ApplicationElbWithPublicAccess_appelb' WHERE `cfkey` = 'pacman.auto.warning.mail.subject.ElbWithPublicAccess_version-1_ApplicationElbWithPublicAccess_appelb';
UPDATE `pacmandata`.`pac_config_key_metadata` SET `description` = 'Pacbot autofix warning mail subject for ElbWithPublicAccess_version-1_ClassicElbWithPublicAccess_classicelb' WHERE `cfkey` = 'pacman.auto.warning.mail.subject.ElbWithPublicAccess_version-1_ClassicElbWithPublicAccess_classicelb';
UPDATE `pacmandata`.`pac_config_key_metadata` SET `description` = 'Pacbot autofix warning mail subject for RedShiftPublicAccess_version-1_RedShiftPublicAccess_redshift' WHERE `cfkey` = 'pacman.auto.warning.mail.subject.RedShiftPublicAccess_version-1_RedShiftPublicAccess_redshift';
UPDATE `pacmandata`.`pac_config_key_metadata` SET `description` = 'Pacbot autofix warning mail subject for S3GlobalAccess_version-1_S3BucketShouldnotpubliclyaccessble_s3' WHERE `cfkey` = 'pacman.auto.warning.mail.subject.S3GlobalAccess_version-1_S3BucketShouldnotpubliclyaccessble_s3';
UPDATE `pacmandata`.`pac_config_key_metadata` SET `description` = 'Pacbot autofix warning mail subject for rdsdb_version-1_RdsDbPublicAccess_rdsdb' WHERE `cfkey` = 'pacman.auto.warning.mail.subject.rdsdb_version-1_RdsDbPublicAccess_rdsdb';
UPDATE `pacmandata`.`pac_config_key_metadata` SET `description` = 'Pacbot autofix contact mailid for Unused-Security-group_version-1_UnusedSecurityGroup_sg' WHERE `cfkey` = 'pacman.autofix.contact.Unused-Security-group_version-1_UnusedSecurityGroup_sg';
UPDATE `pacmandata`.`pac_config_key_metadata` SET `description` = 'Pacbot autofix contact mailid for UnusedElasticIpRule_version-1_UnusedElasticIpRule_elasticip' WHERE `cfkey` = 'pacman.autofix.contact.UnusedElasticIpRule_version-1_UnusedElasticIpRule_elasticip';
UPDATE `pacmandata`.`pac_config_key_metadata` SET `description` = 'Pacbot autofix exempted types for cutoff data' WHERE `cfkey` = 'pacman.autofix.exempted.types.for.cutoff.data';
UPDATE `pacmandata`.`pac_config_key_metadata` SET `description` = 'Pacbot autofix notify for Unused-Security-group_version-1_UnusedSecurityGroup_sg' WHERE `cfkey` = 'pacman.autofix.fix.notify.Unused-Security-group_version-1_UnusedSecurityGroup_sg';
UPDATE `pacmandata`.`pac_config_key_metadata` SET `description` = 'Pacbot autofix notify for UnusedElasticIpRule_version-1_UnusedElasticIpRule_elasticip' WHERE `cfkey` = 'pacman.autofix.fix.notify.UnusedElasticIpRule_version-1_UnusedElasticIpRule_elasticip';
UPDATE `pacmandata`.`pac_config_key_metadata` SET `description` = 'Pacbot autofix fix type for Unused-Security-group_version-1_UnusedSecurityGroup_sg' WHERE `cfkey` = 'pacman.autofix.fix.type.Unused-Security-group_version-1_UnusedSecurityGroup_sg';
UPDATE `pacmandata`.`pac_config_key_metadata` SET `description` = 'Pacbot autofix fix type for UnusedElasticIpRule_version-1_UnusedElasticIpRule_elasticip' WHERE `cfkey` = 'pacman.autofix.fix.type.UnusedElasticIpRule_version-1_UnusedElasticIpRule_elasticip';
UPDATE `pacmandata`.`pac_config_key_metadata` SET `description` = 'Pacbot autofix issue creation time elapsed for UnusedElasticIpRule_version-1_UnusedElasticIpRule_elasticip' WHERE `cfkey` = 'pacman.autofix.issue.creation.time.elapsed.UnusedElasticIpRule_version-1_UnusedElasticIpRule_elasticip';
UPDATE `pacmandata`.`pac_config_key_metadata` SET `description` = 'Pacbot autofix non taggable services' WHERE `cfkey` = 'pacman.autofix.non.taggable.services';
UPDATE `pacmandata`.`pac_config_key_metadata` SET `description` = 'Pacbot autofix policy URL for EC2WithPublicIPAccess_version-1_Ec2WithPublicAccess_ec2' WHERE `cfkey` = 'pacman.autofix.policy.url.EC2WithPublicIPAccess_version-1_Ec2WithPublicAccess_ec2';
UPDATE `pacmandata`.`pac_config_key_metadata` SET `description` = 'Pacbot autofix policy URL for S3GlobalAccess_version-1_S3BucketShouldnotpubliclyaccessble_s3' WHERE `cfkey` = 'pacman.autofix.policy.url.S3GlobalAccess_version-1_S3BucketShouldnotpubliclyaccessble_s3';
UPDATE `pacmandata`.`pac_config_key_metadata` SET `description` = 'Pacbot autofix policy URL path' WHERE `cfkey` = 'pacman.autofix.policy.url.path';
UPDATE `pacmandata`.`pac_config_key_metadata` SET `description` = 'Pacbot autofix rule post fix message for EC2WithPublicIPAccess_version-1_Ec2WithPublicAccess_ec2' WHERE `cfkey` = 'pacman.autofix.rule.post.fix.message.EC2WithPublicIPAccess_version-1_Ec2WithPublicAccess_ec2';
UPDATE `pacmandata`.`pac_config_key_metadata` SET `description` = 'Pacbot autofix rule post fix message for ElasticSearchPublicAccess_version-1_ElasticSearchPublicAccessRule_elasticsearch' WHERE `cfkey` = 'pacman.autofix.rule.post.fix.message.ElasticSearchPublicAccess_version-1_ElasticSearchPublicAccessRule_elasticsearch';
UPDATE `pacmandata`.`pac_config_key_metadata` SET `description` = 'Pacbot autofix rule post fix message for ElbWithPublicAccess_version-1_ApplicationElbWithPublicAccess_appelb' WHERE `cfkey` = 'pacman.autofix.rule.post.fix.message.ElbWithPublicAccess_version-1_ApplicationElbWithPublicAccess_appelb';
UPDATE `pacmandata`.`pac_config_key_metadata` SET `description` = 'Pacbot autofix rule post fix message for ElbWithPublicAccess_version-1_ClassicElbWithPublicAccess_classicelb' WHERE `cfkey` = 'pacman.autofix.rule.post.fix.message.ElbWithPublicAccess_version-1_ClassicElbWithPublicAccess_classicelb';
UPDATE `pacmandata`.`pac_config_key_metadata` SET `description` = 'Pacbot autofix rule post fix message for RedShiftPublicAccess_version-1_RedShiftPublicAccess_redshift' WHERE `cfkey` = 'pacman.autofix.rule.post.fix.message.RedShiftPublicAccess_version-1_RedShiftPublicAccess_redshift';
UPDATE `pacmandata`.`pac_config_key_metadata` SET `description` = 'Pacbot autofix rule post fix message for S3GlobalAccess_version-1_S3BucketShouldnotpubliclyaccessble_s3' WHERE `cfkey` = 'pacman.autofix.rule.post.fix.message.S3GlobalAccess_version-1_S3BucketShouldnotpubliclyaccessble_s3';
UPDATE `pacmandata`.`pac_config_key_metadata` SET `description` = 'Pacbot autofix rule post fix message for Unused-Security-group_version-1_UnusedSecurityGroup_sg' WHERE `cfkey` = 'pacman.autofix.rule.post.fix.message.Unused-Security-group_version-1_UnusedSecurityGroup_sg';
UPDATE `pacmandata`.`pac_config_key_metadata` SET `description` = 'Pacbot autofix rule post fix message for UnusedElasticIpRule_version-1_UnusedElasticIpRule_elasticip' WHERE `cfkey` = 'pacman.autofix.rule.post.fix.message.UnusedElasticIpRule_version-1_UnusedElasticIpRule_elasticip';
UPDATE `pacmandata`.`pac_config_key_metadata` SET `description` = 'Pacbot autofix rule post fix message for rdsdb_version-1_RdsDbPublicAccess_rdsdb' WHERE `cfkey` = 'pacman.autofix.rule.post.fix.message.rdsdb_version-1_RdsDbPublicAccess_rdsdb';
UPDATE `pacmandata`.`pac_config_key_metadata` SET `description` = 'Pacbot autofix rule violation message for EC2WithPublicIPAccess_version-1_Ec2WithPublicAccess_ec2' WHERE `cfkey` = 'pacman.autofix.rule.violation.message.EC2WithPublicIPAccess_version-1_Ec2WithPublicAccess_ec2';
UPDATE `pacmandata`.`pac_config_key_metadata` SET `description` = 'Pacbot autofix rule violation message for ElasticSearchPublicAccess_version-1_ElasticSearchPublicAccessRule_elasticsearch' WHERE `cfkey` = 'pacman.autofix.rule.violation.message.ElasticSearchPublicAccess_version-1_ElasticSearchPublicAccessRule_elasticsearch';
UPDATE `pacmandata`.`pac_config_key_metadata` SET `description` = 'Pacbot autofix rule violation message for ElbWithPublicAccess_version-1_ApplicationElbWithPublicAccess_appelb' WHERE `cfkey` = 'pacman.autofix.rule.violation.message.ElbWithPublicAccess_version-1_ApplicationElbWithPublicAccess_appelb';
UPDATE `pacmandata`.`pac_config_key_metadata` SET `description` = 'Pacbot autofix rule violation message for ElbWithPublicAccess_version-1_ClassicElbWithPublicAccess_classicelb' WHERE `cfkey` = 'pacman.autofix.rule.violation.message.ElbWithPublicAccess_version-1_ClassicElbWithPublicAccess_classicelb';
UPDATE `pacmandata`.`pac_config_key_metadata` SET `description` = 'Pacbot autofix rule violation message for RedShiftPublicAccess_version-1_RedShiftPublicAccess_redshift' WHERE `cfkey` = 'pacman.autofix.rule.violation.message.RedShiftPublicAccess_version-1_RedShiftPublicAccess_redshift';
UPDATE `pacmandata`.`pac_config_key_metadata` SET `description` = 'Pacbot autofix rule violation message for S3GlobalAccess_version-1_S3BucketShouldnotpubliclyaccessble_s3' WHERE `cfkey` = 'pacman.autofix.rule.violation.message.S3GlobalAccess_version-1_S3BucketShouldnotpubliclyaccessble_s3';
UPDATE `pacmandata`.`pac_config_key_metadata` SET `description` = 'Pacbot autofix rule violation message for rdsdb_version-1_RdsDbPublicAccess_rdsdb' WHERE `cfkey` = 'pacman.autofix.rule.violation.message.rdsdb_version-1_RdsDbPublicAccess_rdsdb';
UPDATE `pacmandata`.`pac_config_key_metadata` SET `description` = 'Pacbot autofix rule warning message for EC2WithPublicIPAccess_version-1_Ec2WithPublicAccess_ec2' WHERE `cfkey` = 'pacman.autofix.rule.warning.message.EC2WithPublicIPAccess_version-1_Ec2WithPublicAccess_ec2';
UPDATE `pacmandata`.`pac_config_key_metadata` SET `description` = 'Pacbot autofix rule warning message for ElasticSearchPublicAccess_version-1_ElasticSearchPublicAccessRule_elasticsearch' WHERE `cfkey` = 'pacman.autofix.rule.warning.message.ElasticSearchPublicAccess_version-1_ElasticSearchPublicAccessRule_elasticsearch';
UPDATE `pacmandata`.`pac_config_key_metadata` SET `description` = 'Pacbot autofix rule warning message for ElbWithPublicAccess_version-1_ApplicationElbWithPublicAccess_appelb' WHERE `cfkey` = 'pacman.autofix.rule.warning.message.ElbWithPublicAccess_version-1_ApplicationElbWithPublicAccess_appelb';
UPDATE `pacmandata`.`pac_config_key_metadata` SET `description` = 'Pacbot autofix rule warning message for ElbWithPublicAccess_version-1_ClassicElbWithPublicAccess_classicelb' WHERE `cfkey` = 'pacman.autofix.rule.warning.message.ElbWithPublicAccess_version-1_ClassicElbWithPublicAccess_classicelb';
UPDATE `pacmandata`.`pac_config_key_metadata` SET `description` = 'Pacbot autofix rule warning message for RedShiftPublicAccess_version-1_RedShiftPublicAccess_redshift' WHERE `cfkey` = 'pacman.autofix.rule.warning.message.RedShiftPublicAccess_version-1_RedShiftPublicAccess_redshift';
UPDATE `pacmandata`.`pac_config_key_metadata` SET `description` = 'Pacbot autofix rule warning message for S3GlobalAccess_version-1_S3BucketShouldnotpubliclyaccessble_s3' WHERE `cfkey` = 'pacman.autofix.rule.warning.message.S3GlobalAccess_version-1_S3BucketShouldnotpubliclyaccessble_s3';
UPDATE `pacmandata`.`pac_config_key_metadata` SET `description` = 'Pacbot autofix rule warning message for rdsdb_version-1_RdsDbPublicAccess_rdsdb' WHERE `cfkey` = 'pacman.autofix.rule.warning.message.rdsdb_version-1_RdsDbPublicAccess_rdsdb';
UPDATE `pacmandata`.`pac_config_key_metadata` SET `description` = 'Pacbot autofix wait time for rdsdb_version-1_RdsDbPublicAccess_rdsdb' WHERE `cfkey` = 'pacman.autofix.waittime.EC2WithPublicIPAccess_version-1_Ec2WithPublicAccess_ec2';
UPDATE `pacmandata`.`pac_config_key_metadata` SET `description` = 'Pacbot autofix wait time for ElasticSearchPublicAccess_version-1_ElasticSearchPublicAccessRule_elasticsearch' WHERE `cfkey` = 'pacman.autofix.waittime.ElasticSearchPublicAccess_version-1_ElasticSearchPublicAccessRule_elasticsearch';
UPDATE `pacmandata`.`pac_config_key_metadata` SET `description` = 'Pacbot autofix wait time for ElbWithPublicAccess_version-1_ApplicationElbWithPublicAccess_appelb' WHERE `cfkey` = 'pacman.autofix.waittime.ElbWithPublicAccess_version-1_ApplicationElbWithPublicAccess_appelb';
UPDATE `pacmandata`.`pac_config_key_metadata` SET `description` = 'Pacbot autofix wait time for ElbWithPublicAccess_version-1_ClassicElbWithPublicAccess_classicelb' WHERE `cfkey` = 'pacman.autofix.waittime.ElbWithPublicAccess_version-1_ClassicElbWithPublicAccess_classicelb';
UPDATE `pacmandata`.`pac_config_key_metadata` SET `description` = 'Pacbot autofix wait time for RedShiftPublicAccess_version-1_RedShiftPublicAccess_redshift' WHERE `cfkey` = 'pacman.autofix.waittime.RedShiftPublicAccess_version-1_RedShiftPublicAccess_redshift';
UPDATE `pacmandata`.`pac_config_key_metadata` SET `description` = 'Pacbot autofix wait time for S3GlobalAccess_version-1_S3BucketShouldnotpubliclyaccessble_s3' WHERE `cfkey` = 'pacman.autofix.waittime.S3GlobalAccess_version-1_S3BucketShouldnotpubliclyaccessble_s3';
UPDATE `pacmandata`.`pac_config_key_metadata` SET `description` = 'Pacbot autofix wait time for rdsdb_version-1_RdsDbPublicAccess_rdsdb' WHERE `cfkey` = 'pacman.autofix.waittime.rdsdb_version-1_RdsDbPublicAccess_rdsdb';
UPDATE `pacmandata`.`pac_config_key_metadata` SET `description` = 'Pacbot autofix ES transaction index' WHERE `cfkey` = 'pacman.es.auto.fix.transaction.index';
UPDATE `pacmandata`.`pac_config_key_metadata` SET `description` = 'Pacbot autofix ES transaction type' WHERE `cfkey` = 'pacman.es.auto.fix.transaction.type';
UPDATE `pacmandata`.`pac_config_key_metadata` SET `description` = 'Pacbot ES host' WHERE `cfkey` = 'pacman.es.host';
UPDATE `pacmandata`.`pac_config_key_metadata` SET `description` = 'Pacbot ES port' WHERE `cfkey` = 'pacman.es.port';
UPDATE `pacmandata`.`pac_config_key_metadata` SET `description` = 'Pacbot ES reactors index' WHERE `cfkey` = 'pacman.es.reactors.index';
UPDATE `pacmandata`.`pac_config_key_metadata` SET `description` = 'Pacbot ES reactors registry' WHERE `cfkey` = 'pacman.es.reactors.registry';
UPDATE `pacmandata`.`pac_config_key_metadata` SET `description` = 'Pacbot ES stats index' WHERE `cfkey` = 'pacman.es.stats.index';
UPDATE `pacmandata`.`pac_config_key_metadata` SET `description` = 'Pacbot ES stats type' WHERE `cfkey` = 'pacman.es.stats.type';
UPDATE `pacmandata`.`pac_config_key_metadata` SET `description` = 'Pacbot exempted mail subject' WHERE `cfkey` = 'pacman.exempted.mail.subject';
UPDATE `pacmandata`.`pac_config_key_metadata` SET `description` = 'Pacbot host' WHERE `cfkey` = 'pacman.host';
UPDATE `pacmandata`.`pac_config_key_metadata` SET `description` = 'Pacbot integrations slack webhook url' WHERE `cfkey` = 'pacman.integrations.slack.webhook.url';
UPDATE `pacmandata`.`pac_config_key_metadata` SET `description` = 'Pacbot login password' WHERE `cfkey` = 'pacman.login.password';
UPDATE `pacmandata`.`pac_config_key_metadata` SET `description` = 'Pacbot login user name' WHERE `cfkey` = 'pacman.login.user.name';
UPDATE `pacmandata`.`pac_config_key_metadata` SET `description` = 'Pacbot service password' WHERE `cfkey` = 'pacman.service-password';
UPDATE `pacmandata`.`pac_config_key_metadata` SET `description` = 'Pacbot service user' WHERE `cfkey` = 'pacman.service-user';
UPDATE `pacmandata`.`pac_config_key_metadata` SET `description` = 'Pacbot target type alias' WHERE `cfkey` = 'pacman.target.type.alias';
UPDATE `pacmandata`.`pac_config_key_metadata` SET `description` = 'Pacbot URL' WHERE `cfkey` = 'pacman.url';
UPDATE `pacmandata`.`pac_config_key_metadata` SET `description` = 'Pacbot projections assetgroups' WHERE `cfkey` = 'projections.assetgroups';
UPDATE `pacmandata`.`pac_config_key_metadata` SET `description` = 'Pacbot projections targetTypes' WHERE `cfkey` = 'projections.targetTypes';
UPDATE `pacmandata`.`pac_config_key_metadata` SET `description` = 'Pacbot redshift password' WHERE `cfkey` = 'redshift.password';
UPDATE `pacmandata`.`pac_config_key_metadata` SET `description` = 'Pacbot redshift URL' WHERE `cfkey` = 'redshift.url';
UPDATE `pacmandata`.`pac_config_key_metadata` SET `description` = 'Pacbot redshift user name' WHERE `cfkey` = 'redshift.userName';
UPDATE `pacmandata`.`pac_config_key_metadata` SET `description` = 'AWS region to ignore' WHERE `cfkey` = 'region.ignore';
UPDATE `pacmandata`.`pac_config_key_metadata` SET `description` = 'Remind mail cron' WHERE `cfkey` = 'remind.cron';
UPDATE `pacmandata`.`pac_config_key_metadata` SET `description` = 'Remind mail subject' WHERE `cfkey` = 'remind.email.subject';
UPDATE `pacmandata`.`pac_config_key_metadata` SET `description` = 'Remind mail text' WHERE `cfkey` = 'remind.email.text';
UPDATE `pacmandata`.`pac_config_key_metadata` SET `description` = 'Rule Engine invoke URL' WHERE `cfkey` = 'rule-engine.invoke.url';
UPDATE `pacmandata`.`pac_config_key_metadata` SET `description` = 'Rule lambda action disabled' WHERE `cfkey` = 'rule.lambda.action-disabled';
UPDATE `pacmandata`.`pac_config_key_metadata` SET `description` = 'Rule lambda action enabled' WHERE `cfkey` = 'rule.lambda.action-enabled';
UPDATE `pacmandata`.`pac_config_key_metadata` SET `description` = 'Rule lambda function arn' WHERE `cfkey` = 'rule.lambda.function-arn';
UPDATE `pacmandata`.`pac_config_key_metadata` SET `description` = 'Rule lambda function name' WHERE `cfkey` = 'rule.lambda.function-name';
UPDATE `pacmandata`.`pac_config_key_metadata` SET `description` = 'Rule lambda principal' WHERE `cfkey` = 'rule.lambda.principal';
UPDATE `pacmandata`.`pac_config_key_metadata` SET `description` = 'Rule lambda target-id' WHERE `cfkey` = 'rule.lambda.target-id';
UPDATE `pacmandata`.`pac_config_key_metadata` SET `description` = 'Rule s3 bucket-name' WHERE `cfkey` = 'rule.s3.bucket-name';
UPDATE `pacmandata`.`pac_config_key_metadata` SET `description` = 's3 inventory' WHERE `cfkey` = 's3';
UPDATE `pacmandata`.`pac_config_key_metadata` SET `description` = 's3 data' WHERE `cfkey` = 's3.data';
UPDATE `pacmandata`.`pac_config_key_metadata` SET `description` = 's3 processed' WHERE `cfkey` = 's3.processed';
UPDATE `pacmandata`.`pac_config_key_metadata` SET `description` = 's3 region' WHERE `cfkey` = 's3.region';
UPDATE `pacmandata`.`pac_config_key_metadata` SET `description` = 's3 role' WHERE `cfkey` = 's3.role';
UPDATE `pacmandata`.`pac_config_key_metadata` SET `description` = 'Basic security enabled' WHERE `cfkey` = 'security.basic.enabled';
UPDATE `pacmandata`.`pac_config_key_metadata` SET `description` = 'Security oauth2 resource user-info-uri' WHERE `cfkey` = 'security.oauth2.resource.user-info-uri';
UPDATE `pacmandata`.`pac_config_key_metadata` SET `description` = 'Server context-path' WHERE `cfkey` = 'server.context-path';
UPDATE `pacmandata`.`pac_config_key_metadata` SET `description` = 'Server context-path' WHERE `cfkey` = 'server.context-path';
UPDATE `pacmandata`.`pac_config_key_metadata` SET `description` = 'Server context-path' WHERE `cfkey` = 'server.contextPath';
UPDATE `pacmandata`.`pac_config_key_metadata` SET `description` = 'Server servlet context-path' WHERE `cfkey` = 'server.servlet.context-path';
UPDATE `pacmandata`.`pac_config_key_metadata` SET `description` = 'Server type' WHERE `cfkey` = 'server_type';
UPDATE `pacmandata`.`pac_config_key_metadata` SET `description` = 'Service dns name' WHERE `cfkey` = 'service.dns.name';
UPDATE `pacmandata`.`pac_config_key_metadata` SET `description` = 'Service admin URL' WHERE `cfkey` = 'service.url.admin';
UPDATE `pacmandata`.`pac_config_key_metadata` SET `description` = 'Service asset URL' WHERE `cfkey` = 'service.url.asset';
UPDATE `pacmandata`.`pac_config_key_metadata` SET `description` = 'Service auth URL' WHERE `cfkey` = 'service.url.auth';
UPDATE `pacmandata`.`pac_config_key_metadata` SET `description` = 'Service compliance URL' WHERE `cfkey` = 'service.url.compliance';
UPDATE `pacmandata`.`pac_config_key_metadata` SET `description` = 'Service devstandards URL' WHERE `cfkey` = 'service.url.devstandards';
UPDATE `pacmandata`.`pac_config_key_metadata` SET `description` = 'Service pac_auth URL' WHERE `cfkey` = 'service.url.pac_auth';
UPDATE `pacmandata`.`pac_config_key_metadata` SET `description` = 'Service statistics URL' WHERE `cfkey` = 'service.url.statistics';
UPDATE `pacmandata`.`pac_config_key_metadata` SET `description` = 'Service vulnerability URL' WHERE `cfkey` = 'service.url.vulnerability';
UPDATE `pacmandata`.`pac_config_key_metadata` SET `description` = 'Spring boot admin client instance health URL' WHERE `cfkey` = 'spring.boot.admin.client.instance.health-url';
UPDATE `pacmandata`.`pac_config_key_metadata` SET `description` = 'Spring boot admin client instance management URL' WHERE `cfkey` = 'spring.boot.admin.client.instance.management-url';
UPDATE `pacmandata`.`pac_config_key_metadata` SET `description` = 'Spring boot admin client instance service URL' WHERE `cfkey` = 'spring.boot.admin.client.instance.service-url';
UPDATE `pacmandata`.`pac_config_key_metadata` SET `description` = 'Spring boot admin client password' WHERE `cfkey` = 'spring.boot.admin.client.password';
UPDATE `pacmandata`.`pac_config_key_metadata` SET `description` = 'Spring boot admin client URL' WHERE `cfkey` = 'spring.boot.admin.client.url[0]';
UPDATE `pacmandata`.`pac_config_key_metadata` SET `description` = 'Spring boot admin client user name' WHERE `cfkey` = 'spring.boot.admin.client.username';
UPDATE `pacmandata`.`pac_config_key_metadata` SET `description` = 'Spring boot cache names' WHERE `cfkey` = 'spring.cache.cache-names';
UPDATE `pacmandata`.`pac_config_key_metadata` SET `description` = 'Spring boot caffeine spec' WHERE `cfkey` = 'spring.cache.caffeine.spec';
UPDATE `pacmandata`.`pac_config_key_metadata` SET `description` = 'Spring boot cloud bus enabled' WHERE `cfkey` = 'spring.cloud.bus.enabled';
UPDATE `pacmandata`.`pac_config_key_metadata` SET `description` = 'Spring boot datasource driver-class-name' WHERE `cfkey` = 'spring.datasource.driver-class-name';
UPDATE `pacmandata`.`pac_config_key_metadata` SET `description` = 'Spring boot datasource password' WHERE `cfkey` = 'spring.datasource.password';
UPDATE `pacmandata`.`pac_config_key_metadata` SET `description` = 'Spring boot datasource url' WHERE `cfkey` = 'spring.datasource.url';
UPDATE `pacmandata`.`pac_config_key_metadata` SET `description` = 'Spring boot datasource user name' WHERE `cfkey` = 'spring.datasource.username';
UPDATE `pacmandata`.`pac_config_key_metadata` SET `description` = 'Spring boot JPA hibernate naming physical strategy' WHERE `cfkey` = 'spring.jpa.hibernate.naming.physical-strategy';
UPDATE `pacmandata`.`pac_config_key_metadata` SET `description` = 'Spring boot mail default encoding' WHERE `cfkey` = 'spring.mail.defaultEncoding';
UPDATE `pacmandata`.`pac_config_key_metadata` SET `description` = 'Spring boot mail host' WHERE `cfkey` = 'spring.mail.host';
UPDATE `pacmandata`.`pac_config_key_metadata` SET `description` = 'Spring boot mail password' WHERE `cfkey` = 'spring.mail.password';
UPDATE `pacmandata`.`pac_config_key_metadata` SET `description` = 'Spring boot mail port' WHERE `cfkey` = 'spring.mail.port';
UPDATE `pacmandata`.`pac_config_key_metadata` SET `description` = 'Spring boot mail properties mail smtp auth' WHERE `cfkey` = 'spring.mail.properties.mail.smtp.auth';
UPDATE `pacmandata`.`pac_config_key_metadata` SET `description` = 'Spring boot mail properties mail smtp ssl trust' WHERE `cfkey` = 'spring.mail.properties.mail.smtp.ssl.trust';
UPDATE `pacmandata`.`pac_config_key_metadata` SET `description` = 'Spring boot mail properties mail smtp start tls enable' WHERE `cfkey` = 'spring.mail.properties.mail.smtp.starttls.enable';
UPDATE `pacmandata`.`pac_config_key_metadata` SET `description` = 'Spring boot mail protocol' WHERE `cfkey` = 'spring.mail.protocol';
UPDATE `pacmandata`.`pac_config_key_metadata` SET `description` = 'Spring boot mail test connection' WHERE `cfkey` = 'spring.mail.test-connection';
UPDATE `pacmandata`.`pac_config_key_metadata` SET `description` = 'Spring boot mail user name' WHERE `cfkey` = 'spring.mail.username';
UPDATE `pacmandata`.`pac_config_key_metadata` SET `description` = 'Spring boot sleuth sampler probability' WHERE `cfkey` = 'spring.sleuth.sampler.probability';
UPDATE `pacmandata`.`pac_config_key_metadata` SET `description` = 'Spring boot zipkin base URL' WHERE `cfkey` = 'spring.zipkin.baseUrl';
UPDATE `pacmandata`.`pac_config_key_metadata` SET `description` = 'Spring boot zipkin sender type' WHERE `cfkey` = 'spring.zipkin.sender.type';
UPDATE `pacmandata`.`pac_config_key_metadata` SET `description` = 'Square one slack channel' WHERE `cfkey` = 'square.one.slack.channel';
UPDATE `pacmandata`.`pac_config_key_metadata` SET `description` = 'Swagger auth allowlist URLs' WHERE `cfkey` = 'swagger.auth.whitelist';
UPDATE `pacmandata`.`pac_config_key_metadata` SET `description` = 'Mandatory tags for resources' WHERE `cfkey` = 'tagging.mandatoryTags';
UPDATE `pacmandata`.`pac_config_key_metadata` SET `description` = 'Categories for different target types' WHERE `cfkey` = 'target-types.categories';
UPDATE `pacmandata`.`pac_config_key_metadata` SET `description` = 'Template digest-mail URL' WHERE `cfkey` = 'template.digest-mail.url';
UPDATE `pacmandata`.`pac_config_key_metadata` SET `description` = 'Timezone across the application' WHERE `cfkey` = 'time.zone';
UPDATE `pacmandata`.`pac_config_key_metadata` SET `description` = 'Vulnerability application occurance' WHERE `cfkey` = 'vulnerability.application.occurance';
UPDATE `pacmandata`.`pac_config_key_metadata` SET `description` = 'Vulnerability application resource details' WHERE `cfkey` = 'vulnerability.application.resourcedetails';
UPDATE `pacmandata`.`pac_config_key_metadata` SET `description` = 'Vulnerability application resource details both' WHERE `cfkey` = 'vulnerability.application.resourcedetailsboth';
UPDATE `pacmandata`.`pac_config_key_metadata` SET `description` = 'Vulnerability severity summary' WHERE `cfkey` = 'vulnerability.summary.severity';
UPDATE `pacmandata`.`pac_config_key_metadata` SET `description` = 'Vulnerability types' WHERE `cfkey` = 'vulnerability.types';
UPDATE `pacmandata`.`pac_config_key_metadata` SET `description` = 'Job interval' WHERE `cfkey` = 'scheduler.interval';
UPDATE `pacmandata`.`pac_config_key_metadata` SET `description` = 'Job inital delay for shipper' WHERE `cfkey` = 'scheduler.rules.initial.delay';
UPDATE `pacmandata`.`pac_config_key_metadata` SET `description` = 'job inital delay for rules' WHERE `cfkey` = 'scheduler.shipper.initial.delay';
UPDATE `pacmandata`.`pac_config_key_metadata` SET `description` = 'job scheduler' WHERE `cfkey` = 'scheduler.role';
UPDATE `pacmandata`.`pac_config_key_metadata` SET `description` = 'job inital delay for collector' WHERE `cfkey` = 'scheduler.collector.initial.delay';
UPDATE `pacmandata`.`pac_config_key_metadata` SET `description` = 'azure event bus details' WHERE `cfkey` = 'azure.eventbridge.bus.details';
UPDATE `pacmandata`.`pac_config_key_metadata` SET `description` = 'number rules in one set of batch' WHERE `cfkey` = 'scheduler.total.batches';
UPDATE `pacmandata`.`pac_config_key_metadata` SET `description` = ' azure cloud is enabled ' WHERE `cfkey` = 'azure.enabled';
UPDATE `pacmandata`.`pac_config_key_metadata` SET `description` = 'gcp cloud is enabled' WHERE `cfkey` = 'aws.eventbridge.bus.details';
UPDATE `pacmandata`.`pac_config_key_metadata` SET `description` = 'aws event bus details' WHERE `cfkey` = 'gcp.eventbridge.bus.details';
UPDATE `pacmandata`.`pac_config_key_metadata` SET `description` = 'gcp event bus details' WHERE `cfkey` = 'gcp.enabled';

DELETE FROM `pac_config_properties` WHERE cfkey='features.vulnerability.enabled';
INSERT IGNORE INTO pac_config_properties(`cfkey`,`value`,`application`,`profile`,`label`,`createdBy`,`createdDate`,`modifiedBy`,`modifiedDate`) VALUES ('features.vulnerability.enabled',concat(@VULNERABILITY_FEATURE_ENABLED,''),'api','prd','latest',NULL,NULL,NULL,NULL);


INSERT IGNORE INTO `cf_Target` (`targetName`, `targetDesc`, `category`, `dataSourceName`, `targetConfig`, `status`, `userId`, `endpoint`, `createdDate`, `modifiedDate`, `domain`) VALUES('batchaccounts','Azure batchaccounts','Compute','azure','{\"key\":\"id\",\"id\":\"id\"}','enabled','admin@pacbot.org',concat(@eshost,':',@esport,'/azure_batchaccounts/batchaccounts'),'2019-09-19','2019-09-19','Infra & Platforms');
INSERT IGNORE INTO `cf_Target` (`targetName`, `targetDesc`, `category`, `dataSourceName`, `targetConfig`, `status`, `userId`, `endpoint`, `createdDate`, `modifiedDate`, `domain`) VALUES('blobcontainer','Azure blobcontainer','Storage','azure','{\"key\":\"id\",\"id\":\"id\"}','enabled','admin@pacbot.org',concat(@eshost,':',@esport,'/azure_blobcontainer/blobcontainer'),'2019-06-27','2019-06-27','Infra & Platforms');
INSERT IGNORE INTO `cf_Target` (`targetName`, `targetDesc`, `category`, `dataSourceName`, `targetConfig`, `status`, `userId`, `endpoint`, `createdDate`, `modifiedDate`, `domain`) VALUES('cosmosdb','Azure cosmosdb','Databases','azure','{\"key\":\"id\",\"id\":\"id\"}','enabled','admin@pacbot.org',concat(@eshost,':',@esport,'/azure_cosmosdb/cosmosdb'),'2019-06-27','2019-06-27','Infra & Platforms');
INSERT IGNORE INTO `cf_Target` (`targetName`, `targetDesc`, `category`, `dataSourceName`, `targetConfig`, `status`, `userId`, `endpoint`, `createdDate`, `modifiedDate`, `domain`) VALUES('databricks','Azure databricks)','Analytics','azure','{\"key\":\"id\",\"id\":\"id\"}','enabled','admin@pacbot.org',concat(@eshost,':',@esport,'/azure_databricks/databricks'),'2019-06-27','2019-06-27','Infra & Platforms');
INSERT IGNORE INTO `cf_Target` (`targetName`, `targetDesc`, `category`, `dataSourceName`, `targetConfig`, `status`, `userId`, `endpoint`, `createdDate`, `modifiedDate`, `domain`) VALUES('disk','Azure Disk','Compute','azure','{\"key\":\"id\",\"id\":\"id\"}','enabled',NULL,concat(@eshost,':',@esport,'/azure_disk/disk'),'2019-11-05','2019-11-05','Infra & Platforms');
INSERT IGNORE INTO `cf_Target` (`targetName`, `targetDesc`, `category`, `dataSourceName`, `targetConfig`, `status`, `userId`, `endpoint`, `createdDate`, `modifiedDate`, `domain`) VALUES('loadbalancer','Azure Loadbalancer','Networking','azure','{\"key\":\"id\",\"id\":\"id\"}','enabled',NULL,concat(@eshost,':',@esport,'/azure_loadbalancer/loadbalancer'),'2019-11-05','2019-11-05','Infra & Platforms');
INSERT IGNORE INTO `cf_Target` (`targetName`, `targetDesc`, `category`, `dataSourceName`, `targetConfig`, `status`, `userId`, `endpoint`, `createdDate`, `modifiedDate`, `domain`) VALUES('mariadb','Azure mariadb','Databases','azure','{\"key\":\"id\",\"id\":\"id\"}','enabled','admin@pacbot.org',concat(@eshost,':',@esport,'/azure_mariadb/mariadb'),'2019-06-27','2019-06-27','Infra & Platforms');
INSERT IGNORE INTO `cf_Target` (`targetName`, `targetDesc`, `category`, `dataSourceName`, `targetConfig`, `status`, `userId`, `endpoint`, `createdDate`, `modifiedDate`, `domain`) VALUES('mysqlserver','Azure mysqlserver','Databases','azure','{\"key\":\"id\",\"id\":\"id\"}','enabled','admin@pacbot.org',concat(@eshost,':',@esport,'/azure_mysqlserver/mysqlserver'),'2019-06-27','2019-06-27','Infra & Platforms');
INSERT IGNORE INTO `cf_Target` (`targetName`, `targetDesc`, `category`, `dataSourceName`, `targetConfig`, `status`, `userId`, `endpoint`, `createdDate`, `modifiedDate`, `domain`) VALUES('namespaces','Azure namespaces','Web','azure','{\"key\":\"id\",\"id\":\"id\"}','enabled','admin@pacbot.org',concat(@eshost,':',@esport,'/azure_namespaces/namespaces'),'2019-09-19','2019-09-19','Infra & Platforms');
INSERT IGNORE INTO `cf_Target` (`targetName`, `targetDesc`, `category`, `dataSourceName`, `targetConfig`, `status`, `userId`, `endpoint`, `createdDate`, `modifiedDate`, `domain`) VALUES('networkinterface','Azure Network Interface','Networking','azure','{\"key\":\"id\",\"id\":\"id\"}','enabled',NULL,concat(@eshost,':',@esport,'/azure_networkinterface/networkinterface'),'2019-11-05','2019-11-05','Infra & Platforms');
INSERT IGNORE INTO `cf_Target` (`targetName`, `targetDesc`, `category`, `dataSourceName`, `targetConfig`, `status`, `userId`, `endpoint`, `createdDate`, `modifiedDate`, `domain`) VALUES('nsg','Azure Network Security Group','Networking','azure','{\"key\":\"id\",\"id\":\"id\"}','enabled',NULL,concat(@eshost,':',@esport,'/azure_nsg/nsg'),'2019-11-05','2019-11-05','Infra & Platforms');
INSERT IGNORE INTO `cf_Target` (`targetName`, `targetDesc`, `category`, `dataSourceName`, `targetConfig`, `status`, `userId`, `endpoint`, `createdDate`, `modifiedDate`, `domain`) VALUES('policydefinitions','Azure policydefinitions','Governance','azure','{\"key\":\"id\",\"id\":\"id\"}','disable','admin@pacbot.org',concat(@eshost,':',@esport,'/azure_policydefinitions/policydefinitions'),'2019-08-08','2019-08-08','Infra & Platforms');
INSERT IGNORE INTO `cf_Target` (`targetName`, `targetDesc`, `category`, `dataSourceName`, `targetConfig`, `status`, `userId`, `endpoint`, `createdDate`, `modifiedDate`, `domain`) VALUES('policyevaluationresults','Azure policyevaluationresults','Governance','azure','{\"key\":\"id,policyDefinitionId\",\"id\":\"id\"}','disable','admin@pacbot.org',concat(@eshost,':',@esport,'/azure_policyevaluationresults/policyevaluationresults'),'2019-08-08','2019-08-08','Infra & Platforms');
INSERT IGNORE INTO `cf_Target` (`targetName`, `targetDesc`, `category`, `dataSourceName`, `targetConfig`, `status`, `userId`, `endpoint`, `createdDate`, `modifiedDate`, `domain`) VALUES('postgresql','Azure postgresql','Databases','azure','{\"key\":\"id\",\"id\":\"id\"}','enabled','admin@pacbot.org',concat(@eshost,':',@esport,'/azure_postgresql/postgresql'),'2019-06-27','2019-06-27','Infra & Platforms');
INSERT IGNORE INTO `cf_Target` (`targetName`, `targetDesc`, `category`, `dataSourceName`, `targetConfig`, `status`, `userId`, `endpoint`, `createdDate`, `modifiedDate`, `domain`) VALUES('publicipaddress','Azure publicipaddress','Networking','azure','{\"key\":\"id\",\"id\":\"id\"}','enabled','admin@pacbot.org',concat(@eshost,':',@esport,'/azure_publicipaddress/publicipaddress'),'2019-07-01','2019-07-01','Infra & Platforms');
INSERT IGNORE INTO `cf_Target` (`targetName`, `targetDesc`, `category`, `dataSourceName`, `targetConfig`, `status`, `userId`, `endpoint`, `createdDate`, `modifiedDate`, `domain`) VALUES('resourcegroup','Azure resourcegroup','General','azure','{\"key\":\"id\",\"id\":\"id\"}','enabled','admin@pacbot.org',concat(@eshost,':',@esport,'/azure_resourcegroup/resourcegroup'),'2019-06-27','2019-06-27','Infra & Platforms');
INSERT IGNORE INTO `cf_Target` (`targetName`, `targetDesc`, `category`, `dataSourceName`, `targetConfig`, `status`, `userId`, `endpoint`, `createdDate`, `modifiedDate`, `domain`) VALUES('searchservices','Azure searchservices','Web','azure','{\"key\":\"id\",\"id\":\"id\"}','enabled','admin@pacbot.org',concat(@eshost,':',@esport,'/azure_searchservices/searchservices'),'2019-09-19','2019-09-19','Infra & Platforms');
INSERT IGNORE INTO `cf_Target` (`targetName`, `targetDesc`, `category`, `dataSourceName`, `targetConfig`, `status`, `userId`, `endpoint`, `createdDate`, `modifiedDate`, `domain`) VALUES('securityalerts','Azure securityalerts','Governance','azure','{\"key\":\"id\",\"id\":\"id\"}','enabled','admin@pacbot.org',concat(@eshost,':',@esport,'/azure_securityalerts/securityalerts'),'2019-08-08','2019-08-08','Infra & Platforms');
INSERT IGNORE INTO `cf_Target` (`targetName`, `targetDesc`, `category`, `dataSourceName`, `targetConfig`, `status`, `userId`, `endpoint`, `createdDate`, `modifiedDate`, `domain`) VALUES('securitycenter','Azure Security Center','Security','azure','{\"key\":\"id\",\"id\":\"id\"}','enabled',NULL,concat(@eshost,':',@esport,'/azure_securitycenter/securitycenter'),'2019-11-05','2019-11-05','Infra & Platforms');
INSERT IGNORE INTO `cf_Target` (`targetName`,`displayName`, `targetDesc`, `category`, `dataSourceName`, `targetConfig`, `status`, `userId`, `endpoint`, `createdDate`, `modifiedDate`, `domain`) VALUES('sites','Sites','Azure sites','Internet of things','azure','{\"key\":\"id\",\"id\":\"id\"}','enabled','admin@pacbot.org',concat(@eshost,':',@esport,'/azure_sites/sites'),'2019-09-19','2019-09-19','Infra & Platforms');
INSERT IGNORE INTO `cf_Target` (`targetName`, `targetDesc`, `category`, `dataSourceName`, `targetConfig`, `status`, `userId`, `endpoint`, `createdDate`, `modifiedDate`, `domain`) VALUES('sqldatabase','Azure SQL Database','Databases','azure','{\"key\":\"databaseId\",\"id\":\"databaseId\"}','enabled',NULL,concat(@eshost,':',@esport,'/azure_sqldatabase/sqldatabase'),'2019-11-05','2019-11-05','Infra & Platforms');
INSERT IGNORE INTO `cf_Target` (`targetName`, `targetDesc`, `category`, `dataSourceName`, `targetConfig`, `status`, `userId`, `endpoint`, `createdDate`, `modifiedDate`, `domain`) VALUES('sqlserver','Azure sqlserver','Databases','azure','{\"key\":\"id\",\"id\":\"id\"}','enabled',NULL,concat(@eshost,':',@esport,'/azure_sqlserver/sqlserver'),'2019-11-05','2019-11-05','Infra & Platforms');
INSERT IGNORE INTO `cf_Target` (`targetName`, `targetDesc`, `category`, `dataSourceName`, `targetConfig`, `status`, `userId`, `endpoint`, `createdDate`, `modifiedDate`, `domain`) VALUES('storageaccount','Azure Object Storage Accounts','Storage','azure','{\"key\":\"id\",\"id\":\"id\"}','enabled',NULL,concat(@eshost,':',@esport,'/azure_storageaccount/storageaccount'),'2019-11-05','2019-11-05','Infra & Platforms');
INSERT IGNORE INTO `cf_Target` (`targetName`, `targetDesc`, `category`, `dataSourceName`, `targetConfig`, `status`, `userId`, `endpoint`, `createdDate`, `modifiedDate`, `domain`) VALUES('subnets','Azure subnets','Networking','azure','{\"key\":\"id\",\"id\":\"id\"}','enabled','admin@pacbot.org',concat(@eshost,':',@esport,'/azure_subnets/subnets'),'2019-09-19','2019-09-19','Infra & Platforms');
INSERT IGNORE INTO `cf_Target` (`targetName`, `targetDesc`, `category`, `dataSourceName`, `targetConfig`, `status`, `userId`, `endpoint`, `createdDate`, `modifiedDate`, `domain`) VALUES('vaults','Azure vaults','Security','azure','{\"key\":\"id\",\"id\":\"id\"}','enabled','admin@pacbot.org',concat(@eshost,':',@esport,'/azure_vaults/vaults'),'2019-09-19','2019-09-19','Infra & Platforms');
INSERT IGNORE INTO `cf_Target` (`targetName`, `targetDesc`, `category`, `dataSourceName`, `targetConfig`, `status`, `userId`, `endpoint`, `createdDate`, `modifiedDate`, `domain`) VALUES('virtualmachine','Azure Virtual Machines','Compute','azure','{\"key\":\"vmId\",\"id\":\"id\"}','enabled',NULL,concat(@eshost,':',@esport,'/azure_virtualmachine/virtualmachine'),'2019-11-05','2019-11-05','Infra & Platforms');
INSERT IGNORE INTO `cf_Target` (`targetName`, `targetDesc`, `category`, `dataSourceName`, `targetConfig`, `status`, `userId`, `endpoint`, `createdDate`, `modifiedDate`, `domain`) VALUES('vnet','Azure Disk','Networking','azure','{\"key\":\"id\",\"id\":\"id\"}','enabled',NULL,concat(@eshost,':',@esport,'/azure_vnet/vnet'),'2019-11-05','2019-11-05','Infra & Platforms');
INSERT IGNORE INTO `cf_Target` (`targetName`, `targetDesc`, `category`, `dataSourceName`, `targetConfig`, `status`, `userId`, `endpoint`, `createdDate`, `modifiedDate`, `domain`) VALUES('workflows','Azure workflows','Internet of things','azure','{\"key\":\"id\",\"id\":\"id\"}','enabled','admin@pacbot.org',concat(@eshost,':',@esport,'/azure_workflows/workflows'),'2019-09-19','2019-09-19','Infra & Platforms');
INSERT IGNORE INTO `cf_Target` (`targetName`, `targetDesc`, `category`, `dataSourceName`, `targetConfig`, `status`, `userId`, `endpoint`, `createdDate`, `modifiedDate`, `domain`) VALUES('activitylogalert','Azure activitylog','','azure','{\"key\":\"id\",\"id\":\"id\"}','enabled','admin@pacbot.org',concat(@eshost,':',@esport,'/azure_activitylog/activitylog'),'2019-06-27','2019-06-27','Infra & Platforms');
INSERT IGNORE INTO `cf_Target` (`targetName`, `targetDesc`, `category`, `dataSourceName`, `targetConfig`, `status`, `userId`, `endpoint`, `createdDate`, `modifiedDate`, `domain`) VALUES('rediscache','Azure rediscache','','azure','{\"key\":\"id\",\"id\":\"id\"}','enabled','admin@pacbot.org',concat(@eshost,':',@esport,'/azure_rediscache/rediscache'),'2022-05-25','2022-05-25','Infra & Platforms');
INSERT IGNORE INTO `cf_Target` (`targetName`, `targetDesc`, `category`, `dataSourceName`, `targetConfig`, `status`, `userId`, `endpoint`, `createdDate`, `modifiedDate`, `domain`) VALUES('securitypricings','Azure Security Pricing','Security','azure','{\"key\":\"id\",\"id\":\"id\"}','enabled','admin@pacbot.org',concat(@eshost,':',@esport,'/azure_securitypricings/securitypricings'),'2022-05-19','2022-05-19','Infra & Platforms');
INSERT IGNORE INTO `cf_Target` (`targetName`, `targetDesc`, `category`, `dataSourceName`, `targetConfig`, `status`, `userId`, `endpoint`, `createdDate`, `modifiedDate`, `domain`) VALUES('webapp','Azure webapp','','azure','{\"key\":\"id\",\"id\":\"id\"}','enabled','admin@pacbot.org',concat(@eshost,':',@esport,'/azure_webapp/webapp'),'2019-06-27','2019-06-27','Infra & Platforms');
INSERT IGNORE INTO `cf_Target` (`targetName`,`targetDesc`,`category`,`dataSourceName`,`targetConfig`,`status`,`userId`,`endpoint`,`createdDate`,`modifiedDate`,`domain`) VALUES ('vpcfirewall','GCP VPC firewall','Compute','gcp','{"key":"id","id":"name"}','enabled','admin',concat(@eshost,':',@esport,'/gcp_vpcfirewall/vpcfirewall'),'2022-06-01','2022-06-01','Infra & Platforms');
INSERT IGNORE INTO `cf_Target` (`targetName`,`targetDesc`,`category`,`dataSourceName`,`targetConfig`,`status`,`userId`,`endpoint`,`createdDate`,`modifiedDate`,`domain`) VALUES ('cloudstorage','GCP cloud storage','Compute','gcp','{"key":"id","id":"id"}','enabled','admin',concat(@eshost,':',@esport,'/gcp_cloudstorage/cloudstorage'),'2022-06-01','2022-06-01','Infra & Platforms');
INSERT IGNORE INTO `cf_Target` (`targetName`,`targetDesc`,`category`,`dataSourceName`,`targetConfig`,`status`,`userId`,`endpoint`,`createdDate`,`modifiedDate`,`domain`) VALUES ('pubsub','GCP pub sub topic','Compute','gcp','{"key":"id","id":"id"}','enabled','admin',concat(@eshost,':',@esport,'/gcp_pubsub/pubsub'),'2022-06-29','2022-06-29','Infra & Platforms');
INSERT IGNORE INTO `cf_Target` (`targetName`,`targetDesc`,`category`,`dataSourceName`,`targetConfig`,`status`,`userId`,`endpoint`,`createdDate`,`modifiedDate`,`domain`) VALUES ('dataproc','GCP dataproc clusters','Compute','gcp','{"key":"id","id":"id"}','enabled','admin',concat(@eshost,':',@esport,'/gcp_dataproc/dataproc'),'2022-07-19','2022-07-19','Infra & Platforms');
INSERT IGNORE INTO `cf_Target` (`targetName`,`displayName`,`targetDesc`,`category`,`dataSourceName`,`targetConfig`,`status`,`userId`,`endpoint`,`createdDate`,`modifiedDate`,`domain`) VALUES ('gkecluster','GKE Cluster','GKE Cluster','Compute','gcp','{"key":"id","id":"id"}','enabled','admin',concat(@eshost,':',@esport,'/gcp_gkecluster/gkecluster'),'2022-06-29','2022-06-29','Infra & Platforms');
INSERT IGNORE INTO `cf_Target` (`targetName`,`displayName`, `targetDesc`, `category`, `dataSourceName`, `targetConfig`, `status`, `userId`, `endpoint`, `createdDate`, `modifiedDate`, `domain`) VALUES('functionapp','Function App','Azure functionapp','','azure','{\"key\":\"id\",\"id\":\"id\"}','enabled','admin@pacbot.org',concat(@eshost,':',@esport,'/azure_functionapp/functionapp'),'2019-08-23','2022-08-23','Infra & Platforms');
INSERT IGNORE INTO `cf_Target` (`targetName`,`displayName`, `targetDesc`, `category`, `dataSourceName`, `targetConfig`, `status`, `userId`, `endpoint`, `createdDate`, `modifiedDate`, `domain`) VALUES('mysqlflexible','My SQL Flexible Server','Azure mysqlflexible','','azure','{\"key\":\"id\",\"id\":\"id\"}','enabled','admin@pacbot.org',concat(@eshost,':',@esport,'/azure_mysqlflexible/mysqlflexible'),'2019-09-05','2022-09-05','Infra & Platforms');
INSERT IGNORE INTO `cf_Target` (`targetName`,`displayName`, `targetDesc`, `category`, `dataSourceName`, `targetConfig`, `status`, `userId`, `endpoint`, `createdDate`, `modifiedDate`, `domain`) VALUES('diagnosticsetting','Diagnostic Setting','Azure diagnosticsetting','','azure','{\"key\":\"id\",\"id\":\"id\"}','enabled','admin@pacbot.org',concat(@eshost,':',@esport,'/azure_diagnosticsetting/diagnosticsetting'),'2019-09-26','2022-09-26','Infra & Platforms');
INSERT IGNORE INTO `cf_Target` (`targetName`,`displayName`, `targetDesc`, `category`, `dataSourceName`, `targetConfig`, `status`, `userId`, `endpoint`, `createdDate`, `modifiedDate`, `domain`) VALUES('vaultsrbac','Vaults with role base access control','Azure vaultsrbac','','azure','{\"key\":\"id\",\"id\":\"id\"}','enabled','admin@pacbot.org',concat(@eshost,':',@esport,'/azure_vaultsrbac/vaultsrbac'),'2019-11-10','2022-11-10','Infra & Platforms');
INSERT IGNORE INTO `pac_config_properties` (`cfkey`, `value`, `application`, `profile`, `label`, `createdBy`, `createdDate`, `modifiedBy`, `modifiedDate`) values('recommendation.categories','fault_tolerance, cost_optimizing, security, performance','application','prd','latest','admin@pacbot.org','09/06/2019 06:07:43','','');

/* Update DisplayName  to TargetName if the value is null in Target table */
update cf_Target set displayName = targetName where displayName is null;

Update cf_Target set status = 'disabled' where targetName in ('asgpolicy','cloudwatchalarm','cloudwatchlogs','customergateway','datastream','deliverystream','dhcpoption','directconnect','iamgroup','internetgateway','nat','rdscluster','reservedinstance','route53','routetable','targetgroup','videostream','virtualinterface','vpnconnection','cosmosdb','diagnosticsetting','mariadb','publicipaddress','searchservices','securityalerts','sites');


/* disable policydefinition , policyevaluationresults and phd */
update  pacmandata.cf_Target set status = 'disable' where targetName in ( 'policydefinitions', 'policyevaluationresults', 'phd');
delete from cf_AssetGroupTargetDetails where targetType in ( 'policydefinitions', 'policyevaluationresults', 'phd');


/* Updating display name can be removed in future  */
Update cf_Target set displayName = 'Subnet' where targetName = 'subnet';
Update cf_Target set displayName = 'Databricks' where targetName = 'databricks';
Update cf_Target set displayName = 'AKS' where targetName = 'kubernetes';
Update cf_Target set displayName = 'MySQL Server' where targetName = 'mysqlserver';
Update cf_Target set displayName = 'Subnet' where targetName = 'subnets';
Update cf_Target set displayName = 'VM' where targetName = 'virtualmachine';
Update cf_Target set displayName = 'PostgreSQL' where targetName = 'cloudsql_postgres';
Update cf_Target set displayName = 'GKE Cluster' where targetName = 'gkecluster';
Update cf_Target set displayName = 'VM' where targetName = 'vminstance';

/* Updating  status field, can be removed in future   */
Update cf_Target set status = 'finding' where targetName in ( 'checks','phd','securityhub','activitylogalert','policydefinitions','policyevaluationresults' );


update cf_pac_updatable_fields set displayFields='_resourceid,tags.Application,tags.Environment,_entitytype,accountid,accountname,region,_cloudType' where resourceType='all_list';


UPDATE `pacmandata`.`pac_config_properties` SET `value` = concat(@MANDATORY_TAGS,'') WHERE `cfkey` = 'tagging.mandatoryTags';

/* Procedure to update metadata based on the mandatory tags configured */
DELIMITER $$

DROP PROCEDURE IF EXISTS `update_filter_for_tag` $$
CREATE PROCEDURE `update_filter_for_tag`(mandatoryTags MEDIUMTEXT)
BEGIN

DECLARE tag TEXT DEFAULT NULL;
DECLARE tagLength INT DEFAULT NULL;
DECLARE _value TEXT DEFAULT NULL;

-- delete the existing configured filters for mandatory tags
delete from pac_v2_ui_options where optionValue like '%tags%';
delete from pac_v2_ui_options where filterId=8 and optionName in ('Application','Environment');

iterator:
LOOP

  IF CHAR_LENGTH(TRIM(mandatoryTags)) = 0 OR mandatoryTags IS NULL THEN
    LEAVE iterator;
  END IF;

  -- fetch the next value from the mandatoryTags list
  SET tag = SUBSTRING_INDEX(mandatoryTags,',',1);
  SET tagLength = CHAR_LENGTH(tag);

  -- trim the value of leading and trailing spaces
  SET _value = TRIM(tag);

  -- insert the filters metadata for mandatory tags  compliance/v1/filters/tag?ag=aws&tag=tags.Environment.keyword
  INSERT IGNORE INTO pac_v2_ui_options (filterId,optionName,optionValue,optionURL) VALUES (1,_value,concat('tags.',_value,'.keyword'),concat('/compliance/v1/filters/tag?ag=aws&type=issue&tag=tags.',_value,'.keyword'));
  INSERT IGNORE INTO pac_v2_ui_options (filterId,optionName,optionValue,optionURL) VALUES (2,_value,concat('tags.',_value,'.keyword'),concat('/compliance/v1/filters/tag?ag=aws&type=issue&tag=tags.',_value,'.keyword'));
  INSERT IGNORE INTO pac_v2_ui_options (filterId,optionName,optionValue,optionURL) VALUES (3,_value,concat('tags.',_value,'.keyword'),concat('/compliance/v1/filters/tag?ag=aws&tag=tags.',_value,'.keyword'));
  INSERT IGNORE INTO pac_v2_ui_options (filterId,optionName,optionValue,optionURL) VALUES (8,_value,concat('tags.',_value,'.keyword'),concat('/compliance/v1/filters/tag?ag=aws&tag=tags.',_value,'.keyword'));
  INSERT IGNORE INTO pac_v2_ui_options (filterId,optionName,optionValue,optionURL) VALUES (9,_value,concat('tags.',_value,'.keyword'),concat('/compliance/v1/filters/tag?ag=aws&tag=tags.',_value,'.keyword'));

  SET mandatoryTags = INSERT(mandatoryTags,1,tagLength + 1,'');
END LOOP;

END $$

DELIMITER ;

CALL update_filter_for_tag(@MANDATORY_TAGS);

update pac_v2_ui_options set optionValue='policyCategory.keyword' where optionName='Category';

update pac_config_properties set value = concat(@EVENT_BRIDGE_PREFIX,'') where cfkey = 'application.prefix';

update pac_v2_ui_options set optionName='Asset Type' where optionName='Resource Type';


update cf_AssetGroupDetails set groupType = "System" where groupId in ('201','cdffb9cd-71de-4e29-9cae-783c2aa211ac','e0008397-f74e-4deb-9066-10bdf11202ae');
update cf_AssetGroupDetails set description = "Cyber asset inventory in your connected AWS Accounts." where groupId in ('201');
update cf_AssetGroupDetails set description = "Cyber asset inventory in your connected Azure Subscriptions." where groupId in ('cdffb9cd-71de-4e29-9cae-783c2aa211ac');
update cf_AssetGroupDetails set description = "Cyber asset inventory in your connected GCP Projects." where groupId in ('e0008397-f74e-4deb-9066-10bdf11202ae');


/* Procedure to update account id and account name for azure and gcp */
DELIMITER $$

DROP PROCEDURE IF EXISTS `update_displayFields_for_azure_gcp` $$
CREATE PROCEDURE `update_displayFields_for_azure_gcp`(mandatoryTags MEDIUMTEXT)
BEGIN
DECLARE tag TEXT DEFAULT NULL;
DECLARE tagLength INT DEFAULT NULL;
DECLARE _value TEXT DEFAULT NULL;
DECLARE _displayMandatory TEXT default "";
DECLARE _temp TEXT DEFAULT NULL;

iterator:
LOOP

  IF CHAR_LENGTH(TRIM(mandatoryTags)) = 0 OR mandatoryTags IS NULL THEN
    LEAVE iterator;
  END IF;

  -- fetch the next value from the mandatoryTags list
  SET tag = SUBSTRING_INDEX(mandatoryTags,',',1);
  SET tagLength = CHAR_LENGTH(tag);

  -- trim the value of leading and trailing spaces
  SET _value = TRIM(tag);


  SET _temp = concat('tags.',_value);
  SET _displayMandatory = concat(_displayMandatory,_temp,",");
  SET mandatoryTags = INSERT(mandatoryTags,1,tagLength + 1,'');



END LOOP;

update cf_pac_updatable_fields set displayFields=concat(_displayMandatory,"_resourceid,_entitytype,accountid,accountname,region,_cloudType,subscriptionName,subscription,projectName,projectId") where resourceType='all_list';

END $$

DELIMITER ;

CALL update_displayFields_for_azure_gcp(@MANDATORY_TAGS);

