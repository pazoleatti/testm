load data
characterset UTF8
infile 'ref_book_oktmo_st2.dsv'
append
into table ref_book_oktmo
fields terminated by ','
optionally enclosed by '"'
trailing nullcols
( 
  id		"seq_ref_book_record.nextval",
  record_id	,
  status,
  version	date "dd.mm.yyyy",
  code,
  name		char(4000),
  razd
)