@echo off
set rootWAS="C:\IBM_TEST\WebSphere\AppServer"
set destWAShost=nalog-as2.aplana.local
set destWASport=8880
set destWASuser=wasadmin2
set destWASpass=wasadmin2
rem set fullPathToJythonFiles="C:\IBM_TEST\"

cd %~dp0
call %rootWAS%\bin\wsadmin.bat -host %destWAShost% -port %destWASport% -user %destWASuser% -password %destWASpass% -lang jython -f 01-CreateJDBC-J2C.py
call %rootWAS%\bin\wsadmin.bat -host %destWAShost% -port %destWASport% -user %destWASuser% -password %destWASpass% -lang jython -f 02-CreateCache.py
call %rootWAS%\bin\wsadmin.bat -host %destWAShost% -port %destWASport% -user %destWASuser% -password %destWASpass% -lang jython -f 03-CreateBus-JMS.py
rem %rootWAS%\bin\wsadmin.bat -host %destWAShost% -port %destWASport% -user %destWASuser% -password %destWASpass% -lang jython -f 04-CreateJMSMQ.py