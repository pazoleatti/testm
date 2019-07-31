set feedback off;
set verify off;
set serveroutput on;
set linesize 4000;
spool EXPORT.txt;

declare
form_num varchar (4000 char);

begin
form_num := '&2';	 

dbms_output.put_line ('№ п/п|ИНП|ID операции|Код дохода|Признак дохода|Дата начисления дохода|Дата выплаты дохода|'||
'КПП|ОКТМО|Сумма начисленного дохода|Сумма выплаченного дохода|Сумма вычета|Налоговая база|Процентная ставка, %'||
'|Дата НДФЛ|НДФЛ исчисленный|НДФЛ удержанный|НДФЛ не удержанный|НДФЛ излишне удержанный|НДФЛ возвращённый НП'||
'|Срок перечисления в бюджет|Дата платёжного поручения|Номер платёжного поручения|Сумма платёжного поручения'||
'|Идентификатор строки|Дата и время редактирования|Обновил|АСНУ');

for res in (select row_number() over (order by npi.row_num) as num, 
 replace(np.inp,'|','/')||'|'|| replace(npi.operation_id,'|','/')||'|'||replace( npi.income_code,'|','/')||'|'|| 
replace(npi.income_type,'|','/')||'|'|| to_char(npi.income_accrued_date,'dd.mm.yyyy')||'|'||to_char(npi.income_payout_date,'dd.mm.yyyy')||'|'|| 
replace(npi.kpp,'|','/')||'|'|| replace(npi.oktmo,'|','/')||'|'||  
(npi.income_accrued_summ)||'|'|| (npi.income_payout_summ)||'|'|| (npi.total_deductions_summ)||'|'|| (npi.tax_base)||'|'|| (npi.tax_rate)||'|'|| 
to_char(npi.tax_date,'dd.mm.yyyy')||'|'||
(npi.calculated_tax)||'|'|| (npi.withholding_tax)||'|'|| (npi.not_holding_tax)||'|'|| (npi.overholding_tax)||'|'||(npi.refound_tax)||'|'||
to_char(npi.tax_transfer_date,'dd.mm.yyyy')||'|'|| to_char(npi.payment_date,'dd.mm.yyyy')||'|'|| 
replace(npi.payment_number,'|','/')||'|'|| (npi.tax_summ)||'|'||
(npi.id)||'|'|| to_char(npi.modified_date,'dd.mm.yyyy hh24:mi:ss')||'|'|| replace(npi.modified_by,'|','/')||'|'|| (rba.name) as vl
from NDFL_PERSON np  inner join NDFL_PERSON_INCOME npi on npi.ndfl_person_id = np.id 
left join ref_book_asnu rba on npi.asnu_id = rba.id 
where np.declaration_data_id = to_number(form_num)  and 
(npi.tax_transfer_date is not null and  npi.payment_date is not null and  npi.payment_number is not null and  npi.tax_summ is not null)
and not exists (select * from ndfl_person np1 join ndfl_person_income npi1 on npi1.ndfl_person_id=np1.id where np1.declaration_data_id=to_number(form_num) and 
npi1.operation_id=npi.operation_id and npi1.asnu_id=npi.asnu_id and npi1.id<>npi.id 
and not (npi1.tax_transfer_date is not null and  npi1.payment_date is not null and  npi1.payment_number is not null and  npi1.tax_summ is not null))
order by npi.row_num asc)
loop

dbms_output.put_line (res.num||'|'||res.vl );
end loop;
exception
  when INVALID_NUMBER then dbms_output.put_line ('Ошибка! Некорректный номер формы:' || form_num);
  when others then raise;
end;
/

exit;

