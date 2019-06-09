LOAD DATA CHARACTERSET UTF8
INFILE 'csv/ref_book_ndfl_detail_v12_utf8.csv'
   INTO TABLE tmp_dep_params
   REPLACE
   FIELDS TERMINATED BY ';'
   TRAILING NULLCOLS 
(row_num,titname,kpp,oktmo,tax_end,start_date,end_date,place,phone,sign,surname,name,lastname,docname)