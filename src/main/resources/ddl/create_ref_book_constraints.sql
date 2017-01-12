-----------------------------------------------------------------------------------------------------------------------------
-- Создание ограничений для справочников
-----------------------------------------------------------------------------------------------------------------------------
-- Коды видов дохода
alter table ref_book_income_type add constraint pk_ref_book_income_type primary key (id);
alter table ref_book_income_type add constraint chk_ref_book_income_type_st check (status in (-1,0,1,2));
-- Коды видов вычетов
alter table ref_book_deduction_type add constraint pk_ref_book_deduction_type primary key (id);
alter table ref_book_deduction_type add constraint chk_ref_book_deduc_type_status check (status in (-1,0,1,2));

-----------------------------------------------------------------------------------------------------------------------------
