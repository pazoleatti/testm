-- Удаление ошибочно существующих невалидных PL/SQL-объектов, относящихся к TAX_RNU
set serveroutput on size 30000;

declare query_str varchar2(1024) := '';
begin
for x in (select * from user_objects where object_name like 'APL%' or object_name like 'NSI%' and object_type in ('PROCEDURE', 'FUNCTION')) loop
	if x.status = 'VALID' then --Better safe than sorry
		dbms_output.put_line('Valid object '||x.object_name||' ('||x.object_type||') will not be deleted.');
	end if;
	if x.status = 'INVALID' then
		query_str := 'DROP '||x.object_type||' '||x.object_name;
		dbms_output.put_line(query_str||'.');
		commit;
		execute immediate query_str;
	end if;
end loop;
end;
/

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
      where exists (select 1 from user_tab_columns utc where table_name = 'FORM_DATA_'||ft.id and column_name = 'ALIAS') and
            exists (select 1 from form_column where form_template_id = ft.id and type = 'A')
      ) loop
query_str := 'UPDATE FORM_DATA SET number_current_row = (SELECT count(*) FROM FORM_DATA_'||x.form_template_id||' WHERE form_data_id = '||x.form_data_id||' AND (alias IS NULL OR alias LIKE ''%{wan}%'')) WHERE ID = '||x.form_data_id;
execute immediate query_str;      
end loop;
end;
/
COMMIT;

-- http://jira.aplana.com/browse/SBRFACCTAX-12103: Ограничения на form_data_ref_book
alter table form_data_ref_book add constraint form_data_ref_book_fk_refbook foreign key (ref_book_id) references ref_book(id) on delete cascade;

-- http://jira.aplana.com/browse/SBRFACCTAX-12214
alter table form_template add comparative number(1);
comment on column form_template.comparative is '"Признак использования периода сравнения (0 - не используется, 1 - используется)';
alter table form_template add constraint form_template_chk_comparative check (comparative in (0, 1));

-- http://jira.aplana.com/browse/SBRFACCTAX-12216
alter table form_data add (comparative_dep_rep_per_id number(18), accruing number(1));
comment on column form_data.comparative_dep_rep_per_id is 'Период сравнения';
comment on column form_data.accruing is 'Признак расчета значений нарастающим итогом (0 - не нарастающим итогом, 1 - нарастающим итогом, пустое - форма без периода сравнения)';
alter table form_data add constraint form_data_fk_co_dep_rep_per_id foreign key (comparative_dep_rep_per_id) references department_report_period (id);
alter table form_data add constraint form_data_chk_accruing check (accruing in (0, 1));



COMMIT;
EXIT;


