set long 32767 pagesize 0 linesize 4000 feedback off echo off verify off trims on heading off termout off 
SET SERVEROUTPUT ON

column filename new_val filename
column outpath new_val outpath
column users_list new_val users_list
column start_date new_val start_date
column end_date new_val end_date

-- Путь для сохранения результата в указанной папке должны быть созданы папки SIB, SRB, PB
SELECT 'd:\Work\SBRF-NDFL\JIRA\SBRFNDFL-6776' outpath from dual;

-- Cписок пользователей формат ввода для одного пользователя (user_id) для нескольких пользователей (user_id1, user_id2, ..., user_idN)
SELECT '(13277)' users_list from dual;

-- Дата начала периода запроса данных
SELECT '01.01.2019 00:00:00' start_date FROM dual;

-- Дата окончание периода запроса данных
SELECT '01.01.2020 00:00:00' end_date FROM dual;

-- Имя файла скрипта (не изменять)
SELECT 'Script_GeNotificationToFile'||'.sql' filename FROM dual;

spool &outpath\&filename

SELECT 'set long 32767 pagesize 0 linesize 4000 feedback off echo off verify off trims on heading off termout off' FROM DUAL;
with TMP_NOTIF (id, filename, sqlquery_header,sqlquery) as (
SELECT 
  ntf.id, 
  '&outpath\SIB\SIB_'||TO_CHAR(REPLACE(dd.file_name,'.xml'))||'_'||ntf.num_form||'_'
  ||CASE WHEN (SELECT COUNT(id) FROM notification WHERE id = ntf.id AND ntf.up_text LIKE '%ЗАГРУЗКА ФАЙЛА%' AND ntf.up_text LIKE '%ВЫПОЛНЕНО СОЗДАНИЕ НАЛОГОВОЙ ФОРМЫ%' AND ntf.up_text LIKE '%.XML%') > 0 THEN 'Zagr' 
         WHEN (SELECT COUNT(id) FROM notification WHERE id = ntf.id AND ntf.up_text LIKE '%ОШИБКА ЗАГРУЗКИ ФАЙЛА%' AND ntf.up_text LIKE '%.XML%') > 0 THEN 'Zagr' 
         WHEN (SELECT COUNT(id) FROM notification WHERE id = ntf.id AND ntf.up_text LIKE '%ВЫПОЛНЕНА ПРОВЕРКА НАЛОГОВОЙ ФОРМЫ%' AND ntf.up_text NOT LIKE '%ФАТАЛЬ%' AND ntf.up_text NOT LIKE '%ОТМЕН%') > 0 THEN 'Prov' 
         WHEN (SELECT COUNT(id) FROM notification WHERE id = ntf.id AND ntf.up_text LIKE '%ВЫПОЛНЕНА ПРОВЕРКА НАЛОГОВОЙ ФОРМЫ%' AND ntf.up_text LIKE '%ФАТАЛЬ%' AND ntf.up_text NOT LIKE '%ОТМЕН%') > 0 THEN 'Prov' 
         WHEN (SELECT COUNT(id) FROM notification WHERE id = ntf.id AND ntf.up_text LIKE '%НЕ ВЫПОЛНЕНА ОПЕРАЦИЯ "ПРОВЕРКА" ДЛЯ НАЛОГОВОЙ ФОРМЫ%' AND ntf.up_text NOT LIKE '%ОТМЕН%') > 0 THEN 'Prov'
         WHEN (SELECT COUNT(id) FROM notification WHERE id = ntf.id AND ntf.up_text LIKE '%ОПЕРАЦИЯ "ИДЕНТИФИКАЦИЯ ФЛ" ВЫПОЛНЕНА ДЛЯ НАЛОГОВОЙ ФОРМЫ%' AND ntf.up_text NOT LIKE '%ОТМЕН%') > 0 THEN 'Id'
         WHEN (SELECT COUNT(id) FROM notification WHERE id = ntf.id AND ntf.up_text LIKE '%НЕ ВЫПОЛНЕНА ОПЕРАЦИЯ "ИДЕНТИФИКАЦИЯ ФЛ" ДЛЯ НАЛОГОВОЙ ФОРМЫ%' AND ntf.up_text NOT LIKE '%ОТМЕН%') > 0 THEN 'Id'
        ELSE '' END||'_'
  ||CASE WHEN (SELECT COUNT(id) FROM notification WHERE id = ntf.id AND ntf.up_text LIKE '%ЗАГРУЗКА ФАЙЛА%' AND ntf.up_text LIKE '%ВЫПОЛНЕНО СОЗДАНИЕ НАЛОГОВОЙ ФОРМЫ%' AND ntf.up_text LIKE '%.XML%') > 0 THEN 'B' 
         WHEN (SELECT COUNT(id) FROM notification WHERE id = ntf.id AND ntf.up_text LIKE '%ОШИБКА ЗАГРУЗКИ ФАЙЛА%' AND ntf.up_text LIKE '%.XML%') > 0 THEN 'H' 
         WHEN (SELECT COUNT(id) FROM notification WHERE id = ntf.id AND ntf.up_text LIKE '%ВЫПОЛНЕНА ПРОВЕРКА НАЛОГОВОЙ ФОРМЫ%' AND ntf.up_text NOT LIKE '%ФАТАЛЬ%' AND ntf.up_text NOT LIKE '%ОТМЕН%') > 0 THEN 'B' 
         WHEN (SELECT COUNT(id) FROM notification WHERE id = ntf.id AND ntf.up_text LIKE '%ВЫПОЛНЕНА ПРОВЕРКА НАЛОГОВОЙ ФОРМЫ%' AND ntf.up_text LIKE '%ФАТАЛЬ%' AND ntf.up_text NOT LIKE '%ОТМЕН%') > 0 THEN 'BF' 
         WHEN (SELECT COUNT(id) FROM notification WHERE id = ntf.id AND ntf.up_text LIKE '%НЕ ВЫПОЛНЕНА ОПЕРАЦИЯ "ПРОВЕРКА" ДЛЯ НАЛОГОВОЙ ФОРМЫ%' AND ntf.up_text NOT LIKE '%ОТМЕН%') > 0 THEN 'H'
         WHEN (SELECT COUNT(id) FROM notification WHERE id = ntf.id AND ntf.up_text LIKE '%ОПЕРАЦИЯ "ИДЕНТИФИКАЦИЯ ФЛ" ВЫПОЛНЕНА ДЛЯ НАЛОГОВОЙ ФОРМЫ%' AND ntf.up_text NOT LIKE '%ОТМЕН%') > 0 THEN 'B'
         WHEN (SELECT COUNT(id) FROM notification WHERE id = ntf.id AND ntf.up_text LIKE '%НЕ ВЫПОЛНЕНА ОПЕРАЦИЯ "ИДЕНТИФИКАЦИЯ ФЛ" ДЛЯ НАЛОГОВОЙ ФОРМЫ%' AND ntf.up_text NOT LIKE '%ОТМЕН%') > 0 THEN 'H'
        ELSE '' END 
  ||'_'||TO_CHAR(ntf.id)||'.csv',
  'SELECT ''"№ п/п";"Дата-время";"Тип сообщения";"Текст сообщения";"Тип";"Объект"'' FROM DUAL;',
  'SELECT TO_CHAR(ord)||'';''||TO_CHAR(creation_date,''DD.MM.YYYY hh24:mi:ss'')||'';''||CASE WHEN log_level = 0 THEN ''Информация'' WHEN log_level = 1 THEN ''предупреждение'' WHEN log_level = 2 THEN ''ошибка'' ELSE '''' END||'';''||TO_CHAR(message)||'';''||TO_CHAR(type)||'';''||TO_CHAR(object) FROM log_entry WHERE log_id = ''' ||TO_CHAR(ntf.log_id)||''';'
FROM (
      SELECT 
        nt.user_id,
        nt.id, 
        nt.log_id,
        UPPER(nt.text) AS up_text,
        CASE WHEN INSTR(nt.text,'№') > 0 THEN SUBSTR(REPLACE(REPLACE(REPLACE(nt.text,'"'),':'),' '),INSTR(REPLACE(REPLACE(REPLACE(nt.text,'"'),':'),' '),'№')+1,INSTR(REPLACE(REPLACE(REPLACE(nt.text,'"'),':'),' '),',',INSTR(REPLACE(REPLACE(REPLACE(nt.text,'"'),':'),' '),'№')+1)-(INSTR(REPLACE(REPLACE(REPLACE(nt.text,'"'),':'),' '),'№')+1)) ELSE '' END AS num_form
      FROM notification nt
      WHERE nt.user_id IN &users_list 
            AND nt.create_date BETWEEN TO_DATE('&start_date','DD.MM.YYYY hh24:mi:ss') AND TO_DATE('&end_date','DD.MM.YYYY hh24:mi:ss')
            AND (text LIKE '%"Сибирский банк"%' OR text LIKE '%44_0000_00%') ) ntf
LEFT OUTER JOIN declaration_data dd ON dd.id = ntf.num_form
WHERE (ntf.up_text LIKE '%ЗАГРУЗКА ФАЙЛА%' AND ntf.up_text LIKE '%ВЫПОЛНЕНО СОЗДАНИЕ НАЛОГОВОЙ ФОРМЫ%' AND ntf.up_text LIKE '%.XML%')
      OR (ntf.up_text LIKE '%ОШИБКА ЗАГРУЗКИ ФАЙЛА%' AND ntf.up_text LIKE '%.XML%')
      OR (ntf.up_text LIKE '%ВЫПОЛНЕНА ПРОВЕРКА НАЛОГОВОЙ ФОРМЫ%' AND ntf.up_text NOT LIKE '%ФАТАЛЬ%' AND ntf.up_text NOT LIKE '%ОТМЕН%')
      OR (ntf.up_text LIKE '%ВЫПОЛНЕНА ПРОВЕРКА НАЛОГОВОЙ ФОРМЫ%' AND ntf.up_text LIKE '%ФАТАЛЬ%' AND ntf.up_text NOT LIKE '%ОТМЕН%')
      OR (ntf.up_text LIKE '%НЕ ВЫПОЛНЕНА ОПЕРАЦИЯ "ПРОВЕРКА" ДЛЯ НАЛОГОВОЙ ФОРМЫ%' AND ntf.up_text NOT LIKE '%ОТМЕН%')
      OR (ntf.up_text LIKE '%ОПЕРАЦИЯ "ИДЕНТИФИКАЦИЯ ФЛ" ВЫПОЛНЕНА ДЛЯ НАЛОГОВОЙ ФОРМЫ%' AND ntf.up_text NOT LIKE '%ОТМЕН%')
      OR (ntf.up_text LIKE '%НЕ ВЫПОЛНЕНА ОПЕРАЦИЯ "ИДЕНТИФИКАЦИЯ ФЛ" ДЛЯ НАЛОГОВОЙ ФОРМЫ%' AND ntf.up_text NOT LIKE '%ОТМЕН%')
UNION ALL
SELECT 
  ntf.id, 
  '&outpath\SRB\SRB_'||TO_CHAR(REPLACE(dd.file_name,'.xml'))||'_'||ntf.num_form||'_'
  ||CASE WHEN (SELECT COUNT(id) FROM notification WHERE id = ntf.id AND ntf.up_text LIKE '%ЗАГРУЗКА ФАЙЛА%' AND ntf.up_text LIKE '%ВЫПОЛНЕНО СОЗДАНИЕ НАЛОГОВОЙ ФОРМЫ%' AND ntf.up_text LIKE '%.XML%') > 0 THEN 'Zagr' 
         WHEN (SELECT COUNT(id) FROM notification WHERE id = ntf.id AND ntf.up_text LIKE '%ОШИБКА ЗАГРУЗКИ ФАЙЛА%' AND ntf.up_text LIKE '%.XML%') > 0 THEN 'Zagr' 
         WHEN (SELECT COUNT(id) FROM notification WHERE id = ntf.id AND ntf.up_text LIKE '%ВЫПОЛНЕНА ПРОВЕРКА НАЛОГОВОЙ ФОРМЫ%' AND ntf.up_text NOT LIKE '%ФАТАЛЬ%' AND ntf.up_text NOT LIKE '%ОТМЕН%') > 0 THEN 'Prov' 
         WHEN (SELECT COUNT(id) FROM notification WHERE id = ntf.id AND ntf.up_text LIKE '%ВЫПОЛНЕНА ПРОВЕРКА НАЛОГОВОЙ ФОРМЫ%' AND ntf.up_text LIKE '%ФАТАЛЬ%' AND ntf.up_text NOT LIKE '%ОТМЕН%') > 0 THEN 'Prov' 
         WHEN (SELECT COUNT(id) FROM notification WHERE id = ntf.id AND ntf.up_text LIKE '%НЕ ВЫПОЛНЕНА ОПЕРАЦИЯ "ПРОВЕРКА" ДЛЯ НАЛОГОВОЙ ФОРМЫ%' AND ntf.up_text NOT LIKE '%ОТМЕН%') > 0 THEN 'Prov'
         WHEN (SELECT COUNT(id) FROM notification WHERE id = ntf.id AND ntf.up_text LIKE '%ОПЕРАЦИЯ "ИДЕНТИФИКАЦИЯ ФЛ" ВЫПОЛНЕНА ДЛЯ НАЛОГОВОЙ ФОРМЫ%' AND ntf.up_text NOT LIKE '%ОТМЕН%') > 0 THEN 'Id'
         WHEN (SELECT COUNT(id) FROM notification WHERE id = ntf.id AND ntf.up_text LIKE '%НЕ ВЫПОЛНЕНА ОПЕРАЦИЯ "ИДЕНТИФИКАЦИЯ ФЛ" ДЛЯ НАЛОГОВОЙ ФОРМЫ%' AND ntf.up_text NOT LIKE '%ОТМЕН%') > 0 THEN 'Id'
        ELSE '' END||'_'
  ||CASE WHEN (SELECT COUNT(id) FROM notification WHERE id = ntf.id AND ntf.up_text LIKE '%ЗАГРУЗКА ФАЙЛА%' AND ntf.up_text LIKE '%ВЫПОЛНЕНО СОЗДАНИЕ НАЛОГОВОЙ ФОРМЫ%' AND ntf.up_text LIKE '%.XML%') > 0 THEN 'B' 
         WHEN (SELECT COUNT(id) FROM notification WHERE id = ntf.id AND ntf.up_text LIKE '%ОШИБКА ЗАГРУЗКИ ФАЙЛА%' AND ntf.up_text LIKE '%.XML%') > 0 THEN 'H' 
         WHEN (SELECT COUNT(id) FROM notification WHERE id = ntf.id AND ntf.up_text LIKE '%ВЫПОЛНЕНА ПРОВЕРКА НАЛОГОВОЙ ФОРМЫ%' AND ntf.up_text NOT LIKE '%ФАТАЛЬ%' AND ntf.up_text NOT LIKE '%ОТМЕН%') > 0 THEN 'B' 
         WHEN (SELECT COUNT(id) FROM notification WHERE id = ntf.id AND ntf.up_text LIKE '%ВЫПОЛНЕНА ПРОВЕРКА НАЛОГОВОЙ ФОРМЫ%' AND ntf.up_text LIKE '%ФАТАЛЬ%' AND ntf.up_text NOT LIKE '%ОТМЕН%') > 0 THEN 'BF' 
         WHEN (SELECT COUNT(id) FROM notification WHERE id = ntf.id AND ntf.up_text LIKE '%НЕ ВЫПОЛНЕНА ОПЕРАЦИЯ "ПРОВЕРКА" ДЛЯ НАЛОГОВОЙ ФОРМЫ%' AND ntf.up_text NOT LIKE '%ОТМЕН%') > 0 THEN 'H'
         WHEN (SELECT COUNT(id) FROM notification WHERE id = ntf.id AND ntf.up_text LIKE '%ОПЕРАЦИЯ "ИДЕНТИФИКАЦИЯ ФЛ" ВЫПОЛНЕНА ДЛЯ НАЛОГОВОЙ ФОРМЫ%' AND ntf.up_text NOT LIKE '%ОТМЕН%') > 0 THEN 'B'
         WHEN (SELECT COUNT(id) FROM notification WHERE id = ntf.id AND ntf.up_text LIKE '%НЕ ВЫПОЛНЕНА ОПЕРАЦИЯ "ИДЕНТИФИКАЦИЯ ФЛ" ДЛЯ НАЛОГОВОЙ ФОРМЫ%' AND ntf.up_text NOT LIKE '%ОТМЕН%') > 0 THEN 'H'
        ELSE '' END 
  ||'_'||TO_CHAR(ntf.id)||'.csv',
  'SELECT ''"№ п/п";"Дата-время";"Тип сообщения";"Текст сообщения";"Тип";"Объект"'' FROM DUAL;',
  'SELECT TO_CHAR(ord)||'';''||TO_CHAR(creation_date,''DD.MM.YYYY hh24:mi:ss'')||'';''||CASE WHEN log_level = 0 THEN ''Информация'' WHEN log_level = 1 THEN ''предупреждение'' WHEN log_level = 2 THEN ''ошибка'' ELSE '''' END||'';''||TO_CHAR(message)||'';''||TO_CHAR(type)||'';''||TO_CHAR(object) FROM log_entry WHERE log_id = ''' ||TO_CHAR(ntf.log_id)||''';'
FROM (
      SELECT 
        nt.user_id,
        nt.id, 
        nt.log_id,
        UPPER(nt.text) AS up_text,
        CASE WHEN INSTR(nt.text,'№') > 0 THEN SUBSTR(REPLACE(REPLACE(REPLACE(nt.text,'"'),':'),' '),INSTR(REPLACE(REPLACE(REPLACE(nt.text,'"'),':'),' '),'№')+1,INSTR(REPLACE(REPLACE(REPLACE(nt.text,'"'),':'),' '),',',INSTR(REPLACE(REPLACE(REPLACE(nt.text,'"'),':'),' '),'№')+1)-(INSTR(REPLACE(REPLACE(REPLACE(nt.text,'"'),':'),' '),'№')+1)) ELSE '' END AS num_form
      FROM notification nt
      WHERE nt.user_id IN &users_list 
            AND nt.create_date BETWEEN TO_DATE('&start_date','DD.MM.YYYY hh24:mi:ss') AND TO_DATE('&end_date','DD.MM.YYYY hh24:mi:ss')
            AND (text LIKE '%"Среднерусский банк"%' OR text LIKE '%40_0000_00%') ) ntf
LEFT OUTER JOIN declaration_data dd ON dd.id = ntf.num_form
WHERE (ntf.up_text LIKE '%ЗАГРУЗКА ФАЙЛА%' AND ntf.up_text LIKE '%ВЫПОЛНЕНО СОЗДАНИЕ НАЛОГОВОЙ ФОРМЫ%' AND ntf.up_text LIKE '%.XML%')
      OR (ntf.up_text LIKE '%ОШИБКА ЗАГРУЗКИ ФАЙЛА%' AND ntf.up_text LIKE '%.XML%')
      OR (ntf.up_text LIKE '%ВЫПОЛНЕНА ПРОВЕРКА НАЛОГОВОЙ ФОРМЫ%' AND ntf.up_text NOT LIKE '%ФАТАЛЬ%' AND ntf.up_text NOT LIKE '%ОТМЕН%')
      OR (ntf.up_text LIKE '%ВЫПОЛНЕНА ПРОВЕРКА НАЛОГОВОЙ ФОРМЫ%' AND ntf.up_text LIKE '%ФАТАЛЬ%' AND ntf.up_text NOT LIKE '%ОТМЕН%')
      OR (ntf.up_text LIKE '%НЕ ВЫПОЛНЕНА ОПЕРАЦИЯ "ПРОВЕРКА" ДЛЯ НАЛОГОВОЙ ФОРМЫ%' AND ntf.up_text NOT LIKE '%ОТМЕН%')
      OR (ntf.up_text LIKE '%ОПЕРАЦИЯ "ИДЕНТИФИКАЦИЯ ФЛ" ВЫПОЛНЕНА ДЛЯ НАЛОГОВОЙ ФОРМЫ%' AND ntf.up_text NOT LIKE '%ОТМЕН%')
      OR (ntf.up_text LIKE '%НЕ ВЫПОЛНЕНА ОПЕРАЦИЯ "ИДЕНТИФИКАЦИЯ ФЛ" ДЛЯ НАЛОГОВОЙ ФОРМЫ%' AND ntf.up_text NOT LIKE '%ОТМЕН%')
UNION ALL
SELECT 
  ntf.id, 
  '&outpath\PB\PB_'||TO_CHAR(REPLACE(dd.file_name,'.xml'))||'_'||ntf.num_form||'_'
  ||CASE WHEN (SELECT COUNT(id) FROM notification WHERE id = ntf.id AND ntf.up_text LIKE '%ЗАГРУЗКА ФАЙЛА%' AND ntf.up_text LIKE '%ВЫПОЛНЕНО СОЗДАНИЕ НАЛОГОВОЙ ФОРМЫ%' AND ntf.up_text LIKE '%.XML%') > 0 THEN 'Zagr' 
         WHEN (SELECT COUNT(id) FROM notification WHERE id = ntf.id AND ntf.up_text LIKE '%ОШИБКА ЗАГРУЗКИ ФАЙЛА%' AND ntf.up_text LIKE '%.XML%') > 0 THEN 'Zagr' 
         WHEN (SELECT COUNT(id) FROM notification WHERE id = ntf.id AND ntf.up_text LIKE '%ВЫПОЛНЕНА ПРОВЕРКА НАЛОГОВОЙ ФОРМЫ%' AND ntf.up_text NOT LIKE '%ФАТАЛЬ%' AND ntf.up_text NOT LIKE '%ОТМЕН%') > 0 THEN 'Prov' 
         WHEN (SELECT COUNT(id) FROM notification WHERE id = ntf.id AND ntf.up_text LIKE '%ВЫПОЛНЕНА ПРОВЕРКА НАЛОГОВОЙ ФОРМЫ%' AND ntf.up_text LIKE '%ФАТАЛЬ%' AND ntf.up_text NOT LIKE '%ОТМЕН%') > 0 THEN 'Prov' 
         WHEN (SELECT COUNT(id) FROM notification WHERE id = ntf.id AND ntf.up_text LIKE '%НЕ ВЫПОЛНЕНА ОПЕРАЦИЯ "ПРОВЕРКА" ДЛЯ НАЛОГОВОЙ ФОРМЫ%' AND ntf.up_text NOT LIKE '%ОТМЕН%') > 0 THEN 'Prov'
         WHEN (SELECT COUNT(id) FROM notification WHERE id = ntf.id AND ntf.up_text LIKE '%ОПЕРАЦИЯ "ИДЕНТИФИКАЦИЯ ФЛ" ВЫПОЛНЕНА ДЛЯ НАЛОГОВОЙ ФОРМЫ%' AND ntf.up_text NOT LIKE '%ОТМЕН%') > 0 THEN 'Id'
         WHEN (SELECT COUNT(id) FROM notification WHERE id = ntf.id AND ntf.up_text LIKE '%НЕ ВЫПОЛНЕНА ОПЕРАЦИЯ "ИДЕНТИФИКАЦИЯ ФЛ" ДЛЯ НАЛОГОВОЙ ФОРМЫ%' AND ntf.up_text NOT LIKE '%ОТМЕН%') > 0 THEN 'Id'
        ELSE '' END||'_'
  ||CASE WHEN (SELECT COUNT(id) FROM notification WHERE id = ntf.id AND ntf.up_text LIKE '%ЗАГРУЗКА ФАЙЛА%' AND ntf.up_text LIKE '%ВЫПОЛНЕНО СОЗДАНИЕ НАЛОГОВОЙ ФОРМЫ%' AND ntf.up_text LIKE '%.XML%') > 0 THEN 'B' 
         WHEN (SELECT COUNT(id) FROM notification WHERE id = ntf.id AND ntf.up_text LIKE '%ОШИБКА ЗАГРУЗКИ ФАЙЛА%' AND ntf.up_text LIKE '%.XML%') > 0 THEN 'H' 
         WHEN (SELECT COUNT(id) FROM notification WHERE id = ntf.id AND ntf.up_text LIKE '%ВЫПОЛНЕНА ПРОВЕРКА НАЛОГОВОЙ ФОРМЫ%' AND ntf.up_text NOT LIKE '%ФАТАЛЬ%' AND ntf.up_text NOT LIKE '%ОТМЕН%') > 0 THEN 'B' 
         WHEN (SELECT COUNT(id) FROM notification WHERE id = ntf.id AND ntf.up_text LIKE '%ВЫПОЛНЕНА ПРОВЕРКА НАЛОГОВОЙ ФОРМЫ%' AND ntf.up_text LIKE '%ФАТАЛЬ%' AND ntf.up_text NOT LIKE '%ОТМЕН%') > 0 THEN 'BF' 
         WHEN (SELECT COUNT(id) FROM notification WHERE id = ntf.id AND ntf.up_text LIKE '%НЕ ВЫПОЛНЕНА ОПЕРАЦИЯ "ПРОВЕРКА" ДЛЯ НАЛОГОВОЙ ФОРМЫ%' AND ntf.up_text NOT LIKE '%ОТМЕН%') > 0 THEN 'H'
         WHEN (SELECT COUNT(id) FROM notification WHERE id = ntf.id AND ntf.up_text LIKE '%ОПЕРАЦИЯ "ИДЕНТИФИКАЦИЯ ФЛ" ВЫПОЛНЕНА ДЛЯ НАЛОГОВОЙ ФОРМЫ%' AND ntf.up_text NOT LIKE '%ОТМЕН%') > 0 THEN 'B'
         WHEN (SELECT COUNT(id) FROM notification WHERE id = ntf.id AND ntf.up_text LIKE '%НЕ ВЫПОЛНЕНА ОПЕРАЦИЯ "ИДЕНТИФИКАЦИЯ ФЛ" ДЛЯ НАЛОГОВОЙ ФОРМЫ%' AND ntf.up_text NOT LIKE '%ОТМЕН%') > 0 THEN 'H'
        ELSE '' END 
  ||'_'||TO_CHAR(ntf.id)||'.csv',
  'SELECT ''"№ п/п";"Дата-время";"Тип сообщения";"Текст сообщения";"Тип";"Объект"'' FROM DUAL;',
  'SELECT TO_CHAR(ord)||'';''||TO_CHAR(creation_date,''DD.MM.YYYY hh24:mi:ss'')||'';''||CASE WHEN log_level = 0 THEN ''Информация'' WHEN log_level = 1 THEN ''предупреждение'' WHEN log_level = 2 THEN ''ошибка'' ELSE '''' END||'';''||TO_CHAR(message)||'';''||TO_CHAR(type)||'';''||TO_CHAR(object) FROM log_entry WHERE log_id = ''' ||TO_CHAR(ntf.log_id)||''';'
FROM (
      SELECT 
        nt.user_id,
        nt.id, 
        nt.log_id,
        UPPER(nt.text) AS up_text,
        CASE WHEN INSTR(nt.text,'№') > 0 THEN SUBSTR(REPLACE(REPLACE(REPLACE(nt.text,'"'),':'),' '),INSTR(REPLACE(REPLACE(REPLACE(nt.text,'"'),':'),' '),'№')+1,INSTR(REPLACE(REPLACE(REPLACE(nt.text,'"'),':'),' '),',',INSTR(REPLACE(REPLACE(REPLACE(nt.text,'"'),':'),' '),'№')+1)-(INSTR(REPLACE(REPLACE(REPLACE(nt.text,'"'),':'),' '),'№')+1)) ELSE '' END AS num_form
      FROM notification nt
      WHERE nt.user_id IN &users_list 
            AND nt.create_date BETWEEN TO_DATE('&start_date','DD.MM.YYYY hh24:mi:ss') AND TO_DATE('&end_date','DD.MM.YYYY hh24:mi:ss')
            AND (text LIKE '%"Поволжский банк"%' OR text LIKE '%54_0000_00%') ) ntf
LEFT OUTER JOIN declaration_data dd ON dd.id = ntf.num_form
WHERE (ntf.up_text LIKE '%ЗАГРУЗКА ФАЙЛА%' AND ntf.up_text LIKE '%ВЫПОЛНЕНО СОЗДАНИЕ НАЛОГОВОЙ ФОРМЫ%' AND ntf.up_text LIKE '%.XML%')
      OR (ntf.up_text LIKE '%ОШИБКА ЗАГРУЗКИ ФАЙЛА%' AND ntf.up_text LIKE '%.XML%')
      OR (ntf.up_text LIKE '%ВЫПОЛНЕНА ПРОВЕРКА НАЛОГОВОЙ ФОРМЫ%' AND ntf.up_text NOT LIKE '%ФАТАЛЬ%' AND ntf.up_text NOT LIKE '%ОТМЕН%')
      OR (ntf.up_text LIKE '%ВЫПОЛНЕНА ПРОВЕРКА НАЛОГОВОЙ ФОРМЫ%' AND ntf.up_text LIKE '%ФАТАЛЬ%' AND ntf.up_text NOT LIKE '%ОТМЕН%')
      OR (ntf.up_text LIKE '%НЕ ВЫПОЛНЕНА ОПЕРАЦИЯ "ПРОВЕРКА" ДЛЯ НАЛОГОВОЙ ФОРМЫ%' AND ntf.up_text NOT LIKE '%ОТМЕН%')
      OR (ntf.up_text LIKE '%ОПЕРАЦИЯ "ИДЕНТИФИКАЦИЯ ФЛ" ВЫПОЛНЕНА ДЛЯ НАЛОГОВОЙ ФОРМЫ%' AND ntf.up_text NOT LIKE '%ОТМЕН%')
      OR (ntf.up_text LIKE '%НЕ ВЫПОЛНЕНА ОПЕРАЦИЯ "ИДЕНТИФИКАЦИЯ ФЛ" ДЛЯ НАЛОГОВОЙ ФОРМЫ%' AND ntf.up_text NOT LIKE '%ОТМЕН%')
)
SELECT  
     'spool '||filename||chr(13)||sqlquery_header||chr(13)||sqlquery||chr(13)||'spool off;'
FROM TMP_NOTIF;
  
SELECT 'EXIT;' FROM DUAL;
spool off;	

@Script_GeNotificationToFile.sql;
	
exit; 


    
    
    
