SET PREFIX=ndfl
SET SOURCE_DIR=../../data/%PREFIX%
SET BASE_DIR=../../script/%PREFIX%
SET LOG_DIR_REL=../../../%LOG_DIR%
SET BAD_DIR_REL=../../../%BAD_DIR%
ECHO -- clean
"%ORA_BIN%\sqlplus" %AUTH% @"clean.sql" > "%LOG_DIR_REL%/%PREFIX%_1_clean.txt"
CD %SOURCE_DIR%
ECHO -- template
"%ORA_BIN%\sqlldr" userid=%AUTH% control="%BASE_DIR%/template.ldr" log="%LOG_DIR_REL%/%PREFIX%_2_template.txt" bad="%BAD_DIR_REL%/%PREFIX%_template.txt"
ECHO -- template_script
"%ORA_BIN%\sqlldr" userid=%AUTH% control="%BASE_DIR%/template_script.ldr" log="%LOG_DIR_REL%/%PREFIX%_3_template_script.txt" bad="%BAD_DIR_REL%/%PREFIX%_template_script.txt"
CD %BASE_DIR%