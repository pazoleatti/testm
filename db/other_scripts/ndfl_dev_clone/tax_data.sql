

drop trigger &1..DEPARTMENT_BEFORE_INS_UPD;
drop trigger &1..DEPARTMENT_BEFORE_DELETE;
alter table &1..department disable constraint DEPT_FK_PARENT_ID;
alter table &1..department disable constraint DEPARTMENT_FK_TYPE;
alter table &1..log_table_change disable constraint LOG_TABLE_CHANGE_FK_REF_BOOK;
alter table &1..subsystem_role disable constraint SUBSYSTEM_ROLE_FK_SUBSYSTEM;

merge into &1..DEPARTMENT a using
(select id, name, parent_id, type, shortname, tb_index, sbrf_code, region_id, is_active, code, garant_use, sunr_use, destination_parent FROM &2..DEPARTMENT) b
ON (a.id=b.id)
when matched then update set a.name=b.name, a.parent_id=b.parent_id, a.type=b.type, a.shortname=b.shortname, a.tb_index=b.tb_index,
  a.sbrf_code=b.sbrf_code, a.region_id=b.region_id, a.is_active=b.is_active, a.code=b.code, a.garant_use=b.garant_use, a.sunr_use=b.sunr_use, a.destination_parent=b.destination_parent
when NOT MATCHED THEN
INSERT (id, name, parent_id, type, shortname, tb_index, sbrf_code,region_id, is_active, code, garant_use, sunr_use, destination_parent)
VALUES (b.id, b.name, b.parent_id, b.type, b.shortname, b.tb_index, b.sbrf_code, b.region_id, b.is_active, b.code, b.garant_use, b.sunr_use, b.destination_parent);

merge into &1..SEC_ROLE a USING
(SELECT id, alias, name, is_active FROM &2..SEC_ROLE) b
ON (a.id=b.id)
when matched then update SET a.alias=b.alias, a.name=b.name, a.is_active=b.is_active
WHEN NOT MATCHED THEN
INSERT (id, alias, name, is_active)
VALUES (b.id, b.alias, b.name, b.is_active);

merge into &1..SEC_USER a USING
(SELECT id, login, name, department_id, IS_ACTIVE, email FROM &2..SEC_USER) b
ON (a.id=b.id)
when matched then update SET a.login=b.login, a.name=b.name, a.is_active=b.is_active, a.department_id=b.department_id, a.email=b.email
WHEN NOT MATCHED THEN
INSERT (id, login, name, department_id, IS_ACTIVE, email)
VALUES (b.id, b.login, b.name, b.department_id, b.IS_ACTIVE, b.email);

merge into &1..SEC_USER_ROLE a USING
(SELECT USER_ID, ROLE_ID FROM &2..SEC_USER_ROLE) b
ON (a.USER_ID=b.USER_ID AND a.ROLE_ID=b.ROLE_ID)
WHEN NOT MATCHED THEN
INSERT (USER_ID, ROLE_ID)
VALUES (b.USER_ID, b.ROLE_ID);

merge into &1..SUBSYSTEM_ROLE a USING
(SELECT SUBSYSTEM_ID, ROLE_ID FROM &2..SUBSYSTEM_ROLE) b
ON (a.SUBSYSTEM_ID=b.SUBSYSTEM_ID AND a.ROLE_ID=b.ROLE_ID)
WHEN NOT MATCHED THEN
INSERT (SUBSYSTEM_ID, ROLE_ID)
VALUES (b.SUBSYSTEM_ID, b.ROLE_ID);


merge into &1..EVENT a USING
(SELECT id, name FROM &2..EVENT) b
ON (a.ID=b.ID)
WHEN NOT MATCHED THEN
INSERT (id, name)
VALUES (b.id, b.name);

merge into &1..SUBSYSTEM a USING
(SELECT id, NAME, CODE, SHORT_NAME FROM &2..SUBSYSTEM) b
ON (a.ID=b.ID)
WHEN NOT MATCHED THEN
INSERT (id, name, code, short_name)
VALUES (b.id, b.name, b.code, b.short_name);

merge into &1..AUDIT_FORM_TYPE a USING
(SELECT id, name FROM &2..AUDIT_FORM_TYPE) b
ON (a.ID=b.ID)
WHEN NOT MATCHED THEN
INSERT (id, name)
VALUES (b.id, b.name);


merge into &1..FORM_KIND a USING
(SELECT id, name FROM &2..FORM_KIND) b
ON (a.ID=b.ID)
WHEN NOT MATCHED THEN
INSERT (id, name)
VALUES (b.id, b.name);

commit;

exit;