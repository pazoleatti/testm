create table dict_declaration_presentation (
 code varchar2(3) not null,
 name varchar2(510) not null
);

alter table dict_declaration_presentation add constraint dict_decl_present_pk primary key (code);

comment on table dict_declaration_presentation is 'Коды представления налоговой по месту нахождения (учёта)';
comment on column dict_declaration_presentation.code is 'Код';
comment on column dict_declaration_presentation.name is 'Наименование';
-----------------------------------------------------------------------------------------------------------
create table dict_reorganization_form (
 code varchar2(1) not null,
 name varchar2(510) not null
);

alter table dict_reorganization_form add constraint dict_reorg_form_pk primary key (code);

comment on table dict_reorganization_form is 'Коды форм реорганизации и ликвидации организации';
comment on column dict_reorganization_form.code is 'Код';
comment on column dict_reorganization_form.name is 'Наименование';
-------------------------------------------------------------------------------------------------------------
create table dict_declaration_present_way (
 code varchar2(2) not null,
 name varchar2(510) not null
);
alter table dict_declaration_present_way add constraint dict_decl_way_pk primary key (code);

comment on table dict_declaration_present_way is 'Коды, определяющие способ представления налоговой декларации в налоговый орган';
comment on column dict_declaration_present_way.code is 'Код';
comment on column dict_declaration_present_way.name is 'Наименование';
-----------------------------------------------------------------------------------------------------------
create table dict_tax_period (
  code varchar2(2) not null,
  name varchar2(510) not null,
  I    number(1,0) default 0 not null,
  T    number(1,0) default 0 not null,
  P    number(1,0) default 0 not null,
  V    number(1,0) default 0 not null,
  D    number(1,0) default 0 not null
);
alter table dict_tax_period add constraint dict_tax_period_pk primary key (code);

alter table dict_tax_period add constraint dict_tax_period_chk_i check (I in (0, 1));
alter table dict_tax_period add constraint dict_tax_period_chk_t check (T in (0, 1));
alter table dict_tax_period add constraint dict_tax_period_chk_p check (P in (0, 1));
alter table dict_tax_period add constraint dict_tax_period_chk_v check (V in (0, 1));
alter table dict_tax_period add constraint dict_tax_period_chk_d check (D in (0, 1));

comment on table dict_tax_period is 'Коды, определяющие налоговый (отчётный) период';
comment on column dict_tax_period.code is 'Код';
comment on column dict_tax_period.name is 'Наименование';
comment on column dict_tax_period.I is 'Принадлежность к налогу на прибыль';
comment on column dict_tax_period.T is 'Принадлежность к налогу на транспорт';
comment on column dict_tax_period.P is 'Принадлежность к налогу на имущество';
comment on column dict_tax_period.V is 'Принадлежность к налогу ндс';
comment on column dict_tax_period.D is 'Принадлежность к ТЦО';
---------------------------------------------------------------------------------------------
create table dict_region (
  code varchar2(2) not null,
  name varchar2(510) not null,
  okato varchar2(11),
  okato_definition varchar2(11)
);
alter table dict_region add constraint dict_region_pk primary key (code);
alter table dict_region add constraint dict_region_uniq_okato_def unique (okato_definition);

comment on table dict_region is 'Коды субъектов Российской Федерации';
comment on column dict_region.code is 'Код';
comment on column dict_region.name is 'Наименование';
comment on column dict_region.okato is 'Код ОКАТО';
comment on column dict_region.okato_definition is 'Определяющая часть кода ОКАТО';
-----------------------------------------------------------------------------------------------------------
create table dict_okato (
  id number(9) not null,
  parent_id number(9),
  okato varchar(11) not null,
  name varchar(510) not null
);
alter table dict_okato add constraint dict_okato_pk primary key(id);
alter table dict_okato add constraint dict_okato_unique_okato unique (okato);
alter table dict_okato add constraint dict_okato_fk_parent_id foreign key (parent_id) references dict_okato(id);

comment on table dict_okato is 'Коды ОКАТО и Муниципальных образований';
comment on column dict_okato.id is 'Идентификатор записи';
comment on column dict_okato.parent_id is 'Идентификатор родительской записи';
comment on column dict_okato.okato is 'Код ОКАТО';
comment on column dict_okato.name is 'Наименование муниципального образования';
---------------------------------------------------------------------------------------------------
create table transport_tax_rate (
  id number(15) not null,
  code varchar(10) not null,
  min_age number(15),
  max_age number(15),
  min_power number(15,2),
  max_power number(15,2),
  value number(15) not null,
  dict_region_id varchar2(2),
  unit_of_power number(5) not null
);
alter table transport_tax_rate add constraint transport_tax_rate_pk primary key (id);
alter table transport_tax_rate add constraint transport_tax_rate_fk_dict_reg foreign key (dict_region_id) references dict_region(code);

comment on table transport_tax_rate is 'Ставки транспортного налога';
comment on column transport_tax_rate.id is 'Первичный ключ (номер п.п.)';
comment on column transport_tax_rate.code is 'Код транспортного средства';
comment on column transport_tax_rate.min_age is 'Срок использования "От", лет';
comment on column transport_tax_rate.max_age is 'Срок использования "До", лет';
comment on column transport_tax_rate.min_power is 'Мощность "От"';
comment on column transport_tax_rate.max_power is 'Мощность "До"';
comment on column transport_tax_rate.value is 'Ставка, руб.';
comment on column transport_tax_rate.dict_region_id is 'Код региона';
comment on column transport_tax_rate.unit_of_power is 'Ед. измерения мощности';
--------------------------------------------------------------------------------------------------
create table transport_type_code (
  id number(9) not null,
  parent_id number(9) null,
  code varchar(5) not null,
  name varchar(510) not null
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
create table transport_unit_code (
 code number(9) not null,
 name varchar2(510) not null,
 convention varchar2(510) not null
);

comment on table transport_unit_code is 'Коды единиц измерения налоговой базы на основании ОКЕИ (Выписка)';
comment on column transport_unit_code.code is 'Код единиц измерения';
comment on column transport_unit_code.name is 'Наименование единицы измерения';
comment on column transport_unit_code.convention is 'Условное обозначение';
---------------------------------------------------------------------------------------------------
create table dict_tax_benefit (
 code varchar2(10) not null,
 name varchar2(4000) not null
);

alter table dict_tax_benefit add constraint dict_tax_benefit_pk primary key (code);

comment on table dict_tax_benefit is 'Коды налоговых льгот';
comment on column dict_tax_benefit.code is 'Код налоговых льгот';
comment on column dict_tax_benefit.name is 'Наименование льготы';
---------------------------------------------------------------------------------------------------
create table transport_eco_class (
 code number(9) not null,
 name varchar2(510) not null
);

alter table transport_eco_class add constraint transport_eco_class_pk primary key (code);

comment on table transport_eco_class is 'Экономические классы транспортных средств';
comment on column transport_eco_class.code is 'Код';
comment on column transport_eco_class.name is 'Наименование';

create index I_TRANSPORT_TAX_RATE_CODE on TRANSPORT_TAX_RATE (CODE);
-----------------------------------------------------------------------------------------------------------
create table dict_tax_benefit_param (
 id             number(9) not null,
 dict_region_id varchar(2) not null,
 tax_benefit_id varchar2(10) not null,
 section        varchar2(4),
 item           varchar2(4),
 subitem        varchar2(4),
 percent        number(3,2),
 rate           number(15,2)
);

alter table dict_tax_benefit_param add constraint dict_tax_benefit_p_fk_dict_reg foreign key (dict_region_id) references dict_region(code);
alter table dict_tax_benefit_param add constraint dict_tax_ben_p_fk_taxben foreign key (tax_benefit_id) references dict_tax_benefit(code);
alter table dict_tax_benefit_param add constraint dict_tax_benefit_param_pk primary key (id);
alter table dict_tax_benefit_param add constraint dict_tax_benefit_p_chk_perc check ((percent>=0) and (percent<=100));

comment on table dict_tax_benefit_param is 'Параметры налоговых льгот';
comment on column dict_tax_benefit_param.id is 'Первичный ключ';
comment on column dict_tax_benefit_param.dict_region_id is 'Код региона';
comment on column dict_tax_benefit_param.tax_benefit_id is 'Код налоговой льготы';
comment on column dict_tax_benefit_param.section is 'Основание - статья';
comment on column dict_tax_benefit_param.item is 'Основание - пункт';
comment on column dict_tax_benefit_param.subitem is 'Основание - подпункт';
comment on column dict_tax_benefit_param.percent is 'Уменьшающий процент, %';
comment on column dict_tax_benefit_param.rate is 'Пониженная ставка';
-------------------------------------------------------------------------------------------------------------------------------------------
create table form_type (
  id       number(9) not null,
  name     varchar2(600) not null,
  tax_type char(1) not null
);
alter table form_type add constraint form_type_pk primary key (id);
alter table form_type add constraint form_type_chk_taxtype check (tax_type in ('I', 'P', 'T', 'V', 'D'));

comment on table form_type is 'Типы налоговых форм (названия)';
comment on column form_type.id is 'Идентификатор';
comment on column form_type.name is 'Наименование';
comment on column form_type.tax_type is 'Вид налога (I-на прибыль, P-на имущество, T-транспортный, V-НДС, D-ТЦО)';
---------------------------------------------------------------------------------------------------
create table tax_period (
  id number(9) not null,
  tax_type char(1) not null,
  start_date date not null,
  end_date date not null
);
alter table tax_period add constraint tax_period_pk primary key (id);
alter table tax_period add constraint tax_period_chk_taxtype check (tax_type in ('I', 'P', 'T', 'V', 'D'));

comment on table tax_period is 'Налоговые периоды';
comment on column tax_period.id is 'Идентификатор (первичный ключ)';
comment on column tax_period.tax_type is 'Вид налога (I-на прибыль, P-на имущество, T-транспортный, V-НДС, D-ТЦО)';
comment on column tax_period.start_date is 'Дата начала (включительно)';
comment on column tax_period.end_date is 'Дата окончания (включительно)';

create sequence seq_tax_period start with 10000;
---------------------------------------------------------------------------------------------------
create table form_template (
  id number(9) not null,
  type_id number(9) not null,
  data_rows clob,
  version varchar2(20) not null,
  is_active number(1) default 1 not null,
  edition number(9) not null,
  numbered_columns NUMBER(1) not null,
  fixed_rows number(1) not null,
  name varchar2(600) not null,
  fullname varchar2(600) not null,
  code varchar2(600) not null,
  script clob,
  data_headers clob
);
alter table form_template add constraint form_template_pk primary key (id);
alter table form_template add constraint form_template_fk_type_id foreign key (type_id) references form_type(id);
alter table form_template add constraint form_template_uniq_version unique(type_id, version);
alter table form_template add constraint form_template_check_active check (is_active in (0, 1));
alter table form_template add constraint form_template_chk_num_cols check (numbered_columns in (0, 1));
alter table form_template add constraint form_template_chk_fixed_rows check(fixed_rows in (0, 1));

comment on table form_template is 'Описания налоговых форм';
comment on column form_template.data_rows is 'Предопределённые строки формы в формате XML';
comment on column form_template.id is 'Первичный ключ';
comment on column form_template.is_active is 'Признак активности';
comment on column form_template.type_id is 'Идентификатор вида налоговой формы';
comment on column form_template.version is 'Версия формы (уникально в рамках типа)';
comment on column form_template.edition is 'Номер редакции записи';
comment on column form_template.numbered_columns is 'Признак того, что столбцы должны быть пронумерованы';
comment on column form_template.fixed_rows is 'Признак использования фиксированных строк: 0 - используется фиксированный набор строк, 1 - есть возможность добавлять и удалять строки из формы.';
comment on column form_template.name is 'Наименование формы';
comment on column form_template.fullname is 'Полное наименование формы';
comment on column form_template.code is 'Номер формы';
comment on column form_template.script is 'Скрипт, реализующий бизнес-логику налоговой формы';
comment on column form_template.data_headers is 'Описание заголовка таблицы';
---------------------------------------------------------------------------------------------------
create table form_style (
  id					     number(9) not null,
  alias				     varchar(80) not null,
  form_template_id number(9) not null,
  font_color			 number(3) null,
  back_color			 number(3) null,
  italic				   number(1) not null,
  bold				     number(1) not null
);

alter table form_style add constraint form_style_pk primary key (id);
alter table form_style add constraint form_style_fk_form_template_id foreign key (form_template_id) references form_template (id);
alter table form_style add constraint form_style_chk_font_color check (font_color in (0,1,2,3,4,5,6,7,8,9,10,11,12));
alter table form_style add constraint form_style_chk_back_color check (back_color in (0,1,2,3,4,5,6,7,8,9,10,11,12));
alter table form_style add constraint form_style_chk_italic check (italic in (0,1));
alter table form_style add constraint form_style_chk_bold check (bold in (0,1));
alter table form_style add constraint form_style_uniq_alias unique (form_template_id, alias);

comment on table form_style is 'Стили ячеек в налоговой форме';
comment on column form_style.id is 'Первичный ключ';
comment on column form_style.alias is 'Алиас стиля';
comment on column form_style.form_template_id is 'Идентификатор шаблона налоговой формы';
comment on column form_style.font_color is 'Код цвета шрифта';
comment on column form_style.back_color is 'Код цвета фона';
comment on column form_style.italic is 'Признак использования курсива';
comment on column form_style.bold is 'Признак жирного шрифта';

create sequence seq_form_style start with 10000;
------------------------------------------------------------------------------------------------------
create table blob_data (
  id            varchar2(36) not null,
  name          varchar2(200) null,
  data          blob not null,
  creation_date date not null,
  type          number(1) default 0 not null,
  data_size     number(9) default 0 not null
);
alter table blob_data add constraint blob_data_pk primary key(id);
alter table blob_data add constraint blob_data_chk_type check (type in (0, 1));

comment on table blob_data is 'Файловое хранилище';
comment on column blob_data.id is 'Уникальный идентификатор';
comment on column blob_data.name is 'Название файла';
comment on column blob_data.data is 'Бинарные данные';
comment on column blob_data.creation_date is 'Дата создания';
comment on column blob_data.type is 'Тип данных (0 - постоянные, 1 - временные)';
comment on column blob_data.data_size is 'Размер файла в байтах';
----------------------------------------------------------------------------------------------------
create table ref_book (
  id number(9,0) not null,
  name varchar2(200) not null,
  script_id varchar2(36)
);

alter table ref_book add constraint ref_book_pk primary key (id);
alter table ref_book add constraint ref_book_fk_script_id foreign key (script_id) references blob_data(id);

comment on table ref_book is 'Справочник';
comment on column ref_book.id is 'Уникальный идентификатор';
comment on column ref_book.name is 'Название справочника';
comment on column ref_book.script_id is 'Идентификатор связанного скрипта';
------------------------------------------------------------------------------------------------------
create table ref_book_attribute (
  id number(9) not null,
  ref_book_id number(9) not null,
  name varchar2(510) not null,
  alias varchar2(30) not null,
  type number(1) not null,
  ord number(9) not null,
  reference_id number(9),
  attribute_id number(9),
  visible number(1) default 1 not null,
  precision number(2),
  width number(9) default 15 not null
);

alter table ref_book_attribute add constraint ref_book_attr_pk primary key (id);

alter table ref_book_attribute add constraint ref_book_attr_chk_visible check (visible in (0, 1));
alter table ref_book_attribute add constraint ref_book_attr_chk_type check (type in (1, 2, 3, 4));
alter table ref_book_attribute add constraint ref_book_attr_chk_alias check (lower(alias) <> 'record_id');
alter table ref_book_attribute add constraint ref_book_attr_chk_precision check (precision >= 0 and precision <=10);
alter table ref_book_attribute add constraint ref_book_attr_chk_number_type check ((type <> 2 and precision is null) or (type = 2 and not (precision is null)));
alter table ref_book_attribute add constraint ref_book_attribute_uniq_ord unique (ref_book_id, ord);
alter table ref_book_attribute add constraint ref_book_attribute_uniq_alias unique (ref_book_id, alias);

alter table ref_book_attribute add constraint ref_book_attr_fk_ref_book_id foreign key (ref_book_id) references ref_book (id);
alter table ref_book_attribute add constraint ref_book_attr_fk_reference_id foreign key (reference_id) references ref_book (id);
alter table ref_book_attribute add constraint ref_book_attr_fk_attribute_id foreign key (attribute_id) references ref_book_attribute (id);

comment on table ref_book_attribute is 'Атрибут справочника';
comment on column ref_book_attribute.id is 'Уникальный идентификатор';
comment on column ref_book_attribute.ref_book_id is 'Ссылка на справочник';
comment on column ref_book_attribute.name is 'Название';
comment on column ref_book_attribute.alias is 'Псевдоним. Используется для обращения из скриптов бизнес-логики';
comment on column ref_book_attribute.type is 'Типа атрибута (1-строка; 2-число; 3-дата-время; 4-ссылка)';
comment on column ref_book_attribute.ord is 'Порядок следования';
comment on column ref_book_attribute.reference_id is 'Ссылка на справочник, на элемент которого ссылаемся. Только для атрибутов-ссылок';
comment on column ref_book_attribute.attribute_id is 'Код отображаемого атрибута. Только для атрибутов-ссылок';
comment on column ref_book_attribute.visible is 'Признак видимости';
comment on column ref_book_attribute.precision is 'Точность, количество знаков после запятой. Только для атрибутов-чисел';
comment on column ref_book_attribute.width is 'Ширина столбца. Используется при отображении справочника в виде таблицы';
------------------------------------------------------------------------------------------------------
create table ref_book_record (
  id number(9) not null,
  record_id number(9) not null,
  ref_book_id number(9) not null,
  version date not null,
  status number(1) default 0 not null
);

alter table ref_book_record add constraint ref_book_record_pk primary key (id);

alter table ref_book_record add constraint ref_book_record_chk_status check (status in (0, -1));

alter table ref_book_record add constraint ref_book_record_fk_ref_book_id foreign key (ref_book_id) references ref_book (id);

create unique index i_ref_book_record_refbookid on ref_book_record(ref_book_id, record_id, version);

create sequence seq_ref_book_record start with 100000;
create sequence seq_ref_book_record_row_id start with 100000;

comment on table ref_book_record is 'Запись справочника';
comment on column ref_book_record.id is 'Уникальный идентификатор';
comment on column ref_book_record.record_id is 'Идентификатор строки справочника. Может повторяться у разных версий';
comment on column ref_book_record.ref_book_id is 'Ссылка на справочник, к которому относится запись';
comment on column ref_book_record.version is 'Версия. Дата актуальности записи';
comment on column ref_book_record.status is 'Статус записи (0-обычная запись; -1-помеченная на удаление)';
------------------------------------------------------------------------------------------------------
create table ref_book_value (
  record_id number(9) not null,
  attribute_id number(9) not null,
  string_value varchar2(4000),
  number_value number(27,10),
  date_value date,
  reference_value number(9)
);

alter table ref_book_value add constraint ref_book_value_pk primary key (record_id, attribute_id);

alter table ref_book_value add constraint ref_book_value_fk_record_id foreign key (record_id) references ref_book_record (id);
alter table ref_book_value add constraint ref_book_value_fk_attribute_id foreign key (attribute_id) references ref_book_attribute (id);

comment on table ref_book_value is 'Значение записи справочника';
comment on column ref_book_value.record_id is 'Ссылка на запись справочника';
comment on column ref_book_value.attribute_id is 'Ссылка на атрибут справочника';
comment on column ref_book_value.string_value is 'Строковое значение';
comment on column ref_book_value.number_value is 'Численное значение';
comment on column ref_book_value.date_value is 'Значение даты';
comment on column ref_book_value.reference_value is 'Значение ссылки';
------------------------------------------------------------------------------------------------------
create table form_column (
  id number(9) not null,
  name varchar(1000) not null,
  form_template_id number(9) not null,
  ord number(9) not null,
  alias varchar(100) not null,
  type char(1) not null,
  width number(9) not null,
  precision number(9),
  group_name varchar(1000),
  max_length number(4),
  checking  number(1) default 0 not null,
  attribute_id number(9),
  format number(2),
  filter varchar2(1000)
);
alter table form_column add constraint form_column_pk primary key (id);
create sequence seq_form_column start with 10000;

alter table form_column add constraint form_column_fk_form_templ_id foreign key (form_template_id) references form_template(id);
alter table form_column add constraint form_column_uniq_alias unique(form_template_id, alias);
alter table form_column add constraint form_column_chk_type check(type in ('N', 'S', 'D', 'R'));
alter table form_column add constraint form_column_chk_precision check((type = 'N' and precision is not null and precision >=0 and precision < 9) or (type <> 'N' and precision is null));
alter table form_column add constraint form_column_chk_max_length
check ((type = 'S' and max_length is not null and max_length > 0 and max_length <= 1000) or (type = 'N' and max_length is not null and max_length > 0 and max_length <= 27) or ((type ='D' or type ='R') and max_length is null));
alter table form_column add constraint form_column_chk_checking check (checking in (0, 1));
alter table form_column add constraint form_column_chk_attribute_id check ((type = 'R' and attribute_id is not null and precision >=0 and precision < 9) or (type <> 'R' and attribute_id is null));

alter table form_column add constraint form_column_fk_attribute_id foreign key (attribute_id) references ref_book_attribute (id);

comment on table form_column is 'Описания столбцов налоговых форм';
comment on column form_column.alias is 'Код столбца, используемый в скриптинге';
comment on column form_column.form_template_id is 'Идентификатор шаблона налоговой формы';
comment on column form_column.group_name is 'Название группы столбцов';
comment on column form_column.id is 'Первичный ключ';
comment on column form_column.name is 'Название столбца';
comment on column form_column.ord is 'Порядковый номер';
comment on column form_column.precision is 'Количество знаков после запятой (только для числовых столбцов)';
comment on column form_column.type is 'Тип столбца (S - строка, N – число, D – дата, R - ссылка)';
comment on column form_column.width is 'Ширина (в символах)';
comment on column form_column.checking is 'Признак проверочного столбца';
comment on column form_column.attribute_id is 'Код отображаемого атрибута для столбцов-ссылок';
comment on column form_column.format is 'Формат';
comment on column form_column.filter is 'Условие фильтрации элементов справочника';
---------------------------------------------------------------------------------------------------
create table department (
  id number(9) not null,
  name varchar2(510) not null,
  parent_id number(9) null,
  type number(9) not null,
  shortname   varchar2(510),
  dict_region_id varchar2(2),
  tb_index    varchar2(3),
  sbrf_code   varchar2(255)
);
alter table department add constraint department_pk primary key (id);
alter table department add constraint department_fk_dict_region_id foreign key (dict_region_id) references dict_region(code);
alter table department add constraint dept_fk_parent_id foreign key (parent_id) references department(id);

comment on table department is 'Подразделения банка';
comment on column department.id is 'Идентификатор записи';
comment on column department.name is 'Наименование подразделения';
comment on column department.parent_id is 'Идентификатор родительского подразделения';
comment on column department.type is 'Тип подразделения (1 - Банк, 2- ТБ, 3- ГОСБ, 4- ОСБ, 5- ВСП, 6-ПВСП)';
comment on column department.shortname is 'Сокращенное наименование подразделения';
comment on column department.dict_region_id is 'Код субъекта РФ';
comment on column department.tb_index is 'Индекс территориального банка';
comment on column department.sbrf_code is 'Код подразделения в нотации Сбербанка';

alter table department add constraint department_chk_id check ((type= 1 and id = 1) or (type <> 1 and id <> 1));
alter table department add constraint department_chk_parent_id check ((type = 1 and parent_id is null) or (type <> 1 and parent_id is not null));
---------------------------------------------------------------------------------------------------
create table report_period (
  id number(9) not null,
  name varchar2(50) not null,
  is_active number(1) default 1 not null,
  months  number(2) not null,
  tax_period_id number(9) not null,
  ord      number(2) not null,
  department_id number(15) not null,
  is_balance_period number(1) default 0 not null,
  dict_tax_period_id number(9) not null
);

alter table report_period add constraint report_period_pk primary key(id);
alter table report_period add constraint report_period_fk_taxperiod foreign key (tax_period_id) references tax_period (id);
alter table report_period add constraint report_period_chk_active check (is_active in (0, 1));
alter table report_period add constraint report_period_chk_balance check (is_balance_period in (0, 1));
alter table report_period add constraint report_period_fk_department_id foreign key (department_id) references department(id);
alter table report_period add constraint report_period_fk_dtp_id foreign key (dict_tax_period_id) references ref_book_record(id);

comment on table report_period is 'Отчетные периоды';
comment on column report_period.id is 'Первичный ключ';
comment on column report_period.name is 'Наименование периода';
comment on column report_period.is_active is 'Признак активности';
comment on column report_period.months is 'Количество месяцев в периоде';
comment on column report_period.tax_period_id is 'Налоговый период';
comment on column report_period.ord is 'Номер отчетного периода в налоговом';
comment on column report_period.is_balance_period is 'Признак того, что период является периодом ввода остатков';
comment on column report_period.department_id is 'Подразделение';
comment on column report_period.dict_tax_period_id is 'Ссылка на справочник отчетных периодов';

create sequence seq_report_period start with 100;
----------------------------------------------------------------------------------------------------
create table income_101 (
  report_period_id number(9) not null,
  account varchar2(255) not null,
  income_debet_remains number(22,4),
  income_credit_remains number(22,4),
  debet_rate number(22,4),
  credit_rate number(22,4),
  outcome_debet_remains number(22,4),
  outcome_credit_remains number(22,4),
  department_id number(15) not null
);

alter table income_101 add constraint income_101_pk primary key (report_period_id, account,department_id);
alter table income_101 add constraint income_101_fk_department_id foreign key (department_id)references department(id);
alter table income_101 add constraint income_101_fk_report_period_id foreign key (report_period_id) references report_period(id);

comment on table income_101 is 'Оборотная ведомость (Форма 0409101-СБ)';
comment on column income_101.report_period_id is 'Идентификатор отчетного периода';
comment on column income_101.account is 'Номер счета';
comment on column income_101.income_debet_remains is 'Входящие остатки по дебету';
comment on column income_101.income_credit_remains is 'Входящие остатки по кредиту';
comment on column income_101.debet_rate is 'Обороты по дебету';
comment on column income_101.credit_rate is 'Обороты по кредиту';
comment on column income_101.outcome_debet_remains is 'Исходящие остатки по дебету';
comment on column income_101.outcome_credit_remains is 'Исходящие остатки по кредиту';
comment on column income_101.department_id is 'Подразделение';
-------------------------------------------------------------------------------------------------------------------------------------------
create table income_102 (
  report_period_id number(9) not null,
  opu_code varchar2(25) not null,
  total_sum number(22,4),
  department_id number(15) not null);

alter table income_102 add constraint income_102_pk primary key (report_period_id, opu_code,department_id);
alter table income_102 add constraint income_102_fk_department_id foreign key (department_id) references department(id);
alter table income_102 add constraint income_102_fk_report_period_id foreign key (report_period_id) references report_period(id);

comment on table income_102 is 'Отчет о прибылях и убытках (Форма 0409102-СБ)';
comment on column income_102.report_period_id is 'Идентификатор отчетного периода';
comment on column income_102.opu_code is 'Код ОПУ';
comment on column income_102.total_sum is 'Сумма';
comment on column income_102.department_id is 'Подразделение';
---------------------------------------------------------------------------------------------------
create table declaration_type (
  id       number(9) not null,
  tax_type    char(1) not null,
  name      varchar(80) not null
);
alter table declaration_type add constraint declaration_type_pk primary key (id);
alter table declaration_type add constraint declaration_type_chk_tax_type check (tax_type in ('I', 'P', 'T', 'V', 'D'));

comment on table declaration_type is ' Виды деклараций';
comment on column declaration_type.id is 'Идентификатор (первичный ключ)';
comment on column declaration_type.tax_type is 'Вид налога (I-на прибыль, P-на имущество, T-транспортный, V-НДС, D-ТЦО)';
comment on column declaration_type.name is 'Наименование';
-----------------------------------------------------------------------------------------------------------------------------------
create table department_declaration_type (
  id         number(9) not null,
  department_id    number(9) not null,
  declaration_type_id number(9) not null
);
alter table department_declaration_type add constraint dept_decl_type_pk primary key (id);
alter table department_declaration_type add constraint dept_decl_type_fk_dept foreign key (department_id) references department (id);
alter table department_declaration_type add constraint dept_decl_type_fk_decl_type foreign key (declaration_type_id) references declaration_type (id);

comment on table department_declaration_type is 'Сведения о декларациях, с которыми можно работать в подразделении';
comment on column department_declaration_type.id is 'Идентификатор (первичный ключ)';
comment on column department_declaration_type.department_id is 'Идентификатор подразделения';
comment on column department_declaration_type.declaration_type_id is 'Вид декларации';

create sequence seq_dept_declaration_type start with 10000;
-----------------------------------------------------------------------------------------------------------------------------------
create table declaration_template (
  id       number(9) not null,
  edition    number(9) not null,
  version    varchar2(20) not null,
  is_active   number(1) not null,
  create_script       clob,
  jrxml               clob,
  jasper              blob,
  declaration_type_id number(9) not null
);
alter table declaration_template add constraint declaration_template_pk primary key (id);
alter table declaration_template add constraint declaration_t_chk_is_active check (is_active in (0,1));
alter table declaration_template add constraint declaration_template_fk_dtype foreign key (declaration_type_id) references declaration_type (id);

comment on table declaration_template is 'Шаблоны налоговых деклараций';
comment on column declaration_template.id is 'Идентификатор (первичный ключ)';
comment on column declaration_template.edition is 'Номер редакции';
comment on column declaration_template.version is 'Версия';
comment on column declaration_template.is_active is 'Признак активности';
comment on column declaration_template.create_script is 'Скрипт формирования декларации';
comment on column declaration_template.jrxml is 'Макет JasperReports для формирования печатного представления формы';
comment on column declaration_template.jasper is 'Скомпилированный макет JasperReports для формирования печатного представления формы';
comment on column declaration_template.declaration_type_id is 'Вид деклараций';

create sequence seq_declaration_template start with 10000;

-----------------------------------------------------------------------------------------------------------------------------------
create table declaration_data (
  id number(18) not null,
  declaration_template_id number(9) not null,
  report_period_id    number(9) not null,
  department_id      number(9) not null,
  data          clob,
  is_accepted       number(1) not null,
  data_pdf        blob,
  data_xlsx        blob
);
alter table declaration_data add constraint declaration_data_pk primary key (id);
alter table declaration_data add constraint declaration_data_fk_decl_t_id foreign key (declaration_template_id) references declaration_template (id);
alter table declaration_data add constraint declaration_data_fk_rep_per_id foreign key (report_period_id) references report_period (id);
alter table declaration_data add constraint declaration_data_fk_dep_id foreign key (department_id) references department (id);
alter table declaration_data add constraint declaration_data_chk_is_accptd check (is_accepted in (0,1));
alter table declaration_data add constraint declaration_data_uniq_template unique(report_period_id, department_id, declaration_template_id);

comment on table declaration_data is 'Налоговые декларации';
comment on column declaration_data.id is 'Идентификатор (первичный ключ)';
comment on column declaration_data.declaration_template_id is 'Ссылка на шаблон декларации';
comment on column declaration_data.report_period_id is 'Отчётный период';
comment on column declaration_data.department_id is 'Подразделение';
comment on column declaration_data.data is 'Данные декларации в формате законодателя (XML) ';
comment on column declaration_data.is_accepted is 'Признак того, что декларация принята';
comment on column declaration_data.data_pdf is 'pdf';
comment on column declaration_data.data_xlsx is 'xlsx';

create sequence seq_declaration_data start with 10000;
------------------------------------------------------------------------------------------------------------------------------------------
create table form_data (
  id number(18) not null,
  form_template_id number(9) not null,
  department_id number(9) not null,
  state number(9) not null,
  kind number(9) not null,
  report_period_id number(9) not null,
  acceptance_date date,
  creation_date date default sysdate not null,
  return_sign number(1) not null
);
alter table form_data add constraint form_data_pk primary key (id);
alter table form_data add constraint form_data_fk_form_templ_id foreign key (form_template_id) references form_template(id);
alter table form_data add constraint form_data_fk_dep_id foreign key (department_id) references department(id);
alter table form_data add constraint form_data_fk_period_id foreign key (report_period_id) references report_period(id);
alter table form_data add constraint form_data_chk_kind check(kind in (1,2,3,4,5));
alter table form_data add constraint form_data_chk_state check(state in (1,2,3,4));
alter table form_data add constraint form_data_chk_return_sign check(return_sign in (0,1));

comment on table form_data is 'Данные по налоговым формам';
comment on column form_data.id is 'Первичный ключ';
comment on column form_data.form_template_id is 'Идентификатор шаблона формы';
comment on column form_data.department_id is 'Идентификатор подраздения';
comment on column form_data.state is 'Код состояния';
comment on column form_data.kind is 'Тип налоговой формы (1 - Первичная, 2 - Консолидированная, 3 - Сводная, 4 - Форма УНП, 5 - Выходная)';
comment on column form_data.report_period_id is 'Идентификатор отчетного периода';
comment on column form_data.creation_date is 'Дата создания';
comment on column form_data.return_sign is 'Флаг возврата (0 - обычный режим; 1 - форма возвращена из вышестоящего статуса)';

create sequence seq_form_data start with 10000;
---------------------------------------------------------------------------------------------------
create table form_data_signer (
  id      number(18) not null,
  form_data_id number(18) not null,
  name     varchar2(200) not null,
  position   varchar2(200) not null,
  ord     number(3) not null
);
alter table form_data_signer add constraint form_data_signer_pk primary key (id);
alter table form_data_signer add constraint form_data_signer_fk_formdata foreign key (form_data_id) references form_data (id) on delete cascade;

comment on table form_data_signer is 'Подписанты налоговых форм';
comment on column form_data_signer.id is 'Идентфикатор записи (первичный ключ)';
comment on column form_data_signer.form_data_id is 'Идентификатор налоговой формы';
comment on column form_data_signer.name is 'ФИО';
comment on column form_data_signer.position is 'Должность';
comment on column form_data_signer.ord is 'Номер подписанта по порядку';

create sequence seq_form_data_signer start with 10000;
---------------------------------------------------------------------------------------------------
create table form_data_performer (
  form_data_id number(18) not null,
  name varchar2(200) not null,
  phone varchar2(20)
);
alter table form_data_performer add constraint form_data_performer_pk primary key (form_data_id);
alter table form_data_performer add constraint formdata_performer_fk_formdata foreign key (form_data_id) references form_data (id) on delete cascade;

comment on table form_data_performer is 'Исполнитель налоговой формы';
comment on column form_data_performer.form_data_id is 'Первичный ключ';
comment on column form_data_performer.name is 'ФИО исполнителя';
comment on column form_data_performer.phone is 'Телефон';
--------------------------------------------------------------------------------------------------
create table data_row (
  id number(18) not null,
  form_data_id number(18) not null,
  alias varchar(20),
  ord number(14,0) not null,
  type number(1) not null
);
alter table data_row add constraint data_row_pk primary key (id);
alter table data_row add constraint data_row_fk_form_data_id foreign key (form_data_id) references form_data(id) on delete cascade;
alter table data_row add constraint data_row_uniq_form_data_order unique(form_data_id, ord, type);
alter table data_row add constraint data_row_chk_type check (type in (-1, 0, 1));

comment on table data_row is 'Строки данных налоговых форм';
comment on column data_row.alias is 'Идентификатор строки';
comment on column data_row.form_data_id is 'Ссылка на записть в FORM_DATA';
comment on column data_row.id is 'Код строки для доступа из скриптов';
comment on column data_row.ord is 'Номер строки в форме';
comment on column data_row.type is 'тип строки (0 - подтвержденные данные, 1 - строка добавлена, -1 - строка удалена)';

create sequence seq_data_row start with 10000;
---------------------------------------------------------------------------------------------------
create table cell_style (
  row_id  number(18) not null,
  column_id number(9) not null,
  style_id number(9) not null
);
alter table cell_style add constraint cell_style_pk primary key (row_id, column_id);
alter table cell_style add constraint cell_style_fk_column_id foreign key (column_id) references form_column (id);
alter table cell_style add constraint cell_style_fk_data_row foreign key (row_id) references data_row (id) on delete cascade;
alter table cell_style add constraint cell_style_fk_style_id foreign key (style_id) references form_style (id);

comment on table cell_style is 'Привязка стилей к ячейкам налоговой формы';
comment on column cell_style.row_id is 'Идентификатор строки';
comment on column cell_style.column_id is 'Идентификатор столбца';
comment on column cell_style.style_id is 'Идентификатор стиля';
---------------------------------------------------------------------------------------------------
create table cell_editable(
  row_id number(18) not null,
  column_id number(9) not null
);
alter table cell_editable add constraint cell_editable_pk primary key (row_id, column_id);
alter table cell_editable add constraint cell_editable_fk_data_row foreign key (row_id) references data_row (id) on delete cascade;
alter table cell_editable add constraint cell_editable_fk_form_column foreign key (column_id) references form_column (id);

comment on table cell_editable is 'Информация о редактируемых ячейках налоговой формы';
comment on column cell_editable.row_id is 'Идентификатор строки налоговой формы';
comment on column cell_editable.column_id is 'Идентификатор столбца налоговой формы';
---------------------------------------------------------------------------------------------------
create table numeric_value (
  row_id number(18) not null,
  column_id number(9) not null,
  value     decimal(27, 10)
);
alter table numeric_value add constraint numeric_value_pk primary key (row_id, column_id);
alter table numeric_value add constraint numeric_value_fk_column_id foreign key (column_id) references form_column(id);
alter table numeric_value add constraint numeric_value_fk_row_id foreign key (row_id) references data_row(id) on delete cascade;
comment on table numeric_value is 'Числовые значения налоговых форм';
comment on column numeric_value.column_id is 'Идентификатор столбца';
comment on column numeric_value.row_id is 'Идентификатор строки';
comment on column numeric_value.value is 'Значение';
---------------------------------------------------------------------------------------------------
create table string_value (
  row_id number(18) not null,
  column_id number(9) not null,
  value     varchar2(2000 char)
);
alter table string_value add constraint string_value_pk primary key (row_id, column_id);
alter table string_value add constraint string_value_fk_column_id foreign key (column_id) references form_column(id);
alter table string_value add constraint string_value_fk_row_id foreign key (row_id) references data_row(id) on delete cascade;
comment on table string_value is 'Строковые значения налоговых форм';
comment on column string_value.column_id is 'Идентификатор столбца';
comment on column string_value.row_id is 'Идентификатор строки';
comment on column string_value.value is 'Значение';
---------------------------------------------------------------------------------------------------
create table date_value (
  row_id number(18) not null,
  column_id number(9) not null,
  value date
);
alter table date_value add constraint date_value_pk primary key (row_id, column_id);
alter table date_value add constraint date_value_fk_column_id foreign key (column_id) references form_column(id);
alter table date_value add constraint date_value_fk_row_id foreign key (row_id) references data_row(id) on delete cascade;
comment on table date_value is 'Значения налоговых форм типа дата';
comment on column date_value.column_id is 'Идентификатор столбца';
comment on column date_value.row_id is 'Идентификатор строки';
comment on column date_value.value is 'Значение';
---------------------------------------------------------------------------------------------------
create table department_form_type (
  id      number(9) not null,
  department_id number(9) not null,
  form_type_id number(9) not null,
  kind     number(9) not null
);

alter table department_form_type add constraint dept_form_type_fk_dep_id foreign key (department_id) references department(id);
alter table department_form_type add constraint dept_form_type_fk_type_id foreign key (form_type_id) references form_type(id);

alter table department_form_type add constraint dept_form_type_pk primary key (id);
alter table department_form_type add constraint dept_form_type_uniq_form unique (department_id, form_type_id, kind);
alter table department_form_type add constraint dept_form_type_chk_kind check (kind in (1,2,3,4,5));

comment on table department_form_type is 'Связь подразделения банка с формой';
comment on column department_form_type.id is 'Первичный ключ';
comment on column department_form_type.department_id is 'Идентификатор подразделения';
comment on column department_form_type.form_type_id is 'Идентификатор вида налоговой формы';
comment on column department_form_type.kind is 'Тип налоговой формы (1-Первичная, 2-Консолидированная, 3-Сводная, 4-Форма УНП, 5-Выходная)';

create sequence seq_department_form_type start with 10000;
---------------------------------------------------------------------------------------------------
create table declaration_source (
  department_declaration_type_id number(9) not null,
  src_department_form_type_id   number(9) not null
);
alter table declaration_source add constraint declaration_source_pk primary key (department_declaration_type_id,src_department_form_type_id );
alter table declaration_source add constraint decl_source_fk_dept_decltype foreign key (department_declaration_type_id) references department_declaration_type (id);
alter table declaration_source add constraint decl_source_fk_dept_formtype foreign key (src_department_form_type_id) references department_form_type (id);

comment on table declaration_source is 'Информация о формах-источниках данных для деклараций разных видов';
comment on column declaration_source.department_declaration_type_id is 'Иденфтикиатор сочетания вида декларации и подразделения, для которого задаётся источник';
comment on column declaration_source.src_department_form_type_id is 'Идентификатор сочетания типа и вида формы, а также подразделения, которые являются источников данных для деклараций';
----------------------------------------------------------------------------------------------------
create table form_data_source (
  department_form_type_id number(9) not null,
  src_department_form_type_id number(9) not null
);
alter table form_data_source add constraint form_data_source_pk primary key (department_form_type_id, src_department_form_type_id);
alter table form_data_source add constraint form_data_source_fk_dep_id foreign key (department_form_type_id) references department_form_type(id);
alter table form_data_source add constraint form_data_source_fk_src_dep_id foreign key (src_department_form_type_id) references department_form_type(id);
comment on table form_data_source is 'Информация об источниках данных для формирования консолидированных и сводных налоговоых форм';
comment on column form_data_source.department_form_type_id is 'Идентификатор сочетания вида, типа формы и подразделения, для которого задётся источник данных';
comment on column form_data_source.src_department_form_type_id is 'Идентификатор сочетания вида, типа формы и подразделения, которое является источником данных';
------------------------------------------------------------------------------------------------------------------------------------------------------------------
create table sec_user (
  id number(9) not null,
  login varchar(255) not null,
  name varchar(50) not null,
  department_id number(9) not null,
  is_active number(1) not null,
  email varchar2(128)
);
alter table sec_user add constraint sec_user_pk primary key (id);
alter table sec_user add constraint sec_user_fk_dep_id foreign key (department_id) references department(id);
alter table sec_user add constraint sec_user_uniq_login_active unique (login);

comment on table sec_user is 'Пользователи системы';
comment on column sec_user.id is 'Первичный ключ';
comment on column sec_user.login is 'Логин пользователя';
comment on column sec_user.name is 'Полное имя пользователя';
comment on column sec_user.department_id is 'Идентификатор подразделения';
comment on column sec_user.is_active is 'Признак активностии пользователя';
comment on column sec_user.email is 'Адрес электронной почты';

create sequence seq_sec_user start with 10000;
---------------------------------------------------------------------------------------------------
create table object_lock (
  object_id number(20) not null,
  class varchar(100) not null,
  user_id number(9) not null,
  lock_time date not null
);
alter table object_lock add constraint object_lock_pk primary key (object_id, class);
alter table object_lock add constraint object_lock_fk_user_id foreign key (user_id) references sec_user (id) on delete cascade;

comment on table object_lock is 'Сведения о пользовательских блокировках объектов';
comment on column object_lock.object_id is 'Идентификатор объекта';
comment on column object_lock.class is 'Имя класса объекта';
comment on column object_lock.user_id is 'Идентифкатор пользователя, заблокировавшего объект';
comment on column object_lock.lock_time is 'Время блокировки';
-------------------------------------------------------------------------------------------------------------------------------------
create table sec_role (
  id number(9) not null,
  alias varchar(20) not null,
  name varchar(50) not null
);
alter table sec_role add constraint sec_role_pk primary key (id);
alter table sec_role add constraint sec_role_uniq_alias unique (alias);

comment on table sec_role is 'Системные роли';
comment on column sec_role.id is 'Первичный ключ';
comment on column sec_role.alias is 'Код роли (мнемонический идентификатор)';
comment on column sec_role.name is 'Наименование роли';
---------------------------------------------------------------------------------------------------
create table sec_user_role (
  user_id number(9) not null,
  role_id number(9) not null
);

alter table sec_user_role add constraint sec_user_role_pk primary key (user_id, role_id);
alter table sec_user_role add constraint sec_user_role_fk_user_id foreign key (user_id) references sec_user(id);
alter table sec_user_role add constraint sec_user_role_fk_role_id foreign key (role_id) references sec_role(id);

comment on table sec_user_role is 'Привязка ролей к пользователям';
comment on column sec_user_role.user_id is 'Идентификатор пользователя';
comment on column sec_user_role.role_id is 'Идентификатор роли';
----------------------------------------------------------------------------------------------------
create table cell_span_info (
  row_id number(18) not null,
  column_id number(9) not null,
  colspan number(3),
  rowspan number(3)
);
alter table cell_span_info add constraint cell_span_pk primary key (row_id, column_id);
alter table cell_span_info add constraint cell_span_info_fk_row_id foreign key (row_id) references data_row (id) on delete cascade;
alter table cell_span_info add constraint cell_span_info_fk_column_id foreign key (column_id) references form_column (id);
alter table cell_span_info add constraint cell_span_info_chk_span check (colspan is not null or rowspan is not null);

comment on table cell_span_info is 'Информация об объединении ячеек в налоговой форме';
comment on column cell_span_info.row_id is 'Идентификатор строки';
comment on column cell_span_info.column_id is 'Идентификатор столбца';
comment on column cell_span_info.colspan is 'Число ячеек, которые должны быть объединены по горизонтали';
comment on column cell_span_info.rowspan is 'Число ячеек, которые должны быть объединены по вертикали';
----------------------------------------------------------------------------------------------------
create table department_param (
  department_id number(9) not null,
  dict_region_id varchar2(2) not null,
  okato varchar2(11) not null,
  inn varchar2(10) not null,
  kpp varchar2(9) not null,
  tax_organ_code varchar2(4) not null,
  okved_code varchar2(8) not null,
  phone varchar2(20),
  reorg_form_code varchar2(1),
  reorg_inn varchar2(10),
  reorg_kpp varchar2(9),
  name varchar2(2000)
);
alter table department_param add constraint department_param_pk primary key (department_id);
alter table department_param add constraint dept_param_fk_dict_region_id foreign key (dict_region_id) references dict_region(code);
alter table department_param add constraint dept_param_fk_dept_id foreign key (department_id) references department(id);

comment on table department_param is 'Общие сведения';
comment on column department_param.department_id is 'Идентификатор (первичный ключ)';
comment on column department_param.dict_region_id is 'Субъект Российской Федерации (код)';
comment on column department_param.okato is 'Код по ОКАТО';
comment on column department_param.inn is 'ИНН';
comment on column department_param.kpp is 'КПП';
comment on column department_param.tax_organ_code is 'Код налогового органа';
comment on column department_param.okved_code is 'Код вида экономической деятельности и по классификатору ОКВЭД';
comment on column department_param.phone is 'Номер контактного телефона';
comment on column department_param.reorg_form_code is 'Код формы реорганизации и ликвидации';
comment on column department_param.reorg_inn is 'ИНН реорганизованного обособленного подразделения';
comment on column department_param.reorg_kpp is 'КПП реорганизованного обособленного подразделения';
comment on column department_param.name is 'Наименование обособленного подразделения';
---------------------------------------------------------------------------------------------------------------------------------------------------
create table department_param_income (
  department_id    number(9) not null,
  signatory_id    number(1) not null,
  signatory_surname  varchar2(120) not null,
  signatory_firstname varchar2(120),
  signatory_lastname varchar2(120),
  approve_doc_name  varchar2(240),
  approve_org_name  varchar2(2000),
  tax_place_type_code varchar2(3) not null,
  tax_rate      number(4,2),
  external_tax_sum  number(15),
  sum_difference   number(15),
  correction_sum   number(15),
  app_version     varchar2(40),
  format_version   varchar2(5)
);
alter table department_param_income add constraint department_param_income_pk primary key (department_id);
alter table department_param_income add constraint dept_param_income_chk_taxplace check (tax_place_type_code in ('213','214','215','216','218','220','223','225','226','231'));
alter table department_param_income add constraint dept_param_income_fk_dept_id foreign key (department_id) references department(id);

comment on table department_param_income is 'Параметры подразделения по налогу на прибыль';
comment on column department_param_income.department_id is 'Идентификатор (первичный ключ)';
comment on column department_param_income.signatory_id is 'Признак лица подписавшего документ';
comment on column department_param_income.signatory_surname is 'Фамилия подписанта';
comment on column department_param_income.signatory_firstname is 'Имя подписанта';
comment on column department_param_income.signatory_lastname is 'Отчество подписанта';
comment on column department_param_income.approve_doc_name is 'Наименование документа, подтверждающего полномочия представителя';
comment on column department_param_income.approve_org_name is 'Наименование организации-представителя налогоплательщика';
comment on column department_param_income.tax_place_type_code is 'Код места, по которому представляется документ';
comment on column department_param_income.tax_rate is 'Ставка налога (региональная часть)';
comment on column department_param_income.external_tax_sum is 'Сумма налога, выплаченная за пределами Российской Федерации и засчитываемая в уплату налога согласно порядку, установленному ст. 311 НК ';
comment on column department_param_income.sum_difference is 'Суммы отклонения от максимальной (расчетной) цены ';
comment on column department_param_income.correction_sum is 'Внереализационные доходы в виде сумм корректировки прибыли вследствие применения методов определения для целей налогообложения соответствия цен, примененных в сделках, рыночным ценам (рентабельности), предусмотренным статьями 105.12 и 105.13 НК ';
comment on column department_param_income.app_version is 'Версия программы, с помощью которой сформирован файл';
comment on column department_param_income.format_version is 'Версия формата';
--------------------------------------------------------------------------------------------------------------------------------------------------------
create table department_param_transport (
  department_id       number(9) not null,
  signatory_id        number(1) not null,
  signatory_surname   varchar2(120) not null,
  signatory_firstname varchar2(120),
  signatory_lastname  varchar2(120),
  approve_doc_name    varchar2(240),
  approve_org_name    varchar2(2000),
  tax_place_type_code varchar2(3) not null,
  app_version         varchar2(40),
  format_version      varchar2(5)
);
alter table department_param_transport add constraint department_param_transport_pk primary key (department_id);
alter table department_param_transport add constraint dept_param_transport_fk_deptid foreign key (department_id) references department(id);

comment on table department_param_transport is 'Параметры подразделения по транспортному налогу';
comment on column department_param_transport.department_id is 'Идентификатор (первичный ключ)';
comment on column department_param_transport.signatory_id is 'Признак лица подписавшего документ';
comment on column department_param_transport.signatory_surname is 'Фамилия подписанта';
comment on column department_param_transport.signatory_firstname is 'Имя подписанта';
comment on column department_param_transport.signatory_lastname is 'Отчество подписанта';
comment on column department_param_transport.approve_doc_name is 'Наименование документа, подтверждающего полномочия представителя';
comment on column department_param_transport.approve_org_name is 'Наименование организации-представителя налогоплательщика';
comment on column department_param_transport.tax_place_type_code is 'Код места, по которому представляется документ';
comment on column department_param_transport.app_version is 'Версия программы, с помощью которой сформирован файл';
comment on column department_param_transport.format_version is 'Версия формата';
----------------------------------------------------------------------------------------------------
create table log_business (
  id                  number(18,0) primary key,
  log_date            date not null,
  event_id            number(3,0) not null,
  user_id             number(9,0) not null,
  roles               varchar2(200) not null,
  declaration_data_id number(9,0),
  form_data_id        number(9,0),
  note                varchar2(510),
  user_department_id  number(9,0) not null
);

alter table log_business add constraint log_business_fk_user_id foreign key (user_id) references sec_user (id);
alter table log_business add constraint log_business_fk_declaration_id foreign key (declaration_data_id) references declaration_data(id) on delete cascade;
alter table log_business add constraint log_business_fk_form_data_id foreign key (form_data_id) references form_data (id) on delete cascade;

alter table log_business add constraint log_business_chk_event_id check (event_id in (1, 2, 3, 4, 5, 6, 7, 8, 9, 101, 102,
  103, 104, 105, 106, 107, 108, 109, 110, 111, 112, 203, 204, 205, 206, 207, 208, 209, 210, 301, 302, 303));
alter table log_business add constraint log_business_chk_frm_dcl_ev check (form_data_id is not null or declaration_data_id is not null);
alter table log_business add constraint log_business_fk_usr_departm_id foreign key (user_department_id) references department (id);

comment on table log_business is 'Журнал событий налоговых форм\деклараций';

comment on column log_business.id is 'Код записи';
comment on column log_business.log_date is 'Дата события';
comment on column log_business.event_id is 'Код события (1 - создать,2 - удалить,3 - рассчитать,4 - обобщить,5 - проверить,6 - сохранить,7 - импорт данных,101 - утвердить,102 - вернуть из \утверждена\ в \создана\,103 - принять из \утверждена\,104 - вернуть из \принята\ в \утверждена\,105 - принять из \создана\,106 - вернуть из \принята\ в \создана\,107 - подготовить,108, вернуть из \подготовлена\ в \создана\,109, принять из \подготовлена\,110, вернуть из \принята\ в \подготовлена\,203 - после принять из \утверждена\,204 - после вернуть из \принята\ в \утверждена\,205 - после принять из \создана\,206 - после вернуть из \принята\ в \создана\,207 - после принять из \"подготовлена\,301 - добавить строку,303 - удалить строку,302 - загрузка)';
comment on column log_business.user_id is 'Код пользователя';
comment on column log_business.roles is 'Список ролей пользователя';
comment on column log_business.declaration_data_id is 'Код декларации';
comment on column log_business.form_data_id is 'Код налоговой формы';
comment on column log_business.note is 'Текст сообщения';
comment on column log_business.user_department_id is 'Код подразделения пользователя';
------------------------------------------------------------------------------------------------------
create index i_department_parent_id on department(parent_id);
create index i_data_row_form_data_id on data_row(form_data_id);
create index i_form_data_report_period_id on form_data(report_period_id);
create index i_form_data_form_template_id on form_data(form_template_id);
create index i_form_data_department_id on form_data(department_id);
create index i_form_data_kind on form_data(kind);
create index i_form_data_signer_formdataid on form_data_signer(form_data_id);
------------------------------------------------------------------------------------------------------
create table log_system (
  id                  number(18,0) primary key,
  log_date            date not null,
  ip                  varchar2(39),
  event_id            number(3,0) not null,
  user_id             number(9,0),
  roles               varchar2(200),
  department_id       number(9,0) not null,
  report_period_id    number(9,0),
  declaration_type_id number(9,0),
  form_type_id        number(9,0),
  form_kind_id        number(9,0),
  note                varchar2(510),
  user_department_id  number(9,0)
);
alter table log_system add constraint log_system_chk_form_kind_id check (form_kind_id in (1, 2, 3, 4, 5));
alter table log_system add constraint log_system_chk_event_id check (event_id in (1, 2, 3, 4, 5, 6, 7, 8, 9, 101, 102,
  103, 104, 105, 106, 107, 108, 109, 110, 111, 112, 203, 204, 205, 206, 207, 208, 209, 210, 301, 302, 303, 501, 502));

alter table log_system add constraint log_system_chk_dcl_form check (event_id in (501, 502) or
  declaration_type_id is not null or (form_type_id is not null and form_kind_id is not null));
alter table log_system add constraint log_system_chk_rp check (event_id in (501, 502) or report_period_id is not null);

alter table log_system add constraint log_system_fk_user_id foreign key (user_id) references sec_user (id);
alter table log_system add constraint log_system_fk_department_id foreign key (department_id) references department(id);
alter table log_system add constraint log_system_fk_report_period_id foreign key (report_period_id) references report_period(id);
alter table log_system add constraint log_system_fk_decl_type_id foreign key (declaration_type_id) references declaration_type (id);
alter table log_system add constraint log_system_fk_form_type_id foreign key (form_type_id) references form_type(id);
alter table log_system add constraint log_system_fk_user_dep_id foreign key (user_department_id) references department (id);

comment on table log_system is  'Системный журнал';

comment on column log_system.id is 'Код записи';
comment on column log_system.log_date is 'Дата события';
comment on column log_system.ip is 'IP-адрес пользователя';
comment on column log_system.event_id is 'Код события (1 - Создать,2 - Удалить,3 - Рассчитать,4 - Обобщить,5 - Проверить,6 - Сохранить,7 - Импорт данных,101 - Утвердить,102 - Вернуть из \Утверждена\ в \Создана\,103 - Принять из \Утверждена\,104 - Вернуть из \Принята\ в \Утверждена\,105 - Принять из \Создана\,106 - Вернуть из \Принята\ в \Создана\,107 - Подготовить,108 - Вернуть из \Подготовлена\ в \Создана\,109 - Принять из \Подготовлена\,110 - Вернуть из \Принята\ в \Подготовлена\,203 - После принять из \Утверждена\,204 - После вернуть из \Принята\ в \Утверждена\,205 - После принять из \Создана\,206 - После вернуть из \Принята\ в \Создана\,207 - После принять из \"Подготовлена\,301 - Добавить строку,303 - Удалить строку,302 - Загрузка)';
comment on column log_system.user_id is 'Код пользователя';
comment on column log_system.roles is 'Список ролей пользователя';
comment on column log_system.department_id is 'Код подразделения НФ\декларации';
comment on column log_system.report_period_id is 'Код отчетного периода';
comment on column log_system.declaration_type_id is 'Код вида декларации';
comment on column log_system.form_type_id is 'Код вида налоговой формы';
comment on column log_system.form_kind_id is 'Код типа налоговой формы (1,2,3,4,5)';
comment on column log_system.note is 'Текст сообщения';
comment on column log_system.user_department_id is 'Код подразделения пользователя';

create sequence seq_log_system start with 10000;
------------------------------------------------------------------------------------------------------