delete from ref_book_attribute where ref_book_id=942;
delete from ref_book where id=942;


merge into ref_book_attribute a using
	(
		select 9005 as id, 900 as ref_book_id, 'Код роли' as name, 'ROLE_ALIAS' as alias, 4 as type, 3 as ord, 95 as reference_id, 839 as attribute_id, 1 as visible, null as precision, 
		20 as width, 1 as required, 1 as is_unique, null as sort_order,  null as format, 0 as read_only, null as max_length from dual
		union all 
		select 9006 as id, 900 as ref_book_id, 'Наименование роли' as name, 'ROLE_NAME' as alias, 4 as type, 4 as ord, 95 as reference_id, 838 as attribute_id, 1 as visible, null as precision, 
		30 as width, 1 as required, 1 as is_unique, null as sort_order,  null as format, 0 as read_only, null as max_length from dual
	) b
	on (a.id=b.id)
	when not matched then
insert (id, ref_book_id, name, alias, type, ord, reference_id, attribute_id, visible, precision, width, required, is_unique, sort_order, read_only, max_length)
values (b.id, b.ref_book_id, b.name, b.alias, b.type, b.ord, b.reference_id, b.attribute_id, b.visible, b.precision, b.width, b.required, b.is_unique, b.sort_order, b.read_only, b.max_length);


delete from ref_book_attribute where ref_book_id=30 and alias='REGION_ID';

update REF_BOOK_ATTRIBUTE set ord=7 where id=870;
update REF_BOOK_ATTRIBUTE set ord=8 where id=167;
update REF_BOOK_ATTRIBUTE set ord=9 where id=168;

update ref_book set read_only=1 where id=30;