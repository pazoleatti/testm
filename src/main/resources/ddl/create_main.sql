create table dict_tax_period(
code varchar2(2) not null,
name varchar2(510) not null
);
alter table dict_tax_period add constraint dict_tax_period_pk primary key (code);

comment on table dict_tax_period is 'Коды, определяющие налоговый (отчётный) период';
comment on column dict_tax_period.code is 'код';
comment on column dict_tax_period.name is 'наименование';
---------------------------------------------------------------------------------------------
create table dict_region(
	code varchar2(2) not null,
	name varchar2(510) not null,
	okato varchar2(11),
	okato_definition varchar2(11)
);
alter table dict_region add constraint dict_region_pk primary key (code);
alter table dict_region add constraint dict_region_uniq_okato_def unique (okato_definition);

comment on table dict_region is 'Коды субъектов Российской Федерации';
comment on column dict_region.code is 'код';
comment on column dict_region.name is 'наименование';
comment on column dict_region.okato is 'код ОКАТО';
comment on column dict_region.okato_definition is 'определяющая часть кода ОКАТО';
-----------------------------------------------------------------------------------------------------------
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
	value number(9) not null,
	dict_region_id varchar2(2)
);
alter table transport_tax_rate add constraint transport_tax_rate_pk primary key (id);
alter table transport_tax_rate add constraint transport_tax_rate_fk_dict_reg foreign key (dict_region_id) references dict_region(code);

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
-----------------------------------------------------------------------------------------------------------
create table dict_tax_benefit_param
(
id               number(9) not null,
dict_region_id   varchar(2) not null,
tax_benefit_id   varchar2(5) not null,
section          varchar2(4),
item             varchar2(4),
subitem          varchar2(4),
percent          number(3,2),
rate             number(15,2)
);

alter table dict_tax_benefit_param add constraint dict_tax_benefit_p_fk_dict_reg foreign key (dict_region_id) references dict_region(code);
alter table dict_tax_benefit_param add constraint dict_tax_benefit_param_pk primary key (id);
alter table dict_tax_benefit_param add constraint dict_tax_benefit_p_chk_perc check ((percent>=0) and (percent<=100));

comment on table dict_tax_benefit_param is 'Параметры налоговых льгот';
comment on column dict_tax_benefit_param.id is 'Первичный ключ';
comment on column dict_tax_benefit_param.dict_region_id is 'Код региона';
comment on column dict_tax_benefit_param.tax_benefit_id is 'Код налоговой  льготы';
comment on column dict_tax_benefit_param.section is 'Основание - статья';
comment on column dict_tax_benefit_param.item is 'Основание - пункт';
comment on column dict_tax_benefit_param.subitem is 'Основание - подпункт';
comment on column dict_tax_benefit_param.percent is 'Уменьшающий процент, %';
comment on column dict_tax_benefit_param.rate is 'Пониженная ставка';
-------------------------------------------------------------------------------------------------------------------------------------------
create table form_type (
	id number(9) not null,
	name varchar(600) not null,
	tax_type char(1) not null
);
alter table form_type add constraint form_type_pk primary key (id);
alter table form_type add constraint form_type_chk_taxtype check (tax_type in ('I', 'P', 'T', 'V'));

comment on table form_type is 'Типы налоговых форм (названия)';
comment on column form_type.id is 'Идентификатор';
comment on column form_type.name is 'Наименование';
comment on column form_type.tax_type is 'Вид налога (I-на прибыль, P-на имущество, T-транспортный, V-НДС)';

---------------------------------------------------------------------------------------------------
create table tax_period (
  id number(9) not null,
  tax_type char(1) not null,
  start_date date not null,
  end_date date not null
);
alter table tax_period add constraint tax_period_pk primary key (id);
alter table tax_period add constraint tax_period_chk_taxtype check (tax_type in ('I', 'P', 'T', 'V'));

comment on table tax_period is 'Налоговые периоды';
comment on column tax_period.id is 'Идентификатор (первичный ключ)';
comment on column tax_period.tax_type is 'Вид налога (I-на прибыль, P-на имущество, T-транспортный, V-НДС)';
comment on column tax_period.start_date is 'дата начала (включительно)';
comment on column tax_period.end_date is 'дата окончания (включительно)';
---------------------------------------------------------------------------------------------------
create table report_period
(
id number(9) not null,
name varchar2(50) not null,
is_active number(1) default 1 not null,
months    number(2) not null,
tax_period_id number(9) not null,
ord           number(2) not null
);

alter table report_period add constraint report_period_pk primary key(id);
alter table report_period add constraint report_period_fk_taxperiod foreign key (tax_period_id) references tax_period (id);
alter table report_period add constraint report_period_chk_active check (is_active in (0, 1));

comment on table report_period is 'Отчетные периоды';
comment on column report_period.id is 'Первичный ключ';
comment on column report_period.name is 'Наименование периода';
comment on column report_period.is_active is 'Признак активности';
comment on column report_period.months is 'Количество месяцев в периоде';
comment on column report_period.tax_period_id is 'Налоговый период';
comment on column report_period.ord is 'Номер отчетного периода в налоговом';

create sequence seq_report_period start with 100;
----------------------------------------------------------------------------------------------------
create table income_101
(report_period_id number(9) not null,
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
alter table income_101 add constraint income_101_fk_department_id foreign key (department_id)references department (id);
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
comment on column income_101.department_id is 'подразделение';
-------------------------------------------------------------------------------------------------------------------------------------------
create table income_102
(report_period_id number(9) not null,
 opu_code varchar2(25) not null,
 total_sum number(22,4),
 department_id number(15) not null);
 
alter table income_102 add constraint income_102_pk primary key (report_period_id, opu_code,department_id);
alter table income_102 add constraint income_102_fk_department_id foreign key (department_id) references department (id);
alter table income_102 add constraint income_102_fk_report_period_id foreign key (report_period_id) references report_period(id);

comment on table income_102 is 'Отчет о прибылях и убытках (Форма 0409102-СБ)';
comment on column income_102.report_period_id is 'Идентификатор отчетного периода';
comment on column income_102.opu_code is 'Код ОПУ';
comment on column income_102.total_sum is 'Сумма';
comment on column income_102.department_id is 'подразделение';

---------------------------------------------------------------------------------------------------
create table form_template (
	id number(9) not null,
	type_id number(9) not null,
	data_rows clob,
	version varchar2(20) not null,
	is_active number(9) default 1 not null,
	edition number(9) not null,
	numbered_columns NUMBER(1) not null,
	fixed_rows number(1) not null
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
---------------------------------------------------------------------------------------------------
create table form_style
(
	id					number(9) not null,
	alias				varchar(80) not null,
	form_template_id	number(9) not null,
	font_color			number(3) null,
	back_color			number(3) null,
	italic				number(1) not null, 
	bold				number(1) not null
);

alter table form_style add constraint FORM_STYLE_PK primary key (ID);
alter table form_style add constraint FORM_STYLE_FK_FORM_TEMPLATE_ID foreign key (FORM_TEMPLATE_ID) references FORM_TEMPLATE (ID);
alter table form_style add constraint FORM_STYLE_CHK_FONT_COLOR check (font_color in (0,1,2,3,4,5,6,7,8,9,10,11,12));
alter table form_style add constraint FORM_STYLE_CHK_BACK_COLOR check (back_color in (0,1,2,3,4,5,6,7,8,9,10,11,12));
alter table form_style add constraint FORM_STYLE_CHK_ITALIC check (italic in (0,1));
alter table form_style add constraint FORM_STYLE_CHK_BOLD check (bold in (0,1));
alter table form_style add constraint FORM_STYLE_UNIQ_ALIAS unique (form_template_id, alias);

comment on table form_style is 'Стили ячеек в налоговой форме';
comment on column form_style.id is 'Первичный ключ';
comment on column form_style.alias is 'Алиас стиля';
comment on column form_style.form_template_id is 'идентификатор шаблона налоговой формы';
comment on column form_style.font_color is 'код цвета шрифта';
comment on column form_style.back_color is 'код цвета фона';
comment on column form_style.italic is 'признак использования курсива';
comment on column form_style.bold is 'признак жирного шрифта';

create sequence seq_form_style start with 10000;
----------------------------------------------------------------------------------------------------
create table form_column (
	id number(9) not null,
	name varchar(1000) not null,
	form_template_id number(9) not null,
	ord number(9) not null,
	alias varchar(100) not null,
	type char(1) not null,
	width number(9) not null,
	precision number(9),
	dictionary_code varchar2(30),
	group_name varchar(1000),
	max_length number(4),
	checking   number(1) default 0 not null
);
alter table form_column add constraint form_column_pk primary key (id);
create sequence seq_form_column start with 10000;

alter table form_column add constraint form_column_fk_form_templ_id foreign key (form_template_id) references form_template(id);
alter table form_column add constraint form_column_uniq_alias unique(form_template_id, alias);
alter table form_column add constraint form_column_chk_type check(type in ('N', 'S', 'D'));
alter table form_column add constraint form_column_chk_precision check((type = 'N' and precision is not null and precision >=0 and precision < 9) or (type <> 'N' and precision is null));
alter table form_column add constraint form_column_chk_max_length check ((type = 'S' and max_length is not null and max_length > 0 and max_length <= 500) or (type <> 'S' and max_length is null));
alter table form_column add constraint form_column_chk_checking check (checking in (0, 1));

comment on table form_column is 'Описания столбцов налоговых форм';
comment on column form_column.alias is 'Код столбца, используемый в скриптинге';
comment on column form_column.dictionary_code is 'Код справочника (для строковых и числовых столбцов)';
comment on column form_column.form_template_id is 'Идентификатор шаблона налоговой формы';
comment on column form_column.group_name is 'Название группы столбцов';
comment on column form_column.id is 'Первичный ключ';
comment on column form_column.name is 'Название столбца';
comment on column form_column.ord is 'Порядковый номер';
comment on column form_column.precision is 'Количество знаков после запятой (только для числовых столбцов)';
comment on column form_column.type is 'Тип столбца (S- строка, N – число, D – дата)';
comment on column form_column.width is 'Ширина (в символах)';
comment on column form_column.checking is 'признак проверочного столбца';

---------------------------------------------------------------------------------------------------
create table form_script (
	id number(9) not null,
	form_template_id number(9) not null,
	name varchar(255),
	ord number(9) not null,
	body clob,
	condition clob,
	per_row number(1) not null
);
alter table form_script add constraint form_script_pk primary key (id);
alter table form_script add constraint form_script_fk_form_templ_id foreign key (form_template_id) references form_template(id);
alter table form_script add constraint form_script_chk_per_row check (per_row in (0,1));
create sequence seq_form_script start with 10000;

comment on table form_script is 'Скрипты';
comment on column form_script.body is 'Тело скрипта';
comment on column form_script.condition is 'Условие выполнения скрипта';
comment on column form_script.form_template_id is 'Идентификатор шаблона формы';
comment on column form_script.id is 'Первичный ключ';
comment on column form_script.name is 'Наименование скрипта';
comment on column form_script.ord is 'Порядок исполнения';
comment on column form_script.per_row is 'Признак строкового скрипта';
---------------------------------------------------------------------------------------------------
create table department
(
id number(9) not null,
name varchar2(260) not null,
parent_id number(9) null,
type number(9) not null,
shortname      varchar2(255),
dict_region_id varchar2(2),
tb_index       varchar2(3),
sbrf_code      varchar2(255)
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

alter table DEPARTMENT add constraint department_chk_id check ((type= 1 and id = 1) or (type <> 1 and id <> 1));
alter table DEPARTMENT add constraint department_chk_parent_id check ((type = 1 and parent_id is null) or (type <> 1 and parent_id is not null));

---------------------------------------------------------------------------------------------------
create table declaration_type
(
  id             number(9) not null,
  tax_type       char(1) not null,
  name           varchar(80) not null
);
alter table declaration_type add constraint declaration_type_pk primary key (id);
alter table declaration_type add constraint declaration_type_chk_tax_type check (tax_type in ('I', 'P', 'T', 'V'));

comment on table declaration_type is ' Виды деклараций';
comment on column declaration_type.id is 'идентификатор (первичный ключ)';
comment on column declaration_type.tax_type is 'тип налога';
comment on column declaration_type.name is 'наименование';
-----------------------------------------------------------------------------------------------------------------------------------
create table department_declaration_type
(
  id                  number(9) not null,
  department_id       number(9) not null,
  declaration_type_id number(9) not null
);
alter table department_declaration_type add constraint dept_decl_type_pk primary key (id);
alter table department_declaration_type add constraint dept_decl_type_fk_dept foreign key (department_id) references department (id);
alter table department_declaration_type add constraint dept_decl_type_fk_decl_type foreign key (declaration_type_id) references declaration_type (id);

comment on table department_declaration_type is 'Сведения о декларациях, с которыми можно работать в подразделении';
comment on column department_declaration_type.id is 'идентификатор (первичный ключ)';
comment on column department_declaration_type.department_id is 'идентификатор подразделения';
comment on column department_declaration_type.declaration_type_id is 'вид декларации';

create sequence seq_dept_declaration_type start with 10000;
-----------------------------------------------------------------------------------------------------------------------------------
create table declaration_template
(
  id             number(9) not null,
  edition        number(9) not null,
  version        varchar2(20) not null,
  is_active      number(1) not null,
  create_script  CLOB,
  jrxml          CLOB,
  jasper         BLOB,
  declaration_type_id number(9) not null
);
alter table declaration_template add constraint declaration_template_pk primary key (id);
alter table declaration_template add constraint declaration_t_chk_is_active check (is_active in (0,1));
alter table declaration_template add constraint declaration_template_fk_decl_type foreign key (declaration_type_id) references declaration_type (id);

comment on table declaration_template is 'Шаблоны налоговых деклараций';
comment on column declaration_template.id is 'идентификатор (первичный ключ)';
comment on column declaration_template.edition is 'номер редакции';
comment on column declaration_template.version is 'версия';
comment on column declaration_template.is_active is 'признак активности';
comment on column declaration_template.create_script is 'скрипт формирования декларации';
comment on column declaration_template.jrxml is 'макет JasperReports для формирования печатного представления формы';
comment on column declaration_template.jasper is 'скомпилированный макет JasperReports для формирования печатного представления формы';
comment on column declaration_template.declaration_type_id  is 'вид деклараций';

create sequence seq_declaration_template start with 10000;

-----------------------------------------------------------------------------------------------------------------------------------
create table declaration_data
(
  id                      number(18) not null,
  declaration_template_id number(9) not null,
  report_period_id        number(9) not null,
  department_id           number(9) not null,
  data                    clob,
  is_accepted             number(1) not null
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

create sequence seq_declaration_data start with 10000;
------------------------------------------------------------------------------------------------------------------------------------------
create table form_data (
	id number(18) not null,
	form_template_id number(9) not null,
	department_id number(9) not null,
	state number(9) not null,
	kind number(9) not null,
	report_period_id number(9) not null,
	acceptance_date    date
);
alter table form_data add constraint form_data_pk primary key (id);
alter table form_data add constraint form_data_fk_form_templ_id foreign key (form_template_id) references form_template(id);
alter table form_data add constraint form_data_fk_dep_id foreign key (department_id) references department(id);
alter table form_data add constraint form_data_fk_period_id foreign key (report_period_id) references report_period(id);
alter table form_data add constraint form_data_chk_kind check(kind in (1,2,3,4,5));
alter table form_data add constraint form_data_chk_state check(state in (1,2,3,4));

comment on table form_data is 'Данные по налоговым формам';
comment on column form_data.id is 'Первичный ключ';
comment on column form_data.form_template_id is 'Идентификатор шаблона формы';
comment on column form_data.department_id is 'Идентификатор подраздения';
comment on column form_data.state is 'Код состояния';
comment on column form_data.kind is 'Тип налоговой формы';
comment on column form_data.report_period_id is 'Идентификатор отчетного периода';

create sequence seq_form_data start with 10000;

---------------------------------------------------------------------------------------------------
create table form_data_signer
(id           number(18) not null,
 form_data_id number(18) not null,
 name         varchar2(200) not null,
 position     varchar2(200) not null,
 ord          number(3) not null);

alter table form_data_signer add constraint form_data_signer_pk primary key (id);
alter table form_data_signer add constraint form_data_signer_fk_formdata foreign key (form_data_id) references form_data (id) on delete cascade;  

comment on table form_data_signer is 'подписанты налоговых форм';
comment on column form_data_signer.id is 'идентфикатор записи (первичный ключ)';
comment on column form_data_signer.form_data_id is 'идентификатор налоговой формы';
comment on column form_data_signer.name is 'ФИО';
comment on column form_data_signer.position is 'должность';
comment on column form_data_signer.ord is 'номер подписанта по порядку';


create sequence seq_form_data_signer start with 10000;
---------------------------------------------------------------------------------------------------
create table form_data_performer
(form_data_id number(18) not null,
name varchar2(200) not null,
phone varchar2(20));

alter table form_data_performer add constraint form_data_performer_pk primary key (form_data_id);
alter table form_data_performer add constraint formdata_performer_fk_formdata foreign key (form_data_id) references form_data (id) on delete cascade;  

comment on table form_data_performer is 'исполнитель налоговой формы';
comment on column form_data_performer.form_data_id is 'Первичный ключ';
comment on column form_data_performer.name is 'ФИО исполнителя';
comment on column form_data_performer.phone is 'телефон';

---------------------------------------------------------------------------------------------------

create table data_row (
	id number(18) not null,
	form_data_id number(18) not null,
	alias varchar(20),
	ord number(9) not null	
);
alter table data_row add constraint data_row_pk primary key (id);
alter table data_row add constraint data_row_fk_form_data_id foreign key (form_data_id) references form_data(id);
alter table data_row add constraint data_row_uniq_form_data_order unique(form_data_id, ord);

comment on table data_row is 'Строки данных налоговых форм';
comment on column data_row.alias is 'Идентификатор строки';
comment on column data_row.form_data_id is 'Ссылка на записть в FORM_DATA';
comment on column data_row.id is 'Код строки для доступа из скриптов';
comment on column data_row.ord is 'Номер строки в форме';

create sequence seq_data_row start with 10000;
---------------------------------------------------------------------------------------------------
create table cell_style
(
  row_id    number(18) not null,
  column_id number(9) not null,
  style_id  number(9) not null
 );
alter table cell_style add constraint cell_style_pk primary key (row_id, column_id);
alter table cell_style add constraint cell_style_fk_column_id foreign key (column_id) references form_column (id);
alter table cell_style add constraint cell_style_fk_data_row foreign key (row_id) references data_row (id) on delete cascade;
alter table cell_style add constraint cell_style_fk_style_id foreign key (style_id) references form_style (id);

comment on table cell_style is 'Привязка стилей к ячейкам налоговой формы';
comment on column cell_style.row_id is 'Идентификатор строки';
comment on column cell_style.column_id is 'идентификатор столбца';
comment on column cell_style.style_id is 'идентификатор стиля';
---------------------------------------------------------------------------------------------------
create table cell_editable(
row_id number(18) not null,
column_id number(9) not null
);
alter table cell_editable add constraint cell_editable_pk primary key (row_id, column_id);
alter table cell_editable add constraint cell_editable_fk_data_row foreign key (row_id) references data_row (id) on delete cascade;
alter table cell_editable add constraint cell_editable_fk_form_column foreign key (column_id) references form_column (id);

comment on table cell_editable is 'информация о редактируемых ячейках налоговой формы';
comment on column cell_editable.row_id is 'идентификатор строки налоговой формы';
comment on column cell_editable.column_id is 'идентификатор столбца налоговой формы';
---------------------------------------------------------------------------------------------------

create table numeric_value (
	row_id number(18) not null,
	column_id number(9) not null,
	value decimal(25, 8)
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
	value varchar2(500 char)
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
create table department_form_type
(
id            number(9) not null,
department_id number(9) not null,
form_type_id  number(9) not null,
kind          number(9) not null
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
comment on column department_form_type.kind is 'тип налоговой формы';

create sequence seq_department_form_type start with 10000;
---------------------------------------------------------------------------------------------------
create table declaration_source
(
  department_declaration_type_id  number(9) not null,
  src_department_form_type_id     number(9) not null
);
alter table declaration_source add constraint declaration_source_pk primary key (department_declaration_type_id,src_department_form_type_id );
alter table declaration_source add constraint decl_source_fk_dept_decltype foreign key (department_declaration_type_id) references department_declaration_type (id);
alter table declaration_source add constraint decl_source_fk_dept_formtype foreign key (src_department_form_type_id) references department_form_type (id);

comment on table declaration_source is 'Информация о формах-источниках данных для деклараций разных видов';
comment on column declaration_source.department_declaration_type_id is 'иденфтикиатор сочетания вида декларации и подразделения, для которого задаётся источник';
comment on column declaration_source.src_department_form_type_id is 'идентификатор сочетания типа и вида формы, а также подразделения, которые являются источников данных для деклараций';

----------------------------------------------------------------------------------------------------
create table form_data_source
(
department_form_type_id number(9) not null,
src_department_form_type_id number(9) not null
);

alter table form_data_source add constraint form_data_source_pk primary key (department_form_type_id, src_department_form_type_id);
alter table form_data_source add constraint form_data_source_fk_dep_id foreign key (department_form_type_id) references department_form_type(id);
alter table form_data_source add constraint form_data_source_fk_src_dep_id foreign key (src_department_form_type_id) references department_form_type(id);
comment on table form_data_source is 'информация об источниках данных для формирования консолидированных и сводных налоговоых форм';
comment on column form_data_source.department_form_type_id is 'идентификатор сочетания вида, типа формы и подразделения, для которого задётся источник данных';
comment on column form_data_source.src_department_form_type_id is ' идентификатор сочетания вида, типа формы и подразделения, которое является источником данных';
------------------------------------------------------------------------------------------------------------------------------------------------------------------
create table event_script( 
  event_code number(9) not null, 
  script_id number(9) not null, 
  ord number(9) not null 
);

comment on table event_script is 'Привязка скриптов налоговой формы к событиям системы';
comment on column event_script.event_code is 'Тип события';
comment on column event_script.script_id is 'Идентификатор скрипта';
comment on column event_script.ord is 'Порядок выполнения скрипта';

alter table event_script add constraint event_script_chk_event_code check (EVENT_CODE IN (1, 2, 3, 4, 5, 101, 102, 103, 104, 105, 106, 107, 108, 109, 110, 203, 204, 205, 206, 301, 302));

alter table event_script add constraint event_script_pk primary key (event_code, script_id); 
alter table event_script add constraint event_script_fk_script_id foreign key (script_id) references form_script (ID);
---------------------------------------------------------------------------------------------------
create table sec_user (
	id number(9) not null,
	login varchar(50) not null,
	name varchar(50) not null,
	department_id number(9) not null	
);

alter table sec_user add constraint sec_user_pk primary key (id);
alter table sec_user add constraint sec_user_fk_dep_id foreign key (department_id) references department(id);

comment on table sec_user is 'Пользователи системы';
comment on column sec_user.id is 'Первичный ключ';
comment on column sec_user.login is 'Логин пользователя';
comment on column sec_user.name is 'Полное имя пользователя';
comment on column sec_user.department_id is 'Идентификатор подразделения';
---------------------------------------------------------------------------------------------------
create table object_lock
(
  object_id number(20) not null,
  class varchar(100) not null,
  user_id  number(9) not null,
  lock_time date not null
 );
alter table object_lock add constraint object_lock_pk primary key (object_id, class);
alter table object_lock add constraint object_lock_fk_user_id foreign key (user_id) references  sec_user (id) on delete cascade;

comment on table object_lock is 'Сведения о пользовательских блокировках объектов';
comment on column object_lock.object_id is 'идентификатор объекта';
comment on column object_lock.class is 'имя класса объекта';
comment on column object_lock.user_id is 'идентифкатор пользователя, заблокировавшего объект';
comment on column object_lock.lock_time is 'время блокировки';
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
CREATE TABLE CELL_SPAN_INFO
(
row_id number(18) not null,
column_id number(9) not null,
colspan number(3), 
rowspan number(3)
);

alter table cell_span_info add constraint cell_span_pk primary key (row_id, column_id);
alter table CELL_SPAN_INFO add constraint cell_span_info_fk_row_id foreign key (ROW_ID) references data_row (ID) on delete cascade;
alter table CELL_SPAN_INFO add constraint cell_span_info_fk_column_id foreign key (COLUMN_ID) references form_column (ID);
alter table CELL_SPAN_INFO add constraint cell_span_info_chk_span  check (colspan is not null or rowspan is not null);

comment on table cell_span_info is 'Информация об объединении ячеек в налоговой форме';
comment on column cell_span_info.row_id is 'Идентификатор строки';
comment on column cell_span_info.column_id is 'Идентификатор столбца';
comment on column cell_span_info.colspan is 'Число ячеек, которые должны быть объединены по горизонтали';
comment on column cell_span_info.rowspan is 'Число ячеек, которые должны быть объединены по вертикали';

----------------------------------------------------------------------------------------------------
create table department_param 
( department_id number(9) not null,
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
name  varchar2(2000)
);
alter table department_param add constraint department_param_pk primary key (department_id);
alter table department_param add constraint dept_param_fk_dict_region_id foreign key (dict_region_id) references dict_region(code);
alter table department_param add constraint dept_param_fk_dept_id foreign key (department_id) references department(id);

comment on table department_param is 'общие сведения';
comment on column department_param.department_id is 'идентификатор (первичный ключ)';
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
create table department_param_income 
( department_id                  number(9) not null,
  signatory_id        number(1) not null, 
  signatory_surname   varchar2(120) not null, 
  signatory_firstname varchar2(120), 
  signatory_lastname  varchar2(120), 
  approve_doc_name    varchar2(240), 
  approve_org_name    varchar2(240), 
  tax_place_type_code varchar2(3) not null,
  tax_rate            number(4,2),
  external_tax_sum    number(15),
  sum_difference      number(15),
  correction_sum      number(15),
  app_version         varchar2(40),
  format_version      varchar2(5)
);
alter table department_param_income add constraint department_param_income_pk primary key (department_id);
alter table department_param_income add constraint dept_param_income_chk_taxplace check (tax_place_type_code in ('213','214','215','216','218','220','223','225','226','231'));
alter table department_param_income add constraint dept_param_income_fk_dept_id foreign key (department_id) references department(id);

comment on table department_param_income is 'параметры подразделения по налогу на прибыль';
comment on column department_param_income.department_id is 'идентификатор (первичный ключ)';
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
create table department_param_transport 
( department_id       number(9) not null,
  signatory_id        number(1) not null, 
  signatory_surname   varchar2(120) not null, 
  signatory_firstname varchar2(120), 
  signatory_lastname  varchar2(120), 
  approve_doc_name    varchar2(240), 
  approve_org_name    varchar2(240), 
  tax_place_type_code varchar2(3) not null,
  app_version         varchar2(40),
  format_version      varchar2(5)
);
alter table department_param_transport add constraint department_param_transport_pk primary key (department_id);
alter table department_param_transport add constraint dept_param_transport_fk_deptid foreign key (department_id) references department(id);

comment on table department_param_transport is 'параметры подразделения по транспортному налогу';
comment on column department_param_transport.department_id is 'идентификатор (первичный ключ)';
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
create index i_department_parent_id on department(parent_id);
create index i_data_row_form_data_id on data_row(form_data_id);
create index i_form_data_report_period_id on form_data(report_period_id);
create index i_form_data_form_template_id on form_data(form_template_id);
create index i_form_data_department_id on form_data(department_id);
create index i_form_data_kind on form_data(kind);
create index i_form_data_signer_formdataid on form_data_signer(form_data_id);
