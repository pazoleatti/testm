@echo off

cd %~dp0
call settings.bat
call %rootWAS%\bin\wsadmin.bat -host %destWAShost% -port %destWASport% -user %destWASuser% -password %destWASpass% -lang jython -f 03-CreateBus-JMS.py