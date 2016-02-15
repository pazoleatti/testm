insert into blob_data (id, name, data, creation_date) values ('uuid_1', 'name', 'b1', sysdate);
insert into blob_data (id, name, data, creation_date) values ('uuid_2', 'name', 'b2', sysdate - 0.1);
insert into blob_data (id, name, data, creation_date) values ('uuid_3', 'name', 'b3', sysdate - 1.1);
insert into blob_data (id, name, data, creation_date) values ('uuid_4', 'name', 'b4', sysdate - 10);

insert into blob_data (id, name, data, creation_date) values ('uuid_5', 'name', 'b5', sysdate - 10);
insert into blob_data (id, name, data, creation_date) values ('uuid_6', 'name', 'b6', sysdate - 10);

insert into form_data_report (form_data_id, blob_data_id, type, checking, manual, absolute) values (1, 'uuid_5', 0, 0, 0, 0);
insert into declaration_report (declaration_data_id, blob_data_id, type) values (2, 'uuid_6', 'XLSX');