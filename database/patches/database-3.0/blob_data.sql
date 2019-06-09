update blob_data set name='РНУ_НДФЛ_new6.4.xsd' where id in (select xsd from declaration_template where id=100);
update blob_data set name='NO_NDFL2_1_399_00_05_04_02.xsd' where id in (select xsd from declaration_template where id in (102,104));
update blob_data set name='NO_NDFL6_1_152_00_05_02_02.xsd' where id in (select xsd from declaration_template where id=103);
