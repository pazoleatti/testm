begin 
	dbms_output. put_line ('Query #1: Ref book person (count without versions)');
end;
/
select count(*) as count_person from ref_book_person;

--количество записей с учетом версий
begin 
	dbms_output. put_line ('Query #2: Ref book person (count with versions)');
end;
/

select count(*) as count_person from (select record_id from ref_book_person group by record_id);

--количество дубликатов без учета версий
begin 
	dbms_output. put_line ('Query #3: Ref book person (doubles without versions)');
end;
/

select count(*) as count_of_doubles from ref_book_person where old_id <> record_id;

--количество дубликатов с учетом версий
begin 
	dbms_output. put_line ('Query #4: Ref book person (doubles with versions)');
end;
/

select count(*) as count_of_doubles from (select old_id from ref_book_person where old_id <> record_id group by old_id);

--Количество ФЛ в разрезе количества ДУЛ-ов (REF_BOOK_ID_DOC - с одним ДУЛ, с двумя ДУЛ и т. д.)

begin 
	dbms_output. put_line ('Query #5: Ref book person (count of documents and count of persons)');
end;
/

select cnt_all as Count_of_documents, count(*) as count_of_persons from 
(select record_id,  sum(cnt) as cnt_all from
(select record_id, (select count(*) from ref_book_id_doc doc where doc.person_id = ref_book_person.id)  as cnt  from ref_book_person) 
group by record_id)
group by cnt_all;

begin 
	dbms_output. put_line ('Query #6: Notification (count)');
end;
/

--Оповещения (NOTIFICATION):
select count(*) as count_of_notifications from notification;

begin 
	dbms_output. put_line ('Query #7: Log (count)');
end;
/

--Уведомления (LOG_ENTRY):
select count(*) as count_of_logs from log;

begin 
	dbms_output. put_line ('Query #8: Log_entry (count)');
end;
/

select count(*) as count_of_log_entries from log_entry;

begin 
	dbms_output. put_line ('Query #9: Log_business (count)');
end;
/

select count(*) as count_of_log_business from log_business;

--Блобы (BLOB_DATA):

--1. Количество записей в блобах.
begin 
	dbms_output. put_line ('Query #10: BLOBs (count)');
end;
/

select count(*) from blob_data;

--2. Общий размер всех блобов.
begin 
	dbms_output. put_line ('Query #11: BLOBs (size)');
end;
/

select round(sum(dbms_lob.getlength(data)/1024/1024),2) as size_MB from blob_data;

begin 
	dbms_output. put_line ('Query #12: BLOBs (real size)');
end;
/

select 
decode(user_lobs.segment_name, NULL, user_segments.segment_name,
       user_lobs.table_name || ' -> ' || user_lobs.column_name || ' -> ' || user_lobs.segment_name) "SEGMENT",
Round(bytes/1024/1024)                                                                               "Size in MB",
Round((sum(bytes) over ())/1024/1024,2)                                                         "Total Size in MB"
from user_segments, user_lobs
where user_segments.segment_name = user_lobs.segment_name
and   user_lobs.TABLE_NAME = 'BLOB_DATA'
and   user_lobs.column_name = 'DATA';

-- Количество форм в разрезе по макету (DECLARATION_TEMPLATE).--
begin 
	dbms_output. put_line ('Query #12: Forms (count)');
end;
/

select declaration_template_id, count(*) as count_of_forms
from declaration_data
group by declaration_template_id;



-- Количество записей в реквизитах, доходах, вычетах и авансах всех ПНФ и КНФ.

begin 
	dbms_output. put_line ('Query #13: Forms (count of ndfl_persons)');
end;
/

select dd.declaration_template_id, count(*) as count_of_persons from declaration_data dd join ndfl_person np on np.declaration_data_id=dd.id
where declaration_template_id in (100,101) group by declaration_template_id;

begin 
	dbms_output. put_line ('Query #14: Forms (count of ndfl_person_incomes)');
end;
/

select dd.declaration_template_id, count(*) as count_of_income from declaration_data dd join ndfl_person np on np.declaration_data_id=dd.id
join ndfl_person_income npi on npi.ndfl_person_id = np.id
where declaration_template_id in (100,101) group by declaration_template_id;

begin 
	dbms_output. put_line ('Query #15: Forms (count of ndfl_person_deductions)');
end;
/

select dd.declaration_template_id, count(*) as count_of_deduction from declaration_data dd join ndfl_person np on np.declaration_data_id=dd.id
join ndfl_person_deduction npd on npd.ndfl_person_id = np.id
where declaration_template_id in (100,101) group by declaration_template_id;

begin 
	dbms_output. put_line ('Query #16: Forms (count of ndfl_person_prepayments');
end;
/

select dd.declaration_template_id, count(*) as count_of_prepayment from declaration_data dd join ndfl_person np on np.declaration_data_id=dd.id
join ndfl_person_prepayment npp on npp.ndfl_person_id = np.id
where declaration_template_id in (100,101) group by declaration_template_id;

--Количество ПНФ по диапазонам количества реквизитов в ПНФ. 

begin 
	dbms_output. put_line ('Query #17: Forms (range of quantities (primary forms))');
end;
/

select  grps as groups, count(*) as count_of_forms  from
(select id, case  
                when cnt =0 then 0 
                when cnt between 1 and 99 then 1 --'1 - 99'
                when cnt between 100 and 999 then 100 --'100 - 999'
                when cnt between 1000 and 4999 then 1000 --'1000 - 4999' 
                when cnt between 5000 and 9999 then 5000 --'5000 - 9999'
                when cnt between 10000 and 19999 then 10000 --'10000-19999' 
                when cnt between 20000 and 49999 then 20000 --'20000 - 49999'
                when cnt between 50000 and 100000 then 50000 --50000-100000
                when cnt >=100001 then 100001 --'more than 100001' 
                else -1 end  grps from 
(select dd.id, count(*) as cnt from declaration_data dd join ndfl_person np on np.declaration_data_id=dd.id
where declaration_template_id = 100 group by dd.id))
group by grps
order by grps;

-- Количество КНФ по диапазонам количества реквизитов в КНФ. 
begin 
	dbms_output. put_line ('Query #18: Forms (range of quantities (consolidated forms))');
end;
/

select  grps as groups, count(*)  as count_of_forms from
(select id, case  
                when cnt =0 then 0 
                when cnt between 1 and 999 then 1 
                when cnt between 1000 and 4999 then 1000 
                when cnt between 5000 and 9999 then 5000 
                when cnt between 10000 and 49999 then 10000 
                when cnt between 50000 and 99999 then 99999 
                when cnt between 100000 and 199999 then 100000                 
                when cnt between 200000 and 399999 then 200000                
                when cnt between 400000 and 599999 then 400000                                
                when cnt between 600000 and 799999 then 600000 
                when cnt between 800000 and 999999 then 800000  
                when cnt between 1000000 and 1999999 then 1000000                
                when cnt >2000000 then 2000000
                else -1 end  grps from 
(select dd.id, count(*) as cnt from declaration_data dd join ndfl_person np on np.declaration_data_id=dd.id
where declaration_template_id = 101 group by dd.id))
group by grps
order by grps;

-- Количество реквизитов, доходов, вычетов, авансов в КНФ с максимальным числом реквизитов.
begin 
	dbms_output. put_line ('Query #19: Forms (count of persons, incomes, deductions, prepayments in biggest consolidated forms)');
end;
/

select sort_dd.id, sort_dd.cnt max_persons, sort_dd.cnt_rank as rank,
(select count(*) from declaration_data dd join ndfl_person np on np.declaration_Data_id=dd.id join ndfl_person_income npi on npi.ndfl_person_id=np.id where sort_dd.id=dd.id) incomes,
(select count(*) from declaration_data dd join ndfl_person np on np.declaration_Data_id=dd.id join ndfl_person_deduction npd on npd.ndfl_person_id=np.id where sort_dd.id=dd.id) deductions,
(select count(*) from declaration_data dd join ndfl_person np on np.declaration_Data_id=dd.id join ndfl_person_prepayment npp on npp.ndfl_person_id=np.id where sort_dd.id=dd.id) prepayments
from
(select dd.id, count(*) cnt,RANK() OVER(ORDER BY count(*) DESC) cnt_rank  from declaration_data dd join ndfl_person np on dd.id=np.declaration_data_id group by dd.id) sort_dd
where cnt_rank <=3
order by cnt_rank;
/
