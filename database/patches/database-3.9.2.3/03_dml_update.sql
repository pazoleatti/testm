	update ndfl_person 
	    set inp= upper(inp), snils=upper(snils), last_name=upper(last_name), 
		first_name = upper(first_name), middle_name = upper(middle_name), 
		inn_np = upper (inn_np), inn_foreign = upper (inn_foreign), 
		id_doc_number = upper (id_doc_number),
		area = upper (area), city = upper (city), locality = upper (locality), street = upper (street), 
		house = upper (house), building = upper (building), flat = upper (flat);
commit;
/
        update ref_book_person
	    set last_name=upper (last_name), first_name = upper (first_name),
		middle_name = upper (middle_name), inn = upper (inn), inn_foreign = upper (inn_foreign),
		snils = upper (snils), district = upper (district), city = upper (city), 
		locality = upper (locality), street = upper (street), house = upper (house), 
		build = upper (build),appartment = upper (appartment), address_foreign = upper (address_foreign);
commit;
/

	update ref_book_id_doc set doc_number = upper (doc_number);
commit;
/

	update  ref_book_id_tax_payer set inp=upper (inp);
commit;
/
