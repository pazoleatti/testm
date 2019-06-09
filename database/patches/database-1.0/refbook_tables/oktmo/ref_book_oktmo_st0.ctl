load data
characterset UTF8
infile 'ref_book_oktmo_st0.dsv'
truncate
into table ref_book_oktmo
fields terminated by ','
optionally enclosed by '"'
trailing nullcols
( 
  id		"seq_ref_book_record.nextval",
  record_id	"seq_ref_book_record_row_id.nextval",
  status,
  version	date "dd.mm.yyyy",
  code,
  name		char(4000),
  razd
)