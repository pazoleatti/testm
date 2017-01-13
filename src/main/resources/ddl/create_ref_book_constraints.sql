-----------------------------------------------------------------------------------------------------------------------------
-- Создание ограничений для справочников
-----------------------------------------------------------------------------------------------------------------------------
-- Коды видов дохода
alter table ref_book_income_type add constraint pk_ref_book_income_type primary key (id);
alter table ref_book_income_type add constraint chk_ref_book_income_type_st check (status in (-1,0,1,2));
-- Коды видов вычетов
alter table ref_book_deduction_type add constraint pk_ref_book_deduction_type primary key (id);
alter table ref_book_deduction_type add constraint chk_ref_book_deduc_type_status check (status in (-1,0,1,2));
-- Коды субъектов РФ
alter table ref_book_region add constraint pk_ref_book_region primary key (id);
alter table ref_book_region add constraint chk_ref_book_region_status check (status in (-1,0,1,2));
alter table ref_book_region add constraint chk_ref_book_region_okato_def check (decode(translate('#'||okato_definition,'#1234567890','#'),'#','ЦИФРЫ','Буквы')='ЦИФРЫ');
alter table ref_book_region add constraint chk_ref_book_region_oktmo_def check (decode(translate('#'||oktmo_definition,'#1234567890','#'),'#','ЦИФРЫ','Буквы')='ЦИФРЫ');

alter table ref_book_region add constraint fk_ref_book_region_okato foreign key(okato) references ref_book_record(id);
alter table ref_book_region add constraint fk_ref_book_region_oktmo foreign key(oktmo) references ref_book_oktmo(id);

--Коды места представления расчета
alter table ref_book_present_place add constraint pk_ref_book_present_place primary key(id);
alter table ref_book_present_place add constraint chk_ref_book_pres_place_st check (status in (-1,0,1,2));

-----------------------------------------------------------------------------------------------------------------------------