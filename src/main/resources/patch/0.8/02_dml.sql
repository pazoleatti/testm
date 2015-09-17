update ref_book_attribute set is_unique = 1 where id in (3305,3304);

--http://jira.aplana.com/browse/SBRFACCTAX-12605: 0.8 Реализовать возможность создания форм типа "Расчетная"
INSERT INTO form_kind (id, name) VALUES (6, 'Расчетная');

COMMIT;
EXIT;