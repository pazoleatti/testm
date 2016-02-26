insert into declaration_report (declaration_data_id, blob_data_id, type, subreport_id) values (1, 'uuid_1', 0, null);
insert into declaration_report (declaration_data_id, blob_data_id, type, subreport_id) values (1, 'uuid_2', 1, null);
insert into declaration_report (declaration_data_id, blob_data_id, type, subreport_id) values (1, 'uuid_3', 2, null);
insert into declaration_report (declaration_data_id, blob_data_id, type, subreport_id) values (1, 'uuid_4', 3, null);

insert into blob_data (id, name, data, creation_date) values ('uuid_7', 'name', 'b7', sysdate);
insert into blob_data (id, name, data, creation_date) values ('uuid_8', 'name', 'b8', sysdate);

insert into declaration_report (declaration_data_id, blob_data_id, type, subreport_id) values (2, 'uuid_5', 1, null);
insert into declaration_report (declaration_data_id, blob_data_id, type, subreport_id) values (2, 'uuid_6', 2, null);
insert into declaration_report (declaration_data_id, blob_data_id, type, subreport_id) values (2, 'uuid_7', 3, null);
insert into declaration_report (declaration_data_id, blob_data_id, type, subreport_id) values (2, 'uuid_8', 4, 1);
