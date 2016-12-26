create table form_kind (
  id   number(18)    not null,
  name varchar2(100) not null
);
comment on table form_kind is '��� ��������� �����';
comment on column form_kind.id is '������������� ������';
comment on column form_kind.name is '������������';
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
comment on table ref_book_oktmo is '�����';
comment on column ref_book_oktmo.id is '������������� ������';
comment on column ref_book_oktmo.code is '���';
comment on column ref_book_oktmo.name is '������������';
comment on column ref_book_oktmo.parent_id is '������������� ������������ ������';
comment on column ref_book_oktmo.version is '������. ���� ������������ ������';
comment on column ref_book_oktmo.status is '������ ������(0-������� ������, -1-���������, 1-��������, 2-���������)';
comment on column ref_book_oktmo.record_id is '������������� ������ �����������. ����� ����������� � ������ ������';

create sequence seq_ref_book_oktmo start with 300000 increment by 100;
create sequence seq_ref_book_oktmo_record_id start with 1000000;
--------------------------------------------------------------------------------------------------------------
create table configuration (
  code          varchar2(50) not null,
  value         clob,
  department_id number(9)    not null
);
comment on table configuration is '��������� ����������, ������������';
comment on column configuration.code is '��� ���������';
comment on column configuration.value is '�������� ���������';
comment on column configuration.department_id is '��';
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
comment on table form_type is '���� ��������� ���� (��������)';
comment on column form_type.id is '�������������';
comment on column form_type.name is '������������';
comment on column form_type.tax_type is '��� ������';
comment on column form_type.status is '������ ������ (0 - ����������� ������; -1 - ��������� ������, 1 - �������� ������, 2 - ��������� ������)';
comment on column form_type.code is '����� �����';
comment on column form_type.is_ifrs is '���������� ��� ����" (0 - �� ���������� ����, 1 - ���������� ����)';
comment on column form_type.ifrs_name is '������������ ����� ��� ����� ������� ������, ����������� � ����� � ����������� ��� ����';

create sequence seq_form_type start with 10000;
---------------------------------------------------------------------------------------------------
create table tax_period (
  id       number(9) not null,
  tax_type char(1)   not null,
  year     number(4) not null
);
comment on table tax_period is '��������� �������';
comment on column tax_period.id is '������������� (��������� ����)';
comment on column tax_period.tax_type is '��� ������';
comment on column tax_period.year is '���';

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
comment on table form_template is '�������� �������� ��������� ����';
comment on column form_template.data_rows is '��������������� ������ ����� � ������� XML';
comment on column form_template.id is '��������� ����';
comment on column form_template.type_id is '������������� ���� ��������� �����';
comment on column form_template.version is '������ ����� (��������� � ������ ����)';
comment on column form_template.fixed_rows is '������� ������������� ������������� �����: 0 - ������������ ������������� ����� �����, 1 - ���� ����������� ��������� � ������� ������ �� �����.';
comment on column form_template.name is '������������ �����';
comment on column form_template.fullname is '������ ������������ �����';
comment on column form_template.script is '������, ����������� ������-������ ��������� �����';
comment on column form_template.data_headers is '�������� ��������� �������';
comment on column form_template.status is '������ ������ (0 - ����������� ������; -1 - ��������� ������, 1 - �������� ������, 2 - ��������� ������)';
comment on column form_template.monthly is '������� ����������� ����� (0 - �� �����������, 1 - �����������)';
comment on column form_template.header is '������� ���������� �������� �����';
comment on column form_template.comparative is '"������� ������������� ������� ��������� (0 - �� ������������, 1 - ������������)';
comment on column form_template.accruing is '������� ������� ����������� ������ (0 - �� ������������, 1 - ������������)';
comment on column form_template.updating is '���������� ������ "��������" (0 - ���, 1 - ��)';

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
comment on table form_style is '����� ����� � ��������� �����';
comment on column form_style.id is '��������� ����';
comment on column form_style.alias is '����� �����';
comment on column form_style.form_template_id is '������������� ������� ��������� �����';
comment on column form_style.font_color is '��� ����� ������';
comment on column form_style.back_color is '��� ����� ����';
comment on column form_style.italic is '������� ������������� �������';
comment on column form_style.bold is '������� ������� ������';

create sequence seq_form_style start with 10000;
------------------------------------------------------------------------------------------------------
create table blob_data (
  id            varchar2(36)  not null,
  name          varchar2(530) null,
  data          blob          not null,
  creation_date date          not null
);
comment on table blob_data is '�������� ���������';
comment on column blob_data.id is '���������� �������������';
comment on column blob_data.name is '�������� �����';
comment on column blob_data.data is '�������� ������';
comment on column blob_data.creation_date is '���� ��������';
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

comment on table ref_book is '����������';
comment on column ref_book.id is '���������� �������������';
comment on column ref_book.name is '�������� �����������';
comment on column ref_book.script_id is '������������� ���������� �������';
comment on column ref_book.visible is '������� ���������';
comment on column ref_book.type is '��� ����������� (0 - ��������, 1 - �������������)';
comment on column ref_book.read_only is '������ ��� ������ (0 - �������������� �������� ������������; 1 - �������������� ���������� ������������)';
comment on column ref_book.region_attribute_id is '��� ��� ������� ���������� ��������� ������������. ��������� �� �������, �� �������� ������������ �������������� � �������';
comment on column ref_book.table_name is '�������� ������� ��, � ������� �������� ������';
comment on column ref_book.is_versioned is '���������� ���������� (0 - ���, 1 - ��)';
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
comment on table ref_book_attribute is '������� �����������';
comment on column ref_book_attribute.id is '���������� �������������';
comment on column ref_book_attribute.ref_book_id is '������ �� ����������';
comment on column ref_book_attribute.name is '��������';
comment on column ref_book_attribute.alias is '���������. ������������ ��� ��������� �� �������� ������-������';
comment on column ref_book_attribute.type is '���� �������� (1-������; 2-�����; 3-����-�����; 4-������)';
comment on column ref_book_attribute.ord is '������� ����������';
comment on column ref_book_attribute.reference_id is '������ �� ����������, �� ������� �������� ���������. ������ ��� ���������-������';
comment on column ref_book_attribute.attribute_id is '��� ������������� ��������. ������ ��� ���������-������';
comment on column ref_book_attribute.visible is '������� ���������';
comment on column ref_book_attribute.precision is '��������, ���������� ������ ����� �������. ������ ��� ���������-�����';
comment on column ref_book_attribute.width is '������ �������. ������������ ��� ����������� ����������� � ���� �������';
comment on column ref_book_attribute.required is '������� �������������� ���� (1 - �����������; 0 - ���)';
comment on column ref_book_attribute.is_unique is '������� ������������ �������� �������� ����������� (1 - ������ ���� ����������; 0 - ���)';
comment on column ref_book_attribute.sort_order is '���������� ������� ���������� �� ���������';
comment on column ref_book_attribute.format is '������. (��� ���: 0 - "", 1 - "dd.MM.yyyy", 2 - "MM.yyyy", 3 - "MMMM yyyy", 4 - "yyyy", 5 - "dd.MM"; ��� �����: 6 - �������)';
comment on column ref_book_attribute.read_only is '������ ��� ������ (0 - �������������� �������� ������������; 1 - �������������� ���������� ������������)';
comment on column ref_book_attribute.max_length is '������������ ����� ������/������������ ���������� ���� ��� ����� ����� � ����������� �����������';
------------------------------------------------------------------------------------------------------
create table ref_book_record (
  id          number(18)          not null,
  record_id   number(9)           not null,
  ref_book_id number(18)          not null,
  version     date                not null,
  status      number(1) default 0 not null
);
comment on table ref_book_record is '������ �����������';
comment on column ref_book_record.id is '���������� �������������';
comment on column ref_book_record.record_id is '������������� ������ �����������. ����� ����������� � ������ ������';
comment on column ref_book_record.ref_book_id is '������ �� ����������, � �������� ��������� ������';
comment on column ref_book_record.version is '������. ���� ������������ ������';
comment on column ref_book_record.status is '������ ������ (0 - ������� ������, -1 - ���������, 1 - ��������, 2 - ���������)';

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
comment on table ref_book_value is '�������� ������ �����������';
comment on column ref_book_value.record_id is '������ �� ������ �����������';
comment on column ref_book_value.attribute_id is '������ �� ������� �����������';
comment on column ref_book_value.string_value is '��������� ��������';
comment on column ref_book_value.number_value is '��������� ��������';
comment on column ref_book_value.date_value is '�������� ����';
comment on column ref_book_value.reference_value is '�������� ������';
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
comment on table form_column is '�������� �������� ��������� ����';
comment on column form_column.alias is '��� �������, ������������ � ����������';
comment on column form_column.form_template_id is '������������� ������� ��������� �����';
comment on column form_column.id is '��������� ����';
comment on column form_column.name is '�������� �������';
comment on column form_column.ord is '���������� �����';
comment on column form_column.precision is '���������� ������ ����� ������� (������ ��� �������� ��������)';
comment on column form_column.type is '��� ������� (S - ������, N � �����, D � ����, R - ������, A - �������������� �����)';
comment on column form_column.width is '������ (� ��������)';
comment on column form_column.checking is '������� ������������ �������';
comment on column form_column.attribute_id is '��� ������������� �������� ��� ��������-������';
comment on column form_column.format is '������';
comment on column form_column.filter is '������� ���������� ��������� �����������';
comment on column form_column.max_length is '������������ ����� ������';
comment on column form_column.parent_column_id is '������ �� ������������ �����';
comment on column form_column.attribute_id2 is '��� ������������� �������� ��� ��������-������ ������� ������';
comment on column form_column.numeration_row is '��� ��������� ����� ��� �������������� ����� (0 - ����������������, 1 - ��������)';
comment on column form_column.short_name is '������� ������������';

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
comment on table department is '������������� �����';
comment on column department.id is '������������� ������';
comment on column department.name is '������������ �������������';
comment on column department.parent_id is '������������� ������������� �������������';
comment on column department.type is '��� ������������� (1 - ����, 2- ��, 3- ����, ���, 4- ����������, 5- �� ���������� � �����)';
comment on column department.shortname is '����������� ������������ �������������';
comment on column department.tb_index is '������ ���������������� �����';
comment on column department.sbrf_code is '��� ������������� � ������� ���������';
comment on column department.region_id is '��� �������';
comment on column department.is_active is '����������� ������������� (0 - �� �����������, 1 - �����������)';
comment on column department.code is '��� �������������';
comment on column department.garant_use is '�������, ��� ������������ � ������ ��������';
comment on column department.sunr_use is '�������, ��� ������������ � �� ����';

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
comment on table report_period is '�������� �������';
comment on column report_period.id is '��������� ����';
comment on column report_period.name is '������������ �������';
comment on column report_period.tax_period_id is '��������� ������';
comment on column report_period.dict_tax_period_id is '������ �� ���������� �������� ��������';
comment on column report_period.start_date is '���� ������ ��������� �������';
comment on column report_period.end_date is '���� ��������� ��������� �������';
comment on column report_period.calendar_start_date is '����������� ���� ������ ��������� �������';

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
comment on table income_101 is '��������� ��������� (����� 0409101-��)';
comment on column income_101.id is '��� ������';
comment on column income_101.account is '����� �����';
comment on column income_101.income_debet_remains is '�������� ������� �� ������';
comment on column income_101.income_credit_remains is '�������� ������� �� �������';
comment on column income_101.debet_rate is '������� �� ������';
comment on column income_101.credit_rate is '������� �� �������';
comment on column income_101.outcome_debet_remains is '��������� ������� �� ������';
comment on column income_101.outcome_credit_remains is '��������� ������� �� �������';
comment on column income_101.account_name is '�������� �����';
comment on column income_101.account_period_id is '������������� ������� � ������������� ��';

create sequence seq_income_101 start with 100;
-------------------------------------------------------------------------------------------------------------------------------------------
create table income_102 (
  id                number(18)        not null,
  opu_code          varchar2(25 char) not null,
  total_sum         number(22, 4),
  item_name         varchar2(255 char),
  account_period_id number(9)         not null
);
comment on table income_102 is '����� � �������� � ������� (����� 0409102-��)';
comment on column income_102.id is '��� ������';
comment on column income_102.opu_code is '��� ���';
comment on column income_102.total_sum is '�����';
comment on column income_102.item_name is '������������ ������';
comment on column income_102.account_period_id is '������������� ������� � ������������� ��';

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
comment on table declaration_type is ' ���� ����������';
comment on column declaration_type.id is '������������� (��������� ����)';
comment on column declaration_type.tax_type is '��� ������';
comment on column declaration_type.name is '������������';
comment on column declaration_type.status is '������ ������ (-1 -��������� ������, 0 -����������� ������, 1 - �������� ������, 2 - ��������� ������)';
comment on column declaration_type.is_ifrs is '���������� ��� ����" (0 - �� ���������� ����, 1 - ���������� ����)';
comment on column declaration_type.ifrs_name is '������������ ����� ��� ����� ������� ������, ����������� � ����� � ����������� ��� ����';

create sequence seq_declaration_type start with 10000;
-----------------------------------------------------------------------------------------------------------------------------------
create table department_declaration_type (
  id                  number(9) not null,
  department_id       number(9) not null,
  declaration_type_id number(9) not null
);
comment on table department_declaration_type is '�������� � �����������, � �������� ����� �������� � �������������';
comment on column department_declaration_type.id is '������������� (��������� ����)';
comment on column department_declaration_type.department_id is '������������� �������������';
comment on column department_declaration_type.declaration_type_id is '��� ����������';

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
  XSD varchar2(36) 
);
comment on table declaration_template is '������� ��������� ����������';
comment on column declaration_template.id is '������������� (��������� ����)';
comment on column declaration_template.version is '������';
comment on column declaration_template.name is '������������ ������ ������';
comment on column declaration_template.create_script is '������ ������������ ����������';
comment on column declaration_template.jrxml is '����� JasperReports ��� ������������ ��������� ������������� �����';
comment on column declaration_template.declaration_type_id is '��� ����������';
comment on column declaration_template.XSD is 'XSD-�����';
comment on column declaration_template.status is '������ ������ (�������� (-1, 0, 1, 2))';

create sequence seq_declaration_template start with 10000;

-----------------------------------------------------------------------------------------------------------------------------------
create table declaration_data (
  id                          number(18)  not null,
  declaration_template_id     number(9)   not null,
  tax_organ_code              varchar2(4),
  kpp                         varchar2(9),
  is_accepted                 number(1)   not null,
  department_report_period_id number(18)  not null,
  asnu_id                     number(9),
  guid                        varchar2(32)
);

comment on table declaration_data is '��������� ����������';
comment on column declaration_data.id is '������������� (��������� ����)';
comment on column declaration_data.declaration_template_id is '������ �� ������ ����������';
comment on column declaration_data.tax_organ_code is '��������� �����';
comment on column declaration_data.kpp is '���';
comment on column declaration_data.is_accepted is '������� ����, ��� ���������� �������';
comment on column declaration_data.department_report_period_id is '������������� ��������� ������� �������������';
comment on column declaration_data.asnu_id is '������������� ����';
comment on column declaration_data.guid is 'GUID';

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
comment on table form_data is '������ �� ��������� ������';
comment on column form_data.id is '��������� ����';
comment on column form_data.form_template_id is '������������� ������� �����';
comment on column form_data.state is '��� ��������� (1 - �������, 2 - ������������; 3 - ����������; 4 - �������)';
comment on column form_data.kind is '��� ��������� ����� (1 - ���������, 2 - �����������������, 3 - �������, 4 - ����� ���, 5 - ��������)';
comment on column form_data.return_sign is '���� �������� (0 - ������� �����; 1 - ����� ���������� �� ������������ �������)';
comment on column form_data.period_order is '��������� �� ����������� ��������� ����� � ������ ���������� �������. ���������� ���, ��������, �������� ���� � ������ ������������ ��������� �������';
comment on column form_data.number_previous_row is '����� ��������� ������ ���������� ��';
comment on column form_data.department_report_period_id is '������������� ��������� ������� �������������';
comment on column form_data.manual is '����� ����� ������ (0 - �� �������� ������ ������� �����; 1 - ��������)';
comment on column form_data.sorted is '������� ������������ ����������';
comment on column form_data.number_current_row is '���������� ��������������� ����� ������� ��';
comment on column form_data.comparative_dep_rep_per_id is '������ ���������';
comment on column form_data.accruing is '������� ������� �������� ����������� ������ (0 - �� ����������� ������, 1 - ����������� ������)';
comment on column form_data.sorted_backup is '������ ������������ ���������� �� ��� ���������� ����� (0 - ���������� �����������; 1 - ���������� ���������)';
comment on column form_data.edited is '������� ��������� ������ �� � ������ �������������� (0 - ��� ���������; 1 - ���� ���������)';
comment on column form_data.note is '����������� � ��, �������� � ��������� ���� "����� � �����������"';

create sequence seq_form_data start with 10000;
---------------------------------------------------------------------------------------------------
create table form_data_signer (
  id           number(18)    not null,
  form_data_id number(18)    not null,
  name         varchar2(200) not null,
  position     varchar2(200) not null,
  ord          number(3)     not null
);
comment on table form_data_signer is '���������� ��������� ����';
comment on column form_data_signer.id is '������������ ������ (��������� ����)';
comment on column form_data_signer.form_data_id is '������������� ��������� �����';
comment on column form_data_signer.name is '���';
comment on column form_data_signer.position is '���������';
comment on column form_data_signer.ord is '����� ���������� �� �������';

create sequence seq_form_data_signer start with 10000;
---------------------------------------------------------------------------------------------------
create table form_data_performer (
  form_data_id           number(18) not null,
  name                   varchar2(200),
  phone                  varchar2(40),
  print_department_id    number(9),
  report_department_name varchar2(4000 byte)
);
comment on table form_data_performer is '����������� ��������� �����';
comment on column form_data_performer.form_data_id is '��������� ����';
comment on column form_data_performer.name is '��� �����������';
comment on column form_data_performer.phone is '�������';
comment on column form_data_performer.print_department_id is '�������������, ������� �������� ��������� �����';
comment on column form_data_performer.report_department_name is '������������ �������������, ������� ������ ���� ������������ � �������� �����';
---------------------------------------------------------------------------------------------------
create table department_form_type (
  id            number(9) not null,
  department_id number(9) not null,
  form_type_id  number(9) not null,
  kind          number(9) not null
);
comment on table department_form_type is '����� ������������� ����� � ������';
comment on column department_form_type.id is '��������� ����';
comment on column department_form_type.department_id is '������������� �������������';
comment on column department_form_type.form_type_id is '������������� ���� ��������� �����';
comment on column department_form_type.kind is '��� ��������� ����� (1-���������, 2-�����������������, 3-�������, 4-����� ���, 5-��������)';

create sequence seq_department_form_type start with 10000;
---------------------------------------------------------------------------------------------------
create table declaration_source (
  department_declaration_type_id number(9) not null,
  src_department_form_type_id    number(9) not null,
  period_start                   date      not null,
  period_end                     date      null
);
comment on table declaration_source is '���������� � ������-���������� ������ ��� ���������� ������ �����';
comment on column declaration_source.department_declaration_type_id is '������������� ��������� ���� ���������� � �������������, ��� �������� ������� ��������';
comment on column declaration_source.src_department_form_type_id is '������������� ��������� ���� � ���� �����, � ����� �������������, ������� �������� ���������� ������ ��� ����������';
comment on column declaration_source.period_start is '���� ������ �������� ����������';
comment on column declaration_source.period_end is '���� ��������� �������� ����������';
----------------------------------------------------------------------------------------------------
create table form_data_source (
  department_form_type_id     number(9) not null,
  src_department_form_type_id number(9) not null,
  period_start                date      not null,
  period_end                  date      null
);
comment on table form_data_source is '���������� �� ���������� ������ ��� ������������ ����������������� � ������� ���������� ����';
comment on column form_data_source.department_form_type_id is '������������� ��������� ����, ���� ����� � �������������, ��� �������� ������ �������� ������';
comment on column form_data_source.src_department_form_type_id is '������������� ��������� ����, ���� ����� � �������������, ������� �������� ���������� ������';
comment on column form_data_source.period_start is '���� ������ �������� ����������';
comment on column form_data_source.period_end is '���� ��������� �������� ����������';
------------------------------------------------------------------------------------------------------------------------------------------------------------------
create table sec_user (
  id            number(9)     not null,
  login         varchar2(255) not null,
  name          varchar2(512) not null,
  department_id number(9)     not null,
  is_active     number(1)     not null,
  email         varchar2(128)
);
comment on table sec_user is '������������ �������';
comment on column sec_user.id is '��������� ����';
comment on column sec_user.login is '����� ������������';
comment on column sec_user.name is '������ ��� ������������';
comment on column sec_user.department_id is '������������� �������������';
comment on column sec_user.is_active is '������� ���������� ������������ (0 - ������������; 1 - �������)';
comment on column sec_user.email is '����� ����������� �����';

create sequence seq_sec_user start with 10000;
-------------------------------------------------------------------------------------------------------------------------------------
create table sec_role (
  id    number(9)    not null,
  alias varchar2(20) not null,
  name  varchar2(50) not null
);
comment on table sec_role is '��������� ����';
comment on column sec_role.id is '��������� ����';
comment on column sec_role.alias is '��� ���� (������������� �������������)';
comment on column sec_role.name is '������������ ����';
---------------------------------------------------------------------------------------------------
create table sec_user_role (
  user_id number(9) not null,
  role_id number(9) not null
);
comment on table sec_user_role is '�������� ����� � �������������';
comment on column sec_user_role.user_id is '������������� ������������';
comment on column sec_user_role.role_id is '������������� ����';
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
comment on table log_business is '������ ������� ��������� ����\����������';
comment on column log_business.id is '��� ������';
comment on column log_business.log_date is '���� �������';
comment on column log_business.event_id is '��� ������� (1 - �������,2 - �������,3 - ����������,4 - ��������,5 - ���������,6 - ���������,7 - ������ ������,101 - ���������,102 - ������� �� \����������\ � \�������\,103 - ������� �� \����������\,104 - ������� �� \�������\ � \����������\,105 - ������� �� \�������\,106 - ������� �� \�������\ � \�������\,107 - �����������,108, ������� �� \������������\ � \�������\,109, ������� �� \������������\,110, ������� �� \�������\ � \������������\,203 - ����� ������� �� \����������\,204 - ����� ������� �� \�������\ � \����������\,205 - ����� ������� �� \�������\,206 - ����� ������� �� \�������\ � \�������\,207 - ����� ������� �� \"������������\,301 - �������� ������,303 - ������� ������,302 - ��������)';
comment on column log_business.user_login is '����� ������������';
comment on column log_business.roles is '������ ����� ������������';
comment on column log_business.declaration_data_id is '��� ����������';
comment on column log_business.form_data_id is '��� ��������� �����';
comment on column log_business.note is '����� ���������';
comment on column log_business.user_department_name is '������������� ������������';

create sequence seq_log_business;
------------------------------------------------------------------------------------------------------
create table audit_form_type
(
  id   number(9, 0)   not null,
  name varchar2(1000) not null
);

comment on table audit_form_type is '���� ���� ��� ������� ������';
comment on column audit_form_type.id is '��� ������';
comment on column audit_form_type.name is '������������ ����';
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
comment on table log_system is '��������� ������';
comment on column log_system.id is '��� ������';
comment on column log_system.log_date is '���� �������';
comment on column log_system.ip is 'IP-����� ������������';
comment on column log_system.event_id is '��� ������� (1 - �������,2 - �������,3 - ����������,4 - ��������,5 - ���������,6 - ���������,7 - ������ ������,101 - ���������,102 - ������� �� \����������\ � \�������\,103 - ������� �� \����������\,104 - ������� �� \�������\ � \����������\,105 - ������� �� \�������\,106 - ������� �� \�������\ � \�������\,107 - �����������,108 - ������� �� \������������\ � \�������\,109 - ������� �� \������������\,110 - ������� �� \�������\ � \������������\,203 - ����� ������� �� \����������\,204 - ����� ������� �� \�������\ � \����������\,205 - ����� ������� �� \�������\,206 - ����� ������� �� \�������\ � \�������\,207 - ����� ������� �� \"������������\,301 - �������� ������,303 - ������� ������,302 - ��������)';
comment on column log_system.user_login is '����� ������������';
comment on column log_system.roles is '������ ����� ������������';
comment on column log_system.department_name is '������������ ������������� ��\����������';
comment on column log_system.report_period_name is '������������ ��������� �������';
comment on column log_system.form_kind_id is '��� ���� ��������� ����� (1,2,3,4,5)';
comment on column log_system.note is '����� ���������';
comment on column log_system.user_department_name is '������������ ������������� ������������';
comment on column log_system.declaration_type_name is '��� ����������';
comment on column log_system.form_type_name is '��� ��������� �����';
comment on column log_system.form_department_id is '������������� ������������� ��������� �����/����������';
comment on column log_system.blob_data_id is '������ �� ����';
comment on column log_system.form_type_id is '������������� ���� ��';
comment on column log_system.is_error is '������� ������';
comment on column log_system.audit_form_type_id is '��� �����';
comment on column log_system.server is '������';

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
comment on table department_report_period is '�������� �������� �������� � ��������������';
comment on column department_report_period.id is '������������� ������';
comment on column department_report_period.department_id is '��� �������������';
comment on column department_report_period.report_period_id is '��� ��������� �������';
comment on column department_report_period.is_active is '������� ���������� (0 - ������ ������, 1 - ������ ������)';
comment on column department_report_period.is_balance_period is '������� ����, ��� ������ �������� �������� ����� �������� (0 - ������� ������, 1 - ������ ����� ��������)';
comment on column department_report_period.correction_date is '������ ����� �������������';

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
comment on table task_context is '�������� ���������������� ����� ������������';
comment on column task_context.id is '���������� ������������� ������';
comment on column task_context.task_id is '������������� ������ ������������ websphere';
comment on column task_context.task_name is '�������� ������';
comment on column task_context.user_task_jndi is 'JNDI-��� ������-����������� ������';
comment on column task_context.custom_params_exist is '������� ������� ���������������� ����������';
comment on column task_context.serialized_params is '��������������� ��������������� ���������';
comment on column task_context.modification_date is '���� ���������� �������������� ������';
comment on column task_context.user_id is '������������� ������������';

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

comment on table notification is '����������';
comment on column notification.id is '���������� ������������� ����������';
comment on column notification.report_period_id is '������������� ��������� �������';
comment on column notification.sender_department_id is '������������� �������������-�����������';
comment on column notification.receiver_department_id is '������������� �������������-����������';
comment on column notification.text is '����� ����������';
comment on column notification.create_date is '���� �������� ����������';
comment on column notification.deadline is '���� ����� ����������';
comment on column notification.user_id is '������������� ������������, ������� ������� ����������';
comment on column notification.role_id is '������������� ���� ������������, ������� ������� ����������';
comment on column notification.is_read is '������� ���������';
comment on column notification.blob_data_id is '������ �� ����';
comment on column notification.type is '��� ���������� (0 - ������� ����������, 1 - �������� ������ �� ����� �����������)';
comment on column notification.report_id is '������������� ������';

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

comment on table template_changes is '��������� ������ ��������� ��������';
comment on column template_changes.id is '���������� ������������� ������';
comment on column template_changes.form_template_id is '������������� ���������� �������';
comment on column template_changes.declaration_template_id is '������������� ������� ����������';
comment on column template_changes.event is '������� ������';
comment on column template_changes.author is '����� ���������';
comment on column template_changes.date_event is '���� ���������';
comment on column template_changes.ref_book_id is '������������� �����������';
--------------------------------------------------------------------------------------------------------
create table event
(
  id   number(9)     not null,
  name varchar2(510) not null
);

comment on table event is '���������� ������� � �������';
comment on column event.id is '������������� �������';
comment on column event.name is '������������ �������';

create sequence seq_template_changes start with 10000;
--------------------------------------------------------------------------------------------------------
create table role_event (
  event_id number(9) not null,
  role_id  number(9) not null
);
comment on table role_event is '��������� ���� ������� � �������� ������� ������ �� �����';
comment on column role_event.event_id is '������������� �������';
comment on column role_event.role_id is '������������� ����';
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

comment on table lock_data is '���������� � �����������';
comment on column lock_data.key is '��� ����������';
comment on column lock_data.user_id is '������������� ������������, ������������� ����������';
comment on column lock_data.date_lock is '���� ��������� ����������';
comment on column lock_data.state is '������ ���������� ����������� ������, ��������� � �����������';
comment on column lock_data.state_date is '���� ���������� ��������� �������';
comment on column lock_data.description is '�������� ����������';
comment on column lock_data.queue is '�������, � ������� ��������� ��������� ����������� ������';
comment on column lock_data.server_node is '������������ ���� ��������, �� ������� ����������� ��������� ����������� ������';
--------------------------------------------------------------------------------------------------------
create table department_type
(
  id   number(9) not null,
  name varchar2(50)
);

comment on table department_type is '���� ������������� �����';
comment on column department_type.id is '������������� ����';
comment on column department_type.name is '������������ ����';

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

comment on table async_task_type is '���� ����������� �����';
comment on column async_task_type.id is '������������� ������';
comment on column async_task_type.name is '�������� ���� ������';
comment on column async_task_type.handler_jndi is 'JNDI ��� ������-�����������';
comment on column async_task_type.task_limit is '����������� �� ���������� ������';
comment on column async_task_type.short_queue_limit is '����������� �� ���������� ������ � ������� ������� �����';
comment on column async_task_type.limit_kind is '��� �����������';
comment on column async_task_type.dev_mode is '������� ������ ��� dev-����';

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

comment on table form_data_report is '�����';
comment on column form_data_report.form_data_id is '������������� ��������� �����';
comment on column form_data_report.blob_data_id is '������������� ������';
comment on column form_data_report.type is '��� ������ (Excel/CSV/����������� �����)';
comment on column form_data_report.manual is '����� ����� ������ (0 - ������� ������; 1 - ������ ������� �����)';
comment on column form_data_report.checking is '���� �������� (0 - ������ �������, 1 - ������ � ������������)';
comment on column form_data_report.absolute is '����� ������ ������ (0 - ������ ������, 1 - ���������� ��������)';

--------------------------------------------------------------------------------------------------------
create table declaration_report
(
  declaration_data_id number(18)   not null,
  blob_data_id        varchar2(36) not null,
  type                number(1)    not null,
  subreport_id        number(9)
);

comment on table declaration_report is '������ �� �����������';
comment on column declaration_report.declaration_data_id is '������������� ����������';
comment on column declaration_report.blob_data_id is '������������� ������';
comment on column declaration_report.type is '��� ������ (0 - Excel, 1 - XML, 2 - PDF, 3 - Jasper, 4 - ����.�����)';
comment on column declaration_report.subreport_id is '������������� ����. ������';

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

comment on table declaration_subreport is '����. ������ ������ ������ ����������';
comment on column declaration_subreport.id is '������������� ������';
comment on column declaration_subreport.declaration_template_id is '������������� ������� ����������';
comment on column declaration_subreport.name is '������������ ����. ������';
comment on column declaration_subreport.ord is '���������� �����';
comment on column declaration_subreport.alias is '��� ����. ������';
comment on column declaration_subreport.blob_data_id is '����� JasperReports ��� ������������ ��������� ������������� �����';
comment on table declaration_subreport is '����. ������ ������ ������ ����������';

--------------------------------------------------------------------------------------------------------
create table lock_data_subscribers
(
  lock_key varchar2(1000 byte) not null,
  user_id  number(9)           not null
);

comment on table lock_data_subscribers is 'C����� �������������, ��������� ���������� �������� ��� �������� ����������';
comment on column lock_data_subscribers.lock_key is '���� ���������� �������, ����� ���������� �������� ��� �������, ����� ��������� ����������';
comment on column lock_data_subscribers.user_id is '������������� ������������, ������� ������� ����������';

--------------------------------------------------------------------------------------------------------
create table ifrs_data
(
  report_period_id number(9) not null,
  blob_data_id     varchar2(36)
);

comment on table ifrs_data is '���������� ��� ����';
comment on column ifrs_data.report_period_id is '�������� ������';
comment on column ifrs_data.blob_data_id is '���� ������ � ����������� ��� ����';
--------------------------------------------------------------------------------------------------------
create table configuration_email
(
  id          number(9)     not null,
  name        varchar2(200) not null,
  value       varchar2(200),
  description varchar2(1000)
);

comment on table configuration_email is '��������� �����';
comment on column configuration_email.id is '������������� ������';
comment on column configuration_email.name is '��� ���������';
comment on column configuration_email.value is '�������� ���������';
comment on column configuration_email.description is '�������� ���������';

--------------------------------------------------------------------------------------------------------
create table form_data_consolidation
(
  source_form_data_id number(9),
  target_form_data_id number(9) not null
);

comment on table form_data_consolidation is '�������� � ������������ ��������� ���� � ��������� �����';
comment on column form_data_consolidation.source_form_data_id is '������������� �� ���������';
comment on column form_data_consolidation.target_form_data_id is '������������� �� ���������';
--------------------------------------------------------------------------------------------------------

create table declaration_data_consolidation
(
  source_form_data_id        number(9),
  target_declaration_data_id number(9) not null
);

comment on table declaration_data_consolidation is '�������� � ������������ ��������� ���� � ����������';
comment on column declaration_data_consolidation.source_form_data_id is '������������� �� ���������';
comment on column declaration_data_consolidation.target_declaration_data_id is '������������� ���������� ���������';

--------------------------------------------------------------------------------------------------------
create table log_system_report
(
  blob_data_id varchar2(36) not null,
  type         number(1)    not null,
  sec_user_id  number(9)
);

comment on table log_system_report is '�������� ������� ������';
comment on column log_system_report.blob_data_id is '������������� ������� BLOB_DATA, � ������� �������� �����';
comment on column log_system_report.type is '��� �������� (0 - ��������� ������� ������, 1 - ��������� zip-����� ��� ������� ������)';
comment on column log_system_report.sec_user_id is '������������� ������������, ��������������� �������� ���� 1';

--------------------------------------------------------------------------------------------------------
create table tax_type
(
  id   char(1)       not null,
  name varchar2(256) not null
);

comment on table tax_type is '���������� ����� �������';
comment on column tax_type.id is '���������� ������������� ���� ������';
comment on column tax_type.name is '��� ������';

--------------------------------------------------------------------------------------------------------
create table form_data_ref_book
(
  form_data_id number(18) not null,
  ref_book_id  number(18) not null,
  record_id    number(18) not null
);

comment on table form_data_ref_book is '����� ����������� �� � ���������� ������������';
comment on column form_data_ref_book.form_data_id is '������������� ��������� ��������� �����';
comment on column form_data_ref_book.ref_book_id is '������������� �����������';
comment on column form_data_ref_book.record_id is '������������� ������ �����������';

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

comment on table log_clob_query is '����������� DDL/DML �������� �� ��';
comment on column log_clob_query.id is '������������� ������ (seq_log_query)';
comment on column log_clob_query.form_template_id is '������������� �������';
comment on column log_clob_query.sql_mode is 'DDL/DML';
comment on column log_clob_query.text_query is '����� �������';
comment on column log_clob_query.log_date is '����/����� ������ ��������� �������';
comment on column log_clob_query.session_id is '������������� ������ (seq_log_query_session)';

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

comment on table form_data_file is '����� ��������� �����';
comment on column form_data_file.form_data_id is '������������� ���������� ��������� �����';
comment on column form_data_file.blob_data_id is '���� ��������� �����';
comment on column form_data_file.user_name is '������ ��� ������������, ������������� ����';
comment on column form_data_file.user_department_name is '������������ ������������� ������������, ������������� ����';
comment on column form_data_file.note is '����������� � �����';

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

comment on table color is '���������� ������';
comment on column color.id is '������������� ������';
comment on column color.name is '������������ �����';
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

comment on table department_form_type_performer is '���������� ���������� ������������ ��� ������ ��-�������������';
comment on column department_form_type_performer.department_form_type_id is '������������� ����� ������������� � ������';
comment on column department_form_type_performer.performer_dep_id is '�����������';
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

comment on table ref_book_vzl_history is '������� ��������� ��������� ���';
comment on column ref_book_vzl_history.id is '������������� ������';
comment on column ref_book_vzl_history.jur_person is '���';
comment on column ref_book_vzl_history.category is '��������� ���';
comment on column ref_book_vzl_history.form_data_id is '��� �����';
comment on column ref_book_vzl_history.change_date is '���� ���������';
comment on column ref_book_vzl_history.state is '��� ���������';

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

comment on column form_search_result."ID" is '������������� ���������� ������';
comment on column form_search_result."SESSION_ID" is '������������� ������ � ������� ���������� �����';
comment on column form_search_result."FORM_DATA_ID" is '������������� ����� � ������� ���������� �����';
comment on column form_search_result."DATE" is '���� ���������� ������';
comment on column form_search_result."KEY" is '������ ������';

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

comment on column form_search_data_result."ID" is '������������� ���������� ������';
comment on column form_search_data_result."SESSION_ID" is '������������� ������ � ������� ���������� �����';
comment on column form_search_data_result."ROW_INDEX" is '����� ������ � �����';
comment on column form_search_data_result."COLUMN_INDEX" is '����� ������� � �����';
comment on column form_search_data_result."RAW_VALUE" is '�������� � ������ �����';
comment on column form_search_data_result."ORD" is '���������� �����';
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

comment on table department_change is '��������� ����������� "�������������"';
comment on column department_change.department_id is '������������� �������������';
comment on column department_change.log_date is '����/����� ��������� ������';
comment on column department_change.operationType is '��� �������� (0 - ��������, 1 - ���������, 2 - ��������)';
comment on column department_change.hier_level is '������� ������ � ��������';
comment on column department_change.name is '������������ �������������';
comment on column department_change.parent_id is '������������� ������������� �������������';
comment on column department_change.type is '��� ������������� (1 - ����, 2 - ��, 3 - ����, ���, 4 - ����������, 5 - �� ���������� � �����)';
comment on column department_change.shortname is '����������� ������������ �������������';
comment on column department_change.tb_index is '������ ���������������� �����';
comment on column department_change.sbrf_code is '��� ������������� � ������� ���������';
comment on column department_change.region is '������';
comment on column department_change.is_active is '����������� ������������� (0 - �� �����������, 1 - �����������)';
comment on column department_change.code is '��� �������������';
comment on column department_change.garant_use is '�������, ��� ������������ � ������ �������� (0 - �� ������������, 1 - ������������)';
comment on column department_change.sunr_use is '�������, ��� ������������ � �� ���� (0 - �� ������������, 1 - ������������)';
--------------------------------------------------------------------------------------------------------
create table ref_book_asnu (
  id       number(9) primary key,
  code     varchar2(4) not null,
  name     varchar2(100) not null,
  type     varchar2(255) not null
);

comment on table ref_book_asnu is '���������� ����';
comment on column ref_book_asnu.id is '�������������';
comment on column ref_book_asnu.code is '��� ����';
comment on column ref_book_asnu.name is '������������ ����';
comment on column ref_book_asnu.type is '��� ������';
--------------------------------------------------------------------------------------------------------
--                                      �� "����"
--------------------------------------------------------------------------------------------------------
create table ndfl_person (
  id                  number(18)        not null,
  declaration_data_id number(18)        not null,
  inp                 varchar2(30 char) not null,
  snils               varchar2(30 char),
  last_name           varchar2(60 char) not null,
  first_name          varchar2(60 char) not null,
  middle_name         varchar2(60 char),
  birth_day           date              not null,
  citizenship         varchar2(60 char) not null,
  inn_np              varchar2(12 char),
  inn_foreign         varchar2(12 char),
  id_doc_type         varchar2(60 char) not null,
  id_doc_number       varchar2(60 char) not null,
  status              varchar2(60 char) not null,
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

comment on table ndfl_person is '������ � ���������� ���� - ���������� ������';
comment on column ndfl_person.id is '����������� ����';
comment on column ndfl_person.declaration_data_id is '������������� ���������� � ������� ��������� ������';
comment on column ndfl_person.inp is '���������� ��� �������';
comment on column ndfl_person.snils is '��������� ����� ��������������� �������� �����';
comment on column ndfl_person.last_name is '�������';
comment on column ndfl_person.first_name is '���';
comment on column ndfl_person.middle_name is '��������';
comment on column ndfl_person.birth_day is '���� ��������';
comment on column ndfl_person.citizenship is '����������� (��� ������)';
comment on column ndfl_person.inn_np is '���  ����������� ����';
comment on column ndfl_person.inn_foreign is '���  ������������ ����������';
comment on column ndfl_person.id_doc_type is '��� ���� ���������';
comment on column ndfl_person.id_doc_number is '����� � ����� ���������';
comment on column ndfl_person.status is '������';
comment on column ndfl_person.post_index is '������';
comment on column ndfl_person.region_code is '��� �������';
comment on column ndfl_person.area is '�����';
comment on column ndfl_person.city is '�����';
comment on column ndfl_person.locality is '���������� �����';
comment on column ndfl_person.street is '�����';
comment on column ndfl_person.house is '���';
comment on column ndfl_person.building is '������';
comment on column ndfl_person.flat is '��������';
comment on column ndfl_person.country_code is '��� ������';
comment on column ndfl_person.address is '�����';
comment on column ndfl_person.additional_data is '�������������� ����������';

create sequence seq_ndfl_person start with 1000;
------------------------------------------------------------------------------------------------------
create table ndfl_person_income
(
  id                    number(18) not null,
  ndfl_person_id        number(18) not null,
  row_num               number(10) not null,
  income_code           varchar2(100 char),
  income_type           varchar2(100 char),

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
  payment_number        varchar2(20),
  tax_summ              number(10)
);

comment on table ndfl_person_income is '�������� � ������� ����������� ����';
comment on column ndfl_person_income.row_num is '���������� ����� ������';
comment on column ndfl_person_income.income_code is '��� ������';
comment on column ndfl_person_income.income_type is '������� ������';
comment on column ndfl_person_income.income_accrued_date is '���� ���������� ������';
comment on column ndfl_person_income.income_payout_date is '���� ������� ������';
comment on column ndfl_person_income.income_accrued_summ is '����� ������������ ������';
comment on column ndfl_person_income.income_payout_summ is '����� ������������ ������';
comment on column ndfl_person_income.total_deductions_summ is '����� ����� �������';
comment on column ndfl_person_income.tax_base is '��������� ����';
comment on column ndfl_person_income.tax_rate is '������ ������';
comment on column ndfl_person_income.tax_date is '���� ������';
comment on column ndfl_person_income.calculated_tax is '����� ������ �����������';
comment on column ndfl_person_income.withholding_tax is '����� ������ ����������';
comment on column ndfl_person_income.not_holding_tax is '����� ������, �� ���������� ��������� �������';
comment on column ndfl_person_income.overholding_tax is '����� ������, ������� ���������� ��������� �������';
comment on column ndfl_person_income.refound_tax is '����� ������������� ������';
comment on column ndfl_person_income.tax_transfer_date is '���� ������������ ������';
comment on column ndfl_person_income.payment_date is '���� ���������� ���������';
comment on column ndfl_person_income.payment_number is '����� ���������� ��������� ������������ ������ � ������';
comment on column ndfl_person_income.tax_summ is '����� ������ �������������';

create sequence seq_ndfl_person_income start with 1000;
------------------------------------------------------------------------------------------------------
create table ndfl_person_deduction
(
  id               number(18)        not null,
  ndfl_person_id   number(18)        not null,
  row_num          number(10)        not null,
  type_code        varchar2(3 char)  not null,
  notif_type       varchar2(2 char)  not null,
  notif_date       date              not null,
  notif_num        varchar2(20 char) not null,
  notif_source     varchar2(20 char) not null,
  notif_summ       number(20, 2),
  income_accrued   date              not null,
  income_code      varchar2(4 char)  not null,
  income_summ      number(20, 2)     not null,
  period_prev_date date,
  period_prev_summ number(20, 2),
  period_curr_date date              not null,
  period_curr_summ number(20, 2)     not null
);

comment on table ndfl_person_deduction is '�����������, ���������� � ������������� ��������� ������';
comment on column ndfl_person_deduction.row_num is '���������� ����� ������';
comment on column ndfl_person_deduction.type_code is '��� ������';

comment on column ndfl_person_deduction.notif_type is '��� ����������� (��������� � ����� �� ��������� �����)';
comment on column ndfl_person_deduction.notif_date is '���� ������ �����������';
comment on column ndfl_person_deduction.notif_num is '����� �����������, ��������������� ����� �� ������������� ��������� �����';
comment on column ndfl_person_deduction.notif_source is '��� ���������� ������, ��������� �����������';
comment on column ndfl_person_deduction.notif_summ is '����� � ������������ � ���������� �� �����';

comment on column ndfl_person_deduction.income_accrued is '���� ���������� ������';
comment on column ndfl_person_deduction.income_code is '��� ������';
comment on column ndfl_person_deduction.income_summ is '����� ������������ ������';

comment on column ndfl_person_deduction.period_prev_date is '���� ���������� ������ � ���������� �������';
comment on column ndfl_person_deduction.period_prev_summ is '����� ������ ���������� ������ � ���������� �������';
comment on column ndfl_person_deduction.period_curr_date is '���� ���������� ������ � ������� �������';
comment on column ndfl_person_deduction.period_curr_summ is '����� ������ ���������� ������ � ������� �������';

create sequence seq_ndfl_person_deduction start with 1000;
------------------------------------------------------------------------------------------------------
create table ndfl_person_prepayment
(
  id             number(18)        not null,
  ndfl_person_id number(18)        not null,
  row_num        number(10)        not null,
  summ           number(18),
  notif_num      varchar2(20 char) not null,
  notif_date     date              not null,
  notif_source   varchar2(20 char) not null
);

comment on table ndfl_person_prepayment is 'C������� � ������� � ���� ��������� ��������';
comment on column ndfl_person_prepayment.row_num is '���������� ����� ������';
comment on column ndfl_person_prepayment.summ is '����� �������������� ���������� �������';
comment on column ndfl_person_prepayment.notif_num is '����� �����������, ��������������� ����� �� ������������� ��������� �����';
comment on column ndfl_person_prepayment.notif_date is '���� ������ �����������';
comment on column ndfl_person_prepayment.notif_source is '��� ���������� ������, ��������� �����������';

create sequence seq_ndfl_person_prepayment start with 1000;
------------------------------------------------------------------------------------------------------
--  ������ �� ��������� ������� 1151111
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
------------------------------------------------------------------------------------------------------
create table raschsv_sv_sum1_tip
(
   id                 NUMBER(18)           not null,
   sum_vsego_per      NUMBER(17,2),
   sum_vsego_posl_3m  NUMBER(17,2),
   sum_1m_posl_3m     NUMBER(17,2),
   sum_2m_posl_3m     NUMBER(17,2),
   sum_3m_posl_3m     NUMBER(17,2)
);
create sequence seq_raschsv_sv_sum1_tip start with 1;
------------------------------------------------------------------------------------------------------
create table raschsv_obyaz_plat_sv
(
   id                 NUMBER(18)           not null,
   oktmo              VARCHAR2(11 CHAR)
);
create sequence seq_raschsv_obyaz_plat_sv start with 1;
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
------------------------------------------------------------------------------------------------------
create table raschsv_sv_ops_oms
(
   id                 NUMBER(18)           not null,
   raschsv_obyaz_plat_sv_id NUMBER(18)           not null,
   tarif_plat         VARCHAR2(2 CHAR)
);
create sequence seq_raschsv_sv_ops_oms start with 1;
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
------------------------------------------------------------------------------------------------------
create table raschsv_ops_oms_rasch_sum
(
   raschsv_ops_oms_rasch_sum_id NUMBER(18)           not null,
   raschsv_sv_sum1_tip_id NUMBER(18)           not null,
   node_name          VARCHAR2(20 CHAR)    not null
);
------------------------------------------------------------------------------------------------------
create table raschsv_ops_oms_rasch_kol
(
   raschsv_ops_oms_rasch_kol_id NUMBER(18)           not null,
   raschsv_kol_lic_tip_id NUMBER(18)           not null,
   node_name          VARCHAR2(20 CHAR)    not null
);
------------------------------------------------------------------------------------------------------
create table raschsv_oss_vnm
(
   id                 NUMBER(18)           not null,
   raschsv_obyaz_plat_sv_id NUMBER(18)           not null,
   priz_vypl          VARCHAR2(1 CHAR)
);
create sequence seq_raschsv_oss_vnm start with 1;
------------------------------------------------------------------------------------------------------
create table raschsv_upl_sv_prev
(
   id                 NUMBER(18)           not null,
   raschsv_oss_vnm_id NUMBER(18),
   node_name          VARCHAR2(20 CHAR)    not null,
   priznak            VARCHAR2(1 CHAR),
   sv_sum             NUMBER(17, 2)
);
create sequence seq_raschsv_upl_sv_prev start with 1;
------------------------------------------------------------------------------------------------------
create table raschsv_oss_vnm_kol
(
   raschsv_oss_vnm_id NUMBER(18)           not null,
   raschsv_kol_lic_tip_id NUMBER(18)           not null,
   node_name          VARCHAR2(20 CHAR)    not null
);
------------------------------------------------------------------------------------------------------
create table raschsv_oss_vnm_sum
(
   raschsv_oss_vnm_id NUMBER(18)           not null,
   raschsv_sv_sum1_tip_id NUMBER(18)           not null,
   node_name          VARCHAR2(20 CHAR)    not null
);
------------------------------------------------------------------------------------------------------
create table raschsv_rash_oss_zak
(
   id                 NUMBER(18)           not null,
   raschsv_obyaz_plat_sv_id NUMBER(18)           not null
);
create sequence seq_raschsv_rash_oss_zak start with 1;
------------------------------------------------------------------------------------------------------
create table raschsv_rash_oss_zak_rash
(
   id                 NUMBER(18)           not null,
   raschsv_rash_oss_zak_id NUMBER(18),
   node_name          VARCHAR2(20 CHAR)    not null,
   chisl_sluch        NUMBER(7),
   kol_vypl           NUMBER(7),
   pash_vsego         NUMBER(17,2),
   rash_fin_fb        NUMBER(17,2)
);
create sequence seq_raschsv_rash_oss_zak_rash start with 1;
------------------------------------------------------------------------------------------------------
create table raschsv_vypl_fin_fb
(
   id                 NUMBER(18)           not null,
   raschsv_obyaz_plat_sv_id NUMBER(18)           not null
);
create sequence seq_raschsv_vypl_fin_fb start with 1;
------------------------------------------------------------------------------------------------------
create table raschsv_vypl_prichina
(
   id                 NUMBER(18)           not null,
   raschsv_vypl_fin_fb_id NUMBER(18)           not null,
   node_name          VARCHAR2(20 CHAR)    not null,
   sv_vnf_uhod_inv    NUMBER(17, 2)
);
create sequence seq_raschsv_vypl_prichina start with 1;
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
------------------------------------------------------------------------------------------------------
create table raschsv_sv_prim_tarif9_1_427
(
   id                 NUMBER(18)           not null,
   raschsv_obyaz_plat_sv_id NUMBER(18)           not null
);
create sequence seq_raschsv_sv_prim_tarif9_427 start with 1;
------------------------------------------------------------------------------------------------------
create table raschsv_vyplat_it_427
(
   raschsv_sv_prim_tarif9_427_id NUMBER(18)           not null,
   raschsv_sv_sum1_tip_id NUMBER(18)           not null
);
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
------------------------------------------------------------------------------------------------------
create table raschsv_sv_prim_tarif2_2_425
(
   id                 NUMBER(18)           not null,
   raschsv_obyaz_plat_sv_id NUMBER(18)           not null
);
create sequence seq_raschsv_sv_prim_tarif2_425 start with 1;
------------------------------------------------------------------------------------------------------
create table raschsv_vyplat_it_425
(
   raschsv_sv_prim_tarif2_425_id NUMBER(18)           not null,
   raschsv_sv_sum1_tip_id NUMBER(18)           not null
);
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
   otchestvo          VARCHAR2(60 CHAR)
);
------------------------------------------------------------------------------------------------------
create table raschsv_sv_prim_tarif1_3_422
(
   id                 NUMBER(18)           not null,
   raschsv_obyaz_plat_sv_id NUMBER(18)           not null
);
create sequence seq_raschsv_sv_prim_tarif1_422 start with 1;
------------------------------------------------------------------------------------------------------
create table raschsv_sved_obuch
(
   id                 NUMBER(18)           not null,
   raschsv_sv_prim_tarif1_422_id NUMBER(18)           not null,
   raschsv_sv_sum1_tip_id NUMBER(18)           not null,
   unik_nomer         VARCHAR2(3 CHAR),
   familia            VARCHAR2(60 CHAR),
   imya               VARCHAR2(60 CHAR),
   otchestvo          VARCHAR2(60 CHAR),
   sprav_nomer        VARCHAR2(10 CHAR),
   sprav_data         DATE,
   sprav_node_name    VARCHAR2(20 CHAR)
);
create sequence seq_raschsv_sved_obuch start with 1;
------------------------------------------------------------------------------------------------------
create table raschsv_sv_reestr_mdo
(
   id                 NUMBER(18)           not null,
   raschsv_sved_obuch_id NUMBER(18),
   naim_mdo           VARCHAR2(1000 CHAR),
   nomer_zapis        VARCHAR2(28 CHAR)
);
create sequence seq_raschsv_sv_reestr_mdo start with 1;
------------------------------------------------------------------------------------------------------
create table raschsv_vyplat_it_422
(
   raschsv_sv_prim_tarif1_422_id NUMBER(18)           not null,
   raschsv_sv_sum1_tip_id NUMBER(18)           not null
);
------------------------------------------------------------------------------------------------------
create table declaration_subreport_params
(
  id                       number(9) not null,
  declaration_subreport_id number(9) not null,
  name                     varchar2(255 char) not null,
  alias                    varchar2(255 char) not null,
  ord                      number(9) not null,
  type                     char(1) not null,
  filter                   varchar2(1000 char)
);
comment on table declaration_subreport_params is '��������� ����. ������� ����������';
comment on column declaration_subreport_params.name is '������������ ���������';
comment on column declaration_subreport_params.alias is '��������� ��������� ��� ������� �� �������';
comment on column declaration_subreport_params.ord is '���������� ����� ���������';
comment on column declaration_subreport_params.type is '��� ������� (S - ������, N - �����, D - ����, R - ������)';
comment on column declaration_subreport_params.filter is '������� ���������� ��������� �����������';
------------------------------------------------------------------------------------------------------
