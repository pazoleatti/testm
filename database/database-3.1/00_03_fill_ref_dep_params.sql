declare 
	v_task_name varchar2(128):='fill_ref_dep_params block #1 - deleting';  
begin
	delete from ref_book_ndfl_detail;
	delete from ref_book_ndfl;
	dbms_output.put_line(v_task_name||'[INFO]:'||' Success');

	v_task_name:='fill_ref_dep_params block #2 - update tmp_dep_params';  		
	update tmp_dep_params set record_id=seq_ref_book_record_row_id.nextval;
	dbms_output.put_line(v_task_name||'[INFO]:'||' Success');
	
	v_task_name:='fill_ref_dep_params block #3 - insert into ref_book_ndfl';  	
	insert into ref_book_ndfl(department_id,version,record_id,status,id)
	select a.*,seq_ref_book_record_row_id.nextval,0,seq_ref_book_record.nextval
	from (select distinct d.dep_id,to_date(t.start_date,'dd.mm.yyyy') as version from tmp_dep_params t join tmp_depart d on upper(d.name)=trim(upper(replace(t.titname,'ё','е')))) a;
 	dbms_output.put_line(v_task_name||'[INFO]:'||' Success');
	
	v_task_name:='fill_ref_dep_params block #4 - insert into ref_book_ndfl_detail real data';  	
	insert into ref_book_ndfl_detail(id,record_id,version,status,ref_book_ndfl_id,row_ord,department_id,
									tax_organ_code,kpp,present_place,name,oktmo,phone, 
									signatory_id,signatory_surname,signatory_firstname,signatory_lastname,approve_doc_name)
	with a as (
	select t.record_id,to_date(t.start_date,'dd.mm.yyyy') as version,0 as status,n.id ref_book_ndfl_id,t.row_num as row_ord,d.dep_id as department_id,
           t.tax_end as tax_organ_code,t.kpp,pp.id as present_place,t.titname as name,o.mx_id as oktmo,t.phone,
		   s.mx_id as signatory_id,t.surname as signatory_surname,t.name as signatory_firstname,t.lastname as signatory_lastname,t.docname as doc_name
	from tmp_dep_params t 
	join tmp_depart d on (upper(d.name)=trim(upper(replace(t.titname,'ё','е'))))
	join ref_book_ndfl n on (n.department_id=d.dep_id and n.version=to_date(t.start_date,'dd.mm.yyyy'))
	left join ref_book_present_place pp on (pp.code=t.place and pp.status=0)
	left join (select distinct code,last_value(id) over(partition by code order by version rows between unbounded preceding and unbounded following) as mx_id 
			   from ref_book_oktmo where status=0) o on (o.code=t.oktmo)
	left join (select distinct code,last_value(id) over(partition by code order by version rows between unbounded preceding and unbounded following) as mx_id 
			   from ref_book_signatory_mark where status=0) s on (s.code=t.sign)
	) 
	select seq_ref_book_record.nextval as id,record_id,version,status,ref_book_ndfl_id,row_ord,department_id,
           tax_organ_code,kpp,present_place,name,oktmo,phone,
		   signatory_id,signatory_surname,signatory_firstname,signatory_lastname,doc_name
	from a;
	dbms_output.put_line(v_task_name||'[INFO]:'||' Success');
	
	v_task_name:='fill_ref_dep_params block #5 - insert into ref_book_ndfl_detail pseudo data';  	
	insert into ref_book_ndfl_detail(id,record_id,version,status,ref_book_ndfl_id,row_ord,department_id,
									tax_organ_code,kpp,present_place,name,oktmo,phone, 
									signatory_id,signatory_surname,signatory_firstname,signatory_lastname,approve_doc_name)
	with a as (
	select t.record_id,to_date(trim(t.end_date),'dd.mm.yyyy')+1 as version,2 as status,n.id ref_book_ndfl_id,t.row_num as row_ord,d.dep_id as department_id,
           t.tax_end as tax_organ_code,t.kpp,pp.id as present_place,t.titname as name,o.mx_id as oktmo,t.phone,
		   s.mx_id as signatory_id,t.surname as signatory_surname,t.name as signatory_firstname,t.lastname as signatory_lastname,t.docname as doc_name
	from tmp_dep_params t 
	join tmp_depart d on (upper(d.name)=trim(upper(replace(t.titname,'ё','е'))))
	join ref_book_ndfl n on (n.department_id=d.dep_id and n.version=to_date(t.start_date,'dd.mm.yyyy'))
	left join ref_book_present_place pp on (pp.code=t.place and pp.status=0)
	left join (select distinct code,last_value(id) over(partition by code order by version rows between unbounded preceding and unbounded following) as mx_id 
			   from ref_book_oktmo where status=0) o on (o.code=t.oktmo)
	left join (select distinct code,last_value(id) over(partition by code order by version rows between unbounded preceding and unbounded following) as mx_id 
			   from ref_book_signatory_mark where status=0) s on (s.code=t.sign)
	) 
	select seq_ref_book_record.nextval as id,record_id,version,status,ref_book_ndfl_id,row_ord,department_id,
           tax_organ_code,kpp,present_place,name,oktmo,phone,
		   signatory_id,signatory_surname,signatory_firstname,signatory_lastname,doc_name
	from a where version is not null;
	dbms_output.put_line(v_task_name||'[INFO]:'||' Success');
	
	commit;	
EXCEPTION
	when OTHERS then
		dbms_output.put_line(v_task_name||'[FATAL]:'||sqlerrm);
end;
/
