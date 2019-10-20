@echo off
REM схема пользователя
SET NDFL_USR=KONONOVA
REM схема пользователя
SET NDFL_PASS=KONONOVA
REM имя схемы данных нормативно-справочной информации (НСИ)
SET NSI_USR=TAXNSI_NEXT2
REM имя схемы данных подсистемы Налоговая Выверка (TAXREC)
SET TAXREC_USR=TAXREC_NEXT2
REM база данных
SET DBNAME=taxdb
REM строка соединения с базой данных
SET DBA_AUTH=%NDFL_USR%/%NDFL_PASS%@%DBNAME%
REM путь к директории, где располагается исполняемый файл "sqlplus"
SET ORA_BIN=c:\app\oracle\product\11.2.0\client_2\BIN

