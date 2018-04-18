
BEGIN
			
	merge into ref_book_oktmo a using
	(select '27714000' as code, 'Населенные пункты, входящие в состав Неманского городского округа' as name, 2 as razd, to_date('01.03.2017','dd.mm.yyyy') as version from dual
	union all 
	select '27703000' as code, 'Населенные пункты, входящие в состав Багратионовского городского округа' as name, 2 as razd, to_date('01.03.2017','dd.mm.yyyy') as version from dual
	union all 
	select '27718000' as code, 'Населенные пункты, входящие в состав Полесского городского округа' as name, 2 as razd, to_date('01.03.2017','dd.mm.yyyy') as version from dual
	union all 
	select '49625405' as code, 'Тёсово-Нетыльское' as name, 1 as razd, to_date('01.01.2018','dd.mm.yyyy') as version from dual
	union all 
	select '46729000' as code, 'Зарайск' as name, 1 as razd, to_date('01.01.2018','dd.mm.yyyy') as version from dual
	union all 
	select '46733000' as code, 'Истра' as name, 1 as razd, to_date('01.01.2018','dd.mm.yyyy') as version from dual
	union all 
	select '46744000' as code, 'Красногорск' as name, 1 as razd, to_date('01.01.2018','dd.mm.yyyy') as version from dual
	union all 
	select '46747000' as code, 'Луховицы' as name, 1 as razd, to_date('01.01.2018','dd.mm.yyyy') as version from dual
	union all 
	select '46748000' as code, 'Люберцы' as name, 1 as razd, to_date('01.01.2018','dd.mm.yyyy') as version from dual
	union all 
	select '46750000' as code, 'Наро-Фоминский' as name, 1 as razd, to_date('01.03.2018','dd.mm.yyyy') as version from dual
	union all 
	select '46759000' as code, 'Павловский Посад' as name, 1 as razd, to_date('01.01.2018','dd.mm.yyyy') as version from dual
	union all 
	select '46766000' as code, 'Рузский' as name, 1 as razd, to_date('01.01.2018','dd.mm.yyyy') as version from dual
	union all 
	select '46776000' as code, 'Ступино' as name, 1 as razd, to_date('01.03.2018','dd.mm.yyyy') as version from dual
	union all 
	select '46784000' as code, 'Чехов' as name, 1 as razd, to_date('01.03.2018','dd.mm.yyyy') as version from dual
	union all 
	select '46786000' as code, 'Шатура' as name, 1 as razd, to_date('01.03.2018','dd.mm.yyyy') as version from dual
	union all 
	select '28752000' as code, 'Населенные пункты, входящие в состав Осташковского городского округа' as name, 2 as razd, to_date('01.01.2018','dd.mm.yyyy') as version from dual
	) b
	on (a.code=b.code and a.status=0)
	when not matched then
		insert (id, record_id, version, status, code, name, razd)
		values (seq_ref_book_record.nextval,seq_ref_book_record_row_id.nextval, b.version, 0, b.code, b.name, b.razd);
		
	update ref_book_oktmo set name='Неманский (г Неман)', razd=1 where code='27714000' and status=0;
	update ref_book_oktmo set name='Багратионовский', razd=1 where code='27703000' and status=0;
	update ref_book_oktmo set name='Полесский (г Полесск)', razd=1 where code='27718000' and status=0;
	
	merge into ref_book_oktmo a using
	(select '22739000' as code, 'Перевозский (г Перевоз)' as name, 1 as razd, to_date('01.01.2018','dd.mm.yyyy') as version from dual
	union all 
	select '41648165' as code, 'Фёдоровское (гп Фёдоровское)' as name, 1 as razd, to_date('01.01.2018','dd.mm.yyyy') as version from dual
	union all 
	select '07705000' as code, 'Благодарненский (г Благодарный)' as name, 1 as razd, to_date('01.01.2018','dd.mm.yyyy') as version from dual
	union all 
	select '07713000' as code, 'Изобильненский (г Изобильный)' as name, 1 as razd, to_date('01.01.2018','dd.mm.yyyy') as version from dual
	union all 
	select '07714000' as code, 'Ипатовский (г Ипатово)' as name, 1 as razd, to_date('01.01.2018','dd.mm.yyyy') as version from dual
	union all 
	select '07716000' as code, 'Кировский (г Новопавловск)' as name, 1 as razd, to_date('01.01.2018','dd.mm.yyyy') as version from dual
	union all 
	select '07725000' as code, 'Нефтекумский (г Нефтекумск)' as name, 1 as razd, to_date('01.01.2018','dd.mm.yyyy') as version from dual
	union all 
	select '07731000' as code, 'Петровский (г Светлоград)' as name, 1 as razd, to_date('01.01.2018','dd.mm.yyyy') as version from dual
	union all 
	select '07735000' as code, 'Советский (г Зеленокумск)' as name, 1 as razd, to_date('01.01.2018','dd.mm.yyyy') as version from dual
	union all 
	select '41630152' as code, 'Аннинское (гп Новоселье)' as name, 1 as razd, to_date('01.01.2018','dd.mm.yyyy') as version from dual
	union all 
	select '07726000' as code, 'Новоалександровский (г Новоалександровск)' as name, 1 as razd, to_date('01.01.2018','dd.mm.yyyy') as version from dual
	union all 
	select '26720000' as code, 'город Сунжа (г Сунжа)' as name, 1 as razd, to_date('01.01.2017','dd.mm.yyyy') as version from dual
	) b
	on (a.code=b.code and a.status=0)
	when not matched then
		insert (id, record_id, version, status, code, name, razd)
		values (seq_ref_book_record.nextval,seq_ref_book_record_row_id.nextval, b.version, 0, b.code, b.name, b.razd);
		
	commit;

END;
/