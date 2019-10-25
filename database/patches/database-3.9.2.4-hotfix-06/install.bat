@echo off
CALL settings.bat

echo on

IF NOT EXIST _log MKDIR _log
IF NOT EXIST _bad MKDIR _bad
IF EXIST _log\*.* DEL /s /q /f _log\*.*
IF EXIST _bad\*.* DEL /s /q /f _bad\*.*

SET nls_lang=AMERICAN_AMERICA.AL32UTF8

rem "%ORA_BIN%\sqlplus" %DBA_AUTH% @"tech/create_version_history.sql" %NDFL_USR% _log/create_version_history.log



"%ORA_BIN%\sqlplus" %DBA_AUTH% @"patch_03_009_02_04_hotfix_03.sql" %NDFL_USR% _log/patch_03_009_02_04_hotfix_03.log %ORA_BIN% %DBA_AUTH% 

rem "%ORA_BIN%\sqlplus" %DBA_AUTH% @"tech/result_log.sql" %NDFL_USR% _log/result_log.log
pause
