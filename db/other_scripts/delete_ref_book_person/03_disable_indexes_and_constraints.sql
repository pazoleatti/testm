set serveroutput on;

begin

		FOR c1 IN (
					select ui.index_name from user_indexes ui where lower(ui.table_name)='ref_book_person' and lower(ui.uniqueness)='nonunique'
				  )
		LOOP
			execute immediate 'alter index ' || c1.index_name || ' unusable';
		END LOOP;
		
		execute immediate 'alter table REF_BOOK_PERSON disable constraint FK_REF_BOOK_PERSON_REPORT_DOC';
		
		execute immediate 'alter table REF_BOOK_ID_DOC disable constraint FK_REF_BOOK_ID_DOC_PERSON';
		execute immediate 'alter table DECLARATION_DATA_PERSON disable constraint DD_PERSON_PERSON_FK';
		execute immediate 'alter table LOG_BUSINESS disable constraint LOG_BUSINESS_FK_PERSON';
		execute immediate 'alter table NDFL_PERSON disable constraint NDFL_PERSON_FK_PERSON_ID';
		execute immediate 'alter table NDFL_REFERENCES disable constraint FK_NDFL_REFERS_PERSON';
		execute immediate 'alter table REF_BOOK_ID_TAX_PAYER disable constraint FK_REF_BOOK_ID_TAX_PAYER_PERS';
		execute immediate 'alter table REF_BOOK_PERSON_TB disable constraint PERSON_TB_FK_PERSON';

		DBMS_OUTPUT.PUT_LINE('Indexes set to unusable, constraint disabled');
end;
/

exit;