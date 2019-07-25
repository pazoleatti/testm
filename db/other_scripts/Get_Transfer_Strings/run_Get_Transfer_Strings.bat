@echo off
SET NLS_LANG=RUSSIAN_CIS.CL8MSWIN1251    
cls
@echo ..start export..

set /p v_user="user (NDFL):"
set /p v_pwd="password (example: NDFL):"
set /p v_connect="connect (example: TAXDB):"
set /p v_num="Form number (example: 65506):"
set hour=%time:~0,2%
if "%hour:~0,1%" == " " set hour=0%hour:~1,1%
set min=%time:~3,2%
if "%min:~0,1%" == " " set min=0%min:~1,1%
set postfix=_%DATE%_%hour%_%min%

@sqlplus.exe %v_user%/%v_pwd%@%v_connect% @Get_Transfer_Strings.sql %v_user% %v_num% 
@rename "EXPORT.txt" "%v_num%_Операции перечисления%postfix%.csv"

echo ..end..
