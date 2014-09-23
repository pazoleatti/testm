create table form_kind (
  id number(18) not null,
  name varchar2(100) not null
);
comment on table form_kind is 'Тип налоговой формы';
comment on column form_kind.id is 'Идентификатор записи';
comment on column form_kind.name is 'Наименование';
--------------------------------------------------------------------------------------------------------------
create table ref_book_oktmo (
  id number(18) not null,
  code varchar2(4000) not null,
  name varchar2(4000) not null,
  parent_id number(18),
  version date not null,
  status number(1) not null,
  record_id number(9) not null
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
--------------------------------------------------------------------------------------------------------------
create table configuration (
  code varchar2(50) not null,
  value clob,
  department_id number(9) not null
);
comment on table configuration is 'Настройки приложения, конфигурация';
comment on column configuration.code is 'Код параметра';
comment on column configuration.value is 'Значение параметра';
comment on column configuration.department_id is 'ТБ';
-------------------------------------------------------------------------------------------------------------------------------------------
create table form_type (
  id       number(9) not null,
  name     varchar2(1000) not null,
  tax_type char(1) not null,
  status number(1) default 0 not null,
  code varchar2(600)
);
comment on table form_type is 'Типы налоговых форм (названия)';
comment on column form_type.id is 'Идентификатор';
comment on column form_type.name is 'Наименование';
comment on column form_type.tax_type is 'Вид налога (I-на прибыль, P-на имущество, T-транспортный, V-НДС, D-ТЦО)';
comment on column form_type.status is 'Статус версии (0 - действующая версия; -1 - удаленная версия, 1 - черновик версии, 2 - фиктивная версия)';
comment on column form_type.code is 'Номер формы';

create sequence seq_form_type start with 10000;
---------------------------------------------------------------------------------------------------
create table tax_period (
  id number(9) not null,
  tax_type char(1) not null,
  year number(4) not null
);
comment on table tax_period is 'Налоговые периоды';
comment on column tax_period.id is 'Идентификатор (первичный ключ)';
comment on column tax_period.tax_type is 'Вид налога (I-на прибыль, P-на имущество, T-транспортный, V-НДС, D-ТЦО)';
comment on column tax_period.year is 'Год';

create sequence seq_tax_period start with 10000;
---------------------------------------------------------------------------------------------------
create table form_template (
  id number(9) not null,
  type_id number(9) not null,
  data_rows clob,
  version date not null,
  fixed_rows number(1) not null,
  name varchar2(1000) not null,
  fullname varchar2(1000) not null,
  script clob,
  data_headers clob,
  status number(1) default 0 not null,
  monthly number(1) default 0 not null,
  header varchar2(1000)
);
comment on table form_template IS 'Описания шаблонов налоговых форм';
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

create sequence seq_form_template start with 10000;
---------------------------------------------------------------------------------------------------
create table form_style (
  id					     number(9) not null,
  alias				     varchar2(80) not null,
  form_template_id number(9) not null,
  font_color			 number(3) null,
  back_color			 number(3) null,
  italic				   number(1) not null,
  bold				     number(1) not null
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
  id            varchar2(36) not null,
  name          varchar2(530) null,
  data          blob not null,
  creation_date date not null
);
comment on table blob_data is 'Файловое хранилище';
comment on column blob_data.id is 'Уникальный идентификатор';
comment on column blob_data.name is 'Название файла';
comment on column blob_data.data is 'Бинарные данные';
comment on column blob_data.creation_date is 'Дата создания';
----------------------------------------------------------------------------------------------------
create table ref_book (
  id number(18,0) not null,
  name varchar2(200) not null,
  script_id varchar2(36),
  visible number(1) default 1 not null,
  type number(1) default 0 not null,
  read_only number(1) default 0 not null,
  region_attribute_id number(18,0),
  table_name varchar2(100)
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
------------------------------------------------------------------------------------------------------
create table ref_book_attribute (
  id number(18) not null,
  ref_book_id number(18) not null,
  name varchar2(510) not null,
  alias varchar2(30) not null,
  type number(1) not null,
  ord number(9) not null,
  reference_id number(18),
  attribute_id number(18),
  visible number(1) default 1 not null,
  precision number(2),
  width number(9) default 15 not null,
  required number(1) default 0 not null,
  is_unique number(1) default 0 not null,
  sort_order number(9),
  format number(2),
  read_only number(1) default 0 not null,
  max_length number(4)
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
  id number(18) not null,
  record_id number(9) not null,
  ref_book_id number(18) not null,
  version date not null,
  status number(1) default 0 not null
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
  record_id number(18) not null,
  attribute_id number(18) not null,
  string_value varchar2(4000),
  number_value number(27,10),
  date_value date,
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
  id number(9) not null,
  name varchar2(1000) not null,
  form_template_id number(9) not null,
  ord number(9) not null,
  alias varchar2(100) not null,
  type char(1) not null,
  width number(9) not null,
  precision number(9),
  max_length number(4),
  checking  number(1) default 0 not null,
  attribute_id number(18),
  format number(2),
  filter varchar2(1000),
  parent_column_id number(9),
  attribute_id2 number(18),
  numeration_row number(9)
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
comment on column form_column.max_length IS 'Максимальная длина строки';
comment on column form_column.parent_column_id is 'Ссылка на родительскую графу';
comment on column form_column.attribute_id2 is 'Код отображаемого атрибута для столбцов-ссылок второго уровня';
comment on column form_column.numeration_row is 'Тип нумерации строк для автонумеруемой графы (0 - последовательная, 1 - сквозная)';

create sequence seq_form_column start with 10000;
---------------------------------------------------------------------------------------------------
create table department (
  id number(9) not null,
  name varchar2(510) not null,
  parent_id number(9) null,
  type number(9) not null,
  shortname   varchar2(510),
  tb_index    varchar2(3),
  sbrf_code   varchar2(255),
  region_id number(18),
  is_active number(1,0) default 1 not null,
  code number(9,0) not null
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

create sequence seq_department start with 1000;
---------------------------------------------------------------------------------------------------
create table report_period (
  id number(9) not null,
  name varchar2(510) not null,
  tax_period_id number(9) not null,
  dict_tax_period_id number(18) not null,
  start_date date not null,
  end_date date not null,
  calendar_start_date date not null
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
  id                     number(18) not null,
  account                varchar2(255 char) not null,
  income_debet_remains   number(22,4),
  income_credit_remains  number(22,4),
  debet_rate             number(22,4),
  credit_rate            number(22,4),
  outcome_debet_remains  number(22,4),
  outcome_credit_remains number(22,4),
  account_name           varchar2(255 char),
  account_period_id 	 number(9) not null
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
  id               number(18) not null,
  opu_code         varchar2(25 char) not null,
  total_sum        number(22,4),
  item_name        varchar2(255 char),
  account_period_id number(9) not null
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
  id       number(9) not null,
  tax_type    char(1) not null,
  name      varchar2(80) not null,
  status number(1) default 0 not null
);
comment on table declaration_type is ' Виды деклараций';
comment on column declaration_type.id is 'Идентификатор (первичный ключ)';
comment on column declaration_type.tax_type is 'Вид налога (I-на прибыль, P-на имущество, T-транспортный, V-НДС, D-ТЦО)';
comment on column declaration_type.name is 'Наименование';
comment on column declaration_type.status is 'Статус версии (-1 -удаленная версия, 0 -действующая версия, 1 - черновик версии, 2 - фиктивная версия)';

create sequence seq_declaration_type start with 10000;
-----------------------------------------------------------------------------------------------------------------------------------
create table department_declaration_type (
  id         number(9) not null,
  department_id    number(9) not null,
  declaration_type_id number(9) not null
);
comment on table department_declaration_type is 'Сведения о декларациях, с которыми можно работать в подразделении';
comment on column department_declaration_type.id is 'Идентификатор (первичный ключ)';
comment on column department_declaration_type.department_id is 'Идентификатор подразделения';
comment on column department_declaration_type.declaration_type_id is 'Вид декларации';

create sequence seq_dept_declaration_type start with 10000;
-----------------------------------------------------------------------------------------------------------------------------------
create table declaration_template (
  id       number(9) not null,
  status number(1) default 0 not null,
  version date not null,
  name varchar2(600) not null,
  create_script       clob,
  jrxml               varchar2(36),
  declaration_type_id number(9) not null,
  XSD varchar2(36) 
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

create sequence seq_declaration_template start with 10000;
-----------------------------------------------------------------------------------------------------------------------------------
create table declaration_data (
  id number(18) not null,
  declaration_template_id number(9) not null,
  report_period_id        number(9) not null,
  department_id           number(9) not null,
  tax_organ_code          varchar2(4),
  kpp                     varchar2(9),
  data                    varchar2(36),
  is_accepted             number(1) not null,
  data_pdf                varchar2(36),
  data_xlsx               varchar2(36),
  jasper_print            varchar2(36) 
);

comment on table declaration_data is 'Налоговые декларации';
comment on column declaration_data.id is 'Идентификатор (первичный ключ)';
comment on column declaration_data.declaration_template_id is 'Ссылка на шаблон декларации';
comment on column declaration_data.report_period_id is 'Отчётный период';
comment on column declaration_data.department_id is 'Подразделение';
comment on column declaration_data.tax_organ_code is 'Налоговый орган';
comment on column declaration_data.kpp is 'КПП';
comment on column declaration_data.data is 'Данные декларации в формате законодателя (XML)';
comment on column declaration_data.is_accepted is 'Признак того, что декларация принята';
comment on column declaration_data.data_pdf is 'Данные декларации в формате PDF';
comment on column declaration_data.data_xlsx is 'Данные декларации в формате XLSX';
comment on column declaration_data.jasper_print is 'Сформированный отчет во внутреннем формате Jasper Reports';

create sequence seq_declaration_data start with 10000;
------------------------------------------------------------------------------------------------------------------------------------------
create table form_data (
  id number(18) not null,
  form_template_id number(9) not null,
  department_id number(9) not null,
  state number(9) not null,
  kind number(9) not null,
  report_period_id number(9) not null,
  return_sign number(1) not null,
  period_order number(2),
  number_previous_row number (9)
);
comment on table form_data is 'Данные по налоговым формам';
comment on column form_data.id is 'Первичный ключ';
comment on column form_data.form_template_id is 'Идентификатор шаблона формы';
comment on column form_data.department_id is 'Идентификатор подраздения';
comment on column form_data.state is 'Код состояния (1 - Создана, 2 - Подготовлена; 3 - Утверждена; 4 - Принята)';
comment on column form_data.kind is 'Тип налоговой формы (1 - Первичная, 2 - Консолидированная, 3 - Сводная, 4 - Форма УНП, 5 - Выходная)';
comment on column form_data.report_period_id is 'Идентификатор отчетного периода';
comment on column form_data.return_sign is 'Флаг возврата (0 - обычный режим; 1 - форма возвращена из вышестоящего статуса)';
comment on column form_data.period_order is 'Указывает на очередность налоговой формы в рамках налогового периода. Необходимо для, например, месячных форм в рамках квартального отчетного периода';
comment on column form_data.number_previous_row is 'Номер последней строки предыдущей НФ';

create sequence seq_form_data start with 10000;
---------------------------------------------------------------------------------------------------
create table form_data_signer (
  id      number(18) not null,
  form_data_id number(18) not null,
  name     varchar2(200) not null,
  position   varchar2(200) not null,
  ord     number(3) not null
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
  form_data_id number(18) not null,
  name varchar2(200) not null,
  phone varchar2(40),
  print_department_id number(9),
  report_department_name varchar2(4000 byte)
);
comment on table form_data_performer is 'Исполнитель налоговой формы';
comment on column form_data_performer.form_data_id is 'Первичный ключ';
comment on column form_data_performer.name is 'ФИО исполнителя';
comment on column form_data_performer.phone is 'Телефон';
comment on column form_data_performer.print_department_id is 'Подразделение, которое печатает налоговую форму';
comment on column form_data_performer.report_department_name is 'Наименование подразделения, которое должно быть использовано в печатной форме';
--------------------------------------------------------------------------------------------------
create table data_row (
  id number(18) not null,
  form_data_id number(18) not null,
  alias varchar2(20),
  ord number(14,0) not null,
  type number(1) not null,
  manual number(1) default 0 not null
);
comment on table data_row is 'Строки данных налоговых форм';
comment on column data_row.alias is 'Идентификатор строки';
comment on column data_row.form_data_id is 'Ссылка на записть в FORM_DATA';
comment on column data_row.id is 'Код строки для доступа из скриптов';
comment on column data_row.ord is 'Номер строки в форме';
comment on column data_row.type is 'Тип строки (0 - подтвержденные данные, 1 - строка добавлена, -1 - строка удалена)';
comment on column data_row.manual is 'Режим ввода данных (0 - обычная запись; 1 - версия ручного ввода)';

create sequence seq_data_row start with 10000;
---------------------------------------------------------------------------------------------------
create table cell_style (
  row_id  number(18) not null,
  column_id number(9) not null,
  style_id number(9) not null
);
comment on table cell_style is 'Привязка стилей к ячейкам налоговой формы';
comment on column cell_style.row_id is 'Идентификатор строки';
comment on column cell_style.column_id is 'Идентификатор столбца';
comment on column cell_style.style_id is 'Идентификатор стиля';
---------------------------------------------------------------------------------------------------
create table cell_editable(
  row_id number(18) not null,
  column_id number(9) not null
);
comment on table cell_editable is 'Информация о редактируемых ячейках налоговой формы';
comment on column cell_editable.row_id is 'Идентификатор строки налоговой формы';
comment on column cell_editable.column_id is 'Идентификатор столбца налоговой формы';
---------------------------------------------------------------------------------------------------
create table numeric_value (
  row_id number(18) not null,
  column_id number(9) not null,
  value     decimal(27, 10)
);
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
comment on table date_value is 'Значения налоговых форм типа дата';
comment on column date_value.column_id is 'Идентификатор столбца';
comment on column date_value.row_id is 'Идентификатор строки';
comment on column date_value.value is 'Значение';
---------------------------------------------------------------------------------------------------
create table data_cell (
  row_id number(18) not null,
  column_id number(9) not null,
  svalue varchar2(2000 char),
  nvalue decimal(27, 10),
  dvalue date,
  style_id number(9),
  editable number(1) default 0,
  colspan number(3),
  rowspan number(3)
);
comment on table data_cell is 'Значения налоговых форм типа дата';
comment on column data_cell.column_id is 'Идентификатор столбца';
comment on column data_cell.row_id is 'Идентификатор строки';
comment on column data_cell.svalue is 'Строковое значение';
comment on column data_cell.nvalue is 'Числовое значение (в том числе и для ссылок)';
comment on column data_cell.dvalue is 'Значение для даты-времени';
---------------------------------------------------------------------------------------------------
create table department_form_type (
  id      number(9) not null,
  department_id number(9) not null,
  form_type_id number(9) not null,
  kind     number(9) not null,
  performer_dep_id number(9)
);
comment on table department_form_type is 'Связь подразделения банка с формой';
comment on column department_form_type.id is 'Первичный ключ';
comment on column department_form_type.department_id is 'Идентификатор подразделения';
comment on column department_form_type.form_type_id is 'Идентификатор вида налоговой формы';
comment on column department_form_type.kind is 'Тип налоговой формы (1-Первичная, 2-Консолидированная, 3-Сводная, 4-Форма УНП, 5-Выходная)';
comment on column department_form_type.performer_dep_id is 'Исполнитель';

create sequence seq_department_form_type start with 10000;
---------------------------------------------------------------------------------------------------
create table declaration_source (
  department_declaration_type_id number(9) not null,
  src_department_form_type_id   number(9) not null,
  period_start date not null,
  period_end date null
);
comment on table declaration_source is 'Информация о формах-источниках данных для деклараций разных видов';
comment on column declaration_source.department_declaration_type_id is 'Иденфтикиатор сочетания вида декларации и подразделения, для которого задаётся источник';
comment on column declaration_source.src_department_form_type_id is 'Идентификатор сочетания типа и вида формы, а также подразделения, которые являются источников данных для деклараций';
comment on column declaration_source.period_start is 'Дата начала действия назначения';
comment on column declaration_source.period_end is 'Дата окончания действия назначения';
----------------------------------------------------------------------------------------------------
create table form_data_source (
  department_form_type_id number(9) not null,
  src_department_form_type_id number(9) not null,
  period_start date not null,
  period_end date null
);
comment on table form_data_source is 'Информация об источниках данных для формирования консолидированных и сводных налоговоых форм';
comment on column form_data_source.department_form_type_id is 'Идентификатор сочетания вида, типа формы и подразделения, для которого задётся источник данных';
comment on column form_data_source.src_department_form_type_id is 'Идентификатор сочетания вида, типа формы и подразделения, которое является источником данных';
comment on column form_data_source.period_start is 'Дата начала действия назначения';
comment on column form_data_source.period_end is 'Дата окончания действия назначения';
------------------------------------------------------------------------------------------------------------------------------------------------------------------
create table sec_user (
  id number(9) not null,
  login varchar2(255) not null,
  name varchar2(512) not null,
  department_id number(9) not null,
  is_active number(1) not null,
  email varchar2(128)
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
  id number(9) not null,
  alias varchar2(20) not null,
  name varchar2(50) not null
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
create table cell_span_info (
  row_id number(18) not null,
  column_id number(9) not null,
  colspan number(3),
  rowspan number(3)
);
comment on table cell_span_info is 'Информация об объединении ячеек в налоговой форме';
comment on column cell_span_info.row_id is 'Идентификатор строки';
comment on column cell_span_info.column_id is 'Идентификатор столбца';
comment on column cell_span_info.colspan is 'Число ячеек, которые должны быть объединены по горизонтали';
comment on column cell_span_info.rowspan is 'Число ячеек, которые должны быть объединены по вертикали';
----------------------------------------------------------------------------------------------------
create table log_business (
  id                  number(18,0) primary key,
  log_date            date not null,
  event_id            number(3,0) not null,
  user_login          varchar2(255) not null,
  roles               varchar2(200) not null,
  declaration_data_id number(9,0),
  form_data_id        number(9,0),
  note                varchar2(510),
  user_department_id  number(9,0) not null
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
comment on column log_business.user_department_id is 'Код подразделения пользователя';

create sequence seq_log_business;
------------------------------------------------------------------------------------------------------
create table log_system (
  id                  number(18,0) primary key,
  log_date            date not null,
  ip                  varchar2(39),
  event_id            number(3,0) not null,
  user_login          varchar2(255) not null,
  roles               varchar2(200),
  department_name     varchar2(4000 byte) not null,
  report_period_name  varchar2(100),
  form_kind_id        number(9,0),
  note                varchar2(4000 byte),
  user_department_name varchar2(4000 byte),
  declaration_type_name varchar2(80),
  form_type_name      varchar2(1000),
  form_department_id  number(9),
  tb_department_id number(9),
  blob_data_id        varchar2(36)
);
comment on table log_system is  'Системный журнал';
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
comment on column LOG_SYSTEM.DECLARATION_TYPE_NAME is 'Вид декларации';
comment on column LOG_SYSTEM.FORM_TYPE_NAME is 'Вид налоговой формы';
comment on column LOG_SYSTEM.FORM_DEPARTMENT_ID is 'Идентификатор подразделения налоговой формы/декларации';
comment on column log_system.tb_department_id is 'Идентификатор ТБ подразделения налоговой формы/декларации';
comment on column log_system.blob_data_id is 'Ссылка на логи';

create sequence seq_log_system start with 10000;
------------------------------------------------------------------------------------------------------
create table department_report_period (
  id                  number(18, 0)  not null,
  department_id       number(9) not null,
  report_period_id    number(9) not null,
  is_active           number(1) not null,
  is_balance_period   number(1) default 0 not null,
  correction_date     date
);
comment on table department_report_period is  'Привязка отчетных периодов к подразделениям';
comment on column department_report_period.id is 'Идентификатор записи';
comment on column department_report_period.department_id is 'Код подразделения';
comment on column department_report_period.report_period_id is 'Код отчетного периода';
comment on column department_report_period.is_active is 'Признак активности (0 - период закрыт, 1 - период открыт)';
comment on column department_report_period.is_balance_period is 'Признак того, что период является периодом ввода остатков (0 - обычный период, 1 - период ввода остатков)';
comment on column department_report_period.correction_date is 'Период сдачи корректировки';

create sequence seq_department_report_period start with 1000;
------------------------------------------------------------------------------------------------------
create table task_context(
id  number(18,0) primary key,
task_id number(18,0) not null,
task_name varchar2(100) not null,
modification_date date not null,
user_task_jndi varchar2(500) not null,
custom_params_exist number(9,0) not null,
serialized_params blob null,
user_id number(9) not null
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
create table notification(
id number(9) primary key,
report_period_id number(9) not null, 
sender_department_id number(9) not null, 
receiver_department_id number(9) null, 
first_reader_id number(9) null, 
text varchar2(2000) not null, 
create_date date not null, 
deadline date not null
);

comment on table notification is 'Оповещения';
comment on column notification.id is 'Уникальный идентификатор оповещения';
comment on column notification.report_period_id is 'идентификатор отчетного периода'; 
comment on column notification.sender_department_id is 'идентификатор подразделения-отправителя'; 
comment on column notification.receiver_department_id is 'идентификатор подразделения-получателя'; 
comment on column notification.first_reader_id is 'идентификатор первого пользователя, прочитавшего оповещение'; 
comment on column notification.text is 'текст оповещения';
comment on column notification.create_date is 'дата создания оповещения';
comment on column notification.deadline is 'дата сдачи отчетности';

create sequence seq_notification start with 10000;

--------------------------------------------------------------------------------------------------------

create table template_changes (
 id number(9) not null,
 form_template_id number(9),
 declaration_template_id number(9),
 event number(9),
 author number(9) not null,
 date_event date
);

comment on table template_changes is 'Изменение версий налоговых шаблонов';
comment on column template_changes.id is 'Уникальный идентификатор записи';
comment on column template_changes.form_template_id is 'Идентификатор налогового шаблона';
comment on column template_changes.declaration_template_id is 'Идентификатор шаблона декларации';
comment on column template_changes.event is 'Событие версии';
comment on column template_changes.author is 'Автор изменения';
comment on column template_changes.date_event is 'Дата изменения';

create table event
(
id number(9) NOT NULL,
name varchar2(510) NOT NULL
);

COMMENT ON TABLE event IS 'Справочник событий в системе';
COMMENT ON COLUMN event.id IS 'Идентификатор события';
COMMENT ON COLUMN event.name IS 'Наименование события';

create sequence seq_template_changes start with 10000;
--------------------------------------------------------------------------------------------------------
create table lock_data
(
  key varchar2(1000) not null,
  user_id number(9) not null,
  date_before date not null
);

comment on table lock_data is 'Информация о блокировках';
comment on column lock_data.key is 'Код блокировки';
comment on column lock_data.user_id is 'Идентификатор пользователя, установившего блокировку';
comment on column lock_data.date_before is 'Срок истечения блокировки';

--------------------------------------------------------------------------------------------------------
create table department_type
(
id number(9) not null,
name varchar2(50)
);

comment on table department_type is 'Типы подразделений банка';
comment on column department_type.id is 'Идентификатор типа';
comment on column department_type.name is 'Наименование типа';

--------------------------------------------------------------------------------------------------------
create table async_task_type
(
id number(18) not null,
name varchar2(100) not null,
handler_jndi varchar2(500) not null
);

comment on table async_task_type is 'Типы асинхронных задач';
comment on column async_task_type.id is 'Идентификатор строки';
comment on column async_task_type.name is 'Название типа задачи';
comment on column async_task_type.handler_jndi is 'JNDI имя класса-обработчика';

--------------------------------------------------------------------------------------------------------
create table report
(
form_data_id number(18) not null,
blob_data_id varchar2(36) not null,
type number(1) not null,
checking number(1) not null,
manual number(1) not null,
absolute number(1) not null
);

comment on table report is 'Отчет';
comment on column report.form_data_id is 'Идентификатор налоговой формы';
comment on column report.blob_data_id is 'Идентификатор отчета';
comment on column report.type is 'Тип отчета (0 - Excel, 1 - CSV, 2 - PDF, 3 - Jasper)';
comment on column report.manual is 'Режим ввода данных (0 - обычная версия; 1 - версия ручного ввода)'; 
comment on column report.checking is 'Типы столбцов (0 - только обычные, 1 - вместе с контрольными)';
comment on column report.absolute is 'Режим вывода данных (0 - только дельты, 1 - абсолютные значения)';

--------------------------------------------------------------------------------------------------------