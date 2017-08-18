-- Промежуточные таблицы настроек подразделений для импорта 
create table tmp_depart
(
  code varchar2(4 char),
  name varchar2(100 char),
  dep_id number
);

create table tmp_dep_params
(
  depcode varchar2(100 char),
  inn varchar2(100 char),
  tax_end varchar2(100 char),
  kpp varchar2(100 char),
  place varchar2(100 char),
  titname varchar2(255 char),
  oktmo varchar2(100 char),
  phone varchar2(100 char),
  sign varchar2(100 char),
  surname varchar2(100 char),
  name varchar2(100 char),
  lastname varchar2(100 char),
  docname varchar2(100 char),
  orgname varchar2(100 char),
  reorgcode varchar2(100 char),
  row_num number
);

