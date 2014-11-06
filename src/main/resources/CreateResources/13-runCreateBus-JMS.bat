@ECHO OFF

CALL settings.bat
CD "%workplace%"

CALL "%rootWAS%\bin\wsadmin.bat" -javaoption -Xmx%maxHeapSize% -host %destWAShost% -port %destWASport% -user %destWASuser% -password %destWASpass% -lang jython -f "%workplace%\03-CreateBus-JMS.py" -profile "%workplace%\config_was.cfg" "%workplaceSlash%"
PAUSE
