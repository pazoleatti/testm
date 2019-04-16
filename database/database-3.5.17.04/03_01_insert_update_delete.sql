-- 3.5-amandzyak-9 - https://jira.aplana.com/browse/SBRFNDFL-7321 - Добавление видов доходов в справочник.
DECLARE
  v_income_type_id_2202 number;
  v_income_type_id_2510 number;
  v_task_name varchar2(1000):='insert_update_delete block #1 - merge into ref_book_income_kind where ref_book_income_type.code=2202 mark=13';  
BEGIN
  select max(id) into v_income_type_id_2202 from ref_book_income_type it where it.code = '2202' and trunc(it.version) <= trunc(sysdate) and it.status = 0
  and not exists (select 1 from ref_book_income_type it2 where it2.record_id = it.record_id and trunc(it2.version) < trunc(sysdate) and status = 2);
  IF v_income_type_id_2202 is not null THEN
      merge into ref_book_income_kind a using
      (
      	select 
        v_income_type_id_2202 income_type_id,
        '13' mark,
        'Выплата дохода в денежной форме' name,
        to_date('01.01.2016', 'dd.mm.yyyy') version,
        0 status
        from dual
      ) b
      on (a.income_type_id=b.income_type_id and a.mark=b.mark and a.version=b.version and a.status=b.status)
      when not matched then
      insert (id, record_id, income_type_id, mark, name, version, status)
      values (seq_ref_book_record.nextval, seq_ref_book_record_row_id.nextval, b.income_type_id, b.mark, b.name, b.version, b.status);

	CASE SQL%ROWCOUNT 
	WHEN 0 THEN dbms_output.put_line(v_task_name||'[WARNING]:'||' No changes was done');
	ELSE dbms_output.put_line(v_task_name||'[INFO]:'||' Success');
	END CASE; 
	
  ELSE
    dbms_output.put_line(v_task_name||'[WARNING]:'||' Not found');
  END IF;
 
  v_task_name :='insert_update_delete block #2 - merge into ref_book_income_kind where ref_book_income_type.code=2510 mark=13';  
  select max(id) into v_income_type_id_2510 from ref_book_income_type it where it.code = '2510' and trunc(it.version) <= trunc(sysdate) and it.status = 0
  and not exists (select 1 from ref_book_income_type it2 where it2.record_id = it.record_id and trunc(it2.version) < trunc(sysdate) and status = 2);
  IF v_income_type_id_2510 is not null THEN
      merge into ref_book_income_kind a using
      (
      	select 
        v_income_type_id_2510 income_type_id,
        '13' mark,
        'Выплата дохода в денежной форме' name,
        to_date('01.01.2016', 'dd.mm.yyyy') version,
        0 status
        from dual
      ) b
      on (a.income_type_id=b.income_type_id and a.mark=b.mark and a.version=b.version and a.status=b.status)
      when not matched then
      insert (id, record_id, income_type_id, mark, name, version, status)
      values (seq_ref_book_record.nextval, seq_ref_book_record_row_id.nextval, b.income_type_id, b.mark, b.name, b.version, b.status);
	  
	CASE SQL%ROWCOUNT 
	WHEN 0 THEN dbms_output.put_line(v_task_name||'[WARNING]:'||' No changes was done');
	ELSE dbms_output.put_line(v_task_name||'[INFO]:'||' Success');
	END CASE; 
	
  ELSE
    dbms_output.put_line(v_task_name||'[WARNING]:'||' Not found');
  END IF;

  v_task_name :='insert_update_delete block #3 - update ref_book_income_kind.name where ref_book_income_type.code=2202 mark=13';  
  IF v_income_type_id_2202 is not null THEN
	  update ref_book_income_kind a 
	  set name = 'Выплата дохода в денежной форме'
	  where
	  exists 
	  (
	  select 1
	  from
	  (
      	select 
		v_income_type_id_2202 income_type_id,
		'13' mark,
		'Выплата дохода в денежной форме' name,
		to_date('01.01.2016', 'dd.mm.yyyy') version,
		0 status
		from dual
	  ) b
	  where b.income_type_id = a.income_type_id and b.mark = a.mark and b.name <> a.name
	  ); 
	  
	CASE SQL%ROWCOUNT 
	WHEN 0 THEN dbms_output.put_line(v_task_name||'[WARNING]:'||' No changes was done');
	ELSE dbms_output.put_line(v_task_name||'[INFO]:'||' Success');
	END CASE; 
	
  ELSE
    dbms_output.put_line(v_task_name||'[WARNING]:'||' Not found');
  END IF;

  v_task_name :='insert_update_delete block #4 - update ref_book_income_kind.name where ref_book_income_type.code=2510 mark=13';  
  IF v_income_type_id_2510 is not null THEN
	  update ref_book_income_kind a 
	  set name = 'Выплата дохода в денежной форме'
	  where
	  exists 
	  (
	  select 1
	  from
	  (
		select 
		v_income_type_id_2510 income_type_id,
		'13' mark,
		'Выплата дохода в денежной форме' name,
		to_date('01.01.2016', 'dd.mm.yyyy') version,
		0 status
		from dual
	  ) b
	  where b.income_type_id = a.income_type_id and b.mark = a.mark and b.name <> a.name
	  ); 
	  
	CASE SQL%ROWCOUNT 
	WHEN 0 THEN dbms_output.put_line(v_task_name||'[WARNING]:'||' No changes was done');
	ELSE dbms_output.put_line(v_task_name||'[INFO]:'||' Success');
	END CASE; 
	
  ELSE
    dbms_output.put_line(v_task_name||'[WARNING]:'||' Not found');
  END IF;

  commit;

END;
/

-- 3.5-ytrofimov-19 - https://jira.aplana.com/browse/SBRFNDFL-7175 Реализовать запись в историю изменений ОНФ информацию о замене "состояние ЭД" и прикладывании файла
declare 
  v_task_name varchar2(128):='insert_update_delete block #5 - merge into event';  
begin
	merge into event a using
	(select 10002 as id, 'Прикрепление файла ответа от ФНС' as name from dual
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

