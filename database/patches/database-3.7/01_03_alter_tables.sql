-- 3.7-dnovikov-22 https://jira.aplana.com/browse/SBRFNDFL-7092 - Дата создания и создатель формы сделал по-нормальному
DECLARE
	v_run_condition number(1);
	v_task_name varchar2(128):='alter_tables block #1 - alter table declaration_data';  
BEGIN
	select decode(count(*),0,1,0) into v_run_condition from user_tab_columns where TABLE_NAME='DECLARATION_DATA' and COLUMN_NAME='CREATED_DATE';
	IF v_run_condition=1 THEN
		EXECUTE IMMEDIATE 'alter table declaration_data add created_date date default sysdate';
		EXECUTE IMMEDIATE 'update declaration_data dd set
			  created_date = nvl((select log_date from log_business where declaration_data_id = dd.id and event_id = 1), sysdate)';
		EXECUTE IMMEDIATE 'alter table declaration_data modify (created_date not null)';
		EXECUTE IMMEDIATE 'comment on column declaration_data.created_date is ''Дата создания формы''';
		dbms_output.put_line(v_task_name||'[INFO (created date)]:'||' Success');
	ELSE
		dbms_output.put_line(v_task_name||'[WARNING (created date)]:'||' changes had already been implemented');
	END IF;

	select decode(count(*),0,1,0) into v_run_condition from user_tab_columns where TABLE_NAME='DECLARATION_DATA' and COLUMN_NAME='CREATED_BY';
	IF v_run_condition=1 THEN
		EXECUTE IMMEDIATE 'alter table declaration_data add created_by number(18)';
		EXECUTE IMMEDIATE 'alter table declaration_data add constraint declaration_fk_created_by foreign key (created_by) references sec_user (id)';
		EXECUTE IMMEDIATE 'update declaration_data dd set
			  created_by = nvl((
			    select u.id from log_business lb
			    join sec_user u on u.login = lb.user_login
			    where lb.declaration_data_id = dd.id and lb.event_id = 1
			  ), 0)';
		EXECUTE IMMEDIATE 'alter table declaration_data modify (created_by not null)';
		EXECUTE IMMEDIATE 'comment on column declaration_data.created_by is ''Ид пользователя, создавшего форму''';
		dbms_output.put_line(v_task_name||'[INFO (created by)]:'||' Success');
	ELSE
		dbms_output.put_line(v_task_name||'[WARNING (created by)]:'||' changes had already been implemented');
	END IF;

	
EXCEPTION
	when OTHERS then
		dbms_output.put_line(v_task_name||'[FATAL]:'||sqlerrm);	
END;
/

--3.7-dnovikov-20 https://jira.aplana.com/browse/SBRFNDFL-7092 - Ссылка на протокол операций в истории изменений
DECLARE
	v_run_condition number(1);
	v_task_name varchar2(128):='alter_tables block #2 - alter table log_business';  
BEGIN
	select decode(count(*),0,1,0) into v_run_condition from user_tab_columns where TABLE_NAME='LOG_BUSINESS' and COLUMN_NAME='LOG_ID';
	IF v_run_condition=1 THEN
		EXECUTE IMMEDIATE 'alter table log_business add log_id varchar2(36 byte)';
		EXECUTE IMMEDIATE 'alter table log_business add constraint log_business_fk_log_id foreign key (log_id) references log (id) on delete cascade';
		EXECUTE IMMEDIATE 'comment on column log_business.log_id is ''Ссылка на уведомления''';
		dbms_output.put_line(v_task_name||'[INFO]:'||' Success');
	ELSE
		dbms_output.put_line(v_task_name||'[WARNING]:'||' changes had already been implemented');
	END IF;

EXCEPTION
	when OTHERS then
		dbms_output.put_line(v_task_name||'[FATAL]:'||sqlerrm);	
END;
/

--3.7-skononova-4 https://jira.aplana.com/browse/SBRFNDFL-7713 Оптимизация отбора источников при консолидации
DECLARE
	v_run_condition number(1);
	v_task_name varchar2(128):='alter_tables block #3 - indexes';  
BEGIN
	select decode(count(*),0,1,0) into v_run_condition from user_indexes where index_NAME= 'IDX_NPI_OPER_ID_DATES';
	IF v_run_condition=1 THEN
		EXECUTE IMMEDIATE 'create index idx_npi_oper_id_dates on ndfl_person_income (income_payout_date asc, 
				    tax_transfer_date asc, tax_date asc, income_accrued_date asc, operation_id asc )
				    compute statistics';
		dbms_output.put_line(v_task_name||'[INFO]:'||' Success');
	ELSE
		dbms_output.put_line(v_task_name||'[WARNING]:'||' changes had already been implemented');
	END IF;

EXCEPTION
	when OTHERS then
		dbms_output.put_line(v_task_name||'[FATAL]:'||sqlerrm);	
END;
/


COMMIT;


