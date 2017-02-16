SET PREFIX=refbook
"%ORA_BIN%\sqlplus" %AUTH% @"clean.sql" > "../%LOG_DIR%/%PREFIX%_1_clean.txt"
"%ORA_BIN%\sqlldr" userid=%AUTH% control="blob_data.ldr" log="../%LOG_DIR%/%PREFIX%_2_blob_data.txt" bad="../%BAD_DIR%/%PREFIX%_blob_data.txt"
"%ORA_BIN%\sqlplus" %AUTH% @"ref_book_script.sql" > "../%LOG_DIR%/%PREFIX%_3_ref_book_script.txt"