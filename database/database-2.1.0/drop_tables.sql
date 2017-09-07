declare
	v_count number;
begin
	v_count := 0;
	select count(1) into v_count from user_tables where table_name = 'TMP_DUBL_PARAMS';
	if v_count>0 then
		execute immediate 'DROP TABLE TMP_DUBL_PARAMS';
	end if;
	
	v_count := 0;
	select count(1) into v_count from user_tables where table_name = 'TMP_PERSON_DUBLES';
	if v_count>0 then
		execute immediate 'DROP TABLE TMP_PERSON_DUBLES';
	end if;
	
	for rec in (select table_name from user_tables where table_name like 'RASCHSV_%')
	loop
		for rec2 in (select constraint_name from user_constraints WHERE table_name=rec.table_name and constraint_type='R')
		loop
			execute immediate 'ALTER TABLE '||rec.table_name||' DROP CONSTRAINT '||rec2.constraint_name;
		end loop;
	end loop;

	for rec in (select table_name from user_tables where table_name like 'RASCHSV_%')
	loop
		execute immediate 'DROP TABLE '||rec.table_name;
	end loop;
	
	v_count := 0;
	select count(1) into v_count from user_tables where table_name = 'REF_BOOK_FOND_DETAIL';
	if v_count>0 then
		execute immediate 'DROP TABLE REF_BOOK_FOND_DETAIL';
	end if;
	
	v_count := 0;
	select count(1) into v_count from user_tables where table_name = 'REF_BOOK_FOND';
	if v_count>0 then
		execute immediate 'DROP TABLE REF_BOOK_FOND';
	end if;
end;
/