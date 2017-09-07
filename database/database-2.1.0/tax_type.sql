delete from report_period where tax_period_id in (select id from tax_period where tax_type='F');
delete from tax_period where tax_type='F';
delete from tax_type where id='F';