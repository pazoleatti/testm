create index idx_ref_book_id_tax_payer_pers on ref_book_id_tax_payer(person_id);
create index idx_ref_book_id_doc_pers on ref_book_id_doc(person_id);
create index idx_ref_person_status on ref_book_person(status);

create index srch_ref_person_name_brthd on ref_book_person(replace(lower(last_name), ' ', ''),replace(lower(first_name), ' ', ''),replace(lower(middle_name), ' ', ''),birth_date);
create index srch_ref_book_id_doc_tp_num on ref_book_id_doc(doc_id,replace(lower(doc_number), ' ', ''));
create index srch_ref_book_person_snils on ref_book_person(replace(replace(snils, ' ', ''), '-', ''));
create index srch_refb_tax_payer_inp_asnu on ref_book_id_tax_payer(as_nu,lower(inp));
create index srch_ref_book_person_inn on ref_book_person(replace(inn, ' ', ''));
create index srch_ref_book_person_inn_f on ref_book_person(replace(inn_foreign, ' ', ''));

create index idx_ref_book_person_address on ref_book_person(address);

create index idx_ndfl_person_decl_data_id on ndfl_person(declaration_data_id);

create index idx_ndfl_person_inc_person on ndfl_person_income(ndfl_person_id);
create index idx_ndfl_person_inc_taxdt on ndfl_person_income(ndfl_person_id,tax_date);
create index idx_ndfl_person_inc_paymdt on ndfl_person_income(ndfl_person_id,payment_date);
create index idx_ndfl_person_inc_oktmo on ndfl_person_income(ndfl_person_id,oktmo);
create index idx_ndfl_person_inc_kpp on ndfl_person_income(ndfl_person_id,kpp);

create index idx_ndfl_person_ded_ppcurrdt on ndfl_person_deduction(ndfl_person_id,period_curr_date);

create index idx_ndfl_person_prep_pnotdt on ndfl_person_prepayment(ndfl_person_id,notif_date);

--exit;