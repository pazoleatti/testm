@echo off
SET NLS_LANG=AMERICAN_AMERICA.AL32UTF8

cls
@echo ..start checking and creating constraints..

set /p v_user="user (NDFL):"
set /p v_pwd="password (example: NDFL):"
set /p v_connect="connect (example NDFLDB):"


@sqlplus.exe %v_user%/%v_pwd%@%v_connect% @Script2.2.2_constraints_20180614.sql

echo ..end checking and creating constraints..
pause