@echo off
set rootWAS="C:\IBM\WebSphere\AppServer8.5"
set destWAShost=nalog-as1.aplana.local
set destWASport=8880
set destWASuser=wasadmin2
set destWASpass=wasadmin2
set fullPathToJythonFile="C:\CreateResources.py"
rem set fullPathToJythonFile="C:\AppDeploy.py"

%rootWAS%\bin\wsadmin.bat -host %destWAShost% -port %destWASport% -user %destWASuser% -password %destWASpass% -lang jython -f %fullPathToJythonFile%