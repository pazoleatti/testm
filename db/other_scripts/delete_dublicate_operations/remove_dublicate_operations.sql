set long 32767
set pagesize 0
set linesize 4000
set feedback off
set echo off
set verify off
set trims on
set heading off
--set termout off
set serveroutput on size 1000000;

var  form_number number;


declare
	cnt number (19);
begin
	:form_number := '&1';

	select count(*) into cnt from user_tables where table_name='NDFL_PERSON_BACKUP_'||:form_number;
	if (cnt = 0) then
		execute immediate 'create table NDFL_PERSON_BACKUP_'||to_char(:form_number)||' as select *
				from NDFL_PERSON where declaration_data_id = '||to_char(:form_number);
	end if;

	select count(*) into cnt from user_tables where table_name='NDFL_PERSON_INC_BACKUP_'||:form_number;
	if (cnt = 0) then
		execute immediate 'create table NDFL_PERSON_INC_BACKUP_'||:form_number||' as select *
				from NDFL_PERSON_INCOME where ndfl_person_id in (select id from ndfl_person where declaration_data_id = '||:form_number||')';	
	end if;

	select count(*) into cnt from user_tables where table_name='NDFL_PERSON_PP_BACKUP_'||:form_number;
	if (cnt = 0) then
		execute immediate 'create table NDFL_PERSON_PP_BACKUP_'||:form_number||' as select *
				from NDFL_PERSON_PREPAYMENT where ndfl_person_id in (select id from ndfl_person where declaration_data_id = '||:form_number||')';	
	end if;

	select count(*) into cnt from user_tables where table_name='NDFL_PERSON_DED_BACKUP_'||:form_number;
	if (cnt = 0) then
		execute immediate 'create table NDFL_PERSON_DED_BACKUP_'||:form_number||' as select *
				from NDFL_PERSON_DEDUCTION where ndfl_person_id in (select id from ndfl_person where declaration_data_id = '||:form_number||')';	
	end if;


end;
/

declare
    v_np_id number(19);
begin
   loop
   select (select ndfl_person_id from ndfl_person np join ndfl_person_income  npi on npi.ndfl_person_id = np.id  
    join 
    (select declaration_data_id, npi.row_num, min(npi.id) as min_id from ndfl_person np join ndfl_person_income npi on npi.ndfl_person_id=np.id
		where declaration_data_id = :form_number
		group by declaration_data_id, npi.row_num
		having count(*)>1) npd on np.declaration_data_id = npd.declaration_data_id and npi.row_num= npd.row_num 
    and npi.id > npd.min_id 
    where np.declaration_data_id=:form_number and rownum=1) into v_np_id from dual;
    exit when v_np_id is null;
    delete from ndfl_person where id=v_np_id;
    end loop;
end;
/
commit;

exit;
