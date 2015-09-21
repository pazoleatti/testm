--http://jira.aplana.com/browse/SBRFACCTAX-12645: 0.7.1 Справочники "Классификатор доходов", "Классификатор расходов". Изменить название справочника и граф (ПАО "Сбербанк России" - > ПАО Сбербанк)
update ref_book_attribute set name = 'Учётное подразделение Центрального аппарата ПАО Сбербанк' where ref_book_id in (27, 28) and alias = 'UNIT';

update ref_book set name = 'Классификатор расходов ПАО Сбербанк для целей налогового учёта' where id = 27;
update ref_book set name = 'Классификатор доходов ПАО Сбербанк для целей налогового учёта' where id = 28;

COMMIT;
EXIT;