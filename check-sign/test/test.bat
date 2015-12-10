@ECHO OFF
SET exe=../check-sign.exe
ECHO --------------------
ECHO Test-1 SUCCESS
ECHO --------------------
"%exe%" sign.dat 0 testfile
ECHO --------------------
ECHO Test-2 FAIL
ECHO --------------------
"%exe%" sign2.dat 0 testfile2
ECHO --------------------
ECHO Test-3 BOK FAIL
ECHO --------------------
"%exe%" abc 0 testfile2
ECHO --------------------
ECHO Test-4 Checkfile FAIL
ECHO --------------------
"%exe%" sign.dat 0 abc
ECHO --------------------
ECHO Test-5 Delflag FAIL
ECHO --------------------
"%exe%" sign.dat 4 abc
ECHO --------------------
ECHO Test-6 No sign FAIL
ECHO --------------------
"%exe%" testfile 0 sign.dat
ECHO --------------------
@PAUSE