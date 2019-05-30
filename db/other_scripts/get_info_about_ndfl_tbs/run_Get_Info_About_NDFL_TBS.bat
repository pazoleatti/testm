@echo off
SET NLS_LANG=AMERICAN_AMERICA.AL32UTF8    
cls
@echo ..start..

set /p v_user="SEMIDBA User (example: system):"
set /p v_pwd="SEMIDBA Password: "
set /p v_ndfl="NDFL User (example: NDFL):"
set /p v_connect="connect (example: TAXDB):"


@sqlplus.exe %v_user%/%v_pwd%@%v_connect% @Script_Get_Info_About_NDFL_TBS_20190529.sql %v_user% %v_ndfl% 

echo ..end..
