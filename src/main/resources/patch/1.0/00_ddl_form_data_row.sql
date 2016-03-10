--http://jira.aplana.com/browse/SBRFACCTAX-14414: Изменить структуру хранения НФ
alter session set NLS_NUMERIC_CHARACTERS = '.,';
alter session set NLS_DATE_FORMAT = 'dd.MM.yyyy HH24:MI:ss';

SET SERVEROUTPUT ON SIZE 1000000;

ALTER TABLE form_column ADD data_ord NUMBER(2);
COMMENT ON COLUMN form_column.data_ord IS 'Порядковый номер столбца в таблице данных';

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

alter table form_data_row add constraint form_data_row_pk primary key(id);
alter table form_data_row add constraint form_data_row_fk_form_data foreign key(form_data_id) references form_data(id) on delete cascade;
alter table form_data_row add constraint form_data_row_unq unique (FORM_DATA_ID, TEMPORARY, MANUAL, ORD);
alter table form_data_row add constraint form_data_row_chk_temp check (TEMPORARY in (0, 1)) ;
alter table form_data_row add constraint form_data_row_chk_manual check (MANUAL in (0, 1)) ;

create or replace function get_style (style_id number, editable number, colspan number, rowspan number) return varchar2 is
res varchar2(100);
begin
    res := TRIM(TRAILING ';' FROM (CASE WHEN NOT style_id IS NULL THEN 's' || style_id || ';' END ||
                    CASE WHEN editable = 1 THEN 'e;' END ||
                    CASE WHEN NOT colspan IS NULL AND colspan <> 1 THEN 'c' || colspan || ';' END ||
                    CASE WHEN NOT rowspan IS NULL AND rowspan <> 1 THEN 'r' || rowspan END));
    
    return (res);
end;
/

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
begin
  --Получить идентификатор текущей сессии для логирования
	    select seq_log_query_session.nextval into v_session_id from dual;
		insert into log_clob_query (id, form_template_id, sql_mode, session_id) values(seq_log_query.nextval, 0, 'DDL', v_session_id);
		
  for x in (select table_name, ft.id from user_tables ut full outer join form_template ft on 'FORM_DATA_'||ft.id = ut.table_name where regexp_like(table_name, '^FORM_DATA_[0-9]+$') order by ft.id nulls first, table_name) loop
      if (x.id is null) then
         dbms_output.put_line(x.table_name || ' - no corresponding ID in form_template. Table will be just dropped.');   
		 execute immediate 'DROP TABLE '|| x.table_name;
      elsif (x.table_name is not null) then          
         insert_header := 'insert into form_data_row (id, form_data_id, temporary, manual, ord, alias';
         insert_body := 'select id, form_data_id, temporary, manual, ord, alias';
         for y in (select id, data_ord from form_column fc where fc.form_template_id = x.id order by data_ord) loop
			insert_header := insert_header || ', C' || y.data_ord || ', C'||y.data_ord||'_style';
            insert_body := insert_body || ', C' || y.id ||'  AS c'||y.data_ord ||', get_style(C'|| y.id ||'_style_id, C'|| y.id ||'_editable, C'|| y.id ||'_colspan, C'|| y.id ||'_rowspan) as C'||y.data_ord||'_style';  	
		 end loop;
         insert_body := insert_body || ' from '||x.table_name ||' t where exists (select 1 from form_data fd where fd.id = t.form_data_id) and temporary = 0';
		 insert_header := insert_header || ')';
      end if;
      
      query_stmt := insert_header || insert_body;
	  if (query_stmt is not null) then
		  insert into log_clob_query (id, form_template_id, sql_mode, text_query, session_id) values(seq_log_query.nextval, x.id, 'DDL', query_stmt, v_session_id);       
		  execute immediate query_stmt;
		  cnt_rows := sql%rowcount;
		  update log_clob_query set rows_affected = cnt_rows where form_template_id = x.id;
	  end if;	  
	
	  commit;	
  end loop;
  
  for dt in (select table_name from user_tables where regexp_like(table_name, '^FORM_DATA_[0-9]+$')) loop
	query_stmt := 'DROP TABLE '||dt.table_name;
	execute immediate query_stmt;
	dbms_output.put_line(query_stmt);
  end loop;
end;
/

drop procedure create_form_data_nnn;
drop sequence seq_form_data_nnn;
drop function get_style;
drop package body form_data_nnn;
drop package form_data_nnn;

drop view v_log_clob_query;
drop table log_clob_query;
drop sequence seq_log_query;
drop sequence seq_log_query_session; 


COMMIT;
EXIT;