declare 
  v_count number;
begin
	select count(1) into v_count from user_indexes where lower(index_name)='idx_decl_type_checks';
	IF v_count=0 THEN
		EXECUTE IMMEDIATE 'create index idx_decl_type_checks on decl_template_checks(declaration_type_id)';
	END IF; 
	
	select count(1) into v_count from user_indexes where lower(index_name)='idx_decl_template_checks';
	IF v_count=0 THEN
		EXECUTE IMMEDIATE 'create index idx_decl_template_checks on decl_template_checks(declaration_template_id)';
	END IF; 
	
	select count(1) into v_count from user_indexes where lower(index_name)='unq_dt_checks';
	IF v_count=0 THEN
		EXECUTE IMMEDIATE 'create unique index unq_dt_checks on decl_template_checks(declaration_type_id,declaration_template_id,check_code)';
	END IF; 
end;
/