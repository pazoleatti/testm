@ECHO OFF
SET NDFL_USER=ndfl_1_0
SET NDFL_PASS=ndfl_1_0
SET NDFL_DBNAME=apldb
SET AUTH=%NDFL_USER%/%NDFL_PASS%@%NDFL_DBNAME%
ECHO ## DB: %AUTH%

SET ORA_BIN=C:\Oracle\product\11.2.0\dbhome_2\BIN
SET LOG_DIR=logs
SET nls_lang=AMERICAN_AMERICA.AL32UTF8

ECHO ## clean
DEL /s /q /f *.bad
DEL /s /q /f *.log

ECHO ## dml: load ref_book_oktmo
"%ORA_BIN%\sqlldr" %AUTH% "control=ref_book_oktmo_st0.ctl data=ref_book_oktmo_st0.dsv log=../%LOG_DIR%/ref_book_oktmo_st0.log"
"%ORA_BIN%\sqlldr" %AUTH% "control=ref_book_oktmo_st2.ctl data=ref_book_oktmo_st2.dsv"

ECHO ## dml: merge record_id in ref_book_oktmo
"%ORA_BIN%\sqlplus" %AUTH% @"merge_oktmo_record_id.sql"> "../%LOG_DIR%/merge_oktmo_record_id.log"

PAUSE