set feedback off;
set verify off;
set serveroutput on;
spool &1

Prompt Alter tables
alter table ref_book_person enable row movement;
alter table ref_book_id_doc enable row movement;
alter table ref_book_id_tax_payer enable row movement;

Prompt Drop functional indexes
begin
    for ind in (select index_name from user_indexes where table_name in ('REF_BOOK_PERSON', 'REF_BOOK_ID_DOC', 'REF_BOOK_ID_TAX_PAYER') and index_type like 'FUNCTION-BASED%')
    loop
        execute immediate 'Drop index '||ind.index_name;
        dbms_output.put_line ('Index '||ind.index_name ||' dropped');
    end loop;    
end;
/

Prompt Shrink persons. Please wait...
alter table ref_book_person shrink space cascade;
alter table ref_book_id_doc shrink space cascade;
alter table ref_book_id_tax_payer shrink space cascade;

Prompt Rebuild indexes
begin
    for ind in (select index_name from user_indexes where table_name in ('REF_BOOK_PERSON', 'REF_BOOK_ID_DOC', 'REF_BOOK_ID_TAX_PAYER') )
    loop
        execute immediate 'Alter index '||ind.index_name ||' rebuild compute statistics';
        dbms_output.put_line ('Index '||ind.index_name ||' rebuild complete');
     end loop;   
end;
/


CREATE INDEX SRCH_REF_BOOK_ID_DOC_TP_NUM ON REF_BOOK_ID_DOC (DOC_ID, REPLACE(DOC_NUMBER,' ','')) compute statistics;
CREATE INDEX SRCH_FULL_REF_PERS_DUBLE ON REF_BOOK_PERSON (REPLACE(NVL(LAST_NAME,'empty'),' ',''), REPLACE(NVL(FIRST_NAME,'empty'),' ',''), REPLACE(NVL(MIDDLE_NAME,'empty'),' ',''), BIRTH_DATE, REPLACE(REPLACE(NVL(SNILS,'empty'),' ',''),'-',''), REPLACE(NVL(INN,'empty'),' ',''), REPLACE(NVL(INN_FOREIGN,'empty'),' ',''))  compute statistics;
CREATE INDEX SRCH_REF_BOOK_PERSON_INN ON REF_BOOK_PERSON (REPLACE(INN,' ',''))  compute statistics;
CREATE INDEX SRCH_REF_BOOK_PERSON_INN_F ON REF_BOOK_PERSON (REPLACE(INN_FOREIGN,' ',''))  compute statistics;
CREATE INDEX SRCH_REF_BOOK_PERSON_SNILS ON REF_BOOK_PERSON (REPLACE(REPLACE(SNILS,' ',''),'-',''))  compute statistics;
CREATE INDEX SRCH_REF_PERSON_NAME_BRTHD ON REF_BOOK_PERSON (REPLACE(LAST_NAME,' ',''), REPLACE(FIRST_NAME,' ',''), REPLACE(MIDDLE_NAME,' ',''), BIRTH_DATE)  compute statistics;

alter table ref_book_person disable row movement;
alter table ref_book_id_doc disable row movement;
alter table ref_book_id_tax_payer disable row movement;

Prompt Shrink forms. Please wait...

begin
    ndfl_tools.shrink_tables();
end;
/
Prompt Script complete!

exit;
