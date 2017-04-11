create or replace view vw_depart_kpp_oktmo
as
select d.id dep_id,d.name dep_name,
       sd.kpp,o.code oktmo
  from department d join ref_book_ndfl s on (s.department_id=d.id)
                    join ref_book_ndfl_detail sd on (sd.ref_book_ndfl_id=s.id)
                    join ref_book_oktmo o on (o.id=sd.oktmo);
/
create or replace view vw_decl_kpp_oktmo_form7
as
select decl.id,decl.kpp,decl.oktmo,templ.name,templ.form_kind,per.report_period_id,per.correction_date
from declaration_data decl join declaration_template templ on (templ.id=decl.declaration_template_id)
                           join department_report_period per on (per.id=decl.department_report_period_id)
where templ.form_kind=7
union
select decl.id,inc.kpp,inc.oktmo,templ.name,templ.form_kind,per.report_period_id,per.correction_date
from declaration_data decl join declaration_template templ on (templ.id=decl.declaration_template_id)
                           join department_report_period per on (per.id=decl.department_report_period_id)
                           join ndfl_person pers on (pers.declaration_data_id=decl.id)
                           join ndfl_person_income inc on (inc.ndfl_person_id=pers.id)
where templ.form_kind=7;
/
exit;