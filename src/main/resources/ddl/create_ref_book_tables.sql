-----------------------------------------------------------------------------------------------------------------------------
-- Создание таблиц для справочников
-----------------------------------------------------------------------------------------------------------------------------
-- Коды видов дохода
create table ref_book_income_type
(
  id        number(18) not null,
  record_id number(9) not null,
  version   date not null,
  status    number(1) default 0 not null,
  code      varchar2(4 char) not null,
  name      varchar2(2000 char) not null
);

comment on table ref_book_income_type is 'Коды видов дохода';
comment on column ref_book_income_type.id is 'Уникальный идентификатор';
comment on column ref_book_income_type.record_id is 'Идентификатор строки справочника. Может повторяться у разных версий';
comment on column ref_book_income_type.version is 'Версия. Дата актуальности записи';
comment on column ref_book_income_type.status is 'Статус записи (0 - обычная запись, -1 - удаленная, 1 - черновик, 2 - фиктивная)';
comment on column ref_book_income_type.code is 'Код';
comment on column ref_book_income_type.name is 'Наименование дохода';

create sequence seq_ref_book_income_type start with 21 increment by 1;

-- Коды видов вычетов
create table ref_book_deduction_type
(
  id number(18) not null, 
	record_id number(9) not null, 
	version date not null, 
	status number(1) default 0 not null, 
	code varchar2(3 char) not null, 
	name varchar2(2000 char) not null
);

comment on table ref_book_deduction_type is 'Коды видов вычетов';
comment on column ref_book_deduction_type.id is 'Уникальный идентификатор';
comment on column ref_book_deduction_type.record_id is 'Идентификатор строки справочника. Может повторяться у разных версий';
comment on column ref_book_deduction_type.version is 'Версия. Дата актуальности записи';
comment on column ref_book_deduction_type.status is 'Статус записи (0 - обычная запись, -1 - удаленная, 1 - черновик, 2 - фиктивная)';
comment on column ref_book_deduction_type.code is 'Код';
comment on column ref_book_deduction_type.name is 'Наименование вычета';

create sequence seq_ref_book_deduction_type start with 106 increment by 1;

-- Коды субъектов РФ
create table ref_book_region
(
  id        number(18) not null,
  record_id number(9) not null,
  version   date not null,
  status    number(1) default 0 not null,
  code      varchar2(2 char) not null,
  name      varchar2(255 char) not null,
  okato_definition varchar2(11 char),
  okato     number(18), 
  oktmo     number(18),
  oktmo_definition varchar2(11 char)
);

comment on table ref_book_region is 'Коды субъектов РФ';
comment on column ref_book_region.id is 'Уникальный идентификатор';
comment on column ref_book_region.record_id is 'Идентификатор строки справочника. Может повторяться у разных версий';
comment on column ref_book_region.version is 'Версия. Дата актуальности записи';
comment on column ref_book_region.status is 'Статус записи (0 - обычная запись, -1 - удаленная, 1 - черновик, 2 - фиктивная)';
comment on column ref_book_region.code is 'Код';
comment on column ref_book_region.name is 'Наименование';
comment on column ref_book_region.okato is 'Ссылка на код ОКАТО';
comment on column ref_book_region.okato_definition is 'Определяющая часть кода ОКАТО';
comment on column ref_book_region.oktmo is 'Ссылка на код ОКТМО';
comment on column ref_book_region.oktmo_definition is 'Определяющая часть кода ОКТМО';

create sequence seq_ref_book_region start with 101 increment by 1;
-----------------------------------------------------------------------------------------------------------------------------
