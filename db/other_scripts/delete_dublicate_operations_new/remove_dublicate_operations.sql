set feedback off;
set verify off;
set serveroutput on;
spool &2

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
	delete from ndfl_person_income where id in (
	select npi.id from ndfl_person np join ndfl_person_income  npi on npi.ndfl_person_id = np.id  
	    join 
		    (select declaration_data_id, npi.row_num, min(npi.id) as min_id from ndfl_person np join ndfl_person_income npi on npi.ndfl_person_id=np.id
			where declaration_data_id = :form_number
			group by declaration_data_id, npi.row_num
			having count(*)>1) npd on np.declaration_data_id = npd.declaration_data_id and npi.row_num= npd.row_num 
	    and npi.id > npd.min_id 
	    where np.declaration_data_id=:form_number);

	dbms_output.put_line ('Delete from ndfl_person_income complete ');

	delete from ndfl_person_deduction where id in 
		(select npd.id from ndfl_person_deduction npd join ndfl_person np on npd.ndfl_person_id=np.id and 
			np.declaration_data_id=:form_number
		left join ndfl_person_income npi on npi.ndfl_person_id=np.id and npi.operation_id = npd.operation_id
		where npi.id is null);

	delete from ndfl_person_deduction where id in (
	select npd.id from ndfl_person np join ndfl_person_deduction  npd on npd.ndfl_person_id = np.id  
	    join 
		    (select declaration_data_id, npd.row_num, min(npd.id) as min_id from ndfl_person np join ndfl_person_deduction npd on npd.ndfl_person_id=np.id
			where declaration_data_id = :form_number
			group by declaration_data_id, npd.row_num
			having count(*)>1) npdd on np.declaration_data_id = npdd.declaration_data_id and npdd.row_num= npd.row_num 
	    and npd.id > npdd.min_id 
	    where np.declaration_data_id=:form_number);

	dbms_output.put_line ('Delete from ndfl_person_deduction complete ');

	delete from ndfl_person_prepayment where id in 
		(select npd.id from ndfl_person_prepayment npd join ndfl_person np on npd.ndfl_person_id=np.id and 
			np.declaration_data_id=:form_number
			left join ndfl_person_income npi on npi.ndfl_person_id=np.id and npi.operation_id = npd.operation_id
		where npi.id is null);


	delete from ndfl_person_prepayment where id in (
	select npp.id from ndfl_person np join ndfl_person_prepayment  npp on npp.ndfl_person_id = np.id  
	    join 
		    (select declaration_data_id, npp.row_num, min(npp.id) as min_id from ndfl_person np join ndfl_person_prepayment npp on npp.ndfl_person_id=np.id
			where declaration_data_id = :form_number
			group by declaration_data_id, npp.row_num
			having count(*)>1) nppd on np.declaration_data_id = nppd.declaration_data_id and nppd.row_num= npp.row_num 
	    and npp.id > nppd.min_id 
	    where np.declaration_data_id=:form_number);

	dbms_output.put_line ('Delete from ndfl_person_prepayment complete');

        delete from ndfl_person np where declaration_data_id = :form_number and not exists 
		(select 1 from ndfl_person_income where ndfl_person_id = np.id);

	dbms_output.put_line ('Delete from ndfl_person complete: ');	
	
end;
/
commit;

exit;
