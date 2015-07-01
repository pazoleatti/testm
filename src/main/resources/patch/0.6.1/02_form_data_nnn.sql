--http://jira.aplana.com/browse/SBRFACCTAX-11918: Изменение типа графы "Дивиденды выплаченные. Сумма дивидендов. Выплачиваемая через ОАО "Сбербанк России". Получатель. Статус"
--update form_column set type='N', max_length = 1, precision = 0 where alias = 'status' and form_template_id = 10080;

begin
for x in (select id from form_column where alias = 'status' and form_template_id = 10080) loop
    execute immediate 'LOCK TABLE FORM_DATA_10080 IN EXCLUSIVE MODE';
    execute immediate 'ALTER TABLE FORM_DATA_10080 RENAME COLUMN C'||x.id||' TO C'||x.id||'_TEMP';
    execute immediate 'ALTER TABLE FORM_DATA_10080 ADD C'||x.id||' number(1)';
    execute immediate 'UPDATE FORM_DATA_10080 SET C'||x.id||'= CASE WHEN C'||x.id||'_TEMP=''RUS'' THEN 1 ELSE NULL END';
    execute immediate 'ALTER TABLE FORM_DATA_10080 DROP COLUMN C'||x.id||'_TEMP';
end loop;
end;
/

BEGIN
    for x in (select id, translate( alias || ' - ' || name, '''', ' ') as col_comment from form_column where alias = 'status' and form_template_id = 10080) loop
        execute immediate 'COMMENT ON COLUMN FORM_DATA_10080.C'||x.id||' IS '''||x.col_comment||'''';
end loop;
END;
/

--http://jira.aplana.com/browse/SBRFACCTAX-11938: Поменять тип столбцов в сводном отчете (R->S)

declare query_stmt varchar2(4000) := '';
begin
	execute immediate 'LOCK TABLE FORM_DATA_2409 IN EXCLUSIVE MODE';
	select 'alter table form_data_2409 modify ('||listagg('c'||id||' varchar2(4000)', ', ') within group (order by ord)||')' into query_stmt
		from form_column 
		where form_template_id = 2409 and alias in ('organRegNum', 'taxpayerCode', 'address');
	
	execute immediate query_stmt;
	
	for x in (select * from form_column where form_template_id = 2409 and alias in ('organRegNum', 'taxpayerCode', 'address') and type='R') loop
		query_stmt := 'update form_data_2409 set c'||x.id||' = (select string_value from ref_book_value where attribute_id = '||x.attribute_id||' and record_id = c'||x.parent_column_id||')';
		execute immediate query_stmt;
	end loop;	
end;
/
--update form_column set type = 'S', max_length=2000, parent_column_id = null, attribute_id = null where form_template_id = 2409 and alias in ('organRegNum', 'taxpayerCode', 'address') and type='R';
commit;

COMMIT;
EXIT;