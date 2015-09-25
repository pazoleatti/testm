--http://jira.aplana.com/browse/SBRFACCTAX-12614: 0.8 Добавить новые поля в FORM_DATA
alter table form_data add sorted_backup number(1) default 0 not null, edited number(1) default 0 not null;
comment on column form_data.sorted_backup is 'Статус актуальности сортировки НФ для резервного среза (0 - Сортировка неактуальна; 1 - Сортировка актуальна)';
comment on column form_data.edited is 'Признак изменения данных НФ в режиме редактирования (0 - Нет изменений; 1 - Есть изменения)';

alter table form_data add constraint form_data_chk_edited check (edited in (0, 1));
alter table form_data add constraint form_data_chk_sorted_backup check (sorted_backup in (0, 1));

--http://jira.aplana.com/browse/SBRFACCTAX-12692: 0.8 Добавить в патч изменение таблиц LOCK_DATA и CONFIGURATION_LOCK
alter table lock_data modify date_lock default sysdate;
alter table lock_data drop column date_before;
drop table configuration_lock;

--http://jira.aplana.com/browse/SBRFACCTAX-12711: Обязательность заполнения для form_data.accruing
update form_data set accruing = 0 where accruing is null;
alter table form_data modify accruing default 0 not null;
comment on column form_data.accruing is 'Признак расчета значений нарастающим итогом (0 - не нарастающим итогом, 1 - нарастающим итогом)';


--http://jira.aplana.com/browse/SBRFACCTAX-12708: 0.8 БД. "Файлы и комментарии". Добавить поле COMMENT в FORM_DATA и таблицу FORM_DATA_FILE
alter table form_data add note varchar2(512);
comment on column form_data.note is 'Комментарий к НФ, вводимый в модальном окне "Файлы и комментарии"';

create table form_data_file
(
   form_data_id number(18) not null,
   blob_data_id varchar2(36) not null,
   user_name varchar2(512) not null,
   user_department_name varchar2(4000) not null,
   note varchar2(512)    
);

comment on table form_data_file is 'Файлы налоговой формы';
comment on column form_data_file.form_data_id is 'Идентификатор экземпляра налоговой формы';
comment on column form_data_file.blob_data_id is 'Файл налоговой формы';
comment on column form_data_file.user_name is 'Полное имя пользователя, прикрепившего файл';
comment on column form_data_file.user_department_name is 'Наименование подразделения пользователя, прикрепившего файл';
comment on column form_data_file.note is 'Комментарий к файлу';

alter table form_data_file add constraint form_data_file_pk primary key (blob_data_id, form_data_id);
alter table form_data_file add constraint form_data_file_fk_form_data foreign key (form_data_id) references form_data(id);
alter table form_data_file add constraint form_data_file_fk_blob_data foreign key (blob_data_id) references blob_data(id);

--http://jira.aplana.com/browse/SBRFACCTAX-12762: Ограничение для аудита
insert into event (id, name) values (904, 'Импорт скриптов');

alter table log_system drop constraint log_system_chk_rp;
alter table log_system drop constraint log_system_chk_dcl_form;

alter table log_system add constraint log_system_chk_rp check (event_id in (7, 11, 401, 402, 501, 502, 503, 601, 650, 901, 902, 903, 810, 811, 812, 813, 820, 821, 830, 831, 832, 840, 841, 842, 850, 860, 701, 702, 703, 704, 705, 904) or report_period_name is not null);
alter table log_system add constraint log_system_chk_dcl_form check (event_id in (7, 11, 401, 402, 501, 502, 503, 601, 650, 901, 902, 903, 810, 811, 812, 813, 820, 821, 830, 831, 832, 840, 841, 842, 850, 860, 701, 702, 703, 704, 705, 904) or declaration_type_name is not null or (form_type_name is not null and form_kind_id is not null));


--http://jira.aplana.com/browse/SBRFACCTAX-12760: PK для lock_data_subscribers
alter table lock_data_subscribers add constraint lock_data_subscribers_pk primary key (lock_key, user_id);

commit;
exit;