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


	update ref_book_person set 
		snils = '' where id in (select person_id from ndfl_person where declaration_data_id=&1);

        select 'Clearing SNILS complete (' ||count(*)|| ' records)' from ref_book_person 	
			where id in (select person_id from ndfl_person where declaration_data_id=&1);
	commit;
exit;
