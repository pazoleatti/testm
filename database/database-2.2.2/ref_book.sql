DECLARE
	v_id number;
BEGIN

	update ref_book set xsd_id='290d3b24-ba60-408a-b4a7-ec5c0d166efa' where id=904;
	
	update ref_book_attribute set max_length=500 where ref_book_id = 901 and alias in ('DISTRICT', 'CITY', 'LOCALITY', 'STREET');


END;
/