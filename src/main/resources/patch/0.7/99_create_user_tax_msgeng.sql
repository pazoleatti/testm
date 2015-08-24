-- http://jira.aplana.com/browse/SBRFACCTAX-12268: Шаблон скрипта для создания схем-хранилищ JMS-сообщений

--Скрипт для запуска в SQLPlus (Запускать от пользователя с привилегиями DBA, например, SYSTEM.)
--Используемые параметры:
--&sibusername - Имя пользователя, который будет владельцем объектов SIB%
--&sibuserpassword - Пароль пользователя &sibusername.
--&taxusername - Имя супер-пользователя (ожидается TAX%)

ACCEPT sibusername PROMPT 'Enter SIB owner name (to be created if doesn''t exist): '
ACCEPT sibuserpassword PROMPT 'Enter SIB owner password (will not be changed if user exists): '
ACCEPT taxusername PROMPT 'Enter assigned TAX user name (privileges on SIB objects to be granted): '

set long 32767 pagesize 0 linesize 4000 echo off trims on heading off termout off

COLUMN exectime  NEW_VALUE script_exectime
select to_char(systimestamp, 'YYYY_MM_DD_HH24_MI_SS') as exectime from dual;

spool &sibusername._&script_exectime..log

--Создание пользователя
create user &sibusername identified by &sibuserpassword;
grant unlimited tablespace to &sibusername;

drop table &sibusername..SIB000;
drop table &sibusername..SIB001;
drop table &sibusername..SIB002;
drop table &sibusername..SIBCLASSMAP;
drop table &sibusername..SIBKEYS;
drop table &sibusername..SIBLISTING;
drop table &sibusername..SIBOWNER;
drop table &sibusername..SIBOWNERO;
drop table &sibusername..SIBXACTS;

--Создание объектов (таблицы + индексы)
CREATE TABLE &sibusername..SIBOWNER (
  ME_UUID VARCHAR(16),
  INC_UUID VARCHAR(16),
  VERSION INTEGER,
  MIGRATION_VERSION INTEGER,
  ME_LUTS TIMESTAMP,
  ME_INFO VARCHAR(254),
  ME_STATUS VARCHAR(16)
);

CREATE TABLE &sibusername..SIBOWNERO (
   EMPTY_COLUMN INTEGER
);

CREATE TABLE &sibusername..SIBCLASSMAP (CLASSID INTEGER NOT NULL, URI VARCHAR2(2048) NOT NULL, PRIMARY KEY(CLASSID));

CREATE TABLE &sibusername..SIBLISTING (
  ID INTEGER NOT NULL,
  SCHEMA_NAME VARCHAR2(10),
  TABLE_NAME VARCHAR2(10) NOT NULL,
  TABLE_TYPE CHAR(1) NOT NULL,
  PRIMARY KEY(ID)
);

CREATE TABLE &sibusername..SIB000 (
  ID NUMBER(19) NOT NULL,
  STREAM_ID NUMBER(19) NOT NULL,
  TYPE CHAR(2),
  EXPIRY_TIME NUMBER(19),
  STRATEGY INTEGER,
  REFERENCE NUMBER(19),
  CLASS_ID INTEGER NOT NULL,
  PRIORITY INTEGER,
  SEQUENCE NUMBER(19),
  PERMANENT_ID INTEGER,
  TEMPORARY_ID INTEGER,
  LOCK_ID NUMBER(19),
  DATA_SIZE INTEGER NOT NULL,
  LONG_DATA BLOB,
  XID VARCHAR(254),
  DELETED SMALLINT,
  REDELIVERED_COUNT INTEGER,
  PRIMARY KEY(ID)
) LOB(LONG_DATA) STORE AS (CACHE STORAGE(INITIAL 10M NEXT 10M));

CREATE INDEX &sibusername..SIB000STREAMIX ON
&sibusername..SIB000(STREAM_ID,SEQUENCE);

CREATE TABLE &sibusername..SIB001 (
  ID NUMBER(19) NOT NULL,
  STREAM_ID NUMBER(19) NOT NULL,
  TYPE CHAR(2),
  EXPIRY_TIME NUMBER(19),
  STRATEGY INTEGER,
  REFERENCE NUMBER(19),
  CLASS_ID INTEGER NOT NULL,
  PRIORITY INTEGER,
  SEQUENCE NUMBER(19),
  PERMANENT_ID INTEGER,
  TEMPORARY_ID INTEGER,
  LOCK_ID NUMBER(19),
  DATA_SIZE INTEGER NOT NULL,
  LONG_DATA BLOB,
  XID VARCHAR(254),
  DELETED SMALLINT,
  REDELIVERED_COUNT INTEGER,
  PRIMARY KEY(ID)
) LOB(LONG_DATA) STORE AS (CACHE STORAGE(INITIAL 10M NEXT 10M));

CREATE INDEX &sibusername..SIB001STREAMIX ON &sibusername..SIB001(STREAM_ID,SEQUENCE);

CREATE TABLE &sibusername..SIB002 (
  ID NUMBER(19) NOT NULL,
  STREAM_ID NUMBER(19) NOT NULL,
  TYPE CHAR(2),
  EXPIRY_TIME NUMBER(19),
  STRATEGY INTEGER,
  REFERENCE NUMBER(19),
  CLASS_ID INTEGER NOT NULL,
  PRIORITY INTEGER,
  SEQUENCE NUMBER(19),
  PERMANENT_ID INTEGER,
  TEMPORARY_ID INTEGER,
  LOCK_ID NUMBER(19),
  DATA_SIZE INTEGER NOT NULL,
  LONG_DATA BLOB,
  XID VARCHAR(254),
  DELETED SMALLINT,
  REDELIVERED_COUNT INTEGER,
  PRIMARY KEY(ID)
) LOB(LONG_DATA) STORE AS (CACHE STORAGE(INITIAL 10M NEXT 10M));

CREATE INDEX &sibusername..SIB002STREAMIX ON
&sibusername..SIB002(STREAM_ID,SEQUENCE);

CREATE TABLE &sibusername..SIBXACTS (
  XID VARCHAR2(254) NOT NULL,
  STATE CHAR(1) NOT NULL,
  PRIMARY KEY(XID)
);

CREATE TABLE &sibusername..SIBKEYS (
  ID VARCHAR2(50) NOT NULL,
  LAST_KEY NUMBER(19) NOT NULL,
  PRIMARY KEY(ID)
);

--Раздача слонов
GRANT SELECT,INSERT,UPDATE ON &sibusername..SIBOWNER TO &taxusername;
GRANT SELECT,INSERT,UPDATE ON &sibusername..SIBOWNERO TO &taxusername;
GRANT SELECT,INSERT ON &sibusername..SIBCLASSMAP TO &taxusername;
GRANT SELECT,INSERT ON &sibusername..SIBLISTING TO &taxusername;
GRANT SELECT,INSERT,DELETE,UPDATE ON &sibusername..SIB000 TO &taxusername;
GRANT SELECT,INSERT,DELETE,UPDATE ON &sibusername..SIB001 TO &taxusername;
GRANT SELECT,INSERT,DELETE,UPDATE ON &sibusername..SIB002 TO &taxusername;
GRANT SELECT,INSERT,UPDATE,DELETE ON &sibusername..SIBXACTS TO &taxusername;
GRANT SELECT,INSERT,UPDATE ON &sibusername..SIBKEYS TO &taxusername;

spool off;

EXIT;