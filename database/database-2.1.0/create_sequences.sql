declare 
  v_count number;
begin
	select count(1) into v_count from user_sequences where lower(sequence_name)='seq_async_task';
	IF v_count=0 THEN
		EXECUTE IMMEDIATE 'create sequence seq_async_task start with 100000 increment by 100';
	END IF; 
	
	select count(1) into v_count from user_sequences where lower(sequence_name)='seq_decl_template_event_script';
	IF v_count=0 THEN
		EXECUTE IMMEDIATE 'create sequence seq_decl_template_event_script start with 1 increment by 1';
	END IF; 
	
	select count(1) into v_count from user_sequences where lower(sequence_name)='seq_decl_template_checks';
	IF v_count=0 THEN
		EXECUTE IMMEDIATE 'create sequence seq_decl_template_checks start with 1 increment by 1';
	END IF; 
end;
/