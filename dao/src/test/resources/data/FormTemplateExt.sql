INSERT INTO department_type (id, name) VALUES (1, 'Банк');
INSERT INTO department (id, name, parent_id, type, shortname, tb_index, sbrf_code, region_id, code, garant_use)
  VALUES (1, 'Банк', NULL, 1, NULL, NULL, '12', 1, 1, 0);
INSERT INTO department_report_period(id, department_id, report_period_id, is_active, is_balance_period) VALUES (1, 1, 1, 1, 0);
INSERT INTO form_kind (id, name) VALUES (3, 'Сводная');
INSERT INTO form_data(id, form_template_id, department_report_period_id, state, kind, return_sign) VALUES (1, 1, 1, 1, 3, 0);

INSERT INTO form_data_row(id, form_data_id, temporary, manual, ord, alias, c0, c1)
  SELECT 1, 1, 0, 0, 1, trim('testAlias'), trim('aaa'), 111 FROM DUAL UNION
  SELECT 2, 1, 0, 0, 2, null,              trim('bbbb'), 222 FROM DUAL UNION
  SELECT 3, 1, 0, 0, 3, trim('alias 3'),   trim('ccccc'), 333 FROM DUAL UNION
  SELECT 4, 1, 0, 0, 4, trim('alias 4'),   trim('dddddd'), 444 FROM DUAL;