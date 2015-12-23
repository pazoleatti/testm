--http://jira.aplana.com/browse/SBRFACCTAX-12997: Комментарии к справочнику цветов (0.8!)
comment on table color is 'Справочник цветов';
comment on column color.id is 'Идентификатор записи';
comment on column color.name is 'Наименование цвета';
comment on column color.r is 'R';
comment on column color.g is 'G';
comment on column color.b is 'B';
comment on column color.hex is 'Hex';

--Удаление ненужных объектов
drop sequence seq_data_row;
drop procedure create_form_data_nnn_archive;
---------------------------------------------------------------------------------------------------

--http://jira.aplana.com/browse/SBRFACCTAX-13698: Отключить констрейнт ЖА->SEC_USER
alter table log_system disable constraint log_system_fk_user_login;

--http://jira.aplana.com/browse/SBRFACCTAX-13592: Изменить индекс I_REF_BOOK_RECORD_REFBOOKID
drop index i_ref_book_record_refbookid;
create unique index i_ref_book_record_refbookid on ref_book_record (
       case when status <> -1 then ref_book_id else null end, 
       case when status <> -1 then record_id else null end, 
       case when status <> -1 then version else null end);

--http://jira.aplana.com/browse/SBRFACCTAX-13460: Изменить ограничение на длину поля FORM_TYPE.CODE до 9 символов
update form_type set code = substrc(code, 1, 9) where code is not null and code <> substrc(code, 1, 9);
alter table form_type modify code varchar2(9 char);

--http://jira.aplana.com/browse/SBRFACCTAX-13905:  Назначения нескольких исполнителей для связки нф-подразделение
create table department_form_type_performer
(
  department_form_type_id number(9) not null,
  performer_dep_id number(9) not null
);

comment on table department_form_type_performer is 'Назначения нескольких исполнителей для связки НФ-подразделение';
comment on column department_form_type_performer.department_form_type_id is 'Идентификатор связи подразделения с формой';
comment on column department_form_type_performer.performer_dep_id is 'Исполнитель'; 

alter table department_form_type_performer add constraint department_form_type_perf_pk primary key (department_form_type_id, performer_dep_id);
alter table department_form_type_performer add constraint dept_form_type_perf_fk_perf foreign key (performer_dep_id) references department (id);
alter table department_form_type_performer add constraint dept_form_type_perf_fk_id foreign key (department_form_type_id) references department_form_type (id) on delete cascade; 

merge into department_form_type_performer tgt 
using (select id, performer_dep_id from department_form_type where performer_dep_id is not null) src
on (tgt.department_form_type_id = src.id and tgt.performer_dep_id = src.performer_dep_id)
when not matched then
	insert (tgt.department_form_type_id, tgt.performer_dep_id) values (src.id, src.performer_dep_id);
	
alter table department_form_type drop column performer_dep_id;	

-----------------------------------------------------------------------------------------
--http://jira.aplana.com/browse/SBRFACCTAX-13912 Изменение таблицы form_data_report для специфичных отчетов
alter table form_data_report drop constraint form_data_rep_pk;

alter table form_data_report add type_num number(1);
update form_data_report set type_num = type;
alter table form_data_report drop column type;
alter table form_data_report add type varchar2(50 char);
comment on column form_data_report.type is 'Тип отчета (Excel/CSV/Специфичный отчет)';

update form_data_report set type = decode (type_num, 0, 'XLSM', 1, 'CSV');
alter table form_data_report modify type not null;
alter table form_data_report add constraint form_data_rep_pk primary key (form_data_id, type, manual, checking, absolute);
alter table form_data_report drop column type_num;

-----------------------------------------------------------------------------------------
--http://jira.aplana.com/browse/SBRFACCTAX-14002: Новое поле NOTIFICATION.URL
alter table notification add url varchar2(2000);
comment on column notification.url is 'Ссылка';



commit;
exit;