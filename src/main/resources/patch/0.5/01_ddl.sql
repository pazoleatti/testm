--http://jira.aplana.com/browse/SBRFACCTAX-10007: Обновление данных в таблице DATA_CELL
update data_cell set dvalue = to_date('01.01.1900', 'DD.MM.YYYY') where dvalue < to_date('01.01.1900', 'DD.MM.YYYY');
alter table data_cell add constraint data_cell_chk_min_dvalue check (dvalue >= to_date('01.01.1900', 'DD.MM.YYYY'));


--http://jira.aplana.com/browse/SBRFACCTAX-10063: Увеличение размерности поля
ALTER TABLE log_system MODIFY declaration_type_name VARCHAR2(1000);

