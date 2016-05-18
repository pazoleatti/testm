--http://jira.aplana.com/browse/SBRFACCTAX-14414: Изменить структуру хранения НФ
alter session set NLS_NUMERIC_CHARACTERS = '.,';
alter session set NLS_DATE_FORMAT = 'dd.MM.yyyy HH24:MI:ss';

set serveroutput on size 1000000;
begin dbms_output.put_line('Script start: ' || current_timestamp); end; 
/

ALTER TABLE form_column ADD data_ord NUMBER(2);
COMMENT ON COLUMN form_column.data_ord IS 'Порядковый номер столбца в таблице данных';

--https://jira.aplana.com/browse/SBRFACCTAX-15158: добавить в FORM_COLUMN строковое поле SHORT_NAME
alter table form_column add short_name varchar2(1000);
comment on column form_column.short_name is 'Краткое наименование';

MERGE INTO form_column tgt
USING (select id, row_number() over (partition by form_template_id order by ord) - 1 as data_ord from form_column) src
ON (tgt.id = src.id)
WHEN MATCHED THEN
  UPDATE SET tgt.data_ord = src.data_ord;

ALTER TABLE form_column MODIFY data_ord NOT NULL;
ALTER TABLE form_column ADD CONSTRAINT form_column_unique_data_ord UNIQUE (form_template_id, data_ord);
ALTER TABLE form_column ADD CONSTRAINT form_column_check_data_ord CHECK (data_ord between 0 and 99);

---------------------------------------------------------------------------------------------------------------
DECLARE 
	seq_form_data_nnn_nextval number(18);
BEGIN
	select seq_form_data_nnn.nextval into seq_form_data_nnn_nextval from dual;
	execute immediate 'create sequence seq_form_data_row start with '|| seq_form_data_nnn_nextval;
END;
/
	
CREATE TABLE form_data_row (
	id NUMBER(18),
	form_data_id NUMBER(18),
	temporary NUMBER(1),
	manual NUMBER(1),
	ord NUMBER(14),
	alias VARCHAR2(20 char),
	c0 VARCHAR2(2000 char),	c0_style VARCHAR2(50 char),
	c1 VARCHAR2(2000 char),	c1_style VARCHAR2(50 char),
	c2 VARCHAR2(2000 char),	c2_style VARCHAR2(50 char),
	c3 VARCHAR2(2000 char),	c3_style VARCHAR2(50 char),	
	c4 VARCHAR2(2000 char),	c4_style VARCHAR2(50 char),
	c5 VARCHAR2(2000 char),	c5_style VARCHAR2(50 char),
	c6 VARCHAR2(2000 char),	c6_style VARCHAR2(50 char),
	c7 VARCHAR2(2000 char),	c7_style VARCHAR2(50 char),	
	c8 VARCHAR2(2000 char),	c8_style VARCHAR2(50 char),
	c9 VARCHAR2(2000 char),	c9_style VARCHAR2(50 char),
	c10 VARCHAR2(2000 char), c10_style VARCHAR2(50 char),
	c11 VARCHAR2(2000 char), c11_style VARCHAR2(50 char),
	c12 VARCHAR2(2000 char), c12_style VARCHAR2(50 char),
	c13 VARCHAR2(2000 char), c13_style VARCHAR2(50 char),	
	c14 VARCHAR2(2000 char), c14_style VARCHAR2(50 char),
	c15 VARCHAR2(2000 char), c15_style VARCHAR2(50 char),
	c16 VARCHAR2(2000 char), c16_style VARCHAR2(50 char),
	c17 VARCHAR2(2000 char), c17_style VARCHAR2(50 char),	
	c18 VARCHAR2(2000 char), c18_style VARCHAR2(50 char),
	c19 VARCHAR2(2000 char), c19_style VARCHAR2(50 char),
	c20 VARCHAR2(2000 char), c20_style VARCHAR2(50 char),
	c21 VARCHAR2(2000 char), c21_style VARCHAR2(50 char),
	c22 VARCHAR2(2000 char), c22_style VARCHAR2(50 char),
	c23 VARCHAR2(2000 char), c23_style VARCHAR2(50 char),	
	c24 VARCHAR2(2000 char), c24_style VARCHAR2(50 char),
	c25 VARCHAR2(2000 char), c25_style VARCHAR2(50 char),
	c26 VARCHAR2(2000 char), c26_style VARCHAR2(50 char),
	c27 VARCHAR2(2000 char), c27_style VARCHAR2(50 char),	
	c28 VARCHAR2(2000 char), c28_style VARCHAR2(50 char),
	c29 VARCHAR2(2000 char), c29_style VARCHAR2(50 char),
	c30 VARCHAR2(2000 char), c30_style VARCHAR2(50 char),
	c31 VARCHAR2(2000 char), c31_style VARCHAR2(50 char),
	c32 VARCHAR2(2000 char), c32_style VARCHAR2(50 char),
	c33 VARCHAR2(2000 char), c33_style VARCHAR2(50 char),	
	c34 VARCHAR2(2000 char), c34_style VARCHAR2(50 char),
	c35 VARCHAR2(2000 char), c35_style VARCHAR2(50 char),
	c36 VARCHAR2(2000 char), c36_style VARCHAR2(50 char),
	c37 VARCHAR2(2000 char), c37_style VARCHAR2(50 char),	
	c38 VARCHAR2(2000 char), c38_style VARCHAR2(50 char),
	c39 VARCHAR2(2000 char), c39_style VARCHAR2(50 char),
	c40 VARCHAR2(2000 char), c40_style VARCHAR2(50 char),
	c41 VARCHAR2(2000 char), c41_style VARCHAR2(50 char),
	c42 VARCHAR2(2000 char), c42_style VARCHAR2(50 char),
	c43 VARCHAR2(2000 char), c43_style VARCHAR2(50 char),	
	c44 VARCHAR2(2000 char), c44_style VARCHAR2(50 char),
	c45 VARCHAR2(2000 char), c45_style VARCHAR2(50 char),
	c46 VARCHAR2(2000 char), c46_style VARCHAR2(50 char),
	c47 VARCHAR2(2000 char), c47_style VARCHAR2(50 char),	
	c48 VARCHAR2(2000 char), c48_style VARCHAR2(50 char),
	c49 VARCHAR2(2000 char), c49_style VARCHAR2(50 char),
	c50 VARCHAR2(2000 char), c50_style VARCHAR2(50 char),
	c51 VARCHAR2(2000 char), c51_style VARCHAR2(50 char),
	c52 VARCHAR2(2000 char), c52_style VARCHAR2(50 char),
	c53 VARCHAR2(2000 char), c53_style VARCHAR2(50 char),	
	c54 VARCHAR2(2000 char), c54_style VARCHAR2(50 char),
	c55 VARCHAR2(2000 char), c55_style VARCHAR2(50 char),
	c56 VARCHAR2(2000 char), c56_style VARCHAR2(50 char),
	c57 VARCHAR2(2000 char), c57_style VARCHAR2(50 char),	
	c58 VARCHAR2(2000 char), c58_style VARCHAR2(50 char),
	c59 VARCHAR2(2000 char), c59_style VARCHAR2(50 char),
	c60 VARCHAR2(2000 char), c60_style VARCHAR2(50 char),
	c61 VARCHAR2(2000 char), c61_style VARCHAR2(50 char),
	c62 VARCHAR2(2000 char), c62_style VARCHAR2(50 char),
	c63 VARCHAR2(2000 char), c63_style VARCHAR2(50 char),	
	c64 VARCHAR2(2000 char), c64_style VARCHAR2(50 char),
	c65 VARCHAR2(2000 char), c65_style VARCHAR2(50 char),
	c66 VARCHAR2(2000 char), c66_style VARCHAR2(50 char),
	c67 VARCHAR2(2000 char), c67_style VARCHAR2(50 char),	
	c68 VARCHAR2(2000 char), c68_style VARCHAR2(50 char),
	c69 VARCHAR2(2000 char), c69_style VARCHAR2(50 char),
	c70 VARCHAR2(2000 char), c70_style VARCHAR2(50 char),
	c71 VARCHAR2(2000 char), c71_style VARCHAR2(50 char),
	c72 VARCHAR2(2000 char), c72_style VARCHAR2(50 char),
	c73 VARCHAR2(2000 char), c73_style VARCHAR2(50 char),	
	c74 VARCHAR2(2000 char), c74_style VARCHAR2(50 char),
	c75 VARCHAR2(2000 char), c75_style VARCHAR2(50 char),
	c76 VARCHAR2(2000 char), c76_style VARCHAR2(50 char),
	c77 VARCHAR2(2000 char), c77_style VARCHAR2(50 char),	
	c78 VARCHAR2(2000 char), c78_style VARCHAR2(50 char),
	c79 VARCHAR2(2000 char), c79_style VARCHAR2(50 char),
	c80 VARCHAR2(2000 char), c80_style VARCHAR2(50 char),
	c81 VARCHAR2(2000 char), c81_style VARCHAR2(50 char),
	c82 VARCHAR2(2000 char), c82_style VARCHAR2(50 char),
	c83 VARCHAR2(2000 char), c83_style VARCHAR2(50 char),	
	c84 VARCHAR2(2000 char), c84_style VARCHAR2(50 char),
	c85 VARCHAR2(2000 char), c85_style VARCHAR2(50 char),
	c86 VARCHAR2(2000 char), c86_style VARCHAR2(50 char),
	c87 VARCHAR2(2000 char), c87_style VARCHAR2(50 char),	
	c88 VARCHAR2(2000 char), c88_style VARCHAR2(50 char),
	c89 VARCHAR2(2000 char), c89_style VARCHAR2(50 char),
	c90 VARCHAR2(2000 char), c90_style VARCHAR2(50 char),
	c91 VARCHAR2(2000 char), c91_style VARCHAR2(50 char),
	c92 VARCHAR2(2000 char), c92_style VARCHAR2(50 char),
	c93 VARCHAR2(2000 char), c93_style VARCHAR2(50 char),	
	c94 VARCHAR2(2000 char), c94_style VARCHAR2(50 char),
	c95 VARCHAR2(2000 char), c95_style VARCHAR2(50 char),
	c96 VARCHAR2(2000 char), c96_style VARCHAR2(50 char),
	c97 VARCHAR2(2000 char), c97_style VARCHAR2(50 char),	
	c98 VARCHAR2(2000 char), c98_style VARCHAR2(50 char),
	c99 VARCHAR2(2000 char), c99_style VARCHAR2(50 char)
);

create or replace function get_style (style_id varchar2, editable number) return varchar2 is
res varchar2(100);
begin
    res := CASE WHEN editable = 1 THEN 'e' END || CASE WHEN NOT style_id IS NULL THEN style_id END;
    return (res);
end;
/

--https://jira.aplana.com/browse/SBRFACCTAX-15026: Реализовать постраничное отображение\редактирование объединенных по вертикали строк
create table form_data_row_span
(
  row_id       number(18) not null,
  form_data_id number(9) not null,
  temporary    number(1) not null,
  manual       number(1) not null,
  data_ord     number(3) not null,
  ord          number(9) not null,
  colspan      number(9),
  rowspan      number(9)
);

comment on table form_data_row_span is 'Информация по горизонтальном/вертикальном объединении ячеек в НФ';
comment on column form_data_row_span.row_id is 'Идентификатор записи из таблицы form_data_row';
comment on column form_data_row_span.form_data_id is 'Идентификатор НФ';
comment on column form_data_row_span.manual is 'Версия ручного ввода';
comment on column form_data_row_span.temporary is 'Резервный/временный срез';
comment on column form_data_row_span.ord is 'Позиция в строке';
comment on column form_data_row_span.data_ord is 'Позиция в столбце';
comment on column form_data_row_span.colspan is 'Объединение по горизонтали';
comment on column form_data_row_span.rowspan is 'Объединение по вертикали';

-----------------------------------------------------------------------------------------------------
alter table log_clob_query add rows_affected number(9);

create or replace view v_log_clob_query as
with t as (select form_template_id, length(text_query) as query_length, rows_affected, log_date as td1, lead(log_date) over (order by log_date) as td2 from log_clob_query)
select t.*,
EXTRACT (HOUR   FROM (td2-td1))*60*60+
             EXTRACT (MINUTE FROM (td2-td1))*60+
             EXTRACT (SECOND FROM (td2-td1)) as template_duration_sec,
sum(
EXTRACT (HOUR   FROM (td2-td1))*60*60+
             EXTRACT (MINUTE FROM (td2-td1))*60+
             EXTRACT (SECOND FROM (td2-td1)))
             over (order by form_template_id)/60 total_duration_min
from t order by form_template_id;

truncate table log_clob_query;

--Перенос данных
declare 
	query_stmt varchar2(32767) := '';
	insert_header varchar2(32767) := '';
	insert_body varchar2(32767) := '';
	v_session_id number(18);
	cnt_rows number(18);
	decode_str varchar2(512);
	decode_stmt varchar2(512);
	form_data_row_span_stmt varchar2(512);
begin
  --Получить идентификатор текущей сессии для логирования
	    select seq_log_query_session.nextval into v_session_id from dual;
		insert into log_clob_query (id, form_template_id, sql_mode, session_id) values(seq_log_query.nextval, 0, 'DDL', v_session_id);
  
  dbms_output.enable (buffer_size => null);
		
  for x in (select table_name, ft.id from user_tables ut full outer join form_template ft on 'FORM_DATA_'||ft.id = ut.table_name where regexp_like(table_name, '^FORM_DATA_[0-9]+$') order by ft.id nulls first, table_name) loop
      if (x.id is null) then
         dbms_output.put_line(x.table_name || ' - no corresponding ID in form_template. Table will be just dropped.');   
		 execute immediate 'DROP TABLE '|| x.table_name;
      elsif (x.table_name is not null) then          
         insert_header := 'insert into form_data_row (id, form_data_id, temporary, manual, ord, alias';
         insert_body := 'select id, form_data_id, temporary, manual, ord, alias';
		 
		 decode_str := 'select listagg('', ''|| id || '', '''''' || case when italic = 1 then ''i'' end || case when bold = 1 then ''b'' end || fs.font_color ||''-''||fs.back_color|| '''''''') within group (order by id) || '', '''''''')'' as style from form_style fs where form_template_id = '||x.id;
		 execute immediate decode_str into decode_stmt;
		 
		 if (decode_stmt = ', '''')') then decode_stmt := ', '''', '''', '''')'; end if;
		 
         for y in (select id, data_ord from form_column fc where fc.form_template_id = x.id order by data_ord) loop
			insert_header := insert_header || ', C' || y.data_ord || ', C'||y.data_ord||'_style';
            insert_body := insert_body || ', C' || y.id ||'  AS c'||y.data_ord ||', get_style(decode(C'|| y.id ||'_style_id' || decode_stmt||', C'|| y.id ||'_editable) as C'||y.data_ord||'_style';  	
			
			--Отдельно стили по colspan / rowspan
			form_data_row_span_stmt := 'insert into form_data_row_span (row_id, form_data_id, temporary, manual, data_ord, ord, colspan, rowspan) select id, form_data_id, temporary, manual, '||y.data_ord||' as data_ord, ord, C'||y.id||'_COLSPAN, C'||y.id||'_ROWSPAN FROM '||x.table_name ||' t where exists (select 1 from form_data fd where fd.id = t.form_data_id) and temporary = 0 and (C'||y.id||'_COLSPAN <> 1 or C'||y.id||'_ROWSPAN <> 1)';
			execute immediate form_data_row_span_stmt;	 
		 end loop;
         insert_body := insert_body || ' from '||x.table_name ||' t where exists (select 1 from form_data fd where fd.id = t.form_data_id) and temporary = 0';
		 insert_header := insert_header || ')';
		 
      end if;
      
      query_stmt := insert_header || insert_body;
	  if (query_stmt is not null) then
		  insert into log_clob_query (id, form_template_id, sql_mode, text_query, session_id) values(seq_log_query.nextval, x.id, 'DDL', query_stmt, v_session_id); 
		  commit;		  
		  execute immediate query_stmt;
		  cnt_rows := sql%rowcount;
		  update log_clob_query set rows_affected = cnt_rows where form_template_id = x.id;
	  end if;	  
	
	  commit;	
  end loop;
  
  for dt in (select table_name from user_tables where regexp_like(table_name, '^FORM_DATA_[0-9]+$') order by table_name) loop
	query_stmt := 'DROP TABLE '||dt.table_name;
	execute immediate query_stmt;
	dbms_output.put_line(dt.table_name || ' dropped.');
  end loop;
end;
/

update form_data_row_span set colspan = null where colspan = 1;
update form_data_row_span set rowspan = null where rowspan = 1;

-- Констрейнты на новые таблицы
alter table form_data_row add constraint form_data_row_pk primary key(id);
alter table form_data_row add constraint form_data_row_fk_form_data foreign key(form_data_id) references form_data(id) on delete cascade;
alter table form_data_row add constraint form_data_row_unq unique (FORM_DATA_ID, TEMPORARY, MANUAL, ORD);
alter table form_data_row add constraint form_data_row_chk_temp check (TEMPORARY in (0, 1)) ;
alter table form_data_row add constraint form_data_row_chk_manual check (MANUAL in (0, 1)) ;

alter table form_data_row_span add constraint form_data_row_span_fk_row foreign key(row_id) references form_data_row(id) on delete cascade;
create index i_form_data_row_span_fk_row on form_data_row_span (row_id);
alter table form_data_row_span add constraint form_data_row_span_fk foreign key (form_data_id) references form_data (id);
alter table form_data_row_span add constraint form_data_row_span_unq unique (form_data_id, temporary, manual, data_ord, ord);
alter table form_data_row_span add constraint form_data_row_span_chk_dataord check (data_ord between 0 and 99);
------------------------------------------------------------------------------------------------------------------------------------------

select sum(rows_affected), max(total_duration_min) from v_log_clob_query;

drop procedure create_form_data_nnn;
drop sequence seq_form_data_nnn;
drop function get_style;
drop package body form_data_nnn;
drop package form_data_nnn;

drop view v_log_clob_query;
drop table log_clob_query;
drop sequence seq_log_query;
drop sequence seq_log_query_session; 
drop procedure delete_form_template;
drop procedure delete_form_type;

--https://jira.aplana.com/browse/SBRFACCTAX-15119: Удалить из таблицы FORM_STYLE поле ID
alter table form_style drop constraint form_style_pk;
alter table form_style drop constraint form_style_uniq_alias;
alter table form_style drop column id;
alter table form_style add constraint form_style_pk primary key (form_template_id, alias);

--https://jira.aplana.com/browse/SBRFACCTAX-15121: Создать таблицу STYLE
create table style (
  alias				     varchar2(50 char) not null,
  font_color			 number(3) null,   
  back_color			 number(3) null,  
  italic				 number(1) not null,
  bold				     number(1) not null
);
comment on table style is 'Стили ячеек в налоговой форме';
comment on column style.alias is 'Алиас стиля';
comment on column style.font_color is 'Код цвета шрифта';
comment on column style.back_color is 'Код цвета фона';
comment on column style.italic is 'Признак использования курсива';
comment on column style.bold is 'Признак жирного шрифта';

alter table style add constraint style_pk primary key(alias);
alter table style add constraint style_fk_color_font foreign key(font_color) references color(id);
alter table style add constraint style_fk_color_back foreign key(back_color) references color(id);
alter table style add constraint style_chk_italic check(italic in (0, 1));
alter table style add constraint style_chk_bold check(bold in (0, 1));

MERGE INTO style t USING (
    SELECT 'Автозаполняемая' ALIAS, 13 FONT_COLOR, 4 BACK_COLOR, 0 ITALIC, 1 BOLD FROM DUAL UNION ALL
    SELECT 'Контрольные суммы' ALIAS, 0 FONT_COLOR, 2 BACK_COLOR, 0 ITALIC, 1 BOLD FROM DUAL UNION ALL
    SELECT 'Корректировка-без изменений' ALIAS, 0 FONT_COLOR, 6 BACK_COLOR, 0 ITALIC, 0 BOLD FROM DUAL UNION ALL
    SELECT 'Корректировка-добавлено' ALIAS, 0 FONT_COLOR, 12 BACK_COLOR, 0 ITALIC, 0 BOLD FROM DUAL UNION ALL
    SELECT 'Корректировка-изменено' ALIAS, 10 FONT_COLOR, 4 BACK_COLOR, 0 ITALIC, 1 BOLD FROM DUAL UNION ALL
    SELECT 'Корректировка-удалено' ALIAS, 0 FONT_COLOR, 8 BACK_COLOR, 0 ITALIC, 0 BOLD FROM DUAL UNION ALL
    SELECT 'Редактируемая' ALIAS, 0 FONT_COLOR, 3 BACK_COLOR, 0 ITALIC, 0 BOLD FROM DUAL) s ON
    (t.alias = s.alias)
WHEN MATCHED THEN UPDATE SET 
    t.FONT_COLOR = s.FONT_COLOR,
    t.BACK_COLOR = s.BACK_COLOR,
    t.ITALIC = s.ITALIC,
    t.BOLD = s.BOLD
WHEN NOT MATCHED THEN INSERT (t.alias, t.font_color, t.back_color, t.italic, t.bold)
VALUES
    (s.alias, s.font_color, s.back_color, s.italic, s.bold);
	
begin dbms_output.put_line('Script end:   ' || current_timestamp); end; 
/

COMMIT;
EXIT;