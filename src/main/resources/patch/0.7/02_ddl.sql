-- Удаление ошибочно существующих невалидных PL/SQL-объектов, относящихся к TAX_RNU
set serveroutput on size 30000;

declare query_str varchar2(1024) := '';
begin
for x in (select * from user_objects where object_name like 'APL%' or object_name like 'NSI%' and object_type in ('PROCEDURE', 'FUNCTION')) loop
	if x.status = 'VALID' then --Better safe than sorry
		dbms_output.put_line('Valid object '||x.object_name||' ('||x.object_type||') will not be deleted.');
	end if;
	if x.status = 'INVALID' then
		query_str := 'DROP '||x.object_type||' '||x.object_name;
		dbms_output.put_line(query_str||'.');
		commit;
		execute immediate query_str;
	end if;
end loop;
end;
/

--http://jira.aplana.com/browse/SBRFACCTAX-12250: Наименование узла кластера, на котором выполняется связанная асинхронная задача
alter table lock_data add SERVER_NODE varchar2(100);
comment on column lock_data.server_node is 'Наименование узла кластера, на котором выполняется связанная асинхронная задача';

-- http://jira.aplana.com/browse/SBRFACCTAX-12090: Добавить в FORM_DATA признак актуальности сортировки и счетчик количества пронумерованных строк текущей НФ
alter table form_data add sorted number(1) default 0 not null;
alter table form_data add constraint form_data_chk_sorted check (sorted in (0, 1));
comment on column form_data.sorted is 'Признак актуальности сортировки';

alter table form_data add number_current_row number(9);
comment on column form_data.number_current_row is 'Количество пронумерованных строк текущей НФ';

-- http://jira.aplana.com/browse/SBRFACCTAX-12112: Инициализировать поле FORM_DATA.NUMBER_CURRENT_ROW
declare query_str varchar2(512);
begin
for x in (select fd.id as form_data_id, ft.id as form_template_id
      from form_template ft 
      join form_data fd on fd.form_template_id = ft.id
      where exists (select 1 from user_tab_columns utc where table_name = 'FORM_DATA_'||ft.id and column_name = 'ALIAS') and
            exists (select 1 from form_column where form_template_id = ft.id and type = 'A')
      ) loop
query_str := 'UPDATE FORM_DATA SET number_current_row = (SELECT count(*) FROM FORM_DATA_'||x.form_template_id||' WHERE form_data_id = '||x.form_data_id||' AND (alias IS NULL OR alias LIKE ''%{wan}%'')) WHERE ID = '||x.form_data_id;
execute immediate query_str;      
end loop;
end;
/
COMMIT;

-- http://jira.aplana.com/browse/SBRFACCTAX-12103: Ограничения на form_data_ref_book
alter table form_data_ref_book add constraint form_data_ref_book_fk_refbook foreign key (ref_book_id) references ref_book(id) on delete cascade;

-- http://jira.aplana.com/browse/SBRFACCTAX-12214
alter table form_template add comparative number(1);
comment on column form_template.comparative is '"Признак использования периода сравнения (0 - не используется, 1 - используется)';
alter table form_template add constraint form_template_chk_comparative check (comparative in (0, 1));

-- http://jira.aplana.com/browse/SBRFACCTAX-12216
alter table form_data add (comparative_dep_rep_per_id number(18), accruing number(1));
comment on column form_data.comparative_dep_rep_per_id is 'Период сравнения';
comment on column form_data.accruing is 'Признак расчета значений нарастающим итогом (0 - не нарастающим итогом, 1 - нарастающим итогом, пустое - форма без периода сравнения)';
alter table form_data add constraint form_data_fk_co_dep_rep_per_id foreign key (comparative_dep_rep_per_id) references department_report_period (id);
alter table form_data add constraint form_data_chk_accruing check (accruing in (0, 1));

-- http://jira.aplana.com/browse/SBRFACCTAX-12153: Ошибка при удалении периода
CREATE OR REPLACE TRIGGER DEP_REP_PER_BEFORE_INS_UPD 
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

CREATE OR REPLACE TRIGGER DEP_REP_PER_BEFORE_DELETE for delete on department_report_period 
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


COMMIT;
EXIT;


