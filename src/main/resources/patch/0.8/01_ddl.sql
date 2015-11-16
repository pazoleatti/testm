--http://jira.aplana.com/browse/SBRFACCTAX-12614: 0.8 Добавить новые поля в FORM_DATA
alter table form_data add (sorted_backup number(1) default 0 not null, edited number(1) default 0 not null);
comment on column form_data.sorted_backup is 'Статус актуальности сортировки НФ для резервного среза (0 - Сортировка неактуальна; 1 - Сортировка актуальна)';
comment on column form_data.edited is 'Признак изменения данных НФ в режиме редактирования (0 - Нет изменений; 1 - Есть изменения)';

alter table form_data add constraint form_data_chk_edited check (edited in (0, 1));
alter table form_data add constraint form_data_chk_sorted_backup check (sorted_backup in (0, 1));

--http://jira.aplana.com/browse/SBRFACCTAX-12692: 0.8 Добавить в патч изменение таблиц LOCK_DATA и CONFIGURATION_LOCK
alter table lock_data modify date_lock default sysdate;
alter table lock_data drop column date_before;
drop table configuration_lock;

--http://jira.aplana.com/browse/SBRFACCTAX-12711: Обязательность заполнения для form_data.accruing
update form_data set accruing = 0 where accruing is null;
alter table form_data modify accruing default 0 not null;
comment on column form_data.accruing is 'Признак расчета значений нарастающим итогом (0 - не нарастающим итогом, 1 - нарастающим итогом)';


--http://jira.aplana.com/browse/SBRFACCTAX-12708: 0.8 БД. "Файлы и комментарии". Добавить поле COMMENT в FORM_DATA и таблицу FORM_DATA_FILE
alter table form_data add note varchar2(512);
comment on column form_data.note is 'Комментарий к НФ, вводимый в модальном окне "Файлы и комментарии"';

create table form_data_file
(
   form_data_id number(18) not null,
   blob_data_id varchar2(36) not null,
   user_name varchar2(512) not null,
   user_department_name varchar2(4000) not null,
   note varchar2(512)    
);

comment on table form_data_file is 'Файлы налоговой формы';
comment on column form_data_file.form_data_id is 'Идентификатор экземпляра налоговой формы';
comment on column form_data_file.blob_data_id is 'Файл налоговой формы';
comment on column form_data_file.user_name is 'Полное имя пользователя, прикрепившего файл';
comment on column form_data_file.user_department_name is 'Наименование подразделения пользователя, прикрепившего файл';
comment on column form_data_file.note is 'Комментарий к файлу';

alter table form_data_file add constraint form_data_file_pk primary key (blob_data_id, form_data_id);
alter table form_data_file add constraint form_data_file_fk_form_data foreign key (form_data_id) references form_data(id);
alter table form_data_file add constraint form_data_file_fk_blob_data foreign key (blob_data_id) references blob_data(id);

--http://jira.aplana.com/browse/SBRFACCTAX-12762: Ограничение для аудита
insert into event (id, name) values (904, 'Импорт скриптов');

alter table log_system drop constraint log_system_chk_rp;
alter table log_system drop constraint log_system_chk_dcl_form;

alter table log_system add constraint log_system_chk_rp check (event_id in (7, 11, 401, 402, 501, 502, 503, 601, 650, 901, 902, 903, 810, 811, 812, 813, 820, 821, 830, 831, 832, 840, 841, 842, 850, 860, 701, 702, 703, 704, 705, 904) or report_period_name is not null);
alter table log_system add constraint log_system_chk_dcl_form check (event_id in (7, 11, 401, 402, 501, 502, 503, 601, 650, 901, 902, 903, 810, 811, 812, 813, 820, 821, 830, 831, 832, 840, 841, 842, 850, 860, 701, 702, 703, 704, 705, 904) or declaration_type_name is not null or (form_type_name is not null and form_kind_id is not null));


--http://jira.aplana.com/browse/SBRFACCTAX-12760: PK для lock_data_subscribers
alter table lock_data_subscribers add constraint lock_data_subscribers_pk primary key (lock_key, user_id);

--http://jira.aplana.com/browse/SBRFACCTAX-12997: Справочник "Цвета"
create table color
(
id number(3) not null,
name varchar2(100) not null,
r number(3) not null,
g number(3) not null,
b number(3) not null, 
hex varchar2(7) not null
);

alter table color add constraint color_pk primary key(id);
alter table color add constraint color_unq_name unique(name);
alter table color add constraint color_unq_rgb unique (r,g,b);
alter table color add constraint color_unq_hex unique (hex);
alter table color add constraint color_chk_rgb_limits check ((r between 0 and 255) and (g between 0 and 255) and (b between 0 and 255));

insert all
	into color values (0,  'Черный',          0,  0,  0,  '#000000')
	into color values (4,  'Белый',          255, 255, 255, '#FFFFFF')
	into color values (1,  'Светло-желтый',      255, 255, 153, '#FFFF99')
	into color values (2,  'Светло-коричневый',    255, 204, 153,  '#FFCC99')
	into color values (3,  'Светло-голубой',      204, 255, 255,  '#CCFFFF')
	into color values (5,  'Темно-серый',        149, 149, 149,	'#959595')
	into color values (6,	'Серый',					192, 192, 192,	'#C0C0C0')
	into color values (7,	'Голубой',					153, 204, 255,	'#99CCFF')
	into color values (8,	'Светло-красный',			240, 128, 128,	'#F08080')
	into color values (9,	'Светло-оранжевый',			255, 220, 130,	'#FFDC82')
	into color values (10,	'Красный',					255, 0,	0,	'#FF0000')
	into color values (11,	'Синий',					0,	0,	255,	'#0000FF')
	into color values (12,	'Светло-зеленый',			152, 251, 152,	'#98FB98')
	into color values (13,	'Темно-зеленый',			0,	108, 0,	'#006C00')
select * from dual;	

alter table form_style add constraint form_style_fk_font_color foreign key(font_color) references color(id);
alter table form_style add constraint form_style_fk_back_color foreign key(back_color) references color(id);

--http://jira.aplana.com/browse/SBRFACCTAX-12847: Новые поля в form_template
alter table form_template add accruing number(1) default 0;
alter table form_template add updating number(1) default 0;
comment on column form_template.accruing is 'Признак расчета нарастающим итогом (0 - не используется, 1 - используется)';
comment on column form_template.updating is 'Отображать кнопку "Обновить" (0 - нет, 1 - да)';
alter table form_template add constraint form_template_chk_accruing check (accruing in (0, 1));
alter table form_template add constraint form_template_chk_updating check (updating in (0, 1));

--http://jira.aplana.com/browse/SBRFACCTAX-13269: Удаление временного среза
set serveroutput on size 30000;
declare ifTableExists varchar2(256);
begin
  for x in (select * from form_template ft where status <> 2 order by id) loop
  
       select coalesce(max(ifExists), 0) into ifTableExists from (
          select 1 as ifExists
            from dual
            where exists (select 1 from user_tables where table_name = 'FORM_DATA_'||x.id));
            
       if ifTableExists = 1 then
          execute immediate 'delete from form_data_'||x.id||' where temporary=1';
				if sql%rowcount <> 0 then
					dbms_output.put_line('Table form_data_'||x.id||': '||sql%rowcount||' rows deleted.');
				end if;
		   else
			    dbms_output.put_line('Table form_data_'||x.id||' does not exist.');
		end if;     
  end loop;
end;
/
commit;

------------------------------------------------------------------------------------------------
--http://jira.aplana.com/browse/SBRFACCTAX-13329: Увеличить кол-во значащих цифр для числовых граф до 38
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

set serveroutput on size 100000;
declare
	v_query_str varchar2(1024);
	v_session_id number(18) := 0;
	v_log_id_shift number(9);
begin
	select seq_log_query_session.nextval into v_session_id from dual;
	select seq_log_query.nextval into v_log_id_shift from dual;
	
	for x in (
		select fc.form_template_id, fc.alias, fc.id, utc.DATA_SCALE, utc.DATA_PRECISION, 'ALTER TABLE '||'FORM_DATA_'||fc.form_template_id||' MODIFY C'||fc.id ||' /* '||fc.alias ||' ('||data_precision||','||data_scale||') */ NUMBER(38, 19)' as query_str
		from form_column fc
		join user_tab_columns utc on utc.TABLE_NAME = 'FORM_DATA_'||fc.form_template_id and utc.COLUMN_NAME = 'C'||fc.id
		where type in ('N', 'A') 
		order by fc.form_template_id, fc.ord) loop
		
	v_query_str := x.query_str;	
	insert into log_clob_query (id, form_template_id, sql_mode, text_query, session_id) 
		values(seq_log_query.nextval, x.form_template_id, 'DDL_ALTER', v_query_str, v_session_id);
    execute immediate v_query_str;	
		
	end loop;	
	EXCEPTION
	WHEN OTHERS THEN
		dbms_output.put_line('Sabotage detected! ('|| SQLCODE ||' - '||SQLERRM || ' ) Executed queries (LIFO): ');
		for y in (select id, text_query from log_clob_query where session_id = v_session_id order by id desc) loop
			dbms_output.put_line('['||(y.id - v_log_id_shift)||']: '||y.text_query);
		end loop;	
end;
/

-- Create/Recreate check constraints 
alter table FORM_COLUMN drop constraint FORM_COLUMN_CHK_ATTRIBUTE_ID;
alter table FORM_COLUMN drop constraint FORM_COLUMN_CHK_MAX_LENGTH;
alter table FORM_COLUMN drop constraint FORM_COLUMN_CHK_PRECISION;
alter table REF_BOOK_ATTRIBUTE drop constraint REF_BOOK_ATTR_CHK_MAX_LENGTH;
alter table REF_BOOK_ATTRIBUTE drop constraint REF_BOOK_ATTR_CHK_PRECISION;

alter table FORM_COLUMN
  add constraint FORM_COLUMN_CHK_PRECISION
  check ((type = 'N' and precision is not null and precision >=0 and precision <= 19) or (type <> 'N' and precision is null));

alter table FORM_COLUMN
  add constraint FORM_COLUMN_CHK_ATTRIBUTE_ID
  check ((type = 'R' and attribute_id is not null) or (type <> 'R' and attribute_id is null));

alter table FORM_COLUMN
  add constraint FORM_COLUMN_CHK_MAX_LENGTH
  check ((type = 'S' and max_length is not null and max_length > 0 and max_length <= 2000) or (type = 'N' and max_length is not null and max_length > 0 and max_length <= 38 and max_length - precision<=19) or ((type ='D' or type ='R' or type='A') and max_length is null));

update ref_book_attribute set max_length = precision + 19 where max_length - precision > 19;

alter table REF_BOOK_ATTRIBUTE
  add constraint REF_BOOK_ATTR_CHK_MAX_LENGTH
  check ((type=1 and max_length is not null and max_length between 1 and 2000) or (type=2 and max_length is not null and max_length between 1 and 38 and max_length - precision<=19) or (type in (3,4) and max_length IS null));

alter table REF_BOOK_ATTRIBUTE
  add constraint REF_BOOK_ATTR_CHK_PRECISION
  check (precision >= 0 and precision <=19);
  
alter table REF_BOOK_VALUE modify NUMBER_VALUE NUMBER(38, 19);  

commit;
exit;