--http://jira.aplana.com/browse/SBRFACCTAX-12614: 0.8 Добавить новые поля в FORM_DATA
alter table form_data add sorted_backup number(1) default 0 not null, edited number(1) default 0 not null;
comment on column form_data.sorted_backup is 'Статус актуальности сортировки НФ для резервного среза (0 - Сортировка неактуальна; 1 - Сортировка актуальна)';
comment on column form_data.edited is 'Признак изменения данных НФ в режиме редактирования (0 - Нет изменений; 1 - Есть изменения)';

alter table form_data add constraint form_data_chk_edited check (edited in (0, 1));
alter table form_data add constraint form_data_chk_sorted_backup check (sorted_backup in (0, 1));

--http://jira.aplana.com/browse/SBRFACCTAX-12692: 0.8 Добавить в патч изменение таблиц LOCK_DATA и CONFIGURATION_LOCK
alter table LOCK_DATA modify DATE_LOCK default sysdate;
update CONFIGURATION_LOCK set timeout = timeout / 60000 where timeout >= 60000;


commit;
exit;