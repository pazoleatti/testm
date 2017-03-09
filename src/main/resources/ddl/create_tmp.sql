create global temporary table tmp_version 
(	
	version date, 
	record_id number(18,0) not null enable, 
	calc_date date
);

create index idx_tmp_version_calc_ver_rec on tmp_version(calc_date,version,record_id);