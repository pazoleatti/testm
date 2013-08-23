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

comment on table dict_tax_period is '����, ������������ ��������� (��������) ������';
comment on column dict_tax_period.code is '���';
comment on column dict_tax_period.name is '������������';
comment on column dict_tax_period.I is '�������������� � ������ �� �������';
comment on column dict_tax_period.T is '�������������� � ������ �� ���������';
comment on column dict_tax_period.P is '�������������� � ������ �� ���������';
comment on column dict_tax_period.V is '�������������� � ������ ���';
comment on column dict_tax_period.D is '�������������� � ���';
---------------------------------------------------------------------------------------------
create table dict_region (
  code varchar2(2) not null,
  name varchar2(510) not null,
  okato varchar2(11),
  okato_definition varchar2(11)
);
alter table dict_region add constraint dict_region_pk primary key (code);
alter table dict_region add constraint dict_region_uniq_okato_def unique (okato_definition);

comment on table dict_region is '���� ��������� ���������� ���������';
comment on column dict_region.code is '���';
comment on column dict_region.name is '������������';
comment on column dict_region.okato is '��� �����';
comment on column dict_region.okato_definition is '������������ ����� ���� �����';
-------------------------------------------------------------------------------------------------------------------------------------------
create table form_type (
  id       number(9) not null,
  name     varchar2(600) not null,
  tax_type char(1) not null
);
alter table form_type add constraint form_type_pk primary key (id);
alter table form_type add constraint form_type_chk_taxtype check (tax_type in ('I', 'P', 'T', 'V', 'D'));

comment on table form_type is '���� ��������� ���� (��������)';
comment on column form_type.id is '�������������';
comment on column form_type.name is '������������';
comment on column form_type.tax_type is '��� ������ (I-�� �������, P-�� ���������, T-������������, V-���, D-���)';
---------------------------------------------------------------------------------------------------
create table tax_period (
  id number(9) not null,
  tax_type char(1) not null,
  start_date date not null,
  end_date date not null
);
alter table tax_period add constraint tax_period_pk primary key (id);
alter table tax_period add constraint tax_period_chk_taxtype check (tax_type in ('I', 'P', 'T', 'V', 'D'));

comment on table tax_period is '��������� �������';
comment on column tax_period.id is '������������� (��������� ����)';
comment on column tax_period.tax_type is '��� ������ (I-�� �������, P-�� ���������, T-������������, V-���, D-���)';
comment on column tax_period.start_date is '���� ������ (������������)';
comment on column tax_period.end_date is '���� ��������� (������������)';

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

comment on table form_template IS '�������� �������� ��������� ����';
comment on column form_template.data_rows is '��������������� ������ ����� � ������� XML';
comment on column form_template.id is '��������� ����';
comment on column form_template.is_active is '������� ����������';
comment on column form_template.type_id is '������������� ���� ��������� �����';
comment on column form_template.version is '������ ����� (��������� � ������ ����)';
comment on column form_template.edition is '����� �������� ������';
comment on column form_template.numbered_columns is '������� ����, ��� ������� ������ ���� �������������';
comment on column form_template.fixed_rows is '������� ������������� ������������� �����: 0 - ������������ ������������� ����� �����, 1 - ���� ����������� ��������� � ������� ������ �� �����.';
comment on column form_template.name is '������������ �����';
comment on column form_template.fullname is '������ ������������ �����';
comment on column form_template.code is '����� �����';
comment on column form_template.script is '������, ����������� ������-������ ��������� �����';
comment on column form_template.data_headers is '�������� ��������� �������';
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
  id            varchar2(36) not null,
  name          varchar2(200) null,
  data          blob not null,
  creation_date date not null,
  type          number(1) default 0 not null,
  data_size     number(9) not null
);
alter table blob_data add constraint blob_data_pk primary key(id);
alter table blob_data add constraint blob_data_chk_type check (type in (0, 1));

comment on table blob_data is '�������� ���������';
comment on column blob_data.id is '���������� �������������';
comment on column blob_data.name is '�������� �����';
comment on column blob_data.data is '�������� ������';
comment on column blob_data.creation_date is '���� ��������';
comment on column blob_data.type is '��� ������ (0 - ����������, 1 - ���������)';
comment on column blob_data.data_size is '������ ����� � ������';
----------------------------------------------------------------------------------------------------
create table ref_book (
  id number(18,0) not null,
  name varchar2(200) not null,
  script_id varchar2(36)
);

alter table ref_book add constraint ref_book_pk primary key (id);
alter table ref_book add constraint ref_book_fk_script_id foreign key (script_id) references blob_data(id);

comment on table ref_book is '����������';
comment on column ref_book.id is '���������� �������������';
comment on column ref_book.name is '�������� �����������';
comment on column ref_book.script_id is '������������� ���������� �������';
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
  width number(9) default 15 not null
);

alter table ref_book_attribute add constraint ref_book_attr_pk primary key (id);

alter table ref_book_attribute add constraint ref_book_attr_chk_visible check (visible in (0, 1));
alter table ref_book_attribute add constraint ref_book_attr_chk_type check (type in (1, 2, 3, 4));
alter table ref_book_attribute add constraint ref_book_attr_chk_alias check (lower(alias) <> 'record_id' and lower(alias) <> 'row_number_over');
alter table ref_book_attribute add constraint ref_book_attr_chk_precision check (precision >= 0 and precision <=10);
alter table ref_book_attribute add constraint ref_book_attr_chk_number_type check ((type <> 2 and precision is null) or (type = 2 and not (precision is null)));
alter table ref_book_attribute add constraint ref_book_attribute_uniq_ord unique (ref_book_id, ord);
alter table ref_book_attribute add constraint ref_book_attribute_uniq_alias unique (ref_book_id, alias);

alter table ref_book_attribute add constraint ref_book_attr_fk_ref_book_id foreign key (ref_book_id) references ref_book (id);
alter table ref_book_attribute add constraint ref_book_attr_fk_reference_id foreign key (reference_id) references ref_book (id);
alter table ref_book_attribute add constraint ref_book_attr_fk_attribute_id foreign key (attribute_id) references ref_book_attribute (id);

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
------------------------------------------------------------------------------------------------------
create table ref_book_record (
  id number(18) not null,
  record_id number(9) not null,
  ref_book_id number(18) not null,
  version date not null,
  status number(1) default 0 not null
);

alter table ref_book_record add constraint ref_book_record_pk primary key (id);

alter table ref_book_record add constraint ref_book_record_chk_status check (status in (0, -1));

alter table ref_book_record add constraint ref_book_record_fk_ref_book_id foreign key (ref_book_id) references ref_book (id);

create unique index i_ref_book_record_refbookid on ref_book_record(ref_book_id, record_id, version);

create sequence seq_ref_book_record start with 100000 increment by 100;
create sequence seq_ref_book_record_row_id start with 100000;

comment on table ref_book_record is '������ �����������';
comment on column ref_book_record.id is '���������� �������������';
comment on column ref_book_record.record_id is '������������� ������ �����������. ����� ����������� � ������ ������';
comment on column ref_book_record.ref_book_id is '������ �� ����������, � �������� ��������� ������';
comment on column ref_book_record.version is '������. ���� ������������ ������';
comment on column ref_book_record.status is '������ ������ (0-������� ������; -1-���������� �� ��������)';
------------------------------------------------------------------------------------------------------
create table ref_book_value (
  record_id number(18) not null,
  attribute_id number(18) not null,
  string_value varchar2(4000),
  number_value number(27,10),
  date_value date,
  reference_value number(18)
);

alter table ref_book_value add constraint ref_book_value_pk primary key (record_id, attribute_id);

alter table ref_book_value add constraint ref_book_value_fk_record_id foreign key (record_id) references ref_book_record (id);
alter table ref_book_value add constraint ref_book_value_fk_attribute_id foreign key (attribute_id) references ref_book_attribute (id);

comment on table ref_book_value is '�������� ������ �����������';
comment on column ref_book_value.record_id is '������ �� ������ �����������';
comment on column ref_book_value.attribute_id is '������ �� ������� �����������';
comment on column ref_book_value.string_value is '��������� ��������';
comment on column ref_book_value.number_value is '��������� ��������';
comment on column ref_book_value.date_value is '�������� ����';
comment on column ref_book_value.reference_value is '�������� ������';
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
  attribute_id number(18),
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

comment on table form_column is '�������� �������� ��������� ����';
comment on column form_column.alias is '��� �������, ������������ � ����������';
comment on column form_column.form_template_id is '������������� ������� ��������� �����';
comment on column form_column.group_name is '�������� ������ ��������';
comment on column form_column.id is '��������� ����';
comment on column form_column.name is '�������� �������';
comment on column form_column.ord is '���������� �����';
comment on column form_column.precision is '���������� ������ ����� ������� (������ ��� �������� ��������)';
comment on column form_column.type is '��� ������� (S - ������, N � �����, D � ����, R - ������)';
comment on column form_column.width is '������ (� ��������)';
comment on column form_column.checking is '������� ������������ �������';
comment on column form_column.attribute_id is '��� ������������� �������� ��� ��������-������';
comment on column form_column.format is '������';
comment on column form_column.filter is '������� ���������� ��������� �����������';
comment on column form_column.max_length IS '������������ ����� ������';
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

comment on table department is '������������� �����';
comment on column department.id is '������������� ������';
comment on column department.name is '������������ �������������';
comment on column department.parent_id is '������������� ������������� �������������';
comment on column department.type is '��� ������������� (1 - ����, 2- ��, 3- ����, 4- ���, 5- ���, 6-����)';
comment on column department.shortname is '����������� ������������ �������������';
comment on column department.dict_region_id is '��� �������� ��';
comment on column department.tb_index is '������ ���������������� �����';
comment on column department.sbrf_code is '��� ������������� � ������� ���������';

alter table department add constraint department_chk_id check ((type= 1 and id = 1) or (type <> 1 and id <> 1));
alter table department add constraint department_chk_parent_id check ((type = 1 and parent_id is null) or (type <> 1 and parent_id is not null));
---------------------------------------------------------------------------------------------------
create table report_period (
  id number(9) not null,
  name varchar2(50) not null,
  is_active number(1) default 1,
  months  number(2) not null,
  tax_period_id number(9) not null,
  ord      number(2) not null,
  department_id number(15),
  is_balance_period number(1) default 0,
  dict_tax_period_id number(18) not null
);

alter table report_period add constraint report_period_pk primary key(id);
alter table report_period add constraint report_period_fk_taxperiod foreign key (tax_period_id) references tax_period (id);
--alter table report_period add constraint report_period_chk_active check (is_active in (0, 1));
--alter table report_period add constraint report_period_chk_balance check (is_balance_period in (0, 1));
--alter table report_period add constraint report_period_fk_department_id foreign key (department_id) references department(id);
alter table report_period add constraint report_period_fk_dtp_id foreign key (dict_tax_period_id) references ref_book_record(id);

comment on table report_period is '�������� �������';
comment on column report_period.id is '��������� ����';
comment on column report_period.name is '������������ �������';
comment on column report_period.is_active is '������� ����������';
comment on column report_period.months is '���������� ������� � �������';
comment on column report_period.tax_period_id is '��������� ������';
comment on column report_period.ord is '����� ��������� ������� � ���������';
comment on column report_period.is_balance_period is '������� ����, ��� ������ �������� �������� ����� ��������';
comment on column report_period.department_id is '�������������';
comment on column report_period.dict_tax_period_id is '������ �� ���������� �������� ��������';

create sequence seq_report_period start with 100;
----------------------------------------------------------------------------------------------------
create table income_101 (
  id                     number(18) not null,
  report_period_id       number(9) not null,
  account                varchar2(255 char) not null,
  income_debet_remains   number(22,4),
  income_credit_remains  number(22,4),
  debet_rate             number(22,4),
  credit_rate            number(22,4),
  outcome_debet_remains  number(22,4),
  outcome_credit_remains number(22,4),
  account_name varchar2(255 char)
);

alter table income_101 add constraint income_101_pk primary key (id);
alter table income_101 add constraint income_101_fk_report_period_id foreign key (report_period_id) references report_period(id);

comment on table income_101 is '��������� ��������� (����� 0409101-��)';
comment on column income_101.id is '��� ������';
comment on column income_101.report_period_id is '������������� ��������� �������';
comment on column income_101.account is '����� �����';
comment on column income_101.income_debet_remains is '�������� ������� �� ������';
comment on column income_101.income_credit_remains is '�������� ������� �� �������';
comment on column income_101.debet_rate is '������� �� ������';
comment on column income_101.credit_rate is '������� �� �������';
comment on column income_101.outcome_debet_remains is '��������� ������� �� ������';
comment on column income_101.outcome_credit_remains is '��������� ������� �� �������';
comment on column income_101.account_name is '�������� �����';

create sequence seq_income_101 start with 1;
-------------------------------------------------------------------------------------------------------------------------------------------
create table income_102 (
  id               number(18) not null,
  report_period_id number(9) not null,
  opu_code         varchar2(25 char) not null,
  total_sum        number(22,4),
  item_name        varchar2(255 char)
  );

alter table income_102 add constraint income_102_pk primary key (id);
alter table income_102 add constraint income_102_fk_report_period_id foreign key (report_period_id) references report_period(id);

comment on table income_102 is '����� � �������� � ������� (����� 0409102-��)';
comment on column income_102.id is '��� ������';
comment on column income_102.report_period_id is '������������� ��������� �������';
comment on column income_102.opu_code is '��� ���';
comment on column income_102.total_sum is '�����';
comment on column income_102.item_name is '������������ ������';

create sequence seq_income_102 start with 1;
---------------------------------------------------------------------------------------------------
create table declaration_type (
  id       number(9) not null,
  tax_type    char(1) not null,
  name      varchar(80) not null
);
alter table declaration_type add constraint declaration_type_pk primary key (id);
alter table declaration_type add constraint declaration_type_chk_tax_type check (tax_type in ('I', 'P', 'T', 'V', 'D'));

comment on table declaration_type is ' ���� ����������';
comment on column declaration_type.id is '������������� (��������� ����)';
comment on column declaration_type.tax_type is '��� ������ (I-�� �������, P-�� ���������, T-������������, V-���, D-���)';
comment on column declaration_type.name is '������������';
-----------------------------------------------------------------------------------------------------------------------------------
create table department_declaration_type (
  id         number(9) not null,
  department_id    number(9) not null,
  declaration_type_id number(9) not null
);
alter table department_declaration_type add constraint dept_decl_type_pk primary key (id);
alter table department_declaration_type add constraint dept_decl_type_fk_dept foreign key (department_id) references department (id);
alter table department_declaration_type add constraint dept_decl_type_fk_decl_type foreign key (declaration_type_id) references declaration_type (id);

comment on table department_declaration_type is '�������� � �����������, � �������� ����� �������� � �������������';
comment on column department_declaration_type.id is '������������� (��������� ����)';
comment on column department_declaration_type.department_id is '������������� �������������';
comment on column department_declaration_type.declaration_type_id is '��� ����������';

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

comment on table declaration_template is '������� ��������� ����������';
comment on column declaration_template.id is '������������� (��������� ����)';
comment on column declaration_template.edition is '����� ��������';
comment on column declaration_template.version is '������';
comment on column declaration_template.is_active is '������� ����������';
comment on column declaration_template.create_script is '������ ������������ ����������';
comment on column declaration_template.jrxml is '����� JasperReports ��� ������������ ��������� ������������� �����';
comment on column declaration_template.jasper is '���������������� ����� JasperReports ��� ������������ ��������� ������������� �����';
comment on column declaration_template.declaration_type_id is '��� ����������';

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

comment on table declaration_data is '��������� ����������';
comment on column declaration_data.id is '������������� (��������� ����)';
comment on column declaration_data.declaration_template_id is '������ �� ������ ����������';
comment on column declaration_data.report_period_id is '�������� ������';
comment on column declaration_data.department_id is '�������������';
comment on column declaration_data.data is '������ ���������� � ������� ������������ (XML) ';
comment on column declaration_data.is_accepted is '������� ����, ��� ���������� �������';
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
  return_sign number(1) not null,
  period_order number(2)
);
alter table form_data add constraint form_data_pk primary key (id);
alter table form_data add constraint form_data_fk_form_templ_id foreign key (form_template_id) references form_template(id);
alter table form_data add constraint form_data_fk_dep_id foreign key (department_id) references department(id);
alter table form_data add constraint form_data_fk_period_id foreign key (report_period_id) references report_period(id);
alter table form_data add constraint form_data_chk_kind check(kind in (1,2,3,4,5));
alter table form_data add constraint form_data_chk_state check(state in (1,2,3,4));
alter table form_data add constraint form_data_chk_return_sign check(return_sign in (0,1));

comment on table form_data is '������ �� ��������� ������';
comment on column form_data.id is '��������� ����';
comment on column form_data.form_template_id is '������������� ������� �����';
comment on column form_data.department_id is '������������� �����������';
comment on column form_data.state is '��� ���������';
comment on column form_data.kind is '��� ��������� ����� (1 - ���������, 2 - �����������������, 3 - �������, 4 - ����� ���, 5 - ��������)';
comment on column form_data.report_period_id is '������������� ��������� �������';
comment on column form_data.return_sign is '���� �������� (0 - ������� �����; 1 - ����� ���������� �� ������������ �������)';
comment on column form_data.period_order is '��������� �� ����������� ��������� ����� � ������ ��������� �������. ���������� ���, ��������, �������� ���� � ������ ������������ ��������� �������';

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

comment on table form_data_signer is '���������� ��������� ����';
comment on column form_data_signer.id is '������������ ������ (��������� ����)';
comment on column form_data_signer.form_data_id is '������������� ��������� �����';
comment on column form_data_signer.name is '���';
comment on column form_data_signer.position is '���������';
comment on column form_data_signer.ord is '����� ���������� �� �������';

create sequence seq_form_data_signer start with 10000;
---------------------------------------------------------------------------------------------------
create table form_data_performer (
  form_data_id number(18) not null,
  name varchar2(200) not null,
  phone varchar2(20)
);
alter table form_data_performer add constraint form_data_performer_pk primary key (form_data_id);
alter table form_data_performer add constraint formdata_performer_fk_formdata foreign key (form_data_id) references form_data (id) on delete cascade;

comment on table form_data_performer is '����������� ��������� �����';
comment on column form_data_performer.form_data_id is '��������� ����';
comment on column form_data_performer.name is '��� �����������';
comment on column form_data_performer.phone is '�������';
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

comment on table data_row is '������ ������ ��������� ����';
comment on column data_row.alias is '������������� ������';
comment on column data_row.form_data_id is '������ �� ������� � FORM_DATA';
comment on column data_row.id is '��� ������ ��� ������� �� ��������';
comment on column data_row.ord is '����� ������ � �����';
comment on column data_row.type is '��� ������ (0 - �������������� ������, 1 - ������ ���������, -1 - ������ �������)';

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

comment on table cell_style is '�������� ������ � ������� ��������� �����';
comment on column cell_style.row_id is '������������� ������';
comment on column cell_style.column_id is '������������� �������';
comment on column cell_style.style_id is '������������� �����';
---------------------------------------------------------------------------------------------------
create table cell_editable(
  row_id number(18) not null,
  column_id number(9) not null
);
alter table cell_editable add constraint cell_editable_pk primary key (row_id, column_id);
alter table cell_editable add constraint cell_editable_fk_data_row foreign key (row_id) references data_row (id) on delete cascade;
alter table cell_editable add constraint cell_editable_fk_form_column foreign key (column_id) references form_column (id);

comment on table cell_editable is '���������� � ������������� ������� ��������� �����';
comment on column cell_editable.row_id is '������������� ������ ��������� �����';
comment on column cell_editable.column_id is '������������� ������� ��������� �����';
---------------------------------------------------------------------------------------------------
create table numeric_value (
  row_id number(18) not null,
  column_id number(9) not null,
  value     decimal(27, 10)
);
alter table numeric_value add constraint numeric_value_pk primary key (row_id, column_id);
alter table numeric_value add constraint numeric_value_fk_column_id foreign key (column_id) references form_column(id);
alter table numeric_value add constraint numeric_value_fk_row_id foreign key (row_id) references data_row(id) on delete cascade;
comment on table numeric_value is '�������� �������� ��������� ����';
comment on column numeric_value.column_id is '������������� �������';
comment on column numeric_value.row_id is '������������� ������';
comment on column numeric_value.value is '��������';
---------------------------------------------------------------------------------------------------
create table string_value (
  row_id number(18) not null,
  column_id number(9) not null,
  value     varchar2(2000 char)
);
alter table string_value add constraint string_value_pk primary key (row_id, column_id);
alter table string_value add constraint string_value_fk_column_id foreign key (column_id) references form_column(id);
alter table string_value add constraint string_value_fk_row_id foreign key (row_id) references data_row(id) on delete cascade;
comment on table string_value is '��������� �������� ��������� ����';
comment on column string_value.column_id is '������������� �������';
comment on column string_value.row_id is '������������� ������';
comment on column string_value.value is '��������';
---------------------------------------------------------------------------------------------------
create table date_value (
  row_id number(18) not null,
  column_id number(9) not null,
  value date
);
alter table date_value add constraint date_value_pk primary key (row_id, column_id);
alter table date_value add constraint date_value_fk_column_id foreign key (column_id) references form_column(id);
alter table date_value add constraint date_value_fk_row_id foreign key (row_id) references data_row(id) on delete cascade;
comment on table date_value is '�������� ��������� ���� ���� ����';
comment on column date_value.column_id is '������������� �������';
comment on column date_value.row_id is '������������� ������';
comment on column date_value.value is '��������';
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

comment on table department_form_type is '����� ������������� ����� � ������';
comment on column department_form_type.id is '��������� ����';
comment on column department_form_type.department_id is '������������� �������������';
comment on column department_form_type.form_type_id is '������������� ���� ��������� �����';
comment on column department_form_type.kind is '��� ��������� ����� (1-���������, 2-�����������������, 3-�������, 4-����� ���, 5-��������)';

create sequence seq_department_form_type start with 10000;
---------------------------------------------------------------------------------------------------
create table declaration_source (
  department_declaration_type_id number(9) not null,
  src_department_form_type_id   number(9) not null
);
alter table declaration_source add constraint declaration_source_pk primary key (department_declaration_type_id,src_department_form_type_id );
alter table declaration_source add constraint decl_source_fk_dept_decltype foreign key (department_declaration_type_id) references department_declaration_type (id);
alter table declaration_source add constraint decl_source_fk_dept_formtype foreign key (src_department_form_type_id) references department_form_type (id);

comment on table declaration_source is '���������� � ������-���������� ������ ��� ���������� ������ �����';
comment on column declaration_source.department_declaration_type_id is '������������� ��������� ���� ���������� � �������������, ��� �������� ������� ��������';
comment on column declaration_source.src_department_form_type_id is '������������� ��������� ���� � ���� �����, � ����� �������������, ������� �������� ���������� ������ ��� ����������';
----------------------------------------------------------------------------------------------------
create table form_data_source (
  department_form_type_id number(9) not null,
  src_department_form_type_id number(9) not null
);
alter table form_data_source add constraint form_data_source_pk primary key (department_form_type_id, src_department_form_type_id);
alter table form_data_source add constraint form_data_source_fk_dep_id foreign key (department_form_type_id) references department_form_type(id);
alter table form_data_source add constraint form_data_source_fk_src_dep_id foreign key (src_department_form_type_id) references department_form_type(id);
comment on table form_data_source is '���������� �� ���������� ������ ��� ������������ ����������������� � ������� ���������� ����';
comment on column form_data_source.department_form_type_id is '������������� ��������� ����, ���� ����� � �������������, ��� �������� ������ �������� ������';
comment on column form_data_source.src_department_form_type_id is '������������� ��������� ����, ���� ����� � �������������, ������� �������� ���������� ������';
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

comment on table sec_user is '������������ �������';
comment on column sec_user.id is '��������� ����';
comment on column sec_user.login is '����� ������������';
comment on column sec_user.name is '������ ��� ������������';
comment on column sec_user.department_id is '������������� �������������';
comment on column sec_user.is_active is '������� ����������� ������������';
comment on column sec_user.email is '����� ����������� �����';

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

comment on table object_lock is '�������� � ���������������� ����������� ��������';
comment on column object_lock.object_id is '������������� �������';
comment on column object_lock.class is '��� ������ �������';
comment on column object_lock.user_id is '������������ ������������, ���������������� ������';
comment on column object_lock.lock_time is '����� ����������';
-------------------------------------------------------------------------------------------------------------------------------------
create table sec_role (
  id number(9) not null,
  alias varchar(20) not null,
  name varchar(50) not null
);
alter table sec_role add constraint sec_role_pk primary key (id);
alter table sec_role add constraint sec_role_uniq_alias unique (alias);

comment on table sec_role is '��������� ����';
comment on column sec_role.id is '��������� ����';
comment on column sec_role.alias is '��� ���� (������������� �������������)';
comment on column sec_role.name is '������������ ����';
---------------------------------------------------------------------------------------------------
create table sec_user_role (
  user_id number(9) not null,
  role_id number(9) not null
);

alter table sec_user_role add constraint sec_user_role_pk primary key (user_id, role_id);
alter table sec_user_role add constraint sec_user_role_fk_user_id foreign key (user_id) references sec_user(id);
alter table sec_user_role add constraint sec_user_role_fk_role_id foreign key (role_id) references sec_role(id);

comment on table sec_user_role is '�������� ����� � �������������';
comment on column sec_user_role.user_id is '������������� ������������';
comment on column sec_user_role.role_id is '������������� ����';
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

comment on table cell_span_info is '���������� �� ����������� ����� � ��������� �����';
comment on column cell_span_info.row_id is '������������� ������';
comment on column cell_span_info.column_id is '������������� �������';
comment on column cell_span_info.colspan is '����� �����, ������� ������ ���� ���������� �� �����������';
comment on column cell_span_info.rowspan is '����� �����, ������� ������ ���� ���������� �� ���������';
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

comment on table log_business is '������ ������� ��������� ����\����������';

comment on column log_business.id is '��� ������';
comment on column log_business.log_date is '���� �������';
comment on column log_business.event_id is '��� ������� (1 - �������,2 - �������,3 - ����������,4 - ��������,5 - ���������,6 - ���������,7 - ������ ������,101 - ���������,102 - ������� �� \����������\ � \�������\,103 - ������� �� \����������\,104 - ������� �� \�������\ � \����������\,105 - ������� �� \�������\,106 - ������� �� \�������\ � \�������\,107 - �����������,108, ������� �� \������������\ � \�������\,109, ������� �� \������������\,110, ������� �� \�������\ � \������������\,203 - ����� ������� �� \����������\,204 - ����� ������� �� \�������\ � \����������\,205 - ����� ������� �� \�������\,206 - ����� ������� �� \�������\ � \�������\,207 - ����� ������� �� \"������������\,301 - �������� ������,303 - ������� ������,302 - ��������)';
comment on column log_business.user_id is '��� ������������';
comment on column log_business.roles is '������ ����� ������������';
comment on column log_business.declaration_data_id is '��� ����������';
comment on column log_business.form_data_id is '��� ��������� �����';
comment on column log_business.note is '����� ���������';
comment on column log_business.user_department_id is '��� ������������� ������������';

CREATE SEQUENCE seq_log_business;
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

comment on table log_system is  '��������� ������';

comment on column log_system.id is '��� ������';
comment on column log_system.log_date is '���� �������';
comment on column log_system.ip is 'IP-����� ������������';
comment on column log_system.event_id is '��� ������� (1 - �������,2 - �������,3 - ����������,4 - ��������,5 - ���������,6 - ���������,7 - ������ ������,101 - ���������,102 - ������� �� \����������\ � \�������\,103 - ������� �� \����������\,104 - ������� �� \�������\ � \����������\,105 - ������� �� \�������\,106 - ������� �� \�������\ � \�������\,107 - �����������,108 - ������� �� \������������\ � \�������\,109 - ������� �� \������������\,110 - ������� �� \�������\ � \������������\,203 - ����� ������� �� \����������\,204 - ����� ������� �� \�������\ � \����������\,205 - ����� ������� �� \�������\,206 - ����� ������� �� \�������\ � \�������\,207 - ����� ������� �� \"������������\,301 - �������� ������,303 - ������� ������,302 - ��������)';
comment on column log_system.user_id is '��� ������������';
comment on column log_system.roles is '������ ����� ������������';
comment on column log_system.department_id is '��� ������������� ��\����������';
comment on column log_system.report_period_id is '��� ��������� �������';
comment on column log_system.declaration_type_id is '��� ���� ����������';
comment on column log_system.form_type_id is '��� ���� ��������� �����';
comment on column log_system.form_kind_id is '��� ���� ��������� ����� (1,2,3,4,5)';
comment on column log_system.note is '����� ���������';
comment on column log_system.user_department_id is '��� ������������� ������������';

create sequence seq_log_system start with 10000;
------------------------------------------------------------------------------------------------------

create table department_report_period (
  department_id       number(9) not null,
  report_period_id    number(9) not null,
  is_active           number(1) not null,
  is_balance_period   number(1) default 0 not null
);

alter table department_report_period add constraint department_report_period_pk primary key (department_id, report_period_id);

alter table department_report_period add constraint dep_rep_per_chk_is_active check (is_active in (0, 1));
alter table department_report_period add constraint dep_rep_per_chk_is_balance_per check (is_balance_period in (0, 1));

comment on table department_report_period is  '�������� �������� �������� � ��������������';

comment on column department_report_period.department_id is '��� �������������';
comment on column department_report_period.report_period_id is '��� ��������� �������';
comment on column department_report_period.is_active is '������� ���������� (0 - ������ ������, 1 - ������ ������)';
comment on column department_report_period.is_balance_period is '������� ����, ��� ������ �������� �������� ����� �������� (0 - ������� ������, 1 - ������ ����� ��������)';

alter table department_report_period add constraint dep_rep_per_fk_department_id foreign key (department_id) references DEPARTMENT (id);
alter table department_report_period add constraint dep_rep_per_fk_rep_period_id foreign key (report_period_id) references REPORT_PERIOD (id);

------------------------------------------------------------------------------------------------------
create index i_department_parent_id on department(parent_id);
create index i_data_row_form_data_id on data_row(form_data_id);
create index i_form_data_report_period_id on form_data(report_period_id);
create index i_form_data_form_template_id on form_data(form_template_id);
create index i_form_data_department_id on form_data(department_id);
create index i_form_data_kind on form_data(kind);
create index i_form_data_signer_formdataid on form_data_signer(form_data_id);
create index i_ref_book_value_string on ref_book_value(string_value);
------------------------------------------------------------------------------------------------------

