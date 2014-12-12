CREATE OR REPLACE TRIGGER "DEP_REP_PER_BEFORE_DELETE" 
for delete on department_report_period
COMPOUND TRIGGER

    TYPE t_change_tab IS TABLE OF DEPARTMENT_REPORT_PERIOD%ROWTYPE;
    g_change_tab  t_change_tab := t_change_tab();
    
  BEFORE EACH ROW IS
    vCurrentID number(9) := :old.id;
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
    
    --Проверка существования ссылок из FORM_DATA, DECLARATION_DATA
         select coalesce(max(tHasLinks), 0) into vHasLinks from (
          select 1 as tHasLinks
            from dual
            where exists
              (select 1
               from form_data
               where department_report_period_id = vCurrentID)
             or exists
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
  
  FOR i IN g_change_tab.first .. g_change_tab.last LOOP
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

CREATE OR REPLACE TRIGGER DEP_REP_PER_BEFORE_INS_UPD
  before insert or update on department_report_period  
  for each row
declare
  pragma autonomous_transaction;

  vCurrentID number(9) := :new.id;
  vCurrentDepartmentID number(9) := :new.department_id;
  vCurrentReportPeriodID number(9) := :new.report_period_id;
  vFormerDepartmentID number(9) := :old.department_id;
  vFormerReportPeriodID number(9) := :old.report_period_id;
  vCurrentIsActive number(1) := :new.is_active;
  vFormerIsActive number(1) := :old.is_active;
  vCurrentIsBalancePeriod number(1) := :new.is_balance_period;
  vCurrentCorrectionDate date := trunc(:new.correction_date);
  vFormerCorrectionDate date := trunc(:old.correction_date);
  vMaxCorrectionDatePerGroup date;
  vHasLinks number(1);  
  vHasInBetweenPeriods number(1);
  vHasConflictPeriods number(1);  
  vHasOtherBalancePeriod number(1); 
  vCntOpenPeriods number(1);                 
begin
  if updating then
     -- --Операция недопустима, если она изменяет значение REPORT_PERIOD_ID или DEPARTMENT_ID
     if vCurrentDepartmentID <> vFormerDepartmentID or vCurrentReportPeriodID <> vFormerReportPeriodID then
        raise_application_error(-20100, 'Операция недопустима, если она изменяет значение REPORT_PERIOD_ID или DEPARTMENT_ID');
     end if;
  end if;

  --------------------------------------------------------------------------------------------
  -- Общие проверки при добавлении и обновлении
  -- --  Корр. периоду не может быть присвоен признак ввода остатков
  if vCurrentCorrectionDate is not null and vCurrentIsBalancePeriod = 1 then
     raise_application_error(-20101, 'Корректирующему периоду не может быть присвоен признак ввода остатков');
  end if;
  
  -- -- Если есть период ввода остатков, то других периодов не может быть (is_balance_period)
  select coalesce(max(tHasBalancePeriod), 0) into vHasOtherBalancePeriod
          from (select 1 as tHasBalancePeriod
            from dual
            where exists (select 1 
                         from department_report_period
                         where department_id = vCurrentDepartmentID
                               and report_period_id = vCurrentReportPeriodID
                               and id <> vCurrentID 
                               and is_balance_period = 1
                               and vCurrentIsBalancePeriod <> 1));
   
  if vHasOtherBalancePeriod = 1 then
     raise_application_error(-20102, 'Если есть период ввода остатков, то других периодов не может быть');
  end if; 
  
  select max(trunc(correction_date)) into vMaxCorrectionDatePerGroup 
           from department_report_period 
           where department_id = vCurrentDepartmentID 
                 and report_period_id = vCurrentReportPeriodID;
                 
  select count(id) into vCntOpenPeriods 
           from department_report_period 
           where department_id = vCurrentDepartmentID 
                 and report_period_id = vCurrentReportPeriodID
                 and id <> vCurrentID
                 and is_active = 1;                             
                   
  -- -- Если для периода есть корректирующий период, то ему не может быть присвоен признак ввода остатков
  if vCurrentCorrectionDate is null and vMaxCorrectionDatePerGroup is not null and vCurrentIsBalancePeriod = 1 then
     raise_application_error(-20103, 'Если для периода есть корректирующий период, то ему не может быть присвоен признак ввода остатков');
  end if;  
  
  -- -- Проверки на открытый период
  if (vCurrentIsActive = 1 and (vCntOpenPeriods <> 0 or vCurrentCorrectionDate < vMaxCorrectionDatePerGroup)) then
    raise_application_error(-20104, 'Открытым может быть только один период, при этом он обязательно последний из корректирующих');
  end if; 
  
  -- -- Запрос на уникальность даты корректировки
  select coalesce(max(tHasConflictPeriods), 0) into vHasConflictPeriods
          from (select 1 as tHasConflictPeriods
            from dual
            where exists (select 1 
                         from department_report_period
                         where department_id = vCurrentDepartmentID
                               and report_period_id = vCurrentReportPeriodID
                               and id <> vCurrentID 
                               and trunc(correction_date) = vCurrentCorrectionDate));
   
  if vHasConflictPeriods = 1 then
     raise_application_error(-20105, 'Дата корректировки должна быть уникальна');
  end if; 
  
  --------------------------------------------------------------------------------------------
  --Проверки при обновлении (исключительно)
  if updating then
    begin
     -- -- Нельзя изменить период, если он не открыт
    if vFormerIsActive <> 1 and vCurrentIsActive <> 1 then
       raise_application_error(-20106, 'Нельзя изменить период, если он не открыт');
    end if;
    
     -- -- Проверка существования ссылок из FORM_DATA, DECLARATION_DATA
     select coalesce(max(tHasLinks), 0) into vHasLinks from (
      select 1 as tHasLinks
        from dual
        where exists
          (select 1
           from form_data
           where department_report_period_id = vCurrentID)
         or exists
          (select 1
           from declaration_data
           where department_report_period_id = vCurrentID));    
           
     -- Запрет изменений, если на запись существуют ссылки
     if vHasLinks = 1 and not(vFormerIsActive = 1 and vCurrentIsActive = 0) then
        raise_application_error(-20107, 'Нельзя изменить период, если на него есть ссылка в FORM_DATA или DECLARATION_DATA');
     end if;       
     
     -- Нельзя удалить дату корректировки
     if vCurrentCorrectionDate is null and vFormerCorrectionDate is not null then
        raise_application_error(-20108, 'Нельзя удалить дату корректировки');
     end if;
     
     -- Нельзя изменить дату корректировки так, чтобы она стала меньше даты корректировки другого корр. периода в этой же цепочке периодов, если раньше она была больше этой даты
     if vFormerCorrectionDate > vCurrentCorrectionDate then 
        begin 
          select coalesce(max(tHasInBetweenPeriods), 0) into vHasInBetweenPeriods
          from (select 1 as tHasInBetweenPeriods
            from dual
            where exists (select 1 
                         from department_report_period
                         where department_id = vCurrentDepartmentID
                               and report_period_id = vCurrentReportPeriodID
                               and id <> vCurrentID 
                               and trunc(correction_date) between vCurrentCorrectionDate and vFormerCorrectionDate));
            
            if vHasInBetweenPeriods = 1 then
               raise_application_error(-20109, 'Нельзя изменить дату корректировки так, чтобы она стала меньше даты корректировки другого корр. периода в этой же цепочке периодов, если раньше она была больше этой даты');
            end if;   
        end;
     end if;
     
     --Нельзя изменить период, если для него есть корректирующий период
     if (vCurrentCorrectionDate is null and vMaxCorrectionDatePerGroup is not null) then   
          raise_application_error(-20001, 'Нельзя изменить период, если для него есть корректирующий период'); 
     end if;                                       
     --Нельзя изменить корректирующий период, если есть корректирующий период с более поздней датой корректировки
     if (vCurrentCorrectionDate is not null and vCurrentCorrectionDate < vMaxCorrectionDatePerGroup) then   
          raise_application_error(-20002, 'Нельзя изменить корректирующий период, если есть корректирующий период с более поздней датой корректировки'); 
     end if;                      
     end;
  end if; 
  --------------------------------------------------------------------------------------------        
  
end DEP_REP_PER_BEFORE_INS_UPD;
/