@ECHO OFF
REM ????? ????????? ??? ? ?????? ????????? ????? ? ????? ???? ??????
REM ??????: user_name/password@host:port/service_name
SET AUTH=ndfl_next2/ndfl_next2@172.19.214.46:1521/orcl.aplana.local
REM ????? ????????? ???? ? ????? ORACLE_HOME\BIN
SET ORA_BIN=C:\app\oracle\product\11.2.0\dbhome_1\BIN
SET LOG_DIR=_logs
SET BAD_DIR=_bad
SET nls_lang=AMERICAN_AMERICA.AL32UTF8
REM ????? ? ??? ???????????? ? ??????? ??????????????
SET ADM_LOGIN=ADMIN_NDFL
SET ADM_NAME=ADMIN_NDFL

ECHO ## DB: %AUTH%

SET MODE=1
MKDIR %LOG_DIR%
MKDIR %BAD_DIR%

DEL /s /q /f %LOG_DIR%\*.txt
DEL /s /q /f %BAD_DIR%\*.*

ECHO ## ddl: create_all_tables
"%ORA_BIN%\sqlplus" %AUTH% @"create_all_tables.sql" > "%LOG_DIR%/create_all_tables.txt"

ECHO ## dml: common
CD dml
CALL dml.bat
CD ..	

ECHO ## dml: refbook
CD refbook_tables
ECHO ## dml: oktmo
CD oktmo
call load_oktmo.bat
CD ..
ECHO ## dml: fill ref_book tables
CALL fill.bat
ECHO ## dml: ref_book_ndfl and ref_book_ndfl_detail
CD deppar
call fill.bat
CD ../..

ECHO ## ddl: create all constraint
"%ORA_BIN%\sqlplus" %AUTH% @"create_all_constraints.sql" > "%LOG_DIR%/create_all_constraints.txt"

ECHO ## ddl: create indexes
"%ORA_BIN%\sqlplus" %AUTH% @"create_indexes.sql" > "%LOG_DIR%/create_indexes.txt"


ECHO ## ddl: create triggers
"%ORA_BIN%\sqlplus" %AUTH% @"create_triggers.sql" > "%LOG_DIR%/create_triggers.txt"

ECHO ## ddl: create views
"%ORA_BIN%\sqlplus" %AUTH% @"create_views.sql" > "%LOG_DIR%/create_views.txt"

ECHO ## ddl: create materiazed views
"%ORA_BIN%\sqlplus" %AUTH% @"create_mat_views.sql" > "%LOG_DIR%/create_mat_views.txt"

ECHO ## ddl: create sources
"%ORA_BIN%\sqlplus" %AUTH% @"create_procedures.sql" > "%LOG_DIR%/create_procedures.txt"

ECHO ## dml: templates
CD templates/script
CALL templates.bat
CD ../..

rem ???????? ?????? ?????????? ???????????????????
PAUSE