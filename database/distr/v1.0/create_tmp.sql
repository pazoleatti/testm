create table tmp_version 
(	
	version date, 
	record_id number(18,0) not null enable, 
	calc_date date
);

create index idx_tmp_version_ver_rec on tmp_version (version, record_id); 
  