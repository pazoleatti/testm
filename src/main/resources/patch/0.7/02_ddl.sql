-- http://jira.aplana.com/browse/SBRFACCTAX-12090: Добавить в FORM_DATA признак актуальности сортировки и счетчик количества пронумерованных строк текущей НФ
alter table form_data add sorted number(1) default 0 not null;
alter table form_data add constraint form_data_chk_sorted check (sorted in (0, 1));
comment on column form_data.sorted is 'Признак актуальности сортировки';

alter table form_data add number_current_row number(9);
comment on column form_data.number_current_row is 'Количество пронумерованных строк текущей НФ';

-- http://jira.aplana.com/browse/SBRFACCTAX-12112: Инициализировать поле FORM_DATA.NUMBER_CURRENT_ROW
declare query_str varchar2(512);
begin
for x in (select fd.id as form_data_id, ft.id as form_template_id
			from form_template ft 
			join form_data fd on fd.form_template_id = ft.id
			where exists (select 1 from user_tab_columns utc where table_name = 'FORM_DATA_'||ft.id and column_name = 'ALIAS')) loop
query_str := 'UPDATE FORM_DATA SET number_current_row = (SELECT count(*) FROM FORM_DATA_'||x.form_template_id||' WHERE form_data_id = '||x.form_data_id||' AND (alias IS NULL OR alias LIKE ''%{wan}%'')) WHERE ID = '||x.form_data_id;
execute immediate query_str;			
end loop;
end;
/
COMMIT;


-- http://jira.aplana.com/browse/SBRFACCTAX-12103: Ограничения на form_data_ref_book
alter table form_data_ref_book add constraint form_data_ref_book_fk_refbook foreign key (ref_book_id) references ref_book(id) on delete cascade;

COMMIT;
EXIT;


