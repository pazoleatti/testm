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

commit;
exit;