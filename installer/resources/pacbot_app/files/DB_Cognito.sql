USE `pacmandata`;


delete from  pac_config_properties  where cfkey="auth.active";
INSERT IGNORE INTO pac_config_properties(`cfkey`,`value`,`application`,`profile`,`label`,`createdBy`,`createdDate`,`modifiedBy`,`modifiedDate`) VALUES ('auth.active','cognito','application','prd','latest',NULL,NULL,NULL,NULL);


drop table if exists `role_permissions`;
drop table if exists `role`;
drop table if exists `permission`;

CREATE TABLE IF NOT EXISTS `role` (
  `role_id` int(20) NOT NULL AUTO_INCREMENT,
  `role_name` varchar(200) DEFAULT NULL,
  `role_description` varchar(200) DEFAULT NULL,
  PRIMARY KEY (`role_id`)
) ENGINE=InnoDB AUTO_INCREMENT=340 DEFAULT CHARSET=latin1;

CREATE TABLE IF NOT EXISTS `permission` (
  `permission_id` int(20) NOT NULL AUTO_INCREMENT,
  `permission_name` varchar(200) DEFAULT NULL,
  `permission_description` varchar(200) DEFAULT NULL,
  `is_admin` boolean DEFAULT false,
  PRIMARY KEY (`permission_id`)
) ENGINE=InnoDB AUTO_INCREMENT=340 DEFAULT CHARSET=latin1;

CREATE TABLE `role_permissions` (
  `role_id` int(20) NOT NULL,
  `permission_id` int(20) NOT NULL,
  PRIMARY KEY (`role_id`,`permission_id`),
  KEY `role_id` (`role_id`),
  CONSTRAINT `role_permissions_ibfk_1`
   FOREIGN KEY (`role_id`) REFERENCES `role` (`role_id`),
  CONSTRAINT `role_permissions_ibfk_2`
   FOREIGN KEY (`permission_id`) REFERENCES `permission` (`permission_id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;


INSERT IGNORE INTO `role`(`role_id`,`role_name`,`role_description`) VALUES (1,'ROLE_USER','PaladinCloud role for non-admin read only user');
INSERT IGNORE INTO `role`(`role_id`,`role_name`,`role_description`) VALUES (2,'ROLE_ADMIN','PaladinCloud role for admin user');


INSERT IGNORE INTO `permission`(`permission_id`,`permission_name`,`permission_description`,`is_admin`) VALUES (1,'asset-group-management','Permission to perform admin operations on asset groups', true);
INSERT IGNORE INTO `permission`(`permission_id`,`permission_name`,`permission_description`,`is_admin`) VALUES (2,'exemption-management','Permission to perform admin operations on issue exemption', true);
INSERT IGNORE INTO `permission`(`permission_id`,`permission_name`,`permission_description`,`is_admin`) VALUES (3,'rule-severity-management','Permission to modify rule severity', true);
INSERT IGNORE INTO `permission`(`permission_id`,`permission_name`,`permission_description`,`is_admin`) VALUES (4,'rules-security','Permission to enable/disable rule belonging to security category', true);
INSERT IGNORE INTO `permission`(`permission_id`,`permission_name`,`permission_description`,`is_admin`) VALUES (5,'security-log-management','Permission to perform admin operations on security logs', true);
INSERT IGNORE INTO `permission`(`permission_id`,`permission_name`,`permission_description`,`is_admin`) VALUES (6,'compliance-management','Permission to perform admin operations on compliance', true);
INSERT IGNORE INTO `permission`(`permission_id`,`permission_name`,`permission_description`,`is_admin`) VALUES (7,'user-management','Permission to perform admin operations on user and roles', true);
INSERT IGNORE INTO `permission`(`permission_id`,`permission_name`,`permission_description`,`is_admin`) VALUES (8,'connector-management','Permission to perform admin operations on connectors', true);
INSERT IGNORE INTO `permission`(`permission_id`,`permission_name`,`permission_description`,`is_admin`) VALUES (9,'rule-admin','Permission to perform admin operations on rules, except severity of rule', true);
INSERT IGNORE INTO `permission`(`permission_id`,`permission_name`,`permission_description`,`is_admin`) VALUES (10,'rules-technical-admin','Permission to enable/disable rule belonging to categories- Cost, Operations and Tagging', true);
INSERT IGNORE INTO `permission`(`permission_id`,`permission_name`,`permission_description`,`is_admin`) VALUES (11,'policy-management','Permission to perform admin operations on policies', true);
INSERT IGNORE INTO `permission`(`permission_id`,`permission_name`,`permission_description`,`is_admin`) VALUES (12,'job-execution-management','Permission to perform admin operations on job execution',true);
INSERT IGNORE INTO `permission`(`permission_id`,`permission_name`,`permission_description`,`is_admin`) VALUES (13,'domain-management','Permission to perform admin operations on domain',true);
INSERT IGNORE INTO `permission`(`permission_id`,`permission_name`,`permission_description`,`is_admin`) VALUES (14,'target-type-management','Permission to perform admin operations on target types',true);
INSERT IGNORE INTO `permission`(`permission_id`,`permission_name`,`permission_description`,`is_admin`) VALUES (15,'configuration-management','Permission to perform admin operations on configurations',true);
INSERT IGNORE INTO `permission`(`permission_id`,`permission_name`,`permission_description`,`is_admin`) VALUES (16,'system-management','Permission to perform admin operations on system-management',true);
INSERT IGNORE INTO `permission`(`permission_id`,`permission_name`,`permission_description`,`is_admin`) VALUES (17,'relationship-data','Permission to perform admin operations on relationship-data',true);
INSERT IGNORE INTO `permission`(`permission_id`,`permission_name`,`permission_description`,`is_admin`) VALUES (18,'operational-metrics','Permission to access Operational metrics- uptime, maintenance, updates',true);
INSERT IGNORE INTO `permission`(`permission_id`,`permission_name`,`permission_description`,`is_admin`) VALUES (19,'activity-log','Permission to manage activity logs',true);
INSERT IGNORE INTO `permission`(`permission_id`,`permission_name`,`permission_description`,`is_admin`) VALUES (20,'readonly','Read only permission',false);

INSERT IGNORE INTO `role_permissions`(`role_id`,`permission_id`) VALUES (1,20);


INSERT IGNORE INTO `role_permissions`(`role_id`,`permission_id`) VALUES (2,1);
INSERT IGNORE INTO `role_permissions`(`role_id`,`permission_id`) VALUES (2,2);
INSERT IGNORE INTO `role_permissions`(`role_id`,`permission_id`) VALUES (2,3);
INSERT IGNORE INTO `role_permissions`(`role_id`,`permission_id`) VALUES (2,4);
INSERT IGNORE INTO `role_permissions`(`role_id`,`permission_id`) VALUES (2,5);
INSERT IGNORE INTO `role_permissions`(`role_id`,`permission_id`) VALUES (2,6);
INSERT IGNORE INTO `role_permissions`(`role_id`,`permission_id`) VALUES (2,7);
INSERT IGNORE INTO `role_permissions`(`role_id`,`permission_id`) VALUES (2,8);
INSERT IGNORE INTO `role_permissions`(`role_id`,`permission_id`) VALUES (2,9);
INSERT IGNORE INTO `role_permissions`(`role_id`,`permission_id`) VALUES (2,10);
INSERT IGNORE INTO `role_permissions`(`role_id`,`permission_id`) VALUES (2,11);
INSERT IGNORE INTO `role_permissions`(`role_id`,`permission_id`) VALUES (2,12);
INSERT IGNORE INTO `role_permissions`(`role_id`,`permission_id`) VALUES (2,13);
INSERT IGNORE INTO `role_permissions`(`role_id`,`permission_id`) VALUES (2,14);
INSERT IGNORE INTO `role_permissions`(`role_id`,`permission_id`) VALUES (2,15);
INSERT IGNORE INTO `role_permissions`(`role_id`,`permission_id`) VALUES (2,16);
INSERT IGNORE INTO `role_permissions`(`role_id`,`permission_id`) VALUES (2,17);
INSERT IGNORE INTO `role_permissions`(`role_id`,`permission_id`) VALUES (2,18);
INSERT IGNORE INTO `role_permissions`(`role_id`,`permission_id`) VALUES (2,19);
INSERT IGNORE INTO `role_permissions`(`role_id`,`permission_id`) VALUES (2,20);


/* testing*/