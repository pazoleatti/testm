create table form_kind (
  id   number(18)    not null,
  name varchar2(100) not null
);
comment on table form_kind is 'Тип налоговой формы';
comment on column form_kind.id is 'Идентификатор записи';
comment on column form_kind.name is 'Наименование';
--------------------------------------------------------------------------------------------------------------
create table ref_book_oktmo (
  id        number(18)     not null,
  code      varchar2(4000) not null,
  name      varchar2(4000) not null,
  parent_id number(18),
  version   date           not null,
  status    number(1)      not null,
  record_id number(9)      not null
);
comment on table ref_book_oktmo is 'ОКТМО';
comment on column ref_book_oktmo.id is 'Идентификатор записи';
comment on column ref_book_oktmo.code is 'Код';
comment on column ref_book_oktmo.name is 'Наименование';
comment on column ref_book_oktmo.parent_id is 'Идентификатор родительской записи';
comment on column ref_book_oktmo.version is 'Версия. Дата актуальности записи';
comment on column ref_book_oktmo.status is 'Статус записи(0-обычная запись, -1-удаленная, 1-черновик, 2-фиктивная)';
comment on column ref_book_oktmo.record_id is 'Идентификатор строки справочника. Может повторяться у разных версий';

create sequence seq_ref_book_oktmo start with 300000 increment by 100;
create sequence seq_ref_book_oktmo_record_id start with 1000000;
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
  is_versioned        number(1) default 1 not null
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
  calendar_start_date date          not null
);
comment on table report_period is 'Отчетные периоды';
comment on column report_period.id is 'Первичный ключ';
comment on column report_period.name is 'Наименование периода';
comment on column report_period.tax_period_id is 'Налоговый период';
comment on column report_period.dict_tax_period_id is 'Ссылка на справочник отчетных периодов';
comment on column report_period.start_date is 'Дата начала отчетного периода';
comment on column report_period.end_date is 'Дата окончания отчетного периода';
comment on column report_period.calendar_start_date is 'Календарная дата начала отчетного периода';

create sequence seq_report_period start with 100;
----------------------------------------------------------------------------------------------------
create table income_101 (
  id                     number(18)         not null,
  account                varchar2(255 char) not null,
  income_debet_remains   number(22, 4),
  income_credit_remains  number(22, 4),
  debet_rate             number(22, 4),
  credit_rate            number(22, 4),
  outcome_debet_remains  number(22, 4),
  outcome_credit_remains number(22, 4),
  account_name           varchar2(255 char),
  account_period_id      number(9)          not null
);
comment on table income_101 is 'Оборотная ведомость (Форма 0409101-СБ)';
comment on column income_101.id is 'Код записи';
comment on column income_101.account is 'Номер счета';
comment on column income_101.income_debet_remains is 'Входящие остатки по дебету';
comment on column income_101.income_credit_remains is 'Входящие остатки по кредиту';
comment on column income_101.debet_rate is 'Обороты по дебету';
comment on column income_101.credit_rate is 'Обороты по кредиту';
comment on column income_101.outcome_debet_remains is 'Исходящие остатки по дебету';
comment on column income_101.outcome_credit_remains is 'Исходящие остатки по кредиту';
comment on column income_101.account_name is 'Название счёта';
comment on column income_101.account_period_id is 'Идентификатор периода и подразделения БО';

create sequence seq_income_101 start with 100;
-------------------------------------------------------------------------------------------------------------------------------------------
create table income_102 (
  id                number(18)        not null,
  opu_code          varchar2(25 char) not null,
  total_sum         number(22, 4),
  item_name         varchar2(255 char),
  account_period_id number(9)         not null
);
comment on table income_102 is 'Отчет о прибылях и убытках (Форма 0409102-СБ)';
comment on column income_102.id is 'Код записи';
comment on column income_102.opu_code is 'Код ОПУ';
comment on column income_102.total_sum is 'Сумма';
comment on column income_102.item_name is 'Наименование статьи';
comment on column income_102.account_period_id is 'Идентификатор периода и подразделения БО';

create sequence seq_income_102 start with 100;
---------------------------------------------------------------------------------------------------
create table declaration_type (
  id        number(9)           not null,
  tax_type  char(1)             not null,
  name      varchar2(1000)      not null,
  status    number(1) default 0 not null,
  is_ifrs   number(1) default 0 not null,
  ifrs_name varchar2(200)
);
comment on table declaration_type is ' Виды деклараций';
comment on column declaration_type.id is 'Идентификатор (первичный ключ)';
comment on column declaration_type.tax_type is 'Вид налога';
comment on column declaration_type.name is 'Наименование';
comment on column declaration_type.status is 'Статус версии (-1 -удаленная версия, 0 -действующая версия, 1 - черновик версии, 2 - фиктивная версия)';
comment on column declaration_type.is_ifrs is 'Отчетность для МСФО" (0 - не отчетность МСФО, 1 - отчетность МСФО)';
comment on column declaration_type.ifrs_name is 'Наименование формы для файла данного макета, включаемого в архив с отчетностью для МСФО';

create sequence seq_declaration_type start with 10000;
-----------------------------------------------------------------------------------------------------------------------------------
create table department_declaration_type (
  id                  number(9) not null,
  department_id       number(9) not null,
  declaration_type_id number(9) not null
);
comment on table department_declaration_type is 'Сведения о декларациях, с которыми можно работать в подразделении';
comment on column department_declaration_type.id is 'Идентификатор (первичный ключ)';
comment on column department_declaration_type.department_id is 'Идентификатор подразделения';
comment on column department_declaration_type.declaration_type_id is 'Вид декларации';

create sequence seq_dept_declaration_type start with 10000;
-----------------------------------------------------------------------------------------------------------------------------------
create table declaration_template (
  id                  number(9)           not null,
  status              number(1) default 0 not null,
  version             date                not null,
  name                varchar2(1000)      not null,
  create_script       clob,
  jrxml               varchar2(36),
  declaration_type_id number(9) not null,
  XSD varchar2(36), 
  form_kind number(18),
  form_type number(18)
);
comment on table declaration_template is 'Шаблоны налоговых деклараций';
comment on column declaration_template.id is 'Идентификатор (первичный ключ)';
comment on column declaration_template.version is 'Версия';
comment on column declaration_template.name is 'Наименование версии макета';
comment on column declaration_template.create_script is 'Скрипт формирования декларации';
comment on column declaration_template.jrxml is 'Макет JasperReports для формирования печатного представления формы';
comment on column declaration_template.declaration_type_id is 'Вид деклараций';
comment on column declaration_template.XSD is 'XSD-схема';
comment on column declaration_template.status is 'Статус версии (значения (-1, 0, 1, 2))';
comment on column declaration_template.form_kind is 'Тип налоговой формы';
comment on column declaration_template.form_kind is 'Вид налоговой формы';

create sequence seq_declaration_template start with 10000;

-----------------------------------------------------------------------------------------------------------------------------------
create table declaration_data (
  id                          number(18)  not null,
  declaration_template_id     number(9)   not null,
  tax_organ_code              varchar2(4),
  kpp                         varchar2(9),
  department_report_period_id number(18)  not null,
  asnu_id                     number(9),
  note                        varchar2(512),
  state                       number(1)   default 1 not null,
  file_name                   varchar2(255 char)
);


comment on table declaration_data is 'Налоговые декларации';
comment on column declaration_data.id is 'Идентификатор (первичный ключ)';
comment on column declaration_data.declaration_template_id is 'Ссылка на шаблон декларации';
comment on column declaration_data.tax_organ_code is 'Налоговый орган';
comment on column declaration_data.kpp is 'КПП';
comment on column declaration_data.department_report_period_id is 'Идентификатор отчетного периода подразделения';
comment on column declaration_data.asnu_id is 'Идентификатор АСНУ';
comment on column declaration_data.note is 'Комментарий к НФ, вводимый в модальном окне "Файлы и комментарии"';
comment on column declaration_data.state is 'Состояние формы';
comment on column declaration_data.file_name is 'Имя файла';

create sequence seq_declaration_data start with 10000;
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
create table department_form_type (
  id            number(9) not null,
  department_id number(9) not null,
  form_type_id  number(9) not null,
  kind          number(9) not null
);
comment on table department_form_type is 'Связь подразделения банка с формой';
comment on column department_form_type.id is 'Первичный ключ';
comment on column department_form_type.department_id is 'Идентификатор подразделения';
comment on column department_form_type.form_type_id is 'Идентификатор вида налоговой формы';
comment on column department_form_type.kind is 'Тип налоговой формы (1-Первичная, 2-Консолидированная, 3-Сводная, 4-Форма УНП, 5-Выходная)';

create sequence seq_department_form_type start with 10000;
---------------------------------------------------------------------------------------------------
create table declaration_source (
  department_declaration_type_id number(9) not null,
  src_department_form_type_id    number(9) not null,
  period_start                   date      not null,
  period_end                     date      null
);
comment on table declaration_source is 'Информация о формах-источниках данных для деклараций разных видов';
comment on column declaration_source.department_declaration_type_id is 'Иденфтикиатор сочетания вида декларации и подразделения, для которого задаётся источник';
comment on column declaration_source.src_department_form_type_id is 'Идентификатор сочетания типа и вида формы, а также подразделения, которые являются источников данных для деклараций';
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
  id                   number(18, 0) primary key,
  log_date             date           not null,
  event_id             number(3, 0)   not null,
  user_login           varchar2(255)  not null,
  roles                varchar2(200)  not null,
  declaration_data_id  number(9, 0),
  form_data_id         number(9, 0),
  note                 varchar2(510),
  user_department_name varchar2(4000) not null
);
comment on table log_business is 'Журнал событий налоговых форм\деклараций';
comment on column log_business.id is 'Код записи';
comment on column log_business.log_date is 'Дата события';
comment on column log_business.event_id is 'Код события (1 - создать,2 - удалить,3 - рассчитать,4 - обобщить,5 - проверить,6 - сохранить,7 - импорт данных,101 - утвердить,102 - вернуть из \утверждена\ в \создана\,103 - принять из \утверждена\,104 - вернуть из \принята\ в \утверждена\,105 - принять из \создана\,106 - вернуть из \принята\ в \создана\,107 - подготовить,108, вернуть из \подготовлена\ в \создана\,109, принять из \подготовлена\,110, вернуть из \принята\ в \подготовлена\,203 - после принять из \утверждена\,204 - после вернуть из \принята\ в \утверждена\,205 - после принять из \создана\,206 - после вернуть из \принята\ в \создана\,207 - после принять из \"подготовлена\,301 - добавить строку,303 - удалить строку,302 - загрузка)';
comment on column log_business.user_login is 'Логин пользователя';
comment on column log_business.roles is 'Список ролей пользователя';
comment on column log_business.declaration_data_id is 'Код декларации';
comment on column log_business.form_data_id is 'Код налоговой формы';
comment on column log_business.note is 'Текст сообщения';
comment on column log_business.user_department_name is 'Подразделение пользователя';

create sequence seq_log_business;
------------------------------------------------------------------------------------------------------
create table audit_form_type
(
  id   number(9, 0)   not null,
  name varchar2(1000) not null
);

comment on table audit_form_type is 'Типы форм для журнала аудита';
comment on column audit_form_type.id is 'Код записи';
comment on column audit_form_type.name is 'Наименование типа';
------------------------------------------------------------------------------------------------------
create table log_system (
  id                    number(18, 0) primary key,
  log_date              date                not null,
  ip                    varchar2(39),
  event_id              number(3, 0)        not null,
  user_login            varchar2(255)       not null,
  roles                 varchar2(200),
  department_name       varchar2(4000 byte),
  report_period_name    varchar2(100),
  form_kind_id          number(9, 0),
  note                  varchar2(4000 byte),
  user_department_name  varchar2(4000 byte),
  declaration_type_name varchar2(1000),
  form_type_name        varchar2(1000),
  form_department_id    number(9),
  blob_data_id          varchar2(36),
  form_type_id          number(9, 0),
  is_error              number(1) default 0 not null,
  audit_form_type_id    number(9, 0),
  server                varchar2(200)
);
comment on table log_system is 'Системный журнал';
comment on column log_system.id is 'Код записи';
comment on column log_system.log_date is 'Дата события';
comment on column log_system.ip is 'IP-адрес пользователя';
comment on column log_system.event_id is 'Код события (1 - Создать,2 - Удалить,3 - Рассчитать,4 - Обобщить,5 - Проверить,6 - Сохранить,7 - Импорт данных,101 - Утвердить,102 - Вернуть из \Утверждена\ в \Создана\,103 - Принять из \Утверждена\,104 - Вернуть из \Принята\ в \Утверждена\,105 - Принять из \Создана\,106 - Вернуть из \Принята\ в \Создана\,107 - Подготовить,108 - Вернуть из \Подготовлена\ в \Создана\,109 - Принять из \Подготовлена\,110 - Вернуть из \Принята\ в \Подготовлена\,203 - После принять из \Утверждена\,204 - После вернуть из \Принята\ в \Утверждена\,205 - После принять из \Создана\,206 - После вернуть из \Принята\ в \Создана\,207 - После принять из \"Подготовлена\,301 - Добавить строку,303 - Удалить строку,302 - Загрузка)';
comment on column log_system.user_login is 'Логин пользователя';
comment on column log_system.roles is 'Список ролей пользователя';
comment on column log_system.department_name is 'Наименование подразделения НФ\декларации';
comment on column log_system.report_period_name is 'Наименование отчетного периода';
comment on column log_system.form_kind_id is 'Код типа налоговой формы (1,2,3,4,5)';
comment on column log_system.note is 'Текст сообщения';
comment on column log_system.user_department_name is 'Наименование подразделения пользователя';
comment on column log_system.declaration_type_name is 'Вид декларации';
comment on column log_system.form_type_name is 'Вид налоговой формы';
comment on column log_system.form_department_id is 'Идентификатор подразделения налоговой формы/декларации';
comment on column log_system.blob_data_id is 'Ссылка на логи';
comment on column log_system.form_type_id is 'Идентификатор вида НФ';
comment on column log_system.is_error is 'Признак ошибки';
comment on column log_system.audit_form_type_id is 'Тип формы';
comment on column log_system.server is 'Сервер';

create sequence seq_log_system start with 10000;
------------------------------------------------------------------------------------------------------
create table department_report_period (
  id                number(18, 0)       not null,
  department_id     number(9)           not null,
  report_period_id  number(9)           not null,
  is_active         number(1)           not null,
  is_balance_period number(1) default 0 not null,
  correction_date   date
);
comment on table department_report_period is 'Привязка отчетных периодов к подразделениям';
comment on column department_report_period.id is 'Идентификатор записи';
comment on column department_report_period.department_id is 'Код подразделения';
comment on column department_report_period.report_period_id is 'Код отчетного периода';
comment on column department_report_period.is_active is 'Признак активности (0 - период закрыт, 1 - период открыт)';
comment on column department_report_period.is_balance_period is 'Признак того, что период является периодом ввода остатков (0 - обычный период, 1 - период ввода остатков)';
comment on column department_report_period.correction_date is 'Период сдачи корректировки';

create sequence seq_department_report_period start with 1000;
------------------------------------------------------------------------------------------------------
create table task_context (
  id                  number(18, 0) primary key,
  task_id             number(18, 0) not null,
  task_name           varchar2(100) not null,
  modification_date   date          not null,
  user_task_jndi      varchar2(500) not null,
  custom_params_exist number(9, 0)  not null,
  serialized_params   blob          null,
  user_id             number(9)     not null
);
comment on table task_context is 'Контекст пользовательских задач планировщика';
comment on column task_context.id is 'Уникальный идентификатор записи';
comment on column task_context.task_id is 'Идентификатор задачи планировщика websphere';
comment on column task_context.task_name is 'Название задачи';
comment on column task_context.user_task_jndi is 'JNDI-имя класса-обработчика задачи';
comment on column task_context.custom_params_exist is 'Признак наличия пользовательских параметров';
comment on column task_context.serialized_params is 'Сериализованные пользователькие параметры';
comment on column task_context.modification_date is 'Дата последнего редактирования задачи';
comment on column task_context.user_id is 'Идентификатор пользователя';

create sequence seq_task_context start with 100;
------------------------------------------------------------------------------------------------------
create table notification (
  id                     number(9) primary key,
  report_period_id       number(9)              null,
  sender_department_id   number(9)              null,
  receiver_department_id number(9)              null,
  text                   varchar2(2000)         not null,
  create_date            date                   not null,
  deadline               date                   null,
  user_id                number(9)              null,
  role_id                number(9)              null,
  is_read                number(1) default 0    not null,
  blob_data_id           varchar2(36),
  type                   number(2, 0) default 0 not null,
  report_id              varchar2(36)
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
comment on column notification.blob_data_id is 'Ссылка на логи';
comment on column notification.type is 'Тип оповещения (0 - обычное оповещение, 1 - содержит ссылку на отчет справочника)';
comment on column notification.report_id is 'Идентификатор отчета';

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
comment on column template_changes.declaration_template_id is 'Идентификатор шаблона декларации';
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
create table role_event (
  event_id number(9) not null,
  role_id  number(9) not null
);
comment on table role_event is 'Настройка прав доступа к событиям журнала аудита по ролям';
comment on column role_event.event_id is 'Идентификатор события';
comment on column role_event.role_id is 'Идентификатор роли';
--------------------------------------------------------------------------------------------------------
create table lock_data
(
  key         varchar2(1000)       not null,
  user_id     number(9)            not null,
  date_lock   date default sysdate not null,
  state       varchar2(500),
  state_date  date,
  description varchar2(4000),
  queue       number(9) default 0  not null,
  server_node varchar2(100)
);

comment on table lock_data is 'Информация о блокировках';
comment on column lock_data.key is 'Код блокировки';
comment on column lock_data.user_id is 'Идентификатор пользователя, установившего блокировку';
comment on column lock_data.date_lock is 'Дата установки блокировки';
comment on column lock_data.state is 'Статус выполнения асинхронной задачи, связанной с блокировкой';
comment on column lock_data.state_date is 'Дата последнего изменения статуса';
comment on column lock_data.description is 'Описание блокировки';
comment on column lock_data.queue is 'Очередь, в которой находится связанная асинхронная задача';
comment on column lock_data.server_node is 'Наименование узла кластера, на котором выполняется связанная асинхронная задача';
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
  handler_jndi      varchar2(500)       not null,
  short_queue_limit number(18),
  task_limit        number(18),
  limit_kind        varchar2(400),
  dev_mode          number(1) default 0 not null
);

comment on table async_task_type is 'Типы асинхронных задач';
comment on column async_task_type.id is 'Идентификатор строки';
comment on column async_task_type.name is 'Название типа задачи';
comment on column async_task_type.handler_jndi is 'JNDI имя класса-обработчика';
comment on column async_task_type.task_limit is 'Ограничение на выполнение задачи';
comment on column async_task_type.short_queue_limit is 'Ограничение на выполнение задачи в очереди быстрых задач';
comment on column async_task_type.limit_kind is 'Вид ограничения';
comment on column async_task_type.dev_mode is 'Признак задачи для dev-мода';

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

comment on table declaration_report is 'Отчеты по декларациям';
comment on column declaration_report.declaration_data_id is 'Идентификатор декларации';
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
  blob_data_id            varchar2(36)
);

comment on table declaration_subreport is 'Спец. отчеты версии макета декларации';
comment on column declaration_subreport.id is 'Идентификатор отчета';
comment on column declaration_subreport.declaration_template_id is 'Идентификатор шаблона декларации';
comment on column declaration_subreport.name is 'Наименование спец. отчета';
comment on column declaration_subreport.ord is 'Порядковый номер';
comment on column declaration_subreport.alias is 'Код спец. отчета';
comment on column declaration_subreport.blob_data_id is 'Макет JasperReports для формирования печатного представления формы';
comment on table declaration_subreport is 'Спец. отчеты версии макета декларации';

--------------------------------------------------------------------------------------------------------
create table lock_data_subscribers
(
  lock_key varchar2(1000 byte) not null,
  user_id  number(9)           not null
);

comment on table lock_data_subscribers is 'Cписок пользователей, ожидающих выполнения операций над объектом блокировки';
comment on column lock_data_subscribers.lock_key is 'Ключ блокировки объекта, после завершения операции над которым, будет выполнено оповещение';
comment on column lock_data_subscribers.user_id is 'Идентификатор пользователя, который получит оповещение';

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
  source_form_data_id        number(9),
  target_declaration_data_id number(9) not null
);

comment on table declaration_data_consolidation is 'Сведения о консолидации налоговых форм в декларации';
comment on column declaration_data_consolidation.source_form_data_id is 'Идентификатор НФ источника';
comment on column declaration_data_consolidation.target_declaration_data_id is 'Идентификатор декларации приемника';

--------------------------------------------------------------------------------------------------------
create table log_system_report
(
  blob_data_id varchar2(36) not null,
  type         number(1)    not null,
  sec_user_id  number(9)
);

comment on table log_system_report is 'Выгрузки журнала аудита';
comment on column log_system_report.blob_data_id is 'Идентификатор таблицы BLOB_DATA, в которой хранятся файлы';
comment on column log_system_report.type is 'Тип выгрузки (0 - архивация журнала аудита, 1 - генерация zip-файла для журнала аудита)';
comment on column log_system_report.sec_user_id is 'Идентификатор пользователя, инициировавшего выгрузку типа 1';

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
create table log_clob_query
(
  id               number(9)                              not null primary key,
  form_template_id number(9),
  sql_mode         varchar2(10),
  text_query       clob,
  log_date         timestamp(6) default current_timestamp not null,
  session_id       number(18) default 0                   not null
);

comment on table log_clob_query is 'Логирование DDL/DML запросов из ХП';
comment on column log_clob_query.id is 'Идентификатор записи (seq_log_query)';
comment on column log_clob_query.form_template_id is 'Идентификатор шаблона';
comment on column log_clob_query.sql_mode is 'DDL/DML';
comment on column log_clob_query.text_query is 'Текст запроса';
comment on column log_clob_query.log_date is 'Дата/время начала обработки запроса';
comment on column log_clob_query.session_id is 'Идентификатор сессии (seq_log_query_session)';

create sequence seq_log_query start with 1;
create sequence seq_log_query_session start with 1;
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
create table color
(
  id   number(3)     not null,
  name varchar2(100) not null,
  r    number(3)     not null,
  g    number(3)     not null,
  b    number(3)     not null,
  hex  varchar2(7)   not null
);

comment on table color is 'Справочник цветов';
comment on column color.id is 'Идентификатор записи';
comment on column color.name is 'Наименование цвета';
comment on column color.r is 'R';
comment on column color.g is 'G';
comment on column color.b is 'B';
comment on column color.hex is 'Hex';
--------------------------------------------------------------------------------------------------------
create table department_form_type_performer
(
  department_form_type_id number(9) not null,
  performer_dep_id        number(9) not null
);

comment on table department_form_type_performer is 'Назначения нескольких исполнителей для связки НФ-подразделение';
comment on column department_form_type_performer.department_form_type_id is 'Идентификатор связи подразделения с формой';
comment on column department_form_type_performer.performer_dep_id is 'Исполнитель';
--------------------------------------------------------------------------------------------------------
create table ref_book_vzl_history
(
  id           number(18) not null,
  jur_person   number(18) not null,
  category     number(18) not null,
  form_data_id number(18) not null,
  change_date  date       not null,
  state        number(9)  not null
);

comment on table ref_book_vzl_history is 'История изменения категории ВЗЛ';
comment on column ref_book_vzl_history.id is 'Идентификатор записи';
comment on column ref_book_vzl_history.jur_person is 'ВЗЛ';
comment on column ref_book_vzl_history.category is 'Категория ВЗЛ';
comment on column ref_book_vzl_history.form_data_id is 'Код формы';
comment on column ref_book_vzl_history.change_date is 'Дата изменения';
comment on column ref_book_vzl_history.state is 'Код состояния';

create sequence seq_ref_book_vzl_history start with 1;
--------------------------------------------------------------------------------------------------------
create table form_search_result
(
  "ID"           number(9, 0) primary key,
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
  inp                 varchar2(30 char),
  snils               varchar2(30 char),
  last_name           varchar2(60 char),
  first_name          varchar2(60 char),
  middle_name         varchar2(60 char),
  birth_day           date,
  citizenship         varchar2(60 char),
  inn_np              varchar2(12 char),
  inn_foreign         varchar2(12 char),
  id_doc_type         varchar2(60 char),
  id_doc_number       varchar2(60 char),
  status              varchar2(60 char),
  post_index          varchar2(6 char),
  region_code         varchar2(30 char),
  area                varchar2(60 char),
  city                varchar2(500 char),
  locality            varchar2(500 char),
  street              varchar2(500 char),
  house               varchar2(10 char),
  building            varchar2(10 char),
  flat                varchar2(10 char),
  country_code        varchar2(10 char),
  address             varchar2(500 char),
  additional_data     varchar2(4000 char)
);

comment on table ndfl_person is 'Данные о физическом лице - получателе дохода';
comment on column ndfl_person.id is 'Суррогатный ключ';
comment on column ndfl_person.declaration_data_id is 'Идентификатор декларации к которой относятся данные';
comment on column ndfl_person.inp is 'Уникальный код клиента';
comment on column ndfl_person.snils is 'Страховой номер индивидуального лицевого счёта';
comment on column ndfl_person.last_name is 'Фамилия';
comment on column ndfl_person.first_name is 'Имя';
comment on column ndfl_person.middle_name is 'Отчество';
comment on column ndfl_person.birth_day is 'Дата рождения';
comment on column ndfl_person.citizenship is 'Гражданство (код страны)';
comment on column ndfl_person.inn_np is 'ИНН  физического лица';
comment on column ndfl_person.inn_foreign is 'ИНН  иностранного гражданина';
comment on column ndfl_person.id_doc_type is 'Код вида документа';
comment on column ndfl_person.id_doc_number is 'Серия и номер документа';
comment on column ndfl_person.status is 'Статус';
comment on column ndfl_person.post_index is 'Индекс';
comment on column ndfl_person.region_code is 'Код Региона';
comment on column ndfl_person.area is 'Район';
comment on column ndfl_person.city is 'Город';
comment on column ndfl_person.locality is 'Населенный пункт';
comment on column ndfl_person.street is 'Улица';
comment on column ndfl_person.house is 'Дом';
comment on column ndfl_person.building is 'Корпус';
comment on column ndfl_person.flat is 'Квартира';
comment on column ndfl_person.country_code is 'Код страны';
comment on column ndfl_person.address is 'Адрес';
comment on column ndfl_person.additional_data is 'Дополнительная информация';

create sequence seq_ndfl_person start with 1000;
------------------------------------------------------------------------------------------------------
create table ndfl_person_income
(
  id                    number(18) not null,
  ndfl_person_id        number(18) not null,
  row_num               number(10),
  income_code           varchar2(100 char),
  income_type           varchar2(100 char),
  operation_id          number(18),
  oktmo                 varchar2(20 char),
  kpp                   varchar2(20 char),
  income_accrued_date   date,
  income_payout_date    date,
  income_accrued_summ   number(20, 2),
  income_payout_summ    number(20, 2),
  total_deductions_summ number(20, 2),
  tax_base              number(20, 2),
  tax_rate              number(2),
  tax_date              date,
  calculated_tax        number(10),
  withholding_tax       number(10),
  not_holding_tax       number(10),
  overholding_tax       number(10),
  refound_tax           number(10),
  tax_transfer_date     date,
  payment_date          date,
  payment_number        varchar2(20 char),
  tax_summ              number(10)
);


comment on table ndfl_person_income is 'Сведения о доходах физического лица';
comment on column ndfl_person_income.row_num is 'Порядковый номер строки';
comment on column ndfl_person_income.income_code is 'Код дохода';
comment on column ndfl_person_income.income_type is 'Признак дохода';
comment on column ndfl_person_income.operation_id is 'Номер операции';
comment on column ndfl_person_income.oktmo is 'ОКТМО';
comment on column ndfl_person_income.kpp is 'КПП';
comment on column ndfl_person_income.income_accrued_date is 'Дата начисления дохода';
comment on column ndfl_person_income.income_payout_date is 'Дата выплаты дохода';
comment on column ndfl_person_income.income_accrued_summ is 'Сумма начисленного дохода';
comment on column ndfl_person_income.income_payout_summ is 'Сумма выплаченного дохода';
comment on column ndfl_person_income.total_deductions_summ is 'Общая сумма вычетов';
comment on column ndfl_person_income.tax_base is 'Налоговая база';
comment on column ndfl_person_income.tax_rate is 'Ставка налога';
comment on column ndfl_person_income.tax_date is 'Дата налога';
comment on column ndfl_person_income.calculated_tax is 'Сумма налога исчисленная';
comment on column ndfl_person_income.withholding_tax is 'Сумма налога удержанная';
comment on column ndfl_person_income.not_holding_tax is 'Сумма налога, не удержанная налоговым агентом';
comment on column ndfl_person_income.overholding_tax is 'Сумма налога, излишне удержанная налоговым агентом';
comment on column ndfl_person_income.refound_tax is 'Сумма возвращенного налога';
comment on column ndfl_person_income.tax_transfer_date is 'Срок перечисления налога';
comment on column ndfl_person_income.payment_date is 'Дата платежного поручения';
comment on column ndfl_person_income.payment_number is 'Номер платежного поручения перечисления налога в бюджет';
comment on column ndfl_person_income.tax_summ is 'Сумма налога перечисленная';

create sequence seq_ndfl_person_income start with 1000;
------------------------------------------------------------------------------------------------------
create table ndfl_person_deduction
(
  id               number(18)        not null,
  ndfl_person_id   number(18)        not null,
  row_num          number(10),
  operation_id     number(18),
  type_code        varchar2(3 char),
  notif_type       varchar2(2 char),
  notif_date       date,
  notif_num        varchar2(20 char),
  notif_source     varchar2(20 char),
  notif_summ       number(20, 2),
  income_accrued   date,
  income_code      varchar2(4 char),
  income_summ      number(20, 2),
  period_prev_date date,
  period_prev_summ number(20, 2),
  period_curr_date date,
  period_curr_summ number(20, 2)
);

comment on table ndfl_person_deduction is 'Стандартные, социальные и имущественные налоговые вычеты';
comment on column ndfl_person_deduction.row_num is 'Порядковый номер строки';
comment on column ndfl_person_deduction.operation_id is 'Номер операции';
comment on column ndfl_person_deduction.type_code is 'Код вычета';

comment on column ndfl_person_deduction.notif_type is 'Тип уведомления (Документа о праве на налоговый вычет)';
comment on column ndfl_person_deduction.notif_date is 'Дата выдачи уведомления';
comment on column ndfl_person_deduction.notif_num is 'Номер уведомления, подтверждающего право на имущественный налоговый вычет';
comment on column ndfl_person_deduction.notif_source is 'Код налогового органа, выдавшего уведомление';
comment on column ndfl_person_deduction.notif_summ is 'Сумма в соответствии с документом на вычет';

comment on column ndfl_person_deduction.income_accrued is 'Дата начисления дохода';
comment on column ndfl_person_deduction.income_code is 'Код дохода';
comment on column ndfl_person_deduction.income_summ is 'Сумма начисленного дохода';

comment on column ndfl_person_deduction.period_prev_date is 'Дата применения вычета в предыдущем периоде';
comment on column ndfl_person_deduction.period_prev_summ is 'Сумма вычета применения вычета в предыдущем периоде';
comment on column ndfl_person_deduction.period_curr_date is 'Дата применения вычета в текущем периоде';
comment on column ndfl_person_deduction.period_curr_summ is 'Сумма вычета применения вычета в текущкм периоде';

create sequence seq_ndfl_person_deduction start with 1000;
------------------------------------------------------------------------------------------------------
create table ndfl_person_prepayment
(
  id             number(18)        not null,
  ndfl_person_id number(18)        not null,
  row_num        number(10),
  operation_id   number(18),
  summ           number(18),
  notif_num      varchar2(20 char),
  notif_date     date,
  notif_source   varchar2(20 char)
);

comment on table ndfl_person_prepayment is 'Cведения о доходах в виде авансовых платежей';
comment on column ndfl_person_prepayment.row_num is 'Порядковый номер строки';
comment on column ndfl_person_prepayment.operation_id is 'Номер операции';
comment on column ndfl_person_prepayment.summ is 'Сумма фиксированного авансового платежа';
comment on column ndfl_person_prepayment.notif_num is 'Номер уведомления, подтверждающего право на имущественный налоговый вычет';
comment on column ndfl_person_prepayment.notif_date is 'Дата выдачи уведомления';
comment on column ndfl_person_prepayment.notif_source is 'Код налогового органа, выдавшего уведомление';

create sequence seq_ndfl_person_prepayment start with 1000;
------------------------------------------------------------------------------------------------------
--  Расчет по страховым взносам 1151111
------------------------------------------------------------------------------------------------------
create table raschsv_svnp_podpisant
(
   id                 NUMBER(18)           not null,
   declaration_data_id NUMBER(18)          not null,
   svnp_okved         VARCHAR2(8 CHAR),
   svnp_tlph          VARCHAR2(20 CHAR),
   svnp_naim_org      VARCHAR2(1000 CHAR),
   svnp_innyl         VARCHAR2(10 CHAR),
   svnp_kpp           VARCHAR2(9 CHAR),
   svnp_sv_reorg_form VARCHAR2(1 CHAR),
   svnp_sv_reorg_innyl VARCHAR2(10 CHAR),
   svnp_sv_reorg_kpp  VARCHAR2(9 CHAR),
   familia            VARCHAR2(60 CHAR),
   imya               VARCHAR2(60 CHAR),
   middle_name        VARCHAR2(60 CHAR),
   podpisant_pr_podp  VARCHAR2(1 CHAR),
   podpisant_naim_doc VARCHAR2(120 CHAR),
   podpisant_naim_org VARCHAR2(1000 CHAR)
);
create sequence seq_raschsv_svnp_podpisant start with 1;
comment on table raschsv_svnp_podpisant is 'Сведения о плательщике страховых взносов и лице, подписавшем документ';
comment on column raschsv_svnp_podpisant.id is 'Идентификатор';
comment on column raschsv_svnp_podpisant.declaration_data_id is 'Идентификатор декларации';
comment on column raschsv_svnp_podpisant.svnp_okved is 'Код вида экономической деятельности по классификатору ОКВЭД2 (ОКВЭД)';
comment on column raschsv_svnp_podpisant.svnp_tlph is 'Номер контактного телефона (Тлф)';
comment on column raschsv_svnp_podpisant.svnp_naim_org is 'Наименование организации, обособленного подразделения (НаимОрг)';
comment on column raschsv_svnp_podpisant.svnp_innyl is 'ИНН организации (ИННЮЛ)';
comment on column raschsv_svnp_podpisant.svnp_kpp is 'КПП (КПП)';
comment on column raschsv_svnp_podpisant.svnp_sv_reorg_form is 'Код формы реорганизации (ликвидация) (ФормРеорг)';
comment on column raschsv_svnp_podpisant.svnp_sv_reorg_innyl is 'ИНН организации (ИННЮЛ)';
comment on column raschsv_svnp_podpisant.svnp_sv_reorg_kpp is 'КПП (КПП)';
comment on column raschsv_svnp_podpisant.familia is 'Фамилия (Фамилия)';
comment on column raschsv_svnp_podpisant.imya is 'Имя (Имя)';
comment on column raschsv_svnp_podpisant.middle_name is 'Отчество (Отчество)';
comment on column raschsv_svnp_podpisant.podpisant_pr_podp is 'Признак лица, подписавшего документ (ПрПодп)';
comment on column raschsv_svnp_podpisant.podpisant_naim_doc is 'Наименование документа, подтверждающего полномочия представителя (НаимДок)';
comment on column raschsv_svnp_podpisant.podpisant_naim_org is 'Наименование организации - представителя плательщика (НаимОрг)';
------------------------------------------------------------------------------------------------------
create table raschsv_kol_lic_tip
(
   id                 NUMBER(18)           not null,
   kol_vsego_per      NUMBER(7),
   kol_vsego_posl_3m  NUMBER(7),
   kol_1m_posl_3m     NUMBER(7),
   kol_2m_posl_3m     NUMBER(7),
   kol_3m_posl_3m     NUMBER(7)
);
create sequence seq_raschsv_kol_lic_tip start with 1;
comment on table raschsv_kol_lic_tip is 'Сведения по количеству физических лиц (КолЛицТип)';
comment on column raschsv_kol_lic_tip.id is 'Идентификатор';
comment on column raschsv_kol_lic_tip.kol_vsego_per is 'Всего с начала расчетного периода (КолВсегоПер)';
comment on column raschsv_kol_lic_tip.kol_vsego_posl_3m is 'В том числе за последние три месяца расчетного (отчетного) периода, всего (КолВсегоПосл3М)';
comment on column raschsv_kol_lic_tip.kol_1m_posl_3m is 'В том числе 1 месяц из последних трех месяцев расчетного (отчетного) периода (Кол1Посл3М)';
comment on column raschsv_kol_lic_tip.kol_2m_posl_3m is 'В том числе 2 месяц из последних трех месяцев расчетного (отчетного) периода (Кол2Посл3М)';
comment on column raschsv_kol_lic_tip.kol_3m_posl_3m is 'В том числе 3 месяц из последних трех месяцев расчетного (отчетного) периода (Кол3Посл3М)';
------------------------------------------------------------------------------------------------------
create table raschsv_sv_sum_1tip
(
   id                 NUMBER(18)           not null,
   sum_vsego_per      NUMBER(17,2),
   sum_vsego_posl_3m  NUMBER(17,2),
   sum_1m_posl_3m     NUMBER(17,2),
   sum_2m_posl_3m     NUMBER(17,2),
   sum_3m_posl_3m     NUMBER(17,2)
);
create sequence seq_raschsv_sv_sum_1tip start with 1;
comment on table raschsv_sv_sum_1tip is 'Сведения по суммам (тип 1) (СвСум1Тип)';
comment on column raschsv_sv_sum_1tip.id is 'Идентификатор';
comment on column raschsv_sv_sum_1tip.sum_vsego_per is 'Всего с начала расчетного периода (СумВсегоПер)';
comment on column raschsv_sv_sum_1tip.sum_vsego_posl_3m is 'Всего с начала расчетного периода (СумВсегоПосл3М)';
comment on column raschsv_sv_sum_1tip.sum_1m_posl_3m is 'В том числе 1 месяц из последних трех месяцев расчетного (отчетного) периода (Сум1Посл3М)';
comment on column raschsv_sv_sum_1tip.sum_2m_posl_3m is 'В том числе 2 месяц из последних трех месяцев расчетного (отчетного) периода (Сум2Посл3М)';
comment on column raschsv_sv_sum_1tip.sum_3m_posl_3m is 'В том числе 3 месяц из последних трех месяцев расчетного (отчетного) периода (Сум3Посл3М)';
------------------------------------------------------------------------------------------------------
create table raschsv_obyaz_plat_sv
(
   id                 NUMBER(18)           not null,
   declaration_data_id NUMBER(18)           not null,
   oktmo              VARCHAR2(11 CHAR)
);
create sequence seq_raschsv_obyaz_plat_sv start with 1;
comment on table raschsv_obyaz_plat_sv is 'Сводные данные об обязательствах плательщика страховых взносов (ОбязПлатСВ)';
comment on column raschsv_obyaz_plat_sv.id is 'Идентификатор';
comment on column raschsv_obyaz_plat_sv.declaration_data_id is 'Идентификатор декларации';
comment on column raschsv_obyaz_plat_sv.oktmo is 'Код по ОКТМО (ОКТМО)';
------------------------------------------------------------------------------------------------------
create table raschsv_upl_per
(
   id                 NUMBER(18)           not null,
   raschsv_obyaz_plat_sv_id NUMBER(18)           not null,
   node_name          VARCHAR2(20 CHAR)    not null,
   kbk                VARCHAR2(20 CHAR),
   sum_sb_upl_per     NUMBER(17, 2),
   sum_sb_upl_1m      NUMBER(17, 2),
   sum_sb_upl_2m      NUMBER(17, 2),
   sum_sb_upl_3m      NUMBER(17, 2)
);
create sequence seq_raschsv_upl_per start with 1;
comment on table raschsv_upl_per is 'Суммы страховых взносов, подлежащие уплате за расчетный (отчетный) период (УплПерОПС, УплПерОМС, УплПерОПСДоп, УплПерДСО)';
comment on column raschsv_upl_per.id is 'Идентификатор';
comment on column raschsv_upl_per.raschsv_obyaz_plat_sv_id is 'Внешний ключ на Сводные данные об обязательствах плательщика страховых взносов';
comment on column raschsv_upl_per.node_name is 'Имя узла';
comment on column raschsv_upl_per.kbk is 'Код бюджетной классификации (КБК)';
comment on column raschsv_upl_per.sum_sb_upl_per is 'Сумма страховых взносов, подлежащая уплате за расчетный (отчетный) период (СумСВУплПер)';
comment on column raschsv_upl_per.sum_sb_upl_1m is 'Сумма страховых взносов, подлежащая уплате за первый из последних трех месяцев расчетного (отчетного) периода (СумСВУпл1М)';
comment on column raschsv_upl_per.sum_sb_upl_2m is 'Сумма страховых взносов, подлежащая уплате за второй из последних трех месяцев расчетного (отчетного) периода (СумСВУпл2М)';
comment on column raschsv_upl_per.sum_sb_upl_3m is 'Сумма страховых взносов, подлежащая уплате за третий из последних трех месяцев расчетного (отчетного) периода (СумСВУпл3М)';
------------------------------------------------------------------------------------------------------
create table raschsv_upl_prev_oss
(
   id                 NUMBER(18)           not null,
   raschsv_obyaz_plat_sv_id NUMBER(18)           not null,
   kbk                VARCHAR2(20 CHAR),
   sum_sb_upl_per     NUMBER(17, 2),
   sum_sb_upl_1m      NUMBER(17, 2),
   sum_sb_upl_2m      NUMBER(17, 2),
   sum_sb_upl_3m      NUMBER(17, 2),
   prev_rash_sv_per   NUMBER(17, 2),
   prev_rash_sv_1m    NUMBER(17, 2),
   prev_rash_sv_2m    NUMBER(17, 2),
   prev_rash_sv_3m    NUMBER(17, 2)
);
create sequence seq_raschsv_upl_prev_oss start with 1;
comment on table raschsv_upl_prev_oss is 'Сумма страховых взносов на обязательное социальное страхование на случай временной нетрудоспособности и в связи с материнством, подлежащая уплате за расчетный и Сумма превышения произведенных плательщиком расходов на выплату страхового обеспечения (УплПерОСС, ПревРасхОСС)';
comment on column raschsv_upl_prev_oss.id is 'Идентификатор';
comment on column raschsv_upl_prev_oss.raschsv_obyaz_plat_sv_id is 'Внешний ключ на Сводные данные об обязательствах плательщика страховых взносов';
comment on column raschsv_upl_prev_oss.kbk is 'Код бюджетной классификации (КБК)';
comment on column raschsv_upl_prev_oss.sum_sb_upl_per is 'Сумма страховых взносов, подлежащая уплате за расчетный (отчетный) период (СумСВУплПер)';
comment on column raschsv_upl_prev_oss.sum_sb_upl_1m is 'Сумма страховых взносов, подлежащая уплате за расчетный (отчетный) период, в том числе за первый из последних трех месяцев расчетного (отчетного) периода (СумСВУпл1М)';
comment on column raschsv_upl_prev_oss.sum_sb_upl_2m is 'Сумма страховых взносов, подлежащая уплате за расчетный (отчетный) период, в том числе за второй из последних трех месяцев расчетного (отчетного) периода (СумСВУпл2М)';
comment on column raschsv_upl_prev_oss.sum_sb_upl_3m is 'Сумма страховых взносов, подлежащая уплате за расчетный (отчетный) период, в том числе за третий из последних трех месяцев расчетного (отчетного) периода (СумСВУпл3М)';
comment on column raschsv_upl_prev_oss.prev_rash_sv_per is 'Суммы превышения расходов над исчисленными страховыми взносами за расчетный (отчетный) период (ПревРасхСВПер)';
comment on column raschsv_upl_prev_oss.prev_rash_sv_1m is 'Суммы превышения расходов над исчисленными страховыми взносами за первый из последних трех месяцев расчетного (отчетного) периода (ПревРасхСВ1М)';
comment on column raschsv_upl_prev_oss.prev_rash_sv_2m is 'Суммы превышения расходов над исчисленными страховыми взносами за второй из последних трех месяцев расчетного (отчетного) периода (ПревРасхСВ2М)';
comment on column raschsv_upl_prev_oss.prev_rash_sv_3m is 'Суммы превышения расходов над исчисленными страховыми взносами за третий из последних трех месяцев расчетного (отчетного) периода (ПревРасхСВ3М)';
------------------------------------------------------------------------------------------------------
create table raschsv_sv_ops_oms
(
   id                 NUMBER(18)           not null,
   raschsv_obyaz_plat_sv_id NUMBER(18)           not null,
   tarif_plat         VARCHAR2(2 CHAR)
);
create sequence seq_raschsv_sv_ops_oms start with 1;
comment on table raschsv_sv_ops_oms is 'Расчет сумм страховых взносов на обязательное пенсионное и медицинское страхование (РасчСВ_ОПС_ОМС)';
comment on column raschsv_sv_ops_oms.id is 'Идентификатор';
comment on column raschsv_sv_ops_oms.raschsv_obyaz_plat_sv_id is 'Внешний ключ на Сводные данные об обязательствах плательщика страховых взносов';
comment on column raschsv_sv_ops_oms.tarif_plat is 'Код тарифа плательщика (ТарифПлат)';
------------------------------------------------------------------------------------------------------
create table raschsv_sv_ops_oms_rasch
(
   id                 NUMBER(18)           not null,
   raschsv_sv_ops_oms_id NUMBER(18)           not null,
   node_name          VARCHAR2(20 CHAR)    not null,
   pr_osn_sv_dop      VARCHAR2(1 CHAR),
   kod_osnov          VARCHAR2(1 CHAR),
   osnov_zap          VARCHAR2(1 CHAR),
   klas_usl_trud      VARCHAR2(1 CHAR),
   pr_rasch_sum       VARCHAR2(1 CHAR)
);
create sequence seq_raschsv_sv_ops_oms_rasch start with 1;
comment on table raschsv_sv_ops_oms_rasch is 'Дочерний узел для Расчета сумм страховых взносов на обязательное пенсионное и медицинское страхование';
comment on column raschsv_sv_ops_oms_rasch.id is 'Идентификатор';
comment on column raschsv_sv_ops_oms_rasch.raschsv_sv_ops_oms_id is 'Внешний ключ на Расчет сумм страховых взносов на обязательное пенсионное и медицинское страхование';
comment on column raschsv_sv_ops_oms_rasch.node_name is 'Имя узла';
comment on column raschsv_sv_ops_oms_rasch.pr_osn_sv_dop is 'Признак основания исчисления сумм страховых взносов по дополнительному тарифу (ПрОснСВДоп)';
comment on column raschsv_sv_ops_oms_rasch.kod_osnov is 'Код основания исчисления (КодОснов)';
comment on column raschsv_sv_ops_oms_rasch.osnov_zap is 'Основание заполнения (ОсновЗап)';
comment on column raschsv_sv_ops_oms_rasch.klas_usl_trud is 'Код класса условий труда (КласУслТруд)';
comment on column raschsv_sv_ops_oms_rasch.pr_rasch_sum is 'Код основания исчисления страховых взносов на дополнительное социальное обеспечение (ПрРасчСум)';
------------------------------------------------------------------------------------------------------
create table raschsv_ops_oms_rasch_sum
(
   raschsv_ops_oms_rasch_sum_id NUMBER(18)           not null,
   raschsv_sv_sum1_tip_id NUMBER(18)           not null,
   node_name          VARCHAR2(20 CHAR)    not null
);
comment on table raschsv_ops_oms_rasch_sum is 'Связь записи дочернего узла для Расчета сумм страховых взносов на обязательное пенсионное и медицинское страхование с записью Сведения по суммам (тип 1)';
comment on column raschsv_ops_oms_rasch_sum.raschsv_ops_oms_rasch_sum_id is 'Внешний ключ на Дочерний узел расчета сумм страховых взносов на обязательное пенсионное и медицинское страхование';
comment on column raschsv_ops_oms_rasch_sum.raschsv_sv_sum1_tip_id is 'Внешний ключ на Сведения по суммам (тип 1)';
comment on column raschsv_ops_oms_rasch_sum.node_name is 'Имя узла';
------------------------------------------------------------------------------------------------------
create table raschsv_ops_oms_rasch_kol
(
   raschsv_ops_oms_rasch_kol_id NUMBER(18)           not null,
   raschsv_kol_lic_tip_id NUMBER(18)           not null,
   node_name          VARCHAR2(20 CHAR)    not null
);
comment on table raschsv_ops_oms_rasch_kol is 'Связь записи дочернего узла для Расчета сумм страховых взносов на обязательное пенсионное и медицинское страхование с записью Сведения по количеству физических лиц';
comment on column raschsv_ops_oms_rasch_kol.raschsv_ops_oms_rasch_kol_id is 'Внешний ключ на Дочерний узел расчета сумм страховых взносов на обязательное пенсионное и медицинское страхование';
comment on column raschsv_ops_oms_rasch_kol.raschsv_kol_lic_tip_id is 'Внешний ключ на Сведения по количеству физических лиц';
comment on column raschsv_ops_oms_rasch_kol.node_name is 'Имя узла';
------------------------------------------------------------------------------------------------------
create table raschsv_oss_vnm
(
   id                 NUMBER(18)           not null,
   raschsv_obyaz_plat_sv_id NUMBER(18)           not null,
   priz_vypl          VARCHAR2(1 CHAR)
);
create sequence seq_raschsv_oss_vnm start with 1;
comment on table raschsv_oss_vnm is 'Расчет сумм страховых взносов на обязательное социальное страхование на случай временной нетрудоспособности и в связи с материнством (РасчСВ_ОСС.ВНМ)';
comment on column raschsv_oss_vnm.id is 'Идентификатор';
comment on column raschsv_oss_vnm.raschsv_obyaz_plat_sv_id is 'Внешний ключ на Сводные данные об обязательствах плательщика страховых взносов';
comment on column raschsv_oss_vnm.priz_vypl is 'Признак выплат (ПризВыпл)';
------------------------------------------------------------------------------------------------------
create table raschsv_upl_sv_prev
(
   id                 NUMBER(18)           not null,
   raschsv_oss_vnm_id NUMBER(18)           not null,
   node_name          VARCHAR2(20 CHAR)    not null,
   priznak            VARCHAR2(1 CHAR),
   sv_sum             NUMBER(17, 2)
);
create sequence seq_raschsv_upl_sv_prev start with 1;
comment on table raschsv_upl_sv_prev is 'Сумма страховых взносов, подлежащая уплате (сумма превышения произведенных расходов над исчисленными страховыми взносами) (УплСВПрев)';
comment on column raschsv_upl_sv_prev.id is 'Идентификатор';
comment on column raschsv_upl_sv_prev.raschsv_oss_vnm_id is 'Внешний ключ на Расчет сумм страховых взносов на обязательное социальное страхование на случай временной нетрудоспособности и в связи с материнством';
comment on column raschsv_upl_sv_prev.node_name is 'Имя узла';
comment on column raschsv_upl_sv_prev.priznak is 'Признак (Признак)';
comment on column raschsv_upl_sv_prev.sv_sum is 'Сумма (Сумма)';
------------------------------------------------------------------------------------------------------
create table raschsv_oss_vnm_kol
(
   raschsv_oss_vnm_id NUMBER(18)           not null,
   raschsv_kol_lic_tip_id NUMBER(18)           not null,
   node_name          VARCHAR2(20 CHAR)    not null
);
comment on table raschsv_oss_vnm_kol is 'Связь записи Суммы страховых взносов, подлежащая уплате (сумма превышения произведенных расходов над исчисленными страховыми взносами) с записью Сведения по количеству физических лиц';
comment on column raschsv_oss_vnm_kol.raschsv_oss_vnm_id is 'Внешний ключ на Сумма страховых взносов, подлежащая уплате (сумма превышения произведенных расходов над исчисленными страховыми взносами)';
comment on column raschsv_oss_vnm_kol.raschsv_kol_lic_tip_id is 'Внешний ключ на Сведения по количеству физических лиц';
comment on column raschsv_oss_vnm_kol.node_name is 'Имя узла';
------------------------------------------------------------------------------------------------------
create table raschsv_oss_vnm_sum
(
   raschsv_oss_vnm_id NUMBER(18)           not null,
   raschsv_sv_sum1_tip_id NUMBER(18)           not null,
   node_name          VARCHAR2(20 CHAR)    not null
);
comment on table raschsv_oss_vnm_sum is 'Связь записи Суммы страховых взносов, подлежащая уплате (сумма превышения произведенных расходов над исчисленными страховыми взносами) с записью Сведения по суммам (тип 1)';
comment on column raschsv_oss_vnm_sum.raschsv_oss_vnm_id is 'Внешний ключ на Сумма страховых взносов, подлежащая уплате (сумма превышения произведенных расходов над исчисленными страховыми взносами)';
comment on column raschsv_oss_vnm_sum.raschsv_sv_sum1_tip_id is 'Внешний ключ на Сведения по суммам (тип 1)';
comment on column raschsv_oss_vnm_sum.node_name is 'Имя узла';
------------------------------------------------------------------------------------------------------
create table raschsv_rash_oss_zak
(
   id                 NUMBER(18)           not null,
   raschsv_obyaz_plat_sv_id NUMBER(18)           not null
);
create sequence seq_raschsv_rash_oss_zak start with 1;
comment on table raschsv_rash_oss_zak is 'Расходы по обязательному социальному страхованию на случай временной нетрудоспособности и в связи с материнством и расходы, осуществляемые в соответствии с законодательством Российской Федерации (РасхОССЗак)';
comment on column raschsv_rash_oss_zak.id is 'Идентификатор';
comment on column raschsv_rash_oss_zak.raschsv_obyaz_plat_sv_id is 'Внешний ключ на Сводные данные об обязательствах плательщика страховых взносов';
------------------------------------------------------------------------------------------------------
create table raschsv_rash_oss_zak_rash
(
   id                 NUMBER(18)           not null,
   raschsv_rash_oss_zak_id NUMBER(18)           not null,
   node_name          VARCHAR2(20 CHAR)    not null,
   chisl_sluch        NUMBER(7),
   kol_vypl           NUMBER(7),
   pash_vsego         NUMBER(17,2),
   rash_fin_fb        NUMBER(17,2)
);
create sequence seq_raschsv_rash_oss_zak_rash start with 1;
comment on table raschsv_rash_oss_zak_rash is 'Дочерний узел Расходы по обязательному социальному страхованию на случай временной нетрудоспособности и в связи с материнством и расходы, осуществляемые в соответствии с законодательством Российской Федерации';
comment on column raschsv_rash_oss_zak_rash.id is 'Идентификатор';
comment on column raschsv_rash_oss_zak_rash.node_name is 'Имя узла';
comment on column raschsv_rash_oss_zak_rash.chisl_sluch is 'Число случаев (получателей) (ЧислСлуч)';
comment on column raschsv_rash_oss_zak_rash.kol_vypl is 'Количество дней, выплат, пособий (КолВыпл)';
comment on column raschsv_rash_oss_zak_rash.pash_vsego is 'Расходы, всего (РасхВсего)';
comment on column raschsv_rash_oss_zak_rash.rash_fin_fb is 'Расходы за счет средств, финансируемых из федерального бюджета (РасхФинФБ)';
------------------------------------------------------------------------------------------------------
create table raschsv_vypl_fin_fb
(
   id                 NUMBER(18)           not null,
   raschsv_obyaz_plat_sv_id NUMBER(18)           not null
);
create sequence seq_raschsv_vypl_fin_fb start with 1;
comment on table raschsv_vypl_fin_fb is 'Выплаты, произведенные за счет средств, финансируемых из федерального бюджета (ВыплФинФБ)';
comment on column raschsv_vypl_fin_fb.id is 'Идентификатор';
comment on column raschsv_vypl_fin_fb.raschsv_obyaz_plat_sv_id is 'Внешний ключ на Сводные данные об обязательствах плательщика страховых взносов';
------------------------------------------------------------------------------------------------------
create table raschsv_vypl_prichina
(
   id                 NUMBER(18)           not null,
   raschsv_vypl_fin_fb_id NUMBER(18)           not null,
   node_name          VARCHAR2(20 CHAR)    not null,
   sv_vnf_uhod_inv    NUMBER(17, 2)
);
create sequence seq_raschsv_vypl_prichina start with 1;
comment on table raschsv_vypl_prichina is 'Дочерний узел для Выплаты, произведенные за счет средств, финансируемых из федерального бюджета';
comment on column raschsv_vypl_prichina.id is 'Идентификатор';
comment on column raschsv_vypl_prichina.raschsv_vypl_fin_fb_id is 'Внешний ключ на Выплаты, произведенные за счет средств, финансируемых из федерального бюджета';
comment on column raschsv_vypl_prichina.node_name is 'Имя узла';
comment on column raschsv_vypl_prichina.sv_vnf_uhod_inv is 'Страховые взносы, исчисленные на оплату дополнительных выходных дней для ухода за детьми-инвалидами (СВВнФУходИнв)';
------------------------------------------------------------------------------------------------------
create table raschsv_rash_vypl
(
   id                 NUMBER(18)           not null,
   raschsv_vypl_prichina_id NUMBER(18)           not null,
   node_name          VARCHAR2(20 CHAR)    not null,
   chisl_poluch       NUMBER(7),
   kol_vypl           NUMBER(7),
   rashod             NUMBER(17, 2)
);
create sequence seq_raschsv_rash_vypl start with 1;
comment on table raschsv_rash_vypl is 'Данные по выплате, произведенной за счет средств, финансируемых из федерального бюджета';
comment on column raschsv_rash_vypl.id is 'Идентификатор';
comment on column raschsv_rash_vypl.raschsv_vypl_prichina_id is 'Внешний ключ на Выплаты, произведенные за счет средств, финансируемых из федерального бюджета';
comment on column raschsv_rash_vypl.node_name is 'Имя узла';
comment on column raschsv_rash_vypl.chisl_poluch is 'Число получателей (ЧислПолуч)';
comment on column raschsv_rash_vypl.kol_vypl is 'Количество дней, выплат (КолВыпл)';
comment on column raschsv_rash_vypl.rashod is 'Расходы, руб. (Расход)';
------------------------------------------------------------------------------------------------------
create table raschsv_prav_tarif3_1_427
(
   id                 NUMBER(18)           not null,
   raschsv_obyaz_plat_sv_id NUMBER(18)           not null,
   sr_chisl_9mpr      NUMBER(7),
   sr_chisl_per       NUMBER(7),
   doh248_9mpr        NUMBER(15),
   doh248_per         NUMBER(15),
   doh_kr5_427_9mpr   NUMBER(15),
   doh_kr5_427_per    NUMBER(15),
   doh_doh5_427_9mpr  NUMBER(5, 2),
   doh_doh5_427_per   NUMBER(5, 2),
   data_zap_ak_org    DATE,
   nom_zap_ak_org     VARCHAR2(18 CHAR)
);
create sequence seq_raschsv_prav_tarif3_1_427 start with 1;
comment on table raschsv_prav_tarif3_1_427 is 'Расчет соответствия условиям применения пониженного тарифа страховых взносов плательщиками, указанными в подпункте 3 пункта 1 статьи 427 (ПравТариф3.1.427)';
comment on column raschsv_prav_tarif3_1_427.id is 'Идентификатор';
comment on column raschsv_prav_tarif3_1_427.raschsv_obyaz_plat_sv_id is 'Внешний ключ на Сводные данные об обязательствах плательщика страховых взносов';
comment on column raschsv_prav_tarif3_1_427.sr_chisl_9mpr is 'Средняя численность работников / среднесписочная численность работников (чел.) по итогам 9-ти месяцев года, предшествующего текущему расчетному периоду (СрЧисл_9МПр)';
comment on column raschsv_prav_tarif3_1_427.sr_chisl_per is 'Средняя численность работников / среднесписочная численность работников (чел.) по итогам текущего отчетного (расчетного) периода (СрЧисл_Пер)';
comment on column raschsv_prav_tarif3_1_427.doh248_9mpr is 'Сумма доходов, определяемая в соответствии со статьей 248 Налогового кодекса Российской Федерации, всего (руб.) по итогам 9-ти месяцев года, предшествующего текущему расчетному периоду (Дох248_9МПр)';
comment on column raschsv_prav_tarif3_1_427.doh248_per is 'Сумма доходов, определяемая в соответствии со статьей 248 Налогового кодекса Российской Федерации, всего (руб.) по итогам текущего отчетного (расчетного) периода (Дох248_Пер)';
comment on column raschsv_prav_tarif3_1_427.doh_kr5_427_9mpr is 'Сумма доходов, определяемая исходя из критериев, указанных в пункте 5 статьи 427 Налогового кодекса Российской Федерации (руб.) по итогам 9-ти месяцев года, предшествующего текущему расчетному периоду (ДохКр5.427_9МПр)';
comment on column raschsv_prav_tarif3_1_427.doh_kr5_427_per is 'Сумма доходов, определяемая исходя из критериев, указанных в пункте 5 статьи 427 Налогового кодекса Российской Федерации (руб.) по итогам текущего отчетного (расчетного) периода (ДохКр5.427_Пер)';
comment on column raschsv_prav_tarif3_1_427.doh_doh5_427_9mpr is 'Доля доходов, определяемая в целях применения пункта 5 статьи 427 Налогового кодекса Российской Федерации (%) по итогам 9-ти месяцев года, предшествующего текущему расчетному периоду (ДолДох5.427_9МПр)';
comment on column raschsv_prav_tarif3_1_427.doh_doh5_427_per is 'Доля доходов, определяемая в целях применения пункта 5 статьи 427 Налогового кодекса Российской Федерации (%) по итогам текущего отчетного (расчетного) периода (ДолДох5.427_Пер)';
comment on column raschsv_prav_tarif3_1_427.data_zap_ak_org is 'Дата записи в реестре аккредитованных организаций (ДатаЗапАкОрг)';
comment on column raschsv_prav_tarif3_1_427.nom_zap_ak_org is 'Номер записи в реестре аккредитованных организаций (НомЗапАкОрг)';
------------------------------------------------------------------------------------------------------
create table raschsv_prav_tarif5_1_427
(
   id                 NUMBER(18)           not null,
   raschsv_obyaz_plat_sv_id NUMBER(18)           not null,
   doh346_15vs        NUMBER(15),
   doh6_427           NUMBER(15),
   dol_doh6_427       NUMBER(5, 2)
);
create sequence seq_raschsv_prav_tarif5_1_427 start with 1;
comment on table raschsv_prav_tarif5_1_427 is 'Расчет соответствия условиям применения пониженного тарифа страховых взносов плательщиками, указанными в подпункте 5 пункта 1 статьи 427 (ПравТариф5.1.427)';
comment on column raschsv_prav_tarif5_1_427.id is 'Идентификатор';
comment on column raschsv_prav_tarif5_1_427.raschsv_obyaz_plat_sv_id is 'Внешний ключ на Сводные данные об обязательствах плательщика страховых взносов';
comment on column raschsv_prav_tarif5_1_427.doh346_15vs is 'Сумма доходов, определяемая в соответствии со статьей 346.15 Налогового кодекса Российской Федерации, всего (руб.) (Дох346.15Вс)';
comment on column raschsv_prav_tarif5_1_427.doh6_427 is 'Сумма доходов, определяемая в целях применения пункта 6 статьи 427 Налогового кодекса Российской Федерации (руб.) (Дох6.427)';
comment on column raschsv_prav_tarif5_1_427.dol_doh6_427 is 'Доля доходов, определяемая в целях применения пункта 6 статьи 427 Налогового кодекса Российской Федерации (%) (ДолДох6.427)';
------------------------------------------------------------------------------------------------------
create table raschsv_prav_tarif7_1_427
(
   id                 NUMBER(18)           not null,
   raschsv_obyaz_plat_sv_id NUMBER(18)           not null,
   doh_vs_pred        NUMBER(15),
   doh_vs_per         NUMBER(15),
   doh_cel_post_pred  NUMBER(15),
   doh_cel_post_per   NUMBER(15),
   doh_grant_pred     NUMBER(15),
   doh_grant_per      NUMBER(15),
   doh_ek_deyat_pred  NUMBER(15),
   doh_ek_deyat_per   NUMBER(15),
   dol_doh_pred       NUMBER(5, 2),
   dol_doh_per        NUMBER(5, 2)
);
create sequence seq_raschsv_prav_tarif7_1_427 start with 1;
comment on table raschsv_prav_tarif7_1_427 is 'Расчет соответствия условиям применения пониженного тарифа страховых взносов плательщиками, указанными в подпункте 7 пункта 1 статьи 427 Налогового кодекса Российской Федерации (ПравТариф7.1.427)';
comment on column raschsv_prav_tarif7_1_427.id is 'Идентификатор';
comment on column raschsv_prav_tarif7_1_427.raschsv_obyaz_plat_sv_id is 'Внешний ключ на Сводные данные об обязательствах плательщика страховых взносов';
comment on column raschsv_prav_tarif7_1_427.doh_vs_pred is 'Сумма доходов, всего (руб.) по итогам года, предшествующего текущему расчетному периоду (ДохВсПред)';
comment on column raschsv_prav_tarif7_1_427.doh_vs_per is 'Сумма доходов, всего (руб.) по итогам текущего расчетного периода (ДохВсПер)';
comment on column raschsv_prav_tarif7_1_427.doh_cel_post_pred is 'Сумма доходов в виде целевых поступлений на содержание некоммерческих организаций и ведение ими уставной деятельности, поименованной в пункте 7 статьи 427 Налогового кодекса Российской Федерации, определяемых в соответствии с пунктом 2 статьи 251 Налогового кодекса Российской Федерации (руб.) по итогам года, предшествующего текущему расчетному периоду (ДохЦелПостПред)';
comment on column raschsv_prav_tarif7_1_427.doh_cel_post_per is 'Сумма доходов в виде целевых поступлений на содержание некоммерческих организаций и ведение ими уставной деятельности, поименованной в пункте 7 статьи 427 Налогового кодекса Российской Федерации, определяемых в соответствии с пунктом 2 статьи 251 Налогового кодекса Российской Федерации (руб.) по итогам текущего отчетного (расчетного) периода (ДохЦелПостПер)';
comment on column raschsv_prav_tarif7_1_427.doh_grant_pred is 'Сумма доходов в виде грантов, получаемых для осуществления деятельности, поименованной в пункте 7 статьи 427 Налогового кодекса Российской Федерации, определяемых в соответствии с подпунктом 14 пункта 1 статьи 251 Налогового кодекса Российской Федерации (руб.) по итогам года, предшествующего текущему расчетному периоду (ДохГрантПред)';
comment on column raschsv_prav_tarif7_1_427.doh_grant_per is 'Сумма доходов в виде грантов, получаемых для осуществления деятельности, поименованной в пункте 7 статьи 427 Налогового кодекса Российской Федерации, определяемых в соответствии с подпунктом 14 пункта 1 статьи 251 Налогового кодекса Российской Федерации (руб.) по итогам текущего отчетного (расчетного) периода (ДохГрантПер)';
comment on column raschsv_prav_tarif7_1_427.doh_ek_deyat_pred is 'Сумма доходов от осуществления видов экономической деятельности, указанных в доходы от осуществления видов экономической деятельности, указанных в абзацах 17 – 21 и абзацах 34 - 36 подпункта 5 пункта 1 статьи 427 Налогового кодекса Российской Федерации (руб.) по итогам года, предшествующего текущему расчетному периоду (ДохЭкДеятПред)';
comment on column raschsv_prav_tarif7_1_427.doh_ek_deyat_per is 'Сумма доходов от осуществления видов экономической деятельности, указанных в доходы от осуществления видов экономической деятельности, указанных в абзацах 17 – 21 и абзацах 34 – 36 подпункта 5 пункта 1 статьи 427 Налогового кодекса Российской Федерации (руб.) по итогам текущего отчетного (расчетного) периода (ДохЭкДеятПер)';
comment on column raschsv_prav_tarif7_1_427.dol_doh_pred is 'Доля доходов, определяемая в целях применения пункта 7 статьи 427 Налогового кодекса Российской Федерации (%) по итогам года, предшествующего текущему расчетному периоду (ДолДохПред)';
comment on column raschsv_prav_tarif7_1_427.dol_doh_per is 'Доля доходов, определяемая в целях применения пункта 7 статьи 427 Налогового кодекса Российской Федерации (%) по итогам текущего отчетного (расчетного) периода (ДолДохПер)';
------------------------------------------------------------------------------------------------------
create table raschsv_sv_prim_tarif9_1_427
(
   id                 NUMBER(18)           not null,
   raschsv_obyaz_plat_sv_id NUMBER(18)           not null
);
create sequence seq_raschsv_sv_prim_tarif9_427 start with 1;
comment on table raschsv_sv_prim_tarif9_1_427 is 'Сведения, необходимые для применения пониженного тарифа страховых взносов плательщиками, указанными в подпункте 9 пункта 1 статьи 427 Налогового кодекса Российской Федерации (СвПримТариф9.1.427)';
comment on column raschsv_sv_prim_tarif9_1_427.id is 'Идентификатор';
comment on column raschsv_sv_prim_tarif9_1_427.raschsv_obyaz_plat_sv_id is 'Внешний ключ на Сводные данные об обязательствах плательщика страховых взносов';
------------------------------------------------------------------------------------------------------
create table raschsv_vyplat_it_427
(
   raschsv_sv_prim_tarif9_427_id NUMBER(18)           not null,
   raschsv_sv_sum1_tip_id NUMBER(18)           not null
);
comment on table raschsv_vyplat_it_427 is 'Итого выплат для Сведений, необходимых для применения пониженного тарифа страховых взносов плательщиками, указанными в подпункте 9 пункта 1 статьи 427 (ВыплатИт)';
comment on column raschsv_vyplat_it_427.raschsv_sv_prim_tarif9_427_id is 'Внешний ключ на Сведения, необходимые для применения пониженного тарифа страховых взносов плательщиками, указанными в подпункте 9 пункта 1 статьи 427';
comment on column raschsv_vyplat_it_427.raschsv_sv_sum1_tip_id is 'Внешний ключ на Сведения по суммам (тип 1)';
------------------------------------------------------------------------------------------------------
create table raschsv_sved_patent
(
   raschsv_sv_prim_tarif9_427_id NUMBER(18)           not null,
   raschsv_sv_sum1_tip_id NUMBER(18)           not null,
   nom_patent         VARCHAR2(20 CHAR),
   vyd_deyat_patent   VARCHAR2(6 CHAR),
   data_nach_deyst    DATE,
   data_kon_deyst     DATE
);
comment on table raschsv_sved_patent is 'Сведения о патенте (СведПатент)';
comment on column raschsv_sved_patent.raschsv_sv_prim_tarif9_427_id is 'Внешний ключ на Сведения, необходимые для применения пониженного тарифа страховых взносов плательщиками, указанными в подпункте 9 пункта 1 статьи 427';
comment on column raschsv_sved_patent.raschsv_sv_sum1_tip_id is 'Внешний ключ на Сведения по суммам (тип 1)';
comment on column raschsv_sved_patent.nom_patent is 'Номер патента (НомПатент)';
comment on column raschsv_sved_patent.vyd_deyat_patent is 'Код вида предпринимательской деятельности, установленный законодательством субъекта Российской Федерации, указанный в заявлении на получение патента (ВидДеятПатент)';
comment on column raschsv_sved_patent.data_nach_deyst is 'Дата начала действия (ДатаНачДейст)';
comment on column raschsv_sved_patent.data_kon_deyst is 'Дата окончания действия (ДатаКонДейст)';
------------------------------------------------------------------------------------------------------
create table raschsv_sv_prim_tarif2_2_425
(
   id                 NUMBER(18)           not null,
   raschsv_obyaz_plat_sv_id NUMBER(18)           not null
);
create sequence seq_raschsv_sv_prim_tarif2_425 start with 1;
comment on table raschsv_sv_prim_tarif2_2_425 is 'Сведения, необходимые для применения тарифа страховых взносов, установленного абзацем вторым подпункта 2 пункта 2 статьи 425 (СвПримТариф2.2.425)';
comment on column raschsv_sv_prim_tarif2_2_425.id is 'Идентификатор';
comment on column raschsv_sv_prim_tarif2_2_425.raschsv_obyaz_plat_sv_id is 'Внешний ключ на Сводные данные об обязательствах плательщика страховых взносов';
------------------------------------------------------------------------------------------------------
create table raschsv_vyplat_it_425
(
   raschsv_sv_prim_tarif2_425_id NUMBER(18)           not null,
   raschsv_sv_sum1_tip_id NUMBER(18)           not null
);
comment on table raschsv_vyplat_it_425 is 'Итого выплат для Сведений, необходимых для применения тарифа страховых взносов, установленного абзацем вторым подпункта 2 пункта 2 статьи 425 (ВыплатИт)';
comment on column raschsv_vyplat_it_425.raschsv_sv_prim_tarif2_425_id is 'Внешний ключ на Сведения, необходимые для применения тарифа страховых взносов, установленного абзацем вторым подпункта 2 пункта 2 статьи 425';
comment on column raschsv_vyplat_it_425.raschsv_sv_sum1_tip_id is 'Внешний ключ на Сведения по суммам (тип 1)';
------------------------------------------------------------------------------------------------------
create table raschsv_sv_ino_grazd
(
   raschsv_sv_prim_tarif2_425_id NUMBER(18)           not null,
   raschsv_sv_sum1_tip_id NUMBER(18)           not null,
   innfl              VARCHAR2(12 CHAR),
   snils              VARCHAR2(14 CHAR),
   grazd              VARCHAR2(3 CHAR),
   familia            VARCHAR2(60 CHAR),
   imya               VARCHAR2(60 CHAR),
   middle_name          VARCHAR2(60 CHAR)
);
comment on table raschsv_sv_ino_grazd is 'Сведения об иностранных гражданах, лицах без гражданства (СвИноГражд)';
comment on column raschsv_sv_ino_grazd.raschsv_sv_prim_tarif2_425_id is 'Внешний ключ на Сведения, необходимые для применения тарифа страховых взносов, установленного абзацем вторым подпункта 2 пункта 2 статьи 425';
comment on column raschsv_sv_ino_grazd.raschsv_sv_sum1_tip_id is 'Внешний ключ на Сведения по суммам (тип 1)';
comment on column raschsv_sv_ino_grazd.innfl is 'ИННФЛ (ИННФЛ)';
comment on column raschsv_sv_ino_grazd.snils is 'СНИЛС (СНИЛС)';
comment on column raschsv_sv_ino_grazd.grazd is 'Гражданство (код страны) (Гражд)';
comment on column raschsv_sv_ino_grazd.familia is 'Фамилия (Фамилия)';
comment on column raschsv_sv_ino_grazd.imya is 'Имя (Имя)';
comment on column raschsv_sv_ino_grazd.middle_name is 'Отчество (Отчество)';
------------------------------------------------------------------------------------------------------
create table raschsv_sv_prim_tarif1_3_422
(
   id                 NUMBER(18)           not null,
   raschsv_obyaz_plat_sv_id NUMBER(18)           not null
);
create sequence seq_raschsv_sv_prim_tarif1_422 start with 1;
comment on table raschsv_sv_prim_tarif1_3_422 is 'Сведения, необходимые для применения положений подпункта 1 пункта 3 статьи 422 Налогового кодекса Российской Федерации (СвПримТариф1.3.422)';
comment on column raschsv_sv_prim_tarif1_3_422.id is 'Идентификатор';
comment on column raschsv_sv_prim_tarif1_3_422.raschsv_obyaz_plat_sv_id is 'Внешний ключ на Сводные данные об обязательствах плательщика страховых взносов';
------------------------------------------------------------------------------------------------------
create table raschsv_sved_obuch
(
   id                 NUMBER(18)           not null,
   raschsv_sv_prim_tarif1_422_id NUMBER(18)           not null,
   raschsv_sv_sum1_tip_id NUMBER(18)           not null,
   unik_nomer         VARCHAR2(3 CHAR),
   familia            VARCHAR2(60 CHAR),
   imya               VARCHAR2(60 CHAR),
   middle_name          VARCHAR2(60 CHAR),
   sprav_nomer        VARCHAR2(10 CHAR),
   sprav_data         DATE,
   sprav_node_name    VARCHAR2(20 CHAR)
);
create sequence seq_raschsv_sved_obuch start with 1;
comment on table raschsv_sved_obuch is 'Сведения об обучающихся (СведОбуч)';
comment on column raschsv_sved_obuch.id is 'Идентификатор';
comment on column raschsv_sved_obuch.raschsv_sv_prim_tarif1_422_id is 'Внешний ключ на Сведения, необходимые для применения положений подпункта 1 пункта 3 статьи 422';
comment on column raschsv_sved_obuch.raschsv_sv_sum1_tip_id is 'Внешний ключ на Сведения по суммам (тип 1)';
comment on column raschsv_sved_obuch.unik_nomer is 'Уникальный номер (УникНомер)';
comment on column raschsv_sved_obuch.familia is 'Фамилия (Фамилия)';
comment on column raschsv_sved_obuch.imya is 'Имя (Имя)';
comment on column raschsv_sved_obuch.middle_name is 'Отчество (Отчество)';
comment on column raschsv_sved_obuch.sprav_nomer is 'Номер (Номер)';
comment on column raschsv_sved_obuch.sprav_data is 'Дата (Дата)';
comment on column raschsv_sved_obuch.sprav_node_name is 'Имя узла (СправСтудОтряд или СправФормОбуч)';
------------------------------------------------------------------------------------------------------
create table raschsv_sv_reestr_mdo
(
   id                 NUMBER(18)           not null,
   raschsv_sved_obuch_id NUMBER(18)           not null,
   naim_mdo           VARCHAR2(1000 CHAR),
   data_zapis         DATE,
   nomer_zapis        VARCHAR2(28 CHAR)
);
create sequence seq_raschsv_sv_reestr_mdo start with 1;
comment on table raschsv_sv_reestr_mdo is 'Сведения из реестра молодежных и детских объединений, пользующихся государственной поддержкой (СвРеестрМДО)';
comment on column raschsv_sv_reestr_mdo.id is 'Идентификатор';
comment on column raschsv_sv_reestr_mdo.raschsv_sved_obuch_id is 'Внешний ключ на Сведения об обучающихся';
comment on column raschsv_sv_reestr_mdo.naim_mdo is 'Наименование молодежного и детского объединения, пользующегося государственной поддержкой (НаимМДО)';
comment on column raschsv_sv_reestr_mdo.data_zapis is 'Дата записи в реестре (ДатаЗапис)';
comment on column raschsv_sv_reestr_mdo.nomer_zapis is 'Номер записи в реестре (НомерЗапис)';
------------------------------------------------------------------------------------------------------
create table raschsv_vyplat_it_422
(
   raschsv_sv_prim_tarif1_422_id NUMBER(18)           not null,
   raschsv_sv_sum1_tip_id NUMBER(18)           not null
);
comment on table raschsv_vyplat_it_422 is 'Итого выплат для Сведений, необходимые для применения положений подпункта 1 пункта 3 статьи 422 (ВыплатИт)';
comment on column raschsv_vyplat_it_422.raschsv_sv_prim_tarif1_422_id is 'Внешний ключ на Сведения, необходимые для применения положений подпункта 1 пункта 3 статьи 422';
comment on column raschsv_vyplat_it_422.raschsv_sv_sum1_tip_id is 'Внешний ключ на Сведения по суммам (тип 1)';
------------------------------------------------------------------------------------------------------
create table raschsv_pers_sv_strah_lic
(
   id                 NUMBER(18)           not null,
   declaration_data_id NUMBER(18)           not null,
   nom_korr           NUMBER(3),
   period             VARCHAR2(2 CHAR),
   otchet_god         VARCHAR2(4 CHAR),
   nomer              NUMBER(7),
   sv_data            DATE,
   innfl              VARCHAR2(12 CHAR),
   snils              VARCHAR2(14 CHAR),
   data_rozd          DATE,
   grazd              VARCHAR2(3 CHAR),
   pol                VARCHAR2(1 CHAR),
   kod_vid_doc        VARCHAR2(2 CHAR),
   ser_nom_doc        VARCHAR2(25 CHAR),
   priz_ops           VARCHAR2(1 CHAR),
   priz_oms           VARCHAR2(1 CHAR),
   priz_oss           VARCHAR2(1 CHAR),
   familia            VARCHAR2(60 CHAR),
   imya               VARCHAR2(60 CHAR),
   middle_name          VARCHAR2(60 CHAR)
);
create sequence seq_raschsv_pers_sv_strah_lic start with 1;
comment on table raschsv_pers_sv_strah_lic is 'Персонифицированные сведения о застрахованных лицах (ПерсСвСтрахЛиц)';
comment on column raschsv_pers_sv_strah_lic.id is 'Идентификатор';
comment on column raschsv_pers_sv_strah_lic.declaration_data_id is 'Идентификатор декларации';
comment on column raschsv_pers_sv_strah_lic.nom_korr is 'Номер корректировки (НомКорр)';
comment on column raschsv_pers_sv_strah_lic.period is 'Расчетный (отчетный) период (код) (Период)';
comment on column raschsv_pers_sv_strah_lic.otchet_god is 'Календарный год (ОтчетГод)';
comment on column raschsv_pers_sv_strah_lic.nomer is 'Номер (Номер)';
comment on column raschsv_pers_sv_strah_lic.sv_data is 'Дата (Дата)';
comment on column raschsv_pers_sv_strah_lic.innfl is 'ИНН (ИННФЛ)';
comment on column raschsv_pers_sv_strah_lic.snils is 'СНИЛС (СНИЛС)';
comment on column raschsv_pers_sv_strah_lic.data_rozd is 'Дата рождения (ДатаРожд)';
comment on column raschsv_pers_sv_strah_lic.grazd is 'Гражданство (код страны) (Гражд)';
comment on column raschsv_pers_sv_strah_lic.pol is 'Пол (Пол)';
comment on column raschsv_pers_sv_strah_lic.kod_vid_doc is 'Код вида документа, удостоверяющего личность (КодВидДок)';
comment on column raschsv_pers_sv_strah_lic.ser_nom_doc is 'Серия и номер документа, удостоверяющего личность (СерНомДок)';
comment on column raschsv_pers_sv_strah_lic.priz_ops is 'Признак застрахованного лица в системе обязательного пенсионного страхования (ПризОПС)';
comment on column raschsv_pers_sv_strah_lic.priz_oms is 'Признак застрахованного лица в системе обязательного медицинского страхования (ПризОМС)';
comment on column raschsv_pers_sv_strah_lic.priz_oss is 'Признак застрахованного лица в системе обязательного социального страхования (ПризОСС)';
comment on column raschsv_pers_sv_strah_lic.familia is 'Фамилия (Фамилия)';
comment on column raschsv_pers_sv_strah_lic.imya is 'Имя (Имя)';
comment on column raschsv_pers_sv_strah_lic.middle_name is 'Отчество (Отчество)';
------------------------------------------------------------------------------------------------------
create table raschsv_sv_vypl
(
   id                 NUMBER(18)           not null,
   raschsv_pers_sv_strah_lic_id NUMBER(18)           not null,
   sum_vypl_vs3       NUMBER(17,2),
   vypl_ops_vs3       NUMBER(17,2),
   vypl_ops_dog_vs3   NUMBER(17,2),
   nachisl_sv_vs3     NUMBER(17,2)
);
create sequence seq_raschsv_sv_vypl start with 1;
comment on table raschsv_sv_vypl is 'Сведения о сумме выплат и иных вознаграждений, начисленных в пользу физического лица (СвВыпл)';
comment on column raschsv_sv_vypl.id is 'Идентификатор';
comment on column raschsv_sv_vypl.raschsv_pers_sv_strah_lic_id is 'Внешний ключ на Персонифицированные сведения о застрахованных лицах';
comment on column raschsv_sv_vypl.sum_vypl_vs3 is 'Сумма выплат и иных вознаграждений всего за последние три месяца расчетного (отчетного) периода (СумВыплВс3)';
comment on column raschsv_sv_vypl.vypl_ops_vs3 is 'База для исчисления страховых взносов на обязательное пенсионное страхование в пределах предельной величины всего за последние три месяца расчетного (отчетного) периода (ВыплОПСВс3)';
comment on column raschsv_sv_vypl.vypl_ops_dog_vs3 is 'База для исчисления страховых взносов на обязательное пенсионное страхование в пределах предельной величины, в том числе по гражданско-правовым договорам, всего за последние три месяца расчетного (отчетного) периода (ВыплОПСДогВс3)';
comment on column raschsv_sv_vypl.nachisl_sv_vs3 is 'Сумма исчисленных страховых взносов с базы исчисления страховых взносов, не превышающих предельную величину всего за последние три месяца расчетного (отчетного) периода (НачислСВВс3)';
------------------------------------------------------------------------------------------------------
create table raschsv_sv_vypl_mk
(
   id                 NUMBER(18)           not null,
   raschsv_sv_vypl_id NUMBER(18)           not null,
   mesyac             VARCHAR2(2 CHAR),
   kod_kat_lic        VARCHAR2(4 CHAR),
   sum_vypl           NUMBER(17,2),
   vypl_ops           NUMBER(17,2),
   vypl_ops_dog       NUMBER(17,2),
   nachisl_sv         NUMBER(17,2)
);
create sequence seq_raschsv_sv_vypl_mk start with 1;
comment on table raschsv_sv_vypl_mk is 'Сведения о сумме выплат и иных вознаграждений, начисленных в пользу физического лица, по месяцу и коду категории застрахованного лица (СвВыплМК)';
comment on column raschsv_sv_vypl_mk.id is 'Идентификатор';
comment on column raschsv_sv_vypl_mk.raschsv_sv_vypl_id is 'Внешний ключ на Сведения о сумме выплат и иных вознаграждений, начисленных в пользу физического лица';
comment on column raschsv_sv_vypl_mk.mesyac is 'Месяц (Месяц)';
comment on column raschsv_sv_vypl_mk.kod_kat_lic is 'Код категории застрахованного лица (КодКатЛиц)';
comment on column raschsv_sv_vypl_mk.sum_vypl is 'Сумма выплат и иных вознаграждений (СумВыпл)';
comment on column raschsv_sv_vypl_mk.vypl_ops is 'База для исчисления страховых взносов на обязательное пенсионное страхование в пределах предельной величины (ВыплОПС)';
comment on column raschsv_sv_vypl_mk.vypl_ops_dog is 'База для исчисления страховых взносов на обязательное пенсионное страхование в пределах предельной величины, в том числе по гражданско-правовым договорам (ВыплОПСДог)';
comment on column raschsv_sv_vypl_mk.nachisl_sv is 'Сумма исчисленных страховых взносов с базы исчисления страховых взносов, не превышающих предельную величину (НачислСВ)';
------------------------------------------------------------------------------------------------------
create table raschsv_vypl_sv_dop
(
   id                 NUMBER(18)           not null,
   raschsv_pers_sv_strah_lic_id NUMBER(18)           not null,
   vypl_sv_vs3        NUMBER(17,2),
   nachisl_sv_vs3     NUMBER(17,2)
);
create sequence seq_raschsv_vypl_sv_dop start with 1;
comment on table raschsv_vypl_sv_dop is 'Сведения о сумме выплат и иных вознаграждений, начисленных в пользу физического лица, на которые исчислены страховые взносы по дополнительному тарифу (ВыплСВДоп)';
comment on column raschsv_vypl_sv_dop.id is 'Идентификатор';
comment on column raschsv_vypl_sv_dop.raschsv_pers_sv_strah_lic_id is 'Внешний ключ на Персонифицированные сведения о застрахованных лицах';
comment on column raschsv_vypl_sv_dop.vypl_sv_vs3 is 'Сумма выплат и иных вознаграждений, на которые исчислены страховые взносы, всего за последние три месяца расчетного (отчетного) периода (ВыплСВВс3)';
comment on column raschsv_vypl_sv_dop.nachisl_sv_vs3 is 'Сумма исчисленных страховых взносов всего за последние три месяца расчетного (отчетного) периода (НачислСВВс3)';
------------------------------------------------------------------------------------------------------
create table raschsv_vypl_sv_dop_mt
(
   id                 NUMBER(18)           not null,
   raschsv_vypl_sv_dop_id NUMBER(18)           not null,
   mesyac             VARCHAR2(2 CHAR),
   tarif             VARCHAR2(2 CHAR),
   vypl_sv            NUMBER(17,2),
   nachisl_sv         NUMBER(17,2)
);
create sequence seq_raschsv_vypl_sv_dop_mt start with 1;
comment on table raschsv_vypl_sv_dop_mt is 'Сведения о сумме выплат и иных вознаграждений, исчисленных в пользу физического лица, на которые исчислены страховые взносы по дополнительному тарифу, по месяцу и коду тарифа (ВыплСВДопМТ)';
comment on column raschsv_vypl_sv_dop_mt.id is 'Идентификатор';
comment on column raschsv_vypl_sv_dop_mt.raschsv_vypl_sv_dop_id is 'Внешний ключ на Сведения о сумме выплат и иных вознаграждений, начисленных в пользу физического лица, на которые исчислены страховые взносы по дополнительному тарифу';
comment on column raschsv_vypl_sv_dop_mt.mesyac is 'Месяц (Месяц)';
comment on column raschsv_vypl_sv_dop_mt.tarif is 'Тариф (Тариф)';
comment on column raschsv_vypl_sv_dop_mt.vypl_sv is 'Сумма выплат и иных вознаграждений, на которые исчислены страховые взносы (ВыплСВ)';
comment on column raschsv_vypl_sv_dop_mt.nachisl_sv is 'Сумма исчисленных страховых взносов (НачислСВ)';
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
comment on table declaration_subreport_params is 'Параметры спец. отчетов деклараций';
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
-- Справочники
create table fias_operstat
(
    id number(10) not null,
    name       varchar2(100 char) not null
);
comment on table fias_operstat  is 'Справочник "Статус действия"';
comment on column fias_operstat.id  is 'Идентификатор статуса (ключ)';
comment on column fias_operstat.name  is 'Наименование';

create table fias_socrbase
(
    id number(10) not null,
    scname   varchar2(10 char),
    socrname varchar2(60 char),
    kod_t_st varchar2(4 char) not null
);
comment on table fias_socrbase  is 'Справочник "Типы адресных объектов"';
comment on column fias_socrbase.id  is 'Суррогатный ключ';
comment on column fias_socrbase.scname  is 'Краткое наименование типа объекта';
comment on column fias_socrbase.socrname  is 'Полное наименование типа объекта';
comment on column fias_socrbase.kod_t_st  is 'Ключевое поле';



-- Сведения
create table fias_addrobj
(
    id number(18) not null,
    formalname varchar2(120 char) not null,
    regioncode varchar2(2 char) not null,
    autocode varchar2(1 char) not null,
    areacode varchar2(3 char) not null,
    citycode varchar2(3 char) not null,
    ctarcode varchar2(3 char) not null,
    placecode varchar2(3 char) not null,
    plancode varchar2(4 char) not null,
    streetcode varchar2(4 char) not null,
    extrcode varchar2(4 char) not null,
    sextcode varchar2(3 char) not null,
    livestatus number(1) not null,
    centstatus number(2) not null,
    operstatus number(2) not null,
    currstatus number(2) not null,
    divtype number(1) not null,
    offname varchar2(120 char),
    postalcode varchar2(6 char),
    parentguid number(18)
);

comment on column fias_addrobj.id is 'Глобальный уникальный идентификатор адресного объекта';
comment on column fias_addrobj.formalname is 'Формализованное наименование';
comment on column fias_addrobj.regioncode is 'Код региона';
comment on column fias_addrobj.autocode is 'Код автономии';
comment on column fias_addrobj.areacode is 'Код района';
comment on column fias_addrobj.citycode is 'Код города';
comment on column fias_addrobj.ctarcode is 'Код внутригородского района';
comment on column fias_addrobj.placecode is 'Код населенного пункта';
comment on column fias_addrobj.plancode is 'Код элемента планировочной структуры';
comment on column fias_addrobj.streetcode is 'Код улицы';
comment on column fias_addrobj.extrcode is 'Код дополнительного адресообразующего элемента';
comment on column fias_addrobj.sextcode is 'Код подчиненного дополнительного адресообразующего элемента';
comment on column fias_addrobj.livestatus is 'Статус актуальности адресного объекта ФИАС на текущую дату: 0 – Не актуальный, 1 - Актуальный';
comment on column fias_addrobj.centstatus is 'Статус центра: 0 – объект не является центром административно-территориального образования; 1 – объект является центром района; 2 – объект является центром (столицей) региона; 3 – объект является одновременно и центром района и центром региона.';
comment on column fias_addrobj.operstatus is 'Статус действия над записью – причина появления записи (см. fias_operstat)';
comment on column fias_addrobj.currstatus is 'Статус актуальности КЛАДР 4 (последние две цифры в коде)';
comment on column fias_addrobj.divtype is 'Тип деления: 0 – не определено, 1 – муниципальное, 2 – административное';
comment on column fias_addrobj.offname is 'Официальное наименование';
comment on column fias_addrobj.postalcode is 'Почтовый индекс';
comment on column fias_addrobj.parentguid is 'Идентификатор объекта родительского объекта';

create table fias_house
(
    id number(18) not null,
    aoguid     number(18) not null,
    eststatus  number(1) not null,
    strstatus  number(1) not null,
    statstatus number(2) not null,
    divtype    number(1) not null,
    postalcode varchar2(6 char),
    housenum   varchar2(20 char),
    buildnum   varchar2(10 char),
    strucnum   varchar2(10 char)

);
comment on table fias_house  is 'Сведения по отдельным зданиям, сооружениям';
comment on column fias_house.id is 'Глобальный уникальный идентификатор дома';
comment on column fias_house.aoguid is 'id записи родительского объекта (улицы, города, населенного пункта и т.п.)';
comment on column fias_house.eststatus is 'Признак владения: 0 – Не определено, 1 – Владение, 2 – Дом, 3 – Домовладение';
comment on column fias_house.strstatus is 'Признак строения: 0 – Не определено, 1 – Строение, 2 – Сооружение, 3 – Литер';
comment on column fias_house.statstatus is 'Состояние дома';
comment on column fias_house.divtype is 'Тип деления: 0 – не определено, 1 – муниципальное, 2 – административное';
comment on column fias_house.postalcode is 'Почтовый индекс';
comment on column fias_house.housenum is 'Номер дома';
comment on column fias_house.buildnum is 'Номер корпуса';
comment on column fias_house.strucnum is 'Номер строения';

create table fias_houseint
(
    id     number(18) not null,
    aoguid      number(18) not null,
    intstart   number(10) not null,
    intend     number(10) not null,
    intstatus  number(1) not null,
    counter    number(10) not null,
    postalcode varchar2(6 char)
);
comment on table fias_houseint  is 'Интервалы домов';
comment on column fias_houseint.intstart is 'Значение начала интервала';
comment on column fias_houseint.intend is 'Значение окончания интервала';
comment on column fias_houseint.id is 'Глобальный уникальный идентификатор интервала домов';
comment on column fias_houseint.aoguid is 'Идентификатор объекта родительского объекта (улицы, города, населенного пункта и т.п.)';
comment on column fias_houseint.intstatus is 'Статус интервала: 0 – Не определено, 1 – Обычный, 2 – Четный, 3 – Нечетный';
comment on column fias_houseint.counter is 'Счетчик записей по интервалам зданий, сооружений для формирования классификационного кода';
comment on column fias_houseint.postalcode is 'Почтовый индекс';


create table fias_room
(
    id   number(18) not null,
    houseguid   number(18) not null,
    regioncode varchar2(2 char) not null,
    flatnumber varchar2(50 char) not null,
    flattype   number(10) not null,
    livestatus number(1) not null,
    roomnumber varchar2(50 char),
    roomtypeid number(2),
    postalcode varchar2(6 char)
);
comment on table fias_room  is 'Сведения по помещениям';
comment on column fias_room.id is 'Глобальный уникальный идентификатор помещения';
comment on column fias_room.houseguid is 'Глобальный уникальный идентификатор родительского объекта (дома)';
comment on column fias_room.regioncode is 'Код региона';
comment on column fias_room.flatnumber is 'Номер квартиры, офиса и прочего';
comment on column fias_room.flattype is 'Тип квартиры';
comment on column fias_room.livestatus is 'Статус актуальности адресного объекта ФИАС на текущую дату: 0 – Не актуальный, 1 - Актуальный';
comment on column fias_room.roomnumber is 'Номер комнаты или помещения';
comment on column fias_room.roomtypeid is 'Тип комнаты';
comment on column fias_room.postalcode is 'Почтовый индекс';
--------------------------------------------------------------------------------------------------------------------------
-- Справочники физических лиц и статусов налогоплательщиков
-- с учетом изменений по задаче SBRFNDFL-132
--------------------------------------------------------------------------------------------------------------------------
create table ref_book_taxpayer_state
(
  id number(18) not null,
  code varchar2(1 char) not null,
  name varchar2(1000 char) not null
);

comment on table ref_book_taxpayer_state is 'Статусы налогоплательщиков';
comment on column ref_book_taxpayer_state.id is 'Уникальный идентификатор';
comment on column ref_book_taxpayer_state.code is 'Код';
comment on column ref_book_taxpayer_state.name is 'Наименование';

create table ref_book_person
(
  id number(18) not null,
  last_name varchar2(60 char) not null,
  first_name varchar2(60 char) not null,
  middle_name varchar2(60 char),
  sex number(1),
  inn varchar2(12 char),
  inn_foreign varchar2(50 char),
  snils varchar2(14 char),
  taxpayer_state number(18),
  birth_date date not null,
  birth_place varchar2(255 char),
  citizenship number(18),
  address number(18),
  pension number(1) default 2 not null,
  medical number(1) default 2 not null,
  social number(1) default 2 not null,
  employee number(1) default 2 not null,
  record_id number(18) not null,
  version date not null,
  status number(1) default 0 not null
);

comment on table ref_book_person is 'Физические лица';
comment on column ref_book_person.id is 'Уникальный идентификатор';
comment on column ref_book_person.last_name is 'Фамилия';
comment on column ref_book_person.first_name is 'Имя';
comment on column ref_book_person.middle_name is 'Отчество';
comment on column ref_book_person.sex is 'Пол';
comment on column ref_book_person.inn is 'ИНН в Российской Федерации';
comment on column ref_book_person.inn_foreign is 'ИНН в стране гражданства';
comment on column ref_book_person.snils is 'СНИЛС';
comment on column ref_book_person.taxpayer_state is 'Статус налогоплательщика';
comment on column ref_book_person.birth_date is 'Дата рождения';
comment on column ref_book_person.birth_place is 'Место рождения';
comment on column ref_book_person.citizenship is 'Гражданство';
comment on column ref_book_person.address is 'Место жительства';
comment on column ref_book_person.pension is 'Признак застрахованного лица в системе обязательного пенсионного страхования. Возможные значения: 1 - да; 2 - нет';
comment on column ref_book_person.medical is 'Признак застрахованного лица в системе обязательного медицинского страхования. Возможные значения: 1 - да; 2 - нет';
comment on column ref_book_person.social is 'Признак застрахованного лица в системе обязательного социального страхования. Возможные значения: 1 - да; 2 - нет';
comment on column ref_book_person.employee is 'Признак, показывающий, является ли ФЛ сотрудником Сбербанка. Возможные значения: 1 - является; 2 - не является';
comment on column ref_book_person.record_id is 'Идентификатор строки. Может повторяться у разных версий';
comment on column ref_book_person.version is 'Версия. Дата актуальности записи';
comment on column ref_book_person.status is 'Статус записи (0 - обычная запись, -1 - удаленная, 1 - черновик, 2 - фиктивная)';

create table ref_book_id_doc
(
  id number(18) not null,
  person_id number(18),
  doc_id number(18) not null,
  doc_number varchar2(25 char) not null,
  issued_by varchar2(255 char),
  issued_date date,
  inc_rep number(1)
);

comment on table ref_book_id_doc is 'Документ, удостоверяющий личность';
comment on column ref_book_id_doc.id is 'Уникальный идентификатор';
comment on column ref_book_id_doc.person_id is 'Физическое лицо';
comment on column ref_book_id_doc.doc_id is 'Вид документа';
comment on column ref_book_id_doc.doc_number is 'Серия и номер документа';
comment on column ref_book_id_doc.issued_by is 'Кем выдан документ';
comment on column ref_book_id_doc.issued_date is 'Дата выдачи';
comment on column ref_book_id_doc.inc_rep is 'Включается в отчетность';

create table ref_book_address
(
  id number(18) not null,
  address_type number(1) not null,
  country_id number(18),
  region_code varchar2(2 char),
  postal_code varchar2(6 char),
  district varchar2(50 char),
  city varchar2(50 char),
  locality varchar2(50 char),
  street varchar2(50 char),
  house varchar2(20 char),
  build varchar2(20 char),
  appartment varchar2(20 char)
);

comment on table ref_book_address is 'Адрес места жительства';
comment on column ref_book_address.id is 'Уникальный идентификатор';
comment on column ref_book_address.address_type is 'Тип адреса. Значения: 0 - в РФ 1 - вне РФ';
comment on column ref_book_address.country_id is 'Страна';
comment on column ref_book_address.region_code is 'Код региона';
comment on column ref_book_address.postal_code is 'Почтовый индекс';
comment on column ref_book_address.district is 'Район';
comment on column ref_book_address.city is 'Город';
comment on column ref_book_address.locality is 'Населенный пункт (село, поселок)';
comment on column ref_book_address.street is 'Улица (проспект, переулок)';
comment on column ref_book_address.house is 'Номер дома (владения)';
comment on column ref_book_address.build is 'Номер корпуса (строения)';
comment on column ref_book_address.appartment is 'Номер квартиры';

create table ref_book_id_tax_payer
(
  id        number(18) not null,
  person_id number(18) not null,
  inp       varchar2(14) not null,
  as_nu     number(18) not null
);
comment on table ref_book_id_tax_payer is 'Идентификатор налогоплательщика';
comment on column ref_book_id_tax_payer.id is 'Уникальный идентификатор';
comment on column ref_book_id_tax_payer.person_id is 'Ссылка на физическое лицо';
comment on column ref_book_id_tax_payer.inp is 'Уникальный неизменяемый цифровой идентификатор налогоплательщика';
comment on column ref_book_id_tax_payer.as_nu is 'Ссылка на запись справочника (100) Справочник АСНУ';

--------------------------------------------------------------------------------------------------------------------------
create table declaration_data_file
(
   declaration_data_id number(18) not null,
   blob_data_id varchar2(36) not null,
   user_name varchar2(512) not null,
   user_department_name varchar2(4000) not null,
   note varchar2(512)
);

comment on table declaration_data_file is 'Файлы налоговой формы';
comment on column declaration_data_file.declaration_data_id is 'Идентификатор экземпляра налоговой формы';
comment on column declaration_data_file.blob_data_id is 'Файл налоговой формы';
comment on column declaration_data_file.user_name is 'Полное имя пользователя, прикрепившего файл';
comment on column declaration_data_file.user_department_name is 'Наименование подразделения пользователя, прикрепившего файл';
comment on column declaration_data_file.note is 'Комментарий к файлу';
--------------------------------------------------------------------------------------------------------------------------
create table state
(
  id number(1),
  name varchar2(20 char)
);

comment on table state is 'Статус формы';
comment on column state.id is 'Уникальный идентификатор';
comment on column state.name is 'Наименование';

create table state_change
(
 id number(18) not null,
 from_id number(1),
 to_id number(1) not null
);

comment on table state_change is 'Возможные переходы между статусами';
comment on column state_change.id is 'Уникальный идентификатор';
comment on column state_change.from_id is 'Из какого статуса переход';
comment on column state_change.to_id is 'В какой статус переходим';
--------------------------------------------------------------------------------------------------------------------------
