@ECHO ON
REM ����� ��������� ��� � ������ ��������� ����� � ����� ���� ������
SET NDFL_NAME=NDFL_DEV2
SET NDFL_PASS=ndfl_dev2
SET NDFL_SOURCE=NDFL_UNSTABLE
SET TAX_NAME=TAX_DEV2
SET TAX_SOURCE=TAX_1_6
SET SYSTEM_AUTH=SYSTEM/oracle@NALOG-DB.APLANA.LOCAL:1521/orcl.aplana.local
REM ������: user_name/password@host:port/service_name
SET AUTH=%NDFL_NAME%/%NDFL_PASS%@NALOG-DB.APLANA.LOCAL:1521/orcl.aplana.local
REM ����� ��������� ���� � ����� ORACLE_HOME\BIN
SET ORA_BIN=C:\app\oracle\product\11.2.0\dbhome_1\BIN
SET LOG_DIR=_logs
SET BAD_DIR=_bad
SET nls_lang=AMERICAN_AMERICA.AL32UTF8


MKDIR %LOG_DIR%
MKDIR %BAD_DIR%

DEL /s /q /f %LOG_DIR%\*.txt
DEL /s /q /f %BAD_DIR%\*.*

ECHO ## create_scheme
"%ORA_BIN%\sqlplus" %SYSTEM_AUTH% @"create_scheme.sql" > "%LOG_DIR%/create_scheme.txt" %NDFL_NAME% %NDFL_PASS% %TAX_NAME%

ECHO ## grant_quota
"%ORA_BIN%\sqlplus" %SYSTEM_AUTH% @"grant_quota.sql" > "%LOG_DIR%/grant_quota.txt" %NDFL_NAME% %TAX_NAME%

ECHO ## expdp_tax_source
%ORA_BIN%\expdp.exe system/oracle directory=F_NDFL_DUMPS schemas=%TAX_SOURCE% dumpfile=DMP_TAX_SOURCE.dmp logfile=EXP_TAX_SOURCE.log CONTENT=metadata_only

ECHO ## impdp_tax
%ORA_BIN%\impdp.exe system/oracle directory=F_NDFL_DUMPS schemas=%TAX_SOURCE% exclude=STATISTICS dumpfile=DMP_TAX_SOURCE.dmp logfile=IMP_TAX_SOURCE.log remap_schema=%TAX_SOURCE%:%TAX_NAME%

ECHO ## grants
"%ORA_BIN%\sqlplus" %SYSTEM_AUTH% @"grants.sql" > "%LOG_DIR%/grants.txt" %NDFL_NAME% %TAX_NAME% %TAX_SOURCE%

ECHO ## expdp_ndfl_source
%ORA_BIN%\expdp.exe system/oracle directory=F_NDFL_DUMPS schemas=%NDFL_SOURCE% dumpfile=DMP_NDFL_SOURCE.dmp logfile=EXP_NDFL_SOURCE.log

ECHO ## impdp_ndfl
%ORA_BIN%\impdp.exe system/oracle directory=F_NDFL_DUMPS schemas=%NDFL_SOURCE% exclude=STATISTICS dumpfile=DMP_NDFL_SOURCE.dmp logfile=IMP_NDFL_SOURCE.log remap_schema=%NDFL_SOURCE%:%NDFL_NAME%

ECHO ## tax_data
"%ORA_BIN%\sqlplus" %SYSTEM_AUTH% @"tax_data.sql" > "%LOG_DIR%/tax_data.txt" %TAX_NAME% %TAX_SOURCE%

ECHO ## ndfl_synonyms
"%ORA_BIN%\sqlplus" %AUTH% @"ndfl_synonyms.sql" > "%LOG_DIR%/ndfl_synonyms.txt" %TAX_NAME%

PAUSE