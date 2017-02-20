SET PREFIX=fond
SET SOURCE_DIR=../../data/%PREFIX%
SET BASE_DIR=../../template/%PREFIX%
ECHO -- clean
"%ORA_BIN%\sqlplus" %AUTH% @"clean.sql" > "../../%LOG_DIR%/%PREFIX%_1_clean.txt"
ECHO -- type
"%ORA_BIN%\sqlplus" %AUTH% @"type.sql" > "../../%LOG_DIR%/%PREFIX%_2_type.txt"
CD %SOURCE_DIR%
ECHO -- blob
"%ORA_BIN%\sqlldr" userid=%AUTH% control="%BASE_DIR%/blob_data.ldr" log="../../%LOG_DIR%/%PREFIX%_3_blob_data.txt" bad="../../%BAD_DIR%/%PREFIX%_blob_data.txt"
ECHO -- template
"%ORA_BIN%\sqlldr" userid=%AUTH% control="%BASE_DIR%/template.ldr" log="../../%LOG_DIR%/%PREFIX%_4_template.txt" bad="../../%BAD_DIR%/%PREFIX%_template.txt"
CD %BASE_DIR%
ECHO -- subreport
"%ORA_BIN%\sqlplus" %AUTH% @"subreport.sql" > "../../%LOG_DIR%/%PREFIX%_5_subreport.txt"