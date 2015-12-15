--http://jira.aplana.com/browse/SBRFACCTAX-13698: Отключить констрейнт ЖА->SEC_USER
alter table log_system disable constraint log_system_fk_user_login;

--http://jira.aplana.com/browse/SBRFACCTAX-13592: Изменить индекс I_REF_BOOK_RECORD_REFBOOKID
drop index i_ref_book_record_refbookid;
create unique index i_ref_book_record_refbookid on ref_book_record (
       case when status <> -1 then ref_book_id else null end, 
       case when status <> -1 then record_id else null end, 
       case when status <> -1 then version else null end);

--http://jira.aplana.com/browse/SBRFACCTAX-13460: Изменить ограничение на длину поля FORM_TYPE.CODE до 18 байт
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

/* --Отложенные изменения
merge into department_form_type_performer tgt 
using (select id, performer_dep_id from department_form_type where performer_dep_id is not null) src
on (tgt.department_form_type_id = src.id and tgt.performer_dep_id = src.performer_dep_id)
when not matched then
	insert (tgt.department_form_type_id, tgt.performer_dep_id) values (src.id, src.performer_dep_id);
	
alter table department_form_type drop performer_dep_id;	*/

commit;
exit;