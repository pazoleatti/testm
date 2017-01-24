--TAX_TYPE
INSERT INTO tax_type (id, name) VALUES ('N', 'НДФЛ');
INSERT INTO tax_type (id, name) VALUES ('F', 'Сборы, фонды');

INSERT INTO tax_period(id, tax_type, year) VALUES (1, 'N', 2013);
INSERT INTO tax_period(id, tax_type, year) VALUES (10, 'N', 2014);
INSERT INTO tax_period(id, tax_type, year) VALUES (11, 'N', 2012);
INSERT INTO tax_period(id, tax_type, year) VALUES (12, 'F', 2013);