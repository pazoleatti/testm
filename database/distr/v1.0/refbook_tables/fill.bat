ECHO ## dml: fill oktmo
CD oktmo
CALL load_oktmo.bat
CD ..

"%ORA_BIN%\sqlplus" %AUTH% @"fill.sql" > "../%LOG_DIR%/fill.txt"