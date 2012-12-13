alter sequence SEQ_DATA_ROW increment by 9999 nocache;
select SEQ_DATA_ROW.nextval from dual;
alter sequence SEQ_DATA_ROW increment by 1 nocache;
declare
  LastValue integer;
begin
  loop
    select SEQ_DATA_ROW.currval into LastValue from dual;
    exit when LastValue >= 10000 - 1;
    select SEQ_DATA_ROW.nextval into LastValue from dual;
  end loop;
end;
/
alter sequence SEQ_DATA_ROW increment by 1 cache 20;
/
alter sequence SEQ_FORM_DATA increment by 9999 nocache;
select SEQ_FORM_DATA.nextval from dual;
alter sequence SEQ_FORM_DATA increment by 1 nocache;
declare
  LastValue integer;
begin
  loop
    select SEQ_FORM_DATA.currval into LastValue from dual;
    exit when LastValue >= 10000 - 1;
    select SEQ_FORM_DATA.nextval into LastValue from dual;
  end loop;
end;
/
alter sequence SEQ_FORM_DATA increment by 1 cache 20;
/
alter sequence SEQ_FORM_SCRIPT increment by 9999 nocache;
select SEQ_FORM_SCRIPT.nextval from dual;
alter sequence SEQ_FORM_SCRIPT increment by 1 nocache;
declare
  LastValue integer;
begin
  loop
    select SEQ_FORM_SCRIPT.currval into LastValue from dual;
    exit when LastValue >= 10000 - 1;
    select SEQ_FORM_SCRIPT.nextval into LastValue from dual;
  end loop;
end;
/
alter sequence SEQ_FORM_SCRIPT increment by 1 cache 20;
/
alter sequence SEQ_FORM_COLUMN increment by 9999 nocache;
select SEQ_FORM_COLUMN.nextval from dual;
alter sequence SEQ_FORM_COLUMN increment by 1 nocache;
declare
  LastValue integer;
begin
  loop
    select SEQ_FORM_COLUMN.currval into LastValue from dual;
    exit when LastValue >= 10000 - 1;
    select SEQ_FORM_COLUMN.nextval into LastValue from dual;
  end loop;
end;
/
alter sequence SEQ_FORM_COLUMN increment by 1 cache 20;