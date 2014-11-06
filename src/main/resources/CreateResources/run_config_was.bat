@ECHO OFF

CALL settings.bat
CD "%workplace%"

CALL "%rootWAS%\bin\wsadmin.bat" -javaoption -Xmx%maxHeapSize% -host %destWAShost% -port %destWASport% -user %destWASuser% -password %destWASpass% -lang jython -f "%workplace%\01-CreateJDBC-J2C.py" -profile "%workplace%\config_was.cfg" "%workplaceSlash%"
IF %ERRORLEVEL% NEQ 0 (
	ECHO *** ERROR in the script
	PAUSE
	EXIT
)

CALL "%rootWAS%\bin\wsadmin.bat" -javaoption -Xmx%maxHeapSize% -host %destWAShost% -port %destWASport% -user %destWASuser% -password %destWASpass% -lang jython -f "%workplace%\02-CreateCache.py" -profile "%workplace%\config_was.cfg" "%workplaceSlash%"
IF %ERRORLEVEL% NEQ 0 (
	ECHO *** ERROR in the script
	PAUSE
	EXIT
)

CALL "%rootWAS%\bin\wsadmin.bat" -javaoption -Xmx%maxHeapSize% -host %destWAShost% -port %destWASport% -user %destWASuser% -password %destWASpass% -lang jython -f "%workplace%\03-CreateBus-JMS.py" -profile "%workplace%\config_was.cfg" "%workplaceSlash%"
IF %ERRORLEVEL% NEQ 0 (
	ECHO *** ERROR in the script
	PAUSE
	EXIT
)

CALL "%rootWAS%\bin\wsadmin.bat" -javaoption -Xmx%maxHeapSize% -host %destWAShost% -port %destWASport% -user %destWASuser% -password %destWASpass% -lang jython -f "%workplace%\04-CreateJMSMQ.py" -profile "%workplace%\config_was.cfg" "%workplaceSlash%"
IF %ERRORLEVEL% NEQ 0 (
	ECHO *** ERROR in the script
	PAUSE
	EXIT
)

CALL "%rootWAS%\bin\wsadmin.bat" -javaoption -Xmx%maxHeapSize% -host %destWAShost% -port %destWASport% -user %destWASuser% -password %destWASpass% -lang jython -f "%workplace%\99-AppDeploy.py" -profile "%workplace%\config_was.cfg" "%workplaceSlash%"
IF %ERRORLEVEL% NEQ 0 (
	ECHO *** ERROR in the script
	PAUSE
	EXIT
)

PAUSE
