-- удаление ненужных индексов
drop index idx_f_fias_addrobj_formalname;
drop index idx_fias_addrobj_aoid;
drop index idx_fias_addrobj_parentguid;
drop index idx_fias_addrobj_region_status;
drop index idx_fias_addrobj_reg_lstat;
drop index srch_fias_addrobj_csregfn;
drop index srch_fias_addrobj_postcode;
drop index srch_fias_addrobj_regfnls;

-- создание индекса для обновления мат. представлений
create index idx_fias_addr_currst_aolev on fias_addrobj(currstatus,aolevel,replace(lower(formalname),' ',''));
