set serveroutput on;
alter session set skip_unusable_indexes = true;

begin
	execute immediate 'alter index IDX_REF_BOOK_PERSON_REC_ID unusable';
	execute immediate 'alter index SRCH_REF_PERSON_NAME_BRTHD unusable';
	execute immediate 'alter index SRCH_REF_BOOK_PERSON_SNILS unusable';
	execute immediate 'alter index SRCH_REF_BOOK_PERSON_INN unusable';
	execute immediate 'alter index SRCH_REF_BOOK_PERSON_INN_F unusable';
	execute immediate 'alter index SRCH_FULL_REF_PERS_DUBLE unusable';
	execute immediate 'alter table REF_BOOK_PERSON disable constraint FK_REF_BOOK_PERSON_REPORT_DOC';

	DBMS_OUTPUT.PUT_LINE('Indexes set to unusable, constraint disabled');
	
	delete from REF_BOOK_ID_DOC where person_id in (select id from ref_book_person r where 
	not exists(select * from NDFL_REFERENCES where person_id=r.id)
	and
	not exists(select * from NDFL_PERSON where person_id=r.id)
	and 
    not exists(select * from DECLARATION_DATA_PERSON where person_id=r.id));
	
	DBMS_OUTPUT.PUT_LINE('Deleted ' || SQL%ROWCOUNT || ' rows from REF_BOOK_ID_DOC.');

	delete from REF_BOOK_PERSON_TB where person_id in (select id from ref_book_person r where 
	not exists(select * from NDFL_REFERENCES where person_id=r.id)
	and
	not exists(select * from NDFL_PERSON where person_id=r.id)
	and 
    not exists(select * from DECLARATION_DATA_PERSON where person_id=r.id));
	
	DBMS_OUTPUT.PUT_LINE('Deleted ' || SQL%ROWCOUNT || ' rows from REF_BOOK_PERSON_TB.');

	delete from LOG_BUSINESS where person_id in (select id from ref_book_person r where 
	not exists(select * from NDFL_REFERENCES where person_id=r.id)
	and
	not exists(select * from NDFL_PERSON where person_id=r.id)
	and 
    not exists(select * from DECLARATION_DATA_PERSON where person_id=r.id));
	
	DBMS_OUTPUT.PUT_LINE('Deleted ' || SQL%ROWCOUNT || ' rows from LOG_BUSINESS.');

	delete from REF_BOOK_ID_TAX_PAYER where person_id in (select id from ref_book_person r where 
	not exists(select * from NDFL_REFERENCES where person_id=r.id)
	and
	not exists(select * from NDFL_PERSON where person_id=r.id)
	and 
    not exists(select * from DECLARATION_DATA_PERSON where person_id=r.id));
	
	DBMS_OUTPUT.PUT_LINE('Deleted ' || SQL%ROWCOUNT || ' rows from REF_BOOK_ID_TAX_PAYER.');
	
	delete from REF_BOOK_PERSON r where 
	not exists(select * from NDFL_REFERENCES where person_id=r.id)
	and
	not exists(select * from NDFL_PERSON where person_id=r.id)
	and 
    not exists(select * from DECLARATION_DATA_PERSON where person_id=r.id);
	
	DBMS_OUTPUT.PUT_LINE('Deleted ' || SQL%ROWCOUNT || ' rows from REF_BOOK_PERSON.');

	commit;
	
	execute immediate 'alter index IDX_REF_BOOK_PERSON_REC_ID rebuild';
	execute immediate 'alter index SRCH_REF_PERSON_NAME_BRTHD rebuild';
	execute immediate 'alter index SRCH_REF_BOOK_PERSON_SNILS rebuild';
	execute immediate 'alter index SRCH_REF_BOOK_PERSON_INN rebuild';
	execute immediate 'alter index SRCH_REF_BOOK_PERSON_INN_F rebuild';
	execute immediate 'alter index SRCH_FULL_REF_PERS_DUBLE rebuild';
    execute immediate 'alter table REF_BOOK_PERSON enable constraint FK_REF_BOOK_PERSON_REPORT_DOC';
	
	DBMS_OUTPUT.PUT_LINE('Indexes rebuilded, constraint enabled');	
end;
/

exit;
