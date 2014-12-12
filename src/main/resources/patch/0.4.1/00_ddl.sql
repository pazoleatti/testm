-- http://jira.aplana.com/browse/SBRFACCTAX-9829: Переименование деклараций по ТН
ALTER TABLE declaration_template MODIFY name VARCHAR2(1000);
ALTER TABLE declaration_type MODIFY name VARCHAR2(1000);