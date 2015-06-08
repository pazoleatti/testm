--http://jira.aplana.com/browse/SBRFACCTAX-11226: Изменение структуры хранения FORM_DATA
--связанные задачи: http://jira.aplana.com/browse/SBRFACCTAX-11448

--Вспомогательные таблицы для логирования:
create table log_clob_query (id number(9) not null primary key, form_template_id number(9), sql_mode varchar2(10), text_query clob, log_date timestamp(6) default current_timestamp not null, session_id number(18) default 0 not null);

comment on table log_clob_query is 'Логирование DDL/DML запросов из ХП';
comment on column log_clob_query.id is 'Идентификатор записи (seq_log_query)';
comment on column log_clob_query.form_template_id is 'Идентификатор шаблона';
comment on column log_clob_query.sql_mode is 'DDL/DML';
comment on column log_clob_query.text_query is 'Текст запроса';
comment on column log_clob_query.log_date is 'Дата/время начала обработки запроса';
comment on column log_clob_query.session_id is 'Идентификатор сессии (seq_log_query_session)';

create sequence seq_log_query start with 1 increment by 1;
create sequence seq_log_query_session start with 1 increment by 1;
create sequence seq_form_data_nnn start with 1 increment by 1;
----------------------------------------------------------------------------------------
create or replace procedure CREATE_FORM_DATA_NNN_ARCHIVE (P_NNN number)
is
       v_table_name varchar2(512) := ''; 
       query_str varchar2(1024); 
       x_column_type varchar2(64);
       x_column varchar2(64); 
       dml_query_str_header clob := '';     
       dml_query_str_body clob := ''; 
       dml_query clob := '';    
       chk_table number(1) :=0;
begin
       dbms_output.enable;
       for t in (select id as form_template_id, fullname as table_template_fullname from form_template where id in (P_NNN) order by id) loop
          
         v_table_name := 'FORM_DATA_'||t.form_template_id;
         
         select count(*) into chk_table from user_tables ut where ut.table_name = v_table_name;
         if chk_table <> 0 then
            insert into log_clob_query (id, form_template_id, sql_mode, text_query) values(seq_log_query.nextval, t.form_template_id, 'DDL', 'DROP TABLE '||v_table_name);
            EXECUTE IMMEDIATE 'DROP TABLE '||v_table_name;
         end if;   
            
         --DEBUG ONLY
         --EXECUTE IMMEDIATE 'DROP TABLE '||table_name;
         --delete from log_clob_query;
         
         -- Фиксированная "шапка" создания таблицы
         query_str := 'create table '||v_table_name||' ('
		 || 'ID    NUMBER(18) not null,'
         || 'FORM_DATA_ID    NUMBER(18) not null,'
         || 'TEMPORARY       NUMBER(1) not null,'
         || 'MANUAL          NUMBER(1) not null,'
		 || 'ORD             NUMBER(14) not null,'
         || 'ALIAS           VARCHAR2(20)'
         || ')';
                  insert into log_clob_query (id, form_template_id, sql_mode, text_query) values(seq_log_query.nextval, t.form_template_id, 'DDL', query_str);
         EXECUTE IMMEDIATE query_str;
                 
         
         dml_query_str_header := 'select seq_form_data_nnn.nextval, dr.form_data_id, dr.type as temporary, dr.manual, row_number() over (partition by dr.form_data_id, dr.type, dr.manual order by dr.ord) as ord, dr.alias ';
         dml_query_str_body := ' from form_data fd '
                          || ' join data_row dr on dr.form_data_id = fd.id and form_template_id = '||t.form_template_id||' ';
         
         --Цикл по form_column, относящимся к заданному form_template.id
           for x in (select id, alias || ' - ' || name as column_comment, type, max_length, precision, attribute_id, ord from form_column where form_template_id = t.form_template_id order by ord) loop     
            
               --Определить тип данных для значения (form_column.type)
              select DECODE(x.type, 'S', 'VARCHAR2(4000 BYTE)',
                          'N', 'DECIMAL(27, 10)',
                          'R', 'DECIMAL(18)',
                          'D', 'DATE',
                          'A', 'DECIMAL(18)') into x_column_type from dual;
                          
              select DECODE(x.type, 'S', 'SVALUE',
                          'N', 'NVALUE',
                          'R', 'NVALUE',
                          'D', 'DVALUE',
                          'A', 'NVALUE') into x_column from dual;
                                                
              -- Создать столбец, содержащий значение определенного типа           
              query_str := 'ALTER TABLE '||v_table_name||' ADD c'||x.Id ||' '|| x_column_type;
                        insert into log_clob_query (id, form_template_id, sql_mode, text_query) values(seq_log_query.nextval, t.form_template_id, 'DDL', query_str);
              execute immediate query_str;
            
              -- Комментарий = form_column.alias + ' - ' + form_column.name
              query_str := 'COMMENT ON COLUMN '||v_table_name ||'.c'||x.Id ||' is '''||x.column_comment||'''';
                        insert into log_clob_query (id, form_template_id, sql_mode, text_query) values(seq_log_query.nextval, t.form_template_id, 'DDL', query_str);
              execute immediate query_str;
              
              dml_query_str_header := dml_query_str_header || ', ' || ' dc'||x.ord||'.'||x_column ||' as c'||x.Id;
              dml_query_str_body := dml_query_str_body || ' left join data_cell dc'||x.ord||' on dc'||x.ord||'.row_id=dr.id and  dc'||x.ord||'.column_id = '||x.id;
            
              -- Создание столбцов 'STYLE_ID', 'EDITABLE', 'COLSPAN', 'ROWSPAN' с фиксированными метаданными
              for y in (select column_name as add_column_name, data_type || '('|| data_precision ||')' as add_column_type from user_tab_columns where table_name = 'DATA_CELL' and column_name in ('STYLE_ID', 'EDITABLE', 'COLSPAN', 'ROWSPAN') order by column_id) loop
                  query_str := 'ALTER TABLE '||v_table_name||' ADD c'||x.Id||'_'||y.add_column_name ||' '||y.add_column_type;
                            insert into log_clob_query (id, form_template_id, sql_mode, text_query) values(seq_log_query.nextval, t.form_template_id, 'DDL', query_str);
                  execute immediate query_str;
                  
                  dml_query_str_header := dml_query_str_header || ', ' || ' dc'||x.ord||'.'||y.add_column_name ||' as c'||x.Id||'_'||y.add_column_name;        
              end loop;
           end loop;
         
         --Фиксированный первичный ключ + уникальный индекс
          query_str := 'alter table '|| v_table_name ||' add constraint '||v_table_name||'_PK primary key (ID)';
               insert into log_clob_query (id, form_template_id, sql_mode, text_query) values(seq_log_query.nextval, t.form_template_id, 'DDL', query_str);   
          execute immediate query_str;
		  
		  query_str := 'create unique index i_'|| v_table_name ||'_id on '||v_table_name||' (FORM_DATA_ID, TEMPORARY, MANUAL, ORD)';
               insert into log_clob_query (id, form_template_id, sql_mode, text_query) values(seq_log_query.nextval, t.form_template_id, 'DDL', query_str);   
          execute immediate query_str;
          
          
          --Фиксированная ссылка на FORM_DATA + индекс ?
          query_str := 'alter table '|| v_table_name ||' add constraint '||v_table_name||'_FK foreign key (FORM_DATA_ID) references FORM_DATA(ID) on delete cascade';
               insert into log_clob_query (id, form_template_id, sql_mode, text_query) values(seq_log_query.nextval, t.form_template_id, 'DDL', query_str);   
          execute immediate query_str;
          
          query_str := 'create index i_'|| v_table_name||' on '|| v_table_name ||' (form_data_id)';
               insert into log_clob_query (id, form_template_id, sql_mode, text_query) values(seq_log_query.nextval, t.form_template_id, 'DDL', query_str);   
          execute immediate query_str;
          
            
          --Комментарий к таблице из названия шаблона
           query_str := 'comment on table '|| v_table_name || ' is ''' || coalesce(t.table_template_fullname, '') ||'''' ;
                insert into log_clob_query (id, form_template_id, sql_mode, text_query) values(seq_log_query.nextval, t.form_template_id, 'DDL', query_str);     
          execute immediate query_str;
                  
         
         -------------------------------------------------------------------------------------
         --Заполнение данными
         dml_query := 'insert into '||v_table_name||' '||dml_query_str_header || ' '|| dml_query_str_body;
			insert into log_clob_query (id, form_template_id, sql_mode, text_query) values(seq_log_query.nextval, t.form_template_id, 'DML', dml_query);
         execute immediate dml_query;
            insert into log_clob_query (id, form_template_id, sql_mode, text_query) values(seq_log_query.nextval, t.form_template_id, null, '--------------------------------------');
         
         -------------------------------------------------------------------------------------         
      end loop;                  
end CREATE_FORM_DATA_NNN_ARCHIVE;
/

create or replace procedure CREATE_FORM_DATA_NNN (FT_ID number)
is
       v_table_name varchar2(512) := '';
       query_str varchar2(1024);
       x_column_type varchar2(64);
       chk_table number(1) :=0;
       p_nnn number(18) := FT_ID;
     v_session_id number(18) := 0;
begin
		--Получить идентификатор текущей сессии для логирования
	    select seq_log_query_session.nextval into v_session_id from dual;
       
	   for t in (select id as form_template_id, fullname as table_template_fullname from form_template where id in (P_NNN) order by id) loop

         v_table_name := 'FORM_DATA_'||t.form_template_id;

         --Если таблица уже существует, предварительно удалить ее
         select count(*) into chk_table from user_tables ut where ut.table_name = v_table_name;
         if chk_table <> 0 then
            query_str := 'DROP TABLE '||v_table_name;
                      insert into log_clob_query (id, form_template_id, sql_mode, text_query, session_id) values(seq_log_query.nextval, t.form_template_id, 'DDL', query_str, v_session_id);
            EXECUTE IMMEDIATE query_str;
         end if;

         -- Фиксированная "шапка" создания таблицы
         query_str := 'create table '||v_table_name||' ('
         || 'ID    NUMBER(18) not null,'
         || 'FORM_DATA_ID    NUMBER(18) not null,'
         || 'TEMPORARY       NUMBER(1) not null,'
         || 'MANUAL          NUMBER(1) not null,'
         || 'ORD             NUMBER(14) not null,'
         || 'ALIAS           VARCHAR2(20)'
         || ')';

         insert into log_clob_query (id, form_template_id, sql_mode, text_query, session_id) values(seq_log_query.nextval, t.form_template_id, 'DDL', query_str, v_session_id);
         EXECUTE IMMEDIATE query_str;

        --Цикл по form_column, относящимся к заданному form_template.id
           for x in (select id, alias || ' - ' || name as column_comment, type, max_length, precision, attribute_id, ord from form_column where form_template_id = t.form_template_id order by ord) loop

               --Определить тип данных для значения (form_column.type)
              select DECODE(x.type, 'S', 'VARCHAR2(4000 BYTE)',
                          'N', 'DECIMAL(27, 10)',
                          'R', 'DECIMAL(18)',
                          'D', 'DATE',
                          'A', 'DECIMAL(18)') into x_column_type from dual;

              -- Создать столбец, содержащий значение определенного типа
              query_str := 'ALTER TABLE '||v_table_name||' ADD c'||x.Id ||' '|| x_column_type;
                        insert into log_clob_query (id, form_template_id, sql_mode, text_query, session_id) values(seq_log_query.nextval, t.form_template_id, 'DDL', query_str, v_session_id);
              execute immediate query_str;

              -- Комментарий = form_column.alias + ' - ' + form_column.name
              query_str := 'COMMENT ON COLUMN '||v_table_name ||'.c'||x.Id ||' is '''||x.column_comment||'''';
                        insert into log_clob_query (id, form_template_id, sql_mode, text_query, session_id) values(seq_log_query.nextval, t.form_template_id, 'DDL', query_str, v_session_id);
              execute immediate query_str;

              -- Создание столбцов 'STYLE_ID', 'EDITABLE', 'COLSPAN', 'ROWSPAN' с фиксированными метаданными
             query_str := 'ALTER TABLE '||v_table_name||' ADD (c'||x.Id||'_STYLE_ID NUMBER(9), c'
                          || x.Id||'_EDITABLE NUMBER(1), c'
                          || x.Id||'_COLSPAN NUMBER(3), c'
                          || x.Id||'_ROWSPAN NUMBER(3))';
                      insert into log_clob_query (id, form_template_id, sql_mode, text_query, session_id) values(seq_log_query.nextval, t.form_template_id, 'DDL', query_str, v_session_id);
             execute immediate query_str;
           end loop;

         --Фиксированный первичный ключ + уникальный индекс
          query_str := 'alter table '|| v_table_name ||' add constraint '||v_table_name||'_PK primary key (ID)';
               insert into log_clob_query (id, form_template_id, sql_mode, text_query, session_id) values(seq_log_query.nextval, t.form_template_id, 'DDL', query_str, v_session_id);
          execute immediate query_str;

          query_str := 'create unique index i_'|| v_table_name ||'_unq on '||v_table_name||' (FORM_DATA_ID, TEMPORARY, MANUAL, ORD)';
               insert into log_clob_query (id, form_template_id, sql_mode, text_query, session_id) values(seq_log_query.nextval, t.form_template_id, 'DDL', query_str, v_session_id);
          execute immediate query_str;


          --Фиксированная ссылка на FORM_DATA + индекс ?
          query_str := 'alter table '|| v_table_name ||' add constraint '||v_table_name||'_FK foreign key (FORM_DATA_ID) references FORM_DATA(ID) on delete cascade';
               insert into log_clob_query (id, form_template_id, sql_mode, text_query, session_id) values(seq_log_query.nextval, t.form_template_id, 'DDL', query_str, v_session_id);
          execute immediate query_str;

          query_str := 'create index i_'|| v_table_name||' on '|| v_table_name ||' (form_data_id)';
               insert into log_clob_query (id, form_template_id, sql_mode, text_query, session_id) values(seq_log_query.nextval, t.form_template_id, 'DDL', query_str, v_session_id);
          execute immediate query_str;


          --Комментарий к таблице из названия шаблона
           query_str := 'comment on table '|| v_table_name || ' is ''' || coalesce(t.table_template_fullname, '') ||'''' ;
                insert into log_clob_query (id, form_template_id, sql_mode, text_query, session_id) values(seq_log_query.nextval, t.form_template_id, 'DDL', query_str, v_session_id);
          execute immediate query_str;

         -------------------------------------------------------------------------------------
      end loop;
end CREATE_FORM_DATA_NNN;
/
----------------------------------------------------------------------------------------
set serveroutput on size 30000;
begin
  delete from log_clob_query;
  for x in (select id as form_template_id, fullname as table_template_fullname from form_template order by id) loop
      CREATE_FORM_DATA_NNN_ARCHIVE (x.form_template_id);
	  dbms_output.put_line('Done: '|| x.form_template_id);
	  commit;
  end loop; 
end;
/

--STATS
/*select form_template_id, min(log_date), max(log_date),
EXTRACT (HOUR   FROM (max(log_date)-min(log_date)))*60*60+
             EXTRACT (MINUTE FROM (max(log_date)-min(log_date)))*60+
             EXTRACT (SECOND FROM (max(log_date)-min(log_date))) as template_duration_sec,
sum(
EXTRACT (HOUR   FROM (max(log_date)-min(log_date)))*60*60+
             EXTRACT (MINUTE FROM (max(log_date)-min(log_date)))*60+
             EXTRACT (SECOND FROM (max(log_date)-min(log_date)))
             )
             over (order by form_template_id)/60 total_duration_min
from log_clob_query group by form_template_id
order by 2;*/
--------------------------------------------------------------------------------------
create table form_data_ref_book
(
form_data_id number(18) not null,
ref_book_id number(18) not null,
record_id number(18) not null
);

alter table form_data_ref_book add constraint form_data_ref_book_pk primary key (form_data_id, ref_book_id, record_id);
alter table form_data_ref_book add constraint form_data_ref_book_fk_formdata foreign key (form_data_id) references form_data(id) on delete cascade;

comment on table form_data_ref_book is 'Связь экземпляров НФ с элементами справочников';
comment on column form_data_ref_book.form_data_id is 'Идентификатор экземляра налоговой формы';
comment on column form_data_ref_book.ref_book_id is 'Идентификатор справочника';
comment on column form_data_ref_book.record_id is 'Идентификатор записи справочники';

insert into form_data_ref_book
select distinct dr.form_data_id, r.id, dc.nvalue from data_cell dc
join form_column fc on fc.id = dc.column_id
join data_row dr on dr.id = dc.row_id
join ref_book_attribute rba on rba.id = fc.attribute_id
join ref_book r on r.id = rba.ref_book_id
where fc.type='R' and dr.type in (0, -1) and fc.parent_column_id is null and dc.nvalue is not null;

---------------------------------------------------------------------------------------
alter table form_data add manual number(1) default 0 not null;
comment on column form_data.manual is 'Режим ввода данных (0 - не содержит версию ручного ввода; 1 - содержит)';
alter table form_data add constraint form_data_chk_manual check (manual in (0, 1));

merge into form_data tgt
using (
  select fd.id, max(dr.manual) as manual
  from form_data fd
  join data_row dr on dr.form_data_id = fd.id
  having max(dr.manual) = 1
  group by fd.id) src
on (tgt.id = src.id)
when matched then
     update set tgt.manual = src.manual;  
	 
---------------------------------------------------------------------------------------
drop table data_cell;
drop table data_row;	 
	 
COMMIT;
EXIT;
	 