create or replace view vw_decl_kpp_oktmo_form7 (id, kpp, oktmo, name, form_kind, report_period_id, correction_date, pers_cnt, note) as 
select decl.id,decl.kpp,decl.oktmo,templ.name,templ.form_kind,per.report_period_id,per.correction_date,
       case when templ.name='6-НДФЛ' then report_pkg.GetNDFL6PersCnt(decl.id)
            when templ.name like '2-НДФЛ%' then (select count(*) from ndfl_references r where r.declaration_data_id=decl.id)
            else null
       end pers_cnt,
       decl.note
from declaration_data decl join declaration_template templ on (templ.id=decl.declaration_template_id)
                           join department_report_period per on (per.id=decl.department_report_period_id)
where templ.form_kind=7
union
select decl.id,inc.kpp,inc.oktmo,templ.name,templ.form_kind,per.report_period_id,per.correction_date,
       case when templ.name='6-НДФЛ' then report_pkg.GetNDFL6PersCnt(decl.id)
            when templ.name like '2-НДФЛ%' then (select count(*) from ndfl_references r where r.declaration_data_id=decl.id)
            else null
       end pers_cnt,
       decl.note
from declaration_data decl join declaration_template templ on (templ.id=decl.declaration_template_id)
                           join department_report_period per on (per.id=decl.department_report_period_id)
                           join ndfl_person pers on (pers.declaration_data_id=decl.id)
                           join ndfl_person_income inc on (inc.ndfl_person_id=pers.id)
where templ.form_kind=7;
