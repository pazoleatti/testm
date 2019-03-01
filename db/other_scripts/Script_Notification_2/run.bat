@ECHO OFF
REM ����� ��������� ��� � ������ ��������� ����� � ����� ���� ������
REM USER test
SET USER_ID=14450
REM USER Анисимова
REM SET USER_ID=13763
REM USER Гнездилова 
REM SET USER_ID=44389
REM Begin Date
SET BEGIN_DATE=01.02.2019
REM End Date
SET END_DATE=10.02.2019
REM ������: user_name/password@host:port/service_name
SET AUTH=NDFL_UNSTABLE/NDFL_UNSTABLE@172.19.214.45:1521/orcl.aplana.local
REM ����� ��������� ���� � ����� ORACLE_HOME\BIN
SET ORA_BIN=C:\app\client\ghy\product\12.2.0\client_1\bin
SET LOG_DIR=_logs
SET FILE_NAME=NOTIFICATION_201903-1140.csv
SET nls_lang=AMERICAN_AMERICA.AL32UTF8

ECHO ## DB: %AUTH%

SET MODE=1
MKDIR %LOG_DIR%

DEL /s /q /f %LOG_DIR%\*.csv

ECHO ## Script_Notification_2
 "%ORA_BIN%\sqlplus" %AUTH% @"Script_Notification_2.sql" %USER_ID% %BEGIN_DATE% %END_DATE% %LOG_DIR%/%FILE_NAME%

rem �������� ������ ���������� �������������������
PAUSE