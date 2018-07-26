set serveroutput on;

begin
	insert into ref_book_person_tb(id,record_id,version,status,person_id,tb_department_id,import_date)
	select seq_ref_book_record.nextval as id,seq_ref_book_record_row_id.nextval as record_id,to_date('01.01.2017','dd.mm.yyyy') as version,0 as status,id as person_id,113 as tb_department_id,to_timestamp('01.07.2018','dd.mm.yyyy') as import_date
	from ref_book_person rbp where not exists (select * from ref_book_person_tb rbpt where rbpt.person_id=rbp.id);

	DBMS_OUTPUT.PUT_LINE('Inserted ' || SQL%ROWCOUNT || ' rows into REF_BOOK_PERSON_TB.');	
	
	commit;
end;
/

exit;
