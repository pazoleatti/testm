@echo off
REM схема пользователя
SET NDFL_USR=NDFL
REM схема пользователя
SET NDFL_PASS=NDFL
REM имя схемы данных нормативно-справочной информации (НСИ)
SET NSI_USR=TAXNSI
REM имя схемы данных подсистемы Налоговая Выверка (TAXREC)
SET TAXREC_USR=TAXREC
REM база данных
SET DBNAME=taxdb
REM строка соединения с базой данных
SET DBA_AUTH=%NDFL_USR%/%NDFL_PASS%@%DBNAME%
REM путь к директории, где располагается исполняемый файл "sqlplus"
SET ORA_BIN=D:\app\oracle\product\11.2.0\dbhome_1\BIN

