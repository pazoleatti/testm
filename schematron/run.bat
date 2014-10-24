@ECHO OFF
CLS
REM SET BIN=c:\Windows\Microsoft.NET\Framework\v2.0.50727\
REM SET BIN=c:\Windows\Microsoft.NET\Framework\v3.5\
SET BIN=c:\Windows\Microsoft.NET\Framework\v4.0.30319\
SET TARGET_NAME=VSAX3.exe

ECHO ----------------------
ECHO Compile:
ECHO ----------------------
REM "%BIN%\csc.exe" /out:%TARGET_NAME% /reference:VSAX3.dll Schematron.cs
"%bin%\csc.exe" /out:%TARGET_NAME% *.cs VSAX3\*.cs VSAX3\Properties\*.cs VSAX3\Schematron\*.cs VSAX3\SnpLibrary\*.cs /res:VSAX3.Schematron.xsl1.xsl /res:VSAX3.Schematron.xsl2.xsl /res:VSAX3.Schematron.xsl3.xsl /res:VSAX3.Schematron.xsl4.xsl

REM goto :end
IF %errorlevel% EQU 0 (
  ECHO ----------------------
  ECHO Run: 12_1
  ECHO ---------------------- 
  %TARGET_NAME% "./sample/NO_NDS.12_1_1_0212345678020012345_20140331_1.xml" "./sample/NO_NDS.12_1_003_07_05_04_01.xsd"
)
:end
pause