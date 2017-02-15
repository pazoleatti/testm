insert into DEPARTMENT_TYPE(id) values(-1);
insert into DEPARTMENT(id,code,name,type,parent_id,is_active) values(-1,0,' ',-1,0,0);

insert into ref_book_doc_type (id,record_id,version,status,code,name,priority) values (-1,-1,to_date('01.01.2016','dd.mm.yyyy'),2,' ',' ',1);
insert into REF_BOOK_TAXPAYER_STATE(id,code,name) values(-1,' ',' ');
insert into REF_BOOK_ASNU(id,code,name,type) values(-1,'0000',' ',' ');
insert into REF_BOOK_DEDUCTION_TYPE(id,record_id,version,status,code,name,deduction_mark) values(-1,-1,to_date('01.01.2016','dd.mm.yyyy'),2,' ',' ',-1);
--insert into REF_BOOK_INCOME_TYPE(id,record_id,version,status,code,name) values(-1,-1,to_date('01.01.2016','dd.mm.yyyy'),2,' ',' ');
insert into REF_BOOK_REGION(id,record_id,version,status,code,name) values(-1,-1,to_date('01.01.2016','dd.mm.yyyy'),2,' ',' ');
insert into REF_BOOK_PRESENT_PLACE(id,record_id,version,status,code,name) values(-1,-1,to_date('01.01.2016','dd.mm.yyyy'),2,' ',' ');
insert into REF_BOOK_OKVED(id,record_id,version,status,code,name) values(-1,-1,to_date('01.01.2016','dd.mm.yyyy'),2,' ',' ');
insert into REF_BOOK_DEDUCTION_MARK(id,record_id,version,status,code,name) values(-1,-1,to_date('01.01.2016','dd.mm.yyyy'),2,0,' ');
insert into REF_BOOK_REORGANIZATION(id,record_id,version,status,code,name) values(-1,-1,to_date('01.01.2016','dd.mm.yyyy'),2,' ',' ');
insert into REF_BOOK_DOC_STATE(id,name) values(-1,' ');
insert into REF_BOOK_FORM_TYPE(id,code,name,tax_kind) values(-1,' ',' ','N');
insert into REF_BOOK_INCOME_TYPE(id,record_id,version,status,code,name) values(-1,-1,to_date('01.01.2016','dd.mm.yyyy'),2,' ',' ');
insert into REF_BOOK_INCOME_KIND(id,income_type_id,mark) values(-1,-1,'0');
insert into REF_BOOK_ATTACH_FILE_TYPE(id,code,name) values(-1,0,' ');
insert into REF_BOOK_TAX_INSPECTION(id,code,name) values(-1,' ',' ');
insert into REF_BOOK_NDFL_RATE(id,rate) values(-1,0);
insert into REF_BOOK_OKTMO(id,record_id,status,version,code,name) values(-1,-1,2,to_date('01.01.2016','dd.mm.yyyy'),' ',' ');

insert into REF_BOOK_ADDRESS(id,record_id,status,version,address_type,region_code) values(-1,-1,2,to_date('01.01.2016','dd.mm.yyyy'),0,' ');
insert into REF_BOOK_PERSON(id,record_id,version,status,last_name,first_name,birth_date) values(-1,-1,to_date('01.01.2016','dd.mm.yyyy'),2,' ',' ',to_date('01.01.2016','dd.mm.yyyy'));
insert into REF_BOOK_ID_DOC(id,record_id,status,version,person_id,doc_id,doc_number) values(-1,-1,2,to_date('01.01.2016','dd.mm.yyyy'),-1,-1,' ');
insert into REF_BOOK_ID_TAX_PAYER(id,record_id,status,version,person_id,inp,as_nu) values(-1,-1,2,to_date('01.01.2016','dd.mm.yyyy'),-1,' ',-1);

insert into REF_BOOK_NDFL(id,department_id,record_id,version,status) values(-1,-1,-1,to_date('01.01.2016','dd.mm.yyyy'),2);
insert into REF_BOOK_NDFL_DETAIL(id,ref_book_ndfl_id,record_id,version,status,row_ord,department_id) values(-1,-1,-1,to_date('01.01.2016','dd.mm.yyyy'),2,0,-1);
insert into REF_BOOK_FOND(id,department_id,record_id,version,status) values(-1,-1,-1,to_date('01.01.2016','dd.mm.yyyy'),2);
insert into REF_BOOK_FOND_DETAIL(id,ref_book_fond_id,record_id,version,status,row_ord) values(-1,-1,-1,to_date('01.01.2016','dd.mm.yyyy'),2,0);