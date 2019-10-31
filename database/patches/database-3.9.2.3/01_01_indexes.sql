begin
    dbms_output.put_line ('Drop functional indexes...');
    for ind in (select index_name from user_indexes where index_name in ('SRCH_FULL_REF_PERS_DUBLE','SRCH_REF_BOOK_ID_DOC_TP_NUM','SRCH_REFB_TAX_PAYER_INP_ASNU',
                'SRCH_REF_PERSON_NAME_BRTHD','SRCH_REF_BOOK_PERSON_SNILS','SRCH_REF_BOOK_PERSON_INN_F','SRCH_REF_BOOK_PERSON_INN'))
    loop
        execute immediate 'DROP INDEX '|| ind.index_name;
    end loop;
end;
/

begin
    dbms_output.put_line ('Create functional indexes...');
end;
/

  CREATE INDEX "SRCH_FULL_REF_PERS_DUBLE" ON "REF_BOOK_PERSON" (REPLACE((NVL("LAST_NAME",'empty')),' ',''), REPLACE((NVL("FIRST_NAME",'empty')),' ',''), REPLACE((NVL("MIDDLE_NAME",'empty')),' ',''), "BIRTH_DATE", REPLACE(REPLACE(NVL("SNILS",'empty'),' ',''),'-',''), REPLACE(NVL("INN",'empty'),' ',''), REPLACE(NVL("INN_FOREIGN",'empty'),' ','')) ;

  CREATE INDEX "SRCH_REF_BOOK_PERSON_INN" ON "REF_BOOK_PERSON" (REPLACE("INN",' ','')) ;

  CREATE INDEX "SRCH_REF_BOOK_PERSON_INN_F" ON "REF_BOOK_PERSON" (REPLACE("INN_FOREIGN",' ','')) ;

  CREATE INDEX "SRCH_REF_BOOK_PERSON_SNILS" ON "REF_BOOK_PERSON" (REPLACE(REPLACE("SNILS",' ',''),'-','')) ;

  CREATE INDEX "SRCH_REF_PERSON_NAME_BRTHD" ON "REF_BOOK_PERSON" (REPLACE(("LAST_NAME"),' ',''), REPLACE(("FIRST_NAME"),' ',''), REPLACE(("MIDDLE_NAME"),' ',''), "BIRTH_DATE") ;

  CREATE INDEX "SRCH_REF_BOOK_ID_DOC_TP_NUM" ON "REF_BOOK_ID_DOC" ("DOC_ID", REPLACE(("DOC_NUMBER"),' ','')) ;

  CREATE INDEX "SRCH_REFB_TAX_PAYER_INP_ASNU" ON "REF_BOOK_ID_TAX_PAYER" ("AS_NU", ("INP")); 

