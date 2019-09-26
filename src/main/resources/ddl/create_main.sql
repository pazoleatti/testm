create table form_kind (
  id   number(18)    not null,
  name varchar2(100) not null
);
comment on table form_kind is 'Тип налоговой формы';
comment on column form_kind.id is 'Идентификатор записи';
comment on column form_kind.name is 'Наименование';
--------------------------------------------------------------------------------------------------------------
create table configuration (
  code          varchar2(50) not null,
  value         clob,
  department_id number(9)    not null
);
comment on table configuration is 'Настройки приложения, конфигурация';
comment on column configuration.code is 'Код параметра';
comment on column configuration.value is 'Значение параметра';
comment on column configuration.department_id is 'ТБ';
-------------------------------------------------------------------------------------------------------------------------------------------
create table form_type (
  id        number(9)           not null,
  name      varchar2(1000)      not null,
  tax_type  char(1)             not null,
  status    number(1) default 0 not null,
  code      varchar2(9 char),
  is_ifrs   number(1) default 0 not null,
  ifrs_name varchar2(200)
);
comment on table form_type is 'Типы налоговых форм (названия)';
comment on column form_type.id is 'Идентификатор';
comment on column form_type.name is 'Наименование';
comment on column form_type.tax_type is 'Вид налога';
comment on column form_type.status is 'Статус версии (0 - действующая версия; -1 - удаленная версия, 1 - черновик версии, 2 - фиктивная версия)';
comment on column form_type.code is 'Номер формы';
comment on column form_type.is_ifrs is 'Отчетность для МСФО" (0 - не отчетность МСФО, 1 - отчетность МСФО)';
comment on column form_type.ifrs_name is 'Наименование формы для файла данного макета, включаемого в архив с отчетностью для МСФО';

create sequence seq_form_type start with 10000;
---------------------------------------------------------------------------------------------------
create table tax_period (
  id       number(9) not null,
  tax_type char(1)   not null,
  year     number(4) not null
);
comment on table tax_period is 'Налоговые периоды';
comment on column tax_period.id is 'Идентификатор (первичный ключ)';
comment on column tax_period.tax_type is 'Вид налога';
comment on column tax_period.year is 'Год';

create sequence seq_tax_period start with 10000;
---------------------------------------------------------------------------------------------------
create table form_template (
  id           number(9)           not null,
  type_id      number(9)           not null,
  data_rows    clob,
  version      date                not null,
  fixed_rows   number(1)           not null,
  name         varchar2(1000)      not null,
  fullname     varchar2(1000)      not null,
  script       clob,
  data_headers clob,
  status       number(1) default 0 not null,
  monthly      number(1) default 0 not null,
  header       varchar2(1000),
  comparative  number(1) default 0,
  accruing     number(1) default 0,
  updating     number(1) default 0
);
comment on table form_template is 'Описания шаблонов налоговых форм';
comment on column form_template.data_rows is 'Предопределённые строки формы в формате XML';
comment on column form_template.id is 'Первичный ключ';
comment on column form_template.type_id is 'Идентификатор вида налоговой формы';
comment on column form_template.version is 'Версия формы (уникально в рамках типа)';
comment on column form_template.fixed_rows is 'Признак использования фиксированных строк: 0 - используется фиксированный набор строк, 1 - есть возможность добавлять и удалять строки из формы.';
comment on column form_template.name is 'Наименование формы';
comment on column form_template.fullname is 'Полное наименование формы';
comment on column form_template.script is 'Скрипт, реализующий бизнес-логику налоговой формы';
comment on column form_template.data_headers is 'Описание заголовка таблицы';
comment on column form_template.status is 'Статус версии (0 - действующая версия; -1 - удаленная версия, 1 - черновик версии, 2 - фиктивная версия)';
comment on column form_template.monthly is 'Признак ежемесячной формы (0 - не ежемесячная, 1 - ежемесячная)';
comment on column form_template.header is 'Верхний колонтитул печатной формы';
comment on column form_template.comparative is '"Признак использования периода сравнения (0 - не используется, 1 - используется)';
comment on column form_template.accruing is 'Признак расчета нарастающим итогом (0 - не используется, 1 - используется)';
comment on column form_template.updating is 'Отображать кнопку "Обновить" (0 - нет, 1 - да)';

create sequence seq_form_template start with 10000;
---------------------------------------------------------------------------------------------------
create table form_style (
  id               number(9)    not null,
  alias            varchar2(80) not null,
  form_template_id number(9)    not null,
  font_color       number(3)    null,
  back_color       number(3)    null,
  italic           number(1)    not null,
  bold             number(1)    not null
);
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
  id            varchar2(36)  not null,
  name          varchar2(530) null,
  data          blob          not null,
  creation_date date          not null
);
comment on table blob_data is 'Файловое хранилище';
comment on column blob_data.id is 'Уникальный идентификатор';
comment on column blob_data.name is 'Название файла';
comment on column blob_data.data is 'Бинарные данные';
comment on column blob_data.creation_date is 'Дата создания';
----------------------------------------------------------------------------------------------------
create table ref_book (
  id                  number(18, 0)       not null,
  name                varchar2(200)       not null,
  script_id           varchar2(36),
  visible             number(1) default 1 not null,
  type                number(1) default 0 not null,
  read_only           number(1) default 0 not null,
  region_attribute_id number(18, 0),
  table_name          varchar2(100),
  is_versioned        number(1) default 1 not null,
  xsd_id              varchar2(36 byte)
);

comment on table ref_book is 'Справочник';
comment on column ref_book.id is 'Уникальный идентификатор';
comment on column ref_book.name is 'Название справочника';
comment on column ref_book.script_id is 'Идентификатор связанного скрипта';
comment on column ref_book.visible is 'Признак видимости';
comment on column ref_book.type is 'Тип справочника (0 - Линейный, 1 - Иерархический)';
comment on column ref_book.read_only is 'Только для чтения (0 - редактирование доступно пользователю; 1 - редактирование недоступно пользователю)';
comment on column ref_book.region_attribute_id is 'При его наличии справочник считается региональным. Указывает на атрибут, по которому определяется принадлежность к региону';
comment on column ref_book.table_name is 'Название таблицы БД, в которой хранятся данные';
comment on column ref_book.is_versioned is 'Версионный справочник (0 - нет, 1 - да)';
comment on column ref_book.xsd_id is 'Идентификатор связанного XSD файла';
------------------------------------------------------------------------------------------------------
create table ref_book_attribute (
  id           number(18)           not null,
  ref_book_id  number(18)           not null,
  name         varchar2(510)        not null,
  alias        varchar2(30)         not null,
  type         number(1)            not null,
  ord          number(9)            not null,
  reference_id number(18),
  attribute_id number(18),
  visible      number(1) default 1  not null,
  precision    number(2),
  width        number(9) default 15 not null,
  required     number(1) default 0  not null,
  is_unique    number(1) default 0  not null,
  sort_order   number(9),
  format       number(2),
  read_only    number(1) default 0  not null,
  max_length   number(4)
);
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
comment on column ref_book_attribute.required is 'Признак обязательности поля (1 - обязательно; 0 - нет)';
comment on column ref_book_attribute.is_unique is 'Признак уникальности значения атрибута справочника (1 - должно быть уникальным; 0 - нет)';
comment on column ref_book_attribute.sort_order is 'Определяет порядок сортировки по умолчанию';
comment on column ref_book_attribute.format is 'Формат. (Для дат: 0 - "", 1 - "dd.MM.yyyy", 2 - "MM.yyyy", 3 - "MMMM yyyy", 4 - "yyyy", 5 - "dd.MM"; Для чисел: 6 - чекбокс)';
comment on column ref_book_attribute.read_only is 'Только для чтения (0 - редактирование доступно пользователю; 1 - редактирование недоступно пользователю)';
comment on column ref_book_attribute.max_length is 'Максимальная длина строки/Максимальное количество цифр без учета знака и десятичного разделителя';
------------------------------------------------------------------------------------------------------
create table ref_book_record (
  id          number(18)          not null,
  record_id   number(9)           not null,
  ref_book_id number(18)          not null,
  version     date                not null,
  status      number(1) default 0 not null
);
comment on table ref_book_record is 'Запись справочника';
comment on column ref_book_record.id is 'Уникальный идентификатор';
comment on column ref_book_record.record_id is 'Идентификатор строки справочника. Может повторяться у разных версий';
comment on column ref_book_record.ref_book_id is 'Ссылка на справочник, к которому относится запись';
comment on column ref_book_record.version is 'Версия. Дата актуальности записи';
comment on column ref_book_record.status is 'Статус записи (0 - обычная запись, -1 - удаленная, 1 - черновик, 2 - фиктивная)';

create sequence seq_ref_book_record start with 100000 increment by 100;
create sequence seq_ref_book_record_row_id start with 100000;
------------------------------------------------------------------------------------------------------
create table ref_book_value (
  record_id       number(18) not null,
  attribute_id    number(18) not null,
  string_value    varchar2(4000),
  number_value    number(38, 19),
  date_value      date,
  reference_value number(18)
);
comment on table ref_book_value is 'Значение записи справочника';
comment on column ref_book_value.record_id is 'Ссылка на запись справочника';
comment on column ref_book_value.attribute_id is 'Ссылка на атрибут справочника';
comment on column ref_book_value.string_value is 'Строковое значение';
comment on column ref_book_value.number_value is 'Численное значение';
comment on column ref_book_value.date_value is 'Значение даты';
comment on column ref_book_value.reference_value is 'Значение ссылки';
------------------------------------------------------------------------------------------------------
create table form_column (
  id               number(9)           not null,
  name             varchar2(1000)      not null,
  form_template_id number(9)           not null,
  ord              number(9)           not null,
  alias            varchar2(100)       not null,
  type             char(1)             not null,
  width            number(9)           not null,
  precision        number(9),
  max_length       number(4),
  checking         number(1) default 0 not null,
  attribute_id     number(18),
  format           number(2),
  filter           varchar2(1000),
  parent_column_id number(9),
  attribute_id2    number(18),
  numeration_row   number(9),
  short_name       varchar2(1000)
);
comment on table form_column is 'Описания столбцов налоговых форм';
comment on column form_column.alias is 'Код столбца, используемый в скриптинге';
comment on column form_column.form_template_id is 'Идентификатор шаблона налоговой формы';
comment on column form_column.id is 'Первичный ключ';
comment on column form_column.name is 'Название столбца';
comment on column form_column.ord is 'Порядковый номер';
comment on column form_column.precision is 'Количество знаков после запятой (только для числовых столбцов)';
comment on column form_column.type is 'Тип столбца (S - строка, N – число, D – дата, R - ссылка, A - автонумеруемая графа)';
comment on column form_column.width is 'Ширина (в символах)';
comment on column form_column.checking is 'Признак проверочного столбца';
comment on column form_column.attribute_id is 'Код отображаемого атрибута для столбцов-ссылок';
comment on column form_column.format is 'Формат';
comment on column form_column.filter is 'Условие фильтрации элементов справочника';
comment on column form_column.max_length is 'Максимальная длина строки';
comment on column form_column.parent_column_id is 'Ссылка на родительскую графу';
comment on column form_column.attribute_id2 is 'Код отображаемого атрибута для столбцов-ссылок второго уровня';
comment on column form_column.numeration_row is 'Тип нумерации строк для автонумеруемой графы (0 - последовательная, 1 - сквозная)';
comment on column form_column.short_name is 'Краткое наименование';

create sequence seq_form_column start with 10000;
---------------------------------------------------------------------------------------------------
create table department (
  id         number(9)              not null,
  name       varchar2(510)          not null,
  parent_id  number(9)              null,
  type       number(9)              not null,
  shortname  varchar2(510),
  tb_index   varchar2(3),
  sbrf_code  varchar2(255),
  region_id  number(18),
  is_active  number(1, 0) default 1 not null,
  code       number(15, 0)          not null,
  garant_use number(1) default 0    not null,
  sunr_use   number(1) default 0    not null
);
comment on table department is 'Подразделения банка';
comment on column department.id is 'Идентификатор записи';
comment on column department.name is 'Наименование подразделения';
comment on column department.parent_id is 'Идентификатор родительского подразделения';
comment on column department.type is 'Тип подразделения (1 - Банк, 2- ТБ, 3- ЦСКО, ПЦП, 4- Управление, 5- Не передается в СУДИР)';
comment on column department.shortname is 'Сокращенное наименование подразделения';
comment on column department.tb_index is 'Индекс территориального банка';
comment on column department.sbrf_code is 'Код подразделения в нотации Сбербанка';
comment on column department.region_id is 'Код региона';
comment on column department.is_active is 'Действующее подразделение (0 - не действующее, 1 - действующее)';
comment on column department.code is 'Код подразделения';
comment on column department.garant_use is 'Признак, что используется в модуле Гарантий';
comment on column department.sunr_use is 'Признак, что используется в АС СУНР';

create sequence seq_department start with 1000;
---------------------------------------------------------------------------------------------------
create table report_period (
  id                  number(9)     not null,
  name                varchar2(510) not null,
  tax_period_id       number(9)     not null,
  dict_tax_period_id  number(18)    not null,
  start_date          date          not null,
  end_date            date          not null,
  calendar_start_date date          not null,
  form_type_id        number(1)
);
comment on table report_period is 'Отчетные периоды';
comment on column report_period.id is 'Первичный ключ';
comment on column report_period.name is 'Наименование периода';
comment on column report_period.tax_period_id is 'Налоговый период';
comment on column report_period.dict_tax_period_id is 'Ссылка на справочник отчетных периодов';
comment on column report_period.start_date is 'Дата начала отчетного периода';
comment on column report_period.end_date is 'Дата окончания отчетного периода';
comment on column report_period.calendar_start_date is 'Календарная дата начала отчетного периода';
comment on column report_period.form_type_id is 'Вид отчетности';

create sequence seq_report_period start with 100;
-------------------------------------------------------------------------------------------------------------------------------------------
create table declaration_type (
  id        number(9)           not null,
  name      varchar2(1000)      not null,
  status    number(1) default 0 not null
);
comment on table declaration_type is 'Виды деклараций';
comment on column declaration_type.id is 'Идентификатор (первичный ключ)';
comment on column declaration_type.name is 'Наименование';
comment on column declaration_type.status is 'Статус версии (-1 -удаленная версия, 0 -действующая версия, 1 - черновик версии, 2 - фиктивная версия)';

create sequence seq_declaration_type start with 10000;
-----------------------------------------------------------------------------------------------------------------------------------
create table department_declaration_type (
  id                  number(9) not null,
  department_id       number(9) not null,
  declaration_type_id number(9) not null
);
comment on table department_declaration_type is 'Сведения о налоговых формах, с которыми можно работать в подразделении';
comment on column department_declaration_type.id is 'Идентификатор (первичный ключ)';
comment on column department_declaration_type.department_id is 'Идентификатор подразделения';
comment on column department_declaration_type.declaration_type_id is 'Вид налоговой формы';

create sequence seq_dept_declaration_type start with 10000;
-----------------------------------------------------------------------------------------------------------------------------------
create table declaration_template (
  id                  number(9)           not null,
  status              number(1) default 0 not null,
  version             date                not null,
  name                varchar2(512 char)  not null,
  create_script       clob,
  jrxml               varchar2(36),
  declaration_type_id number(9) not null,
  XSD varchar2(36),
  form_kind number(18),
  form_type number(18)
);
comment on table declaration_template is 'Шаблон налоговой формы';
comment on column declaration_template.id is 'Идентификатор (первичный ключ)';
comment on column declaration_template.version is 'Версия';
comment on column declaration_template.name is 'Наименование версии макета';
comment on column declaration_template.create_script is 'Скрипт формирования налоговой формы';
comment on column declaration_template.jrxml is 'Макет JasperReports для формирования печатного представления формы';
comment on column declaration_template.declaration_type_id is 'Вид налоговой формы';
comment on column declaration_template.XSD is 'XSD-схема';
comment on column declaration_template.status is 'Статус версии (значения (-1, 0, 1, 2))';
comment on column declaration_template.form_kind is 'Тип налоговой формы';
comment on column declaration_template.form_kind is 'Вид налоговой формы';

create sequence seq_declaration_template start with 10000;
-----------------------------------------------------------------------------------------------------------------------------------
CREATE TABLE ref_book_knf_type (
  id   NUMBER(9) NOT NULL,
  name VARCHAR2(2000 CHAR) NOT NULL,
  status NUMBER(1,0) default 0
);
COMMENT ON TABLE ref_book_knf_type IS 'Типы КНФ';
COMMENT ON column ref_book_knf_type.id IS 'Код';
COMMENT ON column ref_book_knf_type.name IS 'Наименование типа КНФ';
-----------------------------------------------------------------------------------------------------------------------------------
create table declaration_data (
  id                          number(18) not null,
  declaration_template_id     number(9) not null,
  knf_type_id                 number(9),
  tax_organ_code              varchar2(4 char),
  kpp                         varchar2(9 char),
  oktmo                       varchar2(11 char),
  department_report_period_id number(18) not null,
  asnu_id                     number(18),
  note                        varchar2(512  char),
  state                       number(1) default 1 not null,
  file_name                   varchar2(255 char),
  doc_state_id                number(18),
  manually_created            number(1) default 0 not null,
  last_data_modified          date,
  adjust_negative_values      number(1) default 0 not null,
  correction_num              number(3),
  tax_refund_reflection_mode  number(1),
  negative_income             number(20,2),
  negative_tax                number(20,2),
  negative_sums_sign          number(1),
  person_id                   number(18),
  signatory                   varchar2(60 char),
  created_date                date default sysdate not null,
  created_by                  number(18) not null
);

comment on table declaration_data is 'Налоговые формы';
comment on column declaration_data.id is 'Идентификатор (первичный ключ)';
comment on column declaration_data.declaration_template_id is 'Ссылка на шаблон налоговой формы';
comment on column declaration_data.knf_type_id IS 'Тип КНФ';
comment on column declaration_data.tax_organ_code is 'Налоговый орган';
comment on column declaration_data.kpp is 'КПП';
comment on column declaration_data.oktmo is 'ОКТМО';
comment on column declaration_data.department_report_period_id is 'Идентификатор отчетного периода подразделения';
comment on column declaration_data.asnu_id is 'Идентификатор АСНУ';
comment on column declaration_data.note is 'Комментарий к НФ, вводимый в модальном окне "Файлы и комментарии"';
comment on column declaration_data.state is 'Состояние формы (1 - Создана, 2 - Подготовлена, 3 - Принята)';
comment on column declaration_data.file_name is 'Имя файла';
comment on column declaration_data.doc_state_id is 'Состояние ЭД';
comment on column declaration_data.manually_created is 'Создана вручную (0-нет, 1-да)';
comment on column declaration_data.tax_refund_reflection_mode is 'Показывать возвращенный налог (1 - "Показывать в строке 090 Раздела 1", 2 - "Учитывать возврат как отрицательное удержание в Разделе 2", 3 - Не учитывать)';
comment on column declaration_data.negative_income is 'Нераспределенный отрицательный Доход';
comment on column declaration_data.negative_tax is 'Нераспределенный отрицательный Налог';
comment on column declaration_data.negative_sums_sign is 'Признак нераспределенных сумм (0 - из текущей формы, 1 - из предыдущей формы)';
comment on column declaration_data.person_id is 'Ид ФЛ из реестра ФЛ';
comment on column declaration_data.signatory is 'ФИО подписанта';
comment on column declaration_data.created_date is 'Дата создания формы';
comment on column declaration_data.created_by is 'Ид пользователя, создавшего форму';

create sequence seq_declaration_data start with 10000;
------------------------------------------------------------------------------------------------------------------------------------------
CREATE TABLE declaration_data_kpp (
  declaration_data_id NUMBER(18) NOT NULL,
  kpp                 VARCHAR2(9 CHAR) NOT NULL,
  CONSTRAINT declaration_data_kpp_pk PRIMARY KEY (declaration_data_id, kpp)
);
COMMENT ON TABLE declaration_data_kpp IS 'Включаемые в КНФ КПП';
COMMENT ON column declaration_data_kpp.declaration_data_id IS 'Ид КНФ';
COMMENT ON column declaration_data_kpp.kpp IS 'КПП';
------------------------------------------------------------------------------------------------------------------------------------------
CREATE TABLE declaration_data_person (
  declaration_data_id NUMBER(18) NOT NULL,
  person_id           NUMBER(18) NOT NULL,
  CONSTRAINT ref_book_knf_type PRIMARY KEY (declaration_data_id, person_id)
);
COMMENT ON TABLE declaration_data_person IS 'Включаемые в КНФ ФЛ';
COMMENT ON column declaration_data_person.declaration_data_id IS 'Ид КНФ';
COMMENT ON column declaration_data_person.person_id IS 'Ид ФЛ';
------------------------------------------------------------------------------------------------------------------------------------------
create table form_data (
  id                          number(18)          not null,
  form_template_id            number(9)           not null,
  state                       number(9)           not null,
  kind                        number(9)           not null,
  return_sign                 number(1)           not null,
  period_order                number(2),
  number_previous_row         number(9),
  department_report_period_id number(18)          not null,
  manual                      number(1) default 0 not null,
  sorted                      number(1) default 0 not null,
  number_current_row          number(9),
  comparative_dep_rep_per_id  number(18),
  accruing                    number(1) default 0 not null,
  sorted_backup               number(1) default 0 not null,
  edited                      number(1) default 0 not null,
  note                        varchar2(512)
);
comment on table form_data is 'Данные по налоговым формам';
comment on column form_data.id is 'Первичный ключ';
comment on column form_data.form_template_id is 'Идентификатор шаблона формы';
comment on column form_data.state is 'Код состояния (1 - Создана, 2 - Подготовлена; 3 - Утверждена; 4 - Принята)';
comment on column form_data.kind is 'Тип налоговой формы (1 - Первичная, 2 - Консолидированная, 3 - Сводная, 4 - Форма УНП, 5 - Выходная)';
comment on column form_data.return_sign is 'Флаг возврата (0 - обычный режим; 1 - форма возвращена из вышестоящего статуса)';
comment on column form_data.period_order is 'Указывает на очередность налоговой формы в рамках налогового периода. Необходимо для, например, месячных форм в рамках квартального отчетного периода';
comment on column form_data.number_previous_row is 'Номер последней строки предыдущей НФ';
comment on column form_data.department_report_period_id is 'Идентификатор отчетного периода подразделения';
comment on column form_data.manual is 'Режим ввода данных (0 - не содержит версию ручного ввода; 1 - содержит)';
comment on column form_data.sorted is 'Признак актуальности сортировки';
comment on column form_data.number_current_row is 'Количество пронумерованных строк текущей НФ';
comment on column form_data.comparative_dep_rep_per_id is 'Период сравнения';
comment on column form_data.accruing is 'Признак расчета значений нарастающим итогом (0 - не нарастающим итогом, 1 - нарастающим итогом)';
comment on column form_data.sorted_backup is 'Статус актуальности сортировки НФ для резервного среза (0 - Сортировка неактуальна; 1 - Сортировка актуальна)';
comment on column form_data.edited is 'Признак изменения данных НФ в режиме редактирования (0 - Нет изменений; 1 - Есть изменения)';
comment on column form_data.note is 'Комментарий к НФ, вводимый в модальном окне "Файлы и комментарии"';

create sequence seq_form_data start with 10000;
---------------------------------------------------------------------------------------------------
create table form_data_signer (
  id           number(18)    not null,
  form_data_id number(18)    not null,
  name         varchar2(200) not null,
  position     varchar2(200) not null,
  ord          number(3)     not null
);
comment on table form_data_signer is 'Подписанты налоговых форм';
comment on column form_data_signer.id is 'Идентфикатор записи (первичный ключ)';
comment on column form_data_signer.form_data_id is 'Идентификатор налоговой формы';
comment on column form_data_signer.name is 'ФИО';
comment on column form_data_signer.position is 'Должность';
comment on column form_data_signer.ord is 'Номер подписанта по порядку';

create sequence seq_form_data_signer start with 10000;
---------------------------------------------------------------------------------------------------
create table form_data_performer (
  form_data_id           number(18) not null,
  name                   varchar2(200),
  phone                  varchar2(40),
  print_department_id    number(9),
  report_department_name varchar2(4000 byte)
);
comment on table form_data_performer is 'Исполнитель налоговой формы';
comment on column form_data_performer.form_data_id is 'Первичный ключ';
comment on column form_data_performer.name is 'ФИО исполнителя';
comment on column form_data_performer.phone is 'Телефон';
comment on column form_data_performer.print_department_id is 'Подразделение, которое печатает налоговую форму';
comment on column form_data_performer.report_department_name is 'Наименование подразделения, которое должно быть использовано в печатной форме';
---------------------------------------------------------------------------------------------------
create table declaration_source (
  department_declaration_type_id number(9) not null,
  src_department_form_type_id    number(9) not null,
  period_start                   date      not null,
  period_end                     date      null
);
comment on table declaration_source is 'Информация о формах-источниках данных для налоговой формы разных видов';
comment on column declaration_source.department_declaration_type_id is 'Идентификатор сочетания вида налоговой формы и подразделения, для которого задаётся источник';
comment on column declaration_source.src_department_form_type_id is 'Идентификатор сочетания типа и вида формы, а также подразделения, которые являются источниками данных для налоговой формы';
comment on column declaration_source.period_start is 'Дата начала действия назначения';
comment on column declaration_source.period_end is 'Дата окончания действия назначения';
----------------------------------------------------------------------------------------------------
create table form_data_source (
  department_form_type_id     number(9) not null,
  src_department_form_type_id number(9) not null,
  period_start                date      not null,
  period_end                  date      null
);
comment on table form_data_source is 'Информация об источниках данных для формирования консолидированных и сводных налоговоых форм';
comment on column form_data_source.department_form_type_id is 'Идентификатор сочетания вида, типа формы и подразделения, для которого задётся источник данных';
comment on column form_data_source.src_department_form_type_id is 'Идентификатор сочетания вида, типа формы и подразделения, которое является источником данных';
comment on column form_data_source.period_start is 'Дата начала действия назначения';
comment on column form_data_source.period_end is 'Дата окончания действия назначения';
------------------------------------------------------------------------------------------------------------------------------------------------------------------
create table sec_user (
  id            number(9)     not null,
  login         varchar2(255) not null,
  name          varchar2(512) not null,
  department_id number(9)     not null,
  is_active     number(1)     not null,
  email         varchar2(128)
);
comment on table sec_user is 'Пользователи системы';
comment on column sec_user.id is 'Первичный ключ';
comment on column sec_user.login is 'Логин пользователя';
comment on column sec_user.name is 'Полное имя пользователя';
comment on column sec_user.department_id is 'Идентификатор подразделения';
comment on column sec_user.is_active is 'Признак активности пользователя (0 - заблокирован; 1 - активен)';
comment on column sec_user.email is 'Адрес электронной почты';

create sequence seq_sec_user start with 10000;
-------------------------------------------------------------------------------------------------------------------------------------
create table sec_role (
  id    number(9)    not null,
  alias varchar2(20) not null,
  name  varchar2(50) not null
);
comment on table sec_role is 'Системные роли';
comment on column sec_role.id is 'Первичный ключ';
comment on column sec_role.alias is 'Код роли (мнемонический идентификатор)';
comment on column sec_role.name is 'Наименование роли';
-------------------------------------------------------------------------------------------------------------------------------------
create table sec_role_ndfl (
  id    number(9)    not null,
  alias varchar2(20) not null,
  name  varchar2(50) not null
);
comment on table sec_role_ndfl is 'Системные роли';
comment on column sec_role_ndfl.id is 'Первичный ключ';
comment on column sec_role_ndfl.alias is 'Код роли (мнемонический идентификатор)';
comment on column sec_role_ndfl.name is 'Наименование роли';
---------------------------------------------------------------------------------------------------
create table sec_user_role (
  user_id number(9) not null,
  role_id number(9) not null
);
comment on table sec_user_role is 'Привязка ролей к пользователям';
comment on column sec_user_role.user_id is 'Идентификатор пользователя';
comment on column sec_user_role.role_id is 'Идентификатор роли';
----------------------------------------------------------------------------------------------------
create table log_business (
  id                   number(18, 0),
  log_date             date           not null,
  event_id             number(3, 0)   not null,
  user_login           varchar2(255)  not null,
  roles                varchar2(2000)  not null,
  declaration_data_id  number(9, 0),
  person_id            number(9, 0),
  log_id               varchar2(36 byte),
  note                 varchar2(4000),
  user_department_name varchar2(4000) not null
);
comment on table log_business is 'Журнал событий налоговых форм';
comment on column log_business.id is 'Код записи';
comment on column log_business.log_date is 'Дата события';
comment on column log_business.event_id is 'Идентификатор события';
comment on column log_business.user_login is 'Логин пользователя';
comment on column log_business.roles is 'Список ролей пользователя';
comment on column log_business.declaration_data_id is 'Идентификатор формы';
comment on column log_business.person_id is 'Идентификатор ФЛ';
comment on column log_business.log_id is 'Ссылка на уведомления';
comment on column log_business.note is 'Текст сообщения';
comment on column log_business.user_department_name is 'Подразделение пользователя';

create sequence seq_log_businessseq_notification start with 10000;;
------------------------------------------------------------------------------------------------------
create table department_report_period (
  id                number(18, 0)       not null,
  department_id     number(9)           not null,
  report_period_id  number(9)           not null,
  is_active         number(1)           not null,
  correction_date   date
);
comment on table department_report_period is 'Привязка отчетных периодов к подразделениям';
comment on column department_report_period.id is 'Идентификатор записи';
comment on column department_report_period.department_id is 'Код подразделения';
comment on column department_report_period.report_period_id is 'Код отчетного периода';
comment on column department_report_period.is_active is 'Признак активности (0 - период закрыт, 1 - период открыт)';
comment on column department_report_period.correction_date is 'Период сдачи корректировки';

create sequence seq_department_report_period start with 1000;
------------------------------------------------------------------------------------------------------
create table notification (
  id                     number(18),
  report_period_id       number(9)              null,
  sender_department_id   number(9)              null,
  receiver_department_id number(9)              null,
  text                   varchar2(2000)         not null,
  create_date            date                   not null,
  deadline               date                   null,
  user_id                number(9)              null,
  role_id                number(9)              null,
  is_read                number(1) default 0    not null,
  type                   number(2, 0) default 0 not null,
  report_id              varchar2(36),
  log_id                 varchar2(36)
);

comment on table notification is 'Оповещения';
comment on column notification.id is 'Уникальный идентификатор оповещения';
comment on column notification.report_period_id is 'идентификатор отчетного периода';
comment on column notification.sender_department_id is 'идентификатор подразделения-отправителя';
comment on column notification.receiver_department_id is 'идентификатор подразделения-получателя';
comment on column notification.text is 'текст оповещения';
comment on column notification.create_date is 'дата создания оповещения';
comment on column notification.deadline is 'дата сдачи отчетности';
comment on column notification.user_id is 'Идентификатор пользователя, который получит оповещение';
comment on column notification.role_id is 'Идентификатор роли пользователя, который получит оповещение';
comment on column notification.is_read is 'Признак прочтения';
comment on column notification.type is 'Тип оповещения (0 - обычное оповещение, 1 - содержит ссылку на отчет справочника)';
comment on column notification.report_id is 'Идентификатор отчета';
comment on column notification.log_id is '';

create sequence seq_notification start with 10000;

--------------------------------------------------------------------------------------------------------

create table template_changes (
  id                      number(9) not null,
  form_template_id        number(9),
  declaration_template_id number(9),
  event                   number(9),
  author                  number(9) not null,
  date_event              date,
  ref_book_id             number(9)
);

comment on table template_changes is 'Изменение версий налоговых шаблонов';
comment on column template_changes.id is 'Уникальный идентификатор записи';
comment on column template_changes.form_template_id is 'Идентификатор налогового шаблона';
comment on column template_changes.declaration_template_id is 'Идентификатор шаблона налоговой формы';
comment on column template_changes.event is 'Событие версии';
comment on column template_changes.author is 'Автор изменения';
comment on column template_changes.date_event is 'Дата изменения';
comment on column template_changes.ref_book_id is 'Идентификатор справочника';
--------------------------------------------------------------------------------------------------------
create table event
(
  id   number(9)     not null,
  name varchar2(510) not null
);

comment on table event is 'Справочник событий в системе';
comment on column event.id is 'Идентификатор события';
comment on column event.name is 'Наименование события';

create sequence seq_template_changes start with 10000;
--------------------------------------------------------------------------------------------------------
create table lock_data
(
  id          number(18) not null,
  key         varchar2(1000)       not null,
  user_id     number(9)            not null,
  task_id     number(18),
  date_lock   date default sysdate not null,
  description varchar2(4000)
);

comment on table lock_data is 'Информация о блокировках';
comment on column lock_data.key is 'Код блокировки';
comment on column lock_data.user_id is 'Идентификатор пользователя, установившего блокировку';
comment on column lock_data.task_id is 'Идентификатор пользователя, установившего блокировку';
comment on column lock_data.date_lock is 'Дата установки блокировки';
comment on column lock_data.description is 'Описание блокировки';


create sequence seq_lock_data start with 100;
--------------------------------------------------------------------------------------------------------
create table department_type
(
  id   number(9) not null,
  name varchar2(50)
);

comment on table department_type is 'Типы подразделений банка';
comment on column department_type.id is 'Идентификатор типа';
comment on column department_type.name is 'Наименование типа';

--------------------------------------------------------------------------------------------------------
create table async_task_type
(
  id                number(18)          not null,
  name              varchar2(300)       not null,
  handler_bean      varchar2(500)       not null,
  short_queue_limit number(18),
  task_limit        number(18),
  limit_kind        varchar2(400)
);

comment on table async_task_type is 'Типы асинхронных задач';
comment on column async_task_type.id is 'Идентификатор строки';
comment on column async_task_type.name is 'Название типа задачи';
comment on column async_task_type.handler_bean is 'Имя spring бина-обработчика задачи';
comment on column async_task_type.task_limit is 'Ограничение на выполнение задачи';
comment on column async_task_type.short_queue_limit is 'Ограничение на выполнение задачи в очереди быстрых задач';
comment on column async_task_type.limit_kind is 'Вид ограничения';

--------------------------------------------------------------------------------------------------------
create table async_task (
  id number(18) not null,
  user_id     number(9) not null,
  type_id number(18) not null,
  create_date timestamp default current_timestamp,
  start_process_date timestamp default null,
  node varchar2(500) default null,
  priority_node varchar2(500) default null,
  queue number(1) not null,
  state number(6) default 1,
  state_date timestamp default current_timestamp,
  serialized_params blob,
  description varchar2(4000)
);

comment on table async_task is 'Асинхронные задачи';
comment on column async_task.id is 'Идентификатор задачи';
comment on column async_task.user_id is 'Идентификатор пользователя, запустившего задачу';
comment on column async_task.type_id is 'Ссылка на тип задачи';
comment on column async_task.create_date is 'Дата создания задачи';
comment on column async_task.start_process_date is 'Дата начала выполнения задачи';
comment on column async_task.node is 'Название узла, на котором выполняется задача';
comment on column async_task.priority_node is 'Узел, которому принудительно будет назначена задача';
comment on column async_task.queue is 'Тип очереди, в которую помещена задача. 1 - короткие, 2 - длинные';
comment on column async_task.state is 'Статус выполнения задачи';
comment on column async_task.state_date is 'Дата последнего изменения статуса';
comment on column async_task.serialized_params is 'Сериализованные параметры, которые нужны для выполнения задачи';
comment on column async_task.description is 'Описание задачи';

create sequence seq_async_task start with 1 increment by 100;
--------------------------------------------------------------------------------------------------------
create table form_data_report
(
  form_data_id number(18)        not null,
  blob_data_id varchar2(36)      not null,
  type         varchar2(50 char) not null,
  checking     number(1)         not null,
  manual       number(1)         not null,
  absolute     number(1)         not null
);

comment on table form_data_report is 'Отчет';
comment on column form_data_report.form_data_id is 'Идентификатор налоговой формы';
comment on column form_data_report.blob_data_id is 'Идентификатор отчета';
comment on column form_data_report.type is 'Тип отчета (Excel/CSV/Специфичный отчет)';
comment on column form_data_report.manual is 'Режим ввода данных (0 - обычная версия; 1 - версия ручного ввода)';
comment on column form_data_report.checking is 'Типы столбцов (0 - только обычные, 1 - вместе с контрольными)';
comment on column form_data_report.absolute is 'Режим вывода данных (0 - только дельты, 1 - абсолютные значения)';

--------------------------------------------------------------------------------------------------------
create table declaration_report
(
  declaration_data_id number(18)   not null,
  blob_data_id        varchar2(36) not null,
  type                number(1)    not null,
  subreport_id        number(9)
);

comment on table declaration_report is 'Отчеты по налоговым формам';
comment on column declaration_report.declaration_data_id is 'Идентификатор налоговой формы';
comment on column declaration_report.blob_data_id is 'Идентификатор отчета';
comment on column declaration_report.type is 'Тип отчета (0 - Excel, 1 - XML, 2 - PDF, 3 - Jasper, 4 - Спец.отчет)';
comment on column declaration_report.subreport_id is 'Идентификатор спец. отчета';

create sequence seq_declaration_subreport start with 100;
--------------------------------------------------------------------------------------------------------
create table declaration_subreport
(
  id                      number(9)      not null,
  declaration_template_id number(9)      not null,
  name                    varchar2(1000) not null,
  ord                     number(9)      not null,
  alias                   varchar2(128)  not null,
  blob_data_id            varchar2(36),
  select_record           number(1) default 0 not null
);

comment on table declaration_subreport is 'Спец. отчеты версии макета налоговой формы';
comment on column declaration_subreport.id is 'Идентификатор отчета';
comment on column declaration_subreport.declaration_template_id is 'Идентификатор шаблона налоговой формы';
comment on column declaration_subreport.name is 'Наименование спец. отчета';
comment on column declaration_subreport.ord is 'Порядковый номер';
comment on column declaration_subreport.alias is 'Код спец. отчета';
comment on column declaration_subreport.blob_data_id is 'Макет JasperReports для формирования печатного представления формы';
comment on column declaration_subreport.select_record is 'Возможность поиска/выбора записи при формировании спец. отчета';
comment on table declaration_subreport is 'Спец. отчеты версии макета налоговой формы';

--------------------------------------------------------------------------------------------------------
create table async_task_subscribers
(
  async_task_id number(18) not null,
  user_id  number(9)           not null
);

comment on table async_task_subscribers is 'Cписок пользователей, ожидающих выполнения операций над объектом блокировки';
comment on column async_task_subscribers.async_task_id is 'Идентификатор задачи, после завершения которой, будет выполнено оповещение';
comment on column async_task_subscribers.user_id is 'Идентификатор пользователя, который получит оповещение';

--------------------------------------------------------------------------------------------------------
create table ifrs_data
(
  report_period_id number(9) not null,
  blob_data_id     varchar2(36)
);

comment on table ifrs_data is 'Отчетность для МСФО';
comment on column ifrs_data.report_period_id is 'Отчетный период';
comment on column ifrs_data.blob_data_id is 'Файл архива с отчетностью для МСФО';
--------------------------------------------------------------------------------------------------------
create table configuration_email
(
  id          number(9)     not null,
  name        varchar2(200) not null,
  value       varchar2(200),
  description varchar2(1000)
);

comment on table configuration_email is 'Настройки почты';
comment on column configuration_email.id is 'Идентификатор записи';
comment on column configuration_email.name is 'Код параметра';
comment on column configuration_email.value is 'Значение параметра';
comment on column configuration_email.description is 'Описание параметра';

--------------------------------------------------------------------------------------------------------
create table form_data_consolidation
(
  source_form_data_id number(9),
  target_form_data_id number(9) not null
);

comment on table form_data_consolidation is 'Сведения о консолидации налоговых форм в налоговые формы';
comment on column form_data_consolidation.source_form_data_id is 'Идентификатор НФ источника';
comment on column form_data_consolidation.target_form_data_id is 'Идентификатор НФ приемника';
--------------------------------------------------------------------------------------------------------

create table declaration_data_consolidation
(
  target_declaration_data_id number(18) not null,
  source_declaration_data_id number(18)
);

comment on table declaration_data_consolidation is 'Сведения о консолидации налоговых форм';
comment on column declaration_data_consolidation.source_declaration_data_id is 'Идентификатор источника';
comment on column declaration_data_consolidation.target_declaration_data_id is 'Идентификатор приемника';

--------------------------------------------------------------------------------------------------------
create table tax_type
(
  id   char(1)       not null,
  name varchar2(256) not null
);

comment on table tax_type is 'Справочник типов налогов';
comment on column tax_type.id is 'Символьный идентификатор типа налога';
comment on column tax_type.name is 'Тип налога';

--------------------------------------------------------------------------------------------------------
create table form_data_ref_book
(
  form_data_id number(18) not null,
  ref_book_id  number(18) not null,
  record_id    number(18) not null
);

comment on table form_data_ref_book is 'Связь экземпляров НФ с элементами справочников';
comment on column form_data_ref_book.form_data_id is 'Идентификатор экземляра налоговой формы';
comment on column form_data_ref_book.ref_book_id is 'Идентификатор справочника';
comment on column form_data_ref_book.record_id is 'Идентификатор записи справочники';

--------------------------------------------------------------------------------------------------------
create sequence seq_form_data_nnn start with 10000;

--------------------------------------------------------------------------------------------------------
create table form_data_file
(
  form_data_id         number(18)     not null,
  blob_data_id         varchar2(36)   not null,
  user_name            varchar2(512)  not null,
  user_department_name varchar2(4000) not null,
  note                 varchar2(512)
);

comment on table form_data_file is 'Файлы налоговой формы';
comment on column form_data_file.form_data_id is 'Идентификатор экземпляра налоговой формы';
comment on column form_data_file.blob_data_id is 'Файл налоговой формы';
comment on column form_data_file.user_name is 'Полное имя пользователя, прикрепившего файл';
comment on column form_data_file.user_department_name is 'Наименование подразделения пользователя, прикрепившего файл';
comment on column form_data_file.note is 'Комментарий к файлу';

--------------------------------------------------------------------------------------------------------
create table form_search_result
(
  "ID"           number(9, 0),
  "SESSION_ID"   number(10, 0),
  "FORM_DATA_ID" number(18, 0),
  "DATE"         date,
  "KEY"          varchar2(4000 byte),
  "ROWS_COUNT"   number(9, 0)
);

comment on column form_search_result."ID" is 'Идентификатор результата поиска';
comment on column form_search_result."SESSION_ID" is 'Идентификатор сессии в которой выполнялся поиск';
comment on column form_search_result."FORM_DATA_ID" is 'Идентификатор формы в которой выполнялся поиск';
comment on column form_search_result."DATE" is 'Дата выполнения поиска';
comment on column form_search_result."KEY" is 'Строка поиска';

create sequence seq_form_search_result start with 1;
--------------------------------------------------------------------------------------------------------
create table FORM_SEARCH_DATA_RESULT
(
  "SESSION_ID"   number(10, 0),
  "ID"           number(9, 0),
  "ROW_INDEX"    number(9, 0),
  "COLUMN_INDEX" number(9, 0),
  "RAW_VALUE"    varchar2(4000 byte),
  "ORD"          number(9, 0)
)
/*PARTITION BY LIST ("SESSION_ID")
(PARTITION "P0"  VALUES (0))*/;

comment on column form_search_data_result."ID" is 'Идентификатор результата поиска';
comment on column form_search_data_result."SESSION_ID" is 'Идентификатор сессии в которой выполнялся поиск';
comment on column form_search_data_result."ROW_INDEX" is 'Номер строки в форме';
comment on column form_search_data_result."COLUMN_INDEX" is 'Номер столбца в форме';
comment on column form_search_data_result."RAW_VALUE" is 'Значение в ячейке формы';
comment on column form_search_data_result."ORD" is 'Порядковый номер';
--------------------------------------------------------------------------------------------------------
create global temporary table form_search_data_result_tmp
(
  "ROW_INDEX"    number(9, 0),
  "COLUMN_INDEX" number(9, 0),
  "RAW_VALUE"    varchar2(4000 byte)
) on commit delete rows;
--------------------------------------------------------------------------------------------------------
create table department_change (
  department_id number(9) not null,
  log_date      date      not null,
  operationType number(9) not null,
  hier_level    number(9),
  name          varchar2(510),
  parent_id     number(9),
  type          number(9),
  shortname     varchar2(510),
  tb_index      varchar2(3),
  sbrf_code     varchar2(255),
  region        varchar2(510),
  is_active     number(1),
  code          number(15),
  garant_use    number(1),
  sunr_use      number(1)
);

comment on table department_change is 'Изменения справочника "Подразделения"';
comment on column department_change.department_id is 'Идентификатор подразделения';
comment on column department_change.log_date is 'Дата/время изменения данных';
comment on column department_change.operationType is 'Тип операции (0 - создание, 1 - изменение, 2 - удаление)';
comment on column department_change.hier_level is 'Уровень записи в иерархии';
comment on column department_change.name is 'Наименование подразделения';
comment on column department_change.parent_id is 'Идентификатор родительского подразделения';
comment on column department_change.type is 'Тип подразделения (1 - Банк, 2 - ТБ, 3 - ЦСКО, ПЦП, 4 - Управление, 5 - Не передается в СУДИР)';
comment on column department_change.shortname is 'Сокращенное наименование подразделения';
comment on column department_change.tb_index is 'Индекс территориального банка';
comment on column department_change.sbrf_code is 'Код подразделения в нотации Сбербанка';
comment on column department_change.region is 'Регион';
comment on column department_change.is_active is 'Действующее подразделение (0 - не действующее, 1 - действующее)';
comment on column department_change.code is 'Код подразделения';
comment on column department_change.garant_use is 'Признак, что используется в модуле Гарантий (0 - не используется, 1 - используется)';
comment on column department_change.sunr_use is 'Признак, что используется в АС СУНР (0 - не используется, 1 - используется)';
--------------------------------------------------------------------------------------------------------
--                                      ФП "НДФЛ"
--------------------------------------------------------------------------------------------------------
create table ndfl_person (
  id                  number(18)        not null,
  declaration_data_id number(18)        not null,
  person_id           number(18),
  row_num             number(10),
  inp                 varchar2(25 char),
  snils               varchar2(14 char),
  last_name           varchar2(36 char),
  first_name          varchar2(36 char),
  middle_name         varchar2(36 char),
  birth_day           date,
  citizenship         varchar2(3 char),
  inn_np              varchar2(12 char),
  inn_foreign         varchar2(50 char),
  id_doc_type         varchar2(2 char),
  id_doc_number       varchar2(25 char),
  status              varchar2(1 char),
  post_index          varchar2(6 char),
  region_code         varchar2(2 char),
  area                varchar2(60 char),
  city                varchar2(50 char),
  locality            varchar2(50 char),
  street              varchar2(120 char),
  house               varchar2(20 char),
  building            varchar2(20 char),
  flat                varchar2(10 char),
  country_code        varchar2(10 char),
  address             varchar2(255 char),
  additional_data     varchar2(4000 char),
  modified_date        date,
  modified_by          varchar2(255 char),
  asnu_id             number(18)
);

comment on table ndfl_person is 'Данные о физическом лице - получателе дохода';
comment on column ndfl_person.id is 'Суррогатный ключ';
comment on column ndfl_person.declaration_data_id is 'Идентификатор налоговой формы к которой относятся данные';
comment on column ndfl_person.person_id is 'Идентификатор в справочнике физлиц';
comment on column ndfl_person.person_id is 'Физическое лицо';
comment on column ndfl_person.row_num is '№пп';
comment on column ndfl_person.inp is 'Налогоплательщик.ИНП';
comment on column ndfl_person.snils is 'Налогоплательщик.СНИЛС';
comment on column ndfl_person.last_name is 'Налогоплательщик.Фамилия';
comment on column ndfl_person.first_name is 'Налогоплательщик.Имя';
comment on column ndfl_person.middle_name is 'Налогоплательщик.Отчество';
comment on column ndfl_person.birth_day is 'Налогоплательщик.Дата рождения';
comment on column ndfl_person.citizenship is 'Гражданство (код страны)';
comment on column ndfl_person.inn_np is 'ИНН.В Российской федерации';
comment on column ndfl_person.inn_foreign is 'ИНН.В стране гражданства';
comment on column ndfl_person.id_doc_type is 'Документ удостоверяющий личность.Код';
comment on column ndfl_person.id_doc_number is 'Документ удостоверяющий личность.Номер';
comment on column ndfl_person.status is 'Статус (Код)';
comment on column ndfl_person.post_index is 'Адрес регистрации в Российской Федерации.Индекс';
comment on column ndfl_person.region_code is 'Адрес регистрации в Российской Федерации.Код субъекта';
comment on column ndfl_person.area is 'Адрес регистрации в Российской Федерации.Район';
comment on column ndfl_person.city is 'Адрес регистрации в Российской Федерации.Город';
comment on column ndfl_person.locality is 'Адрес регистрации в Российской Федерации.Населенный пункт';
comment on column ndfl_person.street is 'Адрес регистрации в Российской Федерации.Улица';
comment on column ndfl_person.house is 'Адрес регистрации в Российской Федерации.Дом';
comment on column ndfl_person.building is 'Адрес регистрации в Российской Федерации.Корпус';
comment on column ndfl_person.flat is 'Адрес регистрации в Российской Федерации.Квартира';
comment on column ndfl_person.country_code is 'Код страны проживания вне РФ';
comment on column ndfl_person.address is 'Адрес проживания вне РФ';
comment on column ndfl_person.additional_data is 'Дополнительная информация';

create sequence seq_ndfl_person start with 1000;
------------------------------------------------------------------------------------------------------
create table ndfl_person_income
(
  id                    number(18) not null,
  ndfl_person_id        number(18) not null,
  source_id             number(18),
  row_num               number(20),
  operation_id          varchar2(100 char),
  income_code           varchar2(4 char),
  income_type           varchar2(2 char),
  oktmo                 varchar2(11 char),
  kpp                   varchar2(9 char),
  income_accrued_date   date,
  income_payout_date    date,
  income_accrued_summ   number(22, 2),
  income_payout_summ    number(22, 2),
  total_deductions_summ number(22, 2),
  tax_base              number(22, 2),
  tax_rate              number(2),
  tax_date              date,
  calculated_tax        number(20),
  withholding_tax       number(20),
  not_holding_tax       number(20),
  overholding_tax       number(20),
  refound_tax           number(15),
  tax_transfer_date     date,
  payment_date          date,
  payment_number        varchar2(20 char),
  tax_summ              number(10),
  modified_date          date,
  modified_by            varchar2(255 char),
  asnu_id               number(18),
  operation_date        date,
  action_date           date,
  row_type              number(3,0),
  oper_info_id          number(20)
);


comment on table ndfl_person_income is 'Сведения о доходах физического лица';
comment on column ndfl_person_income.source_id is 'Cсылка на запись которая является источником при формирование консолидированной формы';
comment on column ndfl_person_income.row_num is '№пп';
comment on column ndfl_person_income.operation_id is 'ID операции';
comment on column ndfl_person_income.income_code is 'Доход.Вид.Код';
comment on column ndfl_person_income.income_type is 'Доход.Вид.Признак';
comment on column ndfl_person_income.oktmo is 'Доход.Источник выплаты.ОКТМО';
comment on column ndfl_person_income.kpp is 'Доход.Источник выплаты.КПП';
comment on column ndfl_person_income.income_accrued_date is 'Доход.Дата.Начисление';
comment on column ndfl_person_income.income_payout_date is 'Доход.Дата.Выплата';
comment on column ndfl_person_income.income_accrued_summ is 'Доход.Сумма.Начисление';
comment on column ndfl_person_income.income_payout_summ is 'Доход.Сумма.Выплата';
comment on column ndfl_person_income.total_deductions_summ is 'Сумма вычета';
comment on column ndfl_person_income.tax_base is 'Налоговая база';
comment on column ndfl_person_income.tax_rate is 'НДФЛ.Процентная ставка';
comment on column ndfl_person_income.tax_date is 'НДФЛ.Расчет.Дата';
comment on column ndfl_person_income.calculated_tax is 'НДФЛ.Расчет.Сумма.Исчисленный';
comment on column ndfl_person_income.withholding_tax is 'НДФЛ.Расчет.Сумма.Удержанный';
comment on column ndfl_person_income.not_holding_tax is 'НДФЛ.Расчет.Сумма.Не удержанный';
comment on column ndfl_person_income.overholding_tax is 'НДФЛ.Расчет.Сумма.Излишне удержанный';
comment on column ndfl_person_income.refound_tax is 'НДФЛ.Расчет.Сумма.Возвращенный налогоплательщику';
comment on column ndfl_person_income.tax_transfer_date is 'НДФЛ.Перечисление в бюджет.Срок';
comment on column ndfl_person_income.payment_date is 'НДФЛ.Перечисление в бюджет.Платежное поручение.Дата';
comment on column ndfl_person_income.payment_number is 'НДФЛ.Перечисление в бюджет.Платежное поручение.Номер';
comment on column ndfl_person_income.tax_summ is 'НДФЛ.Перечисление в бюджет.Платежное поручение.Сумма';

create sequence seq_ndfl_person_income start with 1000;
create sequence seq_oper_info start with 1000;
------------------------------------------------------------------------------------------------------
create global temporary table TMP_OPERATION_ID (
  operation_id varchar2 (100 char)
) on commit delete rows
------------------------------------------------------------------------------------------------------
create table ndfl_person_deduction
(
  id               number(18)        not null,
  ndfl_person_id   number(18)        not null,
  source_id        number(18),
  row_num          number(20),
  operation_id     varchar2(100 char),
  type_code        varchar2(3 char),
  notif_type       varchar2(2 char),
  notif_date       date,
  notif_num        varchar2(20 char),
  notif_source     varchar2(20 char),
  notif_summ       number(22, 2),
  income_accrued   date,
  income_code      varchar2(4 char),
  income_summ      number(22, 2),
  period_prev_date date,
  period_prev_summ number(22, 2),
  period_curr_date date,
  period_curr_summ number(22, 2),
  modified_date     date,
  modified_by       varchar2(255 char),
  asnu_id           number(18),
  oper_info_id      number(20),
  oktmo             varchar2(11 char),
  kpp               varchar2(9 char)
);

comment on table ndfl_person_deduction is 'Стандартные, социальные и имущественные налоговые вычеты';

comment on column ndfl_person_deduction.source_id is 'Cсылка на запись которая является источником при формирование консолидированной формы';

comment on column ndfl_person_deduction.row_num is '№пп';
comment on column ndfl_person_deduction.operation_id is 'Начисленный доход.ID операции';
comment on column ndfl_person_deduction.type_code is 'Код вычета';
comment on column ndfl_person_deduction.notif_type is 'Документ о праве на налоговый вычет.Тип';
comment on column ndfl_person_deduction.notif_date is 'Документ о праве на налоговый вычет.Дата';
comment on column ndfl_person_deduction.notif_num is 'Документ о праве на налоговый вычет.Номер';
comment on column ndfl_person_deduction.notif_source is 'Документ о праве на налоговый вычет.Код источника';
comment on column ndfl_person_deduction.notif_summ is 'Документ о праве на налоговый вычет.Сумма';
comment on column ndfl_person_deduction.income_accrued is 'Начисленный доход.Дата';
comment on column ndfl_person_deduction.income_code is 'Начисленный доход.Код дохода';
comment on column ndfl_person_deduction.income_summ is 'Начисленный доход.Сумма';
comment on column ndfl_person_deduction.period_prev_date is 'Применение вычета.Предыдущий период.Дата';
comment on column ndfl_person_deduction.period_prev_summ is 'Применение вычета.Предыдущий период.Сумма';
comment on column ndfl_person_deduction.period_curr_date is 'Применение вычета.Текущий период.Дата';
comment on column ndfl_person_deduction.period_curr_summ is 'Применение вычета.Текущий период.Сумма';

create sequence seq_ndfl_person_deduction start with 1000;
------------------------------------------------------------------------------------------------------
create table ndfl_person_prepayment
(
  id             number(18)        not null,
  ndfl_person_id number(18)        not null,
  source_id      number(18),
  row_num        number(20),
  operation_id   varchar2(100 char),
  summ           number(20),
  notif_num      varchar2(20 char),
  notif_date     date,
  notif_source   varchar2(20 char),
  modified_date  date,
  modified_by    varchar2(255 char),
  asnu_id        number(18),
  oper_info_id   number(20),
  oktmo          varchar2(11 char),
  kpp            varchar2(9 char)
);

comment on table ndfl_person_prepayment is 'Cведения о доходах в виде авансовых платежей';
comment on column ndfl_person_prepayment.ndfl_person_id is 'Идентификатор в справочнике физлиц';
comment on column ndfl_person_prepayment.source_id is 'Cсылка на запись которая является источником при формирование консолидированной формы';
comment on column ndfl_person_prepayment.row_num is '№пп';
comment on column ndfl_person_prepayment.operation_id is 'ID операции';
comment on column ndfl_person_prepayment.summ is 'Сумма фиксированного авансового платежа';
comment on column ndfl_person_prepayment.notif_num is 'Уведомление, подтверждающее право на уменьшение налога на фиксированные авансовые платежи.Номер уведомления';
comment on column ndfl_person_prepayment.notif_date is 'Уведомление, подтверждающее право на уменьшение налога на фиксированные авансовые платежи.Дата выдачи уведомления';
comment on column ndfl_person_prepayment.notif_source is 'Уведомление, подтверждающее право на уменьшение налога на фиксированные авансовые платежи.Код налогового органа, выдавшего уведомление';

create sequence seq_ndfl_person_prepayment start with 1000;
------------------------------------------------------------------------------------------------------
create table ndfl_references
(
  id number(18) not null,
  record_id number(18) not null,
  version date not null,
  status number(1) not null,
  declaration_data_id number(18)not null,
  person_id number(18) not null,
  num number(10) not null,
  surname varchar2(60 char) not null,
  name varchar2(60 char) not null,
  lastname varchar2(60 char),
  birthday  date not null,
  errtext varchar2(4000 char),
  ndfl_person_id number(18)
);

comment on table ndfl_references is 'Реестр справок';
comment on column ndfl_references.id is 'Уникальный идентификатор';
comment on column ndfl_references.record_id is 'Идентификатор строки. Может повторяться у разных версий';
comment on column ndfl_references.version is 'Версия. Дата актуальности записи';
comment on column ndfl_references.status is 'Статус записи (0 - обычная запись, -1 - удаленная, 1 - черновик, 2 - фиктивная)';
comment on column ndfl_references.declaration_data_id is 'Идентификатор налоговой формы к которой относятся данные';
comment on column ndfl_references.person_id is 'Физическое лицо';
comment on column ndfl_references.num is 'Номер справки';
comment on column ndfl_references.surname is 'Фамилия';
comment on column ndfl_references.name is 'Имя';
comment on column ndfl_references.lastname is 'Отчество';
comment on column ndfl_references.birthday is 'Дата рождения ФЛ';
comment on column ndfl_references.errtext is 'Текст ошибки от ФНС';

------------------------------------------------------------------------------------------------------
-- Параметры спец. отчетов деклараций
create table declaration_subreport_params
(
  id                       number(9) not null,
  declaration_subreport_id number(9) not null,
  name                     varchar2(255 char) not null,
  alias                    varchar2(255 char) not null,
  ord                      number(9) not null,
  type                     char(1) not null,
  filter                   varchar2(1000 char),
  attribute_id             number(18),
  required                 number(1) default 0 not null
);
comment on table declaration_subreport_params is 'Параметры спец. отчетов налоговых форм';
comment on column declaration_subreport_params.name is 'Наименование параметра';
comment on column declaration_subreport_params.alias is 'Псевдоним параметры для доступа из скрипта';
comment on column declaration_subreport_params.ord is 'Порядковый номер параметра';
comment on column declaration_subreport_params.type is 'Тип столбца (S - строка, N - число, D - дата, R - ссылка)';
comment on column declaration_subreport_params.filter is 'Условие фильтрации элементов справочника';
comment on column declaration_subreport_params.attribute_id is 'Код отображаемого атрибута для параметров-ссылок';
comment on column declaration_subreport_params.required is 'Признак обязательности параметра (1 - обязательно; 0 - нет)';
------------------------------------------------------------------------------------------------------------------------------
-- Таблицы для справочника ФИАС
--------------------------------------------------------------------------------------------------------------------------
--Типы адресных объектов
create table fias_socrbase
(
  scname    varchar2(10 char),
  socrname  varchar2(50 char) not null,
  kod_t_st  varchar2(4 char) not null,
  lev       number
);
comment on table fias_socrbase  is 'Справочник "Типы адресных объектов"';
comment on column fias_socrbase.scname  is 'Краткое наименование типа объекта';
comment on column fias_socrbase.socrname  is 'Полное наименование типа объекта';
comment on column fias_socrbase.kod_t_st  is 'Ключевое поле';
comment on column fias_socrbase.lev  is 'Уровень адресного объекта';

-- Сведения
create table fias_addrobj
(
    id number(18) not null,
    aoid number(18) not null,
    formalname varchar2(120 char) not null,
    shortname varchar2(10 char),
    regioncode varchar2(2 char) not null,
    livestatus number(1) not null,
    currstatus number(2) not null,
    aolevel number(10) not null,
    postalcode varchar2(6 char),
    parentguid number(18)
);

comment on column fias_addrobj.id is 'Суррогатный ключ';
comment on column fias_addrobj.aoid is 'Глобальный уникальный идентификатор адресного объекта';
comment on column fias_addrobj.formalname is 'Формализованное наименование';
comment on column fias_addrobj.shortname is 'Краткое наименование типа объекта';
comment on column fias_addrobj.regioncode is 'Код региона';
comment on column fias_addrobj.livestatus is 'Статус актуальности адресного объекта ФИАС на текущую дату: 0 – Не актуальный, 1 - Актуальный';
comment on column fias_addrobj.currstatus is 'Статус актуальности КЛАДР 4 (последние две цифры в коде)';
comment on column fias_addrobj.aolevel is 'Уровень адресного объекта';
comment on column fias_addrobj.postalcode is 'Почтовый индекс';
comment on column fias_addrobj.parentguid is 'Идентификатор объекта родительского объекта';

--------------------------------------------------------------------------------------------------------------------------
-- Справочники физических лиц и статусов налогоплательщиков
-- с учетом изменений по задаче SBRFNDFL-132
--------------------------------------------------------------------------------------------------------------------------
create table ref_book_taxpayer_state
(
  id        number(18)          not null,
  record_id number(9)           not null,
  version   date                not null,
  status    number(1) default 0 not null,
  code      varchar2(1 char)    not null,
  name      varchar2(1000 char) not null
);

comment on table ref_book_taxpayer_state is 'Статусы налогоплательщиков';
comment on column ref_book_taxpayer_state.record_id is 'Идентификатор строки справочника. Может повторяться у разных версий';
comment on column ref_book_taxpayer_state.version is 'Версия. Дата актуальности записи';
comment on column ref_book_taxpayer_state.status is 'Статус записи (0 - обычная запись, -1 - удаленная, 1 - черновик, 2 - фиктивная)';
comment on column ref_book_taxpayer_state.id is 'Уникальный идентификатор';
comment on column ref_book_taxpayer_state.code is 'Код';
comment on column ref_book_taxpayer_state.name is 'Наименование';

create table ref_book_person
(
  id             number(18)          not null,
  last_name      varchar2(60 char),
  first_name     varchar2(60 char),
  middle_name    varchar2(60 char),
  inn            varchar2(12 char),
  inn_foreign    varchar2(50 char),
  snils          varchar2(14 char),
  taxpayer_state number(18),
  birth_date     date,
  birth_place    varchar2(255 char),
  citizenship    number(18),
  report_doc     number(18),
  region_code    varchar2(2 char),
  postal_code    varchar2(6 char),
  district       varchar2(50 char),
  city           varchar2(50 char),
  locality       varchar2(50 char),
  street         varchar2(50 char),
  house          varchar2(20 char),
  build          varchar2(20 char),
  appartment     varchar2(20 char),
  country_id     number(18),
  address_foreign       varchar2(255 char),
  vip            number(1) default 0 not null,
  record_id      number(18)          not null,
  start_date     date                not null,
  end_date       date,
  source_id      number(18),
  old_id         number(18)
);

comment on table ref_book_person is 'Физические лица';
comment on column ref_book_person.id is 'Уникальный идентификатор';
comment on column ref_book_person.last_name is 'Фамилия';
comment on column ref_book_person.first_name is 'Имя';
comment on column ref_book_person.middle_name is 'Отчество';
comment on column ref_book_person.inn is 'ИНН в Российской Федерации';
comment on column ref_book_person.inn_foreign is 'ИНН в стране гражданства';
comment on column ref_book_person.snils is 'СНИЛС';
comment on column ref_book_person.taxpayer_state is 'Статус налогоплательщика';
comment on column ref_book_person.birth_date is 'Дата рождения';
comment on column ref_book_person.birth_place is 'Место рождения';
comment on column ref_book_person.citizenship is 'Гражданство';
comment on column ref_book_person.report_doc is 'Ссылка на ДУЛ, который должен включаться в отчетность';
comment on column ref_book_person.region_code is 'Код региона';
comment on column ref_book_person.postal_code is 'Почтовый индекс';
comment on column ref_book_person.district is 'Район';
comment on column ref_book_person.city is 'Город';
comment on column ref_book_person.locality is 'Населенный пункт (село, поселок)';
comment on column ref_book_person.street is 'Улица (проспект, переулок)';
comment on column ref_book_person.house is 'Номер дома (владения)';
comment on column ref_book_person.build is 'Номер корпуса (строения)';
comment on column ref_book_person.appartment is 'Номер квартиры';
comment on column ref_book_person.country_id is 'Страна проживания';
comment on column ref_book_person.address_foreign is 'Адрес за пределами Российской Федерации';
comment on column ref_book_person.vip is 'Признак, показывающий, является ли ФЛ VIP-ом (0 - Не VIP, 1 - VIP)';
comment on column ref_book_person.record_id is 'Идентификатор строки. Может повторяться у разных версий';
comment on column ref_book_person.start_date is 'Дата начала действия';
comment on column ref_book_person.end_date is 'Дата окончания действия';
comment on column ref_book_person.old_id is 'Старый идентификатор ФЛ';
comment on column ref_book_person.source_id is 'Система-источник: ссылка на справочник кодов АС НУ';


create table ref_book_id_doc
(
  id                  number(18)          not null,
  record_id           number(18)          not null,
  version             date                not null,
  status              number(1) default 0 not null,
  person_id           number(18),
  doc_id              number(18),
  doc_number          varchar2(25 char),
  duplicate_record_id number(18)
);

comment on table ref_book_id_doc is 'Документ, удостоверяющий личность';
comment on column ref_book_id_doc.id is 'Уникальный идентификатор';
comment on column ref_book_id_doc.record_id is 'Идентификатор строки. Может повторяться у разных версий';
comment on column ref_book_id_doc.version is 'Версия. Дата актуальности записи';
comment on column ref_book_id_doc.status is 'Статус записи (0 - обычная запись, -1 - удаленная, 1 - черновик, 2 - фиктивная)';
comment on column ref_book_id_doc.person_id is 'Физическое лицо';
comment on column ref_book_id_doc.doc_id is 'Вид документа';
comment on column ref_book_id_doc.doc_number is 'Серия и номер документа';
comment on column ref_book_id_doc.duplicate_record_id is 'Идентификатор ФЛ - дубля, у которого был скопирован ДУЛ при назначении дубля';

create table ref_book_address
(
  id           number(18) not null,
  record_id    number(18) not null,
  version      date       not null,
  status       number(1)  not null,
  address_type number(1)  not null,
  country_id   number(18),
  region_code  varchar2(2 char),
  postal_code  varchar2(6 char),
  district     varchar2(50 char),
  city         varchar2(50 char),
  locality     varchar2(50 char),
  street       varchar2(50 char),
  house        varchar2(20 char),
  build        varchar2(20 char),
  appartment   varchar2(20 char),
  address      varchar2(255 char),
  address_full varchar2(255 char)
);

comment on table ref_book_address is 'Адрес места жительства';
comment on column ref_book_address.id is 'Уникальный идентификатор';
comment on column ref_book_address.record_id is 'Идентификатор строки. Может повторяться у разных версий';
comment on column ref_book_address.version is 'Версия. Дата актуальности записи';
comment on column ref_book_address.status is 'Статус записи (0 - обычная запись, -1 - удаленная, 1 - черновик, 2 - фиктивная)';
comment on column ref_book_address.address_type is 'Тип адреса. Значения: 0 - в РФ 1 - вне РФ';
comment on column ref_book_address.country_id is 'Страна проживания';
comment on column ref_book_address.region_code is 'Код региона';
comment on column ref_book_address.postal_code is 'Почтовый индекс';
comment on column ref_book_address.district is 'Район';
comment on column ref_book_address.city is 'Город';
comment on column ref_book_address.locality is 'Населенный пункт (село, поселок)';
comment on column ref_book_address.street is 'Улица (проспект, переулок)';
comment on column ref_book_address.house is 'Номер дома (владения)';
comment on column ref_book_address.build is 'Номер корпуса (строения)';
comment on column ref_book_address.appartment is 'Номер квартиры';
comment on column ref_book_address.address is 'Адрес';
comment on column ref_book_address.address_full is 'Полный адрес';

create table ref_book_id_tax_payer
(
  id            number(18)          not null,
  record_id     number(18)          not null,
  version       date                not null,
  status        number(1) default 0 not null,
  person_id     number(18)          not null,
  inp           varchar2(25 char)   not null,
  as_nu         number(18)          not null
);
comment on table ref_book_id_tax_payer is 'Идентификатор налогоплательщика';
comment on column ref_book_id_tax_payer.record_id is 'Идентификатор строки. Может повторяться у разных версий';
comment on column ref_book_id_tax_payer.version is 'Версия. Дата актуальности записи';
comment on column ref_book_id_tax_payer.status is 'Статус записи (0 - обычная запись, -1 - удаленная, 1 - черновик, 2 - фиктивная)';
comment on column ref_book_id_tax_payer.id is 'Уникальный идентификатор';
comment on column ref_book_id_tax_payer.person_id is 'Ссылка на физическое лицо';
comment on column ref_book_id_tax_payer.inp is 'Уникальный неизменяемый цифровой идентификатор налогоплательщика';
comment on column ref_book_id_tax_payer.as_nu is 'Ссылка на запись справочника (100) Справочник АСНУ';

--------------------------------------------------------------------------------------------------------------------------
create table declaration_data_file
(
   declaration_data_id  number(18)      not null,
   blob_data_id         varchar2(36)    not null,
   user_name            varchar2(512)   not null,
   user_department_name varchar2(4000)  not null,
   note                 varchar2(512),
   file_type_id         number(18)      not null,
   file_kind            varchar2(100)
);

comment on table declaration_data_file is 'Файлы налоговой формы';
comment on column declaration_data_file.declaration_data_id is 'Идентификатор экземпляра налоговой формы';
comment on column declaration_data_file.blob_data_id is 'Файл налоговой формы';
comment on column declaration_data_file.user_name is 'Полное имя пользователя, прикрепившего файл';
comment on column declaration_data_file.user_department_name is 'Наименование подразделения пользователя, прикрепившего файл';
comment on column declaration_data_file.note is 'Комментарий к файлу';
comment on column declaration_data_file.file_type_id is 'Категория файла';
comment on column declaration_data_file.file_kind is 'Наименование вида файла';
--------------------------------------------------------------------------------------------------------------------------
create table state
(
  id number(1),
  name varchar2(20 char)
);

comment on table state is 'Статус формы';
comment on column state.id is 'Уникальный идентификатор';
comment on column state.name is 'Наименование';
--------------------------------------------------------------------------------------------------------------------------
-- Журналирование действий пользователей
--------------------------------------------------------------------------------------------------------------------------
create table log
(
    id            varchar2(36) not null,
    user_id       number(18),
    creation_date timestamp not null
);

comment on table log is 'Журналы действий пользователей';
comment on column log.id is 'Уникальный идентификатор';
comment on column log.user_id is 'Ссылка на пользователя';
comment on column log.creation_date is 'Дата-время, включая мс';

create sequence seq_log start with 1;


create table log_entry
(
    log_id        varchar2(36)          not null,
    ord           number(9)             not null,
    creation_date timestamp             not null,
    log_level     number(1)             not null,
    message       varchar2(2000 char),
    type          varchar2(255 char),
    object        varchar2(255 char),
    period        varchar2(50 char)
);

comment on table log_entry is 'Сообщения в журнале';
comment on column log_entry.log_id is 'Ссылка на журнал';
comment on column log_entry.ord is 'Порядковый номер сообщения';
comment on column log_entry.creation_date is 'Дата-время, включая мс';
comment on column log_entry.log_level is 'Уровень важности (0 - информация, 1 - предупреждение, 2 - ошибка)';
comment on column log_entry.message is 'Текст сообщения';
comment on column log_entry.type is 'Тип';
comment on column log_entry.object is 'Объект';
comment on column log_entry.object is 'Период';

create sequence seq_log_entry start with 1;
--------------------------------------------------------------------------------------------------------------------------

create table declaration_template_file
(
  declaration_template_id number(18) not null,
  blob_data_id varchar2(36 byte) not null
);
comment on table declaration_template_file is 'Файлы макета налоговой формы';
comment on column declaration_template_file.declaration_template_id is 'Ссылка на макет налоговой формы';
comment on column declaration_template_file.blob_data_id is 'Ссылка на файл в таблице BLOB_DATA';


create table department_decl_type_performer
(
  department_decl_type_id number(9) not null,
  performer_dep_id number(9) not null
);

comment on table department_decl_type_performer is 'Назначения нескольких исполнителей для связки НФ-подразделение';
comment on column department_decl_type_performer.department_decl_type_id is 'Идентификатор связи подразделения с формой';
comment on column department_decl_type_performer.performer_dep_id is 'Исполнитель';
--------------------------------------------------------------------------------------------------------------------------
-- Планировщик задач
--------------------------------------------------------------------------------------------------------------------------
create table configuration_scheduler (
  id        			number(9)           not null,
  task_name 			varchar2(200 char) not null,
  schedule  			varchar2(100 char),
  active    			number(1) 			    not null,
  modification_date   	date          not null,
  last_fire_date 		date
);

comment on table configuration_scheduler is 'Настройки задач планировщика';
comment on column configuration_scheduler.id is 'Идентификатор задачи';
comment on column configuration_scheduler.task_name is 'Название';
comment on column configuration_scheduler.schedule is 'Расписание';
comment on column configuration_scheduler.active is 'Признак активности';
comment on column configuration_scheduler.modification_date is 'Дата редактирования';
comment on column configuration_scheduler.last_fire_date is 'Дата последнего запуска';
--
create table configuration_scheduler_param(
  id        			number(9)           not null,
  param_name 			varchar2(200 char) not null,
  task_id      			number(9)           not null,
  ord        			number(9)           not null,
  type        			number(1)           not null,
  value		  			varchar2(200 char) not null
);

comment on table configuration_scheduler_param is 'Параметры задач планировщика';
comment on column configuration_scheduler_param.id is 'Идентификатор параметра';
comment on column configuration_scheduler_param.task_id is 'Сслыка на задачу планирощика';
comment on column configuration_scheduler_param.ord is 'Порядок следования';
comment on column configuration_scheduler_param.type is 'Тип параметра(1 - Строка, 2 - Целое число, 3 - Число с плавающей запятой)';
comment on column configuration_scheduler_param.value is 'Значение';

create table decl_template_event_script(
	id number(19) not null,
	declaration_template_id number(19) not null,
	event_id number(19) not null,
	script clob not null
);

create sequence seq_decl_template_event_script start with 1 increment by 1;
--------------------------------------------------------------------------------------------------------
CREATE global TEMPORARY TABLE tmp_numbers0 (
  num NUMBER(18,0)
) ON COMMIT DELETE ROWS ;
CREATE global TEMPORARY TABLE tmp_numbers1 (
  num NUMBER(18,0)
) ON COMMIT DELETE ROWS ;
CREATE GLOBAL TEMPORARY TABLE TMP_STRING_PAIRS(
  STRING1 VARCHAR2(4000 BYTE),
  STRING2 VARCHAR2(4000 BYTE)
) ON COMMIT DELETE ROWS;

----------------------------------------------------------------------------------------------------------
--Справочник Тербанки для ФЛ
----------------------------------------------------------------------------------------------------------
create table ref_book_tb_person
(
  id number(18) not null,
  record_id number(18) not null,
  version date not null,
  status number(1) not null,
  guid varchar2(500 char) not null,
  tb_department_id number(18) not null
);
comment on table ref_book_tb_person is 'Справочник Тербанки для ФЛ при первичной загрузке';
comment on column ref_book_tb_person.id is 'Уникальный идентификатор';
comment on column ref_book_tb_person.record_id is 'Идентификатор строки. Может повторяться у разных версий';
comment on column ref_book_tb_person.version is 'Версия. Дата актуальности записи';
comment on column ref_book_tb_person.status is 'Статус записи (0 - обычная запись, -1 - удаленная, 1 - черновик, 2 - фиктивная)';
comment on column ref_book_tb_person.guid is 'Значение GUID';
comment on column ref_book_tb_person.tb_department_id is 'Ссылка на запись Справочник Подразделения';

create table ref_book_person_tb
(
  id number(18) not null,
  record_id number(18) not null,
  version date not null,
  status number(1) not null,
  person_id number(18) not null,
  tb_department_id number(18) not null,
  import_date timestamp default null
);

comment on table ref_book_person_tb is 'Список тербанков назначенных ФЛ';
comment on column ref_book_person_tb.id is 'Уникальный идентификатор';
comment on column ref_book_person_tb.record_id is 'Идентификатор строки. Может повторяться у разных версий';
comment on column ref_book_person_tb.version is 'Версия. Дата актуальности записи';
comment on column ref_book_person_tb.status is 'Статус записи (0 - обычная запись, -1 - удаленная, 1 - черновик, 2 - фиктивная)';
comment on column ref_book_person_tb.person_id is 'Ссылка на запись справочника ФЛ';
comment on column ref_book_person_tb.tb_department_id is 'Ссылка на тербанк';
comment on column ref_book_person_tb.import_date is 'Дата, показывающая, когда в Систему была загружена из даного ТБ последняя РНУ.';

-----------------------------------------------------
CREATE TABLE department_config (
  ID                    NUMBER(18) NOT NULL,
  KPP                   VARCHAR2(9 CHAR) NOT NULL,
  OKTMO_ID              NUMBER(18,0) NOT NULL,
  start_date            DATE NOT NULL,
  end_date              DATE,
  DEPARTMENT_ID         NUMBER(18) NOT NULL,
  TAX_ORGAN_CODE        VARCHAR2(4 CHAR) NOT NULL,
  TAX_ORGAN_CODE_MID    VARCHAR2(4 CHAR),
  present_place_id      NUMBER(18,0) NOT NULL,
  NAME                  VARCHAR2(1000 CHAR),
  PHONE                 VARCHAR2(20 CHAR),
  reorganization_id     NUMBER(18,0),
  REORG_INN             VARCHAR2(12 CHAR),
  REORG_KPP             VARCHAR2(9 CHAR),
  SIGNATORY_ID          NUMBER(18,0) NOT NULL,
  SIGNATORY_SURNAME     VARCHAR2(60 CHAR),
  SIGNATORY_FIRSTNAME   VARCHAR2(60 CHAR),
  SIGNATORY_LASTNAME    VARCHAR2(60 CHAR),
  APPROVE_DOC_NAME      VARCHAR2(120 CHAR),
  APPROVE_ORG_NAME      VARCHAR2(1000 CHAR),
  reorg_successor_kpp   VARCHAR2(9 CHAR),
  reorg_successor_name  VARCHAR2(1000 CHAR),
  related_kpp           VARCHAR2(9 CHAR),
  related_oktmo         VARCHAR2(11 CHAR)
);
COMMENT ON TABLE department_config IS 'Настройки подразделений';
COMMENT ON COLUMN department_config.ID IS 'Уникальный идентификатор';
COMMENT ON COLUMN department_config.KPP IS 'КПП';
COMMENT ON COLUMN department_config.OKTMO_ID IS 'ОКТМО';
COMMENT ON COLUMN department_config.start_date IS 'Дата начала актуальности';
COMMENT ON COLUMN department_config.end_date IS 'Дата окончания актуальности';
COMMENT ON COLUMN department_config.DEPARTMENT_ID IS 'Код обособленного подразделения';
COMMENT ON COLUMN department_config.TAX_ORGAN_CODE IS 'Код налогового органа конечного';
COMMENT ON COLUMN department_config.TAX_ORGAN_CODE_MID IS 'Код налогового органа промежуточного';
COMMENT ON COLUMN department_config.present_place_id IS 'Место, по которому представляется документ.';
COMMENT ON COLUMN department_config.NAME IS 'Наименование для титульного листа';
COMMENT ON COLUMN department_config.PHONE IS 'Номер контактного телефона';
COMMENT ON COLUMN department_config.reorganization_id IS 'Код формы реорганизации и ликвидации';
COMMENT ON COLUMN department_config.REORG_INN IS 'ИНН реорганизованного обособленного подразделения';
COMMENT ON COLUMN department_config.REORG_KPP IS 'КПП реорганизованного обособленного подразделения';
COMMENT ON COLUMN department_config.SIGNATORY_ID IS 'признак лица, подписавшего документ';
COMMENT ON COLUMN department_config.SIGNATORY_SURNAME IS 'Фамилия подписанта';
COMMENT ON COLUMN department_config.SIGNATORY_FIRSTNAME IS 'Имя подписанта';
COMMENT ON COLUMN department_config.SIGNATORY_LASTNAME IS 'Отчество подписанта';
COMMENT ON COLUMN department_config.APPROVE_DOC_NAME IS 'Наименование документа, подтверждающего полномочия';
COMMENT ON COLUMN department_config.APPROVE_ORG_NAME IS 'Наименование организации-представителя налогоплательщика';
COMMENT ON COLUMN department_config.reorg_successor_kpp is 'Код причины постановки организации по месту нахождения организации правопреемника';
COMMENT ON COLUMN department_config.reorg_successor_name is 'Наименование подразделения для титульного листа отчетных форм по реорганизованному подразделению';
COMMENT ON COLUMN department_config.related_kpp is 'Поле для формирования настройки подразделения Учитывать в КПП/ОКТМО';
COMMENT ON COLUMN department_config.related_oktmo is 'Поле для формирования настройки подразделения Учитывать в КПП/ОКТМО';

create sequence seq_department_config start with 10000;

----------------------------------------------------------------------------------------------------------
-- Справочник "Подсистемы АС УН"
----------------------------------------------------------------------------------------------------------
create table vw_subsystem_syn
(
  id         number(19)         not null,
  code       varchar2(30 char)  not null,
  name       varchar2(100 char) not null,
  short_name varchar2(30 char)  not null,
  mq_query   varchar2(50 char)
);

----------------------------------------------------------------------------------------------------------
-- Транспортные сообщения
----------------------------------------------------------------------------------------------------------
create table transport_message
(
  id                    number(18) not null,
  message_uuid          varchar2(36),
  datetime              timestamp  not null,
  type                  number(1)  not null,
  sender_subsystem_id   number(19),
  receiver_subsystem_id number(19) not null,
  content_type          number(2)  not null,
  state                 number(2)  not null,
  body                  varchar2(4000),
  blob_id               varchar2(36),
  source_file_name      varchar2(255),
  initiator_user_id     number(9)  not null,
  explanation           varchar2(4000),
  declaration_id        number(18)
);

comment on table transport_message is 'Транспортные сообщения для обмена между подсистемами АС УН';
comment on column transport_message.id is 'Уникальный идентификатор сообщения';
comment on column transport_message.message_uuid is 'Уникальный идентификатор UUID, указанный в теле xml-сообщения; используется для связывания сообщения и технологической квитанции';
comment on column transport_message.datetime is 'Дата и время сообщения';
comment on column transport_message.type is 'Направление движения сообщения (0 - исходящее, 1 - входящее)';
comment on column transport_message.sender_subsystem_id is 'ID системы-отправителя';
comment on column transport_message.receiver_subsystem_id is 'ID системы-получателя';
comment on column transport_message.content_type is 'Тип данных в теле сообщения';
comment on column transport_message.state is 'Статус обработки сообщения';
comment on column transport_message.body is 'Тело сообщения';
comment on column transport_message.blob_id is 'Файл, который передавался через папку обмена';
comment on column transport_message.source_file_name is 'Имя исходного файла, который отправлялся в ФНС';
comment on column transport_message.initiator_user_id is 'Инициатор создания сообщения (пользователь/система)';
comment on column transport_message.explanation is 'Текст дополнительного пояснения';
comment on column transport_message.declaration_id is 'Ссылка на форму, с которой связано сообщение';

create sequence seq_transport_message start with 1000 increment by 1;
