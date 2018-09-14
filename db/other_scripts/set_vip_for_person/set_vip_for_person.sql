set serveroutput on;
set verify off;
spool &1;

declare
  v_count_id number;
  v_fio varchar(400);
  v_vip number;
begin
  v_fio := 'Апланянц Аплан Апланардович';
  v_vip := 1;
  select count(id) into v_count_id from REF_BOOK_PERSON where upper(last_name || ' ' || first_name || ' ' || middle_name) = upper(v_fio);
  if v_count_id > 0 then
     DBMS_OUTPUT.PUT_LINE('Person FIO "' || v_fio || '" set VIP=' || v_vip || ' for:');
     for c in (select id, record_id from REF_BOOK_PERSON where upper(last_name || ' ' || first_name || ' ' || middle_name) = upper(v_fio))
     loop
         update REF_BOOK_PERSON set vip = v_vip where id = c.id;
         DBMS_OUTPUT.PUT_LINE('... record_id=' || c.record_id || ' id=' || c.id);
     end loop;
     commit;
  else
     DBMS_OUTPUT.PUT_LINE('Person FIO "' || v_fio || '"  not found');
  end if;
end;
/
exit;
