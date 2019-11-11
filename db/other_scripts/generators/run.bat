@ECHO OFF
REM ����� ��������� ��� � ������ ��������� ����� � ����� ���� ������
REM ������: user_name/password@host:port/service_name
SET AUTH=NDFL_UNSTABLE/ndfl_unstable@172.19.214.45:1521/orcl.aplana.local
REM ����� ��������� ���� � ����� ORACLE_HOME\BIN
SET ORA_BIN=C:\app\southstanly\product\12.1.0\client_1\BIN
SET nls_lang=AMERICAN_AMERICA.AL32UTF8

ECHO ## DB: %AUTH%

ECHO ## department_details
"%ORA_BIN%\sqlplus" %AUTH% @"department_details.sql"

PAUSE