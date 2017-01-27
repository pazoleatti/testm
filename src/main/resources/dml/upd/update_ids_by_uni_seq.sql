
--таблица для установки соответствия прежних и новых id
create table tmp_upd_ids 
(	
    table_name varchar2(128 char), 
    row_id varchar2(32 char), 
    old_id number(18,0), 
    new_id number, 
    old_rec_id number(9,0), 
    new_rec_id number(9,0), 
    first_version date
);

set serveroutput on;

declare
  v_str varchar2(1000 char):='';
  v_primary_key varchar2(1000 char):='';
  f  utl_file.file_type;
begin
  --f:=utl_file.fopen('LOGDIR','change_ids.log','w');
  --utl_file.put_line(f,to_char(systimestamp,'dd.mm.yyyy hh24:mi:ss.ff3')||' СТАРТ...',true);
  dbms_output.put_line(to_char(systimestamp,'dd.mm.yyyy hh24:mi:ss.ff3')||' СТАРТ...');
  execute immediate 'truncate table tmp_upd_ids';
  for tref in (select r.id,r.name,r.table_name,
                      (select count(*) from user_tab_columns t where t.TABLE_NAME=r.table_name and t.COLUMN_NAME='RECORD_ID') has_rec_id,
                       case when id=904 then 1000
                            when id between 950 and 961 then id+96
                            when id=923 then 980
                            else id
                       end ord
                  from ref_book r
                 where r.table_name is not null
                   --and r.table_name like 'REF_BOOK%'
                   and r.table_name='REF_BOOK_ASNU'
                   and r.read_only=0
                   and r.is_versioned=1
                  order by 5) loop
   --utl_file.put_line(f,to_char(systimestamp,'dd.mm.yyyy hh24:mi:ss.ff3')||' Справочник: '||tref.name||'('||tref.table_name||')',true);
    dbms_output.put_line(to_char(systimestamp,'dd.mm.yyyy hh24:mi:ss.ff3')||' Справочник: '||tref.name||'('||tref.table_name||')');
   --utl_file.put_line(f,to_char(systimestamp,'dd.mm.yyyy hh24:mi:ss.ff3')||' отключаем внешние ключи...',true);
    dbms_output.put_line(to_char(systimestamp,'dd.mm.yyyy hh24:mi:ss.ff3')||' отключаем внешние ключи...');
     --отключаем внешние и первичные ключи
     for dkey in (select fk.table_name,fk.constraint_name,
                       'alter table '||fk.TABLE_NAME||' disable constraint '||fk.CONSTRAINT_NAME sql_disab_fk
                  from user_constraints pk join user_constraints fk on (fk.R_CONSTRAINT_NAME=pk.CONSTRAINT_NAME)
                 where pk.TABLE_NAME=upper(tref.table_name)
                   and pk.CONSTRAINT_TYPE='P') loop
      execute immediate dkey.sql_disab_fk;
    end loop;
    --utl_file.put_line(f,to_char(systimestamp,'dd.mm.yyyy hh24:mi:ss.ff3')||' - OK!',true);
    dbms_output.put_line(to_char(systimestamp,'dd.mm.yyyy hh24:mi:ss.ff3')||' - OK!');
    select 'alter table '||pk.TABLE_NAME||' disable constraint '||pk.CONSTRAINT_NAME into v_primary_key
      from user_constraints pk
     where pk.TABLE_NAME=upper(tref.table_name)
       and pk.CONSTRAINT_TYPE='P';
    if (v_primary_key<>'') then
      --utl_file.put_line(f,to_char(systimestamp,'dd.mm.yyyy hh24:mi:ss.ff3')||' отключаем первичный ключ...',true);
      dbms_output.put_line(to_char(systimestamp,'dd.mm.yyyy hh24:mi:ss.ff3')||' отключаем первичный ключ...');
      execute immediate v_primary_key;
      --utl_file.put_line(f,to_char(systimestamp,'dd.mm.yyyy hh24:mi:ss.ff3')||' - OK!',true);
      dbms_output.put_line(to_char(systimestamp,'dd.mm.yyyy hh24:mi:ss.ff3')||' - OK!');
    end if;
       
    -- меняем id
    --utl_file.put_line(f,to_char(systimestamp,'dd.mm.yyyy hh24:mi:ss.ff3')||' сохраняем старые значения id...',true);
    dbms_output.put_line(to_char(systimestamp,'dd.mm.yyyy hh24:mi:ss.ff3')||' сохраняем старые значения id...');
    execute immediate 'insert into tmp_upd_ids(table_name,row_id,old_id) select '''||tref.table_name||''',rowid,id from '||tref.table_name;
    --utl_file.put_line(f,to_char(systimestamp,'dd.mm.yyyy hh24:mi:ss.ff3')||' - OK!',true);
    dbms_output.put_line(to_char(systimestamp,'dd.mm.yyyy hh24:mi:ss.ff3')||' - OK!');
    --utl_file.put_line(f,to_char(systimestamp,'dd.mm.yyyy hh24:mi:ss.ff3')||' устанавливаем новые...',true);
    dbms_output.put_line(to_char(systimestamp,'dd.mm.yyyy hh24:mi:ss.ff3')||' устанавливаем новые...');
    execute immediate 'update '||tref.table_name||' set id=seq_ref_book_record.nextval';
    --utl_file.put_line(f,to_char(systimestamp,'dd.mm.yyyy hh24:mi:ss.ff3')||' - OK!',true);
    dbms_output.put_line(to_char(systimestamp,'dd.mm.yyyy hh24:mi:ss.ff3')||' - OK!');
    --utl_file.put_line(f,to_char(systimestamp,'dd.mm.yyyy hh24:mi:ss.ff3')||' сохраняем новые значения id...');
    dbms_output.put_line(to_char(systimestamp,'dd.mm.yyyy hh24:mi:ss.ff3')||' сохраняем новые значения id...');
    execute immediate 'update tmp_upd_ids t set new_id=(select r.id from '||tref.table_name||' r where r.rowid=t.row_id) where t.old_id is not null and t.table_name='''||tref.table_name||'''';
    --utl_file.put_line(f,to_char(systimestamp,'dd.mm.yyyy hh24:mi:ss.ff3')||' - OK!');
    dbms_output.put_line(to_char(systimestamp,'dd.mm.yyyy hh24:mi:ss.ff3')||' - OK!');
    -- меняем record_id
    if tref.has_rec_id=1 then
      --utl_file.put_line(f,to_char(systimestamp,'dd.mm.yyyy hh24:mi:ss.ff3')||' сохраняем старые значения record_id (повторяющиеся)...',true);
      dbms_output.put_line(to_char(systimestamp,'dd.mm.yyyy hh24:mi:ss.ff3')||' сохраняем старые значения record_id (повторяющиеся)...');
      execute immediate 'insert into tmp_upd_ids(table_name,old_rec_id,row_id,first_version) select '''||tref.table_name||''',record_id,min(rowid),min(version) from '||tref.table_name||' group by record_id having count(*)>1';
      --utl_file.put_line(f,to_char(systimestamp,'dd.mm.yyyy hh24:mi:ss.ff3')||' - OK!',true);
      dbms_output.put_line(to_char(systimestamp,'dd.mm.yyyy hh24:mi:ss.ff3')||' - OK!');
      --utl_file.put_line(f,to_char(systimestamp,'dd.mm.yyyy hh24:mi:ss.ff3')||' устанавливаем новые record_id кроме повторяющихся...',true);
      dbms_output.put_line(to_char(systimestamp,'dd.mm.yyyy hh24:mi:ss.ff3')||' устанавливаем новые record_id кроме повторяющихся...');
      execute immediate 'update '||tref.table_name||' r set r.record_id=seq_ref_book_record_row_id.nextval where not exists (select 1 from tmp_upd_ids t where table_name='''||tref.table_name||''' and t.old_rec_id=r.record_id)';
      --utl_file.put_line(f,to_char(systimestamp,'dd.mm.yyyy hh24:mi:ss.ff3')||' - OK!',true);
      dbms_output.put_line(to_char(systimestamp,'dd.mm.yyyy hh24:mi:ss.ff3')||' - OK!');
      --utl_file.put_line(f,to_char(systimestamp,'dd.mm.yyyy hh24:mi:ss.ff3')||' сохраняем новые значения record_id (по прежним повторяющимся)...',true);
      dbms_output.put_line(to_char(systimestamp,'dd.mm.yyyy hh24:mi:ss.ff3')||' сохраняем новые значения record_id (по прежним повторяющимся)...');
      execute immediate 'update tmp_upd_ids t set new_rec_id=seq_ref_book_record_row_id.nextval where table_name='''||tref.table_name||''' and t.old_rec_id is not null';
      --utl_file.put_line(f,to_char(systimestamp,'dd.mm.yyyy hh24:mi:ss.ff3')||' - OK!',true);
      dbms_output.put_line(to_char(systimestamp,'dd.mm.yyyy hh24:mi:ss.ff3')||' - OK!');
      --utl_file.put_line(f,to_char(systimestamp,'dd.mm.yyyy hh24:mi:ss.ff3')||' устанавливаем новые значения record_id (по повторяющимся)...',true);
      dbms_output.put_line(to_char(systimestamp,'dd.mm.yyyy hh24:mi:ss.ff3')||' устанавливаем новые значения record_id (по повторяющимся)...');
      execute immediate 'update '||tref.table_name||' t set t.record_id=(select r.new_rec_id from tmp_upd_ids r where r.old_rec_id=t.record_id) where t.record_id in (select old_rec_id from tmp_upd_ids c where c.table_name='''||tref.table_name||''' and c.old_rec_id is not null)';
      --utl_file.put_line(f,to_char(systimestamp,'dd.mm.yyyy hh24:mi:ss.ff3')||' - OK!',true);
      dbms_output.put_line(to_char(systimestamp,'dd.mm.yyyy hh24:mi:ss.ff3')||' - OK!');
    end if;
    v_primary_key:=replace(v_primary_key,'disable','enable');
    --utl_file.put_line(f,to_char(systimestamp,'dd.mm.yyyy hh24:mi:ss.ff3')||' включаем первичный ключ ('||v_primary_key||')...',true);
    dbms_output.put_line(to_char(systimestamp,'dd.mm.yyyy hh24:mi:ss.ff3')||' включаем первичный ключ ('||v_primary_key||')...');
    execute immediate v_primary_key;
    --utl_file.put_line(f,to_char(systimestamp,'dd.mm.yyyy hh24:mi:ss.ff3')||' - OK!',true);
    dbms_output.put_line(to_char(systimestamp,'dd.mm.yyyy hh24:mi:ss.ff3')||' - OK!');
    -- меняем ссылки и включаем внешние ключи
    for ekey in (select fk.table_name,fk.constraint_name,fc.column_name,
                       'alter table '||fk.TABLE_NAME||' enable constraint '||fk.constraint_name sql_enab_fk,
                       'update '||fk.TABLE_NAME||' set '||fc.COLUMN_NAME sql_upd_ref
                  from user_constraints pk join user_constraints fk on (fk.r_constraint_name=pk.constraint_name)
                                           join user_cons_columns fc on (fc.constraint_name=fk.constraint_name)
                 where pk.table_name=tref.table_name
                   and pk.constraint_type='P') loop
      --utl_file.put_line(f,to_char(systimestamp,'dd.mm.yyyy hh24:mi:ss.ff3')||' меняем ссылки в '||ekey.table_name||'...',true);
      dbms_output.put_line(to_char(systimestamp,'dd.mm.yyyy hh24:mi:ss.ff3')||' меняем ссылки в '||ekey.table_name||'...');
      for tmp in (select * from tmp_upd_ids where table_name=tref.table_name and old_id is not null) loop
        execute immediate ekey.sql_upd_ref||'='||tmp.new_id||' where '||ekey.column_name||'='||tmp.old_id;
      end loop;
      --utl_file.put_line(f,to_char(systimestamp,'dd.mm.yyyy hh24:mi:ss.ff3')||' - OK!',true);
      dbms_output.put_line(to_char(systimestamp,'dd.mm.yyyy hh24:mi:ss.ff3')||' - OK!');
      --utl_file.put_line(f,to_char(systimestamp,'dd.mm.yyyy hh24:mi:ss.ff3')||' включаем внешний ключ...',true);
      dbms_output.put_line(to_char(systimestamp,'dd.mm.yyyy hh24:mi:ss.ff3')||' включаем внешний ключ...');
      execute immediate ekey.sql_enab_fk;
      --utl_file.put_line(f,to_char(systimestamp,'dd.mm.yyyy hh24:mi:ss.ff3')||' - OK!',true);
      dbms_output.put_line(to_char(systimestamp,'dd.mm.yyyy hh24:mi:ss.ff3')||' - OK!');
    end loop;
    --utl_file.put_line(f,to_char(systimestamp,'dd.mm.yyyy hh24:mi:ss.ff3')||' Обработан!',true);
    dbms_output.put_line(to_char(systimestamp,'dd.mm.yyyy hh24:mi:ss.ff3')||' Обработан!');
    commit;
  end loop;
  --utl_file.put_line(f,to_char(systimestamp,'dd.mm.yyyy hh24:mi:ss.ff3')||' СТОП',true);
  dbms_output.put_line(to_char(systimestamp,'dd.mm.yyyy hh24:mi:ss.ff3')||' СТОП');
  --utl_file.fclose(f);
exception when others then
  v_str:=sqlerrm;
  --utl_file.put_line(f,to_char(systimestamp,'dd.mm.yyyy hh24:mi:ss.ff3')||' - ERROR!',true);
  dbms_output.put_line(to_char(systimestamp,'dd.mm.yyyy hh24:mi:ss.ff3')||' - ERROR!');
  --utl_file.put_line(f,to_char(systimestamp,'dd.mm.yyyy hh24:mi:ss.ff3')||v_str,true);
  dbms_output.put_line(to_char(systimestamp,'dd.mm.yyyy hh24:mi:ss.ff3')||v_str);
end;
/
drop table tmp_upd_ids;
