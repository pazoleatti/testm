begin
--Удалить ОНФ вида 2-НДФЛ(ФЛ), если для нее формой-источником является 2-НДФЛ(1) - созданная в некорректном периоде 
	delete from declaration_data where id in 
	(select target_declaration_Data_id from declaration_data_consolidation where source_declaration_data_id in 
		((select dd.id from
			 declaration_data dd join declaration_template dt on dt.id = dd.declaration_template_id
			 join declaration_type dtp on dtp.id = dt.declaration_type_id
			 join  department_report_period drp on drp.id=dd.department_report_period_id
			 join report_period rp on rp.id = drp.report_period_id where
			dtp.id =102 and
			((form_type_id in (3,4,7) and dict_tax_period_id not in 
			(select id from report_period_type where code in ('34','90'))) or 
			(form_type_id=7 and department_id<>
				(select min(id) from department where name='Центральный аппарат' and is_active=1))))
			) and target_declaration_data_id in 
				(select id from declaration_data where declaration_template_id in 
					(select id from declaration_template where declaration_type_id=105)));

	CASE SQL%ROWCOUNT 
	WHEN 0 THEN dbms_output.put_line('Delete 1:'||'[WARNING]:'||' No changes was done');
	ELSE dbms_output.put_line('Delete 1:'||'[INFO]:'||' Success ('||SQL%ROWCOUNT||')');
	END CASE; 

--удаление транспортных сообщений для удаляемых НФ 
	delete from transport_message
	where declaration_id in
	(select dd.id from
	 declaration_data dd join declaration_template dt on dt.id = dd.declaration_template_id
	join declaration_type dtp on dtp.id = dt.declaration_type_id
	join  department_report_period drp on drp.id=dd.department_report_period_id
	join report_period rp on rp.id = drp.report_period_id where
	dtp.id in (102, 104, 106, 100,101, 105) and
	((form_type_id in (3,4,7) and dict_tax_period_id not in (select id from report_period_type where code in ('34','90'))) or 
	(form_type_id=7 and department_id<>(select min(id) from department where name='Центральный аппарат' and is_active=1))));
	
	
	CASE SQL%ROWCOUNT 
	WHEN 0 THEN dbms_output.put_line('Delete 2:'||'[WARNING]:'||' No changes was done');
	ELSE dbms_output.put_line('Delete 2:'||'[INFO]:'||' Success ('||SQL%ROWCOUNT||')');
       	END CASE; 

--удаление НФ с неправильными периодами
	delete from declaration_data
	where id in
	(select dd.id from
	 declaration_data dd join declaration_template dt on dt.id = dd.declaration_template_id
	join declaration_type dtp on dtp.id = dt.declaration_type_id
	join  department_report_period drp on drp.id=dd.department_report_period_id
	join report_period rp on rp.id = drp.report_period_id where
	dtp.id in (102, 104, 106, 100,101, 105) and
	((form_type_id in (3,4,7) and dict_tax_period_id not in 
	(select id from report_period_type where code in ('34','90'))) or 
	(form_type_id=7 and department_id<>
	(select min(id) from department where name='Центральный аппарат' and is_active=1))));
	
	CASE SQL%ROWCOUNT 
	WHEN 0 THEN dbms_output.put_line('Delete 3:'||'[WARNING]:'||' No changes was done');
       	ELSE dbms_output.put_line('Delete 3:'||'[INFO]:'||' Success ('||SQL%ROWCOUNT||')');
	END CASE; 

--Удаление некорректных периодов подразделений
	delete from department_report_period where id in 
	(select  drp.id from department_report_period drp join report_period rp on rp.id = drp.report_period_id where
	(form_type_id in (3,4,7) and dict_tax_period_id not in 
	(select id from report_period_type where code in ('34','90'))) or 
	(form_type_id=7 and department_id<>
	(select min(id) from department where name='Центральный аппарат' and is_active=1)));
	
	CASE SQL%ROWCOUNT 
	WHEN 0 THEN dbms_output.put_line('Delete 4:'||'[WARNING]:'||' No changes was done');
	ELSE dbms_output.put_line('Delete 4:'||'[INFO]:'||' Success ('||SQL%ROWCOUNT||')');
	END CASE; 
	
	--Удаление некорректных периодов
	
	delete from report_period rp where 
        (form_type_id in (3,4,7) and dict_tax_period_id not in 
	(select id from report_period_type where code in ('34','90'))) or 
	(form_type_id=7 and not exists (select * from department_report_period drp where drp.report_period_id = rp.id 
	and department_id=(select min(id) from department where name='Центральный аппарат' and is_active=1)));

	CASE SQL%ROWCOUNT 
	WHEN 0 THEN dbms_output.put_line('Delete 5:'||'[WARNING]:'||' No changes was done');
	ELSE dbms_output.put_line('Delete 5:'||'[INFO]:'||' Success ('||SQL%ROWCOUNT||')');
	END CASE; 

EXCEPTION
  when OTHERS then 
    dbms_output.put_line('Error periods '||'[FATAL]:'||sqlerrm);		

end;
/
commit;
