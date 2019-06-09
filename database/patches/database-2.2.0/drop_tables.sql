declare
	v_count number;
begin
	v_count := 0;
	select count(1) into v_count from user_tables where table_name = 'REF_BOOK_TARIFF_PAYER';
	if v_count>0 then
		dbms_output.put_line('Dropping table REF_BOOK_TARIFF_PAYER');
		execute immediate 'DROP TABLE REF_BOOK_TARIFF_PAYER';
		select count(1) into v_count from user_tables where table_name = 'REF_BOOK_TARIFF_PAYER';
		if v_count=0 then
			dbms_output.put_line('Table REF_BOOK_TARIFF_PAYER was dropped');
		end if;
	end if;
end;
/