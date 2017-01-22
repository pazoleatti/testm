update declaration_template t set t.form_kind=(case when lower(t.name) like '%первичная%' then 3
                                                    when lower(t.name) like '%консолидированная%' then 2
                                                    else 7
                                               end);

merge into declaration_template t
using (select d.templ_id,d.templ_name,d.status,min(r.id) type_id
        from (select t.id templ_id,t.name templ_name,t.status,
                     trim(replace(replace(replace(replace(substr(t.name,1,decode(instr(t.name,'('),0,length(t.name)+1,instr(t.name,'('))-1),'_','-'),'Test-',''),'6-','6 '),'2-','2 ')) templ_code
                from declaration_template t) d left join ref_book_form_type r on (trim(replace(substr(r.code,1,instr(r.code||'(','(')-1),'Расчет ',''))=d.templ_code)
            group by d.templ_id,d.templ_name,d.status) v 
   on (t.id=v.templ_id)
when matched then update
  set t.form_type=v.type_id;
