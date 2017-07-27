SET PREFIX=ndfl
SET SOURCE_DIR=../../data/%PREFIX%
SET BASE_DIR=../../script/%PREFIX%
SET LOG_DIR_REL=../../../%LOG_DIR%
SET BAD_DIR_REL=../../../%BAD_DIR%

REM ECHO -- clean
REM "%ORA_BIN%\sqlplus" %AUTH% @"clean.sql" > "%LOG_DIR_REL%/%PREFIX%_1_clean.txt"
REM ECHO -- type
REM "%ORA_BIN%\sqlplus" %AUTH% @"type.sql" > "%LOG_DIR_REL%/%PREFIX%_2_type.txt"

CD %SOURCE_DIR%
REM ECHO -- blob
REM "%ORA_BIN%\sqlldr" userid=%AUTH% control="%BASE_DIR%/blob_data.ldr" log="%LOG_DIR_REL%/%PREFIX%_3_blob_data.txt" bad="%BAD_DIR_REL%/%PREFIX%_blob_data.txt"
ECHO -- template
"%ORA_BIN%\sqlldr" userid=%AUTH% control="%BASE_DIR%/template.ldr" log="%LOG_DIR_REL%/%PREFIX%_4_template.txt" bad="%BAD_DIR_REL%/%PREFIX%_template.txt"

REM CD %BASE_DIR%
REM ECHO -- template_file
REM "%ORA_BIN%\sqlplus" %AUTH% @"template_file.sql" > "%LOG_DIR_REL%/%PREFIX%_5_template_file.txt"
REM ECHO -- subreport
REM "%ORA_BIN%\sqlplus" %AUTH% @"subreport.sql" > "%LOG_DIR_REL%/%PREFIX%_6_subreport.txt"