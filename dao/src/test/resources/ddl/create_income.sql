﻿
/*������� ������������ - ����� �� �������*/

create table income_accounting_tax_code
(
  ID               number(9) not null,
  KNU              VARCHAR2(5) not null,
  NAME_OPERATION   VARCHAR2(3000) not null,
  BALANCE_ACCOUNT  VARCHAR2(5), 
  SIMVOL_OPU       VARCHAR2(8),
  RNU              VARCHAR2(7) not null  
);
alter table income_accounting_tax_code
  add constraint income_accounting_tax_code_PK primary key (ID);
comment on table income_accounting_tax_code is '������������� ������������ ������ �������������� ����� ����� ���������� �����';
comment on column income_accounting_tax_code.id is '��������� ���� (����� �.�.)';
comment on column income_accounting_tax_code.knu is '��� - ��� ���������� �����';
comment on column income_accounting_tax_code.name_operation is '������������ ��������';
comment on column income_accounting_tax_code.balance_account is '���������� ����';
comment on column income_accounting_tax_code.simvol_opu is '������ ���';
comment on column income_accounting_tax_code.rnu is '���';

----------------------------------------------------------------------------------------------------------------------------------
