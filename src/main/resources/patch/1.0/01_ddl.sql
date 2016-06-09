--https://jira.aplana.com/browse/SBRFACCTAX-15795: Поиск по ОКТМО
create index I_REF_BOOK_OKTMO_PARENT_ID on REF_BOOK_OKTMO (PARENT_ID);
----------------------------------------------------------------------------------------------------------------
--http://jira.aplana.com/browse/SBRFACCTAX-13720: 1.0 Добавить в ЖА поле "Сервер" по аналогии с блокировками
alter table log_system add server varchar2(200);
comment on column log_system.server is 'Сервер'; 

----------------------------------------------------------------------------------------------------------------
--https://jira.aplana.com/browse/SBRFACCTAX-15340: 1.0 БД. Изменения для логирования импорта скриптов

alter table template_changes drop constraint template_changes_chk_event; 
alter table template_changes add constraint template_changes_chk_event check (event in (701, 702, 703, 704, 705, 904));
----------------------------------------------------------------------------------------------------------------
--http://jira.aplana.com/browse/SBRFACCTAX-14423: Реализовать справочник "История изменения категории ВЗЛ"

insert into ref_book (id, name, visible, type, read_only, region_attribute_id, is_versioned) values (521, 'История изменения категории ВЗЛ', 0, 0, 0, null, 0);
                                                                                                
insert into ref_book_attribute(id, ref_book_id, name, alias, type, ord, reference_id, attribute_id, visible, precision, width, required, is_unique, sort_order, format, read_only, max_length) values (5230, 521, 'ВЗЛ',            'JUR_PERSON',    4,  1,  520,  5201,  1,  null,  10,  1,  0,  null,  null,  0,  null);
insert into ref_book_attribute(id, ref_book_id, name, alias, type, ord, reference_id, attribute_id, visible, precision, width, required, is_unique, sort_order, format, read_only, max_length) values (5231, 521, 'Категория ВЗЛ',        'CATEGORY',      4,  2,  506,  5061,  1,  null,  10,  1,  0,  null,  null,  0,  null);
insert into ref_book_attribute(id, ref_book_id, name, alias, type, ord, reference_id, attribute_id, visible, precision, width, required, is_unique, sort_order, format, read_only, max_length) values (5232, 521, 'Код формы',           'FORM_DATA_ID',   2,   3,   null,   null,   1,   0,     10, 1,   0,   null,   null,   0,   18);
insert into ref_book_attribute(id, ref_book_id, name, alias, type, ord, reference_id, attribute_id, visible, precision, width, required, is_unique, sort_order, format, read_only, max_length) values (5233, 521, 'Дата изменения',        'CHANGE_DATE',    3,  4,  null,  null,  1,  null,  10,  1,  0,  null,  1,    0,  null);
insert into ref_book_attribute(id, ref_book_id, name, alias, type, ord, reference_id, attribute_id, visible, precision, width, required, is_unique, sort_order, format, read_only, max_length) values (5234, 521, 'Код состояния',         'STATE',       2,   5,   null,   null,   1,   0,     10, 1,   0,   null,   null,   0,   9);

--http://jira.aplana.com/browse/SBRFACCTAX-14644: Создать отдельную таблицу для справочника "История ВЗЛ"
create table ref_book_vzl_history
(
id number(18) not null,
jur_person number(18) not null,
category number(18) not null,
form_data_id number(18) not null,
change_date date not null,
state number(9) not null
);

create sequence seq_ref_book_vzl_history start with 1;

comment on table ref_book_vzl_history is 'История изменения категории ВЗЛ';
comment on column ref_book_vzl_history.id is 'Идентификатор записи';
comment on column ref_book_vzl_history.jur_person is 'ВЗЛ';
comment on column ref_book_vzl_history.category is 'Категория ВЗЛ';
comment on column ref_book_vzl_history.form_data_id is 'Код формы';
comment on column ref_book_vzl_history.change_date is 'Дата изменения';
comment on column ref_book_vzl_history.state is 'Код состояния';

alter table ref_book_vzl_history add constraint ref_book_vzl_hist_pk primary key (id);
alter table ref_book_vzl_history add constraint ref_book_vzl_hist_fk_form_data foreign key (form_data_id) references form_data(id) on delete cascade;
alter table ref_book_vzl_history add constraint ref_book_vzl_hist_fk_ref_jur foreign key (jur_person) references ref_book_record(id);
alter table ref_book_vzl_history add constraint ref_book_vzl_hist_fk_ref_cat foreign key (category) references ref_book_record(id);

update ref_book set table_name = upper('ref_book_vzl_history') where id = 521;

insert into ref_book_vzl_history (id, jur_person, category, form_data_id, change_date, state)
select seq_ref_book_vzl_history.nextval, v1.reference_value as JUR_PERSON, v2.reference_value as CATEGORY, v3.number_value as FORM_DATA_ID, v4.date_value as CHANGE_DATE, v5.number_value as STATE from ref_book_record r
left join ref_book_value v1 on r.id = v1.record_id and v1.attribute_id = 5230
left join ref_book_value v2 on r.id = v2.record_id and v2.attribute_id = 5231
left join ref_book_value v3 on r.id = v3.record_id and v3.attribute_id = 5232
left join ref_book_value v4 on r.id = v4.record_id and v4.attribute_id = 5233
left join ref_book_value v5 on r.id = v5.record_id and v5.attribute_id = 5234
where ref_book_id = 521 and exists (select 1 from form_data fd where fd.id = v3.number_value);

delete from ref_book_record where ref_book_id = 521;
commit;

----------------------------------------------------------------------------------------------------------------
--http://jira.aplana.com/browse/SBRFACCTAX-14691: Новое событие ЖА - Изменение пути базы открытых ключей
insert into event (id, name) values (951, 'Редактирование конфигурационного параметра');
insert into role_event (event_id, role_id) values (951, 5);

alter table log_system drop constraint log_system_chk_dcl_form;
alter table log_system add constraint log_system_chk_dcl_form check (event_id in (7, 11, 401, 402, 501, 502, 503, 601, 650, 901, 902, 903, 810, 811, 812, 813, 820, 821, 830, 831, 832, 840, 841, 842, 850, 860, 701, 702, 703, 704, 705, 904, 951) or declaration_type_name is not null or (form_type_name is not null and form_kind_id is not null));
alter table log_system drop constraint log_system_chk_rp;
alter table log_system add constraint log_system_chk_rp check (event_id in (7, 11, 401, 402, 501, 502, 503, 601, 650, 901, 902, 903, 810, 811, 812, 813, 820, 821, 830, 831, 832, 840, 841, 842, 850, 860, 701, 702, 703, 704, 705, 904, 951) or report_period_name is not null);

--https://jira.aplana.com/browse/SBRFACCTAX-15438: БД. Изменения для "Фиксировать в ЖА изменения конфиг. параметров"
UPDATE ref_book_attribute SET NAME = 'Ограничение на выполнение задания в очереди быстрых заданий' WHERE ID = 4104;
UPDATE ref_book_attribute SET NAME = 'Ограничение на выполнение задания' WHERE ID = 4105;
----------------------------------------------------------------------------------------------------------------
--http://jira.aplana.com/browse/SBRFACCTAX-14602: Заархивировать JasperPrint-отчеты декларации
set serveroutput on size 1000000;

create or replace and compile java source named "ZipBlob" as
import oracle.sql.BLOB;
import java.io.EOFException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
public class ZipBlob {
  public static BLOB compress(BLOB blob, String fileName)
    throws Exception {
    Connection con = DriverManager.getConnection("jdbc:default:connection:");
    BLOB result = BLOB.createTemporary(con, true, BLOB.DURATION_SESSION);
    ZipOutputStream out = new ZipOutputStream(result.getBinaryOutputStream());
    InputStream in = null;
    try {
      in = blob.getBinaryStream();
      out.putNextEntry(new ZipEntry(fileName));
      byte[] b = new byte[blob.getChunkSize()];
      int iCount;
      do {
        iCount = in.read(b);
        if (iCount != -1) {
          out.write(b, 0, iCount);
        }
      } while (iCount != -1);
    } catch (EOFException e) {
    } finally {
      if (in != null) {in.close();}
    }
    out.close();
    return result;
  }
}
/

create or replace package pck_zip as
 function blob_compress(
     p_source_blob blob
   , p_source_file_name varchar2
 ) return blob as language java name
   'ZipBlob.compress(oracle.sql.BLOB, java.lang.String) return oracle.sql.BLOB';
end;
/

declare
   err_msg VARCHAR2(200);
   b_temp_file BLOB;
   b_compressed_file BLOB;
   v_filename varchar2(256) := 'report.jasper';
   cursor data_to_compress is
        select dd.id as declaration_data_id, dt.name as template_name, d.name as department_name, rp.name as report_period_name, tp.year, jasper.blob_data_id as jasper_blob_data_id, bd_jasper.data as bd_jasper_data, excel.blob_data_id as excel_blob_data_id, rawtohex(DBMS_LOB.SUBSTR(BLOB_TO_CLOB(bd_jasper.data), 4, 1))
        from declaration_report jasper
        join declaration_data dd on dd.id = jasper.declaration_data_id
        join declaration_template dt on dt.id = dd.declaration_template_id
        join department_report_period drp on drp.id = dd.department_report_period_id
        join department d on d.id = drp.department_id
        join report_period rp on rp.id = drp.report_period_id
        join tax_period tp on tp.id = rp.tax_period_id
        join blob_data bd_jasper on bd_jasper.id = jasper.blob_data_id
        left join declaration_report excel on jasper.declaration_data_id = excel.declaration_data_id and excel.type = 0
        where jasper.type = 3 and rawtohex(DBMS_LOB.SUBSTR(BLOB_TO_CLOB(bd_jasper.data), 4, 1)) <> '504B0304'
        order by excel.blob_data_id nulls last;
 begin
	dbms_output.enable (buffer_size => null);
	
	for x in data_to_compress loop  
		if (x.excel_blob_data_id is not null) then
			delete from blob_data where id = x.jasper_blob_data_id;
			dbms_output.put_line('Deleted : '||x.declaration_data_id||' // '||x.template_name ||' ('||x.department_name||' // '||x.report_period_name || ' ' || x.year||')');
		else 
			b_compressed_file := pck_zip.blob_compress(x.bd_jasper_data, v_filename);
			update blob_data bd set bd.data = b_compressed_file where bd.id = x.jasper_blob_data_id;
			dbms_output.put_line('Compressed : '||x.declaration_data_id||' // '||x.template_name ||' ('||x.department_name||' // '||x.report_period_name || ' ' || x.year||')');
		end if;
	end loop;        
 end;
/
commit;

----------------------------------------------------------------------------------------------------------------
--http://jira.aplana.com/browse/SBRFACCTAX-14699
alter table declaration_report drop constraint decl_report_pk; 
alter table declaration_report modify blob_data_id not null;
alter table declaration_report drop constraint decl_report_chk_type;
alter table declaration_report add constraint decl_report_chk_type check (type in (0, 1, 2, 3, 4));
comment on column declaration_report.type is 'Тип отчета (0 - Excel, 1 - XML, 2 - PDF, 3 - Jasper, 4 - Спец.отчет)';
alter table declaration_report add subreport_id number(9);
alter table declaration_report add constraint decl_report_chk_subreport_id check ((type = 4 and subreport_id is not null) or (type in (0, 1, 2, 3) and subreport_id is null));
comment on column declaration_report.subreport_id is 'Идентификатор спец. отчета';
alter table declaration_report modify type not null;
alter table declaration_report add constraint declaration_report_unq_combo unique (declaration_data_id, type, subreport_id); 
create index i_decl_report_blob_data_id on declaration_report(blob_data_id);

--http://jira.aplana.com/browse/SBRFACCTAX-14735
create table declaration_subreport
(
id number(9) not null,
declaration_template_id number(9) not null,
name varchar2(1000) not null,
ord number(9) not null,
alias varchar2(128) not null,
blob_data_id varchar2(36)
);
 
comment on table declaration_subreport is 'Спец. отчеты версии макета декларации';
comment on column declaration_subreport.id is 'Идентификатор отчета';
comment on column declaration_subreport.declaration_template_id is 'Идентификатор шаблона декларации';
comment on column declaration_subreport.name is 'Наименование спец. отчета';
comment on column declaration_subreport.ord is 'Порядковый номер';
comment on column declaration_subreport.alias is 'Код спец. отчета';
comment on column declaration_subreport.blob_data_id is 'Макет JasperReports для формирования печатного представления формы';
comment on table declaration_subreport is 'Спец. отчеты версии макета декларации';
 
alter table declaration_subreport add constraint decl_subrep_pk primary key(id);
alter table declaration_subreport add constraint decl_subrep_unq_combo unique (declaration_template_id, alias);
alter table declaration_subreport add constraint decl_subrep_fk_decl_template foreign key (declaration_template_id) references declaration_template(id) on delete cascade;
alter table declaration_subreport add constraint decl_subrep_fk_blob_data foreign key (blob_data_id) references blob_data(id);
create index i_decl_subrep_blob_data_id on declaration_subreport(blob_data_id);


alter table declaration_report add constraint decl_report_fk_decl_subreport foreign key (subreport_id) references declaration_subreport(id) on delete cascade;

create sequence seq_declaration_subreport start with 1;
----------------------------------------------------------------------------------------------------------------
--https://jira.aplana.com/browse/SBRFACCTAX-14930: 1.0 БД. Добавить проверку уникальности полей FORM_TYPE.CODE и DEPARTMENT.SBRF_CODE с учетом регистронезависимости.
alter table form_type drop constraint form_type_uniq_code;
create unique index i_form_type_uniq_code on form_type (upper(code));

CREATE OR REPLACE TRIGGER "DEPARTMENT_BEFORE_INS_UPD"     
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
                         p_in_accruing                      number,
						 p_in_periodorder					number
                        ) RETURN t_source PIPELINED;

  FUNCTION get_destinations (
                         p_in_stateRestriction              number,
                         p_in_excludeIfNotExist             number,
                         p_in_sourceFormDataId              number,
                         p_in_formTemplateId                number,
                         p_in_departmentReportPeriodId      number,
                         p_in_kind                          number,
                         p_in_compPeriod                    number,
                         p_in_accruing                      number,
						 p_in_periodorder					number
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
                         p_in_accruing                      number,
						             p_in_periodorder					number
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
                nvl(dual_fd.PERIOD_ORDER, fd.period_order) as period_order
                from (
                 select p_in_destinationFormDataId as ID, p_in_formTemplateId as FORM_TEMPLATE_ID, p_in_kind as KIND, p_in_departmentReportPeriodId as DEPARTMENT_REPORT_PERIOD_ID, p_in_compPeriod as COMPARATIVE_DEP_REP_PER_ID, p_in_accruing as ACCRUING, p_in_periodorder as PERIOD_ORDER from dual) dual_fd
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
                         p_in_accruing                      number,
						             p_in_periodorder					number
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
					select p_in_sourceFormDataId as ID, p_in_formTemplateId as FORM_TEMPLATE_ID, p_in_kind as KIND, p_in_departmentReportPeriodId as DEPARTMENT_REPORT_PERIOD_ID, p_in_compPeriod as COMPARATIVE_DEP_REP_PER_ID, p_in_accruing as ACCRUING, p_in_periodorder as PERIOD_ORDER from dual) dual_fd
					left join form_data fd on fd.id = dual_fd.id) fd
					join department_report_period drp on drp.id = fd.DEPARTMENT_REPORT_PERIOD_ID and (p_in_sourceFormDataId is null or fd.id = p_in_sourceFormDataId)
					join department_report_period neighbours_drp on neighbours_drp.report_period_id = drp.report_period_id
					join (
						  select id, form_template_id, kind, department_report_period_id, COMPARATIVE_DEP_REP_PER_ID, accruing, period_order from form_data
						  union all (select null as ID, cast(p_in_formTemplateId as NUMBER(9,0)) as FORM_TEMPLATE_ID, cast(p_in_kind as NUMBER(9,0)) as KIND, cast(p_in_departmentReportPeriodId as NUMBER(18,0)) as DEPARTMENT_REPORT_PERIOD_ID, cast(p_in_compPeriod as NUMBER(18,0)) as COMPARATIVE_DEP_REP_PER_ID, cast(p_in_accruing as NUMBER(1,0)) as ACCRUING, cast(p_in_periodorder as number(2)) as PERIOD_ORDER from dual)
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
--------------------------------------------------------------------------------------------------------------
drop package pck_zip;
drop java source "ZipBlob";
--------------------------------------------------------------------------------------------------------------
--https://jira.aplana.com/browse/SBRFACCTAX-15555: SQLException при удалении подразделения, если существуют(существовали) ссылки в настройках подразделений
CREATE OR REPLACE TRIGGER "DEPARTMENT_BEFORE_DELETE"    
before delete on department
for each row
declare
    pragma autonomous_transaction;

    vCurrentDepartmentID number(9) := :old.id;
    vCurrentDepartmentType number(9) := :old.type;
    vHasLinks number(9) := -1;
    vHasDescendant number(9) := -1;
begin
--Подразделение с типом "Банк"(type=1) не может быть удалено
if vCurrentDepartmentType=1 then
   raise_application_error(-20001, 'Подразделение с типом "Банк" не может быть удалено');
end if;

--Существуют дочерние подразделения
select count(*) into vHasDescendant
from department
start with parent_id = vCurrentDepartmentID
connect by parent_id = prior id;

if vHasDescendant != 0 then
   raise_application_error(-20002, 'Подразделение, имеющее дочерние подразделения, не может быть удалено');
end if;

--Ссылочная целостность
--FORM_DATA
select count(*) into  vHasLinks from form_data fd
join department_report_period drp on drp.id = fd.department_report_period_id and drp.department_id = vCurrentDepartmentID;

if vHasLinks !=0 then
   raise_application_error(-20003, 'Подразделение не может быть удалено, если на него существует ссылка в FORM_DATA');
end if;

--DECLARATION_DATA
select count(*) into  vHasLinks from declaration_data dd
join department_report_period drp on drp.id = dd.department_report_period_id and drp.department_id = vCurrentDepartmentID;

if vHasLinks !=0 then
   raise_application_error(-20004, 'Подразделение не может быть удалено, если на него существует ссылка в DECLARATION_DATA');
end if;

--SEC_USER
select count(*) into  vHasLinks from sec_user where department_id = vCurrentDepartmentID;

if vHasLinks !=0 then
   raise_application_error(-20005, 'Подразделение не может быть удалено, если на него существует ссылка в SEC_USER');
end if;

--REF_BOOK_VALUE
select count(*) into  vHasLinks from ref_book_value rbv
join ref_book_attribute rba on rba.id = rbv.attribute_id and rba.reference_id = 30
where rbv.reference_value = vCurrentDepartmentID;

if vHasLinks !=0 then
   raise_application_error(-20006, 'Подразделение не может быть удалено, если на него существует ссылка в REF_BOOK_VALUE');
end if;


end department_before_delete;
/
--------------------------------------------------------------------------------------------------------------
commit;
exit;