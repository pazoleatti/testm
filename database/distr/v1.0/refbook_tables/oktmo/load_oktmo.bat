ECHO ## dml: load ref_book_oktmo
"%ORA_BIN%\sqlldr" %AUTH% "control=ref_book_oktmo_st0.ctl silent=all log="../%LOG_DIR%/ref_book_oktmo_st0.txt" bad="../%BAD_DIR%/ref_book_oktmo_st0.bad"
"%ORA_BIN%\sqlldr" %AUTH% "control=ref_book_oktmo_st2.ctl silent=all log="../%LOG_DIR%/ref_book_oktmo_st2.txt" bad="../%BAD_DIR%/ref_book_oktmo_st2.bad"

ECHO ## dml: merge record_id in ref_book_oktmo
"%ORA_BIN%\sqlplus" %AUTH% @"merge_oktmo_record_id.sql"> "../%LOG_DIR%/merge_oktmo_record_id.txt"
