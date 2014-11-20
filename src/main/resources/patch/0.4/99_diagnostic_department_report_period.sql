--Диагностический запрос на корректность данных в таблице department_report_period

-- Прибыль, НДС: все ли подразделения на одну дату корректировки закрыли периоды
select * from
(
select report_period_id, correction_date, tax_type, is_active
from department_report_period drp
join report_period rp on rp.id = drp.report_period_id
join tax_period tp on tp.id = rp.tax_period_id
where tp.tax_type in ( 'I', 'V')
)
pivot
(
count(*)
for is_active in (0 as Closed_period, 1 as Open_period)
)
where correction_date is not null and (closed_period>0 and open_period>0);

--Если есть период ввода остатков, то других периодов быть не может
select department_id,
       d.full_name,
     report_period_id,
	   rp.full_name
from
(select department_id,
         report_period_id,
         count(distinct is_balance_period)
from department_report_period
group by department_id,
           report_period_id having count(distinct is_balance_period) > 1) t
join (
	select id,
		   sys_connect_by_path(name, '|') as full_name
	from department
	start with parent_id = 0
	connect by parent_id = prior id
	) d on d.id = t.department_id
join 
	(
	select rp.id, name ||', ' || tp.year || ' ('||tp.tax_type||')' as full_name
							   from report_period rp
							   join tax_period tp on tp.id = rp.tax_period_id) rp on rp.id = t.report_period_id;


--Корректирующему периоду не может быть присвоен признак ввода остатков
select department_id,
       d.full_name,
     report_period_id,
     rp.full_report_name
from department_report_period t
join (
  select id,
       substr(sys_connect_by_path(name, '/'), 2) as full_name
  from department
  start with parent_id = 0
  connect by parent_id = prior id
  ) d on d.id = t.department_id
join 
  (
  select rp.id, name ||', ' || tp.year || ' ('||tp.tax_type||')' as full_report_name
                 from report_period rp
                 join tax_period tp on tp.id = rp.tax_period_id) rp on rp.id = t.report_period_id
where correction_date is not null and is_balance_period = 1                 
  order by 1, 3;  

--Если для периода есть корректирующий период, то ему не может быть присвоен признак ввода остатков
select 
department_id,
d.full_name,
report_period_id,
rp.full_report_name 
from department_report_period drp 
join (
  select id,
       substr(sys_connect_by_path(name, '/'), 2) as full_name
  from department
  start with parent_id = 0
  connect by parent_id = prior id
  ) d on d.id = drp.department_id
join 
  (
  select rp.id, name ||', ' || tp.year || ' ('||tp.tax_type||')' as full_report_name
                 from report_period rp
                 join tax_period tp on tp.id = rp.tax_period_id) rp on rp.id = drp.report_period_id
where is_balance_period = 1 and exists (select * from department_report_period t where t.id <> drp.id and t.department_id = drp.department_id and t.report_period_id = drp.report_period_id and t.correction_date is not null)
order by 1, 3; 

--Два открытых периода на связку department_id, report_period_id (SBRFACCTAX-9467)
select department_id,
       d.full_name,
     report_period_id,
     rp.full_report_name
from
(select department_id,
         report_period_id,
         count(id)
from department_report_period
where is_active=1
group by department_id,
           report_period_id having count(id) > 1) t
join (
  select id,
       substr(sys_connect_by_path(name, '/'), 2) as full_name
  from department
  start with parent_id = 0
  connect by parent_id = prior id
  ) d on d.id = t.department_id
join 
  (
  select rp.id, name ||', ' || tp.year || ' ('||tp.tax_type||')' as full_report_name
                 from report_period rp
                 join tax_period tp on tp.id = rp.tax_period_id) rp on rp.id = t.report_period_id
  order by 1, 3; 
  
--уникальность даты корректировки
select department_id,
       d.full_name,
     report_period_id,
     rp.full_report_name, 
     CNT_NOT_UNIQUE_PERIODS
from
(select department_id,
         report_period_id,
         trunc(correction_date),
         count(id) as CNT_NOT_UNIQUE_PERIODS
from department_report_period
group by department_id,
           report_period_id,
           trunc(correction_date)  having count(id) > 1) t
join (
  select id,
       substr(sys_connect_by_path(name, '/'), 2) as full_name
  from department
  start with parent_id = 0
  connect by parent_id = prior id
  ) d on d.id = t.department_id
join 
  (
  select rp.id, name ||', ' || tp.year || ' ('||tp.tax_type||')' as full_report_name
                 from report_period rp
                 join tax_period tp on tp.id = rp.tax_period_id) rp on rp.id = t.report_period_id
  order by 1, 3;   
  
--если для открытого периода есть корректирующий период, то он не может быть открыт  
select department_id,
       d.full_name,
     report_period_id,
     rp.full_report_name
from department_report_period t
join (
  select id,
       substr(sys_connect_by_path(name, '/'), 2) as full_name
  from department
  start with parent_id = 0
  connect by parent_id = prior id
  ) d on d.id = t.department_id
join 
  (
  select rp.id, name ||', ' || tp.year || ' ('||tp.tax_type||')' as full_report_name
                 from report_period rp
                 join tax_period tp on tp.id = rp.tax_period_id) rp on rp.id = t.report_period_id
where correction_date is null and is_active=1 and  exists (select 1 from department_report_period a where a.department_id = t.department_id and a.report_period_id = t.report_period_id and a.id <> t.id and a.correction_date is not null)                 
  order by 1, 3;    
  
-- Открытым может быть только один период, при этом он последний из корректирующих
select department_id,
       d.full_name,
     report_period_id,
     rp.full_report_name
from department_report_period t
join (
  select id,
       substr(sys_connect_by_path(name, '/'), 2) as full_name
  from department
  start with parent_id = 0
  connect by parent_id = prior id
  ) d on d.id = t.department_id
join 
  (
  select rp.id, name ||', ' || tp.year || ' ('||tp.tax_type||')' as full_report_name
                 from report_period rp
                 join tax_period tp on tp.id = rp.tax_period_id) rp on rp.id = t.report_period_id
where correction_date is not null and is_active=1 and  exists (select 1 from department_report_period a where a.department_id = t.department_id and a.report_period_id = t.report_period_id and a.id <> t.id and (a.is_active = 1 or a.correction_date > t.correction_date))                 
  order by 1, 3;    
  