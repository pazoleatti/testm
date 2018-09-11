set serveroutput on;
set verify off;
spool &1;

declare
  v_count_id number;
begin
   select count(id) into v_count_id from &2..REF_BOOK_PERSON where record_id = &3;
   if v_count_id > 0 then
     if &4 in (0,1) then
      update &2..REF_BOOK_PERSON set VIP = &4 where record_id = &3;
      DBMS_OUTPUT.PUT_LINE('Person "' || &3 || '" set VIP="' || &4 || '" successfully for ' || SQL%ROWCOUNT || ' rows');
   	  commit;
     else
      DBMS_OUTPUT.PUT_LINE('Person VIP value must be 1 or 0');
     end if;
   else
      DBMS_OUTPUT.PUT_LINE('Person with record_id="' || &3 || '" not found');
   end if;
end;
/
exit;
