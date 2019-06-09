create or replace 
trigger DEP_REP_PER_BEFORE_INS_UPD
  before insert or update on department_report_period
  for each row
declare
  pragma autonomous_transaction;

  vCurrentID number(18) := :new.id;
  vCurrentDepartmentID number(9) := :new.department_id;
  vCurrentReportPeriodID number(9) := :new.report_period_id;
  vFormerDepartmentID number(9) := :old.department_id;
  vFormerReportPeriodID number(9) := :old.report_period_id;
  vCurrentIsActive number(1) := :new.is_active;
  vFormerIsActive number(1) := :old.is_active;
  vCurrentCorrectionDate date := trunc(:new.correction_date);
  vFormerCorrectionDate date := trunc(:old.correction_date);
  vMaxCorrectionDatePerGroup date;
  vHasLinks number(1);
  vHasInBetweenPeriods number(1);
  vHasConflictPeriods number(1);
  vHasOtherBalancePeriod number(1);
  vCntOpenPeriods number(9);
begin
  if updating then
     -- --Операция недопустима, если она изменяет значение REPORT_PERIOD_ID или DEPARTMENT_ID
     if vCurrentDepartmentID <> vFormerDepartmentID or vCurrentReportPeriodID <> vFormerReportPeriodID then
        raise_application_error(-20100, 'Операция недопустима, если она изменяет значение REPORT_PERIOD_ID или DEPARTMENT_ID');
     end if;
  end if;

  --------------------------------------------------------------------------------------------
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
    if vFormerIsActive <> 1 and vCurrentIsActive <> 1 and updating('CORRECTION_DATE') then
       raise_application_error(-20106, 'Нельзя изменить период, если он не открыт');
    end if;

     -- -- Проверка существования ссылок из FORM_DATA, DECLARATION_DATA
     select coalesce(max(tHasLinks), 0) into vHasLinks from (
      select 1 as tHasLinks
        from dual
        where exists
          (select 1
           from declaration_data
           where department_report_period_id = vCurrentID));

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
show errors;