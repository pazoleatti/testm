create or replace function BLOB_TO_CLOB(v_blob_in in blob)
return clob is

v_file_clob clob;
v_file_size integer := 1024;
v_dest_offset integer := 1;
v_src_offset integer := 1;
v_blob_csid number :=  nls_charset_id('CL8MSWIN1251'); --dbms_lob.default_csid;
v_lang_context number := dbms_lob.default_lang_ctx;
v_warning integer;
v_size integer := 0;

begin

dbms_lob.createtemporary(v_file_clob, true);

select dbms_lob.getlength(v_blob_in) into v_size from dual;

if (v_size <= v_file_size) then v_file_size := v_size; end if;

dbms_lob.converttoclob(v_file_clob,
v_blob_in,
v_file_size,
v_dest_offset,
v_src_offset,
v_blob_csid,
v_lang_context,
v_warning);

return v_file_clob;

end;
/
---------------------------------------------------------------------------------------
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
                 
         
         dml_query_str_header := 'select seq_form_data_nnn.nextval, dr.form_data_id, 0 as temporary, dr.manual, row_number() over (partition by dr.form_data_id, dr.manual order by dr.ord) as ord, dr.alias ';
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
         dml_query := 'insert into '||v_table_name||' '||dml_query_str_header || ' '|| dml_query_str_body || ' where dr.type in (0, -1)';
			insert into log_clob_query (id, form_template_id, sql_mode, text_query) values(seq_log_query.nextval, t.form_template_id, 'DML', dml_query);
         execute immediate dml_query;
            insert into log_clob_query (id, form_template_id, sql_mode, text_query) values(seq_log_query.nextval, t.form_template_id, null, '--------------------------------------');
         
		 commit;
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

	   for t in (select id as form_template_id, translate(fullname, '''', ' ') as table_template_fullname from form_template where id in (P_NNN) order by id) loop

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
                          'N', 'DECIMAL(38, 19)',
                          'R', 'DECIMAL(18)',
                          'D', 'DATE',
                          'A', 'DECIMAL(18)') into x_column_type from dual;

              -- Создать столбец, содержащий значение определенного типа
              query_str := 'ALTER TABLE '||v_table_name||' ADD c'||x.Id ||' '|| x_column_type;
                        insert into log_clob_query (id, form_template_id, sql_mode, text_query, session_id) values(seq_log_query.nextval, t.form_template_id, 'DDL', query_str, v_session_id);
              execute immediate query_str;

              -- Комментарий = form_column.alias + ' - ' + form_column.name
              query_str := 'COMMENT ON COLUMN '||v_table_name ||'.c'||x.Id ||' is '''||translate(x.column_comment, '''', ' ') ||'''';
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

          --Индекс на FORM_DATA
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

