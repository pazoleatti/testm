"%ORA_BIN%\sqlplus" %AUTH% @"before_update_template.sql" > "../%LOG_DIR%/before_update_template.txt"

ECHO #################################
ECHO ## template: refbook
ECHO #################################
CD refbook
CALL update.bat
CD ..
ECHO #################################
ECHO ## template: fond
ECHO #################################
CD fond
CALL update.bat
CD ..
ECHO #################################
ECHO ## template: ndfl
ECHO #################################
CD ndfl
CALL update.bat
CD ..

"%ORA_BIN%\sqlplus" %AUTH% @"after_update_template.sql" > "../%LOG_DIR%/after_update_template.txt"