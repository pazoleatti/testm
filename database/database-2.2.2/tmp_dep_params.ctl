LOAD DATA CHARACTERSET UTF8
INFILE 'csv/ref_book_ndfl_detail.csv'
   INTO TABLE tmp_dep_params
   REPLACE
   FIELDS TERMINATED BY ';'
   TRAILING NULLCOLS 
(depcode,titname,kpp,oktmo,tax_end,tax_organ_code_mid,place,okved,region,phone,obligation,reorgcode,reorg_inn,reorg_kpp,sign,surname,name,lastname,docname,orgname,row_num)