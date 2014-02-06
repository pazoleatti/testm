rootWAS = "C:\IBM\WebSphere\AppServer\"
destWAShost = nalog-as2.aplana.local
destWASport = 8880
destWASuser = wasadmin2
destWASpass = wasadmin2
fullPathToJythonFile = "c:\AppDeploy.py"

%rootWAS%\bin\wsadmin.bat -host %destWAShost% -port %destWASport% -user %destWASuser% -password %destWASpass% -lang jython -f %fullPathToJythonFile%