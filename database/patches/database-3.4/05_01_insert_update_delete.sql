--https://jira.aplana.com/browse/SBRFNDFL-5447 изменил имя атрибута справочника
declare
	v_task_name varchar2(128):='insert_update_delete block #1 - update ref_book_attribute';  
begin	
	
    update ref_book_attribute set name = 'Тип вычета' where id = 9213;

	CASE SQL%ROWCOUNT 
	WHEN 0 THEN dbms_output.put_line(v_task_name||'[WARNING]:'||' No changes was done');
	ELSE dbms_output.put_line(v_task_name||'[INFO]:'||' Success');
	END CASE; 
EXCEPTION
  when OTHERS then
    dbms_output.put_line(v_task_name||'[FATAL]: '||sqlerrm);
        ROLLBACK;
end;
/
COMMIT;

--https://jira.aplana.com/browse/SBRFNDFL-6201 сделать спр. "Признак лица, подписавшего документ" нередактируемым
declare
	v_task_name varchar2(128):='insert_update_delete block #2 - update ref_book';  
begin	
	
	update ref_book set read_only = 1 where id = 35;

	CASE SQL%ROWCOUNT 
	WHEN 0 THEN dbms_output.put_line(v_task_name||'[WARNING]:'||' No changes was done');
	ELSE dbms_output.put_line(v_task_name||'[INFO]:'||' Success');
	END CASE; 
EXCEPTION
  when OTHERS then
    dbms_output.put_line(v_task_name||'[FATAL]: '||sqlerrm);
        ROLLBACK;
end;
/
COMMIT;

--https://jira.aplana.com/browse/SBRFNDFL-5489 изменения истории изменений
declare
	v_task_name varchar2(128):='insert_update_delete block #3 - update event';  
begin	
	
	update event set name = 'Обновление' where id = 6;

	CASE SQL%ROWCOUNT 
	WHEN 0 THEN dbms_output.put_line(v_task_name||'[WARNING]:'||' No changes was done');
	ELSE dbms_output.put_line(v_task_name||'[INFO]:'||' Success');
	END CASE; 
EXCEPTION
  when OTHERS then
    dbms_output.put_line(v_task_name||'[FATAL]: '||sqlerrm);
        ROLLBACK;
end;
/
COMMIT;

--https://jira.aplana.com/browse/SBRFNDFL-5489 изменения истории изменений
declare 
  v_task_name varchar2(128):='insert_update_delete block #4 - merge into event';  
begin
	merge into event a using
	(select 10000 as id, 'Создание ФЛ' as name from dual
	 union
	 select 10001 as id, 'Изменение данных ФЛ' as name from dual
	) b
	on (a.id=b.id)
	when not matched then
		insert (id, name)
		values (b.id, b.name);
	
	CASE SQL%ROWCOUNT 
	WHEN 0 THEN dbms_output.put_line(v_task_name||'[WARNING]:'||' No changes was done');
	ELSE dbms_output.put_line(v_task_name||'[INFO]:'||' Success');
	END CASE; 
EXCEPTION
  when OTHERS then
    dbms_output.put_line(v_task_name||'[FATAL]:'||sqlerrm);		
end;
/
COMMIT;

declare
	v_task_name varchar2(128):='insert_update_delete block #5 - update declaration_data';  
begin	
	
	update declaration_data dd set correction_num = 0 where (select form_kind from declaration_template dt
			where dt.id = dd.declaration_template_id) = 7;

	CASE SQL%ROWCOUNT 
	WHEN 0 THEN dbms_output.put_line(v_task_name||'[WARNING]:'||' No changes was done');
	ELSE dbms_output.put_line(v_task_name||'[INFO]:'||' Success');
	END CASE; 
EXCEPTION
  when OTHERS then
    dbms_output.put_line(v_task_name||'[FATAL]: '||sqlerrm);
        ROLLBACK;
end;
/
COMMIT;

--https://jira.aplana.com/browse/SBRFNDFL-6338 Реализовать общую логику работы с асинхронными задачами в рамках UC по операциям
declare
	v_task_name varchar2(128):='insert_update_delete block #6 - delete from decl_template_event_script';  
begin		
    delete from decl_template_event_script where event_id = 105;

	CASE SQL%ROWCOUNT 
	WHEN 0 THEN dbms_output.put_line(v_task_name||'[WARNING]:'||' No changes was done');
	ELSE dbms_output.put_line(v_task_name||'[INFO]:'||' Success');
	END CASE; 
EXCEPTION
  when OTHERS then
    dbms_output.put_line(v_task_name||'[FATAL]: '||sqlerrm);
        ROLLBACK;
end;
/
COMMIT;

--https://jira.aplana.com/browse/SBRFNDFL-5472 - Новая ассинхронная задача изменения состояние ЭД
declare 
  v_task_name varchar2(128):='insert_update_delete block #7 - merge into async_task_type';  
begin
	merge into async_task_type a using
	(select 42 as id, 'Изменение состояния ЭД' as name, 'UpdateDocStateAsyncTask' as handler_bean from dual
	) b
	on (a.id=b.id)
	when not matched then
		insert (id, name, handler_bean)
		values (b.id, b.name, b.handler_bean);
	
	CASE SQL%ROWCOUNT 
	WHEN 0 THEN dbms_output.put_line(v_task_name||'[WARNING]:'||' No changes was done');
	ELSE dbms_output.put_line(v_task_name||'[INFO]:'||' Success');
	END CASE; 
EXCEPTION
  when OTHERS then
    dbms_output.put_line(v_task_name||'[FATAL]:'||sqlerrm);		
end;
/
COMMIT;

--https://jira.aplana.com/browse/SBRFNDFL-6338 Реализовать общую логику работы с асинхронными задачами в рамках UC по операциям
declare
	v_task_name varchar2(128):='insert_update_delete block #8 - update declaration_subreport';  
begin	
	
	update declaration_subreport
	set alias = 'report_2ndfl1'
	where declaration_template_id = 102 and alias = 'report_2ndfl';

	CASE SQL%ROWCOUNT 
	WHEN 0 THEN dbms_output.put_line(v_task_name||'[WARNING]:'||' No changes was done');
	ELSE dbms_output.put_line(v_task_name||'[INFO]:'||' Success');
	END CASE; 
EXCEPTION
  when OTHERS then
    dbms_output.put_line(v_task_name||'[FATAL]: '||sqlerrm);
        ROLLBACK;
end;
/
COMMIT;

--https://jira.aplana.com/browse/SBRFNDFL-6338 Реализовать общую логику работы с асинхронными задачами в рамках UC по операциям
declare
	v_task_name varchar2(128):='insert_update_delete block #9 - update declaration_subreport';  
begin	
	
	update declaration_subreport
	set alias = 'report_2ndfl2'
	where declaration_template_id = 104 and alias = 'report_2ndfl';

	CASE SQL%ROWCOUNT 
	WHEN 0 THEN dbms_output.put_line(v_task_name||'[WARNING]:'||' No changes was done');
	ELSE dbms_output.put_line(v_task_name||'[INFO]:'||' Success');
	END CASE; 
EXCEPTION
  when OTHERS then
    dbms_output.put_line(v_task_name||'[FATAL]: '||sqlerrm);
        ROLLBACK;
end;
/
COMMIT;

--https://jira.aplana.com/browse/SBRFNDFL-6338 Реализовать общую логику работы с асинхронными задачами в рамках UC по операциям
declare 
  v_task_name varchar2(128):='insert_update_delete block #10 - merge into declaration_subreport';  
begin
	merge into declaration_subreport a using
	(select 104 as declaration_template_id, 'Уведомление о задолженности' as name, 2 as ord, 'dept_notice_dec' as alias from dual
	) b
	on (a.declaration_template_id=b.declaration_template_id and a.alias=b.alias)
	when not matched then
		insert (id, declaration_template_id, name, ord, alias)
		values (SEQ_DECLARATION_SUBREPORT.nextval, b.declaration_template_id, b.name, b.ord, b.alias);
	
	CASE SQL%ROWCOUNT 
	WHEN 0 THEN dbms_output.put_line(v_task_name||'[WARNING]:'||' No changes was done');
	ELSE dbms_output.put_line(v_task_name||'[INFO]:'||' Success');
	END CASE; 
EXCEPTION
  when OTHERS then
    dbms_output.put_line(v_task_name||'[FATAL]:'||sqlerrm);		
end;
/
COMMIT;

--https://conf.aplana.com/pages/viewpage.action?pageId=43976595 Введены идентификаторы для состояний ЭД"
declare
	v_task_name varchar2(128):='insert_update_delete block #11 - update declaration_data';  
begin	
	
	update declaration_data dd set doc_state_id = null;

	CASE SQL%ROWCOUNT 
	WHEN 0 THEN dbms_output.put_line(v_task_name||'[WARNING]:'||' No changes was done');
	ELSE dbms_output.put_line(v_task_name||'[INFO]:'||' Success');
	END CASE; 
EXCEPTION
  when OTHERS then
    dbms_output.put_line(v_task_name||'[FATAL]: '||sqlerrm);
        ROLLBACK;
end;
/
COMMIT;

--https://conf.aplana.com/pages/viewpage.action?pageId=43976595 Введены идентификаторы для состояний ЭД"
declare
	v_task_name varchar2(128):='insert_update_delete block #12 - delete from ref_book_doc_state';  
begin		
    delete from ref_book_doc_state;

	CASE SQL%ROWCOUNT 
	WHEN 0 THEN dbms_output.put_line(v_task_name||'[WARNING]:'||' No changes was done');
	ELSE dbms_output.put_line(v_task_name||'[INFO]:'||' Success');
	END CASE; 
EXCEPTION
  when OTHERS then
    dbms_output.put_line(v_task_name||'[FATAL]: '||sqlerrm);
        ROLLBACK;
end;
/
COMMIT;

--https://conf.aplana.com/pages/viewpage.action?pageId=43976595 Введены идентификаторы для состояний ЭД"
declare 
  v_task_name varchar2(128):='insert_update_delete block #13 - merge into ref_book_doc_state';  
begin
	merge into ref_book_doc_state a using
	(select 1 as id, null as knd, 'Не отправлен в ФНС' as name from dual
	 union
	 select 2 as id, null as knd, 'Выгружен для отправки в ФНС' as name from dual
	 union
	 select 3 as id, 1166002 as knd, 'Принят' as name from dual
	 union
	 select 4 as id, 1166006 as knd, 'Отклонен' as name from dual
	 union
	 select 5 as id, 1166007 as knd, 'Успешно отработан' as name from dual
	 union
	 select 6 as id, 1166009 as knd, 'Требует уточнения' as name from dual
	 union
	 select 7 as id, null as knd, 'Ошибка' as name from dual
	) b
	on (a.id=b.id)
	when not matched then
		insert (id, knd, name)
		values (b.id, b.knd, b.name);
	
	CASE SQL%ROWCOUNT 
	WHEN 0 THEN dbms_output.put_line(v_task_name||'[WARNING]:'||' No changes was done');
	ELSE dbms_output.put_line(v_task_name||'[INFO]:'||' Success');
	END CASE; 
EXCEPTION
  when OTHERS then
    dbms_output.put_line(v_task_name||'[FATAL]:'||sqlerrm);		
end;
/
COMMIT;

--https://conf.aplana.com/pages/viewpage.action?pageId=43976595 Введены идентификаторы для состояний ЭД"
declare
	v_task_name varchar2(128):='insert_update_delete block #14 - update declaration_data';  
begin	
	
	update declaration_data dd set doc_state_id = 1 where id in (
				select dd.id from declaration_data dd
				join declaration_template dt on dt.id = dd.declaration_template_id
				join declaration_kind dk on dk.id = dt.form_kind
				where dk.id = 7);

	CASE SQL%ROWCOUNT 
	WHEN 0 THEN dbms_output.put_line(v_task_name||'[WARNING]:'||' No changes was done');
	ELSE dbms_output.put_line(v_task_name||'[INFO]:'||' Success');
	END CASE; 
EXCEPTION
  when OTHERS then
    dbms_output.put_line(v_task_name||'[FATAL]: '||sqlerrm);
        ROLLBACK;
end;
/
COMMIT;

--https://jira.aplana.com/browse/SBRFNDFL-6393 - описание ограничения поправил
declare
	v_task_name varchar2(128):='insert_update_delete block #15 - update async_task_type';  
begin	
	
	update async_task_type set limit_kind = 'Количество ФЛ в НФ' where id = 32;

	CASE SQL%ROWCOUNT 
	WHEN 0 THEN dbms_output.put_line(v_task_name||'[WARNING]:'||' No changes was done');
	ELSE dbms_output.put_line(v_task_name||'[INFO]:'||' Success');
	END CASE; 
EXCEPTION
  when OTHERS then
    dbms_output.put_line(v_task_name||'[FATAL]: '||sqlerrm);
        ROLLBACK;
end;
/
COMMIT;
