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

--alter table ref_book_region add constraint fk_ref_book_region_okato foreign key(okato) references ref_book_record(id);
--alter table ref_book_region add constraint fk_ref_book_region_oktmo foreign key(oktmo) references ref_book_oktmo(id);

--Коды места представления расчета
alter table ref_book_present_place add constraint pk_ref_book_present_place primary key(id);
alter table ref_book_present_place add constraint chk_ref_book_pres_place_st check (status in (-1,0,1,2));

-- Справочник АСНУ
alter table ref_book_asnu add constraint pk_ref_book_asnu primary key(id);
alter table ref_book_asnu add constraint chk_ref_book_asnu_code check (code between '0000' and '9999');

-- Виды налоговых форм
alter table ref_book_form_type add constraint pk_ref_book_form_type primary key(id);
alter table ref_book_form_type add constraint chk_ref_book_form_type_taxkind check (tax_kind in ('F','N'));

-- Типы налоговых форм
alter table declaration_kind add constraint pk_declaration_kind primary key(id);

-- Общероссийский классификатор видов экономической деятельности
alter table ref_book_okved add constraint pk_ref_book_okved primary key(id);
alter table ref_book_okved add constraint chk_ref_book_okved_status check (status between -1 and 2);

-- ОКАТО
alter table ref_book_okato add constraint pk_ref_book_okato primary key(id);
alter table ref_book_okato add constraint chk_ref_book_okato_status check (status between -1 and 2);

-- Признак кода вычета
alter table ref_book_deduction_mark add constraint pk_ref_book_deduction_mark primary key(id);
alter table ref_book_deduction_mark add constraint chk_ref_book_ded_mark_status check (status between -1 and 2);

-- Параметры подразделения по НДФЛ

alter table ref_book_ndfl add constraint pk_ref_book_ndfl primary key(id);
alter table ref_book_ndfl_detail add constraint pk_ref_book_ndfl_detail primary key(id);

alter table ref_book_ndfl add constraint fk_ref_book_ndfl_depart foreign key(department_id) references department(id);
alter table ref_book_ndfl add constraint chk_ref_book_ndfl_status check (status in (-1,0,1,2));

alter table ref_book_ndfl_detail add constraint fk_ref_book_ndfl_det_parent foreign key(ref_book_ndfl_id) references ref_book_ndfl(id);
alter table ref_book_ndfl_detail add constraint fk_ref_book_ndfl_det_pres_pl foreign key(present_place) references ref_book_present_place(id);
alter table ref_book_ndfl_detail add constraint fk_ref_book_ndfl_det_okved foreign key(okved) references ref_book_okved(id);
alter table ref_book_ndfl_detail add constraint fk_ref_book_ndfl_det_region foreign key(region) references ref_book_region(id);
alter table ref_book_ndfl_detail add constraint fk_ref_book_ndfl_det_oblig foreign key(obligation) references ref_book_record(id);
alter table ref_book_ndfl_detail add constraint fk_ref_book_ndfl_det_type foreign key(type) references ref_book_record(id);
alter table ref_book_ndfl_detail add constraint fk_ref_book_ndfl_det_re_code foreign key(reorg_form_code) references ref_book_record(id);
alter table ref_book_ndfl_detail add constraint fk_ref_book_ndfl_det_signatory foreign key(signatory_id) references ref_book_record(id);
alter table ref_book_ndfl_detail add constraint chk_ref_book_ndfl_det_status check (status in (-1,0,1,2));

-- Параметры подразделения по сборам, взносам

alter table ref_book_fond add constraint pk_ref_book_fond primary key(id);
alter table ref_book_fond_detail add constraint pk_ref_book_fond_detail primary key(id);

alter table ref_book_fond add constraint fk_ref_book_fond_depart foreign key(department_id) references department(id);
alter table ref_book_fond add constraint chk_ref_book_fond_status check (status in (-1,0,1,2));

alter table ref_book_fond_detail add constraint fk_ref_book_fond_det_parent foreign key(ref_book_fond_id) references ref_book_fond(id);
alter table ref_book_fond_detail add constraint fk_ref_book_fond_det_pres_pl foreign key(present_place) references ref_book_present_place(id);
alter table ref_book_fond_detail add constraint fk_ref_book_fond_det_okved foreign key(okved) references ref_book_okved(id);
alter table ref_book_fond_detail add constraint fk_ref_book_fond_det_region foreign key(region) references ref_book_region(id);
alter table ref_book_fond_detail add constraint fk_ref_book_fond_det_oblig foreign key(obligation) references ref_book_record(id);
alter table ref_book_fond_detail add constraint fk_ref_book_fond_det_type foreign key(type) references ref_book_record(id);
alter table ref_book_fond_detail add constraint fk_ref_book_fond_det_re_code foreign key(reorg_form_code) references ref_book_record(id);
alter table ref_book_fond_detail add constraint fk_ref_book_fond_det_signatory foreign key(signatory_id) references ref_book_record(id);
alter table ref_book_fond_detail add constraint chk_ref_book_fond_det_status check (status in (-1,0,1,2));

-----------------------------------------------------------------------------------------------------------------------------
alter table declaration_data add constraint declaration_data_fk_asnu_id foreign key (asnu_id) references ref_book_asnu(id);
