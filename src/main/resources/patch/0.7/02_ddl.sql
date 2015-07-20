-- http://jira.aplana.com/browse/SBRFACCTAX-12090: Добавить в FORM_DATA признак актуальности сортировки и счетчик количества пронумерованных строк текущей НФ
alter table form_data add sorted number(1) default 0 not null;
alter table form_data add constraint form_data_chk_sorted check (sorted in (0, 1));
comment on column form_data.sorted is 'Признак актуальности сортировки';

alter table form_data add number_current_row number(9);
comment on column form_data.number_current_row is 'Количество пронумерованных строк текущей НФ';
