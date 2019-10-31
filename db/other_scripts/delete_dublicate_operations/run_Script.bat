@echo off
SET NLS_LANG=AMERICAN_AMERICA.AL32UTF8

cls
@echo ..start export..

set /p v_user="user (NDFL):"
set /p v_pwd="password (example: NDFL):"
set /p v_connect="connect (example: TAXDB):"
set /p v_form_number="Primary Form number (example: 97480):"




@sqlplus.exe %v_user%/%v_pwd%@%v_connect% @remove_dublicate_operations.sql %v_form_number% delete_dublicate_operations_%v_user%_%date%_%v_form_number%_%random%.txt



echo ..end..
pause
