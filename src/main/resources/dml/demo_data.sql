insert into department (ID, NAME, PARENT_ID, TYPE) values (1, 'Банк', null, 1);
insert into department (ID, NAME, PARENT_ID, TYPE) values (2, 'ТБ1', 1, 2);
insert into department (ID, NAME, PARENT_ID, TYPE) values (3, 'ТБ2', 1, 2);

/**
 * Транспортный налог - первичная форма
 */
insert into form_type (id, name) values (1, 'Сведения о транспортных средствах, по которым уплачивается транспортный налог');
insert into form (id, type_id, version, is_active, edition) values (1, 1, '0.1', 1, 1);

insert into form_column (ID, NAME, FORM_ID, ORD, ALIAS, TYPE, EDITABLE, MANDATORY, WIDTH, PRECISION, GROUP_NAME) values (1, '№ пп', 1, 1, 'index', 'N', 0, 1, 4, 0, '');
insert into form_column (ID, NAME, FORM_ID, ORD, ALIAS, TYPE, EDITABLE, MANDATORY, WIDTH, PRECISION, GROUP_NAME) values (2, 'Код ОКАТО', 1, 2, 'okato', 'S', 1, 1, 11, null, '');
insert into form_column (ID, NAME, FORM_ID, ORD, ALIAS, TYPE, EDITABLE, MANDATORY, WIDTH, PRECISION, GROUP_NAME) values (3, 'Муниципальное образование, на территории которого зарегистрировано транспортное средство ', 1, 3, 'region', 'S', 0, 1, 15, null,  '');
insert into form_column (ID, NAME, FORM_ID, ORD, ALIAS, TYPE, EDITABLE, MANDATORY, WIDTH, PRECISION, GROUP_NAME) values (4, 'Код вида', 1, 4, 'typeCode', 'S', 1, 1, 7, null, '');
insert into form_column (ID, NAME, FORM_ID, ORD, ALIAS, TYPE, EDITABLE, MANDATORY, WIDTH, PRECISION, GROUP_NAME) values (5, 'Марка', 1, 5, 'model', 'S', 1, 1, 10, null, '');
insert into form_column (ID, NAME, FORM_ID, ORD, ALIAS, TYPE, EDITABLE, MANDATORY, WIDTH, PRECISION, GROUP_NAME) values (6, 'Категория', 1, 6, 'category', 'S', 1, 0, 8, null, '', '');
insert into form_column (ID, NAME, FORM_ID, ORD, ALIAS, TYPE, EDITABLE, MANDATORY, WIDTH, PRECISION, GROUP_NAME) values (7, 'Идентификационный номер ТС (VIN)', 1, 7, 'vin', 'S', 1, 1, 12, null, '');
insert into form_column (ID, NAME, FORM_ID, ORD, ALIAS, TYPE, EDITABLE, MANDATORY, WIDTH, PRECISION, GROUP_NAME) values (8, 'Государственный регистрационный знак ТС', 1, 8, 'number', 'S', 1, 1, 6, null, '');
insert into form_column (ID, NAME, FORM_ID, ORD, ALIAS, TYPE, EDITABLE, MANDATORY, WIDTH, PRECISION, GROUP_NAME) values (9, 'Дата регистрации ТС', 1, 9, 'registrationDate', 'D', 1, 1, 8, null, '');
insert into form_column (ID, NAME, FORM_ID, ORD, ALIAS, TYPE, EDITABLE, MANDATORY, WIDTH, PRECISION, GROUP_NAME) values (10, 'Дата снятия с регистрации ТС', 1, 10, 'withdrawalDate', 'D', 1, 0, 8, null, '');
insert into form_column (ID, NAME, FORM_ID, ORD, ALIAS, TYPE, EDITABLE, MANDATORY, WIDTH, PRECISION, GROUP_NAME) values (11, 'Мощность (л.с.)', 1, 11, 'power', 'N', 1, 1, 4, 0, '');
insert into form_column (ID, NAME, FORM_ID, ORD, ALIAS, TYPE, EDITABLE, MANDATORY, WIDTH, PRECISION, GROUP_NAME) values (12, 'Год изготовления ТС', 1, 12, 'manufacturingYear', 'N', 1, 1, 4, 0, '');
insert into form_column (ID, NAME, FORM_ID, ORD, ALIAS, TYPE, EDITABLE, MANDATORY, WIDTH, PRECISION, GROUP_NAME) values (13, 'Дата начала розыска ТС', 1, 13, 'searchDate', 'D', 1, 1, 8, null, '');
insert into form_column (ID, NAME, FORM_ID, ORD, ALIAS, TYPE, EDITABLE, MANDATORY, WIDTH, PRECISION, GROUP_NAME) values (14, 'Дата возврата ТС', 1, 14, 'returnDate', 'D', 1, 1, 8, null, '');



/**
 * Транспортный налог - сводная форма
 */
insert into form_type (id, name) values (2, 'Расчет суммы налога по каждому транспортному средству');
insert into form (id, type_id, version, is_active, edition) values (2, 2,'0.1', 1, 1);

insert into form_column (ID, NAME, FORM_ID, ORD, ALIAS, TYPE, EDITABLE, MANDATORY, WIDTH, PRECISION, GROUP_NAME) values (101, 'Код по ОКАТО', 2, 1, 'okato', 'S', 0, 1, 11, null, '');
insert into form_column (ID, NAME, FORM_ID, ORD, ALIAS, TYPE, EDITABLE, MANDATORY, WIDTH, PRECISION, GROUP_NAME) values (102, 'Код вида ТС', 2, 2, 'typeCode', 'S', 0, 1, 5, null, '');
insert into form_column (ID, NAME, FORM_ID, ORD, ALIAS, TYPE, EDITABLE, MANDATORY, WIDTH, PRECISION, GROUP_NAME) values (103, 'Идентификационный номер ТС (VIN)', 2, 3, 'vin', 'S', 0, 1, 17, null, '');
insert into form_column (ID, NAME, FORM_ID, ORD, ALIAS, TYPE, EDITABLE, MANDATORY, WIDTH, PRECISION, GROUP_NAME) values (104, 'Марка ТС', 2, 4, 'model', 'S', 0, 1, 20, null, '');
insert into form_column (ID, NAME, FORM_ID, ORD, ALIAS, TYPE, EDITABLE, MANDATORY, WIDTH, PRECISION, GROUP_NAME) values (105, 'Государственный регистрационный знак ТС', 2, 5, 'number', 'S', 0, 1, 6, null, '');
insert into form_column (ID, NAME, FORM_ID, ORD, ALIAS, TYPE, EDITABLE, MANDATORY, WIDTH, PRECISION, GROUP_NAME) values (106, 'Налоговая база', 2, 6, 'taxBase', 'N', 0, 1, 6, 2, '');
insert into form_column (ID, NAME, FORM_ID, ORD, ALIAS, TYPE, EDITABLE, MANDATORY, WIDTH, PRECISION, GROUP_NAME) values (107, 'Единица измерения налоговой базы по ОКЕИ', 2, 7, 'okei', 'N', 0, 1, 6, 0, '');
insert into form_column (ID, NAME, FORM_ID, ORD, ALIAS, TYPE, EDITABLE, MANDATORY, WIDTH, PRECISION, GROUP_NAME) values (108, 'Срок использования (полных лет)', 2, 8, 'years', 'N', 0, 1, 3, 0, '');
insert into form_column (ID, NAME, FORM_ID, ORD, ALIAS, TYPE, EDITABLE, MANDATORY, WIDTH, PRECISION, GROUP_NAME) values (109, 'Коэффициент, определяемый в соответствии с п.3 ст. 362 НК РФ', 2, 9, 'coef362', 'N', 0, 1, 4, 2, '');
insert into form_column (ID, NAME, FORM_ID, ORD, ALIAS, TYPE, EDITABLE, MANDATORY, WIDTH, PRECISION, GROUP_NAME) values (110, 'Ставка налога', 2, 10, 'taxRate', 'N', 0, 1, 4, 0, '');
insert into form_column (ID, NAME, FORM_ID, ORD, ALIAS, TYPE, EDITABLE, MANDATORY, WIDTH, PRECISION, GROUP_NAME) values (111, 'Исчисленная сумма налога', 2, 11, 'calculatedTaxSum', 'N', 0, 1, 6, 2, '');
insert into form_column (ID, NAME, FORM_ID, ORD, ALIAS, TYPE, EDITABLE, MANDATORY, WIDTH, PRECISION, GROUP_NAME) values (112, 'Код налоговой льготы', 2, 12, 'privelegeCode', 'N', 1, 1, 4, 0, '');
insert into form_column (ID, NAME, FORM_ID, ORD, ALIAS, TYPE, EDITABLE, MANDATORY, WIDTH, PRECISION, GROUP_NAME) values (113, 'Сумма налоговой льготы', 2, 13, 'privelegeSum', 'N', 1, 1, 4, 0, '');
insert into form_column (ID, NAME, FORM_ID, ORD, ALIAS, TYPE, EDITABLE, MANDATORY, WIDTH, PRECISION, GROUP_NAME) values (114, 'Исчисленная сумма налога, подлежащая уплате в бюджет', 2, 14, 'taxSumToPay', 'N', 0, 1, 6, 0, '');

insert into department_form_type (DEPARTMENT_ID, FORM_TYPE_ID) values (1, 1);
insert into department_form_type (DEPARTMENT_ID, FORM_TYPE_ID) values (1, 2);
insert into department_form_type (DEPARTMENT_ID, FORM_TYPE_ID) values (2, 1);
insert into department_form_type (DEPARTMENT_ID, FORM_TYPE_ID) values (2, 2);
insert into department_form_type (DEPARTMENT_ID, FORM_TYPE_ID) values (3, 1);
insert into department_form_type (DEPARTMENT_ID, FORM_TYPE_ID) values (3, 2);