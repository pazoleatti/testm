@echo off
SET NLS_LANG=AMERICAN_AMERICA.AL32UTF8

cls
@echo ..start checking..

set /p v_user="user (NDFL):"
set /p v_pwd="password (example: NDFL):"
set /p v_connect="connect (example NDFLDB):"


@sqlplus.exe %v_user%/%v_pwd%@%v_connect% @checks.sql

echo ..end checking..
pause