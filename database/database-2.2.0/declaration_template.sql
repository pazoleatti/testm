DECLARE
	v_id number;
BEGIN
	update declaration_template set name='РНУ НДФЛ (первичная)' where id=100;
	update declaration_template set name='РНУ НДФЛ (консолидированная)' where id=101;
END;
/