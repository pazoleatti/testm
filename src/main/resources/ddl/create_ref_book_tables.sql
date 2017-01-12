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

alter table ref_book_income_type add constraint pk_ref_book_income_type primary key (id);

alter table ref_book_income_type add constraint chk_ref_book_income_type_ST check (status in (-1,0,1,2));

--create sequence seq_ref_book_income_type start with 21 increment by 1;
-----------------------------------------------------------------------------------------------------------------------------
