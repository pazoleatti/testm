insert into blob_data (id, name, data, creation_date) values ('uuid_1', 'name', 'b1', sysdate);

insert into ifrs_data (report_period_id, blob_data_id) values (20, 'uuid_1');
insert into ifrs_data (report_period_id, blob_data_id) values (21, null);