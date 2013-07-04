-- tax period
insert into tax_period (id, tax_type, start_date, end_date) values (1, 'T', to_date('01.01.02', 'DD.MM.RR'), to_date('31.12.02', 'DD.MM.RR'));
insert into dict_tax_period (code, name, I, T, P, V, D) values ('21', 'первый квартал', 1, 1, 1, 1, 0);

-- department
insert into department (id, name, parent_id, type, shortname, dict_region_id, tb_index, sbrf_code) values (1, 'банк', null, 1, null, null, null, null);
insert into department (id, name, parent_id, type, shortname, dict_region_id, tb_index, sbrf_code) values (2, 'банк2', 1, 2, null, null, null, null);

-- report period
insert into report_period (id, name, is_active, months, tax_period_id, ord, department_id, dict_tax_period_id) values (1, '2002 - 1 квартал', 1, 3, 1, 1, 1, 21);
insert into report_period (id, name, is_active, months, tax_period_id, ord, department_id, dict_tax_period_id) values (2, '2002 - 2 квартал', 0, 3, 1, 2, 1, 21);

-- income101 data
insert into income_102 (report_period_id,  opu_code,  total_sum,  department_id) values (1, '2', 666, 1);
insert into income_102 (report_period_id,  opu_code,  total_sum,  department_id) values (1, '2', 666, 2);
insert into income_102 (report_period_id,  opu_code,  total_sum,  department_id) values (2, '2', 555, 1);
insert into income_102 (report_period_id,  opu_code,  total_sum,  department_id) values (1, '3', 444, 1);