create or replace TRIGGER DEPARTMENT_BEFORE_DELETE for delete on department
COMPOUND TRIGGER

    TYPE t_change_tab IS TABLE OF DEPARTMENT%ROWTYPE;
    g_change_tab  t_change_tab := t_change_tab();

  BEFORE EACH ROW IS
    vCurrentDepartmentID number(9) := :old.id;
    vCurrentDepartmentType number(9) := :old.type;
  BEGIN
    g_change_tab.extend;
    g_change_tab(g_change_tab.last).id      := :old.id;

    --Подразделение с типом "Банк"(type=1) не может быть удалено
		if vCurrentDepartmentType=1 then
		   raise_application_error(-20001, 'Подразделение с типом "Банк" не может быть удалено');
		end if;

  END BEFORE EACH ROW;

  AFTER STATEMENT IS
  vHasLinks number(9) := -1;
  vHasDescendant number(9) := -1;
  BEGIN

  FOR i IN 1 .. g_change_tab.count LOOP
    --Существуют дочерние подразделения
		select count(*) into vHasDescendant
		from department
		start with parent_id = g_change_tab(i).id
		connect by parent_id = prior id;

		if vHasDescendant != 0 then
		   raise_application_error(-20002, 'Подразделение, имеющее дочерние подразделения, не может быть удалено');
		end if;

    --Ссылочная целостность
		--FORM_DATA
		select count(*) into  vHasLinks from form_data fd
		join department_report_period drp on (drp.id = fd.department_report_period_id or drp.id = fd.comparative_dep_rep_per_id) and drp.department_id = g_change_tab(i).id;

		if vHasLinks !=0 then
		   raise_application_error(-20003, 'Подразделение не может быть удалено, если на него существует ссылка в FORM_DATA');
		end if;

		--DECLARATION_DATA
		select count(*) into  vHasLinks from declaration_data dd
		join department_report_period drp on drp.id = dd.department_report_period_id and drp.department_id = g_change_tab(i).id;

		if vHasLinks !=0 then
		   raise_application_error(-20004, 'Подразделение не может быть удалено, если на него существует ссылка в DECLARATION_DATA');
		end if;

		--SEC_USER
		select count(*) into  vHasLinks from sec_user where department_id = g_change_tab(i).id;

		if vHasLinks !=0 then
		   raise_application_error(-20005, 'Подразделение не может быть удалено, если на него существует ссылка в SEC_USER');
		end if;

     --REF_BOOK_VALUE
		select count(*) into vHasLinks from ref_book_value rbv
		join ref_book_attribute rba on rba.id = rbv.attribute_id and rba.reference_id = 30
		where rbv.reference_value = g_change_tab(i).id;

		if vHasLinks !=0 then
		   raise_application_error(-20006, 'Подразделение не может быть удалено, если на него существует ссылка в REF_BOOK_VALUE');
		end if;

	  --FORM_DATA_REF_BOOK
		select count(*) into vHasLinks
		from form_data_ref_book
		where ref_book_id = 30 and record_id = g_change_tab(i).id;

		if vHasLinks !=0 then
		   raise_application_error(-20006, 'Подразделение не может быть удалено, если на него существует ссылка в FORM_DATA_REF_BOOK');
		end if;

  END LOOP;

  g_change_tab.delete;
  END AFTER STATEMENT;
end DEPARTMENT_BEFORE_DELETE;
/

create or replace TRIGGER DEPARTMENT_BEFORE_INS_UPD
  before insert or update on department
  for each row
declare
  pragma autonomous_transaction;

  vCurrentDepartmentID number(9) := :new.id;
  vCurrentDepartmentType number(9) := :new.type;
  vFormerDepartmentType number(9) := :old.type;
  vCurrentDepartmentIsActive number(1) := :new.is_active;
  vCurrentDepartmentSbrfCode varchar2(255) := :new.sbrf_code;

  vParentDepartmentID number(9) := :new.parent_id;
  vFormerParentDepartmentID number(9) := :old.parent_id;
  vParentDepartmentIsActive number(1) := -1;
  vParentDepartmentType number(9) := -1;

  vTBHasChanged number(1) := -1;
  vAmITheOnlyOne number(9) := -1;
  vHasLinks number(9) := -1;
  vHasActiveDescendant number(9) := -1;
  vIsSbrfCodeUnique number(9) := -1;
  vHasLoop number(1) := -1;
begin
  -- Получение данных о (новом) родителе
  if vParentDepartmentID is not null then
    select is_active, type into vParentDepartmentIsActive, vParentDepartmentType
    from department
    where id =  vParentDepartmentID;
  end if;

  -- Общие проверки при обновлении/добавлении записей
  if vCurrentDepartmentIsActive = 1 and vParentDepartmentID is not null and vParentDepartmentIsActive <> 1 then
     raise_application_error(-20101, 'Подразделение с признаком IS_ACTIVE=1 не может иметь родительское подразделение с признаком IS_ACTIVE=0');
  end if;
  -------------------------------------------------------------------
  if updating('is_active') and vCurrentDepartmentIsActive = 0 then
    select count(*) into vHasActiveDescendant
    from department
    where is_active = 1
    start with parent_id = vCurrentDepartmentID
    connect by parent_id = prior id;

    if vHasActiveDescendant <> 0 then
       raise_application_error(-20102, 'Неактивное подразделение не может иметь активные дочерние подразделения');
    end if;
  end if;

  -------------------------------------------------------------------

  if vCurrentDepartmentType = 1 and vParentDepartmentID is not null then
     raise_application_error(-20103, 'Подразделение типа "Банк" не может иметь родительское подраздение');
  end if;

  -------------------------------------------------------------------

  if vCurrentDepartmentType <> 1 and vParentDepartmentID is null then
     raise_application_error(-20104, 'Все подразделения (за исключение типа "Банк") должны иметь родительское подраздение');
  end if;

  -------------------------------------------------------------------
  select count(*) into vAmITheOnlyOne
  from department
  where id <>  vCurrentDepartmentID and type = 1;

  if vCurrentDepartmentType = 1 and vAmITheOnlyOne <> 0  then
     raise_application_error(-20105, 'Возможно существование только одного подразделения с типом "Банк"');
  end if;

  -------------------------------------------------------------------

  if vCurrentDepartmentType = 2 and vParentDepartmentType <> 1 then
     raise_application_error(-20106, 'Подразделение с "ТБ" должно иметь родительское подразделение с типом "Банк"');
  end if;

  -------------------------------------------------------------------
  select count(distinct sbrf_code) into vIsSbrfCodeUnique
  from department
  where is_active = 1 and upper(sbrf_code) = upper(vCurrentDepartmentSbrfCode) and id <> vCurrentDepartmentID;

  if vIsSbrfCodeUnique <> 0 then
     raise_application_error(-20107, 'Значение атрибута "Код подразделения в нотации СБРФ" не уникально среди активных подразделений');
  end if;

  -------------------------------------------------------------------
  if vFormerDepartmentType = 1 and vCurrentDepartmentType <> 1 then
     raise_application_error(-20108, 'Для подразделения с типом "Банк" атрибут типа не может быть изменен');
  end if;
  -------------------------------------------------------------------
  if updating('type') and vFormerDepartmentType = 2 and vCurrentDepartmentType <> 2  then
    select count(*) into vHasLinks from department_report_period drp
    join report_period rp on rp.id = drp.report_period_id
    join tax_period tp on tp.id = rp.tax_period_id and tp.tax_type in ('P', 'T')
    where drp.department_id = vCurrentDepartmentID;

    if vHasLinks <> 0 then
       raise_application_error(-20109, 'Операция смена типа для подразделение уровня "ТБ" недопустима, если существуют зависимые данные в DEPARTMENT_REPORT_PERIOD для налогов на транспорт и имущество');
    end if;
  end if;

  -------------------------------------------------------------------
  if updating('parent_id') and vParentDepartmentID<>vFormerParentDepartmentID then
    select count(distinct id) into vTBHasChanged
    from department
    where type = 2
    start with id in (vParentDepartmentID, vFormerParentDepartmentID)
    connect by id = prior parent_id;

    if vTBHasChanged > 1 then
       raise_application_error(-20110, 'Подразделение не может быть перенесено в поддерево другого ТБ');
    end if;
  end if;

  -------------------------------------------------------------------
  if updating('type') and vFormerDepartmentType in (3, 4) and vCurrentDepartmentType not in (3, 4) then
     select count(*) into vHasLinks
     from sec_user
     where department_id = vCurrentDepartmentID;

     if vHasLinks <> 0 then
        raise_application_error(-20111, 'Операции смена типа для подразделений с типами "ЦСКО, ПЦП", "Управление" недопустимы, если существуют связанные записи в SEC_USER');
     end if;
  end if;

  -------------------------------------------------------------------
  if updating('parent_id') and vParentDepartmentID<>vFormerParentDepartmentID then
     select case when exists
      (select 1
      from department
      where id = vParentDepartmentID
      start with id = vCurrentDepartmentID
      connect by nocycle parent_id = prior id) then 1 else 0 end into vHasLoop
      from dual;

      if vHasLoop = 1 then
        raise_application_error(-20112, 'Подразделение не может входить в иерархию своих дочерних подразделений');
     end if;

  end if;


  -------------------------------------------------------------------
end DEPARTMENT_BEFORE_INS_UPD;
/

create or replace TRIGGER DEP_REP_PER_BEFORE_INS_UPD
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
  vCurrentIsBalancePeriod number(1) := :new.is_balance_period;
  vFormerIsBalancePeriod number(1) := :old.is_balance_period;
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
    if vFormerIsActive <> 1 and vCurrentIsActive <> 1 and (updating('IS_BALANCE_PERIOD') or updating('CORRECTION_DATE')) then
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
     if vHasLinks = 1 and (updating('IS_BALANCE_PERIOD') and vFormerIsBalancePeriod<>vCurrentIsBalancePeriod)  then
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

create or replace TRIGGER DEP_REP_PER_BEFORE_DELETE for delete on department_report_period
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

exit;