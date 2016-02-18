create or replace function BLOB_TO_CLOB(v_blob_in in blob)
return clob is

v_file_clob clob;
v_file_size integer := 1024;
v_dest_offset integer := 1;
v_src_offset integer := 1;
v_blob_csid number :=  nls_charset_id('CL8MSWIN1251'); --dbms_lob.default_csid;
v_lang_context number := dbms_lob.default_lang_ctx;
v_warning integer;
v_size integer := 0;

begin

dbms_lob.createtemporary(v_file_clob, true);

select dbms_lob.getlength(v_blob_in) into v_size from dual;

if (v_size <= v_file_size) then v_file_size := v_size; end if;

dbms_lob.converttoclob(v_file_clob,
v_blob_in,
v_file_size,
v_dest_offset,
v_src_offset,
v_blob_csid,
v_lang_context,
v_warning);

return v_file_clob;

end;
/
---------------------------------------------------------------------------------------
create or replace procedure rebuild_form_data_ref_book
is
    type msg_record is record (err_level varchar2(32), txt varchar2(512));
    type msg is table of msg_record;
    msg_list msg := msg();

    TYPE t_change_tab IS TABLE OF FORM_DATA_REF_BOOK%ROWTYPE;
    g_change_tab  t_change_tab := t_change_tab();
    g_change_tab_temp t_change_tab;
    g_initial_tab t_change_tab := t_change_tab();

    v_str_query varchar2(512) := '';
    v_cursor  SYS_REFCURSOR;
    v_count_deleted number(9) := 0;
    v_count_inserted number(9) := 0;
    v_flag number(1) := 0;
begin
    --Loop by form_template / user_tables
    for x in (select ft.id as ft_id,
                     ft.name as ft_name,
                     ft.version as ft_version,
                     ft.status as ft_status,
                     ut.table_name as table_name
              from form_template ft
              left join user_tables ut on ut.table_name = 'FORM_DATA_'||ft.id
              where exists (select 1 from form_column fc where fc.form_template_id = ft.id and fc.type='R' and fc.parent_column_id is null) order by ut.table_name nulls first) loop
        if (x.table_name is null) then
            msg_list.extend();
            msg_list(msg_list.last).err_level := 'ERROR';
            msg_list(msg_list.last).txt := 'Table not found (Template_ID = '|| x.ft_id ||') ' || x.ft_name || ' (' ||x.ft_version||'), status = '||x.ft_status;
        else
           --loop by form_column
            for y in (select fc.id, fc.alias, fc.attribute_id, rba.ref_book_id, utc.COLUMN_NAME, utc.DATA_TYPE
                      from form_column fc
                      join ref_book_attribute rba on rba.id = fc.attribute_id
                      left join user_tab_columns utc on utc.TABLE_NAME = 'FORM_DATA_'||fc.form_template_id and 'C'||fc.id = utc.COLUMN_NAME
                      where fc.type='R' and fc.parent_column_id is null and fc.form_template_id = x.ft_id
                      order by utc.column_name nulls first) loop

                if (y.column_name is null) then
                    msg_list.extend();
                    msg_list(msg_list.last).err_level := 'ERROR';
                    msg_list(msg_list.last).txt := 'Column not found (Template_ID = '|| x.ft_id ||') column = C' || y.id || ' (' ||y.alias||').';

                elsif (y.data_type <> 'NUMBER') then
                    msg_list.extend();
                    msg_list(msg_list.last).err_level := 'ERROR';
                    msg_list(msg_list.last).txt := 'Wrong datatype for a reference column (Template_ID = '|| x.ft_id ||') column = C' || y.id || ' (' ||y.alias||'): '||y.data_type;

                else

                    v_str_query := 'select distinct form_data_id, '||y.ref_book_id|| ' as ref_book_id, '||y.column_name||' as record_id from form_data_'||x.ft_id||' where '||y.column_name||' is not null ';

                    OPEN v_cursor FOR v_str_query;
                    FETCH v_cursor
                    BULK COLLECT INTO g_change_tab_temp;
                    CLOSE v_cursor;

                    g_change_tab := g_change_tab multiset union g_change_tab_temp;

               end if;
            end loop;
        end if;
    end loop;

  OPEN v_cursor FOR 'SELECT * FROM FORM_DATA_REF_BOOK';
  FETCH v_cursor
  BULK COLLECT INTO g_initial_tab;
  CLOSE v_cursor;

  msg_list.extend();
  msg_list(msg_list.last).err_level := 'INFO ';
  msg_list(msg_list.last).txt := 'Before merge: initial tab ['||g_initial_tab.count||'], new collection (not unique items) ['||g_change_tab.count||']';


  --new records
  for i in 1..g_change_tab.count loop
	  merge into form_data_ref_book tgt
      using (select g_change_tab(i).form_data_id as form_data_id, g_change_tab(i).ref_book_id as ref_book_id, g_change_tab(i).record_id as record_id from dual) src
      on (src.form_data_id = tgt.form_data_id and src.ref_book_id = tgt.ref_book_id and src.record_id = tgt.record_id)
      when not matched then
           insert (tgt.form_data_id, tgt.ref_book_id, tgt.record_id) values (src.form_data_id, src.ref_book_id, src.record_id);

      v_count_inserted := v_count_inserted + sql%rowcount;

  end loop;

  --delete records
  for i in 1..g_initial_tab.count loop
      v_flag := 0;
      for j in 1..g_change_tab.count loop
          if (g_initial_tab(i).form_data_id = g_change_tab(j).form_data_id and g_initial_tab(i).ref_book_id = g_change_tab(j).ref_book_id and g_initial_tab(i).record_id = g_change_tab(j).record_id) then
             v_flag := 1;
          end if;
      end loop;

      if v_flag = 0 then --Not found
         delete from form_data_ref_book where form_data_id = g_initial_tab(i).form_data_id and ref_book_id = g_initial_tab(i).ref_book_id and record_id = g_initial_tab(i).record_id;
         v_count_deleted := v_count_deleted + sql%rowcount;
      end if;

  end loop;

  msg_list.extend();
  msg_list(msg_list.last).err_level := 'INFO ';
  msg_list(msg_list.last).txt := 'Merge: ['||v_count_inserted||'] rows inserted, ['||v_count_deleted||'] rows deleted';


  --Print all generated messages
  for i in 1..msg_list.count loop
    dbms_output.put_line('[' || msg_list(i).err_level || ']:   ' ||msg_list(i).txt);
  end loop;
end;
/

----------------------------------------------------------------------------------------------------------------
create or replace package FORM_DATA_PCKG is
  -- Запросы получения источников-приемников для налоговых форм
  -- Источники - возвращаемый результат
  type t_source_record is record (
       id number(18),                                  --form_data.id
       departmentId number(9),                         --department.id
       departmentName varchar2(510),                   --department.name
       correction_date date,                           --department_report_period.correction_date
       departmentReportPeriod number(9),               --department_report_period.id
       periodName varchar2(510),                       --report_period.name
       year number(4),                                 --tax_period.year
       state number(1),                                --form_data.state
       templateState number(1),                        --form_template.state
       formTypeId number(9),                           --form_type.id
       formTypeName varchar2(1000),                    --form_type.name
       formDataKind number(9),                         --form_kind.id
       performerId number(9),                          --department.id
       performerName varchar2(512),                    --department.name
       periodStartDate date,                           --report_period.start_date
       compPeriodId number(9),                         --department_report_period.id
       compPeriodName varchar2(510),                   --report_period.name
       compPeriodYear number(4),                       --tax_period.year
       compPeriodStartDate date,                       --report_period.calendar_start_date
       accruing number(1),                             --form_data.accruing
       month number(2),                                --form_data.period_order
       manual number(1),                               --form_data.manual
       tax_type char(1)                                --tax_type.id
       );
  TYPE t_source IS TABLE OF t_source_record;

  -- Приемники - возвращаемый результат
  type t_destination_record is record (
       id number(18),                                  --form_data.id
       departmentId number(9),                         --department.id
       departmentName varchar2(510),                   --department.name
       correction_date date,                           --department_report_period.correction_date
       last_correction_date date,                      
       global_last_correction_date date,    
       reportperiodid number(9),
       departmentReportPeriod number(9),               --department_report_period.id
       periodName varchar2(510),                       --report_period.name
       year number(4),                                 --tax_period.year
       state number(1),                                --form_data.state
       templateState number(1),                        --form_template.state
       formTypeId number(9),                           --form_type.id
       formTypeName varchar2(1000),                    --form_type.name
       formDataKind number(9),                         --form_kind.id
       performerId number(9),                          --department.id
       performerName varchar2(512),                    --department.name
       periodStartDate date,                           --report_period.start_date
       compPeriodId number(9),                         --department_report_period.id
       compPeriodName varchar2(510),                   --report_period.name
       compPeriodYear number(4),                       --tax_period.year
       compPeriodStartDate date,                       --report_period.calendar_start_date
       accruing number(1),                             --form_data.accruing
       month number(2),                                --form_data.period_order
       manual number(1),                               --form_data.manual
       tax_type char(1)                                --tax_type.id
       );
  TYPE t_destination IS TABLE OF t_destination_record;
  
  --Объявление методов  
  FUNCTION get_sources (
                         p_in_stateRestriction              number,
                         p_in_excludeIfNotExist             number,
                         p_in_destinationFormDataId         number,
                         p_in_formTemplateId                number,
                         p_in_departmentReportPeriodId      number,
                         p_in_kind                          number,
                         p_in_compPeriod                    number,
                         p_in_accruing                      number
                        ) RETURN t_source PIPELINED;
                        
  FUNCTION get_destinations (
                         p_in_stateRestriction              number,
                         p_in_excludeIfNotExist             number,
                         p_in_sourceFormDataId              number,
                         p_in_formTemplateId                number,
                         p_in_departmentReportPeriodId      number,
                         p_in_kind                          number,
                         p_in_compPeriod                    number,
                         p_in_accruing                      number
                        ) RETURN t_destination PIPELINED;                      

end FORM_DATA_PCKG;
/

CREATE OR REPLACE PACKAGE BODY FORM_DATA_PCKG AS
FUNCTION get_sources (
                         p_in_stateRestriction              number,
                         p_in_excludeIfNotExist             number,
                         p_in_destinationFormDataId         number,
                         p_in_formTemplateId                number,
                         p_in_departmentReportPeriodId      number,
                         p_in_kind                          number,
                         p_in_compPeriod                    number,
                         p_in_accruing                      number
                        ) RETURN t_source PIPELINED IS
    l_source t_source;
    query_source sys_refcursor;
BEGIN
    open query_source for
         with insanity as
           (
           select distinct sfd.id, sd.id as departmentId, sd.name as departmentName, sdrp.id as departmentReportPeriod, stp.YEAR, srp.name as periodName, rp.CALENDAR_START_DATE as periodStartDate,
           sdrp.CORRECTION_DATE, sfd.state, sft.status as templateState, sfd.manual,
           st.id as formTypeId, st.name as formTypeName, sfk.id as formDataKind, fdpd.id as performerId, fdpd.name as performerName, st.tax_type,
           --Если искомый экземпляр создан, то берем его значения периода и признака.
           --Если не создан и в его макете есть признак сравнения, то период и признак такой же как у источника
           --Если не создан и в его макете нет признаков сравнения, то период и признак пустой
           case when (sfd.id is not null) then scdrp.id when (sft.COMPARATIVE = 1) then cdrp.id else null end as compPeriodId,
           case when (sfd.id is not null) then sctp.year when (sft.COMPARATIVE = 1) then ctp.year else null end as compPeriodYear,
           case when (sfd.id is not null) then scrp.CALENDAR_START_DATE when (sft.COMPARATIVE = 1) then crp.CALENDAR_START_DATE else null end as compPeriodStartDate,
           case when (sfd.id is not null) then scrp.name when (sft.COMPARATIVE = 1) then crp.name else null end as compPeriodName,
           case when (sfd.id is not null) then sfd.ACCRUING when (sft.ACCRUING = 1) then fd.ACCRUING else null end as ACCRUING,
           case when sft.MONTHLY=1 then perversion.month else sfd.PERIOD_ORDER end as month, sdft.id as sdft_id
            from (
                select nvl(fd.id, dual_fd.id) as ID,
                nvl(dual_fd.form_template_id, fd.form_template_id) as form_template_id,
                nvl(dual_fd.kind, fd.kind) as kind,
                nvl(dual_fd.DEPARTMENT_REPORT_PERIOD_ID, fd.DEPARTMENT_REPORT_PERIOD_ID) as DEPARTMENT_REPORT_PERIOD_ID,
                nvl(dual_fd.COMPARATIVE_DEP_REP_PER_ID, fd.COMPARATIVE_DEP_REP_PER_ID) as COMPARATIVE_DEP_REP_PER_ID,
                nvl(dual_fd.ACCRUING, fd.ACCRUING) as ACCRUING,
                fd.period_order as period_order
                from (
                 select p_in_destinationFormDataId as ID, p_in_formTemplateId as FORM_TEMPLATE_ID, p_in_kind as KIND, p_in_departmentReportPeriodId as DEPARTMENT_REPORT_PERIOD_ID, p_in_compPeriod as COMPARATIVE_DEP_REP_PER_ID, p_in_accruing as ACCRUING from dual) dual_fd
                 left join form_data fd on fd.id = dual_fd.id) fd
            join department_report_period drp on drp.id = fd.DEPARTMENT_REPORT_PERIOD_ID and (p_in_destinationFormDataId is null or fd.id = p_in_destinationFormDataId)
            join report_period rp on rp.id = drp.REPORT_PERIOD_ID
            join tax_period tp on tp.id = rp.TAX_PERIOD_ID
            left join department_report_period cdrp on cdrp.id = fd.COMPARATIVE_DEP_REP_PER_ID
            left join report_period crp on crp.id = cdrp.REPORT_PERIOD_ID
            left join tax_period ctp on ctp.id = crp.TAX_PERIOD_ID
            join form_template ft on ft.id = fd.FORM_TEMPLATE_ID
            join form_type t on t.id = ft.type_id
            join department_form_type dft on (dft.DEPARTMENT_ID = drp.DEPARTMENT_ID and dft.kind = fd.KIND and dft.FORM_TYPE_ID = ft.TYPE_ID)
            --ограничиваем назначения по пересечению с периодом приемника
            join form_data_source fds on (fds.DEPARTMENT_FORM_TYPE_ID = dft.id and ((fds.period_end >= rp.CALENDAR_START_DATE or fds.period_end is null) and fds.period_start <= rp.END_DATE))
            join department_form_type sdft on sdft.id = fds.SRC_DEPARTMENT_FORM_TYPE_ID
            join form_type st on st.id = sdft.FORM_TYPE_ID
            join form_kind sfk on sfk.ID = sdft.KIND
            --отбираем источники у которых дата корректировки ближе всего
            join (
                  select drp.*,rp.dict_tax_period_id, tp.tax_type, tp.year
                  from department_report_period drp
                  join report_period rp on rp.id = drp.report_period_id
                  join tax_period tp on tp.id = rp.tax_period_id
            ) sdrp on (sdrp.DEPARTMENT_ID = sdft.DEPARTMENT_ID and ((t.tax_type = st.tax_type and sdrp.REPORT_PERIOD_ID = drp.REPORT_PERIOD_ID)
                  --отбираем периоды для форм из других налогов
                  or (t.tax_type != st.tax_type and sdrp.tax_type = st.tax_type and sdrp.year = tp.year and sdrp.dict_tax_period_id = rp.dict_tax_period_id)
            ) and nvl(sdrp.CORRECTION_DATE, to_date('01.01.0001', 'DD.MM.YYYY')) <= nvl(drp.CORRECTION_DATE, to_date('01.01.0001', 'DD.MM.YYYY')))
            join department sd on sd.id = sdrp.DEPARTMENT_ID
            join report_period srp on srp.id = sdrp.REPORT_PERIOD_ID
            join tax_period stp on stp.ID = srp.TAX_PERIOD_ID
            --отбираем макет действующий для приемника в периоде приемника
            join (
              select * from (select ft.id as input_id, ft2.id, ft.type_id, ft.status, ft.accruing, ft.comparative, ft.monthly, ft.version, lead (ft2.version) over (partition by ft.id order by ft2.version) - interval '1' DAY end_version
              from form_template ft
              join form_template ft2 on ft2.type_id = ft.type_id and ft2.status in (0,1)
              ) where input_id = id
            ) sft on sft.status in (0,1) and sft.TYPE_ID = st.ID and ((sft.version <= srp.calendar_start_date and (sft.end_version is null or sft.end_version >= srp.calendar_start_date)) or (sft.version >= srp.calendar_start_date and sft.version <= srp.end_date))
            --если макет источника ежемесячный, то отбираем все возможные месяца для него из справочника
            left join
                 (
                 select t.id as record_id, lvl.i as lvl, extract(month from ADD_MONTHS(t.d1, lvl.i - 1)) as month, t.d1, t.d2 from (
                    select id, end_date as d2, calendar_start_date as d1, round(months_between(end_date, calendar_start_date)) as months_between_cnt from (
                              select r.id, v.date_value, a.alias from ref_book_value v
                              join ref_book_record r on r.id = v.record_id
                              join ref_book_attribute a on a.id = v.ATTRIBUTE_ID and a.alias in ('CALENDAR_START_DATE', 'END_DATE')
                              where r.ref_book_id = 8)
                            pivot
                            (
                              max(date_value) for alias in ('END_DATE' END_DATE, 'CALENDAR_START_DATE' CALENDAR_START_DATE)
                            )) t
                  join (
                       select level i
                       from dual
                       connect by level <= 12
                  ) lvl on ADD_MONTHS(t.d1, lvl.i - 1) <= t.d2
                 ) perversion on perversion.record_id = rp.DICT_TAX_PERIOD_ID and nvl(fd.period_order, perversion.month) = perversion.month--perversion.lvl = case when (sft.MONTHLY=1 and ft.MONTHLY=0) then perversion.lvl else 1 end
            --данные об источнике сравнения для приемника
            left join (
                  select drp.*,rp.dict_tax_period_id, tp.tax_type, tp.year
                  from department_report_period drp
                  join report_period rp on rp.id = drp.report_period_id
                  join tax_period tp on tp.id = rp.tax_period_id
            ) inn_cdrp on (
              (t.tax_type = st.tax_type and inn_cdrp.id = fd.COMPARATIVE_DEP_REP_PER_ID) or
              (t.tax_type != st.tax_type and fd.COMPARATIVE_DEP_REP_PER_ID is not null
              and inn_cdrp.tax_type = st.tax_type and inn_cdrp.year = ctp.year and inn_cdrp.dict_tax_period_id = crp.dict_tax_period_id and ((cdrp.correction_date is null and inn_cdrp.correction_date is null) or inn_cdrp.correction_date = cdrp.correction_date))
            )
            --отбираем экземпляры с учетом периода сравнения, признака нарастающего истога, списка месяцов
            left join (
                      select fd.*, drpc.report_period_id as comparative_report_period_id, rp.dict_tax_period_id, tp.year, drp.department_id as department_id, drp.correction_date
                      from form_data fd
                      join department_report_period drp on drp.id = fd.department_report_period_id
                      join report_period rp on rp.id = drp.report_period_id
                      join tax_period tp on tp.id = rp.TAX_PERIOD_ID
                      --данные об источниках сравнения для потенциальных источников
                      left join department_report_period drpc on fd.comparative_dep_rep_per_id = drpc.id
                      ) sfd
                 on (sfd.kind = sfk.id and sfd.FORM_TEMPLATE_ID = sft.id and sdft.department_id = sfd.department_id and
                 --если налог совпадает, то ищем точное совпадение по периоду, иначе совпадение по
                 ((st.tax_type = t.tax_type and sfd.DEPARTMENT_REPORT_PERIOD_ID = sdrp.id) or (st.tax_type != t.tax_type and tp.year = sfd.year and rp.dict_tax_period_id = sfd.dict_tax_period_id
                 and (nvl(sfd.CORRECTION_DATE, to_date('01.01.0001', 'DD.MM.YYYY')) = nvl(sdrp.CORRECTION_DATE, to_date('01.01.0001', 'DD.MM.YYYY')))))
              and (sft.COMPARATIVE = 0 or ft.COMPARATIVE = 0 or inn_cdrp.report_period_id  = sfd.comparative_report_period_id)
              and (sft.ACCRUING = 0 or ft.ACCRUING = 0 or sfd.ACCRUING = fd.ACCRUING))
              and coalesce(sfd.PERIOD_ORDER, perversion.month) = perversion.month
            left join department_form_type_performer dftp on dftp.DEPARTMENT_FORM_TYPE_ID = sdft.id
            left join department fdpd on fdpd.id = dftp.PERFORMER_DEP_ID
            left join department_report_period scdrp on scdrp.id = sfd.COMPARATIVE_DEP_REP_PER_ID
            left join report_period scrp on scrp.id = scdrp.REPORT_PERIOD_ID
            left join tax_period sctp on sctp.id = scrp.TAX_PERIOD_ID
                 ),
        aggregated_insanity as (
            select sdft_id, max(correction_date) as last_correction_date
            from insanity i
            where id is not null
            group by sdft_id
        )
      select id, departmentId, departmentName, correction_date, departmentReportPeriod, periodName, year, state, templateState, formTypeId, formTypeName, formDataKind, performerId, performerName, periodStartDate, compPeriodId, compPeriodName, compPeriodYear, compPeriodStartDate, accruing, month, manual, tax_type
             from insanity i
             left join aggregated_insanity i_agg on i.sdft_id = i_agg.sdft_id
             where nvl(i.correction_date, to_date('01.01.0001', 'DD.MM.YYYY')) = nvl(i_agg.last_correction_date, to_date('01.01.0001', 'DD.MM.YYYY')) and (p_in_excludeIfNotExist != 1 or id is not null) and (id is null or p_in_stateRestriction is null or state = p_in_stateRestriction)
             order by formTypeName, state, departmentName, month, id;

      fetch query_source bulk collect into l_source;
      close query_source;

      for i in 1..l_source.count loop
          PIPE ROW(l_source(i));
      end loop;
      RETURN;
END;
FUNCTION get_destinations (
                         p_in_stateRestriction              number,
                         p_in_excludeIfNotExist             number,
                         p_in_sourceFormDataId              number,
                         p_in_formTemplateId                number,
                         p_in_departmentReportPeriodId      number,
                         p_in_kind                          number,
                         p_in_compPeriod                    number,
                         p_in_accruing                      number
                        ) RETURN t_destination PIPELINED IS
    l_destination t_destination;
    query_destination sys_refcursor;
BEGIN
    open query_destination for
         with insanity as
     (
     select distinct tfd.id, td.id as departmentId, td.name as departmentName, tdrp.id as departmentReportPeriod, ttp.YEAR, trp.id as reportperiodid, trp.name as periodName,
     tdrp.CORRECTION_DATE, tfd.state, tft.status as templateState, tfd.manual,
     tt.id as formTypeId, tt.name as formTypeName, tfk.id as formDataKind, fdpd.id as performerId, fdpd.name as performerName, rp.CALENDAR_START_DATE as periodStartDate, tt.tax_type,
     --Если искомый экземпляр создан, то берем его значения периода и признака.
     --Если не создан и в его макете есть признак сравнения, то период и признак такой же как у источника
     --Если не создан и в его макете нет признаков сравнения, то период и признак пустой
     case when (tfd.id is not null) then tcdrp.id when (tft.COMPARATIVE = 1) then cdrp.id else null end as compPeriodId,
     case when (tfd.id is not null) then tctp.year when (tft.COMPARATIVE = 1) then ctp.year else null end as compPeriodYear,
     case when (tfd.id is not null) then tcrp.CALENDAR_START_DATE when (tft.COMPARATIVE = 1) then crp.CALENDAR_START_DATE else null end as compPeriodStartDate,
     case when (tfd.id is not null) then tcrp.name when (tft.COMPARATIVE = 1) then crp.name else null end as compPeriodName,
     case when (tfd.id is not null) then tfd.ACCRUING when (tft.ACCRUING = 1) then fd.ACCRUING else null end as ACCRUING,
     case when tft.MONTHLY=1 then perversion.month else tfd.PERIOD_ORDER end as month
      from (
           select neighbours_fd.id,
                  neighbours_fd.form_template_id,
                  neighbours_fd.kind,
                  neighbours_fd.DEPARTMENT_REPORT_PERIOD_ID,
                  neighbours_fd.COMPARATIVE_DEP_REP_PER_ID,
                  neighbours_fd.ACCRUING,
                  neighbours_fd.period_order,
                  neighbours_drp.department_id,
                  neighbours_drp.correction_date,
                  neighbours_drp.report_period_id,
                  coalesce(lag(neighbours_drp.correction_date) over (partition by neighbours_drp.department_id, neighbours_fd.period_order order by neighbours_drp.correction_date desc nulls last), to_date('31.12.9999', 'DD.MM.YYYY')) as next_correction_date
            from
			(
				select nvl(fd.id, dual_fd.id) as ID,
				nvl(dual_fd.form_template_id, fd.form_template_id) as form_template_id,
				nvl(dual_fd.kind, fd.kind) as kind,
				nvl(dual_fd.DEPARTMENT_REPORT_PERIOD_ID, fd.DEPARTMENT_REPORT_PERIOD_ID) as DEPARTMENT_REPORT_PERIOD_ID,
				nvl(dual_fd.COMPARATIVE_DEP_REP_PER_ID, fd.COMPARATIVE_DEP_REP_PER_ID) as COMPARATIVE_DEP_REP_PER_ID,
				nvl(dual_fd.ACCRUING, fd.ACCRUING) as ACCRUING,
        nvl(dual_fd.Period_Order, fd.period_order) as period_order
				from (
					select p_in_sourceFormDataId as ID, p_in_formTemplateId as FORM_TEMPLATE_ID, p_in_kind as KIND, p_in_departmentReportPeriodId as DEPARTMENT_REPORT_PERIOD_ID, p_in_compPeriod as COMPARATIVE_DEP_REP_PER_ID, p_in_accruing as ACCRUING, cast(null as number(2)) as PERIOD_ORDER from dual) dual_fd
					left join form_data fd on fd.id = dual_fd.id) fd
					join department_report_period drp on drp.id = fd.DEPARTMENT_REPORT_PERIOD_ID and (p_in_sourceFormDataId is null or fd.id = p_in_sourceFormDataId)
					join department_report_period neighbours_drp on neighbours_drp.report_period_id = drp.report_period_id
					join (
						  select id, form_template_id, kind, department_report_period_id, COMPARATIVE_DEP_REP_PER_ID, accruing, period_order from form_data
						  union all (select null as ID, cast(p_in_formTemplateId as NUMBER(9,0)) as FORM_TEMPLATE_ID, cast(p_in_kind as NUMBER(9,0)) as KIND, cast(p_in_departmentReportPeriodId as NUMBER(18,0)) as DEPARTMENT_REPORT_PERIOD_ID, cast(p_in_compPeriod as NUMBER(18,0)) as COMPARATIVE_DEP_REP_PER_ID, cast(p_in_accruing as NUMBER(1,0)) as ACCRUING, cast(null as number(2)) as PERIOD_ORDER from dual)
						) neighbours_fd on neighbours_fd.department_report_period_id = neighbours_drp.id and neighbours_fd.form_template_id = fd.form_template_id and neighbours_fd.kind = fd.kind
					) fd
      join report_period rp on rp.id = fd.REPORT_PERIOD_ID
      join tax_period tp on tp.id = rp.TAX_PERIOD_ID
      left join department_report_period cdrp on cdrp.id = fd.COMPARATIVE_DEP_REP_PER_ID
      left join report_period crp on crp.id = cdrp.REPORT_PERIOD_ID
      left join tax_period ctp on ctp.id = crp.TAX_PERIOD_ID
      join form_template ft on ft.id = fd.FORM_TEMPLATE_ID
      join form_type t on t.id = ft.type_id
      join department_form_type dft on (dft.DEPARTMENT_ID = fd.DEPARTMENT_ID and dft.kind = fd.KIND and dft.FORM_TYPE_ID = ft.TYPE_ID)
      --ограничиваем назначения по пересечению с периодом приемника
      join form_data_source fds on (fds.SRC_DEPARTMENT_FORM_TYPE_ID = dft.id and ((fds.period_end >= rp.CALENDAR_START_DATE or fds.period_end is null) and fds.period_start <= rp.END_DATE))
      join department_form_type tdft on tdft.id = fds.DEPARTMENT_FORM_TYPE_ID
      join form_type tt on tt.id = tdft.FORM_TYPE_ID
      join form_kind tfk on tfk.ID = tdft.KIND
      --отбираем источники у которых дата корректировки ближе всего
      join (
            select drp.*,rp.dict_tax_period_id, tp.tax_type, tp.year
            from department_report_period drp
            join report_period rp on rp.id = drp.report_period_id
            join tax_period tp on tp.id = rp.tax_period_id
      ) tdrp on (tdrp.DEPARTMENT_ID = tdft.DEPARTMENT_ID and ((t.tax_type = tt.tax_type and tdrp.REPORT_PERIOD_ID = fd.REPORT_PERIOD_ID)
            --отбираем периоды для форм из других налогов
            or (t.tax_type != tt.tax_type and tdrp.tax_type = tt.tax_type and tdrp.year = tp.year and tdrp.dict_tax_period_id = rp.dict_tax_period_id)
      ) and nvl(tdrp.CORRECTION_DATE, to_date('01.01.0001', 'DD.MM.YYYY')) between nvl(fd.CORRECTION_DATE, to_date('01.01.0001', 'DD.MM.YYYY')) and fd.NEXT_CORRECTION_DATE - 1)
      join department td on td.id = tdrp.DEPARTMENT_ID
      join report_period trp on trp.id = tdrp.REPORT_PERIOD_ID
      join tax_period ttp on ttp.ID = trp.TAX_PERIOD_ID
      --отбираем макет действующий для приемника в периоде источника
      join (
              select * from (select ft.id as input_id, ft2.id, ft.type_id, ft.status, ft.accruing, ft.comparative, ft.monthly, ft.version, lead (ft2.version) over (partition by ft.id order by ft2.version) - interval '1' DAY end_version
              from form_template ft
              join form_template ft2 on ft2.type_id = ft.type_id and ft2.status in (0,1)
              ) where input_id = id
            ) tft on tft.status in (0,1) and tft.TYPE_ID = tt.ID and ((tft.version <= trp.calendar_start_date and (tft.end_version is null or tft.end_version >= trp.calendar_start_date)) or (tft.version >= trp.calendar_start_date and tft.version <= trp.end_date))
      --если макет приемника ежемесячный, то отбираем все возможные месяца для него из справочника
      left join
           (
           select t.id as record_id, lvl.i as lvl, extract(month from ADD_MONTHS(t.d1, lvl.i - 1)) as month, t.d1, t.d2 from (
              select id, end_date as d2, calendar_start_date as d1, round(months_between(end_date, calendar_start_date)) as months_between_cnt from (
                        select r.id, v.date_value, a.alias from ref_book_value v
                        join ref_book_record r on r.id = v.record_id
                        join ref_book_attribute a on a.id = v.ATTRIBUTE_ID and a.alias in ('CALENDAR_START_DATE', 'END_DATE')
                        where r.ref_book_id = 8)
                      pivot
                      (
                        max(date_value) for alias in ('END_DATE' END_DATE, 'CALENDAR_START_DATE' CALENDAR_START_DATE)
                      )) t
            join (
                 select level i
                 from dual
                 connect by level <= 12
            ) lvl on ADD_MONTHS(t.d1, lvl.i - 1) <= t.d2
           ) perversion on perversion.record_id = rp.DICT_TAX_PERIOD_ID and (nvl(fd.period_order, perversion.month) = perversion.month /*and perversion.lvl = case when (tft.MONTHLY=1) then perversion.lvl else 1 end*/)
      --данные об источнике сравнения для приемника
      left join (
            select drp.*,rp.dict_tax_period_id, tp.tax_type, tp.year
            from department_report_period drp
            join report_period rp on rp.id = drp.report_period_id
            join tax_period tp on tp.id = rp.tax_period_id
      ) inn_cdrp on (
        (t.tax_type = tt.tax_type and inn_cdrp.id = fd.COMPARATIVE_DEP_REP_PER_ID) or
        (t.tax_type != tt.tax_type and fd.COMPARATIVE_DEP_REP_PER_ID is not null
        and inn_cdrp.tax_type = tt.tax_type and inn_cdrp.year = ctp.year and inn_cdrp.dict_tax_period_id = crp.dict_tax_period_id and ((cdrp.correction_date is null and inn_cdrp.correction_date is null) or inn_cdrp.correction_date = cdrp.correction_date))
      )
      --отбираем экземпляры с учетом периода сравнения, признака нарастающего итога, списка месяцев
      left join (
                select fd.*, drpc.report_period_id as comparative_report_period_id, rp.dict_tax_period_id, tp.year, drp.department_id as department_id, drp.correction_date
                from form_data fd
                join department_report_period drp on drp.id = fd.department_report_period_id
                join report_period rp on rp.id = drp.report_period_id
                join tax_period tp on tp.id = rp.TAX_PERIOD_ID
                --данные об источниках сравнения для потенциальных источников
                left join department_report_period drpc on fd.comparative_dep_rep_per_id = drpc.id
                ) tfd
           on (tfd.kind = tfk.id and tfd.FORM_TEMPLATE_ID = tft.id and tdft.department_id = tfd.department_id and
           --если налог совпадает, то ищем точное совпадение по периоду, иначе совпадение по
           ((tt.tax_type = t.tax_type and tfd.DEPARTMENT_REPORT_PERIOD_ID = tdrp.id) or (tt.tax_type != t.tax_type and tp.year = tfd.year and rp.dict_tax_period_id = tfd.dict_tax_period_id and
           (nvl(tfd.CORRECTION_DATE, to_date('01.01.0001', 'DD.MM.YYYY')) = nvl(tdrp.CORRECTION_DATE, to_date('01.01.0001', 'DD.MM.YYYY')))))
        and (tft.COMPARATIVE = 0 or ft.COMPARATIVE = 0 or inn_cdrp.report_period_id  = tfd.comparative_report_period_id)
        and (tft.ACCRUING = 0 or ft.ACCRUING = 0 or tfd.ACCRUING = fd.ACCRUING))
        and coalesce(tfd.PERIOD_ORDER, perversion.month) = perversion.month
      left join department_form_type_performer dftp on dftp.DEPARTMENT_FORM_TYPE_ID = tdft.id
      left join department fdpd on fdpd.id = dftp.PERFORMER_DEP_ID
      left join department_report_period tcdrp on tcdrp.id = tfd.COMPARATIVE_DEP_REP_PER_ID
      left join report_period tcrp on tcrp.id = tcdrp.REPORT_PERIOD_ID
      left join tax_period tctp on tctp.id = tcrp.TAX_PERIOD_ID
      where (p_in_sourceFormDataId is null and (fd.id is null and fd.form_template_id = p_in_formTemplateId and fd.DEPARTMENT_REPORT_PERIOD_ID = p_in_departmentReportPeriodId and fd.kind = p_in_kind and (p_in_compPeriod is null and fd.COMPARATIVE_DEP_REP_PER_ID is null or fd.COMPARATIVE_DEP_REP_PER_ID = p_in_compPeriod) and fd.ACCRUING = p_in_accruing)) or fd.id = p_in_sourceFormDataId
  ),
  aggregated_insanity as (
      select departmentId, formtypeid, formdatakind, reportperiodid, month, isExemplarExistent, last_correction_date, global_last_correction_date from
        (select case when i.id is null then '0' else '1' end as agg_type, departmentId, formtypeid, formdatakind, reportperiodid, month, max(correction_date) over(partition by departmentId, formtypeid, formdatakind, reportperiodid, month, case when i.id is null then 0 else 1 end) as last_correction_date, case when count(i.id) over(partition by departmentId, formtypeid, formdatakind, reportperiodid, month) > 0 then 1 else 0 end isExemplarExistent
         from insanity i)
      --транспонирование и агрегирование среди множеств отдельно с сушествующими и несуществующими экземлярами
      pivot
      (
          max(last_correction_date) for agg_type in ('1' as last_correction_date, '0' global_last_correction_date)
      )
)
select i.id, i.departmentId, i.departmentName, i.correction_date, ai.last_correction_date, ai.global_last_correction_date, i.reportperiodid, i.departmentReportPeriod, i.periodName, i.year, i.state, i.templateState, i.formTypeId, i.formTypeName, i.formDataKind, i.performerId, i.performerName, periodStartDate, i.compPeriodId, i.compPeriodName, i.compPeriodYear, i.compPeriodStartDate, i.accruing, i.month, i.manual, i.tax_type
       from insanity i
       --обращение к аггрегированным данным для определения, какие существуют данные в связке по подразделению, типу, виду, периоду и месяцу экземпляры данных, их максимальную дату и дату последнего периода корректировки, если данные по экземлярам отсутствуют
       left join aggregated_insanity ai on i.id is null and ai.departmentId = i.departmentId and ai.formtypeid = i.formtypeid and ai.formdatakind = i.formdatakind and ai.reportperiodid = i.reportperiodid  and nvl(i.month,-1) = nvl(ai.month,-1)
       --отбираем либо записи, либо где идентификатор формы существует, либо если не существует, то берем запись с максимально доступной датой корректировки
       where (id is not null or (ai.isExemplarExistent = 0 and nvl(i.correction_date, to_date('01.01.0001', 'DD.MM.YYYY')) = nvl(ai.global_last_correction_date, to_date('01.01.0001', 'DD.MM.YYYY'))))
       and (p_in_excludeIfNotExist != 1 or id is not null) and (id is null or p_in_stateRestriction is null or state = p_in_stateRestriction)
       order by formTypeName, state, departmentName, month, id;

fetch query_destination bulk collect into l_destination;
      close query_destination;

      for i in 1..l_destination.count loop
          PIPE ROW(l_destination(i));
      end loop;
      RETURN;

END;
END FORM_DATA_PCKG;
/

create or replace package DECLARATION_PCKG is
  -- Запросы получения источников-приемников для деклараций
  -- Источники - возвращаемый результат
  type t_source_record is record (
       id number(18),                                  --declaration_data.id
       departmentId number(9),                         --department.id
       departmentName varchar2(510),                   --department.name
       correction_date date,                           --department_report_period.correction_date
       departmentReportPeriod number(9),               --department_report_period.id
       periodName varchar2(510),                       --report_period.name
       year number(4),                                 --tax_period.year
       state number(1),                                --form_data.state
       templateState number(1),                        --form_template.state
       formTypeId number(9),                           --form_type.id
       formTypeName varchar2(1000),                    --form_type.name
       formDataKind number(9),                         --form_kind.id
       performerId number(9),                          --department.id
       performerName varchar2(512),                    --department.name
       month number(2),                                --form_data.period_order
       manual number(1),                               --form_data.manual
       tax_type char(1)                                --tax_type.id
       );
  TYPE t_source IS TABLE OF t_source_record;

  -- Приемники - возвращаемый результат
  type t_destination_record is record (
       id number(18),                                  --declaration_data.id
       departmentId number(9),                         --department.id
       departmentName varchar2(510),                   --department.name
       correction_date date,                           --department_report_period.correction_date
       last_correction_date date,
       global_last_correction_date date,
       reportperiodid number(9),
       departmentReportPeriod number(9),               --department_report_period.id
       periodName varchar2(510),                       --report_period.name
       year number(4),                                 --tax_period.year
       is_accepted number(1),
       templateState number(1),                        --form_template.state
       declarationTypeId number(9),                    --declaration_type.id
       declarationTypeName varchar2(1000),             --declaration_type.name
       taxOrgan varchar2(4),                           --declaration_data.tax_organ_code
       kpp varchar2(9),                                --declaration_data.kpp
       tax_type char(1)                                --tax_type.id
       );
  TYPE t_destination IS TABLE OF t_destination_record;

  --Объявление методов
  FUNCTION get_sources (
                         p_in_stateRestriction              number,
                         p_in_excludeIfNotExist             number,
                         p_in_declarationId        			    number,
						             p_in_declarationTemplateId 		    number,
						             p_in_departmentReportPeriodId		  number
                        ) RETURN t_source PIPELINED;

  FUNCTION get_destinations (
                         p_in_stateRestriction              number,
                         p_in_excludeIfNotExist             number,
                         p_in_sourceFormDataId              number,
                         p_in_formTemplateId                number,
                         p_in_departmentReportPeriodId      number,
                         p_in_kind                          number,
                         p_in_compPeriod                    number,
                         p_in_accruing                      number
                        ) RETURN t_destination PIPELINED;

end DECLARATION_PCKG;
/

CREATE OR REPLACE PACKAGE BODY DECLARATION_PCKG AS
FUNCTION get_sources (
                         p_in_stateRestriction              number,
                         p_in_excludeIfNotExist             number,
                         p_in_declarationId          		    number,
						             p_in_declarationTemplateId 		    number,
						             p_in_departmentReportPeriodId		  number
                        ) RETURN t_source PIPELINED IS
    l_source t_source;
    query_source sys_refcursor;
BEGIN
    open query_source for
         with insanity as
     (
     select sfd.id, sd.id as departmentId, sd.name as departmentName, sdrp.id as departmentReportPeriod, stp.YEAR, srp.name as periodName,
     sdrp.CORRECTION_DATE, sfd.state, sft.status as templateState, sfd.manual,
     st.id as formTypeId, st.name as formTypeName, sfk.id as formDataKind, fdpd.id as performerId, fdpd.name as performerName, rp.CALENDAR_START_DATE as periodStartDate, st.tax_type,
     case when sft.MONTHLY=1 then perversion.month else sfd.PERIOD_ORDER end as month, sdft.id as sdft_id
	from
		(
		select
			nvl(dd.id, dual_dd.id) as ID,
			nvl(dual_dd.DECLARATION_TEMPLATE_ID, dd.DECLARATION_TEMPLATE_ID) as DECLARATION_TEMPLATE_ID,
			nvl(dual_dd.DEPARTMENT_REPORT_PERIOD_ID, dd.DEPARTMENT_REPORT_PERIOD_ID) as DEPARTMENT_REPORT_PERIOD_ID
		from (select p_in_declarationId as ID, p_in_declarationTemplateId as DECLARATION_TEMPLATE_ID, p_in_departmentReportPeriodId as DEPARTMENT_REPORT_PERIOD_ID from dual) dual_dd
		left join declaration_data dd on dd.id = dual_dd.id
		) dd
	 join department_report_period drp on drp.id = dd.DEPARTMENT_REPORT_PERIOD_ID and (p_in_declarationId is null or dd.id = p_in_declarationId)
      join report_period rp on rp.id = drp.REPORT_PERIOD_ID
      join tax_period tp on tp.id = rp.TAX_PERIOD_ID
      join declaration_template dt on dt.id = dd.DECLARATION_TEMPLATE_ID
      join declaration_type t on t.id = dt.declaration_type_id
      join department_declaration_type ddt on (ddt.DEPARTMENT_ID = drp.DEPARTMENT_ID and ddt.DECLARATION_TYPE_ID = dt.DECLARATION_TYPE_ID)
      --ограничиваем назначения по пересечению с периодом приемника
      join declaration_source ds on (ds.DEPARTMENT_DECLARATION_TYPE_ID = ddt.id and ((ds.period_end >= rp.CALENDAR_START_DATE or ds.period_end is null) and ds.period_start <= rp.END_DATE))
      join department_form_type sdft on sdft.id = ds.SRC_DEPARTMENT_FORM_TYPE_ID
      join form_type st on st.id = sdft.FORM_TYPE_ID
      join form_kind sfk on sfk.ID = sdft.KIND
      --отбираем источники у которых дата корректировки ближе всего
      join (
            select drp.*,rp.dict_tax_period_id, tp.tax_type, tp.year
            from department_report_period drp
            join report_period rp on rp.id = drp.report_period_id
            join tax_period tp on tp.id = rp.tax_period_id
      ) sdrp on (sdrp.DEPARTMENT_ID = sdft.DEPARTMENT_ID and ((t.tax_type = st.tax_type and sdrp.REPORT_PERIOD_ID = drp.REPORT_PERIOD_ID)
            --отбираем периоды для форм из других налогов
            or (t.tax_type != st.tax_type and sdrp.tax_type = st.tax_type and sdrp.year = tp.year and sdrp.dict_tax_period_id = rp.dict_tax_period_id)
      ) and nvl(sdrp.CORRECTION_DATE, to_date('01.01.0001', 'DD.MM.YYYY')) <= nvl(drp.CORRECTION_DATE, to_date('01.01.0001', 'DD.MM.YYYY')))
      join department sd on sd.id = sdrp.DEPARTMENT_ID
      join report_period srp on srp.id = sdrp.REPORT_PERIOD_ID
      join tax_period stp on stp.ID = srp.TAX_PERIOD_ID
      --отбираем макет действующий для приемника в периоде приемника
      join (
              select * from (select ft.id as input_id, ft2.id, ft.type_id, ft.status, ft.monthly, ft.version, lead (ft2.version) over (partition by ft.id order by ft2.version) - interval '1' DAY end_version
              from form_template ft
              join form_template ft2 on ft2.type_id = ft.type_id and ft2.status in (0,1)
              ) where input_id = id
            ) sft on sft.status in (0,1) and sft.TYPE_ID = st.ID and ((sft.version <= srp.calendar_start_date and (sft.end_version is null or sft.end_version >= srp.calendar_start_date)) or (sft.version >= srp.calendar_start_date and sft.version <= srp.end_date))
      --если макет источника ежемесячный, то отбираем все возможные месяца для него из справочника
      left join
           (
           select t.id as record_id, lvl.i as lvl, extract(month from ADD_MONTHS(t.d1, lvl.i - 1)) as month, t.d1, t.d2 from (
              select id, end_date as d2, calendar_start_date as d1, round(months_between(end_date, calendar_start_date)) as months_between_cnt from (
                        select r.id, v.date_value, a.alias from ref_book_value v
                        join ref_book_record r on r.id = v.record_id
                        join ref_book_attribute a on a.id = v.ATTRIBUTE_ID and a.alias in ('CALENDAR_START_DATE', 'END_DATE')
                        where r.ref_book_id = 8)
                      pivot
                      (
                        max(date_value) for alias in ('END_DATE' END_DATE, 'CALENDAR_START_DATE' CALENDAR_START_DATE)
                      )) t
            join (
                 select level i
                 from dual
                 connect by level <= 12
            ) lvl on ADD_MONTHS(t.d1, lvl.i - 1) <= t.d2
           ) perversion on perversion.record_id = rp.DICT_TAX_PERIOD_ID and perversion.lvl = case when sft.MONTHLY=1 then perversion.lvl else 1 end
      --отбираем экземпляры с учетом списка месяцов
      left join (
                select fd.*, rp.dict_tax_period_id, tp.year, drp.department_id as department_id, drp.correction_date
                from form_data fd
                join department_report_period drp on drp.id = fd.department_report_period_id
                join report_period rp on rp.id = drp.report_period_id
                join tax_period tp on tp.id = rp.TAX_PERIOD_ID
                ) sfd
           on (sfd.kind = sfk.id and sfd.FORM_TEMPLATE_ID = sft.id and sdft.department_id = sfd.department_id and
           --если налог совпадает, то ищем точное совпадение по периоду, иначе совпадение по
           ((st.tax_type = t.tax_type and sfd.DEPARTMENT_REPORT_PERIOD_ID = sdrp.id) or (st.tax_type != t.tax_type and tp.year = sfd.year and rp.dict_tax_period_id = sfd.dict_tax_period_id and
           (nvl(sfd.CORRECTION_DATE, to_date('01.01.0001', 'DD.MM.YYYY')) = nvl(sdrp.CORRECTION_DATE, to_date('01.01.0001', 'DD.MM.YYYY'))))))
           and coalesce(sfd.PERIOD_ORDER, perversion.month) = perversion.month
      left join department_form_type_performer dftp on dftp.DEPARTMENT_FORM_TYPE_ID = sdft.id
      left join department fdpd on fdpd.id = dftp.PERFORMER_DEP_ID
           ),
  aggregated_insanity as (
      select sdft_id, max(correction_date) as last_correction_date
      from insanity i
      where id is not null
      group by sdft_id
  )
select id, departmentId, departmentName, correction_date, departmentReportPeriod, periodName, year, state, templateState, formTypeId, formTypeName, formDataKind, performerId, performerName, month, manual, tax_type
       from insanity i
       left join aggregated_insanity i_agg on i.sdft_id = i_agg.sdft_id
       where nvl(i.correction_date, to_date('01.01.0001', 'DD.MM.YYYY')) = nvl(i_agg.last_correction_date, to_date('01.01.0001', 'DD.MM.YYYY')) and (p_in_excludeIfNotExist != 1 or id is not null) and (id is null or p_in_stateRestriction is null or state = p_in_stateRestriction)
       order by formTypeName, state, departmentName, month, id;

      fetch query_source bulk collect into l_source;
      close query_source;

      for i in 1..l_source.count loop
          PIPE ROW(l_source(i));
      end loop;
      RETURN;
END;
FUNCTION get_destinations (
                         p_in_stateRestriction              number,
                         p_in_excludeIfNotExist             number,
                         p_in_sourceFormDataId              number,
                         p_in_formTemplateId                number,
                         p_in_departmentReportPeriodId      number,
                         p_in_kind                          number,
                         p_in_compPeriod                    number,
                         p_in_accruing                      number
                        ) RETURN t_destination PIPELINED IS
    l_destination t_destination;
    query_destination sys_refcursor;
BEGIN
    open query_destination for
         with insanity as
     (
     select tdd.id, td.id as departmentId, td.name as departmentName, tdrp.id as departmentReportPeriod, ttp.YEAR, trp.id as reportperiodid, trp.name as periodName,
     tdrp.CORRECTION_DATE, tdd.IS_ACCEPTED, tdt.status as templateState, dt.id as declarationTypeId, dt.name as declarationTypeName, tdd.TAX_ORGAN_CODE as taxOrgan, tdd.kpp, dt.tax_type
      from (
           select neighbours_fd.id,
                  neighbours_fd.form_template_id,
                  neighbours_fd.kind,
                  neighbours_fd.DEPARTMENT_REPORT_PERIOD_ID,
                  neighbours_fd.COMPARATIVE_DEP_REP_PER_ID,
                  neighbours_fd.ACCRUING,
                  neighbours_drp.department_id,
                  neighbours_drp.correction_date,
                  neighbours_drp.report_period_id,
                  coalesce(lag(neighbours_drp.correction_date) over (partition by neighbours_drp.department_id order by neighbours_drp.correction_date desc nulls last), to_date('31.12.9999', 'DD.MM.YYYY')) as next_correction_date
            from (
				select nvl(fd.id, dual_fd.id) as ID,
				nvl(dual_fd.form_template_id, fd.form_template_id) as form_template_id,
				nvl(dual_fd.kind, fd.kind) as kind,
				nvl(dual_fd.DEPARTMENT_REPORT_PERIOD_ID, fd.DEPARTMENT_REPORT_PERIOD_ID) as DEPARTMENT_REPORT_PERIOD_ID,
				nvl(dual_fd.COMPARATIVE_DEP_REP_PER_ID, fd.COMPARATIVE_DEP_REP_PER_ID) as COMPARATIVE_DEP_REP_PER_ID,
				nvl(dual_fd.ACCRUING, fd.ACCRUING) as ACCRUING
				from (
					select p_in_sourceFormDataId as ID, p_in_formTemplateId as FORM_TEMPLATE_ID, p_in_kind as KIND, p_in_departmentReportPeriodId as DEPARTMENT_REPORT_PERIOD_ID, p_in_compPeriod as COMPARATIVE_DEP_REP_PER_ID, p_in_accruing as ACCRUING from dual) dual_fd
					left join form_data fd on fd.id = dual_fd.id) fd
					join department_report_period drp on drp.id = fd.DEPARTMENT_REPORT_PERIOD_ID and (p_in_sourceFormDataId is null or fd.id = p_in_sourceFormDataId)
					join department_report_period neighbours_drp on neighbours_drp.report_period_id = drp.report_period_id
					join (
						  select id, form_template_id, kind, department_report_period_id, COMPARATIVE_DEP_REP_PER_ID, accruing from form_data
						  union all (select null as ID, cast(p_in_formTemplateId as NUMBER(9,0)) as FORM_TEMPLATE_ID, cast(p_in_kind as NUMBER(9,0)) as KIND, cast(p_in_departmentReportPeriodId as NUMBER(18,0)) as DEPARTMENT_REPORT_PERIOD_ID, cast(p_in_compPeriod as NUMBER(18,0)) as COMPARATIVE_DEP_REP_PER_ID, cast(p_in_accruing as NUMBER(1,0)) as ACCRUING from dual)
						) neighbours_fd on neighbours_fd.department_report_period_id = neighbours_drp.id and neighbours_fd.form_template_id = fd.form_template_id and neighbours_fd.kind = fd.kind
        ) fd
      join report_period rp on rp.id = fd.REPORT_PERIOD_ID
      join tax_period tp on tp.id = rp.TAX_PERIOD_ID
      join form_template ft on ft.id = fd.FORM_TEMPLATE_ID
      join form_type t on t.id = ft.type_id
      join department_form_type dft on (dft.DEPARTMENT_ID = fd.DEPARTMENT_ID and dft.kind = fd.KIND and dft.FORM_TYPE_ID = ft.TYPE_ID)
      --ограничиваем назначения по пересечению с периодом приемника
      join declaration_source ds on (ds.SRC_DEPARTMENT_FORM_TYPE_ID = dft.id and ((ds.period_end >= rp.CALENDAR_START_DATE or ds.period_end is null) and ds.period_start <= rp.END_DATE))
      join department_declaration_type tddt on tddt.id = ds.DEPARTMENT_DECLARATION_TYPE_ID
      join declaration_type dt on dt.id = tddt.DECLARATION_TYPE_ID
      --отбираем источники у которых дата корректировки ближе всего
      join (
            select drp.*,rp.dict_tax_period_id, tp.tax_type, tp.year
            from department_report_period drp
            join report_period rp on rp.id = drp.report_period_id
            join tax_period tp on tp.id = rp.tax_period_id
      ) tdrp on (tdrp.DEPARTMENT_ID = tddt.DEPARTMENT_ID and ((t.tax_type = dt.tax_type and tdrp.REPORT_PERIOD_ID = fd.REPORT_PERIOD_ID)
            --отбираем периоды для форм из других налогов
            or (t.tax_type != dt.tax_type and tdrp.tax_type = dt.tax_type and tdrp.year = tp.year and tdrp.dict_tax_period_id = rp.dict_tax_period_id)
      ) and nvl(tdrp.CORRECTION_DATE, to_date('01.01.0001', 'DD.MM.YYYY')) between nvl(fd.CORRECTION_DATE, to_date('01.01.0001', 'DD.MM.YYYY')) and fd.NEXT_CORRECTION_DATE - 1)
      join department td on td.id = tdrp.DEPARTMENT_ID
      join report_period trp on trp.id = tdrp.REPORT_PERIOD_ID
      join tax_period ttp on ttp.ID = trp.TAX_PERIOD_ID
      --отбираем макет действующий для приемника в периоде источника
      join (
              select * from (select ft.id as input_id, ft2.id, ft.DECLARATION_TYPE_ID, ft.status, ft.version, lead (ft2.version) over (partition by ft.id order by ft2.version) - interval '1' DAY end_version
              from declaration_template ft
              join declaration_template ft2 on ft2.DECLARATION_TYPE_ID = ft.DECLARATION_TYPE_ID and ft2.status in (0,1)
              ) where input_id = id
            ) tdt on tdt.status in (0,1) and tdt.DECLARATION_TYPE_ID = dt.ID and ((tdt.version <= trp.calendar_start_date and (tdt.end_version is null or tdt.end_version >= trp.calendar_start_date)) or (tdt.version >= trp.calendar_start_date and tdt.version <= trp.end_date))
      --отбираем экземпляры с учетом периода сравнения, признака нарастающего истога, списка месяцов
      left join (
                select dd.*, rp.dict_tax_period_id, tp.year, drp.department_id as department_id, drp.correction_date
                from declaration_data dd
                join department_report_period drp on drp.id = dd.department_report_period_id
                join report_period rp on rp.id = drp.report_period_id
                join tax_period tp on tp.id = rp.TAX_PERIOD_ID
                ) tdd
           on (tdd.DECLARATION_TEMPLATE_ID = tdt.id and tddt.department_id = tdd.department_id and
           --если налог совпадает, то ищем точное совпадение по периоду, иначе совпадение по
           ((dt.tax_type = t.tax_type and tdd.DEPARTMENT_REPORT_PERIOD_ID = tdrp.id) or (dt.tax_type != t.tax_type and tp.year = tdd.year and rp.dict_tax_period_id = tdd.dict_tax_period_id and
           (nvl(tdd.CORRECTION_DATE, to_date('01.01.0001', 'DD.MM.YYYY')) = nvl(tdrp.CORRECTION_DATE, to_date('01.01.0001', 'DD.MM.YYYY'))))))
      where (p_in_sourceFormDataId is null and (fd.id is null and fd.form_template_id = p_in_formTemplateId and fd.DEPARTMENT_REPORT_PERIOD_ID = p_in_departmentReportPeriodId and fd.kind = p_in_kind and (p_in_compPeriod is null and fd.COMPARATIVE_DEP_REP_PER_ID is null or fd.COMPARATIVE_DEP_REP_PER_ID = p_in_compPeriod) and fd.ACCRUING = p_in_accruing)) or fd.id = p_in_sourceFormDataId
  ),
  aggregated_insanity as (
      select departmentId, declarationTypeId, reportperiodid, isExemplarExistent, last_correction_date, global_last_correction_date from
        (select case when i.id is null then '0' else '1' end as agg_type, departmentId, declarationTypeId, reportperiodid, max(correction_date) over(partition by departmentId, declarationTypeId, reportperiodid, case when i.id is null then 0 else 1 end) as last_correction_date, case when count(i.id) over(partition by departmentId, declarationTypeId, reportperiodid) > 0 then 1 else 0 end isExemplarExistent
         from insanity i)
      --транспонирование и агрегирование среди множеств отдельно с сушествующими и несуществующими экземлярами
      pivot
      (
          max(last_correction_date) for agg_type in ('1' as last_correction_date, '0' global_last_correction_date)
      )
)
select i.id, i.departmentId, i.departmentName, i.correction_date, ai.last_correction_date, ai.global_last_correction_date, i.reportperiodid, i.departmentReportPeriod, i.periodName, i.year, i.IS_ACCEPTED, i.templateState, i.declarationTypeId, i.declarationTypeName, i.taxOrgan, i.kpp, i.tax_type
       from insanity i
       --обращение к аггрегированным данным для определения, какие существуют данные в связке по подразделению, типу, виду, периоду и месяцу экземпляры данных, их максимальную дату и дату последнего периода корректировки, если данные по экземлярам отсутствуют
       left join aggregated_insanity ai on i.id is null and ai.departmentId = i.departmentId and ai.declarationTypeId = i.declarationTypeId and ai.reportperiodid = i.reportperiodid
       --отбираем либо записи, либо где идентификатор формы существует, либо если не существует, то берем запись с максимально доступной датой корректировки
       where (id is not null or (ai.isExemplarExistent = 0 and nvl(i.correction_date, to_date('01.01.0001', 'DD.MM.YYYY')) = nvl(ai.global_last_correction_date, to_date('01.01.0001', 'DD.MM.YYYY'))))
       and (p_in_excludeIfNotExist != 1 or id is not null) and (id is null or p_in_stateRestriction is null or IS_ACCEPTED = p_in_stateRestriction)
       order by declarationTypeName, IS_ACCEPTED, departmentName, id;


fetch query_destination bulk collect into l_destination;
      close query_destination;

      for i in 1..l_destination.count loop
          PIPE ROW(l_destination(i));
      end loop;
      RETURN;

END;
END DECLARATION_PCKG;
/
