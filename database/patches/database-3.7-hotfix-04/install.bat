@echo off
CALL settings.bat

echo on

IF NOT EXIST _log MKDIR _log
IF NOT EXIST _bad MKDIR _bad
DEL /s /q /f _log\*.*
DEL /s /q /f _bad\*.*

SET nls_lang=AMERICAN_AMERICA.AL32UTF8

"%ORA_BIN%\sqlplus" %DBA_AUTH% @"tech/create_version_history.sql" %NDFL_USR% _log/create_version_history.log


"%ORA_BIN%\sqlplus" %DBA_AUTH% @"patch_03_007_hotfix_04.sql" %NDFL_USR% _log/patch_03_007_hotfix_04.log %ORA_BIN% %DBA_AUTH% 


"%ORA_BIN%\sqlplus" %DBA_AUTH% @"tech/result_log.sql" %NDFL_USR% _log/result_log.log
pause
