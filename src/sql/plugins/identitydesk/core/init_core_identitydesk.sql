
--
-- Data for table core_admin_right
--
DELETE FROM core_admin_right WHERE id_right = 'IDENTITYDESK_MANAGEMENT';
INSERT INTO core_admin_right (id_right,name,level_right,admin_url,description,is_updatable,plugin_name,id_feature_group,icon_url,documentation_url, id_order ) VALUES 
('IDENTITYDESK_MANAGEMENT','identitydesk.adminFeature.ManageIdentities.name',3,'jsp/admin/plugins/identitydesk/ManageIdentities.jsp','identitydesk.adminFeature.ManageIdentities.description',0,'identitydesk',NULL,NULL,NULL,4);


--
-- Data for table core_user_right
--
DELETE FROM core_user_right WHERE id_right = 'IDENTITYDESK_MANAGEMENT';
INSERT INTO core_user_right (id_right,id_user) VALUES ('IDENTITYDESK_MANAGEMENT',3);

