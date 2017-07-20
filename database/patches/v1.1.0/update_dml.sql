prompt update attributes ref_book_ndfl_detaill
@@upd_attr_ndfl_det.sql;

prompt rename subreports 2-НДФЛ 1,2-НДФЛ 2
@@upd_subrep_name.sql;


prompt update event
update event set name='Вернуть в "Создана"' where id=106;

prompt merge configuration
merge into configuration t
using (select 'LIMIT_IDENT' code,'0.65' val from dual
       union
       select 'SHOW_TIMING' code,'0' val from dual) n
  on (t.code=n.code)
when matched then update
  set t.value=n.val
when not matched then
insert (t.code,t.department_id,t.value) values (n.code,0,n.val);

prompt update ref_book_income_kind
update ref_book_income_kind
   set mark = '13',
       name = 'Суммы вознаграждений, выплачиваемых за счет средств прибыли организации, средств специального назначения или целевых поступлений'
where income_type_id in (select id from ref_book_income_type t where t.code='2003' and t.status=0);

-- delete async_task_type
prompt delete async_task_type
delete from async_task_type where id in (3, 4);

-- update async_task_type
prompt update async_task_type
update async_task_type set limit_kind='Количество ФЛ в НФ' where id in (5, 6, 7, 14, 15);
update async_task_type set limit_kind='Количество ФЛ в НФ' where id = 26;
update async_task_type set short_queue_limit=3000 where id in (5, 6, 7, 14, 15);

-- update ref_book_tariff_payer
prompt update ref_book_tariff_payer
update ref_book_tariff_payer
set code='22'
where name='Плательщики страховых взносов, уплачивающие страховые взносы по дополнительным тарифам, установленных пунктом 2 статьи 428 Кодекса';

-- insert new asnu
prompt insert new asnu
insert into ref_book_asnu (id,code,name,type) values (15,'9000','АС "ЕКС"','Возврат платы за подключение к программе страхования)');

prompt update and alter ref_books
@upd_ref_books.sql;

-- UPDATE declaration_subreport_params
prompt UPDATE declaration_subreport_params
@@update_decl_subrep_params.sql

commit;
exit;
