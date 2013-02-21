insert into dict_region (code,name) values ('01','name');
insert into department_param (department_id, dict_region_id, okato, inn, kpp, tax_organ_code, okved_code, phone, reorg_form_code, reorg_inn, reorg_kpp) values (1, '01', 'a', 1, 'd', 'd','d','f','c','f','kpp');
insert into department_param (department_id, dict_region_id, okato, inn, kpp, tax_organ_code, okved_code, phone, reorg_form_code, reorg_inn, reorg_kpp) values (2, '01', 'a', 1, 'd', 'd','d','f','c','f','kpp');
insert into department_param (department_id, dict_region_id, okato, inn, kpp, tax_organ_code, okved_code, phone, reorg_form_code, reorg_inn, reorg_kpp) values (3, '01', 'a', 1, 'd', 'd','d','f','c','f','kpp');

insert into department_param_income (DEPARTMENT_ID, APPROVE_DOC_NAME, APPROVE_ORG_NAME, CORRECTION_SUM, EXTERNAL_TAX_SUM, SIGNATORY_FIRSTNAME, SIGNATORY_ID, SIGNATORY_LASTNAME, SIGNATORY_SURNAME, TAX_PLACE_TYPE_CODE, TAX_RATE, SUM_DIFFERENCE) values (1, '01', 'a', 1, 1, 'd', 1,'f','c','213',1,1);
insert into department_param_transport (DEPARTMENT_ID, APPROVE_DOC_NAME, APPROVE_ORG_NAME, SIGNATORY_FIRSTNAME, SIGNATORY_ID, SIGNATORY_LASTNAME, SIGNATORY_SURNAME, TAX_PLACE_TYPE_CODE) values (1, '01','f','d',1,'l','s','213');

