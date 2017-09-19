create or replace 
trigger "DEP_REP_PER_BEFORE_DELETE" for delete on department_report_period
COMPOUND TRIGGER

    TYPE t_change_tab IS TABLE OF DEPARTMENT_REPORT_PERIOD%ROWTYPE;
    g_change_tab  t_change_tab := t_change_tab();

  BEFORE EACH ROW IS
    vCurrentID number(18) := :old.id;
    vCurrentDepartmentID number(9) := :old.department_id;
    vCurrentReportPeriodID number(9) := :old.report_period_id;
    vCurrentCorrectionDate date := :old.correction_date;
    vHasLinks number(1);
  BEGIN
    g_change_tab.extend;

    g_change_tab(g_change_tab.last).id      := :old.id;
    g_change_tab(g_change_tab.last).department_id := :old.department_id;
    g_change_tab(g_change_tab.last).report_period_id := :old.report_period_id;
    g_change_tab(g_change_tab.last).correction_date := :old.correction_date;

    --Проверка существования ссылок из DECLARATION_DATA
         select coalesce(max(tHasLinks), 0) into vHasLinks from (
          select 1 as tHasLinks
            from dual
            where exists
              (select 1
               from declaration_data
               where department_report_period_id = vCurrentID));

       -- Запрет изменений, если на запись существуют ссылки
         if vHasLinks = 1 then
            raise_application_error(-20003, 'Нельзя удалить период, если на него есть ссылка в FORM_DATA или DECLARATION_DATA');
         end if;
  END BEFORE EACH ROW;

  AFTER STATEMENT IS
  vMaxCorrectionDatePerGroup date;
  BEGIN

  FOR i IN 1 .. g_change_tab.count LOOP
      select max(trunc(correction_date)) into vMaxCorrectionDatePerGroup
           from department_report_period
           where department_id = g_change_tab(i).department_id
                 and report_period_id = g_change_tab(i).report_period_id;

      --Проверки при удалении
       --Нельзя удалить период, если для него есть корректирующий период
         if (g_change_tab(i).correction_date is null and vMaxCorrectionDatePerGroup is not null) then
              raise_application_error(-20001, 'Нельзя удалить период, если для него есть корректирующий период');
         end if;
       --Нельзя удалить корректирующий период, если есть корректирующий период с более поздней датой корректировки
         if (g_change_tab(i).correction_date is not null and g_change_tab(i).correction_date < vMaxCorrectionDatePerGroup) then
              raise_application_error(-20002, 'Нельзя удалить корректирующий период, если есть корректирующий период с более поздней датой корректировки');
         end if;

  END LOOP;

  g_change_tab.delete;
  END AFTER STATEMENT;
end dep_rep_per_before_delete;
/
show errors;