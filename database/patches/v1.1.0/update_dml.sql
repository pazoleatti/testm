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

commit;
exit;
