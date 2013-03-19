<%@ page contentType="text/html; charset=UTF-8" %>
<!doctype html>
<%--
	The DOCTYPE declaration above will set the browser's rendering engine into "Standards Mode". 
	Replacing this declaration with a "Quirks Mode" doctype may lead to some differences in layout. 
--%>

<html>
<head>
	<meta http-equiv="content-type" content="text/html; charset=UTF-8">
	<%--
		Consider inlining CSS to reduce the number of requested files
	--%>
	<link type="text/css" rel="stylesheet" href="Main.css">
	<link type="text/css" rel="stylesheet" href="WidgetCustomization.css">
	<link type="text/css" rel="stylesheet" href="resources/codemirror-2.36/lib/codemirror.css">
	<%-- Code Mirror 2 dependencies. --%>
	<script type="text/javascript" src="resources/codemirror-2.36/lib/codemirror.js"></script>
	<script type="text/javascript" src="resources/codemirror-2.36/mode/groovy/groovy.js"></script>
	<%-- Any title is fine --%>
	<title>АС "Учёт налогов"</title>
	<link rel="icon" href="favicon.ico" type="image/vnd.microsoft.icon" />  
	<link rel="shortcut icon" href="favicon.ico" type="image/vnd.microsoft.icon" />  
	<%-- This script loads your compiled module. If you add any GWT meta tags, they must be added before this line. --%>
	<script type="text/javascript" src="Main/Main.nocache.js"></script>                                          
</head>
<body>
	<%-- OPTIONAL: include this if you want history support --%>
	<iframe src="javascript:''" id="__gwt_historyFrame" tabIndex='-1'
		style="position: absolute; width: 0; height: 100%; min-height: 100%; border: 0"></iframe>
	<%-- RECOMMENDED if your web app will not function without JavaScript enabled --%>
	<noscript>
		<div
			style="width: 22em; position: absolute; left: 50%; margin-left: -11em; color: red; background-color: white; border: 1px solid red; padding: 4px;">
			Your web browser must have JavaScript enabled in order for this
			application to display correctly.</div>
	</noscript>
</body>
</html>
