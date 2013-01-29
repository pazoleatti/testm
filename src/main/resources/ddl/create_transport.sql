/**
 * Таблицы справочников
 */
create table transport_okato (
	id number(9) not null,
	parent_id number(9),
	okato varchar(11) not null,
	name varchar(100) not null
);
alter table transport_okato add constraint transport_okato_ok primary key(id);
alter table transport_okato add constraint transport_okato_unique_okato unique (okato);
alter table transport_okato add constraint transport_okato_fk_parent_id foreign key (parent_id) references transport_okato(id);

comment on table transport_okato is 'Коды ОКАТО и Муниципальных образований';
comment on column transport_okato.id is 'Идентификатор записи';
comment on column transport_okato.parent_id is 'Идентификатор родительской записи';
comment on column transport_okato.okato is 'Код ОКАТО';
comment on column transport_okato.name is 'Наименование муниципального образования';
---------------------------------------------------------------------------------------------------

create table transport_tax_rate (
	id number(9) not null,
	code varchar(10) not null,
	min_age number(9),
	max_age number(9),
	min_power number(9),
	max_power number(9),
	value number(9) not null
);
alter table transport_tax_rate add constraint transport_tax_rate_pk primary key (id);

comment on table transport_tax_rate is 'Ставки транспортного налога';
comment on column transport_tax_rate.id is 'Первичный ключ (номер п.п.)';
comment on column transport_tax_rate.code is 'Код транспортного средства';
comment on column transport_tax_rate.min_age is 'Срок использования "От", лет';
comment on column transport_tax_rate.max_age is 'Срок использования "До", лет';
comment on column transport_tax_rate.min_power is 'Мощность "От", л.с.';
comment on column transport_tax_rate.max_power is 'Мощность "До", л.с.';
comment on column transport_tax_rate.value is 'Ставка, руб.';
---------------------------------------------------------------------------------------------------

create table transport_type_code (
	id number(9) not null,
	parent_id number(9) null,	
	code varchar(5) not null,
	name varchar(500) not null
);
alter table transport_type_code add constraint transport_type_code_pk primary key(id);
alter table transport_type_code add constraint transport_type_code_un_code unique (code);
alter table transport_type_code add constraint transport_type_code_fk_parent foreign key(parent_id) references transport_type_code(id);

comment on table transport_type_code is 'Код вида транспортного средства';
comment on column transport_type_code.id is 'Идентификатор записи';
comment on column transport_type_code.parent_id is 'Идентификатор родительской записи';
comment on column transport_type_code.code is 'Код типа';
comment on column transport_type_code.name is 'Наименование';

---------------------------------------------------------------------------------------------------

create table transport_unit_code
(
code number(9) not null,
name varchar2(128) not null, 
convention varchar2(16) not null
);

comment on table transport_unit_code is 'Коды единиц измерения налоговой базы на основании ОКЕИ (Выписка)';
comment on column transport_unit_code.code is 'Код единиц измерения';
comment on column transport_unit_code.name is 'Наименование единицы измерения';
comment on column transport_unit_code.convention is 'Условное обозначение';
---------------------------------------------------------------------------------------------------
create table transport_tax_benefit_code
(
code varchar2(5) not null,
name varchar2(256) not null,
regulation varchar(128) not null
);

alter table transport_tax_benefit_code add constraint transport_tax_benefit_code_pk primary key (code);

comment on table transport_tax_benefit_code is 'Коды налоговых льгот';
comment on column transport_tax_benefit_code.code is 'Код налоговых льгот';
comment on column transport_tax_benefit_code.name is 'Наименование льготы';
comment on column transport_tax_benefit_code.regulation is 'Основание';

---------------------------------------------------------------------------------------------------
create table transport_eco_class
(
code number(9) not null,
name varchar2(20) not null
);

alter table transport_eco_class add constraint transport_eco_class_pk primary key (code);

comment on table transport_eco_class is 'Экономические классы транспортных средств';
comment on column transport_eco_class.code is 'Код';
comment on column transport_eco_class.name is 'Наименование';

create index I_TRANSPORT_TAX_RATE_CODE on TRANSPORT_TAX_RATE (CODE);