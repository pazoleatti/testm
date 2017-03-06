create index srch_full_ref_pers_duble on ref_book_person (replace(lower(last_name),' ',''), replace(lower(first_name),' ',''), replace(lower(middle_name),' ',''), birth_date, replace(replace(snils,' ',''),'-',''), replace(inn,' ',''));

create index idx_ras_psv_strlic_decl_person on raschsv_pers_sv_strah_lic(declaration_data_id,person_id);

create index idx_ref_deduct_mark_name on ref_book_deduction_mark(name);
create index idx_ref_deduct_type_dmark on ref_book_deduction_type(deduction_mark);

create index srch_ndfl_pers_inc_income on ndfl_person_income(ndfl_person_id,operation_id,income_accrued_date,income_code);
