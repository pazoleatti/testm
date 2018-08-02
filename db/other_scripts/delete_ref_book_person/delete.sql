set serveroutput on;
alter session set skip_unusable_indexes = true;

begin
	execute immediate 'alter index IDX_REF_PERSON_STATUS unusable';
	execute immediate 'alter index IDX_REF_BOOK_PERSON_REC_ID unusable';
	execute immediate 'alter index IDX_REF_PERSON_ST_VER_REC unusable';
	execute immediate 'alter index IDX_REF_BOOK_PERSON_ADDRESS unusable';
	execute immediate 'alter index SRCH_REF_PERSON_NAME_BRTHD unusable';
	execute immediate 'alter index SRCH_REF_BOOK_PERSON_SNILS unusable';
	execute immediate 'alter index SRCH_REF_BOOK_PERSON_INN unusable';
	execute immediate 'alter index SRCH_REF_BOOK_PERSON_INN_F unusable';
	execute immediate 'alter index SRCH_FULL_REF_PERS_DUBLE unusable';
	execute immediate 'alter table REF_BOOK_PERSON disable constraint FK_REF_BOOK_PERSON_ADDRESS';

	DBMS_OUTPUT.PUT_LINE('Indexes set to unusable, constraint disabled');
	
	delete from REF_BOOK_ID_DOC where person_id in (select id from ref_book_person r where 
	not exists(select * from NDFL_REFERENCES where person_id=r.id)
	and
	not exists(select * from NDFL_PERSON where person_id=r.id));
	
	DBMS_OUTPUT.PUT_LINE('Deleted ' || SQL%ROWCOUNT || ' rows from REF_BOOK_ID_DOC.');

	delete from REF_BOOK_ID_TAX_PAYER where person_id in (select id from ref_book_person r where 
	not exists(select * from NDFL_REFERENCES where person_id=r.id)
	and
	not exists(select * from NDFL_PERSON where person_id=r.id));
	
	DBMS_OUTPUT.PUT_LINE('Deleted ' || SQL%ROWCOUNT || ' rows from REF_BOOK_ID_TAX_PAYER.');
	
	delete from REF_BOOK_PERSON r where 
	not exists(select * from NDFL_REFERENCES where person_id=r.id)
	and
	not exists(select * from NDFL_PERSON where person_id=r.id);
	
	DBMS_OUTPUT.PUT_LINE('Deleted ' || SQL%ROWCOUNT || ' rows from REF_BOOK_PERSON.');

	delete from REF_BOOK_ADDRESS rba where not exists (select * from REF_BOOK_PERSON rbp where rbp.address=rba.id);

	DBMS_OUTPUT.PUT_LINE('Deleted ' || SQL%ROWCOUNT || ' rows from REF_BOOK_ADDRESS.');	
	
	commit;
	
    execute immediate 'alter table REF_BOOK_PERSON enable constraint FK_REF_BOOK_PERSON_ADDRESS';
	execute immediate 'alter index IDX_REF_PERSON_STATUS rebuild';
	execute immediate 'alter index IDX_REF_BOOK_PERSON_REC_ID rebuild';
	execute immediate 'alter index IDX_REF_PERSON_ST_VER_REC rebuild';
	execute immediate 'alter index IDX_REF_BOOK_PERSON_ADDRESS rebuild';
	execute immediate 'alter index SRCH_REF_PERSON_NAME_BRTHD rebuild';
	execute immediate 'alter index SRCH_REF_BOOK_PERSON_SNILS rebuild';
	execute immediate 'alter index SRCH_REF_BOOK_PERSON_INN rebuild';
	execute immediate 'alter index SRCH_REF_BOOK_PERSON_INN_F rebuild';
	execute immediate 'alter index SRCH_FULL_REF_PERS_DUBLE rebuild';
	
	DBMS_OUTPUT.PUT_LINE('Indexes rebuilded, constraint enabled');	
end;
/

exit;
