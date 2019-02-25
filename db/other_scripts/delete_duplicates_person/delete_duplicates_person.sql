set serveroutput on;
spool &1;

begin

	execute immediate 'alter table REF_BOOK_PERSON disable constraint FK_REF_BOOK_PERSON_REPORT_DOC';
	dbms_output.put_line('Constraint FK_REF_BOOK_PERSON_REPORT_DOC disabled');

	for c1 in (
              select
              distinct id, record_id, last_name, first_name, middle_name, birth_date
              from
              (
              select
              count(id) over (partition by p.last_name, p.first_name, p.middle_name, p.birth_date, nvl(p.doc_id,-1), nvl(p.doc_number,'null')) cnt_fl
              ,min(id)  over (partition by p.last_name, p.first_name, p.middle_name, p.birth_date, nvl(p.doc_id,-1), nvl(p.doc_number,'null')) min_id
              ,p.id, p.record_id, p.last_name, p.first_name, p.middle_name, p.birth_date, p.doc_id, p.doc_number
              from
              (
              select
              doc.doc_id,
              lower(doc.doc_number) doc_number,
              rbp.*
              from
              ref_book_person rbp,
              ref_book_id_doc doc
              where
              doc.person_id(+)=rbp.id
              ) p
              ) dp where 
              cnt_fl > 1
              and id <> min_id
              and
              not exists(select * from ndfl_references where person_id=dp.id)
              and
              not exists(select * from ndfl_person where person_id=dp.id)
              and 
              not exists(select * from declaration_data_person where person_id=dp.id)
            )
  loop
	
      dbms_output.put_line('Delete person: ID=' || to_char(c1.id) 
                           || ', RECORD_ID=' || to_char(c1.record_id)
                           || ', LAST_NAME=' || c1.last_name
                           || ', FIRST_NAME=' || c1.first_name
                           || ', MIDDLE_NAME=' || c1.middle_name
                           || ', BIRTH_DATE=' || to_char(c1.birth_date, 'dd.mm.yyyy')
                          );

      for c2 in (select doc.id, doc.doc_id, doc.doc_number from ref_book_id_doc doc where doc.person_id = c1.id)
      loop
        dbms_output.put_line('   delete REF_BOOK_ID_DOC ID=' || to_char(c2.id) || ', DOC_ID=' || to_char(c2.doc_id) || ', DOC_NUMBER=' || c2.doc_number);
        delete from ref_book_id_doc where id = c2.id;
      end loop;

      delete from ref_book_id_tax_payer where person_id  = c1.id;
    	
      dbms_output.put_line('   deleted ' || SQL%ROWCOUNT || ' rows from REF_BOOK_ID_TAX_PAYER.');
      
      delete from ref_book_person_tb where person_id  = c1.id;
    	
      dbms_output.put_line('   deleted ' || SQL%ROWCOUNT || ' rows from REF_BOOK_PERSON_TB.');
      
      delete from log_business where person_id  = c1.id;
    	
      dbms_output.put_line('   deleted ' || SQL%ROWCOUNT || ' rows from LOG_BUSINESS.');
    	
      delete from ref_book_person where id = c1.id;
    	
  end loop;
  
  for c3 in (select p.id, p.record_id, p.last_name, p.first_name, p.middle_name, p.birth_date, p.report_doc
             from ref_book_person p
             where p.report_doc is not null and not exists (select 1 from ref_book_id_doc where id = p.report_doc)
             )
  loop
      dbms_output.put_line('REPORT_DOC=' || to_char(c3.report_doc) 
                           || ' for person: ID=' || to_char(c3.id) 
                           || ', RECORD_ID=' || to_char(c3.record_id)
                           || ', LAST_NAME=' || c3.last_name
                           || ', FIRST_NAME=' || c3.first_name
                           || ', MIDDLE_NAME=' || c3.middle_name
                           || ', BIRTH_DATE=' || to_char(c3.birth_date, 'dd.mm.yyyy')
                           || ' not found and SET NULL'
                          );
      update ref_book_person set report_doc = null where id = c3.id;
  end loop;

  commit;

  execute immediate 'alter table REF_BOOK_PERSON enable constraint FK_REF_BOOK_PERSON_REPORT_DOC';
	dbms_output.put_line('Constraint FK_REF_BOOK_PERSON_REPORT_DOC enabled');

exception
  when others then
    rollback;
	dbms_output.put_line(sqlerrm);
  execute immediate 'alter table REF_BOOK_PERSON enable constraint FK_REF_BOOK_PERSON_REPORT_DOC';
	dbms_output.put_line('Delete person duplicates FAILED!');	
end;
/
exit;
