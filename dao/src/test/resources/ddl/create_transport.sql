/**
 * ������� ������������
 */
create table transport_okato (
	id number(9) not null,
	parent_id number(9),
	okato varchar(11) not null,
	name varchar(100) not null
);
alter table transport_okato add constraint transport_okato_ok primary key(id);
alter table transport_okato add constraint transport_okato_unique_okato unique (okato);
alter table transport_okato add constraint transport_okato_fk_parent_id foreign key (parent_id) references transport_okato(id);

comment on table transport_okato is '���� ����� � ������������� �����������';
comment on column transport_okato.id is '������������� ������';
comment on column transport_okato.parent_id is '������������� ������������ ������';
comment on column transport_okato.okato is '��� �����';
comment on column transport_okato.name is '������������ �������������� �����������';
---------------------------------------------------------------------------------------------------

create table transport_tax_rate (
	id number(9) not null,
	code varchar(10) not null,
	min_age number(9),
	max_age number(9),
	min_power number(9),
	max_power number(9),
	value number(9) not null
);
alter table transport_tax_rate add constraint transport_tax_rate_pk primary key (id);

comment on table transport_tax_rate is '������ ������������� ������';
comment on column transport_tax_rate.id is '��������� ���� (����� �.�.)';
comment on column transport_tax_rate.code is '��� ������������� ��������';
comment on column transport_tax_rate.min_age is '���� ������������� "��", ���';
comment on column transport_tax_rate.max_age is '���� ������������� "��", ���';
comment on column transport_tax_rate.min_power is '�������� "��", �.�.';
comment on column transport_tax_rate.max_power is '�������� "��", �.�.';
comment on column transport_tax_rate.value is '������, ���.';
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

comment on table transport_type_code is '��� ���� ������������� ��������';
comment on column transport_type_code.id is '������������� ������';
comment on column transport_type_code.parent_id is '������������� ������������ ������';
comment on column transport_type_code.code is '��� ����';
comment on column transport_type_code.name is '������������';

---------------------------------------------------------------------------------------------------

create table transport_unit_code
(
code number(9) not null,
name varchar2(128) not null, 
convention varchar2(16) not null
);

comment on table transport_unit_code is '���� ������ ��������� ��������� ���� �� ��������� ���� (�������)';
comment on column transport_unit_code.code is '��� ������ ���������';
comment on column transport_unit_code.name is '������������ ������� ���������';
comment on column transport_unit_code.convention is '�������� �����������';
---------------------------------------------------------------------------------------------------
create table transport_tax_benefit_code
(
code varchar2(5) not null,
name varchar2(256) not null,
regulation varchar(128) not null
);

alter table transport_tax_benefit_code add constraint transport_tax_benefit_code_pk primary key (code);

comment on table transport_tax_benefit_code is '���� ��������� �����';
comment on column transport_tax_benefit_code.code is '��� ��������� �����';
comment on column transport_tax_benefit_code.name is '������������ ������';
comment on column transport_tax_benefit_code.regulation is '���������';

---------------------------------------------------------------------------------------------------
create table transport_eco_class
(
code number(9) not null,
name varchar2(20) not null
);

alter table transport_eco_class add constraint transport_eco_class_pk primary key (code);

comment on table transport_eco_class is '������������� ������ ������������ �������';
comment on column transport_eco_class.code is '���';
comment on column transport_eco_class.name is '������������';