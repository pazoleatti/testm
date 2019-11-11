@echo off
SET NLS_LANG=AMERICAN_AMERICA.AL32UTF8

cls
@echo ..start export..

set /p v_user="user (NDFL):"
set /p v_pwd="password (example: NDFL):"
set /p v_connect="connect (example: TAXDB):"


@sqlplus.exe %v_user%/%v_pwd%@%v_connect% @shrink_persons_forms.sql shrink_%v_user%_%date%_%random%.txt

echo ..end..
