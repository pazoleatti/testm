@echo off
CALL settings.bat

echo on

IF NOT EXIST _log MKDIR _log
IF NOT EXIST _bad MKDIR _bad
IF EXIST _log\*.* DEL /s /q /f _log\*.*
IF EXIST _bad\*.* DEL /s /q /f _bad\*.*

SET nls_lang=AMERICAN_AMERICA.AL32UTF8

"%ORA_BIN%\sqlplus" %DBA_AUTH% @"tech/create_version_history.sql" %NDFL_USR% _log/create_version_history.log

cd PATCHES

"%ORA_BIN%\sqlplus" %DBA_AUTH% @"database-3.7/patch_03_007_00.sql" %NDFL_USR% ../_log/patch_03_007_00.log %NSI_USR% %ORA_BIN% %DBA_AUTH% 

"%ORA_BIN%\sqlplus" %DBA_AUTH% @"database-3.7.1/patch_03_007_01.sql" %NDFL_USR% ../_log/patch_03_007_01.log %NSI_USR% %ORA_BIN% %DBA_AUTH% 

"%ORA_BIN%\sqlplus" %DBA_AUTH% @"database-3.8/patch_03_008_01.sql" %NDFL_USR% ../_log/patch_03_008_01.log %NSI_USR% %ORA_BIN% %DBA_AUTH% %TAXREC_USR%

"%ORA_BIN%\sqlplus" %DBA_AUTH% @"database-3.9/patch_03_009_00.sql" %NDFL_USR% ../_log/patch_03_009_00.log %NSI_USR% %ORA_BIN% %DBA_AUTH% %TAXREC_USR%

"%ORA_BIN%\sqlplus" %DBA_AUTH% @"database-3.9.0.1/patch_03_009_00_1.sql" %NDFL_USR% ../_log/patch_03_009_00_1.log %NSI_USR% %ORA_BIN% %DBA_AUTH% %TAXREC_USR%

"%ORA_BIN%\sqlplus" %DBA_AUTH% @"database-3.9.1/patch_03_009_01.sql" %NDFL_USR% ../_log/patch_03_009_01.log %NSI_USR% %ORA_BIN% %DBA_AUTH% %TAXREC_USR%

"%ORA_BIN%\sqlplus" %DBA_AUTH% @"database-3.9.2/patch_03_009_02.sql" %NDFL_USR% ../_log/patch_03_009_02.log %NSI_USR% %ORA_BIN% %DBA_AUTH% %TAXREC_USR%

"%ORA_BIN%\sqlplus" %DBA_AUTH% @"database-3.9.2.3/patch_03_009_02_03.sql" %NDFL_USR% ../_log/patch_03_009_02_03.log %NSI_USR% %ORA_BIN% %DBA_AUTH% %TAXREC_USR%

"%ORA_BIN%\sqlplus" %DBA_AUTH% @"database-3.9.2.4/patch_03_009_02_04.sql" %NDFL_USR% ../_log/patch_03_009_02_04.log %NSI_USR% %ORA_BIN% %DBA_AUTH% %TAXREC_USR%

"%ORA_BIN%\sqlplus" %DBA_AUTH% @"database-3.10/patch_03_010_00.sql" %NDFL_USR% ../_log/patch_03_010_00.log %NSI_USR% %ORA_BIN% %DBA_AUTH% %TAXREC_USR%

"%ORA_BIN%\sqlplus" %DBA_AUTH% @"database-3.10.0.1/patch_03_010_00_01.sql" %NDFL_USR% ../_log/patch_03_010_00_01.log %NSI_USR% %ORA_BIN% %DBA_AUTH% %TAXREC_USR%

cd ..
@echo "Gather statistics"
"%ORA_BIN%\sqlplus" %DBA_AUTH% @"tech/gather_statistics.sql" %NDFL_USR% _log/stats.log

"%ORA_BIN%\sqlplus" %DBA_AUTH% @"tech/result_log.sql" %NDFL_USR% _log/result_log.log

pause
