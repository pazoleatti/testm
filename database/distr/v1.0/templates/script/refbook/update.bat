SET PREFIX=refbook
SET SOURCE_DIR=../../data/%PREFIX%
SET BASE_DIR=../../script/%PREFIX%
SET LOG_DIR_REL=../../../%LOG_DIR%
SET BAD_DIR_REL=../../../%BAD_DIR%
ECHO -- clean
"%ORA_BIN%\sqlplus" %AUTH% @"clean.sql" > "%LOG_DIR_REL%/%PREFIX%_1_clean.txt"
CD %SOURCE_DIR%
ECHO -- blob
"%ORA_BIN%\sqlldr" userid=%AUTH% control="%BASE_DIR%/blob_data.ldr" log="%LOG_DIR_REL%/%PREFIX%_2_blob_data.txt" bad="%BAD_DIR_REL%/%PREFIX%_blob_data.txt"
CD %BASE_DIR%
ECHO -- script
"%ORA_BIN%\sqlplus" %AUTH% @"ref_book_script.sql" > "%LOG_DIR_REL%/%PREFIX%_3_ref_book_script.txt"