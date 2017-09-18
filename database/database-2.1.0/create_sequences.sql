declare 
  v_count number;
begin
	select count(1) into v_count from user_sequences where sequence_name='SEQ_ASYNC_TASK';
	IF v_count=0 THEN
		EXECUTE IMMEDIATE 'create sequence seq_async_task start with 100000 increment by 100';
	END IF; 
end;
/