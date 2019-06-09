create or replace 
procedure DeleteRefBookPerson(p_person_id number)
is
	v_address number;
begin
	delete from REF_BOOK_ID_TAX_PAYER where person_id=p_person_id;
			  
	delete from REF_BOOK_ID_DOC where person_id=p_person_id;
			  
	select address into v_address from REF_BOOK_PERSON where id=p_person_id;
			  
	delete from REF_BOOK_PERSON where id=p_person_id;
			  
	if v_address is not null then
		delete from REF_BOOK_ADDRESS where id=v_address;
	end if;

end DeleteRefBookPerson;
/