@ECHO OFF
CLS
REM SET BIN=c:\Windows\Microsoft.NET\Framework\v2.0.50727\
REM SET BIN=c:\Windows\Microsoft.NET\Framework\v3.5\
SET BIN=c:\Windows\Microsoft.NET\Framework\v4.0.30319\
SET TARGET_NAME=VSAX3.exe

ECHO ----------------------
ECHO Compile:
ECHO ----------------------
"%BIN%\csc.exe" /out:%TARGET_NAME% /reference:VSAX3.dll Schematron.cs

REM goto :end
IF %errorlevel% EQU 0 (
  ECHO ----------------------
  ECHO Run: 12_1 Syntax1 SUCCESS
  ECHO ---------------------- 
  %TARGET_NAME% "./sample/NO_NDS.12_1_1_0212345678020012345_20140331_1.xml" "./sample/NO_NDS.12_1_003_07_05_04_01.xsd"

  ECHO ----------------------
  ECHO Run: 12_1 Syntax2 FAIL
  ECHO ---------------------- 
  %TARGET_NAME% "./sample/NO_NDS.12_1_1_0212345678020012345_20140331_1.xml" "./sample/NO_NDS.12_1_003_07_05_04_01.xsd" "wrong_name"

  ECHO ----------------------
  ECHO Run: 12_1 Syntax3 SUCCESS
  ECHO ---------------------- 
  %TARGET_NAME% "./sample/NO_NDS.12_1_1_0212345678020012345_20140331_1.xml" "./sample/NO_NDS.12_1_003_07_05_04_01.xsd" "NO_NDS.12_1_1_0212345678020012345_20140331_1.xml"
)
:end
pause