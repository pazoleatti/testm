DECLARE
	v_id number;
BEGIN

	update ref_book set xsd_id='290d3b24-ba60-408a-b4a7-ec5c0d166efa' where id=904;
	
	update ref_book_attribute set max_length=500 where ref_book_id = 901 and alias in ('DISTRICT', 'CITY', 'LOCALITY', 'STREET');
	
	update ref_book_income_kind set ref_book_income_kind.VERSION=(select ref_book_income_type.version from ref_book_income_type where ref_book_income_type.id = ref_book_income_kind.income_type_id)
            where exists(select 1 from ref_book_income_type where ref_book_income_type.id = ref_book_income_kind.income_type_id);
	
	merge into ref_book_income_kind a using
	(select (select id from ref_book_income_type where code = '1543' and status = 2) as income_type_id, '01' as mark, 'Начисление дохода при выводе денежных средств с брокерского счёта' as name,
		(select version from ref_book_income_type where code = '1543' and status = 2) as version, 2 as status, 
		(select record_id from ref_book_income_kind where income_type_id = (select id from ref_book_income_type where code = '1543' and status = 0) and mark='01') as record_id from dual) b
	on (a.income_type_id=b.income_type_id and a.mark=b.mark and a.status=b.status)
	when not matched then
	insert (id, income_type_id, mark, name, version, status, record_id)
	values (seq_ref_book_record.nextval, b.income_type_id, b.mark, b.name, b.version, b.status, b.record_id);
	
	merge into ref_book_income_kind a using
	(select (select id from ref_book_income_type where code = '1543' and status = 2) as income_type_id, '02' as mark, 'Начисление дохода при выводе ценных бумаг с торгового раздела счёта депо' as name,
		(select version from ref_book_income_type where code = '1543' and status = 2) as version, 2 as status, 
		(select record_id from ref_book_income_kind where income_type_id = (select id from ref_book_income_type where code = '1543' and status = 0) and mark='02') as record_id from dual) b
	on (a.income_type_id=b.income_type_id and a.mark=b.mark and a.status=b.status)
	when not matched then
	insert (id, income_type_id, mark, name, version, status, record_id)
	values (seq_ref_book_record.nextval, b.income_type_id, b.mark, b.name, b.version, b.status, b.record_id);
	
	merge into ref_book_income_kind a using
	(select (select id from ref_book_income_type where code = '1543' and status = 2) as income_type_id, '03' as mark, 'Начисление дохода при расторжении договора брокерского обслуживания' as name,
		(select version from ref_book_income_type where code = '1543' and status = 2) as version, 2 as status, 
		(select record_id from ref_book_income_kind where income_type_id = (select id from ref_book_income_type where code = '1543' and status = 0) and mark='03') as record_id from dual) b
	on (a.income_type_id=b.income_type_id and a.mark=b.mark and a.status=b.status)
	when not matched then
	insert (id, income_type_id, mark, name, version, status, record_id)
	values (seq_ref_book_record.nextval, b.income_type_id, b.mark, b.name, b.version, b.status, b.record_id);
	
	merge into ref_book_income_kind a using
	(select (select id from ref_book_income_type where code = '1543' and status = 2) as income_type_id, '04' as mark, 'Начисление дохода по окончанию налогового периода за который начислен доход' as name,
		(select version from ref_book_income_type where code = '1543' and status = 2) as version, 2 as status, 
		(select record_id from ref_book_income_kind where income_type_id = (select id from ref_book_income_type where code = '1543' and status = 0) and mark='04') as record_id from dual) b
	on (a.income_type_id=b.income_type_id and a.mark=b.mark and a.status=b.status)
	when not matched then
	insert (id, income_type_id, mark, name, version, status, record_id)
	values (seq_ref_book_record.nextval, b.income_type_id, b.mark, b.name, b.version, b.status, b.record_id);

	update ref_book set is_versioned = 1 where id = 933;
	
	delete from REF_BOOK_ID_DOC where doc_id in (select id from REF_BOOK_DOC_TYPE where code in ('22', '09', '28', '26', '27', '05', '81', '60', '61', '62', '63', '01', '02', '18', '04', '06'));
	
	delete from REF_BOOK_DOC_TYPE where code in ('22', '09', '28', '26', '27', '05', '81', '60', '61', '62', '63', '01', '02', '18', '04', '06');
END;
/