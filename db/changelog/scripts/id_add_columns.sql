create or replace TRIGGER REF_BOOK_ID_DOC_BEFORE_INS_UPD
  before insert or update on ref_book_id_doc
  for each row
begin
    if (:new.doc_number!=:old.doc_number) then
        :new.search_doc_number := regexp_replace(:new.doc_number,'[^0-9A-Za-zА-Яа-я]','');        
    end if;  
end REF_BOOK_ID_DOC_BEFORE_INS_UPD;


create or replace TRIGGER REF_BOOK_PERSON_BEFORE_INS_UPD
  before insert or update on ref_book_person
  for each row
begin
    if (:new.last_name!=:old.last_name) then
        :new.search_LAST_NAME := replace(nvl(:new.last_name,'empty'),' ','');        
    end if;  

    if (:new.first_name!=:old.first_name) then
        :new.search_first_NAME := replace(nvl(:new.first_name,'empty'),' ','');        
    end if;  

    if (:new.middle_name!=:old.middle_name) then
        :new.search_middle_NAME := replace(nvl(:new.middle_name,'empty'),' ','');        
    end if;  

    if (:new.inn!=:old.inn) then
        :new.search_inn := replace(nvl(:new.inn,'empty'),' ','');        
    end if;  

    if (:new.inn_foreign!=:old.inn_foreign) then
        :new.search_inn_foreign := replace(nvl(:new.inn_foreign,'empty'),' ','');        
    end if;  

    if (:new.snils!=:old.snils) then
        :new.search_snils := replace(replace(nvl(:new.snils,'empty'),' ',''),'-','');
    end if;  
    
end REF_BOOK_PERSON_BEFORE_INS_UPD;

begin
    dbms_output.put_line ('Drop functional indexes...');
    for ind in (select index_name from user_indexes where index_name in ('SRCH_FULL_REF_PERS_DUBLE','SRCH_REF_BOOK_ID_DOC_TP_NUM','SRCH_REFB_TAX_PAYER_INP_ASNU',
                'SRCH_REF_PERSON_NAME_BRTHD','SRCH_REF_BOOK_PERSON_SNILS','SRCH_REF_BOOK_PERSON_INN_F','SRCH_REF_BOOK_PERSON_INN'))
    loop
        execute immediate 'DROP INDEX '|| ind.index_name;
    end loop;
end;


create index idx_ref_book_id_doc_srch_doc on ref_book_id_doc (search_doc_number asc);
create index idx_ref_book_person_srch_fio on ref_book_person (search_last_name asc, search_first_name asc, search_middle_name asc, birth_date asc);
create index idx_ref_book_person_srch_inn on ref_book_person (search_inn asc);
create index idx_ref_book_person_srch_innf on ref_book_person (search_inn_foreign asc);
create index idx_ref_book_person_srch_snils on ref_book_person (search_snils asc);
