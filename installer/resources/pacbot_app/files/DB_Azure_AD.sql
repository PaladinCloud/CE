USE `pacmandata`;



SET @AD_TENANT_ID='$AD_TENANT_ID';
SET @AD_CLIENT_ID='$AD_CLIENT_ID';
SET @AD_SECRET_KEY='$AD_SECRET_KEY';
SET @AD_ENCRY_SECRET_KEY='$AD_ENCRY_SECRET_KEY';
SET @AD_PUBLIC_KEY_URL='$AD_PUBLIC_KEY_URL';
SET @AD_PUBLIC_KEY='$AD_PUBLIC_KEY';
SET @PACMAN_HOST_NAME='$PACMAN_HOST_NAME';
SET @AD_ADMIN_USER_ID='$AD_ADMIN_USER_ID';


delete from  pac_config_properties  where cfkey="auth.active";

INSERT IGNORE INTO pac_config_properties(`cfkey`,`value`,`application`,`profile`,`label`,`createdBy`,`createdDate`,`modifiedBy`,`modifiedDate`) VALUES ('auth.active','azuread','application','prd','latest',NULL,NULL,NULL,NULL);



insert into pac_config_properties (cfkey, value, application, profile, label, createdBy, createdDate, modifiedBy, modifiedDate) 
values ('azure.issuer', concat('https://sts.windows.net/',@AD_TENANT_ID,'/'), 'api', 'prd', 'latest', 'admin',now(), NULL, NULL) ON DUPLICATE KEY UPDATE `value`= concat('https://sts.windows.net/',@AD_TENANT_ID,'/'); 

update pac_config_properties set value = concat(@AD_CLIENT_ID,'') where cfkey="pacman.api.oauth2.client-id";

insert into pac_config_properties (cfkey,value,application,profile,label,createdBy,createdDate,modifiedBy,modifiedDate) 
values ('azure.public-key.url',concat(@AD_PUBLIC_KEY_URL,''),'api','prd','latest','admin',now(),null,null) ON DUPLICATE KEY UPDATE `value`= concat(@AD_PUBLIC_KEY_URL,'');

insert into pac_config_properties (cfkey,value,application,profile,label,createdBy,createdDate,modifiedBy,modifiedDate) 
values ('azure.public-key',concat(@AD_PUBLIC_KEY,''),'api','prd','latest','admin',now(),null,null) ON DUPLICATE KEY UPDATE `value`= concat(@AD_PUBLIC_KEY,'');

insert into pac_config_properties (cfkey,value,application,profile,label,createdby,createdDate) value ('azure.id-token.claims.user-id','unique_name','api','prd','latest','admin',now()) ON DUPLICATE KEY UPDATE `value`="unique_name";
insert into pac_config_properties (cfkey,value,application,profile,label,createdby,createdDate) value ('azure.id-token.claims.user-name','name','api','prd','latest','admin',now()) ON DUPLICATE KEY UPDATE `value`="name";
insert into pac_config_properties (cfkey,value,application,profile,label,createdby,createdDate) value ('azure.id-token.claims.first-name','given_name','api','prd','latest','admin',now()) ON DUPLICATE KEY UPDATE `value`="given_name";
insert into pac_config_properties (cfkey,value,application,profile,label,createdby,createdDate) value ('azure.id-token.claims.last-name','family_name','api','prd','latest','admin',now()) ON DUPLICATE KEY UPDATE `value`="family_name";
insert into pac_config_properties (cfkey,value,application,profile,label,createdby,createdDate) value ('azure.id-token.claims.email','unique_name','api','prd','latest','admin',now()) ON DUPLICATE KEY UPDATE `value`="email";

delete from oauth_client_details where client_id = concat(@AD_CLIENT_ID,'');
insert into oauth_client_details(`client_id`,`resource_ids`,`client_secret`,`scope`,`authorized_grant_types`,`web_server_redirect_uri`,`authorities`,`access_token_validity`,`refresh_token_validity`,`additional_information`,`autoapprove`)
 values (concat(@AD_CLIENT_ID,''),NULL,concat(@AD_ENCRY_SECRET_KEY,''),'resource-access','implicit,authorization_code,refresh_token,password,client_credentials',NULL,'ROLE_CLIENT,ROLE_USER',NULL,NULL,NULL,'') ON DUPLICATE KEY UPDATE client_secret = concat(@AD_ENCRY_SECRET_KEY,'');
 
update oauth_user_roles  set client = concat(@AD_CLIENT_ID,'') ;
update oauth_user_role_mapping set clientId = concat(@AD_CLIENT_ID,'');

delete from oauth_access_token ;
delete from oauth_refresh_token;

insert into pac_config_properties (cfkey,value,application,profile,label,createdBy,createdDate,modifiedBy,modifiedDate) 
values ('api.auth.url',concat(@PACMAN_HOST_NAME,'/api/auth/oauth/token?grant_type=client_credentials'),'api','prd','latest','admin',now(),null,null) ON DUPLICATE KEY UPDATE `value`= concat(@PACMAN_HOST_NAME,'/api/auth/oauth/token?grant_type=client_credentials');

insert into pac_config_properties (cfkey,value,application,profile,label,createdBy,createdDate,modifiedBy,modifiedDate) 
values ('api.client.id',concat(@AD_CLIENT_ID,''),'api','prd','latest','admin',now(),null,null) ON DUPLICATE KEY UPDATE `value`=concat(@AD_CLIENT_ID,'');

insert into pac_config_properties (cfkey,value,application,profile,label,createdBy,createdDate,modifiedBy,modifiedDate)
 values ('pacman.api.oauth2.client-secret',concat(@AD_SECRET_KEY,''),'auth-service','prd','latest','admin',now(),null,null) ON DUPLICATE KEY UPDATE `value`=concat(@AD_SECRET_KEY,'');
 
 insert into pac_config_properties (cfkey,value,application,profile,label,createdBy,createdDate,modifiedBy,modifiedDate)
 values ('api.client.secret',concat(@AD_SECRET_KEY,''),'api','prd','latest','admin',now(),null,null) ON DUPLICATE KEY UPDATE `value`=concat(@AD_SECRET_KEY,'');
 

insert into pac_config_properties (cfkey,value,application,profile,label,createdBy,createdDate,modifiedBy,modifiedDate) values ('azure.activedirectory.client-id',concat(@AD_CLIENT_ID,''),'api','prd','latest','admin',now(),null,null) ON DUPLICATE KEY UPDATE `value`=concat(@AD_CLIENT_ID,'');


insert into pac_config_properties (cfkey,value,application,profile,label,createdBy,createdDate,modifiedBy,modifiedDate) values ('azure.authorizeEndpoint',concat('https://login.microsoftonline.com/',@AD_TENANT_ID,'/oauth2/authorize'),'api','prd','latest','admin',now(),null,null) ON DUPLICATE KEY UPDATE `value`= concat('https://login.microsoftonline.com/',@AD_TENANT_ID,'/oauth2/authorize');

insert into pac_config_properties (cfkey,value,application,profile,label,createdBy,createdDate,modifiedBy,modifiedDate) values ('azure.activedirectory.scope','open-id','api','prd','latest','admin',now(),null,null) ON DUPLICATE KEY UPDATE `value`='open-id';

insert into oauth_user_role_mapping (userRoleId,userId,roleId,clientId,allocator,createdDate) value ('1001',concat(@AD_ADMIN_USER_ID,''),703,concat(@AD_CLIENT_ID,''),'admin' ,now()) ON DUPLICATE KEY UPDATE userId = concat(@AD_ADMIN_USER_ID,''), roleId = 703, clientId = concat(@AD_CLIENT_ID,'');


