@ECHO OFF
CLS
SET BIN=c:\Windows\Microsoft.NET\Framework\v4.0.30319\
SET TARGET_NAME=check-sign.exe

ECHO ----------------------
ECHO Compile:
ECHO ----------------------
"%bin%\csc.exe" /out:%TARGET_NAME% *.cs

IF %errorlevel% EQU 0 (
  ECHO ----------------------
  ECHO TEST 1 true
  ECHO ---------------------- 
  %TARGET_NAME% ./test/1/sign.dat ./test/dll 0 ./test/1/testfile

  ECHO ----------------------
  ECHO TEST 2 false
  ECHO ---------------------- 
  %TARGET_NAME% ./test/2/sign.dat ./test/dll 0 ./test/2/testfile
  
  ECHO ----------------------
  ECHO TEST 3 true
  ECHO ---------------------- 
  %TARGET_NAME% ./test/3/sign.dat ./test/dll 0 ./test/3/testfile
)
PAUSE