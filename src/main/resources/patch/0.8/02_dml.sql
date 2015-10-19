update ref_book_attribute set is_unique = 1 where id in (3305,3304);

--http://jira.aplana.com/browse/SBRFACCTAX-12605: 0.8 Реализовать возможность создания форм типа "Расчетная"
INSERT INTO form_kind (id, name) VALUES (6, 'Расчетная');

--http://jira.aplana.com/browse/SBRFACCTAX-12866: справочники для табличных частей настроек  - неверсионными
update ref_book set IS_VERSIONED = 0 where id in (206, 310, 330);

COMMIT;
EXIT;