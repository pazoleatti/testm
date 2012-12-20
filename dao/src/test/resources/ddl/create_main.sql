create table form_type (
	id number(9) not null,
	name varchar(200) not null,
	tax_type char(1) not null
);
alter table form_type add constraint form_type_pk primary key (id);
alter table form_type add constraint form_type_chk_taxtype check (tax_type in ('I', 'P', 'T', 'V'));

comment on table form_type is 'Типы налоговых форм (названия)';

comment on column form_type.id is 'Идентификатор';
comment on column form_type.name is 'Наименование';
comment on column form_type.tax_type is 'Вид налога (I-на прибыль, P-на имущество, T-транспортный, V-НДС)';

---------------------------------------------------------------------------------------------------
create table report_period
(
id number(9) not null,
name varchar2(50) not null,
tax_type char(1) not null,
is_active number(1) default 1 not null
);

alter table report_period add constraint report_period_pk primary key(id);
alter table report_period add constraint report_period_chk_active check (is_active in (0, 1));
alter table report_period add constraint report_period_chk_taxtype check (tax_type in ('I', 'P', 'T', 'V'));

comment on table report_period is 'Отчетные периоды';
comment on column report_period.id is 'Первичный ключ';
comment on column report_period.name is 'Наименование периода';
comment on column report_period.tax_type is 'Вид налога (I-на прибыль, P-на имущество, T-транспортный, V-НДС)';
comment on column report_period.is_active is 'Признак активности';

---------------------------------------------------------------------------------------------------
create table form (
	id number(9) not null,
	type_id number(9) not null,
	data_rows clob,
	version varchar2(20) not null,
	is_active number(9) default 1 not null,
	edition number(9) not null
);
alter table form add constraint form_pk primary key (id);
alter table form add constraint form_fk_type_id foreign key (type_id) references form_type(id);
alter table form add constraint form_uniq_version unique(type_id, version);
alter table form add constraint form_check_active check (is_active in (0, 1));
comment on table form is 'Описания налоговых форм';
comment on column form.data_rows is 'Предопределённые строки формы в формате JSON';
comment on column form.id is 'Первичный ключ';
comment on column form.is_active is 'Признак активности';
comment on column form.type_id is 'Идентификатор вида налоговой формы';
comment on column form.version is 'Версия формы (уникально в рамках типа)';
comment on column form.edition is 'Номер редакции записи';
---------------------------------------------------------------------------------------------------
create table form_column (
	id number(9) not null,
	name varchar(200) not null,
	form_id number(9) not null,
	ord number(9) not null,
	alias varchar(100) not null,
	type char(1) not null,
	editable number(9) default 1 not null,
	mandatory number(9)  default 1 not null,
	width number(9) not null,
	precision number(9),
	dictionary_code varchar2(30),
	group_name varchar(255)
);
alter table form_column add constraint form_column_pk primary key (id);
create sequence seq_form_column start with 10000;

alter table form_column add constraint form_column_fk_form_id foreign key (form_id) references form(id);
alter table form_column add constraint form_column_uniq_alias unique(form_id, alias);
alter table form_column add constraint form_column_chk_type check(type in ('N', 'S', 'D'));
alter table form_column add constraint form_column_chk_editable check(editable in (0, 1));
alter table form_column add constraint form_column_chk_mandatory check(mandatory in (0, 1));
alter table form_column add constraint form_column_chk_precision check((type = 'N' and precision is not null and precision >=0 and precision < 5) or (type <> 'N' and precision is null));

comment on table form_column is 'Описания столбцов налоговых форм';
comment on column form_column.alias is 'Код столбца, используемый в скриптинге';
comment on column form_column.dictionary_code is 'Код справочника (для строковых и числовых столбцов)';
comment on column form_column.editable is 'Признак возможности редактирования пользователем';
comment on column form_column.form_id is 'Идентификатор налоговой формы';
comment on column form_column.group_name is 'Название группы столбцов';
comment on column form_column.id is 'Первичный ключ';
comment on column form_column.mandatory is 'Признак обязательности';
comment on column form_column.name is 'Название столбца';
comment on column form_column.ord is 'Порядковый номер';
comment on column form_column.precision is 'Количество знаков после запятой (только для числовых столбцов)';
comment on column form_column.type is 'Тип столбца (S- строка, N – число, D – дата)';
comment on column form_column.width is 'Ширина (в символах)';

---------------------------------------------------------------------------------------------------
create table form_script (
	id number(9) not null,
	form_id number(9) not null,
	name varchar(255),
	ord number(9) not null,
	body clob,
	condition clob,
	per_row number(1) not null
);
alter table form_script add constraint form_script_pk primary key (id);
alter table form_script add constraint form_script_fk_form_id foreign key (form_id) references form(id);
alter table form_script add constraint form_script_chk_per_row check (per_row in (0,1));
create sequence seq_form_script start with 10000;

comment on table form_script is 'Скрипты';
comment on column form_script.body is 'Тело скрипта';
comment on column form_script.condition is 'Условие выполнения скрипта';
comment on column form_script.form_id is 'Идентификатор формы';
comment on column form_script.id is 'Первичный ключ';
comment on column form_script.name is 'Наименование скрипта';
comment on column form_script.ord is 'Порядок исполнения';
comment on column form_script.per_row is 'Признак строкового скрипта';
---------------------------------------------------------------------------------------------------
create table department
(
id number(9) not null,
name varchar2(200) not null,
parent_id number(9) null,
type number(9) not null
);
alter table department add constraint department_pk primary key (id);

comment on table department is 'Подразделения банка';
comment on column department.id is 'Идентификатор записи';
comment on column department.name is 'Наименование подразделения';
comment on column department.parent_id is 'Идентификатор родительского подразделения';
comment on column department.type is 'Тип подразделения';

alter table DEPARTMENT add constraint department_chk_id check ((type= 1 and id = 1) or (type <> 1 and id <> 1));
alter table DEPARTMENT add constraint department_chk_parent_id check ((type = 1 and parent_id is null) or (type <> 1 and parent_id is not null));

---------------------------------------------------------------------------------------------------

create table form_data (
	id number(18) not null,
	form_id number(9) not null,
	department_id number(9) not null,
	state number(9) not null,
	kind number(9) not null,
	report_period_id number(9) not null
);
alter table form_data add constraint form_data_pk primary key (id);
alter table form_data add constraint form_data_fk_form_id foreign key (form_id) references form(id);
alter table form_data add constraint form_data_fk_dep_id foreign key (department_id) references department(id);
alter table form_data add constraint form_data_fk_period_id foreign key (report_period_id) references report_period(id);
alter table form_data add constraint form_data_chk_kind check(kind in (1,2,3));
alter table form_data add constraint form_data_chk_state check(state in (1,2,3,4));

comment on table form_data is 'Данные по налоговым формам';
comment on column form_data.id is 'Первичный ключ';
comment on column form_data.form_id is 'Идентификатор формы';
comment on column form_data.department_id is 'Идентификатор подраздения';
comment on column form_data.state is 'Код состояния';
comment on column form_data.kind is 'Тип налоговой формы';
comment on column form_data.report_period_id is 'Идентификатор отчетного периода';

create sequence seq_form_data start with 10000;

---------------------------------------------------------------------------------------------------

create table data_row (
	id number(18) not null,
	form_data_id number(18) not null,
	alias varchar(20),
	ord number(9) not null,
	MANAGED_BY_SCRIPTS number(1) default 0 not null
);
alter table data_row add constraint data_row_pk primary key (id);
alter table data_row add constraint data_row_fk_form_data_id foreign key (form_data_id) references form_data(id);
alter table data_row add constraint data_row_uniq_form_data_order unique(form_data_id, ord);
alter table data_row add constraint data_row_managed_check check (MANAGED_BY_SCRIPTS IN (0, 1));

comment on table data_row is 'Строки данных налоговых форм';
comment on column data_row.alias is 'Идентификатор строки';
comment on column data_row.form_data_id is 'Ссылка на записть в FORM_DATA';
comment on column data_row.id is 'Код строки для доступа из скриптов';
comment on column data_row.ord is 'Номер строки в форме';
comment on column data_row.managed_by_scripts is 'Признак того, что содержимое строки контролируется скриптами';

create sequence seq_data_row start with 10000;
---------------------------------------------------------------------------------------------------

create table numeric_value (
	row_id number(18) not null,
	column_id number(9) not null,
	value decimal(25, 4)
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
department_id number(9) not null,
form_type_id number(9) not null
);

alter table department_form_type add constraint dept_form_type_fk_dep_id foreign key (department_id) references department(id);
alter table department_form_type add constraint dept_form_type_fk_type_id foreign key (form_type_id) references form_type(id);

alter table department_form_type add constraint dept_form_type_pk primary key (department_id, form_type_id);

comment on table department_form_type is 'Связь подразделения банка с формой';
comment on column department_form_type.department_id is 'Идентификатор подразделения';
comment on column department_form_type.form_type_id is 'Идентификатор вида налоговой формы';
---------------------------------------------------------------------------------------------------
create table event_script( 
  event_code number(9) not null, 
  script_id number(9) not null, 
  ord number(9) not null 
);

comment on table event_script is 'Привязка скриптов налоговой формы к событиям системы';
comment on column event_script.event_code is 'Тип события';
comment on column event_script.script_id is 'Идентификатор скрипта';
comment on column event_script.ord is 'Порядок выполнения скрипта';

alter table event_script add constraint event_script_chk_event_code check (EVENT_CODE IN (1, 2, 3, 4, 101, 102, 103, 104, 105, 106, 203, 204, 205, 206));

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
 create index i_department_parent_id on department(parent_id);
 create index i_data_row_form_data_id on data_row(form_data_id);
 create index i_form_data_report_period_id on form_data(report_period_id);
 create index i_form_data_form_id on form_data(form_id);
 create index i_form_data_department_id on form_data(department_id);
 create index i_form_data_kind on form_data(kind);