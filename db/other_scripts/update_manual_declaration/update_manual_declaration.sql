set serveroutput on;
DECLARE
-- Параметры скрипта
 v_tb_index VARCHAR2(100) := 'TB_INDEX'; -- индекс территориального банка Территориальный банка, для которого выполняется перенос 
 v_asnu VARCHAR2(100) := '1000'; -- АСНУ
BEGIN
FOR c1 IN (
            select 
            pdrp.id TB_DEPARTMENT_REPORT_PERIOD_ID,
            pdrp.department_id tb_department_id, 
            pdrp.report_period_id, 
            pdrp.is_active, 
            nvl(pdrp.correction_date, trunc(sysdate)) correction_date,
            pdp.name tb_name
            from 
            DEPARTMENT_REPORT_PERIOD pdrp,
            TAX_UNSTABLE.DEPARTMENT pdp
            where 
            pdp.id = pdrp.department_id
            and pdp.type = 2 -- Тип подразделения = "Территориальный банк"
            and pdrp.is_active = 1 -- Период  открыт
            and pdp.tb_index = v_tb_index
          )
LOOP

    FOR c2 IN (
                select
                dd.id declaration_id,
                dd.department_report_period_id,
                drp.department_id,
                dp.name dep_name
                from
                declaration_data dd,
                REF_BOOK_ASNU rba,
                DEPARTMENT_REPORT_PERIOD drp,
                TAX_UNSTABLE.DEPARTMENT dp
                where
                dd.asnu_id = rba.id 
                and rba.code=v_asnu -- Форма.АСНУ = АСНУ
                and dd.declaration_template_id = 100 -- Форма.Вид формы = "РНУ НДФЛ (первичная)"
                and dd.manually_created = 1 -- Форма создана вручную
                and drp.id = dd.department_report_period_id
                and dp.id = drp.department_id
                and dp.type <> 2 -- не ТБ
                and dp.parent_id = c1.tb_department_id -- подразделение ТБ
                and drp.report_period_id = c1.report_period_id
                and drp.is_active = c1.is_active
                and nvl(drp.correction_date, trunc(sysdate)) = c1.correction_date
              )
    LOOP
       update declaration_data dd
       set dd.department_report_period_id = c1.tb_department_report_period_id
       where dd.id = c2.declaration_id;
      
       dbms_output.put_line('Форма '||to_char(c2.declaration_id) 
                            || ': прежнее подразделение - ' || c2.dep_name || ' (department_report_period_id=' || to_char(c2.department_report_period_id) || ')'
                            || ': новое подразделение - ' || c1.tb_name || ' (department_report_period_id=' || to_char(c1.tb_department_report_period_id) || ')'
                           );
    END LOOP;

END LOOP;

END;
/
COMMIT;
exit;
